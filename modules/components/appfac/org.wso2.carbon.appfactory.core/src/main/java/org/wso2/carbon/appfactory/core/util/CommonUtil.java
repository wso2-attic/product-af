/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package org.wso2.carbon.appfactory.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.cache.AppTypeCache;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifactImpl;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;

public class CommonUtil {
    private static final Log log = LogFactory.getLog(CommonUtil.class);
    public static String getAdminUsername() {
        return ServiceHolder.getAppFactoryConfiguration()
                .getFirstProperty(AppFactoryConstants.SERVER_ADMIN_NAME);
    }

    public static String getAdminUsername(String applicationId) {
        return ServiceHolder.getAppFactoryConfiguration()
                .getFirstProperty(AppFactoryConstants.SERVER_ADMIN_NAME) +
                "@" + applicationId;
    }

    public static String getServerAdminPassword() {
        return ServiceHolder.getAppFactoryConfiguration()
                .getFirstProperty(AppFactoryConstants.SERVER_ADMIN_PASSWORD);
    }

    /**
     * Returns the type of the application for a given the application Id and tenant name
     *
     * @param applicationId Id of the application
     * @param tenantDomain  Tenant domain of the application
     * @return the application type
     * @throws org.wso2.carbon.appfactory.common.AppFactoryException
     *          If invalid application or application type is not available
     */
    public static String getApplicationType(String applicationId, String tenantDomain) throws AppFactoryException {
        AppTypeCache appTypeCache = AppTypeCache.getAppTypeCache();
        String applicationType = appTypeCache.getAppType(tenantDomain, applicationId);

        if (applicationType != null) {
            return applicationType;
        } else {
            GenericArtifactImpl artifact = getApplicationArtifact(applicationId, tenantDomain);

            if (artifact == null) {
                String errorMsg =
                        String.format("Unable to find application information for id : %s",
                                applicationId);
                log.error(errorMsg);
                throw new AppFactoryException(errorMsg);
            }

            try {
                applicationType = artifact.getAttribute("application_type");
                appTypeCache.addToCache(tenantDomain, applicationId, applicationType);
                return applicationType;
            } catch (RegistryException e) {
                String errorMsg =
                        String.format("Unable to find the application type for application " +
                                "id: %s",
                                applicationId);
                log.error(errorMsg, e);
                throw new AppFactoryException(errorMsg, e);
            }
        }
    }

    /**
     * A Util method to load an Application artifact from the registry.
     *
     * @param applicationId the application Id
     * @param tenantDomain  the tenant name of the application
     * @return a {@link org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifactImpl} representing the application or
     *         null if application (by the id is not in registry)
     * @throws org.wso2.carbon.appfactory.common.AppFactoryException if an error occurs.
     */
    public static GenericArtifactImpl getApplicationArtifact(String applicationId, String tenantDomain)
            throws AppFactoryException {
        GenericArtifact artifact = null;
        try {
	        /** TODO: Started the tenant flow here to fix APPFAC-1883.
	         * But we need to stop passing tenant domain here and needs to
	         * start tenant flow from a higher level
	         * */
	        PrivilegedCarbonContext.startTenantFlow();
	        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain,true);
            if(log.isDebugEnabled()){
                log.debug("Tenant Domain : " + tenantDomain);
            }

            RegistryService registryService =
                    ServiceHolder
                            .getRegistryService();
            UserRegistry userRegistry = registryService.getGovernanceSystemRegistry(
                    ServiceHolder.getRealmService().getTenantManager().getTenantId(tenantDomain));
            String path = AppFactoryConstants.REGISTRY_APPLICATION_PATH + "/" + applicationId + "/" + "appinfo";

            if (log.isDebugEnabled()) {
                log.debug("Username for registry :" + userRegistry.getUserName() + " Tenant ID : " + userRegistry.getTenantId());
                log.debug("Username from carbon context :" + CarbonContext.getThreadLocalCarbonContext().getUsername());
            }
            if(!userRegistry.resourceExists(path)){
                return null;
            }

            Resource resource = userRegistry.get(path);
            artifact = getApplicationRXTManager(tenantDomain).getGenericArtifact(resource.getUUID());

        } catch (RegistryException e) {
            String errorMsg =
                    String.format("Unable to load the application information for applicaiton id: %s",
                            applicationId);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        } catch (UserStoreException e) {
            String errorMsg =
                    String.format("User Registration Error for applicaiton id: %s",
                            applicationId);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        } finally {
	        PrivilegedCarbonContext.endTenantFlow();
        }

        return (GenericArtifactImpl) artifact;
    }

    /**
     * Return App Info RXT Manager for a tenant
     *
     * @param tenantDomain
     * @return
     * @throws org.wso2.carbon.appfactory.common.AppFactoryException
     */
    public static GenericArtifactManager getApplicationRXTManager(String tenantDomain) throws AppFactoryException {

        RegistryService registryService =
                ServiceHolder
                        .getRegistryService();
        UserRegistry userRegistry = null;
        GenericArtifactManager artifactManager;
        try {
            userRegistry = registryService.getGovernanceSystemRegistry(ServiceHolder.
                    getRealmService().getTenantManager().getTenantId(tenantDomain));
            GovernanceUtils.loadGovernanceArtifacts(userRegistry);
            artifactManager =
                    new GenericArtifactManager(userRegistry,
                            "application");
        } catch (RegistryException e) {
            String errorMsg =
                    String.format("Error while getting application RXT Manager in tenant: %s", tenantDomain);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        } catch (UserStoreException e) {
            String errorMsg =
                    String.format("Error while getting tenant id for %s",
                            tenantDomain);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }

        return artifactManager;
    }


}
