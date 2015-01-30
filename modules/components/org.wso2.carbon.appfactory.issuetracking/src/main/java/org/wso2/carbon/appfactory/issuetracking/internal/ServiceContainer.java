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
package org.wso2.carbon.appfactory.issuetracking.internal;


import org.wso2.carbon.appfactory.application.mgt.service.ApplicationManagementService;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;

public class ServiceContainer {

    private static AppFactoryConfiguration appFactoryConfiguration;
    private static ApplicationManagementService applicationManagementService;

    public static AppFactoryConfiguration getAppFactoryConfiguration() {
        return appFactoryConfiguration;
    }

    public static void setAppFactoryConfiguration(AppFactoryConfiguration appFactoryConfiguration) {
        ServiceContainer.appFactoryConfiguration = appFactoryConfiguration;
    }

    public static void setApplicationManagementService(
            ApplicationManagementService applicationManagementService) {
        ServiceContainer.applicationManagementService = applicationManagementService;
    }

    public static ApplicationManagementService getApplicationManagementService() {
        return applicationManagementService;
    }
}