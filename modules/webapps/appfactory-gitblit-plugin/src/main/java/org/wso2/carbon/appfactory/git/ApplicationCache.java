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

import java.util.concurrent.TimeUnit;

/**
 * Guva cache based cache for applications of an user
 */
public class ApplicationCache {
	private Cache<String, String[]> cache;

	public ApplicationCache(GitBlitConfiguration configuration) {
		int cacheExpiryTime =
		                      Integer.parseInt(configuration.getProperty(GitBlitConstants.APPFACTORY_CACHE_EXPIRY_TIME,
		                                                                 "1"));
		cache =
		        CacheBuilder.newBuilder().maximumSize(1000)
		                    .expireAfterWrite(cacheExpiryTime, TimeUnit.MINUTES).build();
	}

	public void put(String user, String[] apps) {

		cache.put(user, apps);
	}

	public String[] get(String user) {

		return cache.getIfPresent(user);
	}
}
