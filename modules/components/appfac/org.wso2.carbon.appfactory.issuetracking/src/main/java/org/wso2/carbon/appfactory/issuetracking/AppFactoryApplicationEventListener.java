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
package org.wso2.carbon.appfactory.issuetracking;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.ApplicationEventsHandler;
import org.wso2.carbon.appfactory.core.dto.Application;
import org.wso2.carbon.appfactory.core.dto.UserInfo;
import org.wso2.carbon.appfactory.core.dto.Version;
import org.wso2.carbon.appfactory.eventing.AppFactoryEventException;
import org.wso2.carbon.appfactory.eventing.Event;

import org.wso2.carbon.appfactory.eventing.EventNotifier;
import org.wso2.carbon.appfactory.eventing.builder.utils.AppCreationEventBuilderUtil;
import org.wso2.carbon.appfactory.issuetracking.beans.GenericUser;
import org.wso2.carbon.appfactory.issuetracking.beans.IssueRepositoryConnector;
import org.wso2.carbon.appfactory.issuetracking.beans.Project;
import org.wso2.carbon.appfactory.issuetracking.exception.IssueTrackerException;

/**
 * Application event listener implementation for Issue tracker
 */
public class AppFactoryApplicationEventListener extends ApplicationEventsHandler {
    private static final Log log = LogFactory.getLog(AppFactoryApplicationEventListener.class);
    private IssueRepository repository;
    private IssueRepositoryConnector connector = null;

    public AppFactoryApplicationEventListener(String identifier, int listnerPriority) {
    	super(identifier, listnerPriority);
        repository = IssueRepository.getIssueRepository();
        connector = repository.getConnector();
        log.info("Application listener for redmine was initiated.");
    }

