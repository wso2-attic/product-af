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
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserOperationEventListener;

import java.util.*;


public class AppFactoryUserOperationEventListener extends AbstractUserOperationEventListener {
    private static final Log log = LogFactory.getLog(AppFactoryUserOperationEventListener.class);
    private int executionOrderId = 10;

    public void setExecutionOrderId(int executionOrderId) {
        this.executionOrderId = executionOrderId;
    }

    @Override
    public int getExecutionOrderId() {
        return this.executionOrderId;
    }

    @Override
    public boolean doPreAuthenticate(String userName, Object credential, UserStoreManager userStoreManager)
            throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed()) {
            Util.checkAuthorizationForUserRealm();
        }
        return super.doPreAuthenticate(userName, credential, userStoreManager);

    }

    @Override
    public boolean doPreAddUser(String userName, Object credential, String[] roleList, Map<String, String> claims,
                                String profile, UserStoreManager userStoreManager) throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed()) {
            Util.checkAuthorizationForUserRealm();
            Util.checkNonModifiableRoles(roleList);
        }
        return super.doPreAddUser(userName, credential, roleList, claims, profile, userStoreManager);

    }


    @Override
    public boolean doPreGetUserClaimValues(String userName, String[] claims, String profileName, Map<String, String> claimMap,
                                           UserStoreManager storeManager) throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed()) {
            Util.checkAuthorizationForUserRealm();
        }
        return super.doPreGetUserClaimValues(userName, claims, profileName, claimMap, storeManager);
    }

    @Override
    public boolean doPreGetUserClaimValue(String userName, String claim, String profileName,
                                          UserStoreManager storeManager) throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed()) {
            Util.checkAuthorizationForUserRealm();
        }
        return super.doPreGetUserClaimValue(userName, claim, profileName, storeManager);
    }

    @Override
    public boolean doPreUpdateRoleListOfUser(String userName, String[] deletedRoles, String[] newRoles,
                                             UserStoreManager userStoreManager) throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed()) {
            Util.checkAuthorizationForUserRealm();
            Util.checkNonModifiableRoles(deletedRoles);
            Util.checkNonModifiableRoles(newRoles);
        }
        return super.doPreUpdateRoleListOfUser(userName, deletedRoles, newRoles, userStoreManager);
    }

    @Override
    public boolean doPreUpdateUserListOfRole(String roleName, String[] deletedUsers, String[] newUsers,
                                             UserStoreManager userStoreManager) throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed()) {
            Util.checkAuthorizationForUserRealm();
            Util.checkNonModifiableRoles(new String[]{roleName});
        }
        return super.doPreUpdateUserListOfRole(roleName, deletedUsers, newUsers, userStoreManager);
    }

    @Override
    public boolean doPreDeleteUserClaimValue(String userName, String claimURI, String profileName,
                                             UserStoreManager userStoreManager) throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed()) {
            Util.checkAuthorizationForUserRealm();
        }
        return super.doPreDeleteUserClaimValue(userName, claimURI, profileName, userStoreManager);
    }

    @Override
    public boolean doPreAddRole(String roleName, String[] userList, Permission[] permissions,
                                UserStoreManager userStoreManager) throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed()) {
            Util.checkAuthorizationForUserRealm();
        }
        return super.doPreAddRole(roleName, userList, permissions, userStoreManager);
    }

    @Override
    public boolean doPreDeleteRole(String roleName, UserStoreManager userStoreManager) throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed()) {
            Util.checkAuthorizationForUserRealm();
            Util.checkNonModifiableRoles(new String[]{roleName});
        }
        return super.doPreDeleteRole(roleName, userStoreManager);
    }

    @Override
    public boolean doPreUpdateRoleName(String roleName, String newRoleName, UserStoreManager userStoreManager)
            throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed()) {
            Util.checkAuthorizationForUserRealm();
            Util.checkNonModifiableRoles(new String[]{roleName});
        }
        return super.doPreUpdateRoleName(roleName, newRoleName, userStoreManager);
    }

    @Override
    public boolean doPreUpdateCredential(String userName, Object newCredential, Object oldCredential,
                                         UserStoreManager userStoreManager) throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed()) {
            Util.checkAuthorizationForUserRealm();
        }
        return super.doPreUpdateCredential(userName, newCredential, oldCredential, userStoreManager);
    }

    @Override
    public boolean doPreUpdateCredentialByAdmin(String userName, Object newCredential,
                                                UserStoreManager userStoreManager) throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed()) {
            String errorMsg = "Updating credential by Admin method is not supported for applications.";
            log.warn(errorMsg);
            throw new UserStoreException(errorMsg);
        }
        return super.doPreUpdateCredentialByAdmin(userName, newCredential, userStoreManager);
    }

    @Override
    public boolean doPreDeleteUser(String userName, UserStoreManager userStoreManager) throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed()) {
            Util.checkAuthorizationForUserRealm();
            Util.checkUserInNonModifiableRole(userName);
        }
        return super.doPreDeleteUser(userName, userStoreManager);
    }

    @Override
    public boolean doPreSetUserClaimValue(String userName, String claimURI, String claimValue, String profileName,
                                          UserStoreManager userStoreManager) throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed()) {
            Util.checkAuthorizationForUserRealm();
        }
        return super.doPreSetUserClaimValue(userName, claimURI, claimValue, profileName, userStoreManager);
    }

    @Override
    public boolean doPreSetUserClaimValues(String userName, Map<String, String> claims, String profileName,
                                           UserStoreManager userStoreManager) throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed()) {
            Util.checkAuthorizationForUserRealm();
        }
        return super.doPreSetUserClaimValues(userName, claims, profileName, userStoreManager);
    }

    @Override
    public boolean doPreDeleteUserClaimValues(String userName, String[] claims, String profileName,
                                              UserStoreManager userStoreManager) throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed()) {
            Util.checkAuthorizationForUserRealm();
        }
        return super.doPreDeleteUserClaimValues(userName, claims, profileName, userStoreManager);
    }
}
