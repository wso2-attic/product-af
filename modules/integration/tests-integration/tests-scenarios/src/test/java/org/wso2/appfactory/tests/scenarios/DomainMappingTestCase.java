/*
 * Copyright 2005-2014 WSO2, Inc. (http://wso2.com)
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

package org.wso2.appfactory.tests.scenarios;

import org.apache.commons.httpclient.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.appfactory.integration.test.utils.AFConstants;
import org.wso2.appfactory.integration.test.utils.AppFactoryIntegrationTest;
import org.wso2.appfactory.integration.test.utils.rest.ApplicationRestClient;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;

import javax.xml.xpath.XPathExpressionException;
import java.util.HashMap;
import java.util.Map;

/**
 * Test cases for domain mapping
 */
public class DomainMappingTestCase extends AppFactoryIntegrationTest {
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

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        initWithTenantAndApplicationCreation();
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Add new custom url test")
    public void addNewCustomUrlTest() throws Exception {
        String newUrl = generateCustomUrl(NEW_URL_STEM);
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(REQUEST_KEY_ACTION, ACTION_ADD_NEW_CUSTOM_URL);
        msgBodyMap.put(REQUEST_KEY_APPKEY, getPropertyValue(AFConstants.DEFAULT_APP_APP_KEY));
        msgBodyMap.put(REQUEST_KEY_NEW_URL, newUrl);
        HttpResponse httpResponse = getHttpResponse(msgBodyMap, EP_ADD_NEW_CUSTOM_URL);
        Assert.assertEquals(httpResponse.getResponseCode(), HttpStatus.SC_OK,
                            "Adding new custom url is not success.");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Add a existing url as a custom url test", dependsOnMethods = {"addNewCustomUrlTest"})
    public void addExistingCustomUrlAsNewUrlTest() throws Exception {
        String newUrl = generateCustomUrl(NEW_URL_STEM);
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(REQUEST_KEY_ACTION, ACTION_UPDATE_UNMAPPED_CUSTOM_URL);
        msgBodyMap.put(REQUEST_KEY_APPKEY, getPropertyValue(AFConstants.DEFAULT_APP_APP_KEY));
        msgBodyMap.put(REQUEST_KEY_NEW_URL, newUrl);
        try {
            HttpResponse httpResponse = getHttpResponse(msgBodyMap, EP_UPDATE_CUSTOM_URL);
            Assert.assertNotEquals(httpResponse.getResponseCode(), HttpStatus.SC_OK,
                                   "Should not allow to add an existing url as a new custom url");
        } catch (Exception e) {
            // Pass. Since it should not allow to add existing url as a new custom url
            Assert.assertTrue(true);
        }

    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Update existing unmapped customUrl test", dependsOnMethods = {"addNewCustomUrlTest"})
    public void updateExistingUnmappedCustomUrlTest() throws Exception {
        String newUrl = generateCustomUrl(UPDATED_URL_STEM);
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(REQUEST_KEY_ACTION, ACTION_UPDATE_UNMAPPED_CUSTOM_URL);
        msgBodyMap.put(REQUEST_KEY_APPKEY, getPropertyValue(AFConstants.DEFAULT_APP_APP_KEY));
        msgBodyMap.put(REQUEST_KEY_NEW_URL, newUrl);
        HttpResponse httpResponse = getHttpResponse(msgBodyMap, EP_UPDATE_CUSTOM_URL);
        Assert.assertEquals(httpResponse.getResponseCode(), HttpStatus.SC_OK,
                            "Updating existing custom url is not success.");

    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Remove customUrl from application test")
    public void removeCustomUrlTest() throws Exception {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(REQUEST_KEY_ACTION, ACTION_REMOVE_CUSTOM_URL);
        msgBodyMap.put(REQUEST_KEY_APPKEY, getPropertyValue(AFConstants.DEFAULT_APP_APP_KEY));
        HttpResponse httpResponse = getHttpResponse(msgBodyMap, EP_UPDATE_CUSTOM_URL);
        Assert.assertEquals(httpResponse.getResponseCode(), HttpStatus.SC_OK,
                            "Removing custom url is not success.");
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
        ApplicationRestClient appMgtRestClient = new ApplicationRestClient(
                getPropertyValue(AFConstants.URLS_APPFACTORY),
                getAdminUsername(tenantInfoBean.getAdmin(), tenantInfoBean.getTenantDomain()),
                getPropertyValue(AFConstants.DEFAULT_TENANT_ADMIN_PASSWORD));
        return appMgtRestClient.doPostRequest(epSuffix, keyVal);
    }

    /**
     * Generate a custom url using {@code stem} as a stem
     * @param stem stem
     * @return generated custom url in the format of
     * {@link org.wso2.appfactory.integration.test.utils.AFConstants#DEFAULT_APP_APP_KEY}.{@code stem}.{@link org.wso2.appfactory.integration.test.utils.AFConstants#DOMAIN_MAPPING_DEFAULT_HOST}
     * @throws XPathExpressionException
     */
    private String generateCustomUrl(String stem) throws XPathExpressionException {
        return getPropertyValue(AFConstants.DEFAULT_APP_APP_KEY) + SUB_DOMAIN_SEPARATOR + stem + SUB_DOMAIN_SEPARATOR +
               getPropertyValue(AFConstants.DOMAIN_MAPPING_DEFAULT_HOST);
    }

}
