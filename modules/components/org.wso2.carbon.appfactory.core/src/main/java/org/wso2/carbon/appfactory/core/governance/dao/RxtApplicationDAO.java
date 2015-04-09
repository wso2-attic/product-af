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

package org.wso2.carbon.appfactory.core.governance.dao;

import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.dao.JDBCApplicationDAO;
import org.wso2.carbon.appfactory.core.dto.Application;
import org.wso2.carbon.appfactory.core.dto.ApplicationSummary;
import org.wso2.carbon.appfactory.core.governance.cache.RxtApplicationCacheManager;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.appfactory.core.util.Constants;
import org.wso2.carbon.appfactory.core.util.GovernanceUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifactImpl;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains the CRUD operations done on Resources created via application.rxt Meta Data Model
 */
public class RxtApplicationDAO {

    private static RxtApplicationDAO rxtApplicationDAO = new RxtApplicationDAO();
    Log log = LogFactory.getLog(RxtApplicationDAO.class);

    private RxtApplicationDAO() {
    }

    public static RxtApplicationDAO getInstance() {
        return rxtApplicationDAO;
    }

    /**
     * Method to add an application artifact to registry
     *
     * @param info               information of application, that need to be added to the registry
     * @param lifecycleAttribute the name of lifecycle, that should be associated with the application
     * @return registry path of the application artifact
     * @throws AppFactoryException
     */
    public String addApplicationArtifact(String info, String lifecycleAttribute) throws AppFactoryException {
        RegistryUtils.recordStatistics(AppFactoryConstants.RXT_KEY_APPINFO_APPLICATION, info, lifecycleAttribute);
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            UserRegistry userRegistry = GovernanceUtil.getUserRegistry();
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.IS_COALESCING, true);

            XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(info));
            GenericArtifactManager manager = new GenericArtifactManager(userRegistry,
                                                                        AppFactoryConstants.RXT_KEY_APPINFO_APPLICATION);
            GenericArtifact artifact = manager.newGovernanceArtifact(new StAXOMBuilder(reader).getDocumentElement());

            // want to save original content, so set content here
            artifact.setContent(info.getBytes());

            manager.addGenericArtifact(artifact);
            if (lifecycleAttribute != null) {
                String lifecycle = artifact.getAttribute(lifecycleAttribute);
                if (lifecycle != null) {
                    artifact.attachLifecycle(lifecycle);
                }
            }
            return AppFactoryConstants.REGISTRY_GOVERNANCE_PATH + artifact.getPath();
        } catch (Exception e) {
            log.error("Error while adding application artifact in tenant : " + tenantDomain);
            throw new AppFactoryException(e);
        }
    }


    /**
     * Method to retrieve the type of an application
     *
     * @param applicationId id of application
     * @return application type of the given application id
     */
    public String getApplicationType(String applicationId) throws AppFactoryException {
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            GenericArtifact artifact = getAppInfoArtifact(applicationId, tenantDomain);
            return artifact.getAttribute(AppFactoryConstants.RXT_KEY_APPINFO_TYPE);
        } catch (GovernanceException e) {
            throw new AppFactoryException("Error while getting Application type for application: " + applicationId, e);
        }
    }

    /**
     * Method to retrieve all the application of a given user
     *
     * @param userName username of the user, whose applications need to be retrieved
     * @return array of application of the given user
     * @throws AppFactoryException
     */
    public Application[] getAllApplicationsOfUser(String userName) throws AppFactoryException {
        CarbonContext context = CarbonContext.getThreadLocalCarbonContext();
        ArrayList<Application> applications = new ArrayList<Application>();
        try {

            String[] roles =
                    context.getUserRealm().getUserStoreManager()
                            .getRoleListOfUser(userName);
            for (String role : roles) {
                if (role.startsWith(AppFactoryConstants.APP_ROLE_PREFIX)) {
                    String appKeyFromPerAppRoleName = AppFactoryUtil.getAppkeyFromPerAppRoleName(role);
                    applications.add(getApplication(appKeyFromPerAppRoleName));
                }
            }
            return applications.toArray(new Application[applications.size()]);
        } catch (UserStoreException e) {
            String message = "Failed to retrieve applications of the user" + userName;
            throw new AppFactoryException(message, e);
        }
    }

    /**
     * Method to retrieve all the applications created by a given user
     *
     * @param userName username of the user eg: user@tenant.com
     * @return array of applications created by the given user
     * @throws AppFactoryException
     */
    public Application[] getAllApplicationsCreatedByUser(String userName) throws AppFactoryException {
        ArrayList<Application> applicationsCreatedByUser = new ArrayList<Application>();
        String tenantAwareUsername = MultitenantUtils.getTenantAwareUsername(userName);
        Application[] applicationsOfUser = getAllApplicationsOfUser(tenantAwareUsername);
        for (Application application : applicationsOfUser) {
            if (application.getOwner().equals(userName)) {
                applicationsCreatedByUser.add(application);
            }
        }
        return applicationsCreatedByUser.toArray(new Application[applicationsCreatedByUser.size()]);
    }

    /**
     * get application summary from registry
     * @param applicationKeys array of application keys
     * @return Application Summary List
     * @throws AppFactoryException
     */
    public List<ApplicationSummary> getSummarizedApplicationInfo(String[] applicationKeys)
            throws AppFactoryException {
        UserRegistry userRegistry =
                (UserRegistry) CarbonContext.getThreadLocalCarbonContext()
                        .getRegistry(RegistryType.SYSTEM_GOVERNANCE);
        return getApplicationSummary(applicationKeys, userRegistry);
    }

    /**
     * Method to retrieve the application information for given application id
     *
     * @param applicationId id of the application
     * @return application information of the given id
     * @throws AppFactoryException
     */
    public Application getApplicationInfo(String applicationId) throws AppFactoryException {
        return getApplication(applicationId);
    }

    /**
     * Method to retrieve the value of a given attribute of an application
     *
     * @param applicationId id of the application
     * @param key the attribute, which's value need to be retrieve
     * @return value of the given key
     * @throws AppFactoryException
     */
    public String getAppInfoRxtValue(String applicationId, String key, String tenantDomain) throws AppFactoryException {
        GenericArtifactImpl artifact;
        String keyValue;
        try {
            artifact = getAppInfoArtifact(applicationId, tenantDomain);
            keyValue = artifact.getAttribute(key);

        } catch (RegistryException e) {
            String errorMsg =
                    String.format("Unable to load the application information for application id: %s", applicationId);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }

        return keyValue;
    }

    /**
     * Method to update a new value for a given attribute of an application
     *
     * @param applicationId id of the application
     * @param key the attribute, which needs to be updated
     * @param newValue the new value of the given attribute key
     * @throws AppFactoryException
     */
    public void updateAppInfoRxt(String applicationId, String key,
                                 String newValue, String tenantDomain) throws AppFactoryException {
        // creating a unique key for an application in a tenant
        String tenant_appLock = tenantDomain.concat("_").concat(applicationId);

        synchronized (tenant_appLock.intern()) {
            GenericArtifactImpl artifact = getAppInfoArtifact(applicationId, tenantDomain);
            if (log.isDebugEnabled()) {
                log.debug("Updating application information rxt with key : " + key + " value : " + newValue
                          + " applicationId : " + applicationId + " in tenant domain : " + tenantDomain);
            }

            try {
                artifact.setAttribute(key, newValue);
                UserRegistry userRegistry = GovernanceUtil.getUserRegistry();
                GovernanceUtils.loadGovernanceArtifacts(userRegistry);
                GenericArtifactManager artifactManager =
                        new GenericArtifactManager(userRegistry, AppFactoryConstants.RXT_KEY_APPINFO_APPLICATION);
                artifactManager.updateGenericArtifact(artifact);
                RxtApplicationCacheManager.clearCache(applicationId);
            } catch (RegistryException e) {
                String errorMsg = "Error while updating the artifact " + applicationId;
                log.error(errorMsg, e);
                throw new AppFactoryException(errorMsg, e);
            }
        }
    }

    /**
     * Method to delete the application artifact from registry
     *
     * @param applicationId id of application, which needs to be deleted
     * @param tenantDomain tenant domain of current tenant
     * @throws AppFactoryException
     * @throws UserStoreException
     * @throws RegistryException
     */
    public void deleteApplicationArtifact(String applicationId, String tenantDomain)
            throws AppFactoryException, UserStoreException, RegistryException {
        TenantManager tenantManager = ServiceHolder.getRealmService().getTenantManager();

        PrivilegedCarbonContext threadLocalCarbonContext;
        try {
            int tenantId = tenantManager.getTenantId(tenantDomain);
            PrivilegedCarbonContext.startTenantFlow();
            threadLocalCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            threadLocalCarbonContext.setTenantId(tenantId, true);

            String resourcePath =
                    AppFactoryConstants.REGISTRY_APPLICATION_PATH + RegistryConstants.PATH_SEPARATOR + applicationId;
            // removing all the permissions given to the resource
            AuthorizationManager authMan =
                    ServiceHolder.getRealmService().getTenantUserRealm(tenantId)
                            .getAuthorizationManager();
            authMan.clearResourceAuthorizations(resourcePath);

            // deleting the resource for the application
            RegistryService registryService = ServiceHolder.getRegistryService();
            UserRegistry userRegistry = registryService.getGovernanceSystemRegistry(tenantId);

            if (userRegistry.resourceExists(resourcePath)) {
                userRegistry.delete(resourcePath);
                RxtApplicationCacheManager.clearCache(applicationId);
            }

        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Method to retrieve an application for a given app id
     *
     * @param applicationId id of the application
     * @return Application object if its available in registry; else null
     * @throws AppFactoryException
     */
    private Application getApplication(String applicationId) throws AppFactoryException {
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        GenericArtifact artifact = getAppInfoArtifact(applicationId, tenantDomain);
        Application application = null;
        if (artifact != null) {
            application = getAppInfoFromRXT(artifact);

            // from DB
            JDBCApplicationDAO applicationDAO = JDBCApplicationDAO.getInstance();
            application.setBranchCount(applicationDAO.getBranchCount(application.getId()));
            // set application creation status, if not found, consider as completed because previously created
            // applications does not contain this attribute
            String applicationCreationStatus = applicationDAO.getApplicationCreationStatus(application.getId()).name();
            if (applicationCreationStatus != null) {
                application.setApplicationCreationStatus(
                        Constants.ApplicationCreationStatus.valueOf(applicationCreationStatus));
            } else {
                application.setApplicationCreationStatus(Constants.ApplicationCreationStatus.COMPLETED);
            }
        }
        return application;
    }

    /**
     * Method to generate Application object from application generic artifact. Also do the necessary validations
     *
     * @param artifact generic artifact for an application
     * @return application object, which is generated from the artifact
     * @throws
     */
    private Application getAppInfoFromRXT(GenericArtifact artifact) throws AppFactoryException {
        Application appInfo;
        try {
            appInfo =
                    new Application(
                            artifact.getAttribute(AppFactoryConstants.RXT_KEY_APPINFO_KEY),
                            artifact.getAttribute(AppFactoryConstants.RXT_KEY_APPINFO_NAME),
                            artifact.getAttribute(AppFactoryConstants.RXT_KEY_APPINFO_TYPE),
                            artifact.getAttribute(AppFactoryConstants.RXT_KEY_APPINFO_REPO_TYPE),
                            artifact.getAttribute(AppFactoryConstants.RXT_KEY_APPINFO_DESC),
                            artifact.getAttribute(AppFactoryConstants.RXT_KEY_APPINFO_REPO_ACCESSABILITY),
                            artifact.getAttribute(AppFactoryConstants.RXT_KEY_APPINFO_OWNER),
                            artifact.getAttribute(AppFactoryConstants.RXT_KEY_APPINFO_DEFAULT_DOMAIN));

            String customDomainVerified = artifact.getAttribute(
                    AppFactoryConstants.RXT_KEY_APPINFO_CUSTOM_URL_VERIFICATION);
            if (customDomainVerified != null) {
                appInfo.setcustomUrlVerificationCode(customDomainVerified);
            }

            String repoAccessibility = artifact.getAttribute(AppFactoryConstants.RXT_KEY_APPINFO_REPO_ACCESSABILITY);
            if (repoAccessibility != null) {
                appInfo.setRepoAccessability(repoAccessibility);
            }

            String[] prodVersions = artifact.getAttributes(AppFactoryConstants.RXT_KEY_APPINFO_PROD_VERSION);
            if (!ArrayUtils.isEmpty(prodVersions) && prodVersions.length > 0) {
                appInfo.setProduction(Boolean.TRUE);
            }

            String customDomain = artifact.getAttribute(AppFactoryConstants.RXT_KEY_APPINFO_CUSTOM_URL);
            if (customDomain != null) {
                appInfo.setCustomUrl(customDomain);
            }
        } catch (GovernanceException e) {
            String errorMsg = String.format("Unable to extract information from RXT: %s", artifact.getId());
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }

        return appInfo;
    }

    /**
     * Method to retrieve application
     * @param applicationKeys - Application ID array
     * @return Information object or null if application by the id is not in registry
     * @throws AppFactoryException
     */
    private List<ApplicationSummary> getApplicationSummary(String[] applicationKeys, UserRegistry userRegistry)
            throws AppFactoryException {

        if (log.isDebugEnabled()) {
            log.debug("Username for registry :" + userRegistry.getUserName() + " Tenant ID : " +
                      userRegistry.getTenantId());
            log.debug("Username from carbon context :" +
                      CarbonContext.getThreadLocalCarbonContext().getUsername());
        }

        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        List<ApplicationSummary> appSummaryList = new ArrayList<ApplicationSummary>();

        for(String applicationId : applicationKeys){

            GenericArtifact artifact = getAppInfoArtifact(applicationId, tenantDomain);
            ApplicationSummary applicationSummary = null;
            if(artifact !=null) {
                applicationSummary = getAppInfoSummaryFromRXT(artifact);
            }
            appSummaryList.add(applicationSummary);
        }
        return appSummaryList;
    }


    /**
     *
     * @param artifact
     * @return Application summary object from RXT
     * @throws AppFactoryException
     */
    private ApplicationSummary getAppInfoSummaryFromRXT(GenericArtifact artifact)
            throws AppFactoryException {
        ApplicationSummary appInfo;
        try {
            appInfo =
                    new ApplicationSummary(
                            artifact.getAttribute(AppFactoryConstants.RXT_KEY_APPINFO_KEY),
                            artifact.getAttribute(AppFactoryConstants.RXT_KEY_APPINFO_NAME),
                            artifact.getAttribute(AppFactoryConstants.RXT_KEY_APPINFO_TYPE));
        } catch (GovernanceException e) {
            String errorMsg =
                    String.format("Unable to extract information from RXT: %s",
                                  artifact.getId());
            log.error(errorMsg);
            throw new AppFactoryException(errorMsg, e);
        }

        return appInfo;
    }

    /**
     * Method to retrieve the application artifact from registry
     *
     * @param applicationId id of the application
     * @return Generic artifact object of the given application id
     * @throws AppFactoryException
     */
    private GenericArtifactImpl getAppInfoArtifact(String applicationId, String tenantDomain)
            throws AppFactoryException {
        GenericArtifactImpl artifact;
        artifact = (GenericArtifactImpl) RxtApplicationCacheManager.getAppArtifactFromCache(applicationId);
        if(artifact != null){
            return artifact;
        }
        try {

            UserRegistry userRegistry = GovernanceUtil.getUserRegistry();
            String resourcePath = getAppInfoRegistryPath(applicationId);
            if(!userRegistry.resourceExists(resourcePath)){
                return null;
            }
            Resource resource = userRegistry.get(resourcePath);
            GovernanceUtils.loadGovernanceArtifacts(userRegistry);
            GenericArtifactManager artifactManager = new GenericArtifactManager(userRegistry,
                                                                                AppFactoryConstants.RXT_KEY_APPINFO_APPLICATION);
            artifact = (GenericArtifactImpl) artifactManager.getGenericArtifact(resource.getUUID());

        } catch (RegistryException e) {
            String errorMsg = String.format("Unable to load the application information for application id: %s",
                                            applicationId);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }

        if (artifact == null) {
            String errorMsg =
                    "Failed to get generic artifact implementation of application information artifact with " +
                    "tenant domain : " + tenantDomain + " applicationId : " + applicationId;
            log.error(errorMsg);
            throw new AppFactoryException(errorMsg);
        } else {
            RxtApplicationCacheManager.addArtifactToCache(artifact, applicationId);
        }
        return artifact;
    }

    /**
     * Method to construct the app info registry path for a given application
     *
     * @param applicationId id of the application
     * @return
     */
    private String getAppInfoRegistryPath(String applicationId) {
        return AppFactoryConstants.REGISTRY_APPLICATION_PATH + RegistryConstants.PATH_SEPARATOR + applicationId +
               RegistryConstants.PATH_SEPARATOR + AppFactoryConstants.RXT_KEY_APPINFO;
    }
}
