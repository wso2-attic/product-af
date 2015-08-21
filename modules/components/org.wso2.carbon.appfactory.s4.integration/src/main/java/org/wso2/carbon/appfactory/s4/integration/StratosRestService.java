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

package org.wso2.carbon.appfactory.s4.integration;

import com.google.gson.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.stratos.cli.exception.CommandException;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.ServerResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;

/**
 * Http Client based REST Client for calling Stratos APIs
 */
public class StratosRestService {

    private static final Log log = LogFactory.getLog(StratosRestService.class);

    private String stratosManagerURL;
    private String username;
    private String password;
    // REST endpoints
    private static final String INITIAL_END_POINT = "/stratos/admin/init";
    private static final String LIST_AVAILABLE_CARTRIDGES_END_POINT = "/stratos/admin/cartridge/list";
    private static final String LIST_SUBSCRIBED_CARTRIDGES_END_POINT = "/stratos/admin/cartridge/list/subscribed";
    private static final String SUBSCRIBE_CARTRIDGE_REST_END_POINT = "/stratos/admin/cartridge/subscribe";
    private static final String ADD_TENANT_END_POINT = "/stratos/admin/tenant";
    private static final String UNSUBSCRIBE_TENANT_END_POINT = "/stratos/admin/cartridge/unsubscribe";
    private static final String CARTRIDGE_DEPLOYMENT_END_POINT = "/stratos/admin/cartridge/definition";
    private static final String PARTITION_DEPLOYMENT_END_POINT = "/stratos/admin/policy/deployment/partition";
    private static final String AUTO_SCALING_POLICY_DEPLOYMENT_END_POINT = "/stratos/admin/policy/autoscale";
    private static final String DEPLOYMENT_POLICY_DEPLOYMENT_END_POINT = "/stratos/admin/policy/deployment";
    private static final String LIST_PARTITION_END_POINT = "/stratos/admin/partition";
    private static final String LIST_AUTO_SCALE_POLICY_END_POINT = "/stratos/admin/policy/autoscale";
    private static final String LIST_DEPLOYMENT_POLICY_END_POINT = "/stratos/admin/policy/deployment";
    private static final String LIST_DETAILS_OF_SUBSCRIBED_CARTRIDGE = "/stratos/admin/cartridge/info/";
    private static final String UNSUBSCRIBE_CARTRIDGE_END_POINT = "/stratos/admin/cartridge/unsubscribe";

    public StratosRestService(String stratosManagerURL, String username, String password) {
        this.username = username;
        this.stratosManagerURL = stratosManagerURL;
        this.password = password;
    }

