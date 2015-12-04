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

import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.wso2.carbon.appfactory.provisioning.runtime.Utils.KubernetesProvisioningUtils;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Set;

/**
 * This class will implement the runtime provisioning service specific to Kubernetes
 */
public class KubernetesRuntimeProvisioningService implements RuntimeProvisioningService{

    private static final Log log = LogFactory.getLog(KubernetesRuntimeProvisioningService.class);
    private static ApplicationContext applicationContext;

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
    public String getRuntimeLogs(Query query, String containerName) throws RuntimeProvisioningException {

        String logOutPut;
        URI uri = null;

        if (query != null) {

            HttpGet httpGet = KubernetesProvisioningUtils.getHttpGETForKubernetes();
            try {
                uri = new URI(KubernetesPovisioningConstants.KUB_MASTER_URL + "api/v1/namespaces/" + applicationContext
                        .getNameSpace().getMetadata().getNamespace() + "/pods/" + containerName + "/log?&previous="
                        + query.getPreviousRecords() + "&timestamps=" + query.getTimeStamp());
                httpGet.setURI(uri);
                HttpClient httpclient = KubernetesProvisioningUtils.getHttpClientForKubernetes();
                HttpResponse response = httpclient.execute(httpGet);
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                StringBuffer result = new StringBuffer();
                String record;
                while ((record = rd.readLine()) != null) {
                    result.append(record);
                }

                logOutPut = record.toString();

            } catch (URISyntaxException e) {
                log.error("Error in url syntax : " + uri, e);
                throw new RuntimeProvisioningException("Error in url syntax : " + uri, e);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeProvisioningException(e);
            } catch (KeyStoreException e) {
                throw new RuntimeProvisioningException(e);
            } catch (KeyManagementException e) {
                throw new RuntimeProvisioningException(e);
            } catch (ClientProtocolException e) {
                throw new RuntimeProvisioningException(e);
            } catch (IOException e) {
                throw new RuntimeProvisioningException(e);
            }

        } else {
            KubernetesClient kubernetesClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();
            logOutPut = kubernetesClient.pods()
                    .inNamespace(applicationContext.getNameSpace().getMetadata().getNamespace()).withName(containerName)
                    .getLog(true);
        }

        return logOutPut;
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
}
