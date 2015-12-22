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
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.extensions.*;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.appfactory.provisioning.runtime.Utils.KubernetesProvisioningUtils;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.*;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.Container;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * This class will implement the runtime provisioning service specific to Kubernetes
 */
public class KubernetesRuntimeProvisioningService implements RuntimeProvisioningService {

    private static final Log log = LogFactory.getLog(KubernetesRuntimeProvisioningService.class);
    private ApplicationContext applicationContext;
    private Namespace namespace;

    public KubernetesRuntimeProvisioningService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.namespace = KubernetesProvisioningUtils.getNameSpace(applicationContext);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws RuntimeProvisioningException {

    }

    @Override
    public void createOrganization(TenantInfo tenantInfo) throws RuntimeProvisioningException {
        KubernetesClient kubernetesClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();
        kubernetesClient.namespaces().create(this.namespace);
        kubernetesClient.close();
    }

    @Override
    public void updateOrganization(TenantInfo tenantInfo) throws RuntimeProvisioningException {

    }

    @Override
    public void deleteOrganization(TenantInfo tenantInfo) throws RuntimeProvisioningException {
        KubernetesClient kubernetesClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();
        kubernetesClient.namespaces().delete(this.namespace);
        kubernetesClient.close();
    }

    @Override
    public void archiveOrganization(TenantInfo tenantInfo) throws RuntimeProvisioningException {

    }

   

    @Override
    public List<String> deployApplication(DeploymentConfig config) throws RuntimeProvisioningException {

        KubernetesClient kubClient = null;
        List<Container> containers = config.getContainers();
        ArrayList<io.fabric8.kubernetes.api.model.Container> kubContainerList = new ArrayList<>();
        List<String> serviceNameList = new ArrayList<>();

        try {
            //Deployment creation
            for (Container container : containers) {
                io.fabric8.kubernetes.api.model.Container kubContainer = new io.fabric8.kubernetes.api.model.Container();
                kubContainer.setName(container.getBaseImageName());
                kubContainer.setImage(container.getBaseImageName() + ":" + container.getBaseImageVersion());
                List<ContainerPort> containerPorts = new ArrayList<>();
                List<ServiceProxy> serviceProxies = container.getServiceProxies();
                for (ServiceProxy serviceProxy : serviceProxies) {
                    ContainerPort kubContainerPort = new ContainerPortBuilder()
                            .withContainerPort(serviceProxy.getServiceBackendPort())
                            .build();
                    containerPorts.add(kubContainerPort);
                }
                kubContainer.setPorts(containerPorts);
                List<EnvVar> envVarList = new ArrayList<>();
                for (Map.Entry envVarEntry : container.getEnvVariables().entrySet()) {
                    EnvVar envVar = new EnvVarBuilder()
                            .withName((String) envVarEntry.getKey())
                            .withValue((String) envVarEntry.getValue())
                            .build();
                    envVarList.add(envVar);
                }
                kubContainer.setEnv(envVarList);
                kubContainerList.add(kubContainer);
            }

            PodSpec podSpec = new PodSpecBuilder()
                    .withContainers(kubContainerList)
                    .build();

            PodTemplateSpec podTemplateSpec = new PodTemplateSpecBuilder()
                    .withMetadata(new ObjectMetaBuilder().withLabels(config.getLables()).build())
                    .withSpec(podSpec)
                    .build();

            DeploymentSpec deploymentSpec = new DeploymentSpecBuilder()
                    .withReplicas(config.getReplicas())
                    .withTemplate(podTemplateSpec)
                    .build();

            Deployment deployment = new DeploymentBuilder().withApiVersion(Deployment.ApiVersion.EXTENSIONS_V_1_BETA_1)
                    .withKind(KubernetesPovisioningConstants.KIND_DEPLOYMENT)
                    .withMetadata(new ObjectMetaBuilder().withName(config.getDeploymentName()).build())
                    .withSpec(deploymentSpec)
                    .build();

            //Service creation
            List<Service> serviceList = new ArrayList<>();
            for (Container container : containers) {
                List<ServiceProxy> serviceProxies = container.getServiceProxies();
                for (ServiceProxy serviceProxy : serviceProxies) {
                    ServicePort servicePorts = new ServicePortBuilder()
                            .withName(serviceProxy.getServiceName())
                            .withProtocol(serviceProxy.getServiceProtocol())
                            .withPort(serviceProxy.getServicePort())
                            .withTargetPort(new IntOrString(serviceProxy.getServiceBackendPort()))
                            .build();
                    ServiceSpec serviceSpec = new ServiceSpecBuilder()
                            .withSelector(config.getLables())
                            .withPorts(servicePorts)
                            .build();
                    //Deployment Unique service name is built using deployment name and the service name.
                    String serviceName = config.getDeploymentName() + "-" + serviceProxy.getServiceName();
                    Service service = new ServiceBuilder()
                            .withKind(KubernetesPovisioningConstants.KIND_SERVICE)
                            .withSpec(serviceSpec)
                            .withMetadata(new ObjectMetaBuilder().withName(serviceName).build())
                            .build();
                    serviceList.add(service);
                }
            }

            kubClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();
            DeploymentList deploymentList = kubClient.extensions().deployments().list();

            if (deploymentList.getItems().contains(deployment)) {
                //Redeployment
                //Deployment recreation should happen after comparing the new Deployment config with
                // running service configs.
                kubClient.inNamespace(namespace.getMetadata().getName()).extensions()
                        .deployments().withName(config.getDeploymentName()).replace(deployment);
                //Service recreation should happen after comparing the new service config with running service configs.
                for (Service service : serviceList) {
                    kubClient.inNamespace(namespace.getMetadata().getName()).services().replace(service);
                    serviceNameList.add(service.getMetadata().getName());
                }
            } else {
                //New Deployment
                kubClient.inNamespace(namespace.getMetadata().getName()).extensions()
                        .deployments().create(deployment);
                for (Service service : serviceList) {
                    kubClient.inNamespace(namespace.getMetadata().getName()).services().create(service);
                    serviceNameList.add(service.getMetadata().getName());
                }
            }
        } catch (KubernetesClientException e) {
            String msg = "Error while creating Deployment : " + config.getDeploymentName();
            log.error(msg, e);
            throw new RuntimeProvisioningException(msg, e);
        } finally {
            if (kubClient != null) {
                kubClient.close();
            }
        }
        return serviceNameList;
    }

