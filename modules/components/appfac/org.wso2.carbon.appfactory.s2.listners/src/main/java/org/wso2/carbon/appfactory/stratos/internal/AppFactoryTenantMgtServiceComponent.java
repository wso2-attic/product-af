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

package org.wso2.carbon.appfactory.stratos.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.core.TenantCloudInitializer;
import org.wso2.carbon.appfactory.stratos.S2TenantCloudInitializer;
import org.wso2.carbon.appfactory.stratos.listeners.AppFactoryTenantKeyStoreMgtListener;
import org.wso2.carbon.appfactory.stratos.listeners.CloudEnvironmentPermissionListener;
import org.wso2.carbon.appfactory.stratos.listeners.S2IntegrationTenantActivationListener;
import org.wso2.carbon.appfactory.stratos.util.AppFactoryTenantMgtUtil;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name=
 *                "org.wso2.carbon.appfactory.tenant.roles"
 *                immediate="true"
 * @scr.reference name="user.realmservice.default"
 *                interface="org.wso2.carbon.user.core.service.RealmService"
 *                cardinality="1..1" policy="dynamic" bind="setRealmService"
 *                unbind="unsetRealmService"
 * @scr.reference name="appfactory.common"
 *                interface=
 *                "org.wso2.carbon.appfactory.common.AppFactoryConfiguration"
 *                cardinality="1..1"
 *                policy="dynamic" bind="setAppFactoryConfiguration"
 *                unbind="unsetAppFactoryConfiguration"
 * @scr.reference name="registry.service"
 *                interface=
 *                "org.wso2.carbon.registry.core.service.RegistryService"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setRegistryService"
 *                unbind="unsetRegistryService"
 * @scr.reference name="configuration.context.service"
 *                interface="org.wso2.carbon.utils.ConfigurationContextService"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setConfigurationContextService"
 *                unbind="unsetConfigurationContextService"
 * @scr.reference name="registry.loader.default"
 *                interface="org.wso2.carbon.registry.core.service.TenantRegistryLoader"
 *                cardinality="1..1"
 *                policy="dynamic"
 *                bind="setRegistryLoader"
 *                unbind="unsetRegistryLoader"
 */
public class AppFactoryTenantMgtServiceComponent {
    private static Log log = LogFactory.getLog(AppFactoryTenantMgtServiceComponent.class);

    protected void activate(ComponentContext context) {

        try {
            if(System.getProperty("stratos.stage")!=null){
                //this is SC register only  TenantMgtListener
        	context.getBundleContext().registerService(TenantMgtListener.class.getName(),
        	                               			new CloudEnvironmentPermissionListener(),null);
        	
        	context.getBundleContext().registerService(TenantMgtListener.class.getName(),
        			new AppFactoryTenantKeyStoreMgtListener(),null);
        	
            }else {
                //this is AF register only TenantCloudInitializer
            context.getBundleContext().registerService(TenantCloudInitializer.class.getName(),
                    new S2TenantCloudInitializer(),null);
            }

            if (log.isDebugEnabled()) {
                log.debug("DefaultRolesCreatorServiceComponent Service  bundle is activated");
            }
        } catch (Exception e) {
            log.error("DefaultRolesCreatorServiceComponent activation failed.", e);
        }
    }

    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("DefaultRolesCreatorServiceComponent Service  bundle is deactivated ");
        }
    }

    protected void setRealmService(RealmService realmService) {
        AppFactoryTenantMgtUtil.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        AppFactoryTenantMgtUtil.setRealmService(null);
    }

    protected void setAppFactoryConfiguration(AppFactoryConfiguration appFactoryConfiguration) {
        AppFactoryTenantMgtUtil.setConfiguration(appFactoryConfiguration);
    }

    protected void unsetAppFactoryConfiguration(AppFactoryConfiguration appFactoryConfiguration) {
        AppFactoryTenantMgtUtil.setConfiguration(null);
    }

    protected void setRegistryService(RegistryService registryService) {
        if (registryService != null && log.isDebugEnabled()) {
            log.debug("Registry service initialized");
        }
        AppFactoryTenantMgtUtil.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        AppFactoryTenantMgtUtil.setRegistryService(null);
    }
    protected void setConfigurationContextService(ConfigurationContextService configurationContextService){
        AppFactoryTenantMgtUtil.setConfigurationContextService(configurationContextService);
    }
    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService){
        AppFactoryTenantMgtUtil.setConfigurationContextService(null);
    }
    
    protected void setRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
    	AppFactoryTenantMgtUtil.setTenantRegistryLoader(tenantRegistryLoader);
    }

    protected void unsetRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
    	AppFactoryTenantMgtUtil.setTenantRegistryLoader(null);
    }
}