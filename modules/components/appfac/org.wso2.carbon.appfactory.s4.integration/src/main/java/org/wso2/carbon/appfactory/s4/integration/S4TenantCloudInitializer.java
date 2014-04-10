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
package org.wso2.carbon.appfactory.s4.integration;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.tenant.mgt.stub.TenantMgtAdminServiceExceptionException;
import org.apache.stratos.tenant.mgt.stub.TenantMgtAdminServiceStub;
import org.apache.stratos.tenant.mgt.stub.beans.xsd.TenantInfoBean;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.TenantCloudInitializer;
import org.wso2.carbon.appfactory.core.task.AppFactoryTenantCloudInitializerTask;
import org.wso2.carbon.appfactory.s4.integration.internal.ServiceReferenceHolder;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Map;

/**
 * This creates tenant in particular environment in S2 based cloud
 */
public class S4TenantCloudInitializer implements TenantCloudInitializer {
    private static final Log log = LogFactory.getLog(S4TenantCloudInitializer.class);
    private StratosRestService restService;
    private TenantMgtAdminServiceStub stub;

    /**
     * This method will be called for creating tenant in a stage
     *
     * @param properties key value pairs supplied by task
     */
    @Override
    public void onTenantCreation(Map<String, String> properties) {

        try {
            init(properties);

            addTenant(properties);

            String stage = properties
                    .get(AppFactoryTenantCloudInitializerTask.STAGE);

            DeployerInfoBuilder deployerInfoBuilder = new DeployerInfoBuilder();
            deployerInfoBuilder.build();
            Map<String, DeployerInfo> deployerInfoMap = deployerInfoBuilder.getDeployerInfo(stage);

            for (DeployerInfo deployerInfo : deployerInfoMap.values()) {
                String repoURL;

                repoURL = createGitRepository(deployerInfo, properties);
                deployerInfo.setRepoURL(repoURL);

                subscribe(deployerInfo, properties, stage);
            }
            log.info("successfully created tenant in " + stage);
        } catch (AppFactoryException e) {
            String msg = "Can not continue tenant creation due to " + e.getLocalizedMessage();
            log.error(msg, e);
        }


    }

    private String generateRepoUrlFromTemplate(String pattern, int tenantId, String stage) {
        String s = pattern.replace("{@stage}", stage) + File.separator + Integer.toString(tenantId);
        log.info("**************************************generated repo URL: " + s + "******************************");
        return s;

    }

    private void init(Map<String, String> properties) throws AppFactoryException {
       /* String serverURL = properties.get(AppFactoryTenantCloudInitializerTask.SERVER_URL);
        String superAdmin = properties.get(AppFactoryTenantCloudInitializerTask.SUPER_TENANT_ADMIN);
        String superAdminPassword = properties.get(AppFactoryTenantCloudInitializerTask
                .SUPER_TENANT_ADMIN_PASSWORD);
        try {
            commandLineService = RestCommandLineService.getInstance();
            commandLineService.login(serverURL, superAdmin,
                    superAdminPassword, false);
        } catch (Exception e) {
            throw new AppFactoryException("Could not initialize the Rest Client", e);
        }*/

        String serviceEPR = properties.get(AppFactoryTenantCloudInitializerTask.SERVER_URL)
                + "/services/TenantMgtAdminService";
        try {
            stub = new TenantMgtAdminServiceStub(ServiceReferenceHolder.getInstance()
                    .getConfigurationContextService()
                    .getClientConfigContext(), serviceEPR
            );
            CarbonUtils.setBasicAccessSecurityHeaders(properties.get
                    (AppFactoryTenantCloudInitializerTask.SUPER_TENANT_ADMIN),
                    properties.get(AppFactoryTenantCloudInitializerTask.SUPER_TENANT_ADMIN_PASSWORD),
                    stub._getServiceClient());
        } catch (AxisFault axisFault) {
            String msg = "Error while initializing TenantMgt Admin Service Stub ";
            log.error(msg, axisFault);
        }
    }

