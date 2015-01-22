/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *    WSO2 Inc. licenses this file to you under the Apache License,
 *    Version 2.0 (the "License"); you may not use this file except
 *    in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

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
		int cacheExpiryTime =
		                      Integer.parseInt(configuration.getProperty(GitBlitConstants.APPFACTORY_CACHE_EXPIRY_TIME,
		                                                                 "1"));
		cache =
		        CacheBuilder.newBuilder().maximumSize(1000)
		                    .expireAfterWrite(cacheExpiryTime, TimeUnit.MINUTES).build();
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
