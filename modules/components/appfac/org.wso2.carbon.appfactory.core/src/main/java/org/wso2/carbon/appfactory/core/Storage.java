/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
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
 */

package org.wso2.carbon.appfactory.core;

import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.util.AppFactoryCoreUtil;

/**
 * 
 * This class specifies the common behaviors to deployment from storage.
 *
 */
public abstract class Storage {

    /**
     * Deploys an artifact to a given stage.
     * 
     * @param applicationId Application Id
     * @param version Application Version
     * @param revision This is a deprecated parameter
     * @param artifactType type of the artifact ( corresponds to application type)
     * @param stage the target stage
     * @param tenantDomain Tenant domain
     * @param userName Name of the use whose doing the deployment
     * @throws AppFactoryException
     */
	public void deployArtifact(String applicationId, String version, String revision, String artifactType,
            String stage, String tenantDomain, String userName, String deployAction) throws AppFactoryException {
		
	 
		if (AppFactoryUtil.isInitialLifeCycleStage(stage) || AppFactoryCoreUtil.isUplodableAppType(artifactType)){
			
			deployLatestSuccessArtifact(applicationId, version, revision, artifactType, stage, tenantDomain, userName,
			                            deployAction);
		}else {
			deployPromotedArtifact(applicationId, version, revision, artifactType, stage, tenantDomain, userName);
		}
	}
	
	
	/**
	 * Currently not use this tag base deployment
	 * 
	 * @param applicationId
	 * @param version
	 * @param revision
	 * @param tenantDomain
	 * @return
	 * @throws AppFactoryException
	 */
	public abstract String[] getTagNamesOfPersistedArtifacts(String applicationId, String version, String revision,
	                                                String tenantDomain) throws AppFactoryException;

	/**
	 * Deploy artifact that available in given storage
	 * 
	 * 
	 * @param applicationId
	 * @param version
	 * @param revision
	 * @param artifactType
	 * @param stage
	 * @param tenantDomain
	 * @param userName
	 * @param deployAction
	 * @throws AppFactoryException
	 */
	protected abstract void deployLatestSuccessArtifact(String applicationId, String version, String revision, String artifactType,
	                                        String stage, String tenantDomain, String userName, String deployAction)
	                                                                                                                throws AppFactoryException;
	/**
	 * Deploying promoted artifacts.
	 * 
	 * 
	 * @param applicationId
	 * @param version
	 * @param revision
	 * @param artifactType
	 * @param stage
	 * @param tenantDomain
	 * @param userName
	 * @throws AppFactoryException
	 */
	protected abstract void deployPromotedArtifact(String applicationId, String version, String revision, String artifactType,
	                                   String stage, String tenantDomain, String userName) throws AppFactoryException;

}
