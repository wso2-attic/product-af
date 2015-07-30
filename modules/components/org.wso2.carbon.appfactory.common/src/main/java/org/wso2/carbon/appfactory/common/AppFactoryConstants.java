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

import org.apache.commons.lang3.text.WordUtils;

import java.io.File;

/**
 * Constants for AppFactory configuration
 */
public class AppFactoryConstants {
	public static final String CONFIG_FOLDER = "appfactory";
	public static final String CONFIG_FILE_NAME = "appfactory.xml";
	public static final String CONFIG_APPLICATION_ROLLBACK_NOTICE_EMAIL = "application-rollback-notice-email.xml";
	public static final String CONFIG_NAMESPACE = "http://www.wso2.org/appfactory/";

	public static final String SERVER_ADMIN_NAME = "AdminUserName";
	public static final String SERVER_ADMIN_PASSWORD = "AdminPassword";
	public static final String SERVER_ADMIN_EMAIL = "AdminEmail";
	public static final String DEPLOYMENT_STAGES = "ApplicationDeployment.DeploymentStage";
	public static final String DEPLOYMENT_URL = "DeploymentServerURL";
	public static final String APPFACTORY_WEB_CONTEXT_ROOT = "WebContextRoot";
	public static final String DOMAIN_NAME = "DomainName";
	public static final String FINE_GRAINED_DOMAIN_MAPPING_ALLOWED_STAGE = "FineGrainedDomainMappingAllowedStage";
	public static final String AUTOMATIC_DEPLOYMENT_POLLING_PERIOD = "AutomaticDeployment.PollingPeriod";

	public static final String APPFACTORY_SERVER_URL = "ServerUrls.AppFactory";
	public static final String BPS_SERVER_URL = "ServerUrls.BPS";
	public static final String GREG_SERVER_URL = "ServerUrls.Greg";
	public static final String APP_OWNER_ROLE = "appowner";
	public static final String APP_ROLE_PREFIX = "app_";
	public static final String APP_FACTORY_USERS_ROLE = "appfactoryusers";
	public static final String UPPLOADABLE_SUFFIX = ".uploaded";

	public static final String MESSAGE_BROKER_CONNECTION_URL = "MessageBrokerConnectionURL";
    public static final String UPLOADABLE_ARTIFACT_MAX_SIZE="UploadableArtifactMaxSize";

	public static final String REGISTRY_GET = "REGISTRY_GET";
	public static final String REGISTRY_PUT = "REGISTRY_PUT";
	public static final String REGISTRY_DELETE = "REGISTRY_DELETE";

	public static final String DEFAULT_ACTION = "ui.execute";
	public static final String PERMISSION_VIBILITY = "/permission/admin/appfactory/visibility/";
	public static final String PERMISSION_DEPLOY_TO = "/permission/admin/appfactory/deployTo/";
	public static final String PERMISSION_RESOURCE_UPDATE_IN = "/permission/admin/appfactory/resources/update/";
	public static final String PERMISSION_RESOURCE_CREATE = "/permission/admin/appfactory/resources/create/";
	public static final String PROPERTY_ARTIFACT_VERSION_NAME = "TrunkVersioning.ServiceVersioning.ArtifactVersionName";

	public static final String RUNTIMES="Runtimes";
	public static final String RUNTIME="Runtime";
	public static final String RUNTIME_REPOSITORY_URL_PATTERN="PAASArtifactStorageURLPattern";
	public static final String RUNTIME_ALIAS_PREFIX="AliasPrefix";
	public static final String RUNTIME_CARTRIDGE_TYPE_PREFIX="CartridgeTypePrefix";
	public static final String RUNTIME_DEPLOYMENT_POLICY="DeploymentPolicy";
	public static final String RUNTIME_AUTOSCALE_POLICY="AutoscalePolicy";
	public static final String RUNTIME_REPO_URL="RepoURL";
	public static final String RUNTIME_DATA_CARTRIDGE_TYPE="DataCartridgeType";
	public static final String RUNTIME_DATA_CARTRIDGE_ALIAS="DataCartridgeAlias";
	public static final String RUNTIME_SUBSCRIBE_ON_DEPLOYMENT="SubscribeOnDeployment";
	public static final String RUNTIME_REPO_PROVIDER_URL="repoProviderUrl";
	public static final String RUNTIME_NAME_FOR_APPTYPE ="runtimesNameForApptype";

