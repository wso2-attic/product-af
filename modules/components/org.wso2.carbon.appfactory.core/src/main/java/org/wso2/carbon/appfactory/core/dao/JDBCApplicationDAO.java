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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.cache.JDBCApplicationCacheManager;
import org.wso2.carbon.appfactory.core.dto.Version;
import org.wso2.carbon.appfactory.core.dto.Application;
import org.wso2.carbon.appfactory.core.dto.BuildStatus;
import org.wso2.carbon.appfactory.core.dto.CartridgeCluster;
import org.wso2.carbon.appfactory.core.dto.DeployStatus;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.appfactory.core.sql.SQLConstants;
import org.wso2.carbon.appfactory.core.sql.SQLParameterConstants;
import org.wso2.carbon.appfactory.core.util.AppFactoryCoreUtil;
import org.wso2.carbon.appfactory.core.util.AppFactoryDBUtil;
import org.wso2.carbon.appfactory.core.util.Constants;
import org.wso2.carbon.context.CarbonContext;

import javax.cache.Cache;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * DAO Class for managing app factory runtime data
 */
public class JDBCApplicationDAO {
    private static final Log log = LogFactory.getLog(JDBCApplicationDAO.class);
    private static JDBCApplicationDAO applicationDAO = new JDBCApplicationDAO();

    private JDBCApplicationDAO() {
    }

    public static JDBCApplicationDAO getInstance() {
        return applicationDAO;
    }

    private static void handleException(String msg, Throwable t) throws AppFactoryException {

        // We append tenant domain for every message that comes here.
        msg += " of tenant " + CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        log.error(msg, t);
        throw new AppFactoryException(msg, t);
    }

    private static void handleException(String msg) throws AppFactoryException {
        msg += " of tenant " + CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        log.error(msg);
        throw new AppFactoryException(msg);
    }

    private static void handleDebugLog(String msg) {
        if (log.isDebugEnabled()) {
            msg += " of tenant " + CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            log.debug(msg);
        }

    }

