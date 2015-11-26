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
package org.wso2.appfactory.tests.scenarios.tenantadmin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.appfactory.integration.test.utils.AFConstants;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTest;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestException;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestUtils;
import org.wso2.appfactory.integration.test.utils.rest.DatabaseClient;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;

import java.util.HashMap;
import java.util.Map;

/**
 * This tests all CRUD operations
 */
public class DatabaseTestCase extends AFIntegrationTest {

    private static final String DB_ONE = "db1";
    private static final String DB_TWO = "db2";
    private static final String DB_SUFFIX = "test";
    private static final String DB_PASSWORD = "123456";
    private static final String DB_DESCRIPTION = "test_db";
    private static final String STAGE_DEVELOPMENT = "Development";
    private static final String DB_USER_TWO = "tom";
    private static final String CUSTOM_TEMPLATE = "testtemplate";
    private DatabaseClient databaseClient = null;
    private String defaultTenantDomain = null;
    private String dbOneActualName = null;
    private String dbTwoActualName = null;
    private String dbUserOneActualName = null;
    private String dbUserTwoActualName = null;
    private String defaultTemplate = null;
    private String customTemplateActualName = null;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        databaseClient = new DatabaseClient(AFserverUrl, defaultAdmin, defaultAdminPassword);
        defaultTenantDomain = AFIntegrationTestUtils.getDefaultTenantDomain();
        dbOneActualName = getActualDatabaseName(DB_ONE + DB_SUFFIX, AFIntegrationTestUtils.getDefaultTenantDomain());
        dbTwoActualName = getActualDatabaseName(DB_TWO + DB_SUFFIX, AFIntegrationTestUtils.getDefaultTenantDomain());
        dbUserOneActualName = getFullyQualifiedUsername(DB_ONE + DB_SUFFIX);
        dbUserTwoActualName = getFullyQualifiedUsername(DB_USER_TWO);
        defaultTemplate = defaultAppKey + "_admin@" + STAGE_DEVELOPMENT;
        customTemplateActualName = CUSTOM_TEMPLATE + "@" + STAGE_DEVELOPMENT;

