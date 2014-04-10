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
package org.wso2.carbon.appfactory.tenant.build.integration;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.tenant.build.integration.buildserver.BuildServerApp;
import org.wso2.carbon.appfactory.tenant.build.integration.buildserver.JenkinsBuildSeverApp;
import org.wso2.carbon.appfactory.tenant.build.integration.uploder.BuildServerUploader;
import org.wso2.carbon.appfactory.tenant.build.integration.uploder.DirectUploader;
import org.wso2.carbon.appfactory.tenant.build.integration.utils.Util;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * Implementation of {@link BuildServerManagementService}. Class provides
 * implementations to do build server managing activities through a WSO2
 * App server.
 */
public class AppServerBasedBuildServerManager extends AbstractAdmin implements
                                                                   BuildServerManagementService {

	private static Log log = LogFactory.getLog(AppServerBasedBuildServerManager.class);

	@Override
	public void createTenant(String tenantDomain) throws BuildServerManagementException {
		if (log.isDebugEnabled()) {
			log.debug("[Invoked] Tenant creation for build server. tenant - " + tenantDomain);
		}
		try {

			String appPath =
			                 Util.getCarbonResourcesPath() + File.separator +
			                         JenkinsBuildSeverApp.DEFAULT_JENKINS_APP_NAME;
			BuildServerApp serverApp = new JenkinsBuildSeverApp(appPath);

			// modify the app according to tenant.
			String modifiedAppPath = serverApp.getModifiedAppPath(tenantDomain);

			// upload the app to app server
			BuildServerUploader uploader = new DirectUploader(tenantDomain);
			uploader.uploadBuildServerApp(new File(modifiedAppPath));

		} catch (Exception e) {
			String msg = "Error while creating tenant in build server.";
			log.error(msg, e);
			throw new BuildServerManagementException(
			                                         msg,
			                                         e,
			                                         BuildServerManagementException.Code.ERROR_CREATING_TENANT);
		}

	}

	@Override
	public void deleteTenant(String tenantDomain)
			throws BuildServerManagementException {
		if(CarbonContext.getThreadLocalCarbonContext().getTenantId() == MultitenantConstants.SUPER_TENANT_ID){
			if (log.isDebugEnabled()) {
				log.debug("[Invoked] Tenant deletion for build server. tenant - "
						+ tenantDomain);
			}
			String appPath = Util.getCarbonResourcesPath() + File.separator
					+ JenkinsBuildSeverApp.DEFAULT_JENKINS_APP_NAME;
			try {
				BuildServerApp serverApp = new JenkinsBuildSeverApp(appPath);
				File file = serverApp.getFile();
				BuildServerUploader uploader = new DirectUploader(tenantDomain);
				uploader.deleteBuildServerApp(file);
			} catch (Exception e) {
				String msg = "Error while deleting tenant in build server.";
				log.error(msg);
				throw new BuildServerManagementException(msg, e,
						BuildServerManagementException.Code.ERROR_CREATING_TENANT);
			}	
		} else {
			log.warn("Unauthorized request to delete tenant registry data "+tenantDomain);
		}
		
	}

}
