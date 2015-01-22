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
import org.wso2.carbon.issue.tracker.bean.Project;
import org.wso2.carbon.issue.tracker.dao.ProjectDAO;
import org.wso2.carbon.issue.tracker.util.DBConfiguration;
import org.wso2.carbon.issue.tracker.util.ISQLConstants;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link ProjectDAO}
 */
public class ProjectDAOImpl implements ProjectDAO {

    private static Logger logger = Logger.getLogger(ProjectDAOImpl.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public int add(Project project) throws SQLException {

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        boolean isInserted = false;
        String insertTableSQL = ISQLConstants.ADD_PROJECT;

        int projectId = 0;
        try {
            dbConnection = DBConfiguration.getDBConnection();
            preparedStatement = dbConnection.prepareStatement(insertTableSQL, Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setString(1, project.getName());
            preparedStatement.setString(2, project.getOwner());
            preparedStatement.setString(3, project.getDescription());
            preparedStatement.setInt(4, project.getOrganizationId());
            preparedStatement.setString(5, project.getKey());

            // execute insert SQL stetement
            isInserted = preparedStatement.executeUpdate() == 1 ? true : false;

            rs = preparedStatement.getGeneratedKeys();
            rs.next();
            projectId = rs.getInt(1);

        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }
        }
        return projectId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean update(Project project) throws SQLException {

        String updateTableSQL = ISQLConstants.UPDATE_PROJECT;
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        int effectedRows = -1;
        try {
            dbConnection = DBConfiguration.getDBConnection();
            preparedStatement = dbConnection.prepareStatement(updateTableSQL);

            preparedStatement.setString(1, project.getName());
            preparedStatement.setString(2, project.getOwner());
            preparedStatement.setString(3, project.getDescription());
            preparedStatement.setString(4, project.getKey());
            preparedStatement.setInt(5, project.getOrganizationId());

            // execute update SQL stetement
            effectedRows = preparedStatement.executeUpdate();


        } catch (SQLException e) {

            logger.error(e.getMessage(), e);
            throw e;

        } finally {

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }

        }
        return effectedRows > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Project get(String key, int tenantId) throws SQLException {

        String selectSQL = ISQLConstants.GET_PROJECT;
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        Project project = null;

        try {
            dbConnection = DBConfiguration.getDBConnection();
            preparedStatement = dbConnection.prepareStatement(selectSQL);
            preparedStatement.setString(1, key);
            preparedStatement.setInt(2, tenantId);

            preparedStatement.setMaxRows(1);

            // execute select SQL stetement
            rs = preparedStatement.executeQuery();

            if (rs.first()) {

                project = new Project();
                project.setId(rs.getInt("PROJECT_ID"));
                project.setName(rs.getString("PROJECT_NAME"));
                project.setOwner(rs.getString("OWNER"));
                project.setDescription(rs.getString("DESCRIPTION"));
                project.setOrganizationId(rs.getInt("ORGANIZATION_ID"));

            }

        } catch (SQLException e) {

            logger.error(e.getMessage(), e);

        } finally {
            if (rs != null) {
                rs.close();
            }
            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }

        }

        return project;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Project> getProjectsByOrganizationId(int organizationId) throws SQLException {

        String selectSQL = ISQLConstants.GET_PROJECTS_BY_ORGANIZATION_ID;
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        List<Project> projects = new ArrayList<Project>();
        try {
            dbConnection = DBConfiguration.getDBConnection();
            preparedStatement = dbConnection.prepareStatement(selectSQL);
            preparedStatement.setInt(1, organizationId);

            // execute select SQL stetement
            rs = preparedStatement.executeQuery();

            while (rs.next()) {
                Project project = new Project();
                project.setId(rs.getInt("PROJECT_ID"));
                project.setName(rs.getString("PROJECT_NAME"));
                project.setOwner(rs.getString("OWNER"));
                project.setDescription(rs.getString("DESCRIPTION"));
                project.setOrganizationId(rs.getInt("ORGANIZATION_ID"));
                project.setKey(rs.getString("PROJECT_KEY"));
                projects.add(project);
            }

        } catch (SQLException e) {

            logger.error(e.getMessage(), e);

        } finally {
            if (rs != null) {
                rs.close();
            }

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }

        }

        return projects;
    }

    /**
     * Delete the project related to given application key with all the versions and issues related to the application
     *
     * @param key      application Key
     * @param tenantId Tenant ID
     * @return
     * @throws SQLException
     */
    @Override
    public boolean delete(String key, int tenantId) throws SQLException {
        String deleteComments = ISQLConstants.DELETE_COMMENTS_FOR_PROJECT_ISSUES;
        String deleteIssues = ISQLConstants.DELETE_ISSUES_OF_PROJECT;
        String deleteVersions = ISQLConstants.DELETE_VERSIONS_OF_PROJECT;
        String deleteProject = ISQLConstants.DELETE_PROJECT;
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        int affectedRows = -1;

        try {
            dbConnection = DBConfiguration.getDBConnection();

            // retrieving the project id which maps to the application key
            if (key != null) {
                Project project = get(key, tenantId);
                if (project != null) {
                    int projectId = project.getId();

                    // deleting the comments in the issues related to the given project
                    preparedStatement = dbConnection.prepareStatement(deleteComments);
                    preparedStatement.setString(1, String.valueOf(projectId));
                    preparedStatement.executeUpdate();

                    // deleting the issues related to the project
                    preparedStatement = dbConnection.prepareStatement(deleteIssues);
                    preparedStatement.setString(1, String.valueOf(projectId));
                    preparedStatement.executeUpdate();

                    // deleting the versions of the project
                    preparedStatement = dbConnection.prepareStatement(deleteVersions);
                    preparedStatement.setString(1, String.valueOf(projectId));
                    preparedStatement.executeUpdate();

                    // deleting the project
                    preparedStatement = dbConnection.prepareStatement(deleteProject);
                    preparedStatement.setString(1, key);
                    preparedStatement.setInt(2, tenantId);

                    affectedRows = preparedStatement.executeUpdate();
                }
            }


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
        return affectedRows > 0;
    }
}
