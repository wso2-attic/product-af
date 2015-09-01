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

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.axiom.util.base64.Base64Utils;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.repository.mgt.service.RepositoryAuthenticationServiceStub;

import java.io.FileInputStream;
import java.rmi.RemoteException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayList;
import java.util.List;

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

			setAuthHeaders(serviceStub._getServiceClient(), userName);
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

    private void setAuthHeaders(ServiceClient serviceClient, String username) throws AppFactoryException {
        // Set authorization header to service client
        List headerList = new ArrayList();
        Header header = new Header();
        header.setName(HTTPConstants.HEADER_AUTHORIZATION);
        header.setValue(getSignedAuthHeader(username));
        headerList.add(header);
        serviceClient.getOptions().setProperty(HTTPConstants.HTTP_HEADERS, headerList);
    }

    private String getSignedAuthHeader(String username) throws AppFactoryException {

        try {
            //Get the default primary certificate of the keystore
            String keystoreName = gitBlitConfiguration.getProperty(GitBlitConstants.APPFACTORY_KEY_STORE_LOCATION,
                    GitBlitConstants.APPFACTORY_KEY_STORE_DEFAULT_LOCATION);
            String keystoreCredential = gitBlitConfiguration.getProperty(GitBlitConstants.APPFACTORY_KEY_STORE_PASSWORD,
                    GitBlitConstants.APPFACTORY_KEY_STORE_DEFAULT_PASSWORD);
            KeyStore keyStore = KeyStore.getInstance("jks");
            keyStore.load(new FileInputStream(keystoreName), keystoreCredential.toCharArray());
            PrivateKey key = (PrivateKey)keyStore.getKey(keystoreCredential, keystoreCredential.toCharArray());
            JWSSigner signer = new RSASSASigner((RSAPrivateKey) key);
            JWTClaimsSet claimsSet = new JWTClaimsSet();
            claimsSet.setClaim(AppFactoryConstants.SIGNED_JWT_AUTH_USERNAME, username);
            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS512), claimsSet);
            signedJWT.sign(signer);

            // generate authorization header value
            return "Bearer " + Base64Utils.encode(signedJWT.serialize().getBytes());
        } catch (Exception e) {
            String msg = "Failed to get primary default certificate";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
    }
}
