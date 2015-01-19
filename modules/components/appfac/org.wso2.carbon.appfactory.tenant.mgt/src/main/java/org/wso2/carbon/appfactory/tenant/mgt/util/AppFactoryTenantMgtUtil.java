/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.appfactory.tenant.mgt.util;

import java.util.*;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.tenant.mgt.beans.UserInfoBean;
import org.wso2.carbon.appfactory.tenant.mgt.service.TenantManagementException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.appfactory.core.TenantUserEventListner;

public class AppFactoryTenantMgtUtil {
    private static RealmService realmService;
    private static AppFactoryConfiguration configuration;
    private static RegistryService registryService;
    private static ConfigurationContextService configurationContextService;
    public static String EMAIL_CLAIM_URI = "http://wso2.org/claims/emailaddress";
    public static String FIRST_NAME_CLAIM_URI = "http://wso2.org/claims/givenname";
    public static String LAST_NAME_CLAIM_URI = "http://wso2.org/claims/lastname";
    private static Set<TenantUserEventListner> tenantUserEventListners =
                                                                         java.util.Collections.synchronizedSet(new TreeSet<TenantUserEventListner>());
    public static String adminUserName;
    public static String everyOneRoleName;

    public static RealmService getRealmService() {
        return realmService;
    }

    public static synchronized void setRealmService(RealmService realmSer) {
        realmService = realmSer;
        adminUserName = getRealmService().getBootstrapRealmConfiguration().getAdminUserName();
        everyOneRoleName = getRealmService().getBootstrapRealmConfiguration().getEveryOneRoleName();
    }

    public static AppFactoryConfiguration getConfiguration() {
        return configuration;
    }

    public static void setConfiguration(AppFactoryConfiguration configuration) {
        AppFactoryTenantMgtUtil.configuration = configuration;
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static void setRegistryService(RegistryService registryService) {
        AppFactoryTenantMgtUtil.registryService = registryService;
    }

    public static void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        AppFactoryTenantMgtUtil.configurationContextService = configurationContextService;
    }

    public static ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    public static UserInfoBean getUserInfoBean(String userName, int tenantId) throws TenantManagementException {
        try {
            UserRealm realm =
                              AppFactoryTenantMgtUtil.getRealmService()
                                                     .getTenantUserRealm(tenantId);
            String[] claims = { EMAIL_CLAIM_URI, FIRST_NAME_CLAIM_URI, LAST_NAME_CLAIM_URI };
            UserStoreManager userStoreManager = realm.getUserStoreManager();
            if (userStoreManager.isExistingUser(userName)) {
                java.util.Map<String, String> userClaims =
                                                           userStoreManager.getUserClaimValues(userName,
                                                                                               claims,
                                                                                               null);

                String firstName = userClaims.get(FIRST_NAME_CLAIM_URI);
                String lastName = userClaims.get(LAST_NAME_CLAIM_URI);
                String email = userClaims.get(EMAIL_CLAIM_URI);
                StringBuilder displayNameBuilder = new StringBuilder();

                // Display name is constructed by concatenating first name and
                // the
                // last name of the user.
                if (StringUtils.isNotEmpty(firstName)) {
                    displayNameBuilder.append(firstName);
                }

                if (StringUtils.isNotEmpty(lastName)) {
                    displayNameBuilder.append(' ').append(lastName);
                }

                return new UserInfoBean(userName, firstName, lastName, email,
                                        displayNameBuilder.toString(),
                                        filterDefaultUserRoles(userStoreManager.getRoleListOfUser(userName)));
            } else {
                String msg = "No user found with the name " + userName;
                return null;
            }

            /* return new UserInfoBean(userName, firstName, lastName, email); */
        } catch (UserStoreException e) {
            String msg = "Error while getting info for user " + userName;
            throw new TenantManagementException(msg, e);
        }

    }

    /**
     * Filter out default role list,appRole,everyone role from given role array
     * @param roleListOfUser - given role array
     * @return - filtered array of roles
     */
    public static String[] filterDefaultUserRoles(String[] roleListOfUser) {
        List<String> roleList = new ArrayList<String>(Arrays.asList(roleListOfUser));
        ArrayList<String> roles = new ArrayList<String>();
        String[] defaultUserRoles =getConfiguration()
                                       .getProperties(AppFactoryConstants.TENANT_ROLES_DEFAULT_USER_ROLE);
        // remove default role list
        roleList.removeAll(new ArrayList<String>(Arrays.asList(defaultUserRoles)));
        for (String role : roleList) {
            // filter everyone role and appRoles
            if ((!everyOneRoleName.equals(role)) && (!AppFactoryUtil.isAppRole(role))) {
                roles.add(role);
            }
        }
        return roles.toArray(new String[roles.size()]);
    }

    /**
     * Removes admin and everyone role from the set of roles
     * 
     * @param roles
     * @return
     */
    public static String[] removeEveryoneRoles(String[] roles) {
        String everyOneRoleName =
                                  getRealmService().getBootstrapRealmConfiguration()
                                                   .getEveryOneRoleName();
        return (String[]) ArrayUtils.removeElement(roles, everyOneRoleName);
    }

    public static void addTenantUserEventListner(TenantUserEventListner tenantUserEventListner) {
        tenantUserEventListners.add(tenantUserEventListner);
    }

    public static void removeTenantUserEventListner(TenantUserEventListner tenantUserEventListner) {
        tenantUserEventListners.remove(tenantUserEventListner);
    }

    public static Set<TenantUserEventListner> getTenantUserEventListners() {
        return tenantUserEventListners;
    }
}
