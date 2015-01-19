/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.appfactory.nonbuild.artifact.type;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.jenkins.build.service.TenantContinousIntegrationSystemDriverService;
import org.wso2.carbon.appfactory.nonbuild.artifact.ArtifactGeneratorFactory;
import org.wso2.carbon.appfactory.nonbuild.artifact.DeployableArtifact;
import org.wso2.carbon.appfactory.nonbuild.utility.FolderZiper;

public class JaggeryDeployableArtifact extends DeployableArtifact {

	
	private static Log log = LogFactory.getLog(JaggeryDeployableArtifact.class);
	
	
	public JaggeryDeployableArtifact(String rootPath, String applicationId, String version, String stage) {
		super(rootPath, applicationId, version, stage);
	}

	@Override
	public void generateDeployableFile() throws AppFactoryException {
		
		String artifactFileName = "";
		String artifactZIPFileName = "";
		if (getVersion().equals("trunk")) {
			artifactZIPFileName = getApplicationId() + "-default-SNAPSHOT.zip";
			artifactFileName = getApplicationId() + "-default-SNAPSHOT";
		} else {
			artifactZIPFileName = getApplicationId() + "-" + getVersion() + ".zip";
			artifactFileName = getApplicationId() + "-" + getVersion();
		}
		String jaggerySrcFolder =
		                          getRootPath() + File.separator + ArtifactGeneratorFactory.appfactoryGitTmpFolder +
		                                  File.separator + "src";
		String jaggeryArtifactTmpFolder =
		                                  getRootPath() + File.separator +
		                                          ArtifactGeneratorFactory.deployableAtrifactFolder + File.separator +
		                                          artifactFileName;
		String jaggeryArtifactTmpZIP =
		                               getRootPath() + File.separator +
		                                       ArtifactGeneratorFactory.deployableAtrifactFolder + File.separator +
		                                       artifactZIPFileName;
		try {
			
			FileUtils.copyDirectory(new File(jaggerySrcFolder), new File(jaggeryArtifactTmpFolder));
			
		} catch (IOException e) {
			String errMsg =
					"Error when copying folder from src to artifact tmp : " +
							e.getMessage();
			log.error(errMsg, e);
			throw new AppFactoryException(errMsg, e);
		}

		// Zipping application folder
		try {
			FolderZiper.zipFolder(jaggeryArtifactTmpFolder, jaggeryArtifactTmpZIP);
		} catch (Exception e) {
			String msg = "Unable to zip files : " + artifactFileName;
            log.error(msg,e);
            throw new AppFactoryException(msg, e);
		}

		try {
			FileUtils.deleteDirectory(new File(jaggeryArtifactTmpFolder));
		} catch (IOException e) {
			String errMsg =
					"Error when zipping the application folder : " +
							e.getMessage();
			log.error(errMsg, e);
			throw new AppFactoryException(errMsg, e);
		}

	}


}
