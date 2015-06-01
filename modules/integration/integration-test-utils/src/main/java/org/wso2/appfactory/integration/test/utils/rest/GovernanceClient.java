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
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestException;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;

import java.util.HashMap;
import java.util.Map;
/**
 * Rest Client for the Governance test case
 *
 * @throws AFIntegrationTestException
 */
public class GovernanceClient extends BaseClient {

    /**
     * Construct authenticates REST client to invoke appmgt functions
     *
     * @param backEndUrl backend url
     * @param username   username
     * @param password   password
     * @throws Exception
     */
    public GovernanceClient(String backEndUrl, String username, String password) throws Exception {
        super(backEndUrl, username, password);
    }


    /**
     * Test to promote an application's version
     *
     * @param appKey    application Key
     * @param stageName stage name
     * @param version   version
     * @param tagName   tag name
     * @param comment   comment given when promoting
     * @param userName  username of the promoter
     * @throws Exception
     */
    public JsonObject promote(String appKey, String stageName, String version, String tagName, String comment,
                              String userName, JSONArray checkItems) throws AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put("action", "Promote");
        msgBodyMap.put("applicationKey", appKey);
        msgBodyMap.put("stageName", stageName);
        msgBodyMap.put("version", version);
        msgBodyMap.put("tagName", tagName);
        msgBodyMap.put("comment", comment);
        msgBodyMap.put("userName", userName);
        msgBodyMap.put("checkItems", checkItems.toString());