    private void addTenant(Map<String, String> properties) throws AppFactoryException {
        /*String tenantDomain = properties.get(AppFactoryTenantCloudInitializerTask.TENANT_DOMAIN);
        String adminUsername = properties.get(AppFactoryTenantCloudInitializerTask.ADMIN_USERNAME);
        String adminFirstName = properties.get(AppFactoryTenantCloudInitializerTask.ADMIN_LAST_NAME);
        String adminLastName = properties.get(AppFactoryTenantCloudInitializerTask
                .ADMIN_FIRST_NAME);
        String adminPassword = properties.get(AppFactoryTenantCloudInitializerTask
                .ADMIN_PASSWORD);
        String adminEmail = properties.get(AppFactoryTenantCloudInitializerTask
                .ADMIN_EMAIL);
        String serverURL = properties.get(AppFactoryTenantCloudInitializerTask.SERVER_URL);
        try {
            commandLineService.addTenant(adminUsername, adminFirstName, adminLastName, adminPassword,
                    tenantDomain, adminEmail);
        } catch (CommandException e) {
            String msg = "Error while creating a tenant in " + serverURL;
            throw new AppFactoryException(msg, e);
        }*/
        TenantInfoBean tenantInfoBean = new TenantInfoBean();
        tenantInfoBean.setCreatedDate(Calendar.getInstance());
        tenantInfoBean.setUsagePlan(properties.get(AppFactoryTenantCloudInitializerTask.TENANT_USAGE_PLAN));
        tenantInfoBean.setTenantDomain(properties.get(AppFactoryTenantCloudInitializerTask.TENANT_DOMAIN));
        tenantInfoBean.setSuccessKey(properties.get(AppFactoryTenantCloudInitializerTask.SUCCESS_KEY));
        tenantInfoBean.setActive(true);
        tenantInfoBean.setAdmin(properties.get(AppFactoryTenantCloudInitializerTask.ADMIN_USERNAME));
        tenantInfoBean.setAdminPassword(properties.get(AppFactoryTenantCloudInitializerTask.ADMIN_PASSWORD));
        tenantInfoBean.setEmail(properties.get(AppFactoryTenantCloudInitializerTask.ADMIN_EMAIL));
        tenantInfoBean.setFirstname(properties.get(AppFactoryTenantCloudInitializerTask.ADMIN_FIRST_NAME));
        tenantInfoBean.setLastname(properties.get(AppFactoryTenantCloudInitializerTask.ADMIN_LAST_NAME));
        tenantInfoBean.setOriginatedService(properties.get(AppFactoryTenantCloudInitializerTask.ORIGINATED_SERVICE));
        tenantInfoBean.setTenantId(Integer.parseInt(properties.get(AppFactoryTenantCloudInitializerTask.TENANT_ID)));
        try {
            stub.addTenant(tenantInfoBean);
           /* if (log.isDebugEnabled()) {*/
            log.info("Called TenantMgt Admin Service in " + properties.get
                    (AppFactoryTenantCloudInitializerTask.SERVER_URL) + " with " + tenantInfoBean);
           /* }*/

        } catch (RemoteException e) {
            String msg = "Error while adding tenant " + tenantInfoBean.getTenantDomain();
            log.error(msg, e);
        } catch (TenantMgtAdminServiceExceptionException e) {
            String msg = "Error while invoking TenantMgtAdminService for " + tenantInfoBean.getTenantDomain();
            log.error(msg, e);
        }
    }

    private String createGitRepository(DeployerInfo deployerInfo, Map<String, String> properties) throws AppFactoryException {
        String repoUrl;
        String stage = properties
                .get(AppFactoryTenantCloudInitializerTask.STAGE);
        int tenantID = Integer.parseInt(properties.get(AppFactoryTenantCloudInitializerTask
                .TENANT_ID));
        try {
            RepositoryProvider repoProvider = (RepositoryProvider) deployerInfo.getRepoProvider().newInstance();
            repoProvider.setBaseUrl(deployerInfo.getBaseURL());
            repoProvider.setAdminUsername(deployerInfo.getAdminUserName());
            repoProvider.setAdminPassword(deployerInfo.getAdminPassword());
            repoProvider.setRepoName(generateRepoUrlFromTemplate(deployerInfo.getRepoPattern(), tenantID, stage));

            repoUrl = repoProvider.createRepository();
            log.info("***************************repo url 1:" + repoUrl + "******************");
        } catch (InstantiationException e) {
            String msg = "Unable to create repository";
            throw new AppFactoryException(msg, e);
        } catch (IllegalAccessException e) {
            String msg = "Unable to create repository";
            throw new AppFactoryException(msg, e);
        } catch (AppFactoryException e) {
            String msg = "Unable to create repository";
            throw new AppFactoryException(msg, e);
        }
        return repoUrl;
    }

    private void subscribe(DeployerInfo deployerInfo, Map<String, String> properties, String stage) throws AppFactoryException {
        String serverURL = properties.get(AppFactoryTenantCloudInitializerTask.SERVER_URL);
        String tenantAdmin = properties.get(AppFactoryTenantCloudInitializerTask.ADMIN_USERNAME);
        String tenantAdminPassword = properties.get(AppFactoryTenantCloudInitializerTask.ADMIN_PASSWORD);
        String tenantDomain = properties.get(AppFactoryTenantCloudInitializerTask.TENANT_DOMAIN);
        String username = tenantAdmin + "@" + tenantDomain;

        restService = new StratosRestService(serverURL, username, tenantAdminPassword);


        restService.subscribe(deployerInfo.getCartridgeType(), deployerInfo.getAlias()+ tenantDomain.replace(".", "dot"),
                    deployerInfo.getRepoURL(), true, deployerInfo.getAdminUserName(),
                    deployerInfo.getAdminPassword(), deployerInfo.getDataCartridgeType(),
                    deployerInfo.getDataCartridgeAlias(), deployerInfo.getDeploymentPolicy(),
                    deployerInfo.getAutoscalePolicy());

    }
}
