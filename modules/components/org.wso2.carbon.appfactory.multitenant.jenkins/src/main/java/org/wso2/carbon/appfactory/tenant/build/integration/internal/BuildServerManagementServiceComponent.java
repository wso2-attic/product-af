/*
 * Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.appfactory.tenant.build.integration.internal;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.appfactory.tenant.build.integration.utils.JenkinsConfig;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name=
 *                "org.wso2.carbon.appfactory.tenant.build.integration.internal.BuildServerManagementServiceComponent"
 *                immediate="true"
 * @scr.reference name="user.realmservice.default"
 *                interface="org.wso2.carbon.user.core.service.RealmService"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setRealmService"
 *                unbind="unsetRealmService"
 */
public class BuildServerManagementServiceComponent {

	private static final Log log = LogFactory.getLog(BuildServerManagementServiceComponent.class);

	protected void activate(ComponentContext context) {
		try {
			// initializes Jenkins configurations.
			JenkinsConfig.getInstance();
		} catch (IOException e) {
			log.fatal("Error while activating the bundle - multi-tenant jenkins.", e);
		}
	}

	protected void setRealmService(RealmService realmService) {
		ServiceContainer.getInstance().setRealmService(realmService);
		log.debug("set up RealmService for org.wso2.carbon.appfactory.tenant.build.integration");
	}

	protected void unsetRealmService(RealmService realmService) {
		ServiceContainer.getInstance().setRealmService(null);
		log.debug("un set RealmService for org.wso2.carbon.appfactory.tenant.build.integration");
	}

	protected void deactivate(ComponentContext context) {
		if (log.isDebugEnabled()) {
			log.debug("Appfactory common bundle is deactivated");
		}
	}

}