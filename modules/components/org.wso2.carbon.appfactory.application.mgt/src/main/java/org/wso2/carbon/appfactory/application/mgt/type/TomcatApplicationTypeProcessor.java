/*
 * Copyright 2014 WSO2, Inc. (http://wso2.com)
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
 *      See the License for the specifnStic language governing permissions and
 *      limitations under the License.
 */

package org.wso2.carbon.appfactory.application.mgt.type;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.application.mgt.util.Util;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.s4.integration.StratosRestClient;
import org.wso2.carbon.appfactory.s4.integration.utils.CloudUtils;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.api.UserStoreException;

public class TomcatApplicationTypeProcessor extends MavenBasedApplicationTypeProcessor {

    private static final Log log = LogFactory.getLog(TomcatApplicationTypeProcessor.class);

    /**
     * Construct the URL pattern : http://{hostname}/{applicationID}-{version}
     * Hostname is obtained from  the stratos application runtime
     *
     * @param tenantDomain
     * @param applicationID
     * @param applicationVersion
     * @param stage
     * @return launchURLPattern
     * @throws AppFactoryException
     */
    public String getDeployedURL(String tenantDomain, String applicationID, String applicationVersion, String stage)
            throws AppFactoryException {
        AppFactoryConfiguration appfactoryConfiguration = AppFactoryUtil.getAppfactoryConfiguration();
        String stratosServerURL = appfactoryConfiguration.getFirstProperty(AppFactoryConstants.DEPLOYMENT_STAGES
                                                                           + AppFactoryConstants.DOT_SEPERATOR
                                                                           + stage + AppFactoryConstants.DOT_SEPERATOR
                                                                           + AppFactoryConstants.TENANT_MGT_URL);
        int tenantId;
        try {
            tenantId = Util.getRealmService().getTenantManager().getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            String errorMsg = "Unable to get tenant ID for tenant domain " + tenantDomain
                              + " while getting deployed URL";
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }

        String tenantUsername = CarbonContext.getThreadLocalCarbonContext().getUsername();
        StratosRestClient stratosRestClient = StratosRestClient.getInstance(stratosServerURL, tenantUsername);

        String applicationInstanceJson = stratosRestClient.getApplicationRuntime(
                CloudUtils.generateUniqueStratosApplicationId(tenantId, applicationID, applicationVersion, stage));

        JsonParser jsonParser = new JsonParser();
        JsonObject applicationInstanceObject = (JsonObject) jsonParser.parse(applicationInstanceJson);

        String serviceHostname;
        String launchURLPattern = properties.getProperty(LAUNCH_URL_PATTERN);
        if (applicationInstanceJson != null && AppFactoryConstants.STRATOS_RUNTIME_STATUS_ACTIVE.
                equalsIgnoreCase(applicationInstanceObject.get("status").getAsString())) {
            // getting the application service hostname from the application runtime JSON
            serviceHostname = applicationInstanceObject.get("applicationInstances").getAsJsonArray().get(0)
                    .getAsJsonObject().get("clusterInstances").getAsJsonArray().get(0)
                    .getAsJsonObject().get("hostNames").getAsJsonArray().get(0).getAsString();

            String artifactTrunkVersionName = AppFactoryUtil.getAppfactoryConfiguration().
                    getFirstProperty(AppFactoryConstants.TRUNK_WEBAPP_ARTIFACT_VERSION_NAME);
            String sourceTrunkVersionName = AppFactoryUtil.getAppfactoryConfiguration().
                    getFirstProperty(AppFactoryConstants.TRUNK_WEBAPP_SOURCE_VERSION_NAME);

            if (applicationVersion.equalsIgnoreCase(sourceTrunkVersionName)) {
                applicationVersion = artifactTrunkVersionName;
            }
            launchURLPattern = launchURLPattern.replace(PARAM_APP_ID, applicationID).replace(PARAM_HOSTNAME, serviceHostname)
                    .replace(PARAM_APP_VERSION, applicationVersion);

            if (log.isDebugEnabled()) {
                log.debug("Generated URL pattern for applicationID :" + applicationID + " version :" + applicationVersion
                          + " stage : " + stage + " pattern : " + launchURLPattern);
            }
        }
        return launchURLPattern;
    }
}
