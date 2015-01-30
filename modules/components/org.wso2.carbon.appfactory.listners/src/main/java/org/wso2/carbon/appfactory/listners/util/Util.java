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

package org.wso2.carbon.appfactory.listners.util;

import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

public class Util {
	private static RealmService realmService;
	private static AppFactoryConfiguration configuration;
	private static RegistryService registryService;
    private static ConfigurationContextService configurationContextService;

    public static RealmService getRealmService() {
		return realmService;
	}

	public static synchronized void setRealmService(RealmService realmSer) {
		realmService = realmSer;
	}

	public static AppFactoryConfiguration getConfiguration() {
		return configuration;
	}

	public static void setConfiguration(AppFactoryConfiguration configuration) {
		Util.configuration = configuration;
	}

	public static RegistryService getRegistryService() {
		return registryService;
	}

	public static void setRegistryService(RegistryService registryService) {
		Util.registryService = registryService;
	}

    public static void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        Util.configurationContextService = configurationContextService;
    }

    public static ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }
}
