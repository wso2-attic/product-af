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

package org.wso2.carbon.appfactory.nonbuild;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeManager;
import org.wso2.carbon.appfactory.core.deploy.ApplicationDeployer;
import org.wso2.carbon.appfactory.deployers.AbstractStratosDeployer;
import org.wso2.carbon.appfactory.deployers.util.DeployerUtil;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryManager;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryMgtException;
import org.wso2.carbon.appfactory.repository.mgt.client.AppfactoryRepositoryClient;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.util.Map;

/**
 * <p>Implements how a non-buildable artifact needs to be deployed in Stratos. 
 * This class has the knowledge about how to collect (non-buildable) artifacts from the file system.</p>
 * 
 * <b>Note</b>:Logic in this class is meant to be executed in Appfactory VM ( as oppose to executing on Build server. i.e. Jenkins).
 */
public class NonBuildableArtifactDeployer extends AbstractStratosDeployer {

	private static Log log = LogFactory.getLog(NonBuildableArtifactDeployer.class);
    /**
     * {@inheritDoc}
     */
	@Override
	public void deployLatestSuccessArtifact(Map<String, String[]> parameters) throws AppFactoryException {

		String applicationId = DeployerUtil.getParameter(parameters, AppFactoryConstants.APPLICATION_ID);
		String stageName = DeployerUtil.getParameter(parameters, AppFactoryConstants.DEPLOY_STAGE);
		String artifactType = DeployerUtil.getParameter(parameters, AppFactoryConstants.ARTIFACT_TYPE);
		String version = DeployerUtil.getParameter(parameters, AppFactoryConstants.APPLICATION_VERSION);
		String jobName = DeployerUtil.getParameter(parameters, AppFactoryConstants.JOB_NAME);

		String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
		int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
		super.setTenantDomain(tenantDomain);
		super.setTenantID(tenantId);
		super.setTempPath(getArtifactTempPath(applicationId, version, artifactType, stageName, tenantDomain));
		super.setStoragePath(getArtifactStoragePath(applicationId, version, artifactType, stageName, tenantDomain));

		String tmpStgPath = getSuccessfulArtifactTempStoragePath(applicationId, version, artifactType, stageName,
		                                                         tenantDomain,jobName);
		loadRepository(tmpStgPath, applicationId, version, tenantDomain);
		ApplicationTypeManager.getInstance().getApplicationTypeBean(artifactType).
				getProcessor().generateDeployableFile(tmpStgPath, applicationId, version, stageName);
		super.deployLatestSuccessArtifact(parameters);
	}

	@Override
	public String getStoragePath(String tenantDomain) {
		//  TODO: do necessary changes to get storage path
		return super.getStoragePath();
	}

