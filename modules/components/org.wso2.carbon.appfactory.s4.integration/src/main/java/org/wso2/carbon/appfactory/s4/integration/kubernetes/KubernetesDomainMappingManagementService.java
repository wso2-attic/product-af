package org.wso2.carbon.appfactory.s4.integration.kubernetes;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.dao.ApplicationDAO;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.appfactory.eventing.AppFactoryEventException;
import org.wso2.carbon.appfactory.eventing.Event;
import org.wso2.carbon.appfactory.eventing.EventNotifier;
import org.wso2.carbon.appfactory.eventing.builder.utils.AppInfoUpdateEventBuilderUtil;
import org.wso2.carbon.appfactory.provisioning.runtime.KubernetesRuntimeProvisioningService;
import org.wso2.carbon.appfactory.provisioning.runtime.RuntimeProvisioningException;
import org.wso2.carbon.appfactory.provisioning.runtime.Utils.KubernetesProvisioningUtils;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.ApplicationContext;
import org.wso2.carbon.appfactory.s4.integration.DomainMappingVerificationException;
import org.wso2.carbon.appfactory.s4.integration.internal.ServiceReferenceHolder;
import org.wso2.carbon.appfactory.s4.integration.utils.DomainMappingUtils;
import org.wso2.carbon.context.CarbonContext;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Random;
import java.util.Set;

public class KubernetesDomainMappingManagementService {


    private static final Log log = LogFactory.getLog(KubernetesDomainMappingManagementService.class);

    // DNS records
    public static final String DNS_A_RECORD = "A";
    public static final String DNS_CNAME_RECORD = "CNAME";

    // App wall messages
    private static final String AF_APPWALL_SUCCESS_MSG = "Production URL updated successfully.";
    private static final String AF_APPWALL_REMOVE_SUCCESS_MSG = "Production URL removed successfully.";
    private static final String AF_APPWALL_ERROR_MSG = "Failed to update the domain.";
    private static final String AF_APPWALL_CUSTOM_URL_INVALID_MSG = "Fail to verify custom URL.";
    private static final String AF_APPWALL_URL = "URL: %s";

    private static final String JNDI_KEY_NAMING_FACTORY_INITIAL = "java.naming.factory.initial";
    private static final String JNDI_KEY_DNS_TIMEOUT = "com.sun.jndi.dns.timeout.initial";
    private static final String JDNI_KEY_DNS_RETRIES = "com.sun.jndi.dns.timeout.retries";

