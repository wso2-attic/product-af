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

package org.wso2.carbon.appfactory.nonbuild.artifact;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeManager;
import org.wso2.carbon.appfactory.jenkins.build.internal.ServiceContainer;
import org.wso2.carbon.appfactory.nonbuild.artifact.type.DSSDeployableArtifact;
import org.wso2.carbon.appfactory.nonbuild.artifact.type.JaggeryDeployableArtifact;
import org.wso2.carbon.appfactory.nonbuild.artifact.type.PHPDeployableArtifact;
import org.wso2.carbon.appfactory.nonbuild.artifact.type.UploadedApplicationDeployableArtifact;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryManager;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryMgtException;
import org.wso2.carbon.appfactory.repository.mgt.client.AppfactoryRepositoryClient;

import java.io.File;

/**
 * ArtifactGeneratorFactory is used to generate non-buildable artifact. Checkout the repository from given repository
 * location and pack it according to its own pattern.
 */
public class ArtifactGeneratorFactory {
	private static Log log = LogFactory.getLog(ArtifactGeneratorFactory.class);
	public static String appfactoryGitTmpFolder = "repository";
	public static String deployableAtrifactFolder = "artifact";

	private static ArtifactGeneratorFactory artifactGeneratorFactory = null;

	private ArtifactGeneratorFactory() {

	}

	/**
	 * @return
	 */
	public static ArtifactGeneratorFactory newInstance() {
		if (artifactGeneratorFactory == null) {
			synchronized (ArtifactGeneratorFactory.class) {
				artifactGeneratorFactory = new ArtifactGeneratorFactory();
			}
		}
		return artifactGeneratorFactory;
	}

	/**
	 * Generate deployable artifact
	 *
	 * @param rootPath
	 * @param applicationId
	 * @param version
	 * @param stage
	 * @param artifactType
	 * @param tenantDomain
	 * @return
	 * @throws AppFactoryException
	 */
	public DeployableArtifact generateDeployableArtifact(String rootPath, String applicationId,
	                                                     String version, String stage,
	                                                     String artifactType, String tenantDomain)
			throws AppFactoryException {

		DeployableArtifact deployableArtifact = null;

		File filePath = new File(rootPath + File.separator + appfactoryGitTmpFolder);
		if (!filePath.exists()) {
			if(!filePath.mkdirs()){
                // try to create directory again.
                if(!filePath.mkdir()){
                    throw new AppFactoryException("Failed to create directory:"+filePath.getAbsolutePath());
                }
            }
		}

		loadRepsitory(filePath, rootPath, applicationId, version, stage, artifactType, tenantDomain);

		AppFactoryConfiguration appFactoryConfiguration =
				ServiceContainer.getAppFactoryConfiguration();

		boolean isUploadableAppType =
				ApplicationTypeManager.getInstance().getApplicationTypeBean(artifactType).isUploadableAppType();

		// return deployable artifact
		if (isUploadableAppType) {
			deployableArtifact = new UploadedApplicationDeployableArtifact(rootPath, applicationId,
			                                                               version, stage);

		} else {
			/*
			if (AppFactoryConstants.APPLICATION_TYPE_JAGGERY.equals(artifactType)) {
				deployableArtifact =
						new JaggeryDeployableArtifact(rootPath, applicationId, version, stage);
			} else if (AppFactoryConstants.APPLICATION_TYPE_DBS.equals(artifactType)) {
				deployableArtifact =
						new DSSDeployableArtifact(rootPath, applicationId, version, stage);
			} else if (AppFactoryConstants.APPLICATION_TYPE_PHP.equals(artifactType)) {
				deployableArtifact =
						new PHPDeployableArtifact(rootPath, applicationId, version, stage);
			}*/
		}
		if (deployableArtifact == null) {
			String error = "Deployable artifact could not be generated for artifact type:" + artifactType +
			               " for application:" + applicationId + " for version:" + version + " in stage:" + stage +
			               " in tenant:" + tenantDomain;
			log.error(error);
			throw new AppFactoryException(error);
		}
		return deployableArtifact;
	}

	/**
	 * Checkout repository from given location.
	 *
	 * @param filePath
	 * @param rootPath
	 * @param applicationId
	 * @param version
	 * @param stage
	 * @param artifactType
	 * @param tenantDomain
	 * @throws AppFactoryException
	 */
	private void loadRepsitory(File filePath, String rootPath, String applicationId, String version, String stage,
	                           String artifactType, String tenantDomain) throws AppFactoryException {
		try {
			AppfactoryRepositoryClient appfactoryGitClient =
					(new RepositoryManager()).getRepositoryProvider("git").getRepositoryClient();
			AppFactoryConfiguration appfactoryConfiguration = AppFactoryUtil.getAppfactoryConfiguration();
			appfactoryGitClient.init(appfactoryConfiguration.getFirstProperty(
					                         AppFactoryConstants.PAAS_ARTIFACT_REPO_PROVIDER_ADMIN_PASSWORD),
			                         appfactoryConfiguration.getFirstProperty(
					                         AppFactoryConstants.PAAS_ARTIFACT_REPO_PROVIDER_ADMIN_USER_NAME));

			RepositoryManager repositoryManager = new RepositoryManager();
			String appfactoryGitRepoURL =
					repositoryManager.getURLForAppversion(applicationId, version, "git",
					                                      tenantDomain, false, "");
			appfactoryGitClient.checkOut(appfactoryGitRepoURL, version, filePath);

		} catch (RepositoryMgtException e) {
			throw new AppFactoryException(e);
		}
	}
}
