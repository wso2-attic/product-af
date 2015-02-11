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

import java.util.HashMap;
import java.util.Map;

import com.gitblit.Constants;
import com.gitblit.models.RepositoryModel;
import com.gitblit.models.UserModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.appfactory.gitblit.oauth.oauth2.AppFactoryGitBlitAuthenticationProvider;

/**
 * This is custom user model to implement custom repository authorization
 */
public class AppFactoryGitBlitUserModel extends UserModel {

	private static final Logger log = LoggerFactory.getLogger(AppFactoryGitBlitUserModel.class);

	private transient AppFactoryRepositoryAuthorizationClient appFactoryRepositoryAuthorizationClient;
	private transient GitBlitConfiguration configuration;

	// GitBlit Permission cache
	private Map<String, Boolean> accessPermissionMap = new HashMap<String, Boolean>();

	public String getAdminCookie() {
		return cookie;
	}

	public void setAdminCookie(String adminCookie) {
		this.cookie = adminCookie;
	}

	public GitBlitConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(GitBlitConfiguration configuration) {
		this.configuration = configuration;
	}

	public AppFactoryGitBlitUserModel(String username) {
		super(username);

	}

	public AppFactoryGitBlitUserModel(String username,
	                                  GitBlitConfiguration config,
	                                  AppFactoryRepositoryAuthorizationClient appFactoryRepositoryAuthorizationClient) {
		this(username);
		this.appFactoryRepositoryAuthorizationClient = appFactoryRepositoryAuthorizationClient;
		this.configuration = config;

	}

	/**
	 * This method is called when git want to authorize user for each and every
	 * action.
	 * 
	 * ex: When user want to do create branch then git checks several action
	 * permission(RW,RWC,RWD,RW+) in few calls from AppFactory.
	 * 
	 */
	@Override
	protected boolean canAccess(RepositoryModel repository,
	                            Constants.AccessRestrictionType ifRestriction,
	                            Constants.AccessPermission requirePermission) {

		boolean canAccess = true;
		// For non admin gitblit users
		if (!username.equals(configuration.getProperty(GitBlitConstants.APPFACTORY_GITBLIT_ADMIN_USERNAME,
		                                               GitBlitConstants.APPFACTORY_GITBLIT_ADMIN_USERNAME_DEFAULT_VALUE))) {
			if (getAdminCookie() != null && !getAdminCookie().equals("")) {
				appFactoryRepositoryAuthorizationClient.setCookie(getAdminCookie());
			}
			
			//log.debug("Check permission to code : "+requirePermission.code );
			//if (accessPermissionMap.containsKey(requirePermission.code)) {
				// Read permission from the cache
				//canAccess = accessPermissionMap.get(requirePermission.code);
			//} else {

				String appName =
				                 AppFactoryGitBlitAuthenticationProvider.getAppFactoryApplicationName(repository.name);
				canAccess =
				            appFactoryRepositoryAuthorizationClient.authorize(getName(),
				                                                              appName,
				                                                              requirePermission.code,
				                                                              repository.name);
				// Put permission in to the cache
				//accessPermissionMap.put(requirePermission.code, canAccess);
			//}
		}
		log.debug("Accessibility  to code : "+requirePermission.code + " is " + canAccess);
		return canAccess;
	}

	public AppFactoryRepositoryAuthorizationClient getAppFactoryRepositoryAuthorizationClient() {
		return new AppFactoryRepositoryAuthorizationClient(getConfiguration());
	}

	public void setAppFactoryRepositoryAuthorizationClient(AppFactoryRepositoryAuthorizationClient appFactoryRepositoryAuthorizationClient) {
		this.appFactoryRepositoryAuthorizationClient = appFactoryRepositoryAuthorizationClient;
	}

}
