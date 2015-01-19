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

package org.wso2.carbon.appfactory.jenkins.build;

import org.wso2.carbon.appfactory.common.AppFactoryConstants;

import java.lang.String;

public class JenkinsCIConstants {

	public static final String JOB_CONFIG_APP_TYPE_PATH = 
			"/maven2-moduleset/AppfactoryAppType";	
    public static final String CONTINUOUS_INTEGRATION_PROVIDER_CONFIG_SELECTOR =
            "ContinuousIntegrationProvider";
    public static final String BASE_URL_CONFIG_SELECTOR =
             "ContinuousIntegrationProvider.jenkins.Property.BaseURL";
    public static final String MAVEN3_CONFIG_NAME_CONFIG_SELECTOR =
            "ContinuousIntegrationProvider.jenkins.Property.Maven3ConfigName";
    public static final String AUTHENTICATE_CONFIG_SELECTOR =
            "ContinuousIntegrationProvider.jenkins.Property.Authenticate";
    public static final String DEFAULT_GLOBAL_ROLES_CONFIG_SELECTOR =
            "ContinuousIntegrationProvider.jenkins.Property.DefaultGlobalRoles";
    public static final String PROJECT_ROLE_PERMISSIONS_CONFIG_SELECTOR =
            "ContinuousIntegrationProvider.jenkins.Property.ProjectRolePermissions";
    public static final String LISTENER_PRIORITY_CONFIG_SELECTOR =
            "ContinuousIntegrationProvider.jenkins.Property.ListenerPriority";
    public static final String JENKINS_CLIENT_CONNECTION_COUNT =
            "ContinuousIntegrationProvider.jenkins.Property.ClientConnectionCount";
    public static final String REPOSITORY_TYPE = "repository.type";
    public static final String REPOSITORY_URL = "repository.url";
    public static final String SVN_REPOSITORY_XPATH_SELECTOR =
            "/*/scm/locations/hudson.scm.SubversionSCM_-ModuleLocation/remote";
    public static final String GIT_REPOSITORY_XPATH_SELECTOR =
            "/*/scm/userRemoteConfigs/hudson.plugins.git.UserRemoteConfig/url";
    public static final String GIT_REPOSITORY_VERSION_XPATH_SELECTOR =
            "/*/scm/branches/hudson.plugins.git.BranchSpec/name";
    public static final String REPOSITORY_ACCESS_CREDENTIALS_USERNAME =
            "repository.credentials.username";
    public static final String REPOSITORY_ACCESS_CREDENTIALS_PASSWORD =
            "repository.credentials.password";
    public static final String SONAR_PDF_TASK_XPATH_SELECTOR =
            "/*/postbuilders/hudson.tasks.Maven/targets";

    public static final String MAVEN3_CONFIG_NAME = "maven3.config.name";
    public static final String MAVEN3_CONFIG_NAME_XAPTH_SELECTOR = "mavenName";
    public static final String ARTIFACT_ARCHIVER_CONFIG_NAME_XAPTH_SELECTOR = "/*/publishers/hudson.tasks.ArtifactArchiver/artifacts";

    public static final String APPLICATION_ID = AppFactoryConstants.APPLICATION_ID;
    public static final String APPLICATION_VERSION = AppFactoryConstants.APPLICATION_VERSION;
    public static final String APPLICATION_USER = AppFactoryConstants.APPLICATION_USER;
    public static final String REPOSITORY_FROM = "repositoryFrom";
    public static final String APPLICATION_EXTENSION = "application.extension";

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
    public static final String APPLICATION_TRIGGER_PERIOD=
            "/*/triggers/hudson.triggers.SCMTrigger/spec";
    
    
    
    private static final String PUBLISHERS_ARTIFACT_ARCHIVER_XPATH_BASE = "/*/publishers/hudson.tasks.ArtifactArchiver/";
    public static final String PUBLISHERS_APPFACTORY_ARTIFACT_XPATH_SELECTOR =
    		PUBLISHERS_ARTIFACT_ARCHIVER_XPATH_BASE + "/artifacts";
    
    public static final String JENKINS_SERVER_ADMIN_USERNAME = "JenkinsServerAdminUsername";
    public static final String JENKINS_SERVER_ADMIN_PASSWORD = "JenkinsServerAdminPassword";
    
    /**
     * Defines constants related to jenkins role-strategy-pugin
     */
    public final static class RoleStrategy{
    	
    	public static final String ASSIGN_ROLE_SERVICE = "/descriptorByName/com.michelin.cio.hudson.plugins.rolestrategy"
                + ".RoleBasedAuthorizationStrategy/assignRolesSubmit";
    	
		public static final String UNASSIGN_ROLE_SERVICE = "/descriptorByName/com.michelin.cio.hudson.plugins.rolestrategy"
				+ ".RoleBasedAuthorizationStrategy/unAssignRolesSubmit";        
    }
    
    
}

