/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *    WSO2 Inc. licenses this file to you under the Apache License,
 *    Version 2.0 (the "License"); you may not use this file except
 *    in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package org.wso2.carbon.issue.tracker.dao.impl;

import org.apache.log4j.Logger;
import org.wso2.carbon.issue.tracker.bean.SearchBean;
import org.wso2.carbon.issue.tracker.bean.SearchResponse;
import org.wso2.carbon.issue.tracker.dao.SearchDAO;
import org.wso2.carbon.issue.tracker.util.Constants;
import org.wso2.carbon.issue.tracker.util.DBConfiguration;
import org.wso2.carbon.issue.tracker.util.ISQLConstants;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link SearchDAO}
 */
public class SearchDAOImpl implements SearchDAO {
    private static Logger logger = Logger.getLogger(SearchDAOImpl.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SearchResponse> searchIssue(SearchBean searchBean) throws SQLException {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        List<SearchResponse> resultList = new ArrayList<SearchResponse>();

//        StringBuilder query = new StringBuilder();

        String selectSQL = ISQLConstants.SEARCH_ISSUE;

        try {
            dbConnection = DBConfiguration.getDBConnection();
            preparedStatement = dbConnection.prepareStatement(selectSQL);

            int searchType = searchBean.getSearchType();
            String searchValue = searchBean.getSearchValue();

            String projectName = null;
            String owner = null;
            String assignee = null;

            if (searchType == Constants.BY_PROJECT_NAME)
                projectName = searchValue;
            else if (searchType == Constants.BY_ASSIGNEE)
                assignee = searchValue;
            else if (searchType == Constants.BY_OWNER)
                owner = searchValue;

            if (projectName != null && !projectName.equals(""))
                preparedStatement.setString(1,   projectName );
            else
                preparedStatement.setNull(1, Types.VARCHAR);

            preparedStatement.setString(2, searchBean.getIssueStatus());

            if (owner != null && !owner.equals(""))
                preparedStatement.setString(3, "%" + owner + "%");
            else
                preparedStatement.setNull(3, Types.VARCHAR);

            if (assignee != null && !assignee.equals(""))
                preparedStatement.setString(4, "%" + assignee + "%");
            else
                preparedStatement.setNull(4, Types.VARCHAR);

            preparedStatement.setString(5, searchBean.getIssueType());
            preparedStatement.setString(6, searchBean.getPriority());
            preparedStatement.setString(7, searchBean.getSeverity());
            preparedStatement.setInt(8, searchBean.getOrganizationId());

            // execute select SQL statement
            rs = preparedStatement.executeQuery();

            while (rs.next()) {
                SearchResponse response = new SearchResponse();
                response.setProjectKey(rs.getString("PROJECT_KEY"));
                response.setIssuePkey(rs.getString("PKEY"));
                response.setSummary(rs.getString("SUMMARY"));
                response.setIssueType(rs.getString("ISSUE_TYPE"));
                response.setPriority(rs.getString("PRIORITY"));
                response.setOwner(rs.getString("OWNER"));
                response.setStatus(rs.getString("STATUS"));
                response.setAssignee(rs.getString("ASSIGNEE"));
                response.setSeverity(rs.getString("SEVERITY"));

                response.setVersion(rs.getString("VERSION"));
                response.setProjectId(rs.getInt("PROJECT_ID"));
                response.setProjectName(rs.getString("PROJECT_NAME"));
                resultList.add(response);

            }

        } catch (SQLException e) {
            String msg = "Error while getting comment from DB, issueID: ";
            logger.error(msg, e);
            throw e;
        } finally {
            if (rs != null){
                rs.close();
            }

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }

        }
        return resultList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SearchResponse> searchIssueBySummaryContent(SearchBean searchBean) throws SQLException {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        List<SearchResponse> resultList = new ArrayList<SearchResponse>();

        String selectSQL = ISQLConstants.SEARCH_ISSUE_BY_SUMMARY_CONTENT;

        try {
            dbConnection = DBConfiguration.getDBConnection();
            preparedStatement = dbConnection.prepareStatement(selectSQL);

            String searchValue = "%" + searchBean.getSearchValue() + "%";
            String status = searchBean.getIssueStatus();

            preparedStatement.setString(1, searchValue);
            preparedStatement.setString(2, searchValue);
            preparedStatement.setString(3, searchBean.getIssueStatus());
            preparedStatement.setString(4, searchBean.getIssueType());
            preparedStatement.setString(5, searchBean.getPriority());
            preparedStatement.setString(6, searchBean.getSeverity());
            preparedStatement.setInt(7, searchBean.getOrganizationId());

            // execute select SQL statement
            rs = preparedStatement.executeQuery();

            while (rs.next()) {
                SearchResponse response = new SearchResponse();
                response.setIssuePkey(rs.getString("PKEY"));
                response.setSummary(rs.getString("SUMMARY"));
                response.setIssueType(rs.getString("ISSUE_TYPE"));
                response.setPriority(rs.getString("PRIORITY"));
                response.setOwner(rs.getString("OWNER"));
                response.setStatus(rs.getString("STATUS"));
                response.setAssignee(rs.getString("ASSIGNEE"));
                response.setSeverity(rs.getString("SEVERITY"));

                response.setVersion(rs.getString("VERSION"));
                response.setProjectId(rs.getInt("PROJECT_ID"));
                response.setProjectName(rs.getString("PROJECT_NAME"));
                response.setProjectKey(rs.getString("PROJECT_KEY"));
                resultList.add(response);
            }

        } catch (SQLException e) {
            String msg = "Error while getting comment from DB, issueID: ";
            logger.error(msg, e);
            throw e;
        } finally {
            if (rs != null){
                rs.close();
            }
            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }

        }
        return resultList;
    }


}
