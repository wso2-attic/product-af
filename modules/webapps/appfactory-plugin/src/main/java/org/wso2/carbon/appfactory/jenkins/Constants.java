/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *    WSO2 Inc. licenses this file to you under the Apache License,
 *    Version 2.0 (the "License"); you may not use this file except
 *    in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package org.wso2.carbon.appfactory.jenkins;

public class Constants {
    public static final String APPLICATION_ID = "applicationId";
    public static final String JOB_NAME = "jobName";
    public static final String TAG_NAME = "tagName";
    public static final String DEPLOY_STAGE = "deployStage";
    public static final String ARTIFACT_TYPE = "artifactType";
    public static final String DEPLOY_ACTION = "deployAction";
    
    public static final String JENKINS_ADMIN_USERNAME_PATH = "JenkinsServerAdminUsername";
    public static final String JENKINS_ADMIN_PASSWORD_PATH = "JenkinsServerAdminPassword";
    
    public static final String JENKINS_HOME = "JENKINS_HOME";
    public static final String JOB_CONFIG_XPATH = "/*/publishers/org.wso2.carbon.appfactory.jenkins.AppfactoryPluginManager/applicationArtifactExtension";

    public static final String PLACEHOLDER_JEN_HOME = "$JENKINS_HOME";
    public static final String PLACEHOLDER_TENANT_IDENTIFIER = "$TENANT_IDENTIFIER";
}
