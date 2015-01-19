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
package org.wso2.carbon.appfactory.core.task;

import java.util.Calendar;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.core.TenantCreationNotificationInitializer;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.ntask.core.Task;
import org.apache.stratos.tenant.mgt.stub.TenantMgtAdminServiceStub;
import org.apache.stratos.tenant.mgt.stub.beans.xsd.TenantInfoBean;

/**
 * Task for initializing App Factory Tenant Storage
 */
public class AppFactoryTenantCreationNotificationInitializerTask implements Task {
    private static final Log log = LogFactory.getLog(AppFactoryTenantCloudInitializerTask.class);
    public static final String SERVICE_EPR = "epr";
    public static final String TENANT_USAGE_PLAN = "usagePlan";
    public static final String TENANT_DOMAIN = "tenantDomain";
    public static final String SUCCESS_KEY = "successKey";
    public static final String ADMIN_USERNAME = "adminUsername";
    public static final String ADMIN_PASSWORD = "adminPassword";
    public static final String ADMIN_EMAIL = "email";
    public static final String ADMIN_FIRST_NAME = "firstName";
    public static final String ADMIN_LAST_NAME = "lastName";
    public static final String ORIGINATED_SERVICE = "originatedService";
    public static final String SUPER_TENANT_ADMIN = "superAdmin";
    public static final String SUPER_TENANT_ADMIN_PASSWORD = "superAdminPassword";
    public TenantMgtAdminServiceStub stub;
    public Map<String, String> properties;

    @Override
    public void setProperties(Map<String, String> stringStringMap) {
        this.properties = stringStringMap;
    }

    @Override
    public void init() {
    	if (log.isDebugEnabled()) {
            log.debug("Initializing AppFactoryTenantBuildManagerInitializerTask for " + properties.get
                    (AppFactoryTenantBuildManagerInitializerTask.TENANT_DOMAIN));
        }
    }

    @Override
    public void execute() {
        TenantInfoBean tenantInfoBean = new TenantInfoBean();
        tenantInfoBean.setCreatedDate(Calendar.getInstance());
        tenantInfoBean.setUsagePlan(properties.get(TENANT_USAGE_PLAN));
        tenantInfoBean.setTenantDomain(properties.get(TENANT_DOMAIN));
        tenantInfoBean.setSuccessKey(properties.get(SUCCESS_KEY));
        tenantInfoBean.setActive(true);
        tenantInfoBean.setAdmin(properties.get(ADMIN_USERNAME));
        tenantInfoBean.setAdminPassword(properties.get(ADMIN_PASSWORD));
        tenantInfoBean.setEmail(properties.get(ADMIN_EMAIL));
        tenantInfoBean.setFirstname(properties.get(ADMIN_FIRST_NAME));
        tenantInfoBean.setLastname(properties.get(ADMIN_LAST_NAME));
        tenantInfoBean.setOriginatedService(properties.get(ORIGINATED_SERVICE));
        for (TenantCreationNotificationInitializer initializer : ServiceHolder.getInstance().
                getTenantCreationNotificationInitializerList()) {
            initializer.onTenantCreation(tenantInfoBean);
        }
    }
}
