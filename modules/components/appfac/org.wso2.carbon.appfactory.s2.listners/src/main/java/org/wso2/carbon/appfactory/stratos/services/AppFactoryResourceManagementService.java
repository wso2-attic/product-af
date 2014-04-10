package org.wso2.carbon.appfactory.stratos.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.stratos.util.AppFactoryTenantMgtUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;

/**
 * <p>Takes care of the adding registry resources ( i.e. Properties as defined in
 * Appfactory) a given cloud.
 * This service is meant to be run on Statos Controller. </p>
 * 
 * <p>Rational of having this service hosted on the Statos Controller ( as oppose
 * to use relevant Carbon Admin services to achieve end objective) is to tighten
 * the security around resource management. </p>
 * 
 * <p>To have the resource management done via WS-API (of Registry) means the
 * users/roles have to be provisioned with wider security permissions, which
 * in-turn results a larger security hole.
 * With the introduction of this service AF can add properties in a secure
 * manner ( since having a more restrictive AF related permission assigned to
 * roles/users)</p>
 */
public class AppFactoryResourceManagementService {
    private static Log log = LogFactory.getLog(AppFactoryResourceManagementService.class);

    /***
     * Adds a property to Application life cycle stage (which this service is running).
     * @param appId Application ID 
     * @param property Name of the property
     * @param value Initial value of the property
     * @param description The description 
     * @param mediaType The media type
     * @throws AppFactoryException an error
     */
    public void addProperty(String appId, String property, String value, String description, String mediaType, boolean isCollection)
            throws AppFactoryException {
        if (isAccessAllowed()) {
            // deny dependency for everyone, done through tenant-listeners
            // allow appidRole for registry resources /dependency/appid/
            authorizeAppIdRole(appId);
            // create registry resource /dependency/appid/xxx
            try {
                Registry registry = AppFactoryTenantMgtUtil.getRegistryService()
                                      .getGovernanceSystemRegistry(CarbonContext.getThreadLocalCarbonContext().getTenantId());
                String path = "/dependencies/" + appId + "/" + property;
                if (registry.resourceExists(path)) {
                    String errorMsg = "Property:" + property + " for application:" + appId + " already exists.";
                    log.error(errorMsg);
                    throw new AppFactoryException(errorMsg);
                }
                Resource resource = null;
                if(isCollection){
                	resource = registry.newCollection();
                }else{
                	resource = registry.newResource();
                }  
                resource.setContent(value);
                if (description != null && !description.isEmpty()) {
                    resource.setDescription(description);
                }
                if (mediaType != null && !mediaType.isEmpty()) {
                    resource.setMediaType(mediaType);
                }

                registry.put(path, resource);
            } catch (RegistryException e) {
                String errorMsg = "Failed to add property:" + property + " for application:" + appId;
                log.error(errorMsg, e);
                throw new AppFactoryException(errorMsg, e);
            }

        } else {
            String errorMsg = "User is not authorized to add property:" + property + " for application:" + appId;
            log.error(errorMsg);
            throw new AppFactoryException(errorMsg);
        }

    }

    /**
     * Return true if the 'This' (- currently logged in user) meets following criteria:
     *  <ul>
     *   <li> The User belongs to the team of this application. <li>
     *   <li> He/she (rather the role has) should have the permission to create <b>OR</b> update resources in this stage.</li>
     *   </ul>
     * 
     * @return true of the above criteria is met, false otherwise.
     * @throws AppFactoryException an error
     */
    private boolean isAccessAllowed() throws AppFactoryException {
        String currentCloudStage = System.getProperty(AppFactoryConstants.CLOUD_STAGE);
        if (currentCloudStage == null || currentCloudStage.isEmpty()) {
            log.error(AppFactoryConstants.CLOUD_STAGE + " system variable is not set.");
            throw new AppFactoryException(AppFactoryConstants.CLOUD_STAGE + " system variable is not set.");
        }
        String currentUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
        if (currentUser == null) {
            return false;
        }

        try {
            AuthorizationManager authorizationManager =
                                                        AppFactoryTenantMgtUtil.getRealmService()
                                                                               .getTenantUserRealm(CarbonContext.getThreadLocalCarbonContext()
                                                                                                                .getTenantId())
                                                                               .getAuthorizationManager();
            boolean isApplicationUser =
                                        authorizationManager.isUserAuthorized(currentUser,
                                                                              AppFactoryConstants.PER_APP_ROLE_PERMISSION,
                                                                              CarbonConstants.UI_PERMISSION_ACTION);
            boolean hasAddPropertyPermission =
                                               authorizationManager.isUserAuthorized(currentUser,
                                                                                     AppFactoryConstants.PERMISSION_RESOURCE_CREATE,
                                                                                     CarbonConstants.UI_PERMISSION_ACTION);
            boolean hasUpdatePropertyPermission =
                                                  authorizationManager.isUserAuthorized(currentUser,
                                                                                        AppFactoryConstants.PERMISSION_RESOURCE_UPDATE_IN +
                                                                                                currentCloudStage,
                                                                                        CarbonConstants.UI_PERMISSION_ACTION);

            return isApplicationUser && (hasAddPropertyPermission || hasUpdatePropertyPermission);
        } catch (UserStoreException e) {
            String errorMsg = "Error occurred while getting authorization manager.";
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }
    }

    /**
     * Adds role to this Cloud stage with permission to add/retrieve/delete resources in '_system/governance/dependencies/{appId}'.
     *  
     * @param appId The application ID
     * @throws AppFactoryException an error
     */
    private void authorizeAppIdRole(String appId) throws AppFactoryException {
        try {
            AuthorizationManager authorizationManager =
                                                        AppFactoryTenantMgtUtil.getRealmService()
                                                                               .getTenantUserRealm(CarbonContext.getThreadLocalCarbonContext()
                                                                                                                .getTenantId())
                                                                               .getAuthorizationManager();
            String applicationRole = AppFactoryUtil.getRoleNameForApplication(appId);

            String baseResourcePath = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + "/dependencies/" + appId;
            if (!authorizationManager.isRoleAuthorized(applicationRole, baseResourcePath, ActionConstants.GET)) {
                authorizationManager.authorizeRole(applicationRole, baseResourcePath, ActionConstants.GET);
            }
            if (!authorizationManager.isRoleAuthorized(applicationRole, baseResourcePath, ActionConstants.PUT)) {
                authorizationManager.authorizeRole(applicationRole, baseResourcePath, ActionConstants.PUT);
            }
            if (!authorizationManager.isRoleAuthorized(applicationRole, baseResourcePath, ActionConstants.DELETE)) {
                authorizationManager.authorizeRole(applicationRole, baseResourcePath, ActionConstants.DELETE);
            }
        } catch (UserStoreException e) {
            String errorMsg = "Error occurred while getting authorization manager.";
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }
    }

}
