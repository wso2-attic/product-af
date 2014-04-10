package org.wso2.carbon.appfactory.git;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Guva cache based cache for applications of an user
 */
public class ApplicationCache {
    private Cache<String, String[]> cache;

    public ApplicationCache(GitBlitConfiguration configuration) {
        int cacheExpiryTime= Integer.parseInt(configuration.getProperty(GitBlitConstants.APPFACTORY_CACHE_EXPIRY_TIME,"1"));
        cache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(cacheExpiryTime, TimeUnit.MINUTES)
                .build();
    }

    public void put(String user, String[] apps) {

        cache.put(user, apps);
    }

    public String[] get(String user) {

        return cache.getIfPresent(user);
    }
}
