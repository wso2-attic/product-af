/*
 * Copyright 2005-2012 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.appfactory.dashboard.mgt.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.application.mgt.service.ApplicationManagementException;
import org.wso2.carbon.appfactory.application.mgt.service.ApplicationManagementService;
import org.wso2.carbon.appfactory.application.mgt.service.UserRoleCount;
import org.wso2.carbon.appfactory.application.mgt.util.Util;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;

/**
 * 
 * Service class defines operations related to ETA Dashboard
 * 
 */
public class DashboardManagementService extends AbstractAdmin {

	private static Log log = LogFactory
			.getLog(DashboardManagementService.class);

    ApplicationManagementService applicationManagementService = new ApplicationManagementService();


	/**
	 * Return the count of same user playing the same role in all the projects for all the users
	 * @param roleName
	 *            name of the user role
	 * @return array of UserCount objects
	 * @throws ApplicationManagementException
	 */
	public UserCount[] getUserCountPerRoleAcrossAllApps(String roleName)
			throws ApplicationManagementException {
		UserRealm realm = null;
		String[] userList = null;
		UserStoreManager userStoreManager;
		Map<String, UserCount> userCountObjs = new HashMap<String, UserCount>();

		TenantManager manager = Util.getRealmService().getTenantManager();
		try {
			Tenant[] tenants = manager.getAllTenants();
			for (Tenant tenant : tenants) {
				realm = Util.getRealmService().getTenantUserRealm(
						tenant.getId());
				userStoreManager = realm.getUserStoreManager();
				userList = userStoreManager.getUserListOfRole(roleName);

                String adminUserame = Util.getRealmService().getBootstrapRealmConfiguration().getAdminUserName();

                for (int i = 0; i < userList.length; i++)
                {
                    if (userList[i].equals(adminUserame))
                    {
                        userList[i] = null;
                        break;
                    }
                }

				for (String userId : userList) {

                    if(userId == null){
                       continue;
                    }else {
					UserCount userCount = null;
					if (userCountObjs.containsKey(userId)) {
						userCount = userCountObjs.get(userId);
						userCount.setCount(userCount.getCount() + 1);
					} else {
						userCount = new UserCount();
						userCount.setRole(roleName);
						userCount.setCount(1);

						userCount.setUserInfoBean(Util.getUserInfoBean(userId));
						userCountObjs.put(userId, userCount);
					}
                    }
                   }

			}
			return userCountObjs.values().toArray(
					new UserCount[userCountObjs.size()]);

		} catch (UserStoreException e) {
			String msg = "Error while getting all tenants";
			log.error(msg, e);
			throw new ApplicationManagementException(msg, e);
		}
	}

    public UserRoleCount[] getUserCountByRoles() throws ApplicationManagementException {

        String[] createdApplications = applicationManagementService.getAllCreatedApplications();
        Map<String, Set<String>> userRoleCountMap = new HashMap<String, Set<String>>();

        for (String applicationId : createdApplications) {
            TenantManager tenantManager = Util.getRealmService().getTenantManager();

            try {
                UserRealm realm = Util.getRealmService().getTenantUserRealm(tenantManager.
                        getTenantId(applicationId));
                String[] roles = realm.getUserStoreManager().getRoleNames();

                for (String roleName : roles) {
                    if (!Util.getRealmService().getBootstrapRealmConfiguration()
                            .getEveryOneRoleName().equals(roleName)) {
                        String[] usersOfRole = realm.getUserStoreManager()
                                .getUserListOfRole(roleName);

                        if(!userRoleCountMap.containsKey(roleName)) {
                            Set<String> initialRoles = new HashSet<String>();
                            userRoleCountMap.put(roleName, initialRoles);
                        }

                        Set<String> fetchedRoles = userRoleCountMap.get(roleName);
                        fetchedRoles.addAll(Arrays.asList(usersOfRole));

                        if(fetchedRoles.contains(Util.getRealmService().getBootstrapRealmConfiguration().getAdminUserName())){
                              fetchedRoles.remove(Util.getRealmService().getBootstrapRealmConfiguration().getAdminUserName());
                        }

                        userRoleCountMap.put(roleName, fetchedRoles);
                    }
                }

            } catch (UserStoreException e) {
                String msg = "Error while getting users of application "
                        + applicationId;
                log.error(msg, e);
                throw new ApplicationManagementException(msg, e);
            }
        }

        Set<String> userRoleKeySet = userRoleCountMap.keySet();
        UserRoleCount arrUserRoleCount[] = new UserRoleCount[userRoleKeySet.size()];

        int counter = 0;
        for (String roleName : userRoleKeySet) {
            UserRoleCount userRoleCount = new UserRoleCount();
            userRoleCount.setRoleName(roleName);
            userRoleCount.setUserCount(userRoleCountMap.get(roleName).size());
            arrUserRoleCount[counter++] = userRoleCount;
        }

        return arrUserRoleCount;
    }

//    Commenting out this method since we can not get application keys using tenant domain anymore.
    /*public Version[] getVersionsInStages()
            throws ApplicationManagementException, AppFactoryException {

        TenantManager manager = Util.getRealmService().getTenantManager();
        try {
            Tenant[] tenants = manager.getAllTenants();
            ArrayList<Version> allVersions = new ArrayList<Version>();

            for (Tenant tenant : tenants) {
                Version[] versions = ProjectUtils.getVersions(tenant.getDomain());
                if (versions != null && versions.length != 0) {
                    for (Version version : versions) {
                        allVersions.add(version);
                    }
                }
            }

            Version[] arrVersions = new Version[allVersions.size()];
            arrVersions = allVersions.toArray(arrVersions);

            return arrVersions;
        } catch (UserStoreException e) {
            String msg = "Error while getting versions";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
    }*/


}