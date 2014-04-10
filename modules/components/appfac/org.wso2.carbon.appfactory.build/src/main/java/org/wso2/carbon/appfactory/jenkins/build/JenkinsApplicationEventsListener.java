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

package org.wso2.carbon.appfactory.jenkins.build;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.ApplicationEventsHandler;
import org.wso2.carbon.appfactory.core.deploy.ApplicationDeployer;
import org.wso2.carbon.appfactory.core.dto.Application;
import org.wso2.carbon.appfactory.core.dto.UserInfo;
import org.wso2.carbon.appfactory.core.dto.Version;
import org.wso2.carbon.appfactory.core.governance.RxtManager;
import org.wso2.carbon.appfactory.core.util.AppFactoryCoreUtil;
import org.wso2.carbon.appfactory.jenkins.build.internal.ServiceContainer;
import org.wso2.carbon.appfactory.utilities.project.ProjectUtils;

/**
 * Listens to Application events (such as creation, user addition etc) and makes
 * relevant changes on Jenkins CI server.
 */
public class JenkinsApplicationEventsListener extends ApplicationEventsHandler {

    private static Log log = LogFactory.getLog(JenkinsApplicationEventsListener.class);
    private RxtManager rxtManager;
    private int priority;

    /**
     * Creates a listener instance with given priority.
     *
     * @param priority The Priority
     */
    public JenkinsApplicationEventsListener(int priority) {

        this.identifier = AppFactoryConstants.JENKINS;
        this.priority = priority;
        this.rxtManager = new RxtManager();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreation(Application application, String userName, String tenantDomain) throws AppFactoryException {
    	
    	if (!AppFactoryUtil.isBuildable(application.getType())) {
			return;
		}
    	
        log.info("Application Creation event recieved for : " + application.getId() + " " +
                application.getName());
        
        JenkinsCISystemDriver jenkinsCISystemDriver = ServiceContainer.getJenkinsCISystemDriver();
        jenkinsCISystemDriver.createJob(application.getId(), "trunk", "", tenantDomain);
        
        jenkinsCISystemDriver.setupApplicationAccount(application.getId(), tenantDomain);
        //adding app creator to jenkins
        jenkinsCISystemDriver.addUsersToApplication(application.getId(), new String[]{userName.split("@")[0]}, tenantDomain);
        Version[] versions = ProjectUtils.getVersions(application.getId(),tenantDomain);
        String stage = rxtManager.getStage(application.getId(), versions[0].getId(), tenantDomain);
        if (ArrayUtils.isNotEmpty(versions)) {
            //No need to create job.
            jenkinsCISystemDriver.startBuild(application.getId(), versions[0].getId(), true, stage, "", tenantDomain, userName);
        }
    }

    @Override
    public void onDeletion(Application application, String userName, String tenantDomain) throws AppFactoryException {
        // deleting the artifacts deployed
    	if (!AppFactoryUtil.isBuildable(application.getType())) {
			return;
		}

    	JenkinsCISystemDriver jenkinsCISystemDriver = ServiceContainer.getJenkinsCISystemDriver();
        jenkinsCISystemDriver.deleteJob(application.getId(), "trunk", tenantDomain);
        ApplicationDeployer applicationDeployer = new ApplicationDeployer();
        applicationDeployer.undeployAllArtifactsOfAppFromDepSyncGitRepo(application.getId(), application.getType(), ProjectUtils.getVersions(application.getId(), tenantDomain));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUserAddition(Application application, UserInfo user, String tenantDomain) throws AppFactoryException {

    	if (!AppFactoryUtil.isBuildable(application.getType())) {
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
    public void onVersionCreation(Application application, Version source, Version target,String tenantDomain, String userName)
            throws AppFactoryException {

    	boolean appIsBuildable = AppFactoryUtil.isBuildable(application.getType());
		if (!appIsBuildable) {
			return;
		}
		
        log.info("Version Creation event recieved for : " + application.getId() + " " +
                application.getName() + " Version : " + target.getId());

        ServiceContainer.getJenkinsCISystemDriver().createJob(application.getId(), target.getId(),
                "", tenantDomain);
        String jobName = ServiceContainer.getJenkinsCISystemDriver().getJobName(application.getId(), target.getId(), "");
        log.info("Job created successfully in jenkins. Job name - " + jobName);
        ServiceContainer.getJenkinsCISystemDriver().startBuild(application.getId(), target.getId(), true,
                rxtManager.getStage(application.getId(), target.getId(), tenantDomain), "", tenantDomain, userName);
        log.info("Started the build for the newly created version. jobname - " + jobName);

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

    public void onLifeCycleStageChange(Application application, Version version,
                                       String previousStage, String nextStage, String tenantDomain) throws
            AppFactoryException {
    	
    	boolean appIsBuildable = AppFactoryUtil.isBuildable(application.getType());
		if (!appIsBuildable) {
			return;
		}

        String deploymentState = "";
        int pollingPeriod = 0;

        AppFactoryConfiguration configuration = ServiceContainer.getAppFactoryConfiguration();
        boolean previousDeploymentStage = Boolean.parseBoolean(configuration.getFirstProperty(
                "ApplicationDeployment.DeploymentStage." + previousStage +
                        ".AutomaticDeployment.Enabled"));
        boolean nextDeploymentStage = Boolean.parseBoolean(configuration.getFirstProperty(
                "ApplicationDeployment.DeploymentStage." + nextStage +
                        ".AutomaticDeployment.Enabled"));
        if (!previousDeploymentStage && nextDeploymentStage) {
            pollingPeriod = Integer.parseInt(configuration.getFirstProperty(
                    "ApplicationDeployment.DeploymentStage."
                            + previousStage + ".AutomaticDeployment.PollingPeriod"));
            deploymentState = "addAD";

        } else if (previousDeploymentStage && !nextDeploymentStage) {
            deploymentState = "removeAD";
        }

        ServiceContainer.getJenkinsCISystemDriver().editADJobConfiguration(
                application.getId(), version.getId(), deploymentState, pollingPeriod, tenantDomain);

    }

    @SuppressWarnings("UnusedDeclaration")
    public void onAutoDeploymentVersionChange(Application application, Version previousVersion,
                                              Version newVersion, String newStage, String tenantDomain)
            throws AppFactoryException {

    	if (!AppFactoryUtil.isBuildable(application.getType())) {
			return;
		}
    	
        log.info("AutoDeployment Version Change event recieved for : " + application.getId() + " " +
                application.getName() + " From Version : " + previousVersion.getId() +
                " To Version : " + newVersion.getId());
        int pollingPeriod = 0;

        //noinspection ConstantConditions
        if (previousVersion != null) {
            ServiceContainer.getJenkinsCISystemDriver().editADJobConfiguration(
                    application.getId(), previousVersion.getId(), "removeAD", pollingPeriod, tenantDomain);
        }

        //noinspection ConstantConditions
        if (newVersion != null) {
            AppFactoryConfiguration configuration = ServiceContainer.getAppFactoryConfiguration();
            pollingPeriod = Integer.parseInt(configuration.getFirstProperty(
                    "ApplicationDeployment.DeploymentStage." + newStage +
                            ".AutomaticDeployment.PollingPeriod"));
            ServiceContainer.getJenkinsCISystemDriver().editADJobConfiguration(
                    application.getId(), newVersion.getId(), "addAD", pollingPeriod, tenantDomain);


        }


    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUserDeletion(Application application, UserInfo user, String tenantDomain) throws AppFactoryException {
        ServiceContainer.getJenkinsCISystemDriver()
                .removeUsersFromApplication(application.getId(),
                        new String[]{user.getUserName()}, tenantDomain);
    }

    /**
     * {@inheritDoc}.
     */
    public int getPriority() {
        return priority;
    }

    @Override
    public boolean hasExecuted(Application application, String userName, String tenantDomain) throws AppFactoryException {
        // TODO check whether the global role is assigned to app owner
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onUserUpdate(Application application, UserInfo user, String tenantDomain) throws AppFactoryException {
        // TODO update user roles in jenkins

    }

	@Override
	public void onForking(Application application, String version,
			String userName, String tenantDomain) throws AppFactoryException {
		// TODO Auto-generated method stub
		
	}
}
