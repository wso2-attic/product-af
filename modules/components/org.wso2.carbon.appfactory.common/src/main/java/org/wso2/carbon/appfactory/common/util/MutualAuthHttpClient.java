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

package org.wso2.carbon.appfactory.common.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.context.CarbonContext;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Wrapper to do REST call with HttpClient via mutual authentication
 */
public class MutualAuthHttpClient {
    private static final Log log = LogFactory.getLog(MutualAuthHttpClient.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * Send rest POST request to Stratos SM.
     *
     * @param body        message body
     * @param endPointUrl end point to send the message
     * @throws org.wso2.carbon.appfactory.common.AppFactoryException
     */
    public static ServerResponse sendPostRequest(String body, String endPointUrl, String username)
            throws AppFactoryException {
        HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        PostMethod postMethod = new PostMethod(endPointUrl);

        // password as garbage value since we authenticate with mutual ssl
        postMethod.setRequestHeader(AUTHORIZATION_HEADER, getAuthHeaderValue(username));
        StringRequestEntity requestEntity;
        try {
            requestEntity = new StringRequestEntity(body, "application/json", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            String msg = "Error while setting parameters";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        postMethod.setRequestEntity(requestEntity);
        return send(httpClient, postMethod);
    }

    /**
     * Send REST DELETE request to stratos SM.
     *
     * @param endPointUrl end point to send the message
     * @throws org.wso2.carbon.appfactory.common.AppFactoryException
     */
    public static ServerResponse sendDeleteRequest(String endPointUrl, String username)
            throws AppFactoryException {
        HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        DeleteMethod deleteMethod = new DeleteMethod(endPointUrl);
        deleteMethod.setRequestHeader(AUTHORIZATION_HEADER, getAuthHeaderValue(username));
        return send(httpClient, deleteMethod);
    }

    /**
     * Send rest GET request to Stratos SM
     *
     * @param endPointUrl end point to send the message
     * @throws org.wso2.carbon.appfactory.common.AppFactoryException
     */
    public static ServerResponse sendGetRequest(String endPointUrl, String username)
            throws AppFactoryException {
        HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        GetMethod getMethod = new GetMethod(endPointUrl);
        getMethod.setRequestHeader(AUTHORIZATION_HEADER, getAuthHeaderValue(username));
        return send(httpClient, getMethod);
    }

    /**
     * Send rest request.
     *
     * @param httpClient client object
     * @param method     method type
     * @throws org.wso2.carbon.appfactory.common.AppFactoryException
     */
    private static ServerResponse send(HttpClient httpClient, HttpMethodBase method) throws AppFactoryException {
        int responseCode;
        String responseString = null;
        try {
            responseCode = httpClient.executeMethod(method);
        } catch (IOException e) {
            String msg = "Error occurred while executing method " + method.getName();
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        try {
            responseString = method.getResponseBodyAsString();
        } catch (IOException e) {
            String msg = "error while getting response as String for " + method.getName();
            log.error(msg, e);
            throw new AppFactoryException(msg, e);

        } finally {
            method.releaseConnection();
        }
        if (log.isDebugEnabled()) {
            log.debug("Response id: " + responseCode + " message:  " + responseString);
        }
        return new ServerResponse(responseString, responseCode);
    }

    /**
     * Get auth header value
     *
     * @return auth header in basic encode
     */
    private static String getAuthHeaderValue(String username) {
        //get user name from thread local if it's empty
        if (StringUtils.isNotBlank(username)) {
            username = CarbonContext.getThreadLocalCarbonContext().getUsername() + "@"
                       + CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        }
        // password as garbage value since we authenticate with mutual ssl.
        // We need to set the auth header for requests with the username.
        byte[] getUserPasswordInBytes = (username + ":" + AppFactoryConstants.STRATOS_REST_SERVICE_PASSWORD).getBytes();
        String encodedValue = new String(Base64.encodeBase64(getUserPasswordInBytes));
        return "Basic " + encodedValue;
    }
}
