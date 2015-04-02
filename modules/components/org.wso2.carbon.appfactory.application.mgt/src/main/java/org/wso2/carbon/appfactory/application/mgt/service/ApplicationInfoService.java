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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.application.mgt.util.Util;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.ApplicationEventsHandler;
import org.wso2.carbon.appfactory.core.cache.ApplicationsOfUserCache;
import org.wso2.carbon.appfactory.core.dao.JDBCApplicationDAO;
import org.wso2.carbon.appfactory.core.deploy.Artifact;
import org.wso2.carbon.appfactory.core.dto.Application;
import org.wso2.carbon.appfactory.core.dto.ApplicationSummary;
import org.wso2.carbon.appfactory.core.dto.BuildStatus;
import org.wso2.carbon.appfactory.core.dto.BuildandDeployStatus;
import org.wso2.carbon.appfactory.core.dto.DeployStatus;
import org.wso2.carbon.appfactory.core.governance.RxtManager;
import org.wso2.carbon.appfactory.core.governance.dao.AppVersionDAO;
import org.wso2.carbon.appfactory.core.governance.dao.RxtApplicationDAO;
import org.wso2.carbon.appfactory.core.util.AppFactoryCoreUtil;
import org.wso2.carbon.appfactory.core.util.Constants;
import org.wso2.carbon.appfactory.eventing.AppFactoryEventException;
import org.wso2.carbon.appfactory.eventing.Event;
import org.wso2.carbon.appfactory.eventing.EventNotifier;
import org.wso2.carbon.appfactory.eventing.builder.utils.AppCreationEventBuilderUtil;
import org.wso2.carbon.appfactory.jenkins.build.JenkinsCISystemDriver;
import org.wso2.carbon.appfactory.jenkins.build.internal.ServiceContainer;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This service provides necessary information of the application. Detailed and
 * lightweight methods related to retrieving applications information
 * 
 * 
 */
public class ApplicationInfoService {
    private static Log log = LogFactory.getLog(ApplicationInfoService.class);

    private static final Log perfLog = LogFactory.getLog("org.wso2.carbon.appfactory.perf.application.load");
    public static JDBCApplicationDAO applicationDAO=JDBCApplicationDAO.getInstance();

    /**
     * Get the application information bean of the application
     * 
     * @param applicationKey
     * @return
     * @throws AppFactoryException
     */
    private Application getBasicApplicationInfo(String applicationKey) throws AppFactoryException {
        Application application = RxtApplicationDAO.getInstance().getApplicationInfo(applicationKey);
        return application;
    }

    /**
     * Gets application information of the user to populate the user home
     *
     * @param userName
     * @return
     * @throws ApplicationManagementException
     */
    public Application[] getApplicationInfoForUser(String userName)
                                                                           throws ApplicationManagementException {
        String[] applicationKeys = getApplicationKeysOfUser(userName);
        String tenantDomain = getTenantDomain();
        List<Application> appInfoList = new ArrayList<Application>();
        for (String applicationKey : applicationKeys) {
            try {
                Application application = RxtApplicationDAO.getInstance().getApplicationInfo(applicationKey);
                appInfoList.add(application);
            } catch (AppFactoryException e) {
                String msg =
                             "Error while getting application info for user " + userName +
                                     " of tenant" + tenantDomain;
                log.error(msg, e);
            }
        }
        return appInfoList.toArray(new Application[appInfoList.size()]);
    }

