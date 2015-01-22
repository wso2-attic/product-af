/*
 * Copyright 2005-2014 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.appfactory.jenkins.build.notify;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.build.stub.BuildStatusRecieverServiceAppFactoryExceptionException;
import org.wso2.carbon.appfactory.build.stub.BuildStatusRecieverServiceStub;
import org.wso2.carbon.appfactory.build.stub.xsd.BuildStatusBean;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.rmi.RemoteException;

/**
 * The client for notify the build status of an application to the BuildStatusReceiverService
 */
public class BuildStatusReceiverClient {

    private static final Log log = LogFactory.getLog(BuildStatusReceiverClient.class);
    private BuildStatusRecieverServiceStub clientStub;

    /**
     * constructor for BuildStatusReceiverClient
     *
     * @param epr      end point reference of the service
     * @param username admin user name
     * @param password admin password
     * @throws AxisFault
     */
    public BuildStatusReceiverClient(String epr, String username, String password) {
        try {
            clientStub = new BuildStatusRecieverServiceStub(epr);
        } catch (AxisFault e){
            String msg = "Error occurred while initializing BuildStatusRecieverServiceStub";
            log.error(msg, e);
            //the exception is not thrown here, because this should not affect the application deployment process
        }
        ServiceClient client = clientStub._getServiceClient();
        CarbonUtils.setBasicAccessSecurityHeaders(username, password, client);
    }

    /**
     * This method will be executed when a build is completed
     *
     * @param buildStatus    status of the build
     * @param tenantUserName user name of tenant
     */
    public void onBuildCompletion(BuildStatusBean buildStatus, String tenantUserName) {

        if (log.isDebugEnabled()) {
            log.debug(buildStatus.getApplicationId() + " build completed for the buildId " + buildStatus.getBuildId());
        }
        //We have the tenant user name here. So we are splitting it and getting only the tenant domain
        String tenantDomain = MultitenantUtils.getTenantDomain(tenantUserName);

        try {
            clientStub.onBuildCompletion(buildStatus, tenantDomain);
        } catch (RemoteException e) {
            String msg = "Failed to send the build status for application " + buildStatus.getApplicationId();
            log.error(msg, e);
            //the exception is not thrown here, because this should not affect the application deployment process
        } catch (BuildStatusRecieverServiceAppFactoryExceptionException e) {
            String msg = "Error occurred while executing onBuildCompletion method in server side";
            log.error(msg, e);
            //the exception is not thrown here, because this should not affect the application deployment process
        } finally {
            try {
                clientStub._getServiceClient().cleanupTransport();
                clientStub._getServiceClient().cleanup();
            } catch (AxisFault e) {
                log.error("Failed to clean up buildStatusReceiverServiceStub", e);
            }
        }
    }
}
