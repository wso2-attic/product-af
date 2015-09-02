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

import javax.ws.rs.core.Response;
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
     * @throws LifecycleManagementException
     */
    public GenericArtifact getAppArtifact(String appKey, String artifactName, String tenantDomain)
            throws LifecycleManagementException {
        PrivilegedCarbonContext carbonContext;
        GenericArtifact artifact = null;
        try {
            PrivilegedCarbonContext.startTenantFlow();
            carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain(tenantDomain, true);

            UserRegistry userRegistry = GovernanceUtil.getUserRegistry();
            String resourcePath = getAppRegistryPath(appKey, artifactName);

            if (!userRegistry.resourceExists(resourcePath)) {
                String errorMsg =
                        "Unable to load resources of application :" + appKey + " of the tenant :" + tenantDomain;
                log.error(errorMsg);
                throw new LifecycleManagementException(errorMsg, Response.Status.NOT_FOUND);
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
            throw new LifecycleManagementException(errorMsg, Response.Status.INTERNAL_SERVER_ERROR);
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
     * @throws LifecycleManagementException
     */
    private String getAppRegistryPath(String applicationId, String artifactName) {
        return AppFactoryConstants.REGISTRY_APPLICATION_PATH + RegistryConstants.PATH_SEPARATOR + applicationId
                + RegistryConstants.PATH_SEPARATOR + artifactName;
    }

    /**
     * Method to retrieve life cycle names
     *
     * @return String array of life cycle names
     * @throws LifecycleManagementException
     */
    public String[] getLifeCycleList() throws LifecycleManagementException {
        String[] lifecycleList;
        LifeCycleManagementService lifeCycleManagementService = new LifeCycleManagementService();
        try {
            lifecycleList = lifeCycleManagementService.getLifecycleList();
            if (lifecycleList == null) {
                String errorMsg = "Unable to load list of life cycles from LifeCycleManagementService";
                log.error(errorMsg);
                throw new LifecycleManagementException(errorMsg, Response.Status.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            String errorMsg = "Error occurred while getting the list of lifecycle from LifeCycleManagementService";
            log.error(errorMsg, e);
            throw new LifecycleManagementException(errorMsg, Response.Status.INTERNAL_SERVER_ERROR);
        }
        return lifecycleList;
    }

    /**
     * Method to retrieve content of the life cycle configuration file.
     *
     * @param lifecycleName name of the life cycle
     * @return lifeCycle configuration file
     * @throws LifecycleManagementException
     */
    public String getLifeCycleConfiguration(String lifecycleName) throws LifecycleManagementException {
        String lifeCycleFile;
        LifeCycleManagementService lifeCycleManagementService = new LifeCycleManagementService();
        try {
            lifeCycleFile = lifeCycleManagementService.getLifecycleConfiguration(lifecycleName);
        } catch (Exception e) {
            String errorMsg =
                    "Error while loading the lifecycle configuration file for the life cycle :" + lifecycleName;
            log.error(errorMsg, e);
            throw new LifecycleManagementException(errorMsg, Response.Status.NOT_FOUND);
        }
        return lifeCycleFile;
    }

    /**
     * Method to retrieve life cycle name of a given application
     *
     * @param appKey     name of application key
     * @param appVersion version of the application
     * @return life cycle name for the application
     * @throws LifecycleManagementException
     */
    public String getLifeCycleName(String appKey, String appVersion, String tenantDomain)
            throws LifecycleManagementException {
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
                String errorMsg = "Error while loading the application version details of the application id :" + appKey
                        + " with the version :" + appVersion + " of the tenant :" + tenantDomain;
                log.error(errorMsg);
                throw new LifecycleManagementException(errorMsg, Response.Status.NOT_FOUND);
            }
        } catch (GovernanceException e) {
            String errorMsg =
                    "Error while loading life cycle name of the application " + appKey + " with the version"
                            + appVersion + "of the tenant :" + tenantDomain;
            log.error(errorMsg, e);
            throw new LifecycleManagementException(errorMsg, Response.Status.INTERNAL_SERVER_ERROR);
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
     * @throws LifecycleManagementException
     */
    public void updateAppVersionLifeCycle(String appKey, String tenantDomain, String appVersion)
            throws LifecycleManagementException {

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        carbonContext.setTenantDomain(tenantDomain, true);

        GenericArtifact artifact = getAppArtifact(appKey, appVersion, tenantDomain);
        if (artifact == null) {
            String errorMsg =
                    "Unable to load application details of application :" + appKey + " with the application version"
                            + appVersion + " of the tenant :" + tenantDomain;
            log.error(errorMsg);
            throw new LifecycleManagementException(errorMsg, Response.Status.NOT_FOUND);
        } else {
            try {
                String appInfoLifeCycleName = getArtifactLifecycleName(appKey, tenantDomain);
                if (appInfoLifeCycleName == null || artifact.getLifecycleName().equals(appInfoLifeCycleName)) {
                    String errorMsg =
                            "Error while updating the artifact :" + appKey + " with the application version :"
                                    + appVersion + " of the tenant :" + tenantDomain;
                    log.error(errorMsg);
                    throw new LifecycleManagementException(errorMsg, Response.Status.BAD_REQUEST);
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
                throw new LifecycleManagementException(errorMsg, Response.Status.INTERNAL_SERVER_ERROR);
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
     * @throws LifecycleManagementException
     */
    private String getArtifactLifecycleName(String appKey, String tenantDomain)
            throws LifecycleManagementException {
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
                throw new LifecycleManagementException(errorMsg, Response.Status.NOT_FOUND);
            }
        } catch (GovernanceException e) {
            String errorMsg = "Error while loading life cycle name of the application :" + appKey + " of the tenant :"
                    + tenantDomain;
            log.error(errorMsg, e);
            throw new LifecycleManagementException(errorMsg, Response.Status.INTERNAL_SERVER_ERROR);
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
     * @throws LifecycleManagementException
     */
    public boolean isAppLifecycleChangeValid(String appKey, String lifecycleName, String tenantDomain)
            throws LifecycleManagementException {
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
                if (AppFactoryConstants.TRUNK.equals(versionNames.get(0).getVersion()) ||
                        AppFactoryConstants.INITIAL_UPLOADED_APP_VERSION.equals(versionNames.get(0).getVersion())) {
                    //Check whether the default application lifecycle name and the
                    // new lifecycle selected by the user are same
                    GenericArtifact artifact = getAppArtifact(appKey, versionNames.get(0).getVersion(), tenantDomain);
                    if (!artifact.getLifecycleName().equals(lifecycleName)) {
                        status = true;
                    }
                }
            }
        } catch (AppFactoryException e) {
            String errorMsg =
                    "Error while loading application versions of the application :" + appKey + " of the tenant :"
                            + tenantDomain;
            log.error(errorMsg, e);
            throw new LifecycleManagementException(errorMsg, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (GovernanceException e) {
            String errorMsg =
                    "Error while loading application version details of the application :" + appKey + " of the tenant :"
                            + tenantDomain;
            log.error(errorMsg, e);
            throw new LifecycleManagementException(errorMsg, Response.Status.INTERNAL_SERVER_ERROR);
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
     * @throws LifecycleManagementException
     */
    public void setAppInfoLifecycleName(String appKey, String lifecycleName, String tenantDomain)
            throws LifecycleManagementException {
        PrivilegedCarbonContext carbonContext;

        try {
            PrivilegedCarbonContext.startTenantFlow();
            carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain(tenantDomain, true);
            //Get the appinfo artifact and attach lifecycle name
            GenericArtifact appInfoArtifact =
                    getAppArtifact(appKey, AppFactoryConstants.APPLICATION_ARTIFACT_NAME, tenantDomain);
            appInfoArtifact.attachLifecycle(lifecycleName);

            UserRegistry userRegistry = GovernanceUtil.getUserRegistry();
            GovernanceUtils.loadGovernanceArtifacts(userRegistry);
            GenericArtifactManager artifactManager = new GenericArtifactManager(userRegistry,
                    AppFactoryConstants.RXT_KEY_APPINFO_APPLICATION);
            artifactManager.updateGenericArtifact(appInfoArtifact);
            JDBCApplicationCacheManager.getApplicationArtifactCache().remove(appKey);

        } catch (RegistryException e) {
            String errorMsg =
                    "Error while updating the application artifact " + appKey + " with the lifecycle name :"
                            + lifecycleName + " of the tenant :" + tenantDomain;
            log.error(errorMsg);
            throw new LifecycleManagementException(errorMsg, Response.Status.INTERNAL_SERVER_ERROR);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Method to check whether application life cycle is changed by the user or not.
     * (This is used to check whether the user has changed the lifecycle before creating a new branch for an app)
     *
     * @param appKey application key
     * @return true/false
     * @throws LifecycleManagementException
     */
    public boolean isAppLifecycleChanged(String appKey, String tenantDomain)
            throws LifecycleManagementException {
        boolean status = false;
        LifecycleDAO dao = new LifecycleDAO();
        PrivilegedCarbonContext carbonContext;
        try {
            PrivilegedCarbonContext.startTenantFlow();
            carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain(tenantDomain, true);

            if (dao.getAppArtifact(appKey,
                    AppFactoryConstants.APPLICATION_ARTIFACT_NAME, tenantDomain).getLifecycleName() != null) {
                status = true;
            }

        } catch (GovernanceException e) {
            String errorMsg =
                    "Error while checking lifecycle of the application :" + appKey + " of the tenant :" + tenantDomain;
            log.error(errorMsg, e);
            throw new LifecycleManagementException(errorMsg, Response.Status.INTERNAL_SERVER_ERROR);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return status;
    }

    /**
     * Update each version of the application with the lifecycle name in appInfo artifact
     *
     * @param appKey       name of application key
     * @param tenantDomain tenant domain
     * @throws LifecycleManagementException
     */
    public void updateAppVersionList(String appKey, String tenantDomain) {
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
        } catch (AppFactoryException e) {
            String errorMsg =
                    "Error while loading application versions of the application :" + appKey + " of the tenant :"
                            + tenantDomain;
            log.error(errorMsg, e);
            throw new LifecycleManagementException(errorMsg, Response.Status.INTERNAL_SERVER_ERROR);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

}
