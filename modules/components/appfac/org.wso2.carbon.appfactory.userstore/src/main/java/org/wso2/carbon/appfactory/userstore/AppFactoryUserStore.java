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

package org.wso2.carbon.appfactory.userstore;

import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.ldap.ReadWriteLDAPUserStoreManager;
import org.wso2.carbon.user.core.profile.ProfileConfigurationManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AppFactoryUserStore extends ReadWriteLDAPUserStoreManager {

    public AppFactoryUserStore(RealmConfiguration realmConfig,
                               Map<String, Object> properties, ClaimManager claimManager,
                               ProfileConfigurationManager profileManager, UserRealm realm,
                               Integer tenantId) throws UserStoreException {
        super(realmConfig, properties, claimManager, profileManager, realm,
                tenantId);
    }

    public AppFactoryUserStore(RealmConfiguration realmConfig,
                               ClaimManager claimManager,
                               ProfileConfigurationManager profileManager) throws UserStoreException {
        super(realmConfig, claimManager, profileManager);
    }
    
    @Override
    public boolean doAuthenticate(String username, Object credential) throws UserStoreException {
        boolean isAuthenticated = false;
        isAuthenticated = super.authenticate(username, credential);
        if (isAuthenticated) {
            String[] roles = getRoleListOfUser(username);
            if (roles.length > 0) {
                return true;
            }
        }
        return isAuthenticated;
    }

    @Override
    public String[] doListUsers(String arg0, int maxItemLimit) throws UserStoreException {
        String[] roles = getRoleNames();
        List<String> users = new ArrayList<String>();
        for (String role : roles) {
            String[] usersInRole = getUserListOfRole(role);
            users.addAll(Arrays.asList(usersInRole));
        }
        return users.toArray(new String[users.size()]);
    }

    @Override
    public void addUser(String userName, Object credential, String[] roleList,
                        Map<String, String> claims, String profileName) throws UserStoreException {
        if (isExistingUser(userName)) {
            // do nothing
        } else {
            super.addUser(userName, credential, roleList, claims, profileName);
        }
    }

    @Override
    public void doAddUser(String userName, Object credential, String[] roleList,
                        Map<String, String> claims, String profileName, boolean requirePasswordChange) throws UserStoreException {
        if (isExistingUser(userName)) {
            // do nothing
        } else {
            super.addUser(userName, credential, roleList, claims, profileName,
                    requirePasswordChange);
        }
    }

    @Override
    public void doUpdateCredentialByAdmin(String userName, Object newCredential) throws UserStoreException {
        // TODO - This is a hack to prevent password being updated each time the application is added
        String passwordValue = (String) newCredential;
        if (passwordValue != null && passwordValue.trim().length() > 0) {
            super.updateCredentialByAdmin(userName, newCredential);
        }
    }
    
    

}
