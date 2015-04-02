package org.wso2.carbon.appfactory.core.governance.cache;

import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;

import javax.cache.Cache;
import javax.cache.Caching;

/**
 * class to maintain the application rxt information in carbon level cache
 */
public class RxtApplicationCacheManager {

    private static final String AF_APPLICATION_ARTIFACT_CACHE_MANAGER = "af.application.artifact.cache.manager";
    private static final String AF_APPLICATION_ARTIFACT_CACHE = "af.application.artifact.cache";

    /**
     * Method to add the application artifact to cache
     *
     * @param appArtifact application rxt artifact, which need to be saved in cache
     * @param appKey key of the application. since the cache is tenant specific,
     *               this application key will act as the cache key as well.
     */
    public static void addArtifactToCache(GenericArtifact appArtifact, String appKey) {
        Cache<String, GenericArtifact> cache = Caching.getCacheManager(
                AF_APPLICATION_ARTIFACT_CACHE_MANAGER).getCache(AF_APPLICATION_ARTIFACT_CACHE);
        cache.put(appKey, appArtifact);
    }

    /**
     * Method to get the application rxt artifact from cache
     *
     * @param appKey key of the application, which need to be retrieved from cache. since the cache is tenant specific,
     *               this application key will act as the cache key as well.
     * @return
     */
    public static GenericArtifact getAppArtifactFromCache(String appKey) {
        Cache<String, GenericArtifact> cache = Caching.getCacheManager(AF_APPLICATION_ARTIFACT_CACHE_MANAGER).getCache(
                AF_APPLICATION_ARTIFACT_CACHE);
        return cache.get(appKey);
    }

    /**
     * Method to remove a specific application rxt artifact from cache
     *
     * @param appKey key of the application, which need to be removed from cache. since the cache is tenant specific,
     *               this application key will act as the cache key as well.
     * @return
     */
    public static boolean clearCache(String appKey) {
        Cache<String, GenericArtifact> cache = Caching.getCacheManager(AF_APPLICATION_ARTIFACT_CACHE_MANAGER).getCache(
                AF_APPLICATION_ARTIFACT_CACHE);
        return cache.remove(appKey);
    }
}
