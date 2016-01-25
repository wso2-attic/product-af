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

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.provisioning.runtime.KubernetesPovisioningConstants;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.ApplicationContext;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.TenantInfo;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * This will have the utility methods to provision kubernetes
 */

public class KubernetesProvisioningUtils {
    private static final Log log = LogFactory.getLog(KubernetesProvisioningUtils.class);

    /**
     * This method will create a common Kubernetes client object with authentication to the Kubernetes master server
     *
     * @return Kubernetes client object
     */
    public static KubernetesClient getFabric8KubernetesClient() {

        String stage = KubernetesPovisioningConstants.DEFAULT_STAGE;
        return getFabric8KubernetesClient(stage);
    }

    /**
     * Create Kubernetes client for given stage
     *
     * @param stage stage of the application
     * @return Kubernetes client
     */
    public static KubernetesClient getFabric8KubernetesClient(String stage) {
        KubernetesClient kubernetesClient = null;
        try {
            String masterURL = AppFactoryUtil.getAppfactoryConfiguration()
                    .getFirstProperty(MessageFormat.format(KubernetesPovisioningConstants.PROPERTY_KUB_MASTER_URL, stage));
            String APIVersion = AppFactoryUtil.getAppfactoryConfiguration()
                    .getFirstProperty(MessageFormat.format(KubernetesPovisioningConstants.PROPERTY_KUB_API_VERSION, stage));
            String userName = AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(
                    MessageFormat.format(KubernetesPovisioningConstants.PROPERTY_KUB_API_SERVER_USERNAME, stage));
            String password = AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(
                    MessageFormat.format(KubernetesPovisioningConstants.PROPERTY_KUB_API_SERVER_PASSWORD, stage));

            Config config = new Config();
            config.setMasterUrl(masterURL);
            config.setApiVersion(APIVersion);
            config.setUsername(userName);
            config.setPassword(password);
            config.setNoProxy(new String[] { masterURL });

            kubernetesClient = new DefaultKubernetesClient(config);

        } catch (AppFactoryException e) {
            String message = "Unable to read Kubernetes configuration for stage : " + stage + " from appfactory.xml";
            log.error(message, e);
        }

        return kubernetesClient;
    }

    /**
     * This utility method will generate the namespace of the current application context
     *
     * @param applicationContext context of the current application
     * @return namespace of the current application context
     */
    public static Namespace getNameSpace(ApplicationContext applicationContext) {

        // todo: consider constraints of 24 character limit in namespace.
        String ns = applicationContext.getTenantInfo().getTenantDomain() + "-" + applicationContext.getCurrentStage();
        ns = ns.replace(".", "-").toLowerCase();

        log.info("Constructing a namespace with value: "+ns);
        ObjectMeta metadata = new ObjectMetaBuilder()
                .withName(ns)
                .build();
        return new NamespaceBuilder()
                .withMetadata(metadata)
                .build();
    }

    /**
     * This utility method will provide the list of pods for particular application
     *
     * @param applicationContext context of the current application
     * @return list of pods related for the current application context
     */
    public static PodList getPods (ApplicationContext applicationContext){

        Map<String, String> selector = getLableMap(applicationContext);
        KubernetesClient kubernetesClient = getFabric8KubernetesClient();
        PodList podList = kubernetesClient.inNamespace(getNameSpace(applicationContext).getMetadata()
                .getName()).pods().withLabels(selector).list();
        return podList;
    }

    /**
     * This utility method will provide the list of services for particular application
     *
     * @param applicationContext context of the current application
     * @return list of services related for the current application context
     */
    public static ServiceList getServices(ApplicationContext applicationContext){
        Map<String, String> selector = getLableMap(applicationContext);
        KubernetesClient kubernetesClient = getFabric8KubernetesClient();
        ServiceList serviceList = kubernetesClient.inNamespace(getNameSpace(applicationContext).getMetadata()
                .getName()).services().withLabels(selector).list();
        return serviceList;
    }
    /**
     * This utility method will generate the appropriate selector for filter out the necessary kinds for particular
     * application
     *
     * @param applicationContext context of the current application
     * @return selector which can be use to filter out certain kinds
     */
    public static Map<String, String> getLableMap(ApplicationContext applicationContext) {

        //todo generate a common selector valid for all types of application
        Map<String, String> selector = new HashMap<>();
        selector.put("app", applicationContext.getId());
        selector.put("version", applicationContext.getVersion());
        selector.put("stage",applicationContext.getCurrentStage());
        return selector;
    }

    /**
     * Generate a unique name for an ingress
     * @param applicationContext
     * @param domain
     * @param serviceName
     * @return generated unique name for ingress (appName-appVersion-service)
     */
    public static String createIngressMetaName(ApplicationContext applicationContext, String domain, String serviceName){

        return (applicationContext.getId() + "-" + applicationContext.getVersion() + "-" + domain + "-" + serviceName)
                .replace(".","-").toLowerCase();
    }

    public static ApplicationContext getApplicationContext(String id, String version, String stage,
            String type, int tenantId, String tenantDomain){
        ApplicationContext applicationContext = new ApplicationContext();
        applicationContext.setId(id);
        applicationContext.setVersion(version);
        applicationContext.setType(type);
        applicationContext.setCurrentStage(stage);
        TenantInfo tenantInfo = new TenantInfo();
        tenantInfo.setTenantId(tenantId);
        tenantInfo.setTenantDomain(tenantDomain);
        applicationContext.setTenantInfo(tenantInfo);
        return applicationContext;
    }
}