    @Override
    public boolean getDeploymentStatus(DeploymentConfig config) throws RuntimeProvisioningException {

        DefaultKubernetesClient kubClient = null;
        DeploymentStatus deploymentStatus = kubClient.inNamespace(namespace.getMetadata().getNamespace())
                .extensions().deployments().withName(config.getDeploymentName()).get().getStatus();
        //Assuming AF does not do zero replica deployments
        if (deploymentStatus.getReplicas() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public DeploymentLogs streamRuntimeLogs(DeploymentConfig deploymentConfig) throws RuntimeProvisioningException {

        LogQuery query = new LogQuery(true, 0, 0);
        DeploymentLogs deploymentLogs = new DeploymentLogs();
        Map<String, BufferedReader> logOutPut = new HashMap<>();
        URI uri = null;
        CloseableHttpClient httpclient = null;
        PodList podList = KubernetesProvisioningUtils.getPods(applicationContext);
        for (Pod pod : podList.getItems()) {

            try {
                for (Container container : deploymentConfig.getContainers()) {
                    uri = new URI(KubernetesPovisioningConstants.KUB_MASTER_URL
                            + "/api/v1/namespaces/" + namespace.getMetadata().getName()
                            + "/pods/" + pod.getMetadata().getName()
                            + "/log?container=" +container.getContainerName()
                            + "&follow=" + String.valueOf(query.getIsFollowing()));

                    HttpGet httpGet = (HttpGet) KubernetesProvisioningUtils
                            .getHttpMethodForKubernetes(HttpGet.METHOD_NAME, uri);
                    httpclient = KubernetesProvisioningUtils.getHttpClientForKubernetes();
                    HttpResponse response = httpclient.execute(httpGet);
                    BufferedReader logStream =
                            new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    logOutPut.put(pod.getMetadata().getName()+ ":" + container.getContainerName(), logStream);
                    deploymentLogs.setDeploymentLogs(logOutPut);
                }

            } catch (URISyntaxException e) {
                String msg = "Error in url syntax : " + uri + " while getting logs from container : "
                        + pod.getMetadata().getName();
                log.error(msg, e);
                throw new RuntimeProvisioningException(msg, e);
            } catch (NoSuchAlgorithmException e) {
                String msg =
                        "Error in SSL protocol while connecting to Kubernetes api while getting logs from container : "
                                + pod.getMetadata().getName();
                log.error(msg, e);
                throw new RuntimeProvisioningException(msg, e);
            } catch (IOException e) {
                String msg = "Error while reading log stream from container : " + pod.getMetadata().getName();
                log.error(msg, e);
                throw new RuntimeProvisioningException(msg, e);
            } catch (KeyManagementException e) {
                String msg = "Error creating SSL connection to Kubernetes api while getting logs from container : "
                        + pod.getMetadata().getName();
                log.error(msg, e);
                throw new RuntimeProvisioningException(e);
            } catch (KeyStoreException e) {
                String msg = "Error creating SSL connection to Kubernetes api while getting logs from container : "
                        + pod.getMetadata().getName();
                log.error(msg, e);
                throw new RuntimeProvisioningException(e);
            } finally {
                httpclient.getConnectionManager().shutdown();
            }
        }
        return deploymentLogs;
    }

    @Override
    public DeploymentLogs getRuntimeLogs(DeploymentConfig deploymentConfig, LogQuery query)
            throws RuntimeProvisioningException {

        DeploymentLogs deploymentLogs = new DeploymentLogs();
        Map<String, BufferedReader> logOutPut = new HashMap<>();
        URI uri = null;
        CloseableHttpClient httpclient = null;

        if (query != null) {
            PodList podList = KubernetesProvisioningUtils.getPods(applicationContext);
            for (Pod pod : podList.getItems()) {
                try {
                    for (Container container : deploymentConfig.getContainers()) {

                        if (query.getPreviousRecordsCount() > 0) {
                            uri = new URI(KubernetesPovisioningConstants.KUB_MASTER_URL
                                    + "/api/v1/namespaces/" + namespace.getMetadata().getName()
                                    + "/pods/" + pod.getMetadata().getName()
                                    + "/log?container=" + container.getContainerName()
                                    + "&previous=" + String.valueOf(query.getPreviousRecordsCount()));
                        } else if (query.getDurationInHours() > 0) {
                            uri = new URI(KubernetesPovisioningConstants.KUB_MASTER_URL
                                    + "/api/v1/namespaces/" + namespace.getMetadata().getName()
                                    + "/pods/" + pod.getMetadata().getName()
                                    + "/log?container=" + container.getContainerName()
                                    + "&timestamps=" + String.valueOf(query.getDurationInHours()));
                        } else {
                            throw new RuntimeProvisioningException("Error in log retrieving query while querying logs" +
                                    " of application : " + applicationContext.getName());
                        }

                        HttpGet httpGet = (HttpGet) KubernetesProvisioningUtils
                                .getHttpMethodForKubernetes(HttpGet.METHOD_NAME, uri);
                        httpclient = KubernetesProvisioningUtils.getHttpClientForKubernetes();
                        HttpResponse response = httpclient.execute(httpGet);
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity()
                                .getContent()));
                        logOutPut.put(pod.getMetadata().getName() + ":" + container.getContainerName(), bufferedReader);
                        deploymentLogs.setDeploymentLogs(logOutPut);
                    }
                } catch (URISyntaxException e) {
                    String msg = "Error in url syntax : " + uri + " while getting logs from container : "
                            + pod.getMetadata().getName();
                    log.error(msg, e);
                    throw new RuntimeProvisioningException(msg, e);
                } catch (NoSuchAlgorithmException e) {
                    String msg =
                            "Error in SSL protocol while connecting to Kubernetes api while getting logs from container : "
                                    + pod.getMetadata().getName();
                    log.error(msg, e);
                    throw new RuntimeProvisioningException(msg, e);
                } catch (IOException e) {
                    String msg = "Error while reading log stram from container : " + pod.getMetadata().getName();
                    log.error(msg, e);
                    throw new RuntimeProvisioningException(msg, e);
                } catch (KeyManagementException e) {
                    String msg = "Error creating SSL connection to Kubernetes api while getting logs from container : "
                            + pod.getMetadata().getName();
                    log.error(msg, e);
                    throw new RuntimeProvisioningException(e);
                } catch (KeyStoreException e) {
                    String msg = "Error creating SSL connection to Kubernetes api while getting logs from container : "
                            + pod.getMetadata().getName();
                    log.error(msg, e);
                    throw new RuntimeProvisioningException(e);
                } finally {
                    httpclient.getConnectionManager().shutdown();
                }
            }
        } else {
            KubernetesClient kubernetesClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();
            PodList podList = KubernetesProvisioningUtils.getPods(applicationContext);
            for (Pod pod : podList.getItems()) {
                kubernetesClient.extensions().deployments();
                for (Container container : deploymentConfig.getContainers()) {
                    String logs = kubernetesClient.pods().inNamespace(namespace.getMetadata().getName())
                            .withName(pod.getMetadata().getName()).inContainer(container.getContainerName())
                            .getLog(true);
                    InputStream is = new ByteArrayInputStream(logs.getBytes());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
                    logOutPut.put(pod.getMetadata().getName() + ":" + container.getContainerName(), bufferedReader);
                }
            }
        }
        return deploymentLogs;
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

    /**
     * add a set of custom domains for an application version. This will create an ingress for each domain
     * and each service
     *
     * @param domains set of domains
     * @throws RuntimeProvisioningException
     */
    @Override
    public void addCustomDomain(Set<String> domains) throws RuntimeProvisioningException {

        HttpClient httpclient = null;
        URI uri = null;
        String ingJson;
        ObjectMapper objectMapper = new ObjectMapper();

        ServiceList serviceList = KubernetesProvisioningUtils.getServices(applicationContext);

        for (String domain : domains) {
            for (Service service : serviceList.getItems()) {
                Ingress ing = new IngressBuilder()
                        .withApiVersion(Ingress.ApiVersion.EXTENSIONS_V_1_BETA_1)
                        .withKind(KubernetesPovisioningConstants.KIND_INGRESS)
                        .withNewMetadata()
                        .withName(KubernetesProvisioningUtils
                                .createIgressMetaName(applicationContext, domain, service.getMetadata().getName()))
                        .withNamespace(namespace.getMetadata().getNamespace())
                        .endMetadata()
                        .withNewSpec().addNewRule()
                        .withHost(domain)
                        .withNewHttp().addNewPath()
                        .withNewBackend()
                        .withServiceName(service.getMetadata().getName())
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
                    uri = new URI(KubernetesPovisioningConstants.KUB_MASTER_URL
                            + KubernetesPovisioningConstants.INGRESS_API_NAMESPACE_RESOURCE_PATH
                            + namespace.getMetadata().getNamespace()
                            + KubernetesPovisioningConstants.INGRESS_API_RESOURCE_PATH_SUFFIX);

                    HttpPost httpPost = (HttpPost) KubernetesProvisioningUtils
                            .getHttpMethodForKubernetes(HttpPost.METHOD_NAME, uri);
                    httpPost.addHeader(HttpHeaders.CONTENT_TYPE, KubernetesPovisioningConstants.MIME_TYPE_JSON);
                    httpPost.setEntity(stringEntity);

                    HttpResponse response = httpclient.execute(httpPost);

                    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                        throw new RuntimeProvisioningException(
                                "Failed to add domain mapping Domain: " + domains + "HTTP error code : " + response
                                        .getStatusLine().getStatusCode());
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
                } finally {
                    httpclient.getConnectionManager().shutdown();
                }
            }
        }
    }

