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

package org.wso2.carbon.appfactory.repository.mgt.git;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeManager;
import org.wso2.carbon.appfactory.core.util.CommonUtil;
import org.wso2.carbon.appfactory.repository.mgt.BranchingStrategy;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryMgtException;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryProvider;
import org.wso2.carbon.appfactory.repository.mgt.client.AppfactoryRepositoryClient;
import org.wso2.carbon.appfactory.utilities.project.ProjectUtils;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Contains operation to be done before doing git repository operations
 */
public class GITBranchingStrategy implements BranchingStrategy {
    private static final Log log = LogFactory.getLog(GITBranchingStrategy.class);
    private static final String MASTER_BRANCH = "master";
    private RepositoryProvider provider;

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepareRepository(String applicationKey, String url, String tenantDomain) throws RepositoryMgtException {
        String dirpath = CarbonUtils.getTmpDir() + File.separator + "create" +
                File.separator + applicationKey + File.separator;

        File workDir = new File(dirpath);
        try {
            FileUtils.forceMkdir(workDir);
            AppfactoryRepositoryClient client = provider.getRepositoryClient();
            client.retireveMetadata(url, false, workDir);               // checkout master after git initialization

            try {
                String applicationType = CommonUtil.getApplicationType(applicationKey, tenantDomain);
                ApplicationTypeManager.getInstance().getApplicationTypeBean(applicationType).getProcessor().generateApplicationSkeleton(applicationKey, workDir.getAbsolutePath());
            } catch (AppFactoryException e) {
                //There is an exception when generating the maven archetype.
                String msg = "Could not generate the project using maven archetype for application : " + applicationKey;
                log.error(msg, e);
                throw new RepositoryMgtException(msg, e);
            }
            generateGitIgnoreRecursively(workDir);

            String commitMsg = "creating trunk,branches and tags ";
            client.add(url, workDir, true, false, workDir);             // git add all the new and updated files
            client.commitLocally(commitMsg, true, workDir);             // git commit after adding new files
            boolean isSuccessful = client.pushLocalCommits(url, MASTER_BRANCH, workDir);       // git push
            if (!isSuccessful) {
                String errorMsg = "Failed to complete git push because remote git server rejected the push command.";
                log.error(errorMsg);
                throw new RepositoryMgtException(errorMsg);
            }
        } catch (IOException e) {
            String msg = "Error creating work directory at location" + workDir.getAbsolutePath();
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        } finally {
            try {
                FileUtils.deleteDirectory(workDir);
            } catch (IOException e) {
                log.error("Error deleting work directory " + e.getMessage(), e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doRepositoryBranch(String appId, String currentVersion, String targetVersion,
                                   String currentRevision, String tenantDomain) throws RepositoryMgtException {
        String sourceURL = provider.getAppRepositoryURL(appId, tenantDomain);
        String applicationType;
        try {
            applicationType = CommonUtil.getApplicationType(appId, tenantDomain);
        } catch (AppFactoryException e1) {
            String msg = "Error while getting application type for " + appId;
            log.error(msg, e1);
            throw new RepositoryMgtException(msg, e1);
        }
        String dirpath = CarbonUtils.getTmpDir() + File.separator + "branch" +
                File.separator + appId + File.separator + targetVersion;
        String lock = tenantDomain + appId + targetVersion;

        synchronized (lock.intern()) {
            File workDir = new File(dirpath);
            try {
                FileUtils.forceMkdir(workDir);
                AppfactoryRepositoryClient client = provider.getRepositoryClient();
                String currentBranch = currentVersion;
                if (AppFactoryConstants.TRUNK.equals(currentVersion)) {             // Since there is no branch called trunk in git
                    currentBranch = MASTER_BRANCH;
                }

                client.retireveMetadata(sourceURL, true, workDir);                  // git clone without checkout
                client.branch(sourceURL, targetVersion, currentBranch, workDir);    // create branch from remote references
                client.checkOut(sourceURL, targetVersion, workDir);                 // checkout new branch

                try {
                    List<File> deletableFiles =
		                    ApplicationTypeManager.getInstance().getApplicationTypeBean(applicationType).getProcessor().
				                    getPreVersionDeleteableFiles(appId,
                                    targetVersion,
                                    currentVersion,
                                    workDir.getAbsolutePath());

                    for (File file : deletableFiles) {
                        client.delete(sourceURL, file, "", workDir);                   // git remove
                    }
                    if (deletableFiles.size() > 0) {
                        String deleteMsg = "Commit by the AppFactory System : deleting files before creating the branch.";
                        client.commitLocally(deleteMsg, true, workDir);             // commit after git remove
                    }
	                ApplicationTypeManager.getInstance().getApplicationTypeBean(applicationType).getProcessor()
	                                      .doVersion(appId, targetVersion, currentVersion, workDir.getAbsolutePath());
                } catch (AppFactoryException e) {
                    String msg = "Could not perform versioning for application : " + appId;
                    log.error(msg, e);
                    throw new RepositoryMgtException(msg, e);
                }
                String commitMsg = "Modified after the branching";
                client.add(sourceURL, workDir, true, false, workDir);               // git add all the new and updated files
                client.commitLocally(commitMsg, true, workDir);                     // git commit after adding new files
                boolean isSuccessful = client.pushLocalCommits(sourceURL, targetVersion, workDir);         // git push
                if (!isSuccessful) {
                    String errorMsg = "Failed to complete git push because remote git server rejected the push command.";
                    log.error(errorMsg);
                    throw new RepositoryMgtException(errorMsg);
                }
            } catch (IOException e) {
                String msg = "Error creating work directory at location" + workDir.getAbsolutePath();
                log.error(msg, e);
                throw new RepositoryMgtException(msg, e);
            } finally {
                try {
                    FileUtils.deleteDirectory(workDir);
                } catch (IOException e) {
                    log.error("Error deleting work directory " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doRepositoryTag(String appId, String currentVersion, String targetVersion,
                                String currentRevision, String tenantDomain) throws RepositoryMgtException {
        throw new RepositoryMgtException("Not supported");
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
        return this.provider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getURLForAppVersion(String applicationKey, String version, String tenantDomain)
            throws RepositoryMgtException {
        return getRepositoryProvider().getAppRepositoryURL(applicationKey, tenantDomain);
    }

    private void generateGitIgnoreRecursively(File workDir) throws RepositoryMgtException {

        if (workDir.isDirectory()) {
            if (workDir.listFiles().length == 0) {
                try {
                    ProjectUtils.generateGitIgnore(workDir.getAbsolutePath());
                } catch (AppFactoryException e) {
                    String msg = "Could not add gitignore files ";
                    log.error(msg, e);
                    throw new RepositoryMgtException(msg, e);
                }
            } else {
                for (File child : workDir.listFiles()) {
                    if (child.isDirectory()) {
                        generateGitIgnoreRecursively(child);
                    }
                }
            }
        }

    }
}
