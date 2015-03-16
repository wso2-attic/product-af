/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;

/**
 * Stores users who are newly invited to applications to refresh the users application list
 * from the rxts
 * Used for Performance improvement in the user home
 */
public class ApplicationsOfUserCache {

    private static ApplicationsOfUserCache appsOfUserCache = new ApplicationsOfUserCache();
    private Cache<String,Boolean> newlyInvitedUserCache;
    private ApplicationsOfUserCache() {
        CacheManager appCacheManager = Caching.getCacheManager(AppFactoryConstants.APPS_OF_USER_CACHE_MANAGER);
        newlyInvitedUserCache = appCacheManager.getCache(AppFactoryConstants.APPS_OF_USER_CACHE);
    }

    /**
     * Retrieve the cache
     * @return appsOfUserCache instance
     */
    public static ApplicationsOfUserCache getApplicationsOfUserCache() {
        return appsOfUserCache;
    }

    public void addToCache(String userName, boolean isUserInvitedToApplication) {
        newlyInvitedUserCache.put(userName,isUserInvitedToApplication);
    }

    public boolean isUserInvitedToApplication(String userName) {
        if(Boolean.TRUE.equals(newlyInvitedUserCache.get(userName))){
           return true;
        }
        return false;
    }

    public void clearCacheForUserName(String userName) {
        if(Boolean.TRUE.equals(newlyInvitedUserCache.get(userName))){
            newlyInvitedUserCache.remove(userName);
        }
    }
}
