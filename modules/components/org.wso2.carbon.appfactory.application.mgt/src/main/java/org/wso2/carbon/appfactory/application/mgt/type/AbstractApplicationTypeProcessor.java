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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeProcessor;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeValidationStatus;
import org.wso2.carbon.appfactory.utilities.project.ProjectUtils;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Base class contains getters and setters
 */
public abstract class AbstractApplicationTypeProcessor implements ApplicationTypeProcessor {
    private static final Log log = LogFactory.getLog(AbstractApplicationTypeProcessor.class);
    public static final String MAVEN_ARCHETYPE_REQUEST = "MavenArcheTypeRequest";
    public static final String LAUNCH_URL_PATTERN = "LaunchURLPattern";
    public static final String PARAM_TENANT_DOMAIN = "{tenantDomain}";
    public static final String PARAM_APP_ID = "{applicationID}";
    public static final String PARAM_APP_VERSION = "{applicationVersion}";
    public static final String PARAM_APP_STAGE = "{stage}";
	public static final String PARAM_HOSTNAME = "{hostname}";
    public static final String PARAM_APP_STAGE_NAME_SUFFIX = "StageParam";
    public static final String ARTIFACT_VERSION_XPATH = "TrunkVersioning.WebappVersioning.ArtifactVersionName";
    public static final String SOURCE_VERSION_XPATH = "TrunkVersioning.WebappVersioning.SourceVersionName";
    public static final String XPATH_SEPERATOR = ".";

	protected Properties properties;

	@Override
	public void generateApplicationSkeleton(String applicationId, String workingDirectory) throws AppFactoryException {
		boolean isSuccess = ProjectUtils.generateProjectArchetype(applicationId, workingDirectory
				, ProjectUtils.getArchetypeRequest(applicationId, getProperty(MAVEN_ARCHETYPE_REQUEST)));
		if(isSuccess) {
			String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
			File archetypeDir = new File(
					CarbonUtils.getTmpDir() + File.separator + tenantDomain + File.separator + applicationId +
					File.separator +
					AppFactoryConstants.MAVEN_ARCHETYPE_DIR);
			initialDeployArtifactGeneration(applicationId, workingDirectory, archetypeDir);
			ProjectUtils.configureFinalName(archetypeDir.getAbsolutePath());
			ProjectUtils.copyArchetypeToTrunk(archetypeDir.getAbsolutePath(), workingDirectory);
			boolean deleteResult = FileUtils.deleteQuietly(archetypeDir);
			if (!deleteResult) {
				log.warn("Error while deleting the archetype directory");
			}
		}
	}

	/**
	 * Generate the initial artifact by running maven assembly plugin on pre installed artifacts in archetype
	 * @param applicationId application id
	 * @param workingDirectory current working directory. this is where we do all application related file operations
	 * @param archetypeDir location of the archetypes generated
	 * @throws AppFactoryException
	 */
	protected void initialDeployArtifactGeneration(String applicationId, String workingDirectory, File archetypeDir)
			throws AppFactoryException {
		List<String> goals = new ArrayList<String>();
		goals.add(AppFactoryConstants.MVN_GOAL_CLEAN);
		goals.add(AppFactoryConstants.MVN_GOAL_INSTALL);
		goals.add(AppFactoryConstants.MVN_GOAL_ASSEMBLY);
		File projectDir = new File(archetypeDir.getAbsolutePath() + File.separator + applicationId);
		File initialArtifact = new File(archetypeDir.getAbsolutePath() + File.separator + applicationId
		                                + AppFactoryConstants.AF_ARCHETYPE_INITIAL_ARTIFACT_LOCATION);
		boolean isInitialArtifactGenerationSuccess = ProjectUtils.initialDeployArtifactGeneration
				(applicationId, projectDir, initialArtifact, new File(workingDirectory), goals);
		if(isInitialArtifactGenerationSuccess){
			try {
				File initialArtifactSource = new File(projectDir + File.separator
				                                 + AppFactoryConstants.AF_ARCHETYPE_INITIAL_ARTIFACT_SOURCE_LOCATION);
				FileUtils.deleteDirectory(initialArtifactSource);
				File assemblyFile = new File(projectDir + File.separator
				                             + AppFactoryConstants.AF_ARCHETYPE_INITIAL_ARTIFACT_ASSEMBLY_XML_LOCATION);
				FileUtils.forceDelete(assemblyFile);
				File assemblyDescriptorFile = new File(projectDir + File.separator
				                                       + AppFactoryConstants.AF_ARCHETYPE_INITIAL_ARTIFACT_BIN_XML_LOCATION);
				FileUtils.forceDelete(assemblyDescriptorFile);
			} catch (IOException e) {
				String msg = "Error occurred while deleting files used in deploy artifact generation";
				log.error(msg, e);
				throw new AppFactoryException(msg, e);
			}
		}
	}

	@Override
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	@Override
	public String getProperty(String name) {
		return (String) this.properties.getProperty(name);
	}

