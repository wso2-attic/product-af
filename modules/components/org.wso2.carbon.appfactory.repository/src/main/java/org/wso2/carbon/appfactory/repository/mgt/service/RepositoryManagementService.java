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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.bam.BamDataPublisher;
import org.wso2.carbon.appfactory.common.beans.RuntimeBean;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeBean;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeManager;
import org.wso2.carbon.appfactory.core.dao.ApplicationDAO;
import org.wso2.carbon.appfactory.core.runtime.RuntimeManager;
import org.wso2.carbon.appfactory.eventing.AppFactoryEventException;
import org.wso2.carbon.appfactory.eventing.Event;
import org.wso2.carbon.appfactory.eventing.EventNotifier;
import org.wso2.carbon.appfactory.eventing.builder.utils.ContinousIntegrationEventBuilderUtil;
import org.wso2.carbon.appfactory.eventing.builder.utils.RepoCreationEventBuilderUtil;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryManager;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryMgtException;
import org.wso2.carbon.appfactory.s4.integration.GITBlitBasedGITRepositoryProvider;
import org.wso2.carbon.appfactory.utilities.project.ProjectUtils;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.math.BigDecimal;
import java.util.Date;

/**
 * This is an admin service for repository related operations.
 * Note:This service depends on external information(RXT) for getting repository
 * type of
 * application
 */
public class RepositoryManagementService extends AbstractAdmin {
    private static final Log log = LogFactory.getLog(RepositoryManagementService.class);
    private RepositoryManager repositoryManager;

    public RepositoryManagementService() {
        this.repositoryManager = new RepositoryManager();
    }

    /**
     * @param applicationKey
     * @param type
     * @param username
     * @throws RepositoryMgtException
     */
    public void provisionUser(String applicationKey, String type, String username)
            throws RepositoryMgtException {
        repositoryManager.provisionUser(applicationKey, type, username);
    }

    /**
     * Gives the repository url of application
     *
     * @param applicationKey Application ID
     * @param tenantDomain   Tenant domain of application
     * @return Repository URL
     * @throws RepositoryMgtException
     */
    public String getURL(String applicationKey, String tenantDomain) throws RepositoryMgtException {
        return repositoryManager.getAppRepositoryURL(applicationKey,
                getRepositoryType(applicationKey, tenantDomain),
                tenantDomain);
    }

    /**
     * Gives the URL of repository of a version of application
     *
     * @param applicationKey
     * @param version
     * @param tenantDomain   Tenant Domain of application
     * @return
     * @throws RepositoryMgtException
     */
    public String getURLForAppVersion(String applicationKey, String version, String tenantDomain)
            throws RepositoryMgtException {

        // Getting the tenant ID from the CarbonContext since this is called as
        // a SOAP service.
        tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        return repositoryManager.getURLForAppversion(applicationKey,
                version,
                getRepositoryType(applicationKey, tenantDomain),
                tenantDomain);
    }

    /**
     * Branches the repository of an application
     *
     * @param appId
     * @param currentVersion
     * @param targetVersion
     * @param currentRevision
     * @param tenantDomain    Tenant domain of application
     * @throws RepositoryMgtException
     */
    public void branch(String appId, String currentVersion, String targetVersion,
                       String currentRevision, String tenantDomain) throws RepositoryMgtException {
        // AppVersionCache cache = AppVersionCache.getAppVersionCache();
        // cache.clearCacheForAppId(appId);
        String repositoryType = getRepositoryType(appId, tenantDomain);
        String correlationKey = tenantDomain + "-" + appId + "-" + repositoryType + "-" + currentVersion +
                "-" + targetVersion + "-" + currentRevision;
        try {
            // Getting the tenant ID from the CarbonContext since this is called
            // as a SOAP service.
            tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

            repositoryManager.branch(appId, repositoryType, currentVersion,
                    targetVersion, currentRevision, tenantDomain);
            try {
                String infoMessage = "Branch " + targetVersion + " creation completed";
                EventNotifier.getInstance()
                        .notify(RepoCreationEventBuilderUtil.buildBranchCreationCompleteEvent(appId, infoMessage,
                                "",
                                Event.Category.INFO, correlationKey));
            } catch (AppFactoryEventException e) {
                log.error("Failed to notify Branch creation event", e);
                // do not throw again.
            }
        } catch (RepositoryMgtException e) {
            String error = "Error while creating branch " + targetVersion;
            String errorMsg = e.getMessage();
            try {
                EventNotifier.getInstance().notify(RepoCreationEventBuilderUtil.buildBranchCreationCompleteEvent(appId,
                        error, errorMsg, Event.Category.ERROR, correlationKey));
            } catch (AppFactoryEventException exception) {
                log.error("Failed to notify the failure in Branch creation event", exception);
                // do not throw again.
            }

            log.error(error + " due to " + errorMsg, e);
            throw new RepositoryMgtException(error + " due to " + errorMsg, e);
        }

        publishBAMStats(appId, targetVersion);

    }

