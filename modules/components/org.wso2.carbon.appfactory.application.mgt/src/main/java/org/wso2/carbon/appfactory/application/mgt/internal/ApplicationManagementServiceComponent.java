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

package org.wso2.carbon.appfactory.application.mgt.internal;

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.appfactory.application.mgt.listners.ApplicationInfomationChangeListner;
import org.wso2.carbon.appfactory.application.mgt.listners.EnvironmentAuthorizationListener;
import org.wso2.carbon.appfactory.application.mgt.listners.InitialArtifactDeployerHandler;
import org.wso2.carbon.appfactory.application.mgt.listners.SingleTenantApplicationEventListner;
import org.wso2.carbon.appfactory.application.mgt.listners.StatPublishEventsListener;
import org.wso2.carbon.appfactory.application.mgt.service.ApplicationInfoService;
import org.wso2.carbon.appfactory.application.mgt.service.ApplicationManagementService;
import org.wso2.carbon.appfactory.application.mgt.service.ApplicationUserManagementService;
import org.wso2.carbon.appfactory.application.mgt.util.Util;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.core.ApplicationEventsHandler;
import org.wso2.carbon.appfactory.core.ContinuousIntegrationSystemDriver;
import org.wso2.carbon.appfactory.jenkins.build.service.TenantContinousIntegrationSystemDriverService;
import org.wso2.carbon.appfactory.nonbuild.NonBuildableApplicationEventListner;
import org.wso2.carbon.appfactory.tenant.mgt.service.TenantManagementService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="org.wso2.carbon.appfactory.user.registration" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService"
 * unbind="unsetRegistryService"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"
 * bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 * @scr.reference name="appfactory.configuration"
 * interface="org.wso2.carbon.appfactory.common.AppFactoryConfiguration"
 * cardinality="1..1" policy="dynamic"
 * bind="setAppFactoryConfiguration"
 * unbind="unsetAppFactoryConfiguration"
 * @scr.reference name="appfactory.continuous.integration"
 * interface="org.wso2.carbon.appfactory.core.ContinuousIntegrationSystemDriver"
 * cardinality="0..1" policy="dynamic"
 * bind="setContinuousIntegrationSystemDriver"
 * unbind="unsetContinuousIntegrationSystemDriver"
 * @scr.reference name="appfactory.application.events.listener"
 * interface="org.wso2.carbon.appfactory.core.ApplicationEventsHandler"
 * cardinality="0..n" policy="dynamic"
 * bind="setApplicationEventsListener"
 * unbind="unsetApplicationEventsListener"
 * @scr.reference name="appfactory.tenant.mgt.service"
 * interface="org.wso2.carbon.appfactory.tenant.mgt.service.TenantManagementService"
 * cardinality="0..1" policy="dynamic"
 * bind="setTenantManagementService"
 * unbind="unsetTenantManagementService"
 * @scr.reference name="appfactory.tenant.continuous.integration"
 * interface="org.wso2.carbon.appfactory.jenkins.build.service.TenantContinousIntegrationSystemDriverService"
 * cardinality="0..1" policy="dynamic"
 * bind="setTenantContinousIntegrationSystemDriverService"
 * unbind="unsetTenantContinousIntegrationSystemDriverService"
 */
public class ApplicationManagementServiceComponent {
    private static Log log = LogFactory.getLog(ApplicationManagementServiceComponent.class);
    private static ConfigurationContextService configContextService = null;

