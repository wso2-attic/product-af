/*
 * Copyright 2005-2013 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.appfactory.repository.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.embed.Embedder;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.core.ApplicationEventsHandler;
import org.wso2.carbon.appfactory.core.TenantRepositoryManagerInitializer;
import org.wso2.carbon.appfactory.repository.mgt.*;
import org.wso2.carbon.appfactory.repository.mgt.client.AppfactoryRepositoryClient;
import org.wso2.carbon.appfactory.repository.mgt.client.DefaultAgent;
import org.wso2.carbon.appfactory.repository.mgt.client.DefaultRepositoryClient;
import org.wso2.carbon.appfactory.repository.mgt.client.SCMAgent;
import org.wso2.carbon.appfactory.repository.mgt.git.GITBranchingStrategy;
import org.wso2.carbon.appfactory.repository.mgt.git.GitAgent;
import org.wso2.carbon.appfactory.repository.mgt.git.GitRepositoryClient;
import org.wso2.carbon.appfactory.repository.mgt.git.JGitAgent;
import org.wso2.carbon.appfactory.repository.mgt.listeners.RepositoryHandler;
import org.wso2.carbon.appfactory.repository.mgt.service.RepositoryAuthenticationService;
import org.wso2.carbon.appfactory.repository.mgt.service.RepositoryManagementService;
import org.wso2.carbon.appfactory.repository.mgt.svn.SVNBranchingStrategy;
import org.wso2.carbon.appfactory.tenant.mgt.service.TenantManagementService;
import org.wso2.carbon.appfactory.utilities.version.AppVersionStrategyExecutor;
import org.wso2.carbon.user.core.service.RealmService;

import java.lang.reflect.Constructor;

/**
 * @scr.component name="org.wso2.carbon.appfactory.repository.mgt" immediate="true"
 * @scr.reference name="appfactory.configuration" interface=
 *                "org.wso2.carbon.appfactory.common.AppFactoryConfiguration"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setAppFactoryConfiguration"
 *                unbind="unsetAppFactoryConfiguration"
 * @scr.reference name="user.realmservice.default"
 *                interface="org.wso2.carbon.user.core.service.RealmService"
 *                cardinality="1..1" policy="dynamic" bind="setRealmService"
 *                unbind="unsetRealmService"
 * @scr.reference name="appversion.executor"
 *                interface="org.wso2.carbon.appfactory.utilities.version.AppVersionStrategyExecutor"
 *                cardinality="1..1" policy="dynamic" bind="setAppVersionStrategyExecutor"
 *                unbind="unsetAppVersionStrategyExecutor"
 * @scr.reference name="appfactory.tenant.mgt.service"
 *                interface="org.wso2.carbon.appfactory.tenant.mgt.service.TenantManagementService"
 *                cardinality="0..1" policy="dynamic"
 *                bind="setTenantManagementService"
 *                unbind="unsetTenantManagementService"
 */
public class RepositoryMgtServiceComponent {
	Log log = LogFactory.getLog(RepositoryMgtServiceComponent.class);
	public String repositoryTypes[] = { "svn", "git" };
	private Embedder plexus;

	protected void unsetAppFactoryConfiguration(AppFactoryConfiguration appFactoryConfiguration) {
		Util.setConfiguration(null);
	}

	protected void setAppFactoryConfiguration(AppFactoryConfiguration appFactoryConfiguration) {
		Util.setConfiguration(appFactoryConfiguration);
	}

	protected void setRealmService(RealmService realmService) {

		Util.setRealmService(realmService);
	}

	protected void unsetRealmService(RealmService realmService) {
		Util.setRealmService(null);
	}

	protected void setAppVersionStrategyExecutor(AppVersionStrategyExecutor versionExecutor) {
		Util.setVersionStrategyExecutor(versionExecutor);
	}

	protected void unsetAppVersionStrategyExecutor(AppVersionStrategyExecutor versionExecutor) {
		Util.setVersionStrategyExecutor(null);
	}
	
