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

package org.wso2.carbon.appfactory.deployers;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.Deployer;
import org.wso2.carbon.appfactory.deployers.build.api.BuildStatusProvider;
import org.wso2.carbon.appfactory.deployers.util.DeployerUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 
 * This abstract class is used to handle the deployment pattern.
 * 
 * ex : deploy latest artifact, promoting a application or label last success as
 * PROMOTED. There are few more method should implement by extending to get done
 * some deployment using this abstraction.
 * 
 */
public abstract class AbstractDeployer implements Deployer {

	private static final Log log = LogFactory.getLog(AbstractDeployer.class);
	private static final String FREE_STYLE_APP_TYPE_FILE_PATH = "archive";

	protected String adminUserName;
	protected String adminPassword;
	protected String appfactoryServerURL;
	protected String storagePath;
	protected String tempPath;
	protected BuildStatusProvider buildStatusProvider;
	// reason for storing tenant domain and id is because jenkins based
	// deployers should be able to work on non carbon cartridges.
	protected String tenantDomain;
	protected int tenantID;

	/**
	 * This method can be used to deploy a promoted artifact to a required stage. This method can be used after first
	 * promote action of an application, so the artifact deployed in first promote action will be deployed in the next
	 * promote action.
	 *
	 * @param parameters Map of parameters which are used by deployment
	 * @throws Exception If there is an issue in deploying of promoted artifacts
	 */
	public void deployPromotedArtifact(Map<String, String[]> parameters)
			throws Exception {

		String artifactType = DeployerUtil.getParameter(parameters,
				AppFactoryConstants.ARTIFACT_TYPE);
		String applicationId = DeployerUtil.getParameter(parameters,
				AppFactoryConstants.APPLICATION_ID);
		String stageName = DeployerUtil.getParameter(parameters,
				AppFactoryConstants.DEPLOY_STAGE);
		String version = DeployerUtil.getParameter(parameters,
				AppFactoryConstants.APPLICATION_VERSION);
		String extension = DeployerUtil.getParameter(parameters,
		        AppFactoryConstants.APPLICATION_EXTENSION);
		String tenantDomain = DeployerUtil.getParameter(parameters, AppFactoryConstants.TENANT_DOMAIN);
		String pathToPromotedArtifact = getArtifactStoragePath(applicationId,
				version, artifactType, stageName, tenantDomain);

		File[] fileToDeploy = getArtifact(pathToPromotedArtifact, extension, stageName, applicationId, false);

		deploy(artifactType, fileToDeploy, parameters, false);
	}

    /**
     * This will deploy the latest successfully built artifact of the given job
     *
     * @param parameters Map of parameters which are used by deployment
     * @throws AppFactoryException If there is an issue in deploying latest success artifacts
     */
    public void deployLatestSuccessArtifact(Map<String, String[]> parameters)
			throws AppFactoryException {

		String applicationId = DeployerUtil.getParameter(parameters,
				AppFactoryConstants.APPLICATION_ID);
		String stageName = DeployerUtil.getParameter(parameters,
				AppFactoryConstants.DEPLOY_STAGE);
		String deployAction = DeployerUtil.getParameter(parameters,
				AppFactoryConstants.DEPLOY_ACTION);
		String artifactType = DeployerUtil.getParameter(parameters,
				AppFactoryConstants.ARTIFACT_TYPE);
		String version = DeployerUtil.getParameter(parameters,
				AppFactoryConstants.APPLICATION_VERSION);
		String tenantDomain = DeployerUtil.getParameter(parameters, AppFactoryConstants.TENANT_DOMAIN);
		String extension = DeployerUtil.getParameter(parameters,
		        AppFactoryConstants.APPLICATION_EXTENSION);
	    String jobName = DeployerUtil.getParameter(parameters, AppFactoryConstants.JOB_NAME);

		try {
			String path = this.getSuccessfulArtifactTempStoragePath(applicationId, version, artifactType, stageName,
			                                                        tenantDomain, jobName);

			File[] artifactToDeploy = getLastBuildArtifact(path, extension, stageName, applicationId, false);

			if (AppFactoryConstants.DEPLOY_ACTION_LABEL_ARTIFACT.equalsIgnoreCase(deployAction)) {
				deploy(artifactType, artifactToDeploy, parameters, true);
				log.debug("Making last successful build as PROMOTED");
				labelLastSuccessAsPromoted(applicationId, version, artifactType, stageName, tenantDomain, extension, jobName);
			} else {
				deploy(artifactType, artifactToDeploy, parameters, true);
			}

		} catch (Exception e) {
			String errMsg = "Error when calling deployLatestSuccessArtifact "
					+ e.getMessage();
			log.error(errMsg, e);
			throw new AppFactoryException(errMsg, e);
		}
	}

