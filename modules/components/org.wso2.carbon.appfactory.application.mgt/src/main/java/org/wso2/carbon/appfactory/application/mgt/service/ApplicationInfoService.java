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
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.ApplicationEventsHandler;
import org.wso2.carbon.appfactory.core.cache.ApplicationsOfUserCache;
import org.wso2.carbon.appfactory.core.dao.JDBCAppVersionDAO;
import org.wso2.carbon.appfactory.core.dao.JDBCApplicationDAO;
import org.wso2.carbon.appfactory.core.dto.Version;
import org.wso2.carbon.appfactory.core.dto.Application;
import org.wso2.carbon.appfactory.core.dto.ApplicationSummary;
import org.wso2.carbon.appfactory.core.dto.BuildStatus;
import org.wso2.carbon.appfactory.core.dto.BuildandDeployStatus;
import org.wso2.carbon.appfactory.core.dto.DeployStatus;
import org.wso2.carbon.appfactory.core.governance.RxtManager;
import org.wso2.carbon.appfactory.core.dao.ApplicationDAO;
import org.wso2.carbon.appfactory.core.util.AppFactoryCoreUtil;
import org.wso2.carbon.appfactory.core.util.Constants;
import org.wso2.carbon.appfactory.eventing.AppFactoryEventException;
import org.wso2.carbon.appfactory.eventing.Event;
import org.wso2.carbon.appfactory.eventing.EventNotifier;
import org.wso2.carbon.appfactory.eventing.builder.utils.AppCreationEventBuilderUtil;
import org.wso2.carbon.appfactory.jenkins.build.JenkinsCISystemDriver;
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
 */
public class ApplicationInfoService {
    private static final Log perfLog = LogFactory.getLog("org.wso2.carbon.appfactory.perf.application.load");
    private static Log log = LogFactory.getLog(ApplicationInfoService.class);


    /**
     * Get the application information bean of the application
     *
     * @param applicationKey
     * @return
     * @throws AppFactoryException
     */
    private Application getBasicApplicationInfo(String applicationKey) throws AppFactoryException {
        Application application = ApplicationDAO.getInstance().getApplicationInfo(applicationKey);
        return application;
    }

