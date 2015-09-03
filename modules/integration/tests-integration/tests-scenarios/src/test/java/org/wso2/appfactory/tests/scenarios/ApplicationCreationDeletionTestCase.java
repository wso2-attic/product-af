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
import org.testng.annotations.*;
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
	private ApplicationClient appMgtRestClient;
	private String appName;
	private String apptype;
	private String initialStage;
	private String defaultArtifactVersion;
	private String runtimeAlias;
	private String extension;

	static {
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		});
	}

	@Factory(dataProvider = "userModeDataProvider")
	public ApplicationCreationDeletionTestCase(String apptype, String initialStage, String defaultArtifactVersion,
	                                           String runtimeAlias, String extension) {
		this.apptype = apptype;
		this.initialStage = initialStage;
		this.defaultArtifactVersion = defaultArtifactVersion;
		this.runtimeAlias = runtimeAlias;
		this.extension = extension;
	}

	@DataProvider
	public static Object[][] userModeDataProvider() {
		return new Object[][]{
				new Object[]{"war", "Development", "default-SNAPSHOT" , "as", "war"},
				new Object[]{"jaxrs", "Development", "default-SNAPSHOT" , "as", "war"},
				new Object[]{"jaxws", "Development", "default-SNAPSHOT" , "as", "war"},
				new Object[]{"jaggery", "Development", "default-SNAPSHOT" , "as", ""},
				new Object[]{"dbs", "Development", "default-SNAPSHOT" , "as", "dbs"},
				/*new Object[]{"esb", "Development", "default-SNAPSHOT" , "esb", "car"},*/
		};
	}

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        appMgtRestClient = new ApplicationClient(AFserverUrl, defaultAdmin, defaultAdminPassword);
	    appName = "foo_" + this.apptype + "_bar";
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Create application using rest api")
    public void testCreateApplication() throws Exception {

	    log.info("Creating application of type :" + this.apptype + " with name :" + appName);
	    if (appMgtRestClient.isAppNameAlreadyAvailable(appName) &&
	        appMgtRestClient.isApplicationKeyAvailable(appName)) {
		    appMgtRestClient.createNewApplication(appName, appName, this.apptype,
		                                          defaultAdmin, appName);
		    // Wait till Create Application completion
		    AFDefaultDataPopulator populator = new AFDefaultDataPopulator();
		    populator.waitUntilApplicationCreationCompletes(10000L, 8, defaultAdmin, defaultAdminPassword,
		                                                    appName, appName, this.extension,
		                                                    this.defaultArtifactVersion, this.initialStage,
		                                                    this.runtimeAlias,
		                                                    AFIntegrationTestUtils.getPropertyValue(
				                                                    AFConstants.DEFAULT_TENANT_TENANT_ID));
	    }
        log.info("Application creation is completed.");
    }

	@SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
	@Test(description = "Delete the created application", dependsOnMethods = {"testCreateApplication"})
	public void testDeleteApplication() throws Exception {
		appMgtRestClient.deleteApplication(defaultAdmin, appName);
		// Wait till Create Application completion
		AFDefaultDataPopulator populator = new AFDefaultDataPopulator();
		populator.waitUntilApplicationDeletionCompletes(10000L, 8, defaultAdmin, defaultAdminPassword,
		                                                appName, appName, this.extension,
		                                                this.defaultArtifactVersion, this.initialStage,
		                                                this.runtimeAlias,
		                                                AFIntegrationTestUtils.getPropertyValue(
				                                                AFConstants.DEFAULT_TENANT_TENANT_ID));
	}


    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }

}
