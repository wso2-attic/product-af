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

package org.wso2.carbon.appfactory.tenant.mgt.beans;

import org.apache.commons.lang.ArrayUtils;
import org.wso2.carbon.appfactory.tenant.mgt.util.AppFactoryTenantMgtUtil;


public class UserInfoBean {
	private String userName;
	private String firstName;
	private String lastName;
	private String email;
	/** Name to be displayed in front end */
	private String displayName;
	private String[] roles;

	public UserInfoBean(String userName, String firstName, String lastName, 
			String email, String displayName,String[] roles) {
		this.userName = userName;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.displayName = displayName;
		String everyOneRoleName = AppFactoryTenantMgtUtil.getRealmService()
				.getBootstrapRealmConfiguration().getEveryOneRoleName();
		if(ArrayUtils.contains(roles,everyOneRoleName)){
			this.roles=(String[]) ArrayUtils.removeElement(roles, everyOneRoleName);
		}else{
			this.roles=roles;

		}
		
 	}

	public String[] getRoles() {
		return roles;
	}

	public void setRoles(String[] roles) {
		this.roles = roles;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
}
