/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.*;
import org.wso2.carbon.appfactory.core.build.ArtifactCreator;
import org.wso2.carbon.appfactory.core.build.ContinuousIntegrationStatisticsService;
import org.wso2.carbon.appfactory.core.registry.AppFacRegistryResourceService;
import org.wso2.carbon.appfactory.core.registry.AppFacRemoteRegistryAccessService;
import org.wso2.carbon.appfactory.core.util.AppFactoryDBUtil;
import org.wso2.carbon.appfactory.core.util.DependencyUtil;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.mgt.UserMgtConstants;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name=
 *                "org.wso2.carbon.appfactory.core.internal.AppFactoryCoreServiceComponent"
 *                immediate="true"
 * @scr.reference name="appfactory.configuration"
 *                interface="org.wso2.carbon.appfactory.common.AppFactoryConfiguration"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setAppFactoryConfiguration"
 *                unbind="unsetAppFactoryConfiguration"
 * @scr.reference name="appfactory.artifact"
 *                interface="org.wso2.carbon.appfactory.core.ArtifactStorage"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setArtifactStorage"
 *                unbind="unsetArtifactStorage"
 * @scr.reference name="appfactory.cidriver"
 *                interface="org.wso2.carbon.appfactory.core.ContinuousIntegrationSystemDriver"
 *                cardinality="0..1" policy="dynamic"
 *                bind="setContinuousIntegrationSystemDriver"
 *                unbind="unsetContinuousIntegrationSystemDriver"
 * @scr.reference name="registry.service"
 *                interface="org.wso2.carbon.registry.core.service.RegistryService"
 *                cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="appfactory.storage"
 *                interface="org.wso2.carbon.appfactory.core.Storage"
 *                cardinality="0..n" policy="dynamic"
 *                bind="setStorage"
 *                unbind="unsetStorage"
 * @scr.reference name="user.realmservice.default"
 *                interface="org.wso2.carbon.user.core.service.RealmService"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setRealmService"
 *                unbind="unsetRealmService"
 * @scr.reference name="registry.loader.default"
 *                interface="org.wso2.carbon.registry.core.service.TenantRegistryLoader"
 *                cardinality="1..1"
 *                policy="dynamic"
 *                bind="setRegistryLoader"
 *                unbind="unsetRegistryLoader"
 * @scr.reference name="config.context.service"
 *                interface="org.wso2.carbon.utils.ConfigurationContextService"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setConfigurationContextService"
 *                unbind="unsetConfigurationContextService"
 * @scr.reference name="repository.initializer"
 *                interface="org.wso2.carbon.appfactory.core.TenantRepositoryManagerInitializer"
 *                cardinality="1..n" policy="dynamic"
 *                bind="setTenantRepositoryManagerInitializer"
 *                unbind="unsetTenantRepositoryManagerInitializer"
 * @scr.reference name="build.initializer"
 *                interface="org.wso2.carbon.appfactory.core.TenantBuildManagerInitializer"
 *                cardinality="1..n" policy="dynamic"
 *                bind="setTenantBuildManagerInitializer"
 *                unbind="unsetTenantBuildManagerInitializer"
 *@scr.reference name="cloud.initializer"
 *                interface="org.wso2.carbon.appfactory.core.TenantCloudInitializer"
 *                cardinality="1..n" policy="dynamic"
 *                bind="setTenantCloudInitializer"
 *                unbind="unsetTenantCloudInitializer"
 * @scr.reference name="ntask.component"
 *                interface="org.wso2.carbon.ntask.core.service.TaskService"
 *                cardinality="1..1" policy="dynamic" bind="setTaskService"
 *                unbind="unsetTaskService"
 */
public class AppFactoryCoreServiceComponent {

	private static final Log log = LogFactory.getLog(AppFactoryCoreServiceComponent.class);
	private static BundleContext bundleContext;

	protected void activate(ComponentContext context) {
		AppFactoryCoreServiceComponent.bundleContext = context.getBundleContext();

		try {

			DependencyUtil.createDependenciesMountPoints();
		} catch (AppFactoryException e) {
			log.error("Error while creating mount points for dependency management");
		}

		try {

			Registry registry = ServiceHolder.getRegistryService().getGovernanceSystemRegistry();
			Collection resource = registry.newCollection();
			resource.setProperty(UserMgtConstants.DISPLAY_NAME, "appfactory");
			registry.put("/permission/admin/appfactory", resource);

			AppFactoryConfiguration config = AppFactoryUtil.getAppfactoryConfiguration();
			String[] permissionList = config.getProperties("Permissions.Permission");
			for (String permission : permissionList) {
				String permissionName = config.getFirstProperty("Permissions.Permission." + permission);
				resource = registry.newCollection();
				resource.setProperty(UserMgtConstants.DISPLAY_NAME, permissionName);
				registry.put(permission, resource);
			}

			bundleContext.registerService(ContinuousIntegrationStatisticsService.class.getName(),
			                              new ContinuousIntegrationStatisticsService(), null);
			bundleContext.registerService(ArtifactCreator.class.getName(), new ArtifactCreator(), null);
			bundleContext.registerService(RemoteRegistryService.class.getName(),
			                              new AppFacRemoteRegistryAccessService(), null);
			bundleContext.registerService(AppFacRegistryResourceService.class.getName(),
			                              new AppFacRegistryResourceService(), null);
            AppFactoryDBUtil.initializeDatasource();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			/*if (log.isDebugEnabled()) {*/
				log.info("Appfactory core bundle is activated");
			/*}*/
		} catch (Throwable e) {
			log.error("Error in creating appfactory configuration", e);
		}

	}

