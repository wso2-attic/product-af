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

package org.wso2.carbon.appfactory.repository.mgt.internal;

import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.core.services.LifecycleManagementService;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryProvider;
import org.wso2.carbon.appfactory.tenant.mgt.service.TenantManagementService;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton class for storing services and map to hold repository providers vs type
 */
public class Util {
    private static AppFactoryConfiguration configuration = null;
    private static RealmService realmService;
    //this is a map to sore Repository providers against repository type(i.e svn,git)
    private static Map<String, RepositoryProvider> repositoryProviderMap = new HashMap<String, RepositoryProvider>();
    private static LifecycleManagementService lifecycleManagementService;
    private static TenantManagementService tenantManagementService;

    public static AppFactoryConfiguration getConfiguration() {
        return configuration;
    }

    public static void setConfiguration(AppFactoryConfiguration configuration) {
        Util.configuration = configuration;
    }

    public static void setRealmService(RealmService realmService) {
        Util.realmService = realmService;
    }

    public static RealmService getRealmService() {
        return Util.realmService;
    }

    public static RepositoryProvider getRepositoryProvider(String type) {
        return repositoryProviderMap.get(type);
    }

    public static void setRepositoryProvider(String type, RepositoryProvider provider) {
        Util.repositoryProviderMap.put(type, provider);
    }

    public static boolean isProviderMapEmpty() {
        return repositoryProviderMap.isEmpty();
    }

    public static TenantManagementService getTenantManagementService() {
        return tenantManagementService;
    }

    public static void setTenantManagementService(TenantManagementService tenantManagementService) {
        Util.tenantManagementService = tenantManagementService;
    }

    public static void setLifecycleManagementService(LifecycleManagementService lifecycleManagementService){
        Util.lifecycleManagementService = lifecycleManagementService;
    }

    public static LifecycleManagementService getLifecycleManagementService(){
        return Util.lifecycleManagementService;
    }
}
