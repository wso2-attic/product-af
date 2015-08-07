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

package org.wso2.carbon.appfactory.utilities.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.core.ApplicationEventsHandler;
import org.wso2.carbon.appfactory.core.ArtifactStorage;
import org.wso2.carbon.appfactory.core.RemoteRegistryService;
import org.wso2.carbon.appfactory.utilities.dataservice.DSApplicationListener;
import org.wso2.carbon.appfactory.utilities.esb.ESBApplicationListener;
import org.wso2.carbon.appfactory.utilities.storage.FileArtifactStorage;
import org.wso2.carbon.appfactory.utilities.version.AppVersionStrategyExecutor;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;


/**
 * @scr.component name="org.wso2.carbon.appfactory.utilities" immediate="true"
 * @scr.reference name="appfactory.configuration"
 *              interface="org.wso2.carbon.appfactory.common.AppFactoryConfiguration"
 *              cardinality="1..1" policy="dynamic"
 *              bind="setAppFactoryConfiguration"
 *              unbind="unsetAppFactoryConfiguration"
 * @scr.reference name="registry.service"
 *              interface="org.wso2.carbon.registry.core.service.RegistryService"
 *              cardinality="1..1" policy="dynamic"
 *              bind="setRegistryService"
 *              unbind="unsetRegistryService"
 * @scr.reference name="user.realmservice.default"
 * 				interface="org.wso2.carbon.user.core.service.RealmService"
 * 				cardinality="1..1" policy="dynamic"
 * 				bind="setRealmService"
 * 				unbind="unsetRealmService"
 * @scr.reference name="config.context.service"
 *              interface="org.wso2.carbon.utils.ConfigurationContextService"
 *              cardinality="1..1" policy="dynamic"
 *              bind="setConfigurationContextService"
 *              unbind="unsetConfigurationContextService"
 * @scr.reference name="appfactory.registry.service"
 *              interface="org.wso2.carbon.appfactory.core.RemoteRegistryService"
 *              cardinality="1..1" policy="dynamic"
 *              bind="setAppfactoryRemoteRegistryService"
 *              unbind="unsetAppfactoryRemoteRegistryService"
 */
public class UtilitiesServiceComponent {
    Log log = LogFactory.getLog(org.wso2.carbon.appfactory.utilities.internal.UtilitiesServiceComponent.class);

    protected void activate(ComponentContext context) {

        if (log.isDebugEnabled()) {
            log.debug("File artifact storage bundle is activated. ");
        }
        try {
            BundleContext bundleContext = context.getBundleContext();

            // TODO Read from appfactory.xml and then register the correct ones
            FileArtifactStorage fileArtifactStorage = new FileArtifactStorage();
            bundleContext.registerService(ArtifactStorage.class.getName(), fileArtifactStorage, null);

            AppVersionStrategyExecutor versionExecutor = new AppVersionStrategyExecutor();
            bundleContext.registerService(AppVersionStrategyExecutor.class.getName(), versionExecutor, null);

            AppFactoryConfiguration appFactoryConfiguration = ServiceReferenceHolder.getInstance().getAppFactoryConfiguration();
            int listenerPriority = Integer.valueOf(appFactoryConfiguration.getFirstProperty("EventHandlers.DSApplicationHandler.priority"));
            bundleContext.registerService(ApplicationEventsHandler.class.getName(),
                                          new DSApplicationListener("DSApplicationListener", listenerPriority), null);
	        int esbListenerPriority = Integer.valueOf(appFactoryConfiguration.getFirstProperty("EventHandlers.ESBApplicationHandler.priority"));
	        bundleContext.registerService(ApplicationEventsHandler.class.getName(),
	                                      new ESBApplicationListener("ESBApplicationListener", esbListenerPriority), null);


        } catch (Throwable e) {
            log.error("Error in registering artifact storage ", e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("File artifact storage bundle is deactivated.");
        }
    }
    
    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the ConfigurationContext");
        }
        ServiceReferenceHolder.getInstance().setConfigContextService(contextService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting the ConfigurationContext");
        }
    }


    protected void unsetAppFactoryConfiguration(AppFactoryConfiguration appFactoryConfiguration) {
        ServiceReferenceHolder.getInstance().setAppFactoryConfiguration(null);
    }

    protected void setAppFactoryConfiguration(AppFactoryConfiguration appFactoryConfiguration) {
        ServiceReferenceHolder.getInstance().setAppFactoryConfiguration(appFactoryConfiguration);
    }

    protected void setRegistryService(RegistryService registryService) {

        if (registryService != null && log.isDebugEnabled()) {
            log.debug("Registry service initialized");
        }
        ServiceReferenceHolder.getInstance().setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        ServiceReferenceHolder.getInstance().setRegistryService(null);
    }

    protected void setRealmService(RealmService realmService) {
        ServiceReferenceHolder.getInstance().setRealmService(realmService);
	    if(log.isDebugEnabled()) {
		    log.debug("set up RealmService for org.wso2.carbon.appfactoutilities.internalion");
	    }
    }

    protected void unsetRealmService(RealmService realmService){
        ServiceReferenceHolder.getInstance().setRealmService(null);
        if(log.isDebugEnabled()) {
	        log.debug("un set RealmService for org.wso2.carbon.appfactoutilities.internalion");
        }
    }

	protected void setAppfactoryRemoteRegistryService(RemoteRegistryService appfactoryRemoteRegistryService){
		ServiceReferenceHolder.getInstance().setAppfactoryRemoteRegistryService(appfactoryRemoteRegistryService);
		if(log.isDebugEnabled()){
			log.debug("Setup appfactory remote registry service initialization");
		}
	}

	protected void unsetAppfactoryRemoteRegistryService(RemoteRegistryService appfactoryRemoteRegistryService){
		ServiceReferenceHolder.getInstance().setAppfactoryRemoteRegistryService(null);
		if(log.isDebugEnabled()){
			log.debug("unset appfactory remote registry service");
		}
	}

}
