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

package org.wso2.carbon.appfactory.application.mgt.type;

import org.apache.axiom.om.OMElement;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.application.mgt.type.validator.war.WarValidationException;
import org.wso2.carbon.appfactory.application.mgt.type.validator.war.WarValidator;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeManager;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeValidationStatus;
import org.wso2.carbon.appfactory.utilities.project.ProjectUtils;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Application processor for uploaded application type eg:- war files
 * 
 *
 */
public class UploadedApplicationTypeProcessor extends AbstractApplicationTypeProcessor {

	private static Log log = LogFactory.getLog(UploadedApplicationTypeProcessor.class);

    public UploadedApplicationTypeProcessor(String type) {
        super(type);
    }

    @Override
	public void doVersion(String applicationID, String targetVersion, String currentVersion,
	                      String workingDirectory) throws AppFactoryException {

		try {
			String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
			String applicationExtenstion = ProjectUtils.getApplicationExtenstion(applicationID, tenantDomain);
			String uploadedFileName = applicationID + "-" + targetVersion + "." + applicationExtenstion
			                          + AppFactoryConstants.UPPLOADABLE_SUFFIX;

	        copyUploadedAppToLocation(uploadedFileName, workingDirectory);
	        
	        if (log.isDebugEnabled()) {
				log.debug("Version creation hanlded for Uploaded application type with application key -" + applicationID);
			}
	        
		} catch (IOException e) {
			log.error(e);
			throw new AppFactoryException("Error when creating version of uploaded application", e);
		}

	}

    @Override
	public void generateApplicationSkeleton(String applicationID, String workingDirectory)
			throws AppFactoryException {

		try {
			String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
			String applicationExtenstion =
					ProjectUtils.getApplicationExtenstion(applicationID,
					                                      tenantDomain);
			String uploadedFileName = applicationID + "-1.0.0." + applicationExtenstion + AppFactoryConstants.UPPLOADABLE_SUFFIX;
            copyUploadedAppToLocation(uploadedFileName , workingDirectory);
			
			if (log.isDebugEnabled()) {
				log.debug("Application skeleton creation hanlded for Uploaded application type with application key -" + applicationID);
			}
		} catch (IOException e) {
			log.error(e);
			throw new AppFactoryException("Error when generating uploaded application skeleton", e);
		}
	}

	@Override
    public List<File> getPreVersionDeleteableFiles(String applicationID, String targetVersion,
                                                   String currentVersion, String workingDir) throws AppFactoryException {
		String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
		String applicationExtension;
		applicationExtension = ProjectUtils.getApplicationExtenstion(applicationID, tenantDomain);
		List<File> deletableFiles = new ArrayList<File>();
		deletableFiles.add(new File(
				CarbonUtils.getTmpDir() + File.separator + AppFactoryConstants.REPOSITORY_BRANCH + File.separator +
				applicationID + File.separator + targetVersion + File.separator + applicationID +
				AppFactoryConstants.MINUS + AppFactoryConstants.INITIAL_UPLOADED_APP_VERSION + AppFactoryConstants.DOT_SEPERATOR +
				applicationExtension));
		return deletableFiles;
	}

	@Override
	public OMElement configureBuildJob(OMElement jobConfigTemplate, Map<String, String> parameters,
	                                   String projectType) throws AppFactoryException {
		if (jobConfigTemplate == null) {
			String msg =
					"Class loader is unable to find the jenkins job configuration template for uploadable application types";
			log.error(msg);
			throw new AppFactoryException(msg);
		}

		String artifactArchiver = null;
		Object hudsonArtifactArchiver = ApplicationTypeManager.getInstance().getApplicationTypeBean(projectType)
		                                                      .getProperty(
				                                                      AppFactoryConstants.HUDSON_ARTIFACT_ARCHIVER);
		if (hudsonArtifactArchiver != null) {
			artifactArchiver = hudsonArtifactArchiver.toString();
		}
		jobConfigTemplate = configureRepositoryData(jobConfigTemplate, parameters);

		// Support for post build listener residing in jenkins server
		setValueUsingXpath(jobConfigTemplate,
		                   AppFactoryConstants.PUBLISHERS_APPFACTORY_POST_BUILD_APP_EXTENSION_XPATH_SELECTOR,
		                   parameters.get(AppFactoryConstants.APPTYPE_EXTENSION));

		setValueUsingXpath(jobConfigTemplate,
		                   AppFactoryConstants.PUBLISHERS_APPFACTORY_POST_BUILD_APP_ID_XPATH_SELECTOR,
		                   parameters.get(AppFactoryConstants.APPLICATION_ID));

		setValueUsingXpath(jobConfigTemplate,
		                   AppFactoryConstants.PUBLISHERS_APPFACTORY_POST_BUILD_APP_VERSION_XPATH_SELECTOR,
		                   parameters.get(AppFactoryConstants.APPLICATION_VERSION));

		if (StringUtils.isNotBlank(artifactArchiver)) {
			setValueUsingXpath(jobConfigTemplate,
			                   AppFactoryConstants.ARTIFACT_ARCHIVER_CONFIG_NAME_XAPTH_SELECTOR,
			                   artifactArchiver);
		}

		if (ApplicationTypeManager.getInstance().getApplicationTypeBean(projectType).isUploadableAppType()) {
			setValueUsingXpath(jobConfigTemplate, AppFactoryConstants.ARTIFACT_ARCHIVER_CONFIG_NAME_XAPTH_SELECTOR,
			                   AppFactoryConstants.DEFAULT_ARTIFACT_NAME +
			                   ApplicationTypeManager.getInstance().getApplicationTypeBean(projectType)
			                                         .getExtension());

			String repositoryBranchName = parameters.get(AppFactoryConstants.APPLICATION_VERSION);
			if (AppFactoryConstants.INITIAL_UPLOADED_APP_VERSION.equals(repositoryBranchName)) {
				setValueUsingXpath(jobConfigTemplate, AppFactoryConstants.GIT_REPOSITORY_VERSION_XPATH_SELECTOR,
				                   AppFactoryConstants.APPLICATION_VERSION_VALUE_FREESTYLE);
			}
		}

		setValueUsingXpath(jobConfigTemplate,
		                   AppFactoryConstants.APPLICATION_TRIGGER_PERIOD,
		                   parameters.get(AppFactoryConstants.APPLICATION_POLLING_PERIOD));

		return jobConfigTemplate;
	}

