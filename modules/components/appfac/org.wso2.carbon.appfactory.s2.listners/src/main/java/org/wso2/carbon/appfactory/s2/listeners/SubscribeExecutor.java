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

package org.wso2.carbon.appfactory.s2.listeners;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.adc.mgt.utils.ApplicationManagementUtil;
import org.wso2.carbon.appfactory.common.AppFactoryException;

import java.io.File;

public class SubscribeExecutor implements Runnable {
    private static final Log log = LogFactory.getLog(SubscribeExecutor.class);

   // private String applicationId;
    private DeployerInfo deployerInfo;
    private String stage;
    private int tenantId;
    private String tenantDomain;


//    public void setApplicationId(String applicationId) {
//        this.applicationId = applicationId;
//    }

    public void setDeployerInfo(DeployerInfo deployerInfo) {
        this.deployerInfo = deployerInfo;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    @Override
    public void run() {
        if (log.isDebugEnabled()) {
            log.debug("Thread started for tenant id : " + tenantId + " for cartridge type : "
                    + deployerInfo.getCartridgeType());
        }
        String repoUrl = null;
//        This is where we create a git repo
        try {
            RepositoryProvider repoProvider = (RepositoryProvider) deployerInfo.getRepoProvider().newInstance();
            repoProvider.setBaseUrl(deployerInfo.getBaseURL());
            repoProvider.setAdminUsername(deployerInfo.getAdminUserName());
            repoProvider.setAdminPassword(deployerInfo.getAdminPassword());
            repoProvider.setRepoName(generateRepoUrlFromTemplate(deployerInfo.getRepoPattern(), tenantId, stage));

            repoUrl = repoProvider.createRepository();
            log.info("***************************repo url 1:" + repoUrl + "******************");
        } catch (InstantiationException e) {
            String msg = "Unable to create repository";
            log.error(msg, e);
        } catch (IllegalAccessException e) {
            String msg = "Unable to create repository";
            log.error(msg, e);
        } catch (AppFactoryException e) {
            String msg = "Unable to create repository";
            log.error(msg, e);
        }


        /* Alias needs to be alpha-numeric - lower case */
        String alias =  deployerInfo.getAlias();
        try {

            if (deployerInfo.getEndpoint() != null) {

//                Subscribing via the Utility method.
//                Username is passed as null because it is only used when an internal git repo is used

                        log.info("***************************repo url 2:" + repoUrl + "******************");

               ApplicationManagementUtil.doSubscribe(deployerInfo.getCartridgeType(),
                       alias+Integer.toString(tenantId),
                       //*This will the name of policy file*//*
                                                      deployerInfo.getPolicyName() , repoUrl,
                                                      true,deployerInfo.getAdminUserName(),
                                                      deployerInfo.getAdminPassword(),
                                                      deployerInfo.getDataCartridgeType(),
                                                      deployerInfo.getDataCartridgeAlias(), tenantDomain,
                                                      tenantId,tenantDomain);


            }

            // public static org.wso2.carbon.adc.mgt.dto.SubscriptionInfo doSubscribe(java.lang.String cartridgeType,
            // java.lang.String alias, java.lang.String policy, java.lang.String repoURL,
            // boolean privateRepo, java.lang.String repoUsername, java.lang.String repoPassword, java.lang.String dataCartridgeType,
            // java.lang.String dataCartridgeAlias, java.lang.String username, int tenantId, java.lang.String tenantDomain)

        } catch (Exception e) {
            String msg = "Unable to subscribe to the cartridge : " + deployerInfo.getCartridgeType() +
                    " for the tenant : " + tenantId + " repo Url : " + repoUrl;
            log.error(msg, e);
        }

    }

    private String generateRepoUrlFromTemplate(String pattern, int tenantId, String stage) {
//

       String s =  pattern.replace("{@stage}",stage) + File.separator + Integer.toString(tenantId);
        log.info("**************************************generated repo URL: " + s + "******************************");
        return s;





    }
}
