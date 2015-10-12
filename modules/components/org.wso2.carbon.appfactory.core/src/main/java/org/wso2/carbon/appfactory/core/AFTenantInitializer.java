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

package org.wso2.carbon.appfactory.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.RoleBean;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.appfactory.core.workflow.WorkflowConstant;
import org.wso2.carbon.appfactory.core.workflow.WorkflowExecutor;
import org.wso2.carbon.appfactory.core.workflow.WorkflowExecutorFactory;
import org.wso2.carbon.appfactory.core.workflow.dto.TenantCreationWorkflowDTO;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.user.api.*;

import java.util.Map;
import java.util.Set;

/**
 * Tenant level roles defined in appfactory.xml are created through this
 * class.
 * All the permissions defined are assigned to the roles and if the role is
 * existing, permissions
 * are updated.
 */
public class AFTenantInitializer {
    private static Log log = LogFactory.getLog(AFTenantInitializer.class);
    // TODO: Get this claims from a config file
    private static String FIRST_NAME_CLAIM_URI = "http://wso2.org/claims/givenname";
    private static String LAST_NAME_CLAIM_URI = "http://wso2.org/claims/lastname";

    public static void initializeAFTenant(String tenantDomain) throws UserStoreException, AppFactoryException {
        int tenantId = ServiceHolder.getInstance().getRealmService().getTenantManager().getTenantId(tenantDomain);
        Tenant tenantInfoBean = ServiceHolder.getInstance().getRealmService().getTenantManager().getTenant(tenantId);
        //APPFAC-3211 fix
        tenantInfoBean.setAdminPassword("");
        UserRealm userRealm = ServiceHolder.getInstance().getRealmService().getTenantUserRealm(tenantId);
        String defaultRole = AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(
                AppFactoryConstants.TENANT_ROLES_DEFAULT_USER_ROLE);
        boolean isExisting = userRealm.getUserStoreManager().isExistingRole(defaultRole);
        if (!isExisting) {
            //TODO acquire clusterwide lock
            isExisting = userRealm.getUserStoreManager().isExistingRole(defaultRole);
            if (!isExisting) {
                Set<RoleBean> roleBeanList = AppFactoryUtil.getRolePermissionConfigurations(
                        AppFactoryConstants.TENANT_ROLES_DEFAULT_USER_ROLE,
                        tenantInfoBean.getAdminName());
                roleBeanList.addAll(AppFactoryUtil.getRolePermissionConfigurations(
                        AppFactoryConstants.TENANT_ROLES_ROLE, tenantInfoBean.getAdminName()));

                UserStoreManager userStoreManager = userRealm.getUserStoreManager();
                String[] claims = new String[]{FIRST_NAME_CLAIM_URI, LAST_NAME_CLAIM_URI};
                Map<String, String> claimMappings = userStoreManager.getUserClaimValues(
                        tenantInfoBean.getAdminName(), claims, null);
                String firstName = claimMappings.get(FIRST_NAME_CLAIM_URI);
                String lastName = claimMappings.get(LAST_NAME_CLAIM_URI);
                AuthorizationManager authorizationManager = userRealm.getAuthorizationManager();
                AppFactoryUtil.addRolePermissions(userStoreManager, authorizationManager, roleBeanList);

                try {
                    executeTenantCreationWorkflow(tenantDomain, tenantId, tenantInfoBean, firstName, lastName);
                } catch (AppFactoryException e) {
                    String message = "Unable to initialize tenant. Please contact administrator";
                    log.error(message, e);
                    throw new AppFactoryException(message, e);
                }

            }
        }
    }

    /**
     * Execute tenant creation workflow according to configured workflow config type
     *
     * @param tenantDomain Domain of the tenant
     * @param tenantId     id of the tenant
     * @param tenant       The details of the tenant
     * @param firstName    First name of the tenant
     * @param lastName     Last name of the tenant
     * @throws AppFactoryException
     */
    private static void executeTenantCreationWorkflow(String tenantDomain, int tenantId, Tenant tenant,
            String firstName, String lastName) throws AppFactoryException {

        TenantInfoBean bean = populateTenantInfoBean(tenantDomain, tenantId, tenant, firstName, lastName);

        WorkflowExecutorFactory workflowExecutorFactory = WorkflowExecutorFactory.getInstance();

        WorkflowExecutor tenantCreationWorkflowExecutor = workflowExecutorFactory
                .getWorkflowExecutor(WorkflowConstant.WorkflowType.TENANT_CREATION);
        TenantCreationWorkflowDTO tenantCreationWorkflow = getTenantCreationWorkflowDTO(tenantDomain, tenantId,
                workflowExecutorFactory, bean);
        tenantCreationWorkflowExecutor.execute(tenantCreationWorkflow);

    }

    private static TenantInfoBean populateTenantInfoBean(String tenantDomain, int tenantId, Tenant tenant,
            String firstName, String lastName) {
        String successKey = "key";
        String usagePlan = "Demo";
        String originatedService = "WSO2 Stratos Manager";

        TenantInfoBean bean = new TenantInfoBean();
        bean.setAdmin(tenant.getAdminName());
        bean.setFirstname(firstName);
        bean.setLastname(lastName);
        bean.setAdminPassword(tenant.getAdminPassword());
        bean.setTenantDomain(tenantDomain);
        bean.setTenantId(tenantId);
        bean.setEmail(tenant.getEmail());
        bean.setActive(true);
        bean.setSuccessKey(successKey);
        bean.setUsagePlan(usagePlan);
        bean.setOriginatedService(originatedService);
        return bean;
    }

    private static TenantCreationWorkflowDTO getTenantCreationWorkflowDTO(String tenantDomain, int tenantId,
            WorkflowExecutorFactory workflowExecutorFactory, TenantInfoBean bean) {
        TenantCreationWorkflowDTO tenantCreationWorkflowDTO = (TenantCreationWorkflowDTO) workflowExecutorFactory
                .createWorkflowDTO(WorkflowConstant.WorkflowType.TENANT_CREATION);
        tenantCreationWorkflowDTO.setTenantDomain(tenantDomain);
        tenantCreationWorkflowDTO.setTenantId(tenantId);
        tenantCreationWorkflowDTO.setTenantInfoBean(bean);
        return tenantCreationWorkflowDTO;
    }

}
