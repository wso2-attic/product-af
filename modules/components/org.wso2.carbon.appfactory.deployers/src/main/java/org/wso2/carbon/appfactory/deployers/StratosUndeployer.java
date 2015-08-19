package org.wso2.carbon.appfactory.deployers;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.beans.RuntimeBean;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeBean;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryMgtException;
import org.wso2.carbon.appfactory.repository.mgt.client.AppfactoryRepositoryClient;
import org.wso2.carbon.appfactory.repository.mgt.git.GitRepositoryClient;
import org.wso2.carbon.appfactory.repository.mgt.git.JGitAgent;
import org.wso2.carbon.context.CarbonContext;

import java.io.File;
import java.io.IOException;

/**
 * Concrete implementation of Stratos Undeployer.
 */
public class StratosUndeployer extends AbstractStratosUndeployer {
    private static final Log log = LogFactory.getLog(StratosUndeployer.class);

    public StratosUndeployer() {
    }

    @Override
    public void undeployArtifact(String applicationId, String applicationType, String version, String lifecycleStage,
                                 ApplicationTypeBean applicationTypeBean, RuntimeBean runtimeBean)
                                 throws AppFactoryException {

        String serverDeploymentPath = applicationTypeBean.getServerDeploymentPath();
        String fileExtension = applicationTypeBean.getExtension();

        File applicationTempLocation = Files.createTempDir();
        if (log.isDebugEnabled()) {
            log.debug("Deleting application of application id : " + applicationId + " version : " + version +
                    " stage :  " + lifecycleStage + " application type : " + applicationType +
                    " server deployment path : " + serverDeploymentPath);
        }
        try {
            undeployArtifactFromDepSyncGitRepo(
                    applicationId, lifecycleStage, version, serverDeploymentPath, fileExtension,
                    applicationTempLocation, generateRepoUrl(runtimeBean, applicationId, lifecycleStage));
            log.info("Successfully undeployed the artifact application id : " + applicationId + " stage : " + lifecycleStage +
                    " version : " + version + " from server deployment path : " + serverDeploymentPath);
        } catch (RepositoryMgtException e) {
            String msg = "Undeploying application failed for application id : " + applicationId + " stage : " + lifecycleStage +
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

        // Get file or directory to delete
        File file = getFileToDelete(applicationId, version, fileExtension, applicationRootLocation);

        // Removing files from git
        if (file.exists()) {
            if (log.isDebugEnabled()) {
                log.debug("Removing the file in the path : " + file.getAbsolutePath()+" from dep sync git repo");
            }
            // git remove file
            if (!repositoryClient.delete(gitRepoUrl, file, "Undeploying the file : " + file.getName(),
                    applicationTempLocation)) {
                String msg = "Unable to remove the file from dep sync git repository : " + file.getAbsolutePath();
                throw new AppFactoryException(msg);
            }
            repositoryClient.commitLocally("Undelpoying artifacts of applicationId : " + applicationId, true,
                                           applicationTempLocation);
            repositoryClient.pushLocalCommits(gitRepoUrl, AppFactoryConstants.MASTER, applicationTempLocation);
            if (log.isDebugEnabled()) {
                log.debug("Deleted artifact for applicationId : " + applicationId + " stage : " + stage + " version : "
                          + version + " server deployment path : " + serverDeploymentPath +
                          " application root location : " + applicationRootLocation);
            }
        } else {
            if(log.isDebugEnabled()) {
                log.debug("Unable to remove the file from dep sync git repository: " + file.getAbsolutePath()+
                          ". File: "+file.getName()+" does not exists in the dep sync git repository: "+gitRepoUrl +
                          " in server deployment path: "+serverDeploymentPath);
            }
        }
    }

    @Override
    public String generateRepoUrl(RuntimeBean runtimeBean, String applicationId, String stage)
            throws AppFactoryException {

        String paasRepositoryURLPattern = runtimeBean.getPaasRepositoryURLPattern();
        boolean subscribeOnDeployment = runtimeBean.getSubscribeOnDeployment();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String baseUrl = getBaseRepoUrl();
        String gitRepoUrl;
        if (subscribeOnDeployment) {
            gitRepoUrl = baseUrl + AppFactoryConstants.GIT_REPOSITORY_CONTEXT + AppFactoryConstants.URL_SEPERATOR + paasRepositoryURLPattern
                    + AppFactoryConstants.URL_SEPERATOR + tenantId + AppFactoryConstants.URL_SEPERATOR + applicationId
                    + tenantDomain.replace(AppFactoryConstants.DOT_SEPERATOR,
                    AppFactoryConstants.SUBSCRIPTION_ALIAS_DOT_REPLACEMENT)
                    + AppFactoryConstants.GIT_REPOSITORY_EXTENSION;
        } else {
            String repoFrom = "";
            String preDevRepoNameAppender = "";
            // append _<username>, if the deployment repo is a forked one
            if (AppFactoryConstants.FORK_REPOSITORY.equals(repoFrom)) {
                String tenantAwareUsername = CarbonContext.getThreadLocalCarbonContext().getUsername();
                preDevRepoNameAppender = "_" + tenantAwareUsername;
            }

            gitRepoUrl = baseUrl + AppFactoryConstants.GIT_REPOSITORY_CONTEXT + AppFactoryConstants.URL_SEPERATOR + paasRepositoryURLPattern
                    + AppFactoryConstants.URL_SEPERATOR + tenantId + preDevRepoNameAppender
                    + AppFactoryConstants.GIT_REPOSITORY_EXTENSION;
        }
        gitRepoUrl = gitRepoUrl.replace(AppFactoryConstants.STAGE_PLACE_HOLDER, stage);
        return gitRepoUrl;
    }

}
