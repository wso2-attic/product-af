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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name=
 *                "org.wso2.carbon.appfactory.apiManager.integration.internal.AppFactoryIntegrationServiceComponent"
 *                immediate="true"
 * @scr.reference name="appfactory.configuration"
 *                interface="org.wso2.carbon.appfactory.common.AppFactoryConfiguration"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setAppFactoryConfiguration"
 *                unbind="unsetAppFactoryConfiguration"
 *@scr.reference name="registry.service"
 *                interface="org.wso2.carbon.registry.core.service.RegistryService"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setRegistryService"
 *                unbind="unsetRegistryService"
 * @scr.reference name="user.realmservice.default"
 *                interface="org.wso2.carbon.user.core.service.RealmService"
 *                cardinality="1..1"
 *                policy="dynamic"
 *                bind="setRealmService"
 *                unbind="unsetRealmService"
 * @scr.reference name="registry.loader.default"
 *                interface="org.wso2.carbon.registry.core.service.TenantRegistryLoader"
 *                cardinality="1..1"
 *                policy="dynamic"
 *                bind="setRegistryLoader"
 *                unbind="unsetRegistryLoader"
 */

public class AppFactoryIntegrationServiceComponent {

	private static final Log log = LogFactory
			.getLog(AppFactoryIntegrationServiceComponent.class);
    private static BundleContext bundleContext;
    protected void activate(ComponentContext context) {
        AppFactoryIntegrationServiceComponent. bundleContext = context.getBundleContext();

        try {
            if (log.isDebugEnabled()) {
                log.debug("Appfactory integration bundle is activated");
            }
        } catch (Throwable e) {
            log.error("Error in creating appfactory configuration", e);
        }

    }

	protected void deactivate(ComponentContext context) {
		if (log.isDebugEnabled()) {
			log.debug("Appfactory common bundle is deactivated");
		}
	}

    protected void setAppFactoryConfiguration(AppFactoryConfiguration appFactoryConfiguration) {
        ServiceHolder.getInstance().setAppFactoryConfiguration(appFactoryConfiguration);
    }

    protected void unsetAppFactoryConfiguration(AppFactoryConfiguration appFactoryConfiguration) {
        ServiceHolder.getInstance().setAppFactoryConfiguration(null);
    }

    public static BundleContext getBundleContext() {
        return bundleContext;
    }

    public static void setBundleContext(BundleContext bundleContext) {
        AppFactoryIntegrationServiceComponent.bundleContext = bundleContext;
    }
    protected void setRegistryService(RegistryService registryService) {
        if (registryService != null && log.isDebugEnabled()) {
            log.debug("Registry service initialized");
        }
        ServiceHolder.getInstance().setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        ServiceHolder.getInstance().setRegistryService(null);
    }
    protected void setRealmService(RealmService realmService) {
        ServiceHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        ServiceHolder.getInstance().setRealmService(null);
    }
    protected void setRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        ServiceHolder.getInstance().setTenantRegistryLoader(tenantRegistryLoader);
    }

    protected void unsetRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        ServiceHolder.getInstance().setTenantRegistryLoader(null);
    }
}
