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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.utilities.file.FileUtilities;
import org.wso2.carbon.appfactory.utilities.project.ProjectUtils;
import org.wso2.carbon.appfactory.utilities.version.AppVersionStrategyExecutor;

import java.io.File;
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
		List<String> newGoals = new ArrayList<String>();
		newGoals.add("clean");
		newGoals.add("install");
		InvocationOutputHandler invocationOutputHandler = new InvocationOutputHandler() {
			@Override
			public void consumeLine(String s) {
				log.info(applicationId + ":" + s);
			}
		};
		ProjectUtils.runMavenCommand(newGoals, invocationOutputHandler, projectDir, null);
		super.doVersion(applicationId, targetVersion, currentVersion, workingDirectory);
		FileUtilities.deleteTargetFolders(new File(workingDirectory));
		AppVersionStrategyExecutor.doVersionCarArtifacts(targetVersion, new File(workingDirectory));
	}

}
