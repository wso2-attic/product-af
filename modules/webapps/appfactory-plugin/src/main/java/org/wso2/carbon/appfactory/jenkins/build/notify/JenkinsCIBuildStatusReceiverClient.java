package org.wso2.carbon.appfactory.jenkins.build.notify;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.wso2.carbon.appfactory.jenkins.build.stub.JenkinsCIBuildStatusRecieverServiceStub;
import org.wso2.carbon.appfactory.jenkins.build.stub.xsd.BuildStatusBean;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JenkinsCIBuildStatusReceiverClient {

    private static final Logger log = Logger.getLogger(JenkinsCIBuildStatusReceiverClient.
                                                               class.getName());
    private JenkinsCIBuildStatusRecieverServiceStub clientStub;

    public JenkinsCIBuildStatusReceiverClient(String epr, String username, String password)
            throws AxisFault {
        clientStub = new JenkinsCIBuildStatusRecieverServiceStub(epr);
        ServiceClient client = clientStub._getServiceClient();
        CarbonUtils.setBasicAccessSecurityHeaders(username, password, client);
    }

    /**
     * sends the build results to appfactory side
     * @param buildStatus
     */
    public void onBuildCompletion(BuildStatusBean buildStatus, String tenantUserName) {
        log.info(buildStatus.getApplicationId() + " build completed for the buildId " +
                 buildStatus.getBuildId());
//        Note that we have the tenant user name here. So we are splitting it and getting only the tenant domain
        String tenantDomain = MultitenantUtils.getTenantDomain(tenantUserName);

        try {
            clientStub.onBuildCompletion(buildStatus, null, null, tenantDomain);
        } catch (RemoteException e) {
            log.log(Level.SEVERE, "Failed to send build status in failed build for " +
                                  buildStatus.getApplicationId() + ":" + e);
        }finally {
            try {
                clientStub._getServiceClient().cleanupTransport();
                clientStub._getServiceClient().cleanup();
            } catch (AxisFault ignore) {
                log.warning("Failed to clean up jenkinsCIBuildStatusReceiverServiceStub.");
            }
        }
    }
}
