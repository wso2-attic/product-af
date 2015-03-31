/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package org.wso2.carbon.appfactory.core.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.deploy.Artifact;
import org.wso2.carbon.appfactory.core.dto.Version;
import org.wso2.carbon.appfactory.core.sql.SQLConstants;
import org.wso2.carbon.appfactory.core.sql.SQLParameterConstants;
import org.wso2.carbon.appfactory.core.util.AppFactoryDBUtil;
import org.wso2.carbon.context.CarbonContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO Class for managing app version related CRUD operations
 */
public class JDBCAppVersionDAO {
    private static final Log log = LogFactory.getLog(JDBCAppVersionDAO.class);
    private static JDBCAppVersionDAO appVersionDAO = new JDBCAppVersionDAO();

    private JDBCAppVersionDAO() {
    }

    public static JDBCAppVersionDAO getInstance() {
        return appVersionDAO;
    }

    /**
     * Update method for promote status of an application version
     *
     * @param version        version number
     * @param status         status of promotion
     * @return true if it success false if it failed
     * @throws org.wso2.carbon.appfactory.common.AppFactoryException
     */
    public boolean updatePromoteStatusOfVersion(int autoIncrementAppId, String version,
                                                String status) throws AppFactoryException {
        Connection databaseConnection = null;
        PreparedStatement preparedStatement = null;
        try {
            databaseConnection = AppFactoryDBUtil.getConnection();
            preparedStatement = databaseConnection.prepareStatement(SQLConstants.UPDATE_PROMOTE_STATUS_OF_VERSION);
            preparedStatement.setString(1, status);
            preparedStatement.setInt(2, autoIncrementAppId);
            preparedStatement.setString(3, version);
            preparedStatement.execute();
            int affectedRows = preparedStatement.getUpdateCount();
            if (affectedRows > 0) {
                databaseConnection.commit();
                return true;
            }
            String msg = "Error while updating promote status of version " + version;
            log.error(msg);
            throw new AppFactoryException(msg);
        } catch (SQLException e) {
            try {
                if (databaseConnection != null) {
                    databaseConnection.rollback();
                }
            } catch (SQLException e1) {

                // Only logging this exception since this is not the main issue. The original issue is thrown.
                log.error("Error while rolling back update promote status of version " +
                        "" + version, e);
            }
            String msg = "Error while updating promote status of version " + version;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } finally {
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }
    }

    /**
     * Update method for subdomain of an application version
     *
     * @param version        version number
     * @param subdomain         sub domain
     * @return true if it success false if it failed
     * @throws org.wso2.carbon.appfactory.common.AppFactoryException
     */
    public boolean updateSubDomainsOfVersion(int autoIncrementAppId, String version,
                                                String subdomain) throws AppFactoryException {
        Connection databaseConnection = null;
        PreparedStatement preparedStatement = null;
        try {
            databaseConnection = AppFactoryDBUtil.getConnection();
            preparedStatement = databaseConnection.prepareStatement(SQLConstants.UPDATE_SUBDOMAIN_OF_VERSION);
            preparedStatement.setString(1, subdomain);
            preparedStatement.setInt(2, autoIncrementAppId);
            preparedStatement.setString(3, version);
            preparedStatement.execute();
            int affectedRows = preparedStatement.getUpdateCount();
            if (affectedRows > 0) {
                databaseConnection.commit();
                return true;
            }
            String msg = "Error while updating subdomain of version " + version;
            log.error(msg);
            throw new AppFactoryException(msg);
        } catch (SQLException e) {
            try {
                if (databaseConnection != null) {
                    databaseConnection.rollback();
                }
            } catch (SQLException e1) {

                // Only logging this exception since this is not the main issue. The original issue is thrown.
                log.error("Error while rolling back update subdomain of version " +
                        "" + version, e);
            }
            String msg = "Error while rolling back update subdomain of version " + version;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } finally {
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }
    }

    /**
     * Update method for auto build status of an application version
     *
     * @param version        version number
     * @param isAutoBuildable         Auto Build is set or not
     * @return true if it success false if it failed
     * @throws org.wso2.carbon.appfactory.common.AppFactoryException
     */
    public boolean updateAutoBuildStatusOfVersion(int autoIncrementAppId, String version,
                                             boolean isAutoBuildable) throws AppFactoryException {
        Connection databaseConnection = null;
        PreparedStatement preparedStatement = null;
        try {
            databaseConnection = AppFactoryDBUtil.getConnection();
            preparedStatement = databaseConnection.prepareStatement(SQLConstants.UPDATE_AUTO_BUILD_STATUS_OF_VERSION);
            preparedStatement.setInt(1, isAutoBuildable ? 1 : 0);
            preparedStatement.setInt(2, autoIncrementAppId);
            preparedStatement.setString(3, version);
            preparedStatement.execute();
            int affectedRows = preparedStatement.getUpdateCount();
            if (affectedRows > 0) {
                databaseConnection.commit();
                return true;
            }
            String msg = "Error while updating auto build status of version " + version;
            log.error(msg);
            throw new AppFactoryException(msg);
        } catch (SQLException e) {
            try {
                if (databaseConnection != null) {
                    databaseConnection.rollback();
                }
            } catch (SQLException e1) {

                // Only logging this exception since this is not the main issue. The original issue is thrown.
                log.error("Error while rolling back update auto build status of version " +
                        "" + version, e);
            }
            String msg = "Error while rolling back update auto build status of version " + version;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } finally {
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }
    }


