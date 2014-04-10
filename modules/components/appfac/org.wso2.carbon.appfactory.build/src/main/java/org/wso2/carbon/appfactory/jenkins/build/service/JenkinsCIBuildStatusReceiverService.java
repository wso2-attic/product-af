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

package org.wso2.carbon.appfactory.jenkins.build.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.build.DefaultBuildDriverListener;
import org.wso2.carbon.appfactory.jenkins.build.internal.ServiceContainer;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.activation.DataHandler;

/**
 * This service class is used to receive the build status
 */
public class JenkinsCIBuildStatusReceiverService {

    private static Log log = LogFactory.getLog(JenkinsCIBuildStatusReceiverService.class);

    /**
     * This is to be called after a completion of a build
     *
     * @param buildStatus bean which contains the information of the completed build
     */
    @SuppressWarnings("UnusedDeclaration")

//    This service runs on super tenant mode. Hence we are passing the tenant domain as a parameter too.
    public void onBuildCompletion(BuildStatusBean buildStatus, DataHandler data, String fileName, String tenantDomain){
        /// TODO remove DataHandler and fileName params from here and change stub and jenkins side

//        Getting the tenant ID to set in tenant flow.
        int tenantId = MultitenantConstants.INVALID_TENANT_ID;
        try {
            tenantId = ServiceContainer.getRealmService().getTenantManager().getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            String msg = "Unable to get the tenant Id for domain : " + tenantDomain;
            log.error(msg, e);
        }

        try {
//           Starting the tenant flow.
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);

//            We need to load the tenant registry as well. Otherwise there will be an error from the user registry
//            TenantRegistryLoader tenantRegistryLoader = ServiceContainer.getTenantRegistryLoader();
//            tenantRegistryLoader.loadTenantRegistry(tenantId);

            log.info("Build completed for " + buildStatus.getApplicationId() +
                    " with buildId " + buildStatus.getBuildId());

            DefaultBuildDriverListener listener = new DefaultBuildDriverListener();

            if (buildStatus.isBuildSuccessful()) {
                listener.onBuildSuccessful(buildStatus.getApplicationId(), buildStatus.getVersion(),
                        null, buildStatus.getBuildId(), data, fileName, tenantDomain);
            } else {
                listener.onBuildFailure(buildStatus.getApplicationId(), buildStatus.getVersion(),
                        null, buildStatus.getBuildId(), buildStatus.getLogMsg(), tenantDomain);
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }
}
