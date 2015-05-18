package org.wso2.appfactory.integration.test.utils.rest;

import org.apache.commons.httpclient.HttpStatus;
import org.testng.Assert;
import org.wso2.appfactory.integration.test.utils.AFConstants;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestException;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestUtils;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;

import javax.xml.xpath.XPathExpressionException;
import java.util.HashMap;
import java.util.Map;

public class DomainMappingClient extends BaseClient {

    public static final String ACTION_ADD_NEW_CUSTOM_URL = "addNewCustomUrl";
    public static final String ACTION_UPDATE_UNMAPPED_CUSTOM_URL = "updateExistingUnmappedCustomUrl";
    public static final String ACTION_REMOVE_CUSTOM_URL = "removeCustomUrl";
    public static final String REQUEST_KEY_ACTION = "action";
    public static final String REQUEST_KEY_APPKEY = "applicationKey";
    public static final String EP_ADD_NEW_CUSTOM_URL = "urlmapper/add/ajax/add.jag";
    public static final String EP_UPDATE_CUSTOM_URL = "urlmapper/update/ajax/update.jag";
    public static final String NEW_URL_STEM = "test_new_url";
    public static final String UPDATED_URL_STEM = "test_updated_url";
    public static final String SUB_DOMAIN_SEPARATOR = ".";
    private static final String REQUEST_KEY_NEW_URL = "newCustomUrl";


    /**
     * Construct authenticates REST client to invoke appmgt functions
     *
     * @param backEndUrl backend url
     * @param username   username
     * @param password   password
     * @throws Exception
     */
    public DomainMappingClient(String backEndUrl, String username, String password) throws Exception {
        super(backEndUrl, username, password);
    }

    public void addNewCustomUrl() throws Exception {
        String newUrl;
        try {
            newUrl = generateCustomUrl(NEW_URL_STEM);
        } catch (XPathExpressionException e) {
            throw new Exception("Custom URL generation failed.",e);
        }
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(REQUEST_KEY_ACTION, ACTION_ADD_NEW_CUSTOM_URL);
        msgBodyMap.put(REQUEST_KEY_APPKEY, AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_APP_KEY));
        msgBodyMap.put(REQUEST_KEY_NEW_URL, newUrl);
        HttpResponse httpResponse = getHttpResponse(msgBodyMap, EP_ADD_NEW_CUSTOM_URL);
        Assert.assertEquals(httpResponse.getResponseCode(), HttpStatus.SC_OK,
                            "Adding new custom url is not success.");
    }

    public void addExistingCustomUrlAsNewUrl() throws Exception{
        String newUrl = generateCustomUrl(NEW_URL_STEM);
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(REQUEST_KEY_ACTION, ACTION_UPDATE_UNMAPPED_CUSTOM_URL);
        msgBodyMap.put(REQUEST_KEY_APPKEY, AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_APP_KEY));
        msgBodyMap.put(REQUEST_KEY_NEW_URL, newUrl);
        HttpResponse httpResponse = getHttpResponse(msgBodyMap, EP_UPDATE_CUSTOM_URL);
        Assert.assertNotEquals(httpResponse.getResponseCode(), HttpStatus.SC_OK,
                               "Should not allow to add an existing url as a new custom url");
    }

    /**
     * Generate a custom url using {@code stem} as a stem
     * @param stem stem
     * @return generated custom url in the format of
     * {@link org.wso2.appfactory.integration.test.utils.AFConstants#DEFAULT_APP_APP_KEY}.{@code stem}.
     * {@link org.wso2.appfactory.integration.test.utils.AFConstants#DOMAIN_MAPPING_DEFAULT_HOST}
     * @throws javax.xml.xpath.XPathExpressionException
     */
    private String generateCustomUrl(String stem) throws XPathExpressionException {
        return AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_APP_KEY) + SUB_DOMAIN_SEPARATOR + stem +
               SUB_DOMAIN_SEPARATOR + AFIntegrationTestUtils.getPropertyValue(AFConstants.DOMAIN_MAPPING_DEFAULT_HOST);
    }

    /**
     * Gets https response from the BE server
     *
     * @param keyVal   parameters as key-val map
     * @param epSuffix suffix of the end point url
     * @return http response from the back end
     * @throws Exception
     */
    private HttpResponse getHttpResponse(Map<String, String> keyVal, String epSuffix) throws Exception {
        ApplicationClient appMgtRestClient = new ApplicationClient(
                AFIntegrationTestUtils.getPropertyValue(AFConstants.URLS_APPFACTORY),
                AFIntegrationTestUtils.getAdminUsername(),
                AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_TENANT_ADMIN_PASSWORD));
        return appMgtRestClient.doPostRequest(epSuffix, keyVal);
    }
}
