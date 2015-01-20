/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.appfactory.apiManager.integration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.APIIntegration;
import org.wso2.carbon.appfactory.core.dto.API;
import org.wso2.carbon.appfactory.core.dto.APIMetadata;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.wso2.carbon.appfactory.apiManager.integration.utils.Constants.*;
import static org.wso2.carbon.appfactory.apiManager.integration.utils.Utils.*;

public class APIManagerIntegrationService extends AbstractAdmin implements APIIntegration {

	private static final Log log = LogFactory.getLog(APIManagerIntegrationService.class);

	public void loginToStore(HttpClient httpClient, String samlToken) throws AppFactoryException {
		login(STORE_LOGIN_ENDPOINT, httpClient, samlToken);
	}

	public void loginToPublisher(HttpClient httpClient, String samlToken) throws AppFactoryException {
		login(PUBLISHER_LOGIN_ENDPOINT, httpClient, samlToken);
	}

	/**
	 * Method to login API Manager. Login is done using given {@code httpClient}
	 * so it gets authenticated.
	 * Use the same client for further processing.
	 * Otherwise Authentication failure will occur.
	 * 
	 * @param endpoint
	 * @param httpClient
	 * @throws AppFactoryException
	 */
	private void login(String endpoint, HttpClient httpClient, String samlToken) throws AppFactoryException {

		// We expect an encoded saml token.
		if (samlToken == null || samlToken.equals("")) {
			String msg = "Unable to get the SAML token";
			log.error(msg);
			throw new AppFactoryException(msg);
		}

		URL apiManagerUrl = getApiManagerURL();

		List<NameValuePair> parameters = new ArrayList<NameValuePair>();

		parameters.add(new BasicNameValuePair(ACTION, "loginWithSAMLToken"));
		parameters.add(new BasicNameValuePair("samlToken", samlToken));

		HttpPost postMethod = createHttpPostRequest(apiManagerUrl, parameters, endpoint);
		HttpResponse response = executeHttpMethod(httpClient, postMethod);

		try {
			EntityUtils.consume(response.getEntity());
		} catch (IOException e) {
			String msg = "Failed to consume http response";
			log.error(msg, e);
			throw new AppFactoryException(msg, e);
		}
	}

	public boolean createApplication(String applicationId, String samlToken) throws AppFactoryException {

		HttpClient httpClient = new DefaultHttpClient();
		try {
			loginToStore(httpClient, samlToken);

			if (!isApplicationNameInUse(applicationId, samlToken)) {
				URL apiManagerUrl = getApiManagerURL();

				List<NameValuePair> parameters = new ArrayList<NameValuePair>();

				parameters.add(new BasicNameValuePair(ACTION, "addApplication"));
				parameters.add(new BasicNameValuePair("application", applicationId));
				parameters.add(new BasicNameValuePair("tier", getDefaultTier()));
				parameters.add(new BasicNameValuePair("callbackUrl", getDefaultCallbackURL()));
				parameters.add(new BasicNameValuePair(DESCRIPTION, ""));

				HttpPost postMethod =
				                      createHttpPostRequest(apiManagerUrl, parameters,
				                                            CREATE_APPLICATION_ENDPOINT);
				HttpResponse response = executeHttpMethod(httpClient, postMethod);

				if (response != null) {
					try {
						EntityUtils.consume(response.getEntity());
					} catch (IOException e) {
						String msg = "Failed to consume http response";
						log.error(msg, e);
						throw new AppFactoryException(msg, e);
					}
				}
			}
			return true;
		} finally {
			httpClient.getConnectionManager().shutdown();

			try {
				// Remove DefaultApplication from API Manager
				removeApplication("DefaultApplication", samlToken);
			} catch (AppFactoryException e) {
				log.error("Error while deleteing 'DefaultApplication' from API Manager");
			}
		}
	}

