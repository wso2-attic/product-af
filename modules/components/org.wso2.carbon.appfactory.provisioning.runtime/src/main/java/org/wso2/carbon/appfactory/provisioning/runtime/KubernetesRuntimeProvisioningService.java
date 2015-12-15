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

import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.PodTemplateSpecBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.kubernetes.api.model.ServiceSpecBuilder;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.api.model.extensions.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.extensions.DeploymentList;
import io.fabric8.kubernetes.api.model.extensions.DeploymentSpec;
import io.fabric8.kubernetes.api.model.extensions.DeploymentSpecBuilder;
import io.fabric8.kubernetes.api.model.extensions.DeploymentStatus;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressBuilder;
import io.fabric8.kubernetes.api.model.extensions.IngressList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.wso2.carbon.appfactory.provisioning.runtime.Utils.KubernetesProvisioningUtils;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.ApplicationContext;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.BuildConfiguration;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.Container;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.DeploymentConfig;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.DeploymentLogs;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.LogQuery;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.RuntimeProperty;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.ServiceProxy;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.TenantInfo;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        DeploymentStatus deploymentStatus = kubClient.inNamespace(namespace.getMetadata().getName())
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

        KubernetesClient kubClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();
        ServiceList serviceList = KubernetesProvisioningUtils.getServices(applicationContext);

        for (String domain : domains) {
            for (Service service : serviceList.getItems()) {
                Ingress ing = new IngressBuilder()
                        .withApiVersion(Ingress.ApiVersion.EXTENSIONS_V_1_BETA_1)
                        .withKind(KubernetesPovisioningConstants.KIND_INGRESS)
                        .withNewMetadata()
                        .withName(KubernetesProvisioningUtils
                                .createIngressMetaName(applicationContext, domain, service.getMetadata().getName()))
                        .withNamespace(namespace.getMetadata().getName())
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

                kubClient.extensions().ingress().inNamespace(namespace.getMetadata().getName()).create(ing);
            }
        }
    }

    /**
     * update a certain domain by replacing the ingresses created for related services with new ingresses
     *
     * @param oldDomain old domain name to be changed
     * @param newDomain new domain name to be changed to
     * @throws RuntimeProvisioningException
     */
    @Override
    public void updateCustomDomain(String oldDomain, String newDomain) throws RuntimeProvisioningException {

        KubernetesClient kubClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();

        ServiceList serviceList = KubernetesProvisioningUtils.getServices(applicationContext);

        for (Service service : serviceList.getItems()) {
            Ingress oldIng = new IngressBuilder().withApiVersion(Ingress.ApiVersion.EXTENSIONS_V_1_BETA_1)
                    .withKind(KubernetesPovisioningConstants.KIND_INGRESS)
                    .withNewMetadata()
                    .withName(KubernetesProvisioningUtils
                            .createIngressMetaName(applicationContext, oldDomain, service.getMetadata().getName()))
                    .withNamespace(namespace.getMetadata().getName())
                    .endMetadata()
                    .build();

            Ingress newIng = new IngressBuilder().withApiVersion(Ingress.ApiVersion.EXTENSIONS_V_1_BETA_1)
                    .withKind(KubernetesPovisioningConstants.KIND_INGRESS)
                    .withNewMetadata()
                    .withName(KubernetesProvisioningUtils
                            .createIngressMetaName(applicationContext, newDomain, service.getMetadata().getName()))
                    .withNamespace(namespace.getMetadata().getName())
                    .endMetadata()
                    .withNewSpec()
                    .addNewRule()
                    .withHost(newDomain)
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

            kubClient.extensions().ingress().inNamespace(namespace.getMetadata().getName()).delete(oldIng);
            kubClient.extensions().ingress().inNamespace(namespace.getMetadata().getName()).create(newIng);


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

        KubernetesClient kubClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();
        Set<String> domains = new HashSet<>();

        IngressList ingressList = kubClient.extensions().ingress().
                inNamespace(namespace.getMetadata().getName()).list();
        for (Ingress ingress : ingressList.getItems()){
            domains.add(ingress.getSpec().getRules().get(0).getHost());
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
        KubernetesClient kubClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();

        ServiceList serviceList = KubernetesProvisioningUtils.getServices(applicationContext);

        for (Service service : serviceList.getItems()) {
            Ingress ing = new IngressBuilder()
                    .withApiVersion(Ingress.ApiVersion.EXTENSIONS_V_1_BETA_1)
                    .withKind(KubernetesPovisioningConstants.KIND_INGRESS)
                    .withNewMetadata()
                    .withName(KubernetesProvisioningUtils
                            .createIngressMetaName(applicationContext, domain, service.getMetadata().getName()))
                    .withNamespace(namespace.getMetadata().getName())
                    .endMetadata()
                    .build();

            kubClient.extensions().ingress().inNamespace(namespace.getMetadata().getName()).delete(ing);
        }
    }
}
