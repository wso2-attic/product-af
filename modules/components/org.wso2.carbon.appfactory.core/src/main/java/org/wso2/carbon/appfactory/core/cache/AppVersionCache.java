/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.appfactory.core.cache;

import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.core.deploy.Artifact;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;

/**
 * Stores app versions in a cache against app id.
 * Used for performance ehancement in the UI
 */
public class AppVersionCache {
    
    private static AppVersionCache appVersionCache = new AppVersionCache();
    private Cache<String,Artifact[]> artifactCache;
    private AppVersionCache() {
        CacheManager appCacheManager= Caching.getCacheManager(AppFactoryConstants.APP_VERSION_CACHE_MANAGER);
        artifactCache=appCacheManager.getCache(AppFactoryConstants.APP_VERSION_CACHE);
    }

    public static AppVersionCache getAppVersionCache() {
        return appVersionCache;
    }

    public void addToCache(String appId, Artifact[] artifacts) {
          artifactCache.put(appId,artifacts);
    }

    public Artifact[] getAppVersions(String appId) {
           return   artifactCache.get(appId);
    }
    
    //Improve this method to clear cache for app Id
    public void clearCacheForAppId(String appId) {
           artifactCache.remove(appId);
    }

}
