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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.Deployer;
import org.wso2.carbon.appfactory.core.Storage;

/**
 * 
 * This class is a storage implementation for non-buildable artifacts.
 * 
 */
public class NonBuildableStorage extends Storage {

	private static Log log = LogFactory.getLog(NonBuildableStorage.class);

	@Override
	public String[] getTagNamesOfPersistedArtifacts(String applicationId, String version, String revision,
	                                                String tenantDomain) throws AppFactoryException {
		// Currently we are not support tag artifacts.
		return null;
	}

	@Override
	public void deployLatestSuccessArtifact(String applicationId, String version, String revision, String artifactType,
	                                        String stage, String tenantDomain, String userName, String deployAction,
	                                        String repoFrom)
			throws AppFactoryException {

		try {
            Deployer deployer = new NonBuildableArtifactDeployer();
			Map<String, String[]> paramList = new HashMap<String, String[]>();

			paramList.put(AppFactoryConstants.APPLICATION_ID, new String[] { applicationId });
			paramList.put(AppFactoryConstants.APPLICATION_VERSION, new String[] { version });
			paramList.put(AppFactoryConstants.ARTIFACT_TYPE, new String[] { artifactType });
			paramList.put(AppFactoryConstants.DEPLOY_STAGE, new String[] { stage });
			paramList.put(AppFactoryConstants.DEPLOY_ACTION, new String[] { deployAction });
			paramList.put(AppFactoryConstants.TENANT_DOMAIN, new String[] { tenantDomain });
			paramList.put(AppFactoryConstants.USER_NAME, new String[] { userName });

			deployer.deployLatestSuccessArtifact(paramList);

		} catch (Exception e) {
			String errMsg = "Error when do the deploy the artifact : " + e.getMessage();
			log.error(errMsg, e);
			throw new AppFactoryException(errMsg, e);
		}

	}

	@Override
	public void deployPromotedArtifact(String applicationId, String version, String revision, String artifactType,
	                                   String stage, String tenantDomain, String userName) throws AppFactoryException {
		try {
            Deployer deployer = new NonBuildableArtifactDeployer();
            Map<String, String[]> paramList = new HashMap<String, String[]>();

			paramList.put(AppFactoryConstants.APPLICATION_ID, new String[] { applicationId });
			paramList.put(AppFactoryConstants.APPLICATION_VERSION, new String[] { version });
			paramList.put(AppFactoryConstants.ARTIFACT_TYPE, new String[] { artifactType });
			paramList.put(AppFactoryConstants.DEPLOY_STAGE, new String[] { stage });
			paramList.put(AppFactoryConstants.TENANT_DOMAIN, new String[] { tenantDomain });
			paramList.put(AppFactoryConstants.USER_NAME, new String[] { userName });

			deployer.deployPromotedArtifact(paramList);
		} catch (Exception e) {
			String errMsg = "Error when do the deploy the promoted artifact : " + e.getMessage();
			log.error(errMsg, e);
			throw new AppFactoryException(errMsg, e);
		}
	}

}
