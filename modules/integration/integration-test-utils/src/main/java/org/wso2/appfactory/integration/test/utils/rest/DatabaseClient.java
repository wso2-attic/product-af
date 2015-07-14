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
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestException;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Database related stub
 */
public class DatabaseClient extends BaseClient {

    private static final String REQUEST_KEY_ACTION = "action";
    private static final String APP_KEY = "applicationKey";

    public DatabaseClient(String backEndUrl, String username, String password) throws Exception {
        super(backEndUrl, username, password);
    }

    /**
     * Create database and attach user
     *
     * @param applicationKey
     * @param dbName
     * @param dbServerInstance
     * @param customPassword
     * @param dbDescription
     * @param isBasic
     * @param copyToAll
     * @param createDatasource
     * @param userName
     * @param templateName
     * @throws AFIntegrationTestException
     */
    public void createDatabaseAndAttachUser(String applicationKey, String dbName, String dbServerInstance,
                                            String customPassword, String dbDescription, String isBasic,
                                            String copyToAll, String createDatasource, String userName,
                                            String templateName, String dbSuffix) throws AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(REQUEST_KEY_ACTION, "createDatabaseAndAttachUser");
        msgBodyMap.put(APP_KEY, applicationKey);
        msgBodyMap.put("databaseName", dbName + dbSuffix);
        msgBodyMap.put("databaseServerInstanceName", dbServerInstance);
        msgBodyMap.put("customPassword", customPassword);
        msgBodyMap.put("databaseDescription", dbDescription);
        msgBodyMap.put("isBasic", isBasic);
        msgBodyMap.put("copyToAll", copyToAll);
        msgBodyMap.put("createDatasource", createDatasource);
        msgBodyMap.put("userName", userName);
        msgBodyMap.put("templateName", templateName);
        HttpResponse response = super.doPostRequest(BaseClient.APPMGT_DATABASE_ADD, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            return;
        } else {
            throw new AFIntegrationTestException(
                    "Error occurred while creating database " + response.getResponseCode() + response.getData());
        }
    }

    /**
     * Get Databases for the application key
     *
     * @param applicationKey
     * @return
     * @throws Exception
     */
    public JsonArray getDatabases(String applicationKey) throws AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(REQUEST_KEY_ACTION, "getDatabases");
        msgBodyMap.put(APP_KEY, applicationKey);
        HttpResponse response = super.doPostRequest(BaseClient.APPMGT_DATABASE_ADD, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            JsonParser jsonParser = new JsonParser();
            JsonElement jsonElement = jsonParser.parse(response.getData());
            return jsonElement.getAsJsonArray();
        } else {
            throw new AFIntegrationTestException("Error while getting databases " + response.getResponseCode()
                                                         + response.getData());
        }
    }

    /**
     * Get database user template information for stages
     *
     * @param applicationKey
     * @return
     * @throws AFIntegrationTestException
     */
    public JsonArray getDbUserTemplateInfoForStages(String applicationKey) throws AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(REQUEST_KEY_ACTION, "getDbUserTemplateInfoForStages");
        msgBodyMap.put(APP_KEY, applicationKey);
        HttpResponse response = super.doPostRequest(BaseClient.APPMGT_DATABASE_ADD, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            JsonParser jsonParser = new JsonParser();
            JsonElement jsonElement = jsonParser.parse(response.getData());
            return jsonElement.getAsJsonArray();
        } else {
            throw new AFIntegrationTestException("Error occurred while retrieving database user template info for " +
                                                         "stages " + response.getResponseCode() + response.getData());
        }
    }

    /**
     * Get creatable RSS Instance
     *
     * @param applicationKey
     * @return
     * @throws Exception
     */
    public JsonArray getCreatableRSSinstances(String applicationKey) throws AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(REQUEST_KEY_ACTION, "getCreatableRSSinstances");
        msgBodyMap.put(APP_KEY, applicationKey);
        HttpResponse response = super.doPostRequest(BaseClient.APPMGT_DATABASE_ADD, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            JsonParser jsonParser = new JsonParser();
            JsonElement jsonElement = jsonParser.parse(response.getData());
            return jsonElement.getAsJsonArray();
        } else {
            throw new AFIntegrationTestException(
                    "Error occurred while retrieving creatable RSS instance  " + response.getData());
        }
    }

    /**
     * Get all RSS Instance
     *
     * @param applicationKey
     * @return
     * @throws Exception
     */
    public JsonArray getRSSinstances(String applicationKey) throws AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(REQUEST_KEY_ACTION, "getRSSinstances");
        msgBodyMap.put(APP_KEY, applicationKey);
        HttpResponse response = super.doPostRequest(BaseClient.APPMGT_DATABASE_ADD, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            JsonParser jsonParser = new JsonParser();
            JsonElement jsonElement = jsonParser.parse(response.getData());
            return jsonElement.getAsJsonArray();
        } else {
            throw new AFIntegrationTestException(
                    "Error occurred while retrieving RSS instance " + response.getResponseCode() + response.getData());
        }
    }

    /**
     * Get all the attach users in database
     *
     * @param applicationKey
     * @param databaseName
     * @param stage
     * @return
     * @throws Exception
     */
    public JsonArray getAttachedUsers(String applicationKey, String databaseName, String stage) throws
            AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(REQUEST_KEY_ACTION, "getAttachedUsers");
        msgBodyMap.put(APP_KEY, applicationKey);
        msgBodyMap.put("dbname", databaseName);
        msgBodyMap.put("rssInstance", stage);
        HttpResponse response = super.doPostRequest(BaseClient.APPMGT_DATABASE_ADD, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            JsonParser jsonParser = new JsonParser();
            JsonElement jsonElement = jsonParser.parse(response.getData());
            return jsonElement.getAsJsonArray();
        } else {
            throw new AFIntegrationTestException(
                    "Error occurred while retrieving attached users " + response.getResponseCode()
                            + response.getData());
        }
    }

    /**
     * Attach new user to the database
     *
     * @param applicationKey
     * @param databaseName
     * @param stage
     * @param users
     * @param templates
     * @throws Exception
     */
    public void attachNewUser(String applicationKey, String databaseName, String stage, String users, String templates)
            throws AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(REQUEST_KEY_ACTION, "attachNewUser");
        msgBodyMap.put(APP_KEY, applicationKey);
        msgBodyMap.put("databaseName", databaseName);
        msgBodyMap.put("dbServerInstanceName", stage);
        msgBodyMap.put("users", users);
        msgBodyMap.put("templates", templates);
        HttpResponse response = super.doPostRequest(BaseClient.APPMGT_DATABASE_ADD, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            return;
        } else {
            throw new AFIntegrationTestException(
                    "Error occurred while attaching new user " + response.getResponseCode() + response.getData());
        }
    }

    /**
     * Detach user from database
     *
     * @param applicationKey
     * @param databaseName
     * @param stage
     * @param username
     * @throws Exception
     */
    public void detachUser(String applicationKey, String databaseName, String stage, String username)
            throws AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(REQUEST_KEY_ACTION, "detachUser");
        msgBodyMap.put(APP_KEY, applicationKey);
        msgBodyMap.put("databaseName", databaseName);
        msgBodyMap.put("dbServerInstanceName", stage);
        msgBodyMap.put("username", username);
        HttpResponse response = super.doPostRequest(BaseClient.APPMGT_DATABASE_ADD, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            return;
        } else {
            throw new AFIntegrationTestException("Error occurred while detaching user " + response.getResponseCode() +
                                                         response.getData());
        }
    }

    /**
     * Get user privileges
     *
     * @param applicationKey
     * @param databaseName
     * @param stage
     * @param username
     * @return
     * @throws Exception
     */
    public JsonArray getUserPrivileges(String applicationKey, String databaseName, String stage, String username)
            throws AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(REQUEST_KEY_ACTION, "getUserPrivileges");
        msgBodyMap.put(APP_KEY, applicationKey);
        msgBodyMap.put("dbname", databaseName);
        msgBodyMap.put("rssInstanceName", stage);
        msgBodyMap.put("username", username);
        HttpResponse response = super.doPostRequest(BaseClient.APPMGT_DATABASE_ADD, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            JsonParser jsonParser = new JsonParser();
            JsonElement jsonElement = jsonParser.parse(response.getData());
            return jsonElement.getAsJsonArray();
        } else {
            throw new AFIntegrationTestException("Error occurred while retrieving user privileges " + response
                    .getResponseCode() + response.getData());
        }
    }

    /**
     * Get all databases information
     *
     * @param applicationKey
     * @return
     * @throws AFIntegrationTestException
     */
    public JsonObject getAllDatabasesInfo(String applicationKey) throws AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(REQUEST_KEY_ACTION, "getAllDatabasesInfo");
        msgBodyMap.put(APP_KEY, applicationKey);
        HttpResponse response = super.doPostRequest(BaseClient.APPMGT_DATABASE_ADD, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            JsonParser jsonParser = new JsonParser();
            return jsonParser.parse(response.getData()).getAsJsonObject();
        } else {
            throw new AFIntegrationTestException("Error occurred while retrieving all database info " + response
                    .getResponseCode() + response.getData());
        }
    }

    /**
     * Drop database
     *
     * @param applicationKey
     * @param databaseName
     * @param rssInstanceName
     * @param deleteDatasource
     * @throws Exception
     */
    public void dropDatabase(String applicationKey, String databaseName, String rssInstanceName,
                             String deleteDatasource) throws AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(REQUEST_KEY_ACTION, "dropDatabase");
        msgBodyMap.put(APP_KEY, applicationKey);
        msgBodyMap.put("databaseName", databaseName);
        msgBodyMap.put("rssInstanceName", rssInstanceName);
        msgBodyMap.put("deleteDatasource", deleteDatasource);
        HttpResponse response = super.doPostRequest(BaseClient.APPMGT_DATABASE_DROP, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            return;
        } else {
            throw new AFIntegrationTestException(
                    "Error occurred while deleting database " + response.getResponseCode() + response.getData());
        }
    }

    /**
     * Create new template
     *
     * @param applicationKey
     * @param templateName
     * @param environment
     * @param permissions
     * @throws AFIntegrationTestException
     */
    public void createTemplates(String applicationKey, String templateName, String environment,
                                Map<String, String> permissions) throws
            AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(REQUEST_KEY_ACTION, "createTemplate");
        msgBodyMap.put(APP_KEY, applicationKey);
        msgBodyMap.put("templateName", templateName);
        msgBodyMap.put("environment", environment);
        msgBodyMap.putAll(permissions);
        HttpResponse response = super.doPostRequest(BaseClient.APPMGT_DATABASE_TEMPLATE, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            return;
        } else {
            throw new AFIntegrationTestException("Error occurred while retrieving templates " + response.getResponseCode
                    () + response.getData());
        }
    }

    /**
     * Get available templates in database
     *
     * @param applicationKey
     * @return
     * @throws Exception
     */
    public String getAvailableTemplatesToAttachToDatabase(String applicationKey, String rssInstance) throws
            AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(REQUEST_KEY_ACTION, "getTemplates");
        msgBodyMap.put(APP_KEY, applicationKey);
        msgBodyMap.put("rssInstance", rssInstance);
        HttpResponse response = super.doPostRequest(BaseClient.APPMGT_DATABASE_TEMPLATE, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            return response.getData();
        } else {
            throw new AFIntegrationTestException("Error occurred while retrieving templates " +
                                                         response.getResponseCode() + response.getData());
        }
    }

    /**
     * Get Databases Users in RSS Instance
     *
     * @param applicationKey
     * @param rssInstance
     * @return
     * @throws Exception
     */
    public JsonArray getDatabaseUsersForRssInstance(String applicationKey, String rssInstance)
            throws AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(REQUEST_KEY_ACTION, "getDatabaseUsersForRssInstance");
        msgBodyMap.put(APP_KEY, applicationKey);
        msgBodyMap.put("rssInstance", rssInstance);
        HttpResponse response = super.doPostRequest(BaseClient.APPMGT_DATABASE_USER, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            JsonParser jsonParser = new JsonParser();
            JsonElement jsonElement = jsonParser.parse(response.getData());
            return jsonElement.getAsJsonArray();
        } else {
            throw new AFIntegrationTestException("Error occurred while retrieving database user for RSS Instance " +
                                                         response.getResponseCode() + response.getData());
        }
    }

    /**
     * Get available users to attach to database
     *
     * @param applicationKey
     * @param databaseName
     * @param databaseServerInstanceName
     * @throws Exception
     */
    public JsonArray getAvailableUsersToAttachToDatabase(String applicationKey, String databaseName,
                                                         String databaseServerInstanceName) throws
            AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(REQUEST_KEY_ACTION, "getAvailableUsersToAttachToDatabase");
        msgBodyMap.put(APP_KEY, applicationKey);
        msgBodyMap.put("databaseName", databaseName);
        msgBodyMap.put("dbServerInstanceName", databaseServerInstanceName);
        HttpResponse response = super.doPostRequest(BaseClient.APPMGT_DATABASE_USER, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            JsonParser jsonParser = new JsonParser();
            JsonElement jsonElement = jsonParser.parse(response.getData());
            return jsonElement.getAsJsonArray();
        } else {
            throw new AFIntegrationTestException("Error occurred while retrieving available users attached in " +
                                                         "database " + response.getResponseCode() + response.getData());
        }
    }

    /**
     * Delete database user
     *
     * @param applicationKey
     * @param name
     * @param rssInstance
     * @throws Exception
     */
    public void deleteUser(String applicationKey, String name, String rssInstance) throws AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(REQUEST_KEY_ACTION, "deleteUser");
        msgBodyMap.put(APP_KEY, applicationKey);
        msgBodyMap.put("name", name);
        msgBodyMap.put("rssInstanceName", rssInstance);
        HttpResponse response = super.doPostRequest(BaseClient.APPMGT_DATABASE_USER, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            return;
        } else {
            throw new AFIntegrationTestException("Error occurred while deleting database user " + response
                    .getResponseCode() + response.getData());
        }
    }

    /**
     * Create database user
     *
     * @param applicationKey
     * @param password
     * @param rssInstance
     * @param username
     * @return
     * @throws Exception
     */
    public void createDatabaseUser(String applicationKey, String password, String rssInstance, String username) throws
            AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(REQUEST_KEY_ACTION, "createDatabaseUser");
        msgBodyMap.put(APP_KEY, applicationKey);
        msgBodyMap.put("password", password);
        msgBodyMap.put("rssInstance", rssInstance);
        msgBodyMap.put("username", username);
        HttpResponse response = super.doPostRequest(BaseClient.APPMGT_DATABASE_USER, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            return;
        } else {
            throw new AFIntegrationTestException(
                    "Error occurred while creating database user " + response.getResponseCode() +
                            response.getData());
        }
    }
}
