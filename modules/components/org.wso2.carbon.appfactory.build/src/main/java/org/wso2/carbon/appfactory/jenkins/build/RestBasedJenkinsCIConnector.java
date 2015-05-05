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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.beans.RuntimeBean;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeBean;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeManager;
import org.wso2.carbon.appfactory.core.build.DefaultBuildDriverListener;
import org.wso2.carbon.appfactory.core.dto.Statistic;
import org.wso2.carbon.appfactory.core.dao.ApplicationDAO;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.appfactory.core.runtime.RuntimeManager;
import org.wso2.carbon.appfactory.core.util.AppFactoryCoreUtil;
import org.wso2.carbon.appfactory.eventing.AppFactoryEventException;
import org.wso2.carbon.appfactory.eventing.Event;
import org.wso2.carbon.appfactory.eventing.EventNotifier;
import org.wso2.carbon.appfactory.eventing.builder.utils.ContinousIntegrationEventBuilderUtil;
import org.wso2.carbon.appfactory.eventing.utils.EventingConstants;
import org.wso2.carbon.appfactory.jenkins.build.internal.ServiceContainer;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryMgtException;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryProvider;
import org.wso2.carbon.appfactory.repository.mgt.internal.Util;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Connects to a jenkins server using its 'Remote API'.
 */
public class RestBasedJenkinsCIConnector {

	private static final Log log = LogFactory.getLog(RestBasedJenkinsCIConnector.class);
	private static final String UNDEPLOY_ARTIFACT_URL = "/plugin/appfactory-plugin/undeployArtifact";
	private static RestBasedJenkinsCIConnector restBasedJenkinsCIConnector;