    /**
     * Gets application creation status of the {@code applicationKeys}.
     *
     * @param applicationKeys application keys
     * @return Map as applicationKey and {@link org.wso2.carbon.appfactory.core.util.Constants
     * .ApplicationCreationStatus} as key-value pairs.
     * e.g
     * appKey1 : {@link org.wso2.carbon.appfactory.core.util.Constants.ApplicationCreationStatus#COMPLETED}
     * appKey2 : {@link org.wso2.carbon.appfactory.core.util.Constants.ApplicationCreationStatus#PENDING}
     * appKey3 : {@link org.wso2.carbon.appfactory.core.util.Constants.ApplicationCreationStatus#FAULTY}
     * @throws AppFactoryException
     */
    public Map<String, Constants.ApplicationCreationStatus> getAppCreationStatus(String[] applicationKeys)
            throws AppFactoryException {
        JDBCApplicationDAO jdbcApplicationDAO = JDBCApplicationDAO.getInstance();
        Map<String, Constants.ApplicationCreationStatus> applicationCreationStatus = null;
        try {
            if (ArrayUtils.isNotEmpty(applicationKeys)) {
                applicationCreationStatus = jdbcApplicationDAO.getApplicationCreationStatusByKeys(applicationKeys);
            }
        } catch (AppFactoryException e) {
            String errMsg = "Error while getting application creation status of applications " +
                            StringUtils.join(applicationKeys, ",");
            log.error(errMsg, e);
            throw new AppFactoryException(errMsg, e);
        }
        return applicationCreationStatus;
    }

    /**
     * Get the type of the application, given the application Id and the tenant name
     *
     * @param applicationId Id of the application
     * @param tenantDomain  Tenant domain of the application
     * @return the application type
     * @throws AppFactoryException If invalid application or application type is not available
     */
    public String getApplicationType(String applicationId, String tenantDomain) throws AppFactoryException {
        return RxtApplicationDAO.getInstance().getApplicationType(applicationId);
    }


    public String addArtifact(String key, String info, String lifecycleAttribute) throws AppFactoryException {
       RegistryUtils.recordStatistics(key, info, lifecycleAttribute);
       if(key.equals(AppFactoryConstants.RXT_KEY_APPINFO_APPLICATION)){
          return RxtApplicationDAO.getInstance().addApplicationArtifact(info, lifecycleAttribute);
       }else if(key.equals(AppFactoryConstants.RXT_KEY_APPVERSION)){
           return AppVersionDAO.getInstance().addArtifact(info,lifecycleAttribute);
       }
        return null;
    }

    private String getTenantDomain() {
        return CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    }

    /**
     * Get the application object by giving application id
     *
     * @param applicationId Id of the application
     * @return application object
     * @throws AppFactoryException
     */
    public Application getApplication(String applicationId) throws AppFactoryException {
        return RxtApplicationDAO.getInstance().getApplicationInfo(applicationId);
    }

    /**
     * Get application URL for an application with given version
     *
     * @param applicationId Application Id
     * @param version       version of the application
     * @param stage         stage of the application
     * @param tenantDomain  tenant domain
     * @return application url
     * @throws AppFactoryException
     */
    public String getApplicationUrl(String applicationId, String version, String stage,
                                    String tenantDomain) throws AppFactoryException {
        return AppFactoryCoreUtil.getApplicationUrl(applicationId, version, stage, tenantDomain);
    }

    /**
     * This method can be used to get the status of the application. eg : deployed, build etc
     *
     * @param applicationId application id
     * @param version       version of the application
     * @param stage         stage of the application
     * @return status of the application
     * @throws AppFactoryException
     */
    public String getApplicationStatus(String applicationId, String version, String stage) throws AppFactoryException {
        try {
            return applicationDAO.getDeployStatus(applicationId, version, stage, false, null).getLastDeployedStatus();
        } catch (AppFactoryException e) {
            String message = "Error while retrieving application status for the application id : " + applicationId
                             + " with version : " + version + " in stage : " + stage;
            log.error(message, e);
            throw new AppFactoryException(message, e);
        }
    }

