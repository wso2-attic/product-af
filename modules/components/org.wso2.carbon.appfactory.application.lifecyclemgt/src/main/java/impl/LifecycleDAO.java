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
import org.wso2.carbon.appfactory.core.dao.JDBCAppVersionDAO;
import org.wso2.carbon.appfactory.core.dto.Version;
import org.wso2.carbon.appfactory.core.util.GovernanceUtil;
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

import java.util.ArrayList;

/**
 * Manage lifecycle details of application artifacts
 */
public class LifecycleDAO {

    private static final String RXT_APPINFO_LIFECYCLE_NAME = "serviceLifecycle_lifecycleName";

    Log log = LogFactory.getLog(LifecycleDAO.class);

    /**
     * Method to retrieve the application (appInfo/appVersion) artifact  from registry
     *
     * @param appKey       id of the application
     * @param artifactName version/ app info artifact name of the application
     * @return Generic artifact object of the given application id
     * @throws AppFactoryException
     */
    public GenericArtifact getAppArtifact(String appKey, String artifactName, String tenantDomain)
            throws AppFactoryException, LifecycleManagementException {
        PrivilegedCarbonContext carbonContext;
        GenericArtifact artifact = null;
        try {
            PrivilegedCarbonContext.startTenantFlow();
            carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain(tenantDomain, true);

            UserRegistry userRegistry = GovernanceUtil.getUserRegistry();
            String resourcePath = getAppRegistryPath(appKey, artifactName);

            if (!userRegistry.resourceExists(resourcePath)) {
                String msg = "Unable to load resources of application :"+appKey+" of the tenant :"+tenantDomain;
                log.error(msg);
                throw new LifecycleManagementException(msg);
            }

            Resource resource = userRegistry.get(resourcePath);
            GovernanceUtils.loadGovernanceArtifacts(userRegistry);
            GenericArtifactManager artifactManager;

            if (artifactName.equals(AppFactoryConstants.APPLICATION_ARTIFACT_NAME)) {
                artifactManager = new GenericArtifactManager(userRegistry,
                        AppFactoryConstants.RXT_KEY_APPINFO_APPLICATION);
            } else {
                artifactManager = new GenericArtifactManager(userRegistry, AppFactoryConstants.RXT_KEY_APPVERSION);
            }
            artifact = artifactManager.getGenericArtifact(resource.getUUID());

        } catch (RegistryException e) {
            String errorMsg =
                    "Unable to load application information of the application :" + appKey + " of the tenant :"
                            + tenantDomain;
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return artifact;
    }

    /**
     * Method to construct the app info/app version registry path for a given application
     *
     * @param artifactName  version of the application
     * @param applicationId id of the application
     */
    private String getAppRegistryPath(String applicationId, String artifactName) {
        return AppFactoryConstants.REGISTRY_APPLICATION_PATH + RegistryConstants.PATH_SEPARATOR + applicationId
                + RegistryConstants.PATH_SEPARATOR + artifactName;
    }

    /**
     * Method to retrieve life cycle names
     *
     * @return String array of life cycle names
     */
    public String[] getLifeCycleList() throws AppFactoryException {
        String[] lifecycleList;
        LifeCycleManagementService lifeCycleManagementService = new LifeCycleManagementService();
        try {
            lifecycleList = lifeCycleManagementService.getLifecycleList();
            if (lifecycleList == null) {
                String msg = "Unable to load list of life cycles from LifeCycleManagementService";
                log.error(msg);
                throw new LifecycleManagementException(msg);
            }
        } catch (Exception e) {
            String errorMsg = "Error occurred while getting the list of lifecycle from LifeCycleManagementService";
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }
        return lifecycleList;
    }

    /**
     * Method to retrieve content of the life cycle configuration file.
     *
     * @param lifecycleName name of the life cycle
     * @return lifeCycle configuration file
     */
    public String getLifeCycleConfiguration(String lifecycleName) throws AppFactoryException {
        String lifeCycleFile;
        LifeCycleManagementService lifeCycleManagementService = new LifeCycleManagementService();
        try {
            lifeCycleFile = lifeCycleManagementService.getLifecycleConfiguration(lifecycleName);
        } catch (Exception e) {
            String errorMsg =
                    "Error while loading the lifecycle configuration file for the life cycle :" + lifecycleName;
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }
        return lifeCycleFile;
    }

    /**
     * Method to retrieve life cycle name of a given application
     *
     * @param appKey     name of application key
     * @param appVersion version of the application
     * @return life cycle name for the application
     */
    public String getLifeCycleName(String appKey, String appVersion, String tenantDomain)
            throws AppFactoryException, LifecycleManagementException {
        PrivilegedCarbonContext carbonContext;
        String lifecycleName = null;
        try {
            PrivilegedCarbonContext.startTenantFlow();
            carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain(tenantDomain, true);

            GenericArtifact artifact = getAppArtifact(appKey, appVersion, tenantDomain);
            if (artifact != null) {
                lifecycleName = artifact.getAttribute(RXT_APPINFO_LIFECYCLE_NAME);
            } else {
                String msg = "Error while loading the application version details of the application id :" + appKey
                        + " with the version :" + appVersion + " of the tenant :" + tenantDomain;
                log.error(msg);
                throw new LifecycleManagementException(msg);
            }
        } catch (GovernanceException e) {
            String errorMsg =
                    "Error while loading life cycle name of the application " + appKey + " with the version"
                            + appVersion + "of the tenant :" + tenantDomain;
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        } catch (AppFactoryException e) {
            String errorMsg =
                    "Error while loading details of the application " + appKey + " with the version" + appVersion
                            + "of the tenant :" + tenantDomain;
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return lifecycleName;
    }

    /**
     * Method to change life cycle name of a given version of an application
     *
     * @param appKey       name of application key
     * @param appVersion   version of the application
     * @param tenantDomain tenant domain
     */
    public void updateAppVersionLifeCycle(String appKey, String tenantDomain, String appVersion)
            throws AppFactoryException, LifecycleManagementException {

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        carbonContext.setTenantDomain(tenantDomain, true);

        GenericArtifact artifact = getAppArtifact(appKey, appVersion, tenantDomain);
        if (artifact == null) {
            String msg =
                    "Unable to load application details of application :" + appKey + " with the application version"
                            + appVersion + " of the tenant :" + tenantDomain;
            log.error(msg);
            throw new LifecycleManagementException(msg);
        } else {
            try {
                String appInfoLifeCycleName = getArtifactLifecycleName(appKey, tenantDomain);
                if (appInfoLifeCycleName == null || artifact.getLifecycleName().equals(appInfoLifeCycleName)) {
                    String errorMsg =
                            "Error while updating the artifact :" + appKey + " with the application version :"
                                    + appVersion + " of the tenant :" + tenantDomain;
                    log.error(errorMsg);
                    throw new LifecycleManagementException(errorMsg);
                }

                artifact.attachLifecycle(appInfoLifeCycleName);
                artifact.setAttribute(RXT_APPINFO_LIFECYCLE_NAME, appInfoLifeCycleName);

                UserRegistry userRegistry = GovernanceUtil.getUserRegistry();
                GovernanceUtils.loadGovernanceArtifacts(userRegistry);
                GenericArtifactManager artifactManager = new GenericArtifactManager(userRegistry,
                        AppFactoryConstants.RXT_KEY_APPVERSION);
                artifactManager.updateGenericArtifact(artifact);
                JDBCApplicationCacheManager.getApplicationArtifactCache().remove(appKey);

            } catch (RegistryException e) {
                String errorMsg =
                        "Error while updating the artifact :" + appKey + " with the application version :" + appVersion
                                + " of the tenant :" + tenantDomain;
                log.error(errorMsg, e);
                throw new AppFactoryException(errorMsg, e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    /**
     * Method to retrieve life cycle name of a given appInfo artifact
     *
     * @param appKey name of application key
     * @return life cycle name
     */
    private String getArtifactLifecycleName(String appKey, String tenantDomain)
            throws LifecycleManagementException, AppFactoryException {
        String lifecycleName = null;
        PrivilegedCarbonContext carbonContext;
        try {

            PrivilegedCarbonContext.startTenantFlow();
            carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain(tenantDomain, true);

            GenericArtifact artifact = getAppArtifact(appKey, AppFactoryConstants.APPLICATION_ARTIFACT_NAME,
                    tenantDomain);
            if (artifact != null) {
                lifecycleName = artifact.getLifecycleName();
            } else {
                String errorMsg = "Error while loading the artifact :" + appKey + " of the tenant :" + tenantDomain;
                log.error(errorMsg);
                throw new LifecycleManagementException(errorMsg);
            }
        } catch (AppFactoryException e) {
            String errorMsg =
                    "Error while loading artifact for the application :" + appKey + " of the tenant :" + tenantDomain;
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        } catch (GovernanceException e) {
            String errorMsg = "Error while loading life cycle name of the application :" + appKey + " of the tenant :"
                    + tenantDomain;
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();

        }
        return lifecycleName;
    }

    /**
     * Method to check whether changing life cycle name for an application is valid or not
     * (Lifecycle name can be changed only if no version is created by the user)
     *
     * @param appKey name of application key
     * @return true/false
     */
    public boolean isAppLifecycleChangeValid(String appKey, String tenantDomain)
            throws LifecycleManagementException, AppFactoryException {
        boolean status = false;
        ArrayList<Version> versionNames;
        PrivilegedCarbonContext carbonContext;
        try {
            PrivilegedCarbonContext.startTenantFlow();
            carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain(tenantDomain, true);

            versionNames = JDBCAppVersionDAO.getInstance().getAllVersionsOfApplication(appKey);
            //Assume that app versions can not be deleted from an application
            if (versionNames.size() == 1) {
                if (AppFactoryConstants.TRUNK.equals(versionNames.get(0).getVersion()) ||
                        AppFactoryConstants.INITIAL_UPLOADED_APP_VERSION.equals(versionNames.get(0).getVersion())) {
                    status = true;
                }
            } else {
                String errorMsg =
                        "Lifecycle can not be changed in the application :" + appKey + " of the tenant :"
                                + tenantDomain;
                log.error(errorMsg);
                throw new LifecycleManagementException(errorMsg);
            }
        } catch (AppFactoryException e) {
            String errorMsg =
                    "Error while loading application versions of the application :" + appKey + " of the tenant :"
                            + tenantDomain;
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();

        }
        return status;
    }

    /**
     * Method to change life cycle name of a given application version artifact
     *
     * @param appKey        name of application key
     * @param tenantDomain  tenant domain
     * @param lifecycleName new life cycle name
     */
    public void setAppInfoLifecycleName(String appKey, String lifecycleName, String tenantDomain)
            throws AppFactoryException, LifecycleManagementException {
        PrivilegedCarbonContext carbonContext;

        try {
            PrivilegedCarbonContext.startTenantFlow();
            carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain(tenantDomain, true);
            //Get the appinfo artifact and attach lifecycle name
            GenericArtifact appInfoArtifact =
                    getAppArtifact(appKey, AppFactoryConstants.APPLICATION_ARTIFACT_NAME, tenantDomain);
            appInfoArtifact.attachLifecycle(lifecycleName);
            //update each version of the application with the new lifecycle name
            ArrayList<Version> versionNames = JDBCAppVersionDAO.getInstance().getAllVersionsOfApplication(appKey);
            if (versionNames != null) {
                for (Version versionName : versionNames) {
                    updateAppVersionLifeCycle(appKey, tenantDomain, versionName.getVersion());
                }
            }
            UserRegistry userRegistry = GovernanceUtil.getUserRegistry();
            GovernanceUtils.loadGovernanceArtifacts(userRegistry);
            GenericArtifactManager artifactManager = new GenericArtifactManager(userRegistry,
                    AppFactoryConstants.RXT_KEY_APPINFO_APPLICATION);
            artifactManager.updateGenericArtifact(appInfoArtifact);
            JDBCApplicationCacheManager.getApplicationArtifactCache().remove(appKey);

        } catch (RegistryException e) {
            String errorMsg =
                    "Error while updating the artifact " + appKey + " with the lifecycle name :" + lifecycleName
                            + " of the tenant :" + tenantDomain;
            log.error(errorMsg);
            throw new AppFactoryException(errorMsg);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

}
