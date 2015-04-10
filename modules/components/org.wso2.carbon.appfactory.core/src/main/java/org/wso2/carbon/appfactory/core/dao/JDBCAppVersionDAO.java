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
import org.wso2.carbon.appfactory.core.cache.JDBCApplicationCacheManager;
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
     * Updates the promote status of an application version
     *
     * @param applicationKey application key
     * @param version version number
     * @param status  status of promotion
     * @return true if it a success; false if it a failure
     * @throws org.wso2.carbon.appfactory.common.AppFactoryException
     */
    public boolean updatePromoteStatusOfVersion(String applicationKey, String version, String status) throws
                                                                                                  AppFactoryException {
        JDBCApplicationCacheManager.getAppVersionCache().remove(JDBCApplicationCacheManager.constructAppVersionCacheKey(applicationKey, version));
        Connection databaseConnection = null;
        PreparedStatement preparedStatement = null;
        try {
            databaseConnection = AppFactoryDBUtil.getConnection();
            preparedStatement = databaseConnection.prepareStatement(SQLConstants.UPDATE_PROMOTE_STATUS_OF_VERSION);
            preparedStatement.setString(1, status);
            preparedStatement.setInt(2, JDBCApplicationDAO.getInstance().getAutoIncrementAppID(applicationKey));
            preparedStatement.setString(3, version);
            preparedStatement.execute();
            int affectedRows = preparedStatement.getUpdateCount();
            if (affectedRows > 0) {
                databaseConnection.commit();
                return true;
            }
            String msg = "Error while updating promote status of version : " + version;
            log.error(msg);
            throw new AppFactoryException(msg);
        } catch (SQLException e) {
            try {
                if (databaseConnection != null) {
                    databaseConnection.rollback();
                }
            } catch (SQLException e1) {
                // Only logging this exception since this is not the main issue. The original issue is thrown.
                log.error("Error while rolling back update promote status of version : " + version, e1);
            }
            String msg = "Error while updating promote status of version : " + version;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } finally {
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }
    }

    /**
     * Updates the stage of an application version
     *
     * @param applicationKey application key
     * @param version version number
     * @param stage   stage
     * @return true if it a success; false if it a failure
     * @throws org.wso2.carbon.appfactory.common.AppFactoryException
     */
    public boolean updateStageOfVersion(String applicationKey, String version, String stage) throws
                                                                                              AppFactoryException {
        JDBCApplicationCacheManager.getAppVersionCache().remove(JDBCApplicationCacheManager.constructAppVersionCacheKey(applicationKey, version));
        Connection databaseConnection = null;
        PreparedStatement preparedStatement = null;
        try {
            databaseConnection = AppFactoryDBUtil.getConnection();
            preparedStatement = databaseConnection.prepareStatement(SQLConstants.UPDATE_STAGE_OF_VERSION);
            preparedStatement.setString(1, stage);
            preparedStatement.setInt(2, JDBCApplicationDAO.getInstance().getAutoIncrementAppID(applicationKey));
            preparedStatement.setString(3, version);
            preparedStatement.execute();
            databaseConnection.commit();
        } catch (SQLException e) {
            try {
                if (databaseConnection != null) {
                    databaseConnection.rollback();
                }
            } catch (SQLException e1) {
                // Only logging this exception since this is not the main issue. The original issue is thrown.
                log.error("Error while rolling back update stage of version : " + version, e1);
            }
            String msg = "Error while updating stage of version : " + version;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } finally {
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }
        return true;
    }

    /**
     * Updates sub domain of an application version
     *
     * @param applicationKey application key
     * @param version   version number
     * @param subdomain sub domain
     * @return true if it success false if it failed
     * @throws org.wso2.carbon.appfactory.common.AppFactoryException
     */
    public boolean updateSubDomainsOfVersion(String applicationKey, String version, String subdomain) throws
                                                                                                       AppFactoryException {
        JDBCApplicationCacheManager.getAppVersionCache().remove(JDBCApplicationCacheManager.constructAppVersionCacheKey(applicationKey, version));
        Connection databaseConnection = null;
        PreparedStatement preparedStatement = null;
        try {
            databaseConnection = AppFactoryDBUtil.getConnection();
            preparedStatement = databaseConnection.prepareStatement(SQLConstants.UPDATE_SUBDOMAIN_OF_VERSION);
            preparedStatement.setString(1, subdomain);
            preparedStatement.setInt(2, JDBCApplicationDAO.getInstance().getAutoIncrementAppID(applicationKey));
            preparedStatement.setString(3, version);
            preparedStatement.execute();
            databaseConnection.commit();
        } catch (SQLException e) {
            try {
                if (databaseConnection != null) {
                    databaseConnection.rollback();
                }
            } catch (SQLException e1) {
                // Only logging this exception since this is not the main issue. The original issue is thrown.
                log.error("Error while rolling back update sub domain of version : " + version, e1);
            }
            String msg = "Error while rolling back update sub domain of version : " + version;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } finally {
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }
        return true;
    }

    /**
     * Updates auto build status of an application version
     *
     * @param applicationKey application key
     * @param version         version number
     * @param isAutoBuildable Auto Build is set or not
     * @return true if it success false if it failed
     * @throws org.wso2.carbon.appfactory.common.AppFactoryException
     */
    public boolean updateAutoBuildStatusOfVersion(String applicationKey, String version, boolean isAutoBuildable)
                                                                                           throws AppFactoryException {
        JDBCApplicationCacheManager.getAppVersionCache().remove(JDBCApplicationCacheManager.constructAppVersionCacheKey(applicationKey, version));
        Connection databaseConnection = null;
        PreparedStatement preparedStatement = null;
        try {
            databaseConnection = AppFactoryDBUtil.getConnection();
            preparedStatement = databaseConnection.prepareStatement(SQLConstants.UPDATE_AUTO_BUILD_STATUS_OF_VERSION);
            preparedStatement.setInt(1, isAutoBuildable ? 1 : 0);
            preparedStatement.setInt(2, JDBCApplicationDAO.getInstance().getAutoIncrementAppID(applicationKey));
            preparedStatement.setString(3, version);
            preparedStatement.execute();
            databaseConnection.commit();
        } catch (SQLException e) {
            try {
                if (databaseConnection != null) {
                    databaseConnection.rollback();
                }
            } catch (SQLException e1) {
                // Only logging this exception since this is not the main issue. The original issue is thrown.
                log.error("Error while rolling back update auto build status of version : " + version, e1);
            }
            String msg = "Error while rolling back update auto build status of version : " + version;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } finally {
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }
        return true;
    }

    /**
     * Gets auto build status of a given application version
     *
     * @param applicationKey applicationKey of application
     * @param versionName        version number
     * @return true or false for auto build status
     * @throws AppFactoryException
     */
    public boolean getAutoBuildStatusOfVersion(String applicationKey, String versionName) throws AppFactoryException {
        Version version = getApplicationVersion(applicationKey, versionName);
        return version.isAutoBuild();
    }

    /**
     * Update method for auto deploy status of an application version
     *
     * @param applicationKey applicationKey of application
     * @param version          version number
     * @param isAutoDeployable Auto Deploy is set or not
     * @return true if it success false if it failed
     * @throws org.wso2.carbon.appfactory.common.AppFactoryException
     */
    public boolean updateAutoDeployStatusOfVersion(String applicationKey, String version,
                                                   boolean isAutoDeployable) throws AppFactoryException {
        JDBCApplicationCacheManager.getAppVersionCache().remove(JDBCApplicationCacheManager.constructAppVersionCacheKey(applicationKey, version));
        Connection databaseConnection = null;
        PreparedStatement preparedStatement = null;
        try {
            databaseConnection = AppFactoryDBUtil.getConnection();
            preparedStatement = databaseConnection.prepareStatement(SQLConstants.UPDATE_AUTO_DEPLOY_STATUS_OF_VERSION);
            preparedStatement.setInt(1, isAutoDeployable ? 1 : 0);
            preparedStatement.setInt(2,JDBCApplicationDAO.getInstance().getAutoIncrementAppID(applicationKey));
            preparedStatement.setString(3, version);
            preparedStatement.execute();
            databaseConnection.commit();
        } catch (SQLException e) {
            try {
                if (databaseConnection != null) {
                    databaseConnection.rollback();
                }
            } catch (SQLException e1) {
                // Only logging this exception since this is not the main issue. The original issue is thrown.
                log.error("Error while rolling back update auto deploy status of version " + version, e1);
            }
            String msg = "Error while rolling back update auto deploy status of version " + version;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } finally {
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }
        return true;
    }

    /**
     * et auto deploy status of a given application version
     *
     * @param applicationKey applicationKey of application
     * @param versionName        version number
     * @return true or false for auto deploy status
     * @throws AppFactoryException
     */
    public boolean getAutoDeployStatusOfVersion(String applicationKey, String versionName) throws AppFactoryException {
        Version version = getApplicationVersion(applicationKey, versionName);
        return version.isAutoDeploy();
    }

    /**
     * Returns the stage of a particular application version
     *
     * @param versionName        version number
     * @param applicationKey application key
     * @return true if it success false if it failed
     * @throws org.wso2.carbon.appfactory.common.AppFactoryException
     */
    public String getAppVersionStage(String applicationKey, String versionName) throws AppFactoryException {
        Version version = getApplicationVersion(applicationKey, versionName);
        return version.getStage();
    }

    /**
     * Get a {@link org.wso2.carbon.appfactory.core.dto.Version} of a given application and version name
     *
     * @param applicationKey application key
     * @param versionName        version number
     * @return {@link org.wso2.carbon.appfactory.core.dto.Version}
     * @throws AppFactoryException
     */
    public Version getApplicationVersion(String applicationKey, String versionName) throws AppFactoryException {
        Connection databaseConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet allVersions = null;
        Version version = null;
        version = JDBCApplicationCacheManager.getAppVersionCache().get(JDBCApplicationCacheManager.
                                              constructAppVersionCacheKey(applicationKey, versionName));
        if (version != null) {
            return version;
        }
        try {
            databaseConnection = AppFactoryDBUtil.getConnection();
            preparedStatement = databaseConnection.prepareStatement(SQLConstants.GET_APPLICATION_VERSION_SQL);
            preparedStatement.setInt(1, JDBCApplicationDAO.getInstance().getAutoIncrementAppID(applicationKey));
            preparedStatement.setString(2, versionName);
            allVersions = preparedStatement.executeQuery();
            if (allVersions.next()) {
                version = new Version();
                version.setVersion(allVersions.getString(SQLParameterConstants.COLUMN_NAME_VERSION_NAME));
                version.setStage(allVersions.getString(SQLParameterConstants.COLUMN_NAME_STAGE));
                version.setPromoteStatus(allVersions.getString(SQLParameterConstants.COLUMN_NAME_PROMOTE_STATUS));
            }

            JDBCApplicationCacheManager.getAppVersionCache().
                    put(JDBCApplicationCacheManager.constructAppVersionCacheKey(applicationKey, versionName),
                        version);

        } catch (SQLException e) {
            String msg = "Error while getting application version : " + versionName;
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
     * This method is used to get a list of all {@link org.wso2.carbon.appfactory.core.dto.Version} versions of the a application
     *
     * @param applicationKey The application applicationKey of the current application
     * @return {@link org.wso2.carbon.appfactory.core.dto.Version}
     * @throws AppFactoryException if SQL operation fails
     */
    public String[] getAllVersionsOfApplication(String applicationKey) throws AppFactoryException {
        Connection databaseConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<String> versionList = JDBCApplicationCacheManager.getAppVersionListCache().
                                                               get(JDBCApplicationCacheManager.constructAppVersionListCacheKey(applicationKey));
        if (versionList != null) {
            return versionList.toArray(new String[versionList.size()]);
        }
        try {
            //TODO - Punnadi - write code to get the list of names


//        } catch (SQLException e) {
//            String msg = "Error while getting all the version of application key : " + applicationKey;
//            log.error(msg, e);
//            throw new AppFactoryException(msg, e);
        } finally {
            AppFactoryDBUtil.closeResultSet(resultSet);
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }
        if (log.isDebugEnabled()) {
            log.debug("List of Version IDs of application key : " + applicationKey + " are : " + versionList);
        }
        return null; //TODO - Fix Punnadi
    }

    /**
     * Add new a version of a application
     *
     * @param applicationKey application key of an application
     * @param version        Version with version name
     * @return true if it successful false if it failed
     * @throws AppFactoryException
     */
    public boolean addVersion(String applicationKey, Version version) throws AppFactoryException {
        Connection databaseConnection = null;
        try {
            databaseConnection = AppFactoryDBUtil.getConnection();
            JDBCApplicationDAO.getInstance().addVersion(version, databaseConnection, applicationKey);
            databaseConnection.commit();
            return true;
        } catch (SQLException e) {
            try {
                if (databaseConnection != null) {
                    databaseConnection.rollback();
                }
            } catch (SQLException e1) {

                // Only logging this exception since this is not the main issue. The original issue is thrown.
                log.error("Error while rolling back add version " + version.getVersion() + " of application key : " + applicationKey, e1);
            }
            String msg = "Error while adding version : " + version.getVersion() + " of application key : " + applicationKey;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } finally {
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }
    }

}
