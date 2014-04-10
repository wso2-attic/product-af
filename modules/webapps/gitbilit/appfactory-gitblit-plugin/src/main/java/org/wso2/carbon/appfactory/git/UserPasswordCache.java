package org.wso2.carbon.appfactory.git;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

/**
 * Guva cache based cache for hashed password
 */
public class UserPasswordCache {
    private static final Logger log = LoggerFactory.getLogger(UserPasswordCache.class);
    private Cache<String, byte[]> cache;

    public UserPasswordCache(GitBlitConfiguration configuration) {
        int cacheExpiryTime= Integer.parseInt(configuration.getProperty(GitBlitConstants.APPFACTORY_CACHE_EXPIRY_TIME,"1"));
        cache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(cacheExpiryTime, TimeUnit.MINUTES)
                .build();
    }


    public void put(String user, String password) {
        byte[] pwd;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(password.getBytes());
            pwd = md.digest();
        } catch (NoSuchAlgorithmException e) {
            log.error("Specified hashing algorithm is not found ", e);
            return;
        }

        cache.put(user, pwd);
    }

    public byte[] get(String user, String password) {

        return cache.getIfPresent(user);
    }

    public static byte[] getHashedPassword(String password) {
        byte[] pwd = new byte[0];
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(password.getBytes());
            pwd = md.digest();
        } catch (NoSuchAlgorithmException e) {
            log.error("Specified hashing algorithm is not found ", e);
        }
        return pwd;
    }
}
