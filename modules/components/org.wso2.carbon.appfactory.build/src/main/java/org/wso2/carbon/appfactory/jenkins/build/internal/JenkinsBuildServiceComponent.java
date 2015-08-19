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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.core.*;
import org.wso2.carbon.appfactory.jenkins.build.*;
import org.wso2.carbon.appfactory.jenkins.build.strategy.BucketSelectingStrategy;
import org.wso2.carbon.appfactory.jenkins.build.strategy.ClusterSelectingStrategy;
import org.wso2.carbon.appfactory.nonbuild.NonBuildableStorage;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryManager;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * @scr.component name="org.wso2.carbon.appfactory.jenkins.build.internal.JenkinsBuildServiceComponent"
 *                immediate="true"
 * @scr.reference name="appfactory.configuration" interface=
 *                "org.wso2.carbon.appfactory.common.AppFactoryConfiguration"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setAppFactoryConfiguration"
 *                unbind="unsetAppFactoryConfiguration"
 * @scr.reference name="repository.manager" interface=
 *                "org.wso2.carbon.appfactory.repository.mgt.RepositoryManager"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setRepositoryManager" unbind="unsetRepositoryManager"
 * @scr.reference name="user.realmservice.default"
 *                interface="org.wso2.carbon.user.core.service.RealmService"
 *                cardinality="1..1" policy="dynamic" bind="setRealmService"
 *                unbind="unsetRealmService"
 * @scr.reference name="registry.loader.default"
 *                interface="org.wso2.carbon.registry.core.service.TenantRegistryLoader"
 *                cardinality="1..1" policy="dynamic" bind="setRegistryLoader"
 *                unbind="unsetRegistryLoader"
 */
public class JenkinsBuildServiceComponent {

	private static final Log log = LogFactory
			.getLog(JenkinsBuildServiceComponent.class);

	@SuppressWarnings("UnusedDeclaration")
	protected void setAppFactoryConfiguration(
			AppFactoryConfiguration appFactoryConfiguration) {
		ServiceContainer.setAppFactoryConfiguration(appFactoryConfiguration);
	}

	@SuppressWarnings("UnusedDeclaration")
	protected void unsetAppFactoryConfiguration(
			AppFactoryConfiguration appFactoryConfiguration) {
		ServiceContainer.setAppFactoryConfiguration(null);
	}

	@SuppressWarnings("UnusedDeclaration")
	protected void setRepositoryManager(RepositoryManager repoManager) {
		ServiceContainer.setRepositoryManager(repoManager);
	}

	@SuppressWarnings("UnusedDeclaration")
	protected void unsetRepositoryManager(RepositoryManager repoManager) {
		ServiceContainer.setRepositoryManager(null);
	}

	protected void setRealmService(RealmService realmService) {
		if (log.isDebugEnabled()) {
			log.debug("RealmService acquired");
		}
		ServiceContainer.setRealmService(realmService);
	}

	protected void unsetRealmService(RealmService realmService) {
		ServiceContainer.setRealmService(null);
	}

