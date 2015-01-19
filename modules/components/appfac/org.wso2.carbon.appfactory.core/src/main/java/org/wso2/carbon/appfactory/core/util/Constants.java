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

public class Constants {

//    Constants used by dependency management bundles
    public static final String DEPENDENCIES_HOME = "dependencies";
    public static final String MOUNT_POINT_PREFIX = "ApplicationDeployment.DeploymentStage.";
    public static final String DEPLOYMENT_STAGES = "ApplicationDeployment.DeploymentStage";

    public static final String MOUNT_POINT_SUFFIX = ".MountPoint";

    public  static final String TENANT_SPACE = "t";
    public  static final String JENKINS_WEBAPPS = "/webapps/jenkins";

    public static final String RECEIVER_MAIL_ADDRESS = "NotificationConfig.Email.ReceiverMailAddress";
    public static final String EMAIL_ADMIN_NAME = "NotificationConfig.Email.AdminName";

    public enum ApplicationCreationStatus {
        PENDING,COMPLETED,FAULTY, NONE
    }

}
