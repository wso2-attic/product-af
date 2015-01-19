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

package org.wso2.carbon.appfactory.application.mgt.service.applicationqueue;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.rampart.RampartMessageData;
import org.wso2.carbon.appfactory.application.mgt.internal.ServiceReferenceHolder;
import org.wso2.carbon.appfactory.application.mgt.service.ApplicationInfoBean;
import org.wso2.carbon.appfactory.application.mgt.util.Util;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.core.queue.Executor;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;

/**
 * Executor implements to the ApplicationCreation in AppFactory
 *
 */
public class ApplicationExecutor implements Executor<ApplicationInfoBean> {

    private final Log log = LogFactory.getLog(ApplicationExecutor.class);

    private static final String APPLICATION_CREATION_SERVICE = "CreateApplication";
    private static final String BPEL_POLICY = "/appfactory/bpel-policy.xml";

    @Override
    public void execute(ApplicationInfoBean applicationInfoBean) throws Exception {
        callApplicationCreateBPEL(applicationInfoBean);
    }

    public String getProperty(String propertyName) {
        AppFactoryConfiguration configuration = Util.getConfiguration();
        return configuration.getFirstProperty(propertyName);

    }

    /**
     * To Create the application in appfactory, need to call the
     * CreateApplication BPEL.
     *
     * @param applicationInfoBean is application detail object to create an application.
     */
    private void callApplicationCreateBPEL(ApplicationInfoBean applicationInfoBean)
            throws Exception {

        final String EPR =
                getProperty(AppFactoryConstants.BPS_SERVER_URL) +
                ApplicationExecutor.APPLICATION_CREATION_SERVICE;
        String userName = getProperty(AppFactoryConstants.SERVER_ADMIN_NAME);
        String password = getProperty(AppFactoryConstants.SERVER_ADMIN_PASSWORD);
        String configs = CarbonUtils.getCarbonConfigDirPath();

        ServiceClient client = null;
        try {
            ServiceReferenceHolder holder = ServiceReferenceHolder.getInstance();
            ConfigurationContext context =
                    holder.getConfigContextService()
                            .getClientConfigContext();
            client = new ServiceClient(context, null);

            // Set the endpoint address
            client.getOptions().setTo(new EndpointReference(EPR));
            client.engageModule("rampart");
            client.engageModule("addressing");

            client.getOptions().setUserName(userName);
            client.getOptions().setPassword(password);
            client.getOptions().setTimeOutInMilliSeconds(1000000);

            Policy policy = loadPolicy(configs + ApplicationExecutor.BPEL_POLICY);

            client.getOptions().setAction("http://wso2.org");
            client.getOptions().setProperty(RampartMessageData.KEY_RAMPART_POLICY, policy);

            // call bpel ApplicationCreation using applicationInfoBean
            client.fireAndForget(getPayload(applicationInfoBean));
            client.cleanup();
            log.info("Application creation is initiated for application : " +
                     applicationInfoBean.getApplicationKey());

        } catch (Exception e) {
            String errorMsg = "Error in BPEL calling," + e.getMessage();
            log.error(errorMsg, e);
            throw new Exception(errorMsg, e);

        } finally {
            try {
                client.cleanup();
            } catch (Exception e) {
                log.info("Error in cleanup resources, " + e.getMessage());
            }
        }

    }

    /**
     * Generate Policy Document
     *
     * @param xmlPath
     * @return
     * @throws Exception
     */
    private static Policy loadPolicy(String xmlPath) throws Exception {
        StAXOMBuilder builder = new StAXOMBuilder(xmlPath);
        return PolicyEngine.getPolicy(builder.getDocumentElement());
    }

    /**
     * Generate Payload for the CreateApplicationRequest service operation
     *
     * @param applicationInfoBean
     * @return
     * @throws XMLStreamException
     * @throws javax.xml.stream.XMLStreamException
     *
     */
    private static OMElement getPayload(ApplicationInfoBean applicationInfoBean)
            throws XMLStreamException,
                   javax.xml.stream.XMLStreamException {

        String payload =
                "<p:CreateApplicationRequest xmlns:p=\"http://wso2.org\">\n" +
                "      <applicationId xmlns=\"http://wso2.org\">" +
                applicationInfoBean.getApplicationKey() +
                "</applicationId>\n" +
                "      <userName xmlns=\"http://wso2.org\">" +
                applicationInfoBean.getOwnerUserName() +
                "</userName>\n" +
                "      <repositoryType xmlns=\"http://wso2.org\">" +
                applicationInfoBean.getRepositoryType() +
                "</repositoryType>\n" +
                "<domainName xmlns=\"http://wso2.org\">" +
                        CarbonContext.getThreadLocalCarbonContext().getTenantDomain() +
                "</domainName>\n" +
                "      <adminUserName xmlns=\"http://wso2.org\">" +
                Util.getConfiguration()
                        .getFirstProperty(AppFactoryConstants.SERVER_ADMIN_NAME) +
                "</adminUserName>\n" + "   </p:CreateApplicationRequest>";
        return new StAXOMBuilder(new ByteArrayInputStream(payload.getBytes())).getDocumentElement();
    }

}
