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
import org.json.JSONArray;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestException;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * User management client
 */
public class UserMgtClient extends BaseClient {

    //Endpoints
    public static final String EP_GET_USERS = "application/user/get/ajax/list.jag";
    public static final String EP_APP_ADD_USERS = "application/user/add/ajax/add.jag";
    public static final String EP_APP_UPDATE_USERS = "application/user/update/ajax/update.jag";

    // Request actions
    public static final String ACTON_GET_USERS_OF_APPLICATION = "getUsersOfApplication";
    public static final String ACTION_INVITE_USER_TO_APPLICATION = "inviteUserToApplication";
    public static final String ACTION_REMOVE_USER_FROM_APPLICATION = "removeUserFromApplication";

    // Other Request parameters
    private static final String REQUEST_KEY_ACTION = "action";
    public static final String REQUEST_KEY_USERS = "users";
    private static final String REQUEST_KEY_APPKEY = "applicationKey";


    public UserMgtClient(String backEndUrl, String username, String password) throws Exception {
        super(backEndUrl, username, password);
    }

    /**
     * Get users of application
     *
     * @param appKey application key
     * @return JSONArray of users
     * Sample
     * [
     *  {
     *      "userName": "devUser",
     *      "firstName": null,
     *      "lastName": "devUser",
     *      "email": "devUser",
     *      "roles": ["developer"],
     *      "displayName": " devUser",
     *      "displayRoles": ["Developer"]
     *  },
     *  {
     *      "userName": "allUser",
     *      "firstName": null,
     *      "lastName": "allUser",
     *      "email": "allUser",
     *      "roles": ["devops","qa","cxo","appowner","developer"],
     *      "displayName": " allUser","displayRoles": ["DevOps","QA","CXO","Application Owner","Developer"]
     *  }
     * ]
     * @throws AFIntegrationTestException
     */
    public JSONArray getUsersOfApplication(String appKey) throws Exception {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(REQUEST_KEY_ACTION, ACTON_GET_USERS_OF_APPLICATION);
        msgBodyMap.put(REQUEST_KEY_APPKEY, appKey);
        HttpResponse response = super.doPostRequest(EP_GET_USERS, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            JSONArray jsonObject = new JSONArray(response.getData());
            return jsonObject;
        } else {
            throw new AFIntegrationTestException("getUsersOfApplication failed. HTTP status: " + response
                    .getResponseMessage() + ", Response message: " + response.getResponseMessage());
        }
    }


    /**
     * Invite users to the application
     *
     * @param appKey    application key
     * @param userNames user names as comma separated values. ex: "user1,user2"
     * @return success or not
     * @throws AFIntegrationTestException if an error occurred
     */
    public boolean inviteUsersToApplication(String appKey, String userNames) throws Exception {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(REQUEST_KEY_ACTION, ACTION_INVITE_USER_TO_APPLICATION);
        msgBodyMap.put(REQUEST_KEY_APPKEY, appKey);
        msgBodyMap.put(REQUEST_KEY_USERS, userNames);
        HttpResponse response = super.doPostRequest(EP_APP_ADD_USERS, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            return Boolean.valueOf(response.getData());
        } else {
            throw new AFIntegrationTestException("inviteUserToApplication failed. HTTP status: " + response
                    .getResponseMessage() + ", Response message: " + response.getResponseMessage());
        }
    }


    /**
     * Remove users from the application
     *
     * @param appKey    application key
     * @param userNames user names as comma separated values. ex: "user1,user2"
     * @return success or not
     * @throws AFIntegrationTestException if an error occurred
     */
    public boolean removeUsersFromApplication(String appKey, String userNames) throws Exception {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(REQUEST_KEY_ACTION, ACTION_REMOVE_USER_FROM_APPLICATION);
        msgBodyMap.put(REQUEST_KEY_APPKEY, appKey);
        msgBodyMap.put(REQUEST_KEY_USERS, userNames);
        HttpResponse response = super.doPostRequest(EP_APP_UPDATE_USERS, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            return Boolean.valueOf(response.getData());
        } else {
            throw new AFIntegrationTestException("removeUserFromApplication failed. HTTP status: " + response
                    .getResponseMessage() + ", Response message: " + response.getResponseMessage());
        }
    }

}
