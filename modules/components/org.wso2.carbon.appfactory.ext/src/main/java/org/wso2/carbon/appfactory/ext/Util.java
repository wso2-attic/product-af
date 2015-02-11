/*
 * Copyright 2004,2013 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.appfactory.ext;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.ext.authorization.AppFactorySecurityPermission;
import org.wso2.carbon.appfactory.ext.internal.AuthorizationMetaDataHolder;
import org.wso2.carbon.appfactory.ext.internal.ServiceHolder;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.*;
import java.util.regex.Pattern;

public class Util {
    private static final Log log = LogFactory.getLog(Util.class);
    public static String JNDI_APPLICATION_SUB_CONTEXT_PREFIX="app-";

    /**
     * This method provides current artifact name without considering the version. example, foo-1.0.0 is returned as foo.
     *
     * @return artifact name without version
     */
    public static String getCurrentArtifactName() {
        //todo:need improvement in logic.
        String applicationNameWithVersion = CarbonContext.getThreadLocalCarbonContext().getApplicationName();
        String applicationName = null;
        // if it is a web application
        if (applicationNameWithVersion != null && applicationNameWithVersion.contains("-")) {
            applicationName = applicationNameWithVersion.substring(0, applicationNameWithVersion.indexOf("-"));
        }
        return applicationName;
    }

    /**
     * Check if given path contains current artifact name
     *
     * @param path - path as a string(a registry path)
     * @return - true if it matches with the regular expression
     */
    public static boolean pathContainsCurrentArtifactName(String path) {
        //todo:refine the regex
        String regex = ".*/" + getCurrentArtifactName() + "/.*";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(path).matches();
    }

    public static boolean isCurrentTenantLoaded() throws UserStoreException {
        try {
            return TenantAxisUtils.getLastAccessed(CarbonContext.getThreadLocalCarbonContext().getTenantDomain(),
                    ServiceHolder.getInstance().getConfigContextService().getServerConfigContext()) != -1;
        } catch (Exception e) {
            throw new UserStoreException("Failed to get active list of tenants.", e);
        }
    }

    public static boolean isApplicationSpecificRequest() throws UserStoreException {
        String currentApplicationName = CarbonContext.getThreadLocalCarbonContext().getApplicationName();

        if (log.isDebugEnabled()) {
            log.debug("Current application name in carbon context:" + currentApplicationName);
        }
        // if current application null, it is not a application specific request
        // if current application is a hidden service or a admin service, it is not a appfactory application specific request
        return currentApplicationName != null &&
                !AuthorizationMetaDataHolder.getInstance().getAdminServices().contains(currentApplicationName) &&
                !AuthorizationMetaDataHolder.getInstance().getHiddenServices().contains(currentApplicationName);
    }

    public static boolean isUserMgtPermissionsAllowed() throws UserStoreException {
        boolean isAuthorized = false;
        RealmService realmService = ServiceHolder.getInstance().getRealmService();
        try {
            String currentApplicationName = CarbonContext.getThreadLocalCarbonContext().getApplicationName();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setApplicationName(null);
            UserRealm userRealm = realmService.getTenantUserRealm(CarbonContext.getThreadLocalCarbonContext().getTenantId());
            AuthorizationManager authorizationManager = userRealm.getAuthorizationManager();
            // check if current logged in user is given user management permissions.
            String currentLoggedInUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
            if (currentLoggedInUser != null) {
                isAuthorized = authorizationManager.isUserAuthorized(currentLoggedInUser,
                        CarbonConstants.UI_ADMIN_PERMISSION_COLLECTION,
                        CarbonConstants.UI_PERMISSION_ACTION);

            }
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setApplicationName(currentApplicationName);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String errorMsg = "Failed to get the tenant user realm of tenant:" +
                    CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            log.error(errorMsg, e);
            throw new UserStoreException(errorMsg, e);
        }
        return isAuthorized;
    }

    public static void checkAuthorizationForUserRealm() throws UserStoreException {
        boolean isAuthorized;
        RealmService realmService = ServiceHolder.getInstance().getRealmService();
        String roleNameForApplication = AppFactoryUtil.getRoleNameForApplication(getCurrentArtifactName());
        try {
            String currentApplicationName = CarbonContext.getThreadLocalCarbonContext().getApplicationName();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setApplicationName(null);
            UserRealm userRealm = realmService.getTenantUserRealm(CarbonContext.getThreadLocalCarbonContext().getTenantId());
            // check if current application is authorized to user realm operations by checking application specific role is authorized
            isAuthorized = userRealm.getAuthorizationManager().isRoleAuthorized(roleNameForApplication,
                    AppFactoryConstants.INVOKE_PERMISSION, AppFactoryConstants.CONSUME);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setApplicationName(currentApplicationName);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String errorMsg = "Failed to check role:" + roleNameForApplication + " authorization for resource:" +
                    AppFactoryConstants.INVOKE_PERMISSION + " on action:" + AppFactoryConstants.CONSUME;
            log.error(errorMsg, e);
            throw new UserStoreException(errorMsg, e);
        }

        // if not authorized, throw user store exception
        if (!isAuthorized) {
            String errorMsg = "Application:" + getCurrentArtifactName() + " is trying to perform user realm actions." +
                    " Application must have been authorized by privileged user to allow user realm actions.";
            log.warn(errorMsg);
            throw new UserStoreException(errorMsg);
        }
    }

    public static void checkNonModifiablePermissions(String permission) throws UserStoreException {
        if (permission != null) {
            try {
                Set<String> afPermissions = AuthorizationMetaDataHolder.getInstance().getAppFactoryPermissions();
                if (afPermissions.contains(permission.trim())) {
                    String errorMsg = Util.getCurrentArtifactName() + " is trying to modify AppFactory specific system permissions." +
                            " Applications are not allowed to modify AppFactory specific system permissions.";
                    log.warn(errorMsg);
                    throw new UserStoreException(errorMsg);
                }
            } catch (AppFactoryException e) {
                String errorMsg = "Failed to get appfactory permissions.";
                log.error(errorMsg, e);
                throw new UserStoreException(errorMsg, e);
            }
        }
    }

    public static void checkNonModifiableRoles(String[] roles) throws UserStoreException {
        if (roles != null) {
            Set<String> securedRoles = AuthorizationMetaDataHolder.getInstance().getSecuredRoles();
            Set<String> givenRoles = new HashSet<String>(Arrays.asList(roles));

            // check if application is trying to modify secured roles
            if (!Collections.disjoint(securedRoles, givenRoles)) {
                String errorMsg = Util.getCurrentArtifactName() + " is trying to modify AppFactory specific system roles." +
                        " Applications are not allowed to modify AppFactory specific system roles.";
                log.warn(errorMsg);
                throw new UserStoreException(errorMsg);
            }

            // check if application is trying to modify appfactory application specific roles.
            for (String role : roles) {
                if (AppFactoryUtil.isAppRole(role)) {
                    String errorMsg = Util.getCurrentArtifactName() + " is trying to modify AppFactory application " +
                            "specific system role:" + role + " Applications are not allowed to modify AppFactory " +
                            "application specific system roles.";
                    log.warn(errorMsg);
                    throw new UserStoreException(errorMsg);
                }
            }

        }
    }

    /**
     * Check if given user belongs to a non modifiable role
     *
     * @param username - given user
     * @throws UserStoreException throw exceptions when user belongs to non modifiable roles.
     */
    public static void checkUserInNonModifiableRole(String username) throws UserStoreException {
        if (username != null) {
            Set<String> userRolesSet;
            String everyoneRoleName;
            try {
                UserRealm tenantUserRealm = ServiceHolder.getInstance().getRealmService().
                        getTenantUserRealm(CarbonContext.getThreadLocalCarbonContext().getTenantId());
                String[] userRoles = tenantUserRealm.getUserStoreManager().getRoleListOfUser(username);
                userRolesSet = new HashSet<String>(Arrays.asList(userRoles));
                everyoneRoleName = tenantUserRealm.getRealmConfiguration().getEveryOneRoleName();
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                String errorMsg = "Failed to get the tenant user realm of tenant:" +
                        CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
                log.error(errorMsg, e);
                throw new UserStoreException(errorMsg, e);
            }
            // remove everyone role name from list as it is a non modifiable role.
            if (userRolesSet.contains(everyoneRoleName)) {
                userRolesSet.remove(everyoneRoleName);
            }

            checkNonModifiableRoles(userRolesSet.toArray(new String[userRolesSet.size()]));
        }
    }

    public static boolean isRequestFromSystemCode() {
        SecurityManager secMan = System.getSecurityManager();
        if (secMan != null) {
            try{
                secMan.checkPermission(new AppFactorySecurityPermission("RegistryPermission"));
            }   catch (RuntimeException e)   {
                if(log.isDebugEnabled()){
                    log.debug(e);
                }
                 return false;
            }
        }
        return true;
    }

    public static String getCurrentApplicationContextName() {
        String currentArtifactName=getCurrentArtifactName();
        String currentApplicationContextName = null;
        if(currentArtifactName!=null){
            currentApplicationContextName=JNDI_APPLICATION_SUB_CONTEXT_PREFIX+currentArtifactName;
        }
        return currentApplicationContextName;
    }
}