    /**
     * @param appName
     *            Name of the application to be checked
     * @return if the application is already subscribed or not
     */
    public boolean isAlreadySubscribed(String appName)
            throws AppFactoryException {
        String alias = appName;

        HttpClient httpClient = getNewHttpClient();

        try {
            // get the details of the subscription for an app
            ServerResponse response = doGet(httpClient, this.stratosManagerURL
                                                        + this.LIST_DETAILS_OF_SUBSCRIBED_CARTRIDGE + appName);
            System.out.println("response.getStatusCode() = "
                               + response.getResponse());
            // already subscribed!
            if (response.getStatusCode() == HttpStatus.SC_OK) {

                if (log.isDebugEnabled()) {
                    log.debug("Status 200 returned when retrieving the subscription info");
                }
                return true;
                // No alias found or not subscribed yet
            } else if (response.getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
                return false;
            } else {
                if (log.isDebugEnabled()) {
                    String subscriptionListOutput = response.getResponse();
                    log.debug("Status response when retrieving the subscription info:\n"
                              + subscriptionListOutput);
                }
                return true;
            }
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.error("Error occurred while getting subscription info ", e);
            }
            return true;
        }

    }

    // This method does the cartridge subscription
    public void subscribe(String cartridgeType, String alias,
                          String externalRepoURL, boolean privateRepo, String username,
                          String password, String dataCartridgeType,
                          String dataCartridgeAlias, String asPolicy, String depPolicy)
            throws AppFactoryException {
//TODO Punnadi
	/*	CartridgeInfoBean cartridgeInfoBean = new CartridgeInfoBean();

		try {

			if (StringUtils.isNotBlank(dataCartridgeType)
					&& StringUtils.isNotBlank(dataCartridgeAlias)) {
				log.info(String.format(
						"Subscribing to data cartridge %s with alias %s.%n",
						dataCartridgeType, dataCartridgeAlias));

				cartridgeInfoBean.setCartridgeType(dataCartridgeType);
				cartridgeInfoBean.setAlias(dataCartridgeAlias);
				cartridgeInfoBean.setRepoURL(null);
				cartridgeInfoBean.setPrivateRepo(false);
				cartridgeInfoBean.setRepoUsername(null);
				cartridgeInfoBean.setRepoPassword(null);
				cartridgeInfoBean.setAutoscalePolicy(null);
				cartridgeInfoBean.setDeploymentPolicy(null);
				subscribeCartridge(cartridgeInfoBean);

			}

			cartridgeInfoBean.setCartridgeType(cartridgeType.toLowerCase());
			cartridgeInfoBean.setAlias(alias.toLowerCase());
			cartridgeInfoBean.setRepoURL(externalRepoURL);
			cartridgeInfoBean.setPrivateRepo(privateRepo);
			cartridgeInfoBean.setRepoUsername(username);
			cartridgeInfoBean.setRepoPassword(password);
			cartridgeInfoBean.setAutoscalePolicy(asPolicy);
			cartridgeInfoBean.setDeploymentPolicy(depPolicy);
			subscribeCartridge(cartridgeInfoBean);

		} catch (CommandException e) {
			handleException("Exception in subscribing to cartridge", e);
		}*/
    }

	/*private void subscribeCartridge(CartridgeInfoBean cartridgeInfoBean)
			throws CommandException, AppFactoryException {
		String completeJsonSubscribeString;

		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.create();
		completeJsonSubscribeString = gson.toJson(cartridgeInfoBean,
				CartridgeInfoBean.class);

		HttpClient httpClient = getNewHttpClient();

		ServerResponse response = doPost(httpClient, this.stratosManagerURL
				+ this.SUBSCRIBE_CARTRIDGE_REST_END_POINT,
				completeJsonSubscribeString);

		if (response.getStatusCode() == HttpStatus.SC_OK) {

			if (log.isDebugEnabled()) {
				log.debug(" Status 200 returned when subsctibing to cartridge "
						+ cartridgeInfoBean.getCartridgeType());
			}

			String subscriptionOutput = response.getResponse();

			if (subscriptionOutput == null) {
				log.error("Error occurred while getting response. Response is null");
				return;
			}

			String subscriptionOutputJSON = subscriptionOutput.substring(20,
					subscriptionOutput.length() - 1);
			SubscriptionInfo subcriptionInfo = gson.fromJson(
					subscriptionOutputJSON, SubscriptionInfo.class);

			log.info(String.format(
					"Successfully subscribed to %s cartridge with alias %s with repo url %s"
							+ ".%n", cartridgeInfoBean.getCartridgeType(),
					cartridgeInfoBean.getAlias(),
					subcriptionInfo.getRepositoryURL()));
		} else if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
			log.error("Authorization failed when subsctibing to cartridge "
					+ cartridgeInfoBean.getCartridgeType());
			return;
		} else {
			log.error("Error occurred while subscribing to cartdridge,"
					+ "server returned  " + response.getStatusCode() + " "
					+ response.getResponse());
			return;
		}
	}*/

    // This method helps to unsubscribe cartridges
    public void unsubscribe(String alias) throws AppFactoryException {
        HttpClient httpClient = getNewHttpClient();
        try {
            doPost(httpClient, this.stratosManagerURL
                               + UNSUBSCRIBE_TENANT_END_POINT, alias);
            log.info("Successfully unsubscribed " + alias);
        } catch (Exception e) {
            handleException("Exception in un-subscribing cartridge", e);
        }
    }

    public String getSubscribedCartridgeLbClusterId(String cartridgeAlias) throws AppFactoryException {
        JsonObject catridgeJson = getSubscribedCartridge(cartridgeAlias);
        if (catridgeJson != null && catridgeJson.has("lbClusterId")) {
            return catridgeJson.get("lbClusterId").getAsString();
        }
        return null;
    }

    public String getSubscribedCartridgeClusterId(String cartridgeAlias) throws AppFactoryException {
        JsonObject catridgeJson = getSubscribedCartridge(cartridgeAlias);
        if (catridgeJson != null && catridgeJson.has("clusterId")) {
            return catridgeJson.get("clusterId").getAsString();
        }
        return null;
    }

    private JsonObject getSubscribedCartridge(String cartridgeAlias) throws AppFactoryException {
        HttpClient httpClient = getNewHttpClient();
        JsonObject catridgeJson = null;
        try {
            String serviceEndPoint = stratosManagerURL + LIST_DETAILS_OF_SUBSCRIBED_CARTRIDGE + cartridgeAlias;
            ServerResponse response = doGet(httpClient, serviceEndPoint);

            if (HttpStatus.SC_OK == response.getStatusCode()) {
                if (log.isDebugEnabled()) {
                    log.debug("Successfully retrieved the subscription info");
                }

                GsonBuilder gsonBuilder = new GsonBuilder();
                JsonParser jsonParser = new JsonParser();
                JsonObject subscriptionInfo = jsonParser.parse(response.getResponse()).getAsJsonObject();
                if (subscriptionInfo != null && subscriptionInfo.isJsonObject()) {
                    JsonElement catridge = subscriptionInfo.get("cartridge");
                    if (catridge.isJsonObject()) {
                        catridgeJson = catridge.getAsJsonObject();
                    }
                }
            }
        } catch (Exception e) {
            handleException("Error occurred while getting subscription info", e);
        }
        return catridgeJson;
    }

    // This method gives the HTTP response string
    private String getHttpResponseString(HttpResponse response) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    (response.getEntity().getContent())));

            String output;
            String result = "";
            while ((output = reader.readLine()) != null) {
                result += output;
            }
            return result;
        } catch (SocketException e) {
            log.error("Error while connecting to the server ", e);
            return null;
        } catch (NullPointerException e) {
            log.error("Null value return from server ", e);
            return null;
        } catch (IOException e) {
            log.error("IO error ", e);
            return null;
        }
    }

    // This is for handle exception
    private void handleException(String msg, Exception e)
            throws AppFactoryException {

        log.error(msg, e);
        throw new AppFactoryException(msg, e);
    }

    /**
     * Mutual SSL
     *
     * @return authorization header string for the requests to Stratos
     */
    private static String getAuthHeaderValue(String userName) {

        byte[] getUserPasswordInBytes = (userName + ":nopassword").getBytes();
        String encodedValue = new String(
                Base64.encodeBase64(getUserPasswordInBytes));
        return "Basic " + encodedValue;
    }

    public ServerResponse doPost(HttpClient httpClient, String resourcePath,
                                 String jsonParamString) throws CommandException,
                                                                AppFactoryException {

        PostMethod postRequest = new PostMethod(resourcePath);

        StringRequestEntity input = null;
        try {
            input = new StringRequestEntity(jsonParamString,
                                            "application/json", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            handleException("Error occurred while getting POST parameters", e);
        }

        postRequest.setRequestEntity(input);

        String userPass = this.username + ":" + this.password;
        String basicAuth = null;
        try {
            basicAuth = getAuthHeaderValue(username);
        } catch (Exception e) {
            handleException("Error occurred while getting username:password", e);
        }
        postRequest.addRequestHeader("Authorization", basicAuth);

        int response = 0;
        String responseString = null;
        try {
            response = httpClient.executeMethod(postRequest);
        } catch (IOException e) {
            handleException("Error occurred while executing POST method", e);
        }
        try {
            responseString = postRequest.getResponseBodyAsString();
        } catch (IOException e) {
            handleException("error while getting response as String", e);
        }

        return new ServerResponse(responseString, response);

    }

    /**
     * sends a GET request to the specified URL
     *
     * @param httpClient
     *            Http client that sends the request
     * @param resourcePath
     *            EPR for the resource
     * @return
     * @throws CommandException
     * @throws Exception
     */
    public ServerResponse doGet(HttpClient httpClient, String resourcePath)
            throws CommandException, Exception {
        GetMethod getRequest = new GetMethod(resourcePath);
        String userPass = this.username + ":" + this.password;
        String basicAuth = null;
        try {
            basicAuth = getAuthHeaderValue(username);
        } catch (Exception e) {
            handleException("Error occurred while getting username:password", e);
        }
        getRequest.addRequestHeader("Authorization", basicAuth);

        int response = 0;
        String responseString = null;
        try {
            response = httpClient.executeMethod(getRequest);
        } catch (IOException e) {
            handleException("Error occurred while executing GET method", e);
        }
        try {
            responseString = getRequest.getResponseBodyAsString();
        } catch (IOException e) {
            handleException("error while getting response as String", e);
        }

        return new ServerResponse(responseString, response);

    }

    public HttpClient getNewHttpClient() {
        return new HttpClient(new MultiThreadedHttpConnectionManager());
    }
}
