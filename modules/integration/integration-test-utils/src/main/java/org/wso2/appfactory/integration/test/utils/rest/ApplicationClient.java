/*
 * Copyright 2015 WSO2, Inc. (http://wso2.com)
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.appfactory.integration.test.utils.rest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.httpclient.HttpStatus;
import org.json.JSONObject;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestException;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * REST client for AppMgt
 * TODO: use {@link this#generateMsgBody(java.util.Map)} method to generate the message body
 */
public class ApplicationClient extends BaseClient {

	private static final String REQUEST_KEY_ACTION = "action";

	/**
	 * Construct authenticates REST client to invoke appmgt functions
	 *
	 * @param backEndUrl backend url
	 * @param username   username
	 * @param password   password
	 * @throws Exception
	 */
	public ApplicationClient(String backEndUrl, String username, String password) throws Exception {
		super(backEndUrl, username, password);
	}

	/**
	 * Get app info
	 *
	 * @param applicationKey application key
	 * @return HTTP response
	 * @throws Exception
	 */
	public HttpResponse getAppInfo(String applicationKey, boolean checkErrors) throws Exception {
		HttpResponse response = HttpRequestUtil
			.doPost(new URL(getBackEndUrl() + APPMGT_URL_SURFIX + APPMGT_APPLICATION_GET),
					"action=getAppInfo&applicationKey=" + applicationKey, getRequestHeaders());

		if (response.getResponseCode() == HttpStatus.SC_OK) {
			if(checkErrors) {
				checkErrors(response);
			}
			return response;
		} else {
			throw new AFIntegrationTestException("GetAppInfo failed " + response.getData());
		}
	}

	/**
	 * Check whether application name already available
	 *
	 * @param applicationName application name
	 * @return true if application name available otherwise false
	 * @throws Exception
	 */
	public boolean isAppNameAlreadyAvailable(String applicationName) throws Exception {
		HttpResponse response = HttpRequestUtil
			.doPost(new URL(getBackEndUrl() + APPMGT_URL_SURFIX + APPMGT_APPLICATION_ADD),
					"action=isAppNameAlreadyAvailable&applicationName=" + applicationName, getRequestHeaders());

		if (response.getResponseCode() == HttpStatus.SC_OK) {
			checkErrors(response);
			JSONObject jsonObject = new JSONObject(response.getData());
			return Boolean.parseBoolean(jsonObject.getString("isAvailable"));
		} else {
			throw new AFIntegrationTestException("IsAppNameAlreadyAvailable failed " + response.getData());
		}
	}

	/**
	 * Check whether application key available
	 *
	 * @param applicationKey application key
	 * @return true if application key NOT available otherwise false
	 * @throws Exception
	 */
	public boolean isApplicationKeyAvailable(String applicationKey) throws Exception {
		HttpResponse response = HttpRequestUtil
			.doPost(new URL(getBackEndUrl() + APPMGT_URL_SURFIX + APPMGT_APPLICATION_ADD),
					"action=isApplicationKeyAvailable&applicationKey=" + applicationKey, getRequestHeaders());

		if (response.getResponseCode() == HttpStatus.SC_OK) {
			checkErrors(response);
			JSONObject jsonObject = new JSONObject(response.getData());
			return Boolean.parseBoolean(jsonObject.getString("isAvailable"));
		} else {
			throw new AFIntegrationTestException("IsApplicationKeyAvailable failed " + response.getData());
		}
	}

