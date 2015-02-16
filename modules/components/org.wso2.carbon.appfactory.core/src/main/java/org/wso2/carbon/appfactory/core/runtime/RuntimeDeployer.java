/*
 *
 * Copyright 2014 WSO2, Inc. (http://wso2.com)
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
 *
 */

package org.wso2.carbon.appfactory.core.runtime;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;

import java.io.File;

/**
 * Deployer for runtimes. Deploy archived zip files with runtime extension.
 * Archive needs to have the runtime xml named as runtime.xml
 * Sample is in the resources/sample-runtime.xml
 */
public class RuntimeDeployer extends AbstractDeployer {

	private static final Log log = LogFactory.getLog(RuntimeDeployer.class);
	private AxisConfiguration axisConfig;
	private static final String RUNTIME_EXTENSION = "xml";

	public RuntimeDeployer() {
	}

	public RuntimeDeployer(AxisConfiguration axisConfig) {
		this.axisConfig = axisConfig;
	}

	@Override
	public void init(ConfigurationContext configurationContext) {
		this.axisConfig = configurationContext.getAxisConfiguration();

	}

	@Override
	public void deploy(DeploymentFileData runtimeFileData) {

		File runtimeFile = runtimeFileData.getFile();
		boolean isDirectory = runtimeFile.isDirectory();
		if (log.isDebugEnabled()) {
			log.debug("Deploying the new runtime from : " + runtimeFile.getName());
		}

		if (!RUNTIME_EXTENSION.equals(FilenameUtils.getExtension(runtimeFile.getName()))) {
			if (log.isDebugEnabled()) {
				log.debug("Runtime file is not a xml file, The available file is" + runtimeFile.getName());
			}
			return;
		} else if (isDirectory) {  // Ignore folders
			if (log.isDebugEnabled()) {
				log.debug("The available file is a directory" + runtimeFile);
			}
			return;
		}

		try {
			RuntimeManager.getInstance().addAppRuntime(runtimeFile);
		} catch (AppFactoryException e) {
			log.error("Error while deploying the runtime : " + runtimeFile.getName(), e);
		}

		if (log.isDebugEnabled()) {
			log.debug("Deployed the new runtime from : " + runtimeFile.getName());
		}
	}

	@Override
	public void undeploy(String fileName) throws DeploymentException {
		super.undeploy(fileName);
	}

	@Override
	public void setDirectory(String s) {

	}

	@Override
	public void setExtension(String s) {

	}
}
