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
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.cache.ApplicationsOfUserCache;
import org.wso2.carbon.appfactory.core.dao.JDBCApplicationDAO;
import org.wso2.carbon.appfactory.core.dto.Application;
import org.wso2.carbon.appfactory.core.dto.ApplicationSummary;
import org.wso2.carbon.appfactory.core.governance.ApplicationManager;
import org.wso2.carbon.appfactory.core.governance.RxtManager;
import org.wso2.carbon.appfactory.core.governance.dao.AppVersionDAO;
import org.wso2.carbon.appfactory.core.util.Constants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.ArrayList;
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

    /**
     * Get the application information bean of the application
     * 
     * @param applicationKey
     * @return
     * @throws AppFactoryException
     */
    private Application getBasicApplicationInfo(String applicationKey) throws AppFactoryException {
        Application application = ApplicationManager.getInstance().getApplicationInfo(applicationKey);
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
                Application application = ApplicationManager.getInstance().getApplicationInfo(applicationKey);
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
        return ApplicationManager.getInstance().getApplicationType(applicationId);
    }


    public String addArtifact(String key, String info, String lifecycleAttribute) throws AppFactoryException {
       RegistryUtils.recordStatistics(key, info, lifecycleAttribute);
       if(key.equals("application")){
          // ApplicationDAO.getInstance()
       }else if(key.equals("appversion")){
           AppVersionDAO.getInstance().addArtifact(info,lifecycleAttribute);
       }


        return RxtManager.getInstance().addArtifact(key, info, lifecycleAttribute);
    }

    private String getTenantDomain() {
        return CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
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
                    ApplicationManager.getInstance().getSummarizedApplicationInfo(applicationKeys);
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
            Application[] apps = ApplicationManager.getInstance().getAllApplicaitonsOfUser(userName);
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
            return ApplicationManager.getInstance().getAllApplicationsCreatedByUser(userName);
        } catch (AppFactoryException e) {
            throw new ApplicationManagementException("Failed to retrieve applications created by the user" +
                                                     userName, e);
        }
    }


}
