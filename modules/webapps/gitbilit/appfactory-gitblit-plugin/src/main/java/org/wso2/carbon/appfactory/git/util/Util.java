package org.wso2.carbon.appfactory.git.util;

import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;

/**
 * Utility class
 */
public class Util {
    public static void setMaxTotalConnection(ServiceClient client){
        ServiceContext context=client.getServiceContext();
        MultiThreadedHttpConnectionManager connManager = (MultiThreadedHttpConnectionManager)context.getProperty(HTTPConstants.MULTITHREAD_HTTP_CONNECTION_MANAGER);
        if(connManager == null) {
            connManager = new MultiThreadedHttpConnectionManager();
            context.setProperty(HTTPConstants.MULTITHREAD_HTTP_CONNECTION_MANAGER, connManager);
            connManager.getParams().setMaxTotalConnections(200);
            connManager.getParams().setMaxConnectionsPerHost(HostConfiguration.ANY_HOST_CONFIGURATION, 200);
        }
    }
}
