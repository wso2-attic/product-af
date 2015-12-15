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

import org.testng.annotations.*;
import org.wso2.carbon.appfactory.provisioning.runtime.KubernetesRuntimeProvisioningService;
import org.wso2.carbon.appfactory.provisioning.runtime.RuntimeProvisioningException;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoggingTest {

    TestUtils testUtils = new TestUtils();
    KubernetesRuntimeProvisioningService afKubClient;
    DeploymentConfig deploymentConfig;

    @BeforeClass
    public void initialize() throws RuntimeProvisioningException {
        testUtils.setAppCtx(getApplicationContext());
        testUtils.createNamespace();
        afKubClient = new KubernetesRuntimeProvisioningService(testUtils.getAppCtx());

        List<Container> containerList = this.getContainers();
        deploymentConfig = this.getDeploymentConfig(containerList);
        afKubClient.deployApplication(deploymentConfig);

        //todo add assertions
    }

    /**
     * Test getting snapshot logs of containers
     *
     * @throws RuntimeProvisioningException
     */

    @Test(groups = {"org.wso2.carbon.appfactory.provisioning.runtime"}, description = "Kub Logging")
    public void testSnapshotLogs() throws RuntimeProvisioningException, InterruptedException {

        //Initially waits until deployment successful
        Thread.sleep(30000);
        for (int i = 0; i < 5; i++) {
            if(afKubClient.getDeploymentStatus(deploymentConfig)){
                afKubClient.getRuntimeLogs(deploymentConfig, null);
            }else {
                //If deployment is not successful check again in 30 seconds
                Thread.sleep(30000);
            }

            //todo add assertions
        }
    }

    /**
     * Test getting logs with specific number of records by querying in deployed containers
     *
     * @throws RuntimeProvisioningException
     */
    @Test(groups = {"org.wso2.carbon.appfactory.provisioning.runtime"}, description = "Kub Logging")
    public void testLogsWithQueryRecords() throws RuntimeProvisioningException, InterruptedException {

        LogQuery query = new LogQuery(false, 1000, -1);

        //Initially waits until deployment successful
        Thread.sleep(30000);
        for (int i = 0; i < 5; i++) {
            if(afKubClient.getDeploymentStatus(deploymentConfig)){
                afKubClient.getRuntimeLogs(deploymentConfig, query);
            }else {
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
    @Test(groups = {"org.wso2.carbon.appfactory.provisioning.runtime"}, description = "Kub Logging")
    public void testLogsWithQueryDuration() throws RuntimeProvisioningException, InterruptedException {

        LogQuery query = new LogQuery(false, -1, 1);

        //Initially waits until deployment successful
        Thread.sleep(30000);
        for (int i = 0; i < 5; i++) {
            if(afKubClient.getDeploymentStatus(deploymentConfig)){
                afKubClient.getRuntimeLogs(deploymentConfig, query);
            }else {
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
    @Test(groups = {"org.wso2.carbon.appfactory.provisioning.runtime"}, description = "Kub Logging")
    public void testLogStream() throws RuntimeProvisioningException, InterruptedException {

        LogQuery query = new LogQuery(true, -1, 1);

        //Initially waits until deployment successful
        Thread.sleep(30000);
        for (int i = 0; i < 5; i++) {
            if(afKubClient.getDeploymentStatus(deploymentConfig)){
                afKubClient.streamRuntimeLogs(deploymentConfig);
            }else {
                //If deployment is not successful check again in 30 seconds
                Thread.sleep(30000);
            }

            //todo add assertions
        }
    }

    //todo move duplicate code into the testUtil

    private List<Container> getContainers(){
        List<Container> containerList = new ArrayList<>();

        Container container1 = new Container();
        container1.setBaseImageName("nginx");
        container1.setBaseImageVersion("1.7.1");
        Map<String,String> envs1 = new HashMap<>();
        envs1.put("HTML_ROOT","/var/www/html");
        envs1.put("ORG","WSO2");
        container1.setEnvVariables(envs1);
        List<ServiceProxy> serviceProxyList1 = new ArrayList<>();
        ServiceProxy serviceProxy1 = new ServiceProxy();
        serviceProxy1.setServiceName("http");
        serviceProxy1.setServiceProtocol("TCP");
        serviceProxy1.setServicePort(31080);
        serviceProxy1.setServiceBackendPort(80);
        serviceProxyList1.add(serviceProxy1);
        container1.setServiceProxies(serviceProxyList1);

        Container container2 = new Container();
        container2.setBaseImageName("tomcat");
        container2.setBaseImageVersion("8.0");
        Map<String,String> envs2 = new HashMap<>();
        envs2.put("JAVA_HOME","/opt/java");
        envs2.put("ORG","WSO2");
        container2.setEnvVariables(envs2);
        List<ServiceProxy> serviceProxyList2 = new ArrayList<>();
        ServiceProxy serviceProxy2 = new ServiceProxy();
        serviceProxy2.setServiceName("https");
        serviceProxy2.setServiceProtocol("TCP");
        serviceProxy2.setServicePort(39080);
        serviceProxy2.setServiceBackendPort(8080);
        serviceProxyList2.add(serviceProxy2);
        container2.setServiceProxies(serviceProxyList2);

        containerList.add(container1);
        containerList.add(container2);

        return containerList;
    }

    private DeploymentConfig getDeploymentConfig(List<Container> containers){

        DeploymentConfig deploymentConfig = new DeploymentConfig();
        deploymentConfig.setDeploymentName("test-logs");
        deploymentConfig.setReplicas(2);
        deploymentConfig.setContainers(containers);
        Map<String,String> labels = new HashMap<>();
        labels.put("DeploymentName","test-logs");
        deploymentConfig.setLables(labels);

        return deploymentConfig;
    }

    private ApplicationContext getApplicationContext(){

        ApplicationContext applicationCtx = new ApplicationContext();
        applicationCtx.setCurrentStage("DEV");
        applicationCtx.setName("My-JAXRS");
        applicationCtx.setVersion("1.0.0");
        TenantInfo tenant = new TenantInfo();
        tenant.setTenantId(5);
        tenant.setTenantDomain("log.wso2.org");
        applicationCtx.setTenantInfo(tenant);
        applicationCtx.setId("MYJAXRSAPPID");

        return applicationCtx;
    }

    @AfterClass
    private void cleanup(){
        testUtils.deleteNamespace();
    }
}
