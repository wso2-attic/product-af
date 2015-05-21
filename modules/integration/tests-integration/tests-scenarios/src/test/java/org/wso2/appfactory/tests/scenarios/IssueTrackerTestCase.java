/*
*Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.appfactory.tests.scenarios;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minidev.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.appfactory.integration.test.utils.AFConstants;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTest;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestUtils;
import org.wso2.appfactory.integration.test.utils.rest.ApplicationClient;
import org.wso2.appfactory.integration.test.utils.rest.IssueTrackerRestClient;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;

/**
 * This class is used to test all the REST api's in issuetracker feature
 */
public class IssueTrackerTestCase extends AFIntegrationTest {

    private static final String APPLICATION_TYPE ="war";
    private static final String SUMMARY = "Testing issue tracker ";
    private static final String DESCRIPTION = "NPE when login in to issue";
    private static final String TYPE = "NEW_FEATURE";
    private static final String UPDATE_TYPE = "BUG";
    private static final String PRIORITY = "HIGH";
    private static final String STATUS = "RESOLVED";
    private static final String ASSIGNEE = "admin";
    private static final String SEVERITY = "NONE";
    private static final String COMMENT = "this is not a bug";
    private static final String SUMMARY_KEY = "summary";
    private static final String ISSUE_KEY = "issue";
    private static final String TYPE_KEY = "type";
    private static final String DATA_KEY = "data";
    private static final String RESPONCE_BEAN_KEY = "responseBean";
    private static final String SUCCSESS = "success";
    private static final String DESCRIPTION_KEY = "description";
    private static final String PRIORITY_KEY = "priority";
    private static final String STATUS_KEY = "status";
    private static final String ASSIGNEE_KEY = "assignee";
    private static final String VERSION_ID_KEY = "versionId";
    private static final String SEVERITY_KEY = "severity";
    private static final String VERSION_KEY = "version";
    private static final String ISSUEPKEY = "key";



    private String userName = null;
    private static String applicationKey = null;
    private static String issuePKey = null;
    private static IssueTrackerRestClient issueTrackerRestClient;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        userName = AFIntegrationTestUtils.getAdminUsername();
        applicationKey = AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_APP_KEY);
        ApplicationClient  applicationClient = new ApplicationClient(AFIntegrationTestUtils.getPropertyValue(AFConstants
                .URLS_APPFACTORY),userName,AFIntegrationTestUtils.getPropertyValue(
                AFConstants.DEFAULT_TENANT_ADMIN_PASSWORD));
