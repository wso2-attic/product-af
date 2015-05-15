package org.wso2.appfactory.tests.scenarios.tenantadmin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTest;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestException;
import org.wso2.appfactory.integration.test.utils.rest.PropertyClient;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;

/**
 * Created by binalip91 on 5/13/15.
 */
public class PropertyTestCase extends AFIntegrationTest {
    private PropertyClient client;
    private static final Log log = LogFactory.getLog(APIMIntegrationTestCase.class);
    private static final String actionCreateResource ="createResource";
    private static final String actionDeleteResource ="deleteResource";
    private static final String actionUpdateResource ="updateResource";
    private static final String actionGetAllResources="getAllDependencies";

    private static final String resourseName="Property-Test";
    private static final String resourceDesc="Property-Test_Description";
    private static final String updatedDesc="Property-Test_UpdatedDesc";
    private static final String resourseMediaType="Registry";
    private static final String contentValue="Property-Test_Value";
    private static final String updatedValue="Property-Test_UpdatedValue";
    private static final String stage="Development";
    private static final String copyToAll="false";

    @BeforeClass(alwaysRun = true)
    public void setEnvironment()  {
        try {
            client = new PropertyClient(AFserverUrl, defaultAdmin, defaultAdminPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.PLATFORM })
    @Test(description = "Testing create Property")
    public void testCreateResource() throws Exception{
        Assert.assertEquals( client.createResource(actionCreateResource,defaultAppKey,resourseName,resourceDesc,
                resourseMediaType,contentValue,stage,copyToAll),true,"Create Property");

    }
    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.PLATFORM })
    @Test(dependsOnMethods = { "testCreateResource" ,"testGetAllDependencies"},description = "Testing update Property")
    public void testUpdateDescription() throws AFIntegrationTestException {
        Assert.assertEquals( client.updateResource(actionUpdateResource,defaultAppKey,resourseName,updatedDesc,
                resourseMediaType,updatedValue,stage),true,"update Property");

    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.PLATFORM })
    @Test(dependsOnMethods = { "testCreateResource" },description = "get All Property")
    public void testGetAllDependencies() throws AFIntegrationTestException {
        Assert.assertEquals( client.getAllDependencies(actionGetAllResources,defaultAppKey,resourseName),
                true,"get all Property for the application");

    }
    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.PLATFORM })
    @Test(dependsOnMethods = { "testCreateResource" ,"testUpdateDescription","testGetAllDependencies"},
            description = "Testing delete Property")
    public void testDeleteResource() throws AFIntegrationTestException {
    Assert.assertEquals( client.deleteResource(actionDeleteResource,defaultAppKey,resourseName,updatedDesc,
            resourseMediaType,updatedValue,stage),true,"delete Property");

   }
    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }


}
