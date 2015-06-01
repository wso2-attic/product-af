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

package org.wso2.appfactory.tests.scenarios.tenantadmin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTest;
import org.wso2.appfactory.integration.test.utils.rest.APIMIntegrationClient;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;

import java.util.ArrayList;
import java.util.List;
/**
 * Test cases for API integration
 */
public class APIMIntegrationTestCase extends AFIntegrationTest {

	private APIMIntegrationClient client;
    private static final Log log = LogFactory.getLog(APIMIntegrationTestCase.class);
    private static final String createApplicationAction="createApplication";
    private static final String getAPISOfAppAction="getAPIsOfApp";
    private static final String keyExistsInStagesAction= "keysExistsAllInStages";
    private static final String getSavedKeysAction="getSavedKeys";
    private static final String isSync="true";

    @BeforeClass(alwaysRun = true)
    public void setEnvironment()  {
        try {
            client = new APIMIntegrationClient(AFserverUrl, defaultAdmin, defaultAdminPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.PLATFORM })
    @Test(description = "Testing create application at APIM")
	public void testCreateApplication() throws Exception {
        Assert.assertEquals( client.createApplication(createApplicationAction,defaultAppKey, defaultAdmin),true,
                "Create Application");
	}

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.PLATFORM })
    @Test(description = "Testing cGet APIs of APP ")
    public void testGetAPIsOfApp() throws Exception {
        Assert.assertEquals(client.getAPIsOfApp(getAPISOfAppAction,defaultAppKey, defaultAdmin),true,"Get APIs of APP");
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.PLATFORM })
    @Test(description = "Get Saved Keys")
    public void testGetSavedKeys() throws Exception {
        Assert.assertEquals(client.getSavedKeys(getSavedKeysAction,defaultAppKey, defaultAdmin),true,"Get Saved Keys");
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.PLATFORM })
    @Test(description = "Key Exists in all stages")
    public void testKeysExistsInAllStages() throws Exception {
        Assert.assertEquals(client.keysExistsInAllStages(keyExistsInStagesAction,defaultAppKey, defaultAdmin,isSync),true,
                "Key Exists in all stages");
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }

}
