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
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.appfactory.application.mgt.service.ApplicationManagementServiceApplicationManagementExceptionException;
import org.wso2.carbon.appfactory.application.mgt.service.ApplicationManagementServiceStub;
import org.wso2.carbon.appfactory.git.util.Util;
import org.wso2.carbon.utils.CarbonUtils;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Service client for calling Application Management service
 */
public class ApplicationManagementServiceClient {
	private static final Logger log =
	                                  LoggerFactory.getLogger(ApplicationManagementServiceClient.class);
	private ApplicationManagementServiceStub serviceStub;
	private GitBlitConfiguration gitBlitConfiguration;
	private ContextHolder holder;

	/**
	 * Constructor initializing the client with configurations from
	 * gitblit.properties or with
	 * usual default values
	 * 
	 * @param configuration
	 */
	public ApplicationManagementServiceClient(GitBlitConfiguration configuration) {
		gitBlitConfiguration = configuration;
		holder = ContextHolder.getHolder(gitBlitConfiguration);
		String username =
		                  configuration.getProperty(GitBlitConstants.APPFACTORY_ADMIN_USERNAME,
		                                            GitBlitConstants.APPFACTORY_ADMIN_USERNAME_DEFAULT_VALUE);
		String password =
		                  configuration.getProperty(GitBlitConstants.APPFACTORY_ADMIN_PASSWORD,
		                                            GitBlitConstants.APPFACTORY_ADMIN_PASSWORD_DEFAULT_VALUE);

		try {
			ConfigurationContext context = holder.getConfigurationContext();
			serviceStub =
			              new ApplicationManagementServiceStub(
			                                                   context,
			                                                   configuration.getProperty(GitBlitConstants.APPFACTORY_URL,
			                                                                             GitBlitConstants.APPFACTORY_DEFAULT_URL) +
			                                                           "/services/ApplicationManagementService");
			CarbonUtils.setBasicAccessSecurityHeaders(username, password,
			                                          serviceStub._getServiceClient());
			serviceStub._getServiceClient().getOptions()
			           .setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
			Util.setMaxTotalConnection(serviceStub._getServiceClient());

		} catch (AxisFault e) {
			log.error("Error while calling ApplicationManagementService:Error is " +
			                  e.getLocalizedMessage(), e);
		}
	}

	/**
	 * Get all the applications of a user
	 * 
	 * @param userName
	 * @return List of application key
	 */
	public List<String> getAllApplicationsOfUser(String domainName, String userName) {
		String[] apps;
		apps = holder.getCache().get(userName);
		if (apps != null) {
			return Arrays.asList(apps);
		} else {
			try {
				apps = serviceStub.getAllApplications(domainName, userName);
				if (apps != null) {
					holder.getCache().put(userName, apps);
					return Arrays.asList(apps);
				}
			} catch (AxisFault e) {
				log.error("Error while calling ApplicationManagementService:Error is " +
				                  e.getLocalizedMessage(), e);
			} catch (RemoteException e) {
				log.error("Error while calling ApplicationManagementService:Error is " +
				                  e.getLocalizedMessage(), e);
			} catch (ApplicationManagementServiceApplicationManagementExceptionException e) {
				log.error("Error while calling ApplicationManagementService:Error is " +
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
		}
		return Collections.emptyList();
	}

	/**
	 * Return list of users of an application
	 * 
	 * @param appFactoryApplicationName
	 * @return list of username
	 */
	public List<String> getUsersOfApplication(String appFactoryApplicationName) {
		String users[];
		try {
			users = serviceStub.getUsersOfApplication(appFactoryApplicationName);
			if (users != null) {
				return Arrays.asList(users);
			}
		} catch (RemoteException e) {
			log.error("Error while calling ApplicationManagementService:Error is " +
			                  e.getLocalizedMessage(), e);
		} catch (ApplicationManagementServiceApplicationManagementExceptionException e) {
			log.error("Error while calling ApplicationManagementService:Error is " +
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
		return Collections.emptyList();
	}
}
