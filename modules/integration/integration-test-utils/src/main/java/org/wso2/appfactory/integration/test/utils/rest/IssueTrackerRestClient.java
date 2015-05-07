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
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import org.apache.commons.httpclient.HttpStatus;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestException;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

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
        JsonElement  jsonElement =jsonParser.parse(stringResponse);
        JsonObject loginResponce = jsonElement.getAsJsonObject();
        if (response.getResponseCode() == HttpStatus.SC_OK && loginResponce.get("error").toString().equals("\"false\"")) {
            String session = getSession(response.getHeaders());
            if (session == null) {
                throw new AFIntegrationTestException("No session cookie found with response");
            }
            setSession(session);
        } else {
            throw new AFIntegrationTestException("Login failed " + response.getData());
        }
    }

    @Override
    public HttpResponse doPostRequest(String urlSuffix, Map<String, String> keyVal) throws Exception {
        String postBody = generateMsgBody(keyVal);
        return HttpRequestUtil.doPost(new URL(getBackEndUrl() + ISSUETRACKER_URL_SURFIX
                        + urlSuffix),postBody,
                getRequestHeaders());
    }

    /**
     * Calls Save issue API
     *
     * @param projectKey projectKey
     * @param jsonString details of the issue
     * @param action action
     * @return value of data element of the response
     * @throws Exception
     */
    public String saveIssue(String projectKey, String jsonString, String action) throws Exception {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put("action",action);
        msgBodyMap.put("jsonString",jsonString);
        msgBodyMap.put("projectKey",projectKey);
        HttpResponse response = doPostRequest(ISSUETRACKER_ISSUE_SAVE,msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {

            String stringResponce  = response.getData();
            try{
                int start =    stringResponce.indexOf('{');
                String jsonResponse = stringResponce.substring(start,stringResponce.length());
                JsonParser jsonParser = new JsonParser();
                JsonElement  jsonElement =jsonParser.parse(jsonResponse);
                JsonObject saveResponse = jsonElement.getAsJsonObject();
                return saveResponse.get("data").toString().replace("\"","");
            }catch (Exception e){
                return  null;
            }
        } else {
            throw new AFIntegrationTestException("" + response.getResponseCode() + response.getData());
        }
    }

    /**
     * Calls edit issue api
     *
     * @param issueKey issue id
     * @param jsonString details of the issue
     * @param action action
     * @return value of success element of the response
     * @throws Exception
     */
    public boolean editIssue(String issueKey,String jsonString,String action) throws Exception {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put("action",action);
        msgBodyMap.put("jsonString",jsonString);
        msgBodyMap.put("issueKey",issueKey);
        HttpResponse response = doPostRequest(ISSUETRACKER_ISSUE_SAVE,msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            try{
                String stringResponse = response.getData();
                int start = stringResponse.indexOf('{');
                String jsonResponse = stringResponse.substring(start,stringResponse.length());
                JsonParser jsonParser = new JsonParser();
                JsonElement  jsonElement =jsonParser.parse(jsonResponse);
                JsonObject editResponse = jsonElement.getAsJsonObject();
                return editResponse.getAsJsonObject("data").getAsJsonObject("responseBean")
                        .get("success").getAsBoolean();
            }catch (Exception e){
                 return  false;
            }
        } else {
            throw new AFIntegrationTestException("" + response.getResponseCode() + response.getData());
        }
    }

    /**
     * Calls get issue api
     *
     * @param issuePkey issuePkey
     * @param appkey appKey
     * @return whether the application key is equal to  application key in the responce
     * @throws Exception
     */
    public boolean getIssue(String issuePkey,String appkey) throws Exception {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put("issuePkey",issuePkey);
        msgBodyMap.put("appkey",appkey);
        msgBodyMap.put("isRedirection","isRedirection");
        HttpResponse response = doPostRequest(ISSUETRACKER_ISSUE_GET,msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            try{
                String stringResponse = response.getData();
                int start = stringResponse.indexOf("{");
                String jsonResponse = stringResponse.substring(start,stringResponse.length());
                JsonParser jsonParser = new JsonParser();
                JsonElement  jsonElement =jsonParser.parse(jsonResponse);
                JsonObject editResponse = jsonElement.getAsJsonObject();
                return editResponse.getAsJsonObject().get("projectKey").getAsString().replace("\"", "").equals(appkey);
            }catch (Exception e){
                return  false;
            }
        } else {
            throw new AFIntegrationTestException("" + response.getResponseCode() + response.getData());
        }
    }

    /**
     * Calls add issue comment api
     *
     * @param issuePkey isssue id
     * @param jasonString  comment json
     * @param  action actionName
     * @return value of success element of the response
     * @throws Exception
     */
    public boolean addComment(String issuePkey,String jasonString,String action) throws Exception {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put("issueUniqueKey",issuePkey);
        msgBodyMap.put("commentJsonString",jasonString);
        msgBodyMap.put("action",action);
        String postBody = generateMsgBody(msgBodyMap);
        HttpResponse response = HttpRequestUtil.doPost(new URL(getBackEndUrl() + ISSUETRACKER_URL_COMMENT),
                postBody,
                getRequestHeaders());
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            try{
                String stringResponse = response.getData();
                int start = stringResponse.indexOf('{');
                String jsonResponse = stringResponse.substring(start,stringResponse.length());
                JsonParser jsonParser = new JsonParser();
                JsonElement  jsonElement =jsonParser.parse(jsonResponse);
                JsonObject editResponse = jsonElement.getAsJsonObject();
                return editResponse.getAsJsonObject("data").getAsJsonObject("responseBean")
                        .get("success").getAsBoolean();
            }catch (Exception e){
                return  false;
            }
        } else {
            throw new AFIntegrationTestException("" + response.getResponseCode() + response.getData());
        }
    }

    /**
     * Calls add comment to issue
     *
     * @param appKey application id
     * @return whether the application key is equal to application key in the responce
     * @throws Exception
     */
    public boolean addIssue(String appKey) throws Exception {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put("appkey",appKey);
        msgBodyMap.put("isRedirection","isRedirection");
        HttpResponse response =   doPostRequest(ISSUETRACKER_ISSUE_ADD, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            String  stringResponse = response.getData();
            int start = stringResponse.indexOf('{');
            String jsonString = stringResponse.substring(start,stringResponse.length());
            try {
                JsonParser jsonParser = new JsonParser();
                JsonElement jsonElement = jsonParser.parse(jsonString);
                JsonObject addIssueResponse = jsonElement.getAsJsonObject();
                String responseKey = addIssueResponse.getAsJsonArray("project").get(0).getAsJsonObject().get("key").getAsString();
                return responseKey.equals(appKey);
            }catch (Exception e){
                return  false;
            }
        } else {
            throw new AFIntegrationTestException("" + response.getResponseCode() + response.getData());
        }
    }

    /**
     * Calls get projet version API
     *
     * @param projectKey application id
     * @return whether the array size is lager that 0 or not
     * @throws Exception
     */
    public boolean getProjectVersion(String projectKey) throws Exception {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put("projectKey",projectKey);
        msgBodyMap.put("isRedirection","isRedirection");
        HttpResponse response =   doPostRequest(ISSUETRACKER_ISSUE_GETPROJECTVERSION,msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            try {
                String responseString = response.getData();
                int start = responseString.lastIndexOf('[');
                String jsonString = responseString.substring(start,responseString.length());
                JsonParser jsonParser = new JsonParser();
                JsonElement jsonElement = jsonParser.parse(jsonString);
                JsonArray jsonArray = jsonElement.getAsJsonArray();
                if(jsonArray.size() > 0){
                    return true;
                }else{
                    return  false;
                }
            }catch (Exception e){
                return  false;
            }
        } else {
            throw new AFIntegrationTestException("" + response.getResponseCode() + response.getData());
        }
    }


}
