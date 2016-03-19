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

/**
 * This class will maintains Kubernetes related constants
 */
public class KubernetesPovisioningConstants {

    public static final String PROPERTY_KUB_MASTER_URL = "KubernetesClusterConfig.{0}.Property.MasterURL";
    public static final String PROPERTY_KUB_API_VERSION = "KubernetesClusterConfig.{0}.Property.APIVersion";
    public static final String PROPERTY_KUB_API_SERVER_USERNAME = "KubernetesClusterConfig.{0}.Property.UserName";
    public static final String PROPERTY_KUB_API_SERVER_PASSWORD = "KubernetesClusterConfig.{0}.Property.Password";
    public static final String PROPERTY_KUB_VOLUME_MOUNT_PATH = "KubernetesClusterConfig.Container.Property.MountPath";

    public static String MASTER_USERNAME;
    public static String MASTER_PASSWORD;

    public static final String INGRESS_API_NAMESPACE_RESOURCE_PATH = "apis/extensions/v1beta1/namespaces/";
    public static final String INGRESS_API_RESOURCE_PATH_SUFFIX = "/ingresses/";
    public static final String MIME_TYPE_JSON = "application/json";

    public static final String KIND_NAMESPACE = "Namespace";
    public static final String KIND_DEPLOYMENT = "Deployment";
    public static final String KIND_INGRESS = "Ingress";
    public static final String KIND_SERVICE = "Service";
    public static final String KIND_SECRET = "Secret";

    public static final String ITEMS = "items";
    public static final String SPEC = "spec";
    public static final String RULES = "rules";
    public static final String HOST = "host";

    public static final String VOLUME_MOUNT = "default";

    public static final String DEFAULT_STAGE = "Development";
}