    /**
     * Gets application information of the user to populate the user home
     *
     * @param userName user name
     * @return
     * @throws ApplicationManagementException
     */
    public Application[] getApplicationInfoForUser(String userName) throws ApplicationManagementException {
        List<Application> appInfoList = new ArrayList<Application>();
        String[] applicationKeys = getApplicationKeysOfUser(userName);
        for (String applicationKey : applicationKeys) {
            try {
                Application application = ApplicationDAO.getInstance().getApplicationInfo(applicationKey);
                appInfoList.add(application);
            } catch (AppFactoryException e) {
                String msg = "Error while getting application info for user : " + userName + " of tenant : " +
                             getTenantDomain();
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
        Map<String, Constants.ApplicationCreationStatus> applicationCreationStatus = null;
        try {
            if (ArrayUtils.isNotEmpty(applicationKeys)) {
                applicationCreationStatus = JDBCApplicationDAO.getInstance().getApplicationCreationStatusByKeys
                        (applicationKeys);
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
        return ApplicationDAO.getInstance().getApplicationType(applicationId);
    }


    /**
     * Add the artifacts to registry such as application/appversion
     *
     * @param key  application/appversion
     * @param info metadata information
     * @param lifecycleAttribute
     * @return artifact path inregistry
     * @throws AppFactoryException
     */
    public String addArtifact(String key, String info, String lifecycleAttribute) throws AppFactoryException {
       RegistryUtils.recordStatistics(key, info, lifecycleAttribute);
       if(key.equals(AppFactoryConstants.RXT_KEY_APPINFO_APPLICATION)){
          return ApplicationDAO.getInstance().addApplicationArtifact(info, lifecycleAttribute);
       }else if(key.equals(AppFactoryConstants.RXT_KEY_APPVERSION)){
           return RxtManager.getInstance().addArtifact(key, info,lifecycleAttribute);
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
        return ApplicationDAO.getInstance().getApplicationInfo(applicationId);
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
            return JDBCApplicationDAO.getInstance().getDeployStatus(applicationId, version, stage, false, null).
                    getLastDeployedStatus();
        } catch (AppFactoryException e) {
            String message = "Error while retrieving application status for the application id : " + applicationId
                             + " with version : " + version + " in stage : " + stage;
            log.error(message, e);
            throw new AppFactoryException(message, e);
        }
    }

    /**
     * Method to get the stage of a given application version
     *
     * @param applicationId id of the application
     * @param version       version of the application
     * @return              the stage
     * @throws AppFactoryException
     */
    public String getStage(String applicationId, String version) throws AppFactoryException {
        try {
            return JDBCAppVersionDAO.getInstance().getAppVersionStage(applicationId, version);
        } catch (AppFactoryException e) {
            String msg = "Unable to get stage for the application id : " + applicationId + " and version : " + version
                         +" of tenant " + getTenantDomain();
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
    public Version[] getAllVersionsOfApplicationPerUser(String domainName, String applicationId, String userName)
            throws AppFactoryException {
        try {
            JDBCAppVersionDAO appVersionsDAO = JDBCAppVersionDAO.getInstance();
            JDBCApplicationDAO applicationDAO = JDBCApplicationDAO.getInstance();
            BuildStatus buildStatus;
            DeployStatus deployStatus;
            ArrayList<Version> versionList = appVersionsDAO.getAllApplicationVersionsOfUser(applicationId,
                                                                                            userName.split("@")[0]);
            for (Version version : versionList) {
                buildStatus = applicationDAO.getBuildStatus(applicationId, version.getVersion(), true, userName.split("@")[0]);
                version.setLastBuildStatus(
                        "build " + buildStatus.getLastBuildId() + " " + buildStatus.getLastBuildStatus());
                deployStatus = applicationDAO.getDeployStatus(applicationId, version.getVersion(),
                                                              AppFactoryConstants.ApplicationStage.
                                                                      DEVELOPMENT.getStageStrValue(), true, userName.split("@")[0]);
                version.setLastDeployedId(deployStatus.getLastDeployedId());
            }
            return versionList.toArray(new Version[versionList.size()]);
        } catch (AppFactoryException e) {
            String msg = "Error while retrieving all the versions of application : " + applicationId + " of user : " +
                         userName + " from database";
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
    //TODO Modify this method with new UX improvements. Simply use Version name list String list. Must do item.
    public Version[] getAllVersionsOfApplication(String domainName, String applicationId) throws AppFactoryException {
        try {
            JDBCAppVersionDAO appVersionsDAO = JDBCAppVersionDAO.getInstance();
            JDBCApplicationDAO applicationDAO = JDBCApplicationDAO.getInstance();
            ArrayList<Version> versionList = appVersionsDAO.getAllVersionsOfApplication(applicationId);

            for(Version version : versionList){
                BuildStatus buildStatus = applicationDAO.getBuildStatus(applicationId, version.getVersion(), false, null);
                version.setLastBuildStatus("build " + buildStatus.getLastBuildId() + " " + buildStatus.getLastBuildStatus());

                DeployStatus deployStatus = applicationDAO.getDeployStatus(applicationId, version.getVersion(),
                                                                           AppFactoryConstants.ApplicationStage.DEVELOPMENT.getStageStrValue(),
                                                                           false, null);
                version.setLastDeployedId(deployStatus.getLastDeployedId());
            }

            return versionList.toArray(new Version[versionList.size()]);
        } catch (AppFactoryException e) {
            String msg =
                    "Error while retrieving all versions of application : " + applicationId +
                    " from database in tenant domain : " + domainName;
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
        BuildStatus buildStatus = JDBCApplicationDAO.getInstance().getBuildStatus(applicationId, version, false, null);
        DeployStatus deployStatus;
        String stage = JDBCAppVersionDAO.getInstance().getAppVersionStage(applicationId, version);
        deployStatus = JDBCApplicationDAO.getInstance().getDeployStatus(applicationId, version, stage, false, null);
        return new BuildandDeployStatus(buildStatus.getLastBuildId(),
                                                 buildStatus.getLastBuildStatus(),  deployStatus.getLastDeployedId());
    }


    /**
     * Updates the database tables with given auto build information.
     *
     * @param applicationId   application id
     * @param stage           stage of the application
     * @param version         version of the application
     * @param isAutoBuildable auto buildable true or false
     * @throws AppFactoryException
     */
    private void updateDBWithAutoBuildStatus(String applicationId, String stage, String version, boolean isAutoBuildable)
                                                                    throws ApplicationManagementException {
        try {
            JDBCAppVersionDAO.getInstance().updateAutoBuildStatusOfVersion(applicationId, version, isAutoBuildable);
            if (log.isDebugEnabled()) {
                log.debug(" Database updated successfully for application id : " + applicationId + " " + " version : "
                          + version + " stage :" + stage + " isAutoBuildable :" + isAutoBuildable);
            }
        } catch (AppFactoryException e) {
            String msg = "Error occurred while updating the database with auto-build status for application id : "
                         + applicationId + " " + " version : "+ version + " stage :" + stage + " isAutoBuildable :"
                         + isAutoBuildable;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
    }

    /**
     * Service method to make the application related to given {@code applicationId} auto build.
     *
     * @param applicationId    application id
     * @param stage            stage of the application
     * @param version          version of the application
     * @param isAutoBuildable is auto build true or false
     * @throws ApplicationManagementException
     */
    public void publishSetApplicationAutoBuild(String applicationId, String stage, String version,
                                               boolean isAutoBuildable) throws ApplicationManagementException {
        log.info("Auto build change event received for application id : " + applicationId + " " + " Version : "
                 + version +" stage :" + stage + " isAutoBuildable :" + isAutoBuildable);

        // Getting the tenant domain
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        updateDBWithAutoBuildStatus(applicationId, stage, version, isAutoBuildable);
        try {
            JenkinsCISystemDriver jenkinsCISystemDriver = (JenkinsCISystemDriver)
                                                            Util.getContinuousIntegrationSystemDriver();

            int pollingPeriod = 6; // TODO this from configuration
            jenkinsCISystemDriver.setJobAutoBuildable(applicationId, version, isAutoBuildable, pollingPeriod,
                                                      CarbonContext.getThreadLocalCarbonContext().getTenantDomain());
            log.info("Application id : " + applicationId + " successfully configured for auto building " + isAutoBuildable);
        } catch (AppFactoryException e) {
            String msg = "Error occurred while updating jenkins configuration for auto build : " + isAutoBuildable
                         +" for application id : "+ applicationId + "for tenant domain: " + tenantDomain;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
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
                                                boolean isAutoDeployable) throws ApplicationManagementException {

        // Getting the tenant domain
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        updateDBAutoDeploymentStatus(applicationId, stage, version, isAutoDeployable);
        try {
            String applicationType = AppFactoryCoreUtil.getApplicationType(applicationId, tenantDomain);
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
        } catch (AppFactoryException e) {
            String msg = "Error occurred while updating jenkins configuration for auto deploy: " + isAutoDeployable
                         +" for application id : "+ applicationId + "for tenant domain: " + tenantDomain;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (RegistryException e) {
            String msg = "Error occurred while reading application type from registry for application id : "
                         + applicationId + "for tenant domain: " + tenantDomain;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
    }

    /**
     * Updates the rxt registry with given auto deploy information.
     *
     * @param applicationId id of the application
     * @param stage current stage of the application
     * @param version
     * @param isAutoDeployable
     * @throws ApplicationManagementException
     */
    private void updateDBAutoDeploymentStatus(String applicationId, String stage, String version,
                                              boolean isAutoDeployable) throws ApplicationManagementException {
        try {
            JDBCAppVersionDAO.getInstance().updateAutoDeployStatusOfVersion(applicationId, version, isAutoDeployable);
            if (log.isDebugEnabled()) {
                log.debug(" DB updated successfully for application id : " + applicationId + " " + " version : "
                          + version +" stage :" + stage + " isAutoDeployable :" + isAutoDeployable);
            }
        } catch (AppFactoryException e) {
            String msg = "Error occurred while updating the database with auto-build status for application id : "
                         + applicationId + " " + " version : "+ version +" stage :" + stage + " isAutoDeployable :"
                         + isAutoDeployable;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
    }

    /**
     * Updating DB value when doing the promote action.
     *
     * @param applicationId id of the application
     * @param stage         current stage of the application
     * @param version       version of the application
     * @param action        promote action
     * @throws ApplicationManagementException
     */

    //TODO rename method as rxt is not involved anymore
    public void updateRxtWithPromoteState(String applicationId, String stage, String version, String action,
                                          String state) throws ApplicationManagementException {
        if (action == null || !action.equals(AppFactoryConstants.RXT_KEY_APPVERSION_PROMOTE)) {
            return;
        }

        try {
            JDBCAppVersionDAO.getInstance().updatePromoteStatusOfVersion(applicationId, version, state);
            if (log.isDebugEnabled()) {
                log.debug("Successfully updated Promote status as Pending for application id : " + applicationId +
                          " version : "+ version + " stage :" + stage);
            }
        } catch (AppFactoryException e) {
            String msg = "Error occurred while updating with promote status for application id : " + applicationId +
                         " version : "+ version + " stage :" + stage;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
    }


    /**
     * Updating the stage based on application promote/demote
     *
     * @param applicationId
     * @param stage
     * @param version
     * @throws ApplicationManagementException
     */
    public void updateCurrentStage(String applicationId, String stage, String version)
            throws ApplicationManagementException {
        try {
            JDBCAppVersionDAO.getInstance().updateStageOfVersion(applicationId, version, stage);
            if (log.isDebugEnabled()) {
                log.debug(" Successfully updated stage to " + stage + " for application id : " + applicationId
                          + " version : " + version);
            }
        } catch (AppFactoryException e) {
            String msg = "Error occurred while updating the stage to " +stage + " for application id : " + applicationId
                         + " version : " + version;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
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
            String msg = "Error while removing the application roles from LDAP for application id : " + applicationId;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        try {
            ApplicationDAO.getInstance().deleteApplicationArtifact(applicationId, domainName);
        } catch (UserStoreException e) {
            String msg = "Error while deleting the application resource from registry for application id : " +
                         applicationId;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (RegistryException e) {
            String msg = "Error while deleting the application resource from registry for application id : " +
                         applicationId;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        JDBCApplicationDAO.getInstance().deleteApplication(applicationId);
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

            //we don't throw error here since this is a event notifier.
            log.error("Failed to notify application deletion event for the application id : " + applicationId, e);
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
                    ApplicationDAO.getInstance().getSummarizedApplicationInfo(applicationKeys);
        } catch (AppFactoryException e) {
            String msg =
                    "Error while getting application summary info for user " + userName +
                    " of tenant" + tenantDomain;
            log.error(msg, e);
        }
        ApplicationsOfUserCache applicationsOfUserCache = new ApplicationsOfUserCache();
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
            Application[] apps = ApplicationDAO.getInstance().getAllApplicationsOfUser(userName);
            long endTime = System.currentTimeMillis();
            if (perfLog.isDebugEnabled()) {
                perfLog.debug("AFProfiling getApplicaitonsOfTheUser : " + (endTime - startTime));
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
     *
     * @param userName
     * @return String array of applicaiton keys
     * @throws ApplicationManagementException
     */
    public String[] getApplicationKeysOfUser(String userName) throws ApplicationManagementException {
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
            log.error(message, e);
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
            return ApplicationDAO.getInstance().getAllApplicationsCreatedByUser(userName);
        } catch (AppFactoryException e) {
            throw new ApplicationManagementException("Failed to retrieve applications created by the user" +
                                                     userName, e);
        }
    }

    /**
     *Publish Application Version Creation
     * @param applicationId
     * @param sourceVersion
     * @param targetVersion
     * @throws ApplicationManagementException
     */
    public void publishApplicationVersionCreation(String applicationId, String sourceVersion,
                                                  String targetVersion) throws ApplicationManagementException {
        try {

            // Getting the tenant ID from the CarbonContext since this is called
            // as a SOAP service.
            CarbonContext threadLocalCarbonContext = CarbonContext.getThreadLocalCarbonContext();
            String domainName = threadLocalCarbonContext.getTenantDomain();
            String userName = threadLocalCarbonContext.getUsername();

            Application application = ApplicationDAO.getInstance().getApplicationInfo(applicationId);
            String applicationType = AppFactoryCoreUtil.getApplicationType(applicationId, domainName);

            Version version = AppFactoryCoreUtil.isUplodableAppType(application.getType()) ?
                              new Version(targetVersion, AppFactoryConstants.ApplicationStage.PRODUCTION
                                      .getCapitalizedString()) : new Version(targetVersion, AppFactoryConstants.
                    ApplicationStage.DEVELOPMENT.getCapitalizedString());
            JDBCAppVersionDAO.getInstance().addVersion(applicationId, version);

            // find the versions.
            Version source = JDBCAppVersionDAO.getInstance().getApplicationVersion(applicationId, sourceVersion);
            Version target = JDBCAppVersionDAO.getInstance().getApplicationVersion(applicationId, targetVersion);

            Iterator<ApplicationEventsHandler> appEventListeners = Util.getApplicationEventsListeners().iterator();
            ApplicationEventsHandler listener = null;
            while (appEventListeners.hasNext()) {
                try {
                    listener = appEventListeners.next();
                    listener.onVersionCreation(application, source, target, domainName, userName);
                } catch (Throwable e) {
                    log.error("Error while executing onVersionCreation method of ApplicationEventsListener: "
                              + listener, e);
                }
            }

        } catch (AppFactoryException ex) {
            String errorMsg = "Unable to publish version creation for application id : " + applicationId
                              + " source version : " + sourceVersion + " target version : " +targetVersion;
            log.error(errorMsg, ex);
            throw new ApplicationManagementException(errorMsg, ex);
        } catch (RegistryException e) {
            log.error(e);
            throw new ApplicationManagementException(e);
        }
    }

}
