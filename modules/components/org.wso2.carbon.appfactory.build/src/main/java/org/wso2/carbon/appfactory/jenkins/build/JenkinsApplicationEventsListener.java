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

package org.wso2.carbon.appfactory.jenkins.build;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.ApplicationEventsHandler;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeManager;
import org.wso2.carbon.appfactory.core.dto.Application;
import org.wso2.carbon.appfactory.core.dto.UserInfo;
import org.wso2.carbon.appfactory.core.dto.Version;
import org.wso2.carbon.appfactory.core.governance.RxtManager;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.appfactory.core.util.AppFactoryCoreUtil;
import org.wso2.carbon.appfactory.eventing.AppFactoryEventException;
import org.wso2.carbon.appfactory.eventing.Event;
import org.wso2.carbon.appfactory.eventing.EventNotifier;
import org.wso2.carbon.appfactory.eventing.builder.utils.AppCreationEventBuilderUtil;
import org.wso2.carbon.appfactory.jenkins.build.internal.ServiceContainer;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryMgtException;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryProvider;
import org.wso2.carbon.appfactory.repository.mgt.internal.Util;
import org.wso2.carbon.appfactory.utilities.project.ProjectUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * Listens to Application events (such as creation, user addition etc) and makes
 * relevant changes on Jenkins CI server.
 */
public class JenkinsApplicationEventsListener extends ApplicationEventsHandler {

    private static Log log = LogFactory.getLog(JenkinsApplicationEventsListener.class);
    private RxtManager rxtManager;