    /**
     * Add new domain mapping entry to stratos.
     *
     * @param stage          mapping stage
     * @param domain         e.g: some.organization.org this doesn't require the protocol such as http/https
     * @param appKey         application key
     * @param version        version to be mapped, null if map to default page
     * @param isCustomDomain whether {@code domain} is custom domain or not
     * @throws AppFactoryException                if an error occurred during the operation
     * @throws DomainMappingVerificationException if the requested {@code domain} is not verified, given that it is a
     *                                            custom domain({@code isCustomDomain} is true). To find more info on
     *                                            verification method, please refer to {@link
     *                                            #verifyCustomUrlForApplication}
     */
    public void addSubscriptionDomain(String stage, String domain, String appKey, String version,
            boolean isCustomDomain)
            throws AppFactoryException, DomainMappingVerificationException {
        String appType = ApplicationDAO.getInstance().getApplicationInfo(appKey).getType();
        String appName = ApplicationDAO.getInstance().getApplicationInfo(appKey).getName();
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        int tenantId =  CarbonContext.getThreadLocalCarbonContext().getTenantId();
        ApplicationContext applicationContext = KubernetesProvisioningUtils.getApplicationContext(appKey,
                version, stage, appType, tenantId, tenantDomain);

        KubernetesRuntimeProvisioningService kubernetesRuntimeProvisioningService =
                new KubernetesRuntimeProvisioningService(applicationContext);

        Set<String> domains = new HashSet<String>();
        domains.add(domain);
        boolean isAdded = false;

        synchronized (domain.intern()) {

            // Check domain availability
            boolean domainAvailable = isDomainAvailable(appKey, version, stage, domain, appType);
            if (!domainAvailable) {
                // Throws an exception, if requested domain is not available.
                log.error("Requested domain: " + domain + " does not available for application id :" + appKey +
                        " for stage :" + stage);
                throw new AppFactoryException(String.format(DomainMappingUtils.AF_DOMAIN_NOT_AVAILABLE_MSG, domain));
            }

            // if the given domain is custom domain, check whether domain is verified
            if (isCustomDomain && !verifyCustomUrlForApplication(domain, appKey)) {
                // if custom utl is not verified, throw an exception. Here we are throwing DomainMappingVerificationException therefore
                // we could identify the error occurred due to unsuccessful verification
                notifyAppWall(appKey, AF_APPWALL_CUSTOM_URL_INVALID_MSG, String.format(AF_APPWALL_URL, domain),
                        Event.Category.ERROR);
                log.warn("Requested custom domain :" + domain + " is not verified for application id :" + appKey +
                        " for stage :" + stage);
                throw new DomainMappingVerificationException(
                        String.format(DomainMappingUtils.AF_CUSTOM_URL_NOT_VERIFIED, domain));
            }

            try {

                isAdded = kubernetesRuntimeProvisioningService.addCustomDomain(domains);

            } catch (RuntimeProvisioningException e) {
                log.error("Error occurred adding domain mappings to appkey " + appKey + " version " + version +
                        " domain " + domain, e);
                //Notifying the domain mapping failure to app wall
                notifyAppWall(appKey, AF_APPWALL_ERROR_MSG, "", Event.Category.ERROR);
                throw new AppFactoryException(String.format(DomainMappingUtils.AF_ERROR_ADD_DOMAIN_MSG, domain));
            }

            if (isAdded) {
                log.info("Successfully added domain mapping for application: " + appKey + " domain:" + domain +
                        (StringUtils.isNotBlank(version) ? (" to version: " + version) : ""));
                //Notifying the domain mapping success to app wall
                notifyAppWall(appKey, AF_APPWALL_SUCCESS_MSG, String.format(AF_APPWALL_URL, domain),
                        Event.Category.INFO);
            } else {
                notifyAppWall(appKey, AF_APPWALL_ERROR_MSG, "", Event.Category.ERROR);
                log.error(String.format(DomainMappingUtils.AF_ERROR_ADD_DOMAIN_MSG, domain));
                throw new AppFactoryException(String.format(DomainMappingUtils.AF_ERROR_ADD_DOMAIN_MSG, domain));
            }
        } // end of synchronized
    }

    /**
     * Add default production url, if its failed during app creation time
     *
     * @param appKey  application key
     * @param version version of the application
     * @throws AppFactoryException
     */
    public void addDefaultProdUrl(String appKey, String version) throws AppFactoryException {
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String defaultHostName = ServiceReferenceHolder.getInstance().getAppFactoryConfiguration().getFirstProperty(
                AppFactoryConstants.DOMAIN_NAME);
        String defaultUrl = DomainMappingUtils.generateDefaultProdUrl(
                appKey + AppFactoryConstants.MINUS + (new Random()).nextInt(1000),
                tenantDomain.replace(".", ""), defaultHostName);
        try {
            addSubscriptionDomain(ServiceReferenceHolder.getInstance().getAppFactoryConfiguration().getFirstProperty(
                    AppFactoryConstants.FINE_GRAINED_DOMAIN_MAPPING_ALLOWED_STAGE), defaultUrl, appKey, version, false);
        } catch (DomainMappingVerificationException e) {
            // we are logging this as warn messages since this is caused, due to an user error. For example if the
            // user entered a rubbish custom url(Or a url which is, CNAME record is not propagated at the
            // time of adding the url), then url validation will fail but it is not an system error
            log.warn(String.format(DomainMappingUtils.AF_CUSTOM_URL_NOT_VERIFIED, defaultUrl), e);
            throw new AppFactoryException(String.format(DomainMappingUtils.AF_CUSTOM_URL_NOT_VERIFIED, defaultUrl));
        }
        DomainMappingUtils.updateMetadata(appKey, defaultUrl, version, false);
    }

