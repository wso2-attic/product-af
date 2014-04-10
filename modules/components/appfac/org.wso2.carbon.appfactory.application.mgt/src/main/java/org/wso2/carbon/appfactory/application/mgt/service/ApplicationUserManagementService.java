package org.wso2.carbon.appfactory.application.mgt.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.application.mgt.util.Util;
import org.wso2.carbon.appfactory.bam.integration.BamDataPublisher;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.ApplicationEventsHandler;
import org.wso2.carbon.appfactory.core.dto.Application;
import org.wso2.carbon.appfactory.core.dto.UserInfo;
import org.wso2.carbon.appfactory.jenkins.build.JenkinsCISystemDriver;
import org.wso2.carbon.appfactory.tenant.mgt.beans.UserInfoBean;
import org.wso2.carbon.appfactory.tenant.mgt.service.TenantManagementException;
import org.wso2.carbon.appfactory.tenant.mgt.service.TenantManagementService;
import org.wso2.carbon.appfactory.tenant.mgt.util.AppFactoryTenantMgtUtil;
import org.wso2.carbon.appfactory.utilities.project.ProjectUtils;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

public class ApplicationUserManagementService {
    private static final Log log = LogFactory.getLog(ApplicationUserManagementService.class);

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
                    users.add(tenantManagementService.getUserInfo(user));
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
                    applicationEventsListener.onUserAddition(ProjectUtils.getApplicationInfo(applicationKey, tenantDomain), new UserInfo(userName), tenantDomain);

                }
            }

            // Publish stats to BAM
            publishBAMStats(userNames, applicationKey, tenantDomain, AppFactoryConstants.BAM_ADD_DATA);

            return true;
        } catch (UserStoreException e) {
            String message =
                             "Failed to add user " + userNames + " to the application " +
                                     applicationKey;
            log.error(message,e);
            throw new ApplicationManagementException(message, e);
        } catch (AppFactoryException e) {
            String message =
                    "Failed to add user " + userNames + " to jenkins " +
                            applicationKey;
            log.error(message,e);
            throw new ApplicationManagementException(message, e);
        }

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

        Application app = ProjectUtils.getApplicationInfo(applicationKey, tenantDomain);
        String applicationName = app.getName();

        for (String userName : userNames) {
            BamDataPublisher publisher = new BamDataPublisher();
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
                    applicationEventsListener.onUserDeletion(ProjectUtils.getApplicationInfo(applicationKey, tenantDomain), new UserInfo(userName), tenantDomain);
                }
            }
            return true;
        } catch (UserStoreException e) {
            String message =
                             "Failed to remove user " + userNames + " from application " +
                                     applicationKey;
            log.error(message,e);
            throw new ApplicationManagementException(message, e);
        } catch (AppFactoryException e) {
            String message =
                    "Failed to remove user " + userNames + " from jenkins " +
                            applicationKey;
            log.error(message,e);
            throw new ApplicationManagementException(message, e);
        }
    }
/**
 * Returns the list of applications that user belongs to
 * @param userName
 * @return <b>Application</b> Array
 * @throws ApplicationManagementException
 */
    public Application[] getApplicaitonsOfTheUser(String userName)
                                                                  throws ApplicationManagementException {
        CarbonContext context = CarbonContext.getThreadLocalCarbonContext();
        ArrayList<Application> applications = new ArrayList<Application>();
        try {
            String[] roles =
                             context.getUserRealm().getUserStoreManager()
                                    .getRoleListOfUser(userName);
            String tenantDomain = context.getTenantDomain();
            for (String role : roles) {
                if (role.startsWith(AppFactoryConstants.APP_ROLE_PREFIX)) {
                    String appkeyFromPerAppRoleName = AppFactoryUtil.getAppkeyFromPerAppRoleName(role);
                    applications.add(ProjectUtils.getApplicationInfo(appkeyFromPerAppRoleName,
                                                                     tenantDomain));
                }
            }
            return applications.toArray(new Application[applications.size()]);
        } catch (UserStoreException e) {
            String message = "Failed to retrieve applications of the user" + userName;
            log.error(message,e);
            throw new ApplicationManagementException(message, e);
        } catch (AppFactoryException e) {
            String message = "Failed to retrieve applications of the user" + userName;
            log.error(message,e);
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
}
