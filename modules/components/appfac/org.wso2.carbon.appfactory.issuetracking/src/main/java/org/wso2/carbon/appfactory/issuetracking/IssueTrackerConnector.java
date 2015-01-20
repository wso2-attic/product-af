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

package org.wso2.carbon.appfactory.issuetracking;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.dto.Application;

public class IssueTrackerConnector{

    private static final Log log = LogFactory.getLog(IssueTrackerConnector.class);
    private String issueTrackerUrl;
    private HttpClient httpClient;

    public IssueTrackerConnector() throws AppFactoryException {
        this.httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        this.issueTrackerUrl = AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty("IssueTrackerConnector.issueTracker.Property.Url");
    }

    public boolean createProject(Application application, String userName, String tenantDomain, boolean isUploadableAppType) throws AppFactoryException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name",application.getName());
        jsonObject.put("owner", userName);
        jsonObject.put("key", application.getId());
        jsonObject.put("description", application.getDescription());

        JSONObject project = new JSONObject();
        project.put("project", jsonObject);

        String jsonString = project.toJSONString();

        String postUrl = issueTrackerUrl + "services/tenant/" + tenantDomain + "/project";
        PostMethod post = new PostMethod(postUrl);
        try {
            RequestEntity entity = new StringRequestEntity(jsonString, "application/json", "utf-8");
            post.setRequestEntity(entity);
            HttpClient httpclient = new HttpClient();

            int result = httpclient.executeMethod(post);
            
            String defaultVersion = "trunk";
			if(isUploadableAppType){
				defaultVersion = "1.0.0";
			}
			
            onVersionCreation(application, defaultVersion, tenantDomain);
            return true;
        } catch (Exception e) {
            String msg = "Error while  creating project in issue repository for " + application.getName();
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }  finally {
            // Release current connection to the connection pool once you are
            // done
            post.releaseConnection();
        }
    }

    public boolean deleteProject(Application application, String userName, String tenantDomain) throws AppFactoryException {

        String url = issueTrackerUrl + "services/tenant/" + tenantDomain + "/project/" + application.getId();
        DeleteMethod deleteProject = new DeleteMethod(url);
        int result = -1;
        try {
            HttpClient httpclient = new HttpClient();
            result = httpclient.executeMethod(deleteProject);
        } catch (Exception e) {
            String msg = "Error while  creating project in issue repository for " + application.getName();
            log.error(msg);
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new AppFactoryException(msg, e);
        } finally {
            // Release current connection to the connection pool once you are done
            deleteProject.releaseConnection();
        }
        return result == HttpStatus.SC_OK;
    }

    public JSONObject getProject(String applicationKey, String tenantDomain) throws AppFactoryException{
        String url = issueTrackerUrl + "services/tenant/" + tenantDomain + "/project/"+ applicationKey;
        GetMethod getMethod = new GetMethod(url);
        JSONObject projectJson = null;
        int result = -1;
        try {
            HttpClient httpclient = new HttpClient();
            result = httpclient.executeMethod(getMethod);
            if(result == HttpStatus.SC_OK && getMethod.getResponseBodyAsString() != null) {
                projectJson = (JSONObject)new JSONParser().parse(getMethod.getResponseBodyAsString());
            }

        } catch (Exception e) {
            String msg = "Error while  creating project in issue repository for " + applicationKey;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }  finally {
            // Release current connection to the connection pool once you are done
            getMethod.releaseConnection();
        }
        return projectJson;
    }

    public void deleteProjectVersions(String projectId, String tenantDomain) throws AppFactoryException{
        String postUrl = issueTrackerUrl + "services/tenant/" + tenantDomain + "/project/"+ projectId + "/version";
        DeleteMethod deleteVersion = new DeleteMethod(postUrl);
        int result = -1;
        try {
            HttpClient httpclient = new HttpClient();
            result = httpclient.executeMethod(deleteVersion);
        } catch (Exception e) {
            String msg = "Error while  creating project in issue repository for " + projectId;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }  finally {
            // Release current connection to the connection pool once you are done
            deleteVersion.releaseConnection();
        }
    }

    public boolean deleteProjectVersion(String projectId, String version, String tenantDomain) throws AppFactoryException {
        String postUrl = issueTrackerUrl + "services/tenant/" + tenantDomain + "/project/"+ projectId + "/" + version;
        DeleteMethod deleteVersion = new DeleteMethod(postUrl);
        int result = -1;
        try {
            HttpClient httpclient = new HttpClient();
            result = httpclient.executeMethod(deleteVersion);
        } catch (Exception e) {
            String msg = "Error while  creating project in issue repository for " + projectId;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }  finally {
            // Release current connection to the connection pool once you are done
            deleteVersion.releaseConnection();
        }
        return result == 200;
    }

    public void onVersionCreation(Application application, String version, String tenantDomain) throws AppFactoryException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("version", version);

        JSONObject versionJson = new JSONObject();
        versionJson.put("version",jsonObject);

        String jsonString = versionJson.toJSONString();

        String postUrl = issueTrackerUrl + "services/tenant/" + tenantDomain + "/project/"+ application.getId() + "/version";
        PostMethod post = new PostMethod(postUrl);
        try {
            RequestEntity entity = new StringRequestEntity(jsonString, "application/json", "utf-8");
            post.setRequestEntity(entity);
            HttpClient httpclient = new HttpClient();

            int result = httpclient.executeMethod(post);
        } catch (Exception e) {
            String msg = "Error while  creating project in issue repository for " + application.getName();
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }  finally {
            // Release current connection to the connection pool once you are done
            post.releaseConnection();
        }
    }
}