    /**
     * Add new domain mapping entry by mapping it to initial url
     *
     * @param stage          mapping stage
     * @param domain         e.g: some.organization.org this doesn't require the protocol such as http/https
     * @param appKey         application key
     * @param version        version to be mapped
     * @param isCustomDomain whether {@code domain} is custom domain or not
     */
    public void addNewSubscriptionDomain(String stage, String domain, String appKey, String version,
            boolean isCustomDomain) throws AppFactoryException {
        try {
            addSubscriptionDomain(stage, domain, appKey, version, isCustomDomain);
        } catch (DomainMappingVerificationException e) {
            // we are logging this as warn messages since this is caused, due to an user error. For example if the
            // user entered a rubbish custom url(Or a url which is, CNAME record is not propagated at the
            // time of adding the url), then url validation will fail but it is not an system error
            log.warn(String.format(DomainMappingUtils.AF_CUSTOM_URL_NOT_VERIFIED, domain), e);
            // update the custom url values of the rxt.
            DomainMappingUtils.updateCustomUrlMetadata(appKey, domain, DomainMappingUtils.UNVERIFIED_VERIFICATION_CODE);
            throw new AppFactoryException(String.format(DomainMappingUtils.AF_CUSTOM_URL_NOT_VERIFIED, domain));
        }
        DomainMappingUtils.updateMetadata(appKey, domain, version, isCustomDomain);

    }

    /**
     * Remap given domain to given {@code version}
     * <p/>
     * Example
     * before
     * domain   -> appserver.cloud.wso2.com/t/mytenant/webapps/abc_1.0.0
     * <p/>
     * after
     * domain   -> appserver.cloud.wso2.com/t/mytenant/webapps/abc_2.0.0
     *
     * @param stage           mapping stage
     * @param domain          default domain.e.g: some.organization1.org this doesn't require the protocol such as http/https
     * @param appKey          application key
     * @param newVersion      version to be mapped
     * @param previousVersion previously mapped version
     * @param isCustomDomain  whether the given domains are related to custom domains or not
     * @throws AppFactoryException
     */
    public void remapDomainToContext(String stage, String domain, String appKey,
            String newVersion, String previousVersion, boolean isCustomDomain) throws AppFactoryException {
        // remove existing mapping
        removeSubscriptionDomain(previousVersion, stage, domain, appKey);
        if (StringUtils.isNotBlank(
                previousVersion)) {   // if the domain is already mapped, remove the domain from previousVersion
            DomainMappingUtils.updateVersionMetadataWithMappedDomain(appKey, previousVersion, "");
        }
        try {
            addSubscriptionDomain(stage, domain, appKey, newVersion, isCustomDomain);
        } catch (DomainMappingVerificationException e) {
            // we are logging this as warn messages since this is caused, due to an user error. For example if the
            // user entered a rubbish custom url(Or a url which is, CNAME record is not propagated at the
            // time of adding the url), then url validation will fail but it is not an system error
            log.warn(String.format(DomainMappingUtils.AF_CUSTOM_URL_NOT_VERIFIED, domain), e);
            // update the custom url values of the rxt.
            if (isCustomDomain) {
                DomainMappingUtils.updateCustomUrlMetadata(appKey, domain,
                        DomainMappingUtils.UNVERIFIED_VERIFICATION_CODE);
            }
            throw new AppFactoryException(String.format(DomainMappingUtils.AF_CUSTOM_URL_NOT_VERIFIED, domain));
        }
        DomainMappingUtils.updateMetadata(appKey, domain, newVersion, isCustomDomain);
    }

