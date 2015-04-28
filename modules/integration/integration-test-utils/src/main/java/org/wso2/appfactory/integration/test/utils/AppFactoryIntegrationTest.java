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
package org.wso2.appfactory.integration.test.utils;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.testng.Assert;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.appfactory.integration.admin.clients.TenantManagementServiceClient;
import org.wso2.appfactory.integration.test.utils.bpel.CreateTenantBPELClient;
import org.wso2.appfactory.integration.test.utils.rest.AppVersionRestClient;
import org.wso2.appfactory.integration.test.utils.rest.ApplicationRestClient;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.tenant.mgt.stub.TenantMgtAdminServiceExceptionException;
import org.wso2.carbon.tenant.mgt.stub.beans.xsd.TenantInfoBean;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Base class for App Factory Integration tests
 */
public class AppFactoryIntegrationTest {

    private static final Log log = LogFactory.getLog(AppFactoryIntegrationTest.class);
    protected String superTenantSession;
    protected static AutomationContext context;
    protected TenantInfoBean tenantInfoBean;

    /**
     * Start test execution with super tenant login
     *
     * @throws XPathExpressionException
     * @throws URISyntaxException
     * @throws SAXException
     * @throws XMLStreamException
     * @throws LoginAuthenticationExceptionException
     * @throws IOException
     */
    protected void init() throws XPathExpressionException, URISyntaxException, SAXException, XMLStreamException,
                                 LoginAuthenticationExceptionException, IOException {
        context = new AutomationContext(AFConstants.AF_PRODUCT_GROUP, TestUserMode.SUPER_TENANT_ADMIN);
        superTenantSession = login(context);
    }

    /**
     * Start test execution with super tenant login and create tenant with default values
     * specified in automation.xml
     *
     * @throws Exception
     */
    protected void initWithTenantCreation() throws Exception {
        init();
        tenantInfoBean = createTenant(getPropertyValue(AFConstants.DEFAULT_TENANT_FIRST_NAME),
                                      getPropertyValue(AFConstants.DEFAULT_TENANT_LAST_NAME),
                                      getPropertyValue(AFConstants.DEFAULT_TENANT_EMAIL),
                                      getRandomTenantDomain(),
                                      getPropertyValue(AFConstants.DEFAULT_TENANT_ADMIIN),
                                      getPropertyValue(AFConstants.DEFAULT_TENANT_ADMIN_PASSWORD),
                                      getPropertyValue(AFConstants.DEFAULT_TENANT_USAGE_PLAN));
    }

    /**
     * Start test execution with super tenant login, create tenant and then application with
     * default values specified in automation.xml
     *
     * @throws Exception
     */
    protected void initWithTenantAndApplicationCreation() throws Exception {
        initWithTenantCreation();
        createApplication(getRandomTenantDomain(),
                          getPropertyValue(AFConstants.DEFAULT_TENANT_ADMIIN),
                          getPropertyValue(AFConstants.DEFAULT_TENANT_ADMIN_PASSWORD),
                          getPropertyValue(AFConstants.DEFAULT_APP_APP_NAME),
                          getPropertyValue(AFConstants.DEFAULT_APP_APP_KEY),
                          getPropertyValue(AFConstants.DEFAULT_APP_APP_DESC),
                          getPropertyValue(AFConstants.DEFAULT_APP_APP_TYPE));
    }

