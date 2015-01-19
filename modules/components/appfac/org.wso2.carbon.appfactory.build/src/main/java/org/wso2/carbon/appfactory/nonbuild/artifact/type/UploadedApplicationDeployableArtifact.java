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
import org.wso2.carbon.appfactory.nonbuild.artifact.ArtifactGeneratorFactory;
import org.wso2.carbon.appfactory.nonbuild.artifact.DeployableArtifact;
import org.wso2.carbon.appfactory.utilities.project.ProjectUtils;
import org.wso2.carbon.context.CarbonContext;

/**
 * 
 *
 */
public class UploadedApplicationDeployableArtifact extends DeployableArtifact {

	private static Log log = LogFactory.getLog(UploadedApplicationDeployableArtifact.class);

	public UploadedApplicationDeployableArtifact(String rootPath, String applicationId, String version,
	                                  String stage) {
		super(rootPath, applicationId, version, stage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.wso2.carbon.appfactory.nonbuild.artifact.DeployableArtifact#
	 * generateDeployableFile()
	 */
	@Override
	public void generateDeployableFile() throws AppFactoryException {

		
		String applicationExtenstion = ProjectUtils.getApplicationExtenstion(getApplicationId(), CarbonContext.getThreadLocalCarbonContext().getTenantDomain());	
		
		String artifactFileName = getApplicationId() + "-" + getVersion() + "." + applicationExtenstion;
		
		
		String uploadedAppSrcFile =
		                              getRootPath() + File.separator +
		                                      ArtifactGeneratorFactory.appfactoryGitTmpFolder + File.separator + artifactFileName;

		String uploadedApptmpFolder =
		                              getRootPath() + File.separator +
		                                      ArtifactGeneratorFactory.deployableAtrifactFolder;
		                                      
		try {
			
			FileUtils.copyFileToDirectory(new File(uploadedAppSrcFile), new File(uploadedApptmpFolder));
		} catch (IOException e) {
			String errMsg =
			                "Error when copying folder from src to artifact tmp : " +
			                        e.getMessage();
			log.error(errMsg, e);
			throw new AppFactoryException(errMsg, e);
		}

	}
}
