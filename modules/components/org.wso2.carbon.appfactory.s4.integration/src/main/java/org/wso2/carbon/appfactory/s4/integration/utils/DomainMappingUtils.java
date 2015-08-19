/*
 * Copyright 2005-2013 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.appfactory.s4.integration.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.dao.JDBCAppVersionDAO;
import org.wso2.carbon.appfactory.core.dao.JDBCApplicationDAO;
import org.wso2.carbon.appfactory.core.governance.RxtManager;
import org.wso2.carbon.appfactory.core.dao.ApplicationDAO;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.appfactory.core.util.AppFactoryCoreUtil;
import org.wso2.carbon.appfactory.core.util.CommonUtil;
import org.wso2.carbon.appfactory.s4.integration.DomainMapperEventHandler;
import org.wso2.carbon.appfactory.s4.integration.internal.ServiceReferenceHolder;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

/**
 * Util class for domain mapping
 */
public class DomainMappingUtils {

    // End points
    public static final String DOMAIN_AVAILABLE_END_POINT = "/stratos/admin/cartridge/%s/subscription/%s/domains/%s";
    public static final String ADD_DOMAIN_END_POINT = "/stratos/admin/cartridge/%s/subscription/%s/domains/";
    public static final String REMOVE_DOMAIN_END_POINT = "/stratos/admin/cartridge/%s/subscription/%s/domains/%s";
    public static final String DEFAULT_DOMAIN_SEPERATOR = ".";

    // Messages to display in frontend
    public static final String AF_DOMAIN_AVAILABILITY_ERROR_MSG = "Error occurred while checking availability for domain: \"%s\"";
    public static final String AF_DOMAIN_NOT_AVAILABLE_MSG = "Requested sub domain \"%s\" does not available";
    public static final String AF_ERROR_ADD_DOMAIN_MSG = "Error occurred while updating domain mapping with domain : %s";
    public static final String AF_ERROR_REMOVE_DOMAIN_MSG = "Error occurred while removing domain mapping.";
    public static final String AF_CUSTOM_URL_NOT_VERIFIED = "Requested custom URL %s is not verified";
    public static final String AF_METADATA_ERROR_MSG = "Error occurred while accessing application metadata";
    public static final String AF_ERROR_GENARIC_MSG = "Error occurred while updating domain mapping.";
    public static final String UNVERIFIED_VERIFICATION_CODE = "";
    public static final String UNDEFINED_URL_RXT_VALUE = "";
    public static final String JSON_PAYLOAD_KEY_DOMAINS = "domains";
    public static final String JSON_PAYLOAD_KEY_DOMAIN_NAME = "domainName";
    public static final String JSON_PAYLOAD_KEY_APP_CONTEXT = "applicationContext";
    private static final Log log = LogFactory.getLog(DomainMappingUtils.class);
    private final static RxtManager rxtManager = RxtManager.getInstance();
    private static final String AUTHORIZATION_HEADER = "Authorization";

    // Default prod url
    private static final String DEFAULT_URL_PREFIX = "{prefix}";
    private static final String DEFAULT_URL_MIDDLE = "{middle}";
    private static final String DEFAULT_URL_SUFFIX = "{suffix}";
    private static final String DEFAULT_URL_FORMAT =
            DEFAULT_URL_PREFIX + "." + DEFAULT_URL_MIDDLE + "." + DEFAULT_URL_SUFFIX;