	protected void deactivate(ComponentContext context) {
		if (log.isDebugEnabled()) {
			log.debug("Appfactory common bundle is deactivated");
		}
	}

	protected void unsetArtifactStorage(ArtifactStorage artifactStorage) {
		ServiceHolder.setArtifactStorage(null);
	}

	protected void setArtifactStorage(ArtifactStorage artifactStorage) {
		ServiceHolder.setArtifactStorage(artifactStorage);
	}

	protected void unsetStorage(Storage storage) {
		ServiceHolder.setArtifactStorage(null);
	}

	/**
	 * In here we have stored multiple implementation of Storage interface with property value set when it
	 * is register by some other bundle.
	 * 
	 * @param ref
	 *            ServiceReference
	 */
	protected void setStorage(ServiceReference ref) {
		String propertyValue = (String) ref.getProperty(AppFactoryConstants.STORAGE_TYPE);
		Storage storage = (Storage)ref.getBundle().getBundleContext().getService(ref);
		ServiceHolder.addStorage(propertyValue, storage);
	}

	protected void setAppFactoryConfiguration(AppFactoryConfiguration appFactoryConfiguration) {
		ServiceHolder.setAppFactoryConfiguration(appFactoryConfiguration);
	}

	protected void unsetAppFactoryConfiguration(AppFactoryConfiguration appFactoryConfiguration) {
		ServiceHolder.setAppFactoryConfiguration(null);
	}

	public static BundleContext getBundleContext() {
		return bundleContext;
	}

	public static void setBundleContext(BundleContext bundleContext) {
		AppFactoryCoreServiceComponent.bundleContext = bundleContext;
	}

	protected void unsetContinuousIntegrationSystemDriver(ContinuousIntegrationSystemDriver driver) {
		ServiceHolder.setContinuousIntegrationSystemDriver(null);
	}

	protected void setContinuousIntegrationSystemDriver(ContinuousIntegrationSystemDriver driver) {
		ServiceHolder.setContinuousIntegrationSystemDriver(driver);
	}

	protected void setRegistryService(RegistryService registryService) {
		if (registryService != null && log.isDebugEnabled()) {
			log.debug("Registry service initialized");
		}
		ServiceHolder.setRegistryService(registryService);
	}

	protected void unsetRegistryService(RegistryService registryService) {
		ServiceHolder.setRegistryService(null);
	}

	protected void setRealmService(RealmService realmService) {
		if (log.isDebugEnabled()) {
			log.debug("RealmService acquired");
		}
		ServiceHolder.setRealmService(realmService);
	}

	protected void unsetRealmService(RealmService realmService) {
		ServiceHolder.setRealmService(null);
	}

	protected void setRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
		ServiceHolder.setTenantRegistryLoader(tenantRegistryLoader);
	}

	protected void unsetRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
		ServiceHolder.setTenantRegistryLoader(null);
	}

	protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {
		ServiceHolder.getInstance().setConfigContextService(configurationContextService);
	}

	protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {
		ServiceHolder.getInstance().setConfigContextService(null);
	}

	protected void setTenantRepositoryManagerInitializer(TenantRepositoryManagerInitializer tenantRepositoryManagerInitializer) {
		ServiceHolder.getInstance().addTenantRepositoryManagerInitializer(tenantRepositoryManagerInitializer);
	}

	protected void unsetTenantRepositoryManagerInitializer(TenantRepositoryManagerInitializer tenantRepositoryManagerInitializer) {
		// ServiceHolder.getInstance().setConfigContextService(null);
	}

	protected void setTenantBuildManagerInitializer(TenantBuildManagerInitializer tenantBuildManagerInitializer) {
		ServiceHolder.getInstance().addTenantBuildManagerInitializer(tenantBuildManagerInitializer);
	}

	protected void unsetTenantBuildManagerInitializer(TenantBuildManagerInitializer tenantBuildManagerInitializer) {
		// ServiceHolder.getInstance().setConfigContextService(null);
	}
    protected void setTenantCloudInitializer(TenantCloudInitializer tenantBuildManagerInitializer) {
        ServiceHolder.getInstance().addTenantCloudInitializer(tenantBuildManagerInitializer);
    }

    protected void unsetTenantCloudInitializer(TenantCloudInitializer tenantCloudInitializer) {
        // ServiceHolder.getInstance().setConfigContextService(null);
    }

	protected void setTaskService(TaskService taskService) throws RegistryException {
		ServiceHolder.getInstance().setTaskService(taskService);

	}

	protected void unsetTaskService(TaskService taskService) {
		ServiceHolder.getInstance().setTaskService(null);
	}

}
