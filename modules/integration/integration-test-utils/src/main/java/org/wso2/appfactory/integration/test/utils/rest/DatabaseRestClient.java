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

public class DatabaseRestClient extends BaseRestClient {

	public DatabaseRestClient(String backEndUrl, String username, String password) throws Exception {
		super(backEndUrl, username, password);
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
		HttpResponse response = super.doPostRequest("resources/database/add/ajax/add.jag", msgBodyMap);
		if (response.getResponseCode() == HttpStatus.SC_OK) {
			//TODO
			return;
		} else {
			throw new AFIntegrationTestException("GetAppInfo failed " + response.getData());
		}
	}

	public void createDatabase(String applicationKey, String databaseName, String dbServerInstance) throws Exception{
		Map<String, String> msgBodyMap = new HashMap<String, String>();
		msgBodyMap.put("action", "createDatabaseAndAttachUser");
		msgBodyMap.put("applicationKey", applicationKey);
        msgBodyMap.put("databaseName", databaseName);
		msgBodyMap.put("databaseServerInstanceName", dbServerInstance);
		HttpResponse response = super.doPostRequest("resources/database/add/ajax/add.jag", msgBodyMap);
		if (response.getResponseCode() == HttpStatus.SC_OK) {
			//TODO
			return;
		} else {
			throw new AFIntegrationTestException("GetAppInfo failed " + response.getData());
		}
	}

	public String[] getDatabases(String applicationKey) throws Exception {
		Map<String, String> msgBodyMap = new HashMap<String, String>();
		msgBodyMap.put("action", "getDatabases");
		msgBodyMap.put("applicationKey", applicationKey);
		HttpResponse response = super.doPostRequest("resources/database/add/ajax/add.jag", msgBodyMap);
		if (response.getResponseCode() == HttpStatus.SC_OK) {
			//TODO
			return new String[0];
		} else {
			throw new AFIntegrationTestException("GetAppInfo failed " + response.getData());
		}
	}


	public String[] getDatabasesInfoForStages(String applicationKey, String stage) throws Exception {
		Map<String, String> msgBodyMap = new HashMap<String, String>();
		msgBodyMap.put("action", "getDatabasesInfoForStages");
		msgBodyMap.put("applicationKey", applicationKey);
		msgBodyMap.put("stage", stage);
		HttpResponse response = super.doPostRequest("resources/database/add/ajax/add.jag", msgBodyMap);
		if (response.getResponseCode() == HttpStatus.SC_OK) {
			//TODO
			return new String[0];
		} else {
			throw new AFIntegrationTestException("GetAppInfo failed " + response.getData());
		}
	}

	public String[] getDbUserTemplateInfoForStages(String applicationKey) throws Exception {
		Map<String, String> msgBodyMap = new HashMap<String, String>();
		msgBodyMap.put("action", "getDbUserTemplateInfoForStages");
		msgBodyMap.put("applicationKey", applicationKey);
		HttpResponse response = super.doPostRequest("resources/database/add/ajax/add.jag", msgBodyMap);
		if (response.getResponseCode() == HttpStatus.SC_OK) {
			//TODO
			return new String[0];
		} else {
			throw new AFIntegrationTestException("GetAppInfo failed " + response.getData());
		}

	}



	public String[] getCreatableRSSinstances(String applicationKey) throws Exception {
		Map<String, String> msgBodyMap = new HashMap<String, String>();
		msgBodyMap.put("action", "getCreatableRSSinstances");
		msgBodyMap.put("applicationKey", applicationKey);
		HttpResponse response = super.doPostRequest("resources/database/add/ajax/add.jag", msgBodyMap);
		if (response.getResponseCode() == HttpStatus.SC_OK) {
			//TODO
			return new String[0];
		} else {
			throw new AFIntegrationTestException("GetAppInfo failed " + response.getData());
		}

	}

