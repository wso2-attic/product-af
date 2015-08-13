/*
 * Copyright 2005-2013 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.appfactory.stratos.listeners;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.stratos.common.exception.StratosException;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.RoleBean;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.stratos.util.AppFactoryS4ListenersUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;

import java.util.Set;

public class CloudEnvironmentPermissionListener implements TenantMgtListener {
    private static final Log log = LogFactory.getLog(CloudEnvironmentPermissionListener.class);
    private static final int EXEC_ORDER = 11;

    @Override
    public int getListenerOrder() {
        // TODO Auto-generated method stub
        return EXEC_ORDER;
    }

    @Override
    public void onSubscriptionPlanChange(int arg0, String arg1, String arg2)
            throws StratosException {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTenantActivation(int arg0) throws StratosException {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTenantCreate(TenantInfoBean tenInfoBean) throws StratosException {
        log.info("*********adding permissions******") ;
        int tenantId = tenInfoBean.getTenantId();
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);

        String currentCloudStage = System.getProperty(AppFactoryConstants.CLOUD_STAGE);
        if (currentCloudStage == null || currentCloudStage.isEmpty()) {
            log.error(AppFactoryConstants.CLOUD_STAGE + " system variable is not set. No permissions related to cloud environments are added.");
            throw new StratosException(AppFactoryConstants.CLOUD_STAGE + " system variable is not set. No permissions related to cloud environments are added.");
        }
        try {
            addCloudRolePermissions(tenInfoBean, currentCloudStage);

        } catch (UserStoreException e) {
            String msg = "Error while authorizing permissions defined in appfactory.xml";
            log.error(msg, e);

        } catch (AppFactoryException e) {
            String msg = "Error while authorizing permissions defined in appfactory.xml";
            log.error(msg, e);

        } finally {

            PrivilegedCarbonContext.endTenantFlow();

        }

    }

    private void addCloudRolePermissions(TenantInfoBean tenantInfoBean, String currentCloudStage) throws AppFactoryException, UserStoreException {
        String adminUser = tenantInfoBean.getAdmin();
        String rolePermissionConfigPath = AppFactoryConstants.DEPLOYMENT_STAGES + "." + currentCloudStage + "." + AppFactoryConstants.TENANT_ROLES_ROLE;
        Set<RoleBean> roleBeanList;
        try {
            roleBeanList = AppFactoryUtil.getRolePermissionConfigurations(rolePermissionConfigPath, adminUser);
        } catch (AppFactoryException e) {
            String errorMsg = "Failed to get permission collections from appfactory configuration.";
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }

        UserStoreManager userStoreManager = AppFactoryS4ListenersUtil.getRealmService().
                getTenantUserRealm(tenantInfoBean.getTenantId()).getUserStoreManager();
        AuthorizationManager authorizationManager = AppFactoryS4ListenersUtil.getRealmService().
                getTenantUserRealm(tenantInfoBean.getTenantId()).
                getAuthorizationManager();
        AppFactoryUtil.addRolePermissions(userStoreManager, authorizationManager, roleBeanList);

    }

   

    @Override
    public void onTenantDeactivation(int arg0) throws StratosException {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTenantUpdate(org.wso2.carbon.stratos.common.beans.TenantInfoBean tenantInfoBean)
            throws StratosException {

    }

    @Override
    public void onTenantRename(int i, String s, String s2) throws StratosException {

    }

    @Override
    public void onTenantInitialActivation(int arg0) throws StratosException {
        // TODO Auto-generated method stub

    }



}
