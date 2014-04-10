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

package org.wso2.carbon.appfactory.common;

import java.io.File;

/**
 * Constants for AppFactory configuration
 */
public class AppFactoryConstants {
	public static final String CONFIG_FOLDER = "appfactory";
	public static final String CONFIG_FILE_NAME = "appfactory.xml";
	public static final String CONFIG_NAMESPACE = "http://www.wso2.org/appfactory/";

	public static final String SERVER_ADMIN_NAME = "AdminUserName";
	public static final String SERVER_ADMIN_PASSWORD = "AdminPassword";
	public static final String SERVER_ADMIN_EMAIL = "AdminEmail";
	public static final String DEPLOYMENT_STAGES = "ApplicationDeployment.DeploymentStage";
	public static final String DEPLOYMENT_URL = "DeploymentServerURL";
	public static final String APPFACTORY_WEB_CONTEXT_ROOT = "WebContextRoot";

	public static final String APPFACTORY_SERVER_URL = "ServerUrls.AppFactory";
	public static final String BPS_SERVER_URL = "ServerUrls.BPS";
	public static final String GREG_SERVER_URL = "ServerUrls.Greg";
	public static final String APP_OWNER_ROLE = "appowner";
	public static final String APP_ROLE_PREFIX = "app_";
	public static final String APP_FACTORY_USERS_ROLE = "appfactoryusers";

	public static final String REGISTRY_GET = "REGISTRY_GET";
	public static final String REGISTRY_PUT = "REGISTRY_PUT";
	public static final String REGISTRY_DELETE = "REGISTRY_DELETE";
    
    public static final String DEFAULT_ACTION = "ui.execute";
    public static final String PERMISSION_VIBILITY = "/permission/admin/appfactory/visibility/";
    public static final String PERMISSION_DEPLOY_TO = "/permission/admin/appfactory/deployTo/";
    public static final String PERMISSION_RESOURCE_UPDATE_IN = "/permission/admin/appfactory/resources/update/";
    public static final String PERMISSION_RESOURCE_CREATE = "/permission/admin/appfactory/resources/create/";

	/**
	 * The server URL of API Manager instance.
	 */
	public static final String API_MANAGER_SERVICE_ENDPOINT = "ApiManager.Server.Url";
	public static final String API_MANAGER_SERVER_URL = "ServerUrls.ApiManager";

	public static final String SCM_ADMIN_NAME =
	                                            "RepositoryProviderConfig{@type}.Property.AdminUserName";
	public static final String SCM_ADMIN_PASSWORD =
	                                                "RepositoryProviderConfig.{@type}.Property.AdminPassword";
	public static final String SCM_SERVER_URL = "RepositoryProviderConfig.{@type}.Property.BaseURL";
	public static final String SCM_READ_WRITE_ROLE =
	                                                 "RepositoryProviderConfig.{@type}.Property.ReadWriteRole";
	public static final String REPOSITORY_PROVIDER_CONFIG = "RepositoryProviderConfig";
	public static final String APPLICATION_TYPE_CONFIG = "ApplicationType";

	public static final String DEFAULT_APPLICATION_USER_ROLE = "DefaultUserRole";
	public static final String PERMISSION = "Permission";
	public static final String PER_APP_ROLE_PERMISSION =
	                                                     "/permission/admin/appfactory/belongs/toapplication";
	public static final String REGISTRY_GOVERNANCE_PATH = "/_system/governance";
	public static final String REGISTRY_APPLICATION_PATH = "/repository/applications";
	public static final String APPLICATION_ARTIFACT_NAME = "appinfo";

	public static final String APPLICATION_ID = "applicationId";
	public static final String APPLICATION_REVISION = "revision";
	public static final String APPLICATION_VERSION = "version";
	// public static final String APPLICATION_STAGE = "stage";
	public static final String APPLICATION_BUILD = "build";

	public static final String TRUNK = "trunk";
	public static final String BRANCH = "branches";
	public static final String TAG = "tags";

	public static final String FILE_TYPE_CAR = "car";
	public static final String FILE_TYPE_JAXWS = "jaxws";
	public static final String FILE_TYPE_JAXRS = "jaxrs";
	public static final String FILE_TYPE_WAR = "war";
	public static final String FILE_TYPE_JAGGERY = "jaggery";
	public static final String FILE_TYPE_DBS = "dbs";
	public static final String FILE_TYPE_BPEL = "bpel";
	public static final String FILE_TYPE_PHP = "php";
	public static final String FILE_TYPE_ESB = "esb";

