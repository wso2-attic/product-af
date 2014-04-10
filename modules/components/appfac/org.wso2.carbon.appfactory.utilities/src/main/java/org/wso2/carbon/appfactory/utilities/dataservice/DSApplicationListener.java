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
import org.wso2.carbon.appfactory.core.dto.Application;
import org.wso2.carbon.appfactory.core.dto.UserInfo;
import org.wso2.carbon.appfactory.core.dto.Version;

import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;

public class DSApplicationListener extends ApplicationEventsHandler {

    private static final Log log = LogFactory.getLog(DSApplicationListener.class);
    private int priority;

    private static String sampleDbUrl = "jdbc:mysql://wso2admin.czrket1hdi7t.us-east-1.rds.amazonaws.com/testdb";
    private static final String testUserName = "testuser";
    private static final String testUserPw = "testuser123";
    private static final String sampleDbName = "testdb";
    private static final String sampleDSName = "TestDS";

    public DSApplicationListener(int priority) {
        this.priority = priority;
        identifier = "dss";
        try {
            String dbHost = AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty("SampleDbHostUrl");
            sampleDbUrl = "jdbc:mysql://" + dbHost + "/" + sampleDbName;
        } catch (AppFactoryException e) {
            log.error("Error while setting the sample db host name.");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
    @Override
    public void onCreation(Application application, String userName, String tenantDomain) {
        createSampleDataSource(application);
    }

    @Override
    public void onDeletion(Application application, String userName, String tenantDomain) {
        deleteSampleDatasource(application.getId());

    }

    private void createSampleDataSource(Application application) {

        try {
            AppFactoryConfiguration config = AppFactoryUtil.getAppfactoryConfiguration();
            String stages[] = config.getProperties("ApplicationDeployment.DeploymentStage");

            for (String stage : stages) {
                try {
                    String serverUrl = config.getFirstProperty(
                            "ApplicationDeployment.DeploymentStage." + stage + ".DeploymentServerURL");
                    String serviceURL = serverUrl + "NDataSourceAdmin";

                    ServiceClient client = new ServiceClient();

                    client.getOptions()
                            .setTo(new EndpointReference(serviceURL));
                    client.getOptions().setAction("addDataSource");
                    String tenantUserName = AppFactoryUtil.getAdminUsername() + "@" + application.getId();
                    CarbonUtils.setBasicAccessSecurityHeaders(tenantUserName, AppFactoryUtil.getAdminPassword(), client);

                    String payloadString = "<xsd:addDataSource xmlns:xsd=\"http://org.apache.axis2/xsd\"" +
                                           " xmlns:xsd1=\"http://services.core.ndatasource.carbon.wso2.org/xsd\"" +
                                           " xmlns:xsd2=\"http://core.ndatasource.carbon.wso2.org/xsd\">" +
                                           "<xsd:dsmInfo>" +
                                           "<xsd1:definition>" +
                                           "<xsd1:dsXMLConfiguration>" +
                                           " <![CDATA[<configuration>" +
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
                                           "</xsd1:dsXMLConfiguration>" +
                                           "<xsd1:type>RDBMS</xsd1:type>" +
                                           "</xsd1:definition>" +
                                           "<xsd1:description>Sample datasource</xsd1:description>" +
                                           "<xsd1:jndiConfig>" +
                                           "<xsd2:name>" + "jdbc/" + sampleDSName + "</xsd2:name>" +
                                           "<xsd2:useDataSourceFactory>false</xsd2:useDataSourceFactory>" +
                                           "</xsd1:jndiConfig>" +
                                           "<xsd1:name>" + sampleDSName + "</xsd1:name>" +
                                           "<xsd1:system>false</xsd1:system>" +
                                           "</xsd:dsmInfo>" +
                                           "</xsd:addDataSource>";
                    log.info("####################################################");
                    log.info("payload :"+payloadString);

                    OMElement payload = new StAXOMBuilder(new ByteArrayInputStream(payloadString.getBytes())).getDocumentElement();
                    client.sendRobust(payload);
                } catch (AxisFault e) {
                    log.error("Error while creating the sample data source for stage "+stage + " "+e);
                    e.printStackTrace();
                } catch (XMLStreamException e) {
                    log.error("Error while creating the sample data source for stage "+stage + " "+e);
                    e.printStackTrace();
                } catch (Exception e) {
                    log.error("Error while creating the sample data source for stage "+stage + " "+e);
                }
            }

        } catch (AppFactoryException e) {
            log.error("Failed to load appfactory configurations "+e);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void deleteSampleDatasource(String appId) {
        try {
            AppFactoryConfiguration config = AppFactoryUtil.getAppfactoryConfiguration();
            String stages[] = config.getProperties("ApplicationDeployment.DeploymentStage");

            for (String stage : stages) {
                try {
                    String serverUrl = config.getFirstProperty(
                            "ApplicationDeployment.DeploymentStage." + stage + ".DeploymentServerURL");
                    String serviceURL = serverUrl + "NDataSourceAdmin";

                    ServiceClient client = new ServiceClient();

                    client.getOptions()
                            .setTo(new EndpointReference(serviceURL));
                    client.getOptions().setAction("deleteDataSource");
                    String tenantUserName = AppFactoryUtil.getAdminUsername() + "@" + appId;
                    CarbonUtils.setBasicAccessSecurityHeaders(tenantUserName, AppFactoryUtil.getAdminPassword(), client);

                    String payloadString = "<xsd:deleteDataSource xmlns:xsd=\"http://org.apache.axis2/xsd\"" +
                            " xmlns:xsd1=\"http://services.core.ndatasource.carbon.wso2.org/xsd\"" +
                            " xmlns:xsd2=\"http://core.ndatasource.carbon.wso2.org/xsd\">" +
                            "<xsd:dsName>" + sampleDSName + "</xsd:dsName>" +
                            "</xsd:deleteDataSource>";

                    OMElement payload = new StAXOMBuilder(new ByteArrayInputStream(payloadString.getBytes())).getDocumentElement();
                    client.sendRobust(payload);
                } catch (AxisFault e) {
                    log.error("Error while deleting the sample data source for stage "+stage + " "+e);
                    e.printStackTrace();
                } catch (XMLStreamException e) {
                    log.error("Error while deleting the sample data source for stage "+stage + " "+e);
                    e.printStackTrace();
                } catch (Exception e) {
                    log.error("Error while deleting the sample data source for stage "+stage + " "+e);
                }
            }

        } catch (AppFactoryException e) {
            log.error("Failed to load appfactory configurations "+e);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
    public void onVersionCreation(Application application, Version source, Version target,String tenantDomain, String userName)
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
    public int getPriority() {
        return priority;
    }

    @Override
    public boolean hasExecuted(Application application, String userName, String tenantDomain) throws AppFactoryException {
        ///// TODO implement method
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private boolean isSampleDSExists(){
        boolean dsExists = false;


        return dsExists;
    }
	@Override
	public void onForking(Application application, String version,
			String userName, String tenantDomain) throws AppFactoryException {
		// TODO Auto-generated method stub
		
	}
}
