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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.deploy.ApplicationDeployer;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.util.Map;

/**
 * The deployer that deploys the code during app creation. During app creation we keep an already
 * built code and commit it.
 */
public class InitialArtifactDeployer extends AbstractStratosDeployer {

	private static final Log log = LogFactory.getLog(InitialArtifactDeployer.class.getName());

	private Map<String, String[]> parameters;

	public InitialArtifactDeployer(Map<String, String[]> parameters, int tenantId, String tenantDomain) {
		this.tenantID = tenantId;
		this.tenantDomain = tenantDomain;
		this.parameters = parameters;
	}

//	@Override
//	public void run() {
//		try {
//			deployLatestSuccessArtifact(parameters);
//		} catch (AppFactoryException e) {
//			log.error("Deploying the initial artifact failed for application " +
//			          DeployerUtil.getParameter(parameters, AppFactoryConstants.APPLICATION_ID));
//		}
//	}

	protected File[] getLastBuildArtifact(String path, String extension, String stage, String applicationId, boolean isForLabel) throws AppFactoryException {
		return getArtifact(path, extension, stage, applicationId, isForLabel);
	}

	protected String getBaseRepoUrl() throws AppFactoryException {
		return AppFactoryUtil.getAppfactoryConfiguration().
				getFirstProperty(AppFactoryConstants.PAAS_ARTIFACT_REPO_PROVIDER_BASE_URL);
	}

	@Override
	protected String getAdminPassword() throws AppFactoryException {
		return AppFactoryUtil.getAppfactoryConfiguration().
				getFirstProperty(AppFactoryConstants. PAAS_ARTIFACT_REPO_PROVIDER_ADMIN_PASSWORD);
	}

	@Override
	protected String getStratosServerURL() throws AppFactoryException {
		return null;
	}

	@Override
	protected String getAdminUserName() throws AppFactoryException {
		return AppFactoryUtil.getAppfactoryConfiguration().
				getFirstProperty(AppFactoryConstants.PAAS_ARTIFACT_REPO_PROVIDER_ADMIN_USER_NAME);
	}

    @Override
    public String getSuccessfulArtifactTempStoragePath(String applicationId,
                                                       String applicationVersion,
                                                       String artifactType, String stage,
                                                       String tenantDomain,
                                                       String jobName)
            throws AppFactoryException {

        String dirPath =
                getTempPath(tenantDomain) + File.separator + applicationId + "_deploy_artifact" + File.separator;
        return dirPath;

    }

    @Override
    public String getArtifactStoragePath(String applicationId, String applicationVersion,
                                         String artifactType, String stage,
                                         String tenantDomain) throws AppFactoryException {
        String dirpath =
                getTempPath(tenantDomain) + File.separator + applicationId + "_deploy_artifact" + File.separator;
        return dirpath;
    }

	@Override
	public String getStoragePath(String tenantDomain) {
        // This method doesn't applicable to InitialArtifactDeployer
		throw new IllegalStateException();
	}

    @Override
    public String getTempPath(String tenantDomain) {
        return CarbonUtils.getTmpDir() + File.separator + "create" + File.separator + tenantDomain;
    }

	@Override
	public void postDeploymentNoifier(String message, String applicationId,
	                                            String applicationVersion, String artifactType,
	                                            String stage, String tenantDomain) {
		try {
			//tenant domain is null -- set it
			ApplicationDeployer deployer = new ApplicationDeployer();
			deployer.updateDeploymentInformation(applicationId, stage, applicationVersion, "0", tenantDomain);
		} catch (AppFactoryException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void deployTaggedArtifact(Map<String, String[]> map) throws Exception {
		throw new UnsupportedOperationException("Tags are not supported");
	}


}
