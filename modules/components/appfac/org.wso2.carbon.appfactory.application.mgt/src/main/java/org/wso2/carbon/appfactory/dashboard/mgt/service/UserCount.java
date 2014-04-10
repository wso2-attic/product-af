/*
 * Copyright 2005-2012 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */

package org.wso2.carbon.appfactory.dashboard.mgt.service;

import org.wso2.carbon.appfactory.application.mgt.service.UserInfoBean;

/**
 * This class holds the total count of particular user playing the same role in
 * all the applications
 *
 */
public class UserCount {
	// Holds reference to UserInfoBean instance
	private UserInfoBean userInfoBean;

	// Holds the role name
	private String role;

	// Holds the total count where this user has been assigned to this
	// particular role across all the projects
	private int count;

	public UserInfoBean getUserInfoBean() {
		return userInfoBean;
	}

	public void setUserInfoBean(UserInfoBean userInfoBean) {
		this.userInfoBean = userInfoBean;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

}
