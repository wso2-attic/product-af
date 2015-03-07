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

package org.wso2.carbon.appfactory.application.mgt.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.application.mgt.util.Util;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.bam.BamDataPublisher;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.ApplicationEventsHandler;
import org.wso2.carbon.appfactory.core.dto.Application;
import org.wso2.carbon.appfactory.core.dto.UserInfo;
import org.wso2.carbon.appfactory.core.governance.ApplicationManager;
import org.wso2.carbon.appfactory.eventing.AppFactoryEventException;
import org.wso2.carbon.appfactory.eventing.Event;
import org.wso2.carbon.appfactory.eventing.EventNotifier;
import org.wso2.carbon.appfactory.eventing.builder.utils.UserManagementEventBuilderUtil;
import org.wso2.carbon.appfactory.tenant.mgt.beans.UserInfoBean;
import org.wso2.carbon.appfactory.tenant.mgt.service.TenantManagementException;
import org.wso2.carbon.appfactory.tenant.mgt.service.TenantManagementService;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;

import java.util.ArrayList;
import java.util.Iterator;

public class ApplicationUserManagementService {
    private static final Log log = LogFactory.getLog(ApplicationUserManagementService.class);

    private static final Log perfLog = LogFactory.getLog("org.wso2.carbon.appfactory.perf.application.load");

    /**
     * get user info beans of the users of the Application
     * 
     * @param applicationKey
     * @return UserInfoBean that contains user name,email,roles etc
     * @throws ApplicationManagementException
     */
    public UserInfoBean[] getUsersOftheApplication(String applicationKey)
                                                                         throws ApplicationManagementException {
        String applicationRole = AppFactoryUtil.getRoleNameForApplication(applicationKey);
        ArrayList<UserInfoBean> users = new ArrayList<UserInfoBean>();
        try {
            TenantManagementService tenantManagementService = Util.getTenantManagementService();
            String[] usersOfApplication =
                                          CarbonContext.getThreadLocalCarbonContext()
                                                       .getUserRealm().getUserStoreManager()
                                                       .getUserListOfRole(applicationRole);
            for (String user : usersOfApplication) {
                if(tenantManagementService!=null){
                    users.add(tenantManagementService.getUserInfo(user));
                }

            }
            return users.toArray(new UserInfoBean[users.size()]);
        } catch (UserStoreException e) {
            String message = "Failed to retirve list of users for application " + applicationKey;
            log.error(message,e);
            throw new ApplicationManagementException(message, e);
        } catch (TenantManagementException e) {
            String message = "Failed to retirve list of users for application " + applicationKey;
            log.error(message,e);
            throw new ApplicationManagementException(message, e);
        }

    }

    /**
     * Add user to the application in the organization
     * 
     * @param userNames
     * @param applicationKey
     * @return
     * @throws ApplicationManagementException
     */
    public boolean addUsersToApplication(String userNames[], String applicationKey)
                                                                                   throws ApplicationManagementException {
        String applicationRole = AppFactoryUtil.getRoleNameForApplication(applicationKey);
        String separator = ", ";
        String userNameStr = concatUserNames(userNames, separator);
        try {
            CarbonContext threadLocalCarbonContext = CarbonContext.getThreadLocalCarbonContext();
            String tenantDomain=threadLocalCarbonContext.getTenantDomain();
            //passing new String[]{} since null is not handled in doupdateUserListOfRole() method in ReadWriteLDAPUserStoreManager
            threadLocalCarbonContext.getUserRealm().getUserStoreManager()
                         .updateUserListOfRole(applicationRole, new String[]{}, userNames);
            Iterator<ApplicationEventsHandler> applicationEventsListeners = Util.getApplicationEventsListeners().iterator();
            
            while (applicationEventsListeners.hasNext()) {
                ApplicationEventsHandler applicationEventsListener =
                                                                      (ApplicationEventsHandler) applicationEventsListeners.next();
                for(String userName: userNames){
                    applicationEventsListener.onUserAddition(ApplicationManager.getInstance().getApplicationInfo(applicationKey), new UserInfo(userName), tenantDomain);

                }
            }

            //Notify to App wall
            try {

                String infoMessage = userNameStr + " invited to the application";

                EventNotifier.getInstance().notify(UserManagementEventBuilderUtil.buildUserAdditionToAppEvent(applicationKey, infoMessage,
                        "", Event.Category.INFO));
            } catch (AppFactoryEventException exception) {
                log.error("Failed to notify the successful user addition to application event ", exception);
                // do not throw again.
            }

            // Publish stats to BAM
            publishBAMStats(userNames, applicationKey, tenantDomain, AppFactoryConstants.BAM_ADD_DATA);

            return true;
        } catch (UserStoreException e) {

            try {
                String error = "Error in adding " + userNameStr + " to the application";
                EventNotifier.getInstance().notify(UserManagementEventBuilderUtil.buildUserAdditionToAppEvent(applicationKey, error,
                        e.getMessage(), Event.Category.ERROR));
            } catch (AppFactoryEventException exception) {
                log.error("Failed to notify the failure of user addition to app event ", exception);
                // do not throw again.
            }

            String message = "Failed to add user " + userNames + " to the application " + applicationKey;
            log.error(message,e);
            throw new ApplicationManagementException(message, e);
        } catch (AppFactoryException e) {
            try {
                String error = "Error in adding " + userNameStr + " to application";
                EventNotifier.getInstance().notify(UserManagementEventBuilderUtil.buildUserAdditionToAppEvent(applicationKey,
                        error, "", Event.Category.ERROR));
            } catch (AppFactoryEventException exception) {
                log.error("Failed to notify the failure of user addition to app event ", exception);
                // do not throw again.
            }

            String message = "Failed to add " + userNames + " to application " + applicationKey;
            log.error(message,e);
            throw new ApplicationManagementException(message, e);
        }

    }

