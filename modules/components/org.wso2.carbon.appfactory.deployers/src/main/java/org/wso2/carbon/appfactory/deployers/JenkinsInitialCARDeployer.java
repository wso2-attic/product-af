/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *    WSO2 Inc. licenses this file to you under the Apache License,
 *    Version 2.0 (the "License"); you may not use this file except
 *    in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */
package org.wso2.carbon.appfactory.deployers;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.util.AppFactoryCoreUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created for initial deployment of car apptype.
 */
public class JenkinsInitialCARDeployer extends InitialArtifactDeployer {

	private static final Log log = LogFactory.getLog(JenkinsInitialCARDeployer.class);

	public JenkinsInitialCARDeployer(Map<String, String[]> parameters, int tenantId, String tenantDomain) {
		super(parameters, tenantId, tenantDomain);
	}

	/**
	 * This method will be used to retrieve the artifact in the given path filtered by extension.
	 *
	 * @param path      The path were artifact has been stored
	 * @param extension Artifact file extension
	 * @return Array of artifacts as Files
	 * @throws org.wso2.carbon.appfactory.common.AppFactoryException If there is an issue in filtering artifacts
	 */
	protected File[] getArtifact(String path, String extension, String stage, String applicationId, boolean isForLabel) throws AppFactoryException {
		stage = AppFactoryUtil.getAppfactoryConfiguration().
				getFirstProperty(AppFactoryConstants.AF_CONFIGURATION_INITIAL_STAGE_TAG_NAME);
		List<File> fileList = new ArrayList<File>();
		if (StringUtils.isNotBlank(path)) {
			if (StringUtils.isBlank(extension)) {
				String errMsg = "Extension cannot be empty";
				log.error(errMsg);
				throw new AppFactoryException(errMsg);
			}
			String[] fileExtensions = new String[]{extension};
			List<File> allFiles = (List<File>)(FileUtils.listFiles(new File(path), fileExtensions, true));
			if(isForLabel){
				return allFiles.toArray(new File[allFiles.size()]);
			}
			if (allFiles.isEmpty()) {
				String errMsg = "No built artifact found in the path : " + path;
				log.error(errMsg);
				throw new AppFactoryException(errMsg);
			}
			for (File file : allFiles) {
				String artifactID = file.getName().split(AppFactoryConstants.CAR_FILE_ARTIFACT_NAME_VERSION_SEPERATOR)[0];
				if((file.getName().startsWith(stage)) || (applicationId.equals(artifactID))){
					fileList.add(file);
				}
			}
		}
		return fileList.toArray(new File[fileList.size()]);
	}
}
