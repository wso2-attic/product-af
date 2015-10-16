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

package org.wso2.carbon.appfactory.application.mgt.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.appfactory.application.mgt.service.applicationqueue.ApplicationCreator;
import org.wso2.carbon.appfactory.application.mgt.util.UserApplicationCache;
import org.wso2.carbon.appfactory.application.mgt.util.Util;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.bam.BamDataPublisher;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.ApplicationEventsHandler;
import org.wso2.carbon.appfactory.core.dao.ApplicationDAO;
import org.wso2.carbon.appfactory.core.dao.JDBCAppVersionDAO;
import org.wso2.carbon.appfactory.core.dao.JDBCApplicationDAO;
import org.wso2.carbon.appfactory.core.dto.Application;
import org.wso2.carbon.appfactory.core.dto.DeployStatus;
import org.wso2.carbon.appfactory.core.queue.AppFactoryQueueException;
import org.wso2.carbon.appfactory.core.util.AppFactoryCoreUtil;
import org.wso2.carbon.appfactory.core.util.CommonUtil;
import org.wso2.carbon.appfactory.core.util.Constants;
import org.wso2.carbon.appfactory.eventing.AppFactoryEventException;
import org.wso2.carbon.appfactory.eventing.Event;
import org.wso2.carbon.appfactory.eventing.EventNotifier;
import org.wso2.carbon.appfactory.eventing.builder.utils.AppCreationEventBuilderUtil;
import org.wso2.carbon.appfactory.eventing.builder.utils.ContinousIntegrationEventBuilderUtil;
import org.wso2.carbon.appfactory.tenant.mgt.beans.UserInfoBean;
import org.wso2.carbon.appfactory.utilities.project.ProjectUtils;
import org.wso2.carbon.appfactory.utilities.services.EmailSenderService;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.*;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ApplicationManagementService extends AbstractAdmin {

    private static final Log perfLog = LogFactory.getLog("org.wso2.carbon.appfactory.perf.appversion.load");
    public static String EMAIL_CLAIM_URI = "http://wso2.org/claims/emailaddress";
    public static String FIRST_NAME_CLAIM_URI = "http://wso2.org/claims/givenname";
    public static String LAST_NAME_CLAIM_URI = "http://wso2.org/claims/lastname";
    public static UserApplicationCache userApplicationCache = UserApplicationCache.getUserApplicationCache();
    private static Log log = LogFactory.getLog(ApplicationManagementService.class);

    /**
     * This createApplication method is used for the create an application. When
     * call this method, it put to the queue. TODO:Make it work in cluster and
     * MT environment
     *
     * @param applicationName        Application name.
     * @param applicationKey         Key for the Application. This should be unique.
     * @param applicationDescription Description of the application.
     * @param applicationType        Type of the application. ex: war, jaxrs, jaxws ...
     * @param repositoryType         Type of the repository that should use. ex: svn, git
     * @param userName               Logged-in user name.
     */
    public void createApplication(String applicationName, String applicationKey, String applicationDescription,
                                  String applicationType, String repositoryType,
                                  String userName) throws ApplicationManagementException {

        Application application = new Application();
        application.setName(applicationName);
        application.setId(applicationKey);
        application.setDescription(applicationDescription);
        application.setType(applicationType);
        application.setRepositoryType(repositoryType);
        application.setOwner(userName);

        try {
            ApplicationCreator applicationCreator = ApplicationCreator.getInstance();
            applicationCreator.getExecutionEngine().getSynchQueue().put(application);

            BamDataPublisher publisher = BamDataPublisher.getInstance();
            String tenantId = "" + Util.getRealmService().getBootstrapRealmConfiguration().getTenantId();
            //TODO: Check if we need to put the repo accessibility into this also
            publisher.PublishAppCreationEvent(applicationName, applicationKey, applicationDescription, applicationType,
                    repositoryType, System.currentTimeMillis(), tenantId, userName);

        } catch (AppFactoryQueueException e) {
            String errorMsg = "Error occurred when adding an application in to queue";
            log.error(errorMsg, e);
            throw new ApplicationManagementException(errorMsg, e);
        } catch (AppFactoryException e) {
            String msg = "Unable to publish data to BAM";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
    }

    /**
     * Checks if the given application key already exists in the database.
     * This check is done against the AppFactory database
     *
     * @param applicationKey the application key to be checked
     * @return false if application key exists
     * @throws ApplicationManagementException
     */
    public boolean isApplicationIdAvailable(String applicationKey) throws ApplicationManagementException {
        try {
            // We need to return the opposite of what the DAO returns.
            // The DAO will return true if the given application key exists
            // Therefore we need to return false since the application id is not available
            return !JDBCApplicationDAO.getInstance().isApplicationKeyExists(applicationKey);
        } catch (AppFactoryException e) {
            String msg = "Error while validating application key :  " + applicationKey;
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        }
    }

    /**
     * Checks if the provided application name is already available for the given tenant
     *
     * @param applicationName the application name to be checked
     * @return false if application name exists
     * @throws ApplicationManagementException
     */
    public boolean isApplicationNameAvailable(String applicationName) throws ApplicationManagementException {
        try {
            // We need to return the opposite of what the DAO returns.
            // The DAO will return true if the given application name exists
            // Therefore we need to return false since the application name is not available
            return !JDBCApplicationDAO.getInstance().isApplicationNameExists(applicationName);
        } catch (AppFactoryException e) {
            String msg = "Error while validating application name :  " + applicationName;
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        }
    }

    /**
     * @return Application array
     * @throws AppFactoryException
     * @deprecated Gets all the applications for tenant
     */
    public Application[] getAllApplications() throws AppFactoryException {
        return null;
    }

    //Todo:remove domainName and userName after updating bpel
    public void publishApplicationCreation(String domainName, String userName, String applicationId,
                                           String applicationType) throws ApplicationManagementException {
        // New application is created successfully so now time to clear realm in cache to reload
        // the new realm with updated permissions

        boolean isListnersCompletedSuccessfully = true;
        clearRealmCache(applicationId);
        domainName = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String loggedInUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
        String tenantAwareUserName = loggedInUser + "@" + domainName;
        if (log.isDebugEnabled()) {
            log.debug("Application creation is started by user:" + tenantAwareUserName + " in tenant domain:" +
                    domainName);
        }
        Iterator<ApplicationEventsHandler> appEventListeners = Util.getApplicationEventsListeners().iterator();
        ApplicationEventsHandler listener = null;
        Application application = null;
        PrivilegedCarbonContext threadLocalCarbonContext = null;
        try {
            PrivilegedCarbonContext.startTenantFlow();
            threadLocalCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            threadLocalCarbonContext.setTenantDomain(domainName, true);
            threadLocalCarbonContext.setUsername(loggedInUser);

            // creates role for application in the ldap to add users of the
            createApplicationRole(applicationId, tenantAwareUserName, domainName);
            addRegistryWritePermissionToApp(applicationId, domainName);
            application = ApplicationDAO.getInstance().getApplicationInfo(applicationId);
            if (application == null) {
                String errorMsg = String.format("Unable to load application information for id %s", applicationId);
                throw new ApplicationManagementException(errorMsg);
            }

            // IMO, we should only add application information to AppFactory DB only if the above condition fails.
            // If there are information in the registry, then only we should add them to DB
            JDBCApplicationDAO.getInstance().addApplication(application);

            boolean isUploadableAppType = AppFactoryCoreUtil.isUplodableAppType(application.getType());

            while (appEventListeners.hasNext()) {
                try {
                    listener = appEventListeners.next();
                    listener.onCreation(application, tenantAwareUserName, domainName, isUploadableAppType);
                } catch (Throwable e) {
                    isListnersCompletedSuccessfully = false;
                    String error = "Error while executing onCreation method of ApplicationEventsListener : " + listener;
                    log.error(error, e);
                    this.deleteApplication(application, tenantAwareUserName, domainName);
                    try {

                        String errorMessage = "Error while creating the app " + applicationId;
                        if (error.contains("JenkinsApplicationEventsListener")) {
                            errorMessage = "Error occurred while creating the Jenkins space for the app " +
                                    applicationId + " in tenant:" + domainName;
                        } else if (error.contains("AppFactoryApplicationEventListener")) {
                            errorMessage = "Error occurred while creating the Issue tracker provisioning for the app "
                                    + applicationId + " in tenant:" + domainName;
                        } else if (error.contains("ApplicationInfomationChangeListner")) {
                            errorMessage = "Error occurred while invoking the ApplicationInfomationChangeListner " +
                                    "for the app " + applicationId + " in tenant:" + domainName;
                        } else if (error.contains("DSApplicationListener")) {
                            errorMessage = "Error occurred while creating the data source for the app " + applicationId
                                    + " in tenant:" + domainName;
                        } else if (error.contains("DomainMappingListener")) {
                            errorMessage = "Error occurred while adding the domain mapping for the app " + applicationId
                                    + " in tenant:" + domainName;
                        } else if (error.contains("EnvironmentAuthorizationListener")) {
                            errorMessage = "Error occurred while creating environments for the app  " + applicationId
                                    + " in tenant:" + domainName;
                        } else if (error.contains("InitialArtifactDeployerHandler")) {
                            errorMessage = "Error occurred while initial code committing for the app " + applicationId
                                    + " in tenant:" + domainName;
                        } else if (error.contains("IssueTrackerListener")) {
                            errorMessage = "Error occurred while creating the issue tracker space for the app " +
                                    applicationId + " in tenant:" + domainName;
                        } else if (error.contains("NonBuildableApplicationEventListner")) {
                            errorMessage = "Error occurred while invoking the NonBuildableApplicationEventListner " +
                                    "for the app " + applicationId + " in tenant:" + domainName;
                        } else if (error.contains("RepositoryHandler")) {
                            errorMessage = "Error occurred while creating the source code repository for the " +
                                    "app " + applicationId + " in tenant:" + domainName;
                        } else if (error.contains("StatPublishEventsListener")) {
                            errorMessage = "Error occurred while publishing stats related to the app " + applicationId
                                    + " in tenant:" + domainName;
                        } else if (error.contains("UserProvisioningListener")) {
                            errorMessage = "Error occurred while user provisioning for the app " + applicationId
                                    + " in tenant:" + domainName;
                        }

                        EventNotifier.getInstance().notify(AppCreationEventBuilderUtil.buildApplicationCreationEvent(
                                "Application creation failed for " + application.getName(),
                                errorMessage.concat(". Therefore application will rollback."),
                                Event.Category.ERROR));

                    } catch (AppFactoryEventException e1) {
                        log.error("Failed to notify application creation failed events", e1);
                        // do not throw again.
                    }
                    break;
                }
            }
            if (isListnersCompletedSuccessfully) {
                ProjectUtils
                        .updateApplicationCreationStatus(applicationId, Constants.ApplicationCreationStatus.COMPLETED);
            }
        } catch (AppFactoryException ex) {
            String errorMsg = "Unable to load registry rxt for application " + applicationId;
            log.error(errorMsg, ex);
            if (application != null) {
                try {
                    this.deleteApplication(application, tenantAwareUserName, domainName);
                } catch (AppFactoryException e) {
                    log.error("Failed to delete the application on roll back.", e);
                    // ignore throwing again
                }
            }
            throw new ApplicationManagementException(errorMsg, ex);
        } catch (UserStoreException e) {
            String errorMsg = "Unable to add application role to the userstore";
            log.error(errorMsg, e);
            try {
                //   if (errorMsg.con)
                EventNotifier.getInstance().notify(AppCreationEventBuilderUtil.buildApplicationCreationEvent(
                        "Application creation failed for " + application.getName(),
                        errorMsg.concat("Therefore application will be rollback."), Event.Category.ERROR));
            } catch (AppFactoryEventException e1) {
                log.error("Failed to notify application creation failed events", e1);
                // do not throw again.
            }
            throw new ApplicationManagementException(errorMsg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }


    public void publishForkRepository(String applicationId, String type, String version, String userName,
                                      String[] forkedUser) throws ApplicationManagementException {
        try {

            // Getting the tenant ID from the CarbonContext since this is called
            // as a SOAP service.
            CarbonContext threadLocalCarbonContext = CarbonContext.getThreadLocalCarbonContext();

            String domainName = threadLocalCarbonContext.getTenantDomain();

            Iterator<ApplicationEventsHandler> appEventListeners = Util.getApplicationEventsListeners().iterator();

            Application application = ApplicationDAO.getInstance().getApplicationInfo(applicationId);
            String applicationType = AppFactoryCoreUtil.getApplicationType(applicationId, domainName);

            ApplicationEventsHandler listener = null;
            while (appEventListeners.hasNext()) {
                try {
                    listener = appEventListeners.next();
                    listener.onFork(application, userName, domainName, version, forkedUser);
                } catch (Throwable e) {
                    log.error("Error while executing onFork method of ApplicationEventsHandler : " + listener, e);
                }
            }


        } catch (AppFactoryException ex) {
            String errorMsg = "Unable to publish onForking";
            log.error(errorMsg, ex);
            throw new ApplicationManagementException(errorMsg, ex);
        } catch (RegistryException e) {
            log.error(e);
            throw new ApplicationManagementException(e);
        }
    }

    /**
     * Updating DB value when do the  promote action
     *
     * @param applicationId id of the application
     * @param stage         current stage of the application
     * @param version       version of the application
     * @param action        promote action
     * @throws ApplicationManagementException
     */

    //ToDo renamed
    public void updateRxtWithPromoteState(String applicationId, String stage, String version, String action,
                                          String state) throws ApplicationManagementException {
        if (action == null || !action.equals(AppFactoryConstants.RXT_KEY_APPVERSION_PROMOTE)) {
            return;
        }
        try {
            JDBCAppVersionDAO.getInstance().updatePromoteStatusOfVersion(applicationId, version, state);
            if (log.isDebugEnabled()) {
                log.debug("Successfully updated Promote status as Pending for application id : " + applicationId +
                        " version : " + version + " stage :" + stage);
            }
        } catch (AppFactoryException e) {
            String msg = "Error occurred while updating with promote status for application id : " + applicationId +
                    " version : " + version + " stage :" + stage;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
    }

    private void clearRealmCache(String applicationKey) throws ApplicationManagementException {
        RealmService realmService = Util.getRealmService();
        int tenantID;
        try {
            tenantID = Util.getRealmService().getTenantManager().getTenantId(applicationKey);
            realmService.clearCachedUserRealm(tenantID);
        } catch (UserStoreException e) {
            String errorMsg =
                    "Unable to clear user realm cache for tenant id  " + applicationKey;
            log.error(errorMsg, e);
            throw new ApplicationManagementException(errorMsg, e);
        }
    }


    public boolean deleteApplication(Application application, String userName, String domainName) throws
            AppFactoryException, ApplicationManagementException {
        boolean completedSuccessfully = true;

        String applicationId = application.getId();

        Iterator<ApplicationEventsHandler> appEventListeners = Util.getApplicationEventsListeners().iterator();
        UserInfoBean[] userList = new ApplicationUserManagementService().getUsersOftheApplication(applicationId);

//        for (UserInfoBean anUserList : userList) {
//            EventNotifier.getInstance().notify(AppCreationEventBuilderUtil.buildApplicationDeletionEventForUser(anUserList.getUserName().concat("@").concat(domainName));
//        }

        ApplicationEventsHandler listener;
        while (appEventListeners.hasNext()) {
            listener = appEventListeners.next();
            try {
                if (listener.hasExecuted(application, userName, domainName)) {
                    listener.onDeletion(application, userName, domainName);
                }
            } catch (AppFactoryException e) {
                log.error("Error in calling onDeletion method of ApplicationEventsListener : " + listener, e);
            }

        }

        try {
            removeApplicationRoles(applicationId, userName, domainName);
        } catch (UserStoreException e) {
            log.error("Error while removing the application roles from LDAP for application " + applicationId, e);
        }

        try {
            ApplicationDAO.getInstance().deleteApplicationArtifact(applicationId, domainName);
        } catch (UserStoreException e) {
            log.error("Error while deleting the application resource from registry for application " + applicationId,
                    e);
        } catch (RegistryException e) {
            log.error("Error while deleting the application resource from registry for application " + applicationId,
                    e);
        }
        JDBCApplicationDAO.getInstance().deleteApplication(applicationId);

        String adminEmail = AppFactoryUtil.getAdminEmail();
        new EmailSenderService().sendMail(adminEmail, "application-rollback-notice-email.xml",
                createUserParams(application));

        String title = "Application " + applicationId + " is deleted successfully";
        String messageDescription = "Deleted by: " + userName;
        try {

            for (UserInfoBean anUserList : userList) {
                EventNotifier.getInstance().notify(AppCreationEventBuilderUtil.buildApplicationDeletionEventForUser(
                        anUserList.getUserName().concat("@").concat(domainName), title, messageDescription, userName,
                        Event.Category.INFO));
            }
            EventNotifier.getInstance().notify(AppCreationEventBuilderUtil.buildApplicationDeletionEventForApplication(
                    applicationId, title, messageDescription, userName, Event.Category.INFO));
        } catch (AppFactoryEventException e) {
            log.error("Failed to notify application deletion event", e);
        }

	    // insert application to the failed_application table
	    JDBCApplicationDAO.getInstance().addFailedApplication(applicationId);

        return completedSuccessfully;

    }

    private String[][] createUserParams(Application application) {
        String[][] userParams = new String[4][2];
        userParams[0][0] = "adminUserName";
        userParams[0][1] = AppFactoryUtil.getAdminUsername();
        userParams[1][0] = "applicationName";
        userParams[1][1] = application.getName();
        userParams[2][0] = "applicationKey";
        userParams[2][1] = application.getId();
        userParams[3][0] = "tenantDomain";
        userParams[3][1] = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        return userParams;
    }

    /**
     * Creates the role for the application in the ldap.It appends the
     * APP_ROLE_PREFIX to the appkey and construct the ldap role name
     *
     * @param applicationKey
     * @param appOwner
     * @param tenantDomain
     * @return
     * @throws UserStoreException
     */
    private boolean createApplicationRole(String applicationKey, String appOwner, String tenantDomain)
            throws UserStoreException, AppFactoryException {
        RealmService realmService = Util.getRealmService();
        TenantManager tenantManager = realmService.getTenantManager();
        int tenantId = tenantManager.getTenantId(tenantDomain);
        String applicationName = null;
        UserStoreManager userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
        userStoreManager.addRole(AppFactoryUtil.getRoleNameForApplication(applicationKey), new String[]
                {appOwner.split("@")[0]}, new org.wso2.carbon.user.core.Permission[]
                {new org.wso2.carbon.user.core.Permission(AppFactoryConstants.PER_APP_ROLE_PERMISSION,
                        CarbonConstants.UI_PERMISSION_ACTION)}, false);

        // Publish user add event to BAM
        Application app = ApplicationDAO.getInstance().getApplicationInfo(applicationKey);
        applicationName = app.getName();


        try {
            BamDataPublisher publisher = BamDataPublisher.getInstance();
            publisher.PublishUserUpdateEvent(applicationName, applicationKey, System.currentTimeMillis(),
                    "" + tenantId, appOwner.split("@")[0], AppFactoryConstants.BAM_ADD_DATA);
        } catch (AppFactoryException e) {
            String message = "Failed to publish user add event to bam on application " + applicationKey;
            log.error(message);
            // TODO: throw exception
        }

        return true;
    }

    /**
     * This method can be used to remove the application related roles added to LDAP
     *
     * @param applicationKey
     * @param appOwner
     * @param tenantDomain
     * @return
     * @throws UserStoreException
     */
    private boolean removeApplicationRoles(String applicationKey, String appOwner, String tenantDomain)
            throws UserStoreException {
        RealmService realmService = Util.getRealmService();
        TenantManager tenantManager = realmService.getTenantManager();
        PrivilegedCarbonContext threadLocalCarbonContext = null;
        try {
            int tenantId = tenantManager.getTenantId(tenantDomain);
            PrivilegedCarbonContext.startTenantFlow();
            threadLocalCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            threadLocalCarbonContext.setTenantId(tenantId, true);
            UserStoreManager userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
            if (userStoreManager.isExistingRole(AppFactoryUtil.getRoleNameForApplication(applicationKey))) {
                userStoreManager.deleteRole(AppFactoryUtil.getRoleNameForApplication(applicationKey));
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

        // TODO publish to bam

        return true;
    }

    private void addRegistryWritePermissionToApp(String applicationKey, String tenantDomain) throws UserStoreException {
        String roleName = AppFactoryUtil.getRoleNameForApplication(applicationKey);
        AuthorizationManager authMan = Util.getRealmService().getTenantUserRealm(Util.getRealmService()
                .getTenantManager().getTenantId(tenantDomain)).getAuthorizationManager();
        authMan.authorizeRole(roleName, RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                AppFactoryConstants.REGISTRY_APPLICATION_PATH + AppFactoryConstants.URL_SEPERATOR +
                applicationKey, ActionConstants.PUT);
    }


    public String[] getAllCreatedApplications() throws ApplicationManagementException {
        String apps[] = new String[0];
        List<String> list = new ArrayList<String>();
        TenantManager manager = Util.getRealmService().getTenantManager();
        try {
            Tenant[] tenants = manager.getAllTenants();

            for (Tenant tenant : tenants) {
                list.add(tenant.getDomain());
            }

        } catch (UserStoreException e) {
            String msg = "Error while getting all applications";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
        if (!list.isEmpty()) {
            apps = list.toArray(new String[list.size()]);
        }
        return apps;
    }

    /**
     * update rxt on application deployment success/failure
     *
     * @param applicationId
     * @param version
     * @param tenantDomain
     * @param artifactLastModifiedTime
     */
    public void updateApplicationDeploymentSuccessStatus(String applicationId, String version, String stage,
                                                         String tenantDomain, long artifactLastModifiedTime)
            throws AppFactoryException, AppFactoryEventException {
        long artifactLastModifiedTimeFromDB;
        DeployStatus deployStatus;
        try {
            deployStatus = JDBCApplicationDAO.getInstance().getDeployStatus(applicationId, version, stage,
                    false, null);
        } catch (AppFactoryException e) {
            String errorMsg = String.format("Unable to load the application deploy information " +
                    "for application id : %s", applicationId);
            log.error(errorMsg, e);
            throw new AppFactoryEventException(errorMsg, e);
        }
        artifactLastModifiedTimeFromDB = deployStatus.getLastDeployedTime();

        if (artifactLastModifiedTimeFromDB == 0) {
            artifactLastModifiedTimeFromDB = -1;
        }

        if (artifactLastModifiedTimeFromDB != 0 && artifactLastModifiedTimeFromDB < artifactLastModifiedTime) {
            String msg = version + " deployed in " + stage + " stage";

            try {
                String correlationKey = org.wso2.carbon.appfactory.eventing.utils.Util.deploymentCorrelationKey
                        (applicationId, stage, version, tenantDomain);
                EventNotifier.getInstance().notify(ContinousIntegrationEventBuilderUtil.
                        buildObtainWarDeploymentStatusEvent(applicationId, tenantDomain, msg, "", Event.Category.INFO,
                                correlationKey));
            } catch (AppFactoryEventException e) {
                log.error("Failed to notify the Application deployment success event ", e);
            }

            try {
                deployStatus.setLastDeployedStatus(AppFactoryConstants.APP_LAST_DEPLOY_STATUS);
                deployStatus.setLastDeployedTime(artifactLastModifiedTime);
                JDBCApplicationDAO.getInstance().updateLastDeployStatus(applicationId, version, stage, false, null, deployStatus);
            } catch (AppFactoryException e) {
                GenericArtifact application = null;
                try {
                    application = CommonUtil.getApplicationArtifact(applicationId, tenantDomain);
                } catch (AppFactoryException ex) {
                    String message = "Error while validating application id :  " + applicationId + " version: " + version;
                    log.error(message);
                }
                if (application == null) {
                    log.warn("Application is not available for application id :" + applicationId);
                } else {
                    msg = "Error while updating db";
                    log.error(msg);
                    throw new AppFactoryException(msg, e);
                }
            }
        }
    }
}
