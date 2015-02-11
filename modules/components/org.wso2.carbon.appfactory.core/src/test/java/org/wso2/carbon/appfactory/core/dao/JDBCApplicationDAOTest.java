/*
*  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.appfactory.core.dao;

import junit.framework.Assert;
import org.wso2.carbon.appfactory.core.dao.base.BaseTestCase;
import org.wso2.carbon.appfactory.core.dto.Application;
import org.wso2.carbon.appfactory.core.dto.BuildStatus;
import org.wso2.carbon.appfactory.core.dto.DeployStatus;
import org.wso2.carbon.appfactory.core.dto.Resource;
import org.wso2.carbon.appfactory.core.dto.Version;
import org.wso2.carbon.appfactory.core.util.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBCApplicationDAO Tester.
 */
public class JDBCApplicationDAOTest extends BaseTestCase {

    public static final String[] APPLICATION_KEYS = new String[]{"app0", "app1", "app2", "app3", "app4", "app5",
                                                                 "app6", "app7"};

    public JDBCApplicationDAOTest(String name) {
        super(name);
    }

    public void testAddApplication() throws Exception {
        for (String applicationKey : APPLICATION_KEYS) {
            Application application = new Application();
            application.setName(applicationKey);
            application.setApplicationCreationStatus(Constants.ApplicationCreationStatus.PENDING);
            application.setId(applicationKey);
            application.setType("Uploaded-App-Jax-WS");

            applicationDAO.addApplication(application);
        }
        Application[] applications = applicationDAO.getAllApplications();

        for (Application application : applications) {
            assertEquals("Expected application is not found", true, Arrays.asList(APPLICATION_KEYS).contains
                    (application.getId()));
        }
    }

    public void testAddApplicationCreationStatus() throws Exception {
        applicationDAO.setApplicationCreationStatus("app0", Constants.ApplicationCreationStatus.COMPLETED);
        applicationDAO.setApplicationCreationStatus("app1", Constants.ApplicationCreationStatus.FAULTY);

        assertEquals(Constants.ApplicationCreationStatus.COMPLETED, applicationDAO.getApplicationCreationStatus
                ("app0"));
        assertEquals(Constants.ApplicationCreationStatus.FAULTY, applicationDAO.getApplicationCreationStatus("app1"));
        assertEquals(Constants.ApplicationCreationStatus.PENDING, applicationDAO.getApplicationCreationStatus("app2"));
    }

    public void testAddApplicationVersion() throws Exception {
        Version version = new Version();
        version.setId("1.0.0");
        version.setPromoteStatus("SUCCESS");
        version.setLifecycleStage("Development");
        applicationDAO.addVersion("app0", version);

        Version[] applicationVersions = applicationDAO.getAllApplicationVersions("app0");
        Map<String, Version> versionMap = new HashMap<String, Version>();
        for (Version applicationVersion : applicationVersions) {
            versionMap.put(applicationVersion.getId(), applicationVersion);
        }
        assertEquals("trunk", versionMap.get("trunk").getId());
        assertEquals("1.0.0", versionMap.get("1.0.0").getId());
        assertEquals(2, applicationDAO.getBranchCount("app0"));
    }

    public void testAddApplicationVersionBuildStatus() throws Exception {
        BuildStatus buildStatus = new BuildStatus();
        buildStatus.setLastBuildId("1");
        buildStatus.setLastBuildStatus("SUCCESS");
        buildStatus.setLastBuildTime(System.currentTimeMillis());
        applicationDAO.updateLastBuildStatus("app0", "1.0.0", false, null, buildStatus);

        BuildStatus returnedBuildStatus1 = applicationDAO.getBuildStatus("app0", "1.0.0", false, null);
        assertNotNull(returnedBuildStatus1);
        assertEquals(buildStatus.getLastBuildId(), returnedBuildStatus1.getLastBuildId());
        assertEquals(buildStatus.getLastBuildStatus(), returnedBuildStatus1.getLastBuildStatus());
        assertEquals(buildStatus.getLastBuildTime(), returnedBuildStatus1.getLastBuildTime());

        buildStatus.setLastBuildId("2");
        buildStatus.setLastBuildStatus("FAILED");
        buildStatus.setLastBuildTime(System.currentTimeMillis());
        applicationDAO.updateLastBuildStatus("app0", "1.0.0", false, null, buildStatus);

        BuildStatus returnedBuildStatus2 = applicationDAO.getBuildStatus("app0", "1.0.0", false, null);
        assertNotSame(returnedBuildStatus1.getLastBuildStatus(), returnedBuildStatus2.getLastBuildStatus());
        assertNotSame(returnedBuildStatus1.getLastBuildId(), returnedBuildStatus2.getLastBuildId());
        assertNotSame(returnedBuildStatus1.getLastBuildTime(), returnedBuildStatus2.getLastBuildTime());


        buildStatus.setCurrentBuildId("3");
        applicationDAO.updateCurrentBuildStatus("app0", "1.0.0", false, null, buildStatus);

        BuildStatus returnedBuildStatus3 = applicationDAO.getBuildStatus("app0", "1.0.0", false, null);
        assertEquals("3", returnedBuildStatus3.getCurrentBuildId());
    }


