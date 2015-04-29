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
package rest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.InetAddress;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Date;
import java.util.UUID;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.appfactory.integration.test.utils.AFConstants;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTest;
import org.wso2.appfactory.integration.test.utils.rest.ApplicationRestClient;
import org.wso2.carbon.analytics.hive.stub.HiveExecutionServiceStub;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.AutomationContext;

/**
 * Telemetry test case
 */
public class BAMDataPublisherTestCase extends AFIntegrationTest {
	private static final int HIVE_STUB_TIMEOUT = 60000;

	String activityJSON = "['{'\"timestamp\":\"{0}\",\"item\":\"{1}\",\"action\":\"click\"'}'," +
			              "'{'\"timestamp\":\"{0}\",\"appName\":\"apptest\",\"appVersion\":\"1.0.0\"," +
			              "\"item\":\"{1}\",\"action\":\"click\",\"appKey\":\"apptest\"'}'," +
			              "'{'\"timestamp\":\"{0}\",\"appName\":\"apptest\",\"appVersion\":\"1.0.0\"," +
			              "\"item\":\"{1}\",\"action\":\"click\",\"appKey\":\"apptest\"'}']";

	@BeforeClass(alwaysRun = true)
	public void setEnvironment() throws Exception {
	}

	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.PLATFORM })
	@Test(description = "Testing user activity publishing to BAM")
	public void testUserActivtyPublish() throws Exception {
		String itemValue = UUID.randomUUID().toString();
		Object[] values = new Object[] { Long.toString(new Date().getTime()), itemValue };
		activityJSON = MessageFormat.format(activityJSON, values);
		ApplicationRestClient appMgtRestClient =
		                                    new ApplicationRestClient(
                                                    utils.getPropertyValue(AFConstants.URLS_APPFACTORY),
                                                    utils.getAdminUsername(),
                                                    utils.getPropertyValue(AFConstants.DEFAULT_TENANT_ADMIN_PASSWORD));
		appMgtRestClient.publishUserActivity(activityJSON);
		verify(itemValue);

	}

	private void verify(String itemValue) throws Exception {
		String[] queries = getHiveQueries("UserActivityScript");
		HiveExecutionServiceStub hiveStub = getHiveExecutionStub();
		hiveStub.executeHiveScript(null, queries[0]);
		String sqlQuery = MessageFormat.format(queries[1], itemValue);
		HiveExecutionServiceStub.QueryResult[] results = hiveStub.executeHiveScript(null, sqlQuery);
		if (results == null || results.length == 0) {
			Assert.fail("Recieved result is null or empty");
		} else if (results[0].getResultRows().length == 3) {
			Assert.assertEquals(results[0].getResultRows().length, 3, "Recieved a wrong result set length");
		} else {
			Assert.fail("Recieved resultset size. It MUST be 3");
		}

	}

	private HiveExecutionServiceStub getHiveExecutionStub() throws Exception {
		ConfigurationContext configContext =
		                                     ConfigurationContextFactory.createConfigurationContextFromFileSystem(null);
		String backendUrl = utils.getPropertyValue(AFConstants.URLS_BAM);
        final AutomationContext automationContext = utils.getAutomationContext();
        String loggedInSessionCookie =
		                               super.login(backendUrl,
                                                   automationContext.getSuperTenant().getTenantAdmin().getUserName(),
                                                   automationContext.getSuperTenant().getTenantAdmin().getPassword(),
                                                   InetAddress.getLocalHost().getHostAddress());

		String EPR = backendUrl + "services/HiveExecutionService";
		HiveExecutionServiceStub hiveStub = new HiveExecutionServiceStub(configContext, EPR);
		ServiceClient client = hiveStub._getServiceClient();
		Options option = client.getOptions();
		option.setTimeOutInMilliSeconds(10 * HIVE_STUB_TIMEOUT);
		option.setManageSession(true);
		option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
		                   loggedInSessionCookie);
		return hiveStub;
	}

	private String[] getHiveQueries(String resourceName) throws Exception {
		String[] queries = new String[]{};
		URL url = BAMDataPublisherTestCase.class.getClassLoader().getResource(resourceName);
		BufferedReader bufferedReader = null;
		try {
			bufferedReader =
			                 new BufferedReader(
			                                    new FileReader(
			                                                   new File(url.toURI()).getAbsolutePath()));
			String script = "";
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				script += line;
			}
			queries = script.split(";");
		} finally {
			if (bufferedReader != null) {
				bufferedReader.close();
			}
		}
		return queries;
	}

	@AfterClass(alwaysRun = true)
	public void destroy() throws Exception {
		super.cleanup();
	}

}
