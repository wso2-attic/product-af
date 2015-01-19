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

package org.wso2.carbon.appfactory.repository.mgt.service;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryManager;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryMgtException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.Date;

public class TenantRepositoryCreationService {
     
    private RepositoryManager repositoryManager;
    private static final Log log = LogFactory.getLog(TenantRepositoryCreationService.class);
    public TenantRepositoryCreationService() {
        this.repositoryManager = new RepositoryManager();
    }
    /**
     * Create the tenant repo in the repository
     * @param tenantId specify the tenant that the repo should be created for
     * @return true based on the success of the operation
     * @throws RepositoryMgtException 
     */
    public Boolean createTenantRepo(String tenantId,String type) throws RepositoryMgtException {
        
        Boolean result=repositoryManager.createTenantRepo(tenantId, type); 
        return result;
        
    }
    
    /**
     * Delete the tenant repo in the repository
     * @param tenantId specify the tenant that the repo should be deleted for
     * @return true based on the success of the operation
     * @throws RepositoryMgtException 
     */
    public Boolean deleteTenantRepo(String tenantId,String type) throws RepositoryMgtException {
		if (CarbonContext.getThreadLocalCarbonContext().getTenantId() == MultitenantConstants.SUPER_TENANT_ID) {
			Boolean result=repositoryManager.deleteTenantRepo(tenantId, type); 
	        return result;
		} else {
			log.warn("Unauthorized request to delete tenant registry data "
					+ tenantId);
			return false;
		}
    }

    /**
     * Create a repository for an application with type{svn,git}
     *
     * @param applicationKey  Application ID
     * @param type Repository type
     * @param tenantDomain Tenant domain of application
     * @return url for created repository
     * @throws RepositoryMgtException
     */
    public String createRepository(String applicationKey, String type)
            throws RepositoryMgtException {
    	String tenantDomain=CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    	if(StringUtils.isBlank(tenantDomain)){
    		String msg = "Tenant domain is empty. Cannot create repository";
    		log.error(msg);
    		throw new RepositoryMgtException(msg);
    	}
        long s = new Date().getTime();
        String ss = repositoryManager.createRepository(applicationKey, type, tenantDomain);
        log.info("Repo Time : " + ((new Date().getTime()) - s));
        return ss;
    }

}