    /**
     * Creates a listener instance with given priority.
     *
     * @param priority The Priority
     */
    public JenkinsApplicationEventsListener(String identifier, int priority) {
    	super(identifier, priority);
        this.identifier = AppFactoryConstants.JENKINS;
        this.rxtManager = RxtManager.getInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreation(Application application, String userName, String tenantDomain, boolean isUploadableAppType)
            throws AppFactoryException {

        if (!AppFactoryCoreUtil.isBuildServerRequiredProject(application.getType())) {
            return;
        }

        log.info("Application Creation event recieved for : " + application.getId() + " " +
                application.getName());

        JenkinsCISystemDriver jenkinsCISystemDriver = ServiceContainer.getJenkinsCISystemDriver();

        RepositoryProvider repoProvider =
                Util.getRepositoryProvider(application.getRepositoryType());

        String repoURL = "";
        try {
            repoURL = repoProvider.getAppRepositoryURL(application.getId(), tenantDomain);
        } catch (RepositoryMgtException e) {
            log.error("Error occured whe creating repository URL," + e.getMessage());
            throw new AppFactoryException(e);
        }

        String initialVersion = "trunk";
        if(isUploadableAppType){
        	initialVersion = AppFactoryConstants.INITIAL_UPLOADED_APP_VERSION;
        }
        jenkinsCISystemDriver.createJob(application.getId(), initialVersion, "", tenantDomain, userName,
                repoURL, AppFactoryConstants.ORIGINAL_REPOSITORY);

        //TODO add app users
//        jenkinsCISystemDriver.setupApplicationAccount(application.getId(), tenantDomain);
//        // adding app creator to jenkins
//        jenkinsCISystemDriver.addUsersToApplication(application.getId(),
//                new String[]{userName.split("@")[0]},
//                tenantDomain);
        Version[] versions = ProjectUtils.getVersions(application.getId(), tenantDomain);
        String stage = rxtManager.getStage(application.getId(), versions[0].getId(), tenantDomain);
        if (ArrayUtils.isNotEmpty(versions)) {
            // No need to create job.
            jenkinsCISystemDriver.startBuild(application.getId(), versions[0].getId(), true, stage,
                    "", tenantDomain, userName,
                    AppFactoryConstants.ORIGINAL_REPOSITORY);
        }
        try {
            String infoMsg = "Jenkins space created for " + application.getName() + ".";

            EventNotifier.getInstance().notify(AppCreationEventBuilderUtil.buildApplicationCreationEvent(infoMsg,
                    "", Event.Category.INFO));
        } catch (AppFactoryEventException e) {
            log.error("Failed to notify Initial provisioning of build system events", e);
            // do not throw again.
        }
    }

    /**
     * This method will call RestBasedJenkinsCIConnector to undeploy the artifacts of an application
     * @param application Application we need to delete
     * @param userName User who logged in when invoking the deletion of application
     * @param tenantDomain Tenant domain of application
     * @throws AppFactoryException
     */
    @Override
    public void onDeletion(Application application, String userName, String tenantDomain)
            throws AppFactoryException {
        Version[] versions = ProjectUtils.getVersions(application.getId(), tenantDomain);
        String deployerType = ApplicationTypeManager.getInstance().getApplicationTypeBean(application.getType())
                                                    .getProperty(AppFactoryConstants.DEPLOYER_TYPE).toString();
        JenkinsCISystemDriver jenkinsCISystemDriver = ServiceContainer.getJenkinsCISystemDriver();
        for (Version version : versions) {
            String lifecycleStage = version.getLifecycleStage();
            String jobName = ServiceHolder.getContinuousIntegrationSystemDriver()
                    .getJobName(application.getId(), version.getId(), null);
            jenkinsCISystemDriver.deleteJob(application.getId(), version.getId(), tenantDomain);
            log.info("Successfully deleted the jenkins job : " + jobName +
                     " of the application : " + application.getId() + " in the environment: " + lifecycleStage +
                     " from tenant domain : " + tenantDomain + " in jenkins");
            RestBasedJenkinsCIConnector.getInstance().undeployArtifact(
                    deployerType, jobName, application.getId(), application.getType(), version.getId(), lifecycleStage, tenantDomain);
            log.info("Successfully undeployed the artifact version : " + version.getId() +
                     " of the application : " + application.getId() + " in the environment: " + lifecycleStage +
                     " from tenant domain : " + tenantDomain + " from dep sync repo");

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUserAddition(Application application, UserInfo user, String tenantDomain)
            throws AppFactoryException {

        if (!AppFactoryCoreUtil.isBuildServerRequiredProject(application.getType())) {
            return;
        }

        log.info("User Addition event recieved for : " + application.getId() + " " +
                application.getName() + " User Name : " + user.getUserName());

        ServiceContainer.getJenkinsCISystemDriver()
                .addUsersToApplication(application.getId(),
                        new String[]{user.getUserName()}, tenantDomain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRevoke(Application application, String tenantDomain) throws AppFactoryException {
        // Improvement : remove the jobs from jenkins
        // Improvement : Remore roles (since appfactory uses role strategy
        // plugin) associated with the app
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onVersionCreation(Application application, Version source, Version target,
                                  String tenantDomain, String userName) throws AppFactoryException {

        if (!AppFactoryCoreUtil.isBuildServerRequiredProject(application.getType())) {
            return;
        }

        log.info("Version Creation event recieved for : " + application.getId() + " " +
                application.getName() + " Version : " + target.getId());

        RepositoryProvider repoProvider =
                Util.getRepositoryProvider(application.getRepositoryType());
        String repoURL = "";
        try {
            repoURL = repoProvider.getAppRepositoryURL(application.getId(), tenantDomain);
        } catch (RepositoryMgtException e) {
            log.error("Error occured whe creating repository URL," + e.getMessage());
            throw new AppFactoryException(e);
        }

        ServiceContainer.getJenkinsCISystemDriver()
                .createJob(application.getId(), target.getId(), "", tenantDomain, userName,
                        repoURL, AppFactoryConstants.ORIGINAL_REPOSITORY);

        ServiceContainer.getJenkinsCISystemDriver()
                .startBuild(application.getId(),
                        target.getId(),
                        true,
                        rxtManager.getStage(application.getId(), target.getId(),
                                tenantDomain), "", tenantDomain, userName,
                        AppFactoryConstants.ORIGINAL_REPOSITORY);

    }

    /**
     * onLifeCycleStateChange update the job configuration if needed
     *
     * @param application   application of which LC stage got changed
     * @param version       version of which the LC stage got changed
     * @param previousStage previous LC stage
     * @param nextStage     new LC stage
     * @throws AppFactoryException
     */

    @Override
    public void onLifeCycleStageChange(Application application, Version version,
                                       String previousStage, String nextStage, String tenantDomain)
            throws AppFactoryException {

        if (!AppFactoryCoreUtil.isBuildServerRequiredProject(application.getType())) {
            return;
        }

        String deploymentState = "";
        int pollingPeriod = 0;

        AppFactoryConfiguration configuration = ServiceContainer.getAppFactoryConfiguration();
        boolean previousDeploymentStage =
                Boolean.parseBoolean(configuration.getFirstProperty("ApplicationDeployment.DeploymentStage." +
                        previousStage +
                        ".AutomaticDeployment.Enabled"));
        boolean nextDeploymentStage =
                Boolean.parseBoolean(configuration.getFirstProperty("ApplicationDeployment.DeploymentStage." +
                        nextStage +
                        ".AutomaticDeployment.Enabled"));
        if (!previousDeploymentStage && nextDeploymentStage) {
            pollingPeriod =
                    Integer.parseInt(configuration.getFirstProperty("ApplicationDeployment.DeploymentStage." +
                            previousStage +
                            ".AutomaticDeployment.PollingPeriod"));
            deploymentState = "addAD";

        } else if (previousDeploymentStage && !nextDeploymentStage) {
            deploymentState = "removeAD";
        }

        ServiceContainer.getJenkinsCISystemDriver().editADJobConfiguration(application.getId(),
                version.getId(),
                deploymentState,
                pollingPeriod,
                tenantDomain);

    }

    @SuppressWarnings("UnusedDeclaration")
    public void onAutoDeploymentVersionChange(Application application, Version previousVersion,
                                              Version newVersion, String newStage,
                                              String tenantDomain) throws AppFactoryException {

        if (!AppFactoryCoreUtil.isBuildServerRequiredProject(application.getType())) {
            return;
        }

        log.info("AutoDeployment Version Change event recieved for : " + application.getId() + " " +
                application.getName() + " From Version : " + previousVersion.getId() +
                " To Version : " + newVersion.getId());
        int pollingPeriod = 0;

        // noinspection ConstantConditions
        if (previousVersion != null) {
            ServiceContainer.getJenkinsCISystemDriver()
                    .editADJobConfiguration(application.getId(), previousVersion.getId(),
                            "removeAD", pollingPeriod, tenantDomain);
        }

        // noinspection ConstantConditions
        if (newVersion != null) {
            AppFactoryConfiguration configuration = ServiceContainer.getAppFactoryConfiguration();
            pollingPeriod =
                    Integer.parseInt(configuration.getFirstProperty("ApplicationDeployment.DeploymentStage." +
                            newStage +
                            ".AutomaticDeployment.PollingPeriod"));
            ServiceContainer.getJenkinsCISystemDriver().editADJobConfiguration(application.getId(),
                    newVersion.getId(),
                    "addAD",
                    pollingPeriod,
                    tenantDomain);

        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUserDeletion(Application application, UserInfo user, String tenantDomain)
            throws AppFactoryException {
        ServiceContainer.getJenkinsCISystemDriver()
                .removeUsersFromApplication(application.getId(),
                        new String[]{user.getUserName()},
                        tenantDomain);
    }


    @Override
    public boolean hasExecuted(Application application, String userName, String tenantDomain)
            throws AppFactoryException {
        // TODO check whether the global role is assigned to app owner
        return true; // To change body of implemented methods use File |
        // Settings | File Templates.
    }

    @Override
    public void onUserUpdate(Application application, UserInfo user, String tenantDomain)
            throws AppFactoryException {
        // TODO update user roles in jenkins

    }

    @Override
    public void onFork(Application application, String userName, String tenantDomain,
                       String version, String[] forkedUsers) throws AppFactoryException {
        AppFactoryConfiguration configuration = ServiceContainer.getAppFactoryConfiguration();
        String perDeveloperBuild = configuration.getFirstProperty("EnablePerDeveloperBuild");
        if (!AppFactoryCoreUtil.isBuildServerRequiredProject(application.getType()) || (perDeveloperBuild == null || perDeveloperBuild.equals("false"))) {
            return;
        }
        // for (int i = 0; i < forkedUsers.length; i++) {
        for (String user : forkedUsers) {

            String forkedUser = MultitenantUtils.getTenantAwareUsername(user);
            JenkinsCISystemDriver jenkinsCISystemDriver =
                    ServiceContainer.getJenkinsCISystemDriver();

            RepositoryProvider repoProvider =
                    Util.getRepositoryProvider(application.getRepositoryType());
            String repoURL = "";
            try {
                repoURL =
                        repoProvider.getForkedAppRepositoryURL(application.getId(),
                                tenantDomain, forkedUser);
            } catch (RepositoryMgtException e) {
                log.error("Error occured whe creating repository URL," + e.getMessage());
                throw new AppFactoryException(e);
            }

            if (version == null || version.trim().equals("")) {
                Version[] versions = ProjectUtils.getVersions(application.getId(), tenantDomain);
                for (Version version2 : versions) {
                    jenkinsCISystemDriver.createJob(application.getId(), version2.getId(), "",
                            tenantDomain, forkedUser, repoURL,
                            AppFactoryConstants.FORK_REPOSITORY);
                }
            } else {
                jenkinsCISystemDriver.createJob(application.getId(), version, "", tenantDomain,
                        forkedUser, repoURL,
                        AppFactoryConstants.FORK_REPOSITORY);
            }

            jenkinsCISystemDriver.setupApplicationAccount(application.getId(), tenantDomain);
            // adding app creator to jenkins
            jenkinsCISystemDriver.addUsersToApplication(application.getId(),
                    new String[]{forkedUser}, tenantDomain);


            String perDeveloperAutoBuild = configuration.getFirstProperty("EnablePerDeveloperAutoBuild");
            if (perDeveloperAutoBuild != null && perDeveloperAutoBuild.equals("true")) {

                if (version == null || version.trim().equals("")) {
                    Version[] versions = ProjectUtils.getVersions(application.getId(), tenantDomain);
                    for (Version version2 : versions) {
                        String stage = rxtManager.getStage(application.getId(), versions[0].getId(), tenantDomain);
                        if (ArrayUtils.isNotEmpty(versions)) {
                            jenkinsCISystemDriver.startBuild(application.getId(), versions[0].getId(), true, stage, "", tenantDomain, userName, AppFactoryConstants.FORK_REPOSITORY);
                        }
                    }
                } else {
                    String stage = rxtManager.getStage(application.getId(), version, tenantDomain);
                    jenkinsCISystemDriver.startBuild(application.getId(), version, true, stage, "", tenantDomain, userName, AppFactoryConstants.FORK_REPOSITORY);
                }

            }
        }

    }
}
