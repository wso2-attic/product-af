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
package org.wso2.appfactory.integration.test.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;

import java.rmi.RemoteException;

/**
 * Base class for App Factory Integration tests
 */
public class AFIntegrationTest {

    private static final Log log = LogFactory.getLog(AFIntegrationTest.class);

    /**
     * Clean up the changes
     */
    protected void cleanup() {
        log.info("cleanup called");
    }

    /**
     * Login as any user
     *
     * @param backendUrl backend url
     * @param username   username
     * @param password   password
     * @param host       host
     * @return session
     * @throws RemoteException
     * @throws LoginAuthenticationExceptionException
     */
    protected String login(String backendUrl, String username, String password, String host)
        throws RemoteException, LoginAuthenticationExceptionException {
        AuthenticatorClient client = new AuthenticatorClient(backendUrl + "services/");
        return client.login(username, password, host);
    }

}

