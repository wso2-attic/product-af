<<<<<<< HEAD:modules/integration/tests-integration/tests-scenarios/src/test/java/rest/APIMIntegrationTestCase.java
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
=======
package org.wso2.appfactory.tests.scenarios.tenantadmin;
>>>>>>> d56c99ef7f7d3706db8ff7dfc761a2f1d9e7e519:modules/integration/tests-integration/tests-scenarios/src/test/java/org/wso2/appfactory/tests/scenarios/tenantadmin/APIMIntegrationTestCase.java

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTest;
import org.wso2.appfactory.integration.test.utils.rest.APIMIntegrationClient;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;

public class APIMIntegrationTestCase extends AFIntegrationTest {

	private APIMIntegrationClient client;

	public APIMIntegrationTestCase() throws Exception {
		client = new APIMIntegrationClient(AFserverUrl, defaultAdmin, defaultAdminPassword);
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
