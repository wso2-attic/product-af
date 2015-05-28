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

package org.wso2.appfactory.tests.scenarios;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTest;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestException;
import org.wso2.appfactory.integration.test.utils.rest.GovernanceClient;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;

/**
 * Test scenarios for the app factory governance
 *Test Scenario 1
 *Create new branch of app,build and deploy
 *
 *Test Scenario 2
 *Promote app, Development to Test
 *Demote app, Test to Development
 *
 *Test Scenario 3
 *Promote app, Test to Production
 *Demote app, Production to Test
 *
 *Test Scenario 4
 *Retire app
 */
public class GovernanceTestCase extends AFIntegrationTest {

    private static final String SOURCE_VERSION = "trunk";
    private static final String REPO_FORM = "fork";
    private static final String DO_DEPLOY = "true";
    private static final String TAG_NAME = "";
    private static final String DEPLOY_ACTION = "deploy";
    private static final String ITEM_CHECKED = "true";
    private static final String NEW_VERSION = "5.0.0";
    private static final String INITIAL_STAGE_DEVERLOPMENT = "Development";
    private static final String INITIAL_STAGE_TEST = "Testing";
    private static final String INITIAL_STAGE_PRODUCTION = "Production";
    private static final String CODE_COMPLETE_ITEM_NAME = "Code Completed*";
    private static final String DESIGN_REVIEW_ITEM_NAME = "Design Review Done*";
    private static final String CODE_REVIEW_ITEM_NAME = "Code Review Done*";
    private static final String SMOKE_TEST_ITEM_NAME = "Smoke Tests Passed*";
    private static final String PRODUCTION_VERIFY_ITEM_NAME = "Test Cases Passed*";
    private static final String LIFECYCLE_NAME = "ApplicationLifecycle";
    private static final String LAST_BUILD_RESULT = "build 2 successful";
    private static final String PROMOTE_TEST_MESSAGE = "Promote action was successful.";
    private static final String TEST_CASE_PASS_ITEM_NAME = "Verify no one is using the Application*";
    private static GovernanceClient governanceRestClient = null;
    private static JSONArray pramoteToTestCheckList;
    private static JSONArray pramoteToProducCheckList;
    private static JSONArray demoteCheckList;

