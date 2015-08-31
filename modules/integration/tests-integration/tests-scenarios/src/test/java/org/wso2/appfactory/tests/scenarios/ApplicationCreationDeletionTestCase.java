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

import com.gitblit.models.RepositoryModel;
import com.gitblit.utils.RpcUtils;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.protocol.HTTP;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.appfactory.integration.test.utils.*;
import org.wso2.appfactory.integration.test.utils.rest.ApplicationClient;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ApplicationCreationDeletionTestCase extends AFIntegrationTest {
    private static final Log log = LogFactory.getLog(ApplicationCreationDeletionTestCase.class);
    public static final String APP_TYPE_WAR = "war";
    private static final String INITIAL_STAGE = "Development";
    private ApplicationClient appMgtRestClient;
	private final String appName = "foo_" + APP_TYPE_WAR + "_bar";
	private static final int MAX_SUCCESS_HTTP_STATUS_CODE = 299;

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
	    boolean isSuccess = checkApplicationComponentsCreationStatus();
	    Assert.assertEquals(isSuccess, true, "Application Creation failed.");
        log.info("Application creation is completed.");
    }

	@SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
	@Test(description = "Delete the created application", dependsOnMethods = {"testCreateApplication"})
	public void testDeleteApplication() throws Exception{
		appMgtRestClient.deleteApplication(defaultAdmin, appName);
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
        populator.waitUntilApplicationCreationCompletes(5000L, 5, defaultAdmin, defaultAdminPassword,
                                                        applicationKey, applicationName);
    }

	protected boolean checkApplicationComponentsCreationStatus() throws AFIntegrationTestException {
		boolean isSuccess;
		try {
			log.info("Checking the existance of repo");
			isSuccess = isGitRepoExist(appName, AFIntegrationTestUtils.getPropertyValue(AFConstants.URLS_GIT),
			                           AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_TENANT_TENANT_DOMAIN),
			                           AFIntegrationTestUtils.getPropertyValue(AFConstants.CREDENTIAL_GIT_USERNAME),
			                           AFIntegrationTestUtils.getPropertyValue(AFConstants.CREDENTIAL_GIT_PASSWORD));
		} catch (Exception e){
			log.error("Check is repo exists failed ", e);
			throw new AFIntegrationTestException("Check is repo exists failed " + e);
		}
		if(!isSuccess){
			return false;
		}
		try {
			log.info("Checking the existance of build job");
			isSuccess = isJenkinsJobExists(AFIntegrationTestUtils.getPropertyValue(AFConstants.URLS_JENKINS),
			                               AFIntegrationTestUtils.getJenkinsJobName(appName,
			                                        AFIntegrationTestUtils.getPropertyValue(
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

	/**
	 * Returns whether the given git repo exists
	 *
	 * @param applicationName name of the application
	 * @param baseUrl base url of git server
	 * @param tenantDomain current tenant domain
	 * @param username username for git server
	 * @param password for git server
	 * @return is git repo exists
	 */
	protected boolean isGitRepoExist(String applicationName, String baseUrl, String tenantDomain, String username, String password)
			throws IOException {
		boolean repoExists = false;
		String fullQulifiedRepoName = tenantDomain + "/" + applicationName;
		Map<String, RepositoryModel> repoMap = RpcUtils.getRepositories(baseUrl, username, password.toCharArray());
		for (Map.Entry<String, RepositoryModel> entry : repoMap.entrySet()) {
			String key = entry.getKey().split("r/")[1];
			repoExists = fullQulifiedRepoName.equals(key.split(".git")[0]);
			if (repoExists) {
				return repoExists;
			}
		}
		return repoExists;
	}

	/**
	 * Returns whether the jenkins job exists
	 *
	 * @param jenkinsUrl jenkins base url
	 * @param jobName job name
	 * @param username jenkins username
	 * @param password jenkins user password
	 * @return is jenkins job exists
	 */
	protected boolean isJenkinsJobExists (String jenkinsUrl, String jobName, String username, String password)
			throws Exception {
		Map<String, String> headers = new HashMap<String, String>();
		Header header = BasicScheme.authenticate(new UsernamePasswordCredentials(username, password), HTTP.UTF_8, false);
		headers.put(header.getName(), header.getValue());
		final String wrapperTag = "JobNames";
		boolean isExists;

		String queryParam = URLEncoder.encode("wrapper=" + wrapperTag + "&xpath="
		                                      + String.format("/*/job/name[text()='%s']", jobName),HTTP.UTF_8);
		URL jenkinsURL = new URL(jenkinsUrl + "/job/" + tenantDomain + "/api/xml");
		HttpResponse jobExistResponse =  HttpRequestUtil.doPost(jenkinsURL, queryParam, headers);

		if (!isSuccessfulStatusCode(jobExistResponse.getResponseCode())) {
			final String errorMsg =
					"Unable to check the existence of job " + jobName + ". jenkins returned, http status : " +
					jobExistResponse.getResponseCode();
			log.error(errorMsg);
			throw new AFIntegrationTestException(errorMsg);
		}

		InputStream stream = new ByteArrayInputStream(jobExistResponse.getData().getBytes(StandardCharsets.UTF_8));
		StAXOMBuilder builder = new StAXOMBuilder(stream);
		isExists = builder.getDocumentElement().getChildElements().hasNext();

		return isExists;
	}

	private boolean isSuccessfulStatusCode(int httpStatusCode) {
		return (httpStatusCode >= HttpStatus.SC_OK && httpStatusCode < MAX_SUCCESS_HTTP_STATUS_CODE);
	}

}
