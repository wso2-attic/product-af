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

import org.wso2.carbon.appfactory.core.dto.Resource;

import javax.cache.Cache;
import javax.cache.Caching;
import java.util.ArrayList;
import java.util.List;

/**
 * This class acts as a cache manager for JDBCResourceDAO
 */
public class JDBCResourceCacheManager {

    private static final String AF_RESOURCE_CACHE_MANAGER = "af.resource.cache.manager";
    private static final String AF_RESOURCE_CACHE = "af.resource.cache";
    private static final String KEY_SEPARATOR = "_";    //This character is used to concatenate variables to construct
                                                        // the cache key

    /**
     * Add list of resources of a particular resource type to cache
     *
     * @param applicationId id of the application, which contain the resources
     * @param resourceType  resource type of the resources i.e: data source, database, etc
     * @param environment   the environment of the resources
     * @param resources     the resources, need to be added to the cache
     * @return resource cache key
     */
    public static String addResourcesToCache(String applicationId, String resourceType, String environment,
                                             List<Resource> resources) {
        String cacheKey = constructCacheKey(applicationId, environment, resourceType);
        Cache<String, List<Resource>> cache = Caching.getCacheManager(AF_RESOURCE_CACHE_MANAGER).getCache(
                AF_RESOURCE_CACHE);
        cache.put(cacheKey, resources);
        return cacheKey;
    }

    /**
     * Get all the resource of a specific resource type from the cache
     *
     * @param appId        id of application, which contain the resources
     * @param environment  environment of the resources
     * @param resourceType type of the resource i.e: data source, database, etc
     * @return list of resources retrieved from cache
     */
    public static List<Resource> getResourcesFromCache(String appId, String environment, String resourceType) {
        String cacheKey = constructCacheKey(appId, environment, resourceType);
        Cache<String, List<Resource>> cache = Caching.getCacheManager(AF_RESOURCE_CACHE_MANAGER).getCache(
                AF_RESOURCE_CACHE);
        List<Resource> resources = cache.get(cacheKey);
        if(resources == null){
            resources = new ArrayList<Resource>();
        }
        return resources;
    }

    /**
     * This method check whether a resource is available in cache. Here we check for a single resource of a particular
     * resource type. So we can't directly use the getResourcesFromCache method to check a resource. Because it will
     * return a list of resources of a particular resource type.
     *
     * @param appId        id of application, which contain the resources
     * @param environment  the environment of the resources
     * @param resourceType type of the resource i.e: data source, database, etc.
     * @param resourceName name of the resource, which need to be checked
     * @return boolean value, which state whether the resource is exists in the cache or not
     */
    public static boolean isResourceExist(String appId, String environment, String resourceType, String resourceName) {
        String cacheKey = constructCacheKey(appId, environment, resourceType);
        Cache<String, List<Resource>> cache = Caching.getCacheManager(AF_RESOURCE_CACHE_MANAGER).getCache(
                AF_RESOURCE_CACHE);
        List<Resource> resources = cache.get(cacheKey);
        if (resources != null) {
            for (Resource resource : resources) {
                if (resource.getName().equalsIgnoreCase(resourceName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Clear the list of resources of a particular resource type from the cache.
     *
     * @param appId        id of application, which contain the resources
     * @param environment  environment of the resources
     * @param resourceType type of the resource i.e: data source, database, etc
     */
    public static void clearCache(String appId, String environment, String resourceType) {
        String cacheKey = constructCacheKey(appId, environment, resourceType);
        Caching.getCacheManager(AF_RESOURCE_CACHE_MANAGER).getCache(AF_RESOURCE_CACHE).remove(cacheKey);
    }

    /**
     * This method create the cache key for a specified resource type
     *
     * @param applicationId id of the application, which contain the resources
     * @param environment   the environment of the resources
     * @param resourceType  type of the resource i.e: data source, database, etc
     * @return the constructed key string
     */
    public static String constructCacheKey(String applicationId, String environment, String resourceType) {
        return applicationId + KEY_SEPARATOR + environment + KEY_SEPARATOR + resourceType;
    }

}