	public static final String ROLE_NAME = "name";
	public static final String ROLE_PATTERN = "pattern";
	public static final String ROLE_PERMISSION = "permission";
	public static final String ASSIGN_USER_ID = "sid";
	public static final String PROJECT_ROLE = "projectRole";
	public static final String GLOBAL_ROLE = "globalRole";
	public static final String WRAPPER_TAG_KEY = "wrapper";
	public static final String XPATH_EXPRESSION_KEY = "xpath";
	public static final String IS_AUTOMATIC = "isAutomatic";
	public static final String STRING_FALSE = "false";
	public static final String STRING_TRUE = "true";
	public static final String DO_DEPLOY = "doDeploy";
	public static final String BUILD_REPO_FROM = "repoFrom";
	public static final String TENANT_USER_NAME = "tenantUserName";
	public static final String PERSIST_ARTIFACT = "persistArtifact";
	public static final String MASTER_REPO = "master repo";
	public static final String FORKED_REPO = "forked repo";
	public static final String BUILD_STATUS_UNKNOWN = "Unknown";
	public static final String BUILD_STATUS_BUILDING = "Building";
	public static final String ALL_JOB_NAME = "all";
	public static final String JSON_TREE_STRUCTURE = "tree";
	public static final String STRING_URL = "url";
	public static final String SVN_KIND_KEY = "kind";
	public static final String PASSWORD_STRING = "password";
	public static final String SVN_USERNAME = "username1";
	public static final String SVN_PASSWORD = "password1";
	public static final String LOCATION_HEADER_PARAM = "Location";
	public static final String JOB_NAME_KEY = "name";

	/*
	*  constants for cartridge subscription info
	* */
	public static final String APPEND_STAGE_TO_CARTRIDGE_INFO = "AppendStageToCartridgeInfo";

    /**
     * Defining archetype related constants
     */
    public static final String GOAL_MAVEN_ARCHETYPE_GENERATE = "archetype:generate";
    public static final String MAVEN_ARCHETYPE_DIR = "archetypeDir";
    public static final String DEFAULT_POM_FILE = "pom.xml";
    public static final String DEFAULT_TARGET_FOLDER = "target";
    public static final String DEFAULT_SYNAPSE_NAMESPACE = "http://ws.apache.org/ns/synapse";
    public static final String DEFAULT_SYNAPSE_NAMESPACE_PREFIX = "xmlns";
    public static final String ARTIFACT_NAME = "${artifactId}-${version}";
	public static final String MAVEN_ARTIFACT_ID_REPLACEMENT = " -DartifactId=";
	public static final String MAVEN_ARTIFACT_ID = "-DartifactId=";
	public static final String XML_EXTENSION = "xml";
	public static final String CAR_ARTIFACT_CONFIGURATION = "artifact.xml";
	public static final String CAR_ARTIFACT_CONFIGURATION_ARTIFACT = "artifact";
	public static final String CAR_ARTIFACT_CONFIGURATION_QNAME_VERSION = "version";
	public static final String CAR_ARTIFACT_CONFIGURATION_TYPE_SYNAPSE = "synapse/";
	public static final String CAR_ARTIFACT_CONFIGURATION_QNAME_TYPE = "type";
	public static final String CAR_ARTIFACT_CONFIGURATION_QNAME_NAME = "name";
	public static final String CAR_ARTIFACT_SYNAPSE_CONFIG_STORE_LOCATION = "synapse-config";
	public static final String FILENAME_EXTENSION_SEPERATOR = ".";


