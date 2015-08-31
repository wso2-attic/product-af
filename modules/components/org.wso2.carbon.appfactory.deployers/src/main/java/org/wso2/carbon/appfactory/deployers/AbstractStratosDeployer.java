/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.appfactory.deployers;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.deployers.util.DeployerUtil;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryMgtException;
import org.wso2.carbon.appfactory.repository.mgt.git.GitRepositoryClient;
import org.wso2.carbon.appfactory.repository.mgt.git.JGitAgent;
import org.wso2.carbon.appfactory.s4.integration.RepositoryProvider;
import org.wso2.carbon.appfactory.s4.integration.StratosRestClient;
import org.wso2.carbon.appfactory.s4.integration.utils.CloudUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

public abstract class AbstractStratosDeployer extends AbstractDeployer {

    private static final Log log = LogFactory
            .getLog(AbstractStratosDeployer.class);

    /**
     * deploying artifacts
     *
     * @param artifactType      type of the artifact war /jaxrs/jaxws etc.
     * @param artifactsToDeploy current artifact to deploy
     * @param parameters        hash map with values needed to deploy an artifact
     * @param notify            notify after deployment happens
     * @throws AppFactoryException
     */
    protected void deploy(String artifactType, File[] artifactsToDeploy,
                          Map<String, String[]> parameters, Boolean notify) throws AppFactoryException {

        String applicationId = DeployerUtil.getParameter(parameters,AppFactoryConstants.APPLICATION_ID);
        String currentVersion = DeployerUtil.getParameter(parameters, AppFactoryConstants.APPLICATION_VERSION);
        String deployStage = DeployerUtil.getParameter(parameters, AppFactoryConstants.DEPLOY_STAGE);
        String serverDeploymentPath = DeployerUtil.getParameter(parameters, AppFactoryConstants.SERVER_DEPLOYMENT_PATHS);
        String tenantDomain = DeployerUtil.getParameter(parameters, AppFactoryConstants.TENANT_DOMAIN);
        int tenantId = Integer.parseInt(DeployerUtil.getParameter(parameters, AppFactoryConstants.TENANT_ID));

        String condition = applicationId + AppFactoryConstants.MINUS + currentVersion + AppFactoryConstants.MINUS +
                           deployStage + AppFactoryConstants.MINUS + tenantDomain;
        String applicationType[] = {artifactType};
        parameters.put(AppFactoryConstants.APPLICATION_TYPE_CONFIG, applicationType);
        synchronized (condition.intern()) {

	        for (File artifactToDeploy : artifactsToDeploy) {
		        String fileName = artifactToDeploy.getName();
		        addToGitRepo(fileName, artifactToDeploy, parameters, serverDeploymentPath, null, tenantDomain,
                             tenantId);
	        }

            if (notify) {
                // git uses "master" for the main branch while , we need to parse "trunk" here for the main branch
                if (AppFactoryConstants.MASTER.equals(currentVersion)) {
                    postDeploymentNoifier(null, applicationId, AppFactoryConstants.TRUNK, artifactType, deployStage,
                                          tenantDomain);
                } else {
                    postDeploymentNoifier(null, applicationId, currentVersion, artifactType, deployStage, tenantDomain);
                }
            }
        }
    }

