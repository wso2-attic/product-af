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

package org.wso2.carbon.appfactory.jenkins.build.internal;

import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.jenkins.build.JenkinsCISystemDriver;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryManager;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.user.core.service.RealmService;

public class ServiceContainer {

    private static AppFactoryConfiguration appFactoryConfiguration;
    private static RepositoryManager repositoryManager;
    
    private static JenkinsCISystemDriver jenkinsCISystemDriver;

    private static RealmService realmService;
    private static TenantRegistryLoader tenantRegistryLoader;

    public static AppFactoryConfiguration getAppFactoryConfiguration() {
        return appFactoryConfiguration;
    }

    public static void setAppFactoryConfiguration(AppFactoryConfiguration appFactoryConfiguration) {
        ServiceContainer.appFactoryConfiguration = appFactoryConfiguration;
    }

    public static RepositoryManager getRepositoryManager() {
        return repositoryManager;
    }

    public static void setRepositoryManager(RepositoryManager repositoryManager) {
        ServiceContainer.repositoryManager = repositoryManager;
    }

    public static JenkinsCISystemDriver getJenkinsCISystemDriver() {
        return jenkinsCISystemDriver;
    }

    public static void setJenkinsCISystemDriver(JenkinsCISystemDriver jenkinsCISystemDriver) {
        ServiceContainer.jenkinsCISystemDriver = jenkinsCISystemDriver;
    }

    public static RealmService getRealmService() {
        return realmService;
    }

    public static synchronized void setRealmService(RealmService realmSer) {
        ServiceContainer.realmService = realmSer;
    }

    public static TenantRegistryLoader getTenantRegistryLoader() {
        return tenantRegistryLoader;
    }

    public static void setTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        ServiceContainer.tenantRegistryLoader = tenantRegistryLoader;
    }
}
