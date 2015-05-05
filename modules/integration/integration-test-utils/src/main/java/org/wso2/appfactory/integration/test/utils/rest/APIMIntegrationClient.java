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

import org.apache.commons.httpclient.HttpStatus;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestException;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by muthulee on 4/29/15.
 */
public class APIMIntegrationClient extends BaseClient {

	//src/site/blocks
	//resources/apis/add/ajax/add.jag - createApplication
	//resources/apis/add/block.jag
	//resources/apis/get/ajax/get.jag -getAPIsOfApp
	//resources/apis/get/block.jag
	//resources/apis/key/ajax/key.jag - getSavedKeys, keysExistsInAllStages


	public APIMIntegrationClient(String backEndUrl, String username, String password) throws Exception {
		super(backEndUrl, username, password);
	}

	/**
	 *
	 * @param applicationKey
	 * @param username
	 * @throws Exception
	 */
	public void createApplication(String applicationKey, String username) throws Exception {
		Map<String, String> msgBodyMap = new HashMap<String, String>();
		msgBodyMap.put("action", "createApplication");
		msgBodyMap.put("applicationKey", applicationKey);
		msgBodyMap.put("username", username);
		HttpResponse response = doPostRequest("resources/apis/add/ajax/add.jag", msgBodyMap);
		if (response.getResponseCode() == HttpStatus.SC_OK) {
			return;
		} else {
			throw new AFIntegrationTestException(response.getResponseCode() + " " + response.getData());
		}
	}

	/**
	 *
	 * @param applicationKey
	 * @param appOwner
	 * @return
	 * @throws Exception
	 */
	public String[] getAPIsOfApp(String applicationKey, String appOwner) throws Exception {
		Map<String, String> msgBodyMap = new HashMap<String, String>();
		msgBodyMap.put("action", "getAPIsOfApp");
		msgBodyMap.put("applicationKey", applicationKey);
		msgBodyMap.put("appOwner", applicationKey);
		HttpResponse response = doPostRequest("resources/apis/get/ajax/get.jag", msgBodyMap);
		if (response.getResponseCode() == HttpStatus.SC_OK) {
			//TODO
			System.out.println(response.getData());
			return new String[0];
		} else {
			throw new AFIntegrationTestException(response.getResponseCode() + " " + response.getData());
		}
	}

	/**
	 * This method will always sync and read the saved keys
	 * @param applicationKey
	 * @param appOwner
	 * @return
	 */
	public String[] getSavedKeys(String applicationKey, String appOwner) throws Exception {
		Map<String, String> msgBodyMap = new HashMap<String, String>();
		msgBodyMap.put("applicationKey", applicationKey);
		msgBodyMap.put("isSync", "true");
		msgBodyMap.put("userName", appOwner);
		HttpResponse response = doPostRequest("resources/apis/key/ajax/key.jag", msgBodyMap);
		if (response.getResponseCode() == HttpStatus.SC_OK) {
			//TODO
			System.out.println(response.getData());
			return new String[0];
		} else {
			throw new AFIntegrationTestException(response.getResponseCode() + " " + response.getData());
		}
	}

}
