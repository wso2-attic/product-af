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



import net.minidev.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.appfactory.integration.test.utils.AFConstants;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTest;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestUtils;
import org.wso2.appfactory.integration.test.utils.rest.IssueTrackerRestClient;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;

public class IssueTrackerTestCase extends AFIntegrationTest{

    private static final String SUMMARY = "Testing issue tracker ";
    private static final String DESCRIPTION = "NPE when login in to issue";
    private static final String type = "NEW_FEATURE";
    private static final String PRIORITY = "HIGH";
    private static final String STATUS = "RESOLVED";
    private static final String ASSIGNEE = "admin";
    private static final String SEVERITY ="NONE";
    private static final String COMMENT ="this is not a bug";
    private static String projectKey = "ja001";
    private static String issuePKey = "ja001-3";

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
    }


    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.PLATFORM })
    @Test(description = "Testing Issue API's")
    public void testCreateIssue() throws Exception {
        IssueTrackerRestClient issueTrackerRestClient = new IssueTrackerRestClient(
                AFIntegrationTestUtils.getPropertyValue(AFConstants.URLS_APPFACTORY),
                AFIntegrationTestUtils.getAdminUsername(), AFIntegrationTestUtils.getPropertyValue(
                AFConstants.DEFAULT_TENANT_ADMIN_PASSWORD));
        projectKey = AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_APP_KEY);
        issuePKey = createIssue(issueTrackerRestClient);
        Assert.assertEquals(issuePKey.startsWith(projectKey),true,
                "Issue adding failed");
        boolean isSuccses = editIssue(issueTrackerRestClient);
        Assert.assertEquals(isSuccses,true,
                "Issue editing failed");
        boolean isNull = getIssue(issueTrackerRestClient);
        Assert.assertEquals(isNull,true,
                "error while getting issue");
        Assert.assertEquals(addComment(issueTrackerRestClient), true, "Comment saving failed");
        Assert.assertEquals(addIssue(issueTrackerRestClient), true, "Issue getting failed");
        Assert.assertEquals(getProjectVersion(issueTrackerRestClient),true,"Project Version getting failed");

    }
    /**
     * Calls save issue method
     *
     * @param issueTrackerRestClient issueTrackerRestClient
     * @return new issue id
     * @throws Exception
     */
    private String createIssue(IssueTrackerRestClient issueTrackerRestClient) throws Exception {
        JSONObject issueString = new JSONObject();
        issueString.put("summary", SUMMARY);
        issueString.put("description", DESCRIPTION);
        issueString.put("type",type);
        issueString.put("priority", PRIORITY);
        issueString.put("status", STATUS);
        issueString.put("assignee", ASSIGNEE);
        issueString.put("versionId",AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_VERSION_ONE_SRC));
        issueString.put("severity", SEVERITY);
        issueString.put("version",AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_VERSION_ONE_SRC));
        return issueTrackerRestClient.saveIssue(defaultAppKey, issueString.toJSONString(), "addIssue");
    }

    /**
     * Calls edit issue method
     *
     * @param issueTrackerRestClient issueTrackerRestClient
     * @return value of success element of the response
     * @throws Exception
     */
    private boolean editIssue(IssueTrackerRestClient issueTrackerRestClient) throws Exception {
        JSONObject issueEditJson = new JSONObject();
        issueEditJson.put("key",issuePKey);
        issueEditJson.put("summary", SUMMARY);
        issueEditJson.put("description", DESCRIPTION);
        issueEditJson.put("type","BUG");
        issueEditJson.put("priority", PRIORITY);
        issueEditJson.put("status", STATUS);
        issueEditJson.put("assignee", ASSIGNEE);
        issueEditJson.put("versionId",AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_VERSION_ONE_SRC));
        issueEditJson.put("severity", SEVERITY);
        issueEditJson.put("version",AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_VERSION_ONE_SRC));
        return  issueTrackerRestClient.editIssue(issuePKey,issueEditJson.toJSONString(),"editIssue");
    }

    /**
     * Calls getIssue method in IssueTrackerRestClient
     *
     * @param issueTrackerRestClient issueTrackerRestClient
     * @return whether the application key is equal to  application key in the responce
     * @throws Exception
     */
    private boolean  getIssue(IssueTrackerRestClient issueTrackerRestClient) throws Exception {
       return issueTrackerRestClient.getIssue(issuePKey,projectKey);
    }

    /**
     * Calls addComment method in IssueTrackerRestClient
     *
     * @param issueTrackerRestClient issueTrackerRestClient
     * @return value of success element of the response
     * @throws Exception
     */
    private  boolean addComment(IssueTrackerRestClient issueTrackerRestClient) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("description",COMMENT);
        return  issueTrackerRestClient.addComment(issuePKey, jsonObject.toString(), "addComment");
    }

    /**
     * Calls getProjectVersion method in IssueTrackerRestClient
     *
     * @param issueTrackerRestClient issueTrackerRestClient
     * @return whether the array size is lager that 0 or not
     * @throws Exception
     */
    private boolean getProjectVersion(IssueTrackerRestClient issueTrackerRestClient) throws Exception {
        return issueTrackerRestClient.getProjectVersion(AFIntegrationTestUtils.getPropertyValue(AFConstants.
                DEFAULT_APP_APP_KEY));
    }

    /**
     * Calls addIssue method in IssueTrackerRestClient
     *
     * @param issueTrackerRestClient issueTrackerRestClient
     * @return whether the application key is equal to application key in the responce
     * @throws Exception
     */
    private  boolean addIssue(IssueTrackerRestClient issueTrackerRestClient) throws Exception {
       return issueTrackerRestClient.addIssue(projectKey);
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}