	/**
	 * Defining repository related constants
	 */
	public static final String REPOSITORY_TYPE_GIT = "git";
	public static final String REPOSITORY_BRANCH_MASTER = "master";
	public static final String REPOSITORY_BRANCH = "branch";

	/**
	 * Defining Jenkins job configuration related constants
	 */
	public static final String JENKINS_JOB_CONFIG = "jenkins-config.xml";
	public static final String REPOSITORY_TYPE = "repository.type";
	public static final String REPOSITORY_URL = "repository.url";
	public static final String SVN_REPOSITORY_XPATH_SELECTOR =
			"/*/scm/locations/hudson.scm.SubversionSCM_-ModuleLocation/remote";
	public static final String GIT_REPOSITORY_XPATH_SELECTOR =
			"/*/scm/userRemoteConfigs/hudson.plugins.git.UserRemoteConfig/url";
	public static final String GIT_REPOSITORY_VERSION_XPATH_SELECTOR =
			"/*/scm/branches/hudson.plugins.git.BranchSpec/name";

	public static final String MAVEN3_CONFIG_NAME = "maven3.config.name";
	public static final String MAVEN3_CONFIG_NAME_XAPTH_SELECTOR = "mavenName";
	public static final String ARTIFACT_ARCHIVER_CONFIG_NAME_XAPTH_SELECTOR =
			"/*/publishers/hudson.tasks.ArtifactArchiver/artifacts";

	public static final String REPOSITORY_FROM = "repositoryFrom";
	public static final String APPTYPE_EXTENSION = "application.extension";

	private static final String PUBLISHERS_APPFACTORY_PLUGIN_XPATH_BASE =
			"/*/publishers/org.wso2.carbon.appfactory.jenkins.AppfactoryPluginManager/";
	public static final String PUBLISHERS_APPFACTORY_POST_BUILD_APP_ID_XPATH_SELECTOR =
			PUBLISHERS_APPFACTORY_PLUGIN_XPATH_BASE + "applicationId";
	public static final String PUBLISHERS_APPFACTORY_POST_BUILD_APP_VERSION_XPATH_SELECTOR =
			PUBLISHERS_APPFACTORY_PLUGIN_XPATH_BASE + "/applicationVersion";
	public static final String PUBLISHERS_APPFACTORY_POST_BUILD_APP_EXTENSION_XPATH_SELECTOR =
			PUBLISHERS_APPFACTORY_PLUGIN_XPATH_BASE + "/applicationArtifactExtension";
	public static final String PUBLISHERS_APPFACTORY_POST_BUILD_USERNAME_XPATH_SELECTOR =
			PUBLISHERS_APPFACTORY_PLUGIN_XPATH_BASE + "userName";
	public static final String PUBLISHERS_APPFACTORY_POST_BUILD_REPOFROM_XPATH_SELECTOR =
			PUBLISHERS_APPFACTORY_PLUGIN_XPATH_BASE + "repositoryFrom";
	public static final String APPLICATION_TRIGGER_PERIOD =
			"/*/triggers/hudson.triggers.SCMTrigger/spec";
	public static final String APPLICATION_POLLING_PERIOD = "PollingPeriod";
	public static final String APPLICATION_VERSION_VALUE_FREESTYLE = "**";
	public static final String DEFAULT_ARTIFACT_NAME = "*.";
	public static final String HUDSON_ARTIFACT_ARCHIVER = "HudsonArtifactArchiver";

	/**
	 * Defining git related constants
	 */
	public static final String GIT_REPOSITORY_CONTEXT ="git";
	public static final String GIT_REPOSITORY_TYPE ="git";
	public static final String GIT_REPOSITORY_EXTENSION =".git";
	public static final String GIT_IGNORE_FILE = ".gitignore";
	public static final String GIT_IGNORE_CONTENT = "*\n\n" + "!.gitignore";

	/**
	 * The server URL of API Manager instance.
	 */
	public static final String API_MANAGER_SERVICE_ENDPOINT = "ApiManager.Server.Url";
	public static final String API_MANAGER_SERVER_URL = "ServerUrls.ApiManager";

