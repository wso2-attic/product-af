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
import org.wso2.carbon.appfactory.core.dao.JDBCApplicationDAO;
import org.wso2.carbon.appfactory.core.dto.Application;
import org.wso2.carbon.appfactory.core.util.CommonUtil;
import org.wso2.carbon.appfactory.core.util.Constants;
import org.wso2.carbon.appfactory.utilities.project.ProjectUtils;
import org.wso2.carbon.context.CarbonContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This service provides necessary information of the application. Detailed and
 * lightweight methods related to
 * retrieving applications information
 * 
 * 
 */
public class ApplicationInfoService {
    private static Log log = LogFactory.getLog(ApplicationInfoService.class);

    /**
     * Get the application information bean of the application
     * 
     * @param applicationKey
     * @param tenantDomain
     * @return
     * @throws AppFactoryException
     */
    private ApplicationInfoBean getBasicApplicationInfo(String applicationKey, String tenantDomain)
                                                                                                   throws AppFactoryException {
        Application application = ProjectUtils.getApplicationInfo(applicationKey, tenantDomain);
        return new ApplicationInfoBean(application);
    }

    /**
     * Gets application information of the user to populate the user home
     * 
     * @param userName
     * @return
     * @throws ApplicationManagementException  
     */
    public ApplicationInfoBean[] getApplicationInfoForUser(String userName)
                                                                           throws ApplicationManagementException {
        ApplicationUserManagementService applicationUserManagementService =
                                                                            new ApplicationUserManagementService();
        String[] applicationKeys =
                                   applicationUserManagementService.getApplicationKeysOfUser(userName);
        String tenantDomain = getTenantDomain();
        List<ApplicationInfoBean> appInfoList = new ArrayList<ApplicationInfoBean>();
        for (String applicationKey : applicationKeys) {
            try {
                ApplicationInfoBean appInfo = getBasicApplicationInfo(applicationKey, tenantDomain);
                appInfoList.add(appInfo);
            } catch (AppFactoryException e) {
                String msg =
                             "Error while getting application info for user " + userName +
                                     " of tenant" + tenantDomain;
                log.error(msg, e);
            }
        }
        return appInfoList.toArray(new ApplicationInfoBean[appInfoList.size()]);
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
        return CommonUtil.getApplicationType(applicationId, tenantDomain);
    }

    private String getTenantDomain() {
        return CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    }

}
