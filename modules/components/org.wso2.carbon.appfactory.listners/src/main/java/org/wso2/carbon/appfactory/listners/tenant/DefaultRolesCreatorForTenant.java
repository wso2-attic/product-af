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
import org.wso2.carbon.appfactory.common.bam.BamDataPublisher;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.listners.util.Util;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.ClaimManager;
import org.wso2.carbon.user.api.ClaimMapping;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.claim.Claim;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.stratos.common.exception.StratosException;

import java.util.Set;

/**
 * Tenant level roles defined in appfactory.xml are created through this
 * class.
 * All the permissions defined are assigned to the roles and if the role is
 * existing, permissions
 * are updated.
 */
public class DefaultRolesCreatorForTenant implements TenantMgtListener {
    private static Log log = LogFactory.getLog(DefaultRolesCreatorForSuperTenant.class);
    private static final int EXEC_ORDER = 40;

    @Override
    public void onTenantCreate(TenantInfoBean tenantInfoBean) throws StratosException {
        try {
            createDefaultRoles(tenantInfoBean);
            createNewClaimMappingforFirstLoginCheck(tenantInfoBean);
        } catch (UserStoreException e) {
            String message = "Failed to read roles from appfactory configuration.";
            log.error(message);
            throw new StratosException(message, e);
        } catch (AppFactoryException e) {
            String message = "Failed to read roles from appfactory configuration.";
            log.error(message);
            throw new StratosException(message, e);
        }
    }


    private void createDefaultRoles(TenantInfoBean tenantInfoBean) throws UserStoreException, AppFactoryException {
        String adminUser = tenantInfoBean.getAdmin();
        Set<RoleBean> roleBeanList = null;
        try {
            roleBeanList = AppFactoryUtil.getRolePermissionConfigurations(AppFactoryConstants.TENANT_ROLES_DEFAULT_USER_ROLE, adminUser);
            roleBeanList.addAll(AppFactoryUtil.getRolePermissionConfigurations(AppFactoryConstants.TENANT_ROLES_ROLE, adminUser));
        } catch (AppFactoryException e) {
            String errorMsg = "Failed to get permission collections from appfactory configuration.";
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }

        UserStoreManager userStoreManager = Util.getRealmService().getTenantUserRealm(tenantInfoBean.getTenantId()).getUserStoreManager();
        AuthorizationManager authorizationManager = Util.getRealmService().getTenantUserRealm(tenantInfoBean.getTenantId()).
                getAuthorizationManager();
        AppFactoryUtil.addRolePermissions(userStoreManager, authorizationManager, roleBeanList);

        // Update tenant admin user creation
        updateBAMStats(adminUser, tenantInfoBean.getTenantId(),
                AppFactoryConstants.BAM_ADD_DATA);


    }


    /**
     * Update stats on BAM
     *
     * @param userName user name
     * @param action   action
     */
    private void updateBAMStats(String userName, int tenantId, String action) {
        BamDataPublisher bamDataPublisher = BamDataPublisher.getInstance();
        try {
            bamDataPublisher.PublishTenantUserUpdateEvent("" + tenantId, userName, action,
                    System.currentTimeMillis());
        } catch (AppFactoryException e) {
            String msg = e.getMessage();
            if ((AppFactoryConstants.BAM_DELETE_DATA).equals(action)) {
                msg =
                        "Failed to publish data to BAM on user delete event for tenant " +
                                CarbonContext.getThreadLocalCarbonContext().getTenantDomain() + " due to " +
                                e.getMessage();
            } else if ((AppFactoryConstants.BAM_ADD_DATA).equals(action)) {
                msg =
                        "Failed to publish data to BAM on user add event for tenant " +
                                CarbonContext.getThreadLocalCarbonContext().getTenantDomain() + " due to " +
                                e.getMessage();

            }

            log.error(msg);
        }
    }

    private void createNewClaimMappingforFirstLoginCheck(TenantInfoBean tenantInfoBean) throws UserStoreException {
        UserStoreManager userStoreManager = Util.getRealmService().getTenantUserRealm(tenantInfoBean.getTenantId()).getUserStoreManager();
        ClaimManager claimManager = userStoreManager.getClaimManager();
        Claim claim = new Claim();
        claim.setClaimUri(AppFactoryConstants.CLAIMS_FIRSTLOGIN);
        claim.setDialectURI(AppFactoryConstants.CLAIMS_FIRSTLOGIN);
        claim.setDisplayTag("FirstLoggin");
        ClaimMapping claimmapping = new ClaimMapping(claim, AppFactoryConstants.FIRST_LOGGIN_MAPPED_TO);
        claimManager.addNewClaimMapping(claimmapping);
    }

    @Override
    public void onTenantUpdate(TenantInfoBean tenantInfoBean) throws StratosException {
        // Do nothing
    }

    @Override
    public void onTenantRename(int i, String s, String s1) throws StratosException {
        // Do nothing
    }

    @Override
    public void onTenantInitialActivation(int i) throws StratosException {
        // Do nothing
    }

    @Override
    public void onTenantActivation(int i) throws StratosException {
        // Do nothing
    }

    @Override
    public void onTenantDeactivation(int i) throws StratosException {
        // Do nothing
    }

    @Override
    public void onSubscriptionPlanChange(int i, String s, String s1) throws StratosException {
        // Do nothing
    }

    @Override
    public int getListenerOrder() {
        return EXEC_ORDER;
    }
 	
}