    /**
     * Remap given existing production urls(custom and default) to given {@code version}
     * <p/>
     * Example
     * before
     * domain   -> appserver.cloud.wso2.com/t/mytenant/webapps/abc_1.0.0
     * <p/>
     * after
     * domain   -> appserver.cloud.wso2.com/t/mytenant/webapps/abc_2.0.0
     *
     * @param stage           mapping stage
     * @param appKey          application key
     * @param newVersion      version to be mapped
     * @param previousVersion previously mapped version
     * @throws AppFactoryException
     */
    public void remapDomainToContext(String stage, String appKey, String newVersion, String previousVersion)
            throws AppFactoryException {
        // remap default domain
        String defaultDomain = DomainMappingUtils.getDefaultDomain(appKey);
        if (StringUtils.isNotBlank(defaultDomain)) {
            remapDomainToContext(stage, defaultDomain, appKey, newVersion, previousVersion, false);
        }

        // remap custom domains
        String customDomain = DomainMappingUtils.getCustomDomain(appKey);
        if (StringUtils.isNotBlank(customDomain)) {
            remapDomainToContext(stage, customDomain, appKey, newVersion, previousVersion, true);
        }
    }

    /**
     * Remap given version from existing mapped domain to {@code newDomain}.<br>
     * If existing mapped domain is null, then it will just map {@code version} to {@code newDomain}.<br>
     * <p/>
     * Example
     * [before]
     * previousDomain   -> appserver.cloud.wso2.com/t/mytenant/webapps/abc_1.0.0
     * newDomain        -> null
     * <p/>
     * [after]
     * previousDomain   -> null
     * newDomain        -> appserver.cloud.wso2.com/t/mytenant/webapps/abc_1.0.0
     *
     * @param stage          mapping stage
     * @param newDomain      new domain name. e.g: some.organization1.org this doesn't require the protocol such as
     *                       http/https
     * @param appKey         application key
     * @param version        version to be mapped
     * @param isCustomDomain whether the given domains are related to custom domains or not
     * @throws AppFactoryException
     */
    public void remapContextToDomain(String stage, String newDomain,
            String appKey, String version, boolean isCustomDomain) throws AppFactoryException {
        // Get previously mapped domain, since we have to remove the previous domain mapping
        String previousDomain;
        if (isCustomDomain) {
            previousDomain = DomainMappingUtils.getCustomDomain(appKey);
        } else {
            previousDomain = DomainMappingUtils.getDefaultDomain(appKey);
        }

        // First add the new domain
        try {
            addSubscriptionDomain(stage, newDomain, appKey, version, isCustomDomain);
        } catch (DomainMappingVerificationException e) {    //validation unsuccessful for custom urls
            // we are logging this as warn messages since this is caused, due to an user error. For example if the
            // user entered a rubbish custom url(Or a url which is, CNAME record is not propagated at the
            // time of adding the url), then url validation will fail but it is not an system error
            log.warn(String.format(DomainMappingUtils.AF_CUSTOM_URL_NOT_VERIFIED, newDomain), e);
            // update the custom url values of the rxt with new values.
            DomainMappingUtils.updateCustomUrlMetadata(appKey, newDomain,
                    DomainMappingUtils.UNVERIFIED_VERIFICATION_CODE);

            // Since we are going to keep the new domain, as the custom url
            // remove existing domain mapping for previous domain
            if (StringUtils.isNotBlank(previousDomain)) {
                try {
                    removeSubscriptionDomain(version, stage, previousDomain, appKey);
                } catch (AppFactoryException e1) {
                    String msg = "Domain validation unsuccessful for domain : " + newDomain +
                            " and error occurred removing domain mapping for previous domain " + previousDomain +
                            " for application id: " + appKey;
                    // Removing previous domain should not interrupt the operation. therefore we are not going to throw an exception here
                    log.error(msg, e1);
                }
            }
            // throw an exception with not validated message to display in UI
            throw new AppFactoryException(String.format(DomainMappingUtils.AF_CUSTOM_URL_NOT_VERIFIED, newDomain));
        }
        DomainMappingUtils.updateMetadata(appKey, newDomain, version, isCustomDomain);

        // Since we are going to keep the new domain details, remove previous domain mapping
        if (StringUtils.isNotBlank(previousDomain) && !previousDomain.equalsIgnoreCase(newDomain)) {
            try {
                removeSubscriptionDomain(version, stage, previousDomain, appKey);
            } catch (AppFactoryException e) {
                // if mapping to newDomain is successful it should continue,
                // therefore not throwing an exception here
                String msg = "Successfully added domain mapping for domain : " + newDomain + " version : " + version +
                        " Error occurred removing domain mapping for " + previousDomain + " when remapping" +
                        " for application id: " + appKey;
                log.error(msg, e);
            }
        }
    }