    /**
     * update a certain domain by replacing the ingresses created for related services with new ingresses
     *
     * @param domain domain name
     * @throws RuntimeProvisioningException
     */
    @Override
    public void updateCustomDomain(String domain) throws RuntimeProvisioningException {

        HttpClient httpclient = null;
        URI uri = null;
        String ingressName = null;
        String ingJson;
        ObjectMapper objectMapper = new ObjectMapper();

        ServiceList serviceList = KubernetesProvisioningUtils.getServices(applicationContext);

        for (Service service : serviceList.getItems()) {
            Ingress ing = new IngressBuilder().withApiVersion(Ingress.ApiVersion.EXTENSIONS_V_1_BETA_1)
                    .withKind(KubernetesPovisioningConstants.KIND_INGRESS)
                    .withNewMetadata()
                    .withName(KubernetesProvisioningUtils
                            .createIgressMetaName(applicationContext, domain, service.getMetadata().getName()))
                    .withNamespace(namespace.getMetadata().getNamespace())
                    .endMetadata()
                    .withNewSpec()
                    .addNewRule()
                    .withHost(domain)
                    .withNewHttp()
                    .addNewPath()
                    .withNewBackend()
                    .withServiceName(service.getMetadata().getName())
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
                uri = new URI(KubernetesPovisioningConstants.KUB_MASTER_URL
                        + KubernetesPovisioningConstants.INGRESS_API_NAMESPACE_RESOURCE_PATH
                        + namespace.getMetadata().getNamespace()
                        + KubernetesPovisioningConstants.INGRESS_API_RESOURCE_PATH_SUFFIX
                        + ingressName);

                httpclient = KubernetesProvisioningUtils.getHttpClientForKubernetes();
                HttpPut httpPut = (HttpPut) KubernetesProvisioningUtils
                        .getHttpMethodForKubernetes(HttpPut.METHOD_NAME, uri);
                httpPut.addHeader(HttpHeaders.CONTENT_TYPE, KubernetesPovisioningConstants.MIME_TYPE_JSON);
                httpPut.setEntity(stringEntity);

                HttpResponse response = httpclient.execute(httpPut);

                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    throw new RuntimeProvisioningException(
                            "Failed to update domain mapping Domain: " + domain + "HTTP error code : " + response
                                    .getStatusLine().getStatusCode());
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
            } finally {
                httpclient.getConnectionManager().shutdown();
            }
        }
    }

    /**
     * get a set of custom domains for a particular applicaiton context
     *
     * @return set of domains
     * @throws RuntimeProvisioningException
     */
    @Override
    public Set<String> getCustomDomains() throws RuntimeProvisioningException {

        HttpClient httpclient = null;
        URI uri = null;
        Set<String> domains = new HashSet<>();
        String output = "";

        try {

            httpclient = KubernetesProvisioningUtils.getHttpClientForKubernetes();
            uri = new URI(KubernetesPovisioningConstants.KUB_MASTER_URL
                    + KubernetesPovisioningConstants.INGRESS_API_NAMESPACE_RESOURCE_PATH
                    + namespace.getMetadata().getNamespace()
                    + KubernetesPovisioningConstants.INGRESS_API_RESOURCE_PATH_SUFFIX);

            HttpGet httpGet = (HttpGet) KubernetesProvisioningUtils.getHttpMethodForKubernetes(HttpGet.METHOD_NAME, uri);
            httpGet.addHeader(HttpHeaders.CONTENT_TYPE, KubernetesPovisioningConstants.MIME_TYPE_JSON);

            HttpResponse response = httpclient.execute(httpGet);

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new RuntimeProvisioningException("Failed to get domain mappings: HTTP error code : "
                        + response.getStatusLine().getStatusCode());
            }

            BufferedReader br = new BufferedReader(
                    new InputStreamReader((response.getEntity().getContent())));

            while ((output = br.readLine()) != null) {
                output += output;
            }

            JSONObject jsonObject = new JSONObject(output.trim());

            for (int i = 0; i < jsonObject.getJSONArray(KubernetesPovisioningConstants.ITEMS).length(); i++) {
                domains.add(
                        jsonObject.getJSONArray(KubernetesPovisioningConstants.ITEMS)
                                .getJSONObject(i).getJSONObject(KubernetesPovisioningConstants.SPEC)
                                .getJSONArray(KubernetesPovisioningConstants.RULES)
                                .getJSONObject(0).getString(KubernetesPovisioningConstants.HOST));
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
        } catch (JSONException e) {
            String msg = "Error occured while parsing JSON: " + output;
            log.error(msg, e);
            throw new RuntimeProvisioningException(e);
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
        return domains;
    }

    /**
     * delete a custom domain and delete the ingresses created for related services
     *
     * @param domain domain name
     * @throws RuntimeProvisioningException
     */
    @Override
    public void deleteCustomDomain(String domain) throws RuntimeProvisioningException {
        HttpClient httpclient = null;
        URI uri = null;

        ServiceList serviceList = KubernetesProvisioningUtils.getServices(applicationContext);

        for (Service service : serviceList.getItems()) {
            try {

                httpclient = KubernetesProvisioningUtils.getHttpClientForKubernetes();
                uri = new URI(KubernetesPovisioningConstants.KUB_MASTER_URL
                        + KubernetesPovisioningConstants.INGRESS_API_NAMESPACE_RESOURCE_PATH
                        + namespace.getMetadata().getNamespace()
                        + KubernetesPovisioningConstants.INGRESS_API_RESOURCE_PATH_SUFFIX
                        + KubernetesProvisioningUtils
                        .createIgressMetaName(applicationContext, domain, service.getMetadata().getName()));
                HttpDelete httpDelete = (HttpDelete) KubernetesProvisioningUtils
                        .getHttpMethodForKubernetes(HttpDelete.METHOD_NAME, uri);
                httpDelete.addHeader(HttpHeaders.CONTENT_TYPE, KubernetesPovisioningConstants.MIME_TYPE_JSON);

                HttpResponse response = httpclient.execute(httpDelete);

                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    throw new RuntimeProvisioningException("Failed to delete domain mapping: HTTP error code : "
                            + response.getStatusLine()
                            .getStatusCode());
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
            } finally {
                httpclient.getConnectionManager().shutdown();
            }
        }
    }
}
