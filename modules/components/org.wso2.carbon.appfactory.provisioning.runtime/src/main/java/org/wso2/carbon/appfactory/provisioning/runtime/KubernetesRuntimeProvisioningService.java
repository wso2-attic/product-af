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
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.provisioning.runtime.Utils.KubernetesProvisioningUtils;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.*;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.Container;

import java.io.BufferedReader;
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

        try {
            //Deployment creation
            for (Container container : containers) {
                io.fabric8.kubernetes.api.model.Container kubContainer = new io.fabric8.kubernetes.api.model.Container();
                kubContainer.setName(container.getBaseImageName());
                kubContainer.setImage(container.getBaseImageName() + ":" + container.getBaseImageVersion());

                //Checking whether the container is including volume mounts
                if(container.getVolumeMounts()!= null) {
                    kubContainer.setVolumeMounts(container.getVolumeMounts());
                }

                List<ContainerPort> containerPorts = new ArrayList<>();
                List<ServiceProxy> serviceProxies = container.getServiceProxies();
                if( serviceProxies != null && serviceProxies.size() > 0) {
                    for (ServiceProxy serviceProxy : serviceProxies) {
                        ContainerPort kubContainerPort = new ContainerPortBuilder()
                                .withContainerPort(serviceProxy.getServiceBackendPort())
                                .build();
                        containerPorts.add(kubContainerPort);
                    }
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
                    .withVolumes(config.getVolumes())
                    .build();

            PodTemplateSpec podTemplateSpec = new PodTemplateSpecBuilder()
                    .withMetadata(new ObjectMetaBuilder()
                    .withLabels(KubernetesProvisioningUtils.getLableMap(applicationContext))
                    .build())
                    .withSpec(podSpec)
                    .build();

            DeploymentSpec deploymentSpec = new DeploymentSpecBuilder()
                    .withReplicas(config.getReplicas())
                    .withTemplate(podTemplateSpec)
                    .build();

            Deployment deployment = new DeploymentBuilder().withApiVersion(Deployment.ApiVersion.EXTENSIONS_V_1_BETA_1)
                    .withKind(KubernetesPovisioningConstants.KIND_DEPLOYMENT)
                    .withMetadata(new ObjectMetaBuilder().withName(config.getDeploymentName().toLowerCase()).build())
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
                            .withSelector(KubernetesProvisioningUtils.getLableMap(applicationContext))
                            .withPorts(servicePorts)
                            .build();
                    //Deployment Unique service name is built using deployment name and the service name.
                    String serviceName = config.getDeploymentName() + "-" + serviceProxy.getServiceName();
                    Service service = new ServiceBuilder()
                            .withKind(KubernetesPovisioningConstants.KIND_SERVICE)
                            .withSpec(serviceSpec)
                            .withMetadata(new ObjectMetaBuilder().withName(serviceName.toLowerCase()).build())
                            .build();
                    serviceList.add(service);
                }
            }

            kubClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();
            DeploymentList deploymentList = kubClient.extensions().deployments().list();

            Deployment currentDeployement = kubClient
                    .inNamespace(namespace.getMetadata().getName()).extensions()
                    .deployments()
                    .withName(config.getDeploymentName().toLowerCase())
                    .get();
            if (currentDeployement != null) {
                //Redeployment
                //Deployment recreation should happen after comparing the new Deployment config with
                // running service configs.
                kubClient.inNamespace(namespace.getMetadata().getName()).extensions()
                        .deployments().withName(config.getDeploymentName().toLowerCase()).replace(deployment);
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
    public DeploymentLogStream streamRuntimeLogs() throws RuntimeProvisioningException {

        DeploymentLogStream deploymentLogStream = new DeploymentLogStream();
        Map<String, BufferedReader> logOutPut = new HashMap<>();
        KubernetesClient kubernetesClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();
        PodList podList = KubernetesProvisioningUtils.getPods(applicationContext);
        if (podList != null) {
            try {
                int podCounter = 1;
                for (Pod pod : podList.getItems()) {
                    for (io.fabric8.kubernetes.api.model.Container container : KubernetesHelper.getContainers(pod)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Streaming logs in pod : " + pod.getMetadata().getName() + "-" + container
                                    .getName());
                        }
                        LogWatch logs = kubernetesClient.pods().inNamespace(namespace.getMetadata().getName())
                                .withName(pod.getMetadata().getName()).inContainer(container.getName()).watchLog();

                        //logStream should close by after the streaming done in front end
                        //you can use closeLogStream() method in DeploymentStreamLogs
                        BufferedReader logStream = new BufferedReader(new InputStreamReader(logs.getOutput()));
                        logOutPut.put("Replica-" + podCounter + "-" + container.getName(), logStream);
                        deploymentLogStream.setDeploymentLogs(logOutPut);
                    }
                    podCounter++;
                }
            } catch (KubernetesClientException e) {
                log.error("Error while streaming runtime logs for application : " + applicationContext.getId()
                        + " tenant domain : " + applicationContext.getTenantInfo().getTenantDomain(), e);
                throw new RuntimeProvisioningException(
                        "Error while streaming runtime logs for application : " + applicationContext.getId()
                                + " tenant domain : " + applicationContext.getTenantInfo().getTenantDomain(), e);
            }
        } else {
            log.error("Pod list returned as null for application : " + applicationContext.getId() + " tenant domain : "
                    + applicationContext.getTenantInfo().getTenantDomain());
            throw new RuntimeProvisioningException(
                    "Pod list returned as null for application : " + applicationContext.getId() + " tenant domain : "
                            + applicationContext.getTenantInfo().getTenantDomain());
        }
        return deploymentLogStream;
    }

    @Override
    public DeploymentLogs getRuntimeLogs(LogQuery query) throws RuntimeProvisioningException {

        KubernetesClient kubernetesClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();
        DeploymentLogs deploymentLogs = new DeploymentLogs();
        Map<String, String> logOutPut = new HashMap<>();
        PrettyLoggable prettyLoggable;
        PodList podList = KubernetesProvisioningUtils.getPods(applicationContext);
        if (podList != null) {
            try {
                int podCounter = 1;
                for (Pod pod : podList.getItems()) {
                    for (io.fabric8.kubernetes.api.model.Container container : KubernetesHelper.getContainers(pod)) {
                        if (query == null || (query.getDurationInHours() < 0 && query.getTailingLines() < 0)) {
                            prettyLoggable = kubernetesClient.pods().inNamespace(namespace.getMetadata().getName())
                                    .withName(pod.getMetadata().getName()).inContainer(container.getName());
                        } else if (query.getDurationInHours() < 0 && query.getTailingLines() > 0) {
                            prettyLoggable = kubernetesClient.pods().inNamespace(namespace.getMetadata().getName())
                                    .withName(pod.getMetadata().getName()).inContainer(container.getName())
                                    .tailingLines(query.getTailingLines());
                        } else if (query.getDurationInHours() > 0 && query.getTailingLines() < 0) {
                            prettyLoggable = kubernetesClient.pods().inNamespace(namespace.getMetadata().getName())
                                    .withName(pod.getMetadata().getName()).inContainer(container.getName())
                                    .sinceSeconds(query.getDurationInHours() * 3600);
                        } else if (query.getDurationInHours() > 0 && query.getTailingLines() > 0) {
                            prettyLoggable = kubernetesClient.pods().inNamespace(namespace.getMetadata().getName())
                                    .withName(pod.getMetadata().getName()).inContainer(container.getName())
                                    .sinceSeconds(query.getDurationInHours() * 3600)
                                    .tailingLines(query.getTailingLines());
                        } else {
                            log.error("Error in log query while getting snapshot logs of application : "
                                    + applicationContext.getId() + " tenant domain : " + applicationContext
                                    .getTenantInfo().getTenantDomain());
                            throw new RuntimeProvisioningException(
                                    "Error in log query while getting snapshot logs of application : "
                                            + applicationContext.getId() + " tenant domain : " + applicationContext
                                            .getTenantInfo().getTenantDomain());
                        }
                        if (log.isDebugEnabled()) {
                            log.debug("Retrieving logs in pod : " + pod.getMetadata().getName() + "-" + container
                                    .getName());
                        }
                        String logs = (String) prettyLoggable.getLog(true);
                        logOutPut.put("Replica-" + podCounter + "-" + container.getName(), logs);
                        deploymentLogs.setDeploymentLogs(logOutPut);
                    }
                    podCounter++;
                }
            } catch (KubernetesClientException e) {
                log.error("Error while getting snapshot logs for application : " + applicationContext.getId()
                        + " tenant domain : " + applicationContext.getTenantInfo().getTenantDomain(), e);
                throw new RuntimeProvisioningException(
                        "Error while getting snapshot logs for application : " + applicationContext.getId()
                                + " tenant domain : " + applicationContext.getTenantInfo().getTenantDomain(), e);
            }
            return deploymentLogs;
        } else {
            log.error("Pod list returned as null for application : " + applicationContext.getId() + " tenant domain : "
                    + applicationContext.getTenantInfo().getTenantDomain());
            throw new RuntimeProvisioningException(
                    "Pod list returned as null for application : " + applicationContext.getId() + " tenant domain : "
                            + applicationContext.getTenantInfo().getTenantDomain());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRuntimeProperties(List<RuntimeProperty> runtimeProperties,
            DeploymentConfig deploymentConfig) throws RuntimeProvisioningException {
        //adding already created runtime properties to current list
        runtimeProperties.addAll(getRuntimeProperties());
        //list of secretes
        List<Volume> volumes = new ArrayList();
        //list of env variables
        HashMap<String, String> envVariables = new HashMap<>();
        //create a instance of kubernetes client to invoke service call
        KubernetesClient kubernetesClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();
        List<VolumeMount> volumeMounts = new ArrayList<>();

        for (RuntimeProperty runtimeProperty : runtimeProperties) {
            switch (runtimeProperty.getPropertyType()) {
            case SECURED:
                try {
                    setSecuredRuntimeProperties(volumes, kubernetesClient, volumeMounts, runtimeProperty);
                }catch (KubernetesClientException e){
                    String message = "Error while setting secured runtime properties for application : "
                            + applicationContext.getId() + " in tenant domain "
                            + applicationContext.getTenantInfo().getTenantDomain();

                    throw new RuntimeProvisioningException(message, e);
                }
                break;
            case ENVIRONMENT:
                if (log.isDebugEnabled()) {
                    String message = "Creating property type environment for the application : "
                            + applicationContext.getId() + " for the tenant domain : "
                            + applicationContext.getTenantInfo().getTenantDomain();
                    log.debug(message);
                }
                envVariables.putAll(runtimeProperty.getProperties());
                break;
            default:
                String message = "Runtime property type : " + runtimeProperty.getPropertyType() + " not supported.";
                throw new IllegalArgumentException(message);
            }
        }

        //set volume mounts and env variables for all the containers
        List<Container> containers = deploymentConfig.getContainers();
        for(Container container : containers){
            container.setVolumeMounts(volumeMounts);
            container.setEnvVariables(envVariables);
        }

        //Set secretes to a pod
        deploymentConfig.setVolumes(volumes);

        //Call deploy application to redeploy application with runtime properties
        deployApplication(deploymentConfig);
    }

    /**
     * To create K8s secretes
     * @param volumes volume for each secret
     * @param kubernetesClient K8s client
     * @param volumeMounts volume mount for each volume
     * @param runtimeProperty key value paries
     */
    private void setSecuredRuntimeProperties(List<Volume> volumes, KubernetesClient kubernetesClient,
            List<VolumeMount> volumeMounts, RuntimeProperty runtimeProperty) {
        String namespace = this.namespace.getMetadata().getName();

        if (log.isDebugEnabled()) {
            String message = "Creating property type secret for the application : " + applicationContext.getId()
                    + " for the tenant domain : " + applicationContext.getTenantInfo().getTenantDomain();
            log.debug(message);
        }

        Secret currentSecret = kubernetesClient.secrets().inNamespace(namespace)
                .withName(runtimeProperty.getName()).get();

        //if secrete exists then replace the same secrete, otherwise create a new secrete
        if (currentSecret != null) {
            kubernetesClient.secrets().inNamespace(namespace)
                    .withName(runtimeProperty.getName()).replace(currentSecret);
        } else {
            Secret secret = new SecretBuilder()
                    .withKind(KubernetesPovisioningConstants.KIND_SECRET)
                    .withApiVersion(Secret.ApiVersion.V_1)
                    .withNewMetadata()
                    .withNamespace(namespace)
                    .withLabels(KubernetesProvisioningUtils.getLableMap(applicationContext))
                    .withName(runtimeProperty.getName())
                    .endMetadata().withData(runtimeProperty.getProperties())
                    .build();

            kubernetesClient.secrets().create(secret);
        }

        Volume volume = new VolumeBuilder()
                .withName(runtimeProperty.getName())
                .withNewSecret()
                .withSecretName(runtimeProperty.getName())
                .endSecret()
                .build();

        volumes.add(volume);

        //create volume mount for the secretes
        VolumeMount volumeMount = null;
        try {
            volumeMount = new VolumeMountBuilder()
                    .withName(runtimeProperty.getName())
                    .withMountPath(AppFactoryUtil.getAppfactoryConfiguration()
                                    .getFirstProperty(KubernetesPovisioningConstants.PROPERTY_KUB_VOLUME_MOUNT_PATH)
                                    + applicationContext.getId()).withReadOnly(true).build();
        } catch (AppFactoryException e) {
            String message = "Unable to read Kubernetes configuration for domain : " + namespace
                    + "from appfactory.xml";
            log.error(message, e);
        }
        volumeMounts.add(volumeMount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RuntimeProperty> getRuntimeProperties() throws RuntimeProvisioningException {
        KubernetesClient kubernetesClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();
        SecretList secretList = kubernetesClient.secrets().inNamespace(namespace.getMetadata().getName())
                .withLabels(KubernetesProvisioningUtils.getLableMap(applicationContext)).list();
        List<RuntimeProperty> runtimeProperties = new ArrayList<>();

        for (Secret secret : secretList.getItems()) {
            RuntimeProperty sensitiveRuntimeProperty = new RuntimeProperty();
            sensitiveRuntimeProperty.setPropertyType(RuntimeProperty.PropertyType.SECURED);
            sensitiveRuntimeProperty.setName(secret.getMetadata().getName());
            sensitiveRuntimeProperty.setProperties(secret.getData());
            runtimeProperties.add(sensitiveRuntimeProperty);
        }

        PodList podList = KubernetesProvisioningUtils.getPods(applicationContext);
        HashMap<String, String> data = new HashMap<>();

        for (Pod pod : podList.getItems()) {
            //get only first container from the container list
            List<EnvVar> envVarList = pod.getSpec().getContainers().get(0).getEnv();
            for (EnvVar envVar : envVarList) {
                data.put(envVar.getName(), envVar.getValue());
            }

            RuntimeProperty environmentVariable = new RuntimeProperty();
            environmentVariable.setPropertyType(RuntimeProperty.PropertyType.ENVIRONMENT);
            environmentVariable.setProperties(data);
            runtimeProperties.add(environmentVariable);
        }

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
        Ingress createdIng;
        boolean created = false;

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
                        .withNewSpec()
                        .withRules().addNewRule()
                        .withHost(domain)
                        .withNewHttp()
                        .withPaths().addNewPath()
                        .withNewBackend()
                        .withServiceName(service.getMetadata().getName())
                        .withServicePort(new IntOrString(80))
                        .endBackend()
                        .endPath()
                        .endHttp()
                        .endRule()
                        .endSpec()
                        .build();

                createdIng = kubClient.extensions().ingress().inNamespace(namespace.getMetadata().getName()).create(ing);
                if(createdIng != null && KubernetesProvisioningUtils
                        .createIngressMetaName(applicationContext, domain, service.getMetadata().getName())
                        .equals(createdIng.getMetadata().getName())){
                    created = true;
                    log.info("Kubernetes ingress : " + ing + "created for service : " +
                            service.getMetadata().getName());
                }else{
                    created = false;
                    log.error("Error occured while creating Kubernetes ingress : " + ing + "for service : " +
                            service.getMetadata().getName());
                }
            }
        }

        return created;
    }

    /**
     * update a certain domain by replacing the ingresses created for related services with new ingresses
     *
     * @param oldDomain old domain name to be changed
     * @param newDomain new domain name to be changed to
     * @throws RuntimeProvisioningException
     */
    @Override
    public boolean updateCustomDomain(String oldDomain, String newDomain) throws RuntimeProvisioningException {

        KubernetesClient kubClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();

        ServiceList serviceList = KubernetesProvisioningUtils.getServices(applicationContext);
        boolean deleted = false;
        boolean updated = false;
        Ingress createdIng;

        for (Service service : serviceList.getItems()) {

            String oldIngName =  KubernetesProvisioningUtils
                    .createIngressMetaName(applicationContext, oldDomain, service.getMetadata().getName());

            String newIngName =  KubernetesProvisioningUtils
                    .createIngressMetaName(applicationContext, newDomain, service.getMetadata().getName());
            Ingress oldIng = new IngressBuilder().withApiVersion(Ingress.ApiVersion.EXTENSIONS_V_1_BETA_1)
                    .withKind(KubernetesPovisioningConstants.KIND_INGRESS)
                    .withNewMetadata()
                    .withName(oldIngName)
                    .withNamespace(namespace.getMetadata().getName())
                    .endMetadata()
                    .build();

            Ingress newIng = new IngressBuilder().withApiVersion(Ingress.ApiVersion.EXTENSIONS_V_1_BETA_1)
                    .withKind(KubernetesPovisioningConstants.KIND_INGRESS)
                    .withNewMetadata()
                    .withName(newIngName)
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

            deleted = kubClient.extensions().ingress().inNamespace(namespace.getMetadata().getName()).delete(oldIng);
            if(deleted) {
                createdIng =
                        kubClient.extensions().ingress().inNamespace(namespace.getMetadata().getName()).create(newIng);
                if(createdIng != null && newIngName.equals(createdIng.getMetadata().getName())){
                    updated = true;
                }else{
                    updated = false;
                    log.error("Error occured while creating Kubernetes ingress : " + newIng + "for service : " +
                            service.getMetadata().getName());
                }

            }else{
                log.error("Error occured while deleting Kubernetes ingress : " + oldIng + "for service : " +
                        service.getMetadata().getName());
            }
        }
        return  updated;
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
        boolean deleted = false;

        for (Service service : serviceList.getItems()) {
            String ingName = KubernetesProvisioningUtils
                    .createIngressMetaName(applicationContext, domain, service.getMetadata().getName());
            Ingress ing = new IngressBuilder()
                    .withApiVersion(Ingress.ApiVersion.EXTENSIONS_V_1_BETA_1)
                    .withKind(KubernetesPovisioningConstants.KIND_INGRESS)
                    .withNewMetadata()
                    .withName(ingName)
                    .withNamespace(namespace.getMetadata().getName())
                    .endMetadata()
                    .build();

            deleted = kubClient.extensions().ingress().inNamespace(namespace.getMetadata().getName()).delete(ing);
            if(!deleted){
                log.error("Error occured while deleting Kubernetes ingress : " + ingName + "for service : " +
                        service.getMetadata().getName());
            }
        }
        return deleted;
    }
}
