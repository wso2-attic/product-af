/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appfactory.git;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.repository.mgt.service.RepositoryAuthenticationServiceStub;

import java.rmi.RemoteException;

/**
 * Service client for repository authentication service
 */
public class AppFactoryRepositoryAuthorizationClient {
	private static final Logger log =
	                                  LoggerFactory.getLogger(ApplicationManagementServiceClient.class);
	private RepositoryAuthenticationServiceStub serviceStub;
	private GitBlitConfiguration gitBlitConfiguration;
	private ContextHolder holder;

	/**
	 * Constructor taking Gitblit configuration
	 * 
	 * @param configuration
	 */
	public AppFactoryRepositoryAuthorizationClient(GitBlitConfiguration configuration) {
		gitBlitConfiguration = configuration;
		holder = ContextHolder.getHolder(gitBlitConfiguration);

		try {
			ConfigurationContext context = holder.getConfigurationContext();
			serviceStub =
			              new RepositoryAuthenticationServiceStub(
			                                                      context,
			                                                      configuration.getProperty(GitBlitConstants.APPFACTORY_URL,
			                                                                                GitBlitConstants.APPFACTORY_DEFAULT_URL) +
			                                                              "/services/RepositoryAuthenticationService");
		} catch (AxisFault fault) {
			log.error("Error occurred while initializing client ", fault);
		}
	}

	/**
	 * @param userName
	 * @param repositoryName
	 * @return
	 */
	public boolean authorize(String userName, String repositoryName, String repositoryAction,
	                         String fullRepoName) {
		boolean isAuth = false;
		try {
			log.info("User: " + userName + " is authorizing for " + repositoryName);
            
            String repoDomain = fullRepoName.split("/")[0];
            String tenantDomain = userName.substring(userName.lastIndexOf("@")+1);
            if((!repoDomain.equals(tenantDomain)) & (!repoDomain.equals(("~") + tenantDomain))){
                return false ;
            }

			AppFactoryUtil.setAuthHeaders(serviceStub._getServiceClient(), userName);
			if (serviceStub.hasAccess(userName, repositoryName, repositoryAction, fullRepoName)) {
				log.info("User: " + userName + " is authorized for " + repositoryName);
				isAuth = true;
			}

		} catch (AxisFault e) {
			log.error("Error while calling RepositoryAuthenticationService: Error is " +
			                  e.getLocalizedMessage(), e);
		} catch (RemoteException e) {
			log.error("Error while calling RepositoryAuthenticationService: Error is " +
			                  e.getLocalizedMessage(), e);
		} catch (AppFactoryException e) {
			log.error("Error while calling RepositoryAuthenticationService: Error is " +
			          e.getLocalizedMessage(), e);
		} finally {
			try {
				serviceStub._getServiceClient().cleanupTransport();
				serviceStub._getServiceClient().cleanup();
				serviceStub.cleanup();
			} catch (AxisFault fault) {
				// ignore
			}
		}
		return isAuth;
	}

	public void setCookie(String cookie) {
		Options options = serviceStub._getServiceClient().getOptions();
		options.setManageSession(true);
		options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
		options.setTimeOutInMilliSeconds(10000);
	}
}
