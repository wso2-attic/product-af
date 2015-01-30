/*
 *
 *  Copyright 2014 WSO2, Inc. (http://wso2.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * 
 */

package org.wso2.carbon.appfactory.deployers;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.Undeployer;
import org.wso2.carbon.appfactory.deployers.util.DeployerUtil;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryManager;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryMgtException;
import org.wso2.carbon.appfactory.repository.mgt.client.AppfactoryRepositoryClient;
import org.wso2.carbon.context.CarbonContext;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * This class is used to undeploy artifacts in Git repository.
 */
public abstract class JenkinsStratosUndeployer implements Undeployer {

    private static final Log log = LogFactory.getLog(JenkinsStratosUndeployer.class);
    protected static final String DEFAULT_SNAPSHOT = "-default-SNAPSHOT";
    protected static final String APPLICATION_KEY_REGEX = "{@application_key}";
    protected static final String STAGE_REGEX = "{@stage}";

    /**
     * This method is used to undeploy artifacts. This will do the undeploy job with in build server without letting
     * appfactory to do it. Everything will happen in jenkins side.
     *
     * @param parameters this map contains values related to artifact which is going to be undeployed. eg :
     *                   application type, deployer type, stage, version, application id etc.
     * @throws AppFactoryException
     */
    @Override
    public void undeployArtifact(Map<String, String[]> parameters) throws AppFactoryException {

        String applicationId = DeployerUtil.getParameter(parameters, AppFactoryConstants.APPLICATION_ID);
        String stage = DeployerUtil.getParameter(parameters, AppFactoryConstants.DEPLOY_STAGE);
        String version = DeployerUtil.getParameter(parameters, AppFactoryConstants.APPLICATION_VERSION);
        String applicationType = DeployerUtil.getParameter(parameters, AppFactoryConstants.APP_TYPE);
        String deployerType = DeployerUtil.getParameter(parameters, AppFactoryConstants.DEPLOYER_TYPE);
        String repoProviderAdminName = AppFactoryUtil.getAppfactoryConfiguration().
                getFirstProperty(AppFactoryConstants.PAAS_ARTIFACT_STORAGE_REPOSITORY_PROVIDER_ADMIN_USER_NAME);
        String repoProviderAdminPassword = AppFactoryUtil.getAppfactoryConfiguration().
                getFirstProperty(AppFactoryConstants.PAAS_ARTIFACT_STORAGE_REPOSITORY_PROVIDER_ADMIN_PASSWORD);
        String serverDeploymentPath = DeployerUtil
                .getParameter(parameters, AppFactoryConstants.SERVER_DEPLOYMENT_PATHS);
        String fileExtension = DeployerUtil.getParameter(parameters, AppFactoryConstants.APPLICATION_EXTENSION);
        File applicationTempLocation = Files.createTempDir();
        if (log.isDebugEnabled()) {
            log.debug("Deleting application of application id : " + applicationId + " version : " + version +
                      " stage :  " + stage + " application type : " + applicationType + " deployer type : " +
                      deployerType + " server deployment path : " + serverDeploymentPath);
        }
        try {
            AppfactoryRepositoryClient repositoryClient =
                    (new RepositoryManager()).getRepositoryProvider(AppFactoryConstants.GIT).getRepositoryClient();
            String gitRepoUrl = generateRepoUrl(deployerType, applicationId, stage);
            repositoryClient.init(repoProviderAdminName, repoProviderAdminPassword);
            repositoryClient.retireveMetadata(gitRepoUrl, false, applicationTempLocation);
            File applicationRootLocation = new File(applicationTempLocation, serverDeploymentPath);
            @SuppressWarnings("unchecked")
            Collection<File> filesToDelete =
                    FileUtils.listFiles(applicationRootLocation,
                                        new ArtifactFileFilter(applicationId, version, fileExtension),
                                        null);
            for (File file : filesToDelete) {
                if (log.isDebugEnabled()) {
                    log.debug("Removing the file in the path : " + file.getAbsolutePath());
                }
                if (!repositoryClient.delete(gitRepoUrl, file, "Undeploying the file : " + file.getName(),
                                             applicationTempLocation)) {
                    String msg = "Unable to remove the file from git repository : " + file.getAbsolutePath();
                    throw new AppFactoryException(msg);
                }
            }

            repositoryClient.commitLocally("Undelpoying artifacts of applicationId : " + applicationId, true,
                                           applicationTempLocation);
            repositoryClient.pushLocalCommits(gitRepoUrl, AppFactoryConstants.MASTER, applicationTempLocation);
            if (log.isDebugEnabled()) {
                log.debug("Deleted artifact for applicationId : " + applicationId + " stage : " + stage + " version : "
                          + version + " server deployment path : " + serverDeploymentPath +
                          " application root location : " + applicationRootLocation);
            }
        } catch (RepositoryMgtException e) {
            String msg = "Undeploying application failed for application id : " + applicationId + " stage : " + stage +
                         " version : " + version + " server deployment path : " + serverDeploymentPath;
            throw new AppFactoryException(msg, e);
        } finally {
            try {
                // cleaning up temp directory
                FileUtils.deleteDirectory(applicationTempLocation);
            } catch (IOException e) {
                log.error("Failed to delete temp application directory : "
                          + applicationTempLocation.getAbsolutePath(), e);
            }

        }
    }

