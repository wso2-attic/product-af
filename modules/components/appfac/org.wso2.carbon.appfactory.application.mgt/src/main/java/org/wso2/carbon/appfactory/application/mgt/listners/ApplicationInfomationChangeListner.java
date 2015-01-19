/*
 * Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.appfactory.application.mgt.listners;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.ApplicationEventsHandler;
import org.wso2.carbon.appfactory.core.dto.Application;
import org.wso2.carbon.appfactory.core.dto.UserInfo;
import org.wso2.carbon.appfactory.core.dto.Version;
import org.wso2.carbon.appfactory.eventing.AppFactoryEventException;
import org.wso2.carbon.appfactory.eventing.Event;

import org.wso2.carbon.appfactory.eventing.EventNotifier;
import org.wso2.carbon.appfactory.eventing.builder.utils.AppCreationEventBuilderUtil;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryMgtException;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryProvider;
import org.wso2.carbon.appfactory.repository.mgt.internal.Util;
import org.wso2.carbon.appfactory.utilities.project.ProjectUtils;

/**
 * Listener class to update Application Information (Rxt - appinfo etc) in different application events.
 */
public class ApplicationInfomationChangeListner extends ApplicationEventsHandler {

	private static final Log log = LogFactory.getLog(ApplicationInfomationChangeListner.class);

	public ApplicationInfomationChangeListner(String identifier, int priority) {
		super(identifier, priority);
	}
   
    /*
     * (non-Javadoc)
     *
     * @see
     * org.wso2.carbon.appfactory.core.ApplicationEventsListener#onCreation(
     * org.wso2.carbon.appfactory.core.dto.Application)
     */
    @Override
    public void onCreation(Application application, String userName, String tenantDomain, boolean isUploadableAppType) throws AppFactoryException {

        log.info("On Creation is successfully handled by Application Information Change Listner.");

        String message = "Application " + application.getName() + " created successfully";
        try {
            EventNotifier.getInstance().notify(AppCreationEventBuilderUtil.buildApplicationCreationCompletedEvent(application.getId(),message, "", userName));
            EventNotifier.getInstance().notify(AppCreationEventBuilderUtil.buildAppCreationStatusEventToAppWall(application.getId(), "Issue tracker space created for the app", ""));
            EventNotifier.getInstance().notify(AppCreationEventBuilderUtil.buildAppCreationStatusEventToAppWall(application.getId(), "Jenkins space created for the app", ""));
            String infoMessage = "Initial " + application.getRepositoryType().toUpperCase() + " repo created for the app";

            RepositoryProvider provider = Util.getRepositoryProvider(application.getRepositoryType());
            String url = provider.getAppRepositoryURL(application.getId(), tenantDomain);
            String infoMsgDesc =   "Repo URL: " + url;
            EventNotifier.getInstance().notify(AppCreationEventBuilderUtil.buildAppCreationStatusEventToAppWall(application.getId(), infoMessage, infoMsgDesc));
        } catch (AppFactoryEventException e) {
            log.error("Failed notifying application creation complete event" + e.getMessage(), e);
        } catch (RepositoryMgtException e) {
            String error = "Error while getting the repository";
            log.error(error, e);
            throw new AppFactoryException(error, e);
        }

    }

    public void onDeletion(Application application, String userName, String tenantDomain) throws AppFactoryException {
        //TODO implement method - do nothing

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.wso2.carbon.appfactory.core.ApplicationEventsListener#onUserAddition
     * (org.wso2.carbon.appfactory.core.dto.Application,
     * org.wso2.carbon.appfactory.core.dto.UserInfo)
     */
    @Override
    public void onUserAddition(Application application, UserInfo user, String tenantDomain) throws AppFactoryException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.wso2.carbon.appfactory.core.ApplicationEventsListener#onUserDeletion
     * (org.wso2.carbon.appfactory.core.dto.Application,
     * org.wso2.carbon.appfactory.core.dto.UserInfo)
     */
    @Override
    public void onUserDeletion(Application application, UserInfo user, String tenantDomain) throws AppFactoryException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.wso2.carbon.appfactory.core.ApplicationEventsListener#onUserUpdate
     * (org.wso2.carbon.appfactory.core.dto.Application,
     * org.wso2.carbon.appfactory.core.dto.UserInfo)
     */
    @Override
    public void onUserUpdate(Application application, UserInfo user, String tenantDomain) throws AppFactoryException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.wso2.carbon.appfactory.core.ApplicationEventsListener#onRevoke(org
     * .wso2.carbon.appfactory.core.dto.Application)
     */
    @Override
    public void onRevoke(Application application, String tenantDomain) throws AppFactoryException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.wso2.carbon.appfactory.core.ApplicationEventsListener#onVersionCreation
     * (org.wso2.carbon.appfactory.core.dto.Application,
     * org.wso2.carbon.appfactory.core.dto.Version,
     * org.wso2.carbon.appfactory.core.dto.Version)
     */
    @Override
    public void onVersionCreation(Application application, Version source, Version target, String tenantDomain,
                                  String loggedInUserName)
            throws AppFactoryException {

       //do nothing
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.appfactory.core.ApplicationEventsListener#
     * onLifeCycleStageChange(org.wso2.carbon.appfactory.core.dto.Application,
     * org.wso2.carbon.appfactory.core.dto.Version, java.lang.String,
     * java.lang.String)
     */
    @Override
    public void onLifeCycleStageChange(Application application, Version version,
                                       String previosStage, String nextStage, String tenantDomain)
            throws AppFactoryException {
        if (log.isDebugEnabled()) {
            log.debug("onLifeCycleStageChange is successfully handled by Application Information Change Listner.");
        }

        if (AppFactoryConstants.ApplicationStage.PRODUCTION.getStageStrValue()
                .equalsIgnoreCase(nextStage)) {
            log.debug("adding production version");
            ProjectUtils.addProductionVersion(application.getId(), version.getId());
        } else if (AppFactoryConstants.ApplicationStage.PRODUCTION.getStageStrValue()
                .equalsIgnoreCase(previosStage)) {
            log.debug("removing production version");
            ProjectUtils.removeProductionVersion(application.getId(), version.getId());
        }
        log.info("onLifeCycleStageChange is successfully handled by Application Information Change Listner.");
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.wso2.carbon.appfactory.core.ApplicationEventsListener#getPriority()
     */
    @Override
    public int getPriority() {
        return priority;
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
