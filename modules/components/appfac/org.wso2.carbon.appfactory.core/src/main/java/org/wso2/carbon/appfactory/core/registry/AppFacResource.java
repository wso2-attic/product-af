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

package org.wso2.carbon.appfactory.core.registry;

public class AppFacResource {
	private String resourcePath;
	private String resourceContent;
	private String description;
	private String mediaType;
	private boolean isCollection = false;

	private ResoProperty[] resoProperties = null;

	private AppFacResource[] appFacResources = null;

	public AppFacResource(String resourcePath, String resourceContent) {
		this.resourcePath = resourcePath;
		this.resourceContent = resourceContent;
	}

	public String getResourcePath() {
		return this.resourcePath;
	}

	public void setResourcePath(String resourceName) {
		this.resourcePath = resourceName;
	}

	public String getResourceContent() {
		return this.resourceContent;
	}

	public void setResourceContent(String resourceContent) {
		this.resourceContent = resourceContent;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getMediaType() {
		return this.mediaType;
	}

	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}

	public boolean isCollection() {
		return this.isCollection;
	}

	public void setCollection(boolean isCollection) {
		this.isCollection = isCollection;
	}

	public ResoProperty[] getResoProperties() {
		return this.resoProperties;
	}

	public void setResoProperties(ResoProperty[] resoProperties) {
		this.resoProperties = resoProperties;
	}

	public AppFacResource[] getAppFacResources() {
		return this.appFacResources;
	}

	public void setAppFacResources(AppFacResource[] appFacResources) {
		this.appFacResources = appFacResources;
	}
}