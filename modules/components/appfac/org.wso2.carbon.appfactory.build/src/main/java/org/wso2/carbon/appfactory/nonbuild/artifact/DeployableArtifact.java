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

package org.wso2.carbon.appfactory.nonbuild.artifact;

import org.wso2.carbon.appfactory.common.AppFactoryException;

public abstract class DeployableArtifact {

	private String rootPath;
	private String applicationId;
	private String version;
	private String stage;

	public DeployableArtifact(String rootPath, String applicationId, String version, String stage) {
		this.rootPath = rootPath;
		this.applicationId = applicationId;
		this.version = version;
		this.stage = stage;
	}

	public String getRootPath() {
		return rootPath;
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getStage() {
		return stage;
	}

	public void setStage(String stage) {
		this.stage = stage;
	}


	/**
	 * This is generate deployable file according to the artifact type.
	 * 
	 * @throws AppFactoryException
	 */
	public abstract void generateDeployableFile() throws AppFactoryException;
}
