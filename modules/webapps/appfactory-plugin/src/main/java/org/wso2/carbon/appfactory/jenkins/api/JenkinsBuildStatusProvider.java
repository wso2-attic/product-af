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
import hudson.model.Job;
import jenkins.model.Jenkins;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
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
import org.wso2.carbon.appfactory.jenkins.util.JenkinsUtility;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JenkinsBuildStatusProvider implements BuildStatusProvider {

	private static final Log log = LogFactory.getLog(JenkinsBuildStatusProvider.class);
	public static final String FOLDER_JB_SEPARATOR = "/";


	private HttpClient client = null;

	public Map<String, String> getLastBuildInformation(String applicationId, String version, String userName,
	                                                   String repoFrom, String tenantDomain)
			throws BuildStatusProviderException {
		String jobName = JenkinsUtility.getJobName(applicationId, version,userName,repoFrom);
		String url = null;
		try {
			url = AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(
					AppFactoryConstants.CONTINUOUS_INTEGRATION_PROVIDER_JENKINS_PROPERTY_BASE_URL);
		} catch (AppFactoryException e) {
			String msg = "Error occuered while calling the API";
			throw new BuildStatusProviderException(msg);
		}
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

    public String getLastSuccessfulBuildId(String applicationId, String version, String userName, String repoFrom,
                                           String tenantDomain) throws BuildStatusProviderException {

    	String jobName = JenkinsUtility.getJobName(applicationId, version);
	    Job job = (Job)Jenkins.getInstance().getItemByFullName(tenantDomain + FOLDER_JB_SEPARATOR + jobName);
        String lastSuccessBuildId = job.getLastSuccessfulBuild().getId();
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
