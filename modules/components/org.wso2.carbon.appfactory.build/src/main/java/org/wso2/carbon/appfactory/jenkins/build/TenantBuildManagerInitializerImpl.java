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
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.TenantBuildManagerInitializer;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Default implementation of {@link TenantBuildManagerInitializer}
 */
public class TenantBuildManagerInitializerImpl implements
		TenantBuildManagerInitializer {
	private static final Log log = LogFactory
			.getLog(TenantBuildManagerInitializerImpl.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onTenantCreation(String tenantDomain, String usagePlan) {
		log.info("Initializing jenkins for tenant "+ tenantDomain);
		try {
			createTenantFolder(tenantDomain);
			RestBasedJenkinsCIConnector.getInstance().extractMvnRepo(tenantDomain);
		} catch (AppFactoryException e) {
			String msg = "Error occurred while tenant creation in jenkins";
			log.error(msg, e);
		} catch (XMLStreamException e) {
			String msg = "Error occurred while tenant creation in jenkins";
			log.error(msg, e);
		}

	}

	/**
	 * Set values in OmElement
	 *
	 * @param template Jenkins job configuration template
	 * @param selector Selector of the template
	 * @param value    related value from the project
	 * @throws org.wso2.carbon.appfactory.common.AppFactoryException
	 */
	protected void setValueUsingXpath(OMElement template, String selector, String value)
			throws AppFactoryException {

		try {
			AXIOMXPath axiomxPath = new AXIOMXPath(selector);
			Object selectedObject = axiomxPath.selectSingleNode(template);

			if (selectedObject != null && selectedObject instanceof OMElement) {
				OMElement svnRepoPathElement = (OMElement) selectedObject;
				svnRepoPathElement.setText(value);
			} else {
				log.warn("Unable to find xml element matching selector : " + selector);
			}

		} catch (Exception e) {
			String msg = "Error while setting values using Xpath selector:" + selector;
			log.error(msg, e);
			throw new AppFactoryException(msg, e);
		}
	}

	/**
	 * Create tenant job. This will create "Folder Job"(folder with the name of {@code tenantDomain} in
	 * $JENKINS_HOME/jobs directory) to represent the tenant in the jenkins
	 *
	 * @param tenantDomain tenant Domain
	 * @throws AppFactoryException
	 * @throws XMLStreamException
	 */
	protected void createTenantFolder(String tenantDomain) throws AppFactoryException, XMLStreamException {
		String fileLocation =
				CarbonUtils.getCarbonConfigDirPath() + File.separator + JenkinsCIConstants.CONFIG_FOLDER +
				File.separator + JenkinsCIConstants.TENANT_FOLDER_CONFIG_FILE;
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(fileLocation);
		} catch (FileNotFoundException e) {
			String msg = "Default job configuration for tenant folder creation not found in: " + fileLocation;
			log.error(msg, e);
			throw new AppFactoryException(msg, e);
		}
		StAXOMBuilder builder = new StAXOMBuilder(inputStream);
		OMElement buildTemplate = builder.getDocumentElement();
		setValueUsingXpath(buildTemplate,
		                   JenkinsCIConstants.TENANT_FOLDER_CONFIG_DISPLAY_NAME,
		                   tenantDomain);
		setValueUsingXpath(buildTemplate,
		                   JenkinsCIConstants.TENANT_FOLDER_CONFIG_DESCRIPTION,
		                   tenantDomain);

		// Here we are sending tenant domain as the job name for tenant job
		RestBasedJenkinsCIConnector.getInstance().createTenantJob(tenantDomain, buildTemplate, tenantDomain);
	}
}
