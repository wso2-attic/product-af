/*
 * Copyright 2005-2013 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.appfactory.core.workflow;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.rampart.RampartMessageData;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.appfactory.core.workflow.dto.TenantCreationWorkflowDTO;
import org.wso2.carbon.appfactory.core.workflow.dto.WorkflowDTO;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;
import java.text.MessageFormat;

/**
 * The executor class for tenant creation external execution
 */
public class TenantCreationExternalWorkflowExecutor implements WorkflowExecutor {

    private static Log log = LogFactory.getLog(TenantCreationExternalWorkflowExecutor.class);

    private static Policy loadPolicy(String xmlPath) throws FileNotFoundException, XMLStreamException {
        StAXOMBuilder builder = new StAXOMBuilder(xmlPath);
        return PolicyEngine.getPolicy(builder.getDocumentElement());
    }

    /**
     * Execute tenant creation external workflowDTO process
     *
     * @param workflowDTO the tenant creation workflowDTO dto
     * @throws AppFactoryException AppFactoryException exception throws if not success the execution
     */
    @Override public void execute(WorkflowDTO workflowDTO) throws AppFactoryException {

        if (log.isDebugEnabled()) {
            String message =
                    "Executing tenant creation external workflow : tenant domain : " + workflowDTO.getTenantDomain();
            log.debug(message);
        }
        String EPR;

        if (!(workflowDTO instanceof TenantCreationWorkflowDTO)) {
            String message = "TenantCreationWorkflowDTO type is expected but unexpected type is passed to the execute"
                    + " method : tenant domain : " + workflowDTO.getTenantDomain();
            throw new IllegalArgumentException(message);
        }

        TenantCreationWorkflowDTO tenantCreationWorkflow = (TenantCreationWorkflowDTO) workflowDTO;

        try {
            EPR = AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(WorkflowConstant.BPS_SERVER_URL)
                    + "CreateTenant";
            if (StringUtils.isBlank(EPR)) {
                String message = "Can not read EPR value from the appfactory.xml configuration, the value is null or "
                        + "empty : tenant domain : " + workflowDTO.getTenantDomain();
                throw new AppFactoryException(message);
            }

        } catch (AppFactoryException e) {
            String message =
                    "Unable to read appfactory.xml configuration : tenant domain : " + workflowDTO.getTenantDomain();
            throw new AppFactoryException(message, e);
        }

        String value = createPayload(tenantCreationWorkflow);
        ServiceClient serviceClient = null;
        try {

            ConfigurationContext context = ServiceHolder.getInstance().getConfigContextService()
                    .getClientConfigContext();
            serviceClient = new ServiceClient(context, null);
            serviceClient.engageModule("rampart");
            serviceClient.engageModule("addressing");

            Options options = serviceClient.getOptions();
            options.setTo(new EndpointReference(EPR));
            options.setAction("http://wso2.org/bps/sample/process");

            Policy policy = loadPolicy(CarbonUtils.getCarbonConfigDirPath() + "/appfactory/bpel-policy.xml");

            options.setProperty(RampartMessageData.KEY_RAMPART_POLICY, policy);

            options.setUserName(AppFactoryUtil.getAppfactoryConfiguration()
                    .getFirstProperty(AppFactoryConstants.SERVER_ADMIN_NAME));
            options.setPassword(AppFactoryUtil.getAppfactoryConfiguration()
                    .getFirstProperty(AppFactoryConstants.SERVER_ADMIN_PASSWORD));
            OMElement payload = AXIOMUtil.stringToOM(value);
            serviceClient.sendReceive(payload);

        } catch (AxisFault axisFault) {
            String message = "Unable to do the service class to the BPS server";
            throw new AppFactoryException(message, axisFault);
        } catch (FileNotFoundException e) {
            String message = "Unable to find the bpel-policy.xml file";
            throw new AppFactoryException(message);
        } catch (XMLStreamException e) {
            String message = "Unable to parse the xml";
            throw new AppFactoryException(message, e);
        } finally {
            closeServiceClient(serviceClient);
        }

        log.info("The BPEL ran successfully to create tenant in the cloud. Tenant domain is " +
                tenantCreationWorkflow.getTenantDomain() + ". Tenant Id is " + tenantCreationWorkflow.getTenantId());
    }

    private void closeServiceClient(ServiceClient serviceClient) {
        try {
            if (serviceClient != null) {
                serviceClient.cleanup();
            }
        } catch (AxisFault axisFault) {
            String message = "Unable to clean up the resources which is used by the client";
            log.error(message, axisFault);
        }
    }

    private String createPayload(TenantCreationWorkflowDTO tenantCreationWorkflow) {

        String value = "<p:CreateTenantRequest xmlns:p=\"http://wso2.org/bps/sample\">" +
                "<admin xmlns=\"http://wso2.org/bps/sample\">{0}</admin>" +
                "<firstName xmlns=\"http://wso2.org/bps/sample\">{1}</firstName>" +
                "<lastName xmlns=\"http://wso2.org/bps/sample\">{2}</lastName>" +
                "<adminPassword xmlns=\"http://wso2.org/bps/sample\">{3}</adminPassword>" +
                "<tenantDomain xmlns=\"http://wso2.org/bps/sample\">{4}</tenantDomain>" +
                "<tenantId xmlns=\"http://wso2.org/bps/sample\">{5}</tenantId>" +
                "<email xmlns=\"http://wso2.org/bps/sample\">{6}</email>" +
                "<active xmlns=\"http://wso2.org/bps/sample\">true</active>" +
                "<successKey xmlns=\"http://wso2.org/bps/sample\">key</successKey>" +
                "<createdDate xmlns=\"http://wso2.org/bps/sample\">2001-12-31T12:00:00</createdDate>" +
                "<originatedService xmlns=\"http://wso2.org/bps/sample\">WSO2 Stratos Manager</originatedService>" +
                "<usagePlan xmlns=\"http://wso2.org/bps/sample\">Demo</usagePlan>" +
                "</p:CreateTenantRequest>";

        String payLoadValue = MessageFormat.format(value, tenantCreationWorkflow.getTenantInfoBean().getAdmin(),
                tenantCreationWorkflow.getTenantInfoBean().getFirstname(),
                tenantCreationWorkflow.getTenantInfoBean().getLastname(),
                tenantCreationWorkflow.getTenantInfoBean().getAdminPassword(), tenantCreationWorkflow.getTenantDomain(),
                tenantCreationWorkflow.getTenantInfoBean().getTenantId(),
                tenantCreationWorkflow.getTenantInfoBean().getEmail());

        return payLoadValue;
    }
}