    /**
     * Generate the repository URL (to commit the application artifact)
     *
     * @param deployerType  this means the type of the deployment eg : * / .net /php
     * @param applicationId application Id
     * @param stage         the stage
     * @return the repository URL
     */
    private String generateRepoUrl(String deployerType, String applicationId, String stage)
            throws AppFactoryException {
        String baseUrl = getBaseUrl(deployerType, stage);
        String template = getUrlPattern(deployerType, stage);
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String gitRepoUrl = baseUrl + AppFactoryConstants.GIT + File.separator + template + File.separator + tenantId
                            + AppFactoryConstants.DOT_SEPERATOR + AppFactoryConstants.GIT;
        return gitRepoUrl.replace(APPLICATION_KEY_REGEX, applicationId).replace(STAGE_REGEX, stage);
    }

    /**
     * Reads {@link org.wso2.carbon.appfactory.common.AppFactoryConfiguration} and returns the repository provider URL.
     * if a configuration doesn't exists for a particular application type default value will be returned ( - which is
     * configured using '*' )
     *
     * @param deployerType this means the type of the deployment eg : * / .net /php
     * @param stage        stage
     * @return base URL
     * @throws AppFactoryException
     */
    private String getBaseUrl(String deployerType, String stage) throws AppFactoryException {
        AppFactoryConfiguration configuration = AppFactoryUtil.getAppfactoryConfiguration();
        String baseUrl = configuration.getFirstProperty(
                AppFactoryConstants.DEPLOYMENT_STAGES + AppFactoryConstants.DOT_SEPERATOR + stage +
                AppFactoryConstants.DOT_SEPERATOR + AppFactoryConstants.DEPLOYER_APPLICATION_TYPE +
                AppFactoryConstants.DOT_SEPERATOR + deployerType + AppFactoryConstants.DOT_SEPERATOR +
                AppFactoryConstants.REPOSITORY_PROVIDER_PROPERTY + AppFactoryConstants.DOT_SEPERATOR +
                AppFactoryConstants.BASE_URL);
        if (StringUtils.isBlank(baseUrl)) {
            String msg = "No base URL configured for deployer type : " + deployerType + " and stage : " + stage;
            throw new AppFactoryException(msg);
        }
        return baseUrl;
    }

    /**
     * Reads {@link org.wso2.carbon.appfactory.common.AppFactoryConfiguration} and returns the repository provider URL
     * pattern (of the repository). if a configuration doesn't exists for a particular application type default value
     * will be returned ( - which is configured using '*' )
     *
     * @param applicationType type of the application.
     * @param stage           the stage/environment
     * @return the pattern
     * @throws AppFactoryException
     */

    private String getUrlPattern(String applicationType, String stage) throws AppFactoryException {
        AppFactoryConfiguration configuration = AppFactoryUtil.getAppfactoryConfiguration();
        String template = configuration.getFirstProperty(AppFactoryConstants.DEPLOYMENT_STAGES +
                                                         AppFactoryConstants.DOT_SEPERATOR + stage + AppFactoryConstants.DOT_SEPERATOR +
                                                         AppFactoryConstants.DEPLOYER_APPLICATION_TYPE +
                                                         AppFactoryConstants.DOT_SEPERATOR + applicationType +
                                                         AppFactoryConstants.DOT_SEPERATOR +
                                                         AppFactoryConstants.REPOSITORY_PROVIDER_PROPERTY +
                                                         AppFactoryConstants.DOT_SEPERATOR + AppFactoryConstants.URL_PATTERN);
        if (StringUtils.isBlank(template)) {
            String msg = "No URL pattern configured for application type : " + applicationType + " and stage : " +
                         stage;
            throw new AppFactoryException(msg);
        }
        return template;
    }

    /**
     * Used to filter artifact(s)/ corresponding to specified application id, version and file extension
     */
    private static class ArtifactFileFilter implements IOFileFilter {

        private String fileName;

        /**
         * Constructor of the class.
         *
         * @param applicationId application Id
         * @param version       version
         * @param extension     file extension
         */

        public ArtifactFileFilter(String applicationId, String version, String extension) {
            if (AppFactoryConstants.TRUNK.equals(version)) {
                fileName = applicationId + DEFAULT_SNAPSHOT;
            } else {
                fileName = applicationId + AppFactoryConstants.MINUS + version;
            }
            fileName = fileName + AppFactoryConstants.DOT_SEPERATOR + extension;
        }

        /**
         * Only files are accepted (not directories). they should match the expected file name.
         *
         * @param file file to be checked
         */
        @Override
        public boolean accept(File file) {
            return file.isFile() && file.getName().equals(fileName);
        }

        /**
         * No directories are accepted.
         *
         * @param dir  the directory File to check
         * @param name the filename within the directory to check
         */
        @Override
        public boolean accept(File dir, String name) {
            return false;
        }
    }
}
