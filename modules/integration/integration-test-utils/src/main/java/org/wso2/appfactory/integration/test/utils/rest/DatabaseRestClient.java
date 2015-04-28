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
import org.wso2.appfactory.integration.test.utils.AFConstants;
import org.wso2.appfactory.integration.test.utils.AppFactoryIntegrationTestException;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class DatabaseRestClient extends BaseRestClient {

	public DatabaseRestClient(String backEndUrl, String username, String password) throws Exception {
		setBackEndUrl(backEndUrl);

		if (getRequestHeaders().get(HEADER_CONTENT_TYPE) == null) {
			getRequestHeaders().put(HEADER_CONTENT_TYPE, MEDIA_TYPE_X_WWW_FORM);
		}

	//	login(username, password);
	}

	public void createDatabaseAndAttachUser(String applicationKey, String dbName, String dbServerInstance,
	                                        String customPassword, String isBasic, String copyToAll,
	                                        String createDatasource) throws Exception {


		Map<String, String> msgBodyMap = new HashMap<String, String>();
		msgBodyMap.put("action", "createDatabaseAndAttachUser");
		msgBodyMap.put("databaseServerInstanceName", dbServerInstance);
		msgBodyMap.put("customPassword", customPassword);
		msgBodyMap.put("isBasic", isBasic );
		msgBodyMap.put("copyToAll", copyToAll);
		msgBodyMap.put("createDatasource", createDatasource);
	/*	HttpResponse httpResponse = getHttpResponse(msgBodyMap, new URL(getBackEndUrl() + AFConstants.APPMGT_URL_SURFIX + "resources/database/add/ajax/add.jag");

		HttpResponse response = HttpRequestUtil
				.doPost(),
				        "action=createDatabaseAndAttachUser&applicationKey=" + applicationKey +
				        "&databaseServerInstanceName" +
				        "&customPassword" +
				        "&isBasic" +
				        "&copyToAll" +
				        "&createDatasource"
						, getRequestHeaders());

		if (response.getResponseCode() == HttpStatus.SC_OK) {
			//TODO
			return;
		} else {
			throw new AppFactoryIntegrationTestException("GetAppInfo failed " + response.getData());
		}*/
	}

	public void createDatabase(String appKey, String databaseName, String dbServerInstance) {

	}

	/*
	public String[] getDatabases(String appKey) {

	}


	public String[] getDatabasesInfoForStages(String appKey, String stage) {

	}

	getDbUserTemplateInfoForStages
			getAttachedUsers
	attachNewUser
			detachUser
	getUserPrivileges
			editUserPermissions
	getAllDatabasesInfo
			dropDatabase
	getTemplates
	*/

	/*

	getDatabaseUsers
			getDatabaseUsersForRssInstance
	getAvailableUsersToAttachToDatabase
			deleteUser
	createDatabaseUser
	*/


}