    private static final Log log = LogFactory.getLog(GovernanceTestCase.class);

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        governanceRestClient = new GovernanceClient(AFserverUrl, defaultAdmin, defaultAdminPassword);
        pramoteToTestCheckList = new JSONArray();
        pramoteToProducCheckList = new JSONArray();
        demoteCheckList = new JSONArray();
    }

    /**
     * Test scenario to create,build and deploy a new version of a app.
     *
     * @throws AFIntegrationTestException
     */
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Creating a new version of an existing application")
    public void testCreateBuildDeployVersion() throws AFIntegrationTestException {
        //create a branch of the app
        JsonObject createVersionResponse = governanceRestClient.invokeDoVersion(defaultAppKey, SOURCE_VERSION,
                                                                                NEW_VERSION, LIFECYCLE_NAME,
                                                                                defaultAdmin);
        Assert.assertEquals(createVersionResponse.getAsJsonObject("Development").getAsJsonArray("versions").
                getAsJsonArray().toString().contains(NEW_VERSION), true, "Create version fails");

        //build new branch
        JsonObject buildVersionResponse = governanceRestClient.createArtifact(defaultAppKey, NEW_VERSION, "",
                                                                              INITIAL_STAGE_DEVERLOPMENT, DO_DEPLOY, "",
                                                                              REPO_FORM, defaultAdmin);
        JsonArray versionArray = buildVersionResponse.getAsJsonObject("Development").
                getAsJsonArray("versions").getAsJsonArray();
        for (int i = 0; i < versionArray.size(); i++) {
            if (NEW_VERSION.equals(versionArray.get(i).getAsJsonObject().get("version").getAsString())) {
                Assert.assertEquals(versionArray.get(i).getAsJsonObject().get("LAST_BUILD_RESULT").getAsString(),
                                    LAST_BUILD_RESULT, "Version build fails");
                break;
            }
        }
        //deploy new branch
        JsonObject deployVersionResponse = governanceRestClient.copyNewDependenciesAndDeployArtifact(defaultAppKey,
                                                                                                     INITIAL_STAGE_DEVERLOPMENT,
                                                                                                     NEW_VERSION,
                                                                                                     TAG_NAME,
                                                                                                     DEPLOY_ACTION,
                                                                                                     defaultAdmin);
        JsonArray deployVersionArray = deployVersionResponse.getAsJsonObject("Development").getAsJsonArray("versions")
                .getAsJsonArray();
        for (int i = 0; i < deployVersionArray.size(); i++) {
            if (NEW_VERSION.equals(deployVersionArray.get(i).getAsJsonObject().get("version").getAsString())) {
                Assert.assertNotNull(deployVersionArray.get(i).getAsJsonObject().get("deployedBuildId").getAsString()
                        ,"deploy id should not null");
                break;
            }
        }


    }

    /**
     * Test scenario to Promote app, Development to Test and
     * demote Test to Development
     *
     * @throws AFIntegrationTestException
     */
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Check pramoteToTestCheckList item selection", dependsOnMethods =
            {"testCreateBuildDeployVersion"})

    public void promoteDevToTest() throws AFIntegrationTestException {

        //check items
        Assert.assertEquals(
                Boolean.parseBoolean(governanceRestClient.itemChecked(defaultAppKey,
                                                                      INITIAL_STAGE_DEVERLOPMENT,
                                                                      NEW_VERSION, CODE_COMPLETE_ITEM_NAME,
                                                                      ITEM_CHECKED)),
                true,"code complete check item fail");
        Assert.assertEquals(
                Boolean.parseBoolean(governanceRestClient.itemChecked(defaultAppKey, INITIAL_STAGE_DEVERLOPMENT,
                                                                      NEW_VERSION, DESIGN_REVIEW_ITEM_NAME,
                                                                      ITEM_CHECKED)),
                true,"design review check item fail");
        Assert.assertEquals(
                Boolean.parseBoolean(governanceRestClient.itemChecked(defaultAppKey, INITIAL_STAGE_DEVERLOPMENT,
                                                                      NEW_VERSION, CODE_REVIEW_ITEM_NAME,
                                                                      ITEM_CHECKED)),
                true,"code review check item fail");

        //update check items
        pramoteToTestCheckList.put("true");
        pramoteToTestCheckList.put("true");
        pramoteToTestCheckList.put("true");
        JsonObject codeComplete = governanceRestClient.invokeUpdateLifeCycleCheckList(defaultAppKey, NEW_VERSION,
                                                                                      INITIAL_STAGE_DEVERLOPMENT,
                                                                                      pramoteToTestCheckList,
                                                                                      defaultAdmin);
        JsonArray updateLifeCycleArray = codeComplete.getAsJsonObject("Development").getAsJsonArray("versions")
                .getAsJsonArray();
        for (int i = 0; i < updateLifeCycleArray.size(); i++) {
            if (NEW_VERSION.equals(updateLifeCycleArray.get(i).getAsJsonObject().get("version").getAsString())) {
                Assert.assertEquals(
                        updateLifeCycleArray.get(i).getAsJsonObject().get("lifeCycleCheckListItems").getAsJsonArray()
                                .get(0).getAsJsonObject().get("value").getAsBoolean(), true,
                        "code complete check item update");
                Assert.assertEquals(
                        updateLifeCycleArray.get(i).getAsJsonObject().get("lifeCycleCheckListItems").getAsJsonArray()
                                .get(1).getAsJsonObject().get("value").getAsBoolean(), true,
                        "design review check item update fail");
                Assert.assertEquals(
                        updateLifeCycleArray.get(i).getAsJsonObject().get("lifeCycleCheckListItems").getAsJsonArray()
                                .get(2).getAsJsonObject().get("value").getAsBoolean(), true,
                        "code review check item update fail");

                break;
            }
        }
        //Promote version dev to test
        JsonObject jsonObject_Promote = governanceRestClient
                .promote(defaultAppKey, INITIAL_STAGE_DEVERLOPMENT, NEW_VERSION, "", "", defaultAdmin,
                         pramoteToTestCheckList);
        Assert.assertEquals(jsonObject_Promote.get("message"), PROMOTE_TEST_MESSAGE,
                            "Promote version Development to test fail");

        //demote version test to dev
        JsonObject jsonObject = governanceRestClient.demote(defaultAppKey, INITIAL_STAGE_TEST, NEW_VERSION, "",
                                                            "demote version test to dev<![CDATA[<span></span>]]>",
                                                            defaultAdmin, demoteCheckList);
        Assert.assertEquals(jsonObject.get("message"), PROMOTE_TEST_MESSAGE, "Retire version of app");

    }

    /**
     * Test scenario to Promote app,Test to Production and
     * demote Production to Test
     *
     * @throws AFIntegrationTestException
     */
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Check pramoteToTestCheckList item selection", dependsOnMethods =
            {"testCreateBuildDeployVersion", "promoteDevToTest"})
    public void promotTestToProduction() throws AFIntegrationTestException {
        //promote Dev to Test
        //check items
        governanceRestClient
                .itemChecked(defaultAppKey, INITIAL_STAGE_DEVERLOPMENT, NEW_VERSION, CODE_COMPLETE_ITEM_NAME,
                             ITEM_CHECKED);
        governanceRestClient
                .itemChecked(defaultAppKey, INITIAL_STAGE_DEVERLOPMENT, NEW_VERSION, DESIGN_REVIEW_ITEM_NAME,
                             ITEM_CHECKED);
        governanceRestClient.itemChecked(defaultAppKey, INITIAL_STAGE_DEVERLOPMENT, NEW_VERSION, CODE_REVIEW_ITEM_NAME,
                                         ITEM_CHECKED);

        //update check items
        pramoteToTestCheckList.put("true");
        pramoteToTestCheckList.put("true");
        pramoteToTestCheckList.put("true");
        JsonObject codeComplete = governanceRestClient.invokeUpdateLifeCycleCheckList(defaultAppKey, NEW_VERSION,
                                                                                      INITIAL_STAGE_DEVERLOPMENT,
                                                                                      pramoteToTestCheckList,
                                                                                      defaultAdmin);

        //Promote version dev to test
        JsonObject jsonObject_Promote = governanceRestClient
                .promote(defaultAppKey, INITIAL_STAGE_DEVERLOPMENT, NEW_VERSION, "", "", defaultAdmin,
                         pramoteToTestCheckList);

        //promote to Production
        //check items
        Assert.assertEquals(Boolean.parseBoolean(governanceRestClient.itemChecked(defaultAppKey, INITIAL_STAGE_TEST,
                                                                                  NEW_VERSION, SMOKE_TEST_ITEM_NAME,
                                                                                  ITEM_CHECKED)), true,
                            "smoke test check item fail");
        Assert.assertEquals(Boolean.parseBoolean(governanceRestClient.itemChecked(defaultAppKey, INITIAL_STAGE_TEST,
                                                                                  NEW_VERSION, TEST_CASE_PASS_ITEM_NAME,
                                                                                  ITEM_CHECKED)), true,
                            "test case passed check item fail");

        //update check items
        pramoteToProducCheckList.put("true");
        pramoteToProducCheckList.put("true");
        JsonObject updateCheckItems = governanceRestClient.invokeUpdateLifeCycleCheckList(defaultAppKey, NEW_VERSION,
                                                                                          INITIAL_STAGE_TEST,
                                                                                          pramoteToProducCheckList,
                                                                                          defaultAdmin);
        JsonArray updateLifeCycleArray = updateCheckItems.getAsJsonObject("Testing").getAsJsonArray("versions")
                .getAsJsonArray();
        for (int i = 0; i < updateLifeCycleArray.size(); i++) {
            if (NEW_VERSION.equals(updateLifeCycleArray.get(i).getAsJsonObject().get("version").getAsString())) {

                Assert.assertEquals(
                        updateLifeCycleArray.get(i).getAsJsonObject().get("lifeCycleCheckListItems").getAsJsonArray()
                                .get(0).getAsJsonObject().get("value").getAsBoolean(), true,
                        "code complete check item update");
                Assert.assertEquals(
                        updateLifeCycleArray.get(i).getAsJsonObject().get("lifeCycleCheckListItems").getAsJsonArray()
                                .get(1).getAsJsonObject().get("value").getAsBoolean(), true,
                        "design review check item update fail");
                break;
            }
        }
        //Promote version test to production
        JsonObject jsonObjectPromote = governanceRestClient
                .promote(defaultAppKey, INITIAL_STAGE_TEST, NEW_VERSION, "", "", defaultAdmin,
                         pramoteToProducCheckList);
        Assert.assertEquals(jsonObjectPromote.get("message"), PROMOTE_TEST_MESSAGE,
                            "Promote version Development to test");

        //demote version production to test
        JsonObject jsonObject = governanceRestClient.demote(defaultAppKey, INITIAL_STAGE_PRODUCTION, NEW_VERSION, "",
                                                            "demote version test to dev<![CDATA[<span></span>]]>",
                                                            defaultAdmin, demoteCheckList);
        Assert.assertEquals(jsonObject.get("message"), PROMOTE_TEST_MESSAGE, "Retire version of app");

    }

    /**
     * Test scenario to Retire the version of a app
     *
     * @throws AFIntegrationTestException
     */
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Check pramoteToTestCheckList item selection", dependsOnMethods =
            {"testCreateBuildDeployVersion", "promoteDevToTest"})
    public void Retire() throws AFIntegrationTestException {

        governanceRestClient
                .itemChecked(defaultAppKey, INITIAL_STAGE_TEST, NEW_VERSION, SMOKE_TEST_ITEM_NAME, ITEM_CHECKED);
        governanceRestClient
                .itemChecked(defaultAppKey, INITIAL_STAGE_TEST, NEW_VERSION, TEST_CASE_PASS_ITEM_NAME, ITEM_CHECKED);

        //update check items
        pramoteToProducCheckList.put("true");
        pramoteToProducCheckList.put("true");
        JsonObject updateCheckItems = governanceRestClient.invokeUpdateLifeCycleCheckList(defaultAppKey, NEW_VERSION,
                                                                                          INITIAL_STAGE_TEST,
                                                                                          pramoteToProducCheckList,
                                                                                          defaultAdmin);

        //Promote version test to production
        JsonObject jsonObject_pramote = governanceRestClient
                .promote(defaultAppKey, INITIAL_STAGE_TEST, NEW_VERSION, "", "", defaultAdmin,
                         pramoteToProducCheckList);

        //check items
        Assert.assertEquals(
                Boolean.parseBoolean(governanceRestClient.itemChecked(defaultAppKey, INITIAL_STAGE_PRODUCTION,
                                                                      NEW_VERSION, PRODUCTION_VERIFY_ITEM_NAME,
                                                                      ITEM_CHECKED)), true,
                "APP demote verification check item fail");

        //update check items
        demoteCheckList.put("true");

        JsonObject codeComplete = governanceRestClient.invokeUpdateLifeCycleCheckList(defaultAppKey, NEW_VERSION,
                                                                                      INITIAL_STAGE_PRODUCTION,
                                                                                      demoteCheckList, defaultAdmin);
        JsonArray updateLifeCycleArray = codeComplete.getAsJsonObject("Production").getAsJsonArray("versions")
                .getAsJsonArray();
        for (int i = 0; i < updateLifeCycleArray.size(); i++) {
            if (NEW_VERSION.equals(updateLifeCycleArray.get(i).getAsJsonObject().get("version").getAsString())) {

                Assert.assertEquals(
                        updateLifeCycleArray.get(i).getAsJsonObject().get("lifeCycleCheckListItems").getAsJsonArray()
                                .get(0).getAsJsonObject().get("value").getAsBoolean(), true,
                        "Verify no one is using the Application* is not check");

                break;
            }
        }
        //retire branch
        JsonObject jsonObject = governanceRestClient
                .retire(defaultAppKey, INITIAL_STAGE_PRODUCTION, NEW_VERSION, "", "", defaultAdmin, demoteCheckList);
        Assert.assertEquals(jsonObject.get("message"), PROMOTE_TEST_MESSAGE, "Retire version of app");

    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }


}

