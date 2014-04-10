/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
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
package org.wso2.carbon.cloudmgt.common;

/**
 * Constants for Cloud Management configuration
 */
public class CloudConstants {
	public static final String TENANT_ROLES_DEFAULT_USER_ROLE = "TenantRoles.DefaultUserRole";
	public static final String APP_ROLE_PREFIX = "app_";
	public static final String CONFIG_FOLDER = "appfactory";
	public static final String CONFIG_FILE_NAME = "appfactory.xml";
	public static final String CONFIG_NAMESPACE = "http://www.wso2.org/appfactory/";
	
	public static final String SERVER_ADMIN_NAME = "AdminUserName";
	public static final String SERVER_ADMIN_PASSWORD = "AdminPassword";

	public static final String SECURE_VAULT_NS = "http://org.wso2.securevault/configuration";
	public static final String SECRET_ALIAS_ATTR_NAME = "secretAlias";
	public static final String REGISTRATION_LINK = "RegistrationLink";
	
	public static final String ARTIFACT_TYPE = "artifactType";
	public static final String APP_TYPE = "ApplicationType";
	
	public static final String DENY = "deny:";
	public static final String TENANT_ROLES_ROLE = "TenantRoles.Role";
	public static final String CLOUD_STAGE = "stratos.stage";
	
	public static final String REGISTRY_GET = "REGISTRY_GET";
	public static final String REGISTRY_PUT = "REGISTRY_PUT";
	public static final String REGISTRY_DELETE = "REGISTRY_DELETE";
}