	public static final String GITBLIT_ADMIN_USERNAME = "RepositoryProviderConfig.git.Property.AdminUserName";
	public static final String GITBLIT_ADMIN_PASSWORD = "RepositoryProviderConfig.git.Property.AdminPassword";

	public static final String SCM_ADMIN_NAME = "RepositoryProviderConfig{@type}.Property.AdminUserName";
	public static final String SCM_ADMIN_PASSWORD = "RepositoryProviderConfig.{@type}.Property.AdminPassword";
	public static final String SCM_SERVER_URL = "RepositoryProviderConfig.{@type}.Property.BaseURL";
	public static final String SCM_READ_WRITE_ROLE = "RepositoryProviderConfig.{@type}.Property.ReadWriteRole";
	public static final String REPOSITORY_PROVIDER_CONFIG = "RepositoryProviderConfig";
	public static final String APPLICATION_TYPE_CONFIG = "ApplicationType";

	public static final String DEFAULT_APPLICATION_USER_ROLE = "DefaultUserRole";
	public static final String PERMISSION = "Permission";
	public static final String PER_APP_ROLE_PERMISSION = "/permission/admin/appfactory/belongs/toapplication";
	public static final String REGISTRY_GOVERNANCE_PATH = "/_system/governance";
	public static final String REGISTRY_APPLICATION_PATH = "/repository/applications";
	public static final String APPLICATION_ARTIFACT_NAME = "appinfo";

	public static final String APPLICATION_ID = "applicationId";
	public static final String APPLICATION_REVISION = "revision";
	public static final String APPLICATION_VERSION = "version";
	public static final String APPLICATION_EXTENSION = "extension";
	public static final String APPLICATION_USER = "user";
	public static final String APPLICATION_BUILD = "build";

	public static final String TRUNK = "trunk";
	public static final String BRANCH = "branches";
	public static final String TAG = "tags";
	public static final String SRC_LOCATION = "src";
	public static final String MASTER = "master";

	public static final String TRUNK_WEBAPP_ARTIFACT_VERSION_NAME = "TrunkVersioning.WebappVersioning.ArtifactVersionName";
	public static final String TRUNK_WEBAPP_SOURCE_VERSION_NAME = "TrunkVersioning.WebappVersioning.SourceVersionName";
	public static final String TRUNK_SERVICES_ARTIFACT_VERSION_NAME = "TrunkVersioning.ServiceVersioning.ArtifactVersionName";
	public static final String TRUNK_SERVICES_SOURCE_VERSION_NAME = "TrunkVersioning.ServiceVersioning.SourceVersionName";

	public static final String SCM_READ_WRITE_PERMISSION = "RepositoryProviderConfig.%s.Property.ReadWritePermission";
	public static final String REPO_ACCESSABILITY = "EnablePerDeveloperRepos";

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
    public static final String DB_CONFIG = "Database";
    public static final String DATASOURCE_NAME = DB_CONFIG+".Datasource.Name";
	public static final String EVN_VAR_TENANT_ID = "TENANT_ID";
	public static final String EVN_VAR_TENANT_DOMAIN = "TENANT_DOMAIN";

