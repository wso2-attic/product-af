/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *    WSO2 Inc. licenses this file to you under the Apache License,
 *    Version 2.0 (the "License"); you may not use this file except
 *    in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package org.wso2.carbon.appfactory.jenkins.deploy;

import hudson.EnvVars;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.deployers.AbstractStratosDeployer;
import org.wso2.carbon.appfactory.deployers.notify.DeployNotifier;
import org.wso2.carbon.appfactory.deployers.util.DeployerUtil;
import org.wso2.carbon.appfactory.jenkins.AppfactoryPluginManager;
import org.wso2.carbon.appfactory.jenkins.api.JenkinsBuildStatusProvider;
import org.wso2.carbon.appfactory.jenkins.util.JenkinsUtility;

import java.io.File;
import java.util.Map;

public class JenkinsArtifactDeployer extends AbstractStratosDeployer {
	private static final Log log = LogFactory.getLog(JenkinsArtifactDeployer.class);

	private AppfactoryPluginManager.DescriptorImpl descriptor = new AppfactoryPluginManager.DescriptorImpl();
	private String baseDeployUrl;
	private String s2AdminUsername;
	private String s2AdminPassword;

	public JenkinsArtifactDeployer() {
		super.setAdminUserName(descriptor.getAdminUserName());
		super.setAdminPassword(descriptor.getAdminPassword());
		super.setAppfactoryServerURL(descriptor.getAppfactoryServerURL());
		setBaseDeployUrl(descriptor.getBaseDeployUrl());
		setS2AdminPassword(descriptor.getStratosAdminPassword());
		setS2AdminUsername(descriptor.getStratosAdminUsername());
		super.buildStatusProvider = new JenkinsBuildStatusProvider();
	}

	public void deployLatestSuccessArtifact(Map<String, String[]> parameters) throws AppFactoryException {

		String applicationId = DeployerUtil.getParameter(parameters, AppFactoryConstants.APPLICATION_ID);
		String stageName = DeployerUtil.getParameter(parameters, AppFactoryConstants.DEPLOY_STAGE);
		String deployAction = DeployerUtil.getParameter(parameters, AppFactoryConstants.DEPLOY_ACTION);
		String artifactType = DeployerUtil.getParameter(parameters, AppFactoryConstants.ARTIFACT_TYPE);
		String version = DeployerUtil.getParameter(parameters, AppFactoryConstants.APPLICATION_VERSION);
		String serverDeploymentPath = DeployerUtil.getParameter(parameters, AppFactoryConstants.SERVER_DEPLOYMENT_PATHS);
		log.info("Server deployment path is : " + serverDeploymentPath);

		String jobName = DeployerUtil.getParameter(parameters, AppFactoryConstants.JOB_NAME);
		String tenantDomain = DeployerUtil.getParameter(parameters,AppFactoryConstants.TENANT_DOMAIN);
		String repositoryFrom = DeployerUtil.getParameter(parameters, AppFactoryConstants.REPOSITORY_FROM);
		String path = getSuccessfulArtifactTempStoragePath(applicationId, version, artifactType, stageName,
		                                                   tenantDomain,jobName);

		File lastSuccess = new File(path);
		if (!lastSuccess.exists()) {
			// TODO Move to AF side
//            try {
//                //used for eventing
//                String tenantDomain = getTenantDomain();
//                String correlationKey = applicationId + stageName + version + tenantDomain;
//
//                EventNotifier.getInstance().notify(ContinousIntegrationEventBuilderUtil.autoDeployStatusChangeEvent(applicationId, tenantDomain, "Application deployment couldn't be done, please try again.", "", correlationKey));
//
//            }catch (AppFactoryEventException e) {
//                log.error("Failed to notify deployment of latest successful artifact " + e.getMessage(), e);
//            }

            //We decided to commit because if the this symlink not generated doesn't means there is no build always. There may be build but not create symlink yet.
            //So user must know that and redeploy it.

            /*
            if(log.isDebugEnabled()) {
				log.debug("No builds have been triggered for " + jobName + ". Building " + jobName +
				         " first to deploy the latest built artifact");
			}

			String jenkinsUrl = parameters.get("rootPath")[0];
			String tenantUserName = DeployerUtil.getParameter(parameters, "tenantUserName");
			String tenantDomain = getTenantDomain();
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new NameValuePair("isAutomatic", "false"));
			nameValuePairs.add(new NameValuePair("doDeploy", Boolean.toString(true)));
			nameValuePairs.add(new NameValuePair("deployAction", deployAction));
			nameValuePairs.add(new NameValuePair("deployStage", stageName));
			nameValuePairs.add(new NameValuePair("persistArtifact", String.valueOf(false)));
			nameValuePairs.add(new NameValuePair("tenantUserName", tenantUserName));
			nameValuePairs.add(new NameValuePair(AppFactoryConstants.SERVER_DEPLOYMENT_PATHS, serverDeploymentPath));

			String buildUrl = DeployerUtil.generateTenantJenkinsUrl(jobName, tenantDomain, jenkinsUrl);
			triggerBuild(jobName, buildUrl, nameValuePairs.toArray(new NameValuePair[nameValuePairs.size()]));
			*/
			// since automatic build deploy the latest artifact of successful builds to the
			// server, return after triggering the build

		} else {
			log.info("Deploying Last Successful Artifact with job name - " + jobName + " stageName -" + stageName +
			         " deployAction -" + deployAction);

            try {
                //used for eventing
	            // TODO Move to AF side
	            if (!AppFactoryConstants.FORK_REPOSITORY.equals(repositoryFrom)) {
		            String correlationKey = applicationId + stageName + version + tenantDomain;
//		            EventNotifier.getInstance().notify(
//				            ContinousIntegrationEventBuilderUtil
//						            .buildApplicationDeployementStartedEvent(applicationId, tenantDomain,
//						                "Application deployment started for " + version + " of " + repositoryFrom +
//                                                " repo" , null, correlationKey));
	            }
                    super.deployLatestSuccessArtifact(parameters);
            } catch (AppFactoryException e) {
                String msg = "deployment of latest success artifact failed for applicaion " + jobName;
                handleException(msg, e);
            }

		}

	}

