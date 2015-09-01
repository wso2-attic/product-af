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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jaxen.JaxenException;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.beans.RuntimeBean;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.BuildDriverListener;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeBean;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeManager;
import org.wso2.carbon.appfactory.core.dao.ApplicationDAO;
import org.wso2.carbon.appfactory.core.dto.Statistic;
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

import javax.net.ssl.SSLContext;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Connects to a jenkins server using its 'Remote API'.
 */
public class RestBasedJenkinsCIConnector {

    private static final Log log = LogFactory.getLog(RestBasedJenkinsCIConnector.class);
    public static final String URL_SUFFIX_FORMAT_CREATE_JOB = "/job/%s/createItem";
    public static final long MILLISECONDS_PER_SECOND = 1000L;
    private static RestBasedJenkinsCIConnector restBasedJenkinsCIConnector;
    private static final int MAX_SUCCESS_HTTP_STATUS_CODE = 299;

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
     * Specify whether the host name need to be verified or not
     */
    private boolean allowAllHostNameVerifier;

    /**
     * Specify the max number of connection per route in httpclient
     */
    private int defaultMaxConnectionsPerRoute;

    /**
     * Specify the max number of total connection in httpclient
     */
    private int maxTotalConnections;

    /**
     * Returns an instance of RestBasedJenkinsCIConnector
     *
     * @return instance of RestBasedJenkinsCIConnector
     * @throws AppFactoryException when reading from appfactory.xml
     */
    public static RestBasedJenkinsCIConnector getInstance() throws AppFactoryException {
        return restBasedJenkinsCIConnector;
    }

    /**
     * Private constructor for singleton class
     *
     * @throws AppFactoryException when reading from appfactory.xml
     */
    private RestBasedJenkinsCIConnector() throws AppFactoryException {

        this.authenticate = Boolean.parseBoolean(AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(
                JenkinsCIConstants.AUTHENTICATE_CONFIG_SELECTOR));

        this.username = AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(
                JenkinsCIConstants.JENKINS_SERVER_ADMIN_USERNAME);

        this.apiKeyOrPassword = AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(
                JenkinsCIConstants.JENKINS_SERVER_ADMIN_PASSWORD);

        this.allowAllHostNameVerifier = Boolean.parseBoolean(
                AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(
                        JenkinsCIConstants.ALLOW_ALL_HOSTNAME_VERIFIER));

        this.defaultMaxConnectionsPerRoute = Integer.parseInt(
                AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(
                        JenkinsCIConstants.DEFAULT_MAX_CONNECTIONS_PER_ROUTE));

        this.maxTotalConnections = Integer.parseInt(
                AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(JenkinsCIConstants.MAX_TOTAL_CONNECTIONS));

        if (log.isDebugEnabled()) {
            log.debug(String.format("Authenticate : %s", this.authenticate));
            log.debug(String.format("Jenkins user name : %s", this.username));
        }

        ThreadSafeClientConnManager threadSafeClientConnManager = new ThreadSafeClientConnManager();
        threadSafeClientConnManager.setDefaultMaxPerRoute(this.defaultMaxConnectionsPerRoute);
        threadSafeClientConnManager.setMaxTotal(this.maxTotalConnections);
        this.httpClient = new DefaultHttpClient(threadSafeClientConnManager);
    }


    /**
     * Create the HttpContext and disable host verification
     *
     * @return
     * @throws AppFactoryException
     */
    public HttpContext getHttpContext() throws AppFactoryException {

        HttpContext httpContext = new BasicHttpContext();
        if (this.allowAllHostNameVerifier) {
            SSLContext sslContext;
            try {
                sslContext = SSLContext.getInstance(SSLSocketFactory.TLS);
                sslContext.init(null, null, null);
            } catch (KeyManagementException e) {
                String msg = "Error while initializing ssl context for http client";
                log.error(msg, e);
                throw new AppFactoryException(msg, e);
            } catch (NoSuchAlgorithmException e) {
                String msg = "Error while initializing ssl context for http client";
                log.error(msg, e);
                throw new AppFactoryException(msg, e);
            }
            SSLSocketFactory sf = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            Scheme sch = new Scheme("https", 443, sf);
            httpClient.getConnectionManager().getSchemeRegistry().register(sch);
        }
        return httpContext;
    }

    /**
     * Get jenkins URL by {@code tenantDomain}. Since we are using a bucket strategy to select jenkins cluster, and
     * bucket(therefore jenkins cluster) depends on the {@code tenantDomain}. correct {@code tenantDomain} should be
     * passed here.
     *
     * @param tenantDomain tenant Domain
     * @return jenkins url
     * @throws AppFactoryException
     */
    public String getJenkinsUrl(String tenantDomain) throws AppFactoryException {
        int tenantBucketId = ServiceContainer.getBucketSelectingStrategy().getTenantBucketId(tenantDomain);
        String bucketClusterId = ServiceContainer.getClusterSelectingStrategy().getBucketClusterId(tenantBucketId);
        if (StringUtils.isBlank(bucketClusterId)) {
            throw new IllegalArgumentException("Jenkins server url is unspecified for bucket:"+tenantBucketId);
        }
        return bucketClusterId;
    }

