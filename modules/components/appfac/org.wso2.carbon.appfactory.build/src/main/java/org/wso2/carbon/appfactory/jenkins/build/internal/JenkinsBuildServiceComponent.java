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

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.core.ApplicationEventsHandler;
import org.wso2.carbon.appfactory.core.ContinuousIntegrationSystemDriver;
import org.wso2.carbon.appfactory.core.Storage;
import org.wso2.carbon.appfactory.core.TenantBuildManagerInitializer;
import org.wso2.carbon.appfactory.core.TenantUserEventListner;
import org.wso2.carbon.appfactory.jenkins.build.*;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryManager;
import org.wso2.carbon.appfactory.jenkins.build.JenkinsTenantUserEventListner;
import org.wso2.carbon.appfactory.nonbuild.NonBuildableStorage;
import org.wso2.carbon.appfactory.utilities.application.ApplicationTypeManager;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.user.core.service.RealmService;

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
 * @scr.reference name="app.type.manager" interface=
 *                "org.wso2.carbon.appfactory.utilities.application.ApplicationTypeManager"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setApplicationTypeManager"
 *                unbind="unsetApplicationTypeManager"
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

	protected void setApplicationTypeManager(ApplicationTypeManager manager) {
		ServiceContainer.setApplicationTypeManager(manager);
	}

	protected void unsetApplicationTypeManager(ApplicationTypeManager manager) {
		ServiceContainer.setApplicationTypeManager(null);
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

				String authenticate = ServiceContainer
						.getAppFactoryConfiguration()
						.getFirstProperty(
								JenkinsCIConstants.AUTHENTICATE_CONFIG_SELECTOR);

				String userName = ServiceContainer
						.getAppFactoryConfiguration()
						.getFirstProperty(
								JenkinsCIConstants.JENKINS_SERVER_ADMIN_USERNAME);

				String password = ServiceContainer
						.getAppFactoryConfiguration()
						.getFirstProperty(
								JenkinsCIConstants.JENKINS_SERVER_ADMIN_PASSWORD);

				String jenkinsUrl = ServiceContainer
						.getAppFactoryConfiguration().getFirstProperty(
								JenkinsCIConstants.BASE_URL_CONFIG_SELECTOR);

				String jenkinsDefaultGlobalRoles = ServiceContainer
						.getAppFactoryConfiguration()
						.getFirstProperty(
								JenkinsCIConstants.DEFAULT_GLOBAL_ROLES_CONFIG_SELECTOR);

				String listenerPriority = ServiceContainer
						.getAppFactoryConfiguration()
						.getFirstProperty(
								JenkinsCIConstants.LISTENER_PRIORITY_CONFIG_SELECTOR);
				
				
				if (log.isDebugEnabled()) {
					log.debug(String.format("Authenticate : %s", authenticate));
					log.debug(String.format("Jenkins user name : %s", userName));
					log.debug(String.format("Jenkins api key : %s", password));
					log.debug(String.format("Jenkins url : %s", jenkinsUrl));
					log.debug(String.format("Default Global Roles : %s",
							jenkinsDefaultGlobalRoles));
					log.debug(String.format("Listener Priority : %s",
							listenerPriority));
				}

				RestBasedJenkinsCIConnector connector = new RestBasedJenkinsCIConnector(
						jenkinsUrl, Boolean.parseBoolean(authenticate),
						userName, password);
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
						.getName(), new JenkinsApplicationEventsListener(
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
			        	log.debug("Appfactory jenkins Storage register problem ," + e1.getMessage());
			        }
				
				Dictionary<String, Object> propsNonBuild = new Hashtable<String, Object>();
				propsNonBuild.put(AppFactoryConstants.STORAGE_TYPE, AppFactoryConstants.NONBUILDABLE_STORAGE_TYPE);
		
		        try{
					bundleContext.registerService(Storage.class.getName(), new NonBuildableStorage(),propsNonBuild);
		        } catch (Exception e1) {
		        	log.debug("Appfactory Non-Build Storage register problem ," + e1.getMessage());
		        }
				
				bundleContext.registerService(
						TenantBuildManagerInitializer.class.getName(),
						new TenantBuildManagerInitializerImpl(), null);
			} else {
				log.info("Jenkins is not enabled");
			}

		} catch (Throwable e) {
			log.error("Error in registering Jenkins build service ", e);
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
