package org.wso2.appfactory.tests.scenarios.tenantadmin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.appfactory.integration.test.utils.AFConstants;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTest;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestUtils;
import org.wso2.appfactory.integration.test.utils.rest.BuildRepoClient;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;

/**
 * Test case to test build and repo rest endpoints.
 */
public class BuildRepoTestCase extends AFIntegrationTest {

	private static BuildRepoClient buildRepoRestClient = null;
	private static String applicationKey = null;
	private static final String INITIAL_STAGE = "Development";
	private static final String TAG_NAME = "";
	private static final String DEPLOY_ACTION = "deploy";
	private static String initialVersion = null;
	private static String firstVersion = null;

	private static final Log log = LogFactory.getLog(BuildRepoTestCase.class);

	@BeforeClass(alwaysRun = true)
	public void setEnvironment() throws Exception {
		buildRepoRestClient = new
				BuildRepoClient(AFIntegrationTestUtils.getPropertyValue(AFConstants.URLS_APPFACTORY),
				                AFIntegrationTestUtils.getAdminUsername(),
				                AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_TENANT_ADMIN_PASSWORD));
		applicationKey = AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_APP_KEY);
		initialVersion = AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_VERSION_ONE_SRC);
		firstVersion = AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_VERSION_ONE_TARGET);
	}

	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.PLATFORM})
	@Test(description = "Deploy artifact")
	public void deployArtifactTest() throws Exception {
		buildRepoRestClient.deployArtifact(applicationKey, INITIAL_STAGE, initialVersion, TAG_NAME, DEPLOY_ACTION);
		log.info("Deploy action successfully triggered");
	}
}
