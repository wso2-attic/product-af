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

package org.wso2.carbon.appfactory.application.mgt.listners;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.application.mgt.util.Util;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.bam.BamDataPublisher;
import org.wso2.carbon.appfactory.core.ApplicationEventsHandler;
import org.wso2.carbon.appfactory.core.dto.Application;
import org.wso2.carbon.appfactory.core.dto.UserInfo;
import org.wso2.carbon.appfactory.core.dto.Version;
import org.wso2.carbon.user.api.UserStoreException;

public class StatPublishEventsListener extends ApplicationEventsHandler {


	private static Log log = LogFactory.getLog(StatPublishEventsListener.class);

	public StatPublishEventsListener(String identifier, int priority) {
		super(identifier, priority);
	}

    @Override
    public void onCreation(Application application, String userName,
                           String tenantDomain, boolean isUploadableAppType) throws AppFactoryException {

         // Publish stats to BAM
        try {
			BamDataPublisher publisher = BamDataPublisher.getInstance();
			String tenantId = null;

			try {
			    tenantId = "" + Util.getRealmService().getTenantManager().getTenantId(tenantDomain);
			} catch (UserStoreException e) {
			    String errorMsg = "Unable to get tenant ID for bam stats : " + e.getMessage();
			    log.error(errorMsg, e);
			    throw new AppFactoryException(errorMsg, e);
			}
			
			String applicationName = application.getName();
			String applicationId = application.getId();
			String applicationDescription = application.getDescription();
			String applicationType = application.getType();
			String repositoryType = application.getRepositoryType();

			log.info("Publishing app creation stats to bam");

			publisher.PublishAppCreationEvent(applicationName, applicationId,
			        applicationDescription, applicationType, repositoryType,
			        System.currentTimeMillis(), "" + tenantId, userName);

			// TODO: Get initial version
			String defaultVersion = "trunk";
            String defaultStage = Util.getLifecycleManagementService()
                    .getFirstStageByApplication(application.getName(), tenantDomain);
            if(isUploadableAppType){
				defaultVersion = "1.0.0";
				defaultStage = Util.getLifecycleManagementService()
						.getLastStageByApplication(application.getName(), tenantDomain);
			}
			
			publisher.PublishAppVersionEvent(applicationName, applicationId,
			        System.currentTimeMillis(), tenantId, userName,defaultVersion , defaultStage);
		} catch (Exception e) {
			log.error("Failed to publish stats for " + application.getId() + e.getMessage(), e);
			//not throwing
		}
    }

    @Override
    public void onDeletion(Application application, String userName, String tenantDomain) throws AppFactoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onUserAddition(Application application, UserInfo user, String tenantDomain) throws AppFactoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onUserDeletion(Application application, UserInfo user, String tenantDomain) throws AppFactoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onUserUpdate(Application application, UserInfo user, String tenantDomain) throws AppFactoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onRevoke(Application application, String tenantDomain) throws AppFactoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onVersionCreation(Application application, Version source, Version target, String tenantDomain, String userName) throws AppFactoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onLifeCycleStageChange(Application application, Version version, String previosStage, String nextStage, String tenantDomain) throws AppFactoryException {
        //To change body of implemented methods use File | Settings | File Templates.
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
