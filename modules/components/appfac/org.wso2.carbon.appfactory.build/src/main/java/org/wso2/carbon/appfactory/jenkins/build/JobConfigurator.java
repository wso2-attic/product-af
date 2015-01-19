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

package org.wso2.carbon.appfactory.jenkins.build;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeBean;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeManager;

import java.util.Map;

/**
 * JobConfigurator class configures the build job for a particular application in order to build it
 */
public class JobConfigurator {

	private static final Log log = LogFactory.getLog(JobConfigurator.class);

	Map<String, String> parameters;

	public JobConfigurator(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	/**
	 * This method configures the build job template
	 *
	 * @param projectType type of the project to build
	 * @return configured build job template
	 * @throws AppFactoryException
	 */
	public OMElement configure(String projectType) throws AppFactoryException {
		ApplicationTypeBean applicationTypeBean =
				ApplicationTypeManager.getInstance().getApplicationTypeBean(projectType);
		OMElement jobTemplate = applicationTypeBean.getJenkinsJobConfig();
		return applicationTypeBean.getProcessor()
		                          .configureBuildJob(jobTemplate, parameters, projectType);
		// no need to check null for apptype processor since if it is is null this code won't reach
	}
}
