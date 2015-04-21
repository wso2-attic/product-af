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

package org.wso2.carbon.appfactory.issuetracking;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.ApplicationEventsHandler;
import org.wso2.carbon.appfactory.core.dto.Version;
import org.wso2.carbon.appfactory.core.dto.Application;
import org.wso2.carbon.appfactory.core.dto.UserInfo;
import org.wso2.carbon.appfactory.eventing.AppFactoryEventException;
import org.wso2.carbon.appfactory.eventing.Event;

import org.wso2.carbon.appfactory.eventing.EventNotifier;
import org.wso2.carbon.appfactory.eventing.builder.utils.AppCreationEventBuilderUtil;

public class IssueTrackerListener extends ApplicationEventsHandler {

    private static final Log log = LogFactory.getLog(IssueTrackerListener.class);
    private IssueTrackerConnector connector;

    public IssueTrackerListener(String identifier, int listnerPriority) {
    	super(identifier, listnerPriority);
        try {
            connector = new IssueTrackerConnector();
        } catch (AppFactoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    public void onCreation(Application application, String userName, String tenantDomain, boolean isUploadableAppType) throws AppFactoryException {
        connector.createProject(application, userName, tenantDomain, isUploadableAppType);
        try {
            String infoMessage = "Issue tracker space created for " + application.getName() + ".";
            EventNotifier.getInstance().notify(AppCreationEventBuilderUtil.buildApplicationCreationEvent(infoMessage, "", Event.Category.INFO));
            //EventNotifier.getInstance().notify(EventBuilderUtil.buildApplicationCreationEvent(application.getId(), infoMessage, infoMessage, Event.Category.INFO));
        } catch (AppFactoryEventException e) {
            log.error("Failed to notify issue tracker provisioning events",e);
            // do not throw again.
        }
    }

    @Override
    public void onDeletion(Application application, String userName, String tenantDomain) throws AppFactoryException {
        connector.deleteProject(application, userName, tenantDomain);
        // TODO implement the delete method in connector
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
    public void onVersionCreation(Application application, Version source, Version target, String tenantDomain,String userName)
            throws AppFactoryException {
        connector.onVersionCreation(application, target.getVersion(), tenantDomain);
    }

    @Override
    public void onLifeCycleStageChange(Application application, Version version, String previosStage, String nextStage,
                                       String tenantDomain) throws AppFactoryException {
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
