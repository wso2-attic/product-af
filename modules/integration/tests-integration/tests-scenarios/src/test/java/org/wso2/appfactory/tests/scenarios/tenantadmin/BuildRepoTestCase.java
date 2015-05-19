/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.appfactory.tests.scenarios.tenantadmin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.appfactory.integration.test.utils.AFConstants;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTest;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestException;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestUtils;
import org.wso2.appfactory.integration.test.utils.rest.ApplicationClient;
import org.wso2.appfactory.integration.test.utils.rest.BuildRepoClient;
import org.wso2.appfactory.integration.test.utils.rest.GovernanceClient;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;

/**
 * This class is used to test all the REST api's in Build and repo feature
 */
public class BuildRepoTestCase extends AFIntegrationTest {

    private static final String INITIAL_STAGE = "Development";
    private static final String TAG_NAME = "";
    private static final String DEPLOY_ACTION = "deploy";
    private static final String APPLICATION_TYPE = "war";
    private static final Log log = LogFactory.getLog(BuildRepoTestCase.class);
    private static BuildRepoClient buildRepoRestClient = null;
    private static String firstVersion = "1.0.0";
    private static String applicationKey = null;
    private static String initialVersion = null;
    private static String tenantAdmin = null;
    private static String gitUrl = null;
    private static String appType = null;
    private static int lastBuildID = 0;
    private static final String APPLICATION_LIFECYCLE ="ApplicationLifecycle";

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        tenantAdmin = AFIntegrationTestUtils.getAdminUsername();
        initialVersion = "trunk";
        firstVersion = "1.0.0";
        applicationKey = AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_APP_KEY);
        String tenantAdminPassword = AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_TENANT_ADMIN_PASSWORD);
        String afUrl = AFIntegrationTestUtils.getPropertyValue(AFConstants.URLS_APPFACTORY);
        ApplicationClient applicationClient = new ApplicationClient(afUrl, tenantAdmin, tenantAdminPassword);
        //delete default application
        applicationClient.deleteApplication(tenantAdmin, applicationKey);
        //create default application
        applicationClient.createNewApplication(applicationKey, applicationKey, APPLICATION_TYPE, tenantAdmin,
                "Default Application");
        Thread.sleep(5000);
        GovernanceClient governanceClient = new GovernanceClient(afUrl,tenantAdmin,tenantAdminPassword);
        governanceClient.invokeDoVersion(applicationKey,initialVersion,firstVersion,APPLICATION_LIFECYCLE);
        buildRepoRestClient = new BuildRepoClient(afUrl, tenantAdmin, tenantAdminPassword);
        initialVersion = AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_VERSION_ONE_SRC);
        firstVersion = AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_VERSION_ONE_TARGET);
        gitUrl = AFIntegrationTestUtils.getPropertyValue(AFConstants.URLS_GIT) + "/git/" +
                tenantDomain + "/" + applicationKey + ".git";
        appType = AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_APP_TYPE);
    }

    /**
     * Calls getBuildAndRepoDataForkedRepo method in BuildRepoClient
     *
     * @throws org.wso2.appfactory.integration.test.utils.AFIntegrationTestException
     */
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Get build and repo data forked repo")
    public void testGetBuildAndRepoDataForkedRepo() throws AFIntegrationTestException {
        JsonObject dataObject = buildRepoRestClient.getBuildAndRepoDataForkedRepo(applicationKey,
                "false", "false", "true",tenantAdmin);
        JsonObject versionObject = dataObject.get("trunk").getAsJsonObject().get("version").getAsJsonObject();
        boolean isAssert = true;
        if (versionObject.get("isAutoDeploy").getAsBoolean()
                && versionObject.get("isAutoBuild").getAsBoolean()) {
            isAssert = false;
        }
        Assert.assertEquals(isAssert, false, "failed to set build deployment config for " +
                initialVersion);
    }

    /**
     * Calls deployArtifact method in BuildRepoClient
     * @throws AFIntegrationTestException
     */
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Deploy artifact", dependsOnMethods = {"testGetBuildAndRepoDataForkedRepo"})
    public void testDeployArtifact() throws AFIntegrationTestException {
        String response = buildRepoRestClient.deployArtifact(applicationKey, INITIAL_STAGE, initialVersion,
                TAG_NAME, DEPLOY_ACTION);
        Assert.assertEquals(response,"null", "Application deploying failed");
        JsonObject jsonObject = buildRepoRestClient.getBuildAndRepoDataForkedRepo(applicationKey,
                "false", "false", "true",tenantAdmin);
        System.out.println(jsonObject+">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        int deploymentID = jsonObject.get("trunk").getAsJsonObject().get("deployment").getAsJsonObject().
                get("deployedBuildId").getAsInt();
        boolean isAssert = false;
        if (deploymentID < 1) {
            isAssert = true;
        }
        Assert.assertEquals(isAssert, false, "Application deploying failed");
    }

    /**
     * Calls createFork method in BuildRepoClient
     *
     * @throws org.wso2.appfactory.integration.test.utils.AFIntegrationTestException
     */
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Create fork", dependsOnMethods = {"testDeployArtifact"})
    public void testCreateFork() throws AFIntegrationTestException {
        boolean responseCreateFork = buildRepoRestClient.createFork(applicationKey, tenantAdmin, "git", initialVersion);
        Assert.assertEquals(responseCreateFork, true, "Creating fork failed");
    }

    /**
     * Calls createFork method in BuildRepoClient
     *
     * @throws org.wso2.appfactory.integration.test.utils.AFIntegrationTestException
     */
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Create fork for branch", dependsOnMethods = {"testCreateFork"})
    public void testCreateFornkBranch() throws AFIntegrationTestException {
        boolean responseCreateForkBranch = buildRepoRestClient.createForkBranch(applicationKey, tenantAdmin, "git",
                firstVersion);
        Assert.assertEquals(responseCreateForkBranch, true, "failed to create fork branch");
    }

    /**
     * Calls setBuildDelopymentConfigs method in BuildRepoClient
     *
     * @throws org.wso2.appfactory.integration.test.utils.AFIntegrationTestException
     */
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Build deployment configs", dependsOnMethods = {"testCreateFornkBranch"})
    public void testSetBuildDelopymentConfigs() throws AFIntegrationTestException {
        JsonObject responseObject = buildRepoRestClient.setBuildDelopymentConfigs(applicationKey,
                initialVersion, "true", "false");
        boolean setBuildDelopymentConfigsStatus = false;
        if (responseObject.get("autoDeploy").getAsString().equals("true") &&
                responseObject.get("autoBuild").getAsString().equals("true")) {
            setBuildDelopymentConfigsStatus = true;
        }
        Assert.assertEquals(setBuildDelopymentConfigsStatus, true, "failed to set build deployment config for" +
                initialVersion);
        JsonObject dataObject = buildRepoRestClient.getBuildAndRepoDataForkedRepo(applicationKey,
                "false", "false", "true",tenantAdmin);
        JsonObject versionObject = dataObject.get("trunk").getAsJsonObject().get("version").getAsJsonObject();
        boolean isAssert = true;
        if (!versionObject.get("isAutoDeploy").getAsBoolean()
                && versionObject.get("isAutoBuild").getAsBoolean()) {
            isAssert = false;
        }
        Assert.assertEquals(isAssert, false, "failed to set build deployment config for " +
                initialVersion);
    }

    /**
     * Calls getBuildAndDeployStatusForVersion method in BuildRepoClient
     *
     * @throws org.wso2.appfactory.integration.test.utils.AFIntegrationTestException
     */
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Get build and deploy status for version", dependsOnMethods =
            {"testSetBuildDelopymentConfigs"})
    public void testGetBuildAndDeployStatusForVersion() throws AFIntegrationTestException {
        JsonObject responseDataObject = buildRepoRestClient.
                getBuildAndDeployStatusForVersion(applicationKey, initialVersion);
        Assert.assertEquals(responseDataObject.get("buildStatus").getAsString()
                , "successful", "Build version getting failed");
        log.info("GetBuildAndDeployStatusForVersion successfully triggered");
    }

    /**
     * Calls getJenkinsURL method in BuildRepoClient
     *
     * @throws org.wso2.appfactory.integration.test.utils.AFIntegrationTestException
     */
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Get jenkins URL", dependsOnMethods = {"testGetBuildAndDeployStatusForVersion"})
    public void testGetJenkinsURL() throws AFIntegrationTestException {
        String response = buildRepoRestClient.getJenkinsURL();
        boolean isAssert = true;
        if (response.toString().contains("https")) {
            isAssert = false;
        }
        Assert.assertEquals(isAssert, false, "failed requesting JenkinsURL");
    }

    /**
     * Calls getBuildAndRepoDataForVersion method in BuildRepoClient
     *
     * @throws org.wso2.appfactory.integration.test.utils.AFIntegrationTestException
     */
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Get build and repo data for version", dependsOnMethods = {"testGetJenkinsURL"})
    public void testGetBuildAndRepoDataForVersion() throws AFIntegrationTestException {
        JsonObject dataObject = buildRepoRestClient.getBuildAndRepoDataForVersion(applicationKey, initialVersion,
                tenantAdmin);
        Assert.assertEquals(dataObject.get("key").toString(), applicationKey, "GetBuildAndRepoDataForVersion failed");
    }

    /**
     * Calls buildInfoByAppId method in BuildRepoClient
     *
     * @throws org.wso2.appfactory.integration.test.utils.AFIntegrationTestException
     */
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Build info by application id", dependsOnMethods = {"testGetBuildAndRepoDataForVersion"})
    public void testBuildInfoByAppId() throws AFIntegrationTestException {
        JsonArray dataArray = buildRepoRestClient.buildInfoByAppId(applicationKey);
        lastBuildID = dataArray.get(2).getAsJsonObject().get("value").getAsInt();
        boolean isBuildInfoByAppIdSuccses = false;
        if (lastBuildID >= 1) {
            isBuildInfoByAppIdSuccses = true;
        }
        Assert.assertEquals(isBuildInfoByAppIdSuccses, true, "failed to get App info from application key");
    }

    /**
     * Calls getBuildLogsUrl method in BuildRepoClient
     *
     * @throws org.wso2.appfactory.integration.test.utils.AFIntegrationTestException
     */
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Get build logs URL", dependsOnMethods = {"testBuildInfoByAppId"})
    public void tesGetBuildLogsUrl() throws AFIntegrationTestException {
        String responseData = buildRepoRestClient.getBuildLogsUrl(applicationKey, initialVersion, lastBuildID + "");
        boolean isAssert = true;
        if (responseData.contains("job")) {
            isAssert = false;
        }
        Assert.assertEquals(isAssert, false, "failed to get build URL");

    }

    /**
     * Calls getBuildAndRepoData method in BuildRepoClient
     *
     * @throws org.wso2.appfactory.integration.test.utils.AFIntegrationTestException
     */
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Get build and repo data", dependsOnMethods = {"tesGetBuildLogsUrl"})
    public void testGetBuildAndRepoData() throws AFIntegrationTestException {
        JsonArray dataArray = buildRepoRestClient.getBuildAndRepoData(applicationKey, "false", "false", "true");
        boolean isAssert = true;
        if (dataArray.size() == 2) {
            isAssert = false;
        }
        Assert.assertEquals(isAssert, false, "failed to get build URL");
    }

    @AfterClass(alwaysRun = true)
    public void destroy(){
        super.cleanup();
    }
}

