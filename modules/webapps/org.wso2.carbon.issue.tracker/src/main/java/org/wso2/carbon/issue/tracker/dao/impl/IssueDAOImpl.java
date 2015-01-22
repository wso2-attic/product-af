/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.issue.tracker.dao.impl;

import org.apache.cxf.common.util.StringUtils;
import org.apache.log4j.Logger;
import org.wso2.carbon.appfactory.eventing.AppFactoryEventException;
import org.wso2.carbon.appfactory.eventing.builder.utils.IssueTrackerEventBuilderUtil;
import org.wso2.carbon.issue.tracker.bean.Issue;
import org.wso2.carbon.issue.tracker.bean.IssueResponse;
import org.wso2.carbon.issue.tracker.dao.IssueDAO;
import org.wso2.carbon.issue.tracker.util.Constants;
import org.wso2.carbon.issue.tracker.util.DBConfiguration;
import org.wso2.carbon.issue.tracker.util.ISQLConstants;

import org.wso2.carbon.appfactory.eventing.EventNotifier;
import org.wso2.carbon.issue.tracker.util.TenantUtils;
import org.wso2.carbon.user.api.UserStoreException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;



public class IssueDAOImpl implements IssueDAO {
    Logger logger = Logger.getLogger(IssueDAOImpl.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public String add(Issue issue, String projectKey, int tenantId) throws SQLException {
        String issueKey = null;
        PreparedStatement st = null;
        Connection dbConnection = null;
        ResultSet rs = null;
        try {
            dbConnection = DBConfiguration.getDBConnection();

            String query = ISQLConstants.ADD_ISSUE;

            st = (PreparedStatement) dbConnection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            st.setString(1, issue.getSummary());
            st.setString(2, issue.getDescription());
            st.setString(3, issue.getType());
            st.setString(4, issue.getPriority());
            st.setString(5, issue.getReporter());
            st.setString(6, issue.getStatus());
            st.setString(7, issue.getAssignee());
            if (issue.getVersionId() > 0)
                st.setInt(8, issue.getVersionId());
            else
                st.setNull(8, Types.INTEGER);


            st.setTimestamp(9, getCurrentTimeStamp());
            st.setString(10, issue.getSeverity());
            st.setString(11, projectKey);
            st.setInt(12, tenantId);

            int id = st.executeUpdate();

            rs = st.getGeneratedKeys();
            rs.next();
            id = rs.getInt(1);

            st.close();

            if (id > 0) {
                String query2 = ISQLConstants.UPDATE_ISSUE_PKEY;
                st = (PreparedStatement) dbConnection.prepareStatement(query2);

                issueKey = projectKey + "-" + id;

                st.setString(1, issueKey);
                st.setInt(2, id);

                st.executeUpdate();
            }

        } catch (SQLException e) {
            logger.info("Error occurred while creating the issue " + issue.getId()
                    + " " + e.getMessage());
            throw e;
        } finally {
            if (rs != null) {
                rs.close();
            }
            closeStatement(st, dbConnection);
        }

        try {
            String appKey = projectKey.split("-")[0];
            String issueReporter = issue.getReporter() + "@" + TenantUtils.getTenantDomain(tenantId);
            String notificationTitle = "Issue " + projectKey +  " created by " + issue.getReporter();
            EventNotifier.getInstance().notify(IssueTrackerEventBuilderUtil.issueCreatedEvent(appKey, issueReporter, notificationTitle, "", "INFO"));
        } catch (AppFactoryEventException e) {
            logger.error("Failed to notify the Service Deploy deployment success event " + e.getMessage(), e);
        } catch (UserStoreException e) {
            logger.error("Failed to get the issue reporter " + e.getMessage(), e);
        }

        return issueKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean update(Issue issue, int tenantId) throws SQLException {
        boolean result = false;
        PreparedStatement st = null;
        Connection dbConnection = null;
        try {

            dbConnection = DBConfiguration.getDBConnection();

            st = (PreparedStatement) dbConnection
                    .prepareStatement(ISQLConstants.UPDATE_ISSUE);

            st.setString(1, issue.getDescription());
            st.setString(2, issue.getType());
            st.setString(3, issue.getPriority());
            st.setString(4, issue.getStatus());
            st.setString(5, issue.getAssignee());
            if (issue.getVersionId() > 0)
                st.setInt(6, issue.getVersionId());
            else
                st.setNull(6, Types.INTEGER);

            st.setTimestamp(7, getCurrentTimeStamp());
            st.setString(8, issue.getSeverity());
            st.setString(9, issue.getKey());
            st.setInt(10, tenantId);

            result = st.executeUpdate() == 1 ? true : false;
        } catch (SQLException e) {
            logger.info("Error occurred while updating the issue " + issue.getId()
                    + " " + e.getMessage());

            throw e;
        } finally {
            closeStatement(st, dbConnection);
        }
        return result;
    }

    private void closeStatement(PreparedStatement st, Connection dbConnection) {
        try {
            if (st != null) {
                st.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }
        } catch (SQLException ignore) {

        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public IssueResponse getIssueByKey(String uniqueKey, int tenantId) throws SQLException {

        PreparedStatement st = null;
        Connection dbConnection = null;
        ResultSet rs = null;
        IssueResponse issueResponse = new IssueResponse();
        try {
            dbConnection = DBConfiguration.getDBConnection();

            st = (PreparedStatement) dbConnection
                    .prepareStatement(ISQLConstants.GET_ISSUE_BY_KEY);
            st.setMaxRows(1);
            st.setString(1, uniqueKey);
            st.setInt(2, tenantId);

            rs = st.executeQuery();
            if (rs.first()) {

                Issue issue = new Issue();
                issueResponse.setProjectKey(rs.getString("PROJECT_KEY"));
                issueResponse.setProjectName(rs.getString("PROJECT_NAME"));

                issue.setId(rs.getInt("ISSUE_ID"));
                issue.setKey(rs.getString("PKEY"));
                issue.setProjectId(rs.getInt("PROJECT_ID"));
                issue.setSummary(rs.getString("SUMMARY"));
                issue.setDescription(rs.getString("DESCRIPTION"));
                issue.setType(rs.getString("ISSUE_TYPE"));
                issue.setPriority(rs.getString("PRIORITY"));
                issue.setReporter(rs.getString("OWNER"));
                issue.setStatus(rs.getString("STATUS"));
                issue.setAssignee(rs.getString("ASSIGNEE"));
                issue.setVersionId(rs.getInt("VERSION_ID"));
                issue.setSeverity(rs.getString("SEVERITY"));

                Timestamp createdTime = rs.getTimestamp("CREATED_TIME");
                String createdTimeStr = Constants.DATE_FORMAT
                        .format(createdTime);
                issue.setCreatedTime(createdTimeStr);

                Timestamp updatedTime = rs.getTimestamp("UPDATED_TIME");
                if (updatedTime != null) {
                    String updatedTimeStr = Constants.DATE_FORMAT
                            .format(updatedTime);
                    issue.setUpdatedTime(updatedTimeStr);
                }
                issueResponse.setIssue(issue);

            }

        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            throw e;

        } finally {
            if (rs != null){
                rs.close();
            }
            closeStatement(st, dbConnection);

        }
        return issueResponse;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Issue getIssueById(int id, int tenantId) throws SQLException {

        PreparedStatement st = null;
        Connection dbConnection = null;
        ResultSet rs = null;
        Issue issue = null;
        try {
            dbConnection = DBConfiguration.getDBConnection();

            st = (PreparedStatement) dbConnection
                    .prepareStatement(ISQLConstants.GET_ISSUE_BY_ID);
            st.setMaxRows(1);
            st.setInt(1, id);
            st.setInt(2, tenantId);

            rs = st.executeQuery();
            if (rs.first()) {

                issue = new Issue();
                issue.setId(rs.getInt("ISSUE_ID"));
                issue.setKey(rs.getString("PKEY"));
                issue.setProjectId(rs.getInt("PROJECT_ID"));
                issue.setSummary(rs.getString("SUMMARY"));
                issue.setDescription(rs.getString("DESCRIPTION"));
                issue.setType(rs.getString("ISSUE_TYPE"));
                issue.setPriority(rs.getString("PRIORITY"));
                issue.setPriority(rs.getString("OWNER"));
                issue.setStatus(rs.getString("STATUS"));
                issue.setAssignee(rs.getString("ASSIGNEE"));
                issue.setVersionId(rs.getInt("VERSION_ID"));
                issue.setSeverity(rs.getString("SEVERITY"));

                Timestamp createdTime = rs.getTimestamp("CREATED_TIME");
                String createdTimeStr = Constants.DATE_FORMAT
                        .format(createdTime);
                issue.setCreatedTime(createdTimeStr);

                Timestamp updatedTime = rs.getTimestamp("UPDATED_TIME");
                String updatedTimeStr = Constants.DATE_FORMAT
                        .format(updatedTime);
                issue.setUpdatedTime(updatedTimeStr);

            }

        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            throw e;

        } finally {
            if (rs != null){
                rs.close();
            }
            closeStatement(st, dbConnection);

        }
        return issue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IssueResponse> getAllIssuesOfProjectVersion(String projectKey, int tenantId, String version) throws SQLException {
        PreparedStatement st = null;
        Connection dbConnection = null;
        ResultSet rs = null;
        List<IssueResponse> issueList = new ArrayList<IssueResponse>();

        try {
            dbConnection = DBConfiguration.getDBConnection();

            st = (PreparedStatement) dbConnection
                    .prepareStatement(ISQLConstants.GET_ALL_ISSUE_OF_PROJECT);
            if (StringUtils.isEmpty(projectKey))
                st.setNull(1, Types.INTEGER);
            else
                st.setString(1, projectKey);

            st.setInt(2, tenantId);

            if (version == null) {
                st.setString(3, "%");
            } else {
                st.setString(3, version);
            }

            rs = st.executeQuery();
            while (rs.next()) {
                Issue issue = new Issue();
                issue.setId(rs.getInt("ISSUE_ID"));
                issue.setKey(rs.getString("PKEY"));
                issue.setProjectId(rs.getInt("PROJECT_ID"));
                issue.setSummary(rs.getString("SUMMARY"));
                issue.setDescription(rs.getString("DESCRIPTION"));
                issue.setType(rs.getString("ISSUE_TYPE"));
                issue.setPriority(rs.getString("PRIORITY"));
                issue.setReporter(rs.getString("OWNER"));
                issue.setStatus(rs.getString("STATUS"));
                issue.setAssignee(rs.getString("ASSIGNEE"));
                issue.setVersionId(rs.getInt("VERSION_ID"));
                issue.setSeverity(rs.getString("SEVERITY"));

                IssueResponse issueResponse = new IssueResponse();

                issueResponse.setProjectName(rs.getString("PROJECT_NAME"));
                issueResponse.setVersion(rs.getString("VERSION"));

                Timestamp createdTime = rs.getTimestamp("CREATED_TIME");
                String createdTimeStr = Constants.DATE_FORMAT
                        .format(createdTime);
                issue.setCreatedTime(createdTimeStr);

                Timestamp updatedTime = rs.getTimestamp("UPDATED_TIME");
                if (updatedTime != null) {
                    String updatedTimeStr = Constants.DATE_FORMAT.format(updatedTime);
                    issue.setUpdatedTime(updatedTimeStr);
                }
                issueResponse.setIssue(issue);

                issueList.add(issueResponse);
            }

        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            throw e;

        } finally {
            if (rs != null){
                rs.close();
            }
            closeStatement(st, dbConnection);

        }
        return issueList;
    }

    @Override
    public int deleteIssuesOfProject(String projectId) throws SQLException {
        String selectSQL = ISQLConstants.DELETE_ISSUES_OF_PROJECT;
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        int affectedRows = -1;

        try {
            dbConnection = DBConfiguration.getDBConnection();
            preparedStatement = dbConnection.prepareStatement(selectSQL);
            preparedStatement.setString(1, projectId);

            affectedRows = preparedStatement.executeUpdate();
        } catch (SQLException e) {

            logger.error(e.getMessage(), e);

        } finally {
            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }
        }
        return affectedRows;
    }

    /**
     * Get current time to log DB
     *
     * @return {@link Timestamp}
     */
    private static Timestamp getCurrentTimeStamp() {
        java.util.Date today = new java.util.Date();
        return new Timestamp(today.getTime());
    }

}