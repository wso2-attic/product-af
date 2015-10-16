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

import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.Storage;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;

/**
 * This class will be used to connect to the jenkins persistent storage and deploy artifacts in
 * jenkins side
 */
public class JenkinsStorage extends Storage {

  
    RestBasedJenkinsCIConnector connector;

    public JenkinsStorage(RestBasedJenkinsCIConnector connector) {
        this.connector = connector;
    }

	/**
	 *
	 * @param applicationId
	 * @param version
	 * @param revision
	 * @param tenantDomain
	 * @returnthe tag names of the persisted artifacts for the given job name
	 * @throws AppFactoryException
	 */
    public String[] getTagNamesOfPersistedArtifacts(String applicationId, String version, String revision, String tenantDomain) throws AppFactoryException{
    	String jobName = ServiceHolder.getContinuousIntegrationSystemDriver().getJobName(applicationId, version, null);
        return null;
    }

	/**
	 * Deploy the latest built artifact of the given job
	 * @param applicationId
	 * @param version
	 * @param revision
	 * @param artifactType
	 * @param stage
	 * @param tenantDomain
	 * @param userName
	 * @param deployAction
	 * @param repoFrom
	 * @throws AppFactoryException
	 */
    public void deployLatestSuccessArtifact(String applicationId, String version, String revision, String artifactType,
                                            String stage, String tenantDomain, String userName, String deployAction,
                                            String repoFrom) throws AppFactoryException {

        String jobName = ServiceHolder.getContinuousIntegrationSystemDriver().getJobName(applicationId, version, null);
        /*If this is a freestyle project deployment is done through a new build otherwise new changes to the repo after
        /last build would not be deployed. */
        connector.deployLatestSuccessArtifact(jobName, artifactType, stage, tenantDomain, userName,
                                              AppFactoryConstants.DEPLOY_ACTION_LABEL_ARTIFACT,repoFrom);
    }

	/**
	 * Deploy the tagged artifact for promote
	 * @param applicationId
	 * @param version
	 * @param revision
	 * @param artifactType
	 * @param stage
	 * @param tenantDomain
	 * @param userName
	 * @throws AppFactoryException
	 */
    public void deployPromotedArtifact(String applicationId, String version, String revision,String artifactType,
                                       String stage, String tenantDomain, String userName) throws AppFactoryException {
    	String jobName = ServiceHolder.getContinuousIntegrationSystemDriver().getJobName(applicationId, version, null);
        connector.deployPromotedArtifact(jobName,artifactType, stage, tenantDomain, userName);
    }

    
   
   

}