	public static final String SCM_READ_WRITE_PERMISSION =
	                                                       "RepositoryProviderConfig.%s.Property.ReadWritePermission";
    public static final String REPO_ACCESSABILITY = "EnablePerDeveloperRepos";

	/**
	 * Defines the property name for maven archetype generation parameters
	 */
	public static final String CAPP_MAVEN_ARCHETYPE_PROP_NAME = "MavenArchetype.CApp.Properties";
	public static final String WEBAPP_MAVEN_ARCHETYPE_PROP_NAME =
	                                                              "MavenArchetype.WebApp.Properties";
	public static final String JAX_WEBAPP_MAVEN_ARCHETYPE_PROP_NAME =
	                                                                  "MavenArchetype.JAXWS.Properties";
	public static final String JAX_RS_WEBAPP_MAVEN_ARCHETYPE_PROP_NAME =
	                                                                     "MavenArchetype.JAXRS.Properties";
	public static final String JAGGERY_APP_MAVEN_ARCHETYPE_PROP_NAME =
	                                                                   "MavenArchetype.Jaggery.Properties";
	public static final String DBS_APP_MAVEN_ARCHETYPE_PROP_NAME = "MavenArchetype.DBS.Properties";
	public static final String ESB_APP_MAVEN_ARCHETYPE_PROP_NAME = "MavenArchetype.ESB.Properties";
	public static final String BPEL_MAVEN_ARCHETYPE_PROP_NAME = "MavenArchetype.BPEL.Properties";
	public static final String PHP_MAVEN_ARCHETYPE_PROP_NAME = "MavenArchetype.php.Properties";
	public static final String PREFERRED_REPOSITORY_TYPE = "RepositoryType";

	public static final String SECURE_VAULT_NS = "http://org.wso2.securevault/configuration";
	public static final String SECRET_ALIAS_ATTR_NAME = "secretAlias";
	public static final String REGISTRATION_LINK = "RegistrationLink";

	/**
	 * External system names
	 */
	public static final String REDMINE = "redmine";
	public static final String JENKINS = "jenkins";
	public static final String DENY = "deny:";
	public static final String TENANT_ROLES_DEFAULT_USER_ROLE = "TenantRoles.DefaultUserRole";
	public static final String TENANT_ROLES_ROLE = "TenantRoles.Role";
	public static final String CLOUD_STAGE = "stratos.stage";

	public static String[] JENKINS_MVN_PROJECT_TYPE = { FILE_TYPE_WAR, FILE_TYPE_CAR,
	                                                   FILE_TYPE_JAXRS, FILE_TYPE_JAXWS,
	                                                   FILE_TYPE_JAGGERY, FILE_TYPE_BPEL };
    public static final String CONSUME = "consume";
    public static final String INVOKE_PERMISSION = "/permission/admin/appfactory/realm";

	public static String[] JENKINS_FREESTYLE_PROJECT_TYPE = { FILE_TYPE_DBS, FILE_TYPE_PHP,
	                                                         FILE_TYPE_ESB };

	// constants added for Deployers
	public static final String APPLICATION_TYPE_WAR = "war";
	public static final String APPLICATION_TYPE_CAR = "car";
	public static final String APPLICATION_TYPE_ZIP = "zip";
	public static final String APPLICATION_TYPE_JAXWS = "jaxws";
	public static final String APPLICATION_TYPE_JAXRS = "jaxrs";
	public static final String APPLICATION_TYPE_JAGGERY = "jaggery";
	public static final String APPLICATION_TYPE_DBS = "dbs";
	public static final String APPLICATION_TYPE_PHP = "php";
	public static final String APPLICATION_TYPE_ESB = "esb";
	public static final String APPLICATION_TYPE_XML = "xml";
	public static final String APPLICATION_TYPE_BPEL = "bpel";
	public static final String APPLICATION_TYPE_UPLOADED_WAR = "Uploaded-binary-App-war";
	public static final String APPLICATION_TYPE_UPLOADED_JAGGERY = "Uploaded-App-Jaggery";
	

	public static final String TENANT_DOMAIN = "tenantdomain";
	public static final String APP_ID = "applicationId";
	public static final String USER_NAME = "username";

	public static final String APP_VERSION = "applicationVersion";
	public static final String JOB_NAME = "jobName";
	public static final String TAG_NAME = "tagName";
	public static final String DEPLOY_STAGE = "deployStage";
	public static final String ARTIFACT_TYPE = "artifactType";
	public static final String APP_TYPE = "ApplicationType";
	public static final String DEPLOYMENT_SERVER_URLS = "DeploymentServerURL";
	public static final String DEPLOY_ACTION = "deployAction";
	public static final String DEPLOY_ACTION_LABEL_ARTIFACT = "labelArtifactAsPromoted";

