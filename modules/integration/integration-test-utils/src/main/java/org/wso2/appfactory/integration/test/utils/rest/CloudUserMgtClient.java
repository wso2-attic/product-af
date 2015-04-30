package org.wso2.appfactory.integration.test.utils.rest;

import org.apache.commons.httpclient.HttpStatus;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestException;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Rest client for handle user management functionalities in cloudmgt app
 */
public class CloudUserMgtClient extends BaseClient {

    // Endpoints
    private static final String EP_TENANT_USER_ADD = "tenant/users/add/ajax/add.jag";
    private static final String EP_TENANT_USER_GET = "tenant/users/get/ajax/get.jag";

    // Request actions
    private static final String ACTION_BULK_IMPORT_USERS = "bulkImportUsers";
    private static final String ACTION_DELETE_USER_FROM_TENANT = "deleteUserFromTenant";
    private static final String ACTION_GET_USERSOF_TENANT = "getUsersofTenant";
    private static final String ACTION_UPDATE_USER_ROLES = "updateUserRoles";

    // Other Request parameters
    private static final String REQUEST_KEY_ACTION = "action";
    private static final String REQUEST_KEY_USERS = "users";
    private static final String REQUEST_KEY_DEFAULT_PASSWORD = "defaultPassword";
    private static final String REQUEST_KEY_ROLES_TO_ADD = "rolesToAdd";
    private static final String REQUEST_KEY_ROLES_TO_DELETE = "rolesToDelete";
    private static final String REQUEST_KEY_USER_NAME = "userName";

    public CloudUserMgtClient(String backEndUrl, String username, String password) throws Exception {
        super(backEndUrl, username, password);
    }

    @Override
    protected void login(String userName, String password) throws Exception {
        HttpResponse response = HttpRequestUtil.doPost(
                new URL(getBackEndUrl() + CLOUDMGT_URL_SURFIX + APPMGT_USER_LOGIN),
                "action=login&userName=" + userName + "&password=" + password, getRequestHeaders());

        if (response.getResponseCode() == HttpStatus.SC_OK && response.getData().equals("true")) {
            String session = getSession(response.getHeaders());
            if (session == null) {
                throw new AFIntegrationTestException("No session cookie found with response");
            }
            setSession(session);
        } else {
            throw new AFIntegrationTestException("Login failed for cloudmgt" + response.getData());
        }
    }

    /**
     * Import users as a bulk to tenant(currently logged in).
     *
     * @param userNames       user names as comma separated values. ex: "user1,user2"
     * @param defaultPassword default password
     * @return success or not
     * @throws Exception if an error occurred
     */
    public boolean bulkImportUsers(String userNames, String defaultPassword) throws Exception {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(REQUEST_KEY_ACTION, ACTION_BULK_IMPORT_USERS);
        msgBodyMap.put(REQUEST_KEY_DEFAULT_PASSWORD, defaultPassword);
        msgBodyMap.put(REQUEST_KEY_USERS, userNames);
        HttpResponse response = super.doCloudMgtPostRequest(EP_TENANT_USER_ADD, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            return Boolean.valueOf(response.getData());
        } else {
            throw new AFIntegrationTestException("bulkImportUsers failed. HTTP status: " + response
                    .getResponseMessage() + ", Response message: " + response.getResponseMessage());
        }
    }

    /**
     * Update user roles
     *
     * @param userName      user name
     * @param rolesToAdd    roles to add as as comma separated values. ex "developer,qa"
     * @param rolesToDelete roles to delete as as comma separated values. ex "developer,qa"
     * @return success or not
     * @throws Exception if an error occurred
     */
    public boolean updateUserRoles(String userName, String rolesToAdd, String rolesToDelete) throws Exception {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(REQUEST_KEY_ACTION, ACTION_UPDATE_USER_ROLES);
        msgBodyMap.put(REQUEST_KEY_ROLES_TO_DELETE, rolesToDelete);
        msgBodyMap.put(REQUEST_KEY_ROLES_TO_ADD, rolesToAdd);
        msgBodyMap.put(REQUEST_KEY_USER_NAME, userName);
        HttpResponse response = super.doCloudMgtPostRequest(EP_TENANT_USER_ADD, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            return Boolean.valueOf(response.getData());
        } else {
            throw new AFIntegrationTestException("updateUserRoles failed. HTTP status: " + response
                    .getResponseMessage() + ", Response message: " + response.getResponseMessage());
        }
    }

    /**
     * Deletes the {@code userName} from the tenant(currently logged in).
     *
     * @param userName
     * @return success or not
     * @throws Exception if an error occurred
     */
    public boolean deleteUserFromTenant(String userName) throws Exception {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(REQUEST_KEY_ACTION, ACTION_DELETE_USER_FROM_TENANT);
        msgBodyMap.put(REQUEST_KEY_USER_NAME, userName);
        HttpResponse response = super.doCloudMgtPostRequest(EP_TENANT_USER_ADD, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            return Boolean.valueOf(response.getData());
        } else {
            throw new AFIntegrationTestException("deleteUserFromTenant failed. HTTP status: " + response
                    .getResponseMessage() + ", Response message: " + response.getResponseMessage());
        }
    }

    /**
     * Get all the users of tenant
     *
     * @return success or not
     * @throws Exception if an error occurred
     */
    public String getUsersOfTenant() throws Exception {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(REQUEST_KEY_ACTION, ACTION_GET_USERSOF_TENANT);
        HttpResponse response = super.doCloudMgtPostRequest(EP_TENANT_USER_GET, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            //TODO
            return response.getData();
        } else {
            throw new AFIntegrationTestException("getUsersOfTenant failed. HTTP status: " + response
                    .getResponseMessage() + ", Response message: " + response.getResponseMessage());
        }
    }
}
