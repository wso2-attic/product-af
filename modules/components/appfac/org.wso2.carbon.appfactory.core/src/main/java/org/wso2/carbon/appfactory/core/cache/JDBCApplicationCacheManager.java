/*
*  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.appfactory.core.cache;

import org.wso2.carbon.appfactory.core.dto.BuildStatus;
import org.wso2.carbon.appfactory.core.dto.DeployStatus;
import org.wso2.carbon.appfactory.core.util.Constants;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;

/**
 * This class acts as the cache manager for JDBCApplicationDAO.
 * We use this cache to keep application against application key.
 */
public class JDBCApplicationCacheManager {
	
    public static final String AF_APPLICATION_CACHE_MANAGER = "af.application.cache.manager";
    public static final String AF_APPLICATION_NAME_CACHE = "af.application.name.cache";
    
    public static final String AF_APPLICATION_DEPLOY_STATUS_CACHE = "af.application.deploy.status.cache";
    public static final String AF_APPLICATION_BUILD_STATUS_CACHE = "af.application.build.status.cache";
    public static final String AF_APPLICATION_CREATION_STATUS_CACHE = "af.application.creation.status.cache";
    
    public static final String AF_APPLICATION_DATABASE_ID_CACHE = "af.application.database.id.cache";

    public static final String AF_APPLICATION_INFO_CACHE = "af.application.info.cache";

    // The cache key separator value. This value is used to combine different parameters to generate the cache key
    public static final String KEY_SEPARATOR = "_";
    //the cache key value for application app info
    public static final String APPS_INFO = "apps_info";

    public static CacheManager getCacheManager(){
        return Caching.getCacheManager(AF_APPLICATION_CACHE_MANAGER);
    }

    /**
     * The cache that holds the application name availability against the application name
     *
     * @return the cache that holds the application name
     */
    public static Cache<String, Boolean> getJDBCApplicationNameCache() {
        return getCacheManager().getCache(AF_APPLICATION_NAME_CACHE);
    }

    /**
     * The cache that holds the {@link org.wso2.carbon.appfactory.core.dto.DeployStatus} against the application key
     *
     * @return the cache that holds the deployment status
     */
    public static Cache<String, DeployStatus> getApplicationDeployStatusCache() {
        return getCacheManager().getCache(AF_APPLICATION_DEPLOY_STATUS_CACHE);
    }

    /**
     * The cache that holds the {@link org.wso2.carbon.appfactory.core.dto.BuildStatus} against the application key
     *
     * @return the cache chat holds the build status
     */
    public static Cache<String, BuildStatus> getApplicationBuildStatusCache() {
        return getCacheManager().getCache(AF_APPLICATION_BUILD_STATUS_CACHE);
    }

    /**
     * This cache holds the {@link Constants.ApplicationCreationStatus} against the applicationKey
     *
     * @return the cache that holds the application creation status
     */
    public static Cache<String, Constants.ApplicationCreationStatus> getApplicationCreationStatusCache() {
        return getCacheManager().getCache(AF_APPLICATION_CREATION_STATUS_CACHE);
    }

    /**
     * This cache holds the database id of an application against the application key
     *
     * @return the cache that holds the database ids
     */
    public static Cache<String, Integer> getApplicationIdCache() {
        // This cache holds the AF_APPLICATION database row id against the application key
        // The purpose of this is to reduce the multiple calls that happen to the database to fetch the application id
        return getCacheManager().getCache(AF_APPLICATION_DATABASE_ID_CACHE);
    }

    /**
     * This cache holds the branch count of apps.
     *
     * @return integer value of branch count.
     *
     */
    public static Cache<String, Integer> getApplicationBranchCountCache() {
        // This cache holds the AF_APPLICATION branch count against the application key
        // The purpose of this is to reduce the multiple calls that happen to the database to fetch the application id
        return getCacheManager().getCache(AF_APPLICATION_INFO_CACHE);
    }

