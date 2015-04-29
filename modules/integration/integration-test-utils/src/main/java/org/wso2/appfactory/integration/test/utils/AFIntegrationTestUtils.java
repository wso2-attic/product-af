package org.wso2.appfactory.integration.test.utils;

import org.apache.commons.lang.RandomStringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import javax.xml.xpath.XPathExpressionException;

/**
 * Utility methods
 */
public class AFIntegrationTestUtils {
    private static AFIntegrationTestUtils appFactoryIntegrationTestUtils;
    private static AutomationContext context;
    private static String tenantDomain;

    public static AutomationContext getAutomationContext() throws XPathExpressionException {
        if(context == null) {
            synchronized (AFIntegrationTestUtils.class) {
                if(context == null) {
                context = new AutomationContext(AFConstants.AF_PRODUCT_GROUP, TestUserMode.SUPER_TENANT_ADMIN);
                }
            }
        }
        return context;
    }

    /**
     * Retrieve a custom tenant domain by appending a random value
     * @return
     * @throws XPathExpressionException
     */
    public static String getRandomTenantDomain() throws XPathExpressionException {
        if(tenantDomain == null){
            synchronized (AFIntegrationTestUtils.class) {
                if (tenantDomain == null) {
                    tenantDomain = RandomStringUtils.randomAlphanumeric(5) +
                                   getPropertyValue(AFConstants.DEFAULT_TENANT_TENANT_DOMAIN);
                }
            }
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
    public static String getPropertyValue(String xPath) throws IllegalArgumentException {
        try {
            return context.getConfigurationValue(xPath);
        } catch (XPathExpressionException e) {
            throw new IllegalArgumentException("Error reading " + xPath, e);
        }
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
    public static NodeList getPropertyNodeList(String xPath) throws XPathExpressionException {
        return context.getConfigurationNodeList(xPath);
    }


    /**
     * Returns tenant admin username
     *
     * @return tenant admin username
     */
    public static String getAdminUsername() throws XPathExpressionException {
        return getPropertyValue(AFConstants.DEFAULT_TENANT_ADMIIN) + "@" + tenantDomain;
    }

    public static String getAdminPassword() throws XPathExpressionException {
        return getPropertyValue(AFConstants.DEFAULT_TENANT_ADMIN_PASSWORD);
    }

    public static String getBEServerURL() throws XPathExpressionException {
        return getPropertyValue(AFConstants.URLS_APPFACTORY);
    }
}
