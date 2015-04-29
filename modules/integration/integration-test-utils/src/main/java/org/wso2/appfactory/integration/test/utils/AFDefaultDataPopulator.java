package org.wso2.appfactory.integration.test.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.testng.Assert;
import org.wso2.appfactory.integration.admin.clients.TenantManagementServiceClient;
import org.wso2.appfactory.integration.test.utils.bpel.CreateTenantBPELClient;
import org.wso2.appfactory.integration.test.utils.rest.AppVersionRestClient;
import org.wso2.appfactory.integration.test.utils.rest.ApplicationRestClient;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.context.AutomationContext;
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
 * Class which does all the initial setting up by creating the default tenant, default application and
 * three versions defined in automation.xml
 */
public class AFDefaultDataPopulator {

    private static final Log log = LogFactory.getLog(AFDefaultDataPopulator.class);
    protected String superTenantSession;
    protected static AutomationContext context;
    protected TenantInfoBean tenantInfoBean;
    private static String tenantDomain;
    String tenantAdminUsername;
    String tenantAdminPassword;
    private AFIntegrationTestUtils utils = AFIntegrationTestUtils.getInstance();

    /**
     * Start test execution with super tenant login
     *
     * @throws java.lang.Exception
     */
    private void init() throws Exception {
        superTenantSession = login(context = utils.getAutomationContext());
        tenantDomain = utils.getRandomTenantDomain();
        tenantAdminUsername = utils.getAdminUsername();
        tenantAdminPassword = utils.getPropertyValue(AFConstants.DEFAULT_TENANT_ADMIN_PASSWORD);
    }

    /**
     * Start test execution with super tenant login and create tenant with default values
     * specified in automation.xml
     *
     * @throws Exception
     */
    private void initWithTenantCreation() throws Exception {
        init();
        tenantInfoBean = createTenant(utils.getPropertyValue(AFConstants.DEFAULT_TENANT_FIRST_NAME),
                                      utils.getPropertyValue(AFConstants.DEFAULT_TENANT_LAST_NAME),
                                      utils.getPropertyValue(AFConstants.DEFAULT_TENANT_EMAIL),
                                      utils.getPropertyValue(AFConstants.DEFAULT_TENANT_USAGE_PLAN));
    }

    /**
     * Start test execution with super tenant login, create tenant and then application with
     * default values specified in automation.xml
     *
     * @throws Exception
     */
    private void initWithTenantAndApplicationCreation() throws Exception {
        initWithTenantCreation();
        createApplication(utils.getPropertyValue(AFConstants.DEFAULT_APP_APP_NAME),
                          utils.getPropertyValue(AFConstants.DEFAULT_APP_APP_KEY),
                          utils.getPropertyValue(AFConstants.DEFAULT_APP_APP_DESC),
                          utils.getPropertyValue(AFConstants.DEFAULT_APP_APP_TYPE));
    }

    /**
     * Start test execution with super tenant login, create tenant and then application and three versions with
     * default values specified in automation.xml
     *
     * @throws Exception
     */
    public void initTenantApplicationAndVersionCreation() throws Exception {
        initWithTenantAndApplicationCreation();
        createApplicationVersion(utils.getPropertyValue(AFConstants.DEFAULT_APP_APP_KEY),
                                 utils.getPropertyValue(AFConstants.DEFAULT_APP_VERSION_ONE_SRC),
                                 utils.getPropertyValue(AFConstants.DEFAULT_APP_VERSION_ONE_TARGET));
        createApplicationVersion(utils.getPropertyValue(AFConstants.DEFAULT_APP_APP_KEY),
                                 utils.getPropertyValue(AFConstants.DEFAULT_APP_VERSION_TWO_SRC),
                                 utils.getPropertyValue(AFConstants.DEFAULT_APP_VERSION_TWO_TARGET));
        createApplicationVersion(utils.getPropertyValue(AFConstants.DEFAULT_APP_APP_KEY),
                                 utils.getPropertyValue(AFConstants.DEFAULT_APP_VERSION_THREE_SRC),
                                 utils.getPropertyValue(AFConstants.DEFAULT_APP_VERSION_THREE_TARGET));

    }

    /**
     * Create Tenant flow
     *
     * @param firstName     first name
     * @param lastName      last name
     * @param email         email
     * @param usagePlan     usage plan
     * @return tenant info
     * @throws XPathExpressionException
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.tenant.mgt.stub.TenantMgtAdminServiceExceptionException
     * @throws java.io.FileNotFoundException
     * @throws XMLStreamException
     * @throws InterruptedException
     */
    protected TenantInfoBean createTenant(String firstName, String lastName, String email, String usagePlan)
            throws XPathExpressionException, RemoteException, TenantMgtAdminServiceExceptionException,
                   FileNotFoundException, XMLStreamException, InterruptedException {

        // Check whether tenant is exist
        TenantManagementServiceClient tenantManagementServiceClient =
                new TenantManagementServiceClient(utils.getPropertyValue(AFConstants.URLS_APPFACTORY), superTenantSession);

        Date date = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);

        TenantInfoBean newTenant = new TenantInfoBean();
        newTenant.setActive(true);
        newTenant.setAdmin(tenantAdminUsername);
        newTenant.setAdminPassword(tenantAdminPassword);
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
                new CreateTenantBPELClient(utils.getPropertyValue(AFConstants.URLS_BPS),
                                           superTenantSession);

        String result = createTenantBPELClient
                .createTenant(context, tenantAdminUsername, tenantInfoBean, tenantAdminPassword,
                              "key", "WSO2 App Factory");
        Assert.assertNotNull(result,
                             "Result of createTenantBPELClient.createTenant() is null ");
        Assert.assertTrue(result.contains("true"),
                          "Result of createTenantBPELClient.createTenant() is not true ");

        log.info("Tenant domain " + tenantDomain + " created and activated");

        // Wait until tenant creation completes
        Assert.assertTrue(
                waitUntilTenantCreationCompletes(10000L, 10, tenantAdminUsername, tenantAdminPassword),
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
                login(utils.getPropertyValue(AFConstants.URLS_PROD_SC), tenantAdminUsername,
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
     * Create Application flow
     *
     * @param applicationName        application name
     * @param applicationKey         application key
     * @param applicationDescription application description
     * @param applicationType        application type
     * @throws Exception
     */
    protected void createApplication(String applicationName, String applicationKey, String applicationDescription,
                                     String applicationType)
            throws Exception {
        ApplicationRestClient appMgtRestClient = new ApplicationRestClient(utils.getPropertyValue(
                AFConstants.URLS_APPFACTORY)
                                                , tenantAdminUsername, tenantAdminPassword);
        appMgtRestClient.createNewApplication(applicationName, applicationKey, applicationType,
                                                  tenantAdminUsername, applicationDescription);

        // Wait till Create Application completion
        waitUntilApplicationCreationCompletes(5000L, 5, tenantAdminUsername, tenantAdminPassword, applicationKey,
                                              applicationName);
    }


    /**
     * Create a version
     *
     * @param applicationKey application key
     * @param sourceVersion source version
     * @param targetVersion target version
     */
    protected void createApplicationVersion(String applicationKey, String sourceVersion, String targetVersion)
            throws Exception{
        AppVersionRestClient appVersionRestClient =
                new AppVersionRestClient(utils.getPropertyValue(AFConstants.URLS_APPFACTORY),
                                         tenantAdminUsername, tenantAdminPassword);
        appVersionRestClient.createVersion(applicationKey, sourceVersion, targetVersion);

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
                new ApplicationRestClient(utils.getPropertyValue(AFConstants.URLS_APPFACTORY),
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

}