    // Rxt keys
    public static final String RXT_KEY_APPINFO_DEFAULT_DOMAIN = "application_mappedsubdomain";
    public static final String RXT_KEY_APPINFO_CUSTOM_URL = "application_customurl";
    public static final String RXT_KEY_APPINFO_CUSTOM_URL_VERIFICATION = "application_customurlverificationcode";
    public static final String RXT_KEY_APPVERSION_PROD_MAPPED_VERSION = "appversion_prodmappedsubdomain";
	public static final String RXT_KEY_APPINFO = "appinfo";
	public static final String RXT_KEY_APPINFO_APPLICATION = "application";
	public static final String RXT_KEY_APPINFO_KEY = "application_key";
	public static final String RXT_KEY_APPINFO_NAME = "application_name";
	public static final String RXT_KEY_APPINFO_TYPE = "application_type";
	public static final String RXT_KEY_APPINFO_REPO_TYPE = "application_repositorytype";
	public static final String RXT_KEY_APPINFO_DESC = "application_description";
	public static final String RXT_KEY_APPINFO_REPO_ACCESSABILITY = "application_repoAccessability";
	public static final String RXT_KEY_APPINFO_OWNER = "application_owner";
	public static final String RXT_KEY_APPINFO_PROD_VERSION = "application_prodVersions";
	public static final String RXT_KEY_APPINFO_PRODUCTION_VERSION = "application_productionVersions";
	public static final String RXT_KEY_APPINFO_BRANCHCOUNT = "application_branchcount";
	public static final String RXT_KEY_APPVERSION = "appversion";
	public static final String RXT_KEY_APPVERSION_ISAUTOBUILD = "appversion_isAutoBuild";
	public static final String RXT_KEY_APPVERSION_ISAUTODEPLOY = "appversion_isAutoDeploy";
	public static final String RXT_KEY_APPVERSION_PROMOTE = "Promote";
	public static final String MAVEN_REPO_LOCAL = "maven.repo.local";
	public static final String RESOURCES_FILE_LOCATION = "resources";
	public static final String ESB_CAPPS_LOCAL_REPO = "esbcappslocalrepo";

	public static final String CONSUME = "consume";
	public static final String INVOKE_PERMISSION = "/permission/admin/appfactory/realm";

	// constants added for Deployers

	public static final String APPLICATION_TYPE_JAXRS = "jaxrs";
	public static final String APPLICATION_TYPE_DBS = "dbs";
	public static final String APPLICATION_TYPE_ESB = "esb";
	public static final String APPLICATION_TYPE_XML = "xml";
	public static final String TENANT_ID = "tenantId";
	public static final String TENANT_DOMAIN = "tenantdomain";
	public static final String APP_ID = "applicationId";
	public static final String USER_NAME = "username";
	public static final String DEPLOYER_CLASSNAME ="DeployerClassName";
	public static final String UNDEPLOYER_CLASSNAME ="UndeployerClassName";
	public static final String APP_VERSION = "applicationVersion";
	public static final String JOB_NAME = "jobName";
	public static final String TAG_NAME = "tagName";
	public static final String DEPLOY_STAGE = "deployStage";
	public static final String ARTIFACT_TYPE = "artifactType";
	public static final String APP_TYPE = "ApplicationType";
	public static final String DEPLOYMENT_SERVER_URLS = "DeploymentServerURL";
	public static final String SERVER_DEPLOYMENT_PATHS = "ServerDeploymentPaths";
	public static final String DEPLOY_ACTION = "deployAction";
	public static final String DEPLOY_ACTION_LABEL_ARTIFACT = "labelArtifactAsPromoted";
	public static final String DEPLOY_ACTION_AUTO_DEPLOY = "autoDeployAction";
	public static final String DEPLOYER_APPLICATION_TYPE="Deployer.ApplicationType";
	public static final String UNDEPLOYER_APPLICATION_TYPE="Undeployer.ApplicationType";
	public static final String REPOSITORY_PROVIDER_PROPERTY="RepositoryProvider.Property";
	public static final String CLASS_NAME="ClassName";
	public static final String URL_PATTERN = "URLPattern";
	public static final String BASE_URL = "BaseURL";

	public static final String ESB_ARTIFACT_DEPLOYMENT_PATH = "synapse-configs"
			+ File.separator + "default";

	public static final String APP_VERSION_CACHE = "af.appversion.cache";
	public static final String APP_VERSION_CACHE_MANAGER = "af.appversion.cache.manager";
	public static final String APP_VERSION_STAGE_CACHE = "af.appversion.stage.cache";
	public static final String APP_VERSION_STAGE_CACHE_MANAGER = "af.appversion.stage.cache.manager";
    public static final String APPS_OF_USER_CACHE_MANAGER =  "af.user.application.cache.manager";
    public static final String APPS_OF_USER_CACHE = "af.user.application.cache";