	protected void activate(ComponentContext context){

		BundleContext bundleContext = context.getBundleContext();
		bundleContext.registerService(ApplicationUserManagementService.class.getName(),
		                              new ApplicationUserManagementService(), null);
		AppFactoryConfiguration appFactoryConfiguration = Util.getConfiguration();

		int priority = -1;
		if (Boolean.parseBoolean(appFactoryConfiguration.getFirstProperty("BAM.EnableStatPublishing"))) {
			try {
				priority =
				           Integer.parseInt(appFactoryConfiguration.getFirstProperty("BAM.Property.ListenerPriority"));
				bundleContext.registerService(ApplicationEventsHandler.class.getName(),
				                              new StatPublishEventsListener(
				                                                            "StatPublishEventsListener",
				                                                            priority), null);
			} catch (NumberFormatException nfe) {
				log.error("Invalid priority provided for StatPublishEventsListener", nfe);
			}
		}

		try {
			priority =
			           Integer.parseInt(appFactoryConfiguration.getFirstProperty("EventHandlers.EnvironmentAuthorizationHandler.priority"));
			bundleContext.registerService(ApplicationEventsHandler.class.getName(),
			                              new EnvironmentAuthorizationListener(
			                                                                   "EnvironmentAuthorizationListener",
			                                                                   priority), null);
		} catch (NumberFormatException nfe) {
			log.error("Invalid priority provided for EnvironmentAuthorizationListener", nfe);
		}

		Dictionary<String, Object> propsNonBuild = new Hashtable<String, Object>();
		propsNonBuild.put(AppFactoryConstants.STORAGE_TYPE,
		                  AppFactoryConstants.BUILDABLE_STORAGE_TYPE);

		bundleContext.registerService(ApplicationInfoService.class.getName(),
		                              new ApplicationInfoService(), null);

		try {
			priority =
			           Integer.parseInt(appFactoryConfiguration.getFirstProperty("EventHandlers.NonBuildableApplicationEventListner.priority"));
			bundleContext.registerService(ApplicationEventsHandler.class.getName(),
			                              new NonBuildableApplicationEventListner(
			                                                                      "NonBuildableApplicationEventListner",
			                                                                      priority),
			                              propsNonBuild);
		} catch (NumberFormatException nfe) {
			log.error("Invalid priority provided for NonBuildableApplicationEventListner", nfe);
		}

		try {
			priority =
			           Integer.parseInt(appFactoryConfiguration.getFirstProperty("EventHandlers.ApplicationInfomationChangeListner.priority"));
			bundleContext.registerService(ApplicationEventsHandler.class.getName(),
			                              new ApplicationInfomationChangeListner(
			                                                                     "ApplicationInfomationChangeListner",
			                                                                     priority), null);
		} catch (NumberFormatException nfe) {
			log.error("Invalid priority provided for ApplicationInfomationChangeListner", nfe);
		}

		try {
			priority =
					Integer.parseInt(appFactoryConfiguration.getFirstProperty("EventHandlers.InitialArtifactDeployerHandler.priority"));

			bundleContext.registerService(ApplicationEventsHandler.class.getName(),
			                              new InitialArtifactDeployerHandler(
					                              "InitialArtifactDeployerHandler", 45), null);
		} catch (NumberFormatException nfe) {
			log.error("Invalid priority provided for InitialArtifactDeployerHandler", nfe);
		}

		try {
			priority =
					Integer.parseInt(appFactoryConfiguration.getFirstProperty("EventHandlers.SingleTenantApplicationEventListner.priority"));

			bundleContext.registerService(ApplicationEventsHandler.class.getName(),
			                              new SingleTenantApplicationEventListner(
					                              "InitialArtifactDeployerHandler", priority), null);
		} catch (NumberFormatException nfe) {
			log.error("Invalid priority provided for SingleTenantApplicationEventListner", nfe);
		}

		if (log.isDebugEnabled()) {
			log.debug("Application Management Service  bundle is activated ");
		}
	}

    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Application Management Service  bundle is deactivated. ");
        }
    }

    protected void setRegistryService(RegistryService registryService) {
        Util.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        Util.setRegistryService(null);
    }

    protected void setRealmService(RealmService realmService) {

        Util.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        Util.setRealmService(null);
    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the ConfigurationContext");
        }
        configContextService = contextService;
        ServiceReferenceHolder.getInstance().setConfigContextService(contextService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting the ConfigurationContext");
        }
    }

    protected void setAppFactoryConfiguration(AppFactoryConfiguration configuration) {
        Util.setConfiguration(configuration);
    }

    protected void unsetAppFactoryConfiguration(AppFactoryConfiguration configuration) {
        Util.setConfiguration(null);
    }

    public static void setContinuousIntegrationSystemDriver(ContinuousIntegrationSystemDriver continuousIntegrationSystemDriver) {
        Util.setContinuousIntegrationSystemDriver(continuousIntegrationSystemDriver);
    }

    public static void unsetContinuousIntegrationSystemDriver(ContinuousIntegrationSystemDriver continuousIntegrationSystemDriver) {
        Util.setContinuousIntegrationSystemDriver(null);
    }

    public static void setApplicationEventsListener(ApplicationEventsHandler applicationEventsListener) {
        Util.addApplicationEventsListener(applicationEventsListener);
    }

    public static void unsetApplicationEventsListener(ApplicationEventsHandler applicationEventsListener) {
        Util.removeApplicationEventsListener(applicationEventsListener);
    }
    
    public static void setTenantManagementService(TenantManagementService tenantManagementService) {
        Util.setTenantManagementService(tenantManagementService);
    }

    public static void unsetTenantManagementService(TenantManagementService tenantManagementService) {
        Util.setTenantManagementService(null);
    }
    
    public static void setTenantContinousIntegrationSystemDriverService(TenantContinousIntegrationSystemDriverService tenantContinousIntegrationSystemDriverService) {
		Util.setTenantContinousIntegrationSystemDriverService(tenantContinousIntegrationSystemDriverService);
	}

	public static void unsetTenantContinousIntegrationSystemDriverService(TenantContinousIntegrationSystemDriverService tenantContinousIntegrationSystemDriverService) {
		Util.setContinuousIntegrationSystemDriver(null);
	}
}
