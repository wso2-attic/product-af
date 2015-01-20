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

package org.wso2.carbon.appfactory.listners.tenant;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.RoleBean;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.listners.util.Util;
import org.wso2.carbon.stratos.common.exception.StratosException;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;

import java.util.HashSet;
import java.util.Set;

/**
 * Platform level roles defined in appfactory.xml are created through this
 * class.
 * All the permissions defined are assigned to the roles and if the role is
 * existing, permissions
 * are updated.
 */
public class DefaultRolesCreatorForSuperTenant {
    private static Log log = LogFactory.getLog(DefaultRolesCreatorForSuperTenant.class);
    private Set<RoleBean> roleBeanList = null;
    private static final int EXEC_ORDER = 40;

    public DefaultRolesCreatorForSuperTenant() throws StratosException {
        roleBeanList = new HashSet<RoleBean>();
        try {
            String adminUser = Util.getRealmService().getBootstrapRealm().
                    getRealmConfiguration().getAdminUserName();
            roleBeanList.addAll(AppFactoryUtil.getRolePermissionConfigurations(AppFactoryConstants.TENANT_ROLES_DEFAULT_USER_ROLE, adminUser));
            roleBeanList.addAll(AppFactoryUtil.getRolePermissionConfigurations(AppFactoryConstants.TENANT_ROLES_ROLE, adminUser));
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            String message = "Failed to read default roles from appfactory configuration.";
            log.error(message);
            throw new StratosException(message, e);
        } catch (AppFactoryException e) {
            String message = "Failed to read default roles from appfactory configuration.";
            log.error(message);
            throw new StratosException(message, e);
        }
    }


    public void createDefaultRoles() throws UserStoreException {
        UserStoreManager userStoreManager = Util.getRealmService().getBootstrapRealm().
                getUserStoreManager();
        AuthorizationManager authorizationManager = Util.getRealmService().getBootstrapRealm().
                getAuthorizationManager();

        AppFactoryUtil.addRolePermissions(userStoreManager, authorizationManager, roleBeanList);

    }
}