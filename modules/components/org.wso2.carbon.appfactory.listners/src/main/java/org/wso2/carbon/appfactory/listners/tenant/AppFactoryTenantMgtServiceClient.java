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
package org.wso2.carbon.appfactory.listners.tenant;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.listners.util.Util;
import org.apache.stratos.tenant.mgt.stub.TenantMgtAdminServiceStub;
import org.apache.stratos.tenant.mgt.stub.beans.xsd.TenantInfoBean;
import org.wso2.carbon.utils.CarbonUtils;

import java.rmi.RemoteException;

/**
 *
 */
public class AppFactoryTenantMgtServiceClient {
    private static final Log log = LogFactory.getLog(AppFactoryTenantMgtServiceClient.class);
    //private ServiceClient client = null;;
    TenantMgtAdminServiceStub stub;

    public AppFactoryTenantMgtServiceClient(String serverURL, String username, String password) throws Exception {
        String epr = serverURL + "/services/TenantMgtAdminService";
        try {
           /* client=new ServiceClient(Util.getConfigurationContextService().getClientConfigContext(),null);
            client.getOptions().setTo(new EndpointReference(epr));
            System.out.println(client.getOptions().getSoapVersionURI());


            CarbonUtils.setBasicAccessSecurityHeaders(username, password, client);*/
            stub = new TenantMgtAdminServiceStub(Util.getConfigurationContextService().getClientConfigContext(), epr);
            CarbonUtils.setBasicAccessSecurityHeaders(username, password, stub._getServiceClient());

        } catch (AxisFault axisFault) {
            String msg = "Error while creating service client for AppFactoryTenantMgtAdminService";
            log.error(msg, axisFault);
            throw new Exception(msg, axisFault);
        }

    }

    public void doPostTenantActivation(TenantInfoBean tenantInfoBean) throws Exception {
       /* try {
            client.sendReceive(getPayload(tenantInfoBean));
        } catch (RemoteException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (XMLStreamException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }*/
        try {
            stub.addTenant(tenantInfoBean);
        } catch (RemoteException e) {
            String msg = "Remote Error while creating invoking service method";
            log.error(msg, e);
            throw new Exception(msg, e);
        }
    }
   /* private  OMElement getPayload(TenantInfoBean tenant)
            throws XMLStreamException,
            javax.xml.stream.XMLStreamException {
            client.getOptions().setAction("urn:doPostTenantActivation");
        String payload =" <p:doPostTenantActivation xmlns:p=\"http://services.mgt.tenant.appfactory.carbon.wso2.org\">\n" +
                        "      <!--0 to 1 occurrence-->\n" +
                        "      <ax27:tenantInfoBean xmlns:ax27=\"http://services.mgt.tenant.appfactory.carbon.wso2.org\">\n" +
                        "         <!--0 to 1 occurrence-->\n" +
                        "         <xs:active xmlns:xs=\"http://beans.common.stratos.carbon.wso2.org/xsd\">"+tenant.getActive()+"</xs:active>\n" +
                        "         <!--0 to 1 occurrence-->\n" +
                        "         <xs:admin xmlns:xs=\"http://beans.common.stratos.carbon.wso2.org/xsd\">"+tenant.getAdmin()+"</xs:admin>\n" +
                        "         <!--0 to 1 occurrence-->\n" +
                        "         <xs:adminPassword xmlns:xs=\"http://beans.common.stratos.carbon.wso2.org/xsd\">"+tenant.getAdminPassword()+"</xs:adminPassword>\n" +
                        "         <!--0 to 1 occurrence-->\n" +
                        "         <xs:createdDate xmlns:xs=\"http://beans.common.stratos.carbon.wso2.org/xsd\"></xs:createdDate>\n" +
                        "         <!--0 to 1 occurrence-->\n" +
                        "         <xs:email xmlns:xs=\"http://beans.common.stratos.carbon.wso2.org/xsd\">"+tenant.getEmail()+"</xs:email>\n" +
                        "         <!--0 to 1 occurrence-->\n" +
                        "         <xs:firstname xmlns:xs=\"http://beans.common.stratos.carbon.wso2.org/xsd\">"+tenant.getFirstname()+"</xs:firstname>\n" +
                        "         <!--0 to 1 occurrence-->\n" +
                        "         <xs:lastname xmlns:xs=\"http://beans.common.stratos.carbon.wso2.org/xsd\">"+tenant.getFirstname()+"</xs:lastname>\n" +
                        "         <!--0 to 1 occurrence-->\n" +
                        "         <xs:originatedService xmlns:xs=\"http://beans.common.stratos.carbon.wso2.org/xsd\">"+tenant.getOriginatedService()+"</xs:originatedService>\n" +
                        "         <!--0 to 1 occurrence-->\n" +
                        "         <xs:successKey xmlns:xs=\"http://beans.common.stratos.carbon.wso2.org/xsd\">"+tenant.getSuccessKey()+"</xs:successKey>\n" +
                        "         <!--0 to 1 occurrence-->\n" +
                        "         <xs:tenantDomain xmlns:xs=\"http://beans.common.stratos.carbon.wso2.org/xsd\">"+tenant.getTenantDomain()+"</xs:tenantDomain>\n" +
                        "         <!--0 to 1 occurrence-->\n" +
                        "         <xs:tenantId xmlns:xs=\"http://beans.common.stratos.carbon.wso2.org/xsd\">"+tenant.getTenantId()+"</xs:tenantId>\n" +
                        "         <!--0 to 1 occurrence-->\n" +
                        "         <xs:usagePlan xmlns:xs=\"http://beans.common.stratos.carbon.wso2.org/xsd\">"+tenant.getUsagePlan()+"</xs:usagePlan>\n" +
                        "      </ax27:tenantInfoBean>\n" +
                        "   </p:doPostTenantActivation>";
        return new StAXOMBuilder(new ByteArrayInputStream(payload.getBytes())).getDocumentElement();
    }*/

}