	public static final String ESB_ARTIFACT_PREFIX = "synapse-config";
	public static final String ESB_ARTIFACT_DEPLOYMENT_PATH = "synapse-configs" + File.separator +
	                                                          "default";

	public static final String APP_VERSION_CACHE = "af.appversion.cache";
	public static final String APP_VERSION_CACHE_MANAGER = "af.appversion.cache.manager";
	public static final String APP_VERSION_STAGE_CACHE = "af.appversion.stage.cache";
	public static final String APP_VERSION_STAGE_CACHE_MANAGER =
	                                                             "af.appversion.stage.cache.manager";

	// constants added for BAM Stats
	public static final String BAM_ADD_DATA = "ADD";
	public static final String BAM_UPDATE_DATA = "UPDATE";
	public static final String BAM_DELETE_DATA = "DELETE";
	public static final String BAM_BUILD_SUCCESS = "SUCCESS";
	public static final String BAM_BUILD_FAIL = "FAIL";
	public static final String BAM_BUILD_START = "START";

	// User mgt related
	public static final String FIRST_LOGGIN_MAPPED_TO = "Initials";
	public static final String CLAIMS_FIRSTLOGIN = "http://wso2.org/claims/firstlogin";

	// Registry permission in clouds
	public static final String CLOUD_RESOURCE_PERMISSION =
	                                                       "CloudResourcePermissions.Resources.Resource";
	public static final String GOVERNANCE_REGISTRY = "GovernanceRegistry";
	public static final String ROLES = "Roles";
	public static final String STAGES = "Stages";
	public static final String FULLSTOP = ".";

	public static final String STORAGE_TYPE = "storagetype";
	public static final String BUILDABLE_STORAGE_TYPE = "buildable";
	public static final String NONBUILDABLE_STORAGE_TYPE = "nonbuildable";

	// STS related constants
	public static final String SAML_TOKEN_TYPE = "2.0";
	public static final String SUBJECT_CONFIRMATION_METHOD = "STS.STSSubjectConfirmationMethod";
	public static final String STS_EPR_SERVICES_LOCATION = "STS.Epr.serviceslocation";
	public static final String STS_EPR_SERVICE_NAME = "STS.Epr.servicename";
	public static final String STS_EPR_TENANT_TEMPLATE = "STS.Epr.tenantlocationtemplate";
	public static final String STS_ALLOWED_GROUPS = "STS.STSAllowedUserGroups";
	public static final String STS_SCENARIO_ID = "STS.ScenarioID";
	public static final String STS_EPR_TENANT_TEMPLA_TENANT_DOMAIN_VALUE = "{tenantdomain}";
	public static final String CLAIM_DIALECT = "STS.Cliams";
	public static final String CLAIM_URIS = "STS.ClaimUris";
	public static final String SUBJECT_CONFIRMATION_BEARER = "b";
	public static final String SUBJECT_CONFIRMATION_HOLDER_OF_KEY = "h";
	public static final String SAML_TOKEN_TYPE_10 = "1.0";
	public static final String SAML_TOKEN_TYPE_11 = "1.1";
	public static final String SAML_TOKEN_TYPE_20 = "2.0";
	public static final String CLIAM_NAMESPACE = "STS.ClaimNamespace";
	public static final String CLIAM_TYPE_NAME = "ClaimType";
	public static final String CLIAM_TYPE_VALUE = "wsid";
	public static final String CRYPTO_PROVIDER = "org.wso2.carbon.security.util.ServerCrypto";
	public static final String STS_POLICY_FILE = "STS.PolicyFile";

	// SSO constants
	public static final String SSO_NAME = "SSORelyingParty.Name";
	public static final String SSO_ASSERTION_CONSUMER_URL =
	                                                        "SSORelyingParty.AssertionConsumerService";
	public static final String SSO_IDENTITY_PROVIDER_URL = "SSORelyingParty.IdentityProviderURL";

	public static final String  UPLOADED_APPLICATION_TMP_FOLDER_NAME = "tmpUploadedApps";
				
	/**
	 * Enum to represent of different application stages.
	 */
	public enum ApplicationStage {
		DEVELOPMENT("development"), TEST("test"), STAGING("staging"), PRODUCTION("production"),
		RETIRED("retired");

		String stage = null;

		ApplicationStage(String strValue) {
			stage = strValue;
		}

		public String getStageStrValue() {
			return stage;
		}

	}
}
