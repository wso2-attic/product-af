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

package org.wso2.carbon.appfactory.s4.integration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.ApplicationEventsHandler;
import org.wso2.carbon.appfactory.core.dto.Application;
import org.wso2.carbon.appfactory.core.dto.UserInfo;
import org.wso2.carbon.appfactory.core.dto.Version;
import org.wso2.carbon.appfactory.core.util.AppFactoryCoreUtil;
import org.wso2.carbon.appfactory.s4.integration.internal.ServiceReferenceHolder;
import org.wso2.carbon.appfactory.s4.integration.utils.DomainMappingUtils;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.core.UserCoreConstants;

import java.util.Random;

/**
 * Domain mapping listener for application events.
 */
public class DomainMappingListener extends ApplicationEventsHandler {
    private static final Log log = LogFactory.getLog(DomainMappingListener.class);


    public DomainMappingListener(String identifier, int priority) {
        super(identifier, priority);
    }

    /**
     * Create default production url.
     * {@inheritDoc}
     */
    @Override
    public void onCreation(final Application application, final String userName, final String tenantDomain,
                           final boolean isUploadableAppType)
            throws AppFactoryException {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                String defaultHostName = ServiceReferenceHolder.getInstance().getAppFactoryConfiguration().getFirstProperty(
                        "DomainName");
                String defaultUrl = DomainMappingUtils.generateDefaultProdUrl(application.getId(),
                                                                              tenantDomain.replace(".", ""),
                                                                              defaultHostName);
                try {

                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(
                            userName.split(UserCoreConstants.TENANT_DOMAIN_COMBINER)[0]);

                    if (AppFactoryCoreUtil.isDomainMappingAllowedAppType(application.getType())) {
                        setDefaultProdUrl(application.getId(), defaultUrl, isUploadableAppType);
                        log.info("Successfully added default production url:" + defaultUrl + " for application " +
                                 application.getId() + " for tenant:" + tenantDomain);
                    }
                } catch (AppFactoryException e) {
                    // no need to throw an error here since this operation should not interrupt the app creation procedure
                    log.error("Error while adding default url for application:" + application.getId() + ", tenant:" +
                              tenantDomain + ", default url: " + defaultUrl);

                    // retying again assuming defaultProdUrl is already taken.
                    try {
                        defaultUrl = DomainMappingUtils.
                                generateDefaultProdUrl(application.getId() + "_" + (new Random()).nextInt(1000),
                                                       tenantDomain, defaultHostName);
                        setDefaultProdUrl(application.getId(), defaultUrl, isUploadableAppType);
                        log.info("Successfully added default production url:" + defaultUrl + " for application " +
                                 application.getId() + " for tenant:" + tenantDomain);
                    } catch (AppFactoryException e1) {
                        // no need to throw an error here since this operation should not interrupt the app creation procedure
                        log.error("Error while retrying to add default url for application:" + application.getId() +
                                  ", " +
                                  "tenant:" + tenantDomain + ", default url: " + defaultUrl);
                    }
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }
        };
        new Thread(runnable).start();
    }

    /**
     * Delete default production url and custom url.
     * {@inheritDoc}
     */
    @Override
    public void onDeletion(Application application, String userName, String tenantDomain) throws AppFactoryException {
        try {
            if (AppFactoryCoreUtil.isDomainMappingAllowedAppType(application.getType())) {

                String stage = ServiceReferenceHolder.getInstance().getAppFactoryConfiguration().
                        getFirstProperty("FineGrainedDomainMappingAllowedStage");
                // remove custom url
                ServiceReferenceHolder.getInstance().getDomainMappingManagementService().
                        removeDomainMappingFromApplication(stage, application.getId(), null, true);

                // remove default url
                ServiceReferenceHolder.getInstance().getDomainMappingManagementService().
                        removeDomainMappingFromApplication(stage, application.getId(), null, false);
                log.info("Successfully removed domain mapping from application : " + application.getId() +
                         " in " + stage + " environment for tenant domain : " + tenantDomain);
            }
        } catch (AppFactoryException e) {
            // no need to throw an error here since this operation should not interrupt the app deletion procedure
            log.error("Error while removing default and custom urls for application:" + application.getId() +
                      " for tenant domain :" + tenantDomain, e);
        }
    }

    @Override
    public void onUserAddition(Application application, UserInfo user, String tenantDomain) throws AppFactoryException {

    }

    @Override
    public void onUserDeletion(Application application, UserInfo user, String tenantDomain) throws AppFactoryException {

    }

    @Override
    public void onUserUpdate(Application application, UserInfo user, String tenantDomain) throws AppFactoryException {

    }

    @Override
    public void onRevoke(Application application, String tenantDomain) throws AppFactoryException {

    }

    @Override
    public void onVersionCreation(Application application, Version source, Version target, String tenantDomain,
                                  String userName) throws AppFactoryException {

    }

    @Override
    public void onFork(Application application, String userName, String tenantDomain, String version,
                       String[] forkedUsers) throws AppFactoryException {

    }

    @Override
    public void onLifeCycleStageChange(Application application, Version version, String previosStage, String nextStage,
                                       String tenantDomain) throws AppFactoryException {

    }

    @Override
    public boolean hasExecuted(Application application, String userName, String tenantDomain)
            throws AppFactoryException {
        return true;
    }

    /**
     * Set default production url
     *
     * @param applicationKey      application key
     * @param defaultUrl          default production url
     * @param isUploadableAppType boolean variable for determine whether the app is uploaded or created
     * @throws AppFactoryException
     */
    private void setDefaultProdUrl(String applicationKey, String defaultUrl, boolean isUploadableAppType)
            throws AppFactoryException {
        String version = null;
        if (isUploadableAppType) {
            version = AppFactoryConstants.INITIAL_UPLOADED_APP_VERSION;
        }
        ServiceReferenceHolder.getInstance().getDomainMappingManagementService().addNewSubscriptionDomain(
                ServiceReferenceHolder.getInstance().getAppFactoryConfiguration().getFirstProperty(
                        "FineGrainedDomainMappingAllowedStage"),
                defaultUrl, applicationKey, version, false);
    }
}
