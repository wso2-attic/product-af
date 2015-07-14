package org.wso2.appfactory.integration.test.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.testng.Assert;
import org.wso2.appfactory.integration.admin.clients.TenantManagementServiceClient;
import org.wso2.appfactory.integration.test.utils.bpel.CreateTenantBPELClient;
import org.wso2.appfactory.integration.test.utils.rest.APIRestClient;
import org.wso2.appfactory.integration.test.utils.rest.AppVersionClient;
import org.wso2.appfactory.integration.test.utils.rest.ApplicationClient;
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
    private String superTenantSession;
    private static AutomationContext context;
    private static String tenantDomain;
    private String fullyQualifiedTenantAdmin;
    private String tenantAwareAdminUsername;
    private String tenantAdminPassword;
    private  ApplicationClient applicationClient;

    /**
     * Start test execution with super tenant login
     *
     * @throws java.lang.Exception
     */
    private void init() throws Exception {
        superTenantSession = login(context = AFIntegrationTestUtils.getAutomationContext());
        tenantDomain = AFIntegrationTestUtils.getPropertyValue(
                AFConstants.DEFAULT_TENANT_TENANT_DOMAIN);
        if (System.getProperty(AFConstants.ENV_CREATE_RANDOM_TENANT) != null ) {
            tenantDomain = AFIntegrationTestUtils.getRandomTenantDomain();
        }
        System.setProperty(AFConstants.ENV_CREATED_RANDOM_TENANT_DOMAIN, tenantDomain);
        tenantAdminPassword = AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_TENANT_ADMIN_PASSWORD);
        tenantAwareAdminUsername = AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_TENANT_ADMIIN);
        fullyQualifiedTenantAdmin = tenantAwareAdminUsername + "@" + tenantDomain;
        tenantAdminPassword = AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_TENANT_ADMIN_PASSWORD);
    }

    /**
     * Start test execution with super tenant login, create tenant and then application and three versions with
     * default values specified in automation.xml
     *
     * @throws Exception
     */
    public void initTenantApplicationAndVersionCreation() throws Exception {
        init();
        if (!isTenantExists()) {
            createTenant(AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_TENANT_FIRST_NAME),
                         AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_TENANT_LAST_NAME),
                         AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_TENANT_EMAIL),
                         AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_TENANT_USAGE_PLAN));

        }
        if (!isDefaultApplicationExists()) {
            log.info("Default application doesn't exist, Creating "
                     + AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_APP_NAME));
            createApplication(AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_APP_NAME),
                              AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_APP_KEY),
                              AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_APP_DESC),
                              AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_APP_TYPE));
            Thread.sleep(40000);
            createApplicationVersion(AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_APP_KEY),
                                     AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_VERSION_ONE_SRC),
                                     AFIntegrationTestUtils.getPropertyValue(
                                             AFConstants.DEFAULT_APP_VERSION_ONE_TARGET));
            createApplicationVersion(AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_APP_KEY),
                                     AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_VERSION_TWO_SRC),
                                     AFIntegrationTestUtils.getPropertyValue(
                                             AFConstants.DEFAULT_APP_VERSION_TWO_TARGET));
            createApplicationVersion(AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_APP_KEY),
                                     AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_VERSION_THREE_SRC),
                                     AFIntegrationTestUtils.getPropertyValue(
                                             AFConstants.DEFAULT_APP_VERSION_THREE_TARGET));
        } else {
            log.info("Default application  exists.");
        }
    }

    /**
     * Delete default application "appla"
     * @throws Exception
     */
    public void deleteDefaultApplication() throws Exception {
        if (isDefaultApplicationExists()) {
            ApplicationClient appMgtRestClient = new
                    ApplicationClient(AFIntegrationTestUtils.getPropertyValue(AFConstants.URLS_APPFACTORY),
                                      AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_TENANT_ADMIIN) + "@" +
                                      AFIntegrationTestUtils.getDefaultTenantDomain(),
                                      AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_TENANT_ADMIN_PASSWORD));
            appMgtRestClient.deleteApplication(AFIntegrationTestUtils.getAdminUsername(),
                                               AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_APP_KEY));
            Thread.sleep(20000);
        }
    }

    /**
     * Check if the tenant already exists or not
     * @return
     * @throws Exception
     */
    private boolean isTenantExists() throws Exception {
        TenantManagementServiceClient tenantManagementServiceClient =
                new TenantManagementServiceClient(AFIntegrationTestUtils.getPropertyValue(AFConstants.URLS_APPFACTORY),
                                                  superTenantSession);
        TenantInfoBean tenantInfoBean = tenantManagementServiceClient.getTenant(tenantDomain);
        return tenantInfoBean.getTenantId() == 0 ? false : true;
    }

    /**
     * Check if the default application already exists or not.
     *
     * @return
     * @throws Exception
     */
    private boolean isDefaultApplicationExists() throws Exception {
        applicationClient = new
                ApplicationClient(AFIntegrationTestUtils.getPropertyValue(AFConstants.URLS_APPFACTORY),
                                  AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_TENANT_ADMIIN) + "@" +
                                  AFIntegrationTestUtils.getDefaultTenantDomain(),
                                  AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_TENANT_ADMIN_PASSWORD));
        if (!applicationClient.isAppNameAlreadyAvailable(
                AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_APP_NAME)) &&
            !applicationClient.isApplicationKeyAvailable(
                    AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_APP_KEY))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Create Tenant flow
     *
     * @param firstName first name
     * @param lastName  last name
     * @param email     email
     * @param usagePlan usage plan
     * @return tenant info
     * @throws XPathExpressionException
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.tenant.mgt.stub.TenantMgtAdminServiceExceptionException
     * @throws java.io.FileNotFoundException
     * @throws XMLStreamException
     * @throws InterruptedException
     */
    private void createTenant(String firstName, String lastName, String email, String usagePlan)
            throws XPathExpressionException, RemoteException, TenantMgtAdminServiceExceptionException,
                   FileNotFoundException, XMLStreamException, InterruptedException {

        // Check whether tenant is exist
        TenantManagementServiceClient tenantManagementServiceClient = new TenantManagementServiceClient(
                AFIntegrationTestUtils.getPropertyValue(AFConstants.URLS_APPFACTORY), superTenantSession);
        TenantInfoBean newTenant = new TenantInfoBean();
        newTenant.setActive(true);
        newTenant.setAdmin(tenantAwareAdminUsername);
        newTenant.setAdminPassword(tenantAdminPassword);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
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
        TenantInfoBean tenantInfoBean = tenantManagementServiceClient.getTenant(tenantDomain);
        Assert.assertTrue(tenantInfoBean.getActive(), "Tenant is not active");
        Assert.assertTrue(tenantInfoBean.getTenantId() > 0, "Tenant Id is not more than 0");

        // Invoke CreateTenant BPEL
        CreateTenantBPELClient createTenantBPELClient =
                new CreateTenantBPELClient(AFIntegrationTestUtils.getPropertyValue(AFConstants.URLS_BPS),
                                           superTenantSession);

        String result = createTenantBPELClient
                .createTenant(context, tenantAwareAdminUsername, tenantInfoBean, tenantAdminPassword,
                              "key", "WSO2 App Factory");
        Assert.assertNotNull(result, "Result of createTenantBPELClient.createTenant() is null ");
        Assert.assertTrue(result.contains("true"), "Result of createTenantBPELClient.createTenant() is not true ");

        // Wait until tenant creation completes
        Assert.assertTrue(waitUntilTenantCreationCompletes(10000L, 10, fullyQualifiedTenantAdmin, tenantAdminPassword),
                "Tenant creation unsuccessful");
        log.info("Tenant domain " + tenantDomain + " completed successfully");
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
                                                       String adminPassword) throws InterruptedException {
        boolean isTenantLoggedIn = false;
        int round = 1;
        while (round <= retryCount) {
            try {
                login(AFIntegrationTestUtils.getPropertyValue(AFConstants.URLS_PROD_SC), tenantAdminUsername,
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
    private void createApplication(String applicationName, String applicationKey, String applicationDescription,
                                     String applicationType)
            throws Exception {
        ApplicationClient appMgtRestClient = new ApplicationClient(AFIntegrationTestUtils.getPropertyValue(
                AFConstants.URLS_APPFACTORY)
                , fullyQualifiedTenantAdmin, tenantAdminPassword);
        appMgtRestClient.createNewApplication(applicationName, applicationKey, applicationType,
                                              fullyQualifiedTenantAdmin, applicationDescription);

        // Wait till Create Application completion
        waitUntilApplicationCreationCompletes(5000L, 5, fullyQualifiedTenantAdmin, tenantAdminPassword, applicationKey,
                                              applicationName);
    }


    /**
     * Create a version
     *
     * @param applicationKey application key
     * @param sourceVersion  source version
     * @param targetVersion  target version
     */
    private void createApplicationVersion(String applicationKey, String sourceVersion, String targetVersion)
            throws Exception {
        AppVersionClient appVersionRestClient =
                new AppVersionClient(AFIntegrationTestUtils.getPropertyValue(AFConstants.URLS_APPFACTORY),
                                         fullyQualifiedTenantAdmin, tenantAdminPassword);
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
    public void waitUntilApplicationCreationCompletes(long waitInterval, int retryCount, String tenantAdminUsername,
                                                         String adminPassword, String applicationKey,
                                                         String applicationName) throws Exception {
        ApplicationClient appMgtRestClient =
                new ApplicationClient(AFIntegrationTestUtils.getPropertyValue(AFConstants.URLS_APPFACTORY),
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

    private String login(AutomationContext context)
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
    private String login(String backendUrl, String username, String password, String host)
            throws RemoteException, LoginAuthenticationExceptionException {
        AuthenticatorClient client = new AuthenticatorClient(backendUrl + "services/");
        return client.login(username, password, host);
    }

    /**
     * Create API and Publish in APIM
     *
     *
     * @throws Exception
     */
    public void addDefaultAPI()throws Exception {
        APIRestClient apiRestClient = new APIRestClient(AFIntegrationTestUtils.getPropertyValue(
                AFConstants.URLS_API), AFIntegrationTestUtils.getPropertyValue(
                AFConstants.DEFAULT_API_USER_NAME), AFIntegrationTestUtils.getPropertyValue(
                AFConstants.DEFAULT_API_PASSWORD));
    }

}
