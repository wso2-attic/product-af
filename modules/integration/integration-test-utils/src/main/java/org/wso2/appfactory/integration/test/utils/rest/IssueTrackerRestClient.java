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
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * REST client for issuetracker API
 */
public class IssueTrackerRestClient extends BaseClient {
    /**
     * Construct authenticates REST client to invoke appmgt functions
     *
     * @param backEndUrl backend url
     * @param username   username
     * @param password   password
     * @throws Exception
     */
    public IssueTrackerRestClient(String backEndUrl, String username, String password) throws Exception {
        super(backEndUrl, username, password);
    }

    @Override
    protected void login(String userName, String password) throws Exception {
        HttpResponse response = HttpRequestUtil.doPost(
                new URL(getBackEndUrl() + ISSUETRACKER_URL_SURFIX + ISSUETRACKER_USER_LOGIN),
                "action=login&username=" + userName + "&password=" + password, getRequestHeaders());
        JsonParser jsonParser = new JsonParser();
        String stringResponse = response.getData().toString();
        JsonElement jsonElement = jsonParser.parse(stringResponse);
        JsonObject loginResponce = jsonElement.getAsJsonObject();
        if (response.getResponseCode() == HttpStatus.SC_OK && loginResponce.get("error").toString()
                .equals("\"false\"")) {
            String session = getSession(response.getHeaders());
            if (session == null) {
                throw new AFIntegrationTestException("No session cookie found with response");
            }
            setSession(session);
        } else {
            throw new AFIntegrationTestException("Login failed " +response.getResponseCode()+" "+ response.getData());
        }
    }

    @Override
    public HttpResponse doPostRequest(String urlSuffix, Map<String, String> keyVal) throws AFIntegrationTestException {
        String postBody = generateMsgBody(keyVal);
        try {
            return HttpRequestUtil.doPost(new URL(getBackEndUrl() + ISSUETRACKER_URL_SURFIX
                            + urlSuffix), postBody,
                    getRequestHeaders());
        } catch (Exception e) {
            throw new AFIntegrationTestException(e);
        }
    }

