/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */
package org.wso2.carbon.appfactory.s4.integration;

public class DeployerInfo {

    private String alias;
    private String cartridgeType;
    private String repoURL;
    private String dataCartridgeType;
    private String dataCartridgeAlias;
    private String autoscalePolicy;
    private String deploymentPolicy;

    private String endpoint;


    private String baseURL;
    private String className;
    private Class<?> repoProvider;
    private String adminUserName;
    private String adminPassword;
    private String repoPattern;

    private String appType;

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getAdminUserName() {
        return adminUserName;
    }

    public void setAdminUserName(String adminUserName) {
        this.adminUserName = adminUserName;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public Class<?> getRepoProvider() {
        return repoProvider;
    }

    public void setRepoProvider(Class repoProvider) {
        this.repoProvider = repoProvider;
    }

    public String getRepoPattern() {
        return repoPattern;
    }

    public void setRepoPattern(String repoPattern) {
        this.repoPattern = repoPattern;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getCartridgeType() {
        return cartridgeType;
    }

    public void setCartridgeType(String cartridgeType) {
        this.cartridgeType = cartridgeType;
    }

    public String getRepoURL() {
        return repoURL;
    }

    public void setRepoURL(String repoURL) {
        this.repoURL = repoURL;
    }

    public String getDataCartridgeType() {
        return dataCartridgeType;
    }

    public void setDataCartridgeType(String dataCartridgeType) {
        this.dataCartridgeType = dataCartridgeType;
    }

    public String getDataCartridgeAlias() {
        return dataCartridgeAlias;
    }

    public void setDataCartridgeAlias(String dataCartridgeAlias) {
        this.dataCartridgeAlias = dataCartridgeAlias;
    }

    public String getAutoscalePolicy() {
        return autoscalePolicy;
    }

    public void setAutoscalePolicy(String autoscalePolicy) {
        this.autoscalePolicy = autoscalePolicy;
    }

    public String getDeploymentPolicy() {
        return deploymentPolicy;
    }

    public void setDeploymentPolicy(String deploymentPolicy) {
        this.deploymentPolicy = deploymentPolicy;
    }
}