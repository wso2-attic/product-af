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

import io.fabric8.kubernetes.api.KubernetesHelper;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.api.model.extensions.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.wso2.carbon.appfactory.provisioning.runtime.KubernetesPovisioningConstants;
import org.wso2.carbon.appfactory.provisioning.runtime.Utils.KubernetesProvisioningUtils;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.ApplicationContext;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.TenantInfo;

import java.util.HashMap;
import java.util.Map;

public class TestUtils {

    private ApplicationContext appCtx;
    private KubernetesClient kube;

    public TestUtils() {
        setAppCtx(getApplicationContext());
        kube = KubernetesProvisioningUtils.getFabric8KubernetesClient();
    }

    public Namespace createNamespace() {
        kube.namespaces().create(KubernetesProvisioningUtils.getNameSpace(getAppCtx()));
        return KubernetesProvisioningUtils.getNameSpace(getAppCtx());
    }

    public void createService() {
        Map<String, String> labelsMap = new HashMap<String, String>();
        labelsMap.put("app", getAppCtx().getId());
        labelsMap.put("version", getAppCtx().getVersion());
        labelsMap.put("stage", getAppCtx().getCurrentStage());
        Service service = new ServiceBuilder()
                .withApiVersion(Service.ApiVersion.V_1)
                .withKind(KubernetesPovisioningConstants.KIND_SERVICE)
                .withNewMetadata()
                .withName((getAppCtx().getId() + "-" + getAppCtx().getVersion().replace(".","-")).toLowerCase())
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

    public Deployment createDeployment() {
        Map<String, String> labelsMap = new HashMap<String, String>();
        labelsMap.put("app", getAppCtx().getId());
        Deployment deployment = new DeploymentBuilder()
                .withApiVersion(Deployment.ApiVersion.EXTENSIONS_V_1_BETA_1)
                .withKind("Deployment")
                .withNewMetadata()
                .withName((getAppCtx().getId() + "-" + getAppCtx().getVersion().replace(".","-")).toLowerCase())
                .endMetadata()
                .withNewSpec()
                .withReplicas(2)
                .withNewTemplate()
                .withNewMetadata()
                .withLabels(labelsMap)
                .endMetadata()
                .withNewSpec()
                .withContainers(new ContainerBuilder().withName("tomcat").withImage("tomcat:latest")
                        .withPorts(new ContainerPortBuilder().withContainerPort(80).build()).build())
                .endSpec()
                .endTemplate()
                .endSpec()
                .build();

        kube.extensions().deployments()
                .inNamespace(KubernetesProvisioningUtils.getNameSpace(getAppCtx()).getMetadata().getName())
                .create(deployment);
        return deployment;
    }

    public boolean getPodStatus(Namespace namespace, Map<String, String> selector) {

        PodList podss = kube.inNamespace(namespace.getMetadata().getName()).pods().withLabels(selector).list();
        if (podss.getItems().size() == 0) {
            return false;
        } else {
            for (Pod pod : podss.getItems()) {
                String status = KubernetesHelper.getPodStatusText(pod);
                if (!"Running".equals(status)) {
                    return false;
                }
            }
        }
        return true;
    }
    private ApplicationContext getApplicationContext() {

        ApplicationContext applicationCtx = new ApplicationContext();
        applicationCtx.setCurrentStage("DEV");
        applicationCtx.setVersion("1.0.0");
        TenantInfo tenant = new TenantInfo();
        tenant.setTenantId(5);
        tenant.setTenantDomain("wso2.org");
        applicationCtx.setTenantInfo(tenant);
        applicationCtx.setId("My-JAXRS");

        return applicationCtx;
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