//        applicationClient.deleteApplication(userName,applicationKey);
//        applicationClient.createNewApplication(applicationKey,applicationKey,APPLICATION_TYPE,userName,
//                "Default Application");
//        Thread.sleep(60000);
        issueTrackerRestClient = new IssueTrackerRestClient(AFIntegrationTestUtils.getPropertyValue(AFConstants
                                .URLS_APPFACTORY),userName, AFIntegrationTestUtils.getPropertyValue(AFConstants
                                .DEFAULT_TENANT_ADMIN_PASSWORD));

    }

    /**
     * Calls save issue method
     *
     * @throws Exception
     */
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Creating a issue")
    public void testCreateIssue() throws Exception {
        JSONObject issueString = new JSONObject();
        issueString.put(SUMMARY_KEY, SUMMARY);
        issueString.put(DESCRIPTION_KEY, DESCRIPTION);
        issueString.put(TYPE_KEY, TYPE);
        issueString.put(PRIORITY_KEY, PRIORITY);
        issueString.put(STATUS_KEY, STATUS);
        issueString.put(ASSIGNEE_KEY, ASSIGNEE);
        issueString.put(VERSION_ID_KEY, AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_VERSION_ONE_SRC));
        issueString.put(SEVERITY_KEY, SEVERITY);
        issueString.put(VERSION_KEY, AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_VERSION_ONE_SRC));
        JsonObject dataObject = issueTrackerRestClient.saveIssue(applicationKey, issueString.toJSONString());
        issuePKey = dataObject.get(DATA_KEY).getAsString();
        System.out.println("########################## : " + issuePKey);
        Assert.assertEquals(issuePKey.startsWith(applicationKey), true,
                "Issue creating failed");

        JsonObject issueDetailObject = issueTrackerRestClient.getIssue(issuePKey, applicationKey, UPDATE_TYPE);
        JsonObject issueObject = issueDetailObject.getAsJsonObject().get(ISSUE_KEY).getAsJsonObject();
        String responseIssuType = issueObject.get(TYPE_KEY).getAsString();
        String responseIssueSummary = issueObject.get(SUMMARY_KEY).getAsString();
        boolean isAssert = true;
        if(responseIssuType.equals(TYPE) && responseIssueSummary.equals(SUMMARY)){
            isAssert = false;
        }
        Assert.assertEquals(isAssert, false,
                "Issue creating failed");
    }

    /**
     * Calls edit issue method
     *
     * @throws Exception
     */
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Editing a issue", dependsOnMethods = {"testCreateIssue"})
    public void testEditIssue() throws Exception {
        JSONObject issueEditJson = new JSONObject();
        issueEditJson.put(ISSUEPKEY, issuePKey);
        issueEditJson.put(SUMMARY_KEY, SUMMARY);
        issueEditJson.put(DESCRIPTION_KEY, DESCRIPTION);
        issueEditJson.put(TYPE_KEY, UPDATE_TYPE);
        issueEditJson.put(PRIORITY_KEY, PRIORITY);
        issueEditJson.put(STATUS_KEY, STATUS);
        issueEditJson.put(ASSIGNEE_KEY, ASSIGNEE);
        issueEditJson.put(VERSION_ID_KEY, AFIntegrationTestUtils.
                      getPropertyValue(AFConstants.DEFAULT_APP_VERSION_ONE_SRC));
        issueEditJson.put(SEVERITY_KEY, SEVERITY);
        issueEditJson.put(VERSION_KEY, AFIntegrationTestUtils.
                      getPropertyValue(AFConstants.DEFAULT_APP_VERSION_ONE_SRC));
        JsonObject dataObject = issueTrackerRestClient.editIssue(issuePKey, issueEditJson.toJSONString());
        boolean isSuccses = dataObject.getAsJsonObject(DATA_KEY).getAsJsonObject(RESPONCE_BEAN_KEY)
                .get(SUCCSESS).getAsBoolean();
        Assert.assertEquals(isSuccses, true,
                "Issue editing failed");
        JsonObject issueDetailObject = issueTrackerRestClient.getIssue(issuePKey, applicationKey, UPDATE_TYPE);
        JsonObject issueObject = issueDetailObject.getAsJsonObject().get(ISSUE_KEY).getAsJsonObject();
        String responseIssuType = issueObject.get(TYPE_KEY).getAsString();
        String responseIssueSummary = issueObject.get(SUMMARY_KEY).getAsString();
        boolean isAssert = true;
        if(responseIssuType.equals(UPDATE_TYPE) && responseIssueSummary.equals(SUMMARY)){
            isAssert = false;
        }
        Assert.assertEquals(isAssert, false,
                "Issue editing failed");
    }

    /**
     * Calls addComment method in IssueTrackerRestClient
     *
     * @throws Exception
     */
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Adding a comment", dependsOnMethods = {"testEditIssue"})
    public void testAddComment() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(DESCRIPTION_KEY, COMMENT);
        JsonObject dataObject = issueTrackerRestClient.addComment(issuePKey, jsonObject.toString());
        boolean isSuccses = dataObject.getAsJsonObject(DATA_KEY).getAsJsonObject("responseBean")
                .get("success").getAsBoolean();
        Assert.assertEquals(isSuccses, true, "Comment saving failed");
        JsonObject issueObject = issueTrackerRestClient.getIssue(issuePKey, applicationKey, UPDATE_TYPE);
        boolean isEqual = issueObject.get("comments").getAsJsonArray().get(0).getAsJsonObject().get("description")
                .getAsString().equals(COMMENT);
        Assert.assertEquals(isEqual, true, "Comment saving failed");
    }

    /**
     * Calls addIssue method in IssueTrackerRestClient
     *
     * @throws Exception
     */
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Getting project details", dependsOnMethods = {"testAddComment"})
    public void testgetProjetcDetails() throws Exception {
        JsonObject dataObject = issueTrackerRestClient.getProjectDetails(applicationKey);
        String appKey =dataObject.getAsJsonArray("project").get(0).getAsJsonObject().get("key")
                        .getAsString();
        Assert.assertEquals(appKey, applicationKey, "Issue getting failed");
    }

    /**
     * Calls getProjectVersion method in IssueTrackerRestClient
     *
     * @throws Exception
     */
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Geting a project version", dependsOnMethods = {"testgetProjetcDetails"})
    public void testGetProjectVersion() throws Exception {
        JsonArray dataArray = issueTrackerRestClient.getProjectVersion(applicationKey);
        boolean isAssert = false;
        if (dataArray.size() > 0) {
            isAssert = true;
        }
        Assert.assertEquals(isAssert, true, "Project Version getting failed");
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}

