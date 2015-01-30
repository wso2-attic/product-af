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
package org.wso2.carbon.appfactory.s4.integration.internal;

import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.s4.integration.DomainMapperEventHandler;
import org.wso2.carbon.appfactory.s4.integration.DomainMappingManagementService;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.Set;
import java.util.TreeSet;

/**
 * Singleton class to hold OSGI service references
 */
public class ServiceReferenceHolder {

	private AppFactoryConfiguration configuration;

	private static final ServiceReferenceHolder instance = new ServiceReferenceHolder();
	private ConfigurationContextService configurationContextService;
	private static Set<DomainMapperEventHandler<?>> domainMapperEventHandlers =
	                                                                           new TreeSet<DomainMapperEventHandler<?>>();
    private static DomainMappingManagementService domainMappingManagementService;

	private ServiceReferenceHolder() {
	}

	public static ServiceReferenceHolder getInstance() {
		return instance;
	}

	public AppFactoryConfiguration getAppFactoryConfiguration() {
		return configuration;
	}

	public void setAppFactoryConfiguration(AppFactoryConfiguration appFactoryConfiguration) {
		this.configuration = appFactoryConfiguration;
	}

	public void setConfigurationContextService(ConfigurationContextService configurationContextService) {
		this.configurationContextService = configurationContextService;
	}

	public ConfigurationContextService getConfigurationContextService() {
		return configurationContextService;
	}

	public void addDomainMapperEventHandler(DomainMapperEventHandler domainMapperEventHandler) {
		domainMapperEventHandlers.add(domainMapperEventHandler);

	}

	public void removeDomainMapperEventHandler(DomainMapperEventHandler domainMapperEventHandler) {
		domainMapperEventHandlers.remove(domainMapperEventHandler);

	}

	public Set<DomainMapperEventHandler<?>> getDomainMapperEventHandler() {
		return domainMapperEventHandlers;

	}

    public DomainMappingManagementService getDomainMappingManagementService() {
        return domainMappingManagementService;
    }

    public void setDomainMappingManagementService(
            DomainMappingManagementService domainMappingManagementService) {
        ServiceReferenceHolder.domainMappingManagementService = domainMappingManagementService;
    }
}