        HttpResponse response = super.doPostRequest(APPMGT_LIFECYCLE_ADD, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            String responseString = response.getData();
            JsonParser jsonParser = new JsonParser();
            JsonElement jsonElement = jsonParser.parse(responseString);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            return jsonObject;

        } else {
            throw new AFIntegrationTestException(
                    "Application Promotion failed " + response.getResponseCode() + response.getData());
        }
    }

    /**
     * Test to demote an application's version
     *
     * @param appKey    application Key
     * @param stageName stage name
     * @param version   version
     * @param tagName   tag name
     * @param comment   comment given when demoting
     * @param userName  username of the demoter
     * @throws AFIntegrationTestException
     */
    public JsonObject demote(String appKey, String stageName, String version, String tagName, String comment,
                             String userName, JSONArray checkItems) throws AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put("action", "Demote");
        msgBodyMap.put("applicationKey", appKey);
        msgBodyMap.put("stageName", stageName);
        msgBodyMap.put("version", version);
        msgBodyMap.put("tagName", tagName);
        msgBodyMap.put("comment", comment);
        msgBodyMap.put("userName", userName);
        msgBodyMap.put("checkItems", checkItems.toString());

        HttpResponse response = super.doPostRequest(APPMGT_LIFECYCLE_ADD, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            String responseString = response.getData();
            JsonParser jsonParser = new JsonParser();
            JsonElement jsonElement = jsonParser.parse(responseString);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            return jsonObject;
        } else {
            throw new AFIntegrationTestException("Application Demote failed " + response.getData());
        }
    }

    /**
     * Test to retire an application's version in production stage
     *
     * @param appKey    application Key
     * @param stageName stage name
     * @param version   version
     * @param tagName   tag name
     * @param comment   comment given when retiring
     * @param userName  username of the person invoking retire
     * @throws AFIntegrationTestException
     */
    public JsonObject retire(String appKey, String stageName, String version, String tagName, String comment,
                             String userName, JSONArray checkItems) throws AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put("action", "Retire");
        msgBodyMap.put("applicationKey", appKey);
        msgBodyMap.put("stageName", stageName);
        msgBodyMap.put("version", version);
        msgBodyMap.put("tagName", tagName);
        msgBodyMap.put("comment", comment);
        msgBodyMap.put("userName", userName);
        msgBodyMap.put("checkItems", checkItems.toString());

        HttpResponse response = super.doPostRequest(APPMGT_LIFECYCLE_ADD, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            String responseString = response.getData();
            JsonParser jsonParser = new JsonParser();
            JsonElement jsonElement = jsonParser.parse(responseString);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            return jsonObject;
        } else {
            throw new AFIntegrationTestException("Application Retire failed " + response.getData());
        }
    }

    /**
     * Test to create an artifact
     *
     * @param appKey   application key
     * @param version  version
     * @param revision revision
     * @param stage    stage name
     * @param doDeploy should deploy or not
     * @param tagName  tag name
     * @param repoFrom whether the repo is original or fork
     * @throws AFIntegrationTestException
     */
    public JsonObject createArtifact(String appKey, String version, String revision, String stage, String doDeploy,
                                     String tagName, String repoFrom, String userName) throws
            AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put("action", "createArtifact");
        msgBodyMap.put("version", version);
        msgBodyMap.put("applicationKey", appKey);
        msgBodyMap.put("revision", revision);
        msgBodyMap.put("stage", stage);
        msgBodyMap.put("doDeploy", doDeploy);
        msgBodyMap.put("tagName", tagName);
        msgBodyMap.put("repoFrom", repoFrom);

        HttpResponse response = super.doPostRequest(APPMGT_LIFECYCLE_ADD, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            return getAppVersionsInStagesWithLifeCycleInfo(appKey, userName);

        } else {
            throw new AFIntegrationTestException("Artifact Creation Failed " + response.getData());
        }
    }

    /**
     * Test to update to lifecycle checklist
     *
     * @param appKey  application key
     * @param version application version
     * @param stage   stage which application resides in
     * @throws AFIntegrationTestException
     */
    public JsonObject invokeUpdateLifeCycleCheckList(String appKey, String version, String stage, JSONArray checkItems,
                                                     String userName) throws AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put("action", "invokeUpdateLifeCycleCheckList");
        msgBodyMap.put("applicationKey", appKey);
        msgBodyMap.put("version", version);
        msgBodyMap.put("stage", stage);
        msgBodyMap.put("checkItems", checkItems.toString());

        HttpResponse response = super.doPostRequest(APPMGT_LIFECYCLE_ADD, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            return getAppVersionsInStagesWithLifeCycleInfo(appKey, userName);

        } else {
            throw new AFIntegrationTestException("Invoking Update Lifecycle Checklist Failed " + response.getData());
        }
    }

    /**
     * Test to check on an check list item
     *
     * @param appKey    application key
     * @param stageName stage name
     * @param version   application version
     * @param itemName  check list item name
     * @param checked   whether checked or not
     * @return
     * @throws AFIntegrationTestException
     */
    public String itemChecked(String appKey, String stageName, String version, String itemName, String checked) throws
            AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put("action", "itemChecked");
        msgBodyMap.put("applicationKey", appKey);
        msgBodyMap.put("stageName", stageName);
        msgBodyMap.put("version", version);
        msgBodyMap.put("itemName", itemName);
        msgBodyMap.put("checked", checked);

        HttpResponse response = super.doPostRequest(APPMGT_LIFECYCLE_ADD, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            return response.getData();
        } else {
            throw new AFIntegrationTestException("Checklist item checking Failed" + response.getData());
        }
    }

    /**
     * Test to get application versions in the stages with life cycle info
     *
     * @param appKey   application key
     * @param userName username
     * @return
     * @throws AFIntegrationTestException
     */

    public JsonObject getAppVersionsInStagesWithLifeCycleInfo(String appKey, String userName) throws
            AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put("action", "getAppVersionsInStagesWithLifeCycleInfo");
        msgBodyMap.put("applicationKey", appKey);
        msgBodyMap.put("userName", userName);

        HttpResponse response = super.doPostRequest(APPMGT_LIFECYCLE_ADD, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            String jsonString = response.getData();
            JsonParser jsonParser = new JsonParser();
            JsonElement jsonElement = jsonParser.parse(jsonString);
            JsonObject addIssueResponse = jsonElement.getAsJsonObject();
            return addIssueResponse;
        } else {
            throw new AFIntegrationTestException("Checklist item checking Failed" + response.getData());
        }
    }

    /**
     * Test to get the life cycle history for the application
     *
     * @param appKey    application key
     * @param version   application version
     * @param stageName stage name
     * @param userName  username
     * @return
     * @throws AFIntegrationTestException
     */

    public String getLifeCycleHistoryForApplication(String appKey, String version, String stageName,
                                                    String userName) throws AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put("action", "getLifeCycleHistoryForApplication");
        msgBodyMap.put("applicationKey", appKey);
        msgBodyMap.put("version", version);
        msgBodyMap.put("stageName", stageName);
        msgBodyMap.put("userName", userName);
        HttpResponse response = super.doPostRequest(APPMGT_LIFECYCLE_GET, msgBodyMap);

        if (response.getResponseCode() == HttpStatus.SC_OK) {
//                String jsonString = response.getData();
//                JsonParser jsonParser = new JsonParser();
//                JsonElement jsonElement = jsonParser.parse(jsonString);
//                JsonObject addIssueResponse = jsonElement.getAsJsonObject();
            return response.getData();


        } else {
            throw new AFIntegrationTestException(
                    "Getting LifeCycle History for Application Failed" + response.getData());
        }
    }

    /**
     * Test to check the version creation
     *
     * @param appKey        application Key
     * @param srcVersion    source version name
     * @param targetVersion target version name
     * @param lifecycleName name of the lifecycle to use
     * @throws AFIntegrationTestException
     */
    public JsonObject invokeDoVersion(String appKey, String srcVersion, String targetVersion, String lifecycleName,
                                      String userName)
            throws AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put("action", "invokeDoVersion");
        msgBodyMap.put("applicationKey", appKey);
        msgBodyMap.put("srcVersion", srcVersion);
        msgBodyMap.put("targetVersion", targetVersion);
        msgBodyMap.put("lifecycleName", lifecycleName);
        HttpResponse response = doPostRequest(APPMGT_LIFECYCLE_ADD, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            return getAppVersionsInStagesWithLifeCycleInfo(appKey, userName);

        } else {
            throw new AFIntegrationTestException("Creating a version failed " + response.getData());
        }
    }

    /**
     * Test to copy new dependencies and deploy artifacts when promoting an application
     *
     * @param appKey       application key
     * @param stage        stage name
     * @param version      application version
     * @param tagName      tag name
     * @param deployAction deployaction
     * @throws AFIntegrationTestException
     */
    public JsonObject copyNewDependenciesAndDeployArtifact(String appKey, String stage, String version, String tagName,
                                                           String deployAction, String userName) throws
            AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put("action", "copyNewDependenciesAndDeployArtifact");
        msgBodyMap.put("applicationKey", appKey);
        msgBodyMap.put("stage", stage);
        msgBodyMap.put("version", version);
        msgBodyMap.put("tagName", tagName);
        msgBodyMap.put("deployAction", deployAction);
        HttpResponse response = doPostRequest(APPMGT_LIFECYCLE_ADD, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            return getAppVersionsInStagesWithLifeCycleInfo(appKey, userName);
        } else {
            throw new AFIntegrationTestException("Copying New Dependencies and deploy artifact " + response.getData());
        }
    }

    /**
     * Test to upload new versions of existing application
     *
     * @param existingVersion existing app version
     * @param lifecycleName   name of the lifecycle to use
     * @param appKey          application Key
     * @param userName        username
     * @throws AFIntegrationTestException
     */
    public void uploadNewVersionOfExistingApp(String existingVersion, String lifecycleName, String appKey,
                                              String userName) throws AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put("action", "uploadNewVersionOfExistingApp");
        msgBodyMap.put("applicationKey", appKey);
        msgBodyMap.put("existingVersion", existingVersion);
        msgBodyMap.put("lifecycleName", lifecycleName);
        msgBodyMap.put("userName", userName);
        HttpResponse response = doPostRequest(APPMGT_LIFECYCLE_ADD, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            return;
        } else {
            throw new AFIntegrationTestException(
                    "Uploading new version of an existing app is failed " + response.getData());
        }
    }


}
