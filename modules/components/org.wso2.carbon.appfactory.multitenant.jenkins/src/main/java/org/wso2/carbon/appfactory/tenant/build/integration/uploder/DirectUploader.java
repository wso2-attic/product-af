/*
 * Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.appfactory.tenant.build.integration.uploder;

import java.io.File;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.tenant.build.integration.AppServerBasedBuildServerManager;
import org.wso2.carbon.appfactory.tenant.build.integration.internal.ServiceContainer;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.webapp.mgt.WebappAdmin;
import org.wso2.carbon.webapp.mgt.WebappUploadData;

/**
 * Uploader using local transport.
 * 
 */
public class DirectUploader implements BuildServerUploader {

	private Log log = LogFactory.getLog(AppServerBasedBuildServerManager.class);

	private int tenantId = -1;
	private String tenantDomain;

	public DirectUploader(String tenantDomain) throws UserStoreException {
		this.tenantId = ServiceContainer.getInstance().getTenantManager().getTenantId(tenantDomain);
		if (tenantId < 0) {
			String msg = "Invalid tenant domain - " + tenantDomain;
			log.error(msg);
			throw new IllegalArgumentException(msg);
		}
		this.tenantDomain = tenantDomain;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.wso2.carbon.appfactory.tenant.build.integration.uploder.
	 * BuildServerUploader
	 * #uploadBuildServerApp(org.wso2.carbon.appfactory.tenant
	 * .build.integration.buildserver.BuildServerApp)
	 */
	@Override
	public void uploadBuildServerApp(File serverApp) throws Exception {
		try {

			PrivilegedCarbonContext.startTenantFlow();
			PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(this.tenantId);
			PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(this.tenantDomain);

			WebappAdmin webAppAdmin = new WebappAdmin();
			webAppAdmin.uploadWebapp(new WebappUploadData[] { getWebAppUploadDataItem(serverApp) });
			
			log.info("Build server app successfully uploaded to tenant space " + this.tenantDomain);

		} finally {
			PrivilegedCarbonContext.endTenantFlow();
		}

	}

	private static WebappUploadData getWebAppUploadDataItem(File fileToDeploy) {
		DataHandler dataHandler = new DataHandler(new FileDataSource(fileToDeploy));

		WebappUploadData webappUploadData = new WebappUploadData();
		webappUploadData.setDataHandler(dataHandler);
		webappUploadData.setFileName(fileToDeploy.getName());
		return webappUploadData;
	}

	@Override
	public void deleteBuildServerApp(File serverApp) throws Exception {
		try {

			PrivilegedCarbonContext.startTenantFlow();
			PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(this.tenantId);
			PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(this.tenantDomain);

			WebappAdmin webAppAdmin = new WebappAdmin();
			webAppAdmin.deleteWebapp(serverApp.getName());
			if(serverApp.exists()) {
				serverApp.delete();
			}
			log.info("Deleting the "+ serverApp.getName());

		} finally {
			PrivilegedCarbonContext.endTenantFlow();
		}
		
	}

}
