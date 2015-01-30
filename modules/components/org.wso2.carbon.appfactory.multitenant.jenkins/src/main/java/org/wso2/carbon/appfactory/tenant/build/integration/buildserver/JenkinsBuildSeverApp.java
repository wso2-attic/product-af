/*
 * Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.appfactory.tenant.build.integration.buildserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.tenant.build.integration.BuildServerManagementException;
import org.wso2.carbon.appfactory.tenant.build.integration.internal.ServiceContainer;
import org.wso2.carbon.appfactory.tenant.build.integration.utils.JenkinsConfig;
import org.wso2.carbon.appfactory.tenant.build.integration.utils.Util;
import org.wso2.carbon.user.api.UserStoreException;

/**
 * Represent the Jenkins Build Server App.
 * Contains the logic for setting up jenkins envirnment for a tenant. This
 * includes setting up the JENKINS_HOME and creating a folder for MAVEN
 * repository
 */
public class JenkinsBuildSeverApp extends BuildServerApp {

	private Log log = LogFactory.getLog(JenkinsBuildSeverApp.class);

	/**
	 * A temporary folder created during modifying the jenkins binary ( a.k.a adding context.xml)
	 */
	private static final String TMP_FOLDER_NAME = "tmp";

	/**
	 * File name of the jenkins binary. ( assumed to be located at <CARBON_HOME>/repository/resources )
	 */
	public final static String DEFAULT_JENKINS_APP_NAME = "jenkins.war";

	/**
	 * File name of the archive which contains a pre-configured maven repository.
	 * During the tenant creation, contents of the file will be extracted to tenants maven repository
	 */
	public final static String PRE_CONFIGURED_MAVEN_REPO_ARCHIVE = "preconfigured-mvn-repo.zip";
	
	
	/**
	 * XMl element which specifies the local repository for the tenant.
	 * More information about the tag can be found it : http://maven.apache.org/settings.html
	 */
	public final static String MAVEN_SETTING_LOCAL_REPOSITORY = "localRepository";

	
	public JenkinsBuildSeverApp(String filePath) throws FileNotFoundException {
		super("Jenkins", filePath);
	}

