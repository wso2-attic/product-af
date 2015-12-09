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

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.wso2.carbon.appfactory.provisioning.runtime.KubernetesRuntimeProvisioningService;
import org.wso2.carbon.appfactory.provisioning.runtime.RuntimeProvisioningException;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.ApplicationContext;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.Container;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.DeploymentConfig;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.TenantInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DeploymentTest  {

    ApplicationContext appCtx;
    KubernetesRuntimeProvisioningService afKubClient;

    @BeforeSuite
    public void createNameSpace() throws RuntimeProvisioningException {
        appCtx = this.getApplicationContext();
        afKubClient = new KubernetesRuntimeProvisioningService(appCtx);
        afKubClient.createOrganization(appCtx.getTenantInfo());
    }

    /**
     * Test Deployment operation
     */
    @Test(groups = {"org.wso2.carbon.appfactory.provisioning.runtime"},
          description = "Kub Deploymet")
    public void testDeployment() throws RuntimeProvisioningException {

        ApplicationContext appCtx = this.getApplicationContext();
        KubernetesRuntimeProvisioningService afKubClient = new KubernetesRuntimeProvisioningService(appCtx);
        List<Container> containerList = this.getContainers();
        DeploymentConfig deploymentConfig = this.getDeploymentConfig(containerList);
        afKubClient.deployApplication(deploymentConfig);
    }

    private List<Container> getContainers(){
        List<Container> containerList = new ArrayList<>();

        Container container1 = new Container();
        container1.setBaseImageName("nginx");
        container1.setBaseImageVersion("1.7.1");
        Map<String,String> envs1 = new HashMap<>();
        envs1.put("JAVA_HOME","/opt/java");
        envs1.put("ORG","WSO2");
        container1.setEnvVariables(envs1);

        Container container2 = new Container();
        container2.setBaseImageName("nginx");
        container2.setBaseImageVersion("1.7.1");
        Map<String,String> envs2 = new HashMap<>();
        envs2.put("JAVA_HOME","/opt/java");
        envs2.put("ORG","WSO2");
        container1.setEnvVariables(envs2);

        containerList.add(container1);
        containerList.add(container2);

        return containerList;
    }

    private DeploymentConfig getDeploymentConfig(List<Container> containers){

        DeploymentConfig deploymentConfig = new DeploymentConfig();
        deploymentConfig.setDeploymentName("TestDeployment");
        deploymentConfig.setReplicas(2);
        deploymentConfig.setContainers(containers);
        Map<String,String> labels = new HashMap<>();
        labels.put("app","nginx");
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
        tenant.setTenantDomain("wso2.org");
        applicationCtx.setTenantInfo(tenant);
        applicationCtx.setId("MYJAXRSAPPID");

        return applicationCtx;
    }
}
