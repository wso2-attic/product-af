package org.wso2.carbon.appfactory.utilities.dataservice;
/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.ApplicationEventsHandler;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeManager;
import org.wso2.carbon.appfactory.core.dto.Application;
import org.wso2.carbon.appfactory.core.dto.UserInfo;
import org.wso2.carbon.appfactory.core.dto.Version;
import org.wso2.carbon.appfactory.utilities.security.authorization.RemoteAuthorizationMgtClient;
import org.wso2.carbon.context.CarbonContext;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;

public class DSApplicationListener extends ApplicationEventsHandler {

    private static final Log log = LogFactory.getLog(DSApplicationListener.class);

    private static String sampleDbUrl = "jdbc:mysql://wso2admin.czrket1hdi7t.us-east-1.rds.amazonaws.com/testdb?autoReconnect=true";
    private static String testUserName = "testuser";
    private static String testUserPw = "testuser123";
    private static String sampleDSName = "TestDS";
	private static final String APPLICATION_TYPE_DBS = "dbs";

    public DSApplicationListener(String identifier, int priority) {
        super(identifier, priority);
    }

    @Override
    public void onCreation(Application application, String userName, String tenantDomain, boolean isUploadableAppType) throws AppFactoryException {
        if (APPLICATION_TYPE_DBS.equalsIgnoreCase(application.getType())) {
            createSampleDataSource(application);
        }
    }

    @Override
    public void onDeletion(Application application, String userName, String tenantDomain) throws AppFactoryException {
        if (APPLICATION_TYPE_DBS.equalsIgnoreCase(application.getType())) {
            //deleteSampleDatasource();
        }

    }

    private void createSampleDataSource(Application application) throws AppFactoryException {
	    try {
		    String dbHost = ApplicationTypeManager.getInstance().getApplicationTypeBean(APPLICATION_TYPE_DBS).getProperty("SampleDbHostUrl").toString();
		    testUserName = ApplicationTypeManager.getInstance().getApplicationTypeBean(APPLICATION_TYPE_DBS).getProperty("SampleDbUser").toString();
		    testUserPw = ApplicationTypeManager.getInstance().getApplicationTypeBean(APPLICATION_TYPE_DBS).getProperty("SampleDbUserPassword").toString();
		    String sampleDbName = ApplicationTypeManager.getInstance().getApplicationTypeBean(APPLICATION_TYPE_DBS).getProperty("SampleDbName").toString();
		    sampleDSName = ApplicationTypeManager.getInstance().getApplicationTypeBean(APPLICATION_TYPE_DBS).getProperty("SampleDatasourceName").toString();
		    sampleDbUrl = "jdbc:mysql://" + dbHost + "/" + sampleDbName;
	    } catch (AppFactoryException e) {
		    log.error("Error while setting the sample data source properties", e);
	    }

        AppFactoryConfiguration config = AppFactoryUtil.getAppfactoryConfiguration();
        String stages[] = config.getProperties("ApplicationDeployment.DeploymentStage");
        String tenantUserName = CarbonContext.getThreadLocalCarbonContext().getUsername() + "@" +
                CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        ServiceClient client = null;
        OMElement payload = null;

        for (String stage : stages) {

            try {

                String serverUrl = config.getFirstProperty(
                        "ApplicationDeployment.DeploymentStage." + stage + ".DeploymentServerURL");
                String serviceURL = serverUrl + "AppFactoryNDataSourceAdmin";

	            RemoteAuthorizationMgtClient authorizationMgtClient = new RemoteAuthorizationMgtClient(serverUrl);
	            AppFactoryUtil.setAuthHeaders(authorizationMgtClient.getStub()._getServiceClient(), tenantUserName);

                client = new ServiceClient();

                client.getOptions()
                        .setTo(new EndpointReference(serviceURL));
                client.getOptions().setAction("addDataSource");
                AppFactoryUtil.setAuthHeaders(client, tenantUserName);

	            String payloadString = "<p:addDataSource xmlns:p=\"http://datasource.ext.appfactory.carbon.wso2.org\">"+
	                             "<ax29:dsmInfo xmlns:ax29=\"http://datasource.ext.appfactory.carbon.wso2.org\">" +
	                             "<ax29:definition xmlns:ax29=\"http://services.core.ndatasource.carbon.wso2.org/xsd\">"+
	                             "<xs:dsXMLConfiguration xmlns:xs=\"http://services.core.ndatasource.carbon.wso2.org/xsd\">"+
	                             "<![CDATA[<configuration>" +
	                             "<url>" + sampleDbUrl + "</url>" +
	                             "<username>" + testUserName + "</username>" +
	                             "<password>" + testUserPw + "</password>" +
	                             "<driverClassName>com.mysql.jdbc.Driver</driverClassName>" +
	                             "<maxActive>50</maxActive>" +
	                             "<maxWait>60000</maxWait>" +
	                             "<testOnBorrow>true</testOnBorrow>" +
	                             "<validationQuery>SELECT 1</validationQuery>" +
	                             "<validationInterval>30000</validationInterval>" +
	                             "</configuration>]]>" +
	                             "</xs:dsXMLConfiguration>"+
	                             "<xs:type xmlns:xs=\"http://services.core.ndatasource.carbon.wso2.org/xsd\">RDBMS</xs:type>"+
	                             "</ax29:definition>"+
	                             "<xs:description xmlns:xs=\"http://services.core.ndatasource.carbon.wso2.org/xsd\">Sample datasource</xs:description>"+
	                             "<ax210:jndiConfig xmlns:ax210=\"http://services.core.ndatasource.carbon.wso2.org/xsd\">"+
	                             "<xs:name xmlns:xs=\"http://core.ndatasource.carbon.wso2.org/xsd\">"+ "jdbc/"+sampleDSName+"</xs:name>"+
	                             "<xs:useDataSourceFactory xmlns:xs=\"http://core.ndatasource.carbon.wso2.org/xsd\">false</xs:useDataSourceFactory>" +
	                             "</ax210:jndiConfig>"+
	                             "<xs:name xmlns:xs=\"http://services.core.ndatasource.carbon.wso2.org/xsd\">"+sampleDSName+"</xs:name>"+
	                             "<xs:system xmlns:xs=\"http://services.core.ndatasource.carbon.wso2.org/xsd\">false</xs:system>"+
	                             "</ax29:dsmInfo>"+
	                             "<xs:applicationID xmlns:xs=\"http://datasource.ext.appfactory.carbon.wso2.org\">"+application.getId()+"</xs:applicationID>"+
	                             "</p:addDataSource>";

                if (log.isDebugEnabled()) {
                    log.debug("payload to create sample data source:" + payloadString);
                }

                payload = new StAXOMBuilder(new ByteArrayInputStream(payloadString.getBytes())).getDocumentElement();
                client.sendRobust(payload);

            } catch (AxisFault e) {
                String errorMsg = "Error while creating the sample data source for stage " + stage;
                log.error(errorMsg);

                //if the tenant is unloaded in app server it will throw AxisFault exception. So retrying again.
                if(client != null && payload != null) {
                    log.info("Resending the request to create the sample data source for stage : " + stage);
                    try {
                        client.sendRobust(payload);
                    } catch (AxisFault axisFault) {
                        String msg =
                                "Error during the second attempt of creating the sample data source for stage " + stage;
                        log.error(msg, axisFault);
                        throw new AppFactoryException(msg, axisFault);
                    }
                } else {
                    throw new AppFactoryException(errorMsg, e);
                }
            } catch (XMLStreamException e) {
                log.error("Error while creating the sample data source for stage " + stage + " " + e);
	            throw new AppFactoryException("Error while creating the sample data source for stage " + stage + " " + e);
            } catch (Exception e) {
                log.error("Error while creating the sample data source for stage " + stage + " " + e);
	            throw new AppFactoryException("Error while creating the sample data source for stage " + stage + " " + e);
            }
        }
    }