	// constants added for BAM Stats
	public static final String BAM_ADD_DATA = "ADD";
	public static final String BAM_UPDATE_DATA = "UPDATE";
	public static final String BAM_DELETE_DATA = "DELETE";
	public static final String BAM_BUILD_SUCCESS = "SUCCESS";
	public static final String BAM_BUILD_FAIL = "FAIL";
	public static final String BAM_BUILD_START = "START";

	// User mgt related
	//public static final String FIRST_LOGGIN_MAPPED_TO = "Initials";
	//public static final String CLAIMS_FIRSTLOGIN = "http://wso2.org/claims/firstlogin";

	// Registry permission in clouds
	public static final String CLOUD_RESOURCE_PERMISSION = "CloudResourcePermissions.Resources.Resource";
	public static final String GOVERNANCE_REGISTRY = "GovernanceRegistry";
	public static final String ROLES = "Roles";
	public static final String STAGES = "Stages";
	public static final String FULLSTOP = ".";

	public static final String STORAGE_TYPE = "storagetype";
	public static final String BUILDABLE_STORAGE_TYPE = "buildable";
	public static final String NONBUILDABLE_STORAGE_TYPE = "nonbuildable";
	public static final String ORIGINAL_REPOSITORY = "original";
	public static final String FORK_REPOSITORY = "fork";
	public static final String MASTER_REPOSITORY = "master";

	// SSO constants
	public static final String SSO_NAME = "SSORelyingParty.Name";
	public static final String SSO_ASSERTION_CONSUMER_URL = "SSORelyingParty.AssertionConsumerService";
	public static final String SSO_IDENTITY_PROVIDER_URL = "SSORelyingParty.IdentityProviderURL";

	public static final String UPLOADED_APPLICATION_TMP_FOLDER_NAME = "tmpUploadedApps";

	public static final String PERMISSION_DEVELOP = "/permission/admin/appfactory/develop";
	public static final String PERMISSION_EXECUTE = "ui.execute";
	public static final String PERMISSION_TENANT_ADMIN = "/permission/admin";

	public static final String APP_VERSION_DEPLOYMENT_STATUS = "DeploymentStatus";
	public static final String APP_VERSION_DEPLOYMENT_URL = "DeploymentURL";
	public static final String APP_LAST_SUCCESS_DEPLOY_TIME = "LastSuccessDeployedTime";
    public static final String APP_LAST_DEPLOY_STATUS = "Success";

	public static final String INITIAL_UPLOADED_APP_VERSION = "1.0.0";
    public static final String SNAPSHOT = "-default-SNAPSHOT";

	public static final String APPLICATION_LIFECYCLE_STATE_KEY = "registry.lifecycle.ApplicationLifecycle.state";

	public static final String SIGNED_JWT_AUTH_USERNAME = "Username";

    /**
     * External system variables
     */
    public static final String SYSTEM_VARIABLE_M2_HOME = "M2_HOME";
    public static final String SYSTEM_VARIABLE_M3_HOME = "M3_HOME";

	/**
	 * Constants for file extensions
	 */
	public static final String FILE_EXTENSION_JKS = ".jks";

	/**
	 * Constants for other Strings
	 */
	public static final String EMPTY_STRING = "";
	public static final String WHITE_SPACE = " ";
	public static final String DOT_SEPERATOR = ".";
    public static final String SUBSCRIPTION_ALIAS_DOT_REPLACEMENT = "dot";
	public static final String ASTERISK = "*";
	public static final String MINUS = "-";
	public static final String PAAS_REPOSITORY_URL_PATTERN = "paasRepositoryURLPattern";
	public static final String PROPERTY_VALUE_SEPERATOR =",";
    public static final String UNDER_SCORE = "_";
    public static final String DOT = ".";
    public static final String HYPHEN = "-";


	public static final String APPFACTORY_ARTIFACT_NAME_VERSION_SEPERATOR = "-";


