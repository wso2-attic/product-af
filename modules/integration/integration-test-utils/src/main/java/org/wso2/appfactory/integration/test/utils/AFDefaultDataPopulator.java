package org.wso2.appfactory.integration.test.utils;

import com.gitblit.models.RepositoryModel;
import com.gitblit.utils.RpcUtils;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;
import org.testng.Assert;
import org.wso2.appfactory.integration.admin.clients.TenantManagementServiceClient;
import org.wso2.appfactory.integration.test.utils.bpel.CreateTenantBPELClient;
import org.wso2.appfactory.integration.test.utils.rest.APIRestClient;
import org.wso2.appfactory.integration.test.utils.rest.AppVersionClient;
import org.wso2.appfactory.integration.test.utils.rest.ApplicationClient;
import org.wso2.carbon.appfactory.repository.mgt.client.AppfactoryRepositoryClient;
import org.wso2.carbon.appfactory.repository.mgt.git.GitAgent;
import org.wso2.carbon.appfactory.repository.mgt.git.GitRepositoryClient;
import org.wso2.carbon.appfactory.repository.mgt.git.JGitAgent;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.tenant.mgt.stub.TenantMgtAdminServiceExceptionException;
import org.wso2.carbon.tenant.mgt.stub.beans.xsd.TenantInfoBean;
import org.wso2.carbon.utils.CarbonUtils;
import org.xml.sax.SAXException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.*;

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
	private static final int MAX_SUCCESS_HTTP_STATUS_CODE = 299;
	private TenantInfoBean tenantInfoBean;

	static {
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		});
	}

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
                    AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_APP_TYPE),
                    AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_APP_EXTENSION),
                    AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_ARTIFACT_VERSION),
                    AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_DEFAULT_STAGE),
                    AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_RUNTIME_ALIAS),
                    String.valueOf(tenantInfoBean.getTenantId()));
            Thread.sleep(60000);
            /*
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
                                             AFConstants.DEFAULT_APP_VERSION_THREE_TARGET));*/
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
        this.tenantInfoBean = tenantManagementServiceClient.getTenant(tenantDomain);
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
		this.tenantInfoBean = tenantInfoBean;
        String result = createTenantBPELClient
                .createTenant(context, tenantAwareAdminUsername, tenantInfoBean, tenantAdminPassword,
                              "key", "WSO2 Stratos Manager");
        Assert.assertNotNull(result, "Result of createTenantBPELClient.createTenant() is null ");
        Assert.assertTrue(result.contains("true"), "Result of createTenantBPELClient.createTenant() is not true ");
        long timeOutPeriod = AFIntegrationTestUtils.getTimeOutPeriod();
        int retryCount = AFIntegrationTestUtils.getTimeOutRetryCount();
        // Wait until tenant creation completes
        Assert.assertTrue(waitUntilTenantCreationCompletes(timeOutPeriod, retryCount, fullyQualifiedTenantAdmin, tenantAdminPassword),
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

	            login(AFIntegrationTestUtils.getPropertyValue(AFConstants.URLS_DEV_GREG), tenantAdminUsername,
	                  adminPassword, context.getInstance().getHosts().get("default"));

	            login(AFIntegrationTestUtils.getPropertyValue(AFConstants.URLS_TEST_GREG), tenantAdminUsername,
	                  adminPassword, context.getInstance().getHosts().get("default"));

	            login(AFIntegrationTestUtils.getPropertyValue(AFConstants.URLS_PROD_GREG), tenantAdminUsername,
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
                                     String applicationType, String extension, String artifactVersion, String startStage,
                                     String runtimeAlias, String tenantID)
            throws Exception {
        ApplicationClient appMgtRestClient = new ApplicationClient(AFIntegrationTestUtils.getPropertyValue(
                AFConstants.URLS_APPFACTORY)
                , fullyQualifiedTenantAdmin, tenantAdminPassword);
        appMgtRestClient.createNewApplication(applicationName, applicationKey, applicationType,
                                              fullyQualifiedTenantAdmin, applicationDescription);
        long timeOutPeriod = AFIntegrationTestUtils.getTimeOutPeriod();
        int retryCount = AFIntegrationTestUtils.getTimeOutRetryCount();
        // Wait till Create Application completion
        waitUntilApplicationCreationCompletes(30000L, 8, fullyQualifiedTenantAdmin, tenantAdminPassword, applicationKey,

                                              applicationName, extension, artifactVersion, startStage, runtimeAlias,
                                              tenantID);
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
                                                         String applicationName, String applicationExtension,
                                                         String artifactVersion, String stage, String runtimeAlias,
                                                         String tenantID) throws Exception {
        ApplicationClient appMgtRestClient =
                new ApplicationClient(AFIntegrationTestUtils.getPropertyValue(AFConstants.URLS_APPFACTORY),
                                          tenantAdminUsername, adminPassword);

        HttpResponse httpResponse = null;
	    boolean isGitRepoCreated = false;
	    boolean isJenkinsJobCreated = false;
	    boolean isDeployedArtifactAvailable = false;

        int round = 1;
        while (round <= retryCount) {
            try {
                httpResponse = appMgtRestClient.getAppInfo(applicationKey, true);
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

	    round = 1;
	    while (round <= retryCount) {
		    try {
			    log.info("Checking the existence of repo");
			    isGitRepoCreated = isGitRepoExist(applicationKey, AFIntegrationTestUtils.getPropertyValue(AFConstants.URLS_GIT),
			                               AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_TENANT_TENANT_DOMAIN),
			                               AFIntegrationTestUtils.getPropertyValue(AFConstants.CREDENTIAL_GIT_USERNAME),
			                               AFIntegrationTestUtils.getPropertyValue(AFConstants.CREDENTIAL_GIT_PASSWORD));
			    if(!isGitRepoCreated){
				    String msg = "Attempt " + round + " : Repo does not exists , retry after " +
				                 waitInterval + " millis";
				    log.info(msg);
				    Thread.sleep(waitInterval);
				    round++;
				    continue;
			    }
			    break;
		    } catch (Exception e){
			    String msg = "Attempt " + round + " : Check is repo exists failed , retry after " +
			                 waitInterval + " millis, exception is: " + e.getMessage();
			    log.info(msg);
			    if (log.isDebugEnabled()) {
				    log.debug(msg, e);
			    }
			    Thread.sleep(waitInterval);
			    round++;
		    }
	    }
	    Assert.assertEquals(isGitRepoCreated, true, "Git repo creation failed.");

	    round = 1;
	    while (round <= retryCount) {
		    try {
			    log.info("Checking the existence of build job");
			    isJenkinsJobCreated = isJenkinsJobExists(AFIntegrationTestUtils.getPropertyValue(AFConstants.URLS_JENKINS),
			                                   AFIntegrationTestUtils.getJenkinsJobName(applicationKey,
			                                                                            AFIntegrationTestUtils
					                                                                            .getPropertyValue(
							                                                                            AFConstants.DEFAULT_APP_VERSION_NAME)),
			                                   AFIntegrationTestUtils.getPropertyValue(
					                                   AFConstants.CREDENTIAL_JENKINS_USERNAME),
			                                   AFIntegrationTestUtils.getPropertyValue(
					                                   AFConstants.CREDENTIAL_JENKINS_PASSWORD));
			    if(!isJenkinsJobCreated){
				    String msg = "Attempt " + round + " : Job does not exists , retry after " +
				                 waitInterval + " millis";
				    log.info(msg);
				    Thread.sleep(waitInterval);
				    round++;
				    continue;
			    }
			    break;
		    } catch (Exception e) {
			    String msg = "Attempt " + round + " : Check is jenkins job exists failed, retry after " +
			                 waitInterval + " millis, exception is: " + e.getMessage();
			    log.info(msg);
			    if (log.isDebugEnabled()) {
				    log.debug(msg, e);
			    }
			    Thread.sleep(waitInterval);
			    round++;
		    }
	    }
	    Assert.assertEquals(isJenkinsJobCreated, true, "Jenkins job creation failed.");

	    round = 1;
	    while (round <= retryCount) {
		    try {
			    log.info("Checking the existence of deployed artifact");
			    isDeployedArtifactAvailable = isDeployedArtifactAvailable(applicationKey, artifactVersion, stage,
			                                                              runtimeAlias, applicationExtension, tenantID);
			    if(!isDeployedArtifactAvailable){
				    String msg = "Attempt " + round + " : Artifact does not exists , retry after " +
				                 waitInterval + " millis";
				    log.info(msg);
				    Thread.sleep(waitInterval);
				    round++;
				    continue;
			    }
			    break;
		    } catch (Exception e) {
			    String msg = "Attempt " + round + " : Check is artifact exists failed, retry after " +
			                 waitInterval + " millis, exception is: " + e.getMessage();
			    log.info(msg);
			    if (log.isDebugEnabled()) {
				    log.debug(msg, e);
			    }
			    Thread.sleep(waitInterval);
			    round++;
		    }
	    }
	    Assert.assertEquals(isDeployedArtifactAvailable, true, "Deployment failed.");
    }

	/**
	 * Wait until application deletion completes
	 *
	 * @param waitInterval        wait interval
	 * @param retryCount          retry count
	 * @param tenantAdminUsername tenant admin username
	 * @param adminPassword       admin password
	 * @param applicationKey      application key
	 * @param applicationName     application name
	 * @throws Exception
	 */
	public void waitUntilApplicationDeletionCompletes(long waitInterval, int retryCount, String tenantAdminUsername,
	                                                  String adminPassword, String applicationKey,
	                                                  String applicationName, String applicationExtension,
	                                                  String artifactVersion, String stage, String runtimeAlias,
	                                                  String tenantID) throws Exception {
		ApplicationClient appMgtRestClient =
				new ApplicationClient(AFIntegrationTestUtils.getPropertyValue(AFConstants.URLS_APPFACTORY),
				                      tenantAdminUsername, adminPassword);

		HttpResponse httpResponse = null;
		boolean isGitRepoCreated = false;
		boolean isJenkinsJobCreated = false;
		boolean isDeployedArtifactAvailable = false;

		int round = 1;
		while (round <= retryCount) {
			try {
				httpResponse = appMgtRestClient.getAppInfo(applicationKey, false);
				if(httpResponse != null && httpResponse.getData().equals("null")) {
					break;
				}
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
		Assert.assertEquals(httpResponse.getData().equals("null"), true, "Application not deleted");

		round = 1;
		while (round <= retryCount) {
			try {
				log.info("Checking the existence of repo");
				isGitRepoCreated = isGitRepoExist(applicationKey, AFIntegrationTestUtils.getPropertyValue(AFConstants.URLS_GIT),
				                                  AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_TENANT_TENANT_DOMAIN),
				                                  AFIntegrationTestUtils.getPropertyValue(AFConstants.CREDENTIAL_GIT_USERNAME),
				                                  AFIntegrationTestUtils.getPropertyValue(AFConstants.CREDENTIAL_GIT_PASSWORD));
				if(isGitRepoCreated){
					String msg = "Attempt " + round + " : Repo does exists , retry after " +
					             waitInterval + " millis";
					log.info(msg);
					Thread.sleep(waitInterval);
					round++;
					continue;
				}
				break;
			} catch (Exception e){
				String msg = "Attempt " + round + " : Check is repo exists failed , retry after " +
				             waitInterval + " millis, exception is: " + e.getMessage();
				log.info(msg);
				if (log.isDebugEnabled()) {
					log.debug(msg, e);
				}
				Thread.sleep(waitInterval);
				round++;
			}
		}
		Assert.assertEquals(isGitRepoCreated, false, "Git repo deletion failed.");

		round = 1;
		while (round <= retryCount) {
			try {
				log.info("Checking the existence of build job");
				isJenkinsJobCreated = isJenkinsJobExists(AFIntegrationTestUtils.getPropertyValue(AFConstants.URLS_JENKINS),
				                                         AFIntegrationTestUtils.getJenkinsJobName(applicationKey,
				                                                                                  AFIntegrationTestUtils
						                                                                                  .getPropertyValue(
								                                                                                  AFConstants.DEFAULT_APP_VERSION_NAME)),
				                                         AFIntegrationTestUtils.getPropertyValue(
						                                         AFConstants.CREDENTIAL_JENKINS_USERNAME),
				                                         AFIntegrationTestUtils.getPropertyValue(
						                                         AFConstants.CREDENTIAL_JENKINS_PASSWORD));
				if(isJenkinsJobCreated){
					String msg = "Attempt " + round + " : Job does exists , retry after " +
					             waitInterval + " millis";
					log.info(msg);
					Thread.sleep(waitInterval);
					round++;
					continue;
				}
				break;
			} catch (Exception e) {
				String msg = "Attempt " + round + " : Check is jenkins job exists failed, retry after " +
				             waitInterval + " millis, exception is: " + e.getMessage();
				log.info(msg);
				if (log.isDebugEnabled()) {
					log.debug(msg, e);
				}
				Thread.sleep(waitInterval);
				round++;
			}
		}
		Assert.assertEquals(isJenkinsJobCreated, false, "Jenkins job deletion failed.");

		round = 1;
		while (round <= retryCount) {
			try {
				log.info("Checking the existence of deployed artifact");
				isDeployedArtifactAvailable = isDeployedArtifactAvailable(applicationKey, artifactVersion, stage,
				                                                          runtimeAlias, applicationExtension, tenantID);
				if(isDeployedArtifactAvailable){
					String msg = "Attempt " + round + " : Artifact does exists , retry after " +
					             waitInterval + " millis";
					log.info(msg);
					Thread.sleep(waitInterval);
					round++;
					continue;
				}
				break;
			} catch (Exception e) {
				String msg = "Attempt " + round + " : Check is artifact exists failed, retry after " +
				             waitInterval + " millis, exception is: " + e.getMessage();
				log.info(msg);
				if (log.isDebugEnabled()) {
					log.debug(msg, e);
				}
				Thread.sleep(waitInterval);
				round++;
			}
		}
		Assert.assertEquals(isDeployedArtifactAvailable, false, "Undeployment failed.");
	}

	/**
	 * Returns whether the given git repo exists
	 *
	 * @param applicationName name of the application
	 * @param baseUrl base url of git server
	 * @param tenantDomain current tenant domain
	 * @param username username for git server
	 * @param password for git server
	 * @return is git repo exists
	 */
	public boolean isGitRepoExist(String applicationName, String baseUrl, String tenantDomain, String username, String password)
			throws IOException {
		boolean repoExists = false;
		String fullQulifiedRepoName = tenantDomain + "/" + applicationName;
		Map<String, RepositoryModel> repoMap = RpcUtils.getRepositories(baseUrl, username, password.toCharArray());
		for (Map.Entry<String, RepositoryModel> entry : repoMap.entrySet()) {
			String key = entry.getKey().split("r/")[1];
			repoExists = fullQulifiedRepoName.equals(key.split(".git")[0]);
			if (repoExists) {
				return repoExists;
			}
		}
		return repoExists;
	}

	/**
	 * Returns whether the jenkins job exists
	 *
	 * @param jenkinsUrl jenkins base url
	 * @param jobName job name
	 * @param username jenkins username
	 * @param password jenkins user password
	 * @return is jenkins job exists
	 */
	public boolean isJenkinsJobExists (String jenkinsUrl, String jobName, String username, String password)
			throws Exception {
		Map<String, String> headers = new HashMap<String, String>();
		Header header = BasicScheme.authenticate(new UsernamePasswordCredentials(username, password), HTTP.UTF_8, false);
		headers.put(header.getName(), header.getValue());
		final String wrapperTag = "JobNames";
		boolean isExists = false;

		String queryParam = URLEncoder.encode("wrapper=" + wrapperTag + "&xpath="
		                                      + String.format("/*/job/name[text()='%s']", jobName), HTTP.UTF_8);
		URL jenkinsURL = new URL(jenkinsUrl + "/job/" + tenantDomain + "/api/xml");
		HttpResponse jobExistResponse =  HttpRequestUtil.doPost(jenkinsURL, queryParam, headers);

		if (!isSuccessfulStatusCode(jobExistResponse.getResponseCode())) {
			final String errorMsg =
					"Unable to check the existence of job " + jobName + ". jenkins returned, http status : " +
					jobExistResponse.getResponseCode();
			log.error(errorMsg);
			throw new AFIntegrationTestException(errorMsg);
		}

		InputStream stream = new ByteArrayInputStream(jobExistResponse.getData().getBytes(HTTP.UTF_8));
		StAXOMBuilder builder = new StAXOMBuilder(stream);
		Iterator elementIterator = builder.getDocumentElement().getChildElements();
		while (elementIterator.hasNext()) {
			OMElement element = (OMElement) elementIterator.next();
			if(element.getLocalName().equals("job")){
				Iterator jobIterator = element.getChildElements();
				while (jobIterator.hasNext()) {
					OMElement jobChild = (OMElement) jobIterator.next();
					if(jobChild.getLocalName().equals("name") && jobChild.getText().equals(jobName)) {
						isExists = true;
						break;
					}
				}
			}
		}
		return isExists;
	}

	/**
	 * Is deployed web application available in artifact repo
	 * @param applicationKey application key
	 * @param version application version
	 * @param stage current stage
	 * @param runtime name of the runtime to deploy
	 * @param extension extension of the artifact
	 * @param tenantID id of the tenant
	 * @return is deployed artifact available
	 * @throws Exception
	 */
	public boolean isDeployedArtifactAvailable(String applicationKey, String version, String stage,
	                                           String runtime, String extension, String tenantID) throws Exception{
		AppfactoryRepositoryClient client;
		GitAgent gitAgent = new JGitAgent();
		client = new GitRepositoryClient(gitAgent);
		client.init(AFIntegrationTestUtils.getPropertyValue(AFConstants.CREDENTIAL_S2GIT_USERNAME),
		            AFIntegrationTestUtils.getPropertyValue(AFConstants.CREDENTIAL_S2GIT_PASSWORD));
		// working directory
		File workDir = new File(CarbonUtils.getTmpDir() + "/" + UUID.randomUUID());
		if(workDir.exists()){
			FileUtils.forceDelete(workDir);
		}
		// construct repo url for default application
		String repoURL = AFIntegrationTestUtils.getPropertyValue(AFConstants.URLS_S2GIT) + "r/" + stage + "/"
		                 + runtime + "/" + tenantID + ".git";
		client.retireveMetadata(repoURL, false, workDir);
		String artifactName = applicationKey + "-" + version;
		if(!StringUtils.isEmpty(extension)){
			artifactName += "." + extension;
		}
		File artifact = searchFile(workDir, artifactName);
		FileUtils.forceDelete(workDir);
		if(artifact != null) {
			return true;
		}
		return false;
	}

	private boolean isSuccessfulStatusCode(int httpStatusCode) {
		return (httpStatusCode >= HttpStatus.SC_OK && httpStatusCode < MAX_SUCCESS_HTTP_STATUS_CODE);
	}

	/**
	 * Search for the file name recursively inside a folder
	 *
	 * @param baseDir working directory
	 * @param fileName name of the file to search
	 */
	private File searchFile(File baseDir, String fileName){
		File resultFile = null;
		for (File file : baseDir.listFiles()) {
			if (file.isDirectory()) {
				resultFile = searchFile(file, fileName);
			}
			if (fileName.equals(file.getName())) {
				resultFile = file;
			}
			if (resultFile != null){
				return file;
			}
		}
		return null;
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
