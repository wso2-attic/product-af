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
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.deployers.AbstractStratosDeployer;
import org.wso2.carbon.appfactory.deployers.notify.DeployNotifier;
import org.wso2.carbon.appfactory.deployers.util.DeployerUtil;
import org.wso2.carbon.appfactory.jenkins.AppfactoryPluginManager;
import org.wso2.carbon.appfactory.jenkins.Constants;
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
	private String stratosServerURL;

	public JenkinsArtifactDeployer() {
		super.setAdminUserName(descriptor.getAdminUserName());
		super.setAdminPassword(descriptor.getAdminPassword());
		super.setAppfactoryServerURL(descriptor.getAppfactoryServerURL());
		setBaseDeployUrl(descriptor.getBaseDeployUrl());
		setS2AdminPassword(descriptor.getStratosAdminPassword());
		setS2AdminUsername(descriptor.getStratosAdminUsername());
		setStratosServerURL(descriptor.getStratosServerURL());
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
		String path = getSuccessfulArtifactTempStoragePath(applicationId, version, artifactType, stageName,
		                                                   tenantDomain,jobName);

		File lastSuccess = new File(path);
		if (!lastSuccess.exists()) {
			//TODO Notify to the AF side
			log.error("Failed to notify deployment of latest successful artifact since there is no latest successful artifact");

		} else {
			log.info("Deploying Last Successful Artifact with job name - " + jobName + " stageName -" + stageName +
			         " deployAction -" + deployAction);
            try {
                 super.deployLatestSuccessArtifact(parameters);
            } catch (AppFactoryException e) {
                String msg = "deployment of latest success artifact failed for applicaion " + jobName;
                handleException(msg, e);
            }
        }

	}

	/**
	 * This method is used to build the specified job
	 * build parameters are set in such a way that it does not execute any post build actions
	 *
	 * @param jobName
	 *            job that we need to build
	 * @param buildUrl
	 *            url used to trigger the build
	 * @throws AppFactoryException
	 */
	protected void triggerBuild(String jobName, String buildUrl, NameValuePair[] queryParameters)
	                                                                                             throws AppFactoryException {
		PostMethod buildMethod = new PostMethod(buildUrl);
		buildMethod.setDoAuthentication(true);
		if (queryParameters != null) {
			buildMethod.setQueryString(queryParameters);
		}
		HttpClient httpClient = new HttpClient();
		httpClient.getState().setCredentials(AuthScope.ANY,
		                                     new UsernamePasswordCredentials(getAdminUsername(),
		                                                                     getServerAdminPassword()));
		httpClient.getParams().setAuthenticationPreemptive(true);
		int httpStatusCode = -1;
		try {
			httpStatusCode = httpClient.executeMethod(buildMethod);

		} catch (Exception ex) {
			String errorMsg = String.format("Unable to start the build on job : %s", jobName);
			log.error(errorMsg);
			throw new AppFactoryException(errorMsg, ex);
		} finally {
			buildMethod.releaseConnection();
		}

		if (HttpStatus.SC_FORBIDDEN == httpStatusCode) {
			final String errorMsg =
			                        "Unable to start a build for job [".concat(jobName)
			                                                           .concat("] due to invalid credentials.")
			                                                           .concat("Jenkins returned, http status : [")
			                                                           .concat(String.valueOf(httpStatusCode))
			                                                           .concat("]");
			log.error(errorMsg);
			throw new AppFactoryException(errorMsg);
		}

		if (HttpStatus.SC_NOT_FOUND == httpStatusCode) {
			final String errorMsg =
			                        "Unable to find the job [" + jobName + "Jenkins returned, " + "http status : [" +
			                                httpStatusCode + "]";
			log.error(errorMsg);
			throw new AppFactoryException(errorMsg);
		}
	}

	@Override
	public String getSuccessfulArtifactTempStoragePath(String applicationId, String applicationVersion,
	                                                   String artifactType, String stage, String tenantDomain,
	                                                   String jobName) throws AppFactoryException {
		String jenkinsHome = EnvVars.masterEnvVars.get(Constants.JENKINS_HOME);
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

	public void setStratosServerURL(String stratosServerURL) {
		this.stratosServerURL = stratosServerURL;
	}

	@Override
	protected String getBaseRepoUrl() throws AppFactoryException {
		return this.baseDeployUrl;
	}

	@Override
	protected String getAdminPassword() throws AppFactoryException {
		return this.s2AdminPassword;
	}

	@Override
	protected String getAdminUserName() throws AppFactoryException {
		return this.s2AdminUsername;
	}

	@Override
	protected String getStratosServerURL() throws AppFactoryException {
		return this.stratosServerURL;
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
