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

package org.wso2.carbon.appfactory.application.mgt.type;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.common.util.FolderZiper;

import java.io.File;
import java.io.IOException;

/**
 * PHP application type processor
 */
public class JaggeryApplicationTypeProcessor extends AbstractFreeStyleApplicationTypeProcessor {
    private static final Log log = LogFactory.getLog(JaggeryApplicationTypeProcessor.class);
 @Override
    public void generateApplicationSkeleton(String applicationId, String workingDirectory) throws AppFactoryException {
	    super.generateApplicationSkeleton(applicationId, workingDirectory);
        File pomFile = new File(workingDirectory + File.separator + AppFactoryConstants.DEFAULT_POM_FILE);
        boolean result = FileUtils.deleteQuietly(pomFile);
        if (!result){
            log.warn("Error while deleting pom.xml for application id : " + applicationId);
        }
    }

    @Override
    public void doVersion(String applicationId, String targetVersion, String currentVersion,
                                              String workingDirectory) throws AppFactoryException {
        File workDir = new File(workingDirectory);
        for (File file : workDir.listFiles()) {
            if (file.isDirectory() && file.getName().contains("-")) {
                String newName = changeFileName(file.getName(), targetVersion);
                File newFile =
                    new File(file.getAbsolutePath().replace(file.getName(),
                                                            newName));
                file.renameTo(newFile);
            }
        }
    }

	@Override
	public void generateDeployableFile(String rootPath, String applicationId,
	                                   String version, String stage) throws AppFactoryException {
		String artifactFileName = "";
		String artifactZIPFileName = "";
		if (version.equals(AppFactoryConstants.TRUNK)) {
			artifactZIPFileName = applicationId + AppFactoryConstants.SNAPSHOT +
			                      AppFactoryConstants.FILENAME_EXTENSION_SEPERATOR +
			                      AppFactoryConstants.ZIP_FILE_EXTENSION;
			artifactFileName = applicationId + AppFactoryConstants.SNAPSHOT;
		} else {
			artifactZIPFileName = applicationId + AppFactoryConstants.APPFACTORY_ARTIFACT_NAME_VERSION_SEPERATOR +
			                      version + AppFactoryConstants.FILENAME_EXTENSION_SEPERATOR +
			                      AppFactoryConstants.ZIP_FILE_EXTENSION;;
			artifactFileName = applicationId + AppFactoryConstants.APPFACTORY_ARTIFACT_NAME_VERSION_SEPERATOR + version;
		}
		String jaggerySrcFolder = rootPath + File.separator + AppFactoryConstants.AF_GIT_TMP_FOLDER + File.separator
		                          + AppFactoryConstants.SRC_LOCATION;
		String jaggeryArtifactTmpFolder = rootPath + File.separator + AppFactoryConstants.DEPLOYABLE_ARTIFACT_FOLDER
		                                  + File.separator + artifactFileName;
		String jaggeryArtifactTmpZIP = rootPath + File.separator + AppFactoryConstants.DEPLOYABLE_ARTIFACT_FOLDER
		                               + File.separator + artifactZIPFileName;
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

    private static String changeFileName(String name, String changedVersion) throws AppFactoryException {

        String applicationName = name;
        String artifactVersionXPath = "-" + AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(ARTIFACT_VERSION_XPATH);
        if(name.lastIndexOf(artifactVersionXPath) != -1) {
            applicationName = name.substring(0, name.lastIndexOf(artifactVersionXPath));
        } else if (name.lastIndexOf("-") != -1) {
            applicationName = name.substring(0, name.lastIndexOf("-"));
        }
        return applicationName + "-" + changedVersion;
    }

}
