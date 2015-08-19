/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.cache.JDBCApplicationCacheManager;
import org.wso2.carbon.appfactory.core.sql.SQLConstants;
import org.wso2.carbon.appfactory.core.sql.SQLParameterConstants;
import org.wso2.carbon.appfactory.core.util.AppFactoryDBUtil;
import org.wso2.carbon.appfactory.core.util.GovernanceUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.lcm.services.LifeCycleManagementService;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LifecycleDAO {

    private static String RXT_APPINFO_LIFECYCLE_NAME = "serviceLifecycle_lifecycleName";

    Log log = LogFactory.getLog(LifecycleDAO.class);

    /**
     * Method to retrieve the application artifact from registry
     *
     * @param appKey id of the application
     * @param appVersion version of the application
     * @return Generic artifact object of the given application id
     * @throws AppFactoryException
     */
    public GenericArtifact getAppArtifact(String appKey,String appVersion)
            throws AppFactoryException {
        PrivilegedCarbonContext carbonContext;
        GenericArtifact artifact = null;
        try {
            //-----------------------------------------------------------------------------------
            PrivilegedCarbonContext.startTenantFlow();
            carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain("xyz.com", true);
            //-----------------------------------------------------------------------------------
            UserRegistry userRegistry = GovernanceUtil.getUserRegistry();
            String resourcePath = getAppInfoRegistryPath(appKey,appVersion);

            if(!userRegistry.resourceExists(resourcePath)){
                return null;
            }

            Resource resource = userRegistry.get(resourcePath);
            GovernanceUtils.loadGovernanceArtifacts(userRegistry);
            GenericArtifactManager artifactManager;

            if(appVersion.equals(AppFactoryConstants.APPLICATION_ARTIFACT_NAME)) {
                artifactManager = new GenericArtifactManager(userRegistry,
                        AppFactoryConstants.RXT_KEY_APPINFO_APPLICATION);
            }else{
                artifactManager = new GenericArtifactManager(userRegistry,
                        AppFactoryConstants.RXT_KEY_APPVERSION);
            }
            artifact = artifactManager.getGenericArtifact(resource.getUUID());

        } catch (RegistryException e) {
            String errorMsg = "Unable to load application information for " +
                    "application id : " + appKey;
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return artifact;
    }

    /**
     * Method to construct the app info registry path for a given application
     *@param appVersion version of the application
     * @param applicationId id of the application
     *
     */
    private String getAppInfoRegistryPath(String applicationId,String appVersion) {
        String resourcePath = AppFactoryConstants.REGISTRY_APPLICATION_PATH +
                RegistryConstants.PATH_SEPARATOR + applicationId + RegistryConstants.PATH_SEPARATOR;
        if(appVersion.equals(AppFactoryConstants.APPLICATION_ARTIFACT_NAME)) {
            return resourcePath + AppFactoryConstants.APPLICATION_ARTIFACT_NAME;
        }else{
            return resourcePath + appVersion;
        }
    }

    /**
     * Method to retrieve life cycle names
     * @return String array of life cycle names
     *
     */
    public String[] getLifeCycleList() throws AppFactoryException {
        String[] lifecycleList;
        LifeCycleManagementService lifeCycleManagementService = new LifeCycleManagementService();
        try {
            lifecycleList = lifeCycleManagementService.getLifecycleList();
        } catch (Exception e) {
            String errorMsg = "Unable to load lifecycle names";
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }
        return lifecycleList;
    }

    /**
     * Method to retrieve life cycle xml file
     * @param lifecycleName name of the life cycle
     * @return lifeCycle configuration file
     *
     */
    public String getLifeCycleConfiguration(String lifecycleName) throws AppFactoryException {
        String lifeCycleFile;
        LifeCycleManagementService lifeCycleManagementService = new LifeCycleManagementService();
        try {
            lifeCycleFile = lifeCycleManagementService.getLifecycleConfiguration(lifecycleName);
        } catch (Exception e) {
            String errorMsg = "Error while loading life cycle configuration file";
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }
        return lifeCycleFile;
    }

    /**
     * Method to retrieve life cycle name
     * @param appKey name of application key
     * @param appVersion version of the application
     * @return life cycle name for the application
     *
     */
    public String getLifeCycleName(String appKey,String appVersion) throws AppFactoryException {
        PrivilegedCarbonContext carbonContext;
        String lifecycleName = null;
        try {
            //-----------------------------------------------------------------------------------
            PrivilegedCarbonContext.startTenantFlow();
            carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain("xyz.com", true);
            //-----------------------------------------------------------------------------------
            GenericArtifact artifact = getAppArtifact(appKey, appVersion);
            if (artifact != null) {
                lifecycleName = artifact.getAttribute(RXT_APPINFO_LIFECYCLE_NAME);
            }
        }
        catch (GovernanceException e) {
            String errorMsg = "Error while loading life cycle name for the application "
                    + appKey + " of the version" + appVersion;
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }catch (AppFactoryException e){
            String errorMsg = "Error while loading details of the application "
                    + appKey + "of the version" + appVersion;
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }
        finally{
            PrivilegedCarbonContext.endTenantFlow();
        }
        return lifecycleName;
    }

    /**
     * Method to change life cycle name of a given application
     * @param appKey name of application key
     * @param appVersion version of the application
     * @param tenantDomain tenant domain
     *
     */
    public void updateAppInfo(String appKey,String tenantDomain,String appVersion)
            throws AppFactoryException, LifecycleManagementException {
        PrivilegedCarbonContext carbonContext;
        GenericArtifact artifact = getAppArtifact(appKey, appVersion);
        try {
            //-----------------------------------------------------------------------------------
            PrivilegedCarbonContext.startTenantFlow();
            carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain(tenantDomain, true);
            //-----------------------------------------------------------------------------------
            String newValue = getArtifactLifecycleName(appKey);
            if(newValue == null){
                String errorMsg = "Error while updating the artifact :" + appKey+" " +
                        "application version :"+appVersion+". No life cycle is attached to the artifact.";
                log.error(errorMsg);
                throw new LifecycleManagementException(errorMsg);
            }

            artifact.attachLifecycle(newValue);
            artifact.setAttribute(RXT_APPINFO_LIFECYCLE_NAME, newValue);

            UserRegistry userRegistry = GovernanceUtil.getUserRegistry();
            GovernanceUtils.loadGovernanceArtifacts(userRegistry);
            GenericArtifactManager artifactManager = new GenericArtifactManager(userRegistry,
                    AppFactoryConstants.RXT_KEY_APPVERSION);
            artifactManager.updateGenericArtifact(artifact);
            JDBCApplicationCacheManager.getApplicationArtifactCache().remove(appKey);

        } catch (RegistryException e) {
            String errorMsg = "Error while updating the artifact :" + appKey+" " +
                    "application version :"+appVersion;
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }
        finally{
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Method to retrieve life cycle name of a given appinfo artifact
     * @param appKey name of application key
     * @return life cycle name
     *
     */
    private String getArtifactLifecycleName(String appKey) throws LifecycleManagementException {
        String lifecycleName = null;
        PrivilegedCarbonContext carbonContext;
        try {
            //-----------------------------------------------------------------------------------
            PrivilegedCarbonContext.startTenantFlow();
            carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain("xyz.com", true);
            //-----------------------------------------------------------------------------------
            GenericArtifact artifact = getAppArtifact(appKey, AppFactoryConstants.APPLICATION_ARTIFACT_NAME);
            if(artifact != null) {
                lifecycleName = artifact.getLifecycleName();
            }else{
                String errorMsg = "Error while loading the artifact :" + appKey;
                log.error(errorMsg);
                throw new LifecycleManagementException(errorMsg);
            }
        }catch (AppFactoryException e) {
            log.error("Error while loading artifact for the application :"+appKey);
        }catch (GovernanceException e){
            log.error("Error while loading life cycle name of the application :"+appKey);
        }
        return lifecycleName;
    }

    /**
     * Method to check whether changing life cycle name for an application is valid or not
     * @param appKey name of application key
     * @return true/false
     *
     */
    public boolean isAppLifecycleChangeValid(String appKey) throws LifecycleManagementException {
        boolean status = false;
        String[] versionNames = null;
        try {
            versionNames = getAllVersionsOfApplication(appKey);
        } catch (AppFactoryException e) {
            log.error("Error while loading application versions of the application :"+appKey);
        }
        if(versionNames != null && versionNames.length == 1) {
            if (versionNames[0].equals(AppFactoryConstants.TRUNK) ||
                    versionNames[0].equals(AppFactoryConstants.INITIAL_UPLOADED_APP_VERSION)){
                status = true;
            }
        }else{
            String errorMsg = "Life cycle can not be changed in the application :" + appKey;
            log.error(errorMsg);
            throw new LifecycleManagementException(errorMsg);
        }
        return status;
    }

    /**
     * Method to change life cycle name of a given application version artifact
     * @param appKey name of application key
     * @param tenantDomain tenant domain
     * @param lifecycleName new life cycle name
     *
     */
    public void setAppInfoLifecycleName(String appKey,String lifecycleName, String tenantDomain)
            throws AppFactoryException, LifecycleManagementException {
        PrivilegedCarbonContext carbonContext;

        try {
            //-----------------------------------------------------------------------------------
            PrivilegedCarbonContext.startTenantFlow();
            carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain(tenantDomain, true);
            //-----------------------------------------------------------------------------------
            GenericArtifact artifact = getAppArtifact(appKey, AppFactoryConstants.APPLICATION_ARTIFACT_NAME);
            artifact.attachLifecycle(lifecycleName);

            String[] versionNames = getAllVersionsOfApplication(appKey);
            if(versionNames!= null) {
                for (String versionName : versionNames) {
                    updateAppInfo(appKey, tenantDomain, versionName);
                }
            }
            UserRegistry userRegistry = GovernanceUtil.getUserRegistry();
            GovernanceUtils.loadGovernanceArtifacts(userRegistry);
            GenericArtifactManager artifactManager = new GenericArtifactManager(userRegistry,
                    AppFactoryConstants.RXT_KEY_APPINFO_APPLICATION);
            artifactManager.updateGenericArtifact(artifact);
            JDBCApplicationCacheManager.getApplicationArtifactCache().remove(appKey);

        } catch (RegistryException e) {
            log.error("Error while updating the artifact " + appKey);
        }
        finally{
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * This method is used to get a list of all versions of the an application
     *
     * @param appKey The application application key of the current application
     * @return array of artifact versions
     * @throws AppFactoryException if SQL operation fails
     */

    private String[] getAllVersionsOfApplication(String appKey) throws AppFactoryException {
        Connection databaseConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<String> versionList = null;
        PrivilegedCarbonContext carbonContext;
        try {
            //-----------------------------------------------------------------------------------
            PrivilegedCarbonContext.startTenantFlow();
            carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain("xyz.com", true);
            //-----------------------------------------------------------------------------------
            databaseConnection = AppFactoryDBUtil.getConnection();
            preparedStatement = databaseConnection.prepareStatement(SQLConstants.GET_ALL_VERSIONS_OF_APPLICATION);
            preparedStatement.setString(1, appKey);
            preparedStatement.setInt(2, CarbonContext.getThreadLocalCarbonContext().getTenantId());
            resultSet = preparedStatement.executeQuery();
            versionList = new ArrayList<String>();
            while (resultSet.next()) {
                versionList.add(resultSet.getString(SQLParameterConstants.COLUMN_NAME_VERSION_NAME));
            }
        } catch (SQLException e) {
            log.error("Error while getting all the version of application key : " + appKey);
        } finally {
            AppFactoryDBUtil.closeResultSet(resultSet);
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }
        if (versionList != null) {
            return versionList.toArray(new String[versionList.size()]);
        }
        return null;
    }


}
