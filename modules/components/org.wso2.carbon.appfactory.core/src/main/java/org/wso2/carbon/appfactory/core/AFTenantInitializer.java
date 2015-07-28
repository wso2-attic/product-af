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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.rampart.RampartMessageData;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.RoleBean;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.user.api.*;
import org.wso2.carbon.utils.CarbonUtils;

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
                AuthorizationManager authorizationManager = userRealm.getAuthorizationManager();
                AppFactoryUtil.addRolePermissions(userStoreManager, authorizationManager, roleBeanList);

                //call BPEL

                String EPR = AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(
                                                   AppFactoryConstants.BPS_SERVER_URL) + "CreateTenant";

                String value = "<p:CreateTenantRequest xmlns:p=\"http://wso2.org/bps/sample\">" +
                               "<admin xmlns=\"http://wso2.org/bps/sample\">" + tenantInfoBean.getAdminName() + "</admin>" +
                               "<firstName xmlns=\"http://wso2.org/bps/sample\">" + tenantInfoBean.getAdminFirstName() + "</firstName>" +
                               "<lastName xmlns=\"http://wso2.org/bps/sample\">" + tenantInfoBean.getAdminLastName() + "</lastName>" +
                               "<adminPassword xmlns=\"http://wso2.org/bps/sample\">" + tenantInfoBean.getAdminPassword() + "</adminPassword>" +
                               "<tenantDomain xmlns=\"http://wso2.org/bps/sample\">" + tenantDomain + "</tenantDomain>" +
                               "<tenantId xmlns=\"http://wso2.org/bps/sample\">" + tenantId +
                               "</tenantId>" +
                               "<email xmlns=\"http://wso2.org/bps/sample\">" + tenantInfoBean.getEmail() + "</email>" +
                               "<active xmlns=\"http://wso2.org/bps/sample\">true</active>" +
                               "<successKey xmlns=\"http://wso2.org/bps/sample\">key</successKey>" +
                               "<createdDate xmlns=\"http://wso2.org/bps/sample\">2001-12-31T12:00:00</createdDate>" +
                               "<originatedService xmlns=\"http://wso2.org/bps/sample\">WSO2 Stratos Manager</originatedService>" +
                               "<usagePlan xmlns=\"http://wso2.org/bps/sample\">Demo</usagePlan>" +
                               "</p:CreateTenantRequest>";

                try {

                    ConfigurationContext context =
                            ServiceHolder.getInstance().getConfigContextService().getClientConfigContext();
                    ServiceClient serviceClient = new ServiceClient(context, null);
                    serviceClient.engageModule("rampart");
                    serviceClient.engageModule("addressing");

                    Options options = serviceClient.getOptions();
                    options.setTo(new EndpointReference(EPR));
                    options.setAction("http://wso2.org/bps/sample/process");

                    Policy policy = loadPolicy(
                            CarbonUtils.getCarbonConfigDirPath() + "/appfactory/bpel-policy.xml");

                    options.setProperty(RampartMessageData.KEY_RAMPART_POLICY, policy);

                    options.setUserName(AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(
                                                              AppFactoryConstants.SERVER_ADMIN_NAME));
                    options.setPassword(AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(
                                                              AppFactoryConstants.SERVER_ADMIN_PASSWORD));
                    OMElement payload = AXIOMUtil.stringToOM(value);
                    serviceClient.sendReceive(payload);

                } catch (Exception e) {
                    log.error("Error while calling tenant creation BPEL", e);
                    throw new AppFactoryException(
                            "Unable to initialize tenant. Please contact administrator");
                }

                log.info(
                        "The BPEL ran successfully to create tenant in all Clouds. Tenant domain is " +
                        tenantDomain + ". Tenant Id is " + tenantId);
            }
        }
    }

    private static Policy loadPolicy(String xmlPath) throws Exception {
        StAXOMBuilder builder = new StAXOMBuilder(xmlPath);
        return PolicyEngine.getPolicy(builder.getDocumentElement());
    }



}
