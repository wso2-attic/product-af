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
import org.apache.axis2.context.ConfigurationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.appfactory.git.util.Util;
import org.wso2.carbon.user.mgt.stub.ListUsersUserAdminExceptionException;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.wso2.carbon.utils.CarbonUtils;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Service client for UserAdmin service
 */
public class UserAdminServiceClient {
	private static final Logger log = LoggerFactory.getLogger(UserAdminServiceClient.class);
	private UserAdminStub client;
	private GitBlitConfiguration gitBlitConfiguration;
	private ContextHolder holder;

	/**
	 * Constructor initializing the client with configurations from
	 * gitblit.properties or with
	 * usual default values
	 * 
	 * @param configuration
	 */
	public UserAdminServiceClient(GitBlitConfiguration configuration) {
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
			client =
			         new UserAdminStub(
			                           context,
			                           configuration.getProperty(GitBlitConstants.APPFACTORY_URL,
			                                                     GitBlitConstants.APPFACTORY_DEFAULT_URL) +
			                                   "/services/UserAdmin");
			CarbonUtils.setBasicAccessSecurityHeaders(username, password,
			                                          client._getServiceClient());
			Util.setMaxTotalConnection(client._getServiceClient());
		} catch (AxisFault fault) {
			log.error("Error while calling UserAdminService:Error is " +
			                  fault.getLocalizedMessage(), fault);
		}
	}

	/**
	 * Get all the users of appfactory
	 * 
	 * @return list of usernames
	 */
	public List<String> getAllUsers() {
		String users[];
		try {
			users = client.listUsers("*");
			if (users != null) {
				return Arrays.asList(users);
			}
		} catch (AxisFault fault) {
			log.error("Error while calling UserAdminService:Error is " +
			                  fault.getLocalizedMessage(), fault);
		} catch (RemoteException e) {
			log.error("Error while calling UserAdminService:Error is " + e.getLocalizedMessage(), e);
		} catch (ListUsersUserAdminExceptionException e) {
			log.error("Error while calling UserAdminService:Error is " + e.getLocalizedMessage(), e);
		} finally {
			try {
				client._getServiceClient().cleanupTransport();
				client._getServiceClient().cleanup();
				client.cleanup();
			} catch (AxisFault fault) {
				// ignore
			}
		}
		return Collections.emptyList();
	}
}
