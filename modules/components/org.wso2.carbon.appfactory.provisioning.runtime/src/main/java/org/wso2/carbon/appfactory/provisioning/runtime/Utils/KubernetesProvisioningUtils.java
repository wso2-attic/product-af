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
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.utils.Base64Encoder;
import org.apache.http.auth.AUTH;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.wso2.carbon.appfactory.provisioning.runtime.KubernetesPovisioningConstants;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.ApplicationContext;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

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

        KubernetesClient kubernetesClient = new DefaultKubernetesClient();
        return kubernetesClient;
    }

    /**
     * This utility method will provide appropriate HTTP METHOD for a given URI
     *
     * @param httpMethod HTTP method type (GET, POST, PUT, .etc)
     * @param uri Endpoint uri
     * @return HTTP method for the particular endpoint
     */
    public static HttpRequestBase getHttpMethodForKubernetes(String httpMethod, URI uri) {

        String encoding = Base64Encoder.encode(
                KubernetesPovisioningConstants.MASTER_USERNAME + ":" + KubernetesPovisioningConstants.MASTER_PASSWORD);
        if(HttpGet.METHOD_NAME.equals(httpMethod)) {
            HttpGet httpGet = new HttpGet();
            httpGet.setHeader(AUTH.WWW_AUTH_RESP, AuthSchemes.BASIC + encoding);
            httpGet.setURI(uri);
            return httpGet;
        }else if(HttpPost.METHOD_NAME.equals(httpMethod)){
            HttpPost httpPost = new HttpPost();
            httpPost.setHeader(AUTH.WWW_AUTH_RESP, AuthSchemes.BASIC + encoding);
            httpPost.setURI(uri);
            return httpPost;
        }else if(HttpPut.METHOD_NAME.equals(httpMethod)){
            HttpPut httpPut = new HttpPut();
            httpPut.setHeader(AUTH.WWW_AUTH_RESP, AuthSchemes.BASIC + encoding);
            httpPut.setURI(uri);
            return httpPut;
        }else if(HttpDelete.METHOD_NAME.equals(httpMethod)){
            HttpDelete httpDelete = new HttpDelete();
            httpDelete.setHeader(AUTH.WWW_AUTH_RESP, AuthSchemes.BASIC + encoding);
            httpDelete.setURI(uri);
            return httpDelete;
        }else{
            throw new IllegalArgumentException("HTTP Method Not Supported");
        }
    }

    /**
     * This utility method will generate the HttpClient with appropriate authentication mechanism for K8s Rest Api
     *
     * @return HttpClient with appropriate authentication for api server
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public static CloseableHttpClient getHttpClientForKubernetes()
            throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

        //todo handle the rest api authentication compatible with the cloud deployment (current is for vagrant based)
        SSLContext sslcontext = SSLContexts.custom()
                .loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
        // Allow TLSv1 protocol only
        SSLConnectionSocketFactory sslsf = null;
        try {
            sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1" }, null,
                    SSLConnectionSocketFactory.getDefaultHostnameVerifier());
        }catch(Error e){
            System.out.println(e);
        }

        CloseableHttpClient httpclient = HttpClientBuilder.create().build();
        return httpclient;
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

        Map<String, String> selector = getSelector(applicationContext);
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
        Map<String, String> selector = getSelector(applicationContext);
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
    public static Map<String, String> getSelector(ApplicationContext applicationContext) {

        //todo generate a common selector valid for all types of application
        Map<String, String> selector = new HashMap<>();
        selector.put("name", applicationContext.getName());
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
        //
        return (applicationContext.getName() + "-" + applicationContext.getVersion() + "-" + domain + "-" + serviceName)
                .replace(".","-").toLowerCase();
    }
}
