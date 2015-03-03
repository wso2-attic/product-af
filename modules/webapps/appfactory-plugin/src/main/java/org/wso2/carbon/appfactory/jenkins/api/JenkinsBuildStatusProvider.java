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

package org.wso2.carbon.appfactory.jenkins.api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.deployers.build.api.BuildStatusProvider;
import org.wso2.carbon.appfactory.deployers.build.api.BuildStatusProviderException;
import org.wso2.carbon.appfactory.jenkins.Constants;
import org.wso2.carbon.appfactory.jenkins.artifact.storage.Utils;
import org.wso2.carbon.appfactory.jenkins.util.JenkinsUtility;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JenkinsBuildStatusProvider implements BuildStatusProvider {

	private static final Log log = LogFactory.getLog(JenkinsBuildStatusProvider.class);


	private HttpClient client = null;

	public Map<String, String> getLastBuildInformation(String applicationId, String version, String userName, String repoFrom)
			throws BuildStatusProviderException {
		String jobName = JenkinsUtility.getJobName(applicationId, version,userName,repoFrom);
		String url = null;
		try {
			url = AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(
					AppFactoryConstants.JENKINS_PROPERTYBASE_URL);
		} catch (AppFactoryException e) {
			String msg = "Error occuered while calling the API";
			throw new BuildStatusProviderException(msg);
		}
		String tenantDomain = Utils.getEnvironmentVariable("TENANT_DOMAIN");
		url += "/t/" + tenantDomain + "/webapps/jenkins/"  + "job/" + jobName + "/api/json";
		log.info("Calling jenkins api : " + url);
		GetMethod get = new GetMethod(url);
		NameValuePair valuePair =
				new NameValuePair("tree",
						"builds[number,status,timestamp,id,result]");
		get.setQueryString(new org.apache.commons.httpclient.NameValuePair[] { valuePair });

		try {
			getHttpClient().getState()
			.setCredentials(AuthScope.ANY,
					new UsernamePasswordCredentials(
							AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(
									Constants.JENKINS_ADMIN_USERNAME_PATH),
					        AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(
							        Constants.JENKINS_ADMIN_PASSWORD_PATH)));
		} catch (AppFactoryException e) {
			String msg = "Error occuered while calling the API";
			throw new BuildStatusProviderException(msg);
		}
		getHttpClient().getParams().setAuthenticationPreemptive(true);

		Map<String, String> buildInformarion = null;

		try {
			log.debug("Retrieving last build information for job : " + jobName);
			getHttpClient().executeMethod(get);
			log.info("Retrieving last build information for job : " + jobName +
					" status received : " + get.getStatusCode());
			if (get.getStatusCode() == HttpStatus.SC_OK) {
				String response = get.getResponseBodyAsString();
				log.debug("Returns build information for job : " + jobName + " - " + response);
				buildInformarion = extractBuildInformarion(response);
			} else {
				String msg =
						"Error while retrieving  build information for job : " + jobName +
						" Jenkins returned status code : " + get.getStatusCode();
				log.error(msg);
				throw new BuildStatusProviderException(msg, BuildStatusProviderException.INVALID_RESPONSE);
			}

		} catch (HttpException e) {
			String msg = "Error occuered while calling the API";
			throw new BuildStatusProviderException(msg);

		} catch (IOException e) {
			String msg = "Error occuered while calling the API";
			throw new BuildStatusProviderException(msg);
		} finally {
			get.releaseConnection();
		}
		return buildInformarion;
	}

	private Map<String, String> extractBuildInformarion(String response) {
		Gson gson = new Gson();
		Map<String, List<Map<String, String>>> buildInfoMap =
				gson.fromJson(response,
						new TypeToken<Map<String, List<Map<String, String>>>>() {
				}.getType());
		List<Map<String, String>> buildList = buildInfoMap.get("builds");
		if (buildList.size() > 0) {
			return buildList.get(0);
		} else {
			return null;
		}
	}

	private HttpClient getHttpClient() {

		if (client == null) {
			client = new HttpClient();
		}
		return client;
	}

	void setHttpClient(HttpClient client) {
		this.client = client;
	}

    public String getLastSuccessfulBuildId(String applicationId, String version, String userName, String repoFrom) throws BuildStatusProviderException {

    	String jobName = JenkinsUtility.getJobName(applicationId, version) ;  	
    	
        String buildUrl ="";
        try {
            buildUrl = AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(
		            "ContinuousIntegrationProvider.jenkins.Property.BaseURL");
        } catch (AppFactoryException e) {
            String msg = "Error occuered while calling the API";
            throw new BuildStatusProviderException(msg);
        }
        String tenantDomain = Utils.getEnvironmentVariable("TENANT_DOMAIN");
        buildUrl += "/t/" + tenantDomain + "/webapps/jenkins/"  + "job/" + jobName + "/api/xml";
        String lastSuccessBuildId = null;
        GetMethod checkJobExistsMethod = new GetMethod(buildUrl);
        try {
            getHttpClient().getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(
		            AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(
				            Constants.JENKINS_ADMIN_USERNAME_PATH),
		            AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(
				            Constants.JENKINS_ADMIN_PASSWORD_PATH)));
        } catch (AppFactoryException e) {
            String msg = "Error occuered while calling the API";
            throw new BuildStatusProviderException(msg);
        }
        getHttpClient().getParams().setAuthenticationPreemptive(true);

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
                throw new BuildStatusProviderException(errorMsg);
            }

            StAXOMBuilder builder =
                    new StAXOMBuilder(
                            checkJobExistsMethod.getResponseBodyAsStream());
            OMElement resultElement = builder.getDocumentElement();

            if (resultElement != null) {
                OMElement lastSuccessfulBuild = (resultElement.
                        getFirstChildWithName(new QName("lastSuccessfulBuild")));
                if (lastSuccessfulBuild != null) {
                    lastSuccessBuildId = lastSuccessfulBuild.getFirstChildWithName(new QName("number")).getText();
                }
            }

        } catch (Exception ex) {
            String errorMsg = "Error while checking the status of build: " + buildUrl;
            log.error(errorMsg, ex);
            throw new BuildStatusProviderException(errorMsg);
        } finally {
            checkJobExistsMethod.releaseConnection();
        }

        return lastSuccessBuildId;
    }

    private int resendRequest(GetMethod method) throws AppFactoryException {
        int httpStatusCode = -1;
        try {
            // retry 3 times to process the request
            for(int i = 0; i < 3; i++) {
                Thread.sleep(1000*10); // sleep 10 seconds, giving jenkins time to load the tenant
                log.info("Resending request...");
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
