/*
 * Copyright 2015 WSO2, Inc. (http://wso2.com)
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.appfactory.integration.test.utils.rest;

import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import java.net.URL;


public class APIRestClient extends BaseClient {

    private static HTTPHandler httpHandler;
    private static String session;
    public static final String APPMGT_ADDAPI_URL_SURFIX="/publisher/site/blocks/item-add/ajax/add.jag";
    public static final String APPMGT_LOGGIN_URL_SURFIX="/publisher/site/blocks/user/login/ajax/login.jag";
    public static final String APPMGT_PUBLISHAPI_URL_SURFIX="/publisher/site/blocks/life-cycles/ajax/life-cycles.jag";
    public static final String APPMGT_SUBSCRIBE_API_URL_SURFIX="/subscription/subscription-add/ajax/subscription-add.jag";

    /**
     * Construct authenticates REST client to invoke appmgt functions
     *
     * @param backEndUrl backend url
     * @param username   username
     * @param password   password
     * @throws Exception
     */
    public APIRestClient(String backEndUrl, String username, String password) throws Exception {
        super(backEndUrl, username, password);
    }


    /**
     * Get app info
     * @param userName   username
     * @param password   password
     * @param addAPIPayload payload for add API
     * @param subscribeAPIPayload payload for subscribe API
     * @param publishAPIPayload  payload for publish api in APIM store
     *
     */
    public  void addAPI(String userName,String password,String addAPIPayload,String publishAPIPayload,
                        String subscribeAPIPayload) throws Exception {
        //logging to APIM
        HttpResponse logging_response = HttpRequestUtil.doPost(
                new URL(getBackEndUrl() + APPMGT_LOGGIN_URL_SURFIX),
                "action=login&username=" + userName + "&password=" + password);

        if (logging_response.getResponseCode() == 200) {
            session = getSession(logging_response.getHeaders());
            session = session.split("=")[1].split(";")[0];
        } else {
            throw new Exception("Get Api Information failed> " + logging_response.getData());
        }

        //add API

        httpHandler.doPostHttp(getBackEndUrl()+APPMGT_ADDAPI_URL_SURFIX,addAPIPayload , session,
                "application/x-www-form-urlencoded; charset=UTF-8");

        //publish to store

        httpHandler.doPostHttp(getBackEndUrl()+APPMGT_PUBLISHAPI_URL_SURFIX,publishAPIPayload,session,
                "application/x-www-form-urlencoded; charset=UTF-8");

        //subscribe to API
        System.out.println("---subscribe to store----");
        httpHandler.doPostHttp(getBackEndUrl()+APPMGT_SUBSCRIBE_API_URL_SURFIX,subscribeAPIPayload,session,
                "application/x-www-form-urlencoded; charset=UTF-8");

    }



}

