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
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.ApplicationEventsHandler;
import org.wso2.carbon.appfactory.core.deploy.ApplicationDeployer;
import org.wso2.carbon.appfactory.core.dto.Version;
import org.wso2.carbon.appfactory.core.dto.Application;
import org.wso2.carbon.appfactory.core.dto.UserInfo;
import org.wso2.carbon.appfactory.core.util.AppFactoryCoreUtil;
import org.wso2.carbon.appfactory.jenkins.build.JenkinsApplicationEventsListener;
import org.wso2.carbon.appfactory.jenkins.build.internal.ServiceContainer;

/**
 * 
 * This event trigger in the process of application creation.
 * 
 */
public class NonBuildableApplicationEventListner extends ApplicationEventsHandler {


	private static Log log = LogFactory.getLog(JenkinsApplicationEventsListener.class);

	public NonBuildableApplicationEventListner(String identifier, int priority) {
		super(identifier, priority);
	}

	@Override
	public void onCreation(Application application, String userName, String tenantDomain, boolean isUploadableAppType) throws AppFactoryException {
		
		if (AppFactoryCoreUtil.isBuildServerRequiredProject(application.getType())) {
			return;
		}
		
		log.info("Application Creation(Non-Build) event recieved for : " + application.getId() + " " +
				application.getName());
		ApplicationDeployer applicationDeployer = new ApplicationDeployer();
		
		String defaultVersion = "trunk";
		String defaultStage = ServiceContainer.getAppFactoryConfiguration().getFirstProperty("StartStage");
		if(isUploadableAppType){
			defaultVersion = "1.0.0";
			defaultStage = ServiceContainer.getAppFactoryConfiguration().getFirstProperty("EndStage");
		}

		applicationDeployer.deployArtifact(application.getId(), defaultStage, defaultVersion, "", "deploy","");

       }

    @Override
    public void onDeletion(Application application, String userName, String tenantDomain) throws AppFactoryException {
    	
    	if (AppFactoryCoreUtil.isBuildServerRequiredProject(application.getType())) {
			return;
		}

		log.info("Successfully undeployed all the artifacts of application : " + application.getId() +
				" of tenant domain : " + tenantDomain + " from dep sync git repo");
    }

	@Override
	public void onUserAddition(Application application, UserInfo user, String tenantDomain) throws AppFactoryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUserDeletion(Application application, UserInfo user, String tenantDomain) throws AppFactoryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUserUpdate(Application application, UserInfo user, String tenantDomain) throws AppFactoryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRevoke(Application application, String tenantDomain) throws AppFactoryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onVersionCreation(Application application, Version source, Version target, String tenantDomain,
	                              String userName) throws AppFactoryException {
	if (AppFactoryCoreUtil.isBuildServerRequiredProject(application.getType())) {
		return;
	}

        String defaultStage = ServiceContainer.getAppFactoryConfiguration().getFirstProperty("StartStage");
        ApplicationDeployer applicationDeployer = new ApplicationDeployer();
        applicationDeployer.deployArtifact(application.getId(), defaultStage, target.getVersion(), "", "deploy","");

	}

	@Override
	public void onLifeCycleStageChange(Application application, Version version, String previosStage, String nextStage,
	                                   String tenantDomain) throws AppFactoryException {
		// TODO Auto-generated method stub

	}

    @Override
    public boolean hasExecuted(Application application, String userName, String tenantDomain) throws AppFactoryException {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

	@Override
	public void onFork(Application application, String userName, String tenantDomain, String version, String[] forkedUsers) throws AppFactoryException {
		// TODO Auto-generated method stub
		
	}

}
