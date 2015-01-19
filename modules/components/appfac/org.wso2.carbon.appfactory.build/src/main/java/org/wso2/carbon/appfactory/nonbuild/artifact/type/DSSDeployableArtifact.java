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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.plexus.util.FileUtils;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.nonbuild.artifact.ArtifactGeneratorFactory;
import org.wso2.carbon.appfactory.nonbuild.artifact.DeployableArtifact;

import java.io.File;
import java.io.IOException;

public class DSSDeployableArtifact extends DeployableArtifact {
	
	private static Log log = LogFactory.getLog(DSSDeployableArtifact.class);

	public DSSDeployableArtifact(String rootPath, String applicationId, String version, String stage) {
	    super(rootPath, applicationId, version, stage);
    }

	@Override
	public void generateDeployableFile() throws AppFactoryException {
		String dbsfilepath =
		                     getRootPath() + File.separator +
		                             ArtifactGeneratorFactory.appfactoryGitTmpFolder +
		                             File.separator + "src" + File.separator + "main" +
		                             File.separator + "dataservice" + File.separator;
		String[] fileNames = FileUtils.getFilesFromExtension(dbsfilepath, new String[] { "dbs" });

		String artifactSrcFile = fileNames[0];
		String targetFile =
		                    artifactSrcFile.substring(artifactSrcFile.lastIndexOf(File.separator) + 1);

		String artifactTmpFolder = getRootPath() + File.separator +
	                                   		ArtifactGeneratorFactory.deployableAtrifactFolder +
	                                   		File.separator + targetFile;

		try {
			FileUtils.copyFile(new File(artifactSrcFile), new File(artifactTmpFolder));
		} catch (IOException e) {
			String errMsg = "Error when copying folder from src to artifact tmp : " +
			                        e.getMessage();
			log.error(errMsg, e);
			throw new AppFactoryException(errMsg, e);
		}
	}

}