	/**
	 * Create new application
	 *
	 * @param applicationName        application name
	 * @param applicationKey         application key
	 * @param applicationType        application type
	 * @param userName               username
	 * @param applicationDescription application description
	 * @throws Exception
	 */
	public void createNewApplication(String applicationName, String applicationKey, String applicationType,
									 String userName, String applicationDescription) throws Exception {
		HttpResponse response = HttpRequestUtil
			.doPost(new URL(getBackEndUrl() + APPMGT_URL_SURFIX + APPMGT_APPLICATION_ADD),
					"action=createNewApplication&applicationName=" + applicationName + "&applicationKey=" +
					applicationKey + "&creation_method=create_application" + "&appType=" + applicationType +
					"&uploadableAppType=Uploaded-App-Jax-WS" +
					"&uploaded_application=&appIcon=&applicationDescription=" + applicationDescription +
					"&repoAccessibility=perDevRepo&repositoryType=git" + "&userName=" + userName +
					"&aPaaS=false&iPaaS=false", getRequestHeaders());

		if (response.getResponseCode() == HttpStatus.SC_OK) {
			checkErrors(response);
			JSONObject jsonObject = new JSONObject(response.getData());
			if (!jsonObject.getString("message").equals(
				"Application was created under Repository type git")) {
				throw new AFIntegrationTestException("CreateNewApplication failed : " + response.getData());
			}
		} else {
			throw new AFIntegrationTestException("CreateNewApplication failed " + response.getData());
		}
	}

	public HttpResponse publishUserActivity(String events) throws Exception {
		HttpResponse response =
			HttpRequestUtil.doPost(new URL(getBackEndUrl() +
                                           APPMGT_URL_SURFIX +
                                           EVENTS_PUBLISHING),
                                   "action=userActivity&events=" + events,
                                   getRequestHeaders());

		if (response.getResponseCode() == HttpStatus.SC_OK) {
			// checkErrors(response);
			return response;
		} else {
			throw new Exception("Sending events succeeded " + response.getData());
		}
	}

    /**
     * Delete application
     *
     * @param userName usernam
     * @param applicationKey application key
     * @throws Exception
     */
    public JsonObject deleteApplication(String userName, String applicationKey) throws Exception {
        HttpResponse response = HttpRequestUtil
                .doPost(new URL(getBackEndUrl() + APPMGT_URL_SURFIX +APPMGT_APPLICATION_DELETE),
                        "userName="+userName+"&appKey="+applicationKey, getRequestHeaders());
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            JsonParser jsonParser = new JsonParser();
            JsonElement jsonElement = jsonParser.parse(response.getData());
            return jsonElement.getAsJsonObject();
        } else {
            throw new AFIntegrationTestException("Delete application failed " +response.getResponseCode()+" "
                    + response.getData());
        }
    }

	/**
	 * Get applications of a given user
	 *
	 * @param userName
	 * @return
	 */
	public JsonArray getApplicationsOfUser(String userName) throws AFIntegrationTestException {
		Map<String, String> msgBodyMap = new HashMap<String, String>();
		msgBodyMap.put(REQUEST_KEY_ACTION, "getApplicationsOfUser");
		msgBodyMap.put("userName", userName);
		HttpResponse response = super.doPostRequest(BaseClient.APPMGT_APPLICATION_GET, msgBodyMap);
		if (response.getResponseCode() == HttpStatus.SC_OK) {
			JsonParser jsonParser = new JsonParser();
			JsonElement jsonElement = jsonParser.parse(response.getData());
			return jsonElement.getAsJsonArray();
		} else {
			throw new AFIntegrationTestException("Error while getting applications of user :  " + userName +
			                                     response.getResponseCode() + response.getData());
		}

	}

	public JsonArray getAppVersionsInStage(String userName, String stageName, String applicationKey) throws
	                                                                                   AFIntegrationTestException {
		Map<String, String> msgBodyMap = new HashMap<String, String>();
		msgBodyMap.put(REQUEST_KEY_ACTION, "getAppVersionsInStage");
		msgBodyMap.put("userName", userName);
		msgBodyMap.put("stageName", stageName);
		msgBodyMap.put("applicationKey", applicationKey);
		HttpResponse response = super.doPostRequest(BaseClient.APPMGT_APPLICATION_GET, msgBodyMap);
		if (response.getResponseCode() == HttpStatus.SC_OK) {
			JsonParser jsonParser = new JsonParser();
			JsonElement jsonElement = jsonParser.parse(response.getData());
			return jsonElement.getAsJsonArray();
		} else {
			throw new AFIntegrationTestException(
					"Error while getting app versions of stage :  " + stageName + " for user : " + userName +
					" of application : " + applicationKey + response.getResponseCode() + response.getData());
		}
	}


}
