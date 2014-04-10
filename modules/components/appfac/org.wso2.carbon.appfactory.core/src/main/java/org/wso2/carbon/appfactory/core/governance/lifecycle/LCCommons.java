//package org.wso2.carbon.appfactory.core.governance.lifecycle;
//
//import org.apache.axiom.om.impl.builder.StAXOMBuilder;
//import org.apache.axis2.AxisFault;
//import org.apache.axis2.addressing.EndpointReference;
//import org.apache.axis2.client.ServiceClient;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
//import org.wso2.carbon.appfactory.common.AppFactoryConstants;
//import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
//import org.wso2.carbon.utils.CarbonUtils;
//
//import javax.xml.stream.XMLStreamException;
//import java.io.ByteArrayInputStream;
//
//
//public class LCCommons {
//
//    private static final Log log = LogFactory.getLog(LCCommons.class);
//
//      public static void executeAppDeletion(final String stage, final String applicationId) {
//
//        AppFactoryConfiguration configuration = ServiceReferenceHolder.getInstance().getAppFactoryConfiguration();
//        final String EPR = configuration.getFirstProperty(AppFactoryConstants.GREG_SERVER_URL) + "ApplicationDeployer";
//        final String username = configuration.getFirstProperty(AppFactoryConstants.SERVER_ADMIN_NAME);
//        final String password = configuration.getFirstProperty(AppFactoryConstants.SERVER_ADMIN_PASSWORD);
//        new Thread(new Runnable() {
//            public void run() {
//                try {
//                    Thread.sleep(5000);
//                } catch (InterruptedException ignored) {
//                }
//                try {
//                    //Create a service client
//                    ServiceClient client = new ServiceClient();
//
//                    //Set the endpoint address
//                    client.getOptions().setTo(new EndpointReference(EPR));
//
//                    CarbonUtils.setBasicAccessSecurityHeaders(username, password, client);
//                    //Make the request and get the response
//                    String payload = "   <p:unDeployArtifact xmlns:p=\"http://deploy.core.appfactory.carbon.wso2.org\">\n" +
//                                     "      <xs:stage xmlns:xs=\"http://deploy.core.appfactory.carbon.wso2.org\">" + stage + "</xs:stage>\n" +
//                                     "      <xs:applicationId xmlns:xs=\"http://deploy.core.appfactory.carbon.wso2.org\">" + applicationId + "</xs:applicationId>\n" +
//                                     "   </p:unDeployArtifact>";
//
//                    //Make the request and get the response
//                    client.sendRobust(new StAXOMBuilder(new ByteArrayInputStream(payload.getBytes())).getDocumentElement());
//                } catch (AxisFault e) {
//                    log.error(e);
//                    e.printStackTrace();
//                } catch (XMLStreamException e) {
//                    log.error(e);
//                }
//            }
//        }).start();
//    }
//}