    /**
     * Start test execution with super tenant login, create tenant and then application and three versions with
     * default values specified in automation.xml
     *
     * @throws Exception
     */
    protected void initTenantApplicationAndVersionCreation() throws Exception {
        initWithTenantCreation();
        createApplication(getRandomTenantDomain(),
                          getPropertyValue(AFConstants.DEFAULT_TENANT_ADMIIN),
                          getPropertyValue(AFConstants.DEFAULT_TENANT_ADMIN_PASSWORD),
                          getPropertyValue(AFConstants.DEFAULT_APP_APP_NAME),
                          getPropertyValue(AFConstants.DEFAULT_APP_APP_KEY),
                          getPropertyValue(AFConstants.DEFAULT_APP_APP_DESC),
                          getPropertyValue(AFConstants.DEFAULT_APP_APP_TYPE));
        createApplicationVersion(getRandomTenantDomain(),
                                 getPropertyValue(AFConstants.DEFAULT_TENANT_ADMIIN),
                                 getPropertyValue(AFConstants.DEFAULT_TENANT_ADMIN_PASSWORD),
                                 getPropertyValue(AFConstants.DEFAULT_APP_APP_KEY),
                                 getPropertyValue(AFConstants.DEFAULT_APP_VERSION_ONE_SRC),
                                 getPropertyValue(AFConstants.DEFAULT_APP_VERSION_ONE_TARGET));
        createApplicationVersion(getRandomTenantDomain(),
                                 getPropertyValue(AFConstants.DEFAULT_TENANT_ADMIIN),
                                 getPropertyValue(AFConstants.DEFAULT_TENANT_ADMIN_PASSWORD),
                                 getPropertyValue(AFConstants.DEFAULT_APP_APP_KEY),
                                 getPropertyValue(AFConstants.DEFAULT_APP_VERSION_TWO_SRC),
                                 getPropertyValue(AFConstants.DEFAULT_APP_VERSION_TWO_TARGET));
        createApplicationVersion(getRandomTenantDomain(),
                                 getPropertyValue(AFConstants.DEFAULT_TENANT_ADMIIN),
                                 getPropertyValue(AFConstants.DEFAULT_TENANT_ADMIN_PASSWORD),
                                 getPropertyValue(AFConstants.DEFAULT_APP_APP_KEY),
                                 getPropertyValue(AFConstants.DEFAULT_APP_VERSION_THREE_SRC),
                                 getPropertyValue(AFConstants.DEFAULT_APP_VERSION_THREE_TARGET));

    }

    /**
     * Clean up the changes
     */
    protected void cleanup() {
        log.info("cleanup called");
    }

    /**
     * Create Tenant flow
     *
     * @param firstName     first name
     * @param lastName      last name
     * @param email         email
     * @param tenantDomain  tenant domain
     * @param admin         admin
     * @param adminPassword admin password
     * @param usagePlan     usage plan
     * @return tenant info
     * @throws XPathExpressionException
     * @throws RemoteException
     * @throws TenantMgtAdminServiceExceptionException
     * @throws FileNotFoundException
     * @throws XMLStreamException
     * @throws InterruptedException
     */
    protected TenantInfoBean createTenant(String firstName, String lastName, String email, String tenantDomain,
                                          String admin, String adminPassword, String usagePlan)
        throws XPathExpressionException, RemoteException, TenantMgtAdminServiceExceptionException,
               FileNotFoundException, XMLStreamException, InterruptedException {

        // Check whether tenant is exist
        TenantManagementServiceClient tenantManagementServiceClient =
            new TenantManagementServiceClient(getPropertyValue(AFConstants.URLS_APPFACTORY),
                                              superTenantSession);

		/* tenantManagementServiceClient.getTenant() method always return a TenantInfoBean object
        although the tenantDomain is not exist.
		if tenantDomain is exist and inactive:
		tenantInfoBean.getActive() == false && tenantInfoBean.getTenantId() > 0
		if tenantDomain is not exist:
		tenantInfoBean.getActive() == false && tenantInfoBean.getTenantId() == 0 */
        TenantInfoBean tenantInfoBean = tenantManagementServiceClient.getTenant(tenantDomain);

        String tenantAdminUsername = getAdminUsername(admin, tenantDomain);
        if (!tenantInfoBean.getActive() && tenantInfoBean.getTenantId() > 0) {
            tenantManagementServiceClient.activateTenant(tenantDomain);
            tenantInfoBean.setActive(true);
            log.info("Tenant domain " + tenantDomain + " Activated");

        } else if (!tenantInfoBean.getActive() && tenantInfoBean.getTenantId() == 0) {
            Date date = new Date();
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(date);

            TenantInfoBean newTenant = new TenantInfoBean();
            newTenant.setActive(true);
            newTenant.setAdmin(admin);
            newTenant.setAdminPassword(adminPassword);
            newTenant.setCreatedDate(calendar);
            newTenant.setEmail(email);
            newTenant.setFirstname(firstName);
            newTenant.setLastname(lastName);
            newTenant.setOriginatedService("WSO2 Stratos Manager");
            newTenant.setTenantDomain(tenantDomain);
            newTenant.setUsagePlan(usagePlan);

            tenantManagementServiceClient.addTenant(newTenant);
            tenantManagementServiceClient.activateTenant(tenantDomain);

            // Get created tenant
            tenantInfoBean = tenantManagementServiceClient.getTenant(tenantDomain);
            Assert.assertTrue(tenantInfoBean.getActive(), "Tenant is not active");
            Assert.assertTrue(tenantInfoBean.getTenantId() > 0, "Tenant Id is not more than 0");

            // Invoke CreateTenant BPEL
            CreateTenantBPELClient createTenantBPELClient =
                new CreateTenantBPELClient(getPropertyValue(AFConstants.URLS_BPS),
                                           superTenantSession);

            String result = createTenantBPELClient
                .createTenant(context, tenantAdminUsername, tenantInfoBean, adminPassword,
                              "key", "WSO2 App Factory");
            Assert.assertNotNull(result,
                                 "Result of createTenantBPELClient.createTenant() is null ");
            Assert.assertTrue(result.contains("true"),
                              "Result of createTenantBPELClient.createTenant() is not true ");

            log.info("Tenant domain " + tenantDomain + " created and activated");
        }

        // Wait until tenant creation completes
        Assert.assertTrue(
            waitUntilTenantCreationCompletes(10000L, 10, tenantAdminUsername, adminPassword),
            "Tenant creation unsuccessful");
        log.info("Tenant domain " + tenantDomain + " completed successfully");
        return tenantInfoBean;
    }

