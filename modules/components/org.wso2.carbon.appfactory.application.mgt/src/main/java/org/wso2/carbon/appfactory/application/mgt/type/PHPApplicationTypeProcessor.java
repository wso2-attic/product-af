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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.dao.JDBCApplicationDAO;
import org.wso2.carbon.appfactory.core.dto.CartridgeCluster;
import org.wso2.carbon.appfactory.core.util.AppFactoryCoreUtil;
import org.wso2.carbon.appfactory.s4.integration.StratosRestClient;
import org.wso2.carbon.context.CarbonContext;

import java.io.File;

/**
 * PHP application type processor
 */
public class PHPApplicationTypeProcessor extends AbstractFreeStyleApplicationTypeProcessor {
    private static final Log log = LogFactory.getLog(PHPApplicationTypeProcessor.class);
    private static final String PARAM_CARTRIDGE_IP = "{cartridgeIP}";
    private static final String ENVIRONMENT = "ApplicationDeployment.DeploymentStage";
    private static final String TENANT_MANAGEMENT_URL = "TenantMgtUrl";
    private static final String SYMBOL_AT = "@";



    @Override
    public void generateApplicationSkeleton(String applicationId, String workingDirectory) throws AppFactoryException {
        super.generateApplicationSkeleton(applicationId, workingDirectory);
        File pomFile = new File(workingDirectory + File.separator + AppFactoryConstants.DEFAULT_POM_FILE);
        boolean result = FileUtils.deleteQuietly(pomFile);
        if (!result){
            log.warn("Error while deleting pom.xml for application id : " + applicationId);
        }
    }


    @Override
    public String getDeployedURL(String tenantDomain, String applicationId, String applicationVersion, String stage)
            throws AppFactoryException {

        String deployedUrl = StringUtils.EMPTY;
        try {
            String urlPattern = (String) this.properties.get(LAUNCH_URL_PATTERN);
            String artifactTrunkVersionName = AppFactoryUtil.getAppfactoryConfiguration().
                    getFirstProperty(ARTIFACT_VERSION_XPATH);
            String sourceTrunkVersionName = AppFactoryUtil.getAppfactoryConfiguration().
                    getFirstProperty(SOURCE_VERSION_XPATH);

            if (sourceTrunkVersionName.equalsIgnoreCase(applicationVersion)) {
                applicationVersion = artifactTrunkVersionName;
            }

            // get cartridge active IP
            String cartridgeActiveIp = getCartridgeActiveIP(applicationId, tenantDomain, stage);

            // generate application url
            if (StringUtils.isNotBlank(urlPattern) && StringUtils.isNotBlank(cartridgeActiveIp)) {
                urlPattern = urlPattern.replace(PARAM_CARTRIDGE_IP, cartridgeActiveIp)
                        .replace(PARAM_APP_ID, applicationId)
                        .replace(PARAM_APP_VERSION, applicationVersion);
                deployedUrl = urlPattern;
            }
        } catch (AppFactoryException e) {
            if (log.isErrorEnabled()) {
                log.error("Error while generating application url !", e);
            }
        }
        return deployedUrl;

    }

    private String getCartridgeActiveIP(String applicationId, String tenantDomain, String stage)
            throws AppFactoryException {
        AppFactoryConfiguration configuration = AppFactoryUtil.getAppfactoryConfiguration();
        String serverURL = configuration.getFirstProperty(ENVIRONMENT + XPATH_SEPERATOR + stage + XPATH_SEPERATOR +
                TENANT_MANAGEMENT_URL);
        String userName = CarbonContext.getThreadLocalCarbonContext().getUsername() + SYMBOL_AT +
                CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String cartridgeAlias = AppFactoryCoreUtil.getCartridgeAlias(applicationId, tenantDomain);

        StratosRestClient restService = StratosRestClient.getInstance(serverURL, userName);
        //TODO : implement this using new api
        String clusterId = null;

        // get data from runtime db
        JDBCApplicationDAO jdbcApplicationDAO = JDBCApplicationDAO.getInstance();
        CartridgeCluster cartridgeCluster = jdbcApplicationDAO.getCartridgeClusterByClusterId(clusterId);
        if(cartridgeCluster != null) {
            return cartridgeCluster.getActiveIP();
        }
        return null;
    }

}
