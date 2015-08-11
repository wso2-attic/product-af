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

package org.wso2.carbon.appfactory.core.deploy;

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
import org.wso2.carbon.appfactory.core.Storage;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeManager;
import org.wso2.carbon.appfactory.core.dao.JDBCAppVersionDAO;
import org.wso2.carbon.appfactory.core.dao.JDBCApplicationDAO;
import org.wso2.carbon.appfactory.core.dto.Version;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.appfactory.core.util.AppFactoryCoreUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.wso2.carbon.appfactory.core.util.CommonUtil.getAdminUsername;
import static org.wso2.carbon.appfactory.core.util.CommonUtil.getServerAdminPassword;

/**
 * This service will deploy an artifact (specified as a combination of
 * application, stage, version and revision) to a set of servers associated with
 * specified stage ( e.g. QA, PROD)
 */
public class ApplicationDeployer {

    private static final Log log = LogFactory.getLog(ApplicationDeployer.class);

    /**
     * Service method to get the artifact information for the given applicationId.
     *
     * @param applicationId
     * @throws AppFactoryException
     */
    public List<Version> getArtifactInformation(String applicationId) throws AppFactoryException {
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
//        try {
            //TODO - Check Punnadi why is this needed?
            //return JDBCAppVersionDAO.getInstance().getAllVersionsOfApplication(applicationId);
//        } catch (AppFactoryException e) {
//            log.error("Error while retrieving artifat information from database for application " + applicationId);
//            throw new AppFactoryException(e.getMessage());
//        }
        return null;
    }

    /**
     * Service method to update the latest deployed build information.
     * This service will be called from Jenkins when the deployment is done.
     *
     * @throws AppFactoryException
     */

