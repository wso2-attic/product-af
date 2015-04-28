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
import org.json.JSONObject;
import org.wso2.appfactory.integration.test.utils.AFConstants;
import org.wso2.appfactory.integration.test.utils.AppFactoryIntegrationTestException;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Base REST client.
 */
public class BaseRestClient {

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
     * @throws AppFactoryIntegrationTestException
     */
    protected void checkErrors(HttpResponse response) throws AppFactoryIntegrationTestException {
        JSONObject jsonObject = new JSONObject(response.getData());
        if (jsonObject.keySet().contains("error")) {
            throw new AppFactoryIntegrationTestException(
                "Operation not successful: " + jsonObject.get("message").toString());
        }
    }

}