    private void addToGitRepo(String fileName, File artifacts, Map metadata,String serverDeploymentPath,
                              String relativePathFragment,String tenantDomain,int tenantId) throws AppFactoryException {

        // subscribeOnDeployment is true or not
        boolean subscribeOnDeployment = Boolean.parseBoolean(
                DeployerUtil.getParameterValue(metadata, AppFactoryConstants.RUNTIME_SUBSCRIBE_ON_DEPLOYMENT));
        String applicationId = DeployerUtil.getParameterValue(metadata, AppFactoryConstants.APPLICATION_ID);
        String version = DeployerUtil.getParameterValue(metadata, AppFactoryConstants.APPLICATION_VERSION);
        version = version.replaceAll("\\.+",AppFactoryConstants.MINUS);
        String baseRepoUrl = getBaseRepoUrl();
        String generatedRepoName = generateRepoName(applicationId, version, metadata, tenantId, subscribeOnDeployment);
        String gitRepoUrl = baseRepoUrl +  AppFactoryConstants.GIT_REPOSITORY_CONTEXT + generatedRepoName;
        String stageName = DeployerUtil.getParameterValue(metadata, AppFactoryConstants.DEPLOY_STAGE);

        String applicationAdmin = getAdminUserName();
        String defaultPassword = getAdminPassword();

        // if subscribeOnDeployment is true create a git repo per application version
        if (subscribeOnDeployment) {
          doSubscribeOnDeployment(metadata,generatedRepoName,tenantId,applicationId,version,stageName);
        }

        // Create the temporary directory first. without this we can't proceed
        String path = getTempPath(tenantDomain) + File.separator
                      + stageName + File.separator + StringUtils.deleteWhitespace(fileName).replaceAll("\\.", "_");
        File tempApptypeDirectory = new File(path);
        synchronized (path) {
            log.info("====================Thread name:========"+Thread.currentThread().getName());
            // <tempdir>/jaxrs,
            if (!tempApptypeDirectory.exists()) {
                if (!tempApptypeDirectory.mkdirs()) {
                    String msg = "Unable to create temp directory : "
                                 + tempApptypeDirectory.getAbsolutePath();
                    handleException(msg);
                }
            }

            String appTypeDirectory = null;
            //
            if (serverDeploymentPath != null) {
                appTypeDirectory = tempApptypeDirectory.getAbsolutePath()
                                   + File.separator + serverDeploymentPath; // tempdir/<war>webapps
                // ,tempdir/jaggery/jaggeryapps,
                // //tempdir/esb/synapse-config
            } else {
                appTypeDirectory = tempApptypeDirectory.getAbsolutePath();
            }

            String deployableFileName = null;

            if (StringUtils.isBlank(relativePathFragment)) {
                deployableFileName = appTypeDirectory + File.separator + fileName; // tempdir/webapps/myapp.war
                // ,
                // tempdir/jappgeryapps/myapp.war
            } else {
                deployableFileName = appTypeDirectory + File.separator
                                     + relativePathFragment + File.separator +
                                     fileName; // <tempdir>/synapse-config/proxy-services/MyProxy.xml
            }

            if (log.isDebugEnabled()) {
                log.debug("Deployable file name to be git push:"
                          + deployableFileName);
            }

            GitRepositoryClient repositoryClient = new GitRepositoryClient(new JGitAgent());
            try {
                repositoryClient.init(applicationAdmin, defaultPassword);
                if (tempApptypeDirectory.isDirectory()
                    && tempApptypeDirectory.list().length > 0) {
                    try {
                        FileUtils.cleanDirectory(tempApptypeDirectory);
                    } catch (IOException e) {
                        String msg = "Unable to clean the directory : " + tempApptypeDirectory.getAbsolutePath();
                        throw new AppFactoryException(msg, e);
                    }
//                    File[] filesToDelete = tempApptypeDirectory.listFiles();
//
//                    if (filesToDelete != null) {
//                        for (File fileToDelete : filesToDelete) {
//                            try {
//                                if (fileToDelete.isDirectory()) {
//                                    FileUtils.deleteDirectory(fileToDelete);
//                                } else {
//                                    FileUtils.forceDelete(fileToDelete);
//                                }
//                            } catch (IOException e) {
//                                String msg = "Unable to delete the file : " + fileToDelete.getAbsolutePath();
//                                throw new AppFactoryException(msg, e);
//                            }
//                        }
//                    }
                    repositoryClient.retireveMetadata(gitRepoUrl, false, tempApptypeDirectory);
                } else {
                    // this means no files exists, so we straight away check out the
                    // repo
                    repositoryClient.retireveMetadata(gitRepoUrl, false, tempApptypeDirectory);
                }

                File deployableFile = new File(deployableFileName);
                if (!deployableFile.getParentFile().exists()) {
                    log.debug("deployableFile.getParentFile() doesn't exist: "
                              + deployableFile.getParentFile());
                    if (!deployableFile.getParentFile().mkdirs()) {
                        String msg = "Unable to create parent path of the deployable file "
                                     + deployableFile.getAbsolutePath();
                        // unable to create <tempdir>/war/webapps,
                        // <tempdir>/jaggery/jaggeryapps
                        // or <tempdir>/esb/synapse-config/default/proxy-services
                        handleException(msg);
                    }
                }

                // If there is a file in repo, we delete it first
                if (deployableFile.exists()) {
                    repositoryClient.delete(gitRepoUrl, deployableFile,
                                            "Removing the old file to add the new one", tempApptypeDirectory);
                    // Checking and removing the local file
                    if (deployableFile.exists()) {
                        deployableFile.delete();
                    }
                    // repositoryClient.checkIn(gitRepoUrl, applicationTempLocation,
                    // "Removing the old file to add the new one");

                    try {
                        deployableFile = new File(deployableFileName);
                        // check weather directory exists.
                        if (!deployableFile.getParentFile().isDirectory()) {
                            log.debug("parent directory : "
                                      + deployableFile.getParentFile()
                                    .getAbsolutePath()
                                      + " doesn't exits creating again");
                            if (!deployableFile.getParentFile().mkdirs()) {
                                throw new IOException("Unable to re-create "
                                                      + deployableFile.getParentFile()
                                        .getAbsolutePath());
                            }

                        }

                        if (artifacts.isFile()) {
                            if (!deployableFile.createNewFile()) {
                                throw new IOException(
                                        "unable re-create the target file : "
                                        + deployableFile.getAbsolutePath());
                            }
                            if (deployableFile.canWrite()) {
                                log.debug("Successfully re-created a writable file : "
                                          + deployableFileName);
                            } else {
                                String errorMsg = "re-created file is not writable: "
                                                  + deployableFileName;
                                log.error(errorMsg);
                                throw new IOException(errorMsg);
                            }
                        }

                    } catch (IOException e) {
                        log.error(
                                "Unable to create the new file after deleting the old: "
                                + deployableFile.getAbsolutePath(), e);
                        throw new AppFactoryException(e);
                    }
                }

                copyFilesToGit(artifacts, deployableFile);

                if (repositoryClient.add(gitRepoUrl, deployableFile, true, false, tempApptypeDirectory)) {
                    if (repositoryClient.commitLocally("Adding the artifact to the repo", true, tempApptypeDirectory)) {
                        if (!repositoryClient.pushLocalCommits(gitRepoUrl, AppFactoryConstants.MASTER,
                                                               tempApptypeDirectory)) {
                            String msg = "Unable to push local commits to git repo. Git repo URL : " + gitRepoUrl +
                                         "from local directory " + tempApptypeDirectory.getAbsolutePath();
                            handleException(msg);
                        }
                    } else {
                        String msg =
                                "Unable to commit files locally to location : " +
                                tempApptypeDirectory.getAbsolutePath();
                        handleException(msg);
                    }
                } else {
                    String msg =
                            "Unable to add file : " + deployableFile.getAbsolutePath() + " to git repo : " + gitRepoUrl;
                    handleException(msg);
                }
            } catch (RepositoryMgtException e) {
                String msg = "Unable to add files to git location.";
                handleException(msg, e);
            } finally {
                if (tempApptypeDirectory.exists()) {
                    try {
                        FileUtils.cleanDirectory(tempApptypeDirectory);
                    } catch (IOException e) {
                        String msg = "Unable to clean the directory : " + tempApptypeDirectory.getAbsolutePath();
                        log.warn(msg, e);
                    }
                }
            }
        }
    }

