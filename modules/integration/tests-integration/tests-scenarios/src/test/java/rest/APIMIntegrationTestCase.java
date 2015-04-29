package rest;

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
		client = new APIMIntegrationRestClient(getBEServerURL(), getAdminUsername(), getAdminPassword());
	}

	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.PLATFORM })
	@Test(description = "Testing create application at APIM")
	public void testCreateApplication() throws Exception {
		client.createApplication(getDefaultAppKey(), getAdminUsername());
		//add api - binali
		//subscribe - binali
		String[] apisOfApp = client.getAPIsOfApp(getDefaultAppKey(), getAdminUsername());
		Assert.assertEquals(apisOfApp, 1, "Expected APIs of Application");
		String[] savedKeys = client.getSavedKeys(getDefaultAppKey(), getAdminUsername());
		//Assertion whether number of savedKeys are 4
	}


}