	public static void setTenantManagementService(TenantManagementService tenantManagementService) {
        Util.setTenantManagementService(tenantManagementService);
    }

    public static void unsetTenantManagementService(TenantManagementService tenantManagementService) {
        Util.setTenantManagementService(null);
    }
	    

	protected void activate(ComponentContext context) {

		if (log.isDebugEnabled()) {
			log.debug("Repository mgt bundle is activated.");
		}
		try {
			BundleContext bundleContext = context.getBundleContext();
			AppFactoryConfiguration configuration = Util.getConfiguration();
			Embedder plexus = new Embedder();
			try {
				plexus.start();
			} catch (PlexusContainerException e) {
				String msg = "Could not able to start Plexus";
				log.error(msg, e);
				throw new RepositoryMgtException(msg, e);
			}
			for (String repoType : this.repositoryTypes) {
				//
				StringBuilder classNameKey = new StringBuilder(AppFactoryConstants.REPOSITORY_PROVIDER_CONFIG);
				classNameKey.append(".").append(repoType).append(".").append("Property").append(".").append("Class");
				String className = configuration.getFirstProperty(classNameKey.toString());
				if (className != null) {
					Class<RepositoryProvider> provider =
					                                     (Class<RepositoryProvider>) this.getClass().getClassLoader()
					                                                                     .loadClass(className);
					Constructor constructor = provider.getConstructor();
					RepositoryProvider providerObject = (RepositoryProvider) constructor.newInstance();
					providerObject.setConfiguration(configuration);
					BranchingStrategy branchingStrategy = null;
					AppfactoryRepositoryClient client;
                    DefaultAgent defaultAgent = new SCMAgent(repoType , plexus);
                    client = new DefaultRepositoryClient(defaultAgent);
					if ("svn".equals(repoType)) {
						branchingStrategy = new SVNBranchingStrategy();
					} else if ("git".equals(repoType)) {
						branchingStrategy = new GITBranchingStrategy();
                        GitAgent gitAgent = new JGitAgent();
                        client = new GitRepositoryClient(gitAgent);
					}
					if (branchingStrategy != null) {
						branchingStrategy.setRepositoryProvider(providerObject);
					} else {
						log.error("No Branching Strategy found for" + repoType);
					}

					providerObject.setAppfactoryRepositoryClient(client);
					providerObject.setBranchingStrategy(branchingStrategy);
					log.info("Adding Provider for " + repoType);
					Util.setRepositoryProvider(repoType, providerObject);
				} else {
					log.error("repository provider is not found for " + repoType);
				}
			}
			RepositoryAuthenticationService authenticationService = new RepositoryAuthenticationService();
			RepositoryManagementService managmentService = new RepositoryManagementService();
			RepositoryManager repositoryManager = new RepositoryManager();
			bundleContext.registerService(RepositoryManagementService.class.getName(), managmentService, null);
			bundleContext.registerService(RepositoryAuthenticationService.class.getName(), authenticationService, null);
			bundleContext.registerService(RepositoryManager.class.getName(), repositoryManager, null);
			bundleContext.registerService(TenantRepositoryManagerInitializer.class.getName(),
			                              new TenantRepositoryManagerInitializerImpl(), null);
			
			AppFactoryConfiguration appFactoryConfiguration = Util.getConfiguration();
            int listenerPriority = Integer.parseInt(appFactoryConfiguration.getFirstProperty("EventHandlers.RepositoryHandler.priority"));
            
			bundleContext.registerService(ApplicationEventsHandler.class.getName(), new RepositoryHandler("RepositoryHandler", listenerPriority),
			                              null);

		} catch (Throwable e) {
			log.error("Error in registering Repository Management Service  ", e);
		}
	}

	protected void deactivate(ComponentContext ctxt) {
		if (plexus != null) {
			try {
				plexus.stop();
			} catch (Exception ignore) {
				// According to docs it can be ignored
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("*SVN repository mgt bundle is deactivated. ");
		}
	}

}

