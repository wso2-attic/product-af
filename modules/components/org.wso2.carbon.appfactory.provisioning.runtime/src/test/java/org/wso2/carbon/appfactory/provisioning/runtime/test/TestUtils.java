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

import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.api.model.extensions.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.wso2.carbon.appfactory.provisioning.runtime.KubernetesPovisioningConstants;
import org.wso2.carbon.appfactory.provisioning.runtime.Utils.KubernetesProvisioningUtils;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.ApplicationContext;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.DeploymentConfig;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.ServiceProxy;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.TenantInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestUtils {

    private ApplicationContext appCtx;
    private KubernetesClient kube;

    public TestUtils() {
        setAppCtx(getApplicationContext());
        kube = KubernetesProvisioningUtils.getFabric8KubernetesClient();
    }

    public void createNamespace() {
        kube.namespaces().create(KubernetesProvisioningUtils.getNameSpace(getAppCtx()));
    }

    public void createService() {
        Map<String, String> labelsMap = new HashMap<String, String>();
        labelsMap.put("name", getAppCtx().getName());
        labelsMap.put("version", getAppCtx().getVersion());
        labelsMap.put("stage", getAppCtx().getCurrentStage());
        Service service = new ServiceBuilder()
                .withApiVersion(Service.ApiVersion.V_1)
                .withKind(KubernetesPovisioningConstants.KIND_SERVICE)
                .withNewMetadata()
                .withName((getAppCtx().getName() + "-" + getAppCtx().getVersion().replace(".","-")).toLowerCase())
                .withLabels(labelsMap)
                .endMetadata()
                .withNewSpec()
                .withPorts(
                        new ServicePortBuilder().withPort(80).withProtocol("TCP").withTargetPort(new IntOrString(8080))
                                .build())
                .withType("NodePort")
                .withSelector(labelsMap)
                .endSpec()
                .build();

        kube.services().inNamespace(KubernetesProvisioningUtils.getNameSpace(getAppCtx()).getMetadata().getName())
                .create(service);
    }

    public void createDeployment() {
        Map<String, String> labelsMap = new HashMap<String, String>();
        labelsMap.put("app", getAppCtx().getName());
        Deployment deployment = new DeploymentBuilder()
                .withApiVersion(Deployment.ApiVersion.EXTENSIONS_V_1_BETA_1)
                .withKind("Deployment")
                .withNewMetadata()
                .withName((getAppCtx().getName() + "-" + getAppCtx().getVersion().replace(".","-")).toLowerCase())
                .endMetadata()
                .withNewSpec()
                .withReplicas(2)
                .withNewTemplate()
                .withNewMetadata()
                .withLabels(labelsMap)
                .endMetadata()
                .withNewSpec()
                .withContainers(new ContainerBuilder().withName("nginx").withImage("nginx:1.7.9")
                        .withPorts(new ContainerPortBuilder().withContainerPort(80).build()).build())
                .endSpec()
                .endTemplate()
                .endSpec()
                .build();

        kube.extensions().deployments()
                .inNamespace(KubernetesProvisioningUtils.getNameSpace(getAppCtx()).getMetadata().getName())
                .create(deployment);
    }

    private ApplicationContext getApplicationContext() {

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

    private List<org.wso2.carbon.appfactory.provisioning.runtime.beans.Container> getContainers(){
        List<org.wso2.carbon.appfactory.provisioning.runtime.beans.Container> containerList = new ArrayList<>();

        org.wso2.carbon.appfactory.provisioning.runtime.beans.Container container1 =
                new org.wso2.carbon.appfactory.provisioning.runtime.beans.Container();
        container1.setBaseImageName("nginx");
        container1.setBaseImageVersion("1.7.1");
        Map<String,String> envs1 = new HashMap<>();
        envs1.put("JAVA_HOME","/opt/java");
        envs1.put("ORG","WSO2");
        container1.setEnvVariables(envs1);
        List<ServiceProxy> serviceProxyList = new ArrayList<>();
        ServiceProxy serviceProxy = new ServiceProxy();
        serviceProxy.setServiceName("http");
        serviceProxy.setServiceProtocol("TCP");
        serviceProxy.setServicePort(8000);
        serviceProxy.setServiceBackendPort(31000);
        serviceProxyList.add(serviceProxy);
        container1.setServiceProxies(serviceProxyList);

        org.wso2.carbon.appfactory.provisioning.runtime.beans.Container container2 =
                new org.wso2.carbon.appfactory.provisioning.runtime.beans.Container();
        container2.setBaseImageName("tomcat");
        container2.setBaseImageVersion("8.0");
        Map<String,String> envs2 = new HashMap<>();
        envs2.put("JAVA_HOME","/opt/java");
        envs2.put("ORG","WSO2");
        container2.setEnvVariables(envs2);
        serviceProxy.setServiceName("https");
        serviceProxy.setServiceProtocol("TCP");
        serviceProxy.setServicePort(8001);
        serviceProxy.setServiceBackendPort(31001);
        serviceProxyList.add(serviceProxy);
        container2.setServiceProxies(serviceProxyList);

        containerList.add(container1);
        containerList.add(container2);

        return containerList;
    }

    private DeploymentConfig getDeploymentConfig(
            List<org.wso2.carbon.appfactory.provisioning.runtime.beans.Container> containers){

        DeploymentConfig deploymentConfig = new DeploymentConfig();
        deploymentConfig.setDeploymentName("test-deployment");
        deploymentConfig.setReplicas(2);
        deploymentConfig.setContainers(containers);
        Map<String,String> labels = new HashMap<>();
        labels.put("app", "nginx");
        deploymentConfig.setLables(labels);

        return deploymentConfig;
    }

    public void deleteNamespace(){
        kube.namespaces().delete(KubernetesProvisioningUtils.getNameSpace(getAppCtx()));
    }


    public ApplicationContext getAppCtx() {
        return appCtx;
    }

    public void setAppCtx(ApplicationContext appCtx) {
        this.appCtx = appCtx;
    }
}
