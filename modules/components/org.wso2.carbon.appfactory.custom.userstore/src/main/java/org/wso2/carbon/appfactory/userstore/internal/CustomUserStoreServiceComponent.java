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
import org.wso2.carbon.appfactory.userstore.AppFactoryCustomUserStoreManager;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.user.core.UserStoreException;

/**
 * @scr.component name="org.wso2.carbon.appfactory.userstore.internal.CustomUserStoreServiceComponent" immediate="true"
 */
public class CustomUserStoreServiceComponent {
    Log log = LogFactory.getLog(CustomUserStoreServiceComponent.class);
    private static BundleContext bundleContext;

    protected void activate(ComponentContext context) {
        CustomUserStoreServiceComponent.bundleContext = context.getBundleContext();
        AppFactoryCustomUserStoreManager customUserStore = null;
        customUserStore = new AppFactoryCustomUserStoreManager();
        bundleContext.registerService(AppFactoryCustomUserStoreManager.class.getName(), customUserStore, null);


        if (log.isDebugEnabled()) {
            log.debug("Custom Userstore bundle is activated. ");
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("File artifact storage bundle is deactivated.");
        }
    }
}
