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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.bam.BamDataPublisher;
import org.wso2.carbon.appfactory.core.BuildDriverListener;
import org.wso2.carbon.appfactory.core.dao.JDBCApplicationDAO;
import org.wso2.carbon.appfactory.core.dto.BuildStatus;
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
    public void onBuildStart(String applicationId, String version, String revision,String userName, String repoFrom, String tenantDomain) {
    	
        log.info(applicationId + "-" + version + " build is starting...");

        // updateCurrentBuildStatus(applicationId, version, START, tenantDomain);
        // Publish stats to BAM on start build
        publishStatsToBam(applicationId, version, AppFactoryConstants.BAM_BUILD_START, tenantDomain, "", revision);
        
    }

    @Override
    public void onBuildSuccessful(String applicationId, String version, String revision, String userName, String repoFrom, String buildId,
                                  String tenantDomain) {
        log.info(applicationId + "-" + version + " build successfully");
        updateLastBuildStatus(applicationId, version, buildId, SUCCESS, userName,
                repoFrom, tenantDomain);
        // Publish stats to BAM on success
        publishStatsToBam(applicationId, version, AppFactoryConstants.BAM_BUILD_SUCCESS, tenantDomain, buildId, revision);
    }

    @Override
    public void onBuildFailure(String applicationId, String version, String revision, String userName, String repoFrom, String buildId,
                               String errorMessage, String tenantDomain) {
        log.info(applicationId + "-" + version + " failed to build");
        updateLastBuildStatus(applicationId, version, buildId, FAILED, userName, repoFrom,
                tenantDomain);
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
    public static void updateLastBuildStatus(String applicationId, String version, String buildID,
                                             String result, String userName, String repoFrom, String tenantDomain) {
        try {
            JDBCApplicationDAO applicationDAO = JDBCApplicationDAO.getInstance();

            if (repoFrom.equals(AppFactoryConstants.ORIGINAL_REPOSITORY)) {

                BuildStatus buildStatus = new BuildStatus();
                buildStatus.setLastBuildId(buildID);
                buildStatus.setLastBuildTime(System.currentTimeMillis());
                buildStatus.setLastBuildStatus(result);
                applicationDAO.updateLastBuildStatus(applicationId, version, false, null, buildStatus);


            } else if (repoFrom.equals(AppFactoryConstants.FORK_REPOSITORY)) {

                BuildStatus buildStatus = new BuildStatus();
                buildStatus.setLastBuildId(buildID);
                buildStatus.setLastBuildTime(System.currentTimeMillis());
                buildStatus.setLastBuildStatus(result);
                applicationDAO.updateLastBuildStatus(applicationId, version, true, userName, buildStatus);

            }

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

            JDBCApplicationDAO applicationDAO = JDBCApplicationDAO.getInstance();
            BuildStatus currentBuildStatus = new BuildStatus();
            currentBuildStatus.setCurrentBuildId(result);
            applicationDAO.updateCurrentBuildStatus(applicationId, version, false, null,
                    currentBuildStatus);
            // Removing app version cache related code
            // AppVersionCache.getAppVersionCache().clearCacheForAppId(applicationId);
        } catch (AppFactoryException e) {
            log.error("Error updating the appversion with current build status : " + e
                    .getMessage(), e);
        }
    }

    private void publishStatsToBam(String applicationId, String version, String result, String tenantDomain, String buildId, String revisionId) 
    {
        BamDataPublisher publisher = BamDataPublisher.getInstance();
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