    /**
     * Wait until tenant creation completes (Verifying trying to login as a created tenant)
     *
     * @param waitInterval        wait interval
     * @param retryCount          retry count
     * @param tenantAdminUsername tenant admin username
     * @param adminPassword       tenant admin password
     * @return true if logging successful
     * @throws InterruptedException
     */
    protected boolean waitUntilTenantCreationCompletes(long waitInterval, int retryCount, String tenantAdminUsername,
                                                       String adminPassword) throws InterruptedException
    {
        boolean isTenantLoggedIn = false;
        int round = 1;
        while (round <= retryCount) {
            try {
                login(getPropertyValue(AFConstants.URLS_PROD_SC), tenantAdminUsername,
                      adminPassword, context.getInstance().getHosts().get("default"));
                isTenantLoggedIn = true;
                break;
            } catch (Exception e) {
                String msg = "Attempt " + round + " : Exception when trying to login as tenant, retry after " +
                             waitInterval + " millis, exception is: " + e.getMessage();
                log.info(msg);
                if (log.isDebugEnabled()) {
                    log.debug(msg, e);
                }
                Thread.sleep(waitInterval);
                round++;
            }
        }

        return isTenantLoggedIn;
    }

    /**
     * Create a version
     *
     * @param applicationKey application key
     * @param sourceVersion source version
     * @param targetVersion target version
     */
    protected void createApplicationVersion(String tenantDomain, String admin, String adminPassword,
                                            String applicationKey, String sourceVersion, String targetVersion)
            throws Exception{
        String tenantAdminUsername = getAdminUsername(admin, tenantDomain);

        AppVersionRestClient appVersionRestClient =
                new AppVersionRestClient(getPropertyValue(AFConstants.URLS_APPFACTORY),
                                          tenantAdminUsername, adminPassword);
        appVersionRestClient.createVersion(applicationKey, sourceVersion, targetVersion);

    }

    /**
     * Create Application flow
     *
     * @param tenantDomain           tenant domain
     * @param admin                  admin username
     * @param adminPassword          admin password
     * @param applicationName        application name
     * @param applicationKey         application key
     * @param applicationDescription application description
     * @param applicationType        application type
     * @throws Exception
     */
    protected void createApplication(String tenantDomain, String admin, String adminPassword, String applicationName,
                                     String applicationKey, String applicationDescription, String applicationType)
        throws Exception {

        String tenantAdminUsername = getAdminUsername(admin, tenantDomain);

        ApplicationRestClient appMgtRestClient =
            new ApplicationRestClient(getPropertyValue(AFConstants.URLS_APPFACTORY),
                                 tenantAdminUsername, adminPassword);

        if (appMgtRestClient.isAppNameAlreadyAvailable(applicationName) &&
            appMgtRestClient.isApplicationKeyAvailable(applicationKey)) {
            appMgtRestClient.createNewApplication(applicationName, applicationKey, applicationType,
                                                  tenantAdminUsername, applicationDescription);
        }

        // Wait till Create Application completion
        waitUntilApplicationCreationCompletes(5000L, 5, tenantAdminUsername, adminPassword,
                                              applicationKey, applicationName);
    }

