/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */
package org.wso2.appfactory.tests.scenarios;

import com.google.gson.JsonArray;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.appfactory.integration.test.utils.*;
import org.wso2.appfactory.integration.test.utils.rest.ApplicationClient;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;

public class ApplicationCreationTestCase extends AFIntegrationTest {
    private static final Log log = LogFactory.getLog(ApplicationCreationTestCase.class);
    public static final String APP_TYPE_WAR = "war";
    private static final String INITIAL_STAGE = "Development";
    private ApplicationClient appMgtRestClient;
	private final String appName = "foo_" + APP_TYPE_WAR + "_bar";
    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        appMgtRestClient = new ApplicationClient(AFserverUrl, defaultAdmin, defaultAdminPassword);
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Sample test method")
    public void testWarAppType() throws Exception {

        log.info("Creating application of type :" + APP_TYPE_WAR + " with name :" + appName);
        createApplication(appName, appName, appName, APP_TYPE_WAR);
	    boolean isSuccess = checkApplicationComponentsCreationStatus();
	    Assert.assertEquals(isSuccess, true, "Application Creation failed.");
        log.info("Application creation is completed.");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Get applications of user  <used in Dev Studio>")
    public void testGetApplicationsOfUser() throws AFIntegrationTestException{
        JsonArray resultArray =  appMgtRestClient.getApplicationsOfUser(defaultAdmin);
        boolean isAssert = false;
        if (resultArray.size()!=0){
            isAssert = true;
        }
	    Assert.assertEquals(isAssert, true, "Get applications of user failed.");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Get application versions in stage  <used in Dev Studio>")
    public void testGetAppVersionsInStage() throws AFIntegrationTestException{
        JsonArray resultArray =  appMgtRestClient.getAppVersionsInStage(defaultAdmin,INITIAL_STAGE , defaultAppKey);
        boolean isAssert = false;
        if (resultArray.size()!=0){
            isAssert=true;
        }
        Assert.assertEquals(isAssert,true,"Get application versions in stage failed.");
    }


    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
	    appMgtRestClient.deleteApplication(defaultAdmin, appName);
    }

    protected void createApplication(String applicationName, String applicationKey, String applicationDescription,
                                     String applicationType) throws Exception {

        if (appMgtRestClient.isAppNameAlreadyAvailable(applicationName) &&
            appMgtRestClient.isApplicationKeyAvailable(applicationKey)) {
            appMgtRestClient.createNewApplication(applicationName, applicationKey, applicationType,
                                                  defaultAdmin, applicationDescription);
        }

        // Wait till Create Application completion
        AFDefaultDataPopulator populator = new AFDefaultDataPopulator();
        populator.waitUntilApplicationCreationCompletes(5000L, 5, defaultAdmin, defaultAdminPassword,
                                                        applicationKey, applicationName);
    }

	protected boolean checkApplicationComponentsCreationStatus() throws AFIntegrationTestException {
		boolean isSuccess;
		try {
			log.info("Checking the existance of repo");
			isSuccess = AFIntegrationTestUtils.isGitRepoExist(appName, AFIntegrationTestUtils
					                                                 .getPropertyValue(AFConstants.URLS_GIT),
			                                                 AFIntegrationTestUtils.getPropertyValue(
					                                                 AFConstants.CREDENTIAL_GIT_USERNAME),
			                                                 AFIntegrationTestUtils.getPropertyValue(
					                                                 AFConstants.CREDENTIAL_GIT_PASSWORD));
		} catch (Exception e){
			log.error("Check is repo exists failed ", e);
			throw new AFIntegrationTestException("Check is repo exists failed " + e);
		}
		if(!isSuccess){
			return false;
		}
		try {
			log.info("Checking the existance of build job");
			isSuccess = AFIntegrationTestUtils.isJenkinsJobExists(AFIntegrationTestUtils.getPropertyValue(
					                                                     AFConstants.URLS_JENKINS),
			                                                     AFIntegrationTestUtils.getJenkinsJobName(appName
					                                                     , AFIntegrationTestUtils.getPropertyValue(
					                                                     AFConstants.DEFAULT_VERSION_NAME)),
			                                                     AFIntegrationTestUtils.getPropertyValue(
					                                                     AFConstants.CREDENTIAL_JENKINS_USERNAME),
			                                                     AFIntegrationTestUtils.getPropertyValue(
					                                                     AFConstants.CREDENTIAL_JENKINS_PASSWORD));
		} catch (Exception e) {
			log.error("Check is jenkins job exists failed ", e);
			throw new AFIntegrationTestException("Check is jenkins job exists failed " + e);
		}
		if(!isSuccess){
			return false;
		}
		return isSuccess;
	}

}
