package org.wso2.appfactory.integration.test.utils;

import org.apache.commons.lang.RandomStringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import javax.xml.xpath.XPathExpressionException;

/**
 * Created by punnadi on 4/29/15.
 */
public class AFIntegrationTestUtils {
    private static AFIntegrationTestUtils appFactoryIntegrationTestUtils;
    private static AutomationContext context;
    private static String tenantDomain;

    private AFIntegrationTestUtils() {}

    public static AFIntegrationTestUtils getInstance() {
        if(appFactoryIntegrationTestUtils == null){
            appFactoryIntegrationTestUtils = new AFIntegrationTestUtils();
        }
        return appFactoryIntegrationTestUtils;
    }

    public AutomationContext getAutomationContext() throws XPathExpressionException {
        if(context == null) {
            context = new AutomationContext(AFConstants.AF_PRODUCT_GROUP, TestUserMode.SUPER_TENANT_ADMIN);
        }
        return context;
    }

    /**
     * Retrieve a custom tenant domain by appending a random value
     * @return
     * @throws XPathExpressionException
     */
    public String getRandomTenantDomain() throws XPathExpressionException {
        if(tenantDomain == null){
            tenantDomain = RandomStringUtils.randomAlphanumeric(5) +
                           getPropertyValue(AFConstants.DEFAULT_TENANT_TENANT_DOMAIN);
        }
        return  tenantDomain;
    }

    /**
     * Get value passing xpath
     *
     * @param xPath expression
     * @return value
     * @throws XPathExpressionException
     */
    public String getPropertyValue(String xPath) throws XPathExpressionException {
        return context.getConfigurationValue(xPath);
    }

    /**
     * Get node passing xpath
     *
     * @param xPath expression
     * @return node
     * @throws XPathExpressionException
     */
    public Node getPropertyNode(String xPath) throws XPathExpressionException {
        return context.getConfigurationNode(xPath);
    }

    /**
     * Get list of nodes passing xpath
     *
     * @param xPath expresstion
     * @return node list
     * @throws XPathExpressionException
     */
    public NodeList getPropertyNodeList(String xPath) throws XPathExpressionException {
        return context.getConfigurationNodeList(xPath);
    }


    /**
     * Returns tenant admin username
     *
     * @return tenant admin username
     */
    public String getAdminUsername() throws XPathExpressionException {
        return getPropertyValue(AFConstants.DEFAULT_TENANT_ADMIIN) + "@" + tenantDomain;
    }

}