    /**
     * Wait until application creation completes
     *
     * @param waitInterval        wait interval
     * @param retryCount          retry count
     * @param tenantAdminUsername tenant admin username
     * @param adminPassword       admin password
     * @param applicationKey      application key
     * @param applicationName     application name
     * @throws Exception
     */
    protected void waitUntilApplicationCreationCompletes(long waitInterval, int retryCount, String tenantAdminUsername,
                                                         String adminPassword, String applicationKey,
                                                         String applicationName) throws Exception {
        ApplicationRestClient appMgtRestClient =
            new ApplicationRestClient(getPropertyValue(AFConstants.URLS_APPFACTORY),
                                 tenantAdminUsername, adminPassword);

        HttpResponse httpResponse = null;
        int round = 1;
        while (round <= retryCount) {
            try {
                httpResponse = appMgtRestClient.getAppInfo(applicationKey);
                break;
            } catch (Exception e) {
                String msg = "Attempt " + round + " : Exception when trying to get app info, retry after " +
                             waitInterval + " millis, exception is: " + e.getMessage();
                log.info(msg);
                if (log.isDebugEnabled()) {
                    log.debug(msg, e);
                }
                Thread.sleep(waitInterval);
                round++;
            }
        }

        Assert.assertNotNull(httpResponse, "httpResponse is null");
        JSONObject jsonObject = new JSONObject(httpResponse.getData());
        Assert.assertEquals(applicationName, jsonObject.getString("name"),
                            "Application Name not found");

    }

    /**
     * Login as super tenant
     *
     * @param context automation context
     * @return session
     * @throws IOException
     * @throws XPathExpressionException
     * @throws URISyntaxException
     * @throws SAXException
     * @throws XMLStreamException
     * @throws LoginAuthenticationExceptionException
     */
    protected String login(AutomationContext context)
        throws IOException, XPathExpressionException, URISyntaxException, SAXException,
               XMLStreamException, LoginAuthenticationExceptionException {
        LoginLogoutClient loginLogoutClient = new LoginLogoutClient(context);
        return loginLogoutClient.login();
    }

    /**
     * Login as any user
     *
     * @param backendUrl backend url
     * @param username   username
     * @param password   password
     * @param host       host
     * @return session
     * @throws RemoteException
     * @throws LoginAuthenticationExceptionException
     */
    protected String login(String backendUrl, String username, String password, String host)
        throws RemoteException, LoginAuthenticationExceptionException {
        AuthenticatorClient client = new AuthenticatorClient(backendUrl + "services/");
        return client.login(username, password, host);
    }

    /**
     * Returns tenant admin username
     *
     * @param admin        admin
     * @param tenantDomain tenant domain
     * @return tenant admin username
     */
    protected static String getAdminUsername(String admin, String tenantDomain) {
        return admin + "@" + tenantDomain;
    }

    /**
     * Retrieve a custom tenant domain by appending a random value
     * @return
     * @throws XPathExpressionException
     */
    protected static String getRandomTenantDomain() throws XPathExpressionException {
        return RandomStringUtils.randomAlphanumeric(5)+ getPropertyValue(AFConstants.DEFAULT_TENANT_TENANT_DOMAIN);
    }

    /**
     * Get value passing xpath
     *
     * @param xPath expression
     * @return value
     * @throws XPathExpressionException
     */
    protected static String getPropertyValue(String xPath) throws XPathExpressionException {
        return context.getConfigurationValue(xPath);
    }

    /**
     * Get node passing xpath
     *
     * @param xPath expression
     * @return node
     * @throws XPathExpressionException
     */
    protected static Node getPropertyNode(String xPath) throws XPathExpressionException {
        return context.getConfigurationNode(xPath);
    }

    /**
     * Get list of nodes passing xpath
     *
     * @param xPath expresstion
     * @return node list
     * @throws XPathExpressionException
     */
    protected static NodeList getPropertyNodeList(String xPath) throws XPathExpressionException {
        return context.getConfigurationNodeList(xPath);
    }
}

