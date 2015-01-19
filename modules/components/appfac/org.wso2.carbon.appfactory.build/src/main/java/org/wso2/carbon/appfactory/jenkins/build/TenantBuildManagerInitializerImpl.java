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
package org.wso2.carbon.appfactory.jenkins.build;

import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.buildserver.teanant.mgt.stub.BuildServerManagementServiceBuildServerManagementExceptionException;
import org.wso2.carbon.appfactory.buildserver.teanant.mgt.stub.BuildServerManagementServiceStub;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.core.TenantBuildManagerInitializer;
import org.wso2.carbon.appfactory.jenkins.build.internal.ServiceContainer;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * Default implementation of {@link TenantBuildManagerInitializer}
 */
public class TenantBuildManagerInitializerImpl implements
		TenantBuildManagerInitializer {
	private static final Log log = LogFactory
			.getLog(TenantBuildManagerInitializerImpl.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onTenantCreation(String tenantDomain, String usagePlan) {
		log.info("**********************Initializing build manager for "
				+ tenantDomain + " with " + usagePlan + " *************");
		try {
			
			String endPoint = ServiceContainer.getAppFactoryConfiguration().
                    getFirstProperty(JenkinsCIConstants.BASE_URL_CONFIG_SELECTOR) + "/services/BuildServerManagementService";
			
			BuildServerManagementServiceStub buildServerMgr = new BuildServerManagementServiceStub(endPoint);
			ServiceClient client = buildServerMgr._getServiceClient();
			CarbonUtils.setBasicAccessSecurityHeaders(
					ServiceContainer.getAppFactoryConfiguration()
							.getFirstProperty(
									AppFactoryConstants.SERVER_ADMIN_NAME),
					ServiceContainer.getAppFactoryConfiguration()
							.getFirstProperty(
									AppFactoryConstants.SERVER_ADMIN_PASSWORD),
					client);
			buildServerMgr.createTenant(tenantDomain);

		} catch (AxisFault e) {
			String msg = "Problem occurred when creaing tenant in build server";
			log.error(msg, e);
		} catch (RemoteException e) {
			String msg = "Problem occurred when creaing tenant in build server";
			log.error(msg, e);
		} catch (BuildServerManagementServiceBuildServerManagementExceptionException e) {
			String msg = "Problem occurred when creaing tenant in build server";
			log.error(msg, e);
		}

	}
}
