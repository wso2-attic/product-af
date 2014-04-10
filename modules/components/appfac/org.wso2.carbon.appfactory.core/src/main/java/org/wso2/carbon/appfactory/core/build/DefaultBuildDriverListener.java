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

package org.wso2.carbon.appfactory.core.build;

import javax.activation.DataHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.bam.integration.BamDataPublisher;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.BuildDriverListener;
import org.wso2.carbon.appfactory.core.cache.AppVersionCache;
import org.wso2.carbon.appfactory.core.governance.RxtManager;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.user.api.UserStoreException;


public class DefaultBuildDriverListener implements BuildDriverListener {

    private static final Log log = LogFactory.getLog(DefaultBuildDriverListener.class);
    private static final String START = "start";
    private static final String SUCCESS = "successful";
    private static final String FAILED = "failed";
    private static String LAST_BUILD_STATUS_KEY = "LastBuildStatus";
    private static String CURRENT_BUILD_STATUS_KEY = "CurrentBuildStatus";

    
    @Override
    public void onBuildStart(String applicationId, String version, String revision, String tenantDomain) {
    	
        log.info(applicationId + "-" + version + " build is starting...");
        //updateCurrentBuildStatus(applicationId, version, START, tenantDomain);
        // Publish stats to BAM on start build
        publishStatsToBam(applicationId, version, AppFactoryConstants.BAM_BUILD_START, tenantDomain, "", revision);
        
    }

    @Override
    public void onBuildSuccessful(String applicationId, String version, String revision, String buildId,
                                  DataHandler dataHandler, String fileName, String tenantDomain) {
        log.info(applicationId + "-" + version + " build successfully");
        updateLastBuildStatus(applicationId, version, "build " + buildId + " " + SUCCESS, tenantDomain);
        //updateCurrentBuildStatus(applicationId, version, "", tenantDomain);
        // Publish stats to BAM on success
        publishStatsToBam(applicationId, version, AppFactoryConstants.BAM_BUILD_SUCCESS, tenantDomain, buildId, revision);
    }

    @Override
    public void onBuildFailure(String applicationId, String version, String revision, String buildId,
                               String errorMessage, String tenantDomain) {
        log.info(applicationId + "-" + version + " failed to build");
        updateLastBuildStatus(applicationId, version, "build " + buildId + " " + FAILED, tenantDomain);
        //updateCurrentBuildStatus(applicationId, version, "", tenantDomain);
        // Publish stats to BAM on failure
        publishStatsToBam(applicationId, version, AppFactoryConstants.BAM_BUILD_FAIL, tenantDomain, buildId, revision);
    }

    /**
     *
     * @param applicationId
     * @param version
     * @param result
     * @throws AppFactoryException
     */
    public static void updateLastBuildStatus(String applicationId, String version, String result, String tenantDomain) {
        try {
            RxtManager rxtManager = new RxtManager();
            String []key = {"appversion_"+LAST_BUILD_STATUS_KEY,"appversion_"+CURRENT_BUILD_STATUS_KEY};
            String []values = {result , "finished"};
            rxtManager.updateAppVersionRxt(applicationId, version, key, values, tenantDomain);

            // Removing app version cache related code
            // AppVersionCache.getAppVersionCache().clearCacheForAppId(applicationId);
        } catch (AppFactoryException e) {
            log.error("Error updating the appversion rxt with last build status : " + e.getMessage(), e);
        }
    }
    
    /**
    *
    * @param applicationId
    * @param version
    * @param result
    * @throws AppFactoryException
    */
   public static void updateCurrentBuildStatus(String applicationId, String version, String result, String tenantDomain) {
       try {
           RxtManager rxtManager = new RxtManager();
           String key = "appversion_"+CURRENT_BUILD_STATUS_KEY;
           rxtManager.updateAppVersionRxt(applicationId, version, key, result, tenantDomain);

           // Removing app version cache related code
           // AppVersionCache.getAppVersionCache().clearCacheForAppId(applicationId);
       } catch (AppFactoryException e) {
           log.error("Error updating the appversion rxt with current build status : " + e.getMessage(), e);
       }
   }

    private void publishStatsToBam(String applicationId, String version, String result, String tenantDomain, String buildId, String revisionId) 
    {
        BamDataPublisher publisher = new BamDataPublisher();
        String tenantId = null;
        
	try {
            tenantId = "" + ServiceHolder.getRealmService().getTenantManager().getTenantId(tenantDomain);

            // Need to obtain information such as build revision, user, and application name 
	        publisher.PublishBuildEvent(applicationId, applicationId, version, System.currentTimeMillis(), tenantId, result, buildId, revisionId, "");
        } catch (UserStoreException e) {
            log.error("Unable to get tenant ID for bam stats : " + e.getMessage(), e);
        } catch (AppFactoryException e) {
            log.error("Unable to publish bam stats " + e.getMessage(), e);
        }

    }	

  

}