    private void copyUploadedAppToLocation(String uploadedFileName, String workingDirectory) throws IOException {
        File sourceFile =
                new File(getUploadedApplicationTmpPath() + File.separator +
                         uploadedFileName);
        copyUploadedAppToRepositoryLocation(sourceFile, uploadedFileName, workingDirectory);
        copyUplodedAppToDepolyArtifactLocation(sourceFile, uploadedFileName, workingDirectory);
        FileUtils.forceDelete(sourceFile);
    }

    private void copyUploadedAppToRepositoryLocation(File sourceFile, String uploadedFileName, String workingDirectory)
            throws IOException {
        File desFile = new File(workingDirectory + File.separator +
                                uploadedFileName.replace(AppFactoryConstants.UPPLOADABLE_SUFFIX, ""));
        FileUtils.copyFile(sourceFile, desFile);

        if (log.isDebugEnabled()) {
            log.debug("Uploaded application file " + sourceFile.getName() + " successfully copied to location - " +
                      desFile.getAbsolutePath());
        }
    }

    private void copyUplodedAppToDepolyArtifactLocation(File sourceFile, String uploadedFileName,
                                                        String deployArtifactLocation) throws IOException {
        File desFile = new File(deployArtifactLocation + "_deploy_artifact" + File.separator +
                                uploadedFileName.replace(AppFactoryConstants.UPPLOADABLE_SUFFIX, ""));
        FileUtils.copyFile(sourceFile, desFile);
        if (log.isDebugEnabled()) {
            log.debug("Copied uploaded application from " + sourceFile.getName() + " to deploy artifact location " +
                      desFile.getAbsolutePath());
        }
    }

    public String getUploadedApplicationTmpPath() {
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        return CarbonUtils.getCarbonRepository() + File.separator + "jaggeryapps/appmgt/" +
               AppFactoryConstants.UPLOADED_APPLICATION_TMP_FOLDER_NAME + File.separator + tenantDomain;
    }

	@Override
	public ApplicationTypeValidationStatus validate(String uploadedFileName) {
		WarValidator warValidator = new WarValidator(getUploadedApplicationTmpPath() + File.separator +
		                                             uploadedFileName);
		// Disabling servlet class validation since there is a issue with class loading.
		// Check WarValidator.validateClassUsingClassLoader() method
		warValidator.validateServletClasses(false);
		try {
			warValidator.execute();
			return new ApplicationTypeValidationStatus(true, "Successfully Validated!");
		} catch (WarValidationException e) {
			// Here we log as a warn since this is a user error
			String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
			log.warn("Apptype validation is failed for : " + uploadedFileName+", tenant domain : "+tenantDomain, e);
			return new ApplicationTypeValidationStatus(false, e.getMessage());
		}
	}

	@Override
	public void generateDeployableFile(String rootPath, String applicationId,
	                                   String version, String stage) throws AppFactoryException {
		String applicationExtenstion = ProjectUtils.getApplicationExtenstion(applicationId,
		                                                                     CarbonContext.getThreadLocalCarbonContext()
		                                                                                  .getTenantDomain());
		String artifactFileName = applicationId + AppFactoryConstants.APPFACTORY_ARTIFACT_NAME_VERSION_SEPERATOR
		                          + version + AppFactoryConstants.FILENAME_EXTENSION_SEPERATOR + applicationExtenstion;
		String uploadedAppSrcFile = rootPath + File.separator + AppFactoryConstants.AF_GIT_TMP_FOLDER + File.separator
		                            + artifactFileName;
		String uploadedApptmpFolder = rootPath + File.separator + AppFactoryConstants.DEPLOYABLE_ARTIFACT_FOLDER;
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