	@Override
	public List<File> getPreVersionDeleteableFiles(String appId, String targetVersion,
	                                               String currentVersion, String absolutePath) throws AppFactoryException {
		return new ArrayList<File>();
	}

	@Override
	public String getDeployedURL(String tenantDomain, String applicationID,
	                             String applicationVersion, String stage) throws AppFactoryException{
		String url = (String) this.properties.getProperty(LAUNCH_URL_PATTERN);

		String artifactTrunkVersionName = AppFactoryUtil.getAppfactoryConfiguration().
				getFirstProperty(AppFactoryConstants.TRUNK_WEBAPP_ARTIFACT_VERSION_NAME);
		String sourceTrunkVersionName = AppFactoryUtil.getAppfactoryConfiguration().
				getFirstProperty(AppFactoryConstants.TRUNK_WEBAPP_SOURCE_VERSION_NAME);
		if(applicationVersion.equalsIgnoreCase(sourceTrunkVersionName)) {
			applicationVersion = artifactTrunkVersionName;
		}

		String urlStageValue = "";

		try {
			urlStageValue = (String) this.properties.getProperty(stage + PARAM_APP_STAGE_NAME_SUFFIX);
		} catch (Exception e){
			// no need to throw just log and continue
			log.error("Error while getting the url stage value fo application:" + applicationID, e);
		}

		if(urlStageValue == null){
			urlStageValue = "";
		}

		url = url.replace(PARAM_TENANT_DOMAIN, tenantDomain).replace(PARAM_APP_ID, applicationID)
		         .replace(PARAM_APP_VERSION, applicationVersion).replace(PARAM_APP_STAGE, urlStageValue);
		return url;
	}

	@Override
	public ApplicationTypeValidationStatus validate(String uploadedFileName) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Method not supported");
	}

	/**
	 * Add the repository related information to the jenkinsJobConfig file
	 *
	 * @param jobConfigTemplate template of config file
	 * @param parameters        parameters to be added
	 * @return the template after replacing the default values of repository related configurations
	 * @throws AppFactoryException if an error occurs
	 */
	protected OMElement configureRepositoryData(OMElement jobConfigTemplate, Map<String, String> parameters)
			throws AppFactoryException {
		if (AppFactoryConstants.REPOSITORY_TYPE_GIT.equals(parameters.get(AppFactoryConstants.REPOSITORY_TYPE))) {
			String url = parameters.get(AppFactoryConstants.REPOSITORY_URL);
			setValueUsingXpath(jobConfigTemplate, AppFactoryConstants.GIT_REPOSITORY_XPATH_SELECTOR,
			                   url);
			String repositoryBranchName = parameters.get(AppFactoryConstants.APPLICATION_VERSION);
			if (AppFactoryConstants.TRUNK.equals(repositoryBranchName)) {
				repositoryBranchName = AppFactoryConstants.REPOSITORY_BRANCH_MASTER;
			}
			setValueUsingXpath(jobConfigTemplate,
			                   AppFactoryConstants.GIT_REPOSITORY_VERSION_XPATH_SELECTOR,
			                   repositoryBranchName);
		} else {
			setValueUsingXpath(jobConfigTemplate, AppFactoryConstants.SVN_REPOSITORY_XPATH_SELECTOR,
			                   parameters.get(AppFactoryConstants.REPOSITORY_URL));
		}
		return jobConfigTemplate;
	}

	/**
	 * Set values in OmElement
	 *
	 * @param template Jenkins job configuration template
	 * @param selector Selector of the template
	 * @param value    related value from the project
	 * @throws AppFactoryException
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
     * Generate the unique cartridge alias
     *
     * @param applicationId Unique ID of the user application
     * @param tenantDomain  Tenant domain which application belongs to
     * @return Generated cartridge alias
     */
    public static String getCartridgeAlias(String applicationId, String applicationVersion,String tenantDomain, boolean subscribeOnDeployment) {

        if (StringUtils.isBlank(applicationId) || StringUtils.isBlank(tenantDomain)) {
            return null;
        }
        String cartridgeAlias = null;
        tenantDomain = tenantDomain.replace(AppFactoryConstants.DOT_SEPERATOR, AppFactoryConstants.SUBSCRIPTION_ALIAS_DOT_REPLACEMENT);
        if (subscribeOnDeployment) {
            applicationVersion = applicationVersion.replaceAll("\\.+",AppFactoryConstants.HYPHEN);
            cartridgeAlias = applicationId + AppFactoryConstants.HYPHEN + applicationVersion + tenantDomain;
        }else{
            cartridgeAlias = applicationId + tenantDomain;
        }
        return cartridgeAlias;
    }

	@Override
	public void generateDeployableFile(String rootPath, String applicationId,
	                                   String version, String stage) throws AppFactoryException {
		log.warn("This method is not supported in Abstract Application Type Processor");
	}
}
