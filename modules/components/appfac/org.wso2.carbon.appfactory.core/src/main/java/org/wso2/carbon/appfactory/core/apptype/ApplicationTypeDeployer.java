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

package org.wso2.carbon.appfactory.core.apptype;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.UnzipUtility;
import java.io.File;
import java.io.IOException;

/**
 * Deployer for application types. Deploy archived zip files with apptype extension.
 * Archive needs to have the application type xml named apptype.xml
 * Sample is in the resources/sample-apptype.xml
 */
public class ApplicationTypeDeployer extends AbstractDeployer {

	private static final Log log = LogFactory.getLog(ApplicationTypeDeployer.class);
	private AxisConfiguration axisConfig;
	private static final String APPTYPE_EXTENSION = "apptype";
	private static final String APPTYPE_CONFIGURATION_NAME = "apptype.xml";

	public ApplicationTypeDeployer() {
	}

	public ApplicationTypeDeployer(AxisConfiguration axisConfig) {
		this.axisConfig = axisConfig;
	}

	@Override
	public void init(ConfigurationContext configurationContext) {
		this.axisConfig = configurationContext.getAxisConfiguration();
	}

	/**
	 * This method deploys a new apptype
	 *
	 * @param deploymentFileData apptype file
	 */
	public void deploy(DeploymentFileData deploymentFileData) {
		File deploymentFile = deploymentFileData.getFile();
		boolean isDirectory = deploymentFile.isDirectory();
		if (!FilenameUtils.getExtension(deploymentFile.getName()).equals(APPTYPE_EXTENSION)){
			return;
		} else if (isDirectory) {  // Ignore folders
			return;
		}

		if(log.isDebugEnabled()){
			log.debug("Deploying the new apptype from : " + deploymentFile.getName());
		}

		String archivePath = deploymentFile.getAbsolutePath();
		String destinationFolderPath = deploymentFile.getParent() + File.separator + deploymentFile.getName().substring(
				0, deploymentFile.getName().lastIndexOf("."));
		try {
			UnzipUtility.unzip(archivePath, destinationFolderPath);
			File appTypeConfiguration = new File(destinationFolderPath + File.separator + APPTYPE_CONFIGURATION_NAME);
			File jenkinsJobConfig =
					new File(destinationFolderPath + File.separator + AppFactoryConstants.JENKINS_JOB_CONFIG);
			ApplicationTypeManager.getInstance().addAppType(appTypeConfiguration, jenkinsJobConfig);
		} catch (IOException e) {
			String msg = "Error while reading the apptype configuration : " + deploymentFile.getName();
			log.error(msg, e);
			throw new RuntimeException(msg, e);
		} catch (AppFactoryException e) {
			String msg = "Error while deploying the apptype : " + deploymentFile.getName();
			log.error(msg, e);
			throw new RuntimeException(msg, e);
		}
		if(log.isDebugEnabled()){
			log.debug("Deployed the new apptype from : " + deploymentFile.getName());
		}
	}

	public void undeploy(String fileName) throws DeploymentException {
		super.undeploy(fileName);
	}

	@Override public void setDirectory(String s) {

	}

	@Override public void setExtension(String s) {

	}
}
