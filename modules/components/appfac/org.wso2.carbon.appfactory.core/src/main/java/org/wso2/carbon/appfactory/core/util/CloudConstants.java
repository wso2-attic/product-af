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
package org.wso2.carbon.appfactory.core.util;

/**
 * Cloud related constants
 */
public enum CloudConstants {

    ENVIRONMENT("ApplicationDeployment.DeploymentStage"),SERVER_URL("serverURL"),TENANT_USAGE_PLAN("usagePlan"),
    TENANT_DOMAIN("tenantDomain"),TENANT_ID("tenantID"),SUCCESS_KEY("successKey"),ADMIN_USERNAME("adminUsername"),
    ADMIN_PASSWORD("adminPassword"),ADMIN_EMAIL("email"),ADMIN_FIRST_NAME("firstName"),ADMIN_LAST_NAME("lastName"),
    ORIGINATED_SERVICE("originatedService"),SUPER_TENANT_ADMIN("superAdmin"),
    SUPER_TENANT_ADMIN_PASSWORD("superAdminPassword"),STAGE("stage"),RUNTIMES("runtimes");

    String value;

    CloudConstants(String value){
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
