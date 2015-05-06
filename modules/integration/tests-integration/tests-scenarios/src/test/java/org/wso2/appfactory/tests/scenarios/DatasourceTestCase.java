package org.wso2.appfactory.tests.scenarios;

import com.google.gson.JsonObject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.appfactory.integration.test.utils.AFConstants;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTest;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestException;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestUtils;
import org.wso2.appfactory.integration.test.utils.rest.DatasourceClient;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;

/**
 * Test case for datasources
 */
public class DatasourceTestCase extends AFIntegrationTest {
    public static final String TEST_DATA_SOURCE = "testDataSrc122";
    private DatasourceClient datasourceClient = null;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        datasourceClient = new DatasourceClient(AFserverUrl, defaultAdmin, defaultAdminPassword);
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Add new datasource")
    public void testAddNewDatasource() throws AFIntegrationTestException {
        JSONObject responseObj = datasourceClient.createDatasource(TEST_DATA_SOURCE, "Development",
                                                                   "jdbc:mysql://localhost:3306/test12345",
                                                                   "My test datasource", "mysql", "root",
                                                                   "123456", false,
                                                                   AFIntegrationTestUtils.getPropertyValue(
                                                                           AFConstants.DEFAULT_APP_APP_KEY));
        JSONArray results = (JSONArray) responseObj.get("Development");
        Assert.assertEquals(Boolean.parseBoolean((String) results.get(0)), true);
    }

    //TODO:
  /*  @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Update an existing datasource", dependsOnMethods = {"testAddNewDatasource"})
    public void testUpdateExistingDatasource() throws AFIntegrationTestException {
        JSONObject responseObj = datasourceClient.editDatasource(TEST_DATA_SOURCE, "Development",
                                                                 "jdbc:mysql://localhost:3306/test12345",
                                                                 "My edited test datasource", "mysql", "root",
                                                                 "123123", true,
                                                                 AFIntegrationTestUtils.getPropertyValue(
                                                                         AFConstants.DEFAULT_APP_APP_KEY));
    }
*/

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Delete a datasource", dependsOnMethods = {"testAddNewDatasource"})
    public void testDeleteExistingDatasource() throws AFIntegrationTestException {
        JSONObject responseObj = datasourceClient.deleteDatasource(TEST_DATA_SOURCE, "Development",
                                                                   AFIntegrationTestUtils.getPropertyValue(
                                                                           AFConstants.DEFAULT_APP_APP_KEY));
        JSONObject results = (JSONObject) responseObj.get("Development");
        Assert.assertEquals(results.getBoolean("error"), false);
    }


    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}