    private void copyFilesToGit(File sourceFile, File destinationFile) throws AppFactoryException {
        try {
            if (sourceFile.isFile()) {
                FileUtils.copyFile(sourceFile, destinationFile);
            } else if(sourceFile.isDirectory()){
                FileUtils.copyDirectory(sourceFile, destinationFile);
            }

        } catch (FileNotFoundException e) {
            log.error(e);
            throw new AppFactoryException(e);
        } catch (IOException e) {
            log.error(e);
            throw new AppFactoryException(e);
        }
    }

    protected String generateRepoName(String applicationId, String version, Map metadata,int tenantId,
                                      boolean subscribeOnDeployment) throws AppFactoryException {
        String paasRepositoryURLPattern = DeployerUtil.getParameter(metadata,
                                                                    AppFactoryConstants.PAAS_REPOSITORY_URL_PATTERN);
        String stage = DeployerUtil.getParameterValue(metadata, AppFactoryConstants.DEPLOY_STAGE);

        String gitRepoName = "";
        if (subscribeOnDeployment) {
            gitRepoName = CloudUtils.generateSingleTenantArtifactRepositoryName(paasRepositoryURLPattern,stage,version,
                                                                                applicationId,tenantId);
        } else {
            String repoFrom = DeployerUtil.getParameterValue(metadata,AppFactoryConstants.REPOSITORY_FROM);
            String preDevRepoNameAppender = "";
            // append _<username>, if the deployment repo is a forked one
            if(AppFactoryConstants.FORK_REPOSITORY.equals(repoFrom))
                preDevRepoNameAppender = "_" + MultitenantUtils.getTenantAwareUsername(
                        DeployerUtil.getParameterValue(metadata,AppFactoryConstants.TENANT_USER_NAME));

            gitRepoName =  AppFactoryConstants.URL_SEPERATOR + paasRepositoryURLPattern
                         + AppFactoryConstants.URL_SEPERATOR + tenantId + preDevRepoNameAppender
                         + AppFactoryConstants.GIT_REPOSITORY_EXTENSION;
            gitRepoName = gitRepoName.replace(AppFactoryConstants.STAGE_PLACE_HOLDER, stage);
        }
        return gitRepoName;
    }