	@Override
	public String getSuccessfulArtifactTempStoragePath(String applicationId, String applicationVersion,
	                                                   String artifactType, String stage, String tenantDomain,
	                                                   String jobName) throws AppFactoryException {
		String jenkinsHome = EnvVars.masterEnvVars.get("JENKINS_HOME");
		String path = jenkinsHome + File.separator + "jobs"+ File.separator +tenantDomain+ File.separator + "jobs" +
		              File.separator + jobName + File.separator + "lastSuccessful";
		return path;
	}

	@Override
	public String getArtifactStoragePath(String applicationId, String applicationVersion, String artifactType,
	                                     String stage, String tenantDomain) throws AppFactoryException {
		String jobName = JenkinsUtility.getJobName(applicationId, applicationVersion);
		String path =
		              getStoragePath(tenantDomain) + File.separator + "PROMOTED" + File.separator + jobName + File.separator +
		                      "lastSuccessful";
		return path;
	}

	@Override
	public void postDeploymentNoifier(String message, String applicationId, String applicationVersion,
	                                  String artifactType, String stage, String tenantDomain) {
		try {
			log.info("Application Deployed Successfully. Application ID :" + applicationId + " and version : " +
			         applicationVersion);

			DeployNotifier notifier = new DeployNotifier();
			notifier.deployed(applicationId, applicationVersion, stage, adminUserName, adminPassword,
			                  appfactoryServerURL, buildStatusProvider, tenantDomain);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void setS2AdminUsername(String s2AdminUsername) {
		this.s2AdminUsername = s2AdminUsername;
	}

	public void setBaseDeployUrl(String baseDeployUrl) {
		this.baseDeployUrl = baseDeployUrl;
	}

	public void setS2AdminPassword(String s2AdminPassword) {
		this.s2AdminPassword = s2AdminPassword;
	}

	@Override
	protected String getBaseRepoUrl() throws AppFactoryException {
		return this.baseDeployUrl;
	}

	@Override
	protected String getAdminPassword() throws AppFactoryException {
		return this.s2AdminUsername;
	}

	@Override
	protected String getAdminUserName() throws AppFactoryException {
		return this.s2AdminPassword;
	}

	@Override
	public String getStoragePath(String tenantDomain) {
		return descriptor.getStoragePath(tenantDomain);
	}

	@Override
	public String getTempPath(String tenantDomain) {
		return descriptor.getTempPath(tenantDomain);
	}

	public void deployTaggedArtifact(Map<String, String[]> requestParameters) throws Exception {


	}

}
