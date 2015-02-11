/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *     WSO2 Inc. licenses this file to you under the Apache License,
 *     Version 2.0 (the "License"); you may not use this file except
 *     in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing,
 *    software distributed under the License is distributed on an
 *    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *    KIND, either express or implied.  See the License for the
 *    specific language governing permissions and limitations
 *    under the License.
 */

package org.wso2.carbon.appfactory.gitblit.oauth.oauth2;

import com.gitblit.Constants;
import com.gitblit.IUserService;
import com.gitblit.auth.AuthenticationProvider;
import com.gitblit.manager.IRuntimeManager;
import com.gitblit.models.TeamModel;
import com.gitblit.models.UserModel;
import org.wso2.carbon.appfactory.git.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class AppFactoryGitBlitAuthenticationProvider
                                                    extends
                                                    AuthenticationProvider.UsernamePasswordAuthenticationProvider
                                                                                                                 implements
                                                                                                                 IUserService {

	private static GitBlitConfiguration configuration;

	/**
	 * key:username value:GitBlitUserModelHolder
	 */
	private static Map<String, GitBlitUserModelHolder> userModelMap =
	                                                                  new HashMap<String, GitBlitUserModelHolder>();

	public AppFactoryAuthenticationClient getAppFactoryAuthenticationClient() {
		return new AppFactoryAuthenticationClient(this.getConfiguration());
	}

	public GitBlitConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(GitBlitConfiguration configuration) {
		this.configuration = configuration;
	}

	public AppFactoryRepositoryAuthorizationClient getRepositoryAuthorizationClient() {
		return new AppFactoryRepositoryAuthorizationClient(this.getConfiguration());
	}

	public UserAdminServiceClient getUserAdminServiceClient() {
		return new UserAdminServiceClient(this.getConfiguration());
	}

	public AppFactoryGitBlitAuthenticationProvider() {
		super("appfactory");
	}

	@Override
	public void setup() {
		configuration = new GitBlitConfiguration(settings);
		try {
			System.setProperty("javax.net.ssl.trustStore",
			                   configuration.getProperty(GitBlitConstants.APPFACTORY_TRUST_STORE_LOCATION,
			                                             new File(".").getCanonicalPath() +
			                                                     GitBlitConstants.APPFACTORY_TRUST_STORE_DEFAULT_LOCATION));
			System.setProperty("javax.net.ssl.keyStore",
			                   configuration.getProperty(GitBlitConstants.APPFACTORY_KEY_STORE_LOCATION,
			                                             new File(".").getCanonicalPath() +
			                                                     GitBlitConstants.APPFACTORY_KEY_STORE_DEFAULT_LOCATION));
		} catch (IOException e) {
			logger.error("Could not find any trust store for communicate with app factory");
		}
		// set system property truststore password
		System.setProperty("javax.net.ssl.trustStorePassword",
		                   configuration.getProperty(GitBlitConstants.APPFACTORY_TRUST_STORE_PASSWORD,
		                                             GitBlitConstants.APPFACTORY_TRUST_STORE_DEFAULT_PASSWORD));
		System.setProperty("javax.net.ssl.keyStorePassword",
		                   configuration.getProperty(GitBlitConstants.APPFACTORY_KEY_STORE_PASSWORD,
		                                             GitBlitConstants.APPFACTORY_KEY_STORE_DEFAULT_PASSWORD));
		logger.info("***********App Factory User Service is  initialized ************");

	}

	@Override
	public UserModel authenticate(String username, char[] password) {
		AppFactoryAuthenticationClient client = getAppFactoryAuthenticationClient();
		String cookie = null;
		String passwordStr = new String(password);
		//
		if (settings.getBoolean(GitBlitConstants.APPFACTORY_OAUTH_ENABLE, false) &&
		    passwordStr.equals(GitBlitConstants.APPFACTORY_OAUTH_PASSWORD)) {
			OAuthAuthenticator oauthAuthenticationService = new OAuthAuthenticator(settings);
			UserModel userModel = oauthAuthenticationService.authenticate(username);
			if (userModel != null) {
				userModelMap.put(userModel.getName(), new GitBlitUserModelHolder(userModel));
				return userModel;
			}
		}
		if (authenticateAdmin(username, password) ||
		    (cookie = client.authenticate(username, passwordStr)) != null) {
			UserModel userModel =
			                      new AppFactoryGitBlitUserModel(username, getConfiguration(),
			                                                     getRepositoryAuthorizationClient());
			if (username.equals(settings.getString(GitBlitConstants.APPFACTORY_GITBLIT_ADMIN_USERNAME,
			                                       GitBlitConstants.APPFACTORY_GITBLIT_ADMIN_USERNAME_DEFAULT_VALUE))) {
				userModel.canAdmin = true;
				userModel.password = passwordStr;
			} else {
				userModel.canAdmin = false;
				userModel.cookie = cookie;
				userModel.password = passwordStr;
			}
			// userModel.cookie = StringUtils.getSHA1(userModel.username + new
			// String(password));
			userModelMap.put(username, new GitBlitUserModelHolder(userModel));
			GitBlitUserModelHolder gitBlitUserModelHolder = new GitBlitUserModelHolder(userModel);
			// if(this.userManager instanceof
			// AppFactoryGitBlitAuthenticationProvider){
			// AppFactoryGitBlitUserService userService =
			// (AppFactoryGitBlitUserService)this.userManager;
			// userService.setUserModel(username, gitBlitUserModelHolder);
			// }
			return userModel;
		}
		return null;
	}

	private boolean authenticateAdmin(String username, char[] password) {
		String adminUserName =
		                       settings.getString(GitBlitConstants.APPFACTORY_GITBLIT_ADMIN_USERNAME,
		                                          GitBlitConstants.APPFACTORY_GITBLIT_ADMIN_USERNAME_DEFAULT_VALUE);
		String adminPassword =
		                       settings.getString(GitBlitConstants.APPFACTORY_GITBLIT_ADMIN_PASSWORD,
		                                          GitBlitConstants.APPFACTORY_GITBLIT_ADMIN_PASSWORD_DEFAULT_VALUE);
		if (adminUserName.equals(username)) {
			if (Arrays.equals(adminPassword.toCharArray(), password)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Constants.AccountType getAccountType() {
		return Constants.AccountType.HTPASSWD;
	}

	@Override
	public boolean supportsCredentialChanges() {
		return false;
	}

	@Override
	public boolean supportsDisplayNameChanges() {
		return true;
	}

	@Override
	public boolean supportsEmailAddressChanges() {
		return true;
	}

	@Override
	public boolean supportsTeamMembershipChanges() {
		return true;
	}

	/**
	 * Method to get app factory application name from gitblit repository name
	 * 
	 * @param repositoryName
	 * @return
	 */
	public static String getAppFactoryApplicationName(String repositoryName) {
		String domainAwareAppId = repositoryName.substring(0, repositoryName.lastIndexOf(".git"));
		// google.com/app1
		String applicationName = domainAwareAppId.substring(domainAwareAppId.lastIndexOf("/") + 1);
		return applicationName;
	}

	@Override
	public void setup(IRuntimeManager iRuntimeManager) {

	}

	@Override
	public String getCookie(UserModel model) {
		return null;
	}

	@Override
	public UserModel getUserModel(char[] chars) {
		String username = new String(chars);
		GitBlitUserModelHolder userModelHolder = userModelMap.get(username);
		if (userModelHolder != null) {
			return userModelHolder.getUserModel();
		}
		return null;
	}

	@Override
	public UserModel getUserModel(String username) {

		GitBlitUserModelHolder userModelHolder = userModelMap.get(username);
		if (userModelHolder != null) {
			return userModelHolder.getUserModel();
		}
		return null;
	}

	@Override
	public boolean updateUserModel(UserModel model) {
		return false;
	}

	@Override
	public boolean updateUserModels(Collection<UserModel> models) {
		return false;
	}

	@Override
	public boolean updateUserModel(String username, UserModel model) {
		return false;
	}

	@Override
	public boolean deleteUserModel(UserModel model) {
		return false;
	}

	@Override
	public boolean deleteUser(String username) {
		return false;
	}

	@Override
	public List<String> getAllUsernames() {
		return getUserAdminServiceClient().getAllUsers();
	}

	@Override
	public List<UserModel> getAllUsers() {
		return Collections.emptyList();
	}

	@Override
	public List<String> getAllTeamNames() {
		return Collections.emptyList();
	}

	@Override
	public List<TeamModel> getAllTeams() {
		return Collections.emptyList();
	}

	@Override
	public List<String> getTeamNamesForRepositoryRole(String repositoryName) {
		List<String> appList = new ArrayList<String>();
		appList.add(repositoryName);
		return appList;
	}

	@Override
	public TeamModel getTeamModel(String teamName) {
		return new TeamModel(teamName);
	}

	@Override
	public boolean updateTeamModel(TeamModel model) {
		return false;
	}

	@Override
	public boolean updateTeamModels(Collection<TeamModel> models) {
		return false;
	}

	@Override
	public boolean updateTeamModel(String teamname, TeamModel model) {
		return false;
	}

	@Override
	public boolean deleteTeamModel(TeamModel model) {
		return false;
	}

	@Override
	public boolean deleteTeam(String teamname) {
		return false;
	}

	@Override
	public List<String> getUsernamesForRepositoryRole(String role) {
		return Collections.emptyList();
	}

	@Override
	public boolean renameRepositoryRole(String oldRole, String newRole) {
		return false;
	}

	@Override
	public boolean deleteRepositoryRole(String role) {
		return false;
	}
}
