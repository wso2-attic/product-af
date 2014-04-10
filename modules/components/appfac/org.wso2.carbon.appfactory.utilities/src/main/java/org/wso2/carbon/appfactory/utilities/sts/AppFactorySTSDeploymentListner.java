/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.appfactory.utilities.sts;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.utilities.internal.ServiceReferenceHolder;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.util.AnonymousSessionUtil;
import org.wso2.carbon.security.config.SecurityConfigAdmin;
import org.wso2.carbon.sts.STSDeploymentListener;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.utils.ServerConstants;

public class AppFactorySTSDeploymentListner extends STSDeploymentListener {
	
	private static final Log log = LogFactory.getLog(AppFactorySTSDeploymentListner.class);
	
	@Override
    public void createdConfigurationContext(ConfigurationContext configContext) {
		
        super.createdConfigurationContext(configContext);
        
        try {
			UserRealm realm = AnonymousSessionUtil.getRealmByTenantDomain(ServiceReferenceHolder.getInstance().getRegistryService(),
					ServiceReferenceHolder.getInstance().getRealmService(), CarbonContext.getThreadLocalCarbonContext().getTenantDomain());
			SecurityConfigAdmin admin = new SecurityConfigAdmin(realm, ServiceReferenceHolder.getInstance().getRegistryService().
					getConfigSystemRegistry(), configContext.getAxisConfiguration());
			
			admin.applySecurity(ServerConstants.STS_NAME, 
					AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(AppFactoryConstants.STS_SCENARIO_ID), 
					STSUtil.getPolicyPath(), 
					null, null, 
					AppFactoryUtil.getAppfactoryConfiguration().
					getFirstProperty(AppFactoryConstants.STS_ALLOWED_GROUPS).split(","));
			
		} catch (Exception e) {
			log.error("Failed securing sts service", e);
		}       
    }

}