    /**
     * Remove domain mapping from application.
     *
     * @param stage          mapping stage
     * @param appKey         application key
     * @param version        version to be removed
     * @param isCustomDomain whether to remove custom domain or not
     * @throws AppFactoryException
     */
    public void removeDomainMappingFromApplication(String stage, String appKey, String version, boolean isCustomDomain)
            throws AppFactoryException {
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            if (isCustomDomain) {
                String customDomain = DomainMappingUtils.getCustomDomain(appKey);
                if (StringUtils.isNotBlank(customDomain)) {
                    removeSubscriptionDomain(version, stage, customDomain, appKey);
                    DomainMappingUtils.updateCustomUrlMetadata(appKey, DomainMappingUtils.UNDEFINED_URL_RXT_VALUE,
                            DomainMappingUtils.UNVERIFIED_VERIFICATION_CODE);
                    // NOTE: need not update the appversion rxt as removed since default prod url has not removed
                    log.info("Successfully removed custom domain : " + customDomain + " from application id : " +
                            appKey + " of tenant domain : " + tenantDomain);
                    notifyAppWall(appKey, AF_APPWALL_REMOVE_SUCCESS_MSG, String.format(AF_APPWALL_URL, customDomain),
                            Event.Category.INFO);
                }
            } else {
                String defaultDomain = DomainMappingUtils.getDefaultDomain(appKey);
                if (StringUtils.isNotBlank(defaultDomain)) {
                    removeSubscriptionDomain(version, stage, defaultDomain, appKey);
                    DomainMappingUtils.updateApplicationMetaDataMappedDomain(
                            appKey, DomainMappingUtils.UNDEFINED_URL_RXT_VALUE);
                    if (StringUtils.isNotBlank(version)) {   // update the appversion rxt as removed
                        DomainMappingUtils.updateVersionMetadataWithMappedDomain(
                                appKey, version, DomainMappingUtils.UNDEFINED_URL_RXT_VALUE);
                    }
                    log.info("Successfully removed default domain : " + defaultDomain + " from application id : " +
                            appKey + " of tenant domain : " + tenantDomain);
                    notifyAppWall(appKey, AF_APPWALL_REMOVE_SUCCESS_MSG, String.format(AF_APPWALL_URL, defaultDomain),
                            Event.Category.INFO);
                }
            }
        } catch (AppFactoryException e) {
            log.error("Error occurred while removing domain mapping for application id: " +
                    appKey + " in stage: " + stage + " for tenant domain : " + tenantDomain, e);
            notifyAppWall(appKey, AF_APPWALL_ERROR_MSG, "", Event.Category.ERROR);
            throw new AppFactoryException(DomainMappingUtils.AF_ERROR_REMOVE_DOMAIN_MSG, e);
        }
    }

    /**
     * Remove existing domain mapping entries from Stratos
     * @param version
     * @param stage  mapping stage
     * @param domain e.g: some.organization.org this doesn't require the protocol such as http/https
     * @param appKey application key    @throws AppFactoryException
     */
    public void removeSubscriptionDomain(String version, String stage, String domain, String appKey) throws AppFactoryException {
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String appType = ApplicationDAO.getInstance().getApplicationInfo(appKey).getType();
        String appName = ApplicationDAO.getInstance().getApplicationInfo(appKey).getName();
        int tenantId =  CarbonContext.getThreadLocalCarbonContext().getTenantId();
        ApplicationContext applicationContext = KubernetesProvisioningUtils.getApplicationContext(appKey,
                version, stage, appType, tenantId, tenantDomain);

        KubernetesRuntimeProvisioningService kubernetesRuntimeProvisioningService =
                new KubernetesRuntimeProvisioningService(applicationContext);
        boolean isDeleted = false;

        try {
            isDeleted = kubernetesRuntimeProvisioningService.deleteCustomDomain(domain);


        } catch (RuntimeProvisioningException e) {
            log.error("Error occurred removing domain mapping : " + domain + " from tenant domain : " + tenantDomain +
                    " in stage :" + stage, e);
            throw new AppFactoryException(String.format(DomainMappingUtils.AF_ERROR_GENARIC_MSG, domain));
        }

        if (isDeleted) {
            log.info("Successfully removed custom domain : " + domain + " from tenant domain : " + tenantDomain);
        } else {
            log.error("Error occurred while removing domain mapping : " + domain + " from tenant domain : " +
                    tenantDomain + " in stage :" + stage + "]");
            throw new AppFactoryException(String.format(DomainMappingUtils.AF_ERROR_GENARIC_MSG, domain));
        }
    }

    /**
     * Verify ownership of the custom url by using {@link org.wso2.carbon.appfactory.s4.integration.DomainMappingManagementService#verifyCustomUrlByCname(String, String)} method.
     * Will check whether {@code customUrl} has CNAME entry to {@code defaultProdUrl}.
     *
     * @param customUrl custom url to be verified
     * @param appKey    application key
     * @return whether verification success or not, false if an error occurred while performing the operation
     */
    public boolean verifyCustomUrlForApplication(String customUrl, String appKey) {
        if (StringUtils.isNotBlank(DomainMappingUtils.getSubDomain(customUrl))) {   // if custom url has default host
            return true;
        }
        boolean validation = false;
        try {
            String defaultProdUrl = DomainMappingUtils.getDefaultDomain(appKey);
            if (StringUtils.isNotBlank(defaultProdUrl)) {
                validation = verifyCustomUrlByCname(defaultProdUrl, customUrl);
                if (validation) {
                    log.info("Successfully verified domain: " + customUrl + " for application " + appKey);
                } else {
                    log.warn("Failed to verify domain: " + customUrl + " for application: " + appKey);
                }
            } else {
                log.error("Failed to verify custom domain: " + customUrl + " for application id: " + appKey +
                        " since default production url is empty");
                validation = false;
            }
        } catch (AppFactoryException e) {
            // We are not throwing an error here, since we will return false if verification fails
            log.error("Error occurred while checking domain validation for application: " + appKey + " domain:" +
                    customUrl, e);
        }
        return validation;
    }

    /**
     * Check whether there is a CNAME entry from {@code customUrl} to {@code pointedUrl}.
     *
     * @param pointedUrl url that is pointed by the CNAME entry of {@code customUrl}
     * @param customUrl  custom url.
     * @return success whether there is a CNAME entry from {@code customUrl} to {@code pointedUrl}
     * @throws AppFactoryException
     */
    public boolean verifyCustomUrlByCname(String pointedUrl, String customUrl) throws AppFactoryException {
        Hashtable<String, String> env = new Hashtable<String, String>();
        boolean success;
        // set environment configurations
        env.put(JNDI_KEY_NAMING_FACTORY_INITIAL, ServiceHolder.getAppFactoryConfiguration()
                .getFirstProperty("JNDI.DomainMapping.FactoryInitial"));
        env.put(JNDI_KEY_DNS_TIMEOUT, ServiceHolder.getAppFactoryConfiguration()
                .getFirstProperty("JNDI.DomainMapping.DnsTimeOut"));
        env.put(JDNI_KEY_DNS_RETRIES, ServiceHolder.getAppFactoryConfiguration()
                .getFirstProperty("JNDI.DomainMapping.DnsRetries"));
        try {
            Multimap<String, String> resolvedHosts = resolveDNS(customUrl, env);
            Collection<String> resolvedCnames = resolvedHosts.get(DNS_CNAME_RECORD);
            if (!resolvedCnames.isEmpty() && resolvedCnames.contains(pointedUrl)) {
                if (log.isDebugEnabled()) {
                    log.debug(pointedUrl + " can be reached from: " + customUrl + " via CNAME records");
                }
                success = true;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(pointedUrl + " cannot be reached from: " + customUrl + " via CNAME records");
                }
                success = false;
            }
        } catch (AppFactoryException e) {
            log.error("Error occurred while resolving dns for: " + customUrl, e);
            throw new AppFactoryException("Error occurred while resolving dns for: " + customUrl, e);
        } catch (DomainMappingVerificationException e) {
            // we are logging this as warn messages since this is caused, due to an user error. For example if the
            // user entered a rubbish custom url(Or a url which is, CNAME record is not propagated at the
            // time of adding the url), then url validation will fail but it is not an system error
            log.warn(pointedUrl + " cannot be reached from: " + customUrl + " via CNAME records. Provided custom" +
                    " url: " + customUrl + " might not a valid url.", e);
            success = false;
        }
        return success;
    }

    /**
     * Resolve CNAME and A records for the given {@code hostname}.
     *
     * @param domain             hostname to be resolved.
     * @param environmentConfigs environment configuration
     * @return {@link com.google.common.collect.Multimap} of resolved dns entries. This {@link com.google.common.collect.Multimap} will contain the resolved
     * "CNAME" and "A" records from the given {@code hostname}
     * @throws AppFactoryException if error occurred while the operation
     */
    public Multimap<String, String> resolveDNS(String domain, Hashtable<String, String> environmentConfigs)
            throws AppFactoryException, DomainMappingVerificationException {
        // result mutimap of dns records. Contains the cname and records resolved by the given hostname
        // ex:  CNAME   => foo.com,bar.com
        //      A       => 192.1.2.3 , 192.3.4.5
        Multimap<String, String> dnsRecordsResult = ArrayListMultimap.create();
        Attributes dnsRecords;
        boolean isARecordFound = false;
        boolean isCNAMEFound = false;

        try {
            if (log.isDebugEnabled()) {
                log.debug("DNS validation: resolving DNS for " + domain + " " + "(A/CNAME)");
            }
            DirContext context = new InitialDirContext(environmentConfigs);
            String[] dnsRecordsToCheck = new String[]{DNS_A_RECORD, DNS_CNAME_RECORD};
            dnsRecords = context.getAttributes(domain, dnsRecordsToCheck);
        } catch (NamingException e) {
            String msg = "DNS validation: DNS query failed for: " + domain + ". Error occurred while configuring " +
                    "directory context.";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }

        try {
            // looking for for A records
            Attribute aRecords = dnsRecords.get(DNS_A_RECORD);
            if (aRecords != null && aRecords.size() > 0) {                      // if an A record exists
                NamingEnumeration aRecordHosts = aRecords.getAll();             // get all resolved A entries
                String aHost;
                while (aRecordHosts.hasMore()) {
                    isARecordFound = true;
                    aHost = (String) aRecordHosts.next();
                    dnsRecordsResult.put(DNS_A_RECORD, aHost);
                    if (log.isDebugEnabled()) {
                        log.debug("DNS validation: A record found: " + aHost);
                    }
                }
            }

            // looking for CNAME records
            Attribute cnameRecords = dnsRecords.get(DNS_CNAME_RECORD);
            if (cnameRecords != null && cnameRecords.size() > 0) {              // if CNAME record exists
                NamingEnumeration cnameRecordHosts = cnameRecords.getAll();     // get all resolved CNAME entries for hostname
                String cnameHost;
                while (cnameRecordHosts.hasMore()) {
                    isCNAMEFound = true;
                    cnameHost = (String) cnameRecordHosts.next();
                    if (cnameHost.endsWith(".")) {
                        // Since DNS records are end with "." we are removing it.
                        // For example real dns entry for www.google.com is www.google.com.
                        cnameHost = cnameHost.substring(0, cnameHost.lastIndexOf('.'));
                    }
                    dnsRecordsResult.put(DNS_CNAME_RECORD, cnameHost);
                    if (log.isDebugEnabled()) {
                        log.debug("DNS validation: recurring on CNAME record towards host " + cnameHost);
                    }
                    dnsRecordsResult.putAll(resolveDNS(cnameHost, environmentConfigs)); // recursively resolve cnameHost
                }
            }

            if (!isARecordFound && !isCNAMEFound && log.isDebugEnabled()) {
                log.debug("DNS validation: No CNAME or A record found for domain: '" + domain);
            }
            return dnsRecordsResult;
        } catch (NamingException ne) {
            String msg = "DNS validation: DNS query failed for: " + domain + ". Provided domain: " + domain +
                    " might be a " +
                    "non existing domain.";
            // we are logging this as warn messages since this is caused, due to an user error. For example if the
            // user entered a rubbish custom url(Or a url which is, CNAME record is not propagated at the
            // time of adding the url), then url validation will fail but it is not an system error
            log.warn(msg, ne);
            throw new DomainMappingVerificationException(msg, ne);
        }
    }

    /**
     * Check whether domain is available
     *
     *
     * @param appKey
     * @param version
     *@param stage   mapping stage
     * @param domain  e.g: some.organization1.org this doesn't require the protocol such as http/https
     * @param appType application type    @return true if domain is available
     */
    private boolean isDomainAvailable(String appKey, String version, String stage, String domain, String appType)
            throws AppFactoryException {

        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String appName = ApplicationDAO.getInstance().getApplicationInfo(appKey).getName();
        int tenantId =  CarbonContext.getThreadLocalCarbonContext().getTenantId();
        ApplicationContext applicationContext = KubernetesProvisioningUtils.getApplicationContext(appKey,
                version, stage, appType, tenantId, tenantDomain);

        KubernetesRuntimeProvisioningService kubernetesRuntimeProvisioningService =
                new KubernetesRuntimeProvisioningService(applicationContext);
        boolean isAvailable = true;

        try {

            Set<String> domains = kubernetesRuntimeProvisioningService.getCustomDomains();
            if(domains.contains(domain)){
                isAvailable = false;
            }

        } catch (RuntimeProvisioningException e) {
            log.error("Error occurred while checking domain availability from Stratos side for domain:" + domain, e);
            throw new AppFactoryException(String.format(DomainMappingUtils.AF_DOMAIN_AVAILABILITY_ERROR_MSG, domain));
        }

        return isAvailable;
    }

    /**
     * Notify the app wall status of the domain update
     *
     * @param appKey      application key
     * @param message     message to send
     * @param description message description
     * @param msgType     message type
     */
    private void notifyAppWall(String appKey, String message, String description,
            Event.Category msgType) {
        try {
            EventNotifier.getInstance().notify(
                    AppInfoUpdateEventBuilderUtil
                            .createDomainMappingCompletedEvent(appKey, message, description, msgType));
        } catch (AppFactoryEventException e) {
            // need not to throw an exception since failure of appwall notification should not interrupt the current operation
            log.error("Failed notifying custom url update event", e);
        }
    }






}