	/**deploy
	 * Handling Exception under this class root
	 * 
	 * @param msg
	 * @throws AppFactoryException
	 */
	protected void handleException(String msg) throws AppFactoryException {
		log.error(msg);
		throw new AppFactoryException(msg);
	}

	/**
	 * Handling Exception under this class root
	 * 
	 * @param msg
	 * @throws AppFactoryException
	 */
	public void handleException(String msg, Exception e)
			throws AppFactoryException {
		log.error(msg, e);
		throw new AppFactoryException(msg, e);
	}

    /**
     * This method will be used to retrieve the artifact in the given path filtered by extension.
     *
     * @param path      The path were artifact has been stored
     * @param extension Artifact file extension
     * @return Array of artifacts as Files
     * @throws AppFactoryException If there is an issue in filtering artifacts
     */
    protected File[] getArtifact(String path, String extension, String stage, String applicationId, boolean isForLabel) throws AppFactoryException {
        List<File> fileList = new ArrayList<File>();
        if (StringUtils.isNotBlank(path)) {
            if (StringUtils.isBlank(extension)) {
                fileList.addAll(getArtifactDirectories(path));
            } else {
                // listFiles method always return 'File' type Collection, hence warning is suppressed
				@SuppressWarnings("unchecked")
				String[] fileExtensions = new String[]{extension};
				fileList.addAll(FileUtils.listFiles(new File(path), fileExtensions, true));
            }
            if (fileList.isEmpty()) {
                String errMsg = "No built artifact found in the path : " + path;
                log.error(errMsg);
                throw new AppFactoryException(errMsg);
            }
        }
        return fileList.toArray(new File[fileList.size()]);
    }