	@Override
	public String getTempPath(String s) {
		//  TODO: do necessary changes to get temp path
		return super.getTempPath();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getBaseRepoUrl() throws AppFactoryException {
		return AppFactoryUtil.getAppfactoryConfiguration().
				getFirstProperty(AppFactoryConstants.PAAS_ARTIFACT_REPO_PROVIDER_BASE_URL);
	}

    /**
     * {@inheritDoc}
     */
	@Override
	protected String getAdminPassword() throws AppFactoryException {
		return AppFactoryUtil.getAppfactoryConfiguration().
				getFirstProperty(AppFactoryConstants.PAAS_ARTIFACT_REPO_PROVIDER_ADMIN_PASSWORD);
	}

    /**
     * {@inheritDoc}
     */
	@Override
	protected String getAdminUserName() throws AppFactoryException {
		return AppFactoryUtil.getAppfactoryConfiguration().
				getFirstProperty(AppFactoryConstants.PAAS_ARTIFACT_REPO_PROVIDER_ADMIN_USER_NAME);
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public String getSuccessfulArtifactTempStoragePath(String applicationId, String applicationVersion,
	                                                   String artifactType, String stage, String tenantDomain, String jobname)
			throws AppFactoryException {
		String carbonHome = CarbonUtils.getCarbonHome();
		String path = carbonHome + File.separator + "nonbuildstorage" + File.separator + "appfactory" + File.separator +
		                      tenantDomain + File.separator + stage + File.separator + applicationId + "-" +
		                      applicationVersion;
        if(log.isDebugEnabled()){
            log.debug("Successful artifact storage path:"+path);
        }
		return path;
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public String getArtifactStoragePath(String applicationId, String applicationVersion, String artifactType,
	                                     String stage, String tenantDomain) throws AppFactoryException {
		String carbonHome = CarbonUtils.getCarbonHome();
		String path = carbonHome + File.separator + "nonbuildstorage" + File.separator + "s2storage" + File.separator +
		                      tenantDomain + File.separator + applicationId;
        if(log.isDebugEnabled()){
            log.debug("Artifact storage path:"+path);
        }
		return path;
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public void postDeploymentNoifier(String message, String applicationId, String applicationVersion,
	                                  String artifactType, String stage, String tenantDomain) {
		ApplicationDeployer applicationDeployer = new ApplicationDeployer();
		try {
			applicationDeployer.updateDeploymentInformation(applicationId, stage, applicationVersion, "", tenantDomain);
		} catch (AppFactoryException e) {
			String errMsg = "Error when call notifier : " + e.getMessage();
			log.error(errMsg, e);
		}

	}
	
	
	/**
	 * This is regarding get the artifact store temp path
	 * 
	 * @param applicationId
	 * @param applicationVersion
	 * @param artifactType
	 * @param stage
	 * @param tenantDomain
	 * @return
	 * @throws AppFactoryException
	 */
	private String getArtifactTempPath(String applicationId, String applicationVersion, String artifactType, String stage,
	                          String tenantDomain) throws AppFactoryException {
		String carbonHome = CarbonUtils.getCarbonHome();
		String path = carbonHome + File.separator + "nonbuildstorage" + File.separator + "s2tmp" + File.separator +
		                      tenantDomain;
        if(log.isDebugEnabled()){
            log.debug("Artifact temp path:"+path);
        }
		return path;
	}

	@Override
    public void deployTaggedArtifact(Map<String, String[]> requestParameters) throws Exception {
	    // TODO Auto-generated method stub
	    
    }

    @Override
    protected void deploy(String artifactType, File[] artifactsToDeploy, Map<String, String[]> parameters, Boolean notify) throws AppFactoryException {
        String applicationId = DeployerUtil.getParameter(parameters, AppFactoryConstants.APPLICATION_ID);
        String stageName = DeployerUtil.getParameter(parameters, AppFactoryConstants.DEPLOY_STAGE);
        String version = DeployerUtil.getParameter(parameters, AppFactoryConstants.APPLICATION_VERSION);

        String tenantDomain = getTenantDomain();
        super.setTempPath(getArtifactTempPath(applicationId, version, artifactType, stageName, tenantDomain));
        super.deploy(artifactType, artifactsToDeploy, parameters, notify);
    }

    /**
     * This method can be used to deploy a promoted artifact to a stage
     * We have used this method after first promote action of an application, so the artifact
     * deployed in first promote action will be deployed in the next promote action
     */
    @Override
    public void deployPromotedArtifact(Map<String, String[]> parameters) throws Exception {
        String applicationId = DeployerUtil.getParameter(parameters, AppFactoryConstants.APPLICATION_ID);
        String stageName = DeployerUtil.getParameter(parameters, AppFactoryConstants.DEPLOY_STAGE);
        String artifactType = DeployerUtil.getParameter(parameters, AppFactoryConstants.ARTIFACT_TYPE);
        String version = DeployerUtil.getParameter(parameters, AppFactoryConstants.APPLICATION_VERSION);

        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        super.setTenantDomain(tenantDomain);
        super.setTenantID(tenantId);
        super.setTempPath(getArtifactTempPath(applicationId, version, artifactType, stageName, tenantDomain));
        super.setStoragePath(getArtifactStoragePath(applicationId, version, artifactType, stageName, tenantDomain));
        super.deployPromotedArtifact(parameters);
    }

	/**
	 * Checkout repository from given location.
	 *
	 * @param rootPath location for checkout
	 * @param applicationId application id of the application
	 * @param version version of the application
	 * @param tenantDomain current tenant domain
	 * @throws AppFactoryException
	 */
	private void loadRepository(String rootPath, String applicationId, String version, String tenantDomain)
			throws AppFactoryException {
		try {
			File filePath = new File(rootPath + File.separator + AppFactoryConstants.AF_GIT_TMP_FOLDER);
			if (!filePath.exists()) {
				if(!filePath.mkdirs()){
					// try to create directory again.
					if(!filePath.mkdir()){
						throw new AppFactoryException("Failed to create directory:" + filePath.getAbsolutePath());
					}
				}
			}
			AppfactoryRepositoryClient appfactoryGitClient =
					(new RepositoryManager()).getRepositoryProvider(AppFactoryConstants.GIT_REPOSITORY_TYPE).getRepositoryClient();
			AppFactoryConfiguration appfactoryConfiguration = AppFactoryUtil.getAppfactoryConfiguration();
			appfactoryGitClient.init(
					appfactoryConfiguration.getFirstProperty(AppFactoryConstants.PAAS_ARTIFACT_REPO_PROVIDER_ADMIN_PASSWORD),
					appfactoryConfiguration.getFirstProperty(AppFactoryConstants.PAAS_ARTIFACT_REPO_PROVIDER_ADMIN_USER_NAME));
			RepositoryManager repositoryManager = new RepositoryManager();
			String appfactoryGitRepoURL = repositoryManager.getURLForAppversion(applicationId, version,
			                                                                    AppFactoryConstants.GIT_REPOSITORY_TYPE,
			                                                                    tenantDomain, false, "");
			appfactoryGitClient.checkOut(appfactoryGitRepoURL, version, filePath);
		} catch (RepositoryMgtException e) {
			throw new AppFactoryException(e);
		}
	}
}
