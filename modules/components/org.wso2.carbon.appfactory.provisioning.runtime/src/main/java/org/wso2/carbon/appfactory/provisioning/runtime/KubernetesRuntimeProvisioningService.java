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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.wso2.carbon.appfactory.provisioning.runtime.Utils.KubernetesProvisioningUtils;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public Map<String, BufferedReader> streamRuntimeLogs() throws RuntimeProvisioningException {

        Query query = new Query(true,0,0);

        Map<String, BufferedReader> logOutPut = new HashMap<String, BufferedReader>();
        URI uri = null;
        HttpClient httpclient = null;

        for (String containerName : applicationContext.getContainerList()) {

            try {
                uri = new URI(KubernetesPovisioningConstants.KUB_MASTER_URL + "api/v1/namespaces/"
                        + KubernetesProvisioningUtils.getNameSpace(applicationContext).getMetadata().getNamespace()
                        + "/pods/" + containerName + "/log?follow=" + String.valueOf(query.getIsFollowing()));

                HttpGet httpGet = (HttpGet) KubernetesProvisioningUtils
                        .getHttpMethodForKubernetes(KubernetesPovisioningConstants.HTTP_GET, uri);
                httpGet.setURI(uri);
                httpclient = KubernetesProvisioningUtils.getHttpClientForKubernetes();
                HttpResponse response = httpclient.execute(httpGet);
                BufferedReader logStream = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                logOutPut.put(containerName, logStream);

            } catch (URISyntaxException e) {
                String msg = "Error in url syntax : " + uri + " while getting logs from container : " + containerName;
                log.error(msg, e);
                throw new RuntimeProvisioningException(msg, e);
            } catch (NoSuchAlgorithmException e) {
                String msg =
                        "Error in SSL protocol while connecting to Kubernetes api while getting logs from container : "
                                + containerName;
                log.error(msg, e);
                throw new RuntimeProvisioningException(msg, e);
            } catch (IOException e) {
                String msg = "Error while reading log stream from container : " + containerName;
                log.error(msg, e);
                throw new RuntimeProvisioningException(msg, e);
            } catch (KeyManagementException e) {
                String msg = "Error creating SSL connection to Kubernetes api while getting logs from container : "
                        + containerName;
                log.error(msg, e);
                throw new RuntimeProvisioningException(e);
            } catch (KeyStoreException e) {
                String msg = "Error creating SSL connection to Kubernetes api while getting logs from container : "
                        + containerName;
                log.error(msg, e);
                throw new RuntimeProvisioningException(e);
            }finally {
                httpclient.getConnectionManager().shutdown();
            }
        }
        return logOutPut;
    }

    @Override
    public Map<String, String> getRuntimeLogs(Query query) throws RuntimeProvisioningException {

        Map<String, String> logOutPut = new HashMap<String, String>();
        URI uri = null;
        HttpClient httpclient = null;

        if (query != null) {

            for (String containerName : applicationContext.getContainerList()) {
                try {
                    uri = new URI(KubernetesPovisioningConstants.KUB_MASTER_URL + "api/v1/namespaces/"
                            + KubernetesProvisioningUtils.getNameSpace(applicationContext).getMetadata().getNamespace()
                            + "/pods/" + containerName + "/log?&previous=" + String
                            .valueOf(query.getPreviousRecordsCount()) + "&timestamps=" + String
                            .valueOf(query.getDurationInHours()));
                    HttpGet httpGet = (HttpGet) KubernetesProvisioningUtils
                            .getHttpMethodForKubernetes(KubernetesPovisioningConstants.HTTP_GET, uri);
                    httpGet.setURI(uri);
                    httpclient = KubernetesProvisioningUtils.getHttpClientForKubernetes();
                    HttpResponse response = httpclient.execute(httpGet);
                    BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    StringBuffer result = new StringBuffer();
                    String record;
                    while ((record = rd.readLine()) != null) {
                        result.append(record);
                    }
                    logOutPut.put(containerName, record.toString());
                } catch (URISyntaxException e) {
                    String msg =
                            "Error in url syntax : " + uri + " while getting logs from container : " + containerName;
                    log.error(msg, e);
                    throw new RuntimeProvisioningException(msg, e);
                } catch (NoSuchAlgorithmException e) {
                    String msg =
                            "Error in SSL protocol while connecting to Kubernetes api while getting logs from container : "
                                    + containerName;
                    log.error(msg, e);
                    throw new RuntimeProvisioningException(msg, e);
                } catch (IOException e) {
                    String msg = "Error while reading log stram from container : " + containerName;
                    log.error(msg, e);
                    throw new RuntimeProvisioningException(msg, e);
                } catch (KeyManagementException e) {
                    String msg = "Error creating SSL connection to Kubernetes api while getting logs from container : "
                            + containerName;
                    log.error(msg, e);
                    throw new RuntimeProvisioningException(e);
                } catch (KeyStoreException e) {
                    String msg = "Error creating SSL connection to Kubernetes api while getting logs from container : "
                            + containerName;
                    log.error(msg, e);
                    throw new RuntimeProvisioningException(e);
                }finally {
                    httpclient.getConnectionManager().shutdown();
                }
            }
        } else {
            KubernetesClient kubernetesClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();
            for (String containerName : applicationContext.getContainerList()) {
                String logs = kubernetesClient.pods().inNamespace(
                        KubernetesProvisioningUtils.getNameSpace(applicationContext).getMetadata().getNamespace())
                        .withName(containerName).getLog(true);
                logOutPut.put(containerName, logs);
            }
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

        HttpClient httpclient = null;
        URI uri = null;

        ObjectMapper objectMapper = new ObjectMapper();
        String ingJson;

        for (String domain : domains) {
            Ingress ing = new IngressBuilder()
                    .withApiVersion(Ingress.ApiVersion.EXTENSIONS_V_1_BETA_1)
                    .withKind("Ingress")
                    .withNewMetadata()
                    .withName("ingress")
                    .withNamespace("dev-tom")
                    .endMetadata()
                    .withNewSpec().addNewRule()
                    .withHost(domain)
                    .withNewHttp().addNewPath()
                    .withNewBackend()
                    .withServiceName("tomcat-service")
                    .withServicePort(new IntOrString(80))
                    .endBackend()
                    .endPath()
                    .endHttp()
                    .endRule()
                    .endSpec()
                    .build();

            try {
                httpclient = KubernetesProvisioningUtils.getHttpClientForKubernetes();
                ingJson = objectMapper.writeValueAsString(ing);
                if (log.isDebugEnabled()) {
                    log.debug(ingJson);
                }


                StringEntity stringEntity = new StringEntity(ingJson, "UTF-8");
                uri = new URI(KubernetesPovisioningConstants.KUB_MASTER_URL + "apis/extensions/v1beta1/namespaces/"
                              +KubernetesProvisioningUtils.getNameSpace(applicationContext).getMetadata().getNamespace()
                              + "/ingresses/");

                HttpPost httpPost = (HttpPost) KubernetesProvisioningUtils.getHttpMethodForKubernetes(KubernetesPovisioningConstants.HTTP_POST, uri);
                httpPost.addHeader("Content-Type", "application/json");
                httpPost.setEntity(stringEntity);

                HttpResponse response = httpclient.execute(httpPost);

                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new RuntimeProvisioningException("Failed to add domain mapping Domain: " + domains + "HTTP error code : "
                                                           + response.getStatusLine().getStatusCode());
                }

                if (log.isDebugEnabled()) {
                    log.debug("addCustomDomain response: " + response.getEntity().getContent());
                }

            } catch (JsonProcessingException e) {
                String msg = "Exception in converting the ingress object to json";
                log.error(msg, e);
                throw new RuntimeProvisioningException(e);
            } catch (KeyStoreException e) {
                String msg = "Error in keystore while connecting to Kubernetes cluster";
                log.error(msg, e);
                throw new RuntimeProvisioningException(msg, e);
            } catch (NoSuchAlgorithmException e) {
                String msg = "Cryptographic algorithm not found while connecting to Kubernetes cluster";
                log.error(msg, e);
            } catch (KeyManagementException e) {
                String msg = "Exception in key management while while connecting to Kubernetes cluster";
                log.error(msg, e);
                throw new RuntimeProvisioningException(e);
            } catch (URISyntaxException e) {
                String msg = "Error in url syntax : " + uri;
                log.error(msg, e);
                throw new RuntimeProvisioningException(e);
            } catch (UnsupportedEncodingException e) {
                String msg = "Character encoding used is not supported";
                log.error(msg, e);
                throw new RuntimeProvisioningException(e);
            } catch (ClientProtocolException e) {
                String msg = "Exception occurred in client protocol while trying to invoke Kubernetes api";
                log.error(msg, e);
                throw new RuntimeProvisioningException(e);
            } catch (IOException e) {
                String msg = "Connection exception while connecting to Kubernetes cluster";
                log.error(msg, e);
                throw new RuntimeProvisioningException(e);
            }finally {
                httpclient.getConnectionManager().shutdown();
            }
        }
    }

    @Override
    public void updateCustomDomain(String domain) throws RuntimeProvisioningException {

        HttpClient httpclient = null;
        URI uri = null;
        String ingressName = null;
        String ingJson;
        ObjectMapper objectMapper = new ObjectMapper();

        Ingress ing = new IngressBuilder()
                .withApiVersion(Ingress.ApiVersion.EXTENSIONS_V_1_BETA_1)
                .withKind("Ingress")
                .withNewMetadata()
                .withName("ingress")
                .withNamespace("dev-tom")
                .endMetadata()
                .withNewSpec().addNewRule()
                .withHost(domain)
                .withNewHttp().addNewPath()
                .withNewBackend()
                .withServiceName("tomcat-service")
                .withServicePort(new IntOrString(80))
                .endBackend()
                .endPath()
                .endHttp()
                .endRule()
                .endSpec()
                .build();

        try {

            ingJson = objectMapper.writeValueAsString(ing);
            StringEntity stringEntity = new StringEntity(ingJson, "UTF-8");
            uri = new URI(KubernetesPovisioningConstants.KUB_MASTER_URL + "apis/extensions/v1beta1/namespaces/"
                          + KubernetesProvisioningUtils.getNameSpace(applicationContext).getMetadata().getNamespace()
                          + "/ingresses/" + ingressName);

            httpclient = KubernetesProvisioningUtils.getHttpClientForKubernetes();
            HttpPut httpPut = (HttpPut) KubernetesProvisioningUtils.getHttpMethodForKubernetes(KubernetesPovisioningConstants.HTTP_PUT, uri);
            httpPut.addHeader("Content-Type", "application/json");
            httpPut.setEntity(stringEntity);

            HttpResponse response = httpclient.execute(httpPut);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeProvisioningException("Failed to update domain mapping Domain: " + domain + "HTTP error code : "
                                                       + response.getStatusLine().getStatusCode());
            }

            if (log.isDebugEnabled()) {
                log.debug("addCustomDomain response: " + response.getEntity().getContent());
            }
        } catch (JsonProcessingException e) {
            String msg = "Exception in converting the ingress object to json";
            log.error(msg, e);
            throw new RuntimeProvisioningException(e);
        } catch (KeyStoreException e) {
            String msg = "Error in keystore while connecting to Kubernetes cluster";
            log.error(msg, e);
            throw new RuntimeProvisioningException(msg, e);
        } catch (NoSuchAlgorithmException e) {
            String msg = "Cryptographic algorithm not found while connecting to Kubernetes cluster";
            log.error(msg, e);
        } catch (KeyManagementException e) {
            String msg = "Exception in key management while while connecting to Kubernetes cluster";
            log.error(msg, e);
            throw new RuntimeProvisioningException(e);
        } catch (URISyntaxException e) {
            String msg = "Error in url syntax : " + uri;
            log.error(msg, e);
            throw new RuntimeProvisioningException(e);
        } catch (UnsupportedEncodingException e) {
            String msg = "Character encoding used is not supported";
            log.error(msg, e);
            throw new RuntimeProvisioningException(e);
        } catch (ClientProtocolException e) {
            String msg = "Exception occurred in client protocol while trying to invoke Kubernetes api";
            log.error(msg, e);
            throw new RuntimeProvisioningException(e);
        } catch (IOException e) {
            String msg = "Connection exception while connecting to Kubernetes cluster";
            log.error(msg, e);
            throw new RuntimeProvisioningException(e);
        }finally {
            httpclient.getConnectionManager().shutdown();
        }
    }

    @Override
    public Set<String> getCustomDomains() throws RuntimeProvisioningException {

        HttpClient httpclient = null;
        URI uri = null;

        try {

            httpclient = KubernetesProvisioningUtils.getHttpClientForKubernetes();
            uri = new URI(KubernetesPovisioningConstants.KUB_MASTER_URL + "apis/extensions/v1beta1/namespaces/"
                          + KubernetesProvisioningUtils.getNameSpace(applicationContext).getMetadata().getNamespace()
                          + "/ingresses/");

            HttpGet httpGet = (HttpGet) KubernetesProvisioningUtils.getHttpMethodForKubernetes(KubernetesPovisioningConstants.HTTP_GET, uri);
            httpGet.addHeader("Content-Type", "application/json");

            HttpResponse response = httpclient.execute(httpGet);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeProvisioningException("Failed to get domain mappings: HTTP error code : "
                                                       + response.getStatusLine().getStatusCode());
            }
        } catch (KeyStoreException e) {
            String msg = "Error in keystore while connecting to Kubernetes cluster";
            log.error(msg, e);
            throw new RuntimeProvisioningException(msg, e);
        } catch (NoSuchAlgorithmException e) {
            String msg = "Cryptographic algorithm not found while connecting to Kubernetes cluster";
            log.error(msg, e);
        } catch (KeyManagementException e) {
            String msg = "Exception in key management while while connecting to Kubernetes cluster";
            log.error(msg, e);
            throw new RuntimeProvisioningException(e);
        } catch (URISyntaxException e) {
            String msg = "Error in url syntax : " + uri;
            log.error(msg, e);
            throw new RuntimeProvisioningException(e);
        } catch (UnsupportedEncodingException e) {
            String msg = "Character encoding used is not supported";
            log.error(msg, e);
            throw new RuntimeProvisioningException(e);
        } catch (ClientProtocolException e) {
            String msg = "Exception occurred in client protocol while trying to invoke Kubernetes api";
            log.error(msg, e);
            throw new RuntimeProvisioningException(e);
        } catch (IOException e) {
            String msg = "Connection exception while connecting to Kubernetes cluster";
            log.error(msg, e);
            throw new RuntimeProvisioningException(e);
        }finally {
            httpclient.getConnectionManager().shutdown();
        }
        return null;
    }

    @Override
    public void deleteCustomDomain(String domain) throws RuntimeProvisioningException {
        HttpClient httpclient = null;
        URI uri = null;

        try {

            httpclient = KubernetesProvisioningUtils.getHttpClientForKubernetes();
            uri = new URI(KubernetesPovisioningConstants.KUB_MASTER_URL + "apis/extensions/v1beta1/namespaces/"
                          + KubernetesProvisioningUtils.getNameSpace(applicationContext).getMetadata().getNamespace()
                          + "/ingresses/");
            HttpDelete httpDelete = (HttpDelete) KubernetesProvisioningUtils.getHttpMethodForKubernetes(KubernetesPovisioningConstants.HTTP_DELETE, uri);
            httpDelete.addHeader("Content-Type", "application/json");

            HttpResponse response = httpclient.execute(httpDelete);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeProvisioningException("Failed to delete domain mapping: HTTP error code : "
                                                       + response.getStatusLine().getStatusCode());
            }
        } catch (KeyStoreException e) {
            String msg = "Error in keystore while connecting to Kubernetes cluster";
            log.error(msg, e);
            throw new RuntimeProvisioningException(msg, e);
        } catch (NoSuchAlgorithmException e) {
            String msg = "Cryptographic algorithm not found while connecting to Kubernetes cluster";
            log.error(msg, e);
        } catch (KeyManagementException e) {
            String msg = "Exception in key management while while connecting to Kubernetes cluster";
            log.error(msg, e);
            throw new RuntimeProvisioningException(e);
        } catch (URISyntaxException e) {
            String msg = "Error in url syntax : " + uri;
            log.error(msg, e);
            throw new RuntimeProvisioningException(e);
        } catch (UnsupportedEncodingException e) {
            String msg = "Character encoding used is not supported";
            log.error(msg, e);
            throw new RuntimeProvisioningException(e);
        } catch (ClientProtocolException e) {
            String msg = "Exception occurred in client protocol while trying to invoke Kubernetes api";
            log.error(msg, e);
            throw new RuntimeProvisioningException(e);
        } catch (IOException e) {
            String msg = "Connection exception while connecting to Kubernetes cluster";
            log.error(msg, e);
            throw new RuntimeProvisioningException(e);
        }finally {
            httpclient.getConnectionManager().shutdown();
        }
    }
}