    /**
     * It will add application with default initial version 'trunk'
     *
     * @param application Application with given key
     * @return true if it successful false if it failed
     * @throws AppFactoryException if there is a problem in creating application
     */
    public boolean addApplication(Application application) throws AppFactoryException {
        Connection databaseConnection = null;
        PreparedStatement preparedStatement = null;
        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            databaseConnection = AppFactoryDBUtil.getConnection();
            preparedStatement = databaseConnection.prepareStatement(SQLConstants.ADD_APPLICATION_SQL);
            preparedStatement.setString(1, application.getId());
            preparedStatement.setString(2, application.getName());
            preparedStatement.setInt(3, tenantID);
            preparedStatement.execute();
            int updatedRowCount = preparedStatement.getUpdateCount();
            if (updatedRowCount > 0) {

                //debug log
                handleDebugLog((new StringBuilder()).append("successfully added application.Updated ").append
                        (updatedRowCount).append(" rows").toString());
                Version version = AppFactoryCoreUtil.isUplodableAppType(application.getType()) ? new Version(
                        SQLParameterConstants.VERSION_1_0_0, AppFactoryConstants.ApplicationStage.PRODUCTION.
                        getCapitalizedString()) : new Version(SQLParameterConstants.VERSION_TRUNK, AppFactoryConstants.
                        ApplicationStage.DEVELOPMENT.getCapitalizedString());
                addVersion(version, databaseConnection, application.getId());
                databaseConnection.commit();
                return true;
            }

            String errorMessage = "Adding application is failed for " + application.getId();
            handleException(errorMessage); // This is not caught within this method.
        } catch (SQLException e) {
            try {
                if (databaseConnection != null) {
                    databaseConnection.rollback();
                }
            } catch (SQLException e1) {
                // Only logging this exception since this is not the main issue. The original issue is thrown.
                log.error("Error while rolling back the added application", e1);
            }

            String errorMsg = "Error while adding the application " + application.getId();
            handleException(errorMsg, e);
        } finally {
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }
        return false;
    }

    /**
     * Method to check the existence of the applicationKey
     *
     * @param applicationKey key of the application
     * @return true if key exists
     */
    public boolean isApplicationKeyExists(String applicationKey) throws AppFactoryException {
        Connection databaseConnection = null;
        try {
            databaseConnection = AppFactoryDBUtil.getConnection();
            int applicationId = getAutoIncrementAppID(applicationKey, databaseConnection);

            // The result will contain -1 if there are no applications.
            if (applicationId > 0) {
                return true;
            }
        } catch (SQLException e) {
            handleException("Error while checking the existence of application key : " + applicationKey, e);
        } finally {
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }
        return false;
    }

    /**
     * Method to check the existence of the applicationName
     *
     * @param applicationName name of the application
     * @return true if name exists
     */
    public boolean isApplicationNameExists(String applicationName) throws AppFactoryException {
        // Get the tenant id from CarbonContext. We need to the tenant id to create the cache key
        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String appNameCacheKey = JDBCApplicationCacheManager.constructAppNameCacheKey(applicationName, tenantID);
        // We do the cache check here.
        Cache<String, Boolean> nameCache = JDBCApplicationCacheManager.getJDBCApplicationNameCache();
        if (nameCache.containsKey(appNameCacheKey)) {
            handleDebugLog("Retrieving data from the application name cache for application name : " + applicationName);
            return true;
        }
        // No cache hit
        Connection databaseConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet applicationResult = null;
        try {
            databaseConnection = AppFactoryDBUtil.getConnection();
            preparedStatement = databaseConnection.prepareStatement(SQLConstants.IS_APPLICATION_NAME_EXISTS);
            preparedStatement.setString(1, applicationName);
            preparedStatement.setInt(2, tenantID);
            applicationResult = preparedStatement.executeQuery();
            handleDebugLog("Is application name exists check for application name : " + applicationName);

            // This means we have a result set. So need to update the cache
            if (applicationResult.next()) {
                nameCache.put(appNameCacheKey, Boolean.TRUE);
                return true;
            }
        } catch (SQLException e) {
            handleException("Error while getting application name : " + applicationName, e);
        } finally {
            AppFactoryDBUtil.closeResultSet(applicationResult);
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }
        return false;
    }

    /**
     * Method to get the application name of a given application key.
     * (This method is used get the application name to clear the cache when deleting the application.
     * Hence making it private)
     *
     * @param applicationKey the current application key.
     * @return the application name of the given application key.
     * @throws org.wso2.carbon.appfactory.common.AppFactoryException if an database error occurs
     */
    private String getApplicationName(String applicationKey) throws AppFactoryException {
        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        Connection databaseConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet applicationResult = null;
        try {
            databaseConnection = AppFactoryDBUtil.getConnection();
            preparedStatement = databaseConnection.prepareStatement(SQLConstants.GET_APPLICATION_NAME_SQL);
            preparedStatement.setString(1, applicationKey);
            preparedStatement.setInt(2, tenantID);
            applicationResult = preparedStatement.executeQuery();
            handleDebugLog("Getting the application name for application key : " + applicationKey);

            // This means we have a result set. So need to update the cache
            if (applicationResult.next()) {
                String applicationName = applicationResult.getString(SQLParameterConstants.COLUMN_NAME_APPLICATION_NAME);
                handleDebugLog("Successfully received the application name : " + applicationName + " for application " +
                               "key : " + applicationKey);
                return applicationName;
            }
        } catch (SQLException e) {
            handleException("Error while getting application name of application key : " + applicationKey, e);
        } finally {
            AppFactoryDBUtil.closeResultSet(applicationResult);
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }
        return null;
    }

    /**
     * Helper method to add a version with given DB connection
     *
     * @param version            version with version name
     * @param databaseConnection existing db connection
     * @param applicationKey     application key to identify the application
     * @return true if it successful false if it failed
     * @throws AppFactoryException
     */
     boolean addVersion(Version version, Connection databaseConnection,
                               String applicationKey) throws AppFactoryException {
        PreparedStatement addVersionPreparedStatement = null;
        try {
            addVersionPreparedStatement = databaseConnection.prepareStatement(SQLConstants.ADD_VERSION);
            addVersionPreparedStatement.setInt(1, JDBCApplicationDAO.getInstance().getAutoIncrementAppID(applicationKey,
                                                                                                   databaseConnection));
            addVersionPreparedStatement.setString(2, version.getVersion());
            addVersionPreparedStatement.setString(3, version.getStage());
            addVersionPreparedStatement.setInt(4, CarbonContext.getThreadLocalCarbonContext().getTenantId());
            addVersionPreparedStatement.execute();
            int affectedRowCount = addVersionPreparedStatement.getUpdateCount();
            int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            String applicationAppsBranchCountKey = JDBCApplicationCacheManager
                    .constructApplicationBranchCountCacheKey(tenantID, applicationKey);
            Cache<String, Integer> applicationBranchCountCache =
                    JDBCApplicationCacheManager.getApplicationBranchCountCache();
            if (affectedRowCount > 0) {

                //removing the older cache while adding new version for an app.
                applicationBranchCountCache.remove(applicationAppsBranchCountKey);
                if (log.isDebugEnabled()) {
                    String msg = "successfully added application of tenant " +
                                 CarbonContext.getThreadLocalCarbonContext().getTenantDomain() + " version : " +
                                 version.getVersion() + " of application key : " + applicationKey + " updated : " +
                                 affectedRowCount + " rows";
                    log.debug(msg);
                }
                int versionID = getAutoIncrementVersionID(applicationKey, version.getVersion(), databaseConnection);
                addRepository(versionID, false, null, databaseConnection);
                return true;
            }
        } catch (SQLException e) {
            handleException("Adding new version : " + version.getVersion() + " for application key : " + applicationKey, e);
        } finally {
            AppFactoryDBUtil.closePreparedStatement(addVersionPreparedStatement);
        }
        return false;
    }

    private boolean addRepository(int versionID, boolean isForked, String username, Connection databaseConnection)
            throws AppFactoryException {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = databaseConnection.prepareStatement(SQLConstants.ADD_APPLICATION_REPOSITORY_SQL);
            preparedStatement.setInt(1, versionID);
            preparedStatement.setInt(2, isForked ? 1 : 0);
            preparedStatement.setString(3, username);
            preparedStatement.setInt(4, CarbonContext.getThreadLocalCarbonContext().getTenantId());
            preparedStatement.execute();
            int affectedRows = preparedStatement.getUpdateCount();
            if (affectedRows > 0) {
                int repositoryID = getAutoIncrementRepositoryID(versionID, isForked, username, databaseConnection);
                addBuildStatus(repositoryID, databaseConnection);
                String stages[] = ServiceHolder.getAppFactoryConfiguration().getProperties(AppFactoryConstants
                                                                                                   .DEPLOYMENT_STAGES);
                for (String stage : stages) {
                    addDeployStatus(repositoryID, stage, databaseConnection);
                }
                return true;
            }
            handleException("Adding repository failed for version : " + versionID);
        } catch (SQLException e) {
            handleException("Adding repository failed for version : " + versionID, e);
        } finally {
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
        }
        return false;
    }

    /**
     * Delete all the data related to a application.
     * This method will get all the versions of the application and then retrieve all the repository ids of those
     * versions.
     * Then delete entries related to those repository ids from other database tables.
     *
     * @param applicationKey application key
     * @return true if it successful false if it failed
     * @throws AppFactoryException
     */
    public boolean deleteApplication(String applicationKey) throws AppFactoryException {
        Connection databaseConnection = null;
        PreparedStatement preparedStatement = null;
        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {

            // Get all the version IDs of the application
            List<Integer> allVersionIDs = getAllVersionIdsOfApplication(applicationKey);

            //get all repository IDs
            List<Integer> allRepositoryIDs = getAllRepositoryIDsOfApplication(applicationKey, allVersionIDs);

            // Delete from all tables using the above list of repository ids.
            // We use the same database connection so that we can rollback if an error happens.
            databaseConnection = AppFactoryDBUtil.getConnection();

            deleteFromBuildStatus(databaseConnection, applicationKey, allRepositoryIDs);
            deleteFromDeployStatus(databaseConnection, applicationKey, allRepositoryIDs);
            deleteFromRepository(databaseConnection, applicationKey, allRepositoryIDs);

            // Delete the application Version related information
            deleteFromApplicationVersion(databaseConnection, applicationKey, allVersionIDs);

            //delete resources if any exists
            deleteFromApplicationResources(databaseConnection, applicationKey);

            // We delete the application information from the AF_APPLICATION table
            preparedStatement = databaseConnection.prepareStatement(SQLConstants.DELETE_APPLICATION_SQL);
            preparedStatement.setString(1, applicationKey);
            preparedStatement.setInt(2, tenantID);
            preparedStatement.execute();
            int affectedRows = preparedStatement.getUpdateCount();
            if (affectedRows > 0) {
                databaseConnection.commit();
                handleDebugLog("Removing data from all the caches for application key : " + applicationKey);

                // We remove all the cache entries here. This method will remove application related data from all the caches.
                removeApplicationDataFromAllCaches(applicationKey);
                handleDebugLog("Successfully removed data from all the caches for application key : " + applicationKey
                               + "Successfully deleted application : " + applicationKey);
                return true;
            }

            // If the delete application information from AF_APPLICATION tables yields no results, then we throw and error
            handleException("Deleting application key : " + applicationKey + " failed");
        } catch (SQLException e) {
            try {
                if (databaseConnection != null) {
                    databaseConnection.rollback();
                }
            } catch (SQLException e1) {

                // Only logging this exception since this is not the main issue. The original issue is thrown.
                log.error("Rolling back Deleting application of application key : " + applicationKey + " is failed of " +
                          "tenant id : " + CarbonContext.getThreadLocalCarbonContext().getTenantDomain(), e1);
            }
            handleException("Deleting application of application key : " + applicationKey + " is failed", e);
        } finally {
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }

        return false;
    }

    /**
     * This method is used to delete the build information of the given application
     *
     * @param databaseConnection The current database connection.
     * @param applicationKey     The application key of the deleted application.
     * @param repositoryIDs      List of version repository IDs of the application.
     * @throws org.wso2.carbon.appfactory.common.AppFactoryException If any SQLException occurs
     */
    private void deleteFromBuildStatus(Connection databaseConnection, String applicationKey,
                                       List<Integer> repositoryIDs) throws AppFactoryException {

        // If the list of repository IDs are empty, then there is no need to execute the following
        if (repositoryIDs.isEmpty()) {
            handleDebugLog("The list of repository IDs are empty for application : " + applicationKey);
            return;
        }
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = databaseConnection.prepareStatement(SQLConstants.DELETE_BUILD_STATUS_SQL);
            for (Integer repositoryID : repositoryIDs) {
                preparedStatement.setInt(1, repositoryID);
                preparedStatement.addBatch();

            }
            handleDebugLog("Adding repository id " + repositoryIDs + " of application" + applicationKey + " for " +
                           "deletion of build status information");
            preparedStatement.executeBatch();
            handleDebugLog("Successfully deleted all application build status information of application : " +
                           applicationKey);
        } catch (SQLException e) {

            // We do not rollback at this level since that is done from the calling method
            // We log here so that we can get a more specific message on where the failure happen.
            handleException("Error while deleting build status information of application : " + applicationKey, e);
        } finally {

            // We close only the preparedStatement since the database connection is passed from the calling method.
            // Database connection will be closed in that method.
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
        }
    }

    /**
     * This method is used to delete the deploy information of the given application
     *
     * @param databaseConnection The current database connection.
     * @param applicationKey     The application key of the deleted application.
     * @param repositoryIDs      List of version repository IDs of the application.
     * @throws org.wso2.carbon.appfactory.common.AppFactoryException If any SQLException occurs
     */
    private void deleteFromDeployStatus(Connection databaseConnection, String applicationKey,
                                        List<Integer> repositoryIDs) throws AppFactoryException {

        // If the list of repository IDs are empty, then there is no need to execute the following
        if (repositoryIDs.isEmpty()) {
            handleDebugLog("The list of repository IDs are empty for application : " + applicationKey);
            return;
        }
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = databaseConnection.prepareStatement(SQLConstants.DELETE_DEPLOY_STATUS_SQL);
            for (Integer repositoryID : repositoryIDs) {
                preparedStatement.setInt(1, repositoryID);
                preparedStatement.addBatch();
                handleDebugLog("Adding repository id " + repositoryID + " of application" + applicationKey + " for " +
                               "deletion of deploy status information");
            }
            preparedStatement.executeBatch();
            handleDebugLog("Successfully deleted all application deploy status information of application : " +
                           applicationKey);
        } catch (SQLException e) {

            // We do not rollback at this level since that is done from the calling method
            // We log here so that we can get a more specific message on where the failure happen.
            handleException("Error while deleting deploy status information of application : " + applicationKey, e);
        } finally {

            // We close only the preparedStatement since the database connection is passed from the calling method.
            // Database connection will be closed in that method.
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
        }

    }

    /**
     * This method is used to delete the repository information of the given application
     *
     * @param databaseConnection The current database connection.
     * @param applicationKey     The application key of the deleted application.
     * @param repositoryIDs      List of version repository IDs of the application.
     * @throws org.wso2.carbon.appfactory.common.AppFactoryException If any SQLException occurs
     */
    private void deleteFromRepository(Connection databaseConnection, String applicationKey, List<Integer> repositoryIDs)
            throws AppFactoryException {
        // If the list of repository IDs are empty, then there is no need to execute the following
        if (repositoryIDs.isEmpty()) {
            // debug log
            handleDebugLog("The list of repository IDs are empty for application key : " + applicationKey);
            return;
        }
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = databaseConnection.prepareStatement(SQLConstants.DELETE_APPLICATION_REPOSITORY_SQL);
            for (Integer repositoryID : repositoryIDs) {
                preparedStatement.setInt(1, repositoryID);
                preparedStatement.addBatch();
                handleDebugLog("Adding repository id " + repositoryID + " of application" + applicationKey + " for " +
                               "deletion of repository information");
            }
            preparedStatement.executeBatch();
            handleDebugLog("Successfully deleted all application repository information of application key : " +
                           applicationKey);
        } catch (SQLException e) {

            // We do not rollback at this level since that is done from the calling method
            // We log here so that we can get a more specific message on where the failure happen.
            handleException("Error while deleting repository information of application key : " + applicationKey, e);
        } finally {

            // We close only the preparedStatement since the database connection is passed from the calling method.
            // Database connection will be closed in that method.
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
        }
    }

    /**
     * This method is used to delete the version information of the given application
     *
     * @param databaseConnection The current database connection.
     * @param applicationKey     The application key of the deleted application.
     * @param versionIDs         List of version IDs of the application.
     * @throws org.wso2.carbon.appfactory.common.AppFactoryException If any SQLException occurs
     */
    private void deleteFromApplicationVersion(Connection databaseConnection, String applicationKey,
                                              List<Integer> versionIDs) throws AppFactoryException {

        // The versionIDs can be empty when there are issues in app creation.
        // Hence if the list of version id are empty, we simply return a empty list.
        if (versionIDs.isEmpty()) {
            handleDebugLog("The list of version IDs are empty for application key : " + applicationKey);
            return;
        }
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = databaseConnection.prepareStatement(SQLConstants.DELETE_APPLICATION_VERSION);
            for (Integer versionID : versionIDs) {
                preparedStatement.setInt(1, versionID);
                preparedStatement.addBatch();
                handleDebugLog("Adding repository id : " + versionID + " of application key : " + applicationKey + " for " +
                               "deletion of application version information");
            }
            preparedStatement.executeBatch();
            handleDebugLog("Successfully deleted all application version information of application : " +
                           applicationKey);
        } catch (SQLException e) {

            // We do not rollback at this level since that is done from the calling method
            // We log here so that we can get a more specific message on where the failure happen.
            handleException("Error while deleting version information of application key : " + applicationKey, e);
        } finally {

            // We close only the preparedStatement since the database connection is passed from the calling method.
            // Database connection will be closed in that method.
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
        }
    }

    /**
     * This method is used to delete the application resources information of the given application
     *
     * @param databaseConnection The current database connection.
     * @param applicationKey     The application key of the deleted application.
     * @throws org.wso2.carbon.appfactory.common.AppFactoryException If any SQLException occurs
     */
    private void deleteFromApplicationResources(Connection databaseConnection, String applicationKey)
            throws AppFactoryException {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = databaseConnection.prepareStatement(SQLConstants.DELETE_ALL_RESOURCES_SQL);
            preparedStatement.setInt(1, getAutoIncrementAppID(applicationKey, databaseConnection));
            preparedStatement.execute();
            handleDebugLog("Successfully deleted all application resources information of application key : " +
                           applicationKey);
        } catch (SQLException e) {

            // We do not rollback at this level since that is done from the calling method
            // We log here so that we can get a more specific message on where the failure happen.
            handleException("Error occurred while deleting application resources of application key : " + applicationKey,
                            e);
        } finally {

            // We close only the preparedStatement since the database connection is passed from the calling method.
            // Database connection will be closed in that method.
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
        }
    }

    /**
     * This method is used to get the all version ids of the given application
     * (This method is private since it is only been used when deleting an application.
     * We do not use getAllApplicationVersions(ApplicationID) because of the following reason.
     * The version object does not have a field to keep the database index.
     * If we add such a field then there is the risk of exposing the database index publicly.
     * Since that is a bad practice we use this method to get only the version IDs)
     *
     * @param applicationID The application id of the current application
     * @return a list of version ids
     * @throws AppFactoryException if SQL operation fails
     */
    private List<Integer> getAllVersionIdsOfApplication(String applicationID) throws AppFactoryException {
        Connection databaseConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Integer> versionList = new ArrayList<Integer>();
        try {
            databaseConnection = AppFactoryDBUtil.getConnection();
            preparedStatement = databaseConnection.prepareStatement(SQLConstants.GET_ALL_APPLICATION_VERSION_ID);
            preparedStatement.setInt(1, getAutoIncrementAppID(applicationID, databaseConnection));
            preparedStatement.execute();
            resultSet = preparedStatement.getResultSet();
            while (resultSet.next()) {
                versionList.add(resultSet.getInt(SQLParameterConstants.COLUMN_NAME_ID));
            }
        } catch (SQLException e) {
            handleException("Error while getting all the version of application : " + applicationID, e);
        } finally {
            AppFactoryDBUtil.closeResultSet(resultSet);
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }
        handleDebugLog("List of Version IDs of application : " + applicationID + " are : " + versionList);
        return versionList;
    }

    /**
     * This method returns all the repository IDs of the application.
     *
     * @param versionIDs the list of all the version IDs of the application
     * @return The list of repository IDs that are associated with the given version IDs
     * @throws AppFactoryException if SQL operation fails
     */
    private List<Integer> getAllRepositoryIDsOfApplication(String applicationKey, List<Integer> versionIDs)
            throws AppFactoryException {
        List<Integer> allRepositoryIDs = new ArrayList<Integer>();

        // The versionIDs can be empty when there are issues in app creation.
        // Hence if the list of version id are empty, we simply return a empty list.
        if (versionIDs.isEmpty()) {
            handleDebugLog("The list of version IDs are empty for application : " + applicationKey);
            return allRepositoryIDs;
        }
        Connection databaseConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            databaseConnection = AppFactoryDBUtil.getConnection();

            // We can not use a batch operation for fetching values. Hence we create the SQL dynamically
            String sqlString = SQLConstants.GET_ALL_APPLICATION_REPOSITORY_ID_SQL.replace("?", preparePlaceHolders
                    (versionIDs.size()));
            preparedStatement = databaseConnection.prepareStatement(sqlString);
            int index = 1;
            for (Integer versionID : versionIDs) {
                preparedStatement.setInt(index, versionID);
                index++;
            }
            preparedStatement.execute();
            resultSet = preparedStatement.getResultSet();
            while (resultSet.next()) {
                allRepositoryIDs.add(resultSet.getInt(SQLParameterConstants.COLUMN_NAME_ID));
            }
        } catch (SQLException e) {
            handleException("Error occurred while getting the list of repository IDs of application : " + applicationKey
                    , e);
        } finally {
            AppFactoryDBUtil.closeResultSet(resultSet);
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }
        handleDebugLog("The list of repository IDs of application : " + applicationKey + " are : " + allRepositoryIDs);
        return allRepositoryIDs;
    }

    /**
     * Set application creation status of an application
     *
     * @param applicationKey            application key of an application
     * @param applicationCreationStatus {@link org.wso2.carbon.appfactory.core.util.Constants.ApplicationCreationStatus}
     * @return true if it successful false if it failed
     * @throws AppFactoryException
     */
    public boolean setApplicationCreationStatus(String applicationKey, Constants.ApplicationCreationStatus
                                                        applicationCreationStatus) throws AppFactoryException {
        Connection databaseConnection = null;
        PreparedStatement preparedStatement = null;
        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            databaseConnection = AppFactoryDBUtil.getConnection();
            preparedStatement = databaseConnection.prepareStatement(SQLConstants.UPDATE_APPLICATION_CREATION_STATUS_SQL);
            preparedStatement.setString(1, applicationCreationStatus.name());
            preparedStatement.setString(2, applicationKey);
            preparedStatement.setInt(3, tenantID);
            preparedStatement.execute();
            int affectedRows = preparedStatement.getUpdateCount();
            if (affectedRows > 0) {
                databaseConnection.commit();

                // We remove from cache here
                String appCreationStatusCacheKey = JDBCApplicationCacheManager.constructApplicationCreationCacheKey
                        (tenantID, applicationKey);
                Cache<String, Constants.ApplicationCreationStatus> applicationCreationStatusCache =
                        JDBCApplicationCacheManager.getApplicationCreationStatusCache();
                handleDebugLog("Removing data from the application creation status cache for application key : " +
                               applicationKey);
                applicationCreationStatusCache.remove(appCreationStatusCacheKey);
                return true;
            }
            handleException("Setting application creation status is failed for application key : " + applicationKey);
        } catch (SQLException e) {
            try {
                if (databaseConnection != null) {
                    databaseConnection.rollback();
                }
            } catch (SQLException e1) {

                // Only logging this exception since this is not the main issue. The original issue is thrown.
                log.error("Error while rolling back Setting application creation status for application key : " +
                          applicationKey, e1);
            }
            handleException("Setting application creation status is failed for application key : " + applicationKey, e);
        } finally {
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }
        return false;
    }

    /**
     * Getter method for Application creation status
     *
     * @param applicationKey Application key of an application
     * @return {@link org.wso2.carbon.appfactory.core.util.Constants.ApplicationCreationStatus}
     * @throws AppFactoryException
     */
    public Constants.ApplicationCreationStatus getApplicationCreationStatus(String applicationKey) throws
                                                                                                   AppFactoryException {
        // We check in the cache
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String applicationCreationStatusCacheKey = JDBCApplicationCacheManager.constructApplicationCreationCacheKey
                (tenantId, applicationKey);
        Cache<String, Constants.ApplicationCreationStatus> applicationCreationStatusCache =
                JDBCApplicationCacheManager.getApplicationCreationStatusCache();
        if (applicationCreationStatusCache.containsKey(applicationCreationStatusCacheKey)) {
            handleDebugLog("Retrieving data from the application creation status cache for application key : " +
                           applicationKey);
            return applicationCreationStatusCache.get(applicationCreationStatusCacheKey);
        }

        // No cache hit
        Connection databaseConnection = null;
        PreparedStatement getAppIDPreparedStatement = null;
        ResultSet application = null;

        // The default status should be none. Otherwise if there are no results, AF will treat the app as completed or pending
        Constants.ApplicationCreationStatus status = Constants.ApplicationCreationStatus.NONE;
        try {
            databaseConnection = AppFactoryDBUtil.getConnection();
            getAppIDPreparedStatement = databaseConnection.prepareStatement(SQLConstants
                                                                                    .GET_APPLICATION_CREATION_STATUS_SQL);
            getAppIDPreparedStatement.setString(1, applicationKey);
            getAppIDPreparedStatement.setInt(2, tenantId);
            application = getAppIDPreparedStatement.executeQuery();
            if (application.next()) {
                status = Constants.ApplicationCreationStatus.valueOf(application.getString(SQLParameterConstants
                                                                                                   .COLUMN_NAME_STATUS));
                // We add to cache here
                applicationCreationStatusCache.put(applicationCreationStatusCacheKey, status);
            }
        } catch (SQLException e) {
            handleException("Error while getting application creation status for application key : " + applicationKey, e);
        } finally {
            AppFactoryDBUtil.closeResultSet(application);
            AppFactoryDBUtil.closePreparedStatement(getAppIDPreparedStatement);
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }
        return status;
    }

    /**
     * Getter method for Application creation status
     *
     * @return Map of appKey-{@link org.wso2.carbon.appfactory.core.util.Constants.ApplicationCreationStatus} as
     * key-value pairs.
     * @throws AppFactoryException
     */
    public Map<String, Constants.ApplicationCreationStatus> getApplicationCreationStatusByKeys(String[] appKeyArray)
            throws AppFactoryException {

        Map<String, Constants.ApplicationCreationStatus> applicationMap = new HashMap<String, Constants
                .ApplicationCreationStatus>();

        // We retrieve from the cache
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        Cache<String, Constants.ApplicationCreationStatus> appCreationStatusCache = JDBCApplicationCacheManager.
                                                                                   getApplicationCreationStatusCache();
        handleDebugLog("Retrieving data from the application creation status cache for multiple application keys");
        boolean readFromDB = false;
        for (String applicationKey : appKeyArray) {
            String appCreationStatusCacheKey = JDBCApplicationCacheManager.constructApplicationCreationCacheKey
                    (tenantId, applicationKey);
            if (appCreationStatusCache.containsKey(appCreationStatusCacheKey)) {
                applicationMap.put(applicationKey, appCreationStatusCache.get(appCreationStatusCacheKey));
            } else {

                // This means that one of the entries are missing in the cache. So we ignore the previously retrieved
                // entries as well. We do a direct DB call to fetch the complete result set.
                handleDebugLog("Retrieving data from application creation cache has been aborted due to missing " +
                               "application information in the cache. Retrieving data from the database");
                readFromDB = true;
                break;
            }
        }
        if(readFromDB) {
            applicationMap.clear();
            Connection databaseConnection = null;
            PreparedStatement getAppCreationStatusStatement = null;
            ResultSet result = null;
            String applicationKeys = StringUtils.join(appKeyArray, ",");
            try {
                databaseConnection = AppFactoryDBUtil.getConnection();
                String formattedPreparedSql = String.format(SQLConstants.GET_APPLICATION_CREATION_STATUS_BY_APPKEYS_SQL,
                                                            preparePlaceHolders(appKeyArray.length));
                getAppCreationStatusStatement = databaseConnection.prepareStatement(formattedPreparedSql);
                getAppCreationStatusStatement.setInt(1, tenantId);
                setValues(getAppCreationStatusStatement, 2, appKeyArray);
                result = getAppCreationStatusStatement.executeQuery();
                while (result.next()) {
                    String applicationKey = result.getString(SQLParameterConstants.COLUMN_NAME_APPLICATION_KEY);
                    Constants.ApplicationCreationStatus status = Constants.ApplicationCreationStatus.valueOf(
                            result.getString(SQLParameterConstants.COLUMN_NAME_STATUS));
                    applicationMap.put(applicationKey, status);

                    // We add values to the cache here.
                    appCreationStatusCache.put(applicationKey, status);
                }
            } catch (SQLException e) {
                handleException("Error while getting application creation status of " + applicationKeys, e);
            } finally {
                AppFactoryDBUtil.closeResultSet(result);
                AppFactoryDBUtil.closePreparedStatement(getAppCreationStatusStatement);
                AppFactoryDBUtil.closeConnection(databaseConnection);
            }
        }
        return applicationMap;
    }

    /**
     * Give total branch count of an application including master
     *
     * @param applicationKey key of the application
     * @return number of versions in the application
     * @throws AppFactoryException
     */
    public int getBranchCount(String applicationKey) throws AppFactoryException {
        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        int branchCount = -1;

        //create a key for cache
        String applicationAppsBranchCountKey = JDBCApplicationCacheManager.constructApplicationBranchCountCacheKey(
                tenantID, applicationKey);

        //get the cache
        Cache<String,Integer> applicationBranchCountCache = JDBCApplicationCacheManager.getApplicationBranchCountCache();
        //return the result which is already in cache
        if (applicationBranchCountCache.containsKey(applicationAppsBranchCountKey)) {
            if (log.isDebugEnabled()) {
                log.debug("Retrieving data from the cache for application key : " + applicationKey);
            }
            return applicationBranchCountCache.get(applicationAppsBranchCountKey);
        }

        Connection databaseConnection = null;
        PreparedStatement getAppIDPreparedStatement = null;
        ResultSet application = null;
        try {
            databaseConnection = AppFactoryDBUtil.getConnection();
            getAppIDPreparedStatement = databaseConnection.prepareStatement(SQLConstants
                                                                                    .GET_APPLICATION_BRANCH_COUNT_SQL);
            getAppIDPreparedStatement.setString(1, applicationKey);
            getAppIDPreparedStatement.setInt(2, tenantID);
            getAppIDPreparedStatement.setInt(3, 0);
            application = getAppIDPreparedStatement.executeQuery();
            if (application.next()) {
                branchCount = application.getInt(SQLParameterConstants.COLUMN_NAME_BRANCH_COUNT);
                if (branchCount > 0) {
                    applicationBranchCountCache.put(applicationAppsBranchCountKey, branchCount);
                }
            }
        } catch (SQLException e) {
            handleException("Error while getting branch count for " + applicationKey, e);
        } finally {
            AppFactoryDBUtil.closeResultSet(application);
            AppFactoryDBUtil.closePreparedStatement(getAppIDPreparedStatement);
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }
        return branchCount;
    }

    /**
     * Update build status of a app version.Update will replace existing values
     *
     * @param applicationKey key of an application
     * @param version        version name
     * @param isForked       true if it a forked version
     * @param username       the user who is forked
     * @param buildStatus    build status
     * @return true if it successful false if it failed
     * @throws AppFactoryException
     */
    public boolean updateLastBuildStatus(String applicationKey, String version, boolean isForked, String username,
                                         BuildStatus buildStatus) throws AppFactoryException {
        Connection databaseConnection = null;
        PreparedStatement preparedStatement = null;
        try {
            databaseConnection = AppFactoryDBUtil.getConnection();
            preparedStatement = databaseConnection.prepareStatement(SQLConstants.UPDATE_LAST_BUILD_STATUS_SQL);
            int repositoryID = getRepositoryID(applicationKey, isForked, username, version, databaseConnection);
            preparedStatement.setString(1, buildStatus.getLastBuildId());
            preparedStatement.setString(2, buildStatus.getLastBuildStatus());
            preparedStatement.setTimestamp(3, new Timestamp(buildStatus.getLastBuildTime()));
            preparedStatement.setInt(4, repositoryID);
            preparedStatement.execute();
            int affectedRows = preparedStatement.getUpdateCount();
            if (affectedRows > 0) {
                databaseConnection.commit();

                // We remove the entry from the cache
                int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
                String buildStatusCacheKey = JDBCApplicationCacheManager.constructBuildStatusCacheKey(tenantId,
                                                                        applicationKey, version, isForked, username);
                Cache<String, BuildStatus> buildStatusCache = JDBCApplicationCacheManager.
                        getApplicationBuildStatusCache();
                handleDebugLog("Removing data from the build status cache for application key : " + applicationKey);
                buildStatusCache.remove(buildStatusCacheKey);
                return true;
            }
            handleException("Update latest build is failed for version " + version + " of application key : " +
                            applicationKey);
        } catch (SQLException e) {
            try {
                if (databaseConnection != null) {
                    databaseConnection.rollback();
                }
            } catch (SQLException e1) {

                //Only logging this exception since this is not the main issue. The original issue is thrown.
                log.error("Error while rolling back Update latest build for version : " + version + " of " +
                          "application key : " + applicationKey, e1);
            }
            handleException("Update latest build is failed for version : " + version + " of application key : " +
                            applicationKey, e);

        } finally {
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }
        return false;
    }

    /**
     * Getter method for getting application version build status
     *
     * @param applicationKey key of an application
     * @param version        version name
     * @param isForked       true if it a forked version
     * @param username       the user who is forked
     * @return true if it successful false if it failed
     * @throws AppFactoryException
     */
    public BuildStatus getBuildStatus(String applicationKey, String version, boolean isForked, String username)
            throws AppFactoryException {
        // We check whether it is in the cache
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String buildStatusCacheKey = JDBCApplicationCacheManager.constructBuildStatusCacheKey(tenantId, applicationKey,
                                                                                          version, isForked, username);
        Cache<String, BuildStatus> buildStatusCache = JDBCApplicationCacheManager.getApplicationBuildStatusCache();
        if (buildStatusCache.containsKey(buildStatusCacheKey)) {
            handleDebugLog("Retrieving data from the build status cache for application key : " + applicationKey);
            return buildStatusCache.get(buildStatusCacheKey);
        }

        // No cache hit
        Connection databaseConnection = null;
        PreparedStatement getAppLastBuildPreparedStatement = null;
        ResultSet buildResultSet = null;

        BuildStatus buildStatus = new BuildStatus();
        try {
            databaseConnection = AppFactoryDBUtil.getConnection();
            getAppLastBuildPreparedStatement = databaseConnection.prepareStatement(
                    SQLConstants.GET_APPLICATION_LAST_BUILD_SQL);
            int repositoryID = getRepositoryID(applicationKey, isForked, username, version, databaseConnection);
            getAppLastBuildPreparedStatement.setInt(1, repositoryID);
            buildResultSet = getAppLastBuildPreparedStatement.executeQuery();
            if (buildResultSet.next()) {
                buildStatus.setLastBuildId(buildResultSet.getString(SQLParameterConstants.COLUMN_NAME_LAST_BUILD_ID));
                buildStatus.setLastBuildStatus(buildResultSet.getString(SQLParameterConstants.
                                                                                COLUMN_NAME_LAST_BUILD_STATUS));
                Timestamp lastBuildTime = buildResultSet.getTimestamp(SQLParameterConstants.
                                                                              COLUMN_NAME_LAST_BUILD_TIME);
                if (lastBuildTime != null) {
                    buildStatus.setLastBuildTime(lastBuildTime.getTime());
                }
                // We cache it here
                buildStatusCache.put(buildStatusCacheKey, buildStatus);
            } else {
                handleDebugLog("There is no result for query  get build status application key: " + applicationKey +
                               " version : " + version + " isForked : " + isForked + " " + "username : " + username);
            }
        } catch (SQLException e) {
            handleException("Error while getting build status for version : " + version + " of application key : " +
                            applicationKey, e);
        } finally {
            AppFactoryDBUtil.closeResultSet(buildResultSet);
            AppFactoryDBUtil.closePreparedStatement(getAppLastBuildPreparedStatement);
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }
        return buildStatus;
    }

    /**
     * Update method for deploy status
     *
     * @param applicationKey key of an application
     * @param version        version name
     * @param isForked       true if it a forked version
     * @param username       the user who is forked
     * @param status         deploy status
     * @return true if it successful false if it failed
     * @throws AppFactoryException
     */
    public boolean updateLastDeployStatus(String applicationKey, String version, String environment, boolean isForked,
                                          String username, DeployStatus status) throws AppFactoryException {
        Connection databaseConnection = null;
        PreparedStatement preparedStatement = null;
        try {
            databaseConnection = AppFactoryDBUtil.getConnection();
            preparedStatement = databaseConnection.prepareStatement(SQLConstants.UPDATE_DEPLOY_STATUS_SQL);
            int repositoryID = getRepositoryID(applicationKey, isForked, username, version, databaseConnection);

            preparedStatement.setString(1, status.getLastDeployedStatus());
            if (status.getLastDeployedTime() == 0L) {
                preparedStatement.setTimestamp(2, null);
            } else {
                preparedStatement.setTimestamp(2, new Timestamp(status.getLastDeployedTime()));
            }
            preparedStatement.setInt(3, repositoryID);
            preparedStatement.setString(4, environment);
            preparedStatement.execute();
            int affectedRows = preparedStatement.getUpdateCount();
            if (affectedRows > 0) {
                databaseConnection.commit();

                // We remove the cache entry here.
                int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
                String deployCacheKey = JDBCApplicationCacheManager.constructDeployStatusCacheKey(applicationKey,
                                                                   tenantId, version, environment, isForked, username);
                Cache<String, DeployStatus> deployCache = JDBCApplicationCacheManager.getApplicationDeployStatusCache();
                handleDebugLog("Removing data from the deployment status cache for application key : " + applicationKey);
                deployCache.remove(deployCacheKey);
                return true;
            }
            handleException("Error while updating deploy status for version : " + version + " of application key : " +
                            applicationKey);
        } catch (SQLException e) {
            try {
                if (databaseConnection != null) {
                    databaseConnection.rollback();
                }
            } catch (SQLException e1) {

                // Only logging this exception since this is not the main issue. The original issue is thrown.
                log.error("Error while rolling back update deploy status for version : " + version + " of application " +
                          "key : " + applicationKey, e1);
            }
            handleException("Error while updating deploy status for version : " + version + " of application key : " +
                            applicationKey, e);
        } finally {
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }
        return false;
    }

    /**
     * Update the last deployed id.This will be the build number of the artifact that is
     * submitted for deployment
     *
     * @param applicationKey key of an application
     * @param version        version name
     * @param isForked       true if it a forked version
     * @param username       the user who is forked
     * @param buildID        build number
     * @return true or false
     * @throws AppFactoryException
     */
    public boolean updateLastDeployedBuildID(String applicationKey, String version, String environment,
                                             boolean isForked, String username, String buildID)
            throws AppFactoryException {
        Connection databaseConnection = null;
        PreparedStatement preparedStatement = null;
        try {
            databaseConnection = AppFactoryDBUtil.getConnection();
            preparedStatement = databaseConnection.prepareStatement(SQLConstants
                                                                            .UPDATE_DEPLOYED_BUILD_ID_IN_DEPLOY_STATUS_SQL);
            int repositoryID = getRepositoryID(applicationKey, isForked, username, version, databaseConnection);
            preparedStatement.setString(1, buildID);
            preparedStatement.setInt(2, repositoryID);
            preparedStatement.setString(3, environment);
            preparedStatement.execute();
            int affectedRows = preparedStatement.getUpdateCount();
            if (affectedRows > 0) {
                databaseConnection.commit();

                // We remove the cache entry here before the return.
                int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
                String deployCacheKey = JDBCApplicationCacheManager.constructDeployStatusCacheKey(applicationKey,
                                                                    tenantId, version, environment, isForked, username);
                Cache<String, DeployStatus> deployCache = JDBCApplicationCacheManager.getApplicationDeployStatusCache();
                handleDebugLog("Removing data from the deployment status cache for application key : " + applicationKey);
                deployCache.remove(deployCacheKey);
                return true;
            }
            handleException("Error while updating deployed build id for version : " + version + " application key : " +
                            applicationKey);
        } catch (SQLException e) {
            try {
                if (databaseConnection != null) {
                    databaseConnection.rollback();
                }
            } catch (SQLException e1) {

                // Only logging this exception since this is not the main issue. The original issue is thrown.
                log.error("Error while rolling back update deployed build id for version : " + version + " of " +
                          "application key : " + applicationKey, e1);
            }
            handleException("Error while updating deployed build id for version : " + version + " of application " +
                            "key : " + applicationKey, e);
        } finally {
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }
        return false;
    }

    /**
     * Getter for last deploy status
     *
     * @param applicationKey key of an application
     * @param version        version name
     * @param environment    stage
     * @param isForked       true if it a forked version
     * @param username       the user who is forked
     * @return {@link DeployStatus}
     * @throws AppFactoryException
     */
    public DeployStatus getDeployStatus(String applicationKey, String version, String environment, boolean isForked,
                                        String username) throws AppFactoryException {
        // We get the value from cache
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String deployCacheKey = JDBCApplicationCacheManager.constructDeployStatusCacheKey(applicationKey, tenantId,
                                version, environment, isForked, username);
        Cache<String, DeployStatus> deployCache = JDBCApplicationCacheManager.getApplicationDeployStatusCache();
        if (deployCache.containsKey(deployCacheKey)) {
            handleDebugLog("Retrieving data from the deployment status cache for application key : " + applicationKey);
            return deployCache.get(deployCacheKey);
        }

        // No cache hit
        Connection databaseConnection = null;
        PreparedStatement getAppLastDeployPreparedStatement = null;
        ResultSet deployResultSet = null;

        DeployStatus deployStatus = new DeployStatus();
        try {
            databaseConnection = AppFactoryDBUtil.getConnection();
            getAppLastDeployPreparedStatement = databaseConnection.prepareStatement(SQLConstants
                                                                              .GET_APPLICATION_LAST_DEPLOY_SQL);
            int repositoryID = getRepositoryID(applicationKey, isForked, username, version, databaseConnection);
            getAppLastDeployPreparedStatement.setInt(1, repositoryID);
            getAppLastDeployPreparedStatement.setString(2, environment);
            deployResultSet = getAppLastDeployPreparedStatement.executeQuery();
            if (deployResultSet.next()) {
                deployStatus.setLastDeployedId(deployResultSet.getString(SQLParameterConstants.
                                                                                 COLUMN_NAME_LAST_DEPLOYED_BUILD_ID));
                deployStatus.setLastDeployedStatus(deployResultSet.getString(
                        SQLParameterConstants.COLUMN_NAME_LAST_DEPLOY_STATUS));
                Timestamp deployedTime = deployResultSet.getTimestamp(
                        SQLParameterConstants.COLUMN_NAME_LAST_DEPLOY_TIME);
                if (deployedTime != null) {
                    deployStatus.setLastDeployedTime(deployedTime.getTime());
                }

                // We cache the successful result set here.
                deployCache.put(deployCacheKey, deployStatus);
            }
        } catch (SQLException e) {
            handleException(" Error while getting deploy status for version : " + version + " of application key : " +
                            applicationKey, e);
        } finally {
            AppFactoryDBUtil.closeResultSet(deployResultSet);
            AppFactoryDBUtil.closePreparedStatement(getAppLastDeployPreparedStatement);
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }

        return deployStatus;
    }

    /**
     * Adding a branch to user's forked application space
     *
     * @param applicationKey key of an application
     * @param version        version number
     * @param username       user name of the user who is forking
     * @return true if it successful false if it failed
     * @throws AppFactoryException
     */
    public boolean forkApplicationVersion(String applicationKey, String version, String username)
            throws AppFactoryException {
        Connection databaseConnection = null;
        try {
            databaseConnection = AppFactoryDBUtil.getConnection();
            int applicationID = getAutoIncrementAppID(applicationKey, databaseConnection);
            int versionID = getAutoIncrementVersionID(applicationKey, version, databaseConnection);
            addRepository(versionID, true, username, databaseConnection);
            databaseConnection.commit();
        } catch (SQLException e) {
            try {
                if (databaseConnection != null) {
                    databaseConnection.rollback();
                }
            } catch (SQLException e1) {

                // Only logging this exception since this is not the main issue. The original issue is thrown.
                log.error("Error while rolling back forked application version : " + version + " of application key : "
                          + applicationKey, e1);
            }
            handleException("Error while forking application version : " + version + " of application key : "+ applicationKey, e);
        } finally {
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }
        return false;
    }

    /**
     * Get all the applications of the current tenant
     *
     * @return array of {@link Application}
     * @throws AppFactoryException
     */
    public Application[] getAllApplications() throws AppFactoryException {
        Connection databaseConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet allApplications = null;
        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        List<Application> applications = new ArrayList<Application>();
        try {
            databaseConnection = AppFactoryDBUtil.getConnection();
            preparedStatement = databaseConnection.prepareStatement(SQLConstants.GET_ALL_APPLICATIONS_SQL);
            preparedStatement.setInt(1, tenantID);
            allApplications = preparedStatement.executeQuery();
            Application application;
            while (allApplications.next()) {
                application = new Application();
                application.setId(allApplications.getString(SQLParameterConstants.COLUMN_NAME_APPLICATION_KEY));
                application.setApplicationCreationStatus(Constants.ApplicationCreationStatus.valueOf(
                        allApplications.getString(SQLParameterConstants.COLUMN_NAME_STATUS)));
                application.setBranchCount(getBranchCount(application.getId()));
                applications.add(application);
            }
        } catch (SQLException e) {
            handleException("Error while getting all applications ", e);
        } finally {
            AppFactoryDBUtil.closeResultSet(allApplications);
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }
        return applications.toArray(new Application[applications.size()]);
    }

    /**
     * Helper method to add a empty holder for deploy status
     *
     * @param repositoryID       repository ID
     * @param environment        stage
     * @param databaseConnection existing db connection
     * @return true if it success ,false if it failed
     * @throws AppFactoryException
     */
    private boolean addDeployStatus(int repositoryID, String environment, Connection databaseConnection)
            throws AppFactoryException {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = databaseConnection.prepareStatement(SQLConstants.ADD_DEPLOY_STATUS_SQL);
            preparedStatement.setInt(1, repositoryID);
            preparedStatement.setInt(2, CarbonContext.getThreadLocalCarbonContext().getTenantId());
            preparedStatement.execute();
            int affectedRow = preparedStatement.getUpdateCount();
            if (affectedRow > 0) {
                return true;
            }
            handleException("Error while inserting deploy status for repository : " + repositoryID);
        } catch (SQLException e) {
            handleException("Error while inserting deploy status for repository : " + repositoryID, e);
        } finally {
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
        }

        return false;
    }

    /**
     * Helper method to add a empty holder for build status
     *
     * @param repositoryID       repo id
     * @param databaseConnection existing connection
     * @return true if it success ,false if it failed
     * @throws AppFactoryException
     */
    private boolean addBuildStatus(int repositoryID, Connection databaseConnection) throws AppFactoryException {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = databaseConnection.prepareStatement(SQLConstants.ADD_BUILD_STATUS_SQL);
            preparedStatement.setInt(1, repositoryID);
            preparedStatement.setInt(2, CarbonContext.getThreadLocalCarbonContext().getTenantId());
            preparedStatement.execute();
            int affectedRow = preparedStatement.getUpdateCount();
            if (affectedRow > 0) {
                return true;
            }
            handleException("Error while inserting build status for repository : " + repositoryID);
        } catch (SQLException e) {
            handleException("Error while inserting build status for repository : " + repositoryID, e);
        } finally {
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
        }

        return false;
    }

    /**
     * Retrieve the auto generated application id from table
     *
     * @param applicationKey     application key of an application
     * @param databaseConnection existing connection
     * @return application id
     * @throws AppFactoryException
     */
     int getAutoIncrementAppID(String applicationKey, Connection databaseConnection) throws AppFactoryException {
        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String applicationIdCacheKey = JDBCApplicationCacheManager.constructApplicationIdCacheKey(tenantID,
                                                                                                  applicationKey);
        Cache<String, Integer> applicationIdCache = JDBCApplicationCacheManager.getApplicationIdCache();
        if (applicationIdCache.containsKey(applicationIdCacheKey)) {
            handleDebugLog("Retrieving data from the cache for application key : " + applicationKey);
            return applicationIdCache.get(applicationIdCacheKey);
        }

        // No cache hit
        PreparedStatement getAppIDPreparedStatement = null;
        ResultSet application = null;
        int applicationID = -1;
        try {
            getAppIDPreparedStatement = databaseConnection.prepareStatement(SQLConstants.GET_APPLICATION_SQL);
            getAppIDPreparedStatement.setString(1, applicationKey);
            getAppIDPreparedStatement.setInt(2, tenantID);
            application = getAppIDPreparedStatement.executeQuery();
            if (application.next()) {
                applicationID = application.getInt(SQLParameterConstants.COLUMN_NAME_ID);

                // Add to the cache here
                applicationIdCache.put(applicationIdCacheKey, applicationID);
            }
            handleDebugLog("Getting AF_APPLICATION ID " + applicationID + " for application key : " + applicationKey);
        } catch (SQLException e) {
            handleException("Error while getting application id of application " + applicationKey, e);
        } finally {
            AppFactoryDBUtil.closeResultSet(application);
            AppFactoryDBUtil.closePreparedStatement(getAppIDPreparedStatement);
        }
        return applicationID;
    }

    /**
     * Retrieve the auto generated version id from table
     *
     * @param applicationKey     application key
     * @param version            application version
     * @param databaseConnection existing connection
     * @return application id
     * @throws AppFactoryException
     */
    private int getAutoIncrementVersionID(String applicationKey, String version, Connection databaseConnection)
            throws AppFactoryException {
        PreparedStatement getAppIDPreparedStatement = null;
        ResultSet versionResultSet = null;
        int versionID = -1;
        try {
            getAppIDPreparedStatement = databaseConnection.prepareStatement(SQLConstants.GET_APPLICATION_VERSION_ID_SQL);
            getAppIDPreparedStatement.setInt(1, JDBCApplicationDAO.getInstance().getAutoIncrementAppID(applicationKey,
                                                                                                       databaseConnection));
            getAppIDPreparedStatement.setString(2, version);
            versionResultSet = getAppIDPreparedStatement.executeQuery();
            if (versionResultSet.next()) {
                versionID = versionResultSet.getInt(SQLParameterConstants.COLUMN_NAME_ID);
            }
            handleDebugLog("Getting AF_VERSION ID " + versionID + " for application key : " + applicationKey);
        } catch (SQLException e) {
            handleException("Error while getting version id of version : " + version + " of application key : "
                            + applicationKey, e);
        } finally {
            AppFactoryDBUtil.closeResultSet(versionResultSet);
            AppFactoryDBUtil.closePreparedStatement(getAppIDPreparedStatement);
        }
        return versionID;
    }

    /**
     * Retrieve the auto generated repo id from table
     *
     * @param versionID version id from table
     * @param isFork    whether the repo is forked or not
     * @param userID    user name
     * @param databaseConnection
     * @return
     * @throws AppFactoryException
     */
    private int getAutoIncrementRepositoryID(int versionID, boolean isFork, String userID, Connection databaseConnection)
            throws AppFactoryException {
        PreparedStatement repositoryIDPreparedStatement = null;
        ResultSet repositoryResultSet = null;
        int repositoryID = -1;
        try {
            if (isFork) {
                repositoryIDPreparedStatement = databaseConnection.prepareStatement(
                        SQLConstants.GET_FORKED_APPLICATION_REPOSITORY_ID_SQL);
                repositoryIDPreparedStatement.setInt(1, versionID);
                repositoryIDPreparedStatement.setString(2, userID);
            } else {
                repositoryIDPreparedStatement = databaseConnection.prepareStatement(
                        SQLConstants.GET_APPLICATION_REPOSITORY_ID_SQL);
                repositoryIDPreparedStatement.setInt(1, versionID);
            }
            repositoryResultSet = repositoryIDPreparedStatement.executeQuery();
            if (repositoryResultSet.next()) {
                repositoryID = repositoryResultSet.getInt(SQLParameterConstants.COLUMN_NAME_ID);
            }

            //debug log
            handleDebugLog("Getting Repository ID " + repositoryID + " for version ID " + versionID + " and is fork "
                           + Boolean.toString(isFork) + " and user ID " + userID);
        } catch (SQLException e) {
            handleException("Error while getting repository id for versionID " + versionID, e);
        } finally {
            AppFactoryDBUtil.closeResultSet(repositoryResultSet);
            AppFactoryDBUtil.closePreparedStatement(repositoryIDPreparedStatement);
        }
        return repositoryID;
    }

    private int getRepositoryID(String applicationKey, boolean isForked, String username, String version,
                                Connection dataConnection) throws AppFactoryException {
        int applicationID = getAutoIncrementAppID(applicationKey, dataConnection);
        int versionID = getAutoIncrementVersionID(applicationKey, version, dataConnection);
        return getAutoIncrementRepositoryID(versionID, isForked, username, dataConnection);
    }

    /**
     * Add New CartridgeCluster
     *
     * @param cartridgeCluster {@link org.wso2.carbon.appfactory.core.dto.CartridgeCluster}
     * @return true if it is successful, false if it is failed
     * @throws AppFactoryException if there is a problem in creating CartridgeCluster
     */
    public boolean addCartridgeCluster(CartridgeCluster cartridgeCluster) throws AppFactoryException {
        boolean result = false;
        if (cartridgeCluster != null) {
            Connection databaseConnection = null;
            PreparedStatement preparedStatement = null;
            try {
                databaseConnection = AppFactoryDBUtil.getConnection();
                preparedStatement = databaseConnection.prepareStatement(SQLConstants.ADD_CARTRIDGE_CLUSTER_SQL);
                preparedStatement.setString(1, cartridgeCluster.getClusterId());
                preparedStatement.setString(2, cartridgeCluster.getLbClusterId());
                preparedStatement.setString(3, cartridgeCluster.getActiveIP());
                preparedStatement.setInt(4, CarbonContext.getThreadLocalCarbonContext().getTenantId());
                preparedStatement.execute();
                databaseConnection.commit();
                int updatedRowCount = preparedStatement.getUpdateCount();
                if (updatedRowCount > 0) {
                    result = true;
                }
            } catch (SQLException e) {
                try {
                    if (databaseConnection != null) {
                        databaseConnection.rollback();
                    }
                } catch (SQLException e1) {

                    // no need to throw since this is not related to business logic
                    String msg = "Error while rolling back the updating cartridge for cluster Id : " +
                                 cartridgeCluster.getClusterId();
                    log.error(msg, e1);
                }
                handleException("Updating cartridge is failed for clusterId : " + cartridgeCluster.getClusterId(), e);
            } finally {
                AppFactoryDBUtil.closePreparedStatement(preparedStatement);
                AppFactoryDBUtil.closeConnection(databaseConnection);
            }
        }
        return result;
    }

    /**
     * Update CartridgeCluster
     *
     * @param cartridgeCluster {@link org.wso2.carbon.appfactory.core.dto.CartridgeCluster}
     * @return true if it successful, false if it is failed
     * @throws AppFactoryException if there is a problem in creating CartridgeCluster
     */
    public boolean updateCartridgeCluster(CartridgeCluster cartridgeCluster) throws AppFactoryException {
        boolean result = false;
        if (cartridgeCluster != null) {
            Connection databaseConnection = null;
            PreparedStatement preparedStatement = null;
            try {
                databaseConnection = AppFactoryDBUtil.getConnection();
                preparedStatement = databaseConnection.prepareStatement(SQLConstants.UPDATE_CARTRIDGE_CLUSTER_SQL);
                preparedStatement.setString(1, cartridgeCluster.getLbClusterId());
                preparedStatement.setString(2, cartridgeCluster.getActiveIP());
                preparedStatement.setString(3, cartridgeCluster.getClusterId());
                preparedStatement.execute();
                databaseConnection.commit();
                int updatedRowCount = preparedStatement.getUpdateCount();
                if (updatedRowCount > 0) {
                    result = true;
                }
            } catch (SQLException e) {
                try {
                    if (databaseConnection != null) {
                        databaseConnection.rollback();
                    }
                } catch (SQLException e1) {

                    // no need to throw since, this is not related to business logic
                    String msg = "Error while rolling back the added cartridge";
                    log.error(msg, e1);
                }
                handleException("Adding cartridge is failed for clusterId : " + cartridgeCluster.getClusterId(), e);
            } finally {
                AppFactoryDBUtil.closePreparedStatement(preparedStatement);
                AppFactoryDBUtil.closeConnection(databaseConnection);
            }
        }
        return result;
    }

    /**
     * Get CartridgeCluster
     *
     * @param clusterId cluster id of the CartridgeCluster
     * @return {@link CartridgeCluster}, If there is no data, return null
     * @throws AppFactoryException if there is a problem in getting CartridgeCluster
     */
    public CartridgeCluster getCartridgeClusterByClusterId(String clusterId) throws AppFactoryException {
        CartridgeCluster cartridgeCluster = null;
        if (StringUtils.isNotBlank(clusterId)) {
            Connection databaseConnection = null;
            PreparedStatement preparedStatement = null;
            ResultSet buildResultSet = null;
            try {
                databaseConnection = AppFactoryDBUtil.getConnection();
                preparedStatement = databaseConnection.prepareStatement(
                        SQLConstants.GET_CARTRIDGE_CLUSTER_BY_CLUSTER_ID_SQL);
                preparedStatement.setString(1, clusterId);
                buildResultSet = preparedStatement.executeQuery();
                if (buildResultSet.next()) {
                    cartridgeCluster = new CartridgeCluster();
                    cartridgeCluster.setClusterId(buildResultSet.getString(SQLParameterConstants.
                                                                                   COLUMN_NAME_CLUSTER_ID));
                    cartridgeCluster.setLbClusterId(buildResultSet.getString(
                            SQLParameterConstants.COLUMN_NAME_LB_CLUSTER_ID));
                    cartridgeCluster.setActiveIP(buildResultSet.getString(SQLParameterConstants.COLUMN_NAME_ACTIVE_IP));
                }
            } catch (SQLException e) {
                handleException("Error while getting cartridge of cluster id : " + clusterId, e);
            } finally {
                AppFactoryDBUtil.closeResultSet(buildResultSet);
                AppFactoryDBUtil.closePreparedStatement(preparedStatement);
                AppFactoryDBUtil.closeConnection(databaseConnection);
            }
        }
        return cartridgeCluster;
    }

    /**
     * Prepare number of place holders specified by the {@code length} place holders for SQL IN clause. Some JDBC
     * drivers seem to support @{link PreparedStatement#setArray} on the IN clause for mysql. Therefore we have to
     * decide at the time of the prepareStatement() the number of place holders(?) needed for query.
     *
     * @param length number of place holders to be  build
     * @return comma separated "?" marks of length {@code length}.
     * e.g
     * length = 0  => ""
     * length = 1  => "?"
     * length = 3  => "?,?,?"
     */
    private String preparePlaceHolders(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; ) {
            builder.append("?");
            if (++i < length) {
                builder.append(",");
            }
        }
        return builder.toString();
    }

    /**
     * Set values of the {@code preparedStatement}.
     *
     * @param preparedStatement statement
     * @param firstIndex        first parameter index, {@code values} to be set
     * @param values            values to be set
     * @throws SQLException
     */
    private void setValues(PreparedStatement preparedStatement, int firstIndex, Object... values) throws SQLException {
        for (int i = 0; i < values.length; i++) {
            preparedStatement.setObject(firstIndex + i, values[i]);
        }
    }

    /**
     * This method will be used to clear application related information from all the caches
     *
     * @param applicationKey The application key of the deleted application.
     * @throws org.wso2.carbon.appfactory.common.AppFactoryException if failed to get the
     *                                                               application name from the application key
     */
    private void removeApplicationDataFromAllCaches(String applicationKey) throws AppFactoryException {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        // We ge the cache key prefix here.
        // This prefix will help us to delete all the cache entries related to this application.
        String cacheKeyPrefix = JDBCApplicationCacheManager.constructCacheKeyPrefix(tenantId, applicationKey);

        // Remove from deployStatusCache
        Cache<String, DeployStatus> deployStatusCache = JDBCApplicationCacheManager.getApplicationDeployStatusCache();
        Iterator<Cache.Entry<String, DeployStatus>> deployStatusIterator = deployStatusCache.iterator();
        while (deployStatusIterator.hasNext()) {
            Cache.Entry<String, DeployStatus> entry = deployStatusIterator.next();
            if (entry.getKey().startsWith(cacheKeyPrefix)) {
                deployStatusIterator.remove();
            }
        }

        // Remove from buildStatusCache
        Cache<String, BuildStatus> buildStatusCache = JDBCApplicationCacheManager.getApplicationBuildStatusCache();
        Iterator<Cache.Entry<String, BuildStatus>> buildStatusIterator = buildStatusCache.iterator();
        while (buildStatusIterator.hasNext()) {
            Cache.Entry<String, BuildStatus> entry = buildStatusIterator.next();
            if (entry.getKey().startsWith(cacheKeyPrefix)) {
                buildStatusIterator.remove();
            }
        }

        // Remove from applicationCreationStatusCache
        Cache<String, Constants.ApplicationCreationStatus> applicationCreationStatusCache =
                JDBCApplicationCacheManager.getApplicationCreationStatusCache();
        Iterator<Cache.Entry<String, Constants.ApplicationCreationStatus>> appCreationStatusCacheIterator =
                applicationCreationStatusCache.iterator();

        while (appCreationStatusCacheIterator.hasNext()) {
            Cache.Entry<String, Constants.ApplicationCreationStatus> entry = appCreationStatusCacheIterator.next();
            if (entry.getKey().startsWith(cacheKeyPrefix)) {
                appCreationStatusCacheIterator.remove();
            }
        }

        // Removing data from application name cache
        String applicationName = getApplicationName(applicationKey);
        if (applicationName != null) {
            Cache<String, Boolean> applicationNameCache = JDBCApplicationCacheManager.getJDBCApplicationNameCache();
            String appNameCacheKey = JDBCApplicationCacheManager.constructAppNameCacheKey(applicationName, tenantId);
            applicationNameCache.remove(appNameCacheKey);
            // debug log
            handleDebugLog("Successfully removed from Application Name : " + applicationName);
        }

        // Removing data from application id cache
        Cache<String, Integer> applicationIdCache = JDBCApplicationCacheManager.getApplicationIdCache();
        String applicationIdCacheKey = JDBCApplicationCacheManager.constructApplicationIdCacheKey(tenantId,
                                                                                                  applicationKey);
        applicationIdCache.remove(applicationIdCacheKey);

        //removing data from application branch count cache
        Cache<String, Integer> applicationBranchCountCache =
                JDBCApplicationCacheManager.getApplicationBranchCountCache();
        String applicationBranchCountCacheKey = JDBCApplicationCacheManager
                .constructApplicationBranchCountCacheKey(tenantId, applicationKey);
        applicationBranchCountCache.remove(applicationBranchCountCacheKey);
    }
}