    public void testApplicationVersionDeployStatus() throws Exception {
        DeployStatus deployStatus = new DeployStatus();
        deployStatus.setLastDeployedId("1");
        deployStatus.setLastDeployedStatus("FAILURE");
        deployStatus.setLastDeployedTime(System.currentTimeMillis());
        applicationDAO.updateLastDeployedBuildID("app0", "1.0.0", "Development", false, null, deployStatus
                .getLastDeployedId());
        applicationDAO.updateLastDeployStatus("app0", "1.0.0", "Development", false, null, deployStatus);

        DeployStatus returnedDeployStatus1 = applicationDAO.getDeployStatus("app0", "1.0.0", "Development", false,
                                                                            null);
        assertNotNull(returnedDeployStatus1);
        assertEquals("1", returnedDeployStatus1.getLastDeployedId());
        assertEquals("FAILURE", returnedDeployStatus1.getLastDeployedStatus());

        deployStatus.setLastDeployedId("2");
        deployStatus.setLastDeployedStatus("SUCCESS");
        deployStatus.setLastDeployedTime(System.currentTimeMillis());
        applicationDAO.updateLastDeployStatus("app0", "1.0.0", "Development", false, null, deployStatus);
        applicationDAO.updateLastDeployedBuildID("app0", "1.0.0", "Development", false, null, deployStatus
                .getLastDeployedId());

        DeployStatus returnedDeployStatus2 = applicationDAO.getDeployStatus("app0", "1.0.0", "Development", false,
                                                                            null);
        assertNotNull(returnedDeployStatus2);
        assertEquals("2", returnedDeployStatus2.getLastDeployedId());
        assertEquals("SUCCESS", returnedDeployStatus2.getLastDeployedStatus());
        assertTrue(returnedDeployStatus2.getLastDeployedTime() > returnedDeployStatus1.getLastDeployedTime());
    }

    public void testForkApplicationVersion() throws Exception {
        applicationDAO.forkApplicationVersion("app0", "1.0.0", "user1");
        applicationDAO.forkApplicationVersion("app0", "1.0.0", "user2");
        applicationDAO.forkApplicationVersion("app0", "1.0.0", "user3");
        applicationDAO.forkApplicationVersion("app0", "1.0.0", "user4");

        BuildStatus buildStatus = applicationDAO.getBuildStatus("app0", "1.0.0", true, "user1");
        assertNotNull(buildStatus);

        buildStatus.setLastBuildId("1");
        buildStatus.setLastBuildStatus("SUCCESS");
        buildStatus.setLastBuildTime(System.currentTimeMillis());
        applicationDAO.updateLastBuildStatus("app0", "1.0.0", true, "user1", buildStatus);

        BuildStatus returnedBuildStatus = applicationDAO.getBuildStatus("app0", "1.0.0", true, "user1");
        assertNotNull(returnedBuildStatus.getLastBuildId());
        assertNotNull(returnedBuildStatus.getLastBuildStatus());
        assertNotNull(returnedBuildStatus.getLastBuildTime());

        DeployStatus deployStatus = applicationDAO.getDeployStatus("app0", "1.0.0", "Development", true, "user1");
        assertNotNull(deployStatus);

        applicationDAO.updateLastDeployedBuildID("app0", "1.0.0", "Development", true, "user1", "1");
        deployStatus.setLastDeployedTime(System.currentTimeMillis());
        deployStatus.setLastDeployedStatus("SUCCESS");
        applicationDAO.updateLastDeployStatus("app0", "1.0.0", "Development", true, "user1", deployStatus);
        DeployStatus returnedDeployedStatus = applicationDAO.getDeployStatus("app0", "1.0.0", "Development", true,
                                                                             "user1");
        assertNotNull(returnedBuildStatus);
        assertNotNull(returnedDeployedStatus.getLastDeployedId());
        assertNotNull(returnedDeployedStatus.getLastDeployedStatus());
        assertNotNull(returnedDeployedStatus.getLastDeployedTime());

    }