    /**
     * Gives the directory type artifacts in the given path
     *
     * @param path Path of artifact directory
     * @return List of directories in the given path
     */
    private List<File> getArtifactDirectories(String path) {
        List<File> result = new ArrayList<File>();
        if (StringUtils.isNotBlank(path)) {
            File parentDir = new File(path);
            File[] fileList = parentDir.listFiles();
            if (fileList != null) {
                for (File dir : fileList) {
                    if (dir.isDirectory()) {
                        result.add(dir.getAbsoluteFile());
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns the artifacts related to last build location.
     *
     * @param path      the path were artifact has been stored
     * @param extension artifact file extension
     * @return Array of artifacts as Files
     * @throws AppFactoryException If there is an issue in filtering last build artifacts
     */
    protected File[] getLastBuildArtifact(String path, String extension, String stage, String applicationId, boolean isForLabel) throws AppFactoryException {
	    if (StringUtils.isBlank(extension)) {
		    path = path + File.separator + FREE_STYLE_APP_TYPE_FILE_PATH;
	    }
	    return getArtifact(path, extension, stage, applicationId, isForLabel);
    }

	/**
	 * Method labels the last successful build as PROMOTED by copying last
	 * successful build into PROMOTED location.
	 *
	 * @param artifactType  Type of the artifact
	 * @param extension Extension of the artifact
	 * @throws AppFactoryException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void labelLastSuccessAsPromoted(String applicationId, String version, String artifactType, String stage,
	                                       String tenantDomain, String extension, String jobName)
			throws AppFactoryException, IOException, InterruptedException {

		String lastSucessBuildFilePath = getSuccessfulArtifactTempStoragePath(
				applicationId, version, artifactType, stage, tenantDomain, jobName);
		log.debug("Last success build path is :" + lastSucessBuildFilePath);

		String dest = getArtifactStoragePath(applicationId, version,
				artifactType, stage, tenantDomain);

		File destDir = new File(dest);
		if (destDir.exists()) {
			FileUtils.cleanDirectory(destDir.getParentFile());
		}
		if (!destDir.mkdirs()) {
			log.error("Unable to create promoted tag for application :"
					+ applicationId + ", version :" + version);
			throw new AppFactoryException(
					"Error occured while creating dir for last successful as PROMOTED: application :"
							+ applicationId + ", version :" + version);
		}

		File[] lastSucessFiles = getLastBuildArtifact(lastSucessBuildFilePath, extension, stage, applicationId, true);
		for (File lastSucessFile : lastSucessFiles) {
			File lastSuccessArtifactPath = lastSucessFile;
			String promotedDestDirPath = destDir.getAbsolutePath();

			File promotedDestDir = new File(promotedDestDirPath);
			if (!promotedDestDir.exists()) {
				promotedDestDir.mkdirs();
			}

			File destinationFile = new File(promotedDestDir.getAbsolutePath()
					+ File.separator + lastSuccessArtifactPath.getName());

			if (lastSuccessArtifactPath.isDirectory()) {
				FileUtils.copyDirectory(lastSuccessArtifactPath,
						destinationFile);
			} else {
				FileUtils.copyFile(lastSuccessArtifactPath, destinationFile);
			}
			log.debug("labeled the lastSuccessful as PROMOTED");
		}
	}

	protected String getAdminUsername() {
		return adminUserName;
	}

	protected String getAdminUsername(String applicationId) {
		return adminUserName + "@" + applicationId;
	}

	protected String getServerAdminPassword() {
		return adminPassword;
	}

	public void setAdminUserName(String adminUserName) {
		this.adminUserName = adminUserName;
	}

	public void setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
	}

	public void setAppfactoryServerURL(String appfactoryServerURL) {
		this.appfactoryServerURL = appfactoryServerURL;
	}

	public void setStoragePath(String storagePath) {
		this.storagePath = storagePath;
	}

	@Deprecated
	public String getStoragePath() {
		//TODO: remove this method when removing the org.wso2.carbon.appfactory.nonbuild.NonBuildableArtifactDeployer
		return this.storagePath;
	}

	/**
	 * Storage path wrt {@code tenantDomain}
	 * @param tenantDomain tenant domain
	 * @return storage path
	 */
	public abstract String getStoragePath(String tenantDomain);

	public void setTempPath(String tempPath) {
		this.tempPath = tempPath;
	}

	@Deprecated
	public String getTempPath() {
		//TODO: remove this method when removing the org.wso2.carbon.appfactory.nonbuild.NonBuildableArtifactDeployer
		return this.tempPath;
	}

	/**
	 * Temp path wrt {@code tenantDomain}
	 * @param tenantDomain tenant domain
	 * @return temp path
	 */
	public abstract String getTempPath(String tenantDomain);

	public int getTenantID() {
		return tenantID;
	}

	public void setTenantID(int tenantID) {
		this.tenantID = tenantID;
	}

	public String getTenantDomain() {
		return tenantDomain;
	}

	public void setTenantDomain(String tenantDomain) {
		this.tenantDomain = tenantDomain;
	}

	public abstract String getSuccessfulArtifactTempStoragePath(String applicationId, String applicationVersion,
	                                                            String artifactType, String stage, String tenantDomain,
	                                                            String jobName) throws AppFactoryException;

	public abstract String getArtifactStoragePath(String applicationId,
			String applicationVersion, String artifactType, String stage,
			String tenantDomain) throws AppFactoryException;

	public abstract void postDeploymentNoifier(String message,
			String applicationId, String applicationVersion,
			String artifactType, String stage, String tenantDomain);

	protected abstract void deploy(String artifactType,
			File[] artifactsToDeploy, Map<String, String[]> parameters,
			Boolean notify) throws AppFactoryException;
}
