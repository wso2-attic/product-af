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
package org.wso2.appfactory.tests.scenarios;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.appfactory.integration.test.utils.AFConstants;
import org.wso2.appfactory.integration.test.utils.AppFactoryIntegrationTest;
import org.wso2.carbon.appfactory.repository.mgt.client.AppfactoryRepositoryClient;
import org.wso2.carbon.appfactory.repository.mgt.git.GitAgent;
import org.wso2.carbon.appfactory.repository.mgt.git.GitRepositoryClient;
import org.wso2.carbon.appfactory.repository.mgt.git.JGitAgent;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

/**
 * This test case cover below scenarios on updating root pom.xml of an application
 * 1. update version, groupId, packaging, name
 * 2. update dependencies(positive test case)
 * 3. replace root pom.xml
 * 4. delete root pom.xml
 */
public class GitOperationsTestCase extends AppFactoryIntegrationTest {
	private static final Log log = LogFactory.getLog(GitOperationsTestCase.class);

	private static AppfactoryRepositoryClient client;

	@BeforeClass(alwaysRun = true)
	public void setEnvironment() throws Exception {
		initWithTenantAndApplicationCreation();
		// initiate repository client
		GitAgent gitAgent = new JGitAgent();
		client = new GitRepositoryClient(gitAgent);
		client.init(getPropertyValue(AFConstants.DEFAULT_TENANT_ADMIIN) + "@" +
		            getPropertyValue(AFConstants.DEFAULT_TENANT_TENANT_DOMAIN),
		            getPropertyValue(AFConstants.DEFAULT_TENANT_ADMIN_PASSWORD));

	}

	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.PLATFORM })
	@Test(description = "Modify, Commit and Push")
	public void cloneChangeAndPush() throws Exception {
		// working directory
		File workDir = new File(CarbonUtils.getTmpDir() + "/" + UUID.randomUUID());
		// construct repo url for default application
		String repoURL = getRepoUrl(getPropertyValue(AFConstants.DEFAULT_TENANT_TENANT_DOMAIN),
		                            getPropertyValue(AFConstants.DEFAULT_APP_APP_KEY));
		boolean result = client.retireveMetadata(repoURL, false, workDir);
		Assert.assertEquals(result, true, "Failed to clone.");

		updateFiles(workDir);
		client.commitLocally("Committing changed files.", true, workDir);
		boolean pushSucceeded = client.pushLocalCommits(repoURL, "master", workDir);
		Assert.assertEquals(pushSucceeded, true, "Push failed");

		// clean temp directory
		FileUtils.forceDelete(workDir);
	}

	public String getRepoUrl(String tenantDomain, String appKey) throws XPathExpressionException {
		return getPropertyValue(AFConstants.URLS_GIT) + "git" + "/" + tenantDomain + "/" + appKey +
		       ".git";
	}

	public static void updateFiles( File workDir)
			throws IOException, XmlPullParserException {
		String[] fileExtension = { "*" };

		Collection files = FileUtils.listFiles(workDir, fileExtension, true);
		for (Object fileObj : files) {
			File file = (File) fileObj;
			file.setLastModified(new Date().getTime());
		}
	}


	@AfterClass(alwaysRun = true)
	public void destroy() throws Exception {
		super.cleanup();
	}
}
