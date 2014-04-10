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
