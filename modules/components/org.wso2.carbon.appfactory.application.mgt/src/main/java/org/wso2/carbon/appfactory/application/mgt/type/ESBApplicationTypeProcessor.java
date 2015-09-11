/*
 * Copyright 2005-2014 WSO2, Inc. (http://wso2.com)
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
package org.wso2.carbon.appfactory.application.mgt.type;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.utilities.file.FileUtilities;
import org.wso2.carbon.appfactory.utilities.project.ProjectUtils;
import org.wso2.carbon.appfactory.utilities.version.AppVersionStrategyExecutor;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Application type processor to process esb application requests in App factory
 */
public class ESBApplicationTypeProcessor extends MavenMultiModuleApplicationTypeProcessor {

	private static final Log log = LogFactory.getLog(ESBApplicationTypeProcessor.class);

    public ESBApplicationTypeProcessor(String type) {
        super(type);
    }

    @Override
	public void doVersion(final String applicationId, String targetVersion, String currentVersion,
	                                String workingDirectory) throws AppFactoryException {

		//Initializing goals for maven command
		List<String> goals = new ArrayList<String>();
		goals.add(AppFactoryConstants.MVN_GOAL_CLEAN);
		goals.add(AppFactoryConstants.MVN_GOAL_INSTALL);

		//Initializing output handler for maven command
		InvocationOutputHandler invocationOutputHandler = new InvocationOutputHandler() {
			@Override
			public void consumeLine(String s) {
				log.info(applicationId + ":" + s);
			}
		};

		//Initializing properties for maven command
		Properties properties=new Properties();

		//Create local repo location for build.
		//TODO: When Developer Studio implements the correct solution we can remove this part
		String repositoryPath = CarbonUtils.getCarbonHome() + File.separator +
		                        AppFactoryConstants.RESOURCES_FILE_LOCATION + File.separator +
		                        AppFactoryConstants.ESB_CAPPS_LOCAL_REPO;
		try {
			FileUtils.forceMkdir(new File(repositoryPath));
		} catch (IOException e) {
			String msg = "Error occurred while creating local repo";
			log.error(msg, e);
			throw new AppFactoryException(msg, e);
		}

		properties.put(AppFactoryConstants.MAVEN_REPO_LOCAL, repositoryPath);
		File projectDir = new File(workingDirectory);

		//Run maven command to build capp project before versioning to create capp pom files
		ProjectUtils.runMavenCommand(goals, invocationOutputHandler, projectDir, null, properties);
		if(log.isDebugEnabled()) {
			log.debug("Ran maven command to build before versioning application id : " + applicationId +
			          ", tenantDomain" + CarbonContext.getThreadLocalCarbonContext().getTenantDomain() + " successfully");
		}

		//Call super versioning to do pom versioning
		super.doVersion(applicationId, targetVersion, currentVersion, workingDirectory);

		if(log.isDebugEnabled()) {
			log.debug("Pom versioning happened successfully for application id : " + applicationId
			          + ", tenantDomain" + CarbonContext.getThreadLocalCarbonContext().getTenantDomain());
		}

		//Remove all target locations created by the build
		FileUtilities.deleteTargetFolders(projectDir);

		if(log.isDebugEnabled()) {
			log.debug("Deleted target Folders for application id : " + applicationId
			          + ", tenantDomain" + CarbonContext.getThreadLocalCarbonContext().getTenantDomain());
		}

		//Versioning of car artifacts (artifact.xml and synapse configs)
		AppVersionStrategyExecutor.doVersionCarArtifacts(targetVersion, projectDir);

		if(log.isDebugEnabled()) {
			log.debug("Successfully versioned to application code for application id : " + applicationId
			          + ", tenantDomain" + CarbonContext.getThreadLocalCarbonContext().getTenantDomain());
		}
	}

	@Override
	protected void initialDeployArtifactGeneration(String applicationId, String workingDirectory, File archetypeDir)
			throws AppFactoryException {
		List<String> goals = new ArrayList<String>();
		goals.add(AppFactoryConstants.MVN_GOAL_CLEAN);
		goals.add(AppFactoryConstants.MVN_GOAL_INSTALL);
		File projectDir = new File(archetypeDir.getAbsolutePath() + File.separator + applicationId + File.separator
		                           + AppFactoryConstants.AF_ARCHETYPE_INITIAL_ARTIFACT_SOURCE_LOCATION);
		File initialArtifact = new File(archetypeDir.getAbsolutePath() + File.separator + applicationId
		                                + AppFactoryConstants.AF_ARCHETYPE_INITIAL_ARTIFACT_LOCATION);
		boolean isInitialArtifactGenerationSuccess = ProjectUtils.initialDeployArtifactGeneration
				(applicationId, projectDir, initialArtifact, new File(workingDirectory), goals);
		if(isInitialArtifactGenerationSuccess){
			try {
				FileUtils.deleteDirectory(projectDir);
			} catch (IOException e) {
				String msg = "Error occurred while deleting files used in deploy artifact generation";
				log.error(msg, e);
				throw new AppFactoryException(msg, e);
			}
		}
	}

}