    /**
     * Send rest POST request to Stratos SM.
     *
     * @param stage                         the stage of the Stratos SM
     * @param body                          message body
     * @param addSubscriptionDomainEndPoint end point to send the message
     * @throws AppFactoryException
     */
    public static DomainMappingResponse sendPostRequest(String stage, String body, String addSubscriptionDomainEndPoint)
                                                                                            throws AppFactoryException {
        HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        String endPointUrl = getSMUrl(stage) + addSubscriptionDomainEndPoint;
        PostMethod postMethod = new PostMethod(endPointUrl);

        // password as garbage value since we authenticate with mutual ssl
        postMethod.setRequestHeader(AUTHORIZATION_HEADER, getAuthHeaderValue());
        StringRequestEntity requestEntity;
        try {
            requestEntity = new StringRequestEntity(body, "application/json", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            String msg = "Error while setting parameters";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        postMethod.setRequestEntity(requestEntity);
        return send(httpClient, postMethod);
    }

    /**
     * Get auth header value
     *
     * @return auth header in basic encode
     */
    private static String getAuthHeaderValue() {
        String usernameInContext = CarbonContext.getThreadLocalCarbonContext().getUsername();
        String tenantLessUserName = MultitenantUtils.getTenantAwareUsername(usernameInContext);
        String userName = tenantLessUserName+ UserCoreConstants.TENANT_DOMAIN_COMBINER+
                          CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        // password as garbage value since we authenticate with mutual ssl.
        // We need to set the auth header for requests with the username.
        byte[] getUserPasswordInBytes = (userName + ":nopassword").getBytes();
        String encodedValue = new String(Base64.encodeBase64(getUserPasswordInBytes));
        return "Basic " + encodedValue;
    }

    /**
     * Send REST DELETE request to stratos SM.
     *
     * @param stage                            the stage of the Stratos SM
     * @param removeSubscriptionDomainEndPoint end point to send the message
     * @throws AppFactoryException
     */
    public static DomainMappingResponse sendDeleteRequest(String stage, String removeSubscriptionDomainEndPoint)
                                                                                           throws AppFactoryException {
        HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        String endPointUrl = getSMUrl(stage) + removeSubscriptionDomainEndPoint;
        DeleteMethod deleteMethod = new DeleteMethod(endPointUrl);
        deleteMethod.setRequestHeader(AUTHORIZATION_HEADER, getAuthHeaderValue());
        return send(httpClient, deleteMethod);
    }

    /**
     * Send rest GET request to Stratos SM
     *
     * @param stage                         the stage of the Stratos SM
     * @param getSubscriptionDomainEndPoint end point to send the message
     * @throws AppFactoryException
     */
    public static DomainMappingResponse sendGetRequest(String stage, String getSubscriptionDomainEndPoint)
                                                                                           throws AppFactoryException {
        HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        String endPointUrl = getSMUrl(stage) + getSubscriptionDomainEndPoint;
        GetMethod getMethod = new GetMethod(endPointUrl);
        getMethod.setRequestHeader(AUTHORIZATION_HEADER, getAuthHeaderValue());
        return send(httpClient, getMethod);
    }

    /**
     * Send rest request.
     *
     * @param httpClient client object
     * @param method     method type
     * @throws AppFactoryException
     */
    private static DomainMappingResponse send(HttpClient httpClient, HttpMethodBase method) throws AppFactoryException {
        int responseCode;
        String responseString = null;
        try {
            responseCode = httpClient.executeMethod(method);
        } catch (IOException e) {
            String msg = "Error occurred while executing method " + method.getName();
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        try {
            responseString = method.getResponseBodyAsString();
        } catch (IOException e) {
            String msg = "error while getting response as String for " + method.getName();
            log.error(msg, e);
            throw new AppFactoryException(msg, e);

        } finally {
            method.releaseConnection();
        }
        if (log.isDebugEnabled()) {
            log.debug(" DomainMappingManagementService response id: " + responseCode + " message:  " + responseString);
        }
        return new DomainMappingResponse(responseString, responseCode);
    }

    /**
     * Generate json payload for add new subscription domain
     *
     * @param domain  domain to be mapped
     * @param appKey  application key
     * @param version version to be mapped
     * @param stage   the stage of the Stratos SM
     * @return JSON representation of the payload.
     * <p/>
     * Sample payload
     * domains : {
     * domainName: foo.bar.com,
     * applicationContext: /t/foo.com/webapps/bar-1.0.0
     * }
     */
    public static String generateAddSubscriptionDomainJSON(String domain, String appKey,String version, String stage)
                                                                                           throws AppFactoryException {
        JSONObject domainsJSON = new JSONObject();
        JSONObject domainsDataJSON = new JSONObject();
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            String applicationUrl = AppFactoryCoreUtil.getApplicationUrl(appKey, version, stage, tenantDomain);
            URL url = new URL(applicationUrl);
            domainsDataJSON.put(JSON_PAYLOAD_KEY_DOMAIN_NAME, domain);

            // since in the format of /t/foodotcom/webapps/bar-1.0.0/ we are removing last "/"
            domainsDataJSON.put(JSON_PAYLOAD_KEY_APP_CONTEXT, StringUtils.removeEnd(url.getFile(), "/"));
            domainsJSON.put(JSON_PAYLOAD_KEY_DOMAINS, domainsDataJSON);
            if (log.isDebugEnabled()) {
                log.debug("Add new subscription json payload: " + domainsJSON.toString());
            }
        } catch (JSONException e) {
            String errorMsg = "Error while generating json string for application key : " + appKey + " domain : " + domain +
                              " version : " + version;
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg);
        } catch (MalformedURLException e) {
            String errorMsg = "Error while generating application context for application key : " + appKey + " domain : " +
                              domain + " version : " + version;
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg);
        }
        return domainsJSON.toString();
    }

