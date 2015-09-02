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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.appfactory.integration.test.utils.AFConstants;
import org.wso2.appfactory.integration.test.utils.AFDefaultDataPopulator;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTest;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestUtils;
import org.wso2.appfactory.integration.test.utils.rest.ApplicationClient;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

public class ApplicationCreationDeletionTestCase extends AFIntegrationTest {
    private static final Log log = LogFactory.getLog(ApplicationCreationDeletionTestCase.class);
    public static final String APP_TYPE_WAR = "war";
    private static final String INITIAL_STAGE = "Development";
	private static final String DEFAULT_APP_ARTIFACT_VERSION = "default-SNAPSHOT";
	private static final String DEFAULT_APP_RUNTIME_ALIAS = "as";
	private ApplicationClient appMgtRestClient;
	private final String appName = "foo_" + APP_TYPE_WAR + "_bar";

	static {
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		});
	}

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        appMgtRestClient = new ApplicationClient(AFserverUrl, defaultAdmin, defaultAdminPassword);
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Create application using rest api")
    public void testCreateApplication() throws Exception {

        log.info("Creating application of type :" + APP_TYPE_WAR + " with name :" + appName);
        createApplication(appName, appName, appName, APP_TYPE_WAR);
        log.info("Application creation is completed.");
    }

	@SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
	@Test(description = "Delete the created application", dependsOnMethods = {"testCreateApplication"})
	public void testDeleteApplication() throws Exception {
		appMgtRestClient.deleteApplication(defaultAdmin, appName);
		// Wait till Create Application completion
		AFDefaultDataPopulator populator = new AFDefaultDataPopulator();
		populator.waitUntilApplicationDeletionCompletes(10000L, 8, defaultAdmin, defaultAdminPassword,
		                                                appName, appName, "war",
		                                                DEFAULT_APP_ARTIFACT_VERSION, INITIAL_STAGE,
		                                                DEFAULT_APP_RUNTIME_ALIAS,
		                                                AFIntegrationTestUtils.getPropertyValue(
				                                                AFConstants.DEFAULT_TENANT_TENANT_ID));
	}


    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
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
        populator.waitUntilApplicationCreationCompletes(10000L, 8, defaultAdmin, defaultAdminPassword,
                                                        applicationKey, applicationName, "war",
                                                        DEFAULT_APP_ARTIFACT_VERSION, INITIAL_STAGE,
                                                        DEFAULT_APP_RUNTIME_ALIAS,
                                                        AFIntegrationTestUtils.getPropertyValue(
		                                                        AFConstants.DEFAULT_TENANT_TENANT_ID));
    }



}