	public boolean isApplicationNameInUse(String applicationId, String samlToken) throws AppFactoryException {
		HttpClient httpClient = new DefaultHttpClient();
		try {
			loginToStore(httpClient, samlToken);

			URL apiManagerUrl = getApiManagerURL();

			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			parameters.add(new BasicNameValuePair(ACTION, "getApplications"));
			parameters.add(new BasicNameValuePair(USERNAME,
			                                      CarbonContext.getThreadLocalCarbonContext()
			                                                   .getUsername()));

			HttpPost postMethod =
			                      createHttpPostRequest(apiManagerUrl, parameters,
			                                            LIST_APPLICATION_ENDPOINT);

			HttpResponse httpResponse = executeHttpMethod(httpClient, postMethod);
			if (httpResponse != null) {
				try {

					HttpEntity responseEntity = httpResponse.getEntity();
					String responseBody = EntityUtils.toString(responseEntity);

					JsonObject response = getJsonObject(responseBody);
					JsonArray applications = response.getAsJsonArray("applications");

					if (applications != null) {
						for (JsonElement application : applications) {
							String applicationName =
							                         ((JsonObject) application).get(NAME)
							                                                   .getAsString();
							if (applicationName.equals(applicationId)) {
								return true;
							}
						}
					}
				} catch (IOException e) {
					String msg = "Error reading the json response";
					log.error(msg, e);
					throw new AppFactoryException(msg, e);
				} finally {
					try {
						EntityUtils.consume(httpResponse.getEntity());
					} catch (IOException e) {
						String msg = "Failed to consume http response";
						log.error(msg, e);
						throw new AppFactoryException(msg, e);
					}
				}
			}
			return false;
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
	}

	public boolean removeApplication(String applicationId, String samlToken) throws AppFactoryException {

		HttpClient httpClient = new DefaultHttpClient();
		try {
			loginToStore(httpClient, samlToken);
			URL apiManagerUrl = getApiManagerURL();

			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			parameters.add(new BasicNameValuePair(ACTION, "removeApplication"));
			parameters.add(new BasicNameValuePair("application", applicationId));
			parameters.add(new BasicNameValuePair("tier", getDefaultTier()));

			HttpPost postMethod =
			                      createHttpPostRequest(apiManagerUrl, parameters,
			                                            DELETE_APPLICATION_ENDPOINT);
			HttpResponse response = executeHttpMethod(httpClient, postMethod);

			if (response != null) {

			}
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return true;
	}

	public boolean addAPIsToApplication(String s, String s1, String s2, String s3, String samlToken)
	                                                                              throws AppFactoryException {
		// returning false since we do not support this for the moment.
		return false;
	}

	/**
	 * Get the basic information of APIs belong to a given
	 * appkey
	 * 
	 * @param appKey
	 *            application key
	 * @return an Array or API objects
	 * @throws AppFactoryException
	 */

	public API[] getAPIsOfUserApp(String applicationId, String appOwner, String samlToken) throws AppFactoryException {
		HttpClient httpClient = new DefaultHttpClient();
		try {
			loginToStore(httpClient, samlToken);
			URL apiManagerUrl = getApiManagerURL();

			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			parameters.add(new BasicNameValuePair(ACTION, "getSubscriptionByApplication"));
			parameters.add(new BasicNameValuePair("app", applicationId));
			parameters.add(new BasicNameValuePair("username", appOwner));

			HttpPost postMethod =
			                      createHttpPostRequest(apiManagerUrl, parameters,
			                                            LIST_SUBSCRIPTIONS_ENDPOINT);
			HttpResponse httpResponse = executeHttpMethod(httpClient, postMethod);
			if (httpResponse != null) {

				// Reading the response json
				List<API> apis = new ArrayList<API>();
				try {
					HttpEntity responseEntity = httpResponse.getEntity();
					String responseBody = EntityUtils.toString(responseEntity);

					JsonObject response = getJsonObject(responseBody);
					JsonArray apisAr = response.getAsJsonArray("apis");

					if (apisAr != null) {
						for (JsonElement apiElm : apisAr) {
							JsonObject obj = ((JsonObject) apiElm);
							API api = new API();
							api.setApiName(obj.get(API_NAME).getAsString());
							api.setApiVersion(obj.get(API_VERSION).getAsString());
							api.setApiProvider(obj.get(API_PROVIDER).getAsString());
							try {
								api.setDescription(obj.get(DESCRIPTION).getAsString());
							} catch (UnsupportedOperationException e) {
							}
							apis.add(api);
						}
					}else{
						JsonElement error = response.get("message");
						if(error != null){
							String msg = "Error received from API-M: " + error.getAsString();
    						log.error(msg);
    						throw new AppFactoryException(msg);
						}
					}

				} catch (IOException e) {
					String msg = "Error reading the json response";
					log.error(msg, e);
					throw new AppFactoryException(msg, e);
				} finally {
					try {
						EntityUtils.consume(httpResponse.getEntity());
					} catch (IOException e) {
						String msg = "Failed to consume http response";
						log.error(msg, e);
					}
				}
				return apis.toArray(new API[apis.size()]);
			}
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return null;
	}

	public API[] getAPIsOfApplication(String applicationId, String username, String samlToken) throws AppFactoryException {
		HttpClient httpClient = new DefaultHttpClient();
		try {
			loginToStore(httpClient, samlToken);

			URL apiManagerUrl = getApiManagerURL();


			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			parameters.add(new BasicNameValuePair(ACTION, "getAllSubscriptions"));
			parameters.add(new BasicNameValuePair(USERNAME, username));

			HttpPost postMethod =
			                      createHttpPostRequest(apiManagerUrl, parameters,
			                                            LIST_SUBSCRIPTIONS_ENDPOINT);
			HttpResponse httpResponse = executeHttpMethod(httpClient, postMethod);
			if (httpResponse != null) {
				// Reading the response json
				List<API> apiNames = new ArrayList<API>();
				try {
					HttpEntity responseEntity = httpResponse.getEntity();
					String responseBody = EntityUtils.toString(responseEntity);

					JsonObject response = getJsonObject(responseBody);
					JsonArray subscriptions = response.getAsJsonArray(SUBSCRIPTIONS);

					if (subscriptions != null) {
						for (JsonElement subscription : subscriptions) {
							String applicationName =
							                         ((JsonObject) subscription).get(NAME)
							                                                    .getAsString();
							if (applicationName.equals(applicationId)) {
								JsonArray applicationSubscriptions =
								                                     ((JsonObject) subscription).getAsJsonArray(SUBSCRIPTIONS);
								for (JsonElement applicationSubscription : applicationSubscriptions) {
									API apiInfo =
									              populateAPIInfo((JsonObject) applicationSubscription);

									apiNames.add(apiInfo);
								}
								break;
							}
						}
					}

				} catch (IOException e) {
					String msg = "Error reading the json response";
					log.error(msg, e);
					throw new AppFactoryException(msg, e);
				} finally {
					try {
						EntityUtils.consume(httpResponse.getEntity());
					} catch (IOException e) {
						String msg = "Failed to consume http response";
						log.error(msg, e);
					}
				}
				return apiNames.toArray(new API[apiNames.size()]);
			}
			return new API[0];
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
	}

	public API getAPIInformation(String apiName, String apiVersion, String apiProvider, String samlToken)
	                                                                                   throws AppFactoryException {
		HttpClient httpClient = new DefaultHttpClient();
		try {
			loginToPublisher(httpClient, samlToken);

			URL apiManagerUrl = getApiManagerURL();

			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			parameters.add(new BasicNameValuePair(ACTION, "getAPI"));
			parameters.add(new BasicNameValuePair(NAME, apiName));
			parameters.add(new BasicNameValuePair(VERSION, apiVersion));
			parameters.add(new BasicNameValuePair(PROVIDER, apiProvider));

			HttpPost postMethod =
			                      createHttpPostRequest(apiManagerUrl, parameters,
			                                            PUBLISHER_API_INFO_ENDPOINT);

			HttpResponse httpResponse = executeHttpMethod(httpClient, postMethod);
			if (httpResponse != null) {
				try {

					HttpEntity responseEntity = httpResponse.getEntity();
					String responseBody = EntityUtils.toString(responseEntity);

					JsonObject response = getJsonObject(responseBody);
					JsonObject apiElement = response.getAsJsonObject("api");

					return populateAPIInfo(apiElement);
				} catch (IOException e) {
					String msg = "Error reading the json response";
					log.error(msg, e);
					throw new AppFactoryException(msg, e);
				} finally {
					try {
						EntityUtils.consume(httpResponse.getEntity());
					} catch (IOException e) {
						String msg = "Failed to consume http response";
						log.error(msg, e);
						throw new AppFactoryException(msg, e);
					}
				}
			}
			return new API();
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
	}

	public boolean generateKeys(String appId, String samlToken) throws AppFactoryException {
		HttpClient httpClient = new DefaultHttpClient();
		try {
			loginToStore(httpClient, samlToken);

			URL apiManagerUrl = getApiManagerURL();

			generateKey(appId, apiManagerUrl, "SANDBOX", httpClient);
			generateKey(appId, apiManagerUrl, "PRODUCTION", httpClient);

			return true;
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
	}

	public boolean removeAPIFromApplication(String s, String s1, String s2, String s3, String samlToken)
	                                                                                  throws AppFactoryException {
		// returning false since we do not support this for the moment.
		return false;
	}

	/**
	 * Retrieve generated keys for particular application in API-M front
	 * 
	 * @param applicationId
	 *            application id
	 * @return list of keys both sandbox and prod
	 * @throws AppFactoryException
	 */
	public APIMetadata[] getSavedKeys(String applicationId, String username, String samlToken) throws AppFactoryException {
        List<APIMetadata> keyList = retrieveKeys(applicationId, username, samlToken);
		if (keyList.isEmpty()) {
			return new APIMetadata[0];
		}
		return keyList.toArray(new APIMetadata[keyList.size()]);
	}

	private List<APIMetadata> retrieveKeys(String applicationId, String username, String samlToken) throws AppFactoryException {
		API[] api = getAPIsOfApplication(applicationId, username, samlToken);
		List<APIMetadata> keyList = new ArrayList<APIMetadata>();

		if (api != null && api.length > 0) {

			// Because API Manager has keys per application, not for api
			API singleApi = api[0];
			APIMetadata[] keys = singleApi.getKeys();
			keyList.addAll(Arrays.asList(keys));
		}
		return keyList;
	}

}
