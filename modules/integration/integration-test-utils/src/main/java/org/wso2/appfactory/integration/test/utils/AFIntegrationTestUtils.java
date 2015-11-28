package org.wso2.appfactory.integration.test.utils;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import javax.xml.xpath.XPathExpressionException;

/**
 * Utility methods
 */
public class AFIntegrationTestUtils {
	public static final String JENKINS_JOB_NAME_POSTFIX = "default";
	public static final String JENKINS_JOB_NAME_SEPERATOR = "-";
	private static AutomationContext context;
    private static String tenantDomain;
	private static final int MAX_SUCCESS_HTTP_STATUS_CODE = 299;
    private static final Log log = LogFactory.getLog(AFIntegrationTestUtils.class);

    static {
        try {
            context = new AutomationContext(AFConstants.AF_PRODUCT_GROUP, TestUserMode.SUPER_TENANT_ADMIN);
        } catch (XPathExpressionException e) {
            log.error("Error occurred while initializing automation context",e);
        }

    }

    public static AutomationContext getAutomationContext(){
        return context;
    }

    /**
     * Retrieve a custom tenant domain by appending a random value
     * @return
     * @throws XPathExpressionException
     */
    static String getRandomTenantDomain() throws XPathExpressionException {
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
    public static String getAdminUsername() {
        String tenantDomain = getDefaultTenantDomain();
        return getPropertyValue(AFConstants.DEFAULT_TENANT_ADMIIN) + "@" + tenantDomain;
    }

    public static String getDefaultTenantDomain() {
        String tenantDomain = System.getProperty(AFConstants.ENV_CREATED_RANDOM_TENANT_DOMAIN);
        if (tenantDomain == null) {
            tenantDomain = getPropertyValue(AFConstants.DEFAULT_TENANT_TENANT_DOMAIN);
        }
        return tenantDomain;
    }

    public static String getAdminPassword() {
        return getPropertyValue(AFConstants.DEFAULT_TENANT_ADMIN_PASSWORD);
    }

    public static String getBEServerURL() {
        return getPropertyValue(AFConstants.URLS_APPFACTORY);
    }

	/**
	 * Return jenkins job name
	 * @param appName application name
	 * @param version application version
	 * @return jenkins job name
	 */
	public static String getJenkinsJobName(String appName, String version){
		return appName.concat(JENKINS_JOB_NAME_SEPERATOR).
				concat(version).concat(JENKINS_JOB_NAME_SEPERATOR).concat(JENKINS_JOB_NAME_POSTFIX);
	}

    public static long getTimeOutPeriod() {
        String timeOutPeriod = getPropertyValue(AFConstants.TIMEOUT_PERIOD);
        long result = 10000L;
        try {
            result = Long.parseLong(timeOutPeriod);
        } catch (NumberFormatException e) {
            // NOP
        }
        return result;
    }

    public static int getTimeOutRetryCount() {
        String retryCount = getPropertyValue(AFConstants.TIMEOUT_RETRY_COUNT);
        int result = 8;
        try {
            result = Integer.parseInt(retryCount);
        } catch (NumberFormatException e) {
            // NOP
        }
        return result;
    }

}
