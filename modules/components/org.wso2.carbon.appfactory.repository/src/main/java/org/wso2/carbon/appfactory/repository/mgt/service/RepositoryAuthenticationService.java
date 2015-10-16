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
package org.wso2.carbon.appfactory.repository.mgt.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.lifecycle.management.service.LifecycleManagementService;
import org.wso2.carbon.appfactory.lifecycle.management.service.LifecycleManagementServiceImpl;
import org.wso2.carbon.appfactory.lifecycle.management.bean.LifecycleInfoBean;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryMgtException;
import org.wso2.carbon.appfactory.repository.mgt.internal.Util;
import org.wso2.carbon.appfactory.utilities.project.ProjectUtils;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.governance.custom.lifecycles.checklist.beans.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.util.LifecycleBeanPopulator;
import org.wso2.carbon.governance.custom.lifecycles.checklist.util.Property;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is a non admin service to authenticate and authorize repository access
 * and operations based on appFactory AA model
 */
public class RepositoryAuthenticationService extends AbstractAdmin {
    private static final Log log = LogFactory.getLog(RepositoryAuthenticationService.class);

    public boolean canCommit(String applicationId, String version) {
        String path = "repository/applications/" + applicationId + "/" + version;
        UserRegistry usrRegistry = (UserRegistry) CarbonContext.getThreadLocalCarbonContext()
                .getRegistry(RegistryType.USER_GOVERNANCE);
        Registry configRegistry = (Registry) CarbonContext.getThreadLocalCarbonContext()
                .getRegistry(RegistryType.SYSTEM_CONFIGURATION);
        try {
            LifecycleBean lifecycleBean = LifecycleBeanPopulator.getLifecycleBean(path, usrRegistry, configRegistry);
            if (lifecycleBean == null) {
                log.error("Failed to get lifecycle bean for registry path : " + path + " for application: " +
                        applicationId + " and version : " + version + ". Hence user is not allowed to commit.");
                return false;
            }
            Property[] lifecycleProperties = lifecycleBean.getLifecycleProperties();
            String stage = null;
            for (Property lifecycleProp : lifecycleProperties) {
                if (AppFactoryConstants.APPLICATION_LIFECYCLE_STATE_KEY.equals(lifecycleProp.getKey())) {
                    stage = lifecycleProp.getValues()[0];
                    break;
                }
            }
            /*if(stage != null && Boolean.parseBoolean(AppFactoryUtil.getAppfactoryConfiguration().
					getFirstProperty("ApplicationDeployment.DeploymentStage." + stage + ".CanCommit"))){
				return true;
			}*/
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            LifecycleManagementService lifecycleManagementService = new LifecycleManagementServiceImpl();
            LifecycleInfoBean lifecycle =
                    lifecycleManagementService.getCurrentAppVersionLifeCycle(applicationId, version, tenantDomain);
            if (stage != null && stage.equals(lifecycle.getBuildStageName())) {
                //if the stage equals to build stage relevant to lifecycle
                return true;
            }
            return false;
        } catch (Exception e) {
            String msg =
                    "Error while retrieving the stage of application : " + applicationId + " and version : " + version;
            log.error(msg, e);
            return false;
        }
    }

    /**
     * To authorize git action against permission code configured in appfactory
     * configuration
     *
     * @param username
     * @param applicationId
     * @param repoAction
     * @param fullRepoName
     * @return
     */
    public boolean hasAccess(String username, String applicationId, String repoAction, String fullRepoName) {
        boolean ret = false;
        try {
            String domainName = getTenantDomain();
            String repoDomain = fullRepoName.split("/")[0];

            if (!repoDomain.equals(domainName) & !repoDomain.equals("~" + domainName)) {
                return false;
            }

            if (fullRepoName.contains("~")) {
                String repoUserName = fullRepoName.split("/")[1];
                if (!repoUserName.equals(MultitenantUtils.getTenantAwareUsername(username))) {
                    return false;
                }
            }

            AppFactoryConfiguration configuration = Util.getConfiguration();
            String repositoryType = ProjectUtils.getRepositoryType(applicationId, domainName);

            try {
                if (!isAccessToApplication(username, applicationId, domainName)) {
                    return false;
                }
            } catch (RepositoryMgtException e) {
                String msg = "Error while checking permission for accessing application of " +
                        applicationId + " by " + username;
                log.error(msg, e);
            }

            UserRealm realm = getUserRealm();
            String userNameDomainAware = MultitenantUtils.getTenantAwareUsername(username);
            String[] userRoles = realm.getUserStoreManager().getRoleListOfUser(userNameDomainAware);

            String repoAccessability = configuration.getFirstProperty(AppFactoryConstants.REPO_ACCESSABILITY);

            if (fullRepoName.indexOf("~") != -1 && repoAccessability.equals("false")) {
                return false;
            }

            if (fullRepoName.indexOf("~") != -1) {
                // TODO:We can check specific permission check for fork
                // repository. Because from git side it doesn't differentiate
                // whether it is master or fork.
            }

            for (String userRole : userRoles) {
                ret = checkPermission(repositoryType, userRole, repoAction);
                if (ret) {
                    break;
                }
            }

        } catch (UserStoreException e) {
            String msg = "Error while checking permission for accessing repository of " +
                    applicationId + " by " + username;
            log.error(msg, e);
        } catch (AppFactoryException e) {
            String msg = "Error while getting repository type of application " + applicationId;
            log.error(msg, e);
        }

        return ret;
    }

    /**
     * Check permission against role.
     *
     * @param repoType
     * @param userRole
     * @param repositoryAction
     * @return
     */
    private boolean checkPermission(String repoType, String userRole, String repositoryAction) {
        List<String> permissions = getRepositoryPermissionModel(repoType, userRole);
        return permissions.contains(repositoryAction);
    }

    /**
     * Read user permissions codes per role.
     *
     * @param repoType
     * @param userRole
     * @return
     */
    private List<String> getRepositoryPermissionModel(String repoType, String userRole) {
        AppFactoryConfiguration appFactoryConfiguration = Util.getConfiguration();
        String key = AppFactoryConstants.REPOSITORY_PROVIDER_CONFIG + "." + repoType +
                ".RepositoryUserPermission.Role." + userRole;
        String permissions = appFactoryConfiguration.getFirstProperty(key);
        List<String> permissionList = new ArrayList<String>();
        if (permissions != null) {
            String[] splitResult = permissions.trim().split(",");
            if (splitResult.length > 0) {
                permissionList = Arrays.asList(splitResult);
            }
        }
        return permissionList;

    }

    private boolean isAccessToApplication(String userName, String applicationKey, String tenantDomain)
            throws RepositoryMgtException {
        String applicationRole = AppFactoryUtil.getRoleNameForApplication(applicationKey);

        try {

            UserRealm realm = Util.getRealmService()
                    .getTenantUserRealm(Util.getRealmService().getTenantManager().getTenantId(tenantDomain));

            String[] usersOfApplication = realm.getUserStoreManager().getUserListOfRole(applicationRole);
            userName = MultitenantUtils.getTenantAwareUsername(userName);
            for (String user : usersOfApplication) {
                if (user.equalsIgnoreCase(userName)) {
                    return true;
                }
            }
            return false;

        } catch (UserStoreException e) {
            String message = "Failed to retrieve list of users for application " + applicationKey;
            log.error(message, e);
            throw new RepositoryMgtException(message, e);
        }
    }

}