    /**
     * Publish Stats to BAM
     *
     * @param appId
     * @param targetVersion
     */
    private void publishBAMStats(String appId, String targetVersion) throws RepositoryMgtException {

        log.info("publish app version creation stats to bam");
        PrivilegedCarbonContext threadLocalCarbonContext =
                PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = threadLocalCarbonContext.getTenantId();
        String userName = threadLocalCarbonContext.getUsername();
        String stage = "Development";

        BamDataPublisher bamDataPublisher = BamDataPublisher.getInstance();
        try {
            bamDataPublisher.PublishAppVersionEvent(appId, appId, System.currentTimeMillis(),
                    "" + tenantId, userName, targetVersion, stage);
        } catch (AppFactoryException e) {
            log.error("Can not publish stats to BAM", e);
            throw new RepositoryMgtException("Can not publish stats to BAM", e);

        }

    }

    /**
     * Tags the repository of an application
     *
     * @param appId
     * @param currentVersion
     * @param targetVersion
     * @param currentRevision
     * @param tenantDomain
     * @throws AppFactoryException
     * @throws RepositoryMgtException
     */
    public void tag(String appId, String currentVersion, String targetVersion,
                    String currentRevision, String tenantDomain) throws AppFactoryException,
            RepositoryMgtException {
        repositoryManager.tag(appId, getRepositoryType(appId, tenantDomain), currentVersion,
                targetVersion, currentRevision, tenantDomain);
    }

    public boolean deleteRepository(String applicationKey, String type)
            throws RepositoryMgtException {
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        if (StringUtils.isBlank(tenantDomain)) {
            String msg = "Tenant domain is empty. Cannot delete repository";
            log.error(msg);
            throw new RepositoryMgtException(msg);
        }
        long s = new Date().getTime();
        boolean isDeleted = repositoryManager.deleteRepository(applicationKey, type, tenantDomain);
        log.info("Repo Time : " + (new Date().getTime() - s));
        return isDeleted;
    }

    public boolean repositoryExists(String applicationKey, String type)
            throws RepositoryMgtException {
        return repositoryManager.repositoryExists(applicationKey, type,
                CarbonContext.getThreadLocalCarbonContext()
                        .getTenantDomain());
    }

    /**
     * Get repository type
     *
     * @param applicationId Application ID
     * @param tenantDomain  Tenant domain of application
     * @return
     * @throws RepositoryMgtException
     */
    private String getRepositoryType(String applicationId, String tenantDomain)
            throws RepositoryMgtException {

        String type;
        try {
            type = ProjectUtils.getRepositoryType(applicationId, tenantDomain);
        } catch (AppFactoryException e) {
            String msg = "Error while getting repository type of application " + applicationId;
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        }
        return type;
    }

    public void saveGitData(String gitOrgName, String gitUserName, String gitPassword) {
        log.info("*****************from Java side:" + gitOrgName + gitUserName + gitPassword);
    }

    /**
     * Creating a fork
     *
     * @param applicationKey
     * @param userName
     * @param type
     * @return
     * @throws RepositoryMgtException
     */
    public String createFork(String applicationKey, String userName, String type)
                  throws RepositoryMgtException, AppFactoryException {
        PrivilegedCarbonContext threadLocalCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = threadLocalCarbonContext.getTenantId();

        String forkedRepoURL = null;
        try {
            forkedRepoURL = repositoryManager.createFork(applicationKey, userName, type);

            String forkedDeploymentUrl = createDeploymentFork(applicationKey, tenantId, MultitenantUtils.getTenantAwareUsername(userName));
            if (log.isDebugEnabled()) {
                log.debug(" Deployment repository created for fork application : " + applicationKey + "for tenant id :"
                          + tenantId + " with url :" + forkedDeploymentUrl);
            }
        } catch (RepositoryMgtException e) {
            String msg = "Error while forking for application " + applicationKey;
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        }
        return forkedRepoURL;
    }