    /**
     * Method to get the stage of a given application of given version
     *
     * @param applicationId id of the application
     * @param version version of the application
     * @return
     * @throws AppFactoryException
     */
    public String getStage(String applicationId, String version) throws AppFactoryException {
        try {
            // Getting the tenant domain
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

            return RxtManager.getInstance().getStage(applicationId, version, tenantDomain);
        } catch (AppFactoryException e) {
            String msg = "Unable to get stage of application : " + applicationId + " with version : " + version;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
    }

    /**
     * Get array of artifacts for a given application of given user
     *
     * @param domainName    tenant domain
     * @param applicationId application id
     * @param userName      user name
     * @return array of artifacts
     * @throws AppFactoryException
     */
    public Artifact[] getAllVersionsOfApplicationPerUser(String domainName, String applicationId, String userName)
            throws AppFactoryException {
        Artifact[] artifacts;
        try {
            List<Artifact> artifactsList = RxtManager
                    .getInstance().getRepoUserRxtForApplicationOfUser(domainName, applicationId, userName);
            artifacts = artifactsList.toArray(new Artifact[artifactsList.size()]);
            return artifacts;
        } catch (RegistryException e) {
            String msg = "Error while retrieving artifact information for application id : " + applicationId
                         + "of user : " + userName + " of tenant domain : " + domainName;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
    }

    /**
     * Get array of artifacts of all versions of a given application
     *
     * @param domainName    tenant domain
     * @param applicationId application id
     * @return array of version artifacts
     * @throws AppFactoryException
     */
    public Artifact[] getAllVersionsOfApplication(String domainName, String applicationId) throws AppFactoryException {

        Artifact[] artifacts;
        try {
            List<Artifact> artifactsList = RxtManager.getInstance()
                    .getAppVersionRxtForApplication(domainName, applicationId);
            artifacts = artifactsList.toArray(new Artifact[artifactsList.size()]);
            return artifacts;
        } catch (RegistryException e) {
            String msg = "Error while retrieving artifact information from registry for the application id : "
                         + applicationId + " tenant domain : " + domainName;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
    }

    /**
     * This method returns the build and deploy status
     *
     * @param applicationId application to check the build and deploy status
     * @param tenantDomain  tenant domain that application belongs to
     * @param version       version of the application to check
     * @return return last build id, build status [successful/unsuccessful] last deployed build id
     * @throws AppFactoryException
     */
    public BuildandDeployStatus getBuildandDelpoyedStatus(String applicationId, String tenantDomain, String version)
            throws AppFactoryException {
        BuildStatus buildStatus = applicationDAO.getBuildStatus(applicationId, version, false, null);
        DeployStatus deployStatus;
        String stage = RxtManager.getInstance().getStage(applicationId, version, tenantDomain);
        deployStatus = applicationDAO.getDeployStatus(applicationId, version, stage, false, null);
        BuildandDeployStatus buildandDeployStatus = new BuildandDeployStatus(buildStatus.getLastBuildId(),
                                                                             buildStatus.getLastBuildStatus(),
                                                                             deployStatus.getLastDeployedId());
        return buildandDeployStatus;
    }


    /**
     * Method to make the given application set to auto build.
     *
     * @param applicationId   application id
     * @param stage           stage of the application
     * @param version         version of the application
     * @param isAutoBuildable auto buildable true or false
     * @throws AppFactoryException
     */
    public void publishSetApplicationAutoBuild(String applicationId, String stage, String version,
                                               boolean isAutoBuildable) throws AppFactoryException {
        updateRxtWithBuildStatus(applicationId, stage, version, isAutoBuildable, getTenantDomain());
        JenkinsCISystemDriver jenkinsCISystemDriver =
                (JenkinsCISystemDriver) Util.getContinuousIntegrationSystemDriver();
        AppFactoryConfiguration configuration = ServiceContainer.getAppFactoryConfiguration();
        int pollingPeriod = Integer
                .parseInt(configuration.getFirstProperty(AppFactoryConstants.DEPLOYMENT_STAGES +
                                                         AppFactoryConstants.DOT_SEPERATOR + stage +
                                                         AppFactoryConstants.DOT_SEPERATOR +
                                                         AppFactoryConstants.AUTOMATIC_DEPLOYMENT_POLLING_PERIOD));
        jenkinsCISystemDriver
                .setJobAutoBuildable(applicationId, version, isAutoBuildable, pollingPeriod, getTenantDomain());
        if (log.isDebugEnabled()) {
            log.debug("Auto build change event successful for : " + applicationId + " with version : " + version +
                      " in stage : " + stage + " with isAutoBuildable : " + isAutoBuildable);
        }
    }

    /**
     * Updates the rxt in registry with given auto build information.
     *
     * @param applicationId   application id
     * @param stage           stage of the application
     * @param version         version of the application
     * @param isAutoBuildable is auto buildable true or false
     * @param tenantDomain    tenant domain
     * @throws AppFactoryException
     */
    private void updateRxtWithBuildStatus(String applicationId, String stage, String version, boolean isAutoBuildable,
                                          String tenantDomain) throws AppFactoryException {
        try {
            RxtManager.getInstance().updateAppVersionRxt(applicationId, version,
                                                         AppFactoryConstants.RXT_KEY_APPVERSION_ISAUTOBUILD,
                                                         String.valueOf(isAutoBuildable), tenantDomain);
            if (log.isDebugEnabled()) {
                log.debug("Rtx updated successfully for application : " + applicationId + " with version : " + version
                          + " in stage :" + stage + " with isAutoBuildable :" + isAutoBuildable);
            }
        } catch (AppFactoryException e) {
            String msg = "Error occurred while updating the rxt with auto-build status : " + isAutoBuildable +
                         " for the application : " + applicationId + " with version : " + version + " in stage : " +
                         " of tenant domain : " + tenantDomain;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
    }

    /**
     * Method to make the given application set to auto deploy.
     *
     * @param applicationId    application id
     * @param stage            stage of the application
     * @param version          version of the application
     * @param isAutoDeployable is auto deploy true or false
     * @throws AppFactoryException
     */
    public void publishSetApplicationAutoDeploy(String applicationId, String stage, String version,
                                                boolean isAutoDeployable) throws AppFactoryException {

        try {
            updateRxtWithDeploymentStatus(applicationId, version, isAutoDeployable, getTenantDomain());
            String applicationType = AppFactoryCoreUtil.getApplicationType(applicationId, getTenantDomain());
            boolean appIsBuildable = AppFactoryCoreUtil.isBuildable(applicationType);
            if (appIsBuildable) {
                JenkinsCISystemDriver jenkinsCISystemDriver =
                        (JenkinsCISystemDriver) Util.getContinuousIntegrationSystemDriver();
                jenkinsCISystemDriver.setJobAutoDeployable(applicationId, version, isAutoDeployable, getTenantDomain());
            }
            if (log.isDebugEnabled()) {
                log.info("Auto deploy change event successful for : " + applicationId + " with version : " + version
                         + " in stage : " + stage + " with isAutoBuildable : " + isAutoDeployable);
            }
        } catch (RegistryException e) {
            String msg = "Error occurred while updating registry for the application : " + applicationId + " with " +
                         "version : " + version + " in stage : " + stage;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
    }

    /**
     * This method is used to update rxt with deployment status.
     *
     * @param applicationId    application id
     * @param version          version of the application
     * @param isAutoDeployable is auto deploy enabled or not
     * @param tenantDomain     tenant domain
     * @throws AppFactoryException
     */
    private void updateRxtWithDeploymentStatus(String applicationId, String version, boolean isAutoDeployable,
                                               String tenantDomain) throws AppFactoryException {
        RxtManager.getInstance().updateAppVersionRxt(applicationId, version,
                                                     AppFactoryConstants.RXT_KEY_APPVERSION_ISAUTODEPLOY,
                                                     String.valueOf(isAutoDeployable), tenantDomain);
    }

    /**
     * Updating Rxt value when doing the promote action.
     *
     * @param applicationId id of the application
     * @param stage         current stage of the application
     * @param version       version of the application
     * @param action        promote action
     * @throws ApplicationManagementException
     */
    public void updateRxtWithPromoteState(String applicationId, String stage, String version, String action,
                                          String state) throws AppFactoryException {
        if (action == null || !action.equals(AppFactoryConstants.RXT_KEY_APPVERSION_PROMOTE)) {
            return;
        }
        applicationDAO.updatePromoteStatusOfVersion(applicationId, version, state);
        if (log.isDebugEnabled()) {
            log.debug("Promote status updated successfully for the application : " + applicationId + " with version : "
                      + version + " in stage : " + stage + " with state : " + state);
        }
    }

    /**
     * This method is used to delete an application.
     *
     * @param application application object of the application needed to be deleted
     * @param userName    user name of the user who requests/executes the operation
     * @param domainName  tenant domain name
     * @return return whether operation is successful or not
     * @throws AppFactoryException
     * @throws ApplicationManagementException
     */
    public boolean deleteApplication(Application application, String userName, String domainName)
            throws AppFactoryException, ApplicationManagementException {
        boolean completedSuccessfully = true;
        String applicationId = application.getId();
        Iterator<ApplicationEventsHandler> appEventListeners = Util.getApplicationEventsListeners().iterator();
        org.wso2.carbon.appfactory.tenant.mgt.beans.UserInfoBean[] userList =
                new ApplicationUserManagementService().getUsersOftheApplication(applicationId);
        ApplicationEventsHandler listener;
        while (appEventListeners.hasNext()) {
            listener = appEventListeners.next();
            try {
                if (listener.hasExecuted(application, userName, domainName)) {
                    listener.onDeletion(application, userName, domainName);
                }
            } catch (AppFactoryException e) {
                // we don't throw error here since while loop shouldn't be broken.
                log.error("Error in calling onDeletion method of ApplicationEventsListener : " + listener, e);
            }
        }
        try {
            removeApplicationRoles(applicationId, domainName);
        } catch (UserStoreException e) {
            String msg = "Error while removing the application roles from LDAP for application : " + applicationId;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        try {
            RxtApplicationDAO.getInstance().deleteApplicationArtifact(applicationId, domainName);
        } catch (UserStoreException e) {
            String msg = "Error while deleting the application resource from registry for application : " +
                         applicationId;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (RegistryException e) {
            String msg = "Error while deleting the application resource from registry for application : " +
                         applicationId;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        applicationDAO.deleteApplication(applicationId);
        String title = "Application " + applicationId + " is deleted successfully";
        String messageDescription = "Deleted by: " + userName;
        try {
            for (org.wso2.carbon.appfactory.tenant.mgt.beans.UserInfoBean anUserList : userList) {
                EventNotifier.getInstance().notify(AppCreationEventBuilderUtil.buildApplicationDeletionEventForUser(
                        anUserList.getUserName().concat("@").concat(domainName), title, messageDescription, userName,
                        Event.Category.INFO));
            }
            EventNotifier.getInstance().notify(AppCreationEventBuilderUtil.buildApplicationDeletionEventForApplication(
                    applicationId, title, messageDescription, userName, Event.Category.INFO));
        } catch (AppFactoryEventException e) {
            //we dont throw error here since this is a event notifier.
            log.error("Failed to notify application deletion event for the application : " + applicationId, e);
        }
        return completedSuccessfully;
    }

    /**
     * This method can be used to remove the application related roles added to LDAP
     *
     * @param applicationKey application key
     * @param tenantDomain   tenant domain
     * @return boolean saying whether the operation is success or not
     * @throws UserStoreException
     */
    private boolean removeApplicationRoles(String applicationKey, String tenantDomain) throws UserStoreException {
        RealmService realmService = Util.getRealmService();
        TenantManager tenantManager = realmService.getTenantManager();
        PrivilegedCarbonContext threadLocalCarbonContext;
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
        return true;
    }

    /**
     * Gets application information of the user to populate the user home
     *
     * @param userName username of the user for whom application summary list needs to be loaded
     * @return ApplicationSummary[]
     * @throws ApplicationManagementException
     */
    public ApplicationSummary[] getApplicationSummaryForUser(String userName)
            throws ApplicationManagementException {

        String[] applicationKeys = getApplicationKeysOfUser(userName);
        String tenantDomain = getTenantDomain();
        List<ApplicationSummary> applicationSummaryList = new ArrayList<ApplicationSummary>();
        try {
            applicationSummaryList =
                    RxtApplicationDAO.getInstance().getSummarizedApplicationInfo(applicationKeys);
        } catch (AppFactoryException e) {
            String msg =
                    "Error while getting application summary info for user " + userName +
                    " of tenant" + tenantDomain;
            log.error(msg, e);
        }
        ApplicationsOfUserCache applicationsOfUserCache =  new ApplicationsOfUserCache();
        if (applicationsOfUserCache.isUserInvitedToApplication(userName)) {
            applicationsOfUserCache.clearCacheForUserName(userName);
        }
        return
            applicationSummaryList.toArray(new ApplicationSummary[applicationSummaryList.size()]);
    }


    /**
     * Returns the list of applications that user belongs to
     *
     * @param userName
     * @return <b>Application</b> Array
     * @throws ApplicationManagementException
     */
    public Application[] getApplicaitonsOfTheUser(String userName)
            throws ApplicationManagementException {
        long startTime = System.currentTimeMillis();
        try {
            Application[] apps = RxtApplicationDAO.getInstance().getAllApplicationsOfUser(userName);
            long endTime = System.currentTimeMillis();
            if (perfLog.isDebugEnabled()) {
                perfLog.debug("AFProfiling getApplicaitonsOfTheUser :" + (endTime - startTime) );
            }
            return apps;
        } catch (AppFactoryException e) {
            String message = "Failed to retrieve applications of the user" + userName;
            log.error(message, e);
            throw new ApplicationManagementException(message, e);
        }
    }

    /**
     * Lightweight method to get application keys of the applications of user
     * @param userName
     * @return String array of applicaiton keys
     * @throws ApplicationManagementException
     */
    public String[] getApplicationKeysOfUser(String userName) throws ApplicationManagementException{
        CarbonContext context = CarbonContext.getThreadLocalCarbonContext();
        ArrayList<String> applications = new ArrayList<String>();
        try {
            String[] roles =
                    context.getUserRealm().getUserStoreManager()
                            .getRoleListOfUser(userName);
            for (String role : roles) {
                if (AppFactoryUtil.isAppRole(role)) {
                    try {
                        String appkeyFromPerAppRoleName =
                                AppFactoryUtil.getAppkeyFromPerAppRoleName(role);
                        applications.add(appkeyFromPerAppRoleName);
                    } catch (AppFactoryException e) {
                        // ignore exception here because isAppRole check avoids this exception being thrown..
                    }
                }
            }
            return applications.toArray(new String[applications.size()]);
        } catch (UserStoreException e) {
            String message = "Failed to retrieve applications of the user" + userName;
            log.error(message,e);
            throw new ApplicationManagementException(message, e);
        }

    }

    /**
     * Returns all the applications created by a particular user.
     *
     * @param userName user name of the user with domain eg: user@tenant.com
     * @return <Application> array
     * @throws ApplicationManagementException
     */
    public Application[] getApplicationsCreatedByUser(String userName) throws ApplicationManagementException {
        try {
            return RxtApplicationDAO.getInstance().getAllApplicationsCreatedByUser(userName);
        } catch (AppFactoryException e) {
            throw new ApplicationManagementException("Failed to retrieve applications created by the user" +
                                                     userName, e);
        }
    }


}
