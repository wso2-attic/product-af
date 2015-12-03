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

package org.wso2.carbon.appfactory.provisioning.runtime;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.*;

import java.io.OutputStream;
import java.util.List;
import java.util.Set;

/**
 * This class will implement the runtime provisioning service specific to Kubernetes
 */
public class KubernetesRuntimeProvisioningService implements RuntimeProvisioningService{

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws RuntimeProvisioningException {

    }

    @Override
    public void createOrganization(TenantInfo tenantInfo) throws RuntimeProvisioningException {

    }

    @Override
    public void updateOrganization(TenantInfo tenantInfo) throws RuntimeProvisioningException {

    }

    @Override
    public void deleteOrganization(TenantInfo tenantInfo) throws RuntimeProvisioningException {

    }

    @Override
    public void archiveOrganization(TenantInfo tenantInfo) throws RuntimeProvisioningException {

    }

    @Override
    public int createBuild(BuildConfiguration config) throws RuntimeProvisioningException {
        return 0;
    }

    @Override
    public String getBuildStatus(String buildId) throws RuntimeProvisioningException {
        return null;
    }

    @Override
    public String getBuildLog(String buildId) throws RuntimeProvisioningException {
        return null;
    }

    @Override
    public List<String> getBuildHistory() throws RuntimeProvisioningException {
        return null;
    }

    @Override
    public boolean cancelBuild(String buildId) throws RuntimeProvisioningException {
        return false;
    }

    @Override
    public List<String> deployApplication(DeploymentConfig config) throws RuntimeProvisioningException {
        return null;
    }

    @Override
    public boolean getDeploymentStatus() throws RuntimeProvisioningException {
        return false;
    }

    @Override
    public OutputStream streamRuntimeLogs() throws RuntimeProvisioningException {
        return null;
    }

    @Override
    public String getRuntimeLogs(Query query) throws RuntimeProvisioningException {
        return null;
    }

    @Override
    public void setRuntimeProperties(List<RuntimeProperty> runtimeProperties)
            throws RuntimeProvisioningException {

    }

    @Override
    public void updateRuntimeProperties(RuntimeProperty runtimeProperty) throws RuntimeProvisioningException {

    }

    @Override
    public List<RuntimeProperty> getRuntimeProperties() throws RuntimeProvisioningException {
        return null;
    }

    @Override
    public void addCustomDomain(Set<String> domains) throws RuntimeProvisioningException {

    }

    @Override
    public void updateCustomDomain(String domain) throws RuntimeProvisioningException {

    }

    @Override
    public Set<String> getCustomDomains() throws RuntimeProvisioningException {
        return null;
    }

    @Override
    public void deleteCustomDomain(String domain) throws RuntimeProvisioningException {

    }

    /**
     * This method will create a common kubernetes client object with authentication to the Kubernetes master server
     *
     * @return Kubernetes client object
     */
    private KubernetesClient getKubernetesClient() {

        Config config = new Config();
        config.setMasterUrl(KubernetesPovisioningConstants.KUB_MASTER_URL);
        config.setApiVersion(KubernetesPovisioningConstants.KUB_API_VERSION);

        KubernetesClient kubernetesClient = new DefaultKubernetesClient(config);
        return kubernetesClient;
    }
}
