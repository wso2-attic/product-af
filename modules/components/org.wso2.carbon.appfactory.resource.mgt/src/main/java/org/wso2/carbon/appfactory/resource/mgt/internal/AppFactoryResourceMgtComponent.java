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

package org.wso2.carbon.appfactory.resource.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name=
 *                "org.wso2.carbon.appfactory.resource.mgt.internal.AppFactoryResourceMgtComponent"
 *                immediate="true"
 * @scr.reference name="appfactory.configuration"
 *                interface="org.wso2.carbon.appfactory.common.AppFactoryConfiguration"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setAppFactoryConfiguration"
 *                unbind="unsetAppFactoryConfiguration"
 * @scr.reference name="registry.service"
 *                interface="org.wso2.carbon.registry.core.service.RegistryService"
 *                cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="user.realmservice.default"
 *                interface="org.wso2.carbon.user.core.service.RealmService"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setRealmService"
 *                unbind="unsetRealmService"
 */
public class AppFactoryResourceMgtComponent {

	private static final Log log = LogFactory.getLog(AppFactoryResourceMgtComponent.class);

	@SuppressWarnings("UnusedDeclaration")
	protected void activate(ComponentContext context) {
		if (log.isDebugEnabled()) {
			log.debug("appfactory.eventing service bundle is activated");
		}
	}

	@SuppressWarnings("UnusedDeclaration")
	protected void deactivate(ComponentContext ctxt) {
		if (log.isDebugEnabled()) {
			log.debug("appfactory.eventing service bundle is deactivated");
		}
	}

	protected void setRealmService(RealmService realmService) {
		ServiceHolder.getInstance().setRealmService(realmService);
	}

	protected void unsetRealmService(RealmService realmService) {
		ServiceHolder.getInstance().setRealmService(null);
	}

	protected void setAppFactoryConfiguration(AppFactoryConfiguration configuration) {
		ServiceHolder.getInstance().setAppFactoryConfiguration(configuration);
	}

	protected void unsetAppFactoryConfiguration(AppFactoryConfiguration configuration) {
		ServiceHolder.getInstance().setAppFactoryConfiguration(null);
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
}
