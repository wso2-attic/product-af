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

package org.wso2.carbon.appfactory.apiManager.integration.utils;

public class Constants {
    public static final String STORE_LOGIN_ENDPOINT =
            "store/site/blocks/user/login/ajax/login.jag";
    public static final String CREATE_APPLICATION_ENDPOINT =
            "store/site/blocks/application/application-add/ajax/application-add.jag";
    public static final String DELETE_APPLICATION_ENDPOINT =
            "store/site/blocks/application/application-remove/ajax/application-remove.jag";
    public static final String LIST_APPLICATION_ENDPOINT =
            "store/site/blocks/application/application-list/ajax/application-list.jag";
    public static final String LIST_SUBSCRIPTIONS_ENDPOINT =
            "store/site/blocks/subscription/subscription-list/ajax/subscription-list.jag";
    public static final String ADD_SUBSCRIPTIONS_ENDPOINT =
            "store/site/blocks/subscription/subscription-add/ajax/subscription-add.jag";

    public static final String PUBLISHER_LOGIN_ENDPOINT = "publisher/site/blocks/user/login/ajax/login.jag";
    public static final String PUBLISHER_API_INFO_ENDPOINT = "publisher/site/blocks/listing/ajax/item-list.jag";

    public static final String SAML_TOKEN = "samltoken";

    public static final String NAME = "name";
    public static final String VERSION = "version";
    public static final String PROVIDER = "provider";
    public static final String PROD_KEY = "prodKey";
    public static final String PROD_CONSUMER_KEY = "prodConsumerKey";
    public static final String PROD_CONSUMER_SECRET = "prodConsumerSecret";
    public static final String SANDBOX_KEY = "sandboxKey";
    public static final String SANDBOX_CONSUMER_KEY = "sandboxConsumerKey";
    public static final String SANDBOX_CONSUMER_SECRET = "sandboxConsumerSecret";
    public static final String ACTION = "action";
    public static final String SUBSCRIPTIONS = "subscriptions";
    public static final String USERNAME = "username";
    public static final String DESCRIPTION = "description";
    public static final String WSDL = "wsdl";
    public static final String WADL = "wadl";
    public static final String CONTEXT = "context";
    public static final String SANDBOX = "sandbox";
    public static final String ENDPOINT = "endpoint";
    public static final String PROD = "prod";
    public static final String DEVELOPMENT = "Development";
    public static final String TESTING = "Testing";
    public static final String PRODUCTION = "Production";
    
    public static final String API_PROVIDER = "apiProvider";
    public static final String API_VERSION = "apiVersion";
    public static final String API_NAME = "apiName";

//    Commented out since we don't hard-code the states
//    public static final String DEV_MOUNT = "ApplicationDeployment.DeploymentStage.Development.MountPoint";
//    public static final String TEST_MOUNT = "ApplicationDeployment.DeploymentStage.Testing.MountPoint";
//    public static final String PROD_MOUNT = "ApplicationDeployment.DeploymentStage.Production.MountPoint";

    public static final String MOUNT_PREFIX = "ApplicationDeployment.DeploymentStage.";
    public static final String MOUNT_SUFFIX = ".MountPoint";

    public static final String SANDBOX_KEYS_PROPERTY = "ApiManager.Keys.Property.Sandbox";
    public static final String PRODUCTION_KEYS_PROPERTY = "ApiManager.Keys.Property.Prod";

    public static final String API_MANAGER_DEFAULT_TIER = "ApiManager.DefaultTier";
    public static final String API_MANAGER_DEFAULT_CALLBACK_URL = "ApiManager.DefaultCallbackURL";
    public static final String API_MANAGER_DEFAULT_AUTHORIZED_DOMAINS_ALL = "ALL";
    public static final String DEPLOYER_APPTYPE_EP = "Deployer.ApplicationType.*.Endpoint";
}
