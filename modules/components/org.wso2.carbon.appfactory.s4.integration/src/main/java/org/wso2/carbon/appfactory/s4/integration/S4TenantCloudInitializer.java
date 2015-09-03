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
package org.wso2.carbon.appfactory.s4.integration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.TenantCloudInitializer;
import org.wso2.carbon.appfactory.eventing.AppFactoryEventException;

import java.util.Map;

/**
 * This creates tenant in particular environment in S2 based cloud
 */
public class S4TenantCloudInitializer implements TenantCloudInitializer {
    private static final Log log = LogFactory.getLog(S4TenantCloudInitializer.class);

	/**
	 * This method will be called for creating tenant in a stage
	 * 
	 * @param properties
	 *            key value pairs supplied by task
	 */
	@Override
	public void onTenantCreation(Map<String, String> properties) {

		try {
			String stageJson = properties.get(AppFactoryConstants.STAGE);
			String runtimesJson = properties.get(AppFactoryConstants.RUNTIMES);
			String tenantInfoJson = properties.get(AppFactoryConstants.TENANT_INFO);
			publishToQueue(runtimesJson, tenantInfoJson, properties, stageJson);
			log.info("successfully created tenant in " + stageJson);
		} catch (AppFactoryException e) {
			String msg = "Can not continue tenant creation due to "
					+ e.getLocalizedMessage();
			log.error(msg, e);
		}

	}

	/**
	 * Publish message to queue
	 * @param runtimeJson runtime beans as a json
	 * @param tenantInfoJson tenant info bean as a json
	 * @param properties
	 *@param stageJson stages  @throws AppFactoryException
	 */
	private void publishToQueue(String runtimeJson, String tenantInfoJson, Map<String, String> properties, String stageJson)
			throws AppFactoryException {

		TenantStratosSubscriptionMessagePublisher stratosSubscriptionMessagePublisher =
				new TenantStratosSubscriptionMessagePublisher(AppFactoryConstants.TENANT_SUBSCRIPTION_TOPIC);
		try {
			stratosSubscriptionMessagePublisher.publishMessage(runtimeJson, tenantInfoJson, properties, stageJson);
		} catch (AppFactoryEventException e) {
			String msg = "Failed to subscribe tenant, tenantInfoJSON: " + tenantInfoJson + " due to " + e.getMessage();
			throw new AppFactoryException(msg, e);
		}


	}
}