    /**
     * Generate message content for initial mapping
     *
     * @param domain domain to be mapped
     * @return JSON message
     */
    public static String generateInitialSubscriptionDomainJSON(String domain) throws AppFactoryException {
        JSONObject domainsJSON = new JSONObject();
        JSONObject domainsDataJSON = new JSONObject();
        try {
            domainsDataJSON.put(JSON_PAYLOAD_KEY_DOMAIN_NAME, domain);
            domainsDataJSON.put(JSON_PAYLOAD_KEY_APP_CONTEXT, "/" + getDefaultMappingContext());
            domainsJSON.put(JSON_PAYLOAD_KEY_DOMAINS, domainsDataJSON);
            if (log.isDebugEnabled()) {
                log.debug("Map to initial url json payload: " + domainsJSON.toString());
            }
        } catch (JSONException e) {
            String errorMsg = "Error while generating json string for domain: " + domain + " for initial mapping";
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg);
        }
        return domainsJSON.toString();
    }

    /**
     * Get default application context for domain mapping.
     *
     * @return default application context
     */
    public static String getDefaultMappingContext() {
        return ServiceHolder.getAppFactoryConfiguration().getFirstProperty("DefaultMappingContext");
    }

    /**
     * Get Stratos SM's url.
     *
     * @param stage the stage of the Stratos SM
     * @return base url of the Stratos SM which corresponding to the {@code stage}
     */
    public static String getSMUrl(String stage) {
        return ServiceHolder.getAppFactoryConfiguration().getFirstProperty("ApplicationDeployment.DeploymentStage."
                                                                           + stage + ".TenantMgtUrl");
    }

    /**
     * Publish domain mapping events to registered {@link org.wso2.carbon.appfactory.s4.integration.DomainMapperEventHandler} instances.
     * {@link org.wso2.carbon.appfactory.s4.integration.DomainMapperEventHandler} can perform their specific operations such as add DNS entry etc..
     * based on the event.
     *
     * @param domain domain to be mapped/removed
     * @param action action to to be performed
     * @throws AppFactoryException
     */
    public static void publishToDomainMappingEventHandlers(String domain, DomainMappingAction action) throws
                                                                                                 AppFactoryException {
        Set<DomainMapperEventHandler<?>> domainEventHandler = ServiceReferenceHolder.getInstance()
                .getDomainMapperEventHandler();
        switch (action) {
            case ADD_DOMAIN_MAPPING:
                for (DomainMapperEventHandler<?> aDomainEventHandler1 : domainEventHandler) {
                    DomainMapperEventHandler domainMapperEventHandler =
                            (DomainMapperEventHandler) aDomainEventHandler1;
                    try {
                        domainMapperEventHandler.onDomainMappingCreate(domain);
                    } catch (AppFactoryException e) {
                        String msg =
                                "Error occurred invoking domain mapping created event for " + domain;
                        log.error(msg, e);
                        throw new AppFactoryException(msg, e);
                    }
                }
                break;
            case REMOVE_DOMAIN_MAPPING:
                for (DomainMapperEventHandler<?> aDomainEventHandler : domainEventHandler) {
                    DomainMapperEventHandler domainMapperEventHandler =
                            (DomainMapperEventHandler) aDomainEventHandler;

                    try {
                        domainMapperEventHandler.OnDomainMappingDelete(domain);
                    } catch (AppFactoryException e) {
                        String msg =
                                "Error occurred invoking domain mapping removed event for " + domain;
                        log.error(msg, e);
                        throw new AppFactoryException(msg, e);
                    }
                }
                break;
            default:

        }

    }

    /**
     * Get sub domain of the default url
     *
     * @param defaultUrl default url
     * @return the substring before the first occurrence of the default host domain, null if null String input or {@code defaultUrl} does not has the default domain
     */
    public static String getSubDomain(String defaultUrl) {
        String defaultHostName =
                DEFAULT_DOMAIN_SEPERATOR + ServiceHolder.getAppFactoryConfiguration().getFirstProperty("DomainName");
        if (StringUtils.contains(defaultUrl, defaultHostName)) {
            return StringUtils.substringBefore(defaultUrl, defaultHostName);
        } else {
            return null;
        }
    }

    /**
     * Generate the verification code for custom url
     *
     * @param customUrl custom url
     * @return verification code. Will return {@link org.wso2.carbon.appfactory.s4.integration.utils.DomainMappingUtils#UNVERIFIED_VERIFICATION_CODE}
     * if {@code customUrl} is null or empty.
     */
    public static String generateVerificationCode(String customUrl) {
        // set verification code as hash value of the custom url.
        // this value will use as a pre check, when adding a domain mapping
        if (StringUtils.isNotBlank(customUrl)) {
            return Integer.toString(customUrl.hashCode());
        } else {
            return DomainMappingUtils.UNVERIFIED_VERIFICATION_CODE;
        }

    }

    /**
     * Get the custom domain of the application
     *
     * @param appKey application key
     * @return custom domain. null if custom domain is not defined
     * @throws AppFactoryException
     */
    public static String getCustomDomain(String appKey) throws AppFactoryException {
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String customDomain;
        try {
            customDomain = ApplicationDAO.getInstance().getAppInfoRxtValue(appKey,
                                                        AppFactoryConstants.RXT_KEY_APPINFO_CUSTOM_URL,tenantDomain);
        } catch (AppFactoryException e) {
            log.error("Error occurred while getting custom url for :" + appKey, e);
            throw new AppFactoryException(AF_METADATA_ERROR_MSG);
        }
        return customDomain;
    }

    /**
     * Get default production url for the application {@code appKey}.
     *
     * @param appKey application key
     * @return default production url. null if not defined
     * @throws org.wso2.carbon.appfactory.common.AppFactoryException
     */
    public static String getDefaultDomain(String appKey) throws AppFactoryException {
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String defaultSubDomain;
        try {
            defaultSubDomain = ApplicationDAO.getInstance().getAppInfoRxtValue(appKey,
                                                    AppFactoryConstants.RXT_KEY_APPINFO_DEFAULT_DOMAIN, tenantDomain);
        } catch (AppFactoryException e) {
            log.error("Error occurred while getting default url for :" + appKey, e);
            throw new AppFactoryException(AF_METADATA_ERROR_MSG);
        }
        if (StringUtils.isNotBlank(defaultSubDomain)) {
            // Since we are saving only the subdomain, we append host domain to the end
            return defaultSubDomain + DEFAULT_DOMAIN_SEPERATOR +
                   ServiceHolder.getAppFactoryConfiguration().getFirstProperty("DomainName");
        } else {
            return null;
        }

    }

    /**
     * Updates the registry(appversion) with mapped domain.
     *
     * @param appKey       application key
     * @param version      mapped version
     * @param mappedDomain mapped domain
     * @throws org.wso2.carbon.appfactory.common.AppFactoryException
     */
    public static void updateVersionMetadataWithMappedDomain(String appKey, String version, String mappedDomain)
            throws AppFactoryException {
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            JDBCAppVersionDAO.getInstance().updateSubDomainsOfVersion(appKey, version, mappedDomain);
        } catch (AppFactoryException e) {
            log.error("Error occurred while updating the appversion rxt with mapped domain for application id: " +
                      appKey + " version:" + version + " domain: " + mappedDomain, e);
            throw new AppFactoryException(String.format(AF_ERROR_ADD_DOMAIN_MSG, mappedDomain));
        }
    }

    /**
     * Updates the registry(appinfo) with default mapped domain.
     *
     * @param appKey       application key
     * @param mappedDomain mapped domain
     * @throws org.wso2.carbon.appfactory.common.AppFactoryException
     */
    public static void updateApplicationMetaDataMappedDomain(String appKey, String mappedDomain)
            throws AppFactoryException {
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            String subDomain = UNDEFINED_URL_RXT_VALUE;

            // Since we are saving sub domain of the default domain
            String defaultSubDomain = getSubDomain(mappedDomain);
            if (StringUtils.isNotBlank(defaultSubDomain)) {
                subDomain = defaultSubDomain;
            } else if (!StringUtils.equals(UNDEFINED_URL_RXT_VALUE,
                                           mappedDomain)) {          // if given mappedDomain  does not derived from the default domain host and does not equal to UNDEFINED_URL_RXT_VALUE
                log.warn("Requested default url:" + mappedDomain + " does not derived from the default domain host: " +
                         ServiceHolder.getAppFactoryConfiguration().getFirstProperty("DomainName") +
                         " application id:" + appKey);
            }
            ApplicationDAO.getInstance().updateAppInfoRxt(appKey, AppFactoryConstants.RXT_KEY_APPINFO_DEFAULT_DOMAIN,
                                                             subDomain, tenantDomain);

        } catch (AppFactoryException e) {
            log.error("Error occurred while updating the application rxt with mapped domain for application id: " +
                      appKey + " domain: " + mappedDomain, e);
            throw new AppFactoryException(String.format(AF_ERROR_ADD_DOMAIN_MSG, mappedDomain));
        }
    }

    /**
     * Updates the registry(appinfo) with custom domain and verification code.
     *
     * @param appKey           application key
     * @param mappedDomain     mapped domain
     * @param verificationCode verification code for the {@code mappedDomain}. {@code UNVERIFIED_VERIFICATION_CODE} if not verified
     * @throws AppFactoryException
     */
    public static void updateCustomUrlMetadata(String appKey, String mappedDomain, String verificationCode)
            throws AppFactoryException {
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            ApplicationDAO.getInstance().updateAppInfoRxt(appKey, AppFactoryConstants.RXT_KEY_APPINFO_CUSTOM_URL,
                                                             mappedDomain, tenantDomain);
            ApplicationDAO.getInstance().updateAppInfoRxt(appKey,
                                                             AppFactoryConstants.RXT_KEY_APPINFO_CUSTOM_URL_VERIFICATION,
                                                             verificationCode, tenantDomain);
        } catch (AppFactoryException e) {
            String msg = "Error occurred while updating the application rxt with custom domain for application id: " +
                         appKey + " domain: " + mappedDomain;
            log.error(msg, e);
            throw new AppFactoryException(String.format(AF_ERROR_ADD_DOMAIN_MSG, mappedDomain));
        }
    }

    /**
     * Update registry
     *
     * @param appKey         application key
     * @param mappedDomain   mapped domain
     * @param version        mapped version
     * @param isCustomDomain whether {@code mappedDomain} is custom domain or not
     * @throws org.wso2.carbon.appfactory.common.AppFactoryException
     */
    public static void updateMetadata(String appKey, String mappedDomain, String version, boolean isCustomDomain)
            throws AppFactoryException {
        // update appinfo rxt
        if (isCustomDomain) {
            updateCustomUrlMetadata(appKey, mappedDomain, generateVerificationCode(mappedDomain));
        } else {
            updateApplicationMetaDataMappedDomain(appKey, mappedDomain);
        }

        // update version info rxt
        if (StringUtils.isNotBlank(version)) {
            updateVersionMetadataWithMappedDomain(appKey, version, mappedDomain);
        }

    }

    /**
     * Get domain available end point suffix.
     *
     * @param stage   the stage of the Stratos SM
     * @param domain  domain to be checked for availability
     * @param appType application type
     * @return domain availability end point in the format of
     * "/stratos/admin/cartridge/{cartridgeType}/subscription/{subscriptionAlias}/domains/{@code domain}"
     * <p/>
     * e.g: /stratos/admin/cartridge/samproduction/subscription/assamdotcom/domains/app1.sam.com
     */
    public static String getDomainAvailableEndPoint(String stage, String domain, String appType)
            throws AppFactoryException {
        return String.format(DOMAIN_AVAILABLE_END_POINT, CommonUtil.getCartridgeType(stage, appType),
                             CommonUtil.getSubscriptionAlias(stage, appType), domain);
    }

    /**
     * Get add domain end point suffix.
     *
     * @param stage   the stage of the Stratos SM
     * @param appType application type
     * @return add domain end point in the format of
     * "/stratos/admin/cartridge/{cartridgeType}/subscription/{subscriptionAlias}/domains/"
     * <p/>
     * e.g: /stratos/admin/cartridge/samproduction/subscription/assamdotcom/domains/
     */
    public static String getAddDomainEndPoint(String stage, String appType) throws AppFactoryException {
        return String.format(ADD_DOMAIN_END_POINT, CommonUtil.getCartridgeType(stage, appType),
                             CommonUtil.getSubscriptionAlias(stage, appType));
    }

    /**
     * Get remove domain end point suffix
     *
     * @param stage   the stage of the Stratos SM
     * @param domain  domain to be removed
     * @param appType application type
     * @return remove domain end point in the format of
     * "/stratos/admin/cartridge/{cartridgeType}/subscription/{subscriptionAlias}/domains/{@code domain}"
     * <p/>
     * e.g /stratos/admin/cartridge/samproduction/subscription/assamdotcom/domains/app1.sam.com
     */
    public static String getRemoveDomainEndPoint(String stage, String domain, String appType)
            throws AppFactoryException {
        return String.format(REMOVE_DOMAIN_END_POINT, CommonUtil.getCartridgeType(stage, appType),
                             CommonUtil.getSubscriptionAlias(stage, appType), domain);
    }

    /**
     * Generate default production url in the format of {@code prefix}.{@code middle}.{@code suffix}
     *
     * @param prefix prefix of the url
     * @param middle middle part of the url
     * @param suffix suffix of the url
     * @return production url
     */
    public static String generateDefaultProdUrl(String prefix, String middle, String suffix) {
        return DEFAULT_URL_FORMAT.replace(DEFAULT_URL_PREFIX, prefix).replace(DEFAULT_URL_MIDDLE,
                                                                              middle).replace(DEFAULT_URL_SUFFIX,
                                                                                              suffix);
    }
}