	public String[] getRSSinstances(String applicationKey)  throws Exception {
		Map<String, String> msgBodyMap = new HashMap<String, String>();
		msgBodyMap.put("action", "getRSSinstances");
		msgBodyMap.put("applicationKey", applicationKey);
		HttpResponse response = super.doPostRequest("resources/database/add/ajax/add.jag", msgBodyMap);
		if (response.getResponseCode() == HttpStatus.SC_OK) {
			//TODO
			return new String[0];
		} else {
			throw new AFIntegrationTestException("GetAppInfo failed " + response.getData());
		}

	}

	public String[] getAttachedUsers(String applicationKey, String databaseName, String stage)  throws Exception {
		Map<String, String> msgBodyMap = new HashMap<String, String>();
		msgBodyMap.put("action", "getAttachedUsers");
		msgBodyMap.put("applicationKey", applicationKey);
		msgBodyMap.put("dbname", databaseName);
		msgBodyMap.put("rssInstance", stage);
		HttpResponse response = super.doPostRequest("resources/database/add/ajax/add.jag", msgBodyMap);
		if (response.getResponseCode() == HttpStatus.SC_OK) {
			//TODO
			return new String[0];
		} else {
			throw new AFIntegrationTestException("GetAppInfo failed " + response.getData());
		}
	}

	public void attachNewUser(String applicationKey, String databaseName, String stage, String users, String templates)  throws Exception {
		Map<String, String> msgBodyMap = new HashMap<String, String>();
		msgBodyMap.put("action", "attachNewUser");
		msgBodyMap.put("applicationKey", applicationKey);
		msgBodyMap.put("databaseName", databaseName);
		msgBodyMap.put("dbServerInstanceName", stage);
		msgBodyMap.put("users", users);
		msgBodyMap.put("templates", templates);
		HttpResponse response = super.doPostRequest("resources/database/add/ajax/add.jag", msgBodyMap);
		if (response.getResponseCode() == HttpStatus.SC_OK) {
			//TODO
		} else {
			throw new AFIntegrationTestException("GetAppInfo failed " + response.getData());
		}

	}
	public void detachUser(String applicationKey, String databaseName, String stage, String username)  throws Exception {
		Map<String, String> msgBodyMap = new HashMap<String, String>();
		msgBodyMap.put("action", "detachUser");
		msgBodyMap.put("applicationKey", applicationKey);
		msgBodyMap.put("databaseName", databaseName);
		msgBodyMap.put("dbServerInstanceName", stage);
		HttpResponse response = super.doPostRequest("resources/database/add/ajax/add.jag", msgBodyMap);
		if (response.getResponseCode() == HttpStatus.SC_OK) {
			//TODO
		} else {
			throw new AFIntegrationTestException("GetAppInfo failed " + response.getData());
		}
	}


	public String getUserPrivileges(String applicationKey, String databaseName, String stage, String username) throws Exception {
		Map<String, String> msgBodyMap = new HashMap<String, String>();
		msgBodyMap.put("action", "getUserPrivileges");
		msgBodyMap.put("applicationKey", applicationKey);
		msgBodyMap.put("dbname", databaseName);
		msgBodyMap.put("rssInstanceName", stage);
		msgBodyMap.put("username", username);
		HttpResponse response = super.doPostRequest("resources/database/add/ajax/add.jag", msgBodyMap);
		if (response.getResponseCode() == HttpStatus.SC_OK) {
			//TODO
		} else {
			throw new AFIntegrationTestException("GetAppInfo failed " + response.getData());
		}
		return "";
	}

	public void editUserPermissions() {

	}

	public String[] getAllDatabasesInfo(String applicationKey) throws Exception {
		Map<String, String> msgBodyMap = new HashMap<String, String>();
		msgBodyMap.put("action", "getUserPrivileges");
		msgBodyMap.put("applicationKey", applicationKey);
		HttpResponse response = super.doPostRequest("resources/database/add/ajax/add.jag", msgBodyMap);
		if (response.getResponseCode() == HttpStatus.SC_OK) {
			//TODO
		} else {
			throw new AFIntegrationTestException("GetAppInfo failed " + response.getData());
		}
		return new String[1];

	}

