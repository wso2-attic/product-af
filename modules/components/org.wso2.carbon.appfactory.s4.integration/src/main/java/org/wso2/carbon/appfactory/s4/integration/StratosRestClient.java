/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.appfactory.s4.integration;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.MutualAuthHttpClient;
import org.wso2.carbon.appfactory.common.util.ServerResponse;

/**
 * Mutual Http Client based REST Client for calling Stratos APIs
 */
public class StratosRestClient {

    private static final Log log = LogFactory.getLog(StratosRestClient.class);

    private String stratosManagerURL;
    private String username;

    private static final String APPLICATIONS_REST_END_POINT = "/api/applications";
    //TODO : Move this to appfactory configuration
    private static final String SINGLE_TENANT_APPLICATION_POLICY_ID = "application-policy-st";

    public StratosRestClient(String stratosManagerURL, String username) {
        this.username = username;
        this.stratosManagerURL = stratosManagerURL;
    }

    public static StratosRestClient getInstance(String stratosManagerURL, String username) {
        return new StratosRestClient(stratosManagerURL, username);
    }

    public void createApplication(String applicationId, String repoUrl, String repoUsername, String repoPassword,
                                  String cartridgeType, String cartridgeTypePrefix, String deploymentPolicy,
                                  String autoScalingPolicy) throws AppFactoryException {

        String stratosApplicationJson = getStratosApplicationJson(applicationId, repoUrl, repoUsername, repoPassword,
                                                                  cartridgeType, cartridgeTypePrefix, deploymentPolicy,
                                                                  autoScalingPolicy);

        ServerResponse response = MutualAuthHttpClient.sendPostRequest(stratosApplicationJson, this.stratosManagerURL
                                                                                               + this.APPLICATIONS_REST_END_POINT,
                                                                       username);
        if (response.getStatusCode() == HttpStatus.SC_CREATED) {
            if (log.isDebugEnabled()) {
                log.debug("Stratos application created for appId : " + applicationId);
            }
        } else {
            String errorMsg = "Error occured while creating stratos appliction for ID : " + applicationId
                              + "HTTP Status code : " + response.getStatusCode() + " server response : "
                              + response.getResponse();
            handleException(errorMsg);
        }
    }

    public void deployApplication(String applicationId) throws AppFactoryException {
        ServerResponse response = MutualAuthHttpClient.sendPostRequest("", stratosManagerURL
                                                                           + APPLICATIONS_REST_END_POINT
                                                                           + "/" + applicationId + "/deploy/"
                                                                           + SINGLE_TENANT_APPLICATION_POLICY_ID, username);
        if (response.getStatusCode() == HttpStatus.SC_ACCEPTED) {
            if (log.isDebugEnabled()) {
                log.debug(" Stratos application deployed for appId : " + applicationId);
            }
        } else {
            String errorMsg = "Error occured while deploying stratos appliction for ID : " + applicationId
                              + "HTTP Status code : " + response.getStatusCode() + " server response : "
                              + response.getResponse();
            handleException(errorMsg);
        }
    }

    public void undeployApplication(String applicationId) throws AppFactoryException {
        ServerResponse response = MutualAuthHttpClient.sendPostRequest("", this.stratosManagerURL
                                                                           + this.APPLICATIONS_REST_END_POINT + "/"
                                                                           + applicationId + "/undeploy/", username);
        if (response.getStatusCode() == HttpStatus.SC_ACCEPTED) {
            if (log.isDebugEnabled()) {
                log.debug("Stratos undeployment started successfully for appId : " + applicationId);
            }
        } else {
            String errorMsg = "Error occured while undeploying stratos appliction for ID : " + applicationId
                              + "HTTP Status code : " + response.getStatusCode() + " server response : "
                              + response.getResponse();
            handleException(errorMsg);
        }
    }

    public void deleteApplication(String applicationId) throws AppFactoryException {
        ServerResponse response = MutualAuthHttpClient.sendDeleteRequest(this.stratosManagerURL
                                                                         + this.APPLICATIONS_REST_END_POINT + "/"
                                                                         + applicationId, username);
        if (response.getStatusCode() == HttpStatus.SC_OK) {
            if (log.isDebugEnabled()) {
                log.debug("Stratos application deleted successfully for appId : " + applicationId);
            }
        } else {
            String errorMsg = "Error occured while deleting stratos appliction for ID : " + applicationId
                              + "HTTP Status code : " + response.getStatusCode() + " server response : "
                              + response.getResponse();
            handleException(errorMsg);
        }
    }

