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
package org.wso2.carbon.appfactory.repository.mgt.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.repository.mgt.internal.Util;
import org.wso2.carbon.appfactory.utilities.project.ProjectUtils;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.Arrays;

/**
 * This is a non admin service to authenticate and authorize repository access and operations
 * based on appFactory AA model
 */
public class RepositoryAuthenticationService extends AbstractAdmin {
    private static final Log log = LogFactory.getLog(RepositoryAuthenticationService.class);

    public boolean hasAccess(String username, String applicationId, String repoAction, String fullRepoName) {
        try {
            String domainName = getTenantDomain();
            AppFactoryConfiguration configuration = Util.getConfiguration();
            String repositoryType = ProjectUtils.getRepositoryType(applicationId, domainName);


            UserRealm realm = getUserRealm();
            String permission = configuration.getFirstProperty(String.format(
                    AppFactoryConstants.SCM_READ_WRITE_PERMISSION, repositoryType));
            String repoAccessability = configuration.getFirstProperty(AppFactoryConstants.REPO_ACCESSABILITY);
            if (repoAccessability.equals("false")) {
                if (realm != null && realm.getAuthorizationManager().
                        isUserAuthorized(getUsername(), permission, CarbonConstants.UI_PERMISSION_ACTION)) {
                    return true;
                }
            } else {
                //  //check if the user name is coming with the  repo name is also coming
                if (realm != null) {
                    //Checks if the repo action is clone  (R = CLONE)
                    if (repoAction.equals("R") && realm.getAuthorizationManager().
                            isUserAuthorized(getUsername(), permission, CarbonConstants.UI_PERMISSION_ACTION)) {
                        return true;
                    } else {
                        // When the repo action is push
                        // checks if the user has appowner role by checking the value "appowner" in the roles list
                        String[] rolesArray = realm.getUserStoreManager().getRoleListOfUser(username.split("@")[0]);
                        boolean isAppOwner = Arrays.asList(rolesArray).contains(AppFactoryConstants.APP_OWNER_ROLE);
                        if (isAppOwner && realm.getAuthorizationManager().
                                isUserAuthorized(getUsername(), permission, CarbonConstants.UI_PERMISSION_ACTION)) {
                            return true;
                        }
                    }
                }
            }

        } catch (UserStoreException e) {
            String msg = "Error while checking permission for accessing repository of "
                    + applicationId + " by " + username;
            log.error(msg, e);
        } catch (AppFactoryException e) {
            String msg = "Error while getting repository type of application " + applicationId;
            log.error(msg, e);
        }
        return false;
    }
}
