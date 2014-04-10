package org.wso2.carbon.appfactory.deployers.clients;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.jaggery.app.mgt.stub.JaggeryAppAdminStub;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.bpel.stub.upload.BPELUploaderStub;
import org.wso2.carbon.dataservices.ui.fileupload.stub.DataServiceFileUploaderStub;
import org.wso2.carbon.dataservices.ui.fileupload.stub.ExceptionException;
import org.wso2.carbon.mediation.configadmin.stub.ConfigServiceAdminStub;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;

/**
 * Uploads a artifact ( i.e. A Carbon application) to specified staging server.
 */
public class ArtifactUploadClient {
    private static Log log = LogFactory.getLog(ArtifactUploadClient.class);

    private String authCookie;
    private String backendServerURL;

    /**
     * @param backendServerURL
     */
    public ArtifactUploadClient(String backendServerURL) {
        if (!backendServerURL.endsWith("/")) {
            backendServerURL += "/";
        }
        this.backendServerURL = backendServerURL;
    }

    /**
     * Authenticates the session using specified credentials
     *
     * @param userName The user name
     * @param password The password
     * @param remoteIp the Staging server's hostname/ip
     * @return
     * @throws Exception
     */
    public boolean authenticate(String userName, String password, String remoteIp)
            throws Exception {
        String serviceURL = backendServerURL + "AuthenticationAdmin";
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
     * @param uploadedFileItems Artifacts to upload
     * @throws Exception An error
     */
   /* public void uploadCarbonApp(UploadedFileItem[] uploadedFileItems) throws Exception {
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
*/
    /**
     * Uploads the webapp
     *
     * @param webappUploadDataItems
     * @throws java.rmi.RemoteException
     */
  /*  public void uploadWebApp(WebappUploadData[] webappUploadDataItems) throws RemoteException {
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
*/
  /*
    
    public void uploadJaxWebApp(WebappUploadData[] webappUploadDataItems) throws RemoteException {
        String serviceURL;
        ServiceClient client;
        Options options;
        WebappAdminStub webappAdminStub;

        serviceURL = backendServerURL + "JaxwsWebappAdmin";
        webappAdminStub = new WebappAdminStub(serviceURL);
        client = webappAdminStub._getServiceClient();
        options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                authCookie);
        webappAdminStub.uploadWebapp(webappUploadDataItems);

    }
*/
    public void uploadJaggeryApp(org.jaggeryjs.jaggery.app.mgt.stub.types.carbon.WebappUploadData[] webappUploadDataItems) throws RemoteException {
  
    }

    public void uploadDBSApp(String fileName, DataHandler dataHandler) throws RemoteException,
            ExceptionException {
        String serviceURL;
        ServiceClient client;
        Options options;

        serviceURL = backendServerURL + "DataServiceFileUploader";
        DataServiceFileUploaderStub dataServiceFileUploaderStub =
                new DataServiceFileUploaderStub(
                        serviceURL);
        client = dataServiceFileUploaderStub._getServiceClient();
        options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, authCookie);
        dataServiceFileUploaderStub.uploadService(fileName, "", dataHandler);

    }

    public void uploadBpel(org.wso2.carbon.bpel.stub.upload.types.UploadedFileItem[] uploadedDataItems) throws RemoteException {
        String serviceURL;
        ServiceClient client;
        Options options;
        BPELUploaderStub bpelUploaderStub;

        serviceURL = backendServerURL + "BPELUploader";
        bpelUploaderStub = new BPELUploaderStub(serviceURL);
        client = bpelUploaderStub._getServiceClient();
        options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                authCookie);
        bpelUploaderStub.uploadService(uploadedDataItems);

    }
    
    public void uploadESBApp(String filePath, DataHandler dataHandler)
    		throws RemoteException, ExceptionException {
    	//Have to deploy to the ESB Server
    	
    	String serviceURL;
        ServiceClient client;
        Options options;

        serviceURL = backendServerURL + "ConfigServiceAdmin";
        
        ConfigServiceAdminStub configServiceAdminStub = new ConfigServiceAdminStub(serviceURL);
   
        client = configServiceAdminStub._getServiceClient();
        options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, authCookie);
        
        OMElement configXMLFile = null ;
		try {
			FileInputStream stream = new FileInputStream(filePath);
			String xmlContent = IOUtils.toString(stream);
			configXMLFile = AXIOMUtil.stringToOM(xmlContent);
			
		} catch (FileNotFoundException e) {
			log.error("Error in File : " + e.getMessage(),e);
		} catch (IOException e) {
			log.error("Error in File Reading : " + e.getMessage(), e);
		} catch (XMLStreamException e) {
			log.error("Error loading XML File : " + e.getMessage(),e);
		}
        
        
        configServiceAdminStub.updateConfiguration(configXMLFile);
        

    	
    	//ConfigServiceAdmin?wsdl
    }

}
