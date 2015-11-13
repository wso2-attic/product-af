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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.appfactory.integration.test.utils.AFConstants;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTest;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestUtils;
import org.wso2.appfactory.integration.test.utils.rest.GovernanceClient;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;

import java.util.ArrayList;

public class GovernanceTestCase extends AFIntegrationTest {

    private static GovernanceClient governanceRestClient = null;
    private static String applicationKey = null;
    private static final String INITIAL_STAGE = "Development";
    private static final String TAG_NAME = "";
    private static final String DEPLOY_ACTION = "deploy";
    private static final String CHECK_LIST_ITEM_NAME = "Design Review Done*";
    private static final String ITEM_CHECKED = "true";
    private static final String NEW_VERSION = "2.3.4";
    private static final String LIFECYCLE_NAME = "ApplicationLifecycle";
    private static String sourceVersion = "trunk";
    private static String targeVersion = "2.3.4";

    private static final Log log = LogFactory.getLog(GovernanceTestCase.class);

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        governanceRestClient = new GovernanceClient(AFserverUrl,defaultAdmin,defaultAdminPassword);
        applicationKey = AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_APP_KEY);
        sourceVersion = AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_VERSION_ONE_SRC);
        targeVersion = AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_VERSION_ONE_TARGET);
    }

    /**
     * Test to create a new version
     * @throws Exception
     */
    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.PLATFORM})
    @Test(description = "Creating a new version of an existing application")
    public void createVersionTestCase() throws Exception {
        String invokeVersionResponse =
                governanceRestClient.invokeDoVersion(defaultAppKey, sourceVersion, targeVersion, LIFECYCLE_NAME);
        Assert.assertTrue(Boolean.parseBoolean(invokeVersionResponse));
    }

    //promote

    /**
     * Test to check the check list items in governance page
     * @throws Exception
     */
    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.PLATFORM})
    @Test(description = "Check list item selection", dependsOnMethods = {"createVersionTestCase"})
    public void itemCheckedTestCase() throws Exception {
        String itemCheckedResponse = governanceRestClient.itemChecked(applicationKey, INITIAL_STAGE, targeVersion, CHECK_LIST_ITEM_NAME, ITEM_CHECKED);
        Assert.assertTrue(Boolean.parseBoolean(itemCheckedResponse));
    }


    //promote


    /**
     * Test to check the app versions with lifecycle info of an app
     * @throws Exception
     */
    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.PLATFORM})
    @Test(description = "Get versions of all stages in the application with lifecycle info")
    public void getAppVersionsInStagesWithLifeCycleInfoTestCase() throws Exception {
        Assert.assertEquals(governanceRestClient.getAppVersionsInStagesWithLifeCycleInfo(applicationKey, defaultAdmin)
                                                .toString().contains(targeVersion), true,
                            "Get versions of all stages in the application with lifecycle info not success.");
        }
    }

