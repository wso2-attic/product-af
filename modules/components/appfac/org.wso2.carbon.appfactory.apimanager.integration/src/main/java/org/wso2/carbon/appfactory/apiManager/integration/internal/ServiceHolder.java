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

package org.wso2.carbon.appfactory.apiManager.integration.internal;

import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.user.core.service.RealmService;

public class ServiceHolder {
    private static AppFactoryConfiguration appFactoryConfiguration;
    private static RegistryService registryService;
    private static RealmService realmService;
    private static TenantRegistryLoader tenantRegistryLoader;

    private static final ServiceHolder instance = new ServiceHolder();

    private ServiceHolder() {
    }

    public static ServiceHolder getInstance(){
        return instance;
    }
    public AppFactoryConfiguration getAppFactoryConfiguration() {
        return appFactoryConfiguration;
    }

    public void setAppFactoryConfiguration(AppFactoryConfiguration appFactoryConfiguration) {
        ServiceHolder.appFactoryConfiguration = appFactoryConfiguration;
    }

    public RegistryService getRegistryService() {
        return registryService;
    }

    public void setRegistryService(RegistryService registryService) {
        ServiceHolder.registryService = registryService;
    }

    public RealmService getRealmService() {
        return realmService;
    }


    public synchronized void setRealmService(RealmService realmSer) {
        realmService = realmSer;
    }

    public TenantRegistryLoader getTenantRegistryLoader() {
        return tenantRegistryLoader;
    }

    public void setTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        ServiceHolder.tenantRegistryLoader = tenantRegistryLoader;
    }
}
