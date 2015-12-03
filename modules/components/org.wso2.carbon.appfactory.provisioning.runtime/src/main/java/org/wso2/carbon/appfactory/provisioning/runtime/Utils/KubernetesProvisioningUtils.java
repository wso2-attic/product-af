/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.carbon.appfactory.provisioning.runtime.Utils;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.wso2.carbon.appfactory.provisioning.runtime.KubernetesPovisioningConstants;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.ApplicationContext;
import java.util.List;

/**
 * This will have the utility methods to provision kubernetes
 */

public class KubernetesProvisioningUtils {
    /**
     * This method will create a common kubernetes client object with authentication to the Kubernetes master server
     *
     * @return Kubernetes client object
     */
    private KubernetesClient getKubernetesClient() {

        //todo need to modify config object with master credentials properly
        Config config = new Config();
        config.setMasterUrl(KubernetesPovisioningConstants.KUB_MASTER_URL);
        config.setApiVersion(KubernetesPovisioningConstants.KUB_API_VERSION);

        KubernetesClient kubernetesClient = new DefaultKubernetesClient(config);
        return kubernetesClient;
    }

    private List<Pod> getPodsFromApplicationContext(ApplicationContext applicationContext){

        List <Pod> podList = applicationContext.getPods();
        return podList;
    }

    private List<Service> getServicesFromPod(ApplicationContext applicationContext){

        List <Service> serviceList = applicationContext.getServices();
        return  serviceList;
    }
}
