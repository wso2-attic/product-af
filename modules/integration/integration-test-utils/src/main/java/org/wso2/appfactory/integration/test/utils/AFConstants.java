/*
 * Copyright 2015 WSO2, Inc. (http://wso2.com)
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.appfactory.integration.test.utils;

/**
 * App Factory specific property paths
 */
public class AFConstants {

	// Product groups
	public static final String AF_PRODUCT_GROUP = "AF";

	// Properties from automation.xml
	public static final String URLS_APPFACTORY = "//appFactoryProperties/urls/appFactory";
	public static final String URLS_PROD_SC = "//appFactoryProperties/urls/prodStratosController";
	public static final String URLS_BPS = "//appFactoryProperties/urls/bps";
	public static final String URLS_GIT = "//appFactoryProperties/urls/git";
	public static final String URLS_BAM = "//appFactoryProperties/urls/bam";
    public static final String URLS_API = "//appFactoryProperties/urls/api";

	public static final String ENV_CREATE_RANDOM_TENANT = "af.test.createRandomTenant";
	public static final String ENV_CREATED_RANDOM_TENANT_DOMAIN = "af.test.tenant.domain";


	public static final String DEFAULT_TENANT_FIRST_NAME =
	                                                       "//appFactoryProperties/defaultTenant/firstName";
	public static final String DEFAULT_TENANT_LAST_NAME =
	                                                      "//appFactoryProperties/defaultTenant/lastName";
	public static final String DEFAULT_TENANT_EMAIL = "//appFactoryProperties/defaultTenant/email";
	public static final String DEFAULT_TENANT_ADMIIN = "//appFactoryProperties/defaultTenant/admin";
	public static final String DEFAULT_TENANT_ADMIN_PASSWORD =
	                                                           "//appFactoryProperties/defaultTenant/adminPassword";
	public static final String DEFAULT_TENANT_USAGE_PLAN =
	                                                       "//appFactoryProperties/defaultTenant/usagePlan";

    //this is package private by design. do not change without design review
	static final String DEFAULT_TENANT_TENANT_DOMAIN = "//appFactoryProperties/defaultTenant/tenantDomain";

	public static final String DEFAULT_APP_APP_NAME =
	                                                  "//appFactoryProperties/defaultApplication/applicationName";
	public static final String DEFAULT_APP_APP_KEY =
	                                                 "//appFactoryProperties/defaultApplication/applicationKey";
	public static final String DEFAULT_APP_APP_TYPE =
	                                                  "//appFactoryProperties/defaultApplication/applicationType";
	public static final String DEFAULT_APP_APP_DESC =
	                                                  "//appFactoryProperties/defaultApplication/applicationDescription";
	public static final String DEFAULT_APP_REPO_TYPE =
	                                                   "//appFactoryProperties/defaultApplication/repositoryType";

    public static final String DEFAULT_APP_VERSION_ONE_SRC = "//appFactoryProperties/defaultApplication/versions/" +
                                                             "version/v1/sourceVersion";

    public static final String DEFAULT_APP_VERSION_ONE_TARGET = "//appFactoryProperties/defaultApplication/versions/" +
                                                                "version/v1/targetVersion";

    public static final String DEFAULT_APP_VERSION_TWO_SRC = "//appFactoryProperties/defaultApplication/versions/" +
                                                                "version/v2/sourceVersion";

    public static final String DEFAULT_APP_VERSION_TWO_TARGET = "appFactoryProperties/defaultApplication/versions/" +
                                                                "version/v2/targetVersion";

    public static final String DEFAULT_APP_VERSION_THREE_SRC = "//appFactoryProperties/defaultApplication/versions/" +
                                                               "version/v3/sourceVersion";

    public static final String DEFAULT_APP_VERSION_THREE_TARGET = "appFactoryProperties/defaultApplication/versions/" +
                                                                  "version/v3/targetVersion";

	// Security policy files in resources/security folder
	public static final String SECURITY_POLICIES_SCENARIO1_POLICY_XML =
	                                                                    "security/policies/scenario1-policy.xml";

	// Client modules in resources/client
	public static final String CLIENT_MODULES_STRING = "client";

	// Keystores in resources/keystores
	public static final String KEYSTORES_PRODUCT_CLIENT_TRUSTSTORE =
	                                                                 "keystores/products/client-truststore.jks";
	// Domain mapping
	public static final String DOMAIN_MAPPING_DEFAULT_HOST =  "//appFactoryProperties/domainMapping/domainName";

    public static final String DEFAULT_API_ADD_API_PAYLOAD="//appFactoryProperties/defaultAPI/addPayload";

    public static final String DEFAULT_API_PUBLISH_API_PAYLOAD="//appFactoryProperties/defaultAPI/publishPayload";

    public static final String DEFAULT_API_SUBSCRIBE_API_PAYLOAD="//appFactoryProperties/defaultAPI/subscribePayload";

    public static final String DEFAULT_API_USER_NAME="//appFactoryProperties/defaultAPI/useNAme";

    public static final String DEFAULT_API_PASSWORD="//appFactoryProperties/defaultAPI/password";

    public static final String TRUE = "true";
    public static final String FALSE= "false";
}
