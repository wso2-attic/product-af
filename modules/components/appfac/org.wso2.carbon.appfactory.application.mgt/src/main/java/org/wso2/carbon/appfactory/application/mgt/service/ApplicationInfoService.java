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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.application.mgt.util.Util;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.dto.Application;
import org.wso2.carbon.appfactory.utilities.project.ProjectUtils;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;

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

    private String getTenantDomain() {
        return CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    }

}
