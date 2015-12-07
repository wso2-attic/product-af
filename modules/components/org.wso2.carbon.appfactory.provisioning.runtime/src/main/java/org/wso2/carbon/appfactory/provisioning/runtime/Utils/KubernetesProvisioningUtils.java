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

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.utils.Base64Encoder;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.wso2.carbon.appfactory.provisioning.runtime.KubernetesPovisioningConstants;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.ApplicationContext;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * This will have the utility methods to provision kubernetes
 */

public class KubernetesProvisioningUtils {

    /**
     * This method will create a common Kubernetes client object with authentication to the Kubernetes master server
     *
     * @return Kubernetes client object
     */
    public static KubernetesClient getFabric8KubernetesClient() {

        //todo need to modify config object with master credentials properly
        Config config = new Config();
        config.setMasterUrl(KubernetesPovisioningConstants.KUB_MASTER_URL);
        config.setApiVersion(KubernetesPovisioningConstants.KUB_API_VERSION);

        KubernetesClient kubernetesClient = new DefaultKubernetesClient(config);
        return kubernetesClient;
    }

    public static List<String> getPodsFromApplicationContext(ApplicationContext applicationContext){

        List<String> podList = applicationContext.getContainerList();
        return podList;
    }

    public static List<Service> getServicesFromPod(ApplicationContext applicationContext){

        List <Service> serviceList = applicationContext.getServices();
        return  serviceList;
    }

    public static HttpRequestBase getHttpMethodForKubernetes(String httpMethod, URI uri) {

        String encoding = Base64Encoder.encode(KubernetesPovisioningConstants.MASTER_USERNAME + ":"
                                               + KubernetesPovisioningConstants.MASTER_PASSWORD);
        if(HttpGet.METHOD_NAME.equals(httpMethod)) {
            HttpGet httpGet = new HttpGet();
            httpGet.setHeader(KubernetesPovisioningConstants.AUTHORIZATION_HEADER, KubernetesPovisioningConstants.AUTHORIZATION_BASIC + encoding);
            return httpGet;
        }else if(HttpPost.METHOD_NAME.equals(httpMethod)){
            HttpPost httpPost = new HttpPost();
            httpPost.setHeader(KubernetesPovisioningConstants.AUTHORIZATION_HEADER, KubernetesPovisioningConstants.AUTHORIZATION_BASIC + encoding);
            return httpPost;
        }else if(HttpPut.METHOD_NAME.equals(httpMethod)){
            HttpPut httpPut = new HttpPut();
            httpPut.setHeader(KubernetesPovisioningConstants.AUTHORIZATION_HEADER, KubernetesPovisioningConstants.AUTHORIZATION_BASIC + encoding);
            return httpPut;
        }else if(HttpDelete.METHOD_NAME.equals(httpMethod)){
            HttpDelete httpDelete = new HttpDelete();
            httpDelete.setHeader(KubernetesPovisioningConstants.AUTHORIZATION_HEADER, KubernetesPovisioningConstants.AUTHORIZATION_BASIC + encoding);
            return httpDelete;
        }else{
            throw new IllegalArgumentException("HTTP Method Not Supported");
        }
    }

    public static HttpClient getHttpClientForKubernetes()
            throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

        //todo handle the rest api authentication compatible with the cloud deployment (current is for vagrant deployment)
        SSLContext sslcontext = SSLContexts.custom()
                .loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
        // Allow TLSv1 protocol only
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslcontext,
                new String[] { "TLSv1" },
                null,
                SSLConnectionSocketFactory.getDefaultHostnameVerifier());
        CloseableHttpClient httpclient = HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .build();

        return httpclient;
    }

    public static Namespace getNameSpace(ApplicationContext applicationContext) {
        Namespace namespace = new Namespace();
        namespace.getMetadata()
                .setNamespace(applicationContext.getTenantDomain() + "-" + applicationContext.getCurrentStage());
        return namespace;
    }
}
