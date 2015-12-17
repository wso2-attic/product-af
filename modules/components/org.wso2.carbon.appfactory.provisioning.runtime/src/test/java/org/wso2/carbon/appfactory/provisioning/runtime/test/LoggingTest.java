/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.appfactory.provisioning.runtime.test;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.*;
import org.wso2.carbon.appfactory.provisioning.runtime.KubernetesRuntimeProvisioningService;
import org.wso2.carbon.appfactory.provisioning.runtime.RuntimeProvisioningException;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoggingTest {

    private static final Log log = LogFactory.getLog(LoggingTest.class);

    public static TestUtils testUtils;
    KubernetesRuntimeProvisioningService afKubClient;
    Deployment deployment;
    Namespace namespace;

    @BeforeClass
    public void initialize() throws RuntimeProvisioningException {
        log.info("Starting Logging test case");
        testUtils = new TestUtils();
        testUtils.deleteNamespace();
        namespace = testUtils.createNamespace();
        deployment = testUtils.createDeployment();
    }

    /**
     * Test getting snapshot logs of containers
     *
     * @throws RuntimeProvisioningException
     */

    @Test(groups = {"org.wso2.carbon.appfactory.provisioning.runtime" }, description = "Kub get snapshot logs")
    public void testSnapshotLogs() throws RuntimeProvisioningException, InterruptedException {
        DeploymentLogs deploymentLogs;
        Map <String, String> selector = new HashMap<>();
        selector.put("app" , "My-JAXRS");
        afKubClient = new KubernetesRuntimeProvisioningService(testUtils.getAppCtx());

        //Initially waits until deployment successful
        log.info("Waiting for deployment to complete : " + deployment.getMetadata().getName());
        Thread.sleep(60000);
        int counter;
        //todo get the upper bound value from a config
        for (counter = 0; counter < 10; counter++) {
            if (testUtils.getPodStatus(namespace, selector)) {
                log.info("Pod status : Running");
                deploymentLogs = afKubClient.getRuntimeLogs(null);
                Map <String, BufferedReader> logs = deploymentLogs.getDeploymentLogs();

                //Waiting until tomcat starts
                log.info("Waiting until server startup complete ");
                Thread.sleep(10000);

                for (Map.Entry<String, BufferedReader> logEntry : logs.entrySet()) {
                    String record;
                    StringBuffer buffer = new StringBuffer();
                    try {
                        while ((record = logEntry.getValue().readLine())!=null) {
                            buffer.append(record);
                        }
                    } catch (IOException e) {
                        log.error("Error while reading from the log of deployment : "
                                + deployment.getMetadata().getName(), e);
                    }
                    Assert.assertTrue(buffer.indexOf("Starting service Catalina") > 0);
                }
                break;
            } else {
                log.info("Pod status : Pending");
                log.info("Retrying until deployment complete : " + deployment.getMetadata().getName());
                //If deployment is not successful check again in 30 seconds
                Thread.sleep(30000);
            }
            if(counter == 9){
                log.info("Pods didn't start Running during the retrying period");
                Assert.assertTrue(false);
            }
        }
    }

    /**
     * Test getting logs with specific number of records by querying in deployed containers
     *
     * @throws RuntimeProvisioningException
     */
   // @Test(groups = {"org.wso2.carbon.appfactory.provisioning.runtime" }, description = "Kub snapshot logs with querying records count")
    public void testLogsWithQueryRecords() throws RuntimeProvisioningException, InterruptedException {

        LogQuery query = new LogQuery(false, 1000, -1);
        Map <String, String> selector = new HashMap<>();
        selector.put("app" , "My-JAXRS");

        //Initially waits until deployment successful
        Thread.sleep(30000);

        for (int i = 0; i < 10; i++) {
            if (testUtils.getPodStatus(namespace, selector)) {
                afKubClient.getRuntimeLogs(query);
            } else {
                //If deployment is not successful check again in 30 seconds
                Thread.sleep(30000);
            }

            //todo add assertions
        }
    }

    /**
     * Test getting logs in a specific duration by querying in deployed containers
     *
     * @throws RuntimeProvisioningException
     */
    //@Test(groups = { "org.wso2.carbon.appfactory.provisioning.runtime" }, description = "Kub snapshot logs with querying duration")
    public void testLogsWithQueryDuration() throws RuntimeProvisioningException, InterruptedException {

        LogQuery query = new LogQuery(false, -1, 1);
        Map <String, String> selector = new HashMap<>();
        selector.put("app" , "My-JAXRS");

        //Initially waits until deployment successful
        Thread.sleep(30000);
        for (int i = 0; i < 5; i++) {
            if (testUtils.getPodStatus(namespace, selector)) {
                afKubClient.getRuntimeLogs(query);
            } else {
                //If deployment is not successful check again in 30 seconds
                Thread.sleep(30000);
            }

            //todo add assertions
        }
    }

    /**
     * Test log streaming in deployed containers
     *
     * @throws RuntimeProvisioningException
     */
   // @Test(groups = {"org.wso2.carbon.appfactory.provisioning.runtime" }, description = "Kub Logging")
    public void testLogStream() throws RuntimeProvisioningException, InterruptedException {

        LogQuery query = new LogQuery(true, -1, 1);
        Map <String, String> selector = new HashMap<>();
        selector.put("app" , "My-JAXRS");

        //Initially waits until deployment successful
        Thread.sleep(30000);
        for (int i = 0; i < 5; i++) {
            if (testUtils.getPodStatus(namespace, selector)) {
                afKubClient.streamRuntimeLogs();
            } else {
                //If deployment is not successful check again in 30 seconds
                Thread.sleep(30000);
            }

            //todo add assertions
        }
    }

   @AfterClass private void cleanup() {
       log.info("Cleaning up");
        testUtils.deleteNamespace();
    }
}