    /**
     * Create a job in Jenkins
     *
     * @param jobName      name of the job
     * @param jobParams    Job configuration parameters
     * @param tenantDomain Tenant domain of application
     * @throws AppFactoryException if an error occurs.
     */
    public void createJob(String jobName, Map<String, String> jobParams, String tenantDomain)
            throws AppFactoryException {

        OMElement jobConfiguration = new JobConfigurator(jobParams).configure(
                jobParams.get(JenkinsCIConstants.APPLICATION_EXTENSION));

        List<NameValuePair> queryParams = new ArrayList<NameValuePair>();
        queryParams.add(new BasicNameValuePair(AppFactoryConstants.JOB_NAME_KEY, jobName));
        HttpPost createJob;
        HttpResponse createJobResponse = null;

        try {
            createJob = createPost(String.format(URL_SUFFIX_FORMAT_CREATE_JOB, tenantDomain), queryParams,
                                   new StringEntity(jobConfiguration.toStringWithConsume(), "text/xml", "utf-8"),
                                   tenantDomain);
            createJobResponse = httpClient.execute(createJob, getHttpContext());
            int httpStatusCode = createJobResponse.getStatusLine().getStatusCode();

            if (!isSuccessfulStatusCode(httpStatusCode)) {
                createJobResponse = resendRequest(createJob, createJobResponse);
                httpStatusCode = createJobResponse.getStatusLine().getStatusCode();
            }

            if (!isSuccessfulStatusCode(httpStatusCode)) {
                String errorMsg =
                        "Unable to create the job : " + jobName + ". jenkins returned, http status : " + httpStatusCode;
                log.error(errorMsg);
                throw new AppFactoryException(errorMsg);
            }
        } catch (ClientProtocolException e) {
            String msg =
                    "Error while executing HttpPost method for creating job for job : " + jobName + " in tenant : " +
                    tenantDomain;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (IOException e) {
            String msg =
                    "Error while executing HttpPost method for creating job for job : " + jobName + " in tenant : " +
                    tenantDomain;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (XMLStreamException e) {
            String msg = "Error while converting OMElement to string when creating job for job : " + jobName +
                         " in tenant : " + tenantDomain;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } finally {
            if (createJobResponse != null) {
                try {
                    EntityUtils.consume(createJobResponse.getEntity());
                } catch (IOException e) {
                    String msg =
                            "Error while consuming the create job response for job : " + jobName + " in tenant : " +
                            tenantDomain;
                    log.error(msg, e);
                    throw new AppFactoryException(msg, e);
                }
            }
        }
    }

    /**
     * Create tenant job. This will create "Folder Job"(folder with the name of {@code jobName} in
     * $JENKINS_HOME/jobs directory) to represent the tenant in the jenkins
     *
     * @param jobName           job name.
     * @param jobConfiguration  job configuration
     * @param tenantDomain      tenant domain
     * @throws AppFactoryException
     */
    public void createTenantJob(String jobName, OMElement jobConfiguration,
                                String tenantDomain) throws AppFactoryException {


        List<NameValuePair> queryParams = new ArrayList<NameValuePair>();
        queryParams.add(new BasicNameValuePair(AppFactoryConstants.JOB_NAME_KEY, jobName));
        HttpPost createJob;
        HttpResponse createJobResponse = null;

        try {
            createJob = createPost(
                    "/createItem",
                    queryParams,
                    new StringEntity(jobConfiguration.toStringWithConsume(), "text/xml", "utf-8"),
                    tenantDomain);
            createJobResponse = httpClient.execute(createJob, getHttpContext());
            int httpStatusCode = createJobResponse.getStatusLine().getStatusCode();

            if (!isSuccessfulStatusCode(httpStatusCode)) {
                createJobResponse = resendRequest(createJob, createJobResponse);
                httpStatusCode = createJobResponse.getStatusLine().getStatusCode();
            }

            if (!isSuccessfulStatusCode(httpStatusCode)) {
                String errorMsg =
                        "Unable to create the job : " + jobName + ". jenkins returned, http status : " + httpStatusCode;
                log.error(errorMsg);
                throw new AppFactoryException(errorMsg);
            }
        } catch (ClientProtocolException e) {
            String msg =
                    "Error while executing HttpPost method for creating job for job : " + jobName + " in tenant : " +
                    tenantDomain;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (IOException e) {
            String msg =
                    "Error while executing HttpPost method for creating job for job : " + jobName + " in tenant : " +
                    tenantDomain;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (XMLStreamException e) {
            String msg = "Error while converting OMElement to string when creating job for job : " + jobName +
                         " in tenant : " + tenantDomain;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } finally {
            if (createJobResponse != null) {
                try {
                    EntityUtils.consume(createJobResponse.getEntity());
                } catch (IOException e) {
                    String msg =
                            "Error while consuming the create job response for job : " + jobName + " in tenant : " +
                            tenantDomain;
                    log.error(msg, e);
                    throw new AppFactoryException(msg, e);
                }
            }
        }
    }

    /**
     * Checks weather a job exists in Jenkins server
     *
     * @param applicationId id of the application
     * @param version       version of the application
     * @param tenantDomain  tenant domain, to which the application belongs
     * @return true if job exits, false otherwise.
     * @throws AppFactoryException if an error occurs.
     */
    public boolean isJobExists(String applicationId, String version, String tenantDomain) throws AppFactoryException {

        String jobName = ServiceHolder.getContinuousIntegrationSystemDriver().getJobName(applicationId, version, "");
        final String wrapperTag = "JobNames";

        List<NameValuePair> queryParameters = new ArrayList<NameValuePair>();
        queryParameters.add(new BasicNameValuePair("wrapper", wrapperTag));
        queryParameters.add(new BasicNameValuePair("xpath", String.format("/*/job/name[text()='%s']", jobName)));

        HttpGet checkJobExistsMethod = createGet("/job/"+tenantDomain+"/api/xml", queryParameters, tenantDomain);

        boolean isExists = false;
        HttpResponse jobExistResponse = null;

        try {
            jobExistResponse = httpClient.execute(checkJobExistsMethod, getHttpContext());
            int httpStatusCode = jobExistResponse.getStatusLine().getStatusCode();

            if (!isSuccessfulStatusCode(httpStatusCode)) {
                jobExistResponse = resendRequest(checkJobExistsMethod, jobExistResponse);
                httpStatusCode = jobExistResponse.getStatusLine().getStatusCode();
            }

            if (!isSuccessfulStatusCode(httpStatusCode)) {
                final String errorMsg =
                        "Unable to check the existence of job " + jobName + ". jenkins returned, http status : " +
                        httpStatusCode;
                log.error(errorMsg);
                throw new AppFactoryException(errorMsg);
            }

            StAXOMBuilder builder = new StAXOMBuilder(jobExistResponse.getEntity().getContent());
            isExists = builder.getDocumentElement().getChildElements().hasNext();

        } catch (XMLStreamException e) {
            String msg =
                    "Error while creating StAXOMBuilder using the response of isJobExists method for application : " +
                    applicationId + " version : " + version + " in tenant : " + tenantDomain;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (ClientProtocolException e) {
            String msg =
                    "Error while executing HttpGet method for checking availability of job for application : " +
                    applicationId + " version : " + version + " in tenant : " + tenantDomain;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (IOException e) {
            String msg =
                    "Error while executing HttpGet method for checking availability of job for application : " +
                    applicationId + " version : " + version + " in tenant : " + tenantDomain;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } finally {
            if (jobExistResponse != null) {
                try {
                    EntityUtils.consume(jobExistResponse.getEntity());
                } catch (IOException e) {
                    String msg = "Error while consuming the response for is job exist request for application : " +
                                 applicationId + " version : " + version + " in tenant : " + tenantDomain;
                    log.error(msg, e);
                    throw new AppFactoryException(msg, e);
                }
            }
        }
        return isExists;
    }

    /**
     * Deletes a job in jenkins
     *
     * @param jobName      name of the job
     * @param tenantDomain tenant domain, to which the application belongs
     * @return true if job exited on Jenkins and successfully deleted.
     * @throws AppFactoryException if an error occurs.
     */
    public boolean deleteJob(String jobName, String tenantDomain) throws AppFactoryException {

        HttpPost deleteJobMethod;
        deleteJobMethod = createPost(String.format("/job/"+tenantDomain+"/job/%s/doDelete", jobName), null, null, tenantDomain);
        int httpStatusCode = -1;
        HttpResponse deleteJobResponse = null;

        try {
            deleteJobResponse = httpClient.execute(deleteJobMethod, getHttpContext());
            httpStatusCode = deleteJobResponse.getStatusLine().getStatusCode();

            if (!isSuccessfulStatusCode(httpStatusCode) && HttpStatus.SC_MOVED_TEMPORARILY != httpStatusCode) {
                deleteJobResponse = resendRequest(deleteJobMethod, deleteJobResponse);
                httpStatusCode = deleteJobResponse.getStatusLine().getStatusCode();
            }

            if (!isSuccessfulStatusCode(httpStatusCode) && HttpStatus.SC_MOVED_TEMPORARILY != httpStatusCode) {
                final String msg =
                        "Unable to delete the job : " + jobName + ". jenkins returned http status : " + httpStatusCode;
                log.error(msg);
                throw new AppFactoryException(msg);
            }

        } catch (ClientProtocolException e) {
            String msg = "Error while executing HttpPost method for deleting the job : " + jobName + " in tenant : " +
                         tenantDomain;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (IOException e) {
            String msg = "Error while executing HttpPost method for deleting the job : " + jobName + " in tenant : " +
                         tenantDomain;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } finally {
            if (deleteJobResponse != null) {
                try {
                    EntityUtils.consume(deleteJobResponse.getEntity());
                } catch (IOException e) {
                    String msg = "Error while consuming delete job response for job : " + jobName + " in tenant : " +
                                 tenantDomain;
                    log.error(msg, e);
                    throw new AppFactoryException(msg, e);
                }
            }
        }
        return HttpStatus.SC_NOT_FOUND != httpStatusCode;
    }

    /**
     * Starts a build job available in Jenkins
     *
     * @param applicationId id of the application
     * @param version       version of the application
     * @param doDeploy      specifies whether the artifact need to be deployed or not
     * @param stageName     lifecycle stage of the application i.e: dev, prod, test
     * @param tagName
     * @param tenantDomain  tenant domain, to which the application belongs
     * @param userName      username of the user, who triggered the build
     * @param repoFrom      type of repository. i.e: master, fork
     * @throws AppFactoryException
     */
    public void startBuild(String applicationId, String version, boolean doDeploy, String stageName, String tagName,
                           String tenantDomain, String userName, String repoFrom) throws AppFactoryException {

        userName = MultitenantUtils.getTenantAwareUsername(userName);
        String jobName = ServiceHolder.getContinuousIntegrationSystemDriver().getJobName(applicationId, version,
                                                                                         userName, repoFrom);
        String artifactType = ApplicationDAO.getInstance().getApplicationType(applicationId);
        boolean isFreestyle = false;

        try {
            isFreestyle = AppFactoryCoreUtil.isFreestyleNonBuilableProject(artifactType);
        } catch (AppFactoryException e) {
            log.error("Error while checking whether the apptype is freestyle or not", e);
            //continue the flow as non free style app
        }

        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair(AppFactoryConstants.IS_AUTOMATIC, AppFactoryConstants.STRING_FALSE));
        parameters.add(new BasicNameValuePair(AppFactoryConstants.DO_DEPLOY, Boolean.toString(doDeploy)));
        parameters.add(new BasicNameValuePair(AppFactoryConstants.DEPLOY_STAGE, stageName));
        parameters.add(new BasicNameValuePair(AppFactoryConstants.BUILD_REPO_FROM, repoFrom));
        String tenantUserName = userName + UserCoreConstants.TENANT_DOMAIN_COMBINER + tenantDomain;
        parameters.add(new BasicNameValuePair(AppFactoryConstants.TENANT_USER_NAME, tenantUserName));

        if (tagName != null && !tagName.equals(AppFactoryConstants.EMPTY_STRING)) {
            parameters.add(new BasicNameValuePair(AppFactoryConstants.PERSIST_ARTIFACT, String.valueOf(true)));
            parameters.add(new BasicNameValuePair(AppFactoryConstants.TAG_NAME, tagName));
        } else {
            parameters.add(new BasicNameValuePair(AppFactoryConstants.PERSIST_ARTIFACT, String.valueOf(false)));
        }

        HttpPost startBuildMethod;
        startBuildMethod = createPost(String.format("/job/"+tenantDomain+"/job/%s/buildWithParameters", jobName), parameters, null,
                                      tenantDomain);

        int httpStatusCode;
        HttpResponse buildResponse = null;
        try {
            buildResponse = httpClient.execute(startBuildMethod, getHttpContext());
            httpStatusCode = buildResponse.getStatusLine().getStatusCode();

            if (!isSuccessfulStatusCode(httpStatusCode)) {
                buildResponse = resendRequest(startBuildMethod, buildResponse);
                httpStatusCode = buildResponse.getStatusLine().getStatusCode();
            }

            if (HttpStatus.SC_NOT_FOUND == httpStatusCode) {
                String repoType = ApplicationDAO.getInstance().getApplicationInfo(applicationId).getRepositoryType();
                RepositoryProvider repoProvider = Util.getRepositoryProvider(repoType);
                String repoURL;
                try {
                    repoURL = repoProvider.getAppRepositoryURL(applicationId, tenantDomain);
                } catch (RepositoryMgtException e) {
                    String msg =
                            "Error while creating repository url for application : " + applicationId + " version : " +
                            version + " repo from : " + repoFrom + "in tenant : " + tenantDomain;
                    log.error(msg, e);
                    throw new AppFactoryException(msg, e);
                }

                ServiceContainer.getJenkinsCISystemDriver()
                        .createJob(applicationId, version, "", tenantDomain, userName,
                                   repoURL, AppFactoryConstants.ORIGINAL_REPOSITORY);

                buildResponse = resendRequest(startBuildMethod, buildResponse);
                httpStatusCode = buildResponse.getStatusLine().getStatusCode();
            }

            if (!isSuccessfulStatusCode(httpStatusCode)) {
                String msg = "Error occurred with the http status code : " + httpStatusCode +
                             " while starting a build for application : " + applicationId + " version : " + version +
                             " in tenant : " + tenantDomain;
                log.error(msg);
                throw new AppFactoryException(msg);
            }

        } catch (ClientProtocolException e) {
            String msg =
                    "Error while executing HttpPost method for starting a build for application : " + applicationId +
                    " version : " + version + " in tenant : " + tenantDomain;
            log.error(msg, e);
            if (!isFreestyle) {
                try {
                    addWallMessage(applicationId, tenantUserName, repoFrom, version, false, userName);
                } catch (AppFactoryEventException e1) {
                    log.error("Error while notifying wall notification about the build failure for application : " +
                              applicationId + " version : " + version + " in tenant : " + tenantDomain, e1);
                }
            }
            throw new AppFactoryException(msg, e);
        } catch (IOException e) {
            String msg =
                    "Error while executing HttpPost method for starting a build for application : " + applicationId +
                    " version : " + version + " in tenant : " + tenantDomain;
            log.error(msg, e);
            if (!isFreestyle) {
                try {
                    addWallMessage(applicationId, tenantUserName, repoFrom, version, false, userName);
                } catch (AppFactoryEventException e1) {
                    log.error("Error while notifying wall notification about the build failure for application : " +
                              applicationId + " version : " + version + " in tenant : " + tenantDomain, e1);
                }
            }
            throw new AppFactoryException(msg, e);
        } finally {
            if (buildResponse != null) {
                try {
                    EntityUtils.consume(buildResponse.getEntity());
                } catch (IOException e) {
                    String msg =
                            "Error while consuming the Response of build request for application : " + applicationId +
                            " version : " + version + " in tenant : " + tenantDomain;
                    log.error(msg, e);
                    throw new AppFactoryException(msg, e);
                }
            }
        }

        if (!isFreestyle) {
            try {
                addWallMessage(applicationId, tenantUserName, repoFrom, version, true, userName);
            } catch (AppFactoryEventException e) {
                log.error("Error while notifying wall notification about  for application : " +
                          applicationId + " version : " + version + " in tenant : " + tenantDomain, e);
            }
        }

	    Iterator<BuildDriverListener> buildDriverListeners = ServiceContainer.getBuildDriverListeners().iterator();
	    while (buildDriverListeners.hasNext()) {
		    BuildDriverListener listener = buildDriverListeners.next();
		    listener.onBuildStart(applicationId, version, "", userName, repoFrom, tenantDomain);
	    }
    }


    /**
     * Method to send the build notification to the wall
     *
     * @param applicationId  id of the application
     * @param tenantUserName user's username with tenant domain
     * @param repoFrom       repository type. i.e: master, fork
     * @param version        version of the application
     * @param isBuildSuccess status of the build
     * @param userName       name of the user
     * @throws AppFactoryEventException
     */
    private void addWallMessage(String applicationId, String tenantUserName, String repoFrom, String version,
                                boolean isBuildSuccess, String userName) throws AppFactoryEventException {
        String repoType, infoMessage;
        Event.Category category;

        String correlationKey =
                applicationId + AppFactoryConstants.MINUS + tenantUserName + AppFactoryConstants.MINUS + repoFrom +
                AppFactoryConstants.MINUS + version;

        if (EventingConstants.ORIGINAL_REPO_FORM.equals(repoFrom)) {
            repoType = AppFactoryConstants.MASTER_REPO;
        } else {
            repoType = AppFactoryConstants.FORKED_REPO;
        }

        if (isBuildSuccess) {
            infoMessage = "Build started for " + version + " in " + repoType + " by " + userName;
            category = Event.Category.INFO;
        } else {
            infoMessage = "Unable to start build for " + version + " in " + repoType + " by " + userName;
            infoMessage.concat("\n Tenant domain: " + tenantUserName);
            category = Event.Category.ERROR;
        }

        EventNotifier.getInstance().notify(
                ContinousIntegrationEventBuilderUtil.buildTriggerBuildEvent(applicationId, repoFrom, userName,
                                                                            infoMessage, category, correlationKey));
    }


    /**
     * //TODO NEED TO CHANGE,
     * Method to get the status of the build
     *
     * @param buildUrl     url of the build
     * @param tenantDomain tenant domain, to which the application belongs
     * @return status of the given build
     * @throws AppFactoryException
     */
    public String getbuildStatus(String buildUrl, String tenantDomain) throws AppFactoryException {

        String buildStatus = AppFactoryConstants.BUILD_STATUS_UNKNOWN;
        HttpGet checkJobExistsMethod = createGetByBaseUrl(buildUrl,"api/xml", null);

        HttpResponse getBuildStatusResponse = null;
        try {
            getBuildStatusResponse = httpClient.execute(checkJobExistsMethod, getHttpContext());
            int httpStatusCode = getBuildStatusResponse.getStatusLine().getStatusCode();

            if (!isSuccessfulStatusCode(httpStatusCode)) {
                getBuildStatusResponse = resendRequest(checkJobExistsMethod, getBuildStatusResponse);
                httpStatusCode = getBuildStatusResponse.getStatusLine().getStatusCode();
            }

            if (!isSuccessfulStatusCode(httpStatusCode)) {
                final String errorMsg =
                        "Unable to check the status of build : " + buildUrl + ". jenkins returned, http status : " +
                        httpStatusCode;
                log.error(errorMsg);
                throw new AppFactoryException(errorMsg);
            }

            StAXOMBuilder builder = new StAXOMBuilder(getBuildStatusResponse.getEntity().getContent());
            OMElement resultElement = builder.getDocumentElement();

            if (resultElement != null) {
                if (AppFactoryConstants.STRING_FALSE.equals(getValueUsingXpath(resultElement, "/*/building"))) {
                    buildStatus = getValueUsingXpath(resultElement, "/*/result");
                } else {
                    buildStatus = AppFactoryConstants.BUILD_STATUS_BUILDING;
                }
            }

        } catch (XMLStreamException e) {
            String msg = "Error while building StAXOMBuilder for the response of build status request of build url : " +
                         buildUrl + " in tenant : " + tenantDomain;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (ClientProtocolException e) {
            String msg =
                    "Error while executing HttpGet method for getting the build status of build url : " + buildUrl +
                    " in tenant : " + tenantDomain;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (IOException e) {
            String msg =
                    "Error while executing HttpGet method for getting the build status of build url : " + buildUrl +
                    " in tenant : " + tenantDomain;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } finally {
            if (getBuildStatusResponse != null) {
                try {
                    EntityUtils.consume(getBuildStatusResponse.getEntity());
                } catch (IOException e) {
                    String msg = "Error while consuming get build status response for build url : " + buildUrl +
                                 " in tenant : " + tenantDomain;
                    log.error(msg, e);
                    throw new AppFactoryException(msg, e);
                }
            }
        }
        return buildStatus;
    }

    /**
     * Method to get the list of build urls for a given job
     *
     * @param jobName      name of the job
     * @param tenantDomain tenant domain, to which the application belongs
     * @return the list of build urls for a specifig job
     * @throws AppFactoryException
     */
    public List<String> getBuildUrls(String jobName, String tenantDomain) throws AppFactoryException {

        List<String> listOfUrls = new ArrayList<String>();
        final String wrapperTag = "Builds";
        List<NameValuePair> queryParameters = new ArrayList<NameValuePair>();
        queryParameters.add(new BasicNameValuePair(AppFactoryConstants.WRAPPER_TAG_KEY, wrapperTag));
        queryParameters.add(new BasicNameValuePair(AppFactoryConstants.XPATH_EXPRESSION_KEY, "/*/build/url"));

        HttpGet getBuildsMethod = createGet(String.format("/job/%s/job/%s/api/xml", tenantDomain, jobName), queryParameters, tenantDomain);
        HttpResponse getBuildUrlResponse = null;

        try {
            getBuildUrlResponse = httpClient.execute(getBuildsMethod, getHttpContext());
            int httpStatusCode = getBuildUrlResponse.getStatusLine().getStatusCode();

            if (!isSuccessfulStatusCode(httpStatusCode)) {
                getBuildUrlResponse = resendRequest(getBuildsMethod, getBuildUrlResponse);
                httpStatusCode = getBuildUrlResponse.getStatusLine().getStatusCode();
            }

            if (!isSuccessfulStatusCode(httpStatusCode)) {
                String errorMsg = "Unable to retrieve available build urls from jenkins for job : " + jobName +
                                  ". jenkins returned, http status : " + httpStatusCode;
                log.error(errorMsg);
                throw new AppFactoryException(errorMsg);
            }

            StAXOMBuilder builder = new StAXOMBuilder(getBuildUrlResponse.getEntity().getContent());
            @SuppressWarnings("unchecked")
            Iterator<OMElement> urlElementsIte = builder.getDocumentElement().getChildElements();
            while (urlElementsIte.hasNext()) {
                OMElement urlElement = urlElementsIte.next();
                listOfUrls.add(urlElement.getText());
            }

        } catch (XMLStreamException e) {
            String msg = "Error while building StAXOMBuilder for get build url response for job : " + jobName +
                         " in tenant : " + tenantDomain;
            log.error(msg, e);
        } catch (ClientProtocolException e) {
            String msg =
                    "Error while executing HttpGet method for getting the build urls of job : " + jobName +
                    " in tenant : " + tenantDomain;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (IOException e) {
            String msg =
                    "Error while executing HttpGet method for getting the build urls of job : " + jobName +
                    " in tenant : " + tenantDomain;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } finally {
            if (getBuildUrlResponse != null) {
                try {
                    EntityUtils.consume(getBuildUrlResponse.getEntity());
                } catch (IOException e) {
                    String msg = "Error while consuming get build url response for job : " + jobName + " in tenant : " +
                                 tenantDomain;
                    log.error(msg, e);
                    throw new AppFactoryException(msg, e);
                }
            }
        }
        return listOfUrls;
    }

    /**
     * Returns the over all load of the jenkins server
     *
     * @param tenantDomain tenant domain
     * @return
     * @throws AppFactoryException
     */
    public List<Statistic> getOverallLoad(String tenantDomain) throws AppFactoryException {

        HttpGet overallLoad = createGet("/overallLoad/api/xml", null, tenantDomain);
        List<Statistic> list = new ArrayList<Statistic>();
        HttpResponse getOverallLoadResponse = null;

        try {
            getOverallLoadResponse = httpClient.execute(overallLoad, getHttpContext());
            int httpStatusCode = getOverallLoadResponse.getStatusLine().getStatusCode();

            if (!isSuccessfulStatusCode(httpStatusCode)) {
                getOverallLoadResponse = resendRequest(overallLoad, getOverallLoadResponse);
                httpStatusCode = getOverallLoadResponse.getStatusLine().getStatusCode();
            }

            if (!isSuccessfulStatusCode(httpStatusCode)) {
                final String msg = "Unable to check the overall load of jenkins in tenant : " + tenantDomain +
                                   ". jenkins returned, http status : " + httpStatusCode;
                log.error(msg);
                throw new AppFactoryException(msg);
            }

            StAXOMBuilder builder = new StAXOMBuilder(getOverallLoadResponse.getEntity().getContent());
            @SuppressWarnings("unchecked")
            Iterator<OMElement> elementIterator = (Iterator<OMElement>) builder.getDocumentElement().getChildElements();

            while (elementIterator.hasNext()) {
                OMElement statElement = elementIterator.next();
                String value = StringUtils.isEmpty(statElement.getText()) ? "-1" : statElement.getText();
                Statistic stat = new Statistic(statElement.getLocalName(), value);
                list.add(stat);
            }

        } catch (XMLStreamException e) {
            String msg = "Error while building StAXOMBuilder for get overall load response in tenant : " + tenantDomain;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (ClientProtocolException e) {
            String msg =
                    "Error while executing HttpGet method for getting the overall load in tenant : " + tenantDomain;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (IOException e) {
            String msg =
                    "Error while executing HttpGet method for getting the overall load in tenant : " + tenantDomain;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } finally {
            if (getOverallLoadResponse != null) {
                try {
                    EntityUtils.consume(getOverallLoadResponse.getEntity());
                } catch (IOException e) {
                    String msg = "Error while consuming the get overall load response for tenant : " + tenantDomain;
                    log.error(msg, e);
                    throw new AppFactoryException(msg, e);
                }
            }
        }
        return list;
    }

    /**
     * This method will call jenkins to deploy the latest successfully built
     * artifact of the given job name
     *
     * @param jobName      job name of which the artifact is going to get deployed
     * @param artifactType artifact type (car/war) that is going to get deployed
     * @param stage        server Urls that we need to deploy the artifact into
     * @param tenantDomain tenant domain, to which the job belongs to
     * @param userName     user name of the user, who triggered the deployement
     * @param deployAction
     * @param repoFrom     specifies the repository type. i.e: master, fork
     * @throws AppFactoryException
     */
    public void deployLatestSuccessArtifact(String jobName, String artifactType, String stage, String tenantDomain,
                                            String userName, String deployAction, String repoFrom)
            throws AppFactoryException {

        String deployLatestSuccessArtifactUrl = "/plugin/appfactory-plugin/deployLatestSuccessArtifact";

        HttpPost deployLatestSuccessArtifactMethod;
        HttpResponse deployResponse = null;

        try {
            List<NameValuePair> parameters = new ArrayList<NameValuePair>();
            parameters.add(new BasicNameValuePair(AppFactoryConstants.ARTIFACT_TYPE, artifactType));
            ApplicationTypeBean applicationTypeBean = ApplicationTypeManager.getInstance().getApplicationTypeBean(
                    artifactType);

            if (applicationTypeBean == null) {
                throw new AppFactoryException(
                        "Application Type details cannot be found for Artifact Type : " + artifactType +
                        " , Job Name : " + jobName + ", stage : " + stage + " for username: " + userName);
            }

            String runtimeNameForAppType = applicationTypeBean.getRuntimes()[0];
            RuntimeBean runtimeBean = RuntimeManager.getInstance().getRuntimeBean(runtimeNameForAppType);

            if (runtimeBean == null) {
                throw new AppFactoryException(
                        "Runtime details cannot be found for Artifact Type : " + artifactType + " , Job Name : " +
                        jobName + ", stage : " + stage + " for username: " + userName);
            }

            AppFactoryConfiguration appfactoryConfiguration = AppFactoryUtil.getAppfactoryConfiguration();

            String paasRepositoryProviderClassName = appfactoryConfiguration.getFirstProperty(
                    AppFactoryConstants.PAAS_ARTIFACT_REPO_PROVIDER_CLASS_NAME);

            parameters.add(new BasicNameValuePair(AppFactoryConstants.TENANT_DOMAIN,tenantDomain));
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            parameters.add(new BasicNameValuePair(AppFactoryConstants.TENANT_ID,Integer.toString(tenantId)));
            parameters.add(new BasicNameValuePair(AppFactoryConstants.RUNTIME_NAME_FOR_APPTYPE, runtimeNameForAppType));
            parameters.add(new BasicNameValuePair(AppFactoryConstants.JOB_NAME, jobName));
            parameters.add(new BasicNameValuePair(AppFactoryConstants.DEPLOY_STAGE, stage));
            parameters.add(new BasicNameValuePair(AppFactoryConstants.DEPLOY_ACTION, deployAction));
            parameters.add(new BasicNameValuePair(AppFactoryConstants.PAAS_ARTIFACT_REPO_PROVIDER_CLASS_NAME,
                                                  paasRepositoryProviderClassName));

	        addAppTypeParameters(parameters, applicationTypeBean);
            addRunTimeParameters(stage, parameters, runtimeBean);
	        parameters.add(new BasicNameValuePair(AppFactoryConstants.REPOSITORY_FROM, repoFrom));
	        String tenantUserName = userName + UserCoreConstants.TENANT_DOMAIN_COMBINER + tenantDomain;
	        parameters.add(new BasicNameValuePair(AppFactoryConstants.TENANT_USER_NAME, tenantUserName));

            deployLatestSuccessArtifactMethod = createPost(deployLatestSuccessArtifactUrl, parameters, null,
                                                           tenantDomain);
            deployResponse = httpClient.execute(deployLatestSuccessArtifactMethod, getHttpContext());
            int httpStatusCode = deployResponse.getStatusLine().getStatusCode();

            if (!isSuccessfulStatusCode(httpStatusCode)) {
                deployResponse = resendRequest(deployLatestSuccessArtifactMethod, deployResponse);
                httpStatusCode = deployResponse.getStatusLine().getStatusCode();
            }

            log.info("status code for deploy latest success artifact type : " + artifactType + " job name : " +
                     jobName + " stage : " + stage + " in tenant : " + tenantDomain + " is " + httpStatusCode);
            if (!isSuccessfulStatusCode(httpStatusCode)) {
                String errorMsg = "Unable to deploy the latest success artifact. jenkins "
                                  + "returned, http status : " + httpStatusCode;
                log.error(errorMsg);
                throw new AppFactoryException(errorMsg);
            }

        } catch (ClientProtocolException e) {
            String msg =
                    "Error while executing HttpPost method for deploying the latest successful artifact for job : " +
                    jobName + " artifact type : " + artifactType + " in stage : " + stage + " in tenant : " +
                    tenantDomain;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (IOException e) {
            String msg =
                    "Error while executing HttpPost method for deploying the latest successful artifact for job : " +
                    jobName + " artifact type : " + artifactType + " in stage : " + stage + " in tenant : " +
                    tenantDomain;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } finally {
            if (deployResponse != null) {
                try {
                    EntityUtils.consume(deployResponse.getEntity());
                } catch (IOException e) {
                    String msg = "Error while consuming deploy latest success artifact response for job : " + jobName +
                                 " of user : " + userName + " in tenant : " + tenantDomain;
                    log.error(msg, e);
                    throw new AppFactoryException(msg, e);
                }
            }
        }

    }

	/**
	 * Add application type parameters to the map
	 *
	 * @param parameters parameter map to send to the jenkins
	 * @param applicationTypeBean application type bean object
	 */
	private void addAppTypeParameters(List<NameValuePair> parameters, ApplicationTypeBean applicationTypeBean) {
		parameters.add(new BasicNameValuePair(AppFactoryConstants.APPLICATION_EXTENSION,
		                                      applicationTypeBean.getExtension()));
		parameters.add(new BasicNameValuePair(AppFactoryConstants.DEPLOYER_CLASSNAME,
		                                      applicationTypeBean.getDeployerClassName()));
		parameters.add(new BasicNameValuePair(AppFactoryConstants.SERVER_DEPLOYMENT_PATHS,
		                                      applicationTypeBean.getServerDeploymentPath()));
	}

	/**
     * deploy the promoted artifact
     *
     * @param jobName      name of the job
     * @param artifactType type of the artifact, which need to be deployed
     * @param stage        environment stage, to which the artifact need to be deployed
     * @param tenantDomain tenant domain, to which the application is belongs
     * @param userName     user name of the user, who promoted the application
     * @throws AppFactoryException
     */
    public void deployPromotedArtifact(String jobName, String artifactType, String stage, String tenantDomain,
                                       String userName)
            throws AppFactoryException {

        String deployPromotedArtifactUrl = "/plugin/appfactory-plugin/deployPromotedArtifact";
        ApplicationTypeBean applicationTypeBean =
                ApplicationTypeManager.getInstance().getApplicationTypeBean(artifactType);

        if (applicationTypeBean == null) {
            throw new AppFactoryException(
                    "Application Type details cannot be found for job : " + jobName + " artifact type : " +
                    artifactType + " in tenant : " + tenantDomain);
        }

        String runtimeNameForAppType = applicationTypeBean.getRuntimes()[0];
        RuntimeBean runtimeBean = RuntimeManager.getInstance().getRuntimeBean(runtimeNameForAppType);

        if (runtimeBean == null) {
            throw new AppFactoryException("Runtime details cannot be found");
        }

        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair(AppFactoryConstants.TENANT_DOMAIN,tenantDomain));
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        parameters.add(new BasicNameValuePair(AppFactoryConstants.TENANT_ID,Integer.toString(tenantId)));
        parameters.add(new BasicNameValuePair(AppFactoryConstants.JOB_NAME, jobName));
        parameters.add(new BasicNameValuePair(AppFactoryConstants.ARTIFACT_TYPE, artifactType));
        parameters.add(new BasicNameValuePair(AppFactoryConstants.RUNTIME_NAME_FOR_APPTYPE,
                                              runtimeNameForAppType));
        parameters.add(new BasicNameValuePair(AppFactoryConstants.DEPLOY_STAGE, stage));
        String tenantUserName = userName + UserCoreConstants.TENANT_DOMAIN_COMBINER + tenantDomain;
        parameters.add(new BasicNameValuePair(AppFactoryConstants.TENANT_USER_NAME, tenantUserName));

	    addAppTypeParameters(parameters, applicationTypeBean);
        addRunTimeParameters(stage, parameters, runtimeBean);

        HttpPost deployPromotedArtifactMethod;
        HttpResponse deployResponse = null;
        deployPromotedArtifactMethod = createPost(deployPromotedArtifactUrl, parameters, null, tenantDomain);

        try {
            deployResponse = httpClient.execute(deployPromotedArtifactMethod, getHttpContext());
            int httpStatusCode = deployResponse.getStatusLine().getStatusCode();

            if (!isSuccessfulStatusCode(httpStatusCode)) {
                deployResponse = resendRequest(deployPromotedArtifactMethod, deployResponse);
                httpStatusCode = deployResponse.getStatusLine().getStatusCode();
            }

            if (!isSuccessfulStatusCode(httpStatusCode)) {
                String errorMsg = "Unable to deploy the promoted artifact for job "
                                  + jobName
                                  + ". jenkins returned, http status : "
                                  + httpStatusCode;
                log.error(errorMsg);
                throw new AppFactoryException(errorMsg);
            }
        } catch (ClientProtocolException e) {
            String msg =
                    "Error while executing HttpPost method for deploying the promoted artifact for job : " +
                    jobName + " artifact type : " + artifactType + " in stage : " + stage + " in tenant : " +
                    tenantDomain;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (IOException e) {
            String msg =
                    "Error while executing HttpPost method for deploying the promoted artifact for job : " +
                    jobName + " artifact type : " + artifactType + " in stage : " + stage + " in tenant : " +
                    tenantDomain;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } finally {
            if (deployResponse != null) {
                try {
                    EntityUtils.consume(deployResponse.getEntity());
                } catch (IOException e) {
                    String msg = "Error while consuming deploy promoted artifact response for job : " + jobName +
                                 " of user : " + userName + " in tenant : " + tenantDomain;
                    log.error(msg, e);
                    throw new AppFactoryException(msg, e);
                }
            }
        }
    }

    /**
     * This will extract pre configured mvn repo to tenant
     * @param tenantDomain
     * @throws AppFactoryException
     */
    public void extractMvnRepo(String tenantDomain) throws AppFactoryException {
        String extractMvnRepoUrl = "/plugin/appfactory-plugin/extractMvnRepo";
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair(AppFactoryConstants.TENANT_DOMAIN, tenantDomain));
        HttpResponse extractResponse = null;
        HttpPost extractMvnRepoMethod = createPost(
                extractMvnRepoUrl,
                parameters, null,
                tenantDomain);
        try {
            extractResponse = httpClient.execute(extractMvnRepoMethod, getHttpContext());
            int httpStatusCode = extractResponse.getStatusLine().getStatusCode();

            if (!isSuccessfulStatusCode(httpStatusCode)) {
                extractResponse = resendRequest(extractMvnRepoMethod, extractResponse);
                httpStatusCode = extractResponse.getStatusLine().getStatusCode();
            }

            if (!isSuccessfulStatusCode(httpStatusCode)) {
                String errorMsg = "Unable to extract pre configured maven repo for tenant: "+ tenantDomain
                                  + ". jenkins returned, http status : "
                                  + httpStatusCode;
                log.error(errorMsg);
                throw new AppFactoryException(errorMsg);
            }
        } catch (ClientProtocolException e) {
            String msg =
                    "Error while executing HttpPost method for extract pre configured maven repo for tenant: "+
                    tenantDomain;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (IOException e) {
            String msg =
                    "Error while executing HttpPost method for extract pre configured maven repo for tenant: "+
                    tenantDomain;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } finally {
            if (extractResponse != null) {
                try {
                    EntityUtils.consume(extractResponse.getEntity());
                } catch (IOException e) {
                    String msg = "Error while consuming  extract pre configured maven repo for tenant: "+
                                 tenantDomain;
                    log.error(msg, e);
                    throw new AppFactoryException(msg, e);
                }
            }
        }
    }

    /**
     * Method to set an application auto buildable
     *
     * @param jobName        name of the job
     * @param repositoryType repository type
     * @param isAutoBuild    specify whether the application is auto buildable or not
     * @param pollingPeriod
     * @param tenantDomain   tenant domain, to which the application is belongs
     * @throws AppFactoryException
     */
    public void setJobAutoBuildable(String jobName, String repositoryType, boolean isAutoBuild, int pollingPeriod,
                                    String tenantDomain) throws AppFactoryException {
        OMElement configuration = getAutoBuildUpdatedConfiguration(jobName, repositoryType, isAutoBuild, pollingPeriod,
                                                                   tenantDomain);
        OMElement tmpConfiguration = configuration.cloneOMElement();
        setConfiguration(jobName, tmpConfiguration, tenantDomain);
        if (log.isDebugEnabled()) {
            log.debug("Job : " + jobName + " successfully configured for auto building " + isAutoBuild + " in jenkins");
        }
    }

    /**
     * Method to get the auto build updated configuration
     *
     * @param jobName        name of the job
     * @param repositoryType repository type
     * @param isAutoBuild    specify whether the application is auto buildable or not
     * @param pollingPeriod
     * @param tenantDomain   tenant domain, to which the application is belongs
     * @return
     * @throws AppFactoryException
     */
    private OMElement getAutoBuildUpdatedConfiguration(String jobName, String repositoryType, boolean isAutoBuild,
                                                       int pollingPeriod, String tenantDomain)
            throws AppFactoryException {

        HttpGet getFetchMethod = createGet(String.format("/job/%s/job/%s/config.xml", tenantDomain, jobName), null, tenantDomain);
        OMElement configurations = null;
        HttpResponse buildUpdateResponse = null;

        try {

            buildUpdateResponse = httpClient.execute(getFetchMethod, getHttpContext());
            int httpStatusCode = buildUpdateResponse.getStatusLine().getStatusCode();

            if (!isSuccessfulStatusCode(httpStatusCode)) {
                buildUpdateResponse = resendRequest(getFetchMethod, buildUpdateResponse);
                httpStatusCode = buildUpdateResponse.getStatusLine().getStatusCode();
            }

            if (!isSuccessfulStatusCode(httpStatusCode)) {
                String errorMsg = "Unable to retrieve available config urls from jenkins for job : " + jobName +
                                  ". jenkins returned, http status : " + httpStatusCode;
                log.error(errorMsg);
                throw new AppFactoryException(errorMsg);
            }

            StAXOMBuilder builder = new StAXOMBuilder(buildUpdateResponse.getEntity().getContent());
            configurations = builder.getDocumentElement();

            AXIOMXPath axiomxPath = new AXIOMXPath("//triggers");
            Object selectedObject = axiomxPath.selectSingleNode(configurations);
            if (isAutoBuild) {

                if (selectedObject != null) {
                    OMElement selectedNode = (OMElement) selectedObject;
                    selectedNode.detach();
                }

                StringBuilder payload = new StringBuilder(
                        "<triggers class=\"vector\">" + "<hudson.triggers.SCMTrigger>");
                if ("git".equals(repositoryType)) {
                    payload = payload.append("<spec></spec>");
                } else {
                    payload = payload.append("<spec>*/" + pollingPeriod + " * * * *</spec>");
                }
                payload = payload.append("</hudson.triggers.SCMTrigger>" + "</triggers>");
                OMElement triggerParam = AXIOMUtil.stringToOM(payload.toString());
                configurations.addChild(triggerParam);

            } else {
                if (selectedObject != null) {
                    OMElement selectedNode = (OMElement) selectedObject;
                    selectedNode.detach();
                }
            }

        } catch (XMLStreamException e) {
            String msg = "Error while building StAXOMBuilder for get auto build configuration response for job : " +
                         jobName + " in tenant : " + tenantDomain;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (JaxenException e) {
            String msg =
                    "Error while creating AXIOMXPath when getting the auto build updated configuration for job : " +
                    jobName + " in tenant : " + tenantDomain;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (ClientProtocolException e) {
            String msg =
                    "Error while executing HttpGet method for getting the auto build updated configuration for job : " +
                    jobName + " in tenant : " + tenantDomain;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (IOException e) {
            String msg =
                    "Error while executing HttpGet method for getting the auto build updated configuration for job : " +
                    jobName + " in tenant : " + tenantDomain;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } finally {
            if (buildUpdateResponse != null) {
                try {
                    EntityUtils.consume(buildUpdateResponse.getEntity());
                } catch (IOException e) {
                    String msg =
                            "Error while consuming get auto build updated configuration response for job : " + jobName +
                            " for repository type : " + repositoryType + " in tenant : " + tenantDomain;
                    log.error(msg, e);
                    throw new AppFactoryException(msg, e);
                }
            }
        }
        return configurations;
    }


    /**
     * update the job configuration
     *
     * @param jobName          job name of which we need to update the configuration of
     * @param jobConfiguration new configurations that needs to be set
     * @param tenantDomain     tenant domain, to which the application is belongs
     * @throws AppFactoryException
     */
    private void setConfiguration(String jobName, OMElement jobConfiguration, String tenantDomain)
            throws AppFactoryException {

        List<NameValuePair> queryParams = new ArrayList<NameValuePair>();
        queryParams.add(new BasicNameValuePair(AppFactoryConstants.JOB_NAME_KEY, jobName));
        HttpPost createJob;
        HttpResponse setConfigurationResponse = null;

        try {
            createJob = createPost(String.format("/job/%s/job/%s/config.xml",tenantDomain, jobName), queryParams,
                                   new StringEntity(jobConfiguration.toStringWithConsume(), "text/xml", "utf-8"),
                                   tenantDomain);
            setConfigurationResponse = httpClient.execute(createJob, getHttpContext());
            int httpStatusCode = setConfigurationResponse.getStatusLine().getStatusCode();

            if (!isSuccessfulStatusCode(httpStatusCode)) {
                setConfigurationResponse = resendRequest(createJob, setConfigurationResponse);
                httpStatusCode = setConfigurationResponse.getStatusLine().getStatusCode();
            }

            if (!isSuccessfulStatusCode(httpStatusCode)) {
                String errorMsg = String.format(
                        "Unable to set configuration: [%s]. jenkins "
                        + "returned, http status : %d", jobName,
                        httpStatusCode);
                log.error(errorMsg);
                throw new AppFactoryException(errorMsg);
            }

        } catch (ClientProtocolException e) {
            String msg =
                    "Error while executing HttpGet method for setting configuration for job : " + jobName +
                    " in tenant : " + tenantDomain;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (IOException e) {
            String msg =
                    "Error while executing HttpGet method for setting configuration for job : " + jobName +
                    " in tenant : " + tenantDomain;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (XMLStreamException e) {
            String msg = "Error while converting OMElement to string when creating job for job : " + jobName +
                         " in tenant : " + tenantDomain;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } finally {
            if (setConfigurationResponse != null) {
                try {
                    EntityUtils.consume(setConfigurationResponse.getEntity());
                } catch (IOException e) {
                    String msg =
                            "Error while consuming set configuration response for job : " + jobName + " in tenant : " +
                            tenantDomain;
                    log.error(msg, e);
                    throw new AppFactoryException(msg, e);
                }
            }
        }

    }

    /**
     * Set the application auto deployable in jenkins
     *
     * @param jobName          name of the job
     * @param isAutoDeployable specifies whether the application need to auto deploy or not
     * @param tenantDomain     tenant domain, to which the application belongs
     * @throws AppFactoryException
     */
    public void setJobAutoDeployable(String jobName, boolean isAutoDeployable, String tenantDomain)
            throws AppFactoryException {

        OMElement configuration = getAutoDeployUpdatedConfiguration(jobName, isAutoDeployable, tenantDomain);
        OMElement tmpConfiguration = configuration.cloneOMElement();
        setConfiguration(jobName, tmpConfiguration, tenantDomain);
        if (log.isDebugEnabled()) {
            log.debug("Job : " + jobName + " successfully configured for auto deploying " + isAutoDeployable +
                      " in jenkins");
        }
    }

    /**
     * Method to get the auto deploy updated configuration
     *
     * @param jobName      name of the job
     * @param isAutoDeploy specifies whether the application need to be auto deployed or not
     * @param tenantDomain tenant domain, to which the application belongs
     * @return
     * @throws AppFactoryException
     */
    private OMElement getAutoDeployUpdatedConfiguration(String jobName, boolean isAutoDeploy, String tenantDomain)
            throws AppFactoryException {

        OMElement configurations = null;
        HttpResponse autoDeployUpdateResponse = null;
        int httpStatusCode;

        HttpGet getFetchMethod = createGet(String.format("/job/%s/job/%s/config.xml",tenantDomain, jobName), null, tenantDomain);

        try {
            autoDeployUpdateResponse = httpClient.execute(getFetchMethod, getHttpContext());
            httpStatusCode = autoDeployUpdateResponse.getStatusLine().getStatusCode();

            if (!isSuccessfulStatusCode(httpStatusCode)) {
                autoDeployUpdateResponse = resendRequest(getFetchMethod, autoDeployUpdateResponse);
                httpStatusCode = autoDeployUpdateResponse.getStatusLine().getStatusCode();
            }

            if (!isSuccessfulStatusCode(httpStatusCode)) {
                String errorMsg = "Unable to retrieve available config urls from jenkins for job : " + jobName +
                                  ". jenkins returned, http status : " + httpStatusCode;
                log.error(errorMsg);
                throw new AppFactoryException(errorMsg);
            }

            StAXOMBuilder builder = new StAXOMBuilder(autoDeployUpdateResponse.getEntity().getContent());
            configurations = builder.getDocumentElement();

            String paramValue;
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
                Object parameterDefsObject = axiomP.selectSingleNode(configurations);
                OMElement parameterDefsNode = (OMElement) parameterDefsObject;

                String payload = "<isAutomatic>" + paramValue + "</isAutomatic>";
                OMElement triggerParam = AXIOMUtil.stringToOM(payload);
                parameterDefsNode.addChild(triggerParam);
            }

        } catch (XMLStreamException e) {
            String errorMsg =  "Unable to retrieve available jobs from jenkins";
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        } catch (JaxenException e) {
            String errorMsg =  "Error occurred while updating the job configuration for parameter \"isAutomatic\" for" +
                               " tenant: "+tenantDomain;
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        } catch (IOException e) {
            String errorMsg =  "Unable to retrieve available jobs from jenkins for tenant: "+tenantDomain;
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        } finally {
            if (autoDeployUpdateResponse != null) {
                try {
                    EntityUtils.consume(autoDeployUpdateResponse.getEntity());
                } catch (IOException e) {
                    String msg =
                            "Error while consuming auto deploy update configuration response for job : " + jobName +
                            " in tenant : " + tenantDomain;
                    log.error(msg, e);
                    throw new AppFactoryException(msg, e);
                }
            }
        }

        return configurations;
    }

    private HttpGet createGetByBaseUrl(String baseUrl, String relativePath,
                                       List<NameValuePair> queryParameters) throws AppFactoryException {
        String query = null;
        HttpGet get;

        if (queryParameters != null) {
            query = URLEncodedUtils.format(queryParameters, HTTP.UTF_8);
        }
        try {
            URL url = new URL(baseUrl);
            URI uri = URIUtils.createURI(url.getProtocol(), url.getHost(), url.getPort(), relativePath, query, null);

            get = new HttpGet(uri);
            if (authenticate) {
                get.addHeader(
                        BasicScheme.authenticate(new UsernamePasswordCredentials(this.username, this.apiKeyOrPassword),
                                                 HTTP.UTF_8, false));
            }

        } catch (MalformedURLException e) {
            String msg = "Error while generating URL for the path : " + baseUrl +
                         " during the creation of HttpGet method";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (URISyntaxException e) {
            String msg =
                    "Error while constructing the URI for url : " + baseUrl +
                    " during the creation of HttpGet method";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }

        return get;
    }

    /**
     * Util method to create a http get method
     *
     * @param urlFragment     the url fragment
     * @param queryParameters query parameters
     * @param tenantDomain    tenant domain, to which the application is belongs
     * @return a {@link HttpGet}
     */
    private HttpGet createGet(String urlFragment, List<NameValuePair> queryParameters, String tenantDomain)
            throws AppFactoryException {

        String query = null;
        HttpGet get;

        if (queryParameters != null) {
            query = URLEncodedUtils.format(queryParameters, HTTP.UTF_8);
        }
        try {
            URL url = new URL(getJenkinsUrl(tenantDomain));
            URI uri = URIUtils.createURI(url.getProtocol(), url.getHost(), url.getPort(), urlFragment, query, null);
            get = new HttpGet(uri);
            if (authenticate) {
                get.addHeader(
                        BasicScheme.authenticate(new UsernamePasswordCredentials(this.username, this.apiKeyOrPassword),
                                                 HTTP.UTF_8, false));
            }

        } catch (MalformedURLException e) {
            String msg = "Error while generating URL for the path : " + urlFragment + " in tenant : " + tenantDomain +
                         " during the creation of HttpGet method";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (URISyntaxException e) {
            String msg =
                    "Error while constructing the URI for url fragment "+urlFragment +" in tenant : " + tenantDomain +
                    " during the creation of HttpGet method";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        return get;
    }

    /**
     * Util method to create a POST method
     *
     * @param urlFragment     Url fragments.
     * @param queryParameters Query parameters.
     * @param httpEntity
     * @param tenantDomain    Tenant Domain of application
     * @return a {@link HttpPost}
     */
    private HttpPost createPost(String urlFragment, List<NameValuePair> queryParameters, HttpEntity httpEntity,
                                String tenantDomain) throws AppFactoryException {

        String query = "";
        HttpPost post;

        if (queryParameters != null) {
            query = URLEncodedUtils.format(queryParameters, HTTP.UTF_8);
        }
        try {
            URL url = new URL(getJenkinsUrl(tenantDomain));
            URI uri = URIUtils.createURI(url.getProtocol(), url.getHost(), url.getPort(), urlFragment, query, null);
            post = new HttpPost(uri);

            if (httpEntity != null) {
                post.setEntity(httpEntity);
            }

            if (authenticate) {
                post.addHeader(
                        BasicScheme.authenticate(new UsernamePasswordCredentials(this.username, this.apiKeyOrPassword),
                                                 HTTP.UTF_8, false));
            }
        } catch (MalformedURLException e) {
            String msg = "Error while generating URL for the path : " + urlFragment + " in tenant : " + tenantDomain +
                         " during the creation of HttpGet method";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (URISyntaxException e) {
            String msg =
                    "Error while constructing the URI for tenant : " + tenantDomain + " during the creation of " +
                    "HttpPosts method";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        return post;
    }

    /**
     * @param template
     * @param selector
     * @return
     * @throws AppFactoryException
     */
    private String getValueUsingXpath(OMElement template, String selector) throws AppFactoryException {

        String value = null;
        try {
            AXIOMXPath axiomxPath = new AXIOMXPath(selector);
            Object selectedObject = axiomxPath.selectSingleNode(template);

            if (selectedObject != null && selectedObject instanceof OMElement) {
                OMElement selectedElement = (OMElement) selectedObject;
                value = selectedElement.getText();
            } else {
                log.warn("Unable to find xml element matching selector : " + selector);
            }

        } catch (Exception ex) {
            throw new AppFactoryException("Unable to set value to job config ", ex);
        }
        return value;
    }

    /**
     * When jenkins tenant is unloaded the requests cannot be fulfilled. So this
     * method will be used to resend the Get requests
     *
     * @param method       method to be retried
     * @param httpResponse http response of the previous request
     * @return httpStatusCode
     */
    private HttpResponse resendRequest(HttpGet method, HttpResponse httpResponse) throws AppFactoryException {

        int httpStatusCode;
        int retryCount = Integer.parseInt(AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(
                JenkinsCIConstants.JENKINS_CLIENT_RETRY_COUNT));
        int retryDelay = Integer.parseInt(AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(
                JenkinsCIConstants.JENKINS_CLIENT_RETRY_DELAY));
        log.info("Jenkins client retry count :" + retryCount + " and retry delay in seconds :" +
                 retryDelay + " for " + method.getMethod());

        //TODO - Send mail to cloud
        try {
            // retry retryCount times to process the request
            for (int i = 0; i < retryCount; i++) {
                Thread.sleep(MILLISECONDS_PER_SECOND * retryDelay); // sleep retryDelay seconds, giving jenkins
                // time to load the tenant
                if (log.isDebugEnabled()) {
                    log.debug("Resending request(" + i + ") started for GET");
                }

                if (httpResponse != null) {
                    EntityUtils.consume(httpResponse.getEntity());
                }

                httpResponse = httpClient.execute(method, getHttpContext());
                httpStatusCode = httpResponse.getStatusLine().getStatusCode();

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
            String msg = "Error while resending the request to URI : " + method.getURI();
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (InterruptedException e) {
            String msg = "Error while resending the request to URI : " + method.getURI();
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        return httpResponse;
    }

    /**
     * When jenkins tenant is unloaded the requests cannot be fulfilled. So this
     * method will be used to resend the Post requests
     *
     * @param method       method to be retried
     * @param httpResponse http response of the previous request
     * @return httpStatusCode
     */
    private HttpResponse resendRequest(HttpPost method, HttpResponse httpResponse) throws AppFactoryException {

        int httpStatusCode;
        int retryCount = Integer.parseInt(AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(
                JenkinsCIConstants.JENKINS_CLIENT_RETRY_COUNT));
        int retryDelay = Integer.parseInt(AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(
                JenkinsCIConstants.JENKINS_CLIENT_RETRY_DELAY));
        log.info("Jenkins client retry count :" + retryCount + " and retry delay in seconds :"
                 + retryDelay + " for " + method.getMethod());
        try {
            // retry retryCount times to process the request
            for (int i = 0; i < retryCount; i++) {
                Thread.sleep(MILLISECONDS_PER_SECOND * retryDelay); // sleep retryDelay seconds, giving jenkins
                // time to load the tenant
                if (log.isDebugEnabled()) {
                    log.debug("Resending request(" + i + ") started for POST");
                }
                if (httpResponse != null) {
                    EntityUtils.consume(httpResponse.getEntity());
                }
                HttpContext httpContext = getHttpContext();
                httpResponse = httpClient.execute(method, httpContext);
                httpStatusCode = httpResponse.getStatusLine().getStatusCode();
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
            String msg = "Error while resending the request to URI : " + method.getURI();
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (InterruptedException e) {
            String msg = "Error while resending the request to URI : " + method.getURI();
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        return httpResponse;
    }

    /**
     * Add runtime specific parameters to the parameter map
     *
     * @param stage       current stage of the application version
     * @param parameters  list of name value pair to sent to jenkins
     * @param runtimeBean runtime bean that we need to add parameters from
     */
    private void addRunTimeParameters(String stage, List<NameValuePair> parameters, RuntimeBean runtimeBean) {
        parameters.add(new BasicNameValuePair(AppFactoryConstants.RUNTIME_ALIAS_PREFIX,
                                              runtimeBean.getAliasPrefix() + stage));
        parameters.add(new BasicNameValuePair(AppFactoryConstants.RUNTIME_CARTRIDGE_TYPE_PREFIX,
                                              runtimeBean.getCartridgeTypePrefix() + stage));
        parameters.add(new BasicNameValuePair(AppFactoryConstants.PAAS_REPOSITORY_URL_PATTERN,
                                              runtimeBean.getPaasRepositoryURLPattern()));
        parameters.add(new BasicNameValuePair(AppFactoryConstants.RUNTIME_DEPLOYMENT_POLICY,
                                              runtimeBean.getDeploymentPolicy()));
        parameters.add(new BasicNameValuePair(AppFactoryConstants.RUNTIME_AUTOSCALE_POLICY,
                                              runtimeBean.getAutoscalePolicy()));
        parameters.add(new BasicNameValuePair(AppFactoryConstants.RUNTIME_DATA_CARTRIDGE_TYPE,
                                              runtimeBean.getDataCartridgeType()));
        parameters.add(new BasicNameValuePair(AppFactoryConstants.RUNTIME_DATA_CARTRIDGE_ALIAS,
                                              runtimeBean.getDataCartridgeAlias()));
	    parameters.add(new BasicNameValuePair(AppFactoryConstants.RUNTIME_SUBSCRIBE_ON_DEPLOYMENT,
	                                          Boolean.toString(runtimeBean.getSubscribeOnDeployment())));
    }

    /**
     * Check if the given status code is in 2xx range.
     *
     * @param httpStatusCode - status code to be checked
     * @return true if status code is in 2xx range
     */
    private boolean isSuccessfulStatusCode(int httpStatusCode) {
        return (httpStatusCode >= HttpStatus.SC_OK && httpStatusCode < MAX_SUCCESS_HTTP_STATUS_CODE);
    }

}
