package org.wso2.appfactory.integration.test.utils.rest;

import org.apache.commons.httpclient.HttpStatus;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestException;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * User management client
 */
public class UserMgtClient extends BaseRestClient {

    //Endpoints
    public static final String EP_GET_USERS = "application/users/get/ajax/list.jag";
    public static final String EP_APP_ADD_USERS = "application/users/add/ajax/add.jag";
    public static final String EP_APP_UPDATE_USERS = "application/users/update/ajax/update.jag";

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
     * @return
     * @throws Exception
     */
    public String getUsersOfApplication(String appKey) throws Exception {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(REQUEST_KEY_ACTION, ACTON_GET_USERS_OF_APPLICATION);
        msgBodyMap.put(REQUEST_KEY_APPKEY, appKey);
        HttpResponse response = super.doPostRequest(EP_GET_USERS, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            //TODO
            return response.getData();
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
     * @throws Exception if an error occurred
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
     * @throws Exception if an error occurred
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
