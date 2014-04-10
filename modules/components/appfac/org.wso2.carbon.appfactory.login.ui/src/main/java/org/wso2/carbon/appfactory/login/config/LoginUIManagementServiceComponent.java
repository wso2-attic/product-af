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

package org.wso2.carbon.appfactory.login.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="org.wso2.carbon.appfactory.login"
 * immediate="true"
 * @scr.reference name="appfactory.configuration" interface=
 * "org.wso2.carbon.appfactory.common.AppFactoryConfiguration"
 * cardinality="1..1" policy="dynamic"
 * bind="setAppFactoryConfiguration"
 * unbind="unsetAppFactoryConfiguration"
 */
public class LoginUIManagementServiceComponent {
    private static Log log = LogFactory.getLog(LoginUIManagementServiceComponent.class);
    private static ConfigurationContextService configContextService = null;

    protected void activate(ComponentContext context) {

        BundleContext bundleContext = context.getBundleContext();
        LoginUIManagementServiceComponent mgtService = new LoginUIManagementServiceComponent();
        bundleContext.registerService(LoginUIManagementServiceComponent.class.getName(), mgtService, null);

        if (log.isDebugEnabled()) {
            log.debug("Login UI bundle is activated ");
        }
    }

    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Login UI bundle is deactivated.");
        }
    }

    protected void unsetAppFactoryConfiguration(AppFactoryConfiguration appFactoryConfiguration) {
        ServiceReferenceHolder.getInstance().setAppFactoryConfiguration(null);
    }

    protected void setAppFactoryConfiguration(AppFactoryConfiguration appFactoryConfiguration) {
        ServiceReferenceHolder.getInstance().setAppFactoryConfiguration(appFactoryConfiguration);
    }
}