    /**
     * This method is used to return the cache key for the application name cache.
     * The cache key is constructed by concatenating the tenant id and the application name.
     * Ex:- 1_application1
     *
     * @param applicationName the application name that the cache key is constructed for
     * @param tenantId        the current tenant id
     * @return the cache key
     */
    public static String constructAppNameCacheKey(String applicationName, int tenantId) {
        return constructCacheKeyPrefix(tenantId, applicationName);
    }

    /**
     * This method is used to return the cache key for the deploy status cache
     * The cache key is constructed by concatenating the tenant id with the rest of the parameters that are passed
     * Ex:- 1_application1_1.0.0_dev_false_afTestUser
     *
     * @param applicationKey the application key of the current application
     * @param tenantId       the tenant id of the current tenant
     * @param version        the version of the application
     * @param environment    the current deployment environment
     * @param isForked       whether is it forked or not
     * @param username       the name of the forked user. This can be null
     * @return the constructed cache key
     */
    public static String constructDeployStatusCacheKey(String applicationKey, int tenantId, String version,
                                                       String environment, boolean isForked, String username) {
        return constructCacheKeyPrefix(tenantId, applicationKey) + KEY_SEPARATOR + version + KEY_SEPARATOR +
               environment +
               KEY_SEPARATOR + Boolean.toString(isForked) + KEY_SEPARATOR + (username == null ? "" : username);
    }

    /**
     * This method is used to create the cache key prefix
     * All the cache keys start with the prefix tenantId + "_" + application Key
     * Hence if we need to clear cache for a given application key, this method will be useful
     *
     * @param tenantId       the current tenant id
     * @param applicationKey the application key that the cache key is constructed for
     * @return the constructed cache key prefix
     */
    public static String constructCacheKeyPrefix(int tenantId, String applicationKey) {
        return tenantId + KEY_SEPARATOR + applicationKey;
    }

    /**
     * This method is used to return the cache key for the build status cache
     * The cache key is constructed by concatenating the tenant id with the rest of the parameters that are passed
     * Ex:- 1_application1_1.0.0_false_afTestUser
     *
     * @param applicationKey the application key of the current application
     * @param tenantId       the tenant id of the current tenant
     * @param version        the version of the application
     * @param isForked       whether is it forked or not
     * @param username       the name of the forked user. This can be null
     * @return the constructed cache key
     */
    public static String constructBuildStatusCacheKey(int tenantId, String applicationKey, String version,
                                                      boolean isForked, String username) {
        return constructCacheKeyPrefix(tenantId, applicationKey) + KEY_SEPARATOR + version + KEY_SEPARATOR + Boolean
                .toString(isForked) + KEY_SEPARATOR + (username == null ? "" : username);
    }

    /**
     * This method is used to return the cache key for the application name cache.
     * The cache key is constructed by concatenating the tenant id and the application name.
     * Ex:- 1_application1
     *
     * @param applicationKey the application key that the cache key is constructed for
     * @param tenantId       the current tenant id
     * @return the cache key
     */
    public static String constructApplicationCreationCacheKey(int tenantId, String applicationKey) {
        return constructCacheKeyPrefix(tenantId, applicationKey);
    }

    /**
     * This method is used to return the cache key for the application id cache.
     * The cache key is constructed by concatenating the tenant id and the application name.
     * Ex:- 1_application1
     *
     * @param applicationKey the application key that the cache key is constructed for
     * @param tenantId       the current tenant id
     * @return the cache key
     */
    public static String constructApplicationIdCacheKey(int tenantId, String applicationKey) {
        return constructCacheKeyPrefix(tenantId, applicationKey);
    }

    /**
     * +     * This method is used to construct the cache key for branch count.
     * +     * @param tenantId - the current tenant id
     * +     * @param ApplicationKey - the application key that the key is constructed for
     * +     * @return - the cache key.
     * +
     */
    public static String constructApplicationBranchCountCacheKey(int tenantId,
                                                                 String ApplicationKey) {
        return constructCacheKeyPrefix(tenantId, ApplicationKey) + KEY_SEPARATOR + APPS_INFO;
    }
}
