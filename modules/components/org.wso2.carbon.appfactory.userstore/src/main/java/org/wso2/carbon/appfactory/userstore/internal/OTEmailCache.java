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

package org.wso2.carbon.appfactory.userstore.internal;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.cache.Cache;
import javax.cache.Caching;

public class OTEmailCache {
    private static Log log = LogFactory.getLog(OTEmailCache.class);

    public static final String OT_EMAIL_CACHE_NAME = "OT_EMAIL_CACHE_NAME";
    public static final String OT_EMAIL_CACHE_MANAGER = "OT_EMAIL_CACHE_MANAGER";

    protected Cache<String, String> cache = null;

    private static OTEmailCache emailCache = null;

    private OTEmailCache() {
        this.cache = Caching.getCacheManager(OT_EMAIL_CACHE_MANAGER).getCache(OT_EMAIL_CACHE_NAME);
        if (log.isDebugEnabled()) {
            if (cache != null) {
                log.debug(OT_EMAIL_CACHE_NAME + " is successfully initiated.");
            } else {
                log.error(OT_EMAIL_CACHE_NAME + " is not initiated.");
            }
        }
    }

    public static OTEmailCache getOTEmailCache() {
        if (emailCache == null) {
            emailCache = new OTEmailCache();
        }
        return emailCache;
    }

    public void addToCache(String uid, String email) {
        if (isCacheNull()) {
            return;
        }
        this.cache.put(uid, email);
        if (log.isDebugEnabled()) {
            log.debug(OT_EMAIL_CACHE_NAME + " was updated for uid:" + uid);
        }
    }

    public String getValueFromCache(String uid) {
        String email = null;
        if (isCacheNull()) {
            return email;
        }
        Object cacheValue = this.cache.get(uid);
        if (cacheValue instanceof String) {
            email = (String) cacheValue;
            if (log.isDebugEnabled()) {
                log.debug("Email: " + email + " was loaded from cache.");
            }
        }
        return email;
    }

    private boolean isCacheNull() {
        if (this.cache == null) {
            if (log.isDebugEnabled()) {
                StackTraceElement[] elemets = Thread.currentThread().getStackTrace();
                String traceString = "";
                for (int i = 1; i < elemets.length; ++i) {
                    traceString += elemets[i] + System.getProperty("line.separator");
                }
                log.debug(OT_EMAIL_CACHE_NAME + " doesn't exist in CacheManager:\n" + traceString);
            }
            return true;
        }
        return false;
    }

    public void clearFromCache(String uid) {
        if (getValueFromCache(uid) != null) {
            this.cache.remove(uid);
            if (log.isDebugEnabled()) {
                log.debug(OT_EMAIL_CACHE_NAME + " was cleaned up for uid:" + uid);
            }
        }
    }

}