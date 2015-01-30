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

package org.wso2.carbon.appfactory.application.mgt.util;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.cache.Cache;
import javax.cache.Caching;

public class UserApplicationCache {
    private static Log log = LogFactory.getLog(UserApplicationCache.class);

    public static final String USER_APPLICATIONS_CACHE_NAME = "USER_APPLICATIONS_CACHE";
    public static final String USER_APPLICATIONS_CACHE_MANAGER = "USER_APPLICATIONS_MANAGER";

    protected Cache<String, String[]> cache = null;

    private static UserApplicationCache userApplicationCache = new UserApplicationCache();

    private UserApplicationCache() {
        this.cache = Caching.getCacheManager(USER_APPLICATIONS_CACHE_MANAGER).getCache(USER_APPLICATIONS_CACHE_NAME);
        if (log.isDebugEnabled()) {
            if (cache != null) {
                log.debug(USER_APPLICATIONS_CACHE_NAME + " is successfully initiated.");
            } else {
                log.error(USER_APPLICATIONS_CACHE_NAME + " is not initiated.");
            }
        }
    }

    public static UserApplicationCache getUserApplicationCache() {
        return userApplicationCache;
    }

    public void addToCache(String username, String[] applications) {
        if (isCacheNull()) {
            return;
        }
        this.cache.put(username, applications);
        if (log.isDebugEnabled()) {
            log.debug(USER_APPLICATIONS_CACHE_NAME + " was updated for user:" + username);
        }
    }

    public String[] getValueFromCache(String username) {
        String[] applications = null;
        if (isCacheNull()) {
            return applications;
        }
        Object cacheValue = this.cache.get(username);
        if (cacheValue instanceof String[]) {
            applications = (String[]) cacheValue;
        }
        return applications;
    }

    private boolean isCacheNull() {
        if (this.cache == null) {
            if (log.isDebugEnabled()) {
                StackTraceElement[] elemets = Thread.currentThread().getStackTrace();
                String traceString = "";
                for (int i = 1; i < elemets.length; ++i) {
                    traceString += elemets[i] + System.getProperty("line.separator");
                }
                log.debug(USER_APPLICATIONS_CACHE_NAME + " doesn't exist in CacheManager:\n" + traceString);
            }
            return true;
        }
        return false;
    }

    public void clearFromCache(String username) {
        if (getValueFromCache(username) != null) {
            this.cache.remove(username);
            if (log.isDebugEnabled()) {
                log.debug(USER_APPLICATIONS_CACHE_NAME + " was cleaned up for user:" + username);
            }
        }
    }
}