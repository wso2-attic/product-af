package org.wso2.carbon.appfactory.jenkins.deploy;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.deployers.AbstractStratosUndeployer;
import org.wso2.carbon.appfactory.deployers.util.DeployerUtil;
import org.wso2.carbon.appfactory.jenkins.artifact.storage.Utils;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryMgtException;
import org.wso2.carbon.appfactory.repository.mgt.client.AppfactoryRepositoryClient;
import org.wso2.carbon.appfactory.repository.mgt.git.GitRepositoryClient;
import org.wso2.carbon.appfactory.repository.mgt.git.JGitAgent;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * Concrete implementation of Stratos Undeployer.
 */
public class JenkinsArtifactUndeployer extends AbstractStratosUndeployer {
    private static final Log log = LogFactory.getLog(JenkinsArtifactUndeployer.class);

    public JenkinsArtifactUndeployer() {
    }

    @Override
    public void undeployArtifact(Map<String, String[]> parameters) throws AppFactoryException {

        // Getting required parameters from the request
        String applicationId = DeployerUtil.getParameter(parameters, AppFactoryConstants.APPLICATION_ID);
        String stage = DeployerUtil.getParameter(parameters, AppFactoryConstants.DEPLOY_STAGE);
        String version = DeployerUtil.getParameter(parameters, AppFactoryConstants.APPLICATION_VERSION);
        String applicationType = DeployerUtil.getParameter(parameters, AppFactoryConstants.APP_TYPE);
        String deployerType = DeployerUtil.getParameter(parameters, AppFactoryConstants.DEPLOYER_TYPE);
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
            undeployArtifactFromDepSyncGitRepo(
                    applicationId, stage, version, serverDeploymentPath, fileExtension,
                    applicationTempLocation, generateRepoUrl(parameters));
            log.info("Successfully undeployed the artifact application id : " + applicationId + " stage : " + stage +
                     " version : " + version + " from server deployment path : " + serverDeploymentPath);
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
     * Undeploy the artifacts from dep sync git repo.
     *
     * @param applicationId           application id
     * @param stage                   stage
     * @param version                 version
     * @param serverDeploymentPath    server deployment path eg:"webapps","jaggeyapps"
     * @param fileExtension           file extension  eg:"war"
     * @param applicationTempLocation temp location to checkout
     * @param gitRepoUrl              git repo url
     *                                eg:"https://s2git.appfactory.private.wso2.com:8444/git/Development/as/1.git"
     * @throws RepositoryMgtException
     * @throws AppFactoryException
     */
    private void undeployArtifactFromDepSyncGitRepo(String applicationId,
                                                    String stage, String version,
                                                    String serverDeploymentPath,
                                                    String fileExtension,
                                                    File applicationTempLocation, String gitRepoUrl)
            throws RepositoryMgtException, AppFactoryException {
        AppfactoryRepositoryClient repositoryClient = new GitRepositoryClient(new JGitAgent());
        repositoryClient.init(getAdminUserName(), getAdminPassword());
        repositoryClient.retireveMetadata(gitRepoUrl, false, applicationTempLocation);
        File applicationRootLocation = new File(applicationTempLocation, serverDeploymentPath);

        @SuppressWarnings("unchecked")
        Collection<File> filesToDelete = getFilesToDelete(
                applicationId, version, fileExtension, applicationRootLocation);

        // Removing files from git
        for (File file : filesToDelete) {
            if (log.isDebugEnabled()) {
                log.debug("Removing the file in the path : " + file.getAbsolutePath()+" from dep sync git repo");
            }
            // git remove file
            if (!repositoryClient.delete(gitRepoUrl, file, "Undeploying the file : " + file.getName(),
                                         applicationTempLocation)) {
                String msg = "Unable to remove the file from dep sync git repository : " + file.getAbsolutePath();
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
    }

    @Override
    public String generateRepoUrl(Map parameters)
            throws AppFactoryException {
        String paasRepositoryURLPattern = DeployerUtil.getParameter(parameters,
                                                                    AppFactoryConstants.PAAS_REPOSITORY_URL_PATTERN);
        boolean subscribeOnDeployment = Boolean.parseBoolean(
                DeployerUtil.getParameterValue(parameters, AppFactoryConstants.RUNTIME_SUBSCRIBE_ON_DEPLOYMENT));
        int tenantId = Integer.parseInt(DeployerUtil.getParameter(parameters, "tenantId"));
        String applicationId = DeployerUtil.getParameter(parameters, AppFactoryConstants.APPLICATION_ID);
        String stage = DeployerUtil.getParameterValue(parameters, AppFactoryConstants.DEPLOY_STAGE);
        String tenantDomain = Utils.getEnvironmentVariable(AppFactoryConstants.EVN_VAR_TENANT_DOMAIN);
        String baseUrl = getBaseRepoUrl();
        String gitRepoUrl;
        if (subscribeOnDeployment) {
            gitRepoUrl = baseUrl + AppFactoryConstants.GIT + AppFactoryConstants.URL_SEPERATOR + paasRepositoryURLPattern
                         + AppFactoryConstants.URL_SEPERATOR + tenantId + AppFactoryConstants.URL_SEPERATOR + applicationId
                         + tenantDomain.replace(AppFactoryConstants.DOT_SEPERATOR,
                                                AppFactoryConstants.SUBSCRIPTION_ALIAS_DOT_REPLACEMENT)
                         + AppFactoryConstants.GIT_REPOSITORY_EXTENSION;
        } else {
            String repoFrom = DeployerUtil.getParameterValue(parameters, AppFactoryConstants.REPOSITORY_FROM);
            String preDevRepoNameAppender = "";
            // append _<username>, if the deployment repo is a forked one
            if (AppFactoryConstants.FORK_REPOSITORY.equals(repoFrom)) {
                String tenantAwareUsername = MultitenantUtils.getTenantAwareUsername(
                        DeployerUtil.getParameterValue(parameters, AppFactoryConstants.TENANT_USER_NAME));
                preDevRepoNameAppender = "_" + tenantAwareUsername;
            }

            gitRepoUrl = baseUrl + AppFactoryConstants.GIT + AppFactoryConstants.URL_SEPERATOR + paasRepositoryURLPattern
                         + AppFactoryConstants.URL_SEPERATOR + tenantId + preDevRepoNameAppender
                         + AppFactoryConstants.GIT_REPOSITORY_EXTENSION;
        }
        gitRepoUrl = gitRepoUrl.replace(AppFactoryConstants.STAGE_PLACE_HOLDER, stage);
        return gitRepoUrl;
    }

}