    public static final String TENANT_MGT_URL = "TenantMgtUrl";
	public static final String STAGE_PLACE_HOLDER = "{@stage}";
	public static final String APP_NAME_PLACE_HOLDER = "{@appName}";

	public static final String URL_SEPERATOR = "/";

	/**
	 * PAAS artifact repository configuration path constants
	 */
	public static final String PAAS_ARTIFACT_REPO_PROVIDER = "PAASArtifactStorageRepositoryProvider";
	public static final String PAAS_ARTIFACT_REPO_PROVIDER_BASE_URL =
			PAAS_ARTIFACT_REPO_PROVIDER + ".BaseURL";
	public static final String PAAS_ARTIFACT_REPO_PROVIDER_CLASS_NAME =
			PAAS_ARTIFACT_REPO_PROVIDER + ".ProviderClass";
	public static final String PAAS_ARTIFACT_REPO_PROVIDER_ADMIN_PASSWORD =
			PAAS_ARTIFACT_REPO_PROVIDER + ".AdminPassword";
	public static final String PAAS_ARTIFACT_REPO_PROVIDER_ADMIN_USER_NAME =
			PAAS_ARTIFACT_REPO_PROVIDER + ".AdminUserName";


	public static final String JENKINS_PROPERTYBASE_URL =
			"ContinuousIntegrationProvider.jenkins.Property.BaseURL";

	/**
	 * MB messege constants
	 */
	public static final String RUNTIMES_INFO = "runtimes";
	public static final String TENANT_INFO = "tenantInfoBean";
	public static final String STAGE = "stage";
	public static final String REPO_URL = "repoURL";
	public static final String TENANT_ADMIN_FOR_REST = "tenantAdminForRest";
	public static final String SERVER_URL_FOR_REST = "serverURLForRest";
	public static final String TENANT_ADMIN_PASSWORD_FOR_REST = "tenantAdminPasswordForRest";
	public static final String ANDES_ICF = "org.wso2.andes.jndi.PropertiesFileInitialContextFactory";
	public static final String CF_NAME_PREFIX = "connectionfactory.";
	public static final String CF_NAME = "qpidConnectionfactory";
	public static final String TOPIC = "topic";
	public static final String SUPPORT_DATASOURCE = "SupportDataSource";

    /**
     * AF DEPLOY Status
     */
    public static final String DEPLOY_SUCCESS = "Success";

    /**
     * Symbols
     */

	/**
	 * mvn goals
	 */
	public static final String MVN_GOAL_CLEAN = "clean";
	public static final String MVN_GOAL_INSTALL = "install";
	public static final String MVN_GOAL_ASSEMBLY = "-f assembly.xml";
	public static final String AF_ARCHETYPE_INITIAL_ARTIFACT_LOCATION = "_deploy_artifact";
	public static final String AF_ARCHETYPE_INITIAL_ARTIFACT_SOURCE_LOCATION = "built_artifact";
	public static final String AF_ARCHETYPE_INITIAL_ARTIFACT_ASSEMBLY_XML_LOCATION = "assembly.xml";
	public static final String AF_ARCHETYPE_INITIAL_ARTIFACT_BIN_XML_LOCATION = "bin.xml";

	public static final String AF_GIT_TMP_FOLDER = "repository";
	public static final String DEPLOYABLE_ARTIFACT_FOLDER = "artifact";
	public static final String ZIP_FILE_EXTENSION = "zip";


	public static final String AF_CONFIGURATION_INITIAL_STAGE_TAG_NAME = "StartStage";

	/**
	 * Enum to represent of different application stages.
	 */
	public enum ApplicationStage {
		DEVELOPMENT("development"), TEST("test"), STAGING("staging"), PRODUCTION(
				"production"), RETIRED("retired");

		String stage = null;

		ApplicationStage(String strValue) {
			stage = strValue;
		}

        public String getCapitalizedString(){
           return WordUtils.capitalizeFully(stage);
        }

		public String getStageStrValue() {
			return stage;
		}

	}
}
