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
package org.wso2.carbon.appfactory.ext.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.ext.Util;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractAuthorizationManagerListener;


public class AppFactoryAuthorizationManagerListener extends AbstractAuthorizationManagerListener {
    private static final Log log = LogFactory.getLog(AppFactoryAuthorizationManagerListener.class);
    private int executionOrderId = 10;

    public void setExecutionOrderId(int executionOrderId) {
        this.executionOrderId = executionOrderId;
    }

    @Override
    public int getExecutionOrderId() {
        return executionOrderId;
    }

    @Override
    public boolean isUserAuthorized(String userName, String resourceId, String action,
                                    AuthorizationManager authorizationManager) throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed()) {
            Util.checkNonModifiablePermissions(resourceId);
            Util.checkAuthorizationForUserRealm();
        }
        return super.isUserAuthorized(userName, resourceId, action, authorizationManager);
    }

    @Override
    public boolean isRoleAuthorized(String userName, String resourceId, String action,
                                    AuthorizationManager authorizationManager) throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed()) {
            Util.checkNonModifiablePermissions(resourceId);
            Util.checkAuthorizationForUserRealm();
        }
        return super.isRoleAuthorized(userName, resourceId, action, authorizationManager);
    }

    @Override
    public boolean authorizeUser(String userName, String resourceId, String action,
                                 AuthorizationManager authorizationManager) throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed()) {
            // avoid letting users to use deprecated methods.
            String errorMsg = "AuthorizeUser method is depreciated. Use authorizeRole method instead.";
            log.warn(errorMsg);
            throw new UserStoreException(errorMsg);
        }
        return super.authorizeUser(userName, resourceId, action, authorizationManager);
    }

    @Override
    public boolean authorizeRole(String roleName, String resourceId, String action,
                                 AuthorizationManager authorizationManager) throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed())  {
            Util.checkNonModifiablePermissions(resourceId);
            Util.checkAuthorizationForUserRealm();
            Util.checkNonModifiableRoles(new String[]{roleName});
        }
        return super.authorizeRole(roleName, resourceId, action, authorizationManager);
    }

    @Override
    public boolean denyUser(String userName, String resourceId, String action,
                            AuthorizationManager authorizationManager) throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed())  {
            // avoid letting users to use deprecated methods.
            String errorMsg = "denyUser method is depreciated. Use denyRole method instead.";
            log.warn(errorMsg);
            throw new UserStoreException(errorMsg);
        }
        return super.denyUser(userName, resourceId, action, authorizationManager);
    }

    @Override
    public boolean denyRole(String roleName, String resourceId, String action,
                            AuthorizationManager authorizationManager) throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed())  {
            Util.checkNonModifiablePermissions(resourceId);
            Util.checkAuthorizationForUserRealm();
            Util.checkNonModifiableRoles(new String[]{roleName});
        }
        return super.denyRole(roleName, resourceId, action, authorizationManager);
    }

    @Override
    public boolean clearUserAuthorization(String userName, String resourceId, String action,
                                          AuthorizationManager authorizationManager) throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed())  {
            // avoid letting users to use deprecated methods.
            String errorMsg = "clearUserAuthorization method is depreciated. Use clearRoleAuthorization method instead.";
            log.warn(errorMsg);
            throw new UserStoreException(errorMsg);
        }
        return super.clearUserAuthorization(userName, resourceId, action, authorizationManager);
    }

    @Override
    public boolean clearUserAuthorization(String userName, AuthorizationManager authorizationManager)
            throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed())  {
            // avoid letting users to use deprecated methods.
            String errorMsg = "clearUserAuthorization method is depreciated. Use clearRoleAuthorization method instead.";
            log.warn(errorMsg);
            throw new UserStoreException(errorMsg);
        }
        return super.clearUserAuthorization(userName, authorizationManager);
    }

    @Override
    public boolean clearRoleAuthorization(String roleName, String resourceId, String action,
                                          AuthorizationManager authorizationManager) throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed())  {
            Util.checkNonModifiablePermissions(resourceId);
            Util.checkAuthorizationForUserRealm();
            Util.checkNonModifiableRoles(new String[]{roleName});
        }
        return super.clearRoleAuthorization(roleName, resourceId, action, authorizationManager);
    }

    @Override
    public boolean clearRoleActionOnAllResources(String roleName, String action,
                                                 AuthorizationManager authorizationManager) throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed())  {
            Util.checkAuthorizationForUserRealm();
            Util.checkNonModifiableRoles(new String[]{roleName});
        }
        return super.clearRoleActionOnAllResources(roleName, action, authorizationManager);
    }

    @Override
    public boolean clearRoleAuthorization(String roleName, AuthorizationManager authorizationManager)
            throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed())  {
            Util.checkAuthorizationForUserRealm();
            Util.checkNonModifiableRoles(new String[]{roleName});
        }
        return super.clearRoleAuthorization(roleName, authorizationManager);
    }

    @Override
    public boolean clearResourceAuthorizations(String resourceId, AuthorizationManager authorizationManager)
            throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed())  {
            Util.checkNonModifiablePermissions(resourceId);
            Util.checkAuthorizationForUserRealm();
        }
        return super.clearResourceAuthorizations(resourceId, authorizationManager);
    }

    @Override
    public boolean resetPermissionOnUpdateRole(String roleName, String newRoleName,
                                               AuthorizationManager authorizationManager) throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed())  {
            Util.checkAuthorizationForUserRealm();
            Util.checkNonModifiableRoles(new String[]{roleName, newRoleName});
        }
        return super.resetPermissionOnUpdateRole(roleName, newRoleName, authorizationManager);
    }
}

