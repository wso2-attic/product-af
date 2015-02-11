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
import org.wso2.carbon.appfactory.core.TenantBuildManagerInitializer;
import org.wso2.carbon.appfactory.core.TenantRepositoryManagerInitializer;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.ntask.core.Task;

import java.util.Map;

/**
 * Task for initializing AppFactory Tenant Build Manager
 */
public class AppFactoryTenantBuildManagerInitializerTask implements Task {
    private static final Log log = LogFactory.getLog(AppFactoryTenantBuildManagerInitializerTask
            .class);
    public static String TENANT_DOMAIN = "tenantDomain";
    public static String TENANT_USAGE_PLAN = "usagePlan";
    private Map<String, String> properties;

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
        for (TenantBuildManagerInitializer initializer : ServiceHolder.getInstance().
                getTenantBuildManagerInitializerList()) {
            initializer.onTenantCreation(properties.get(TENANT_DOMAIN), properties.get(TENANT_USAGE_PLAN));
        }
    }
}