    private void deleteSampleDatasource() throws AppFactoryException {

        AppFactoryConfiguration config = AppFactoryUtil.getAppfactoryConfiguration();
        String stages[] = config.getProperties("ApplicationDeployment.DeploymentStage");
        String tenantUserName = CarbonContext.getThreadLocalCarbonContext().getUsername() + "@" +
                CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        for (String stage : stages) {
            try {

                String serverUrl = config.getFirstProperty(
                        "ApplicationDeployment.DeploymentStage." + stage + ".DeploymentServerURL");
                String serviceURL = serverUrl + "NDataSourceAdmin";

                ServiceClient client = new ServiceClient();

                client.getOptions()
                        .setTo(new EndpointReference(serviceURL));
                client.getOptions().setAction("deleteDataSource");
                AppFactoryUtil.setAuthHeaders(client, tenantUserName);
                String payloadString = "<xsd:deleteDataSource xmlns:xsd=\"http://org.apache.axis2/xsd\"" +
                        " xmlns:xsd1=\"http://services.core.ndatasource.carbon.wso2.org/xsd\"" +
                        " xmlns:xsd2=\"http://core.ndatasource.carbon.wso2.org/xsd\">" +
                        "<xsd:dsName>" + sampleDSName + "</xsd:dsName>" +
                        "</xsd:deleteDataSource>";

                OMElement payload = new StAXOMBuilder(new ByteArrayInputStream(payloadString.getBytes())).getDocumentElement();
                client.sendRobust(payload);

            } catch (AxisFault e) {
                log.error("Error while deleting the sample data source for stage " + stage + " " + e);
                throw new AppFactoryException("Error while deleting the sample data source for stage " + stage + " " + e);
            } catch (XMLStreamException e) {
                log.error("Error while deleting the sample data source for stage " + stage + " " + e);
                throw new AppFactoryException("Error while deleting the sample data source for stage " + stage + " " + e);
            } catch (Exception e) {
                log.error("Error while deleting the sample data source for stage " + stage + " " + e);
                throw new AppFactoryException("Error while deleting the sample data source for stage " + stage + " " + e);
            }
        }


    }

    @Override
    public void onUserAddition(Application application, UserInfo user, String tenantDomain) throws AppFactoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onUserDeletion(Application application, UserInfo user, String tenantDomain) throws AppFactoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onUserUpdate(Application application, UserInfo user, String tenantDomain) throws AppFactoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onRevoke(Application application, String tenantDomain) throws AppFactoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onVersionCreation(Application application, Version source, Version target, String tenantDomain, String userName)
            throws AppFactoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onLifeCycleStageChange(Application application, Version version,
                                       String previosStage, String nextStage, String tenantDomain)
            throws AppFactoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean hasExecuted(Application application, String userName, String tenantDomain) throws AppFactoryException {
        ///// TODO implement method
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onFork(Application application, String userName, String tenantDomain, String version, String[] forkedUsers) throws AppFactoryException {
        // TODO Auto-generated method stub

    }
}
