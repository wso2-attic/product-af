package org.wso2.carbon.appfactory.jenkins.deploy;

import hudson.FilePath;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.deployers.AbstractStratosDeployer;
import org.wso2.carbon.appfactory.deployers.notify.DeployNotifier;
import org.wso2.carbon.appfactory.deployers.util.DeployerUtil;
import org.wso2.carbon.appfactory.jenkins.AppfactoryPluginManager;
import org.wso2.carbon.appfactory.jenkins.api.JenkinsBuildStatusProvider;
import org.wso2.carbon.appfactory.jenkins.artifact.storage.Utils;
import org.wso2.carbon.appfactory.jenkins.util.JenkinsUtility;

public class JenkinsArtifactDeployer extends AbstractStratosDeployer {
	private static final Log log = LogFactory.getLog(JenkinsArtifactDeployer.class);

	protected AppfactoryPluginManager.DescriptorImpl descriptor;

	public JenkinsArtifactDeployer() {
		descriptor = new AppfactoryPluginManager.DescriptorImpl();
		super.setAdminUserName(descriptor.getAdminUserName());
		super.setAdminPassword(descriptor.getAdminPassword());
		super.setAppfactoryServerURL(descriptor.getAppfactoryServerURL());
		super.setStoragePath(descriptor.getStoragePath());
		super.setTempPath(descriptor.getTempPath());
		super.buildStatusProvider = new JenkinsBuildStatusProvider();
		String tenantDomain = Utils.getEnvironmentVariable("TENANT_DOMAIN");
		String tenantID = Utils.getEnvironmentVariable("TENANT_ID");
		super.setTenantDomain(tenantDomain);
		super.setTenantID(Integer.parseInt(tenantID));
	}