    private void doSubscribeOnDeployment(Map metadata, String generatedRepoName, int tenantId, String applicationId,
                                         String version, String stageName) throws AppFactoryException {

        String cartridgeType = DeployerUtil.getParameter(metadata, AppFactoryConstants.APPLICATION_TYPE_CONFIG);
        String cartridgeTypePrefix = DeployerUtil.getParameter(metadata,
                                                               AppFactoryConstants.RUNTIME_CARTRIDGE_TYPE_PREFIX);
        String deploymentPolicy = DeployerUtil.getParameter(metadata, AppFactoryConstants.RUNTIME_DEPLOYMENT_POLICY);
        String autoScalingPolicy = DeployerUtil.getParameter(metadata, AppFactoryConstants.RUNTIME_AUTOSCALE_POLICY);
        String tenantUsername = DeployerUtil.getParameterValue(metadata, AppFactoryConstants.TENANT_USER_NAME);
        String paasRepoProviderClass = DeployerUtil.getParameter(
                                                   metadata, AppFactoryConstants.PAAS_ARTIFACT_REPO_PROVIDER_CLASS_NAME);

        String stratosRepoPassword = getAdminPassword();
        String stratosRepoUsernmae = getAdminUsername();
        createStratosArticatRepository(paasRepoProviderClass, getAdminUsername(), stratosRepoPassword,
                                       generatedRepoName);

        String uniqueStratosAppId = CloudUtils.generateUniqueStratosApplicationId(tenantId, applicationId, version,
                                                                                  stageName);
        StratosRestClient stratosRestClient = StratosRestClient.getInstance(getStratosServerURL(), tenantUsername);
        // Create stratos application only if it not created for the uniqueStratosAppId
        if (!stratosRestClient.isApplicationCreated(uniqueStratosAppId)) {
            try {
                stratosRestClient.createApplication(uniqueStratosAppId, getBaseRepoUrl(), stratosRepoUsernmae,
                                                     stratosRepoPassword, cartridgeType, cartridgeTypePrefix,
                                                     deploymentPolicy, autoScalingPolicy);
                stratosRestClient.deployApplication(uniqueStratosAppId);
            }catch(AppFactoryException e){
                log.error("Subscribe on deployment was unsuccessful for applicationID : " + uniqueStratosAppId);
            }
        }
    }

    private void createStratosArticatRepository(String repoProviderClassName, String adminUsername,
                                                  String adminPassword, String repoName) throws AppFactoryException {

        ClassLoader loader = getClass().getClassLoader();
        Class<?> repoProviderClass = null;
        try {
            repoProviderClass = Class.forName(repoProviderClassName, true, loader);
        } catch (ClassNotFoundException e) {
            String msg = "Repository is not created for " + repoName + " due to repository provider "
                         + repoProviderClassName+ " not found error";
            log.error(msg);
            throw new AppFactoryException(msg);
        }
        RepositoryProvider repoProvider = null;
        if (repoProviderClass != null) {
            try {
                repoProvider = (RepositoryProvider) repoProviderClass.newInstance();
            } catch (InstantiationException e) {
                String msg = "Repository is not created for " + repoName + " due to repository provider "
                             + repoProviderClassName+ " class loading error";
                log.error(msg);
                throw new AppFactoryException(msg);
            } catch (IllegalAccessException e) {
                String msg = "Repository is not created for " + repoName + " due to illegal access error";
                log.error(msg);
                throw new AppFactoryException(msg);
            }
        }
        repoProvider.setBaseUrl(getBaseRepoUrl());
        repoProvider.setAdminUsername(adminUsername);
        repoProvider.setAdminPassword(adminPassword);
        repoProvider.setRepoName(repoName);
        if(!repoProvider.isRepoExist()){
            repoProvider.createRepository();
        }
    }

    @Override
    public void unDeployArtifact(Map<String, String[]> requestParameters) throws Exception {
        // To change body of implemented methods use File | Settings | File
        // Templates.
    }

    protected abstract String getBaseRepoUrl() throws AppFactoryException;

    protected abstract String getAdminUserName() throws AppFactoryException;

	protected abstract String getAdminPassword() throws AppFactoryException;

    protected abstract String getStratosServerURL() throws AppFactoryException;
}