	@Override
	public String getModifiedAppPath(String tenant) throws IOException,
	                                               BuildServerManagementException {
		File tenantLocation = new File(JenkinsConfig.getInstance().getTenantsLocation(), tenant);
		File tenantTmpLocation = new File(tenantLocation, TMP_FOLDER_NAME);
		File jenkinsFileLocation = new File(tenantTmpLocation, DEFAULT_JENKINS_APP_NAME);

		if (log.isDebugEnabled()) {
			log.debug("Jenkins web app is making ready to modify with " + tenant +
			          " tenant information " + jenkinsFileLocation.getAbsolutePath());
		}
		if (jenkinsFileLocation.exists()) {
			String msg =
			             "Jenkins web app is already exist at the location " +
			                     jenkinsFileLocation.getAbsolutePath() + ". For tenant " + tenant;
			log.error(msg);
			throw new BuildServerManagementException(
			                                   msg,
			                                   BuildServerManagementException.Code.TENANT_APP_ALREADY_EXISTS);
		}
		log.info("Copying Jenkins web app to tenant location " +
		         tenantTmpLocation.getAbsolutePath());
		FileUtils.copyFile(getFile(), jenkinsFileLocation);

		try {
			
			String tenantJenkinsHomePath = tenantLocation.getAbsolutePath() + File.separatorChar + "jenkinshome";
			
			
			String tenantMavenHomeDir = tenantLocation.getAbsolutePath() + File.separatorChar + "mavenhome";
			
			updateWebApp(tenantTmpLocation.getAbsolutePath(), tenantJenkinsHomePath, 
			             tenantMavenHomeDir,
			             jenkinsFileLocation.getAbsolutePath(), tenant);

			
			if ( log.isInfoEnabled()){
				log.info("Configurating maven repository at : " +  tenantMavenHomeDir);
			}
			
			String mavenSettingsFilePath = tenantMavenHomeDir + File.separatorChar + "settings.xml";
			String mavenRepoFilePath = tenantMavenHomeDir + File.separatorChar + "repository";
			
			configureMavenEnvironment(tenantJenkinsHomePath, mavenSettingsFilePath, mavenRepoFilePath);
			
			if (log.isInfoEnabled()){
				log.info("Extracting preconfigured maven repository at: " + mavenRepoFilePath);
			}
			
			extractPreConfiguredMavenRepo(mavenRepoFilePath);

            if(log.isDebugEnabled()){
                log.debug("Populating jenkins home  "+tenantJenkinsHomePath+" with config files " +
                        " from templates");
            }

            populateJenkinsHome(getTenantJenkinsHomeDir(),tenantJenkinsHomePath);

			log.info("Jenkins web app updated with tenant " + tenant + " tenant information " +
			         jenkinsFileLocation.getAbsolutePath());
			return jenkinsFileLocation.getAbsolutePath();

		} catch (InterruptedException e) {
			String msg = "Error while updating web application with tenant context.";
			log.error(msg, e);
			throw new BuildServerManagementException(
			                                         msg,
			                                         BuildServerManagementException.Code.ERROR_CREATING_TENANT_APP);
		} catch (UserStoreException e) {
			String msg = "Error while updating web application with tenant context.";
			log.error(msg, e);
			throw new BuildServerManagementException(
			                                         msg,
			                                         BuildServerManagementException.Code.ERROR_CREATING_TENANT_APP);
		} catch (XMLStreamException e) {
	        String msg = "Error while setting up jenkins and maven configuration file";
			log.error(msg, e);
			throw new BuildServerManagementException(msg, BuildServerManagementException.Code.ERROR_CREATING_TENANT_APP);
        } catch (FactoryConfigurationError e) {
	        String msg = "Error while setting up jenkins and maven configuration file";
			log.error(msg, e);
			throw new BuildServerManagementException(msg, BuildServerManagementException.Code.ERROR_CREATING_TENANT_APP);
        }

	}

    private String getTenantJenkinsHomeDir() throws BuildServerManagementException {
       String jenkinsHome= System.getenv(JenkinsTenantConstants
                .JENKINS_TENANT_HOME) ;
        if(jenkinsHome!=null){
            return jenkinsHome;
        } else {
            String msg = "System variable JENKINS_TENANT_HOME is not set";
            log.error(msg);
            throw new BuildServerManagementException(msg, BuildServerManagementException.Code
                    .ERROR_CREATING_TENANT);
        }
    }

    private void populateJenkinsHome(String jenkinsConfigResourceDirectory, String tenantJenkinsHomePath) throws BuildServerManagementException {
        AppfactoryConfigurationDeployer configDeployer = new AppfactoryConfigurationDeployer();
        try {
            configDeployer.copyBundledConfigs(jenkinsConfigResourceDirectory, tenantJenkinsHomePath);
        } catch (IOException e) {
           String msg= "Error while copying files from "+jenkinsConfigResourceDirectory+" to " +tenantJenkinsHomePath;
            log.error(msg,e);
            throw new BuildServerManagementException(msg,BuildServerManagementException.Code.ERROR_WHILE_COPYING_CONFIG_TEMPLATES);
        }
        try {
            configDeployer.updatePluginConfigs(jenkinsConfigResourceDirectory,tenantJenkinsHomePath);
        } catch (IOException e) {
            String msg= "Error while updating template values in " +tenantJenkinsHomePath;
            log.error(msg,e);
            throw new BuildServerManagementException(msg,BuildServerManagementException.Code.ERROR_WHILE_UPDATING_CONFIG_TEMPLATES);
        }

    }