	public void deployLatestSuccessArtifact(Map<String, String[]> parameters) throws AppFactoryException {

		String applicationId = DeployerUtil.getParameter(parameters, AppFactoryConstants.APPLICATION_ID);
		String stageName = DeployerUtil.getParameter(parameters, AppFactoryConstants.DEPLOY_STAGE);
		String deployAction = DeployerUtil.getParameter(parameters, AppFactoryConstants.DEPLOY_ACTION);
		String artifactType = DeployerUtil.getParameter(parameters, AppFactoryConstants.ARTIFACT_TYPE);
		String version = DeployerUtil.getParameter(parameters, AppFactoryConstants.APPLICATION_VERSION);

		String jobName = JenkinsUtility.getJobName(applicationId, version);

		String path =
		              getSuccessfulArtifactTempStoragePath(applicationId, version, artifactType, stageName,
		                                                   getTenantDomain());

		File lastSuccess = new File(path);

		if (!lastSuccess.exists()) {
			log.info("No builds have been triggered for " + jobName + ". Building " + jobName +
			         " first to deploy the latest built artifact");
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

			String buildUrl = DeployerUtil.generateTenantJenkinsUrl(jobName, tenantDomain, jenkinsUrl);
			triggerBuild(jobName, buildUrl, nameValuePairs.toArray(new NameValuePair[nameValuePairs.size()]));
			// since automatic build deploy the latest artifact of successful builds to the
			// server, return after triggering the build

		} else {
			log.info("Deplying Last Sucessful Artifact with job name - " + jobName + " stageName -" + stageName +
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

	public void labelLastSuccessAsPromoted(String applicationId, String version, String artifactType)
	                                                                                                 throws AppFactoryException,
	                                                                                                 IOException,
	                                                                                                 InterruptedException {
		log.info("---------------------------Entering Deploy Procedure --------------------------");
		String lastSucessBuildFilePath =
		                                 getSuccessfulArtifactTempStoragePath(applicationId, version, artifactType,
		                                                                      null, null);
		log.debug("Last success build path is :" + lastSucessBuildFilePath);

		String dest = getArtifactStoragePath(applicationId, version, artifactType, null, null);

		File destDir = new File(dest);
		if (destDir.exists()) {
			FileUtils.cleanDirectory(destDir.getParentFile());
		}
		if (!destDir.mkdirs()) {
			log.error("Unable to create promoted tag for application :" + applicationId + ", version :" + version);
			throw new AppFactoryException(
			                              "Error occured while creating dir for last successful as PROMOTED: application :" +
			                                      applicationId + ", version :" + version);
		}

		File[] lastSucessFiles = getLastBuildArtifact(lastSucessBuildFilePath, artifactType);
		for (File lastSucessFile : lastSucessFiles) {
			FilePath lastSuccessArtifactPath = new FilePath(lastSucessFile);
			String promotedDestDirPath =
			                             destDir.getAbsolutePath() +
			                                     File.separator +
			                                     getPromotedDestinationPathForApplication(lastSucessFile.getParent(),
			                                                                              artifactType);

			File promotedDestDir = new File(promotedDestDirPath);
			FilePath promotedDestDirFilePath = new FilePath(promotedDestDir);
			if (!promotedDestDirFilePath.exists()) {
				promotedDestDirFilePath.mkdirs();
			}

			File destFile =
			                new File(promotedDestDir.getAbsolutePath() + File.separator +
			                         lastSuccessArtifactPath.getName());

			FilePath destinationFile = new FilePath(destFile);
			if (lastSuccessArtifactPath.isDirectory()) {
				lastSuccessArtifactPath.copyRecursiveTo(destinationFile);
			} else {
				lastSuccessArtifactPath.copyTo(destinationFile);
			}
			log.info("labeled the lastSuccessful as PROMOTED");
		}
	}

	public void labelAsPromotedArtifact(String jobName, String tagName) {

		try {

			String path = descriptor.getStoragePath() + File.separator + jobName + File.separator + tagName;
			FilePath tagPath = new FilePath(new File(path));

			String jobPromotedPath =
			                         descriptor.getStoragePath() + File.separator + "PROMOTED" + File.separator +
			                                 jobName;
			String dest = jobPromotedPath + File.separator + tagName;

			File toBeCleaned = new File(jobPromotedPath);

			if (toBeCleaned.exists()) {
				// since only one artifact can be promoted for a version
				FileUtils.cleanDirectory(toBeCleaned);
			}

			File destDir = new File(dest);
			if (!destDir.mkdirs()) {
				log.error("Unable to create promoted tag for job:" + jobName + "tag:" + tagName);
			}
			// given tag is copied to <jenkins-home>/storage/PROMOTED/<job-name>/<tag-name>/
			tagPath.copyRecursiveTo(new FilePath(destDir));
			log.info("labeled the tag: " + tagName + " as PROMOTED");

		} catch (Exception e) {
			log.error("Error while labeling the tag: " + tagName + "as PROMOTED", e);
		}

	}

	@Override
	public String getSuccessfulArtifactTempStoragePath(String applicationId, String applicationVersion,
	                                                   String artifactType, String stage, String tenantDomain)
	                                                                                                          throws AppFactoryException {
		String path = null;
		try {
			String jenkinsHome = DeployerUtil.getJenkinsHome();
			String jobName = JenkinsUtility.getJobName(applicationId, applicationVersion);
			path = jenkinsHome + File.separator + "jobs" + File.separator + jobName + File.separator + "lastSuccessful";

		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return path;
	}

	@Override
	public String getArtifactStoragePath(String applicationId, String applicationVersion, String artifactType,
	                                     String stage, String tenantDomain) throws AppFactoryException {

		String jobName = JenkinsUtility.getJobName(applicationId, applicationVersion);
		String path =
		              getStoragePath() + File.separator + "PROMOTED" + File.separator + jobName + File.separator +
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

	@Override
	protected String getBaseRepoUrl(String stage, String appType) throws AppFactoryException {
		return Utils.getRepositoryProviderProperty(stage, "BaseURL", appType);
	}

	@Override
	protected String getBaseRepoUrlPattern(String stage, String appType) throws AppFactoryException {
		return Utils.getRepositoryProviderProperty(stage, "URLPattern", appType);
	}

	@Override
	protected String getAdminPassword(String stage, String appType) throws AppFactoryException {
		return Utils.getRepositoryProviderProperty(stage, "AdminUserName", appType);
	}

	@Override
	protected String getAdminUserName(String stage, String appType) throws AppFactoryException {
		return Utils.getRepositoryProviderProperty(stage, "AdminPassword", appType);
	}

	@Override
	protected String getServerDeploymentPaths(String appType) throws AppFactoryException {
		return Utils.getAppFactoryConfigurationProperty("ApplicationType." + appType +
		                                                ".Property.ServerDeploymentPaths");
	}

	public void deployTaggedArtifact(Map<String, String[]> requestParameters) throws Exception {
		

	}

}
