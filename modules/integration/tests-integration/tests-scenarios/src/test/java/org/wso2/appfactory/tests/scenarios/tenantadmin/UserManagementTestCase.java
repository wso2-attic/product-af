package org.wso2.appfactory.tests.scenarios.tenantadmin;

import org.json.JSONArray;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTest;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestException;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestUtils;
import org.wso2.appfactory.integration.test.utils.rest.CloudUserMgtClient;
import org.wso2.appfactory.integration.test.utils.rest.UserMgtClient;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertTrue;

/**
 * Test Case for test adding users to tenant and application.
 * 01.) Add users to tenant
 * 02.) Update user roles of the user roles
 * 03.) Add users to an application
 * 04.) Remove users from the application
 * 05.) Remove users from the tenant
 *
 * TODO: Add after class method to cleanup
 */
public class UserManagementTestCase extends AFIntegrationTest {
    public static final String USER_DEVELOPER = "devUser";
    public static final String USER_ALL_ROLES = "allUser";
    public static final String DEFAULT_PASSWORD = "user";
    public static final String USERNAME_SEPARATOR = ",";
    public static final String JS_OBJECT_KEY_USER_NAME = "userName";
    private UserMgtClient userMgtClient;        //client to manage users for application
    private CloudUserMgtClient cloudMgtClient;  //cloudmgt client to manage users for tenants

    // map of userName - roles as a key-value pair
    private Map<String, String> userRoleMap = new HashMap<String, String>();

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        userMgtClient = new UserMgtClient(AFserverUrl, defaultAdmin, defaultAdminPassword);
        cloudMgtClient = new CloudUserMgtClient(AFserverUrl, defaultAdmin, defaultAdminPassword);
        userRoleMap.put(USER_DEVELOPER, "developer");
        userRoleMap.put(USER_ALL_ROLES, "developer,devops,qa,appowner,cxo");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Add users to tenant ")
    public void testAddUsersToTenant() throws Exception {
        boolean bulkImportUsers = cloudMgtClient.bulkImportUsers(USER_DEVELOPER + USERNAME_SEPARATOR + USER_ALL_ROLES,
                                                                 DEFAULT_PASSWORD);
        assertTrue(bulkImportUsers, "Adding users: " + USER_DEVELOPER + USERNAME_SEPARATOR + USER_ALL_ROLES +
                                    " to tenant: " + AFIntegrationTestUtils.getDefaultTenantDomain() + " failed");
        ArrayList<String> usersOfLoggedInTenant = getUsersOfLoggedInTenant();
        assertTrue(usersOfLoggedInTenant.contains(USER_DEVELOPER), "User :" + USER_DEVELOPER + " not found in retrieved " +
                                                                   "tenant user list");
        assertTrue(usersOfLoggedInTenant.contains(USER_ALL_ROLES), "User :" + USER_ALL_ROLES + " not found in retrieved " +
                                                                   "tenant user list");

    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Update user roles", dependsOnMethods = {"testAddUsersToTenant"})
    public void testUpdateUserRoles() throws Exception {
        boolean success = cloudMgtClient.updateUserRoles(USER_DEVELOPER, userRoleMap.get(USER_DEVELOPER), null);
        assertTrue(success, "Updating user roles of user: " + USER_DEVELOPER +
                            " to role: " + userRoleMap.get(USER_DEVELOPER) +
                            " for tenant: " + AFIntegrationTestUtils.getDefaultTenantDomain() + " failed");
        success = cloudMgtClient.updateUserRoles(USER_ALL_ROLES, userRoleMap.get(USER_ALL_ROLES), null);
        assertTrue(success, "Updating user roles of user: " + USER_ALL_ROLES +
                            " to role: " + userRoleMap.get(USER_ALL_ROLES) +
                            " for tenant: " + AFIntegrationTestUtils.getDefaultTenantDomain() + " failed");
        //TODO check for roles
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Invite user to application <used in Dev Studio> ", dependsOnMethods = {"testUpdateUserRoles"})
    public void testInviteUsersToApplication() throws Exception {
        boolean success = userMgtClient.inviteUsersToApplication(
                defaultAppKey, USER_DEVELOPER + USERNAME_SEPARATOR + USER_ALL_ROLES);
        assertTrue(success, "Inviting users: " + USER_DEVELOPER +
                            " to application: " + defaultAppKey +
                            " in tenant: " + AFIntegrationTestUtils.getDefaultTenantDomain() + " failed");
        ArrayList<String> returnedUsers = getApplicationUsers(defaultAppKey);
        assertTrue(returnedUsers.contains(USER_DEVELOPER), "User :" + USER_DEVELOPER + " not found in retrieved " +
                                                           "application user list");
        assertTrue(returnedUsers.contains(USER_ALL_ROLES), "User :" + USER_ALL_ROLES + " not found in retrieved " +
                                                           "application user list");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Remove users from application ", dependsOnMethods = {"testInviteUsersToApplication"})
    public void testRemoveUsersFromApplication() throws Exception {
        boolean success = userMgtClient.removeUsersFromApplication(defaultAppKey,
                                                                   USER_DEVELOPER + USERNAME_SEPARATOR + USER_ALL_ROLES);
        assertTrue(success, "Removing users: " + USER_DEVELOPER + USERNAME_SEPARATOR + USER_ALL_ROLES +
                            " from application: " + defaultAppKey +
                            " in tenant: " + AFIntegrationTestUtils.getDefaultTenantDomain() + " failed");

    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Remove users from the tenant ", dependsOnMethods = {"testRemoveUsersFromApplication"})
    public void testDeleteUsersFromTenant() throws Exception {
        boolean success = cloudMgtClient.deleteUserFromTenant(USER_DEVELOPER);
        assertTrue(success, "Removing user: " + USER_DEVELOPER +
                            " from tenant: " + AFIntegrationTestUtils.getDefaultTenantDomain() + " failed");
        success = cloudMgtClient.deleteUserFromTenant(USER_ALL_ROLES);
        assertTrue(success, "Removing user: " + USER_ALL_ROLES +
                            " from tenant: " + AFIntegrationTestUtils.getDefaultTenantDomain() + " failed");
    }

    private ArrayList<String> getUsersOfLoggedInTenant() throws Exception {
        JSONArray users = cloudMgtClient.getUsersOfTenant();
        ArrayList<String> returnedUsers = new ArrayList<String>();
        for (int i = 0; i < users.length(); i++) {
            returnedUsers.add(users.getJSONObject(i).getString(JS_OBJECT_KEY_USER_NAME));
        }
        return returnedUsers;
    }

    private ArrayList<String> getApplicationUsers(String defaultAppKey) throws Exception {
        JSONArray users = userMgtClient.getUsersOfApplication(defaultAppKey);
        ArrayList<String> returnedUsers = new ArrayList<String>();
        for (int i = 0; i < users.length(); i++) {
            returnedUsers.add(users.getJSONObject(i).getString(JS_OBJECT_KEY_USER_NAME));
        }
        return returnedUsers;
    }
}