	public void dropDatabase(String applicationKey) throws Exception {
		Map<String, String> msgBodyMap = new HashMap<String, String>();
		msgBodyMap.put("action", "dropDatabase");
		msgBodyMap.put("applicationKey", applicationKey);
		HttpResponse response = super.doPostRequest("resources/database/drop/ajax/drop.jag", msgBodyMap);
		if (response.getResponseCode() == HttpStatus.SC_OK) {
			//TODO
		} else {
			throw new AFIntegrationTestException("GetAppInfo failed " + response.getData());
		}
	}

	public String[] getTemplates(String applicationKey) throws Exception {
		Map<String, String> msgBodyMap = new HashMap<String, String>();
		msgBodyMap.put("action", "getTemplates");
		msgBodyMap.put("applicationKey", applicationKey);
		HttpResponse response = super.doPostRequest("resources/database/templates/ajax/list.jag", msgBodyMap);
		if (response.getResponseCode() == HttpStatus.SC_OK) {
			//TODO
		} else {
			throw new AFIntegrationTestException("GetAppInfo failed " + response.getData());
		}
		return new String[0];
	}

    /**
     *
     * @param applicationKey
     * @throws Exception
     */
    public void getDatabaseUsers(String applicationKey) throws Exception{

        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put("action", "getDatabaseUsers");
        msgBodyMap.put("applicationKey", applicationKey);

        HttpResponse response = super.doPostRequest("resources/database/users/list/ajax/list.jag", msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            //TODO
            return;
        } else {
            throw new AFIntegrationTestException("Get database users failed " + response.getData());
        }
    }

    public void getDatabaseUsersForRssInstance(String applicationKey,String rssInstance)throws Exception{

        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put("action", "getDatabaseUsersForRssInstance");
        msgBodyMap.put("applicationKey", applicationKey);
        msgBodyMap.put("rssInstance", rssInstance);

        HttpResponse response = super.doPostRequest("resources/database/users/list/ajax/list.jag", msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            return;
        } else {
            throw new AFIntegrationTestException("Create database user failed " + response.getData());
        }
    }

    /**
     *
     * @param applicationKey
     * @param databaseName
     * @param databaseServerInstanceName
     * @throws Exception
     */
    public void getAvailableUsersToAttachToDatabase(String applicationKey,String databaseName,String databaseServerInstanceName) throws Exception{

        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put("action", "getAvailableUsersToAttachToDatabase");
        msgBodyMap.put("applicationKey", applicationKey);
        msgBodyMap.put("databaseName", databaseName);
        msgBodyMap.put("dbServerInstanceName", databaseServerInstanceName );

        HttpResponse response = super.doPostRequest("resources/database/users/list/ajax/list.jag", msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            //TODO
            return;
        } else {
            throw new AFIntegrationTestException("Get available users to attach to database failed " + response.getData());
        }

    }


    /**
     *
     * @param applicationKey
     * @param name
     * @param rssInstance
     * @throws Exception
     */
    public void deleteUser(String applicationKey,String name,String rssInstance) throws Exception{

        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put("action", "deleteUser");
        msgBodyMap.put("applicationKey", applicationKey);
        msgBodyMap.put("name", name);
        msgBodyMap.put("rssInstance", rssInstance);

        HttpResponse response = super.doPostRequest("resources/database/users/list/ajax/list.jag", msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            return;
        } else {
            throw new AFIntegrationTestException("Create database user failed " + response.getData());
        }

    }

    /**
     *
     * @param applicationKey
     * @param password
     * @param rssInstance
     * @param username
     * @return
     * @throws Exception
     */
    public void createDatabaseUser(String applicationKey,String password,String rssInstance,String username) throws Exception{

        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put("action", "createDatabaseUser");
        msgBodyMap.put("applicationKey", applicationKey);
        msgBodyMap.put("password", password);
        msgBodyMap.put("rssInstance", rssInstance);
        msgBodyMap.put("username", username);

        HttpResponse response = super.doPostRequest("resources/database/users/list/ajax/list.jag", msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            return;
        } else {
            throw new AFIntegrationTestException("Create database user failed " + response.getData());
        }
    }

}
