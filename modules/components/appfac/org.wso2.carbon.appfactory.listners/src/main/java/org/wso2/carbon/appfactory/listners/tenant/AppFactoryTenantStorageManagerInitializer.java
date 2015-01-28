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
package org.wso2.carbon.appfactory.listners.tenant;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.TenantCreationNotificationInitializer;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.apache.stratos.tenant.mgt.stub.TenantMgtAdminServiceExceptionException;
import org.apache.stratos.tenant.mgt.stub.TenantMgtAdminServiceStub;
import org.apache.stratos.tenant.mgt.stub.beans.xsd.TenantInfoBean;
import org.wso2.carbon.utils.CarbonUtils;

public class AppFactoryTenantStorageManagerInitializer implements TenantCreationNotificationInitializer{
	
	public static final String TENANT_MGT_ADMIN_SERVICE = "TenantMgtAdminService";
	
	private static final Log log = LogFactory.getLog(AppFactoryTenantStorageManagerInitializer.class);
	private TenantMgtAdminServiceStub stub;
	
	@Override
	public void onTenantCreation(TenantInfoBean tenantInfoBean) {
		try {
			log.info("Storage Server tenant initialization is started.");
			String[] stages = AppFactoryUtil.getAppfactoryConfiguration().
					getProperties("ApplicationDeployment.DeploymentStage");
			String[] endPoints = new String[stages.length];
			for (int i = 0; i < stages.length ; i++) {
				endPoints[i] = AppFactoryUtil.getAppfactoryConfiguration().
						getFirstProperty("ApplicationDeployment.DeploymentStage." + stages[i] 
								+ ".StorageServerUrl") + "TenantMgtAdminService";			
			}
			String[] uniqueEndpoints = (new HashSet<String>(Arrays.asList(endPoints))).toArray(new String[0]);
			for (String endPoint : uniqueEndpoints) {
				try {
					stub = new TenantMgtAdminServiceStub(ServiceHolder.getInstance().getConfigContextService()
		                    .getClientConfigContext(), endPoint);
		            CarbonUtils.setBasicAccessSecurityHeaders(AppFactoryUtil.getAppfactoryConfiguration()
		            		.getFirstProperty(AppFactoryConstants.SERVER_ADMIN_NAME),AppFactoryUtil.getAppfactoryConfiguration()
		            		.getFirstProperty(AppFactoryConstants.SERVER_ADMIN_PASSWORD),
		                    stub._getServiceClient());
					stub.addTenant(tenantInfoBean);
				} catch (RemoteException e) {
		            String msg = "Error while adding tenant for Storage Server" + tenantInfoBean.getTenantDomain();
		            log.error(msg, e);
		        } catch (TenantMgtAdminServiceExceptionException e) {
		            String msg = "Error while invoking TenantMgtAdminService for Storage Server" + tenantInfoBean.getTenantDomain();
		            log.error(msg, e);
		        }
			}
			
		} catch (AppFactoryException e) {
			log.error("Error while retrieving Storage Server Urls : " + e);
		}
		
	}

}
