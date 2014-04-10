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

package org.wso2.carbon.appfactory.core.deploy;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.application.mgt.stub.upload.CarbonAppUploaderStub;
import org.wso2.carbon.application.mgt.stub.upload.types.carbon.UploadedFileItem;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.webapp.mgt.stub.WebappAdminStub;
import org.wso2.carbon.webapp.mgt.stub.types.carbon.WebappUploadData;

import java.rmi.RemoteException;

/**
 * Uploads a artifact ( i.e. A Carbon application) to specified staging server.
 */
public class ArtifactUploadClient {

    private String authCookie;
    private String backendServerURL;

    public ArtifactUploadClient(String backendServerURL) {
        if (!backendServerURL.endsWith("/")) {
            backendServerURL += "/";
        }
        this.backendServerURL = backendServerURL;
    }

    /**
     * Authenticates the session using specified credentials
     * 
     * @param userName
     *            The user name
     * @param password
     *            The password
     * @param remoteIp
     *            the Staging server's hostname/ip
     * @return
     * @throws Exception
     */
    public boolean authenticate(String userName, String password, String remoteIp)
                                                                                  throws Exception {
        String serviceURL = backendServerURL + "AuthenticationAdmin";;
        AuthenticationAdminStub authStub = new AuthenticationAdminStub(serviceURL);
        boolean authenticate;

        authStub._getServiceClient().getOptions().setManageSession(true);
        authenticate = authStub.login(userName, password, remoteIp);
        authCookie =
                     (String) authStub._getServiceClient().getServiceContext()
                                      .getProperty(HTTPConstants.COOKIE_STRING);
        return authenticate;
    }

    /**
     * Uploads the specified artifacts
     * 
     * @param uploadedFileItems
     *            Artifacts to upload
     * @throws Exception
     *             An error
     */
    public void uploadCarbonApp(UploadedFileItem[] uploadedFileItems) throws Exception {
        String serviceURL;
        ServiceClient client;
        Options option;
        CarbonAppUploaderStub carbonAppUploader;

        serviceURL = backendServerURL + "CarbonAppUploader";
        carbonAppUploader = new CarbonAppUploaderStub(serviceURL);
        client = carbonAppUploader._getServiceClient();
        option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                           authCookie);
        carbonAppUploader.uploadApp(uploadedFileItems);
    }

    /**
     * Uploads the webapp
     *
     * @param webappUploadDataItems
     * @throws RemoteException
     */
    public void uploadWebApp(WebappUploadData[] webappUploadDataItems) throws RemoteException {
        String serviceURL;
        ServiceClient client;
        Options options;
        WebappAdminStub webappAdminStub;

        serviceURL = backendServerURL + "WebappAdmin";
        webappAdminStub = new WebappAdminStub(serviceURL);
        client = webappAdminStub._getServiceClient();
        options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                            authCookie);
        webappAdminStub.uploadWebapp(webappUploadDataItems);

    }
}