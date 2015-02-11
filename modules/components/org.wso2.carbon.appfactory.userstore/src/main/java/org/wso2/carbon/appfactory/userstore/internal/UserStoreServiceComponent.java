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

package org.wso2.carbon.appfactory.userstore.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.registry.core.service.RegistryService;


/**
 * @scr.component name="org.wso2.carbon.appfactory.userstore" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 */
public class UserStoreServiceComponent {
    Log log = LogFactory.getLog(UserStoreServiceComponent.class);

    protected void activate(ComponentContext context) {

        if (log.isDebugEnabled()) {
            log.debug("Userstore bundle is activated. ");
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("File artifact storage bundle is deactivated.");
        }
    }

//    protected void unsetAppFactoryConfiguration(AppFactoryConfiguration appFactoryConfiguration) {
//        ServiceReferenceHolder.getInstance().setAppFactoryConfiguration(null);
//    }
//
//    protected void setAppFactoryConfiguration(AppFactoryConfiguration appFactoryConfiguration) {
//        ServiceReferenceHolder.getInstance().setAppFactoryConfiguration(appFactoryConfiguration);
//    }

    protected void setRegistryService(RegistryService registryService) {

        if (registryService != null && log.isDebugEnabled()) {
            log.debug("Registry service initialized");
        }
        ServiceReferenceHolder.getInstance().setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        ServiceReferenceHolder.getInstance().setRegistryService(null);
    }
}
