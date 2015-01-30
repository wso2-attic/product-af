/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.appfactory.eventing.jms;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.andes.stub.AndesAdminServiceStub;
import org.wso2.carbon.appfactory.common.AppFactoryException;

import java.rmi.RemoteException;


public class AndesAdminServiceClient {
    private static Log log = LogFactory.getLog(AndesAdminServiceClient.class);
    private AndesAdminServiceStub stub;

    public AndesAdminServiceClient(String remoteServiceURL) throws AppFactoryException {
        if (remoteServiceURL == null || remoteServiceURL.isEmpty()) {
            throw new AppFactoryException("Remote service URL can not be null.");
        }

        if (!remoteServiceURL.endsWith("/")) {
            remoteServiceURL += "/";
        }
        remoteServiceURL += "AndesAdminService";

        try {
            stub = new AndesAdminServiceStub(remoteServiceURL);
        } catch (AxisFault axisFault) {
            log.error("Failed to create AndesAdminServiceStub stub.", axisFault);
            throw new AppFactoryException("Failed to create AndesAdminServiceStub stub.", axisFault);
        }
    }

    public String getAccessToken() throws AppFactoryException {
        try {
            return getStub().getAccessKey();
        } catch (RemoteException e) {
            String error = "Failed to get access token from AndesAdminService.";
            log.error(error, e);
            throw new AppFactoryException(error, e);
        }
    }

	public AndesAdminServiceStub getStub() {
		return stub;
	}
}
