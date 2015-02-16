/*
 *
 *  Copyright 2014 WSO2, Inc. (http://wso2.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.appfactory.stratos.listeners.dto;

public class RepositoryBean {

	private String repositoryURL;
	private String repositoryAdminUsername;
	private String repositoryAdminPassword;
	private String repositoryType;
	private boolean isCommitEnabled;

	public String getRepositoryURL() {
		return repositoryURL;
	}

	public void setRepositoryURL(String repositoryURL) {
		this.repositoryURL = repositoryURL;
	}

	public String getRepositoryAdminUsername() {
		return repositoryAdminUsername;
	}

	public void setRepositoryAdminUsername(String repositoryAdminUsername) {
		this.repositoryAdminUsername = repositoryAdminUsername;
	}

	public String getRepositoryAdminPassword() {
		return repositoryAdminPassword;
	}

	public void setRepositoryAdminPassword(String repositoryAdminPassword) {
		this.repositoryAdminPassword = repositoryAdminPassword;
	}

	public String getRepositoryType() {
		return repositoryType;
	}

	public void setRepositoryType(String repositoryType) {
		this.repositoryType = repositoryType;
	}

	public boolean isCommitEnabled() {
		return isCommitEnabled;
	}

	public void setCommitEnabled(boolean isCommitEnabled) {
		this.isCommitEnabled = isCommitEnabled;
	}
}