    /**
	 * Updates Jenkins web app by inserting context.xml. This context.xml tells
	 * the web app where the Jenkins home is. Jenkins home needs to be different
	 * from tenant to tenant.
	 * 
	 * @param tmpLocation
	 * @param tenantJenkinsHomePath
	 * @param jenkinsWebApp
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws UserStoreException
	 */
	private void updateWebApp(String tmpLocation, String tenantJenkinsHomePath, String tenantMavenRepoDir, String jenkinsWebApp,
	                          String tenantDomain) throws IOException, InterruptedException,
	                                              UserStoreException {
		if(tenantDomain!=null)
			tenantDomain = tenantDomain.toLowerCase();
		
		String content =
		                 "<?xml version='1.0' encoding='utf-8'?>" +
		                         "<Context>" +
		                         "<Environment name='JENKINS_HOME' value='" + tenantJenkinsHomePath + "' type='java.lang.String' />" +
		                         "<Environment name='MAVEN_HOME' value='" + tenantMavenRepoDir + "' type='java.lang.String' />" +
		                         "<Environment name='TENANT_DOMAIN' value='" + tenantDomain + "' type='java.lang.String' />" +
		                         "<Environment name='TENANT_ID' value='" +
		                         ServiceContainer.getInstance().getRealmService()
		                                         .getTenantManager().getTenantId(tenantDomain) +
		                         "' type='java.lang.String' />" + 
		                         "</Context>";

		File contextFile = new File(tmpLocation, "META-INF" + File.separator + "context.xml");

		if (log.isDebugEnabled()) {
			log.debug("Creating Context.xml for the with tenant information at " +
			          contextFile.getParent());
		}
		if (contextFile.exists()) {
			if (log.isDebugEnabled()) {
				log.debug("Context.xml is already exist at " + contextFile.getAbsolutePath() +
				          " Deleting.");
			}
			contextFile.delete();
		}

		Util.createFile(contextFile, content);

		log.info("Context.xml is created at  " + contextFile.getParent() +
		         ". Updating war file with context.xml");

		Process p =
		            Runtime.getRuntime().exec("jar -uf " + jenkinsWebApp + " -C " + tmpLocation +
		                                              " META-INF" + File.separator + "context.xml");
		p.waitFor();
	}

	/**
	 * 
	 * @param tenantJenkinsHome
	 * @param mavenSettingsFilePath
	 * @param mavenRepoFilePath
	 * @throws IOException
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 */
	private void configureMavenEnvironment(String tenantJenkinsHome, String mavenSettingsFilePath, String mavenRepoFilePath)
	                                                                                       throws IOException, 
	                                                                                       XMLStreamException, 
	                                                                                       FactoryConfigurationError {
		
		OMElement fileContent = JenkinsConfig.getInstance().getGlobalMavenConfigFileContent();
		OMElement globalSettingsProviderEle = Util.findOrAddChild("globalSettingsProvider", fileContent);
		OMElement pathEle = Util.findOrAddChild("path", globalSettingsProviderEle);
		
		pathEle.setText(mavenSettingsFilePath);
		Util.writeToFilePath(tenantJenkinsHome + File.separatorChar + JenkinsConfig.GLOBAL_MAVEN_CONFIG_FILE, fileContent);
		
		OMElement mavenSettingsFileContentEle = JenkinsConfig.getInstance().loadMavenSettingsFileContent();
		
		OMElement localRepository = Util.findOrAddChild(MAVEN_SETTING_LOCAL_REPOSITORY, mavenSettingsFileContentEle);
		localRepository.setText(mavenRepoFilePath);
		
		Util.writeToFilePath(mavenSettingsFilePath, mavenSettingsFileContentEle);
	
	}

	/**
	 * Extract the zip file which contains a pre-configured maven repository
	 * @param mavenRepoPath the path where tenants maven repository will reside
	 * @throws IOException an error
	 */
	private void extractPreConfiguredMavenRepo(String mavenRepoPath) throws IOException {
		File repoArchieve =
		                    new File(Util.getCarbonResourcesPath(),
		                             PRE_CONFIGURED_MAVEN_REPO_ARCHIVE);
		if (repoArchieve.canRead()) {
			Util.unzip(repoArchieve.getAbsolutePath(), mavenRepoPath);
		} else {
			log.warn("unable to find pre-configured maven repository archieve at : " +
			         repoArchieve.getAbsolutePath());
		}

	}
	
}
