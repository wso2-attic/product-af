/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appfactory.stratos.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

public class ListenerUtils {

	private static final Log log = LogFactory.getLog(ListenerUtils.class);

	/**
	 * Aquire the tenant reigstry object
	 * 
	 * @param tenantId
	 * @return
	 * @throws AppFactoryException
	 */
	public static Registry getTenantRegistryObj(int tenantId) throws AppFactoryException {
		try {
			AppFactoryS4ListenersUtil.getTenantRegistryLoader().loadTenantRegistry(tenantId);
			return AppFactoryS4ListenersUtil.getRegistryService()
			                              .getGovernanceSystemRegistry(tenantId);
		} catch (RegistryException e) {
			String msg = "Error while retriving tenant registry for tenant id " + tenantId;
			log.error(msg, e);
			throw new AppFactoryException(msg, e);
		}
	}

	/**
	 * Create the path under /_system/governance
	 * 
	 * @param tenantId
	 * @param resourcePath
	 * @throws AppFactoryException
	 */
	public static void createDependenciesPath(int tenantId, String resourcePath)
	                                                                            throws AppFactoryException {
		Registry tenantRegistry = getTenantRegistryObj(tenantId);

		Resource collection;
		try {
			if (tenantRegistry.resourceExists(resourcePath)) {
				collection = tenantRegistry.get(resourcePath);
			} else {
				collection = tenantRegistry.newCollection();
			}
			tenantRegistry.put(resourcePath, collection);
		} catch (RegistryException e) {
			String msg = "Error while creating registry collection " + resourcePath;
			log.error(msg, e);
			throw new AppFactoryException(msg, e);
		}

	}

	/**
	 * Retrieve the correct action corresponds to the configuration value
	 * 
	 * @param action
	 * @param logMsg
	 * @return
	 * @throws AppFactoryException
	 */
	public static String getActionConstant(String action, String logMsg) throws AppFactoryException {
		if (action.toLowerCase().equals("get")) {
			return ActionConstants.GET;
		} else if (action.toLowerCase().equals("delete")) {
			return ActionConstants.DELETE;
		} else if (action.toLowerCase().equals("put")) {
			return ActionConstants.PUT;
		} else {
			String msg = " Invalid action " + action + logMsg;
			log.error(msg);
			return null;
		}
	}
}
