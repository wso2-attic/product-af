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

package org.wso2.carbon.appfactory.gitblit.oauth.oauth2;

import com.gitblit.IStoredSettings;
import com.gitblit.models.UserModel;
import org.apache.axis2.AxisFault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.appfactory.git.AppFactoryGitBlitUserModel;
import org.wso2.carbon.appfactory.git.AppFactoryRepositoryAuthorizationClient;
import org.wso2.carbon.appfactory.git.GitBlitConfiguration;
import org.wso2.carbon.appfactory.git.GitBlitConstants;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.oauth2.stub.OAuth2TokenValidationServiceStub;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO_OAuth2AccessToken;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationResponseDTO;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.rmi.RemoteException;

public class OAuthAuthenticator {

	protected static final String AUTH_HEADER_NAME = "Authorization";
	protected static final String BEARER = "Bearer";
	protected static final String ACCESS_TOKEN = "access_token";
	protected static final String BEARER_TOKEN_TYPE = "bearer";

	private OAuth2TokenValidationServiceStub stub = null;
	private static final Logger logger = LoggerFactory.getLogger(OAuthAuthenticator.class);
	private GitBlitConfiguration configuration;
	private String endPoint = "";
	private IStoredSettings settings;

	public OAuthAuthenticator(IStoredSettings settings) {
		this.settings = settings;
		configuration = new GitBlitConfiguration(settings);
		String serverUrl =
				configuration.getProperty(GitBlitConstants.APPFACTORY_IS_URL,
				                          GitBlitConstants.APPFACTORY_IS_DEFAULT_URL);
		String serviceName =
				configuration.getProperty(GitBlitConstants.APPFACTORY_IS_OAUTH_SERVICE,
				                          GitBlitConstants.APPFACTORY_IS_OAUTH_DEFAULT_SERVICE);
		String adminuser =
				configuration.getProperty(GitBlitConstants.APPFACTORY_ADMIN_USERNAME,
				                          GitBlitConstants.APPFACTORY_ADMIN_USERNAME_DEFAULT_VALUE);
		String adminpass =
				configuration.getProperty(GitBlitConstants.APPFACTORY_ADMIN_PASSWORD,
				                          GitBlitConstants.APPFACTORY_ADMIN_PASSWORD_DEFAULT_VALUE);
		this.endPoint = serverUrl + "/" + serviceName;

		try {

			stub = new OAuth2TokenValidationServiceStub(endPoint);
			CarbonUtils.setBasicAccessSecurityHeaders(adminuser, adminpass, true,
			                                          stub._getServiceClient());
		} catch (AxisFault e) {
			logger.warn("Stub initialization failed. " + e);
		}
	}

	public UserModel authenticate(String accessTokenStr) {
		OAuth2TokenValidationRequestDTO oauthReq = new OAuth2TokenValidationRequestDTO();
		OAuth2TokenValidationRequestDTO_OAuth2AccessToken accessToken = new OAuth2TokenValidationRequestDTO_OAuth2AccessToken();
		accessToken.setIdentifier(accessTokenStr);
		accessToken.setTokenType(BEARER_TOKEN_TYPE);

		oauthReq.setAccessToken(accessToken);
		OAuth2TokenValidationResponseDTO oauthResponse = null;
		try {
			oauthResponse = stub.validate(oauthReq);
		} catch (RemoteException e) {
		}
		String username = oauthResponse.getAuthorizedUser();

		// if carbon super tenant get tenant aware username
		if (MultitenantUtils.getTenantDomain(username)
		                    .equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME))
			username = MultitenantUtils.getTenantAwareUsername(username);

		UserModel userModel =
				new AppFactoryGitBlitUserModel(
						username,
						this.configuration,
						new AppFactoryRepositoryAuthorizationClient(
								this.configuration));
		return userModel;
	}

}
