/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.appfactory.ext.internal;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisObserver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.ext.appserver.LeaderElector;
import org.wso2.carbon.appfactory.ext.appserver.dbs.deployment.listener.ServiceDeploymentListener;
import org.wso2.carbon.appfactory.ext.listener.AppFactoryAuthorizationManagerListener;
import org.wso2.carbon.appfactory.ext.listener.AppFactoryClaimManagerListener;
import org.wso2.carbon.appfactory.ext.listener.AppFactoryUserOperationEventListener;
import org.wso2.carbon.user.core.listener.AuthorizationManagerListener;
import org.wso2.carbon.user.core.listener.ClaimManagerListener;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.PreAxisConfigurationPopulationObserver;
import org.wso2.carbon.registry.core.service.RegistryService;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * @scr.component name="org.wso2.carbon.appfactory.ext.internal"
 * immediate="true"
 * @scr.reference name="config.context.service" interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="0..1" policy="dynamic"  bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic"
 * bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="appfactory.configuration"
 * interface="org.wso2.carbon.appfactory.common.AppFactoryConfiguration"
 * cardinality="1..1" policy="dynamic"
 * bind="setAppFactoryConfiguration"
 * unbind="unsetAppFactoryConfiguration"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic"
 * bind="setRegistryService"
 * unbind="unsetRegistryService"
 */
public class AppFactoryExtDS {
    private static final Log log = LogFactory.getLog(AppFactoryExtDS.class);
    private LeaderElector leaderElector;

    @SuppressWarnings("UnusedDeclaration")
    protected void activate(ComponentContext context) {

        context.getBundleContext().registerService(UserOperationEventListener.class.getName(),
                new AppFactoryUserOperationEventListener(), null);

        context.getBundleContext().registerService(AuthorizationManagerListener.class.getName(),
                new AppFactoryAuthorizationManagerListener(), null);

        context.getBundleContext().registerService(ClaimManagerListener.class.getName(),
                new AppFactoryClaimManagerListener(), null);

	    Dictionary props = new Hashtable();
	    props.put(CarbonConstants.AXIS2_CONFIG_SERVICE, AxisObserver.class.getName());
	    context.getBundleContext().registerService(AxisObserver.class.getName(), new ServiceDeploymentListener(), props);



	    PreAxisConfigurationPopulationObserver preAxisConfigObserver =
			    new PreAxisConfigurationPopulationObserver() {
				    public void createdAxisConfiguration(AxisConfiguration axisConfiguration) {
					    axisConfiguration.addObservers(new ServiceDeploymentListener());
				    }
			    };
	    context.getBundleContext().registerService(PreAxisConfigurationPopulationObserver.class.getName(),
	                              preAxisConfigObserver, null);

        leaderElector = LeaderElector.getInstance();
        if (log.isDebugEnabled()) {
            log.debug("appfactory.ext service bundle is activated");
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    protected void deactivate(ComponentContext ctxt) {
        leaderElector.terminate();
        if (log.isDebugEnabled()) {
            log.debug("appfactory.ext service bundle is deactivated");
        }
    }

    protected void setConfigurationContextService(ConfigurationContextService configContextService) {
        ServiceHolder.getInstance().setConfigContextService(configContextService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configContextService) {
        ServiceHolder.getInstance().setConfigContextService(null);
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