    private String createDeploymentFork(String applicationKey, Integer tenantId, String userName)
                   throws AppFactoryException {
        String applicationType = ApplicationDAO.getInstance().getApplicationType(applicationKey);
        ApplicationTypeBean applicationTypeBean = ApplicationTypeManager.getInstance()
                                                                        .getApplicationTypeBean(applicationType);

        if (applicationTypeBean == null) {
            throw new AppFactoryException("Application Type details cannot be found for applicationId : " +
                                          applicationKey + " for user :" + userName);
        }

        String runtimeNameForAppType = applicationTypeBean.getRuntimes()[0];
        RuntimeBean runtimeBean = RuntimeManager.getInstance().getRuntimeBean(runtimeNameForAppType);

        if (runtimeBean == null) {
            throw new AppFactoryException("Runtime details cannot be found for applicationId : " + applicationKey +
                                          " for user : " + userName);
        }

        GITBlitBasedGITRepositoryProvider repositoryProvider = new GITBlitBasedGITRepositoryProvider();
        repositoryProvider.setBaseUrl(AppFactoryUtil.getAppfactoryConfiguration().
                getFirstProperty(AppFactoryConstants.PAAS_ARTIFACT_REPO_PROVIDER_BASE_URL));
        repositoryProvider.setAdminUsername(AppFactoryUtil.getAppfactoryConfiguration().
                getFirstProperty(AppFactoryConstants.PAAS_ARTIFACT_REPO_PROVIDER_ADMIN_USER_NAME));
        repositoryProvider.setAdminPassword(AppFactoryUtil.getAppfactoryConfiguration().
                getFirstProperty(AppFactoryConstants.PAAS_ARTIFACT_REPO_PROVIDER_ADMIN_PASSWORD));

        String repoName = runtimeBean.getPaasRepositoryURLPattern().replace("{@stage}", "Development") +
                          File.separator + Integer.toString(tenantId) + "_" + userName;
        repositoryProvider.setRepoName(repoName);

        String forkDeploymentUrl = null;
        try {
            forkDeploymentUrl = repositoryProvider.createRepository();
        } catch (AppFactoryException e) {
            String msg = "Error while creating the deployment repository for fork application: " + applicationKey +
                         " for user " + userName;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }

        return forkDeploymentUrl;
    }

    /**
     * Adding new branch to existing fork repository
     *
     * @param applicationKey
     * @param userName
     * @param type
     * @param version
     * @throws RepositoryMgtException
     */
    public void createForkBranch(String applicationKey, String userName, String type, String version)
            throws RepositoryMgtException {

        try {
            repositoryManager.createForkBranch(applicationKey, userName, type, version);
        } catch (RepositoryMgtException e) {
            String error = " Error while adding " + version + "forked repo";
            String errorMsg = e.getMessage();
            try {
                EventNotifier.getInstance().notify(RepoCreationEventBuilderUtil.buildBranchForkingEvent(applicationKey,
                        error,
                        errorMsg,
                        Event.Category.ERROR, userName));
            } catch (AppFactoryEventException exception) {
                log.error("Failed to notify the failure in forking branch for application event",
                        exception);
                // do not throw again.
            }

            log.error(error + " due to " + errorMsg, e);
            throw new RepositoryMgtException(error + " due to " + errorMsg, e);
        }
    }

    /**
     * publishing git commit event to cassandra
     *
     * @param applicationKey
     * @param gitUserName
     * @param branch
     * @param commitMessage
     */
    public void notifyPostCommit(String applicationKey, String gitUserName, String branch, String commitMessage) {
        String title = gitUserName.split("@")[0] + " committed code to " + branch;
        try {
            EventNotifier.getInstance().notify(ContinousIntegrationEventBuilderUtil.buildPostCommitEvents(applicationKey, gitUserName, title, commitMessage));
            Date date= new java.util.Date();
            BigDecimal timeValue = new BigDecimal(date.getTime());
            JSONObject activity = new JSONObject();
            activity.put("item", "commit");
            activity.put("action", "commit");
            activity.put("timestamp", timeValue.toEngineeringString());
            activity.put("appKey", applicationKey);
            activity.put("appVersion", branch);
            String[] activities = new String[1];
            activities[0] = activity.toString();
            BamDataPublisher.getInstance().publishUserActivityEvents(CarbonContext.getThreadLocalCarbonContext().getTenantId(), gitUserName, activities);
        } catch (JSONException e) {
            log.error("Failed to publish commits of user", e);
        } catch (AppFactoryException e) {
            log.error("Failed to publish commits of user", e);
        } catch (AppFactoryEventException exception) {
            log.error("Failed to notify committing code to repository",
                    exception);
        }
    }

}
