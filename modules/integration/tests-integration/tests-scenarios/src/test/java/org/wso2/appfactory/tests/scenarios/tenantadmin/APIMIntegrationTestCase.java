package org.wso2.appfactory.tests.scenarios.tenantadmin;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.appfactory.integration.test.utils.AFConstants;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTest;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestUtils;
import org.wso2.appfactory.integration.test.utils.rest.APIMIntegrationRestClient;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;

/**
 * Created by muthulee on 4/29/15.
 */
public class APIMIntegrationTestCase extends AFIntegrationTest {

	private APIMIntegrationRestClient client;

	public APIMIntegrationTestCase() throws Exception {
		client = new APIMIntegrationRestClient(AFserverUrl, defaultAdmin, defaultAdminPassword);
	}

	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.PLATFORM })
	@Test(description = "Testing create application at APIM")
	public void testCreateApplication() throws Exception {
		client.createApplication(defaultAppKey, defaultAdmin);
		//add api - binali
		//subscribe - binali
		String[] apisOfApp = client.getAPIsOfApp(defaultAppKey, defaultAdmin);
		Assert.assertEquals(apisOfApp, 1, "Expected APIs of Application");
		String[] savedKeys = client.getSavedKeys(defaultAppKey, defaultAdmin);
		//Assertion whether number of savedKeys are 4
	}



}