        public String getApplicationRuntime(String applicationId) throws AppFactoryException {
        ServerResponse response = MutualAuthHttpClient.sendGetRequest(this.stratosManagerURL
                                                                      + this.APPLICATIONS_REST_END_POINT + "/"
                                                                      + applicationId + "/runtime", username);
        if (response.getStatusCode() == HttpStatus.SC_OK) {
            String applicationInstanceJson = response.getResponse();
            if (log.isDebugEnabled()) {
                log.debug("Stratos application runtime json : " + applicationInstanceJson);
            }
            return applicationInstanceJson;
        } else {
            String errorMsg = "Error occured while getting application runtime for ID : " + applicationId
                              + "HTTP Status code : " + response.getStatusCode() + " server response : "
                              + response.getResponse();
            handleException(errorMsg);
        }
        return null;
    }

    public boolean isApplicationCreated(String applicationId) throws AppFactoryException {
        ServerResponse response = MutualAuthHttpClient.sendGetRequest(this.stratosManagerURL
                                                                      + this.APPLICATIONS_REST_END_POINT + "/"
                                                                      + applicationId, username);
        if (response.getStatusCode() == HttpStatus.SC_OK) {
            String applicationInfoJson = response.getResponse();
            if (log.isDebugEnabled()) {
                log.debug("Stratos application information : " + applicationInfoJson);
            }
            return true;
        } else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
            return false;
        } else {
            String errorMsg = "Error occured while getting while checking isApplicationCreated for ID : " + applicationId
                              + "HTTP Status code : " + response.getStatusCode() + " server response : "
                              + response.getResponse();
            log.error(errorMsg);
            throw new AppFactoryException(errorMsg);
        }
    }

    /**
     * Construct the stratos application JSON
     *
     * @param applicationId
     * @param repoUrl
     * @param repoUsername
     * @param repoPassword
     * @param cartridgeType
     * @param cartridgeTypePrefix
     * @param deploymentPolicy
     * @param autoScalingPolicy
     * @return
     */
    private String getStratosApplicationJson(String applicationId, String repoUrl, String repoUsername,
                                             String repoPassword, String cartridgeType, String cartridgeTypePrefix,
                                             String deploymentPolicy, String autoScalingPolicy) {

        JsonObject applicationJson = new JsonObject();
        applicationJson.addProperty("applicationId", applicationId);
        applicationJson.addProperty("alias", "single-tenant");

        JsonObject components = new JsonObject();

        JsonArray cartridges = new JsonArray();
        JsonObject cartridge = new JsonObject();

        cartridge.addProperty("type", cartridgeType);
        cartridge.addProperty("cartridgeMin", 1);
        cartridge.addProperty("cartridgeMax", 1);

        JsonObject subscribableInfo = new JsonObject();

        if (StringUtils.isNotEmpty(cartridgeTypePrefix)) {
            cartridgeTypePrefix = cartridgeTypePrefix.toLowerCase();
        } else {
            log.error("cartridge prefix cannot be null");
        }

        subscribableInfo.addProperty("alias", cartridgeTypePrefix);
        subscribableInfo.addProperty("deploymentPolicy", deploymentPolicy);
        subscribableInfo.addProperty("autoscalingPolicy", autoScalingPolicy);

        JsonObject artifactRepository = new JsonObject();
        artifactRepository.addProperty("privateRepo", true);
        artifactRepository.addProperty("repoUrl", repoUrl);
        artifactRepository.addProperty("repoUsername", repoUsername);
        artifactRepository.addProperty("repoPassword", repoPassword);

        subscribableInfo.add("artifactRepository", artifactRepository);

        cartridge.add("subscribableInfo", subscribableInfo);

        cartridges.add(cartridge);
        components.add("cartridges", cartridges);
        applicationJson.add("components", components);

        Gson gson = new Gson();

        return gson.toJson(applicationJson);
    }

    private void handleException(String errorMsg) throws AppFactoryException {
        log.error(errorMsg);
        throw new AppFactoryException(errorMsg);
    }
}