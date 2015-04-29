package rest.admin;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTest;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;

/**
 * Test case to test build and repo rest endpoints.
 */
public class BuildRepoTestCase extends AFIntegrationTest {

	@BeforeClass(alwaysRun = true)
	public void setEnvironment() throws Exception {
	}

	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.PLATFORM})
	@Test(description = "Deploy artifact")
	public void deployArtifactTest() throws Exception {
//		String newUrl = generateCustomUrl(NEW_URL_STEM);
//		Map<String, String> msgBodyMap = new HashMap<String, String>();
//		msgBodyMap.put(REQUEST_KEY_ACTION, ACTION_ADD_NEW_CUSTOM_URL);
//		msgBodyMap.put(REQUEST_KEY_APPKEY, getPropertyValue(AFConstants.DEFAULT_APP_APP_KEY));
//		msgBodyMap.put(REQUEST_KEY_NEW_URL, newUrl);
//		HttpResponse httpResponse = getHttpResponse(msgBodyMap, EP_ADD_NEW_CUSTOM_URL);
//		Assert.assertEquals(httpResponse.getResponseCode(), HttpStatus.SC_OK,
//		                    "Adding new custom url is not success.");
	}
}
