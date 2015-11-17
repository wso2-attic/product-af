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
package org.wso2.carbon.appfactory.core.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.cache.JDBCApplicationCacheManager;
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
    private static LifecycleDAO lifecycleDAO = new LifecycleDAO();
    Log log = LogFactory.getLog(LifecycleDAO.class);

    private LifecycleDAO() {
    }

    public static LifecycleDAO getInstance() {
        return lifecycleDAO;
    }

    /**
     * Method to retrieve the appinfo artifact  from registry
     *
     * @param appKey id of the application
     * @return Generic artifact object of the given application id
     * @throws AppFactoryException
     */
    public GenericArtifact getAppInfoArtifact(String appKey, String tenantDomain) throws AppFactoryException {
        PrivilegedCarbonContext carbonContext;
        GenericArtifact artifact = null;
        try {
            PrivilegedCarbonContext.startTenantFlow();
            carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain(tenantDomain, true);

            UserRegistry userRegistry = GovernanceUtil.getUserRegistry();
            String resourcePath =
                    AppFactoryConstants.REGISTRY_APPLICATION_PATH + RegistryConstants.PATH_SEPARATOR + appKey
                            + RegistryConstants.PATH_SEPARATOR + AppFactoryConstants.RXT_KEY_APPINFO;

            if (!userRegistry.resourceExists(resourcePath)) {
                String errorMsg =
                        "Unable to load resources of application :" + appKey + " of the tenant :" + tenantDomain;
                log.error(errorMsg);
                throw new AppFactoryException(errorMsg);
            }

            Resource resource = userRegistry.get(resourcePath);
            GovernanceUtils.loadGovernanceArtifacts(userRegistry);
            GenericArtifactManager artifactManager;

            artifactManager = new GenericArtifactManager(userRegistry, AppFactoryConstants.RXT_KEY_APPINFO_APPLICATION);
            artifact = artifactManager.getGenericArtifact(resource.getUUID());

        } catch (RegistryException e) {
            String errorMsg =
                    "Unable to load application information of the application :" + appKey + " of the tenant :"
                            + tenantDomain;
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return artifact;
    }

    /**
     * Method to retrieve content of the life cycle configuration file.
     *
     * @param lifecycleName name of the life cycle
     * @return lifeCycle configuration file
     * @throws AppFactoryException
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
            throw new AppFactoryException(errorMsg);
        }
        return lifeCycleFile;
    }

    /**
     * Method to retrieve life cycle name of a given appInfo artifact
     *
     * @param appKey name of application key
     * @return life cycle name
     * @throws AppFactoryException
     */
    public String getArtifactLifecycleName(String appKey, String tenantDomain) throws AppFactoryException {
        String lifecycleName = null;
        PrivilegedCarbonContext carbonContext;
        try {

            PrivilegedCarbonContext.startTenantFlow();
            carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain(tenantDomain, true);

            GenericArtifact artifact = getAppInfoArtifact(appKey, tenantDomain);
            if (artifact != null) {
                lifecycleName = artifact.getLifecycleName();
            } else {
                String errorMsg = "Error while loading the artifact :" + appKey + " of the tenant :" + tenantDomain;
                log.error(errorMsg);
                throw new AppFactoryException(errorMsg);
            }
        } catch (GovernanceException e) {
            String errorMsg = "Error while loading life cycle name of the application :" + appKey + " of the tenant :"
                    + tenantDomain;
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();

        }
        return lifecycleName;
    }

    /**
     * Method to check whether changing life cycle name for an application is valid or not
     * (Lifecycle name can be changed only if no version is created by the user)
     *
     * @param appKey       name of application key
     * @param tenantDomain tenant domain
     * @return true/false
     * @throws AppFactoryException
     */
    public boolean isAppLifecycleChangeValid(String appKey, String tenantDomain) throws AppFactoryException {
        boolean status = false;
        ArrayList<Version> versionNames;
        PrivilegedCarbonContext carbonContext;
        try {
            PrivilegedCarbonContext.startTenantFlow();
            carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain(tenantDomain, true);

            versionNames = JDBCAppVersionDAO.getInstance().getAllVersionsOfApplication(appKey);
            //Assume that app versions can not be deleted from an application
            //Check whether there is only default app version in an application
            if (versionNames.size() == 1) {
                if (AppFactoryConstants.TRUNK.equals(versionNames.get(0).getVersion())
                        || AppFactoryConstants.INITIAL_UPLOADED_APP_VERSION.equals(versionNames.get(0).getVersion())) {
                    status = true;
                }
            }
        } catch (AppFactoryException e) {
            String errorMsg =
                    "Error while loading application versions of the application :" + appKey + " of the tenant :"
                            + tenantDomain;
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg);
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
     * @throws AppFactoryException
     */
    public void setAppInfoLifecycleName(String appKey, String lifecycleName, String tenantDomain)
            throws AppFactoryException {
        PrivilegedCarbonContext carbonContext;

        try {
            PrivilegedCarbonContext.startTenantFlow();
            carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain(tenantDomain, true);
            //Get the appInfo artifact and attach lifecycle name
            GenericArtifact appInfoArtifact = getAppInfoArtifact(appKey, tenantDomain);
            appInfoArtifact.attachLifecycle(lifecycleName);

            appInfoArtifact.setAttribute(RXT_APPINFO_LIFECYCLE_NAME, lifecycleName);

            UserRegistry userRegistry = GovernanceUtil.getUserRegistry();
            GovernanceUtils.loadGovernanceArtifacts(userRegistry);
            GenericArtifactManager artifactManager = new GenericArtifactManager(userRegistry,
                    AppFactoryConstants.RXT_KEY_APPINFO_APPLICATION);
            artifactManager.updateGenericArtifact(appInfoArtifact);

        } catch (RegistryException e) {
            String errorMsg = "Error while updating the application artifact " + appKey + " with the lifecycle name :"
                    + lifecycleName + " of the tenant :" + tenantDomain;
            log.error(errorMsg);
            throw new AppFactoryException(errorMsg);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Update each version of the application with the lifecycle name in appInfo artifact
     *
     * @param appKey       name of application key
     * @param tenantDomain tenant domain
     * @throws
     */
    public void updateAppVersionList(String appKey, String tenantDomain) throws AppFactoryException {
        PrivilegedCarbonContext carbonContext;
        try {
            PrivilegedCarbonContext.startTenantFlow();
            carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain(tenantDomain, true);

            ArrayList<Version> versionNames = JDBCAppVersionDAO.getInstance().getAllVersionsOfApplication(appKey);
            if (versionNames != null) {
                for (Version versionName : versionNames) {
                    updateAppVersionLifeCycle(appKey, tenantDomain, versionName.getVersion());
                }
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Method to change life cycle name of a given version of an application
     *
     * @param appKey       name of application key
     * @param appVersion   version of the application
     * @param tenantDomain tenant domain
     * @throws
     */
    public void updateAppVersionLifeCycle(String appKey, String tenantDomain, String appVersion)
            throws AppFactoryException {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        carbonContext.setTenantDomain(tenantDomain, true);

        GenericArtifact artifact = getAppVersionArtifact(appKey, tenantDomain, appVersion);
        if (artifact == null) {
            String errorMsg =
                    "Unable to load application details of application :" + appKey + " with the application version"
                            + appVersion + " of the tenant :" + tenantDomain;
            log.error(errorMsg);
            throw new AppFactoryException(errorMsg);
        } else {
            try {
                String appInfoLifeCycleName = getArtifactLifecycleName(appKey, tenantDomain);
                if (appInfoLifeCycleName == null || artifact.getLifecycleName().equals(appInfoLifeCycleName)) {
                    String errorMsg = "Error while updating the artifact :" + appKey + " with the application version :"
                            + appVersion + " of the tenant :" + tenantDomain;
                    log.error(errorMsg);
                    throw new AppFactoryException(errorMsg);
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
                throw new AppFactoryException(errorMsg);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    /**
     * Method to retrieve the appinfo artifact  from registry
     *
     * @param appKey id of the application
     * @return Generic artifact object of the given application id
     * @throws AppFactoryException
     */
    public GenericArtifact getAppVersionArtifact(String appKey, String tenantDomain, String version)
            throws AppFactoryException {
        PrivilegedCarbonContext carbonContext;
        GenericArtifact artifact = null;
        try {
            PrivilegedCarbonContext.startTenantFlow();
            carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain(tenantDomain, true);

            UserRegistry userRegistry = GovernanceUtil.getUserRegistry();
            String resourcePath =
                    AppFactoryConstants.REGISTRY_APPLICATION_PATH + RegistryConstants.PATH_SEPARATOR + appKey
                            + RegistryConstants.PATH_SEPARATOR + version;

            if (!userRegistry.resourceExists(resourcePath)) {
                String errorMsg =
                        "Unable to load resources of application :" + appKey + " of the tenant :" + tenantDomain;
                log.error(errorMsg);
                throw new AppFactoryException(errorMsg);
            }

            Resource resource = userRegistry.get(resourcePath);
            GovernanceUtils.loadGovernanceArtifacts(userRegistry);
            GenericArtifactManager artifactManager;

            artifactManager = new GenericArtifactManager(userRegistry, AppFactoryConstants.RXT_KEY_APPVERSION);
            artifact = artifactManager.getGenericArtifact(resource.getUUID());

        } catch (RegistryException e) {
            String errorMsg =
                    "Unable to load application information of the application :" + appKey + " of the tenant :"
                            + tenantDomain;
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return artifact;
    }

}
