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

package org.wso2.carbon.appfactory.repository.mgt.svn;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeManager;
import org.wso2.carbon.appfactory.core.util.CommonUtil;
import org.wso2.carbon.appfactory.repository.mgt.BranchingStrategy;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryMgtException;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryProvider;
import org.wso2.carbon.appfactory.repository.mgt.client.AppfactoryRepositoryClient;
import org.wso2.carbon.appfactory.utilities.project.ProjectUtils;
import org.wso2.carbon.appfactory.utilities.version.AppVersionStrategyExecutor;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;

/**
 * Contains operation to be done before doing svn repository operations
 */
public class SVNBranchingStrategy implements BranchingStrategy {
    private static final Log log = LogFactory.getLog(SVNBranchingStrategy.class);
    private RepositoryProvider provider;

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepareRepository(String applicationKey, String url, String tenantDomain)
            throws RepositoryMgtException {
        File workDir = new File(CarbonUtils.getTmpDir() + File.separator + applicationKey);
        if (!workDir.mkdirs()) {
            log.error("Error creating work directory at location" + workDir.getAbsolutePath());
        }
        if (provider != null) {
            AppfactoryRepositoryClient client = provider.getRepositoryClient();
            client.checkOut(url, "0", workDir);
            File trunk = new File(workDir.getAbsolutePath() + File.separator + AppFactoryConstants.TRUNK);
            if (!trunk.mkdir()) {
                log.error("Error creating work directory at location" + trunk.getAbsolutePath());
            }
            try {
                String applicationType = CommonUtil.getApplicationType(applicationKey, tenantDomain);

                ApplicationTypeManager.getInstance().getApplicationTypeBean(applicationType).getProcessor().generateApplicationSkeleton(applicationKey, trunk.getAbsolutePath());
            } catch (AppFactoryException e) {
                //There is an exception when generating the maven archetype.
                String msg = "Could not generate the project using maven archetype for application : " + applicationKey;
                log.error(msg, e);
                throw new RepositoryMgtException(msg, e);
            }

            File branches = new File(workDir.getAbsolutePath() + File.separator + AppFactoryConstants.BRANCH);
            if (!branches.mkdir()) {
                log.error("Error creating work directory at location" + branches.getAbsolutePath());
            }

            File tags = new File(workDir.getAbsolutePath() + File.separator + AppFactoryConstants.TAG);
            if (!tags.mkdir()) {
                log.error("Error creating work directory at location" + tags.getAbsolutePath());
            }

            client.add(url, trunk ,true, false, trunk);
            client.add(url, branches, false, false, branches);
            client.add(url, tags, false, false, tags);
            client.checkin(url, workDir, "creating trunk,branches and tags ", false);
            client.close();
            try {
                FileUtils.deleteDirectory(workDir);
            } catch (IOException e) {
                log.error("Error deleting work directory " + e.getMessage(), e);
            }

        } else {
            String msg = new StringBuilder().append("Repository provider for the  ").append(applicationKey).append(" not found").toString();
            log.error(msg);
            throw new RepositoryMgtException(msg);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doRepositoryBranch(String appId, String currentVersion, String targetVersion,
                                   String currentRevision, String tenantDomain) throws RepositoryMgtException {

        String applicationType;
        try {
            applicationType = CommonUtil.getApplicationType(appId, tenantDomain);
        } catch (AppFactoryException e1) {
            String msg = "Error while getting application type for " + appId;
            log.error(msg, e1);
            throw new RepositoryMgtException(msg, e1);
        }

        String sourceURL = provider.getAppRepositoryURL(appId, tenantDomain);
        String destURL = sourceURL;

        File workDir = new File(CarbonUtils.getTmpDir() + File.separator + appId);
        if (!workDir.mkdir()) {
            log.error("Error creating work directory at location" + workDir.getAbsolutePath());
        }

        if (AppFactoryConstants.TRUNK.equals(currentVersion)) {
            sourceURL = sourceURL + "/" + currentVersion;
        } else {
            sourceURL = sourceURL + "/" + AppFactoryConstants.BRANCH + "/" + currentVersion;
        }

        destURL = destURL + "/" + AppFactoryConstants.BRANCH + "/" + targetVersion;

        AppfactoryRepositoryClient client = null;
        try {
            client = provider.getRepositoryClient();
            client.checkOut(sourceURL, currentRevision, workDir);
            client.branch(sourceURL, targetVersion, null, workDir);

            try {
                FileUtils.deleteDirectory(workDir);
            } catch (IOException e) {
                log.error("Error deleting work directory " + e.getMessage(), e);
            }

            client.checkOut(destURL, currentRevision, workDir);

            ApplicationTypeManager.getInstance().getApplicationTypeBean(applicationType).getProcessor().doVersion(appId, targetVersion, currentVersion, workDir.getAbsolutePath());

            // When we do the version of the project, project structure and the
            // content of the files may be change in the doVersion method.
            // checkin method add/remove the files according to the status
            // and commit.
            client.checkin(destURL, workDir,
                    "Commit by the system : To get update the version of the system.", true);

            try {
                FileUtils.deleteDirectory(workDir);
            } catch (IOException e) {
                log.error("Error deleting work directory " + e.getMessage(), e);
            }

        } catch (Exception e) {
            throw new RepositoryMgtException("Error come when branch is done. " + e.getMessage(), e);
        } finally {
            try {
                client.close();
            } catch (Exception e) {
                log.error("Client closing error : " + e.getMessage(), e);
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doRepositoryTag(String appId, String currentVersion, String targetVersion,
                                String currentRevision, String tenantDomain) throws RepositoryMgtException {

        String sourceURL = provider.getAppRepositoryURL(appId, tenantDomain);

        String applicationType;
        try {
            applicationType = CommonUtil.getApplicationType(appId, tenantDomain);
        } catch (AppFactoryException e1) {
            throw new RepositoryMgtException(e1);
        }

        File workDir = new File(CarbonUtils.getTmpDir() + File.separator + appId);
        if (!workDir.mkdir()) {
            log.error("Error creating work directory at location" + workDir.getAbsolutePath());
        }
        if (AppFactoryConstants.TRUNK.equals(currentVersion)) {
            sourceURL = sourceURL + "/" + currentVersion;
        } else {
            sourceURL = sourceURL + "/" + AppFactoryConstants.BRANCH + "/" + currentVersion;
        }
        AppfactoryRepositoryClient client = provider.getRepositoryClient();
        client.checkOut(sourceURL, currentRevision, workDir);
        new AppVersionStrategyExecutor().doVersion(appId, currentVersion, targetVersion, workDir, applicationType);

        client.tag(sourceURL, targetVersion, null, workDir.getPath(), null);
        try {
            FileUtils.deleteDirectory(workDir);
        } catch (IOException e) {
            log.error("Error deleting work directory " + e.getMessage(), e);
        }
        client.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRepositoryProvider(RepositoryProvider provider) {
        this.provider = provider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RepositoryProvider getRepositoryProvider() {
        return provider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getURLForAppVersion(String applicationKey, String version, String tenantDomain)
            throws RepositoryMgtException {
        StringBuilder builder = new StringBuilder(getRepositoryProvider().getAppRepositoryURL(applicationKey, tenantDomain)).append('/');

        if (AppFactoryConstants.TRUNK.equals(version)) {
            builder.append(version);
        } else {
            builder.append(AppFactoryConstants.BRANCH).append('/').append(version);
        }
        return builder.toString();
    }
}
