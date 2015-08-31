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

package org.wso2.carbon.appfactory.application.mgt.type;

import org.apache.axiom.om.OMElement;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.dao.JDBCApplicationDAO;
import org.wso2.carbon.appfactory.core.dto.CartridgeCluster;
import org.wso2.carbon.appfactory.core.dto.DeployStatus;
import org.wso2.carbon.appfactory.s4.integration.StratosRestClient;
import org.wso2.carbon.appfactory.utilities.version.AppVersionStrategyExecutor;
import org.wso2.carbon.context.CarbonContext;

import java.io.File;
import java.util.Date;
import java.util.Map;

/**
 * Nodejs application type processor
 */
public class NodejsApplicationTypeProcessor extends AbstractApplicationTypeProcessor {

    private static final Log log = LogFactory.getLog(NodejsApplicationTypeProcessor.class);
    private static final String PARAM_CARTRIDGE_IP = "{cartridgeIP}";
    private static final String ENVIRONMENT = "ApplicationDeployment.DeploymentStage";
    private static final String TENANT_MANAGEMENT_URL = "TenantMgtUrl";
    private static final String SYMBOL_AT = "@";

    @Override
    public void doVersion(String applicationId, String targetVersion,
                          String currentVersion, String workingDirectory)
            throws AppFactoryException {
        AppVersionStrategyExecutor.doVersionForMVN(targetVersion, new File(workingDirectory));
    }

    @Override
    public OMElement configureBuildJob(OMElement jobConfigTemplate,
                                       Map<String, String> parameters, String projectType)
            throws AppFactoryException {

        if (jobConfigTemplate == null) {
            String msg =
                    "Class loader is unable to find the jenkins job configuration template for Maven application types";
            log.error(msg);
            throw new AppFactoryException(msg);

        }
        // configure repo data
        jobConfigTemplate = configureRepositoryData(jobConfigTemplate, parameters);

        // set the maven 3 config name
        setValueUsingXpath(jobConfigTemplate, AppFactoryConstants.MAVEN3_CONFIG_NAME_XAPTH_SELECTOR,
                parameters.get(AppFactoryConstants.MAVEN3_CONFIG_NAME));

        // Support for post build listener residing in jenkins server
        setValueUsingXpath(jobConfigTemplate,
                AppFactoryConstants.PUBLISHERS_APPFACTORY_POST_BUILD_APP_EXTENSION_XPATH_SELECTOR,
                parameters.get(AppFactoryConstants.APPTYPE_EXTENSION));

        setValueUsingXpath(jobConfigTemplate,
                AppFactoryConstants.PUBLISHERS_APPFACTORY_POST_BUILD_APP_ID_XPATH_SELECTOR,
                parameters.get(AppFactoryConstants.APPLICATION_ID));

        setValueUsingXpath(jobConfigTemplate,
                AppFactoryConstants.PUBLISHERS_APPFACTORY_POST_BUILD_APP_VERSION_XPATH_SELECTOR,
                parameters.get(AppFactoryConstants.APPLICATION_VERSION));

        setValueUsingXpath(jobConfigTemplate,
                AppFactoryConstants.PUBLISHERS_APPFACTORY_POST_BUILD_USERNAME_XPATH_SELECTOR,
                parameters.get(AppFactoryConstants.APPLICATION_USER));

        setValueUsingXpath(jobConfigTemplate,
                AppFactoryConstants.PUBLISHERS_APPFACTORY_POST_BUILD_REPOFROM_XPATH_SELECTOR,
                parameters.get(AppFactoryConstants.REPOSITORY_FROM));

        setValueUsingXpath(jobConfigTemplate, AppFactoryConstants.APPLICATION_TRIGGER_PERIOD,
                parameters.get(AppFactoryConstants.APPLICATION_POLLING_PERIOD));

        return jobConfigTemplate;
    }

    @Override
    public String getDeployedURL(String tenantDomain, String applicationId,
                                 String applicationVersion, String stage)
            throws AppFactoryException {

        String deployedUrl = StringUtils.EMPTY;
        try {
            String urlPattern = (String) this.properties.get(LAUNCH_URL_PATTERN);

            // get cartridge active IP
            String cartridgeActiveIp =
                    getCartridgeActiveIP(applicationId, applicationVersion, tenantDomain, stage,
                            true);

            // generate application url
            if (StringUtils.isNotBlank(urlPattern) && StringUtils.isNotBlank(cartridgeActiveIp)) {
                urlPattern = urlPattern.replace(PARAM_CARTRIDGE_IP, cartridgeActiveIp);

                deployedUrl = urlPattern;
            }
        } catch (AppFactoryException e) {
            log.error("Error while generating application url !", e);
            throw new AppFactoryException("Error while generating application url !");
        }
        return deployedUrl;

    }

    private String getCartridgeActiveIP(String applicationId, String applicationVersion,
                                        String tenantDomain, String stage,
                                        boolean subscribeOnDeployment) throws AppFactoryException {


        AppFactoryConfiguration configuration = AppFactoryUtil.getAppfactoryConfiguration();
        String serverURL = configuration
                .getFirstProperty(ENVIRONMENT + XPATH_SEPERATOR + stage + XPATH_SEPERATOR +
                        TENANT_MANAGEMENT_URL);
        String userName = CarbonContext.getThreadLocalCarbonContext().getUsername() + SYMBOL_AT +
                CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String cartridgeAlias = getCartridgeAlias(applicationId, applicationVersion, tenantDomain,
                subscribeOnDeployment);

        StratosRestClient restService = StratosRestClient.getInstance(serverURL, userName);
        //TODO : implement this using new api
        String clusterId = null;
        JDBCApplicationDAO jdbcApplicationDAO = JDBCApplicationDAO.getInstance();

        try {
            int retryCount = 5; // Set this to appfactory xml
            int retryDelay = 3; // Set this to appfactory xml
            // retry retryCount times to process the request
            for (int i = 0; i < retryCount; i++) {
                log.info("Trying for " + i + " time");
                CartridgeCluster cartridgeCluster = jdbcApplicationDAO.getCartridgeClusterByClusterId(clusterId);

                if (cartridgeCluster != null) {

                    DeployStatus currentStatus = jdbcApplicationDAO.getDeployStatus(applicationId, applicationVersion,
                            stage, false, userName);
                    if (currentStatus.getLastDeployedStatus() == null) {
                        currentStatus.setLastDeployedStatus(AppFactoryConstants.DEPLOY_SUCCESS);
                        currentStatus.setLastDeployedTime(new Date().getTime());
                        jdbcApplicationDAO
                                .updateLastDeployStatus(applicationId, applicationVersion, stage, false,
                                        null, currentStatus);
                    }

                    return cartridgeCluster.getActiveIP();
                }

                Thread.sleep(1000 * retryDelay); // sleep retryDelay seconds

            }

        } catch (InterruptedException e) {
            String errorMsg = "Error while waiting for Cartridge cluster";
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }
        return null;
    }
}
