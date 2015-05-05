package org.wso2.appfactory.tests.scenarios;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.appfactory.integration.test.utils.AFConstants;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTest;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestUtils;
import org.wso2.appfactory.integration.test.utils.rest.APIMIntegrationClient;
import org.wso2.appfactory.integration.test.utils.rest.BuildRepoClient;
import org.wso2.appfactory.integration.test.utils.rest.DatasourceClient;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;

/**
 * Created by punnadi on 4/29/15.
 */
public class DatasourceTestCase extends AFIntegrationTest{
    private DatasourceClient datasourceClient = null;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        datasourceClient = new DatasourceClient(AFserverUrl, defaultAdmin, defaultAdminPassword);
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Add new datasource")
    public void addNewDatasource() throws Exception {
        datasourceClient.createDatasource("testDataSource", "Development", "jdbc:mysql://localhost:3306/test",
                                          "My test datasource", "mysql", "root", "123456", false,
                                          AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_APP_KEY));

    }


    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}