    @Override
    public void onCreation(Application application, String userName, String tenantDomain, boolean isUploadableAppType) throws AppFactoryException {
        try {
            if (log.isDebugEnabled()) {
                log.debug("On creation event received for redmine application listener.");
            }
            Project project = new Project();
            project.setName(application.getName());
            project.setKey(connector.getProjectApplicationMapping().getProjectKey(application.getId()));
            project.setDescription(application.getDescription());
            connector.createProject(project);

            log.info("Issue tracker provisioning for application:" + application.getId() + " is successfully completed.");
            try {
                EventNotifier.getInstance().notify(AppCreationEventBuilderUtil.buildApplicationCreationEvent(
                        "Issue tracker space created for " + application.getName() + ".",
                        "", Event.Category.INFO));
            } catch (AppFactoryEventException e) {
                log.error("Failed to notify issue tracker provisioning events", e);
                // do not throw again.
            }
        } catch (IssueTrackerException e) {
            String msg = "Error while  creating project in issue repository for " + application.getName();
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
    }

    @Override
    public void onDeletion(Application application, String userName, String tenantDomain) throws AppFactoryException {
        try {
            if (log.isDebugEnabled()) {
                log.debug("On deletion event received for redmine application listener.");
            }
            Project project = new Project();
            project.setName(application.getName());
            project.setKey(connector.getProjectApplicationMapping().getProjectKey(application.getId()));
            project.setDescription(application.getDescription());
            connector.deleteProject(project);
            log.info("On deletion event successfully handled by redmine application listener.");
        } catch (IssueTrackerException e) {
            String msg = "Error while  deleting project in issue repository for " + application.getName();
            log.error(msg);
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new AppFactoryException(msg, e);
        }
    }

    @Override
    public void onUserAddition(Application application, UserInfo userInfo, String tenantDomain)
            throws AppFactoryException {
        try {
            if (log.isDebugEnabled()) {
                log.debug("On user addition event received for redmine application");
            }
            Project project = new Project();
            project.setName(application.getName());
            project.setKey(connector.getProjectApplicationMapping().getProjectKey(application.getId()));
            project.setDescription(application.getDescription());
            GenericUser user = new GenericUser();
            user.setUsername(userInfo.getUserName());
            user.setRoles(userInfo.getRoles());
            connector.addUserToProject(user, project);
            log.info("On user addition event successfully handled by redmine application listener.");
        } catch (IssueTrackerException e) {
            String msg = "Error while adding the user " + userInfo.getUserName() + " of " + application.getName();
            log.error(msg, e);
            if (e.getMessage().equals("0")) {
                throw new AppFactoryException("0", e);
            } else {
                throw new AppFactoryException(msg, e);
            }
        }
    }

    @Override
    public void onUserDeletion(Application application, UserInfo userInfo, String tenantDomain)
            throws AppFactoryException {
        // TODO:invoke listners for redmine upon the deletion of the user from the app

        try {
            if (log.isDebugEnabled()) {
                log.debug("On user removal event received for redmine application");
            }
            Project project = new Project();
            project.setName(application.getName());
            project.setKey(connector.getProjectApplicationMapping()
                    .getProjectKey(application.getId()));
            project.setDescription(application.getDescription());
            GenericUser user = new GenericUser();
            user.setUsername(userInfo.getUserName());
            user.setRoles(userInfo.getRoles());
            connector.removeUserFromProject(user, project);
            log.info("On user removal event successfully handled by redmine application listener.");
        } catch (IssueTrackerException e) {
            String msg =
                    "Error while adding the user " + userInfo.getUserName() + " of " +
                            application.getName();
            log.error(msg, e);
            if (e.getMessage().equals("0")) {
                throw new AppFactoryException("0", e);
            } else {
                throw new AppFactoryException(msg, e);
            }
        }
    }

    @Override
    public void onRevoke(Application application, String tenantDomain) throws AppFactoryException {
        //Todo
    }

    @Override
    public void onVersionCreation(Application application, Version version, Version version1, String tenantDomain, String userName)
            throws AppFactoryException {
        try {
            if (log.isDebugEnabled()) {
                log.debug("On version creation event received for redmine application listener.");
            }
            Project project = new Project();
            org.wso2.carbon.appfactory.issuetracking.beans.Version isstrackerVersion = new org.wso2.carbon.appfactory.issuetracking.beans.Version();
            isstrackerVersion.setName(version1.getId());
            project.setName(application.getName());
            project.setKey(connector.getProjectApplicationMapping().getProjectKey(application.getId()));
            project.setDescription(application.getDescription());
            connector.createVersionInProject(project, isstrackerVersion);
            log.info("On version creation event successfully handled by redmine application listener.");
        } catch (IssueTrackerException e) {
            String msg = "Error while creating version " + version1.getId() + " for " + application.getId();
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
    }

    public void onLifeCycleStageChange(Application application,
                                       Version version, String previosStage, String nextStage, String tenantDomain)
            throws AppFactoryException {

    }

    public void onAutoDeploymentVersionChange(Application application, Version previousVersion,
                                              Version newVersion, String newStage) throws AppFactoryException {
    }

    @Override
    public boolean hasExecuted(Application application, String userName, String tenantDomain) throws AppFactoryException {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onUserUpdate(Application application, UserInfo userInfo, String tenantDomain) throws AppFactoryException {

        try {
            if (log.isDebugEnabled()) {
                log.debug("On user updating event received for redmine application");
            }

            Project project = new Project();
            project.setName(application.getName());
            project.setKey(connector.getProjectApplicationMapping()
                    .getProjectKey(application.getId()));
            project.setDescription(application.getDescription());
            GenericUser user = new GenericUser();
            user.setUsername(userInfo.getUserName());
            user.setRoles(userInfo.getRoles());
            connector.updateUserOfProject(user, project);
            if (log.isDebugEnabled()) {
                log.debug("On user addition event successfully handled by redmine application listener.");
            }

        } catch (IssueTrackerException e) {
            String msg =
                    "Error while updating the user " + userInfo.getUserName() + " of " +
                            application.getName() + e.getMessage();
            log.error(msg, e);

            throw new AppFactoryException(msg, e);
        }
    }

    @Override
    public void onFork(Application application, String userName, String tenantDomain, String version, String[] forkedUsers) throws AppFactoryException {
        // TODO Auto-generated method stub

    }

}
