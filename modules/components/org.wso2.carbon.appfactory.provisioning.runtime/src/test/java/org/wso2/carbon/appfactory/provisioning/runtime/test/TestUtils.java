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

import io.fabric8.kubernetes.api.model.Container;
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

    public void createNamespace() {
        kube.namespaces().create(KubernetesProvisioningUtils.getNameSpace(getAppCtx()));
    }

    public void createService() {
        Map<String, String> labelsMap = new HashMap<String, String>();
        labelsMap.put("app", getAppCtx().getName());
        Service service = new ServiceBuilder().withApiVersion(Service.ApiVersion.V_1)
                .withKind(KubernetesPovisioningConstants.KIND_SERVICE).withNewMetadata()
                .withName(getAppCtx().getName() + "-" + getAppCtx().getVersion()).endMetadata().withNewSpec().withPorts(
                        new ServicePortBuilder().withPort(80).withProtocol("TCP").withTargetPort(new IntOrString(8080))
                                .build()).withType("NodePort").withSelector(labelsMap).endSpec().build();
        kube.services().inNamespace(KubernetesProvisioningUtils.getNameSpace(getAppCtx()).getMetadata().getName())
                .create(service);
    }

    public void createDeployment() {
        Map<String, String> labelsMap = new HashMap<String, String>();
        labelsMap.put("app", getAppCtx().getName());
        Deployment deployment = new DeploymentBuilder().withApiVersion(Deployment.ApiVersion.EXTENSIONS_V_1_BETA_1)
                .withKind("Deployment").withNewMetadata()
                .withName((getAppCtx().getName() + "-" + getAppCtx().getVersion())).endMetadata().withNewSpec()
                .withReplicas(2).withNewTemplate().withNewMetadata().withLabels(labelsMap).endMetadata().withNewSpec()
                .withContainers(new Container()).endSpec().endTemplate().endSpec().build();
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

    public ApplicationContext getAppCtx() {
        return appCtx;
    }

    public void setAppCtx(ApplicationContext appCtx) {
        this.appCtx = appCtx;
    }
}
