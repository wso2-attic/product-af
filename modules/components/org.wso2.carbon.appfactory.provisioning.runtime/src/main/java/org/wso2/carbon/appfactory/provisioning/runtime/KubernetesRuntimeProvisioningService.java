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

import io.fabric8.kubernetes.api.KubernetesHelper;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.extensions.*;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.fabric8.kubernetes.client.dsl.PrettyLoggable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.provisioning.runtime.Utils.KubernetesProvisioningUtils;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.*;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.Container;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    /**
     * Create Kubernetes Deployment and set of services according to the deployment configuration
     * @param config deployment configuration
     * @return list of created service names
     * @throws RuntimeProvisioningException
     */
    @Override
    public List<String> deployApplication(DeploymentConfig config) throws RuntimeProvisioningException {

        KubernetesClient kubClient = null;
        List<Container> containers = config.getContainers();
        ArrayList<io.fabric8.kubernetes.api.model.Container> kubContainerList = new ArrayList<>();
        List<String> serviceNameList = new ArrayList<>();
        List<VolumeMount> volumeMounts = new ArrayList<>();

        try {
            //Deployment creation
            for (Container container : containers) {
                io.fabric8.kubernetes.api.model.Container kubContainer = new io.fabric8.kubernetes.api.model.Container();
                kubContainer.setName(container.getBaseImageName());
                kubContainer.setImage(container.getBaseImageName() + ":" + container.getBaseImageVersion());
                //create volume mount for the secretes
                VolumeMount volumeMount = new VolumeMountBuilder()
                        .withName(KubernetesPovisioningConstants.VOLUME_MOUNT)
                        .withMountPath(KubernetesPovisioningConstants.VOLUME_MOUNT_PATH)
                        .withReadOnly(true)
                        .build();

                volumeMounts.add(volumeMount);
                kubContainer.setVolumeMounts(volumeMounts);

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
                    .withVolumes(config.getSecrets())
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
    public DeploymentLogs streamRuntimeLogs() throws RuntimeProvisioningException {

        DeploymentLogs deploymentLogs = new DeploymentLogs();
        Map<String, BufferedReader> logOutPut = new HashMap<>();
        KubernetesClient kubernetesClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();
        PodList podList = KubernetesProvisioningUtils.getPods(applicationContext);
        for (Pod pod : podList.getItems()) {
            for (io.fabric8.kubernetes.api.model.Container container : KubernetesHelper.getContainers(pod)) {
                LogWatch logs = kubernetesClient.pods().inNamespace(namespace.getMetadata().getName())
                        .withName(pod.getMetadata().getName()).inContainer(container.getName()).watchLog();

                BufferedReader logStream = new BufferedReader(new InputStreamReader(logs.getOutput()));
                logOutPut.put(pod.getMetadata().getName() + ":" + container.getName(), logStream);
                deploymentLogs.setDeploymentLogs(logOutPut);
            }
        }
        return deploymentLogs;
    }

    @Override
    public DeploymentLogs getRuntimeLogs(LogQuery query)
            throws RuntimeProvisioningException {
        KubernetesClient kubernetesClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();
        DeploymentLogs deploymentLogs = new DeploymentLogs();
        Map<String, BufferedReader> logOutPut = new HashMap<>();
        String logs;
        if (query != null) {
            PrettyLoggable prettyLoggable;
            PodList podList = KubernetesProvisioningUtils.getPods(applicationContext);
            for (Pod pod : podList.getItems()) {
                for (io.fabric8.kubernetes.api.model.Container container : KubernetesHelper.getContainers(pod)) {
                    if (query.getPreviousRecordsCount() > 0) {
                        prettyLoggable = kubernetesClient.pods().inNamespace(namespace.getMetadata().getName())
                                .withName(pod.getMetadata().getName()).inContainer(container.getName())
                                .tailingLines(query.getPreviousRecordsCount());
                    } else if (query.getDurationInHours() > 0) {
                        prettyLoggable = kubernetesClient.pods().inNamespace(namespace.getMetadata().getName())
                                .withName(pod.getMetadata().getName()).inContainer(container.getName())
                                .sinceSeconds(query.getDurationInHours() * 3600);
                    } else {
                        throw new RuntimeProvisioningException("Error in log retrieving query while querying logs"
                                + " of application : " + applicationContext.getName());
                    }
                    logs = (String) prettyLoggable.getLog(true);
                    InputStream is = new ByteArrayInputStream(logs.getBytes());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
                    logOutPut.put(pod.getMetadata().getName() + ":" + container.getName(), bufferedReader);
                    deploymentLogs.setDeploymentLogs(logOutPut);
                }
            }
        } else {
            PodList podList = KubernetesProvisioningUtils.getPods(applicationContext);
            for (Pod pod : podList.getItems()) {
                for (io.fabric8.kubernetes.api.model.Container container : KubernetesHelper.getContainers(pod)) {
                    logs = kubernetesClient.pods().inNamespace(namespace.getMetadata().getName())
                            .withName(pod.getMetadata().getName()).inContainer(container.getName())
                            .getLog(true);
                    InputStream is = new ByteArrayInputStream(logs.getBytes());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
                    logOutPut.put(pod.getMetadata().getName() + ":" + container.getName(), bufferedReader);
                }
            }
            deploymentLogs.setDeploymentLogs(logOutPut);
        }
        return deploymentLogs;
    }

    /**
     * Set runtime properties to kubernetes environment
     *
     * @param runtimeProperties runtime properties
     * @param deploymentConfig  includes deployment related details
     * @throws RuntimeProvisioningException
     */
    @Override
    public void setRuntimeProperties(List<RuntimeProperty> runtimeProperties, DeploymentConfig deploymentConfig)
            throws RuntimeProvisioningException {

        List secrets = new ArrayList();
        Container container = new Container();

        //create a instance of kubernetes client to invoke service call
        KubernetesClient kubernetesClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();

        for (RuntimeProperty runtimeProperty : runtimeProperties) {
            switch (runtimeProperty.getPropertyType()) {
            case SENSITIVE:
                if (log.isDebugEnabled()) {
                    String message = "Creating property type secret for the application:" + applicationContext.getName()
                            + " for the tenant domain:" + applicationContext.getTenantInfo().getTenantDomain();
                    log.debug(message);
                }
                Secret secret = new SecretBuilder().withKind(KubernetesPovisioningConstants.KIND_SECRETS)
                        .withApiVersion(Secret.ApiVersion.V_1).withNewMetadata()
                        .withNamespace(namespace.getMetadata().getName()).withName(runtimeProperty.getName())
                        .endMetadata().withData(runtimeProperty.getProperties()).build();

                kubernetesClient.secrets().create(secret);

                Volume volume = new VolumeBuilder()
                        .withName(KubernetesPovisioningConstants.VOLUME_MOUNT)
                        .withNewSecret()
                        .withSecretName(runtimeProperty.getName())
                        .endSecret()
                        .build();

                secrets.add(volume);

                break;
            case ENVIRONMENT:
                if (log.isDebugEnabled()) {
                    String message = "Creating property type environment for the application:"
                            + applicationContext.getName() + " for the tenant domain:"
                            + applicationContext.getTenantInfo().getTenantDomain();
                    log.debug(message);
                }

                //setting environment variables to a container
                container.setEnvVariables(runtimeProperty.getProperties());

                break;
            default:
                String message = "Runtime property type is not support, property type:"
                        + runtimeProperty.getPropertyType();

                throw new IllegalArgumentException(message);

            }
        }

        List<Container> containers = new ArrayList<>();
        containers.add(container);
        deploymentConfig.setContainers(containers);

        //Calling application method to deploy the runtime properties with the application
        deployApplication(deploymentConfig);

    }

    /**
     * Update runtime properties already defined in the application
     *
     * @param runtimeProperties list of runtime properties
     * @param deploymentConfig  includes deployment related details
     * @throws RuntimeProvisioningException
     */
    @Override
    public void updateRuntimeProperties(List<RuntimeProperty> runtimeProperties, DeploymentConfig deploymentConfig)
            throws RuntimeProvisioningException {

        List secrets = new ArrayList();
        Container container = new Container();

        for (RuntimeProperty runtimeProperty : runtimeProperties) {
            switch (runtimeProperty.getPropertyType()) {
            case SENSITIVE:
                if (log.isDebugEnabled()) {
                    String message = "Updating property type secret for the application:" + applicationContext.getName()
                            + " for the tenant domain:" + applicationContext.getTenantInfo().getTenantDomain();

                    log.debug(message);
                }
                Secret secret = new SecretBuilder().withKind(KubernetesPovisioningConstants.KIND_SECRETS)
                        .withApiVersion(Secret.ApiVersion.V_1)
                        .withNewMetadata()
                        .withNamespace(namespace.getMetadata().getNamespace())
                        .withName(runtimeProperty.getName())
                        .endMetadata()
                        .withData(runtimeProperty.getProperties())
                        .build();

                KubernetesClient kubernetesClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();
                kubernetesClient.secrets().inNamespace(namespace.getMetadata().getName())
                        .withName(runtimeProperty.getName()).replace(secret);

                Volume volume = new VolumeBuilder()
                        .withName(KubernetesPovisioningConstants.VOLUME_MOUNT)
                        .withNewSecret()
                        .withSecretName(runtimeProperty.getName())
                        .endSecret()
                        .build();

                secrets.add(volume);

                break;
            case ENVIRONMENT:
                if (log.isDebugEnabled()) {
                    String message = "Updating property type environment for the application:"
                            + applicationContext.getName() + " for the tenant domain:"
                            + applicationContext.getTenantInfo().getTenantDomain();

                    log.debug(message);
                }

                //updating environment variables for container
                container.setEnvVariables(runtimeProperty.getProperties());

                break;
            default:
                String message = "Runtime property type is not support, property type:"
                        + runtimeProperty.getPropertyType();

                throw new IllegalArgumentException(message);
            }
        }

        List<Container> containers = new ArrayList<>();
        containers.add(container);
        deploymentConfig.setContainers(containers);

        //call application deployment to re deploy the application with update variables
        deployApplication(deploymentConfig);
    }

    /**
     * Provide runtime properties for given application context
     *
     * @return list of runtime properties
     * @throws RuntimeProvisioningException
     */
    @Override
    public List<RuntimeProperty> getRuntimeProperties() throws RuntimeProvisioningException {

        KubernetesClient kubernetesClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();
        SecretList secretList = kubernetesClient.secrets().inNamespace(namespace.getMetadata().getName()).list();

        List<RuntimeProperty> runtimeProperties = new ArrayList<>();

        for (Secret secret : secretList.getItems()) {
            RuntimeProperty sensitiveRuntimeProperty = new RuntimeProperty();
            sensitiveRuntimeProperty.setPropertyType(RuntimeProperty.PropertyType.SENSITIVE);
            sensitiveRuntimeProperty.setName(secret.getMetadata().getName());
            sensitiveRuntimeProperty.setProperties(secret.getData());
            runtimeProperties.add(sensitiveRuntimeProperty);

        }

        Pod pod = kubernetesClient.pods().inNamespace(namespace.getMetadata().getName())
                .withName(applicationContext.getName()).get();

        //get only first container from the container list
        List<EnvVar> envVarList = pod.getSpec().getContainers().get(0).getEnv();
        HashMap<String, String> data = new HashMap<>();

        for (EnvVar envVar : envVarList) {
            data.put(envVar.getName(), envVar.getValue());
        }

        RuntimeProperty environmentVariable = new RuntimeProperty();
        environmentVariable.setPropertyType(RuntimeProperty.PropertyType.ENVIRONMENT);
        environmentVariable.setProperties(data);

        runtimeProperties.add(environmentVariable);

        return runtimeProperties;

    }

    /**
     * add a set of custom domains for an application version. This will create an ingress for each domain
     * and each service
     *
     * @param domains set of domains
     * @throws RuntimeProvisioningException
     */
    @Override
    public boolean addCustomDomain(Set<String> domains) throws RuntimeProvisioningException {

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

        return false;
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
    public boolean deleteCustomDomain(String domain) throws RuntimeProvisioningException {
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
        return false;
    }
}
