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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
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
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.build.DefaultBuildDriverListener;
import org.wso2.carbon.appfactory.core.dto.Statistic;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * Connects to a jenkins server using its 'Remote API'.
 */
public class RestBasedJenkinsCIConnector {

    private static final Log log = LogFactory.getLog(RestBasedJenkinsCIConnector.class);

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

    public RestBasedJenkinsCIConnector(String jenkinsUrl, boolean authenticate, String userName,
                                       String apiKeyOrpassword) {
    	this.httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        this.jenkinsUrl = jenkinsUrl;
        if (StringUtils.isBlank(this.jenkinsUrl)) {
            throw new IllegalArgumentException("Jenkins server url is unspecified");
        }

        this.authenticate = authenticate;
        if (this.authenticate) {
            httpClient.getState().setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(userName,
                            apiKeyOrpassword));
            httpClient.getParams().setAuthenticationPreemptive(true);
        }
    }
    
    
    public HttpClient getHttpClient() {
        return httpClient;
    }

    public String getJenkinsUrl() {
        return jenkinsUrl;
    }

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
     * @param roleName Name of the role.
     * @param pattern  a regular expression to match jobs (e.g. app1.*)
     * @param permissions
     * @throws AppFactoryException if an error occurs
     */
    public void createRole(String roleName, String pattern,
                           String permissions [], String tenantDomain) throws AppFactoryException {
        String createRoleUrl = "/descriptorByName/com.michelin.cio.hudson.plugins.rolestrategy" +
                               ".RoleBasedAuthorizationStrategy/createProjectRoleSubmit";
        ArrayList<NameValuePair> parameters=new ArrayList<NameValuePair>();
        parameters.add(new NameValuePair("name", roleName));
        parameters.add( new NameValuePair("pattern", pattern));
        for (String  permission:permissions){
            parameters.add(new NameValuePair("permission",permission));
        }

        PostMethod addRoleMethod = createPost(createRoleUrl,
                                              parameters.toArray(new NameValuePair[0]), null, tenantDomain);

        try {
            int httpStatusCode = getHttpClient().executeMethod(addRoleMethod);

            if(HttpStatus.SC_SERVICE_UNAVAILABLE == httpStatusCode) {
                httpStatusCode = resendRequest(addRoleMethod);
            }

            if (HttpStatus.SC_OK != httpStatusCode) {
                String errorMsg = String.format("Unable to create the role. jenkins returned, " +
                                                "http status : %d",
                                httpStatusCode);
                log.error(errorMsg);
                throw new AppFactoryException(errorMsg);
            }
        } catch (Exception ex) {
            String errorMsg = String.format("Unable to create role in jenkins : %s",
                            ex.getMessage());
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
     * @param userIds          list of user Ids
     * @param projectRoleNames list of project roles
     * @param globalRoleNames  list of global roles
     * @throws AppFactoryException if an error occurs
     */
    public void assignUsers(String[] userIds, String[] projectRoleNames, String[] globalRoleNames, String tenantDomain)
            throws AppFactoryException {

        String assignURL = JenkinsCIConstants.RoleStrategy.ASSIGN_ROLE_SERVICE;

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        for (String id : userIds) {
            params.add(new NameValuePair("sid", id));
        }

        if (projectRoleNames != null) {

            for (String role : projectRoleNames) {
                params.add(new NameValuePair("projectRole", role));
            }

        }

        if (globalRoleNames != null) {
            for (String role : globalRoleNames) {
                params.add(new NameValuePair("globalRole", role));
            }
        }

        PostMethod assignRolesMethod =
                createPost(assignURL,
                        params.toArray(new NameValuePair[params.size()]),
                        null, tenantDomain);

        try {
            int httpStatusCode = getHttpClient().executeMethod(assignRolesMethod);

            if(HttpStatus.SC_SERVICE_UNAVAILABLE == httpStatusCode) {
                httpStatusCode = resendRequest(assignRolesMethod);
            }
            if (HttpStatus.SC_OK != httpStatusCode) {
                String errorMsg = String.format("Unable to assign roles to given sides. jenkins " +
                                                "returned, http status : %d",
                                httpStatusCode);
                log.error(errorMsg);
                throw new AppFactoryException(errorMsg);
            }
        } catch (Exception ex) {
            String errorMsg = String.format("Unable to assign roles in jenkins : %s",
                            ex.getMessage());
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
     * @param appicationKey Application ID
     * @param users         Users List
     * @throws AppFactoryException
     */
	public void unAssignUsers(String appicationKey, String[] users, String tenantDomain) throws AppFactoryException {
		if (appicationKey == null) {
			throw new NullPointerException("Application cannot be null.");
		}

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		for (int i = 0; i < users.length; ++i) {
			params.add(new NameValuePair("sid", users[i]));
		}

		params.add(new NameValuePair("projectRole", appicationKey));

		PostMethod assignRolesMethod =
		                               createPost(JenkinsCIConstants.RoleStrategy.UNASSIGN_ROLE_SERVICE,
		                                          params.toArray(new NameValuePair[params.size()]),
		                                          null, tenantDomain);
		try {
			int httpStatusCode = getHttpClient().executeMethod(assignRolesMethod);

            if(HttpStatus.SC_SERVICE_UNAVAILABLE == httpStatusCode) {
                httpStatusCode = resendRequest(assignRolesMethod);
            }

            if (HttpStatus.SC_OK != httpStatusCode) {
				String errorMsg =
				                  String.format("Unable to un-assign roles to given application. jenkins returned, http status : %d",
                                          Integer.valueOf(httpStatusCode));
				log.error(errorMsg);
				throw new AppFactoryException(errorMsg);
			}
		} catch (Exception e) {
			String errorMsg =
			                  String.format("Error while un-assining user roles from aplication: %s",
			                                new Object[] { e.getMessage() });
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
     * @throws AppFactoryException if an error occurs
     */
    public List<String> getAllJobs(String tenantDomain) throws AppFactoryException {
        return getJobNames(null, tenantDomain);
    }

    /**
     * Returns a list of job names which contains given text as a substring. If
     * the filter text is not specified (i.e. null) then all the jobs will be
     * returned.
     *
     * @param filterText (specifying null return names of all jobs available)
     *                   text to match
     * @return {@link List} of Job names (in jenkins CI)
     * @throws AppFactoryException if an error occurs
     */
    @SuppressWarnings("unchecked")
    public List<String> getJobNames(String filterText, String tenantDomain) throws AppFactoryException {
        List<String> jobNames = new ArrayList<String>();

        final String wrapperTag = "JobNames";

        final String xpathExpression =
                StringUtils.isNotEmpty(filterText)
                        ? String.format("/*/job/name[contains(., '%s')]",
                        filterText)
                        : "/*/job/name";

        NameValuePair[] queryParameters =
                {new NameValuePair("wrapper", wrapperTag),
                        new NameValuePair("xpath", xpathExpression)};

        GetMethod getJobsMethod = createGet("/view/All/api/xml", queryParameters,tenantDomain);
        try {
            int httpStatusCode = getHttpClient().executeMethod(getJobsMethod);

            if(HttpStatus.SC_SERVICE_UNAVAILABLE == httpStatusCode) {
                httpStatusCode = resendRequest(getJobsMethod);
            }

            if (HttpStatus.SC_OK != httpStatusCode) {
                String errorMsg = String.format("Unable to retrieve job names: filter text :%s, " +
                                                "jenkins returned, http status : %d",
                                filterText, httpStatusCode);
                log.error(errorMsg);
                throw new AppFactoryException(errorMsg);
            }

            StAXOMBuilder builder = new StAXOMBuilder(getJobsMethod.getResponseBodyAsStream());

            Iterator<OMElement> jobNameElements = builder.getDocumentElement().getChildElements();

            while (jobNameElements.hasNext()) {
                OMElement jobName = jobNameElements.next();
                jobNames.add(jobName.getText());
            }
        } catch (Exception ex) {
            String errorMsg = String.format("Unable to retrieve available jobs from jenkins : %s",
                            ex.getMessage());
            log.error(errorMsg, ex);
            throw new AppFactoryException(errorMsg, ex);
        } finally {
            getJobsMethod.releaseConnection();
        }
        return jobNames;

    }

    public File getArtifact(String jobName, String artifactName, String tenantDomain) throws AppFactoryException {
        File file = null;
        String url = "/job/" + jobName + "/ws/" + artifactName;
        GetMethod getArtifactMethod = createGet(url, null, tenantDomain);
        try {
            int httpStatusCode = getHttpClient().executeMethod(getArtifactMethod);

            if(HttpStatus.SC_SERVICE_UNAVAILABLE == httpStatusCode) {
                httpStatusCode = resendRequest(getArtifactMethod);
            }

            if (HttpStatus.SC_OK != httpStatusCode) {
                String errorMsg = String.format("Unable to retrieve artifact from jenkins. " +
                                                "jenkins returned, http status : %d",
                                httpStatusCode);
                log.error(errorMsg);
                throw new AppFactoryException(errorMsg);
            }

            String carbonHome = System.getProperty("carbon.home"); //TODO find the constnat

            String fileName = artifactName.substring(artifactName.lastIndexOf("/") + 1);

            InputStream ins = getArtifactMethod.getResponseBodyAsStream();
            @SuppressWarnings("UnusedAssignment")
            int read = 0;
            byte[] bytes = new byte[1024];
            file = new File(carbonHome + "/tmp/" + fileName);
            FileOutputStream out = new FileOutputStream(file);
            while ((read = ins.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            ins.close();
        } catch (Exception ex) {
            String errorMsg = String.format("Unable to retrieve available jobs from jenkins : %s",
                            ex.getMessage());
            log.error(errorMsg, ex);
            throw new AppFactoryException(errorMsg, ex);
        } finally {
            getArtifactMethod.releaseConnection();
        }
        return file;
    }

    /**
     * Create a job in Jenkins
     *
     * @param jobName   name of the job
     * @param jobParams Job configuration parameters
     * @param tenantDomain Tenant domain of applicatoin
     * @throws AppFactoryException if an error occures.
     */
    public void createJob(String jobName, Map<String, String> jobParams, String tenantDomain) throws AppFactoryException {

        OMElement jobConfiguration = new JobConfigurator(jobParams).configure(jobParams.get(JenkinsCIConstants.APPLICATION_EXTENSION));
        NameValuePair[] queryParams = {new NameValuePair("name", jobName)};
        PostMethod createJob = null;
        boolean jobCreatedFlag = false;

        try {
            createJob =
                    createPost("/createItem", queryParams,
                            new StringRequestEntity(jobConfiguration.toStringWithConsume(),
                                    "text/xml", "utf-8"), tenantDomain);
            int httpStatusCode = getHttpClient().executeMethod(createJob);

            if(HttpStatus.SC_SERVICE_UNAVAILABLE == httpStatusCode) {
                httpStatusCode = resendRequest(createJob);
            }

            if (HttpStatus.SC_OK != httpStatusCode) {
                String errorMsg = String.format("Unable to create the job: [%s]. jenkins " +
                                                "returned, http status : %d",
                                jobName, httpStatusCode);
                log.error(errorMsg);
                throw new AppFactoryException(errorMsg);
            } else {
                jobCreatedFlag = true;
            }
            if ("svn".equals(jobParams.get(JenkinsCIConstants.REPOSITORY_TYPE))) {
                setSvnCredentials(jobName,
                                  jobParams.get(JenkinsCIConstants.
                                                        REPOSITORY_ACCESS_CREDENTIALS_USERNAME),
                                  jobParams.get(JenkinsCIConstants.
                                                        REPOSITORY_ACCESS_CREDENTIALS_PASSWORD),
                                  jobParams.get(JenkinsCIConstants.REPOSITORY_URL), tenantDomain);
            }

        } catch (Exception ex) {
            String errorMsg = "Error while trying creating job: " +jobName;
            log.error(errorMsg, ex);

            if (jobCreatedFlag) {
                // the job was created but setting svn
                // credentials failed. Therefore try
                // deleting the entire job (instead of
                // keeping a unusable job in jenkins)
                try {
                    deleteJob(jobName, tenantDomain);
                } catch (AppFactoryException delExpception) {
                    log.error("Unable to delete the job after failed attempt set svn credentials," +
                              " job: " +jobName, delExpception);
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
     * @param jobName name of the job.
     * @return true if job exits, false otherwise.
     * @throws AppFactoryException if an error occurs.
     */
    public boolean isJobExists(String applicationId, String version, String tenantDomain) throws AppFactoryException {

    	String jobName = ServiceHolder.getContinuousIntegrationSystemDriver().getJobName(applicationId, version, "");
    	
    	final String wrapperTag = "JobNames";
        NameValuePair[] queryParameters =
                {
                        new NameValuePair("wrapper", wrapperTag),
                        new NameValuePair(
                                "xpath",
                                String.format("/*/job/name[text()='%s']", jobName))};

        GetMethod checkJobExistsMethod = createGet("/api/xml", queryParameters, tenantDomain);

        boolean isExists = false;

        try {
            checkJobExistsMethod.setQueryString(queryParameters);
            int httpStatusCode = getHttpClient().executeMethod(checkJobExistsMethod);

            if(HttpStatus.SC_SERVICE_UNAVAILABLE == httpStatusCode) {
                httpStatusCode = resendRequest(checkJobExistsMethod);
            }

            if (HttpStatus.SC_OK != httpStatusCode) {
                final String errorMsg = String.format("Unable to check the existance of job: [%s]" +
                                                      ". jenkins returned, http status : %d",
                                jobName, httpStatusCode);

                log.error(errorMsg);
                throw new AppFactoryException(errorMsg);
            }

            StAXOMBuilder builder =
                    new StAXOMBuilder(
                            checkJobExistsMethod.getResponseBodyAsStream());
            isExists = builder.getDocumentElement().getChildElements().hasNext();
        } catch (Exception ex) {
            String errorMsg = "Error while checking the existance of job: " + jobName;
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
     * @param jobName name of the job
     * @return true if job exited on Jenkins and successfully deleted.
     * @throws AppFactoryException if an error occures.
     */
    public boolean deleteJob(String jobName, String tenantDomain) throws AppFactoryException {
        PostMethod deleteJobMethod =
                createPost(String.format("/job/%s/doDelete", jobName), null,
                        null, tenantDomain);
        int httpStatusCode = -1;
        try {
            httpStatusCode = getHttpClient().executeMethod(deleteJobMethod);

            if(HttpStatus.SC_SERVICE_UNAVAILABLE == httpStatusCode) {
                httpStatusCode = resendRequest(deleteJobMethod);
            }

            if (HttpStatus.SC_FORBIDDEN == httpStatusCode) {
                final String errorMsg = String.format("Unable to delete: [%s]. jenkins returned, " +
                                                      "http status : %d", jobName, httpStatusCode);
                log.error(errorMsg);
                throw new AppFactoryException(errorMsg);
            }

        } catch (Exception ex) {
            String errorMsg = "Error while deleting the job: " + jobName;
            log.error(errorMsg);
            throw new AppFactoryException(errorMsg, ex);
        } finally {
            deleteJobMethod.releaseConnection();
        }
        return HttpStatus.SC_NOT_FOUND != httpStatusCode;

    }

    /**
     * Starts a build job available in Jenkins
     *
     * @param jobName Name of the job
     * @throws AppFactoryException if an error occurs.
     */
    public void startBuild(String applicationId, String version, boolean doDeploy, String stageName, String tagName, String tenantDomain, String userName)
            throws AppFactoryException {
    	
    	String jobName = ServiceHolder.getContinuousIntegrationSystemDriver().getJobName(applicationId, version, "");
    	
    	
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new NameValuePair("isAutomatic","false"));
        parameters.add(new NameValuePair("doDeploy", Boolean.toString(doDeploy)));
        parameters.add(new NameValuePair("deployStage", stageName));
        log.info("================User Name=="+userName);
        String tenantUserName = MultitenantUtils.getTenantAwareUsername(userName) + "@" + tenantDomain;

        parameters.add(new NameValuePair("tenantUserName", tenantUserName));

        // TODO should get the persistArtifact parameter value from the user and set here
        if(tagName != null && !tagName.equals("")){
            parameters.add(new NameValuePair("persistArtifact", String.valueOf(true)));
            parameters.add(new NameValuePair("tagName", tagName));
        } else {
            parameters.add(new NameValuePair("persistArtifact", String.valueOf(false)));
        }

        PostMethod startBuildMethod = createPost(String.format("/job/%s/buildWithParameters", jobName),
                parameters.toArray(new NameValuePair[parameters.size()]), null, tenantDomain);

        int httpStatusCode = -1;
        try {
            httpStatusCode = getHttpClient().executeMethod(startBuildMethod);

            if(HttpStatus.SC_SERVICE_UNAVAILABLE == httpStatusCode) {
                httpStatusCode = resendRequest(startBuildMethod);
            }

        } catch (Exception ex) {
            String errorMsg = String.format("Unable to delete start the build on job : %s",
                                            jobName);
            log.error(errorMsg);
            throw new AppFactoryException(errorMsg, ex);
        } finally {
            startBuildMethod.releaseConnection();
        }

        if (HttpStatus.SC_FORBIDDEN == httpStatusCode) {
            final String errorMsg = "Unable to start a build for job [".concat(jobName)
                    .concat("] due to invalid credentials.")
                    .concat("Jenkins returned, http status : [")
                    .concat(String.valueOf(httpStatusCode))
                    .concat("]");
            log.error(errorMsg);
            throw new AppFactoryException(errorMsg);
        }

        if (HttpStatus.SC_NOT_FOUND == httpStatusCode) {
            final String errorMsg = "Unable to find the job [" + jobName + "Jenkins returned, " +
                                    "http status : [" + httpStatusCode + "]";
            log.error(errorMsg);
            throw new AppFactoryException(errorMsg);
        }
        
        DefaultBuildDriverListener listener = new DefaultBuildDriverListener();
        listener.onBuildStart(applicationId, version, "", tenantDomain);

    }

    /**
     * Logs out of the jenkins server
     *
     * @throws AppFactoryException if an error occurs
     */
    @SuppressWarnings("UnusedDeclaration")
    public void logout(String tenantDomain) throws AppFactoryException {
        GetMethod logoutMethod = createGet("/logout", null, tenantDomain);
        try {
            getHttpClient().executeMethod(logoutMethod);
        } catch (Exception ex) {
            String errorMsg = "Unable to login from jenkins";
            log.error(errorMsg);
            throw new AppFactoryException(errorMsg, ex);

        } finally {
            logoutMethod.releaseConnection();
        }

    }


    public String getbuildStatus(String buildUrl, String tenantDomain) throws AppFactoryException {

        String buildStatus = "Unknown";
        GetMethod checkJobExistsMethod =
                createGet(buildUrl, "api/xml",
                        null, tenantDomain);

        try {
            int httpStatusCode = getHttpClient().executeMethod(checkJobExistsMethod);

            if(HttpStatus.SC_SERVICE_UNAVAILABLE == httpStatusCode) {
                httpStatusCode = resendRequest(checkJobExistsMethod);
            }

            if (HttpStatus.SC_OK != httpStatusCode) {
                final String errorMsg = String.format("Unable to check the status  of build: [%s]" +
                                                      ". jenkins returned, http status : %d",
                                buildUrl, httpStatusCode);

                log.error(errorMsg);
                throw new AppFactoryException(errorMsg);
            }

            StAXOMBuilder builder =
                    new StAXOMBuilder(
                            checkJobExistsMethod.getResponseBodyAsStream());
            OMElement resultElement = builder.getDocumentElement();
            if (resultElement != null) {
                if("false".equals(getValueUsingXpath(resultElement,"/*/building")) ){
                                        buildStatus =getValueUsingXpath(resultElement,"/*/result");
                                    } else {
                                        buildStatus="Building";
                                    }

            }

        } catch (Exception ex) {
            String errorMsg = "Error while checking the status of build: " + buildUrl;
            log.error(errorMsg, ex);
            throw new AppFactoryException(errorMsg, ex);
        } finally {
            checkJobExistsMethod.releaseConnection();
        }

        return buildStatus;
    }

    public List<String> getBuildUrls(String jobName, String tenantDomain) throws AppFactoryException {

        List<String> listOfUrls = new ArrayList<String>();

        final String wrapperTag = "Builds";
        NameValuePair[] queryParameters =
                {new NameValuePair("wrapper", wrapperTag),
                        new NameValuePair("xpath", "/*/build/url")};

        GetMethod getBuildsMethod =
                createGet(String.format("/job/%s/api/xml", jobName),
                        queryParameters, tenantDomain);
        try {
            int httpStatusCode = getHttpClient().executeMethod(getBuildsMethod);

            if(HttpStatus.SC_SERVICE_UNAVAILABLE == httpStatusCode) {
                httpStatusCode = resendRequest(getBuildsMethod);
            }

            if (HttpStatus.SC_OK != httpStatusCode) {
                String errorMsg = String.format("Unable to retrieve available build urls from " +
                                                "jenkins for job %s. jenkins returned," +
                                                " http status : %d",
                                jobName, httpStatusCode);
                log.error(errorMsg);
                throw new AppFactoryException(errorMsg);
            }

            StAXOMBuilder builder = new StAXOMBuilder(getBuildsMethod.getResponseBodyAsStream());
            @SuppressWarnings("unchecked")
            Iterator<OMElement> urlElementsIte = builder.getDocumentElement().getChildElements();
            while (urlElementsIte.hasNext()) {
                OMElement urlElement = urlElementsIte.next();
                listOfUrls.add(urlElement.getText());
            }

        } catch (Exception ex) {
            String errorMsg = String.format("Unable to retrieve available jobs from jenkins : %s",
                            ex.getMessage());
            log.error(errorMsg, ex);
            throw new AppFactoryException(errorMsg, ex);
        } finally {
            getBuildsMethod.releaseConnection();
        }
        return listOfUrls;
    }

    public List<Statistic> getOverallLoad(String tenantDomain) throws AppFactoryException {

        GetMethod overallLoad = createGet("/overallLoad/api/xml", null, tenantDomain);

        List<Statistic> list = new ArrayList<Statistic>();

        try {

            int httpStatusCode = getHttpClient().executeMethod(overallLoad);

            if(HttpStatus.SC_SERVICE_UNAVAILABLE == httpStatusCode) {
                httpStatusCode = resendRequest(overallLoad);
            }

            if (HttpStatus.SC_OK != httpStatusCode) {
                final String errorMsg = String.format("Unable to check the overal load of jenkins" +
                                                      ". jenkins returned, http status : %d",
                                httpStatusCode);
                log.error(errorMsg);
                throw new AppFactoryException(errorMsg);
            }

            StAXOMBuilder builder = new StAXOMBuilder(overallLoad.getResponseBodyAsStream());
            @SuppressWarnings("unchecked")
            Iterator<OMElement> elementIterator =
                    (Iterator<OMElement>) builder.getDocumentElement()
                            .getChildElements();

            while (elementIterator.hasNext()) {

                OMElement statElement = elementIterator.next();
                String value =
                        StringUtils.isEmpty(statElement.getText()) ? "-1"
                                : statElement.getText();

                Statistic stat = new Statistic(statElement.getLocalName(), value);
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
     * @param jobName       eg: applicationKey023-trunk-default
     * @param treeStructure eg: builds[number,duration,result]
     * @return  buildsInfo (Which is a JSON array with requested information
     * @throws AppFactoryException
     */
    public String getJsonTree(String jobName,String treeStructure, String tenantDomain) throws AppFactoryException {

        String buildUrl = null;
        String buildsInfo = null;
        log.info(String.format("getJsonTree - for %s > %s",jobName, treeStructure));
        if(jobName==null || jobName.isEmpty() || jobName.equalsIgnoreCase("all") || jobName.equals("*")){
            buildUrl=this.getJenkinsUrl()+"/";
        }else{
            buildUrl = String.format("%s/job/%s/", this.getJenkinsUrl(), jobName);
        }

        NameValuePair[] queryParameters = {new NameValuePair("tree", treeStructure)};

        GetMethod getBuildsHistoryMethod =
                createGet(buildUrl, "api/json",
                        queryParameters, tenantDomain);

        try {
            int httpStatusCode = getHttpClient().executeMethod(getBuildsHistoryMethod);

            if(HttpStatus.SC_SERVICE_UNAVAILABLE == httpStatusCode) {
                httpStatusCode = resendRequest(getBuildsHistoryMethod);
            }

            if (HttpStatus.SC_OK != httpStatusCode) {
                final String errorMsg = String.format("Unable to fetch information from Jenkins for : %d",
                        getBuildsHistoryMethod.getURI(), httpStatusCode);

                log.error(errorMsg);
                throw new AppFactoryException(errorMsg);
            }
            buildsInfo = getBuildsHistoryMethod.getResponseBodyAsString();
        } catch (Exception ex) {
            String errorMsg = String.format("Error while fetching information tree %s for : %s",treeStructure,jobName);
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
     * @param jobName  Name of job
     * @param userName svn username
     * @param password password
     * @param svnRepo  repo url
     * @throws AppFactoryException if an error occurs.
     */
    private void setSvnCredentials(String jobName, String userName, String password, String svnRepo, String tenantDomain)
            throws AppFactoryException {
        final String setCredentialsURL = String.format("/job/%s/descriptorByName/hudson.scm" +
                                                       ".SubversionSCM/postCredential", jobName);

        PostMethod setCredentialsMethod = createPost(setCredentialsURL, null, null, tenantDomain);

        Part[] parts =
                {new StringPart("url", svnRepo), new StringPart("kind", "password"),
                        new StringPart("username1", userName),
                        new StringPart("password1", password),};
        setCredentialsMethod.setRequestEntity(new MultipartRequestEntity(
                parts,
                setCredentialsMethod.getParams()));

        final String redirectedURlFragment = String.format("/job/%s/descriptorByName/hudson.scm" +
                                                           ".SubversionSCM/credentialOK", jobName);

        try {
            int httpStatus = getHttpClient().executeMethod(setCredentialsMethod);
            Header locationHeader = setCredentialsMethod.getResponseHeader("Location");

            // if operation completed successfully Jenkins returns http 302,
            // which location header ending with '..../credentialOK'

            if (HttpStatus.SC_MOVED_TEMPORARILY != httpStatus ||
                    (locationHeader != null && !StringUtils.endsWithIgnoreCase(
                            StringUtils.trimToEmpty(locationHeader.getValue()),
                            redirectedURlFragment))) {

                String errorMsg = "Unable to set svn credentials for the new job: jenkins " +
                                  "returned - Https status " +
                                httpStatus + " ,Location header " + locationHeader;
                log.error(errorMsg);
                throw new AppFactoryException(errorMsg);
            }

        } catch (IOException e) {
            String errorMsg = String.format("Unable to send svn credentials to jenkins for job: " +
                                            "%s", jobName);
            throw new AppFactoryException(errorMsg, e);
        } finally {
            setCredentialsMethod.releaseConnection();
        }
    }

    /**
     * This method will call jenkins to deploy the latest successfully built artifact of the given job name
     * @param jobName job name of which the artifact is going to get deployed
     * @param artifactType artifact type (car/war) that is going to get deployed
     * @param stage server Urls that we need to deploy the artifact into
     * @throws AppFactoryException
     */
    public void deployLatestSuccessArtifact(String jobName, String artifactType, String stage,
                                            String tenantDomain, String userName, String deployAction)
                                                                                 throws AppFactoryException {
        String deployLatestSuccessArtifactUrl =
                                                "/plugin/appfactory-plugin/deployLatestSuccessArtifact";

        PostMethod deployLatestSuccessArtifactMethod = null;

        try {
            List<NameValuePair> parameters = new ArrayList<NameValuePair>();
            parameters.add(new NameValuePair("artifactType", artifactType));
            parameters.add(new NameValuePair("jobName", jobName));
            parameters.add(new NameValuePair("deployStage", stage));
            parameters.add(new NameValuePair("deployAction", deployAction));

            String tenantUserName = userName + "@" + tenantDomain;
            parameters.add(new NameValuePair("tenantUserName", tenantUserName));

            deployLatestSuccessArtifactMethod = createPost(deployLatestSuccessArtifactUrl,
                                                           null,
                                                           null,
                                                           parameters.toArray(new NameValuePair[parameters.size()]),
                                                           tenantDomain);

            int httpStatusCode = getHttpClient().executeMethod(deployLatestSuccessArtifactMethod);

            if(HttpStatus.SC_SERVICE_UNAVAILABLE == httpStatusCode) {
                httpStatusCode = resendRequest(deployLatestSuccessArtifactMethod);
            }

            log.info("status code for deploy latest success artifact : " + httpStatusCode);
            if (HttpStatus.SC_OK != httpStatusCode) {
                String errorMsg =
                                  "Unable to deploy the latest success artifact. jenkins " +
                                          "returned, http status : " + httpStatusCode;
                log.error(errorMsg);
                throw new AppFactoryException(errorMsg);
            }
        } catch (Exception ex) {
            String errorMsg = "Unable to deploy the latest success artifact : " + ex.getMessage();
            log.error(errorMsg, ex);
            throw new AppFactoryException(errorMsg, ex);
        } finally {
            if (deployLatestSuccessArtifactMethod != null) {
                deployLatestSuccessArtifactMethod.releaseConnection();
            }
        }
        

    }

    public void deployPromotedArtifact(String jobName,String artifactType, String stage, String tenantDomain, String userName) throws AppFactoryException {

        String deployPromotedArtifactUrl = "/plugin/appfactory-plugin/deployPromotedArtifact";

        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new NameValuePair("jobName", jobName));
        parameters.add(new NameValuePair("artifactType", artifactType));
        parameters.add(new NameValuePair("deployStage", stage));

        String tenantUserName = userName + "@" + tenantDomain;
        parameters.add(new NameValuePair("tenantUserName", tenantUserName));

        PostMethod deployPromotedArtifactMethod = createPost(deployPromotedArtifactUrl,
                null, null, parameters.toArray(new NameValuePair[parameters.size()]), tenantDomain);

        try {
            int httpStatusCode = getHttpClient().executeMethod(deployPromotedArtifactMethod);

            if(HttpStatus.SC_SERVICE_UNAVAILABLE == httpStatusCode) {
                httpStatusCode = resendRequest(deployPromotedArtifactMethod);
            }

            log.info("status code for deploy promoted artifact artifact : " + httpStatusCode);
            if (HttpStatus.SC_OK != httpStatusCode) {
                String errorMsg = "Unable to deploy the promoted artifact for job " + jobName +
                        ". jenkins returned, http status : " + httpStatusCode;
                log.error(errorMsg);
                throw new AppFactoryException(errorMsg);
            }
        } catch (Exception ex) {
            String errorMsg = "Unable to deploy the promoted artifact for job " + jobName +
                    ": " + ex.getMessage();
            log.error(errorMsg, ex);
            throw new AppFactoryException(errorMsg, ex);
        } finally {
            deployPromotedArtifactMethod.releaseConnection();
        }
    }

    /**
     * Creates the applicationId from the job name
     * @param jobName jobName
     * @return applicationId
     */
    private String getAppId(String jobName) {
        // job name : <applicationId>-<version>-default

        //removing the '-default' part
        String temp = jobName.substring(0, jobName.lastIndexOf("-"));
        //removing the app version
        String applicationId = temp.substring(0,temp.lastIndexOf("-"));
        return applicationId;
    }

    /**
     * This will return the tag names of the persisted artifact of the given job
     * @param jobName job name of which we need to get the tag names
     * @return tag names of the persisted artifacts
     * @throws AppFactoryException
     */
    public String[] getTagNamesOfPersistedArtifacts(String jobName, String tenantDomain) throws AppFactoryException {
        String getIdentifiersOfArtifactsUrl = "/plugin/appfactory-plugin/getTagNamesOfPersistedArtifacts";
        @SuppressWarnings("UnusedAssignment")
        String[] tagNamesOfPersistedArtifacts = new String[0];
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new NameValuePair("jobName", jobName));

//        We are creating the tenant user name using the Carbon Context and sending it to the Jenkins server
        String tenantUserName = CarbonContext.getThreadLocalCarbonContext().getUsername() + "@" + tenantDomain;
        parameters.add(new NameValuePair("tenantUserName", tenantUserName));

        PostMethod getIdsOfPersistArtifactMethod = createPost(getIdentifiersOfArtifactsUrl,
                parameters.toArray(new NameValuePair[parameters.size()]), null, tenantDomain);
        try {
            int httpStatusCode = getHttpClient().executeMethod(getIdsOfPersistArtifactMethod);
            log.info("status code for getting tag names of persisted artifacts : " + httpStatusCode);

            if(HttpStatus.SC_SERVICE_UNAVAILABLE == httpStatusCode) {
                httpStatusCode = resendRequest(getIdsOfPersistArtifactMethod);
            }

            if (HttpStatus.SC_OK != httpStatusCode) {
                String errorMsg = "Unable to get the tag names of persisted artifact for job " +
                                  jobName + ". jenkins returned, http status : " + httpStatusCode;
                log.error(errorMsg);
                throw new AppFactoryException(errorMsg);
            }
            tagNamesOfPersistedArtifacts = getIdsOfPersistArtifactMethod.
                    getResponseBodyAsString().split(",");
            return tagNamesOfPersistedArtifacts;
        } catch (Exception ex) {
            String errorMsg = "Error while retrieving the tags of persisted artifact for job " +
                              jobName + " : " + ex.getMessage();
            log.error(errorMsg, ex);
            throw new AppFactoryException(errorMsg, ex);
        } finally {
            getIdsOfPersistArtifactMethod.releaseConnection();
        }
    }

    /**
     * edit job in lifeCycle change
     *
     * @param jobName       jobName
     * @param updateState   (addAD/removeAD) flag to remove or add Auto Deploy trigger configurations
     * @param pollingPeriod AD pollingPeriod
     * @throws AppFactoryException
     */
    @Deprecated
    public void editJob(String jobName, String updateState, int pollingPeriod, String tenantDomain)
            throws AppFactoryException {
        OMElement configuration = getConfiguration(jobName, updateState, pollingPeriod, tenantDomain);
        OMElement tmpConfiguration = configuration.cloneOMElement();
        setConfiguration(jobName, tmpConfiguration, tenantDomain);


    }

	public void setJobAutoBuildable(String jobName, String repositoryType,boolean isAutoBuild, int pollingPeriod,
                                    String tenantDomain) throws AppFactoryException {
		OMElement configuration =
		                          getAutoBuildUpdatedConfiguration(jobName, repositoryType,isAutoBuild,
		                                                           pollingPeriod, tenantDomain);
		OMElement tmpConfiguration = configuration.cloneOMElement();
		setConfiguration(jobName, tmpConfiguration, tenantDomain);
		log.info("Job : " + jobName + " sccessfully configured for auto building " + isAutoBuild +" in jenkins");
	}

	private OMElement getAutoBuildUpdatedConfiguration(String jobName, String repositoryType,boolean isAutoBuild,
	                                                   int pollingPeriod, String tenantDomain)
	                                                                     throws AppFactoryException {
		GetMethod getFetchMethod = createGet(String.format("/job/%s/config.xml", jobName), null, tenantDomain);
		OMElement configurations = null;
		try {

			int httpStatusCode = getHttpClient().executeMethod(getFetchMethod);

            if(HttpStatus.SC_SERVICE_UNAVAILABLE == httpStatusCode) {
                httpStatusCode = resendRequest(getFetchMethod);
            }

			if (HttpStatus.SC_OK != httpStatusCode) {
				String errorMsg =
				                  String.format("Unable to retrieve available config urls from "
				                                        + "jenkins for job %s. "
				                                        + "jenkins returned, http status : %d",
				                                jobName,
				                                httpStatusCode);
				log.error(errorMsg);
				throw new AppFactoryException(errorMsg);
			}

			StAXOMBuilder builder = new StAXOMBuilder(getFetchMethod.getResponseBodyAsStream());
			configurations = builder.getDocumentElement();

			AXIOMXPath axiomxPath = new AXIOMXPath("//triggers");
			Object selectedObject = axiomxPath.selectSingleNode(configurations);
			if (isAutoBuild) {

				if (selectedObject != null) {
					OMElement selectedNode = (OMElement) selectedObject;
					selectedNode.detach();
				}

				StringBuilder payload =new StringBuilder(
				                 "<triggers class=\"vector\">" + "<hudson.triggers.SCMTrigger>");
               if("git".equals(repositoryType)){
                payload=payload.append("<spec></spec>");
               }else {
                payload=payload.append("<spec>*/" + pollingPeriod + " * * * *</spec>");
               }
			   payload=payload.append("</hudson.triggers.SCMTrigger>" + "</triggers>");
				OMElement triggerParam = AXIOMUtil.stringToOM(payload.toString());
				configurations.addChild(triggerParam);

			} else {
				if (selectedObject != null) {
					OMElement selectedNode = (OMElement) selectedObject;
					selectedNode.detach();
				}
			}

		} catch (Exception ex) {
			String errorMsg =
			                  String.format("Unable to retrieve available jobs from jenkins : %s",
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
     * @param jobName job name of which we need to get the configuration of
     * @param updateState  (addAD/removeAD) flag to remove or add Auto Deploy trigger configurations
     * @param pollingPeriod AutoDeployment pollingPeriod
     * @return configuration after adding or removing AD configurations
     * @throws AppFactoryException
     */
	@Deprecated
    private OMElement getConfiguration(String jobName, String updateState, int pollingPeriod, String tenantDomain)
            throws AppFactoryException {
        GetMethod getFetchMethod = createGet(String.format("/job/%s/config.xml", jobName), null, tenantDomain);
        OMElement configurations = null;

        try {
            int httpStatusCode = getHttpClient().executeMethod(getFetchMethod);

            if(HttpStatus.SC_SERVICE_UNAVAILABLE == httpStatusCode) {
                httpStatusCode = resendRequest(getFetchMethod);
            }

            if (HttpStatus.SC_OK != httpStatusCode) {
                String errorMsg = String.format("Unable to retrieve available config urls from " +
                                                "jenkins for job %s. " +
                                                "jenkins returned, http status : %d",
                                jobName, httpStatusCode);
                log.error(errorMsg);
                throw new AppFactoryException(errorMsg);
            }

            StAXOMBuilder builder = new StAXOMBuilder(getFetchMethod.getResponseBodyAsStream());
            configurations = builder.getDocumentElement();

            if (updateState.equals("removeAD")) {
                AXIOMXPath axiomxPath = new AXIOMXPath("//triggers");
                Object selectedObject = axiomxPath.selectSingleNode(configurations);
                if(selectedObject != null) {
                    OMElement selectedNode = (OMElement) selectedObject;
                    selectedNode.detach();
                }

            } else if (updateState.equals("addAD")) {
                String payload = "<triggers class=\"vector\">" +
                        "<hudson.triggers.SCMTrigger>" +
                        "<spec>*/" + pollingPeriod + " * * * *</spec>" +
                        "</hudson.triggers.SCMTrigger>" +
                        "</triggers>";
                OMElement triggerParam = AXIOMUtil.stringToOM(payload);
                configurations.addChild(triggerParam);

            }

        } catch (Exception ex) {
            String errorMsg = String.format("Unable to retrieve available jobs from jenkins : %s",
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
     * @param jobName job name of which we need to update the configuration of
     * @param jobConfiguration new configurations that needs to be set
     * @throws AppFactoryException
     */
    private void setConfiguration(String jobName, OMElement jobConfiguration, String tenantDomain)
            throws AppFactoryException {

        NameValuePair[] queryParams = {new NameValuePair("name", jobName)};
        PostMethod createJob = null;
        boolean jobCreatedFlag = false;

        try {
            createJob =
                    createPost(String.format("/job/%s/config.xml", jobName), queryParams,
                            new StringRequestEntity(jobConfiguration.toStringWithConsume(),
                                    "text/xml", "utf-8"), tenantDomain);
            int httpStatusCode = getHttpClient().executeMethod(createJob);

            if(HttpStatus.SC_SERVICE_UNAVAILABLE == httpStatusCode) {
                httpStatusCode = resendRequest(createJob);
            }

            if (HttpStatus.SC_OK != httpStatusCode) {
                String errorMsg = String.format("Unable to set configuration: [%s]. jenkins " +
                                                "returned, http status : %d",
                                jobName, httpStatusCode);
                log.error(errorMsg);
                throw new AppFactoryException(errorMsg);
            } else {
                jobCreatedFlag = true;
            }

        } catch (Exception ex) {
            String errorMsg = "Error while setting configuration: " + jobName;
            log.error(errorMsg, ex);

            //noinspection ConstantConditions
            if (jobCreatedFlag) {
                try {
                    deleteJob(jobName, tenantDomain);
                } catch (AppFactoryException delExpception) {
                    log.error("Unable to delete the job after failed attempt set svn credentials, " +
                              "job: " + jobName, delExpception);
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
     * @param urlFragment     Url fragments
     * @param queryParameters query parameters.
     * @return a {@link GetMethod}
     */
    private GetMethod createGet(String urlFragment, NameValuePair[] queryParameters, String tenantDomain) {
        return createGet(getJenkinsUrl(), urlFragment, queryParameters, tenantDomain);
    }

    /**
     * Util method to create a http get method
     *
     * @param baseUrl         the base url //TODO:should be irrelevant
     * @param urlFragment     the url fragment
     * @param queryParameters query parameters
     * @return a {@link GetMethod}
     */
    private GetMethod createGet(String baseUrl, String urlFragment, NameValuePair[] queryParameters,
                                String tenantDomain) {
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
     *  Overloaded Util method to create a POST method
     * @param urlFragment     Url fragments.
     * @param queryParameters Query parameters.
     * @param requestEntity   A request entity
     * @param tenantDomain Tenant domain of application
     * @return a {@link PostMethod}
     */
    private PostMethod createPost(String urlFragment, NameValuePair[] queryParameters,
                                  RequestEntity requestEntity, String tenantDomain) {
        return createPost(urlFragment, queryParameters, requestEntity, null, tenantDomain);
    }


    /**
     * Get Jenkins URL for a given Tenant Domain
     * @param urlFragment     Url fragments
     * @param tenantDomain    Tenant domain of the application
     * @return Jenkins URL
     */
    private String getJenkinsUrlByTenantDomain(String urlFragment, String tenantDomain){
        if(tenantDomain!=null && !tenantDomain.equals(""))
            return getJenkinsUrl() + File.separator + Constants.TENANT_SPACE + File.separator + tenantDomain + Constants.JENKINS_WEBAPPS + File.separator + urlFragment;
        else
            return getJenkinsUrl() + urlFragment;
    }

    /**
     * Util method to create a POST method
     *
     * @param urlFragment     Url fragments.
     * @param queryParameters Query parameters.
     * @param postParameters  Post parameters.
     * @param requestEntity   A request entity
     * @param tenantDomain    Tenant Domain of application
     * @return a {@link PostMethod}
     */
    private PostMethod createPost(String urlFragment, NameValuePair[] queryParameters,
                                  RequestEntity requestEntity, NameValuePair[] postParameters, String tenantDomain) {
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

        if ( postParameters != null){
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
                                value=selectedElement.getText();
                            } else {
                                log.warn("Unable to find xml element matching selector : " + selector);
                            }

                            } catch (Exception ex) {
                        throw new AppFactoryException("Unable to set value to job config", ex);
                    }
                return value;
            }

    public void setJobAutoDeployable(String jobName, boolean isAutoDeployable, String tenantDomain)
                                                                              throws AppFactoryException {

        OMElement configuration = getAutoDeployUpdatedConfiguration(jobName, isAutoDeployable, tenantDomain);
        OMElement tmpConfiguration = configuration.cloneOMElement();
        setConfiguration(jobName, tmpConfiguration, tenantDomain);
        log.info("Job : " + jobName + " sccessfully configured for auto building " +
                 isAutoDeployable + " in jenkins");
    }

    private OMElement getAutoDeployUpdatedConfiguration(String jobName, boolean isAutoDeploy, String tenantDomain)
                                                                                             throws AppFactoryException {
        OMElement configurations = null;

        GetMethod getFetchMethod = createGet(String.format("/job/%s/config.xml", jobName), null,tenantDomain);

        try {
            int httpStatusCode = getHttpClient().executeMethod(getFetchMethod);

            if(HttpStatus.SC_SERVICE_UNAVAILABLE == httpStatusCode) {
                httpStatusCode = resendRequest(getFetchMethod);
            }

            if (HttpStatus.SC_OK != httpStatusCode) {
                String errorMsg =
                                  String.format("Unable to retrieve available config urls from "
                                                        + "jenkins for job %s. "
                                                        + "jenkins returned, http status : %d",
                                                jobName,
                                                httpStatusCode);
                log.error(errorMsg);
                throw new AppFactoryException(errorMsg);
            }

            StAXOMBuilder builder = new StAXOMBuilder(getFetchMethod.getResponseBodyAsStream());
            configurations = builder.getDocumentElement();

            String paramValue = null;
            if (isAutoDeploy) {
                paramValue = "true";
            } else {
                paramValue = "false";
            }

            AXIOMXPath axiomxPath =
                                    new AXIOMXPath(
                                                   "//hudson.model.ParametersDefinitionProperty[1]/parameterDefinitions[1]/hudson.model.StringParameterDefinition[name='isAutomatic']/defaultValue");
            Object selectedObject = axiomxPath.selectSingleNode(configurations);

            if (selectedObject != null) {
                OMElement selectedNode = (OMElement) selectedObject;
                selectedNode.setText(paramValue);
            } else {
                AXIOMXPath axiomP =
                                    new AXIOMXPath(
                                                   "//hudson.model.ParametersDefinitionProperty[1]/parameterDefinitions[1]");
                Object parameterDefsObject = axiomP.selectSingleNode(configurations);
                OMElement parameterDefsNode = (OMElement) parameterDefsObject;

                String payload = "<isAutomatic>" + paramValue + "</isAutomatic>";
                OMElement triggerParam = AXIOMUtil.stringToOM(payload);
                parameterDefsNode.addChild(triggerParam);
            }

        } catch (Exception ex) {
            String errorMsg =
                              String.format("Unable to retrieve available jobs from jenkins : %s",
                                            ex.getMessage());
            log.error(errorMsg, ex);
            throw new AppFactoryException(errorMsg, ex);
        } finally {
            getFetchMethod.releaseConnection();
        }

        return configurations;
    }

    /**
     * When jenkins tenant is unloaded the requests cannot be fulfilled. So this method will be used to resend the
     * Get requests
     * @param method method to be retried
     * @return httpStatusCode
     */
    private int resendRequest(GetMethod method) throws AppFactoryException {
        int httpStatusCode = -1;
        try {
            // retry 3 times to process the request
            for(int i = 0; i < 3; i++) {
                Thread.sleep(1000*10); // sleep 10 seconds, giving jenkins time to load the tenant
                log.info("Resending request...");
                method.releaseConnection();
                httpStatusCode = getHttpClient().executeMethod(method);

                if(HttpStatus.SC_OK == httpStatusCode){
                    log.info("Break resending since "+httpStatusCode);
                    break;
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
     * When jenkins tenant is unloaded the requests cannot be fulfilled. So this method will be used to resend the
     * Post requests
     * @param method method to be retried
     * @return httpStatusCode
     */
    private int resendRequest(PostMethod method) throws AppFactoryException {
        int httpStatusCode = -1;
        try {
            // retry 3 times to process the request
            for(int i = 0; i < 3; i++) {
				Thread.sleep(1000 * 10); // sleep 10 seconds, giving jenkins time to load the tenant
                log.info("Resending request...");
                method.releaseConnection();
                httpStatusCode = getHttpClient().executeMethod(method);                
                
                if(HttpStatus.SC_OK == httpStatusCode){
                    log.info("Break resending since "+httpStatusCode);
                    break;
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
}
