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

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to hold axis2 configuration context
 */
public class ContextHolder {
	private static final Logger log = LoggerFactory.getLogger(ContextHolder.class);
	private static ContextHolder holder;
	private ConfigurationContext configurationContext;
	private ApplicationCache cache;
	private UserPasswordCache passwordCache;

	public ConfigurationContext getConfigurationContext() {
		return configurationContext;
	}

	public ApplicationCache getCache() {
		return cache;
	}

	public UserPasswordCache getUserPasswordCache() {
		return passwordCache;
	}

	public static ContextHolder getHolder(GitBlitConfiguration configuration) {
		if (holder == null) {
			holder = new ContextHolder();
			try {
				log.info("Creating Default Axis2 ConfigurationContext");
				holder.configurationContext =
				                              ConfigurationContextFactory.createConfigurationContextFromFileSystem(null,
				                                                                                                   null);
			} catch (AxisFault fault) {
				log.error("Error occurred while initializing  ConfigurationContext", fault);
			}
			holder.cache = new ApplicationCache(configuration);
			holder.passwordCache = new UserPasswordCache(configuration);
		}

		return holder;
	}

	private ContextHolder() {
	}

}
