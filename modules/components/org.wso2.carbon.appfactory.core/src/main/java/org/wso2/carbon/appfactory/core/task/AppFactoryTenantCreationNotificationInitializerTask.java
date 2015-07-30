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
package org.wso2.carbon.appfactory.core.task;

import org.codehaus.jackson.map.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.core.TenantCreationNotificationInitializer;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.ntask.core.Task;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;

import java.io.IOException;
import java.util.Map;

/**
 * Task for initializing App Factory Tenant Storage
 */
public class AppFactoryTenantCreationNotificationInitializerTask implements Task {
    private static final Log log = LogFactory.getLog(AppFactoryTenantCreationNotificationInitializerTask.class);
    public Map<String, String> properties;

    @Override
    public void setProperties(Map<String, String> stringStringMap) {
        this.properties = stringStringMap;
    }

    @Override
    public void init() {
    	if (log.isDebugEnabled()) {
            log.debug("Initializing AppFactoryTenantBuildManagerInitializerTask for " + properties.get
                    (AppFactoryTenantBuildManagerInitializerTask.TENANT_DOMAIN));
        }
    }

    @Override
    public void execute() {
	    String tenantInfoJson = properties.get(AppFactoryConstants.TENANT_INFO);
	    TenantInfoBean tenantInfoBean;
	    ObjectMapper mapper = new ObjectMapper();
	    try {
		    tenantInfoBean = mapper.readValue(tenantInfoJson, TenantInfoBean.class);
	    } catch (IOException e) {
		    String msg = "Can not read the tenant Info Bean";
		    log.error(msg, e);
		    throw new RuntimeException(e);
	    }
	    for (TenantCreationNotificationInitializer initializer : ServiceHolder.getInstance().
                getTenantCreationNotificationInitializerList()) {
            initializer.onTenantCreation(tenantInfoBean);
        }
    }
}
