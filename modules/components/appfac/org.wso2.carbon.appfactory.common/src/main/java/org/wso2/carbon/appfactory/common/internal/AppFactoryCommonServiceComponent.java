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

package org.wso2.carbon.appfactory.common.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.securevault.SecretCallbackHandlerService;

/**
 * @scr.component name="appfactory.common" immediate="true"
 * @scr.reference name="secret.callback.handler.service"
 * interface="org.wso2.carbon.securevault.SecretCallbackHandlerService"
 * cardinality="1..1" policy="dynamic"
 * bind="setSecretCallbackHandlerService" unbind="unsetSecretCallbackHandlerService"
 */
public class AppFactoryCommonServiceComponent {

    private static final Log log = LogFactory.getLog(AppFactoryCommonServiceComponent.class);
    private static SecretCallbackHandlerService secretCallbackHandlerService;

    protected void activate(ComponentContext context) {
        BundleContext bundleContext = context.getBundleContext();
        AppFactoryConfiguration configuration;
        try {
            configuration = AppFactoryUtil.getAppfactoryConfiguration();
            bundleContext.registerService(AppFactoryConfiguration.class.getName(), configuration, null);
            if (log.isDebugEnabled()) {
                log.debug("Appfactory common bundle is activated");
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

    public static SecretCallbackHandlerService getSecretCallbackHandlerService() {
          return AppFactoryCommonServiceComponent.secretCallbackHandlerService;
      }

      protected void setSecretCallbackHandlerService(
              SecretCallbackHandlerService secretCallbackHandlerService) {
          if (log.isDebugEnabled()) {
              log.debug("SecretCallbackHandlerService acquired");
          }
          AppFactoryCommonServiceComponent.secretCallbackHandlerService = secretCallbackHandlerService;

      }
       protected void unsetSecretCallbackHandlerService(
            SecretCallbackHandlerService secretCallbackHandlerService) {
        AppFactoryCommonServiceComponent.secretCallbackHandlerService = null;
    }

}