    // We are passing the tenant domain as a parameter since this is called within jenkins
    public void updateDeploymentInformation(String applicationId, String stage, String version, String buildId,
                                            String tenantDomain) throws AppFactoryException {
        if (log.isDebugEnabled()) {
            log.debug("Deployment information updation service called for application key : " + applicationId
                      + " stage : " + stage + " version : " + version + " build id : " + buildId + " tenant domain : "
                      + tenantDomain);
        }
        if (AppFactoryUtil.checkAuthorizationForUser(AppFactoryConstants.PERMISSION_DEPLOY_TO + stage,
                                                     AppFactoryConstants.DEFAULT_ACTION)) {
            JDBCApplicationDAO applicationDAO = JDBCApplicationDAO.getInstance();
            try {
                log.info("Waiting before sending deployment notification");
                Thread.sleep(1000 * 15); // since build and deploy updates can override each other, sleep for 15sec
            } catch (InterruptedException e) {
                log.warn("Error while waiting before doing deploy update", e);
            }
            try {
                int tenantId = MultitenantConstants.INVALID_TENANT_ID;
                try {
                    tenantId = ServiceHolder.getRealmService().getTenantManager().getTenantId(tenantDomain);
                } catch (UserStoreException e) {
                    log.info("Deployment information successfully updated ");
                    String msg = "Unable to get the tenant Id for domain : " + tenantDomain;
                    log.error(msg, e);
                }
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                applicationDAO.updateLastDeployedBuildID(applicationId, version, stage, false, null, buildId);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
            if (log.isDebugEnabled()) {
                log.debug("Deployment information successfully updated for application key : " + applicationId +
                          " stage : " + stage + " version : " + version + " build id : " + buildId + " tenant domain : "
                          + tenantDomain);
            }
        } else {
            log.error("No permission to update deployment information for application key : " + applicationId + " stage" +
                      " : " + stage + " version : " + version + " build id : " + buildId + " tenant domain : "
                      + tenantDomain);
        }
    }

    /**
     * Deploys the Artifact to specified stage.
     *
     * @param applicationId The application Id.
     * @param stage         The stage to deploy ( e.g. QA, PROD)
     * @param version       Version of the application
     * @return An array of {@link ArtifactDeploymentStatusBean} indicating the
     * status of each deployment operation.
     * @throws AppFactoryException
     */
    public ArtifactDeploymentStatusBean[] deployArtifact(String applicationId, String stage, String version,
                                    String tagName, String deployAction, String repoFrom) throws AppFactoryException {
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String userName = CarbonContext.getThreadLocalCarbonContext().getUsername();
        String applicationType = null;
        try {
            applicationType = AppFactoryCoreUtil.getApplicationType(applicationId, tenantDomain);
        } catch (RegistryException e) {
            String errorMsg = "Unable to find the application type for application key : " + applicationId;
            handleException(errorMsg, e);
        }

            log.info("Deploy artifact is called for application key : " + applicationId + " stage : " + stage +
                     " version : " + version + " tag name : " + tagName + " deploy action : " + deployAction +
                     " in tenant: " + tenantDomain + " by user:" + userName);
            boolean appIsBuildServerRequired = AppFactoryCoreUtil.isBuildServerRequiredProject(applicationType);
            Storage storage = null;
            if (appIsBuildServerRequired) {
                storage = ServiceHolder.getStorage(AppFactoryConstants.BUILDABLE_STORAGE_TYPE);
            } else {
                storage = ServiceHolder.getStorage(AppFactoryConstants.NONBUILDABLE_STORAGE_TYPE);
            }
            log.info("deploying artifact of application key : " + applicationId + " version : " + version +
                     " application Type : " + applicationType + " Stage : " + stage + " in tenant : " +
                     tenantDomain + " by user : " + userName);

            storage.deployArtifact(applicationId, version, "", applicationType, stage, tenantDomain, userName,
                                   deployAction, repoFrom);
        return null;
    }

    /**
     * Get the artifact details
     * @param file
     * @return
     * @throws AppFactoryException
     */
    public String getArtifactDetails(File file) throws AppFactoryException {
        String artifactDetails = null;
        String fileName;

        if (file == null) {
            return "Not Found";
        }

        fileName = file.getName();
        if (fileName.endsWith(".war")) {
            String artifactVersion = fileName.substring(fileName.indexOf('-') + 1, fileName.indexOf(".war"));

            String artifactName = fileName.substring(0, fileName.indexOf('-'));
            artifactDetails = artifactName + '-' + artifactVersion;
            return artifactDetails;

        } else if (fileName.endsWith(".car")) {
            fileName = file.getAbsolutePath();
            FileInputStream fileInputStream;
            try {
                fileInputStream = new FileInputStream(fileName);
            } catch (FileNotFoundException e) {
                String msg = "Unable to find file : " + fileName;
                log.error(msg, e);
                throw new AppFactoryException(msg, e);
            }

            ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);
            ZipEntry zipEntry;

            try {
                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    String entryName = zipEntry.getName();
                    if (!entryName.equals("artifacts.xml")) {
                        // byte[] buf = new byte[1024];
                        log.info("Name of  Zip Entry : " + entryName);
                        String artifactVersion = entryName.substring(entryName.indexOf('_') + 1);
                        String artifactName = entryName.substring(0, entryName.indexOf('_'));
                        zipInputStream.close();
                        fileInputStream.close();

                        artifactDetails = artifactName + '-' + artifactVersion;
                        return artifactDetails;
                    }

                }
            } catch (IOException e) {
                String msg = "Unable to complete operation";
                log.error(msg, e);
                throw new AppFactoryException(msg, e);
            }
        }
        return artifactDetails;

    }

    /**
     * Get the stage of the application version
     *
     * @param applicationId application key
     * @param version       version
     * @return              stage
     * @throws AppFactoryException
     */
    public String getStage(String applicationId, String version) throws AppFactoryException {
        return JDBCAppVersionDAO.getInstance().getAppVersionStage(applicationId, version);
    }

    /**
     *Get the tag names of persisted artifacts of a given application version
     *
     * @param applicationId application key
     * @param version       version
     * @return tag names array
     * @throws AppFactoryException
     */
    public String[] getTagNamesOfPersistedArtifacts(String applicationId, String version) throws AppFactoryException {

        // Getting the tenant domain
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String applicationType = null;
        try {
            applicationType = AppFactoryCoreUtil.getApplicationType(applicationId, tenantDomain);
        } catch (RegistryException e) {
            String errorMsg = "Unable to find the application type for application id : " + applicationId;
            handleException(errorMsg, e);
        }
        boolean appIsBuildServerRequired = AppFactoryCoreUtil.isBuildServerRequiredProject(applicationType);
        Storage storage = null;
        if (appIsBuildServerRequired) {
            storage = ServiceHolder.getStorage(AppFactoryConstants.BUILDABLE_STORAGE_TYPE);
        } else {
            storage = ServiceHolder.getStorage(AppFactoryConstants.NONBUILDABLE_STORAGE_TYPE);
        }
        return storage.getTagNamesOfPersistedArtifacts(applicationId, version, "", tenantDomain);
    }

    /**
     * Deleting an application from given environment
     *
     * @param stage         Stage to identify the environment
     * @param applicationId Application ID which needs to delete
     * @return boolean
     * @throws AppFactoryException An error
     */
    public boolean unDeployArtifact(String stage, String applicationId, String version) throws AppFactoryException {
        String event = "Deleting application " + applicationId + " in version for " + version + ", from " + stage
                       +" stage";
        log.info(event);
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String applicationType = null;
        try {
            applicationType = AppFactoryCoreUtil.getApplicationType(applicationId, tenantDomain);
        } catch (RegistryException e) {
            String errorMsg = "Unable to find the application type for application id : " + applicationId;
            handleException(errorMsg, e);
        }
        deleteFromDepSyncGitRepo(applicationId, version, applicationType, stage);
        return true;
    }

    private void handleException(String msg) throws AppFactoryException {
        log.error(msg);
        throw new AppFactoryException(msg);
    }

    private void handleException(String msg, Throwable throwable) throws AppFactoryException {
        log.error(msg, throwable);
        throw new AppFactoryException(msg, throwable);
    }

    private String getDeploymentHostFromUrl(String url) throws AppFactoryException {
        String hostName = null;
        try {
            URL deploymentURL = new URL(url);
            hostName = deploymentURL.getHost();
        } catch (MalformedURLException e) {
            handleException("Deployment url is malformed.", e);
        }
        return hostName;
    }

    protected String getParameterValue(Map metadata, String key) {
        if (metadata.get(key) == null) {
            return null;
        }
        if (metadata.get(key) instanceof String[]) {
            String[] values = (String[]) metadata.get(key);
            if (values.length > 0) {
                return values[0];
            }
            return null;
        } else if (metadata.get(key) instanceof String) {
            return metadata.get(key).toString();
        }
        return null;
    }

    protected String[] getParameterValues(Map metadata, String key) {
        if (metadata.get(key) == null) {
            return null;
        }
        if (metadata.get(key) instanceof String[]) {
            return (String[]) metadata.get(key);
        } else if (metadata.get(key) instanceof String) {
            return new String[]{metadata.get(key).toString()};
        }
        return null;
    }

    /**
     * Generate the repository URL (to commit the application artifact)
     *
     * @param applicationId   application Id
     * @param applicationType type of the application
     * @param stage           the stage
     * @return the repository URL
     */
    private String generateRepoUrl(String applicationId, String applicationType, String stage) {
        String baseUrl = getBaseUrl(applicationType, stage);
        String template = getUrlPattern(applicationType, stage);
        String gitRepoUrl = baseUrl + "git/" + template;
        return gitRepoUrl.replace("{@application_key}", applicationId).replace("{@stage}", stage);
    }

    private String getGitRepoUrlForTenant(String applicationId, String applicationType, String stage, int tenantId) {
        String tenantRepoUrl = generateRepoUrl(applicationId, applicationType, stage).concat("/").concat(
                String.valueOf(tenantId)).concat(".git");
        if (log.isDebugEnabled()) {
            log.debug("TenantRepoURL:" + tenantRepoUrl);
        }
        return tenantRepoUrl;
    }

    /**
     * Reads {@link AppFactoryConfiguration} and returns the repository provider
     * URL pattern (of the repository).
     * if a configuration doesn't exists for a particular application type
     * default value will be returned ( - which is configured using '*' )
     *
     * @param applicationType type of the application.
     * @param stage           the stage/environment
     * @return the pattern
     */
    private String getUrlPattern(String applicationType, String stage) {
        String template = ServiceHolder.getAppFactoryConfiguration().getFirstProperty("ApplicationDeployment." +
                "DeploymentStage." + stage + ".Deployer.ApplicationType." + applicationType + "RepositoryProvider." +
                "Property.URLPattern");
        if (StringUtils.isBlank(template)) {

            // default to "*"
            template = ServiceHolder.getAppFactoryConfiguration().getFirstProperty("ApplicationDeployment." +
                 "DeploymentStage." + stage + ".Deployer.ApplicationType.*.RepositoryProvider.Property.URLPattern");
        }
        return template;
    }

    /**
     * Reads {@link AppFactoryConfiguration} and returns the repository provider
     * URL.
     * if a configuration doesn't exists for a particular application type
     * default value will be returned ( - which is configured using '*' )
     *
     * @param applicationType type of the application.
     * @param stage           the stage/environment
     * @return the URL
     */
    private String getBaseUrl(String applicationType, String stage) {
        String baseUrl = ServiceHolder.getAppFactoryConfiguration().getFirstProperty("ApplicationDeployment.DeploymentStage."
                + stage +".Deployer.ApplicationType." + applicationType +"RepositoryProvider.Property.BaseURL");
        if (StringUtils.isBlank(baseUrl)) {

            // default to "*"
            baseUrl = ServiceHolder.getAppFactoryConfiguration().getFirstProperty("ApplicationDeployment.DeploymentStage." +
                 stage + ".Deployer.ApplicationType.*.RepositoryProvider.Property.BaseURL");
        }
        return baseUrl;
    }

    /**
     * Reads {@link AppFactoryConfiguration} and returns the repository provider
     * user name.
     * if a configuration doesn't exists for a particular application type
     * default value will be returned ( - which is configured using '*' )
     *
     * @param applicationType type of the application.
     * @param stage           the stage/environment
     * @return the user name
     */
    private String getRepositoryProviderAdminUser(String applicationType, String stage) {
        String adminUser = ServiceHolder.getAppFactoryConfiguration().getFirstProperty("ApplicationDeployment.DeploymentStage."
                + stage + ".Deployer.ApplicationType." + applicationType + "RepositoryProvider.Property.AdminUserName");
        if (StringUtils.isBlank(adminUser)) {

            // default to "*"
            adminUser = ServiceHolder.getAppFactoryConfiguration().getFirstProperty("ApplicationDeployment.DeploymentStage." +
                  stage + ".Deployer.ApplicationType.*.RepositoryProvider.Property.AdminUserName");
        }
        return adminUser;
    }

    /**
     * Reads {@link AppFactoryConfiguration} and returns the repository provider
     * password.
     * if a configuration doesn't exists for a particular application type
     * default value will be returned ( - which is configured using '*' )
     *
     * @param applicationType type of the application.
     * @param stage           the stage/environment
     * @return the password
     */
    private String getRepositoryProviderAdminPassword(String applicationType, String stage) {
        AppFactoryConfiguration appFactoryConfiguration = ServiceHolder.getAppFactoryConfiguration();
        String adminUser = appFactoryConfiguration.getFirstProperty("ApplicationDeployment.DeploymentStage." + stage +
                 ".Deployer.ApplicationType." + applicationType + "RepositoryProvider.Property.AdminPassword");
        if (StringUtils.isBlank(adminUser)) {

            // default to "*"
            adminUser = appFactoryConfiguration.getFirstProperty("ApplicationDeployment.DeploymentStage." + stage +
                ".Deployer.ApplicationType.*.RepositoryProvider.Property.AdminPassword");
        }
        return adminUser;
    }

    /**
     * Deletes a artifact corresponding to specified application id, version, stage
     *
     * @param applicationId   application Id
     * @param version         version of the application that needs be undeployed.
     * @param applicationType type of the application (war, jaxrs etc)
     * @param stage           currently deployed stage
     * @throws AppFactoryException if an error occurs.
     */
    private void deleteFromDepSyncGitRepo(String applicationId, String version, String applicationType, String stage)
            throws AppFactoryException {
        String repoProviderAdminName = getRepositoryProviderAdminUser(applicationType, stage);
        String repoProviderAdminPassword = getRepositoryProviderAdminPassword(applicationType, stage);
        File applicationTempLocation = Files.createTempDir();
        try {
            AppfactoryRepositoryClient repositoryClient = new AppfactoryRepositoryClient(AppFactoryConstants.REPOSITORY_TYPE_GIT);
            String gitRepoUrl = generateRepoUrl(applicationId, applicationType, stage);
            repositoryClient.init(repoProviderAdminName, repoProviderAdminPassword);
            repositoryClient.checkOut(gitRepoUrl, applicationTempLocation);

            // dbs files are copied to multiple server locations
            String[] deployedServerPaths = getServerDeploymentPaths(applicationType);
            for (String serverPath : deployedServerPaths) {
                File applicationRootLocation = new File(applicationTempLocation, serverPath);
                if (log.isDebugEnabled()) {
                    log.debug("applicationRootLocation : " + applicationRootLocation.getAbsolutePath());
                }
                if (applicationRootLocation.isDirectory()) {
                    String fileExtension = getFileExtension(applicationType);
                    if (log.isDebugEnabled()) {
                        log.debug("search for a file corresponding to : " + applicationId + " version :" + version +
                                  " extension" + fileExtension);
                    }
                    Collection<File> filesToDelete =
                            getFilesToDelete(applicationId, version, applicationRootLocation,
                                             fileExtension, applicationType);
                    for (File f : filesToDelete) {
                        if (log.isDebugEnabled()) {
                            log.debug("git removing the file : " + f.getAbsolutePath());
                        }
                        if (!repositoryClient.remove(gitRepoUrl, f, "Undeploying the file : " + f.getName())) {
                            if (log.isDebugEnabled()) {
                                log.debug("unable to remove the file from git repository" + f.getAbsolutePath());
                            }
                        }
                    }
                } else {
                    log.error("unable to find correct directory structure in git repository : " +
                              applicationRootLocation.getAbsolutePath());
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("checking in git at : " + applicationTempLocation);
            }
            repositoryClient.checkIn(gitRepoUrl, applicationTempLocation, "Undelpoying artifacts");
        } catch (AppFactoryException e) {

            String msg =
                    "Undeploying application failed: Unable to delete files from git repository application id: " +
                    applicationId + " version :" + version + " stage : " + stage;
            handleException(msg, e);
        } finally {

            try {
                FileUtils.deleteDirectory(applicationTempLocation);
            } catch (IOException ioe) {
                // we ignore error of not being able to delete temporary
                // directory.
                log.error("Unable to delete the temporary directory after"
                          + " application demote operation, error will be ignored", ioe);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Collection<File> getFilesToDelete(String applicationId, String version, File applicationRootLocation,
                                              String fileExtension, String applicationType) {

        if (AppFactoryConstants.APPLICATION_TYPE_ESB.equals(applicationType)) {

            return FileUtils.listFiles(applicationRootLocation,
                                       new String[]{AppFactoryConstants.APPLICATION_TYPE_XML}, true);

        } else {
            return FileUtils.listFiles(applicationRootLocation, new ArtifactFileFilter(applicationId, version,
                                                                                       fileExtension), null);
        }
    }

    /**
     * Reads {@link AppFactoryConfiguration} and returns a list of locations (or
     * most of the time one location) a specified application type is deployed
     * in a server
     *
     * @param applicationType type of the application
     * @return an array of String
     */
    private String[] getServerDeploymentPaths(String applicationType) {
        String paths = null;
        try {
            paths = ApplicationTypeManager.getInstance().getApplicationTypeBean(
                    applicationType).getServerDeploymentPath();
        } catch (AppFactoryException e) {
            log.error("Error while retrieving the server deployment path", e);
        }
        return StringUtils.isNotBlank(paths) ? paths.trim().split(",") : new String[0];
    }

    /**
     * Reads {@link AppFactoryConfiguration} and returns file extension of a particular application type
     *
     * @param applicationType type of the application ( war, jaxrs etc).
     * @return file extension.
     */
    private String getFileExtension(String applicationType) {
        String extension = null;
        try {
            extension = ApplicationTypeManager.getInstance().getApplicationTypeBean(applicationType).getExtension();
        } catch (AppFactoryException e) {
            log.error("Error while retrieving the extension ", e);
        }
        return StringUtils.isBlank(extension) || extension.equals("none") ? null : extension.trim();

    }

    /**
     * Used to filter artifact(s)/ corresponding to specified application id, version and file extension
     */
    class ArtifactFileFilter implements IOFileFilter {
        /**
         * Name of the file.
         */
        private String fileName;

        /**
         * Constructor of the class.
         *
         * @param applicationId application Id
         * @param version       version
         * @param extension     file extension
         */
        public ArtifactFileFilter(String applicationId, String version, String extension) {

            if (AppFactoryConstants.TRUNK.equalsIgnoreCase(version)) {
                fileName = applicationId + AppFactoryConstants.SNAPSHOT;
            } else {

            // file naming convention for trunk is different.
                fileName = applicationId + AppFactoryConstants.MINUS + version;
            }
            fileName = fileName + AppFactoryConstants.DOT + extension;
        }

        /**
         * Only files are accepted (not directories). they should match the expected file name.
         */
        @Override
        public boolean accept(File file) {
            return file.isFile() && file.getName().equals(fileName);
        }

        /**
         * No directories are accepted.
         */
        @Override
        public boolean accept(File dir, String name) {
            return false;
        }

    }
}
