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

package org.wso2.carbon.appfactory.jenkins.build;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.TenantUserEventListner;
import org.wso2.carbon.appfactory.core.dto.UserInfo;

public class JenkinsTenantUserEventListner extends TenantUserEventListner  {

	private Log log=LogFactory.getLog(JenkinsTenantUserEventListner.class);
	@Override
	public int compareTo(TenantUserEventListner arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void onUserDeletion(UserInfo userInfo, String tenantDomain)
			throws AppFactoryException {
		// TODO Auto-generated method stub
//		log.info("******************on user deletion event listner for jenkins is called");
//		  ServiceContainer.getJenkinsCISystemDriver()
//          .removeUsersFromApplication("", new String[]{userInfo.getUserName()}, tenantDomain);
	}

	@Override
	public void onUserRoleAddition(UserInfo userInfo, String tenantDomain)
			throws AppFactoryException {
		// Removed user addition to jenkins from Appfactory 2.2.0 M1
	}

	@Override
	public void onUserUpdate(UserInfo userInfo, String tenantDomain)
			throws AppFactoryException {
		// TODO Auto-generated method stub
//		log.info("******************on user update event listner for jenkins is called");
 		
	}

}