        // Detach users if exists
        detachDbUsers();
        //Delete users if exists
        deleteDbUser();
        //Delete databases if exists
        dropDatabase();
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Create database")
    public void testCreateDatabase() throws AFIntegrationTestException {

        databaseClient.createDatabaseAndAttachUser(defaultAppKey, DB_ONE, STAGE_DEVELOPMENT, DB_PASSWORD,
                                                   DB_DESCRIPTION, AFConstants.TRUE, AFConstants.FALSE,
                                                   AFConstants.FALSE, null, null, DB_SUFFIX);
        Assert.assertEquals(databaseClient.getDatabases(defaultAppKey).toString().contains(dbOneActualName), true,
                            "Creating database not success.");

        Assert.assertEquals(
                databaseClient.getAttachedUsers(defaultAppKey, dbOneActualName, STAGE_DEVELOPMENT).toString()
                              .contains(dbUserOneActualName), true, "Creating default database user not success.");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Create new database with advance options", dependsOnMethods = {"testCreateDatabase"})
    public void testCreateDatabaseAdvanceOption() throws AFIntegrationTestException {
        databaseClient.createDatabaseUser(defaultAppKey, DB_PASSWORD, STAGE_DEVELOPMENT, DB_USER_TWO);
        Assert.assertEquals(
                databaseClient.getAvailableUsersToAttachToDatabase(defaultAppKey, dbOneActualName, STAGE_DEVELOPMENT)
                              .toString().contains(dbUserTwoActualName), true, "Creating new database user not success.");
        // permissions for the template
        Map<String, String> permissions = new HashMap<String, String>();
        permissions.put("alterPriv", AFConstants.TRUE);
        permissions.put("alterRoutinePriv", AFConstants.TRUE);
        permissions.put("createPriv", AFConstants.TRUE);
        permissions.put("createRoutinePriv", AFConstants.TRUE);
        permissions.put("createTmpTablePriv", AFConstants.TRUE);
        permissions.put("createViewPriv", AFConstants.TRUE);
        permissions.put("deletePriv", AFConstants.TRUE);
        permissions.put("dropPriv", AFConstants.TRUE);
        permissions.put("eventPriv", AFConstants.TRUE);
        permissions.put("executePriv", AFConstants.TRUE);
        permissions.put("grantPriv", AFConstants.TRUE);
        permissions.put("indexPriv", AFConstants.TRUE);
        permissions.put("insertPriv", AFConstants.TRUE);
        permissions.put("lockTablesPriv", AFConstants.TRUE);
        permissions.put("referencesPriv", AFConstants.TRUE);
        permissions.put("selectPriv", AFConstants.TRUE);
        permissions.put("showViewPriv", AFConstants.TRUE);
        permissions.put("triggerPriv", AFConstants.TRUE);
        permissions.put("updatePriv", AFConstants.TRUE);

        databaseClient.createTemplates(defaultAppKey, CUSTOM_TEMPLATE, STAGE_DEVELOPMENT, permissions);
        Assert.assertEquals(
                databaseClient.getAvailableTemplatesToAttachToDatabase(defaultAppKey, STAGE_DEVELOPMENT)
                              .contains(customTemplateActualName), true, "Creating new database user not success.");

        databaseClient.createDatabaseAndAttachUser(defaultAppKey, DB_TWO, STAGE_DEVELOPMENT, DB_PASSWORD,
                                                   DB_DESCRIPTION, AFConstants.FALSE, AFConstants.FALSE,
                                                   AFConstants.FALSE, dbUserTwoActualName, customTemplateActualName,
                                                   DB_SUFFIX);
        Assert.assertEquals(databaseClient.getAllDatabasesInfo(defaultAppKey).entrySet().size(), 2,
                            "Creating database and attach user not success");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Attaching User For the initially created database", dependsOnMethods =
            {"testCreateDatabaseAdvanceOption"})
    public void testAttachUserForDatabase() throws AFIntegrationTestException {
        databaseClient.attachNewUser(defaultAppKey, dbOneActualName, STAGE_DEVELOPMENT, dbUserTwoActualName,
                                     defaultTemplate);
        Assert.assertEquals(databaseClient.getAttachedUsers(defaultAppKey, dbOneActualName, STAGE_DEVELOPMENT).size(),
                            2, "Attaching user to database not success.");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Get db user temaplate info for stages<used in Dev Studio>", dependsOnMethods =
            {"testAttachUserForDatabase"})
    public void testGetDbUserTemplateInfoForStages() throws AFIntegrationTestException {

        JsonArray resultAray = databaseClient.getDbUserTemplateInfoForStages(defaultAppKey);
        Assert.assertEquals(resultAray.get(0).getAsJsonObject().get("templates").getAsJsonArray().size() != 0, true,
                            "Get db user template info for stages failed");
    }


    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Test creating  database and attached user", dependsOnMethods = {"testGetDbUserTemplateInfoForStages"})
    public void testDetachUserFromDatabase() throws AFIntegrationTestException {
        detachDbUsers();
        Assert.assertEquals(databaseClient.getAttachedUsers(defaultAppKey, dbTwoActualName, STAGE_DEVELOPMENT).size(),
                            0, "Detach users from db2_black_com database not success.");
        Assert.assertEquals(databaseClient.getAttachedUsers(defaultAppKey, dbTwoActualName, STAGE_DEVELOPMENT).size(),
                            0, "Detach users from db1_black_com database not success.");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Delete user from database", dependsOnMethods = {"testDetachUserFromDatabase"})
    public void testDeleteUser() throws AFIntegrationTestException {
        deleteDbUser();
        Assert.assertEquals(databaseClient.getAvailableUsersToAttachToDatabase(
                defaultAppKey, dbOneActualName, STAGE_DEVELOPMENT).size(), 0, "Delete database users not success.");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Drop databases", dependsOnMethods = {"testDeleteUser"})
    public void testDropDatabase() throws AFIntegrationTestException {
        dropDatabase();
        Assert.assertEquals(databaseClient.getDatabases(defaultAppKey).size(), 0, "Deleting databases not success.");
    }

    private void detachDbUsers() throws AFIntegrationTestException {
        databaseClient.detachUser(defaultAppKey, dbTwoActualName, STAGE_DEVELOPMENT, dbUserTwoActualName);
        databaseClient.detachUser(defaultAppKey, dbOneActualName, STAGE_DEVELOPMENT, dbUserTwoActualName);
        databaseClient.detachUser(defaultAppKey, dbOneActualName, STAGE_DEVELOPMENT, dbUserOneActualName);
    }

    private void deleteDbUser() throws AFIntegrationTestException {
        if(isDataBaseUserExist(dbUserTwoActualName)) {
            databaseClient.deleteUser(defaultAppKey, dbUserTwoActualName, STAGE_DEVELOPMENT);
        }
        if(isDataBaseUserExist(dbUserOneActualName)) {
            databaseClient.deleteUser(defaultAppKey, dbUserOneActualName, STAGE_DEVELOPMENT);
        }
    }

    private void dropDatabase() throws AFIntegrationTestException {
        databaseClient.dropDatabase(defaultAppKey, dbOneActualName, STAGE_DEVELOPMENT, "false");
        databaseClient.dropDatabase(defaultAppKey, dbTwoActualName, STAGE_DEVELOPMENT, "false");
    }

    private boolean isDataBaseUserExist(String dbUser) throws AFIntegrationTestException {
        JsonArray resultAray = databaseClient.getDatabaseUsers(defaultAppKey);
        if(resultAray != null) {
            for(Object object : resultAray) {
                if(object instanceof JsonObject) {
                    JsonObject data = (JsonObject)object;
                    String name = data.get("name").getAsString();
                    if(dbUser.equals(name)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Get the actual database name
     * eg:- db_tom_com
     *
     * @param db
     * @param tenantDomain
     * @return
     */
    private String getActualDatabaseName(String db, String tenantDomain) {
        String actualDb;
        String[] tenantNameElements = tenantDomain.split("\\.");
        if (tenantNameElements.length == 2) {
            actualDb = db + "_" + tenantNameElements[0] + "_" + tenantNameElements[1];
        }
        else {
            actualDb = db + "_" + tenantDomain;
        }
        return actualDb;
    }

    /**
     * Get the full username of the database user
     *
     * @param username
     * @return
     */
    private String getFullyQualifiedUsername(String username) {
        byte[] bytes = intToByteArray(defaultTenantDomain.hashCode());
        return username + "_" + org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString(bytes);
    }

    private byte[] intToByteArray(int value) {
        byte[] b = new byte[6];
        for (int i = 0; i < 6; i++) {
            int offset = (b.length - 1 - i) * 8;
            b[i] = (byte) ((value >>> offset) & 0xFF);
        }
        return b;
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws AFIntegrationTestException {
        super.cleanup();
    }

}