    /**
     * Get auto build status of a given application version
     *
     * @param applicationKey applicationKey of application
     * @param version   version number
     * @return true or false for auto build status
     * @throws AppFactoryException
     */
    public boolean getAutoBuildStatusOfVersion(String applicationKey, String version) throws AppFactoryException {
        Connection databaseConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            databaseConnection = AppFactoryDBUtil.getConnection();
            preparedStatement = databaseConnection.prepareStatement(SQLConstants.GET_AUTO_BUILD_STATUS_OF_VERSION);
            preparedStatement.setString(1, applicationKey);
            preparedStatement.setInt(2, CarbonContext.getThreadLocalCarbonContext().getTenantId());
            preparedStatement.setString(3, version);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
               int value = resultSet.getInt(SQLParameterConstants.COLUMN_NAME_AUTO_BUILD);

                if (log.isDebugEnabled()) {
                    log.debug("Successfully received the auto build status of version " + version);
                }

               return value == 0 ? false : true;
            }
        } catch (SQLException e) {
            String msg = "Error while receiving the auto build status of version " + version;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } finally {
            AppFactoryDBUtil.closeResultSet(resultSet);
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }
        return true;
    }


    /**
     * Update method for auto deploy status of an application version
     *
     * @param version        version number
     * @param isAutoDeployable         Auto Deploy is set or not
     * @return true if it success false if it failed
     * @throws org.wso2.carbon.appfactory.common.AppFactoryException
     */
    public boolean updateAutoDeployStatusOfVersion(int autoIncrementAppId, String version,
                                                  boolean isAutoDeployable) throws AppFactoryException {
        Connection databaseConnection = null;
        PreparedStatement preparedStatement = null;
        try {
            databaseConnection = AppFactoryDBUtil.getConnection();
            preparedStatement = databaseConnection.prepareStatement(SQLConstants.UPDATE_AUTO_DEPLOY_STATUS_OF_VERSION);
            preparedStatement.setInt(1, isAutoDeployable ? 1 : 0);
            preparedStatement.setInt(2, autoIncrementAppId);
            preparedStatement.setString(3, version);
            preparedStatement.execute();
            int affectedRows = preparedStatement.getUpdateCount();
            if (affectedRows > 0) {
                databaseConnection.commit();
                return true;
            }
            String msg = "Error while updating auto deploy status of version " + version;
            log.error(msg);
            throw new AppFactoryException(msg);
        } catch (SQLException e) {
            try {
                if (databaseConnection != null) {
                    databaseConnection.rollback();
                }
            } catch (SQLException e1) {

                // Only logging this exception since this is not the main issue. The original issue is thrown.
                log.error("Error while rolling back update auto deploy status of version " +
                        "" + version, e);
            }
            String msg = "Error while rolling back update auto deploy status of version " + version;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } finally {
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }
    }


    /**
     * et auto deploy status of a given application version
     *
     * @param applicationKey applicationKey of application
     * @param version   version number
     * @return true or false for auto deploy status
     * @throws AppFactoryException
     */
    public boolean getAutoDeployStatusOfVersion(String applicationKey, String version) throws AppFactoryException {
        Connection databaseConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            databaseConnection = AppFactoryDBUtil.getConnection();
            preparedStatement = databaseConnection.prepareStatement(SQLConstants.GET_AUTO_DEPLOY_STATUS_OF_VERSION);
            preparedStatement.setString(1, applicationKey);
            preparedStatement.setInt(2, CarbonContext.getThreadLocalCarbonContext().getTenantId());
            preparedStatement.setString(3, version);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int value = resultSet.getInt(SQLParameterConstants.COLUMN_NAME_AUTO_DEPLOY);

                if (log.isDebugEnabled()) {
                    log.debug("Successfully received the auto deploy status of version " + version);
                }

                return value == 0 ? false : true;
            }
        } catch (SQLException e) {
            String msg = "Error while receiving the auto deploy status of version " + version;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } finally {
            AppFactoryDBUtil.closeResultSet(resultSet);
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }
        return true;
    }


    /**
     * Returns the stage of a particular application version
     *
     * @param version        version number
     * @param applicationKey    application key
     * @return true if it success false if it failed
     * @throws org.wso2.carbon.appfactory.common.AppFactoryException
     */
    public String getAppVersionStage(String applicationKey, String version) throws AppFactoryException {
        Connection databaseConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            databaseConnection = AppFactoryDBUtil.getConnection();
            preparedStatement = databaseConnection.prepareStatement(SQLConstants.GET_STAGE_OF_APPLICATION_VERSION);
            preparedStatement.setString(1, applicationKey);
            preparedStatement.setInt(2, CarbonContext.getThreadLocalCarbonContext().getTenantId());
            preparedStatement.setString(3, version);
            preparedStatement.execute();
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String stage = resultSet.getString(SQLParameterConstants.COLUMN_NAME_STAGE);

                if (log.isDebugEnabled()) {
                    log.debug("Successfully received the stage of version " + version + " for application " +
                            "key : " + applicationKey);
                }
                return stage;
            }
        } catch (SQLException e) {
            String msg = "Error while getting stage of version " + version + " for application " +
                    "key : " + applicationKey;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } finally {
            AppFactoryDBUtil.closeResultSet(resultSet);
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }
        return "";
    }

    /**
     * Get a {@link org.wso2.carbon.appfactory.core.dto.Version} of a given application and version name
     *
     * @param autoIncrementAppId auto increment value of application
     * @param versionName   version number
     * @return {@link org.wso2.carbon.appfactory.core.dto.Version}
     * @throws AppFactoryException
     */
    public Version getApplicationVersion(int autoIncrementAppId, String versionName) throws AppFactoryException {
        Connection databaseConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet allVersions = null;

        Version version = new Version();
        try {
            databaseConnection = AppFactoryDBUtil.getConnection();
            preparedStatement = databaseConnection.prepareStatement(SQLConstants.GET_APPLICATION_VERSION_SQL);
            preparedStatement.setInt(1, autoIncrementAppId);
            preparedStatement.setString(2, versionName);
            allVersions = preparedStatement.executeQuery();
            while (allVersions.next()) {
                version.setId(allVersions.getString(SQLParameterConstants.COLUMN_NAME_VERSION_NAME));
                version.setLifecycleStage(allVersions.getString(SQLParameterConstants.COLUMN_NAME_STAGE));
                version.setPromoteStatus(allVersions.getString(SQLParameterConstants.COLUMN_NAME_PROMOTE_STATUS));
            }
        } catch (SQLException e) {
            String msg = "Error while getting app version " + versionName;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } finally {
            AppFactoryDBUtil.closeResultSet(allVersions);
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }
        return version;
    }

    /**
     * This method is used to get a list of all {@link org.wso2.carbon.appfactory.core.deploy.Artifact} versions of the a application
     *
     * @param applicationKey The application applicationKey of the current application
     * @return {@link org.wso2.carbon.appfactory.core.deploy.Artifact}
     * @throws AppFactoryException if SQL operation fails
     */
    public List<Artifact> getAllVersionsOfApplication(String applicationKey) throws AppFactoryException {
        Connection databaseConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        List<Artifact> artifactList = new ArrayList<Artifact>(0);
        try {
            databaseConnection = AppFactoryDBUtil.getConnection();
            preparedStatement = databaseConnection.prepareStatement(SQLConstants.GET_ALL_VERSIONS_OF_APPLICATION);
            preparedStatement.setString(1, applicationKey);
            preparedStatement.setInt(2, CarbonContext.getThreadLocalCarbonContext().getTenantId());
            preparedStatement.execute();
            resultSet = preparedStatement.getResultSet();
            while (resultSet.next()) {
                Artifact artifact = new Artifact( applicationKey,
                "build "+ resultSet.getString(SQLParameterConstants.COLUMN_NAME_LAST_BUILD)
                +" " + resultSet.getString(SQLParameterConstants.COLUMN_NAME_LAST_BUILD_STATUS)
        ,       resultSet.getString(SQLParameterConstants.COLUMN_NAME_VERSION_NAME),
                resultSet.getInt(SQLParameterConstants.COLUMN_NAME_AUTO_BUILD)==1?true:false,
                resultSet.getInt(SQLParameterConstants.COLUMN_NAME_AUTO_DEPLOY)==1?true:false,
                resultSet.getString(SQLParameterConstants.COLUMN_NAME_LAST_DEPLOY),
                resultSet.getString(SQLParameterConstants.COLUMN_NAME_STAGE),
                null,
                resultSet.getString(SQLParameterConstants.COLUMN_NAME_PROMOTE_STATUS));
                artifact.setProductionMappedDomain(resultSet.getString(SQLParameterConstants.COLUMN_NAME_SUB_DOMAIN));
                artifactList.add(artifact);
            }
        } catch (SQLException e) {
            String msg = "Error while getting all the version of application : " + applicationKey;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } finally {
            AppFactoryDBUtil.closeResultSet(resultSet);
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }

        if (log.isDebugEnabled()) {
            log.debug("List of Version IDs of application : " + applicationKey + " are : " + artifactList);
        }
        return artifactList;
    }

}