	static {
		try {
			restBasedJenkinsCIConnector = new RestBasedJenkinsCIConnector();
		} catch (AppFactoryException e) {
			String msg = "Error occurred while instantiating the RestBasedJenkinsCIConnector";
			log.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	/**
	 * The http client used to connect jenkins.
	 */
	private HttpClient httpClient;

	/**
	 * Base url of the jenkins
	 */
	private String jenkinsUrl;

	/**
	 * Flag weather this connector needs to authenticate it self.
	 */
	private boolean authenticate;

	/**
	 * User who authenticate with jenkins rest api
	 */
	private String username;
	/**
	 * API key or password to authenticate user
	 */
	private String apiKeyOrPassword;

	/**
	 * Returns an instance of RestBasedJenkinsCIConnector
	 * @return instance of RestBasedJenkinsCIConnector
	 * @throws AppFactoryException when reading from appfactory.xml
	 */
	public static RestBasedJenkinsCIConnector getInstance() throws AppFactoryException {
		return restBasedJenkinsCIConnector;
	}

    /**
	 * Private constructor for singalton class
	 * @throws AppFactoryException when reading from appfactory.xml
	 */
	private RestBasedJenkinsCIConnector() throws AppFactoryException {

		this.authenticate = Boolean.parseBoolean(AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(
				JenkinsCIConstants.AUTHENTICATE_CONFIG_SELECTOR));

		this.username = AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(
				JenkinsCIConstants.JENKINS_SERVER_ADMIN_USERNAME);

		this.apiKeyOrPassword = AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(
				JenkinsCIConstants.JENKINS_SERVER_ADMIN_PASSWORD);

		this.jenkinsUrl = AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(
				JenkinsCIConstants.BASE_URL_CONFIG_SELECTOR);

		if (log.isDebugEnabled()) {
			log.debug(String.format("Authenticate : %s", this.authenticate));
			log.debug(String.format("Jenkins user name : %s", this.username));
			log.debug(String.format("Jenkins url : %s", this.jenkinsUrl));
		}

		this.httpClient = new HttpClient(
				new MultiThreadedHttpConnectionManager());
		if (StringUtils.isBlank(this.jenkinsUrl)) {
			throw new IllegalArgumentException(
					"Jenkins server url is unspecified");
		}
	}

	/**
	 * Get Authenticated Http Client
	 *
	 * @return Http Client
	 */
	public HttpClient getAuthenticatedHttpClient() {
		// authentication credentials are set for each request because there were instances where 401 returned from Jenkins
		// this is to avoid any overrides of params and state in http client.
		if (this.authenticate) {
			httpClient.getState()
					.setCredentials(
							AuthScope.ANY,
							new UsernamePasswordCredentials(this.username,
							                                this.apiKeyOrPassword));
			httpClient.getParams().setAuthenticationPreemptive(true);
		}
		return httpClient;
	}

    public HttpClient getNewAuthenticatedHttpClient() {
       HttpClient httpClientObj = new HttpClient();
        if (this.authenticate) {
            httpClientObj.getState()
                    .setCredentials(
                            AuthScope.ANY,
                            new UsernamePasswordCredentials(this.username,
                                    this.apiKeyOrPassword));
            httpClientObj.getParams().setAuthenticationPreemptive(true);
        }
        return httpClientObj;
    }

	/**
	 * @return
	 */
	public String getJenkinsUrl() {
		return jenkinsUrl;
	}

	/**
	 * @param jenkinsUrl
	 */
	@SuppressWarnings("UnusedDeclaration")
	public void setJenkinsUrl(String jenkinsUrl) {
		this.jenkinsUrl = jenkinsUrl;
	}

	/**
	 * Creates a project/job role in jenkins server
	 * <p>
	 * <b>NOTE: this method assumes a modified version (by WSO2) of
	 * 'role-strategy' plugin is installed in jenkins server</b>
	 * </p>
	 * 
	 * @param roleName
	 *            Name of the role.
	 * @param pattern
	 *            a regular expression to match jobs (e.g. app1.*)
	 * @param permissions
	 * @throws AppFactoryException
	 *             if an error occurs
	 */
	public void createRole(String roleName, String pattern,
			String permissions[], String tenantDomain)
			throws AppFactoryException {
		String createRoleUrl = "/descriptorByName/com.michelin.cio.hudson.plugins.rolestrategy"
				+ ".RoleBasedAuthorizationStrategy/createProjectRoleSubmit";
		ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
		parameters.add(new NameValuePair(AppFactoryConstants.ROLE_NAME, roleName));
		parameters.add(new NameValuePair(AppFactoryConstants.ROLE_PATTERN, pattern));
		for (String permission : permissions) {
			parameters.add(new NameValuePair(AppFactoryConstants.ROLE_PERMISSION, permission));
		}

		PostMethod addRoleMethod = createPost(createRoleUrl,
				parameters.toArray(new NameValuePair[0]), null, tenantDomain);

		try {
			int httpStatusCode = getAuthenticatedHttpClient().executeMethod(addRoleMethod);

			if (!isSuccessfulStatusCode(httpStatusCode)) {
				httpStatusCode = resendRequest(addRoleMethod);
			}

			if (!isSuccessfulStatusCode(httpStatusCode)) {
				String errorMsg = String.format(
						"Unable to create the role. jenkins returned, "
								+ "http status : %d", httpStatusCode);
				log.error(errorMsg);
				throw new AppFactoryException(errorMsg);
			}
		} catch (Exception ex) {
			String errorMsg = String.format(
					"Unable to create role in jenkins : %s", ex.getMessage());
			log.error(errorMsg, ex);
			throw new AppFactoryException(errorMsg, ex);
		} finally {
			addRoleMethod.releaseConnection();
		}

	}

	/**
	 * Assigns a set of global and/or project roles(s) to a specified user(s)
	 * <p>
	 * <b>NOTE: this method assumes a modified version (by WSO2) of
	 * 'role-strategy' plugin is installed in jenkins server</b>
	 * </p>
	 * 
	 * @param userIds
	 *            list of user Ids
	 * @param projectRoleNames
	 *            list of project roles
	 * @param globalRoleNames
	 *            list of global roles
	 * @throws AppFactoryException
	 *             if an error occurs
	 */
	public void assignUsers(String[] userIds, String[] projectRoleNames,
			String[] globalRoleNames, String tenantDomain)
			throws AppFactoryException {

		String assignURL = JenkinsCIConstants.RoleStrategy.ASSIGN_ROLE_SERVICE;

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		for (String id : userIds) {
			params.add(new NameValuePair(AppFactoryConstants.ASSIGN_USER_ID, id));
		}

		if (projectRoleNames != null) {

			for (String role : projectRoleNames) {
				params.add(new NameValuePair(AppFactoryConstants.PROJECT_ROLE, role));
			}

		}

		if (globalRoleNames != null) {
			for (String role : globalRoleNames) {
				params.add(new NameValuePair(AppFactoryConstants.GLOBAL_ROLE, role));
			}
		}

		PostMethod assignRolesMethod = createPost(assignURL,
				params.toArray(new NameValuePair[params.size()]), null,
				tenantDomain);

		try {
			int httpStatusCode = getAuthenticatedHttpClient().executeMethod(
					assignRolesMethod);

			if (!isSuccessfulStatusCode(httpStatusCode)) {
				httpStatusCode = resendRequest(assignRolesMethod);
			}
			if (!isSuccessfulStatusCode(httpStatusCode)) {
				String errorMsg = String.format(
						"Unable to assign roles to given sides. jenkins "
								+ "returned, http status : %d", httpStatusCode);
				log.error(errorMsg);
				throw new AppFactoryException(errorMsg);
			}
		} catch (Exception ex) {
			String errorMsg = String.format(
					"Unable to assign roles in jenkins : %s", ex.getMessage());
			log.error(errorMsg, ex);
			throw new AppFactoryException(errorMsg, ex);
		} finally {
			assignRolesMethod.releaseConnection();
		}

	}

	/**
	 * Assign set of users to the application given.
	 * <p>
	 * <b>NOTE: this method assumes a modified version (by WSO2) of
	 * 'role-strategy' plugin is installed in jenkins server</b>
	 * </p>
	 * 
	 * @param appicationKey
	 *            Application ID
	 * @param users
	 *            Users List
	 * @throws AppFactoryException
	 */
	public void unAssignUsers(String appicationKey, String[] users,
			String tenantDomain) throws AppFactoryException {
		if (appicationKey == null) {
			throw new NullPointerException("Application cannot be null.");
		}

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		for (String user : users) {
			params.add(new NameValuePair(AppFactoryConstants.ASSIGN_USER_ID, user));
		}

		params.add(new NameValuePair(AppFactoryConstants.PROJECT_ROLE, appicationKey));

		PostMethod assignRolesMethod = createPost(
				JenkinsCIConstants.RoleStrategy.UNASSIGN_ROLE_SERVICE,
				params.toArray(new NameValuePair[params.size()]), null,
				tenantDomain);
		try {
			int httpStatusCode = getAuthenticatedHttpClient().executeMethod(
					assignRolesMethod);

			if (!isSuccessfulStatusCode(httpStatusCode)) {
				httpStatusCode = resendRequest(assignRolesMethod);
			}

			if (!isSuccessfulStatusCode(httpStatusCode)) {
				String errorMsg = String
						.format("Unable to un-assign roles to given application. jenkins returned, http status : %d",
						        httpStatusCode);
				log.error(errorMsg);
				throw new AppFactoryException(errorMsg);
			}
		} catch (Exception e) {
			String errorMsg = String.format(
					"Error while un-assining user roles from application:%s", appicationKey);
			log.error(errorMsg, e);
			throw new AppFactoryException(errorMsg, e);
		} finally {
			if (assignRolesMethod != null) {
				assignRolesMethod.releaseConnection();
			}
		}
	}

	/**
	 * Returns all the jobs defined in jenkins server.
	 * 
	 * @return list of job names
	 * @throws AppFactoryException
	 *             if an error occurs
	 */
	public List<String> getAllJobs(String tenantDomain)
			throws AppFactoryException {
		return getJobNames(null, tenantDomain);
	}

	/**
	 * Returns a list of job names which contains given text as a substring. If
	 * the filter text is not specified (i.e. null) then all the jobs will be
	 * returned.
	 * 
	 * @param filterText
	 *            (specifying null return names of all jobs available) text to
	 *            match
	 * @return {@link List} of Job names (in jenkins CI)
	 * @throws AppFactoryException
	 *             if an error occurs
	 */
	@SuppressWarnings("unchecked")
	public List<String> getJobNames(String filterText, String tenantDomain)
			throws AppFactoryException {
		List<String> jobNames = new ArrayList<String>();

		final String wrapperTag = "JobNames";

		final String xpathExpression = StringUtils.isNotEmpty(filterText) ? String
				.format("/*/job/name[contains(., '%s')]", filterText)
				: "/*/job/name";

		NameValuePair[] queryParameters = {
				new NameValuePair(AppFactoryConstants.WRAPPER_TAG_KEY, wrapperTag),
				new NameValuePair(AppFactoryConstants.XPATH_EXPRESSION_KEY, xpathExpression) };

		GetMethod getJobsMethod = createGet("/view/All/api/xml",
				queryParameters, tenantDomain);
		try {
			int httpStatusCode = getAuthenticatedHttpClient().executeMethod(getJobsMethod);

			if (!isSuccessfulStatusCode(httpStatusCode)) {
				httpStatusCode = resendRequest(getJobsMethod);
			}

			if (!isSuccessfulStatusCode(httpStatusCode)) {
				String errorMsg = String.format(
						"Unable to retrieve job names: filter text :%s, "
								+ "jenkins returned, http status : %d",
						filterText, httpStatusCode);
				log.error(errorMsg);
				throw new AppFactoryException(errorMsg);
			}

			StAXOMBuilder builder = new StAXOMBuilder(
					getJobsMethod.getResponseBodyAsStream());

			Iterator<OMElement> jobNameElements = builder.getDocumentElement()
					.getChildElements();

			while (jobNameElements.hasNext()) {
				OMElement jobName = jobNameElements.next();
				jobNames.add(jobName.getText());
			}
		} catch (Exception ex) {
			String errorMsg = String.format(
					"Unable to retrieve available jobs from jenkins : %s",
					ex.getMessage());
			log.error(errorMsg, ex);
			throw new AppFactoryException(errorMsg, ex);
		} finally {
			getJobsMethod.releaseConnection();
		}
		return jobNames;

	}

	public File getArtifact(String jobName, String artifactName,
			String tenantDomain) throws AppFactoryException {
		File file = null;
		String url = "/job/" + jobName + "/ws/" + artifactName;
		GetMethod getArtifactMethod = createGet(url, null, tenantDomain);
		InputStream ins = null;
		FileOutputStream out = null;
		try {
			int httpStatusCode = getAuthenticatedHttpClient().executeMethod(
					getArtifactMethod);

			if (!isSuccessfulStatusCode(httpStatusCode)) {
				httpStatusCode = resendRequest(getArtifactMethod);
			}

			if (!isSuccessfulStatusCode(httpStatusCode)) {
				String errorMsg = String.format(
						"Unable to retrieve artifact from jenkins. "
								+ "jenkins returned, http status : %d",
						httpStatusCode);
				log.error(errorMsg);
				throw new AppFactoryException(errorMsg);
			}

			String carbonHome = System.getProperty("carbon.home"); // TODO find
																	// the
																	// constnat

			String fileName = artifactName.substring(artifactName
					.lastIndexOf(AppFactoryConstants.URL_SEPERATOR) + 1);

			ins = getArtifactMethod.getResponseBodyAsStream();
			@SuppressWarnings("UnusedAssignment")
			int read = 0;
			byte[] bytes = new byte[1024];
			file = new File(carbonHome + "/tmp/" + fileName);
			out = new FileOutputStream(file);
			while ((read = ins.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
		} catch (HttpException e) {
			String msg = "Failed to get artifact " + artifactName + "for job name : " + jobName + " and tenant : "
			             + tenantDomain;
			throw new AppFactoryException(msg, e);
		} catch (FileNotFoundException e) {
			String msg = "Failed to get artifact " + artifactName + "for job name : " + jobName + " and tenant : "
			             + tenantDomain;
			throw new AppFactoryException(msg, e);
		} catch (IOException e) {
			String msg = "Failed to get artifact " + artifactName + "for job name : " + jobName + " and tenant : "
			             + tenantDomain;
			throw new AppFactoryException(msg, e);
		} finally {
			if (ins != null) {
				try {
					ins.close();
				} catch (IOException e) {
					log.warn("Unable to close input file stream.", e);
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					log.warn("Unable to close output file stream.", e);
				}
			}
			getArtifactMethod.releaseConnection();
		}
		return file;
	}

	/**
	 * Create a job in Jenkins
	 * 
	 * @param jobName
	 *            name of the job
	 * @param jobParams
	 *            Job configuration parameters
	 * @param tenantDomain
	 *            Tenant domain of applicatoin
	 * @throws AppFactoryException
	 *             if an error occures.
	 */
	public void createJob(String jobName, Map<String, String> jobParams,
			String tenantDomain) throws AppFactoryException {

		OMElement jobConfiguration = new JobConfigurator(jobParams)
				.configure(jobParams
						.get(JenkinsCIConstants.APPLICATION_EXTENSION));
		NameValuePair[] queryParams = { new NameValuePair(AppFactoryConstants.JOB_NAME_KEY, jobName) };
		PostMethod createJob = null;
		boolean jobCreatedFlag = false;

		try {
			createJob = createPost(
					"/createItem",
					queryParams,
					new StringRequestEntity(jobConfiguration
							.toStringWithConsume(), "text/xml", "utf-8"),
					tenantDomain);
			int httpStatusCode = getAuthenticatedHttpClient().executeMethod(createJob);

			if (!isSuccessfulStatusCode(httpStatusCode)) {
				httpStatusCode = resendRequest(createJob);
			}

			if (!isSuccessfulStatusCode(httpStatusCode)) {
				String errorMsg = String.format(
						"Unable to create the job: [%s]. jenkins "
								+ "returned, http status : %d", jobName,
						httpStatusCode);
				log.error(errorMsg);
				throw new AppFactoryException(errorMsg);
			} else {
				jobCreatedFlag = true;
			}
			if ("svn".equals(jobParams.get(JenkinsCIConstants.REPOSITORY_TYPE))) {
				setSvnCredentials(
						jobName,
						jobParams
								.get(JenkinsCIConstants.REPOSITORY_ACCESS_CREDENTIALS_USERNAME),
						jobParams
								.get(JenkinsCIConstants.REPOSITORY_ACCESS_CREDENTIALS_PASSWORD),
						jobParams.get(JenkinsCIConstants.REPOSITORY_URL),
						tenantDomain);
			}

		} catch (Exception ex) {
			String errorMsg = "Error while trying creating job: " + jobName;
			log.error(errorMsg, ex);

			if (jobCreatedFlag) {
				// the job was created but setting svn
				// credentials failed. Therefore try
				// deleting the entire job (instead of
				// keeping a unusable job in jenkins)
				try {
					deleteJob(jobName, tenantDomain);
				} catch (AppFactoryException delExpception) {
					log.error(
							"Unable to delete the job after failed attempt set svn credentials,"
									+ " job: " + jobName, delExpception);
				}
			}

			throw new AppFactoryException(errorMsg, ex);
		} finally {

			if (createJob != null) {
				createJob.releaseConnection();
			}
		}
	}

	/**
	 * Checks weather a job exists in Jenkins server
	 * 
	 * @param applicationId
	 *            name of the job.
	 * @return true if job exits, false otherwise.
	 * @throws AppFactoryException
	 *             if an error occurs.
	 */
	public boolean isJobExists(String applicationId, String version,
			String tenantDomain) throws AppFactoryException {

		String jobName = ServiceHolder.getContinuousIntegrationSystemDriver()
				.getJobName(applicationId, version, "");

		final String wrapperTag = "JobNames";
		NameValuePair[] queryParameters = {
				new NameValuePair("wrapper", wrapperTag),
				new NameValuePair("xpath", String.format(
						"/*/job/name[text()='%s']", jobName)) };

		GetMethod checkJobExistsMethod = createGet("/api/xml", queryParameters,
				tenantDomain);

		boolean isExists = false;

		try {
			checkJobExistsMethod.setQueryString(queryParameters);
			int httpStatusCode = getAuthenticatedHttpClient().executeMethod(
					checkJobExistsMethod);

			if (!isSuccessfulStatusCode(httpStatusCode)) {
				httpStatusCode = resendRequest(checkJobExistsMethod);
			}

			if (!isSuccessfulStatusCode(httpStatusCode)) {
				final String errorMsg = String.format(
						"Unable to check the existance of job: [%s]"
								+ ". jenkins returned, http status : %d",
						jobName, httpStatusCode);

				log.error(errorMsg);
				throw new AppFactoryException(errorMsg);
			}

			StAXOMBuilder builder = new StAXOMBuilder(
					checkJobExistsMethod.getResponseBodyAsStream());
			isExists = builder.getDocumentElement().getChildElements()
					.hasNext();
		} catch (Exception ex) {
			String errorMsg = "Error while checking the existance of job: "
					+ jobName;
			log.error(errorMsg, ex);
			throw new AppFactoryException(errorMsg, ex);
		} finally {
			checkJobExistsMethod.releaseConnection();
		}

		return isExists;
	}

	/**
	 * Deletes a job
	 * 
	 * @param jobName
	 *            name of the job
	 * @return true if job exited on Jenkins and successfully deleted.
	 * @throws AppFactoryException
	 *             if an error occures.
	 */
	public boolean deleteJob(String jobName, String tenantDomain)
			throws AppFactoryException {
		PostMethod deleteJobMethod = createPost(
				String.format("/job/%s/doDelete", jobName), null, null,
				tenantDomain);
		int httpStatusCode = -1;
		try {

            //APPFAC-2853 fix; HttpClient has a bug in handling 302, there for reinitializing the client
            HttpClient httpClient =  getNewAuthenticatedHttpClient();

            //This will always result 302 due to APPFAC-2853 fix
			httpStatusCode = httpClient.executeMethod(deleteJobMethod);

			if (HttpStatus.SC_MOVED_TEMPORARILY != httpStatusCode) {
				final String errorMsg = String.format(
						"Unable to delete: [%s]. jenkins returned, "
								+ "http status : %d", jobName, httpStatusCode);
				log.error(errorMsg);
				throw new AppFactoryException(errorMsg);
			}

		} catch (Exception ex) {
			String errorMsg = "Error while deleting the job: " + jobName;
			log.error(errorMsg);
			if (log.isDebugEnabled()) {
				log.debug(errorMsg, ex);
			}
			throw new AppFactoryException(errorMsg, ex);
		} finally {
			deleteJobMethod.releaseConnection();
		}
		return HttpStatus.SC_NOT_FOUND != httpStatusCode;

	}

	/**
	 * Starts a build job available in Jenkins
	 * 
	 * @param applicationId
	 * @param version
	 * @param doDeploy
	 * @param stageName
	 * @param tagName
	 * @param tenantDomain
	 * @param userName
	 * @param repoFrom
	 * @throws AppFactoryException
	 */
	public void startBuild(String applicationId, String version,
			boolean doDeploy, String stageName, String tagName,
			String tenantDomain, String userName, String repoFrom)
			throws AppFactoryException {

		userName = MultitenantUtils.getTenantAwareUsername(userName);
		String jobName = ServiceHolder.getContinuousIntegrationSystemDriver()
				.getJobName(applicationId, version, userName, repoFrom);
		
		String artifactType = "war";

		boolean isFreestyle = false;
		try {
			isFreestyle = AppFactoryCoreUtil
					.isFreestyleNonBuilableProject(artifactType);
		} catch (Exception e) {
			// continue flow as a non freestyle app
		}

		List<NameValuePair> parameters = new ArrayList<NameValuePair>();
		parameters.add(new NameValuePair(AppFactoryConstants.IS_AUTOMATIC,
		                                 AppFactoryConstants.STRING_FALSE));
		parameters
				.add(new NameValuePair(AppFactoryConstants.DO_DEPLOY, Boolean.toString(doDeploy)));
		parameters.add(new NameValuePair(AppFactoryConstants.DEPLOY_STAGE, stageName));
		parameters.add(new NameValuePair(AppFactoryConstants.BUILD_REPO_FROM, repoFrom));

		String tenantUserName = userName + UserCoreConstants.TENANT_DOMAIN_COMBINER + tenantDomain;

		parameters.add(new NameValuePair(AppFactoryConstants.TENANT_USER_NAME, tenantUserName));

		// TODO should get the persistArtifact parameter value from the user and
		// set here
		if (tagName != null && !tagName.equals(AppFactoryConstants.EMPTY_STRING)) {
			parameters.add(new NameValuePair(AppFactoryConstants.PERSIST_ARTIFACT, String
					.valueOf(true)));
			parameters.add(new NameValuePair(AppFactoryConstants.TAG_NAME, tagName));
		} else {
			parameters.add(new NameValuePair(AppFactoryConstants.PERSIST_ARTIFACT, String
					.valueOf(false)));
		}

		PostMethod startBuildMethod = createPost(
				String.format("/job/%s/buildWithParameters", jobName),
				parameters.toArray(new NameValuePair[parameters.size()]), null,
				tenantDomain);

		int httpStatusCode = -1;
		String correlationKey = applicationId + AppFactoryConstants.MINUS + tenantUserName +
		                        AppFactoryConstants.MINUS + repoFrom + AppFactoryConstants.MINUS +
		                        version;
		try {
			httpStatusCode = getAuthenticatedHttpClient().executeMethod(startBuildMethod);

			if (!isSuccessfulStatusCode(httpStatusCode)) {
				httpStatusCode = resendRequest(startBuildMethod);
			}

            if (HttpStatus.SC_NOT_FOUND == httpStatusCode) {
	            String repoType = ApplicationDAO.getInstance().getApplicationInfo(applicationId).getRepositoryType();
	            RepositoryProvider repoProvider = Util.getRepositoryProvider(repoType);
	            String repoURL;
                try {
                    repoURL = repoProvider.getAppRepositoryURL(applicationId, tenantDomain);
                } catch (RepositoryMgtException e) {
                    log.error("Error occured whe creating repository URL," + e.getMessage());
                    throw new AppFactoryException(e);
                }

                ServiceContainer.getJenkinsCISystemDriver()
                        .createJob(applicationId, version, "", tenantDomain, userName,
                                repoURL, AppFactoryConstants.ORIGINAL_REPOSITORY);

                httpStatusCode = resendRequest(startBuildMethod);
            }


		} catch (Exception ex) {
			String repoType = null;
			if (EventingConstants.ORIGINAL_REPO_FORM.equals(repoFrom)) {
				repoType = "master repo";
			} else {
				repoType = "forked repo";
			}
			String errorMsg = "Unable to start build for " + version + " in "
					+ repoType + " by " + userName;
			log.error(errorMsg);

			// Notify to wall

			// freestyle apps should not be built by user. So notification about
			// the build is not sent to the user.
			// ( they are built internally for deployment purposes )
			if (!isFreestyle) {

				try {
					EventNotifier.getInstance().notify(
							ContinousIntegrationEventBuilderUtil.buildTriggerBuildEvent(
                                    applicationId, repoFrom, userName,
                                    errorMsg, ex.getMessage(),
                                    Event.Category.ERROR, correlationKey, userName));
				} catch (AppFactoryEventException e1) {
					log.error("Failed to notify build triggered event", e1);
					// do not throw again.
				}
			}
			throw new AppFactoryException(errorMsg, ex);
		} finally {
			startBuildMethod.releaseConnection();
		}

		if (HttpStatus.SC_FORBIDDEN == httpStatusCode) {
			final String errorMsg = "Unable to start a build for job ["
					.concat(jobName).concat("] due to invalid credentials.")
					.concat("Jenkins returned, http status : [")
					.concat(String.valueOf(httpStatusCode)).concat("]");
			log.error(errorMsg);
			throw new AppFactoryException(errorMsg);
		}

		if (HttpStatus.SC_NOT_FOUND == httpStatusCode) {
			final String errorMsg = "Unable to find the job [" + jobName
					+ "Jenkins returned, " + "http status : [" + httpStatusCode
					+ "]";
			log.error(errorMsg);
			throw new AppFactoryException(errorMsg);
		}

		DefaultBuildDriverListener listener = new DefaultBuildDriverListener();
		try {
			String repoType = null;
			if (EventingConstants.ORIGINAL_REPO_FORM.equals(repoFrom)) {
				repoType = AppFactoryConstants.MASTER_REPO;
			} else {
				repoType = AppFactoryConstants.FORKED_REPO;
			}

			// freestyle apps should not be built by user. So notification about
			// the build is not sent to the user.
			// ( they are built internally for deployment purposes )
			if (!isFreestyle) {
				String infoMessage = "Build started for " + version + " in "
						+ repoType + " by " + userName;
				EventNotifier.getInstance().notify(
						ContinousIntegrationEventBuilderUtil.buildTriggerBuildEvent(applicationId,
								repoFrom, userName, infoMessage, "",
								Event.Category.INFO, correlationKey, userName));
			}

		} catch (AppFactoryEventException e) {
			log.error("Failed to notify build triggered event", e);
			// do not throw again.
		}
		listener.onBuildStart(applicationId, version, "", userName, repoFrom,
				tenantDomain);

	}

	/**
	 * Logs out of the jenkins server
	 * 
	 * @throws AppFactoryException
	 *             if an error occurs
	 */
	@SuppressWarnings("UnusedDeclaration")
	public void logout(String tenantDomain) throws AppFactoryException {
		GetMethod logoutMethod = createGet("/logout", null, tenantDomain);
		try {
			getAuthenticatedHttpClient().executeMethod(logoutMethod);
		} catch (Exception ex) {
			String errorMsg = "Unable to login from jenkins";
			log.error(errorMsg);
			throw new AppFactoryException(errorMsg, ex);

		} finally {
			logoutMethod.releaseConnection();
		}

	}

	public String getbuildStatus(String buildUrl, String tenantDomain)
			throws AppFactoryException {

		String buildStatus = AppFactoryConstants.BUILD_STATUS_UNKNOWN;
		GetMethod checkJobExistsMethod = createGet(buildUrl, "api/xml", null,
				tenantDomain);

		try {
			int httpStatusCode = getAuthenticatedHttpClient().executeMethod(
					checkJobExistsMethod);

			if (!isSuccessfulStatusCode(httpStatusCode)) {
				httpStatusCode = resendRequest(checkJobExistsMethod);
			}

			if (!isSuccessfulStatusCode(httpStatusCode)) {
				final String errorMsg = String.format(
						"Unable to check the status  of build: [%s]"
								+ ". jenkins returned, http status : %d",
						buildUrl, httpStatusCode);

				log.error(errorMsg);
				throw new AppFactoryException(errorMsg);
			}

			StAXOMBuilder builder = new StAXOMBuilder(
					checkJobExistsMethod.getResponseBodyAsStream());
			OMElement resultElement = builder.getDocumentElement();
			if (resultElement != null) {
				if (AppFactoryConstants.STRING_FALSE.equals(getValueUsingXpath(resultElement,
				                                      "/*/building"))) {
					buildStatus = getValueUsingXpath(resultElement, "/*/result");
				} else {
					buildStatus = AppFactoryConstants.BUILD_STATUS_BUILDING;
				}

			}

		} catch (Exception ex) {
			String errorMsg = "Error while checking the status of build: "
					+ buildUrl;
			log.error(errorMsg, ex);
			throw new AppFactoryException(errorMsg, ex);
		} finally {
			checkJobExistsMethod.releaseConnection();
		}

		return buildStatus;
	}

	public List<String> getBuildUrls(String jobName, String tenantDomain)
			throws AppFactoryException {

		List<String> listOfUrls = new ArrayList<String>();

		final String wrapperTag = "Builds";
		NameValuePair[] queryParameters = {
				new NameValuePair(AppFactoryConstants.WRAPPER_TAG_KEY, wrapperTag),
				new NameValuePair(AppFactoryConstants.XPATH_EXPRESSION_KEY, "/*/build/url") };

		GetMethod getBuildsMethod = createGet(
				String.format("/job/%s/api/xml", jobName), queryParameters,
				tenantDomain);
		try {
			int httpStatusCode = getAuthenticatedHttpClient().executeMethod(getBuildsMethod);

			if (!isSuccessfulStatusCode(httpStatusCode)) {
				httpStatusCode = resendRequest(getBuildsMethod);
			}

			if (!isSuccessfulStatusCode(httpStatusCode)) {
				String errorMsg = String.format(
						"Unable to retrieve available build urls from "
								+ "jenkins for job %s. jenkins returned,"
								+ " http status : %d", jobName, httpStatusCode);
				log.error(errorMsg);
				throw new AppFactoryException(errorMsg);
			}

			StAXOMBuilder builder = new StAXOMBuilder(
					getBuildsMethod.getResponseBodyAsStream());
			@SuppressWarnings("unchecked")
			Iterator<OMElement> urlElementsIte = builder.getDocumentElement()
					.getChildElements();
			while (urlElementsIte.hasNext()) {
				OMElement urlElement = urlElementsIte.next();
				listOfUrls.add(urlElement.getText());
			}

		} catch (Exception ex) {
			String errorMsg = String.format(
					"Unable to retrieve available jobs from jenkins : %s",
					ex.getMessage());
			log.error(errorMsg, ex);
			throw new AppFactoryException(errorMsg, ex);
		} finally {
			getBuildsMethod.releaseConnection();
		}
		return listOfUrls;
	}

	public List<Statistic> getOverallLoad(String tenantDomain)
			throws AppFactoryException {

		GetMethod overallLoad = createGet("/overallLoad/api/xml", null,
				tenantDomain);

		List<Statistic> list = new ArrayList<Statistic>();

		try {

			int httpStatusCode = getAuthenticatedHttpClient().executeMethod(overallLoad);

			if (!isSuccessfulStatusCode(httpStatusCode)) {
				httpStatusCode = resendRequest(overallLoad);
			}

			if (!isSuccessfulStatusCode(httpStatusCode)) {
				final String errorMsg = String.format(
						"Unable to check the overal load of jenkins"
								+ ". jenkins returned, http status : %d",
						httpStatusCode);
				log.error(errorMsg);
				throw new AppFactoryException(errorMsg);
			}

			StAXOMBuilder builder = new StAXOMBuilder(
					overallLoad.getResponseBodyAsStream());
			@SuppressWarnings("unchecked")
			Iterator<OMElement> elementIterator = (Iterator<OMElement>) builder
					.getDocumentElement().getChildElements();

			while (elementIterator.hasNext()) {

				OMElement statElement = elementIterator.next();
				String value = StringUtils.isEmpty(statElement.getText()) ? "-1"
						: statElement.getText();

				Statistic stat = new Statistic(statElement.getLocalName(),
						value);
				list.add(stat);
			}

		} catch (Exception ex) {
			String errorMsg = "Error while checking the overall load of jenkins";
			log.error(errorMsg, ex);
			throw new AppFactoryException(errorMsg, ex);
		} finally {
			overallLoad.releaseConnection();
		}
		return list;

	}

	/**
	 * Returns Jenkins-API information as a JSON array.
	 * 
	 * @param jobName
	 *            eg: applicationKey023-trunk-default
	 * @param treeStructure
	 *            eg: builds[number,duration,result]
	 * @return buildsInfo (Which is a JSON array with requested information
	 * @throws AppFactoryException
	 */
	public String getJsonTree(String jobName, String treeStructure,
			String tenantDomain) throws AppFactoryException {

		String buildUrl = null;
		String buildsInfo = null;
		if (log.isDebugEnabled()) {
			log.debug(String.format("getJsonTree - for %s > %s", jobName, treeStructure));
		}
		if (jobName == null || jobName.isEmpty()
		    || jobName.equalsIgnoreCase(AppFactoryConstants.ALL_JOB_NAME) ||
		    jobName.equals(AppFactoryConstants.ASTERISK)) {
			buildUrl = this.getJenkinsUrl() + AppFactoryConstants.URL_SEPERATOR;
		} else {
			buildUrl = String.format("%s/job/%s/", this.getJenkinsUrl(),
					jobName);
		}

		NameValuePair[] queryParameters = { new NameValuePair(AppFactoryConstants.JSON_TREE_STRUCTURE,
				treeStructure) };

		GetMethod getBuildsHistoryMethod = createGet(buildUrl, "api/json",
				queryParameters, tenantDomain);

		try {
			int httpStatusCode = getAuthenticatedHttpClient().executeMethod(
					getBuildsHistoryMethod);

			if (!isSuccessfulStatusCode(httpStatusCode)) {
				httpStatusCode = resendRequest(getBuildsHistoryMethod);
			}

			if (!isSuccessfulStatusCode(httpStatusCode)) {
				final String errorMsg = String.format(
						"Unable to fetch information from Jenkins for : %s, %d",
						getBuildsHistoryMethod.getURI(), httpStatusCode);

				log.error(errorMsg);
				throw new AppFactoryException(errorMsg);
			}
			buildsInfo = getBuildsHistoryMethod.getResponseBodyAsString();
		} catch (Exception ex) {
			String errorMsg = String.format(
					"Error while fetching information tree %s for : %s",
					treeStructure, jobName);
			log.error(errorMsg, ex);
			throw new AppFactoryException(errorMsg, ex);
		} finally {
			getBuildsHistoryMethod.releaseConnection();
		}
		return buildsInfo;
	}

	/**
	 * A convenient methods to pass credentials of a svn repository (specified
	 * in the job)
	 * 
	 * @param jobName
	 *            Name of job
	 * @param userName
	 *            svn username
	 * @param password
	 *            password
	 * @param svnRepo
	 *            repo url
	 * @throws AppFactoryException
	 *             if an error occurs.
	 */
	private void setSvnCredentials(String jobName, String userName,
			String password, String svnRepo, String tenantDomain)
			throws AppFactoryException {
		final String setCredentialsURL = String.format(
				"/job/%s/descriptorByName/hudson.scm"
						+ ".SubversionSCM/postCredential", jobName);

		PostMethod setCredentialsMethod = createPost(setCredentialsURL, null,
				null, tenantDomain);

		Part[] parts = { new StringPart(AppFactoryConstants.STRING_URL, svnRepo),
		                 new StringPart(AppFactoryConstants.SVN_KIND_KEY,
		                                AppFactoryConstants.PASSWORD_STRING),
		                 new StringPart(AppFactoryConstants.SVN_USERNAME, userName),
		                 new StringPart(AppFactoryConstants.SVN_PASSWORD, password), };
		setCredentialsMethod.setRequestEntity(new MultipartRequestEntity(parts,
				setCredentialsMethod.getParams()));

		final String redirectedURlFragment = String.format(
				"/job/%s/descriptorByName/hudson.scm"
						+ ".SubversionSCM/credentialOK", jobName);

		try {
			int httpStatus = getAuthenticatedHttpClient()
					.executeMethod(setCredentialsMethod);
			Header locationHeader = setCredentialsMethod
					.getResponseHeader(AppFactoryConstants.LOCATION_HEADER_PARAM);

			// if operation completed successfully Jenkins returns http 302,
			// which location header ending with '..../credentialOK'

			if (HttpStatus.SC_MOVED_TEMPORARILY != httpStatus
					|| (locationHeader != null && !StringUtils.endsWithIgnoreCase(StringUtils
									.trimToEmpty(locationHeader.getValue()),
									redirectedURlFragment))) {

				String errorMsg = "Unable to set svn credentials for the new job: jenkins "
						+ "returned - Https status "
						+ httpStatus
						+ " ,Location header " + locationHeader;
				log.error(errorMsg);
				throw new AppFactoryException(errorMsg);
			}

		} catch (IOException e) {
			String errorMsg = String.format(
					"Unable to send svn credentials to jenkins for job: "
							+ "%s", jobName);
			throw new AppFactoryException(errorMsg, e);
		} finally {
			setCredentialsMethod.releaseConnection();
		}
	}

	/**
	 * This method will call jenkins to deploy the latest successfully built
	 * artifact of the given job name
	 * 
	 * @param jobName
	 *            job name of which the artifact is going to get deployed
	 * @param artifactType
	 *            artifact type (car/war) that is going to get deployed
	 * @param stage
	 *            server Urls that we need to deploy the artifact into
	 * @throws AppFactoryException
	 */
	public void deployLatestSuccessArtifact(String jobName, String artifactType, String stage, String tenantDomain,
	                                        String userName, String deployAction, String repoFrom)
											throws AppFactoryException {
		String deployLatestSuccessArtifactUrl = "/plugin/appfactory-plugin/deployLatestSuccessArtifact";

		PostMethod deployLatestSuccessArtifactMethod = null;

		try {
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			parameters.add(new NameValuePair(AppFactoryConstants.ARTIFACT_TYPE, artifactType));
			ApplicationTypeBean applicationTypeBean = ApplicationTypeManager.getInstance()
			                                                                .getApplicationTypeBean(artifactType);

			if (applicationTypeBean == null) {
				throw new AppFactoryException(
						"Application Type details cannot be found for Artifact Type : " +
						artifactType + " , Job Name : " + jobName + ", stage : " + stage +
						" for username: " + userName);
			}

			String runtimeNameForAppType = applicationTypeBean.getRuntimes()[0];
			RuntimeBean runtimeBean = RuntimeManager.getInstance().getRuntimeBean(runtimeNameForAppType);

			if (runtimeBean == null) {
				throw new AppFactoryException(
						"Runtime details cannot be found for Artifact Type : " + artifactType +
						" , Job Name : " + jobName + ", stage : " + stage + " for username: " +
						userName);
			}

			parameters.add(new NameValuePair(AppFactoryConstants.RUNTIME_NAME_FOR_APPTYPE,runtimeNameForAppType));
			parameters.add(new NameValuePair(AppFactoryConstants.RUNTIME_SUBSCRIBE_ON_DEPLOYMENT,
			                                 Boolean.toString(runtimeBean.getSubscribeOnDeployment())));
			parameters.add(new NameValuePair(AppFactoryConstants.SERVER_DEPLOYMENT_PATHS,
			                                 applicationTypeBean.getServerDeploymentPath()));
			parameters.add(new NameValuePair(AppFactoryConstants.JOB_NAME, jobName));
			parameters.add(new NameValuePair(AppFactoryConstants.DEPLOY_STAGE, stage));
			parameters.add(new NameValuePair(AppFactoryConstants.DEPLOY_ACTION, deployAction));
			parameters.add(new NameValuePair(AppFactoryConstants.APPLICATION_EXTENSION,
			                                 applicationTypeBean.getExtension()));
			parameters.add(new NameValuePair(AppFactoryConstants.REPOSITORY_FROM, repoFrom));

			String tenantUserName = userName + UserCoreConstants.TENANT_DOMAIN_COMBINER + tenantDomain;
			parameters.add(new NameValuePair(AppFactoryConstants.TENANT_USER_NAME, tenantUserName));

			addRunTimeParameters(stage, parameters, runtimeBean);

			deployLatestSuccessArtifactMethod = createPost(
					deployLatestSuccessArtifactUrl, null, null,
					parameters.toArray(new NameValuePair[parameters.size()]),
					tenantDomain);

			int httpStatusCode = getAuthenticatedHttpClient().executeMethod(
					deployLatestSuccessArtifactMethod);

			if (!isSuccessfulStatusCode(httpStatusCode)) {
				httpStatusCode = resendRequest(deployLatestSuccessArtifactMethod);
			}

			log.info("status code for deploy latest success artifact type : " + artifactType + " job name : " +
			         jobName + " stage : " + stage + " in tenant : " + tenantDomain + " is " + httpStatusCode);
			if (!isSuccessfulStatusCode(httpStatusCode)) {
				String errorMsg = "Unable to deploy the latest success artifact. jenkins "
						+ "returned, http status : " + httpStatusCode;
				log.error(errorMsg);
				throw new AppFactoryException(errorMsg);
			}
		} catch (Exception ex) {
			String errorMsg = "Unable to deploy the latest success artifact : "
					+ ex.getMessage();
			log.error(errorMsg, ex);
			throw new AppFactoryException(errorMsg, ex);
		} finally {
			if (deployLatestSuccessArtifactMethod != null) {
				deployLatestSuccessArtifactMethod.releaseConnection();
			}
		}

	}

	/**
	 * To undeploy any artifact for given below parameters
	 *
	 * @param deployerType    type of the deployments eg :*, net ,php
	 * @param jobName         jenkins job name
	 * @param applicationId   id of the application
	 * @param applicationType type of the application eg :war
	 * @param version         version of the artifact
	 * @param stage           stage eg : dev,prod,test
	 * @param tenantDomain    tenant domain
	 * @throws AppFactoryException
	 */
	public void undeployArtifact(String deployerType, String jobName, String applicationId, String applicationType,
	                             String version, String stage, String tenantDomain) throws AppFactoryException {

		ApplicationTypeBean applicationTypeBean = ApplicationTypeManager.getInstance()
		                                                                .getApplicationTypeBean(applicationType);

		if (applicationTypeBean == null) {
			throw new AppFactoryException(
					"Application Type details cannot be found for Artifact Type : " +
					applicationType + ", Job Name : " + jobName + ", stage : " + stage +
					", application id" + applicationId + " of version " + version +
					" for tenant domain: " + tenantDomain);
		}

		String runtimeNameForAppType = applicationTypeBean.getRuntimes()[0];
		RuntimeBean runtimeBean = RuntimeManager.getInstance().getRuntimeBean(runtimeNameForAppType);

		if (runtimeBean == null) {
			throw new AppFactoryException(
					"Runtime details cannot be found for Artifact Type : " + applicationType +
					", Job Name : " + jobName + ", stage : " + stage + ", application id" +
					applicationId + " of version " + version + " for tenant domain: " +
					tenantDomain);
		}

		List<NameValuePair> parameters = new ArrayList<NameValuePair>();
		addRunTimeParameters(stage, parameters, runtimeBean);
		String userName = CarbonContext.getThreadLocalCarbonContext().getUsername();
		String tenantUserName = userName + UserCoreConstants.TENANT_DOMAIN_COMBINER + tenantDomain;
		parameters.add(new NameValuePair(AppFactoryConstants.TENANT_USER_NAME, tenantUserName));
		parameters.add(new NameValuePair(AppFactoryConstants.APPLICATION_ID, applicationId));
		parameters.add(new NameValuePair(AppFactoryConstants.APP_TYPE, applicationType));
		parameters.add(new NameValuePair(AppFactoryConstants.APPLICATION_VERSION, version));
		parameters.add(new NameValuePair(AppFactoryConstants.DEPLOY_STAGE, stage));
		parameters.add(new NameValuePair(AppFactoryConstants.JOB_NAME, jobName));
		parameters.add(new NameValuePair(AppFactoryConstants.DEPLOYER_TYPE, deployerType));
		parameters.add(new NameValuePair(AppFactoryConstants.APPLICATION_EXTENSION, applicationTypeBean.getExtension()));
		String serverDeploymentPaths = applicationTypeBean.getServerDeploymentPath();
		parameters.add(new NameValuePair(AppFactoryConstants.SERVER_DEPLOYMENT_PATHS, serverDeploymentPaths));
		parameters.add(new NameValuePair(AppFactoryConstants.RUNTIME_UNDEPLOYER_CLASSNAME,
		                                 runtimeBean.getUndeployerClassName()));
		PostMethod undeployArtifactMethod = createPost(UNDEPLOY_ARTIFACT_URL, null, null,
		                                               parameters.toArray(new NameValuePair[parameters.size()]),
		                                               tenantDomain);
		if (log.isDebugEnabled()) {
			log.debug("Trying to undeploy  artifacts for job name : " + jobName + " application id : " + applicationId
			          + " application type : " + applicationType + " deployer type : " + deployerType + " version : " +
			          version + " stage : " + stage + " tenant domain : " + tenantDomain);
		}
		try {
			int httpStatusCode = getAuthenticatedHttpClient().executeMethod(undeployArtifactMethod);

			if (!isSuccessfulStatusCode(httpStatusCode)) {
				httpStatusCode = resendRequest(undeployArtifactMethod);
			}
			if (!isSuccessfulStatusCode(httpStatusCode)) {
				String errorMsg = "Unable to undeploy the  artifact for job name : " + jobName + " application id : " +
				                  applicationId + " version : " + version + " stage : " + stage + " tenant domain : " +
				                  tenantDomain + ". Jenkins returned, http status : " + httpStatusCode;
				throw new AppFactoryException(errorMsg);
			}
		} catch (IOException e) {
			String msg = "Unable to undeploy artifact for application id : " + applicationId + " version : " +
			             version + " stage : " + stage + " tenant domain : " + tenantDomain;
			throw new AppFactoryException(msg, e);
		} finally {
			undeployArtifactMethod.releaseConnection();
		}
	}

	public void deployPromotedArtifact(String jobName, String artifactType,
			String stage, String tenantDomain, String userName)
			throws AppFactoryException {

		String deployPromotedArtifactUrl = "/plugin/appfactory-plugin/deployPromotedArtifact";

		ApplicationTypeBean applicationTypeBean =
				ApplicationTypeManager.getInstance().getApplicationTypeBean(artifactType);

		if (applicationTypeBean == null) {
			throw new AppFactoryException("Application Type details cannot be found");
		}

		String runtimeNameForAppType = applicationTypeBean.getRuntimes()[0];
		RuntimeBean runtimeBean = RuntimeManager.getInstance().getRuntimeBean(runtimeNameForAppType);

		if (runtimeBean == null) {
			throw new AppFactoryException("Runtime details cannot be found");
		}

		List<NameValuePair> parameters = new ArrayList<NameValuePair>();
		parameters.add(new NameValuePair(AppFactoryConstants.JOB_NAME, jobName));
		parameters.add(new NameValuePair(AppFactoryConstants.ARTIFACT_TYPE, artifactType));

		parameters.add(new NameValuePair(AppFactoryConstants.RUNTIME_NAME_FOR_APPTYPE,
		                                 runtimeNameForAppType));
		parameters.add(new NameValuePair(AppFactoryConstants.RUNTIME_SUBSCRIBE_ON_DEPLOYMENT,
		                                 Boolean.toString(runtimeBean.getSubscribeOnDeployment())));
		parameters.add(new NameValuePair(AppFactoryConstants.SERVER_DEPLOYMENT_PATHS,
		                                 applicationTypeBean.getServerDeploymentPath()));
		parameters.add(new NameValuePair(AppFactoryConstants.APPLICATION_EXTENSION,
		                                 applicationTypeBean.getExtension()));
		parameters.add(new NameValuePair(AppFactoryConstants.DEPLOY_STAGE, stage));
		String tenantUserName = userName + UserCoreConstants.TENANT_DOMAIN_COMBINER + tenantDomain;
		parameters.add(new NameValuePair(AppFactoryConstants.TENANT_USER_NAME, tenantUserName));

		addRunTimeParameters(stage, parameters, runtimeBean);

		PostMethod deployPromotedArtifactMethod = createPost(
				deployPromotedArtifactUrl, null, null,
				parameters.toArray(new NameValuePair[parameters.size()]),
				tenantDomain);

		try {
			int httpStatusCode = getAuthenticatedHttpClient().executeMethod(
					deployPromotedArtifactMethod);

			if (!isSuccessfulStatusCode(httpStatusCode)) {
				httpStatusCode = resendRequest(deployPromotedArtifactMethod);
			}

			if (!isSuccessfulStatusCode(httpStatusCode)) {
				String errorMsg = "Unable to deploy the promoted artifact for job "
						+ jobName
						+ ". jenkins returned, http status : "
						+ httpStatusCode;
				log.error(errorMsg);
				throw new AppFactoryException(errorMsg);
			}
		} catch (Exception ex) {
			String errorMsg = "Unable to deploy the promoted artifact for job "
					+ jobName + ": " + ex.getMessage();
			log.error(errorMsg, ex);
			throw new AppFactoryException(errorMsg, ex);
		} finally {
			deployPromotedArtifactMethod.releaseConnection();
		}
	}


	/**
	 * This will return the tag names of the persisted artifact of the given job
	 * 
	 * @param jobName
	 *            job name of which we need to get the tag names
	 * @return tag names of the persisted artifacts
	 * @throws AppFactoryException
	 */
	public String[] getTagNamesOfPersistedArtifacts(String jobName,
			String tenantDomain) throws AppFactoryException {
		String getIdentifiersOfArtifactsUrl = "/plugin/appfactory-plugin/getTagNamesOfPersistedArtifacts";
		@SuppressWarnings("UnusedAssignment")
		String[] tagNamesOfPersistedArtifacts = new String[0];
		List<NameValuePair> parameters = new ArrayList<NameValuePair>();
		parameters.add(new NameValuePair(AppFactoryConstants.JOB_NAME, jobName));

		// We are creating the tenant user name using the Carbon Context and
		// sending it to the Jenkins server
		String tenantUserName = CarbonContext.getThreadLocalCarbonContext()
				.getUsername() + UserCoreConstants.TENANT_DOMAIN_COMBINER + tenantDomain;
		parameters.add(new NameValuePair(AppFactoryConstants.TENANT_USER_NAME, tenantUserName));

		PostMethod getIdsOfPersistArtifactMethod = createPost(
				getIdentifiersOfArtifactsUrl,
				parameters.toArray(new NameValuePair[parameters.size()]), null,
				tenantDomain);
		try {
			int httpStatusCode = getAuthenticatedHttpClient().executeMethod(
					getIdsOfPersistArtifactMethod);
			if (log.isDebugEnabled()) {
				log.debug("status code for getting tag names of persisted artifacts with job name : " +
				          jobName + " is " + httpStatusCode);
			}

			if (!isSuccessfulStatusCode(httpStatusCode)) {
				httpStatusCode = resendRequest(getIdsOfPersistArtifactMethod);
			}

			if (!isSuccessfulStatusCode(httpStatusCode)) {
				String errorMsg = "Unable to get the tag names of persisted artifact for job "
						+ jobName
						+ ". jenkins returned, http status : "
						+ httpStatusCode;
				log.error(errorMsg);
				throw new AppFactoryException(errorMsg);
			}
			tagNamesOfPersistedArtifacts = getIdsOfPersistArtifactMethod
					.getResponseBodyAsString().split(",");
			return tagNamesOfPersistedArtifacts;
		} catch (Exception ex) {
			String errorMsg = "Error while retrieving the tags of persisted artifact for job "
					+ jobName + " : " + ex.getMessage();
			log.error(errorMsg, ex);
			throw new AppFactoryException(errorMsg, ex);
		} finally {
			getIdsOfPersistArtifactMethod.releaseConnection();
		}
	}

	/**
	 * edit job in lifeCycle change
	 * 
	 * @param jobName
	 *            jobName
	 * @param updateState
	 *            (addAD/removeAD) flag to remove or add Auto Deploy trigger
	 *            configurations
	 * @param pollingPeriod
	 *            AD pollingPeriod
	 * @throws AppFactoryException
	 */
	@Deprecated
	public void editJob(String jobName, String updateState, int pollingPeriod,
			String tenantDomain) throws AppFactoryException {
		OMElement configuration = getConfiguration(jobName, updateState,
				pollingPeriod, tenantDomain);
		OMElement tmpConfiguration = configuration.cloneOMElement();
		setConfiguration(jobName, tmpConfiguration, tenantDomain);

	}

	public void setJobAutoBuildable(String jobName, String repositoryType,
			boolean isAutoBuild, int pollingPeriod, String tenantDomain)
			throws AppFactoryException {
		OMElement configuration = getAutoBuildUpdatedConfiguration(jobName,
				repositoryType, isAutoBuild, pollingPeriod, tenantDomain);
		OMElement tmpConfiguration = configuration.cloneOMElement();
		setConfiguration(jobName, tmpConfiguration, tenantDomain);
		if (log.isDebugEnabled()) {
			log.debug("Job : " + jobName + " successfully configured for auto building " + isAutoBuild + " in jenkins");
		}
	}

	private OMElement getAutoBuildUpdatedConfiguration(String jobName,
			String repositoryType, boolean isAutoBuild, int pollingPeriod,
			String tenantDomain) throws AppFactoryException {
		GetMethod getFetchMethod = createGet(
				String.format("/job/%s/config.xml", jobName), null,
				tenantDomain);
		OMElement configurations = null;
		try {

			int httpStatusCode = getAuthenticatedHttpClient().executeMethod(getFetchMethod);

			if (!isSuccessfulStatusCode(httpStatusCode)) {
				httpStatusCode = resendRequest(getFetchMethod);
			}

			if (!isSuccessfulStatusCode(httpStatusCode)) {
				String errorMsg = String.format(
						"Unable to retrieve available config urls from "
								+ "jenkins for job %s. "
								+ "jenkins returned, http status : %d",
						jobName, httpStatusCode);
				log.error(errorMsg);
				throw new AppFactoryException(errorMsg);
			}

			StAXOMBuilder builder = new StAXOMBuilder(
					getFetchMethod.getResponseBodyAsStream());
			configurations = builder.getDocumentElement();

			AXIOMXPath axiomxPath = new AXIOMXPath("//triggers");
			Object selectedObject = axiomxPath.selectSingleNode(configurations);
			if (isAutoBuild) {

				if (selectedObject != null) {
					OMElement selectedNode = (OMElement) selectedObject;
					selectedNode.detach();
				}

				StringBuilder payload = new StringBuilder(
						"<triggers class=\"vector\">"
								+ "<hudson.triggers.SCMTrigger>");
				if ("git".equals(repositoryType)) {
					payload = payload.append("<spec></spec>");
				} else {
					payload = payload.append("<spec>*/" + pollingPeriod
							+ " * * * *</spec>");
				}
				payload = payload.append("</hudson.triggers.SCMTrigger>"
						+ "</triggers>");
				OMElement triggerParam = AXIOMUtil.stringToOM(payload
						.toString());
				configurations.addChild(triggerParam);

			} else {
				if (selectedObject != null) {
					OMElement selectedNode = (OMElement) selectedObject;
					selectedNode.detach();
				}
			}

		} catch (Exception ex) {
			String errorMsg = String.format(
					"Unable to retrieve available jobs from jenkins : %s",
					ex.getMessage());
			log.error(errorMsg, ex);
			throw new AppFactoryException(errorMsg, ex);
		} finally {
			getFetchMethod.releaseConnection();
		}
		return configurations;

	}

	/**
	 * fetch job configurations from jenkins
	 * 
	 * @param jobName
	 *            job name of which we need to get the configuration of
	 * @param updateState
	 *            (addAD/removeAD) flag to remove or add Auto Deploy trigger
	 *            configurations
	 * @param pollingPeriod
	 *            AutoDeployment pollingPeriod
	 * @return configuration after adding or removing AD configurations
	 * @throws AppFactoryException
	 */
	@Deprecated
	private OMElement getConfiguration(String jobName, String updateState,
			int pollingPeriod, String tenantDomain) throws AppFactoryException {
		GetMethod getFetchMethod = createGet(
				String.format("/job/%s/config.xml", jobName), null,
				tenantDomain);
		OMElement configurations = null;

		try {
			int httpStatusCode = getAuthenticatedHttpClient().executeMethod(getFetchMethod);

			if (!isSuccessfulStatusCode(httpStatusCode)) {
				httpStatusCode = resendRequest(getFetchMethod);
			}

			if (!isSuccessfulStatusCode(httpStatusCode)) {
				String errorMsg = String.format(
						"Unable to retrieve available config urls from "
								+ "jenkins for job %s. "
								+ "jenkins returned, http status : %d",
						jobName, httpStatusCode);
				log.error(errorMsg);
				throw new AppFactoryException(errorMsg);
			}

			StAXOMBuilder builder = new StAXOMBuilder(
					getFetchMethod.getResponseBodyAsStream());
			configurations = builder.getDocumentElement();

			if (updateState.equals("removeAD")) {
				AXIOMXPath axiomxPath = new AXIOMXPath("//triggers");
				Object selectedObject = axiomxPath
						.selectSingleNode(configurations);
				if (selectedObject != null) {
					OMElement selectedNode = (OMElement) selectedObject;
					selectedNode.detach();
				}

			} else if (updateState.equals("addAD")) {
				String payload = "<triggers class=\"vector\">"
						+ "<hudson.triggers.SCMTrigger>" + "<spec>*/"
						+ pollingPeriod + " * * * *</spec>"
						+ "</hudson.triggers.SCMTrigger>" + "</triggers>";
				OMElement triggerParam = AXIOMUtil.stringToOM(payload);
				configurations.addChild(triggerParam);

			}

		} catch (Exception ex) {
			String errorMsg = String.format(
					"Unable to retrieve available jobs from jenkins : %s",
					ex.getMessage());
			log.error(errorMsg, ex);
			throw new AppFactoryException(errorMsg, ex);
		} finally {
			getFetchMethod.releaseConnection();
		}
		return configurations;

	}

	/**
	 * update the job configuration
	 * 
	 * @param jobName
	 *            job name of which we need to update the configuration of
	 * @param jobConfiguration
	 *            new configurations that needs to be set
	 * @throws AppFactoryException
	 */
	private void setConfiguration(String jobName, OMElement jobConfiguration,
			String tenantDomain) throws AppFactoryException {

		NameValuePair[] queryParams = { new NameValuePair(AppFactoryConstants.JOB_NAME_KEY, jobName) };
		PostMethod createJob = null;
		boolean jobCreatedFlag = false;

		try {
			createJob = createPost(
					String.format("/job/%s/config.xml", jobName),
					queryParams,
					new StringRequestEntity(jobConfiguration
							.toStringWithConsume(), "text/xml", "utf-8"),
					tenantDomain);
			int httpStatusCode = getAuthenticatedHttpClient().executeMethod(createJob);

			if (!isSuccessfulStatusCode(httpStatusCode)) {
				httpStatusCode = resendRequest(createJob);
			}

			if (!isSuccessfulStatusCode(httpStatusCode)) {
				String errorMsg = String.format(
						"Unable to set configuration: [%s]. jenkins "
								+ "returned, http status : %d", jobName,
						httpStatusCode);
				log.error(errorMsg);
				throw new AppFactoryException(errorMsg);
			} else {
				jobCreatedFlag = true;
			}

		} catch (Exception ex) {
			String errorMsg = "Error while setting configuration: " + jobName;
			log.error(errorMsg, ex);

			// noinspection ConstantConditions
			if (jobCreatedFlag) {
				try {
					deleteJob(jobName, tenantDomain);
				} catch (AppFactoryException delExpception) {
					log.error(
							"Unable to delete the job after failed attempt set svn credentials, "
									+ "job: " + jobName, delExpception);
				}
			}

			throw new AppFactoryException(errorMsg, ex);
		} finally {

			if (createJob != null) {
				createJob.releaseConnection();
			}
		}

	}

	/**
	 * Util method to create a http GET method.
	 * 
	 * @param urlFragment
	 *            Url fragments
	 * @param queryParameters
	 *            query parameters.
	 * @return a {@link GetMethod}
	 */
	private GetMethod createGet(String urlFragment,
			NameValuePair[] queryParameters, String tenantDomain) {
		return createGet(getJenkinsUrl(), urlFragment, queryParameters,
				tenantDomain);
	}

	/**
	 * Util method to create a http get method
	 * 
	 * @param baseUrl
	 *            the base url //TODO:should be irrelevant
	 * @param urlFragment
	 *            the url fragment
	 * @param queryParameters
	 *            query parameters
	 * @return a {@link GetMethod}
	 */
	private GetMethod createGet(String baseUrl, String urlFragment,
			NameValuePair[] queryParameters, String tenantDomain) {
		String url = getJenkinsUrlByTenantDomain(urlFragment, tenantDomain);

		GetMethod get = new GetMethod(url);
		if (authenticate) {
			get.setDoAuthentication(true);
		}
		if (queryParameters != null) {
			get.setQueryString(queryParameters);
		}
		return get;
	}

	/**
	 * Overloaded Util method to create a POST method
	 * 
	 * @param urlFragment
	 *            Url fragments.
	 * @param queryParameters
	 *            Query parameters.
	 * @param requestEntity
	 *            A request entity
	 * @param tenantDomain
	 *            Tenant domain of application
	 * @return a {@link PostMethod}
	 */
	private PostMethod createPost(String urlFragment,
			NameValuePair[] queryParameters, RequestEntity requestEntity,
			String tenantDomain) {
		return createPost(urlFragment, queryParameters, requestEntity, null,
				tenantDomain);
	}

	/**
	 * Get Jenkins URL for a given Tenant Domain
	 * 
	 * @param urlFragment
	 *            Url fragments
	 * @param tenantDomain
	 *            Tenant domain of the application
	 * @return Jenkins URL
	 */
	private String getJenkinsUrlByTenantDomain(String urlFragment,
			String tenantDomain) {
		if (tenantDomain != null && !tenantDomain.equals(""))
			return getJenkinsUrl() + File.separator + Constants.TENANT_SPACE
					+ File.separator + tenantDomain + Constants.JENKINS_WEBAPPS
					+ File.separator + urlFragment;
		else
			return getJenkinsUrl() + urlFragment;
	}

	/**
	 * Util method to create a POST method
	 * 
	 * @param urlFragment
	 *            Url fragments.
	 * @param queryParameters
	 *            Query parameters.
	 * @param postParameters
	 *            Post parameters.
	 * @param requestEntity
	 *            A request entity
	 * @param tenantDomain
	 *            Tenant Domain of application
	 * @return a {@link PostMethod}
	 */
	private PostMethod createPost(String urlFragment,
			NameValuePair[] queryParameters, RequestEntity requestEntity,
			NameValuePair[] postParameters, String tenantDomain) {
		String url = getJenkinsUrlByTenantDomain(urlFragment, tenantDomain);
		// getJenkinsUrl() + urlFragment
		PostMethod post = new PostMethod(url);
		if (authenticate) {
			post.setDoAuthentication(true);
		}

		if (queryParameters != null) {
			post.setQueryString(queryParameters);
		}

		if (requestEntity != null) {
			post.setRequestEntity(requestEntity);
		}

		if (postParameters != null) {
			post.addParameters(postParameters);
		}

		return post;
	}

	private String getValueUsingXpath(OMElement template, String selector)
			throws AppFactoryException {
		String value = null;
		try {
			AXIOMXPath axiomxPath = new AXIOMXPath(selector);
			Object selectedObject = axiomxPath.selectSingleNode(template);

			if (selectedObject != null && selectedObject instanceof OMElement) {
				OMElement selectedElement = (OMElement) selectedObject;
				value = selectedElement.getText();
			} else {
				log.warn("Unable to find xml element matching selector : "
						+ selector);
			}

		} catch (Exception ex) {
			throw new AppFactoryException("Unable to set value to job config",
					ex);
		}
		return value;
	}

	public void setJobAutoDeployable(String jobName, boolean isAutoDeployable,
			String tenantDomain) throws AppFactoryException {

		OMElement configuration = getAutoDeployUpdatedConfiguration(jobName,
				isAutoDeployable, tenantDomain);
		OMElement tmpConfiguration = configuration.cloneOMElement();
		setConfiguration(jobName, tmpConfiguration, tenantDomain);
		if (log.isDebugEnabled()) {
			log.debug("Job : " + jobName + " successfully configured for auto deploying " + isAutoDeployable +
			         " in jenkins");
		}
	}

	private OMElement getAutoDeployUpdatedConfiguration(String jobName,
			boolean isAutoDeploy, String tenantDomain)
			throws AppFactoryException {
		OMElement configurations = null;

		GetMethod getFetchMethod = createGet(
				String.format("/job/%s/config.xml", jobName), null,
				tenantDomain);

		try {
			int httpStatusCode = getAuthenticatedHttpClient().executeMethod(getFetchMethod);

			if (!isSuccessfulStatusCode(httpStatusCode)) {
				httpStatusCode = resendRequest(getFetchMethod);
			}

			if (!isSuccessfulStatusCode(httpStatusCode)) {
				String errorMsg = String.format(
						"Unable to retrieve available config urls from "
								+ "jenkins for job %s. "
								+ "jenkins returned, http status : %d",
						jobName, httpStatusCode);
				log.error(errorMsg);
				throw new AppFactoryException(errorMsg);
			}

			StAXOMBuilder builder = new StAXOMBuilder(
					getFetchMethod.getResponseBodyAsStream());
			configurations = builder.getDocumentElement();

			String paramValue = null;
			if (isAutoDeploy) {
				paramValue = AppFactoryConstants.STRING_TRUE;
			} else {
				paramValue = AppFactoryConstants.STRING_FALSE;
			}

			AXIOMXPath axiomxPath = new AXIOMXPath(
					"//hudson.model.ParametersDefinitionProperty[1]/parameterDefinitions[1]/hudson.model.StringParameterDefinition[name='isAutomatic']/defaultValue");
			Object selectedObject = axiomxPath.selectSingleNode(configurations);

			if (selectedObject != null) {
				OMElement selectedNode = (OMElement) selectedObject;
				selectedNode.setText(paramValue);
			} else {
				AXIOMXPath axiomP = new AXIOMXPath(
						"//hudson.model.ParametersDefinitionProperty[1]/parameterDefinitions[1]");
				Object parameterDefsObject = axiomP
						.selectSingleNode(configurations);
				OMElement parameterDefsNode = (OMElement) parameterDefsObject;

				String payload = "<isAutomatic>" + paramValue
						+ "</isAutomatic>";
				OMElement triggerParam = AXIOMUtil.stringToOM(payload);
				parameterDefsNode.addChild(triggerParam);
			}

		} catch (Exception ex) {
			String errorMsg = String.format(
					"Unable to retrieve available jobs from jenkins : %s",
					ex.getMessage());
			log.error(errorMsg, ex);
			throw new AppFactoryException(errorMsg, ex);
		} finally {
			getFetchMethod.releaseConnection();
		}

		return configurations;
	}

	/**
	 * When jenkins tenant is unloaded the requests cannot be fulfilled. So this
	 * method will be used to resend the Get requests
	 * 
	 * @param method
	 *            method to be retried
	 * @return httpStatusCode
	 */
	private int resendRequest(GetMethod method) throws AppFactoryException {
		int httpStatusCode = -1;
		int retryCount = Integer.parseInt(AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(
				JenkinsCIConstants.JENKINS_CLIENT_RETRY_COUNT));
		int retryDelay = Integer.parseInt(AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(
				JenkinsCIConstants.JENKINS_CLIENT_RETRY_DELAY));
		log.info("Jenkins client retry count :" + retryCount + " and retry delay in seconds :" +
		         retryDelay + " for " + method.getQueryString());
		//TODO - Send mail to cloud
		try {
			// retry retryCount times to process the request
			for (int i = 0; i < retryCount; i++) {
				Thread.sleep(1000 * retryDelay); // sleep retryDelay seconds, giving jenkins
											// time to load the tenant
				if (log.isDebugEnabled()) {
					log.debug("Resending request(" + i + ") started for GET");
				}
				method.releaseConnection();
				httpStatusCode = getAuthenticatedHttpClient().executeMethod(method);
				// In the new jenkins release the response is always 201 or 302
				if (HttpStatus.SC_OK == httpStatusCode
						|| HttpStatus.SC_CREATED == httpStatusCode
						|| HttpStatus.SC_MOVED_TEMPORARILY == httpStatusCode) {
					if (log.isDebugEnabled()) {
						log.debug("Break resending since " + httpStatusCode);
					}
					break;
				}
				if (log.isDebugEnabled()) {
					log.debug("Resent GET request(" + i + ") failed with response code " + httpStatusCode);
				}
			}
		} catch (IOException e) {
			String errorMsg = "Error while resending the request";
			log.error(errorMsg, e);
			throw new AppFactoryException(errorMsg, e);
		} catch (InterruptedException e) {
			String errorMsg = "Error while resending the request";
			log.error(errorMsg, e);
			throw new AppFactoryException(errorMsg, e);
		}
		return httpStatusCode;
	}

	/**
	 * When jenkins tenant is unloaded the requests cannot be fulfilled. So this
	 * method will be used to resend the Post requests
	 * 
	 * @param method
	 *            method to be retried
	 * @return httpStatusCode
	 */
	private int resendRequest(PostMethod method) throws AppFactoryException {
		int httpStatusCode = -1;
		int retryCount = Integer.parseInt(AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(
				JenkinsCIConstants.JENKINS_CLIENT_RETRY_COUNT));
		int retryDelay = Integer.parseInt(AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(
				JenkinsCIConstants.JENKINS_CLIENT_RETRY_DELAY));
		log.info("Jenkins client retry count :" + retryCount + " and retry delay in seconds :"
			          + retryDelay + " for " + method.getRequestCharSet());
		try {
			// retry retryCount times to process the request
			for (int i = 0; i < retryCount; i++) {
				Thread.sleep(1000 * retryDelay); // sleep retryDelay seconds, giving jenkins
											// time to load the tenant
				if (log.isDebugEnabled()) {
					log.debug("Resending request(" + i + ") started for POST");
				}
				method.releaseConnection();
				httpStatusCode = getAuthenticatedHttpClient().executeMethod(method);
				// In the new jenkins release the response is always 201 or 302
				if (HttpStatus.SC_OK == httpStatusCode
						|| HttpStatus.SC_CREATED == httpStatusCode
						|| HttpStatus.SC_MOVED_TEMPORARILY == httpStatusCode) {
					if (log.isDebugEnabled()) {
						log.debug("Break resending since " + httpStatusCode);
					}
					break;
				}
				if (log.isDebugEnabled()) {
					log.debug("Resent POST request(" + i + ") failed with response code " + httpStatusCode);
				}
			}
		} catch (IOException e) {
			String errorMsg = "Error while resending the request";
			log.error(errorMsg, e);
			throw new AppFactoryException(errorMsg, e);
		} catch (InterruptedException e) {
			String errorMsg = "Error while resending the request";
			log.error(errorMsg, e);
			throw new AppFactoryException(errorMsg, e);
		}
		return httpStatusCode;
	}

	/**
	 * Add runtime specific parameters to the parameter map
	 * @param stage current stage of the application version
	 * @param parameters list of name value pair to sent to jenkins
	 * @param runtimeBean runtime bean that we need to add parameters from
	 */
	private void addRunTimeParameters(String stage, List<NameValuePair> parameters, RuntimeBean runtimeBean) {
		parameters.add(new NameValuePair(AppFactoryConstants.RUNTIME_DEPLOYER_CLASSNAME,
		                                 runtimeBean.getDeployerClassName()));
		parameters.add(new NameValuePair(AppFactoryConstants.RUNTIME_ALIAS_PREFIX,
		                                 runtimeBean.getAliasPrefix() + stage));
		parameters.add(new NameValuePair(AppFactoryConstants.RUNTIME_CARTRIDGE_TYPE_PREFIX,
		                                 runtimeBean.getCartridgeTypePrefix() + stage));
		parameters.add(new NameValuePair(AppFactoryConstants.PAAS_REPOSITORY_URL_PATTERN,
		                                 runtimeBean.getPaasRepositoryURLPattern()));
		parameters.add(new NameValuePair(AppFactoryConstants.RUNTIME_DEPLOYMENT_POLICY,
		                                 runtimeBean.getDeploymentPolicy()));
		parameters.add(new NameValuePair(AppFactoryConstants.RUNTIME_AUTOSCALE_POLICY,
		                                 runtimeBean.getAutoscalePolicy()));
		parameters.add(new NameValuePair(AppFactoryConstants.RUNTIME_DATA_CARTRIDGE_TYPE,
		                                 runtimeBean.getDataCartridgeType()));
		parameters.add(new NameValuePair(AppFactoryConstants.RUNTIME_DATA_CARTRIDGE_ALIAS,
		                                 runtimeBean.getDataCartridgeAlias()));
	}

	/**
	 * Check if the given status code is in 2xx range.
	 *
	 * @param httpStatusCode - status code to be checked
	 * @return true if status code is in 2xx range
	 */
	private boolean isSuccessfulStatusCode(int httpStatusCode) {
		return (httpStatusCode >= HttpStatus.SC_OK && httpStatusCode < 299);
	}

}
