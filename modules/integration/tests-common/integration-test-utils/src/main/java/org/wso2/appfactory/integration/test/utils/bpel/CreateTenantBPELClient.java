/*
 * Copyright 2015 WSO2, Inc. (http://wso2.com)
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
package org.wso2.appfactory.integration.test.utils.bpel;

import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.rampart.RampartMessageData;
import org.apache.rampart.policy.model.CryptoConfig;
import org.apache.rampart.policy.model.RampartConfig;
import org.testng.Assert;
import org.wso2.appfactory.integration.test.utils.AFConstants;
import org.wso2.carbon.appfactory.createtenant.stub.CreateTenantStub;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.integration.common.admin.client.utils.AuthenticateStubUtil;
import org.wso2.carbon.tenant.mgt.stub.beans.xsd.TenantInfoBean;

import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.util.Properties;

/**
 * Client to invoke the CreateTenant BPEL
 */
public class CreateTenantBPELClient {
    private static final String SERVICE = "services/CreateTenant";
    private CreateTenantStub createTenantStub;

    private static final String MERLIN_PROVIDER = "org.apache.ws.security.components.crypto.Merlin";
    private static final String MERLIN_KEYSTORE_TYPE = "org.apache.ws.security.crypto.merlin.keystore.type";
    private static final String MERLIN_FILE = "org.apache.ws.security.crypto.merlin.file";
    private static final String MERLIN_KEYSTORE_PASSWORD = "org.apache.ws.security.crypto.merlin.keystore.password";

    /**
     * Construct authenticated CreateTenantStub
     *
     * @param backendUrl    backend url
     * @param sessionCookie session cookie
     * @throws AxisFault
     */
    public CreateTenantBPELClient(String backendUrl, String sessionCookie) throws AxisFault {
        String endPoint = backendUrl + SERVICE;
        ConfigurationContext ctx =
            ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                FrameworkPathUtil.getSystemResourceLocation() + AFConstants.CLIENT_MODULES_STRING,
                null);
        createTenantStub = new CreateTenantStub(ctx, endPoint);
        AuthenticateStubUtil.authenticateStub(sessionCookie, createTenantStub);
    }

    /**
     * Invoke CreateTenant BPEL
     *
     * @param context           AutomationContext object
     * @param adminUsername     admin username
     * @param tenantInfoBean    tenant info bean
     * @param adminPassword     admin password
     * @param successKey        success key
     * @param originatedService originated service
     * @return String
     * @throws XPathExpressionException
     * @throws FileNotFoundException
     * @throws RemoteException
     * @throws XMLStreamException
     */
    public String createTenant(AutomationContext context, String adminUsername, TenantInfoBean tenantInfoBean,
                               String adminPassword, String successKey, String originatedService)
        throws XPathExpressionException, FileNotFoundException, RemoteException, XMLStreamException {

        String securityPolicyPath =
            FrameworkPathUtil.getSystemResourceLocation() +
            AFConstants.SECURITY_POLICIES_SCENARIO1_POLICY_XML;
        String trustStorePath =
            FrameworkPathUtil.getSystemResourceLocation() +
            AFConstants.KEYSTORES_PRODUCT_CLIENT_TRUSTSTORE;
        String username = context.getContextTenant().getContextUser().getUserName();

        ServiceClient serviceClient = createTenantStub._getServiceClient();
        Options options = serviceClient.getOptions();
        options.setUserName(username);
        options.setPassword(context.getContextTenant().getContextUser().getPassword());
        options.setProperty(RampartMessageData.KEY_RAMPART_POLICY,
                            PolicyEngine.getPolicy(new FileInputStream(securityPolicyPath)));
        options.setProperty(RampartMessageData.KEY_RAMPART_POLICY,
                            loadPolicy(username, securityPolicyPath, trustStorePath, "wso2carbon",
                                       "wso2carbon", "wso2carbon")
        );
        serviceClient.engageModule("rampart");

        return createTenantStub.process(adminUsername, tenantInfoBean.getFirstname(),
                                        tenantInfoBean.getLastname(), adminPassword,
                                        tenantInfoBean.getTenantDomain(),
                                        tenantInfoBean.getTenantId(),
                                        tenantInfoBean.getEmail(), tenantInfoBean.getActive(),
                                        successKey, tenantInfoBean.getCreatedDate(),
                                        originatedService, tenantInfoBean.getUsagePlan());

    }

    private Policy loadPolicy(String userName, String securityPolicyPath, String trustStorePath,
                              String trustStorePassword, String userCertAlias, String encryptionUser)
        throws FileNotFoundException, XMLStreamException {

        Policy policy = null;
        StAXOMBuilder builder = null;

        try {
            builder = new StAXOMBuilder(securityPolicyPath);
            policy = PolicyEngine.getPolicy(builder.getDocumentElement());

            RampartConfig rampartConfig = new RampartConfig();

            rampartConfig.setUser(userName);
            rampartConfig.setUserCertAlias(userCertAlias);
            rampartConfig.setEncryptionUser(encryptionUser);
            rampartConfig.setSigCryptoConfig(generateCryptoConfig(trustStorePath, trustStorePassword));
            rampartConfig.setEncrCryptoConfig(generateCryptoConfig(trustStorePath, trustStorePassword));

            policy.addAssertion(rampartConfig);
        } finally {
            if (builder != null) {
                builder.close();
            }
        }
        Assert.assertNotNull(policy, "Policy cannot be null");
        return policy;
    }

    private CryptoConfig generateCryptoConfig(String trustStorePath, String trustStorePassword) {
        CryptoConfig cryptoConfig = new CryptoConfig();
        cryptoConfig.setProvider(MERLIN_PROVIDER);

        Properties properties = new Properties();
        properties.put(MERLIN_KEYSTORE_TYPE, "JKS");
        properties.put(MERLIN_FILE, trustStorePath);
        properties.put(MERLIN_KEYSTORE_PASSWORD, trustStorePassword);

        cryptoConfig.setProp(properties);
        return cryptoConfig;
    }
}

