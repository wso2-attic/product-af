package org.wso2.appfactory.tests.scenarios.tenantadmin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTest;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestUtils;
import org.wso2.appfactory.integration.test.utils.rest.CloudUserMgtClient;
import org.wso2.appfactory.integration.test.utils.rest.UserMgtClient;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;

import java.util.HashMap;
import java.util.Map;

/**
 * Test Case for test adding users to tenant and application
 */
public class UserManagementTestCase extends AFIntegrationTest {
    private static final Log log = LogFactory.getLog(UserManagementTestCase.class);
    public static final String USER_DEVELOPER = "devUser";
    public static final String USER_ALL_ROLES = "allUser";
    public static final String DEFAULT_PASSWORD = "user";
    public static final String USERNAME_SEPARATOR = ",";
    private UserMgtClient userMgtClient;    //client to manage users for application
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
    @Test(description = "Add users to tenant ", groups = {"user-to-tenant"})
    public void addUsersToTenant() throws Exception {
        boolean bulkImportUsers = cloudMgtClient.bulkImportUsers(USER_DEVELOPER + USERNAME_SEPARATOR + USER_ALL_ROLES,
                                                                 DEFAULT_PASSWORD);
        Assert.assertTrue(bulkImportUsers, "Adding users: " + USER_DEVELOPER + USERNAME_SEPARATOR + USER_ALL_ROLES +
                                           " to tenant: " + AFIntegrationTestUtils.getDefaultTenantDomain() + " failed");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Update user roles", dependsOnMethods = {"addUsersToTenant"}, groups = {"user-to-tenant"})
    public void updateUserRoles() throws Exception {
        boolean success = cloudMgtClient.updateUserRoles(USER_DEVELOPER, userRoleMap.get(USER_DEVELOPER), null);
        Assert.assertTrue(success, "Updating user roles of user: " + USER_DEVELOPER +
                                   " to role: " + userRoleMap.get(USER_DEVELOPER) +
                                   " for tenant: " + AFIntegrationTestUtils.getDefaultTenantDomain() + " failed");
        success = cloudMgtClient.updateUserRoles(USER_ALL_ROLES, userRoleMap.get(USER_ALL_ROLES), null);
        Assert.assertTrue(success, "Updating user roles of user: " + USER_ALL_ROLES +
                                   " to role: " + userRoleMap.get(USER_ALL_ROLES) +
                                   " for tenant: " + AFIntegrationTestUtils.getDefaultTenantDomain() + " failed");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Get all users of the tenant", dependsOnMethods = {"updateUserRoles"},
            groups = {"user-to-tenant"})
    public void getUsersOfTenant() throws Exception {
        String getUsersOfTenant = cloudMgtClient.getUsersOfTenant();
        log.info(getUsersOfTenant);
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Invite user to application ", dependsOnGroups = {"user-to-tenant"},
            groups = {"user-to-application"})
    public void inviteUsersToApplication() throws Exception {
        boolean success = userMgtClient.inviteUsersToApplication(
                defaultAppKey, USER_DEVELOPER + USERNAME_SEPARATOR + USER_ALL_ROLES);
        Assert.assertTrue(success, "Inviting users: " + USER_DEVELOPER +
                                   " to application: " + defaultAppKey +
                                   " in tenant: " + AFIntegrationTestUtils.getDefaultTenantDomain() + " failed");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Get users of application ", dependsOnMethods = {"inviteUsersToApplication"},
            groups = {"user-to-application"})
    public void getUsersOfApplication() throws Exception {
        String users = userMgtClient.getUsersOfApplication(defaultAppKey);
        log.info("Users: " + users);

    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Remove users from application ", dependsOnMethods = {"getUsersOfApplication"},
            groups = {"user-to-application"})
    public void removeUsersFromApplication() throws Exception {
        boolean success = userMgtClient.removeUsersFromApplication(defaultAppKey,
                                                                   USER_DEVELOPER + USERNAME_SEPARATOR + USER_ALL_ROLES);
        Assert.assertTrue(success, "Removing users: " + USER_DEVELOPER + USERNAME_SEPARATOR + USER_ALL_ROLES +
                                   " from application: " + defaultAppKey +
                                   " in tenant: " + AFIntegrationTestUtils.getDefaultTenantDomain() + " failed");

    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Remove users from the tenant ", dependsOnGroups = {"user-to-application"})
    public void deleteUsersFromTenant() throws Exception {
        boolean success = cloudMgtClient.deleteUserFromTenant(USER_DEVELOPER);
        Assert.assertTrue(success, "Removing user: " + USER_DEVELOPER +
                                   " from tenant: " + AFIntegrationTestUtils.getDefaultTenantDomain() + " failed");
        success = cloudMgtClient.deleteUserFromTenant(USER_ALL_ROLES);
        Assert.assertTrue(success, "Removing user: " + USER_ALL_ROLES +
                                   " from tenant: " + AFIntegrationTestUtils.getDefaultTenantDomain() + " failed");
    }
}
