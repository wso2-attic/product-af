/*
 * Copyright 2005-2013 WSO2, Inc. (http://wso2.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.appfactory.core.internal;

import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.core.*;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceHolder {

	public static ArtifactStorage artifactStorage;
	public static Storage storage;
	public static ContinuousIntegrationSystemDriver continuousIntegrationSystemDriver;
	public static AppFactoryConfiguration appFactoryConfiguration;
	private static RegistryService registryService;
	private static RealmService realmService;
	private static TenantRegistryLoader tenantRegistryLoader;
	private ConfigurationContextService configContextService;
	private static List<TenantRepositoryManagerInitializer> tenantRepositoryManagerInitializerList =
	                                                                                                 new ArrayList<TenantRepositoryManagerInitializer>();
	private static List<TenantBuildManagerInitializer> tenantBuildManagerInitializerList =
	                                                                                       new ArrayList<TenantBuildManagerInitializer>();
	private static List<TenantCreationNotificationInitializer> tenantCreationNotificationInitializerList =
	                                                                                                       new ArrayList<TenantCreationNotificationInitializer>();
    private static List<TenantCloudInitializer> tenantCloudInitializerList=new
            ArrayList<TenantCloudInitializer>();
	private static TaskService taskService;
	private static final ServiceHolder instance = new ServiceHolder();

	private static Map<String, Storage> storageMap = new HashMap<String, Storage>();

	private ServiceHolder() {
	}

	public static ServiceHolder getInstance() {
		return instance;
	}

	public static ContinuousIntegrationSystemDriver getContinuousIntegrationSystemDriver() {
		return continuousIntegrationSystemDriver;
	}

	public static void setContinuousIntegrationSystemDriver(ContinuousIntegrationSystemDriver continuousIntegrationSystemDriver) {
		ServiceHolder.continuousIntegrationSystemDriver = continuousIntegrationSystemDriver;
	}

	public static ArtifactStorage getArtifactStorage() {
		return artifactStorage;
	}

	public static void setArtifactStorage(ArtifactStorage artifactStorage) {
		ServiceHolder.artifactStorage = artifactStorage;
	}

	public static AppFactoryConfiguration getAppFactoryConfiguration() {
		return appFactoryConfiguration;
	}

	public static void setAppFactoryConfiguration(AppFactoryConfiguration appFactoryConfiguration) {
		ServiceHolder.appFactoryConfiguration = appFactoryConfiguration;
	}

	public static RegistryService getRegistryService() {
		return registryService;
	}

	public static void setRegistryService(RegistryService registryService) {
		ServiceHolder.registryService = registryService;
	}

	public static RealmService getRealmService() {
		return realmService;
	}

	public static synchronized void setRealmService(RealmService realmSer) {
		realmService = realmSer;
	}

	public static TenantRegistryLoader getTenantRegistryLoader() {
		return tenantRegistryLoader;
	}

	public static void setTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
		ServiceHolder.tenantRegistryLoader = tenantRegistryLoader;
	}

	public ConfigurationContextService getConfigContextService() {
		return configContextService;
	}

	public void setConfigContextService(ConfigurationContextService configContextService) {
		this.configContextService = configContextService;
	}

	public void addTenantRepositoryManagerInitializer(TenantRepositoryManagerInitializer initializer) {
		tenantRepositoryManagerInitializerList.add(initializer);
	}

	public void addTenantCreationNotificationInitializer(TenantCreationNotificationInitializer initializer) {
		tenantCreationNotificationInitializerList.add(initializer);
	}

	public void addTenantBuildManagerInitializer(TenantBuildManagerInitializer initializer) {
		tenantBuildManagerInitializerList.add(initializer);
	}

	public List<TenantRepositoryManagerInitializer> getTenantRepositoryManagerInitializerList() {
		return tenantRepositoryManagerInitializerList;
	}

	public List<TenantBuildManagerInitializer> getTenantBuildManagerInitializerList() {
		return tenantBuildManagerInitializerList;
	}

	public List<TenantCreationNotificationInitializer> getTenantCreationNotificationInitializerList() {
		return tenantCreationNotificationInitializerList;
	}

	public TaskService getTaskService() {
		return taskService;
	}

	public void setTaskService(TaskService taskService) {
		ServiceHolder.taskService = taskService;
	}

	public static void addStorage(String key, Storage storage) {
		ServiceHolder.storageMap.put(key, storage);
	}

	public static Storage getStorage(String key) {
		return ServiceHolder.storageMap.get(key);
	}

    public void addTenantCloudInitializer(TenantCloudInitializer tenantBuildManagerInitializer) {
        tenantCloudInitializerList.add(tenantBuildManagerInitializer);
    }
    public List<TenantCloudInitializer> getTenantCloudInitializer() {
        return tenantCloudInitializerList;
    }
}
