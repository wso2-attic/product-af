package org.wso2.appfactory.integration.test.utils;

import com.gitblit.models.RepositoryModel;
import com.gitblit.utils.RpcUtils;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import javax.net.ssl.SSLContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	 * Returns whether the given git repo exists
	 *
	 * @param repoName name of the repository
	 * @param baseUrl base url of git server
	 * @param username username for git server
	 * @param password for git server
	 * @return is git repo exists
	 */
	public static boolean isGitRepoExist(String repoName, String baseUrl, String username, String password)
			throws IOException {
		boolean repoExists = false;
		Map<String, RepositoryModel> repoMap = RpcUtils.getRepositories(baseUrl, username, password.toCharArray());
		for (Map.Entry<String, RepositoryModel> entry : repoMap.entrySet()) {
			String key = entry.getKey().split("r/")[1];
			repoExists = repoName.equals(key.split(".git")[0]);
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
	public static boolean isJenkinsJobExists (String jenkinsUrl, String jobName, String username, String password)
			throws IOException, XMLStreamException, URISyntaxException, KeyManagementException,
			       NoSuchAlgorithmException, AFIntegrationTestException {

		ThreadSafeClientConnManager threadSafeClientConnManager = new ThreadSafeClientConnManager();
		threadSafeClientConnManager.setDefaultMaxPerRoute(1000);
		threadSafeClientConnManager.setMaxTotal(1000);
		HttpClient httpClient = new DefaultHttpClient(threadSafeClientConnManager);
		final String wrapperTag = "JobNames";

		List<NameValuePair> queryParameters = new ArrayList<NameValuePair>();
		queryParameters.add(new BasicNameValuePair("wrapper", wrapperTag));
		queryParameters.add(new BasicNameValuePair("xpath", String.format("/*/job/name[text()='%s']", jobName)));

		HttpGet checkJobExistsMethod = createGet(jenkinsUrl, "/job/"+tenantDomain+"/api/xml", queryParameters,
		                                         username, password);

		boolean isExists = false;
		HttpResponse jobExistResponse = null;

		jobExistResponse = httpClient.execute(checkJobExistsMethod, getHttpContext(httpClient));
		int httpStatusCode = jobExistResponse.getStatusLine().getStatusCode();

		if (!isSuccessfulStatusCode(httpStatusCode)) {
			final String errorMsg =
					"Unable to check the existence of job " + jobName + ". jenkins returned, http status : " +
					httpStatusCode;
			log.error(errorMsg);
			throw new AFIntegrationTestException(errorMsg);
		}

		StAXOMBuilder builder = new StAXOMBuilder(jobExistResponse.getEntity().getContent());
		isExists = builder.getDocumentElement().getChildElements().hasNext();
		EntityUtils.consume(jobExistResponse.getEntity());

		return isExists;
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

	private static HttpGet createGet(String jenkinsUrl, String urlFragment, List<NameValuePair> queryParameters,
	                                 String username, String apiKeyOrPassword)
			throws MalformedURLException, URISyntaxException {

		String query = null;
		HttpGet get;

		if (queryParameters != null) {
			query = URLEncodedUtils.format(queryParameters, HTTP.UTF_8);
		}
		URL url = new URL(jenkinsUrl);
		URI uri = URIUtils.createURI(url.getProtocol(), url.getHost(), url.getPort(), urlFragment, query, null);
		get = new HttpGet(uri);
		get.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials(username, apiKeyOrPassword),
					                         HTTP.UTF_8, false));
		return get;
	}

	private static HttpContext getHttpContext(HttpClient httpClient)
			throws NoSuchAlgorithmException, KeyManagementException {
		HttpContext httpContext = new BasicHttpContext();
		SSLContext sslContext;
		sslContext = SSLContext.getInstance(SSLSocketFactory.TLS);
		sslContext.init(null, null, null);
		SSLSocketFactory sf = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		Scheme sch = new Scheme("https", 443, sf);
		httpClient.getConnectionManager().getSchemeRegistry().register(sch);
		return httpContext;
	}

	private static boolean isSuccessfulStatusCode(int httpStatusCode) {
		return (httpStatusCode >= HttpStatus.SC_OK && httpStatusCode < MAX_SUCCESS_HTTP_STATUS_CODE);
	}

}
