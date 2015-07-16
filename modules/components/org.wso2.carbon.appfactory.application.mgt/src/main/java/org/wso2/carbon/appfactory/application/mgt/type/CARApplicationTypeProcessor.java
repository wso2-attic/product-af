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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Application type processor to process car application requests in App factory
 */
public class CARApplicationTypeProcessor extends MavenMultiModuleApplicationTypeProcessor {

	private static final Log log = LogFactory.getLog(CARApplicationTypeProcessor.class);

	@Override
	public void doVersion(final String applicationId, String targetVersion, String currentVersion,
	                                String workingDirectory) throws AppFactoryException {
		File projectDir = new File(workingDirectory);
		List<String> goals = new ArrayList<String>();
		goals.add(AppFactoryConstants.MVN_GOAL_CLEAN);
		goals.add(AppFactoryConstants.MVN_GOAL_INSTALL);
		InvocationOutputHandler invocationOutputHandler = new InvocationOutputHandler() {
			@Override
			public void consumeLine(String s) {
				log.info(applicationId + ":" + s);
			}
		};
		ProjectUtils.runMavenCommand(goals, invocationOutputHandler, projectDir, null);
		super.doVersion(applicationId, targetVersion, currentVersion, workingDirectory);
		FileUtilities.deleteTargetFolders(new File(workingDirectory));
		AppVersionStrategyExecutor.doVersionCarArtifacts(targetVersion, new File(workingDirectory));
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