    public void testApplicationResource() throws Exception {
        resourceDAO.addResource("app0", "db0", "DATABASE", "Development", "desc");
        resourceDAO.addResource("app0", "db1", "DATABASE", "Development", "desc");
        resourceDAO.addResource("app0", "db2", "DATABASE", "Development", "desc");
        resourceDAO.addResource("app0", "db3", "DATABASE", "Development", "desc");
        resourceDAO.addResource("app0", "db4", "DATABASE", "Development", "desc");
        resourceDAO.addResource("app1", "db1", "DATABASE", "Development", "desc");

        // Check whether 5 dbs exist
        Resource[] resources = resourceDAO.getResources("app0", "DATABASE", "Development");
        assertEquals(5, resources.length);

        // check whether database exist with appKey=app0 & appName=db4
        assertTrue(resourceDAO.isResourceExists("app0", "db4", "DATABASE", "Development"));

        // delete the database (appKey=app0 & appName=db4) and check whether database does not exist
        resourceDAO.deleteResource("app0", "db4", "DATABASE", "Development");
        assertFalse(resourceDAO.isResourceExists("app0", "db4", "DATABASE", "Development"));

        // check whether database exist with appKey=app1 & appName=db4
        assertFalse(resourceDAO.isResourceExists("app1", "db4", "DATABASE", "Development"));
    }

    public void testDeleteApplicationWithNoVersion() throws Exception {

        Application application = new Application();
        application.setName("appd1");
        application.setApplicationCreationStatus(Constants.ApplicationCreationStatus.PENDING);
        application.setId("appd1");
        application.setType("Uploaded-App-Jax-WS");
        applicationDAO.addApplication(application);
        resourceDAO.addResource("appd1", "test", "DB", "DEV", "desc");

        applicationDAO.deleteApplication("appd1");

        // Test whether the cache got cleared properly
        Constants.ApplicationCreationStatus applicationCreationStatus = applicationDAO.getApplicationCreationStatus
                ("appd1");
        assertEquals("Application creation status found for deleted application", Constants.ApplicationCreationStatus
                .NONE, applicationCreationStatus);

        Application[] allApplications = applicationDAO.getAllApplications();
        List<String> applicationKeys = new ArrayList<String>();

        for (Application applications : allApplications) {
            applicationKeys.add(applications.getId());
        }

        assertEquals("Expected application is not found", false, applicationKeys.contains("appd1"));
        assertEquals(0, applicationDAO.getAllApplicationVersions("appd1").length);
        assertEquals(0, resourceDAO.getResources("appd1", "DATABASE", "Development").length);
    }

    public void testDeleteApplicationWithMultipleVersions() throws Exception {

        Application application = new Application();
        application.setName("appd");
        application.setApplicationCreationStatus(Constants.ApplicationCreationStatus.PENDING);
        application.setId("appd");
        application.setType("Uploaded-App-Jax-WS");
        applicationDAO.addApplication(application);
        resourceDAO.addResource("appd", "test", "DB", "DEV", "desc");

        Version version = new Version();
        version.setId("1.0.0");
        version.setPromoteStatus("SUCCESS");
        version.setLifecycleStage("Development");
        applicationDAO.addVersion("appd", version);
        applicationDAO.forkApplicationVersion("appd", "1.0.0", "admin");

        version = new Version();
        version.setId("2.0.0");
        version.setPromoteStatus("SUCCESS");
        version.setLifecycleStage("Development");
        applicationDAO.addVersion("appd", version);

        applicationDAO.deleteApplication("appd");

        // Test whether the cache got cleared properly
        Constants.ApplicationCreationStatus applicationCreationStatus = applicationDAO.getApplicationCreationStatus
                ("appd");
        assertEquals("Application creation status found for deleted application", Constants.ApplicationCreationStatus
                .NONE, applicationCreationStatus);

        Application[] allApplications = applicationDAO.getAllApplications();
        List<String> applicationKeys = new ArrayList<String>();

        for (Application applications : allApplications) {
            applicationKeys.add(applications.getId());
        }

        assertEquals("Expected application is not found", false, applicationKeys.contains("appd"));
        assertEquals(0, applicationDAO.getAllApplicationVersions("appd").length);
        assertEquals(0, resourceDAO.getResources("appd", "DATABASE", "Development").length);

        applicationDAO.deleteApplication("app0");

        allApplications = applicationDAO.getAllApplications();
        applicationKeys = new ArrayList<String>();

        for (Application singleApp : allApplications) {
            applicationKeys.add(singleApp.getId());
        }

        assertEquals("Expected application is not found", false, applicationKeys.contains("app0"));
        Version[] versions = applicationDAO.getAllApplicationVersions("app0");
        assertEquals(0, versions.length);

    }
}