    /**
     * Calls Save issue API
     *
     * @param projectKey projectKey
     * @param jsonString details of the issue
     * @return value of data element of the response
     * @throws Exception
     */
    public JsonObject saveIssue(String projectKey, String jsonString) throws AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put("action", "addIssue");
        msgBodyMap.put("jsonString", jsonString);
        msgBodyMap.put("projectKey", projectKey);
        HttpResponse response = doPostRequest(ISSUETRACKER_ISSUE_SAVE, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            String stringResponce = response.getData();
            int start = stringResponce.indexOf('{');
            String jsonResponse = stringResponce.substring(start, stringResponce.length());
            JsonParser jsonParser = new JsonParser();
            JsonElement jsonElement = jsonParser.parse(jsonResponse);
            JsonObject saveResponse = jsonElement.getAsJsonObject();
            return saveResponse;
        } else {
            throw new AFIntegrationTestException("Issue creating failed"+response.getResponseCode()+" "
                    + response.getData());
        }
    }

    /**
     * Calls edit issue api
     *
     * @param issueKey   issue id
     * @param jsonString details of the issue
     * @return value of success element of the response
     * @throws Exception
     */
    public JsonObject editIssue(String issueKey, String jsonString) throws AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put("action", "editIssue");
        msgBodyMap.put("jsonString", jsonString);
        msgBodyMap.put("issueKey", issueKey);
        HttpResponse response = doPostRequest(ISSUETRACKER_ISSUE_SAVE, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            String stringResponse = response.getData();
            int start = stringResponse.indexOf('{');
            String jsonResponse = stringResponse.substring(start, stringResponse.length());
            JsonParser jsonParser = new JsonParser();
            JsonElement jsonElement = jsonParser.parse(jsonResponse);
            JsonObject editResponse = jsonElement.getAsJsonObject();
            return editResponse;
        } else {
            throw new AFIntegrationTestException("Issue editing failed" +response.getResponseCode()+" "
                    + response.getData());
        }
    }

    /**
     * Calls get issue api
     *
     * @param issuePkey issuePkey
     * @param appkey    appKey
     * @return whether the application key is equal to  application key in the responce
     * @throws Exception
     */
    public JsonObject getIssue(String issuePkey, String appkey, String issueType) throws AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put("issuePkey", issuePkey);
        msgBodyMap.put("appkey", appkey);
        msgBodyMap.put("isRedirection", "isRedirection");
        HttpResponse response = doPostRequest(ISSUETRACKER_ISSUE_GET, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            String stringResponse = response.getData();
            int start = stringResponse.indexOf("{");
            String jsonResponse = stringResponse.substring(start, stringResponse.length());
            JsonParser jsonParser = new JsonParser();
            JsonElement jsonElement = jsonParser.parse(jsonResponse);
            JsonObject getResponse = jsonElement.getAsJsonObject();
            return getResponse;
        } else {
            throw new AFIntegrationTestException("Error while getting issue" +response.getResponseCode()+" "
                    + response.getData());
        }
    }

    /**
     * Calls add issue comment api
     *
     * @param issuePkey   isssue id
     * @param jasonString comment json
     * @return value of success element of the response
     * @throws Exception
     */
    public JsonObject addComment(String issuePkey, String jasonString) throws Exception {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put("issueUniqueKey", issuePkey);
        msgBodyMap.put("commentJsonString", jasonString);
        msgBodyMap.put("action", "addComment");
        String postBody = generateMsgBody(msgBodyMap);
        HttpResponse response = HttpRequestUtil.doPost(new URL(getBackEndUrl() + ISSUETRACKER_URL_COMMENT),
                postBody,
                getRequestHeaders());
        if (response.getResponseCode() == HttpStatus.SC_OK) {

            String stringResponse = response.getData();
            int start = stringResponse.indexOf('{');
            String jsonResponse = stringResponse.substring(start, stringResponse.length());
            JsonParser jsonParser = new JsonParser();
            JsonElement jsonElement = jsonParser.parse(jsonResponse);
            JsonObject addCommentResponse = jsonElement.getAsJsonObject();
            return addCommentResponse;
        } else {
            throw new AFIntegrationTestException("Comment saving failed" +response.getResponseCode()+" "
                    + response.getData());
        }
    }

    /**
     * Calls add comment to issue
     *
     * @param appKey application id
     * @return whether the application key is equal to application key in the responce
     * @throws Exception
     */
    public JsonObject getProjectDetails(String appKey) throws Exception {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put("appkey", appKey);
        msgBodyMap.put("isRedirection", "isRedirection");
        HttpResponse response = doPostRequest(ISSUETRACKER_ISSUE_ADD, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            String stringResponse = response.getData();
            int start = stringResponse.indexOf('{');
            String jsonString = stringResponse.substring(start, stringResponse.length());
            JsonParser jsonParser = new JsonParser();
            JsonElement jsonElement = jsonParser.parse(jsonString);
            JsonObject addIssueResponse = jsonElement.getAsJsonObject();
            return addIssueResponse;
        } else {
            throw new AFIntegrationTestException("Issue getting failed" +response.getResponseCode()+" "
                    + response.getData());
        }
    }

    /**
     * Calls get projet version API
     *
     * @param projectKey application id
     * @return whether the array size is lager that 0 or not
     * @throws Exception
     */
    public JsonArray getProjectVersion(String projectKey) throws AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put("projectKey", projectKey);
        msgBodyMap.put("isRedirection", "isRedirection");
        HttpResponse response = doPostRequest(ISSUETRACKER_ISSUE_GETPROJECTVERSION, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            String responseString = response.getData();
            int start = responseString.lastIndexOf('[');
            String jsonString = responseString.substring(start, responseString.length());
            JsonParser jsonParser = new JsonParser();
            JsonElement jsonElement = jsonParser.parse(jsonString);
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            return jsonArray;
        } else {
            throw new AFIntegrationTestException("Project Version getting failed" +response.getResponseCode()+" "
                    + response.getData());
        }
    }
}
