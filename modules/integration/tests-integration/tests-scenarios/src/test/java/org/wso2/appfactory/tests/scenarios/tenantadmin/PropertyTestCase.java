/*
 * Copyright 2015 WSO2, Inc. (http://wso2.com)
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.appfactory.tests.scenarios.tenantadmin;

import com.google.gson.JsonObject;
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
 * Test scenario for app factory properties
 *
 * @throws AFIntegrationTestException
 */
public class PropertyTestCase extends AFIntegrationTest {
    private PropertyClient client;
    private static final Log log = LogFactory.getLog(APIMIntegrationTestCase.class);
    private static final String ACTION_CREATE_RESOURCE = "createResource";
    private static final String ACTION_DELETE_RESOURCE = "deleteResource";
    private static final String ACTION_UPDATE_RESOURCE = "updateResource";
    private static final String ACTION_GET_ALL_RESOURCES = "getAllDependencies";
    private static final String RESOURSE_NAME = "Property-Test";
    private static final String RESOURCE_DESC = "Property-Test_Description";
    private static final String UPDATED_DESC = "Property-Test_UpdatedDesc";
    private static final String RESOURSE_MEDIA_TYPE = "Registry";
    private static final String CONTENT_VALUE = "Property-Test_Value";
    private static final String UPDATED_VALUE = "Property-Test_UpdatedValue";
    private static final String STAGE = "Development";
    private static final String COPY_TO_ALL = "false";

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() {
        try {
            client = new PropertyClient(AFserverUrl, defaultAdmin, defaultAdminPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Test case for create resource
     *
     * @throws AFIntegrationTestException
     */

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Testing create Property")
    public void testCreateResource() throws AFIntegrationTestException{
        JsonObject createResourceResponse = client.createResource(ACTION_CREATE_RESOURCE, defaultAppKey, RESOURSE_NAME,
                                                                  RESOURCE_DESC,
                                                                  RESOURSE_MEDIA_TYPE, CONTENT_VALUE, STAGE,
                                                                  COPY_TO_ALL);

        Assert.assertEquals(createResourceResponse.getAsJsonObject().get("name").getAsString(), RESOURSE_NAME,
                            "cannot find resource name ");
        Assert.assertEquals(createResourceResponse.getAsJsonObject().get("description").getAsString(), RESOURCE_DESC,
                            "cannot find resource description");
        Assert.assertEquals(createResourceResponse.getAsJsonObject().get("value").getAsString(), CONTENT_VALUE,
                            "cannot find content value");
        Assert.assertEquals(createResourceResponse.getAsJsonObject().get("mediaType").getAsString(),
                            RESOURSE_MEDIA_TYPE, "cannot fin media type");

    }

    /**
     *test case for update resource
     *
     * @throws AFIntegrationTestException
     */
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(dependsOnMethods = {"testCreateResource", "testGetAllDependencies"}, description = "Testing update Property")
    public void testUpdateDescription() throws AFIntegrationTestException {
        JsonObject updateResponse = client.updateResource(ACTION_UPDATE_RESOURCE, defaultAppKey, RESOURSE_NAME,
                                                          UPDATED_DESC,
                                                          RESOURSE_MEDIA_TYPE, UPDATED_VALUE, STAGE);
        Assert.assertEquals(updateResponse.getAsJsonObject().get("name").getAsString(), RESOURSE_NAME,
                            "cannot find resource name ");
        Assert.assertEquals(updateResponse.getAsJsonObject().get("description").getAsString(), RESOURCE_DESC,
                            "cannot find resource description");
        Assert.assertEquals(updateResponse.getAsJsonObject().get("value").getAsString(), CONTENT_VALUE,
                            "cannot find content value");
        Assert.assertEquals(updateResponse.getAsJsonObject().get("mediaType").getAsString(),
                            RESOURSE_MEDIA_TYPE,"cannot fin media type");

    }

    /**
     *test case get all dependencies
     *
     * @throws AFIntegrationTestException
     */
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(dependsOnMethods = {"testCreateResource"}, description = "get All Property")
    public void testGetAllDependencies() throws AFIntegrationTestException {
        JsonObject getAllDependenciesResponse = client.getAllDependencies(ACTION_GET_ALL_RESOURCES, defaultAppKey,
                                                                          RESOURSE_NAME);
        Assert.assertEquals(getAllDependenciesResponse.toString().contains(RESOURSE_NAME), true, "get dependency of " +
                "app fails");

    }

    /**
     *test case for delete resource
     *
     * @throws AFIntegrationTestException
     */
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(dependsOnMethods = {"testCreateResource", "testUpdateDescription", "testGetAllDependencies"},
            description = "Testing delete Property")
    public void testDeleteResource() throws AFIntegrationTestException {
        JsonObject deleteResponse = client.deleteResource(ACTION_DELETE_RESOURCE, defaultAppKey, RESOURSE_NAME,
                                                          UPDATED_DESC,
                                                          RESOURSE_MEDIA_TYPE, UPDATED_VALUE, STAGE);
        Assert.assertNotEquals(deleteResponse.getAsJsonObject().get("name").getAsString(), RESOURSE_NAME,
                               "cannot find resource name ");


    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }


}