	protected void setRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
		ServiceContainer.setTenantRegistryLoader(tenantRegistryLoader);
	}

	protected void unsetRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
		ServiceContainer.setTenantRegistryLoader(null);
	}

	@SuppressWarnings("UnusedDeclaration")
	protected void activate(ComponentContext context) {

		if (log.isDebugEnabled()) {
			log.debug("Jenkins build service bundle is activated");
		}
		try {

			if (isJenkinsEnabled()) {



				String jenkinsDefaultGlobalRoles = ServiceContainer
						.getAppFactoryConfiguration()
						.getFirstProperty(
								JenkinsCIConstants.DEFAULT_GLOBAL_ROLES_CONFIG_SELECTOR);

				String listenerPriority = ServiceContainer
						.getAppFactoryConfiguration()
						.getFirstProperty(
								JenkinsCIConstants.LISTENER_PRIORITY_CONFIG_SELECTOR);
				
				
				if (log.isDebugEnabled()) {
					log.debug(String.format("Default Global Roles : %s",
							jenkinsDefaultGlobalRoles));
					log.debug(String.format("Listener Priority : %s",
							listenerPriority));
				}

				RestBasedJenkinsCIConnector connector = RestBasedJenkinsCIConnector.getInstance();
				String[] globalRoles = jenkinsDefaultGlobalRoles.split(",");
				if (globalRoles == null) {
					globalRoles = new String[] {};
				}

				@SuppressWarnings("UnusedAssignment")
				int jenkinsListenerPriority = -1;
				try {
					jenkinsListenerPriority = Integer
							.parseInt(listenerPriority);
				} catch (NumberFormatException nef) {
					throw new IllegalArgumentException(
							"Invalid priority specified for jenkins "
									+ "application event listener. Please "
									+ "provide a number", nef);
				}

				JenkinsCISystemDriver jenkinsCISystemDriver = new JenkinsCISystemDriver(
						connector, globalRoles);
				ServiceContainer
						.setJenkinsCISystemDriver(jenkinsCISystemDriver);
				BundleContext bundleContext = context.getBundleContext();
				// Note: register the service only if its enabled in the
				// appfactory
				// configuration file.
				bundleContext.registerService(
						ContinuousIntegrationSystemDriver.class.getName(),
						jenkinsCISystemDriver, null);

				// Registering the Jenkins application event listener.
								
				bundleContext.registerService(ApplicationEventsHandler.class
						.getName(), new JenkinsApplicationEventsListener("JenkinsApplicationEventsListener",
						jenkinsListenerPriority), null);
				bundleContext.registerService(
						TenantUserEventListner.class.getName(),
						new JenkinsTenantUserEventListner(), null);

				Dictionary<String, Object> propsBuild = new Hashtable<String, Object>();
				propsBuild.put(AppFactoryConstants.STORAGE_TYPE, AppFactoryConstants.BUILDABLE_STORAGE_TYPE);
				JenkinsStorage jenkinsStorage = new JenkinsStorage(connector);
				
				 try{
					 bundleContext.registerService(Storage.class.getName(), jenkinsStorage, propsBuild);
			        } catch (Exception e1) {
			        	log.error("Appfactory jenkins Storage register problem ," + e1.getMessage());
			        }
				
				Dictionary<String, Object> propsNonBuild = new Hashtable<String, Object>();
				propsNonBuild.put(AppFactoryConstants.STORAGE_TYPE, AppFactoryConstants.NONBUILDABLE_STORAGE_TYPE);
		
		        try{
					bundleContext.registerService(Storage.class.getName(), new NonBuildableStorage(),propsNonBuild);
		        } catch (Exception e1) {
		        	log.error("Appfactory Non-Build Storage register problem ," + e1.getMessage());
		        }

				bundleContext.registerService(
						TenantBuildManagerInitializer.class.getName(),
						new TenantBuildManagerInitializerImpl(), null);

				ClassLoader loader = getClass().getClassLoader();
				SetBucketStrategy(loader);
				SetJenkinsClusterStrategy(loader);
			} else {
				log.info("Jenkins is not enabled");
			}

		} catch (Throwable e) {
			log.error("Error in registering Jenkins build service ", e);
		}
	}

	/**
	 * Set jenkins cluster selecting strategy
	 *
	 * @param loader Class loader
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private void SetJenkinsClusterStrategy(ClassLoader loader) {
		String clusterClzName = ServiceContainer
				.getAppFactoryConfiguration()
				.getFirstProperty(JenkinsCIConstants.JENKINS_LB_CLUSTER_SELECTING_STRATEGY);
		ClusterSelectingStrategy clusterSelectingStrategy = null;
		try {
			Class<?> clusterClzz = Class.forName(clusterClzName, true, loader);
			clusterSelectingStrategy = (ClusterSelectingStrategy) clusterClzz.newInstance();
			ServiceContainer.setClusterSelectingStrategy(clusterSelectingStrategy);
		} catch (ClassNotFoundException e) {
			log.error("Class: " + clusterClzName + " not found to set ClusterSelectingStrategy!", e);
		} catch (InstantiationException e) {
			log.error("Error occurred while initializing the Class: " + clusterClzName + " as ClusterSelectingStrategy!", e);
		} catch (IllegalAccessException e) {
			log.error("Error occurred while initializing the Class: " + clusterClzName + " as ClusterSelectingStrategy!", e);
		}
	}

	/**
	 * Set bucket selecting strategy
	 *
	 * @param loader Class loader
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private void SetBucketStrategy(ClassLoader loader) {
		String bucketClzName = ServiceContainer
				.getAppFactoryConfiguration()
				.getFirstProperty(JenkinsCIConstants.JENKINS_LB_BUCKET_SELECTING_STRATEGY);
		try {
			Class<?> bucketClzz = Class.forName(bucketClzName, true, loader);
			BucketSelectingStrategy bucketSelectingStrategy = (BucketSelectingStrategy) bucketClzz.newInstance();
			ServiceContainer.setBucketSelectingStrategy(bucketSelectingStrategy);
		} catch (ClassNotFoundException e) {
			log.error("Class: " + bucketClzName + " not found to set BucketSelectingStrategy!", e);
		} catch (InstantiationException e) {
			log.error("Error occurred while initializing the Class: " + bucketClzName + " as BucketSelectingStrategy!", e);
		} catch (IllegalAccessException e) {
			log.error("Error occurred while initializing the Class: " + bucketClzName + " as BucketSelectingStrategy!", e);
		}
	}

	/**
	 * Checks whether Jenkins is enabled
	 * 
	 * @return true if jenkins is enabled, else return false
	 */
	private boolean isJenkinsEnabled() {

		String[] definedCIDriverNames = ServiceContainer
				.getAppFactoryConfiguration()
				.getProperties(
						JenkinsCIConstants.CONTINUOUS_INTEGRATION_PROVIDER_CONFIG_SELECTOR);
		boolean defined = false;
		for (String driverName : definedCIDriverNames) {
			if ("jenkins".equalsIgnoreCase(driverName)) {
				defined = true;
				break;
			}
		}
		return defined;
	}

	@SuppressWarnings("UnusedDeclaration")
	protected void deactivate(ComponentContext ctxt) {
		if (log.isDebugEnabled()) {
			log.debug("Jenkins build service bundle is deactivated");
		}
	}
}