    private String concatUserNames(String[] userNames, String separator){

        int userNamesCount = userNames.length;
        String userNameStr = "";
        if (userNamesCount > 0) {
            userNameStr = userNames[0];    // start with the first element
            for (int i=1; i<userNamesCount; i++) {
                userNameStr = userNameStr + separator + userNames[i];
            }
        }
        return userNameStr;
    }

    private void publishBAMStats(String[] userNames, String applicationKey, String tenantDomain, String action) throws AppFactoryException {

        log.info("Publishing users update stats to bam");
        String tenantId = null;

        try {
            tenantId = "" + Util.getRealmService().getTenantManager().
                    getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            String errorMsg = "Unable to get tenant ID for bam stats : " + e.getMessage();
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }

        Application app = ApplicationManager.getInstance().getApplicationInfo(applicationKey);
        String applicationName = app.getName();

        for (String userName : userNames) {
            BamDataPublisher publisher = BamDataPublisher.getInstance();
            publisher.PublishUserUpdateEvent(applicationName, applicationKey,
                    System.currentTimeMillis(), tenantId, userName, action);

        }

    }

    /**
     * Removing user from the application
     * 
     * @param userNames
     * @param applicationKey
     * @return
     * @throws ApplicationManagementException
     */
    public boolean removeUsersFromApplication(String userNames[], String applicationKey)
                                                                                       throws ApplicationManagementException {
        String applicationRole = AppFactoryUtil.getRoleNameForApplication(applicationKey);
        String userNameStr = concatUserNames(userNames, ",");
        try {
           // CarbonContext threadLocalCarbonContext = CarbonContext.getThreadLocalCarbonContext();
           // int tenantId = threadLocalCarbonContext.getTenantId();
           // UserRealm userRealm = Util.getRealmService().getTenantUserRealm(tenantId);
            CarbonContext threadLocalCarbonContext = CarbonContext.getThreadLocalCarbonContext();
            String tenantDomain=threadLocalCarbonContext.getTenantDomain();
            UserRealm userRealm = threadLocalCarbonContext.getUserRealm();
            UserStoreManager userStoreManager = userRealm.getUserStoreManager();
            userStoreManager.updateUserListOfRole(applicationRole, userNames, null);
            Iterator<ApplicationEventsHandler> applicationEventsListeners = Util.getApplicationEventsListeners().iterator();
            
            while (applicationEventsListeners.hasNext()) {
                ApplicationEventsHandler applicationEventsListener =
                                                                      (ApplicationEventsHandler) applicationEventsListeners.next();
                for(String userName: userNames){
                    applicationEventsListener.onUserDeletion(ApplicationManager.getInstance().getApplicationInfo(applicationKey), new UserInfo(userName), tenantDomain);
                }
            }

            try {

                String infoMessage = userNameStr + " removed from application";
                EventNotifier.getInstance().notify(UserManagementEventBuilderUtil.buildUserDeletionFromAppEvent(applicationKey, infoMessage,
                                                                                                "", Event.Category.INFO));
            } catch (AppFactoryEventException exception) {
                log.error("Failed to notify the successful user deletion from the application event", exception);
                // do not throw again.
            }

            return true;
        } catch (UserStoreException e) {
            try {
                String error = "Error in removing " + userNameStr + " from application";
                EventNotifier.getInstance().notify(UserManagementEventBuilderUtil.buildUserDeletionFromAppEvent(applicationKey,
                                                                                                error, "", Event.Category.ERROR));
            } catch (AppFactoryEventException exception) {
                log.error("Failed to notify the failure of user deletion from app event ", exception);
                // do not throw again.
            }
            String message = "Failed to remove user " + userNames + " from application " +
                                     applicationKey;
            log.error(message,e);
            throw new ApplicationManagementException(message, e);
        } catch (AppFactoryException e) {
            try {
                String error = "Error in removing " + userNameStr + " from application";
                EventNotifier.getInstance().notify(UserManagementEventBuilderUtil.buildUserDeletionFromAppEvent(applicationKey,
                                                                                                error, "", Event.Category.ERROR));
            } catch (AppFactoryEventException exception) {
                log.error("Failed to notify the failure of user deletion from app event ", exception);
                // do not throw again.
            }
            String message = "Failed to remove user " + userNames + " from jenkins " +
                            applicationKey;
            log.error(message,e);
            throw new ApplicationManagementException(message, e);
        }
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
			Application[] apps = ApplicationManager.getInstance().getAllApplicaitonsOfUser(userName);
            long endTime = System.currentTimeMillis();
            if (perfLog.isDebugEnabled()) {
                perfLog.debug("AFProfiling getApplicaitonsOfTheUser" + (endTime - startTime) );
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
                        String appkeyFromPerAppRoleName = AppFactoryUtil.getAppkeyFromPerAppRoleName(role);
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
            return ApplicationManager.getInstance().getAllApplicationsCreatedByUser(userName);
        } catch (AppFactoryException e) {
            throw new ApplicationManagementException("Failed to retrieve applications created by the user" +
                                                     userName, e);
        }
    }

}
