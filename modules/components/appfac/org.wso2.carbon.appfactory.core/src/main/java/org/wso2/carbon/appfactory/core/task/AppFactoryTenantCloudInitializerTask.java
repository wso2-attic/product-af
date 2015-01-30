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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.core.TenantCloudInitializer;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.ntask.core.Task;

import java.util.Map;

/**
 * Task for initializing App Factory Tenant Cloud  in an environment eg:Dev QA
 */
public class AppFactoryTenantCloudInitializerTask implements Task {
    private static final Log log = LogFactory.getLog(AppFactoryTenantCloudInitializerTask.class);
    public static final String SERVER_URL = "serverURL";
    public static final String TENANT_USAGE_PLAN = "usagePlan";
    public static final String TENANT_DOMAIN = "tenantDomain";
    public static final String TENANT_ID = "tenantID";
    public static final String SUCCESS_KEY = "successKey";
    public static final String ADMIN_USERNAME = "adminUsername";
    public static final String ADMIN_PASSWORD = "adminPassword";
    public static final String ADMIN_EMAIL = "email";
    public static final String ADMIN_FIRST_NAME = "firstName";
    public static final String ADMIN_LAST_NAME = "lastName";
    public static final String ORIGINATED_SERVICE = "originatedService";
    public static final String SUPER_TENANT_ADMIN = "superAdmin";
    public static final String SUPER_TENANT_ADMIN_PASSWORD = "superAdminPassword";
    public static final String STAGE = "stage";
    public static final String RUNTIMES = "runtimes";

    private Map<String, String> properties;

    @Override
    public void setProperties(Map<String, String> stringStringMap) {
        this.properties = stringStringMap;
    }

    @Override
    public void init() {
        if (log.isDebugEnabled()) {
            log.debug("Initializing AppFactoryTenantCloudInitializerTask for " + properties.get
                    (AppFactoryTenantBuildManagerInitializerTask.TENANT_DOMAIN));
        }
    }

    @Override
    public void execute() {

        for (TenantCloudInitializer initializer : ServiceHolder.getInstance().
                getTenantCloudInitializer()) {
            initializer.onTenantCreation(properties);
        }

    }
}
