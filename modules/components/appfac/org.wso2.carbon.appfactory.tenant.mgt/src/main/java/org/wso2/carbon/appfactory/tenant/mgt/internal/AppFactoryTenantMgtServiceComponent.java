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

package org.wso2.carbon.appfactory.tenant.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.core.ApplicationEventsHandler;
import org.wso2.carbon.appfactory.core.TenantUserEventListner;
import org.wso2.carbon.appfactory.tenant.mgt.service.TenantManagementService;
import org.wso2.carbon.appfactory.tenant.mgt.util.AppFactoryTenantMgtUtil;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="org.wso2.carbon.appfactory.tenant.roles"
 *                immediate="true"
 * @scr.reference name="user.realmservice.default"
 *                interface="org.wso2.carbon.user.core.service.RealmService"
 *                cardinality="1..1" policy="dynamic" 
 *                bind="setRealmService"
 *                unbind="unsetRealmService"
 * @scr.reference name="appfactory.common"
 *                interface="org.wso2.carbon.appfactory.common.AppFactoryConfiguration"
 *                cardinality="1..1"
 *                policy="dynamic" bind="setAppFactoryConfiguration"
 *                unbind="unsetAppFactoryConfiguration"
 * @scr.reference name="registry.service"
 *                interface="org.wso2.carbon.registry.core.service.RegistryService"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setRegistryService"
 *                unbind="unsetRegistryService"
 * @scr.reference name="configuration.context.service"
 *                interface="org.wso2.carbon.utils.ConfigurationContextService"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setConfigurationContextService"
 *                unbind="unsetConfigurationContextService"
 * @scr.reference name="appfactory.tenant.user.events.listener"
 * 				  interface="org.wso2.carbon.appfactory.core.TenantUserEventListner"
 *                cardinality="0..n" policy="dynamic"
 *                bind="setTenantUserEventListner"
 *                unbind="unsetTenantUserEventListner"
 */
public class AppFactoryTenantMgtServiceComponent {
    private static Log log = LogFactory.getLog(AppFactoryTenantMgtServiceComponent.class);

    protected void activate(ComponentContext context) {
    	context.getBundleContext().registerService(TenantManagementService.class.getName(), new TenantManagementService(), null);
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
    public static void setTenantUserEventListner(TenantUserEventListner tenantUserEventsListener) {
        AppFactoryTenantMgtUtil.addTenantUserEventListner(tenantUserEventsListener);
    }

    public static void unsetTenantUserEventListner(TenantUserEventListner tenantUserEventsListener) {
        AppFactoryTenantMgtUtil.removeTenantUserEventListner(tenantUserEventsListener);
    }
}