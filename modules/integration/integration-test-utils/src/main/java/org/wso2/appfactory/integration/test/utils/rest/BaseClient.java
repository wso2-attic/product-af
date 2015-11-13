/*
 * Copyright 2015 WSO2, Inc. (http://wso2.com)
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
package org.wso2.appfactory.integration.test.utils.rest;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.CharEncoding;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestException;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base REST client.
 */
public class BaseClient {
    private static final Log log = LogFactory.getLog(BaseClient.class);

    protected static final String APPMGT_URL_SURFIX = "appmgt/site/blocks/";
    protected static final String CLOUDMGT_URL_SURFIX = "cloudmgt/site/blocks/";
    protected static final String APPMGT_USER_LOGIN = "user/login/ajax/login.jag";
    protected static final String APPMGT_APPLICATION_GET = "application/get/ajax/list.jag";
    protected static final String APPMGT_APPLICATION_ADD = "application/add/ajax/add.jag";
    protected static final String APPMGT_APPLICATION_DELETE= "application/delete/ajax/delete.jag";
    protected static final String EVENTS_PUBLISHING = "events/publish/ajax/publish.jag";
    protected static final String APPMGT_LIFECYCLE_ADD = "lifecycle/add/ajax/add.jag";
    protected static final String APPMGT_REPOSBUILDS_ADD = "reposBuilds/add/ajax/add.jag";
    protected static final String APPMGT_DATASOURCE_ADD = "resources/datasource/add/ajax/add.jag";
    protected static final String APPMGT_DATASOURCE_GET = "resources/datasource/get/ajax/list.jag";
    protected static final String APPMGT_LIFECYCLE_GET = "lifecycle/get/ajax/get.jag";
    protected static final String APPMGT_DATABASE_ADD = "resources/database/add/ajax/add.jag";
    protected static final String APPMGT_DATABASE_DROP = "resources/database/drop/ajax/drop.jag";
    protected static final String APPMGT_DATABASE_USER = "resources/database/users/list/ajax/list.jag";
    protected static final String APPMGT_DATABASE_TEMPLATE = "resources/database/templates/ajax/list.jag";

    protected static final String ISSUETRACKER_URL_SURFIX = "issuetracker/pages/issue";
    protected static final String ISSUETRACKER_USER_LOGIN = "/login.jag";
    protected static final String ISSUETRACKER_ISSUE_SAVE = "/save.jag";
    protected static final String ISSUETRACKER_ISSUE_ADD = "/add.jag";
    protected static final String ISSUETRACKER_ISSUE_GET = "/get.jag";
    protected static final String ISSUETRACKER_ISSUE_GETPROJECTVERSION = "/getProjectVersion.jag";
    protected static final String ISSUETRACKER_URL_COMMENT = "issuetracker/pages/comment/save.jag";

    public static final String HEADER_SET_COOKIE = "Set-Cookie";
    public static final String HEADER_COOKIE = "Cookie";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String MEDIA_TYPE_X_WWW_FORM = "application/x-www-form-urlencoded";

    private String backEndUrl;
    private Map<String, String> requestHeaders = new HashMap<String, String>();

    protected String getBackEndUrl() {
        return backEndUrl;
    }

    protected void setBackEndUrl(String backEndUrl) {
        this.backEndUrl = backEndUrl;
    }

    protected Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    protected void setHTTPHeader(String headerName, String value) {
        requestHeaders.put(headerName, value);
    }

    protected String getHTTPHeader(String headerName) {
        return requestHeaders.get(headerName);
    }

    protected void removeHTTPHeader(String headerName) {
        requestHeaders.remove(headerName);
    }

    /**
     * Construct authenticates REST client to invoke appmgt functions
     *
     * @param backEndUrl backend url
     * @param username   username
     * @param password   password
     * @throws Exception
     */
    public BaseClient(String backEndUrl, String username, String password) throws Exception {
        setBackEndUrl(backEndUrl);

        if (getRequestHeaders().get(HEADER_CONTENT_TYPE) == null) {
            getRequestHeaders().put(HEADER_CONTENT_TYPE, MEDIA_TYPE_X_WWW_FORM);
        }

        login(username, password);
    }

    /**
     * Get session
     *
     * @param responseHeaders response headers
     * @return session
     */
    protected String getSession(Map<String, String> responseHeaders) {
        return responseHeaders.get(HEADER_SET_COOKIE);
    }

    /**
     * Set session
     *
     * @param session session
     */
    protected void setSession(String session) {
        requestHeaders.put(HEADER_COOKIE, session);
    }

    /**
     * Check response errors
     *
     * @param response response
     * @throws org.wso2.appfactory.integration.test.utils.AFIntegrationTestException
     */
    protected void checkErrors(HttpResponse response) throws AFIntegrationTestException {
        JSONObject jsonObject = new JSONObject(response.getData());
        if (jsonObject.keySet().contains("error")) {
            throw new AFIntegrationTestException("Operation not successful: " + jsonObject.get("message").toString());
        }
    }

    /**
     * login to app mgt
     *
     * @param userName username
     * @param password password
     * @throws Exception
     */
    protected void login(String userName, String password) throws Exception {
        HttpResponse response = HttpRequestUtil.doPost(
                new URL(getBackEndUrl() + APPMGT_URL_SURFIX + APPMGT_USER_LOGIN),
                "action=login&userName=" + userName + "&password=" + password, getRequestHeaders());

        if (response.getResponseCode() == HttpStatus.SC_OK && response.getData().equals("true")) {
            String session = getSession(response.getHeaders());
            if (session == null) {
                throw new AFIntegrationTestException("No session cookie found with response");
            }
            setSession(session);
        } else {
            throw new AFIntegrationTestException("Login failed " + response.getData());
        }
    }

    /**
     * Do post request to appfactory.
     *
     * @param urlSuffix url suffix from the block layer
     * @param keyVal  post body
     * @return httpResponse
     */
    public HttpResponse doPostRequest(String urlSuffix, Map<String, String> keyVal) throws AFIntegrationTestException {
        String postBody = generateMsgBody(keyVal);
        try {
            return HttpRequestUtil.doPost(new URL(getBackEndUrl() + APPMGT_URL_SURFIX + urlSuffix), postBody,
                                          getRequestHeaders());
        } catch (Exception e) {
            final String msg = "Error occurred while doing a post :";
            log.error(msg, e);
            throw new AFIntegrationTestException(msg, e);
        }

    }

    /**
     * Do post request to appfactory.
     *
     * @param urlSuffix url suffix from the block layer
     * @param keyVal  post body
     * @return httpResponse
     */
    public HttpResponse doCloudMgtPostRequest(String urlSuffix, Map<String, String> keyVal) throws Exception {
        String postBody = generateMsgBody(keyVal);
        return HttpRequestUtil.doPost(new URL(getBackEndUrl() + CLOUDMGT_URL_SURFIX + urlSuffix), postBody,
                                      getRequestHeaders());

    }

    /**
     * Returns a String that is suitable for use as an application/x-www-form-urlencoded list of parameters in an
     * HTTP PUT or HTTP POST.
     *
     * @param keyVal parameter map
     * @return message body
     */
    public String generateMsgBody(Map<String, String> keyVal) {
        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        for (Map.Entry<String, String> keyValEntry : keyVal.entrySet()) {
            qparams.add(new BasicNameValuePair(keyValEntry.getKey(), keyValEntry.getValue()));
        }
        return URLEncodedUtils.format(qparams, CharEncoding.UTF_8);
    }
}
