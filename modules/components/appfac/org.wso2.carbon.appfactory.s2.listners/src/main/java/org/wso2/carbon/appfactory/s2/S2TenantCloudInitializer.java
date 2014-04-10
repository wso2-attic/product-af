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
package org.wso2.carbon.appfactory.s2;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.core.TenantCloudInitializer;
import org.wso2.carbon.appfactory.core.task.AppFactoryTenantCloudInitializerTask;
import org.wso2.carbon.appfactory.s2.util.AppFactoryTenantMgtUtil;
import org.wso2.carbon.tenant.mgt.stub.TenantMgtAdminServiceExceptionException;
import org.wso2.carbon.tenant.mgt.stub.TenantMgtAdminServiceStub;
import org.wso2.carbon.tenant.mgt.stub.beans.xsd.TenantInfoBean;
import org.wso2.carbon.utils.CarbonUtils;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Map;

/**
 * This creates tenant in particular environment in S2 based cloud
 */
public class S2TenantCloudInitializer implements TenantCloudInitializer {
    private static final Log log = LogFactory.getLog(S2TenantCloudInitializer.class);
    private TenantMgtAdminServiceStub stub;

    /**
     * This method will be called for creating tenant in a stage
     *
     * @param properties
     */
    @Override
    public void onTenantCreation(Map<String, String> properties) {
        String serviceEPR = properties.get(AppFactoryTenantCloudInitializerTask.SERVER_URL)
                + "/services/TenantMgtAdminService";
        try {
            stub = new TenantMgtAdminServiceStub(AppFactoryTenantMgtUtil.getConfigurationContextService()
                    .getClientConfigContext(), serviceEPR
            );
            CarbonUtils.setBasicAccessSecurityHeaders(properties.get
                    (AppFactoryTenantCloudInitializerTask.SUPER_TENANT_ADMIN),
                    properties.get(AppFactoryTenantCloudInitializerTask.SUPER_TENANT_ADMIN_PASSWORD),
                    stub._getServiceClient());
        } catch (AxisFault axisFault) {
            String msg = "Error while initializing TenantMgt Admin Service Stub ";
            log.error(msg, axisFault);
        }
        TenantInfoBean tenantInfoBean = new TenantInfoBean();
        tenantInfoBean.setCreatedDate(Calendar.getInstance());
        tenantInfoBean.setUsagePlan(properties.get(AppFactoryTenantCloudInitializerTask.TENANT_USAGE_PLAN));
        tenantInfoBean.setTenantDomain(properties.get(AppFactoryTenantCloudInitializerTask.TENANT_DOMAIN));
        tenantInfoBean.setSuccessKey(properties.get(AppFactoryTenantCloudInitializerTask.SUCCESS_KEY));
        tenantInfoBean.setActive(true);
        tenantInfoBean.setAdmin(properties.get(AppFactoryTenantCloudInitializerTask.ADMIN_USERNAME));
        tenantInfoBean.setAdminPassword(properties.get(AppFactoryTenantCloudInitializerTask.ADMIN_PASSWORD));
        tenantInfoBean.setEmail(properties.get(AppFactoryTenantCloudInitializerTask.ADMIN_EMAIL));
        tenantInfoBean.setFirstname(properties.get(AppFactoryTenantCloudInitializerTask.ADMIN_FIRST_NAME));
        tenantInfoBean.setLastname(properties.get(AppFactoryTenantCloudInitializerTask.ADMIN_LAST_NAME));
        tenantInfoBean.setOriginatedService(properties.get(AppFactoryTenantCloudInitializerTask.ORIGINATED_SERVICE));
        tenantInfoBean.setTenantId(Integer.parseInt(properties.get(AppFactoryTenantCloudInitializerTask.TENANT_ID)));
        try {
            stub.addTenant(tenantInfoBean);
           /* if (log.isDebugEnabled()) {*/
            log.info("Called TenantMgt Admin Service in " + properties.get
                    (AppFactoryTenantCloudInitializerTask.SERVER_URL) + " with " + tenantInfoBean);
           /* }*/

        } catch (RemoteException e) {
            String msg = "Error while adding tenant " + tenantInfoBean.getTenantDomain();
            log.error(msg, e);
        } catch (TenantMgtAdminServiceExceptionException e) {
            String msg = "Error while invoking TenantMgtAdminService for " + tenantInfoBean.getTenantDomain();
            log.error(msg, e);
        }
    }
}
