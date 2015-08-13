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

package org.wso2.carbon.appfactory.core.apptype;

import org.apache.axiom.om.OMElement;

import java.util.Properties;

/**
 * Bean class to store application type data
 */
public class ApplicationTypeBean {

	private String applicationTypeName;
	private ApplicationTypeProcessor processor;
	private String displayName;
	private String extension;
	private String description;
	private boolean isBuildable;
	private String buildJobTemplate;
	private String serverDeploymentPath;
	private String enabled;
	private String comment;
	private boolean isUploadableAppType;
	private String launchURLPattern;
	private Properties properties;
    private boolean isAllowDomainMapping;
    private int displayOrder;
	private OMElement jenkinsJobConfig;
	private String[] runtimes;
    private String executionType;
    private boolean isCodeEditorSupported;
    private String IconColorClass;
    private String IconImageClass;
	private String initialDeployerClassName;
	//Fully qualified class name of application deployer.
	private String deployerClassName;
	//Fully qualified class name of application undeployer
	private String undeployerClassName;
	private boolean persistApplicationEndPointMetaData;
	public ApplicationTypeBean(){
	}

	public ApplicationTypeBean(Properties properties){
		this.properties = properties;
	}

	public Object getProperty(String name){
		return this.getProperties().getProperty(name);
	}

	public String getApplicationTypeName() {
		return applicationTypeName;
	}

	public void setApplicationTypeName(String applicationTypeName) {
		this.applicationTypeName = applicationTypeName;
	}

	public ApplicationTypeProcessor getProcessor() {
		return processor;
	}

	public void setProcessor(ApplicationTypeProcessor processor) {
		this.processor = processor;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isBuildable() {
		return isBuildable;
	}

	public void setBuildable(boolean isBuildable) {
		this.isBuildable = isBuildable;
	}

	public String getBuildJobTemplate() {
		return buildJobTemplate;
	}

	public void setBuildJobTemplate(String buildJobTemplate) {
		this.buildJobTemplate = buildJobTemplate;
	}

	public String getServerDeploymentPath() {
		return serverDeploymentPath;
	}

	public void setServerDeploymentPath(String serverDeploymentPath) {
		this.serverDeploymentPath = serverDeploymentPath;
	}

	public String getEnabled() {
		return enabled;
	}

	public void setEnabled(String enabled) {
		this.enabled = enabled;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public boolean isUploadableAppType() {
		return isUploadableAppType;
	}

	public void setIsUploadableAppType(boolean isUploadableAppType) {
		this.isUploadableAppType = isUploadableAppType;
	}

	public String getLaunchURLPattern() {
		return launchURLPattern;
	}

	public void setLaunchURLPattern(String launchURLPattern) {
		this.launchURLPattern = launchURLPattern;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

    public boolean isAllowDomainMapping() {
        return isAllowDomainMapping;
    }

    public void setIsAllowDomainMapping(boolean isAllowDomainMapping) {this.isAllowDomainMapping = isAllowDomainMapping;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

	public void setJenkinsJobConfig(OMElement jenkinsJobConfig) {
		this.jenkinsJobConfig = jenkinsJobConfig;
	}

	public OMElement getJenkinsJobConfig() {
		return jenkinsJobConfig;
	}

	public String[] getRuntimes() {
		return runtimes;
	}

	public void setRuntimes(String[] runtimes) {
		this.runtimes = runtimes;
	}

    public String getExecutionType() {
        return executionType;
    }

    public void setExecutionType(String executionType) {
        this.executionType = executionType;
    }

    public boolean isCodeEditorSupported() {
        return isCodeEditorSupported;
    }

    public void setIsCodeEditorSupported(boolean isCodeEditorSupported) {
        this.isCodeEditorSupported = isCodeEditorSupported;
    }

    public String getIconColorClass() {
        return IconColorClass;
    }

    public void setIconColorClass(String iconColorClass) {
        IconColorClass = iconColorClass;
    }

    public void setIconImageClass(String iconImageClass) {
        IconImageClass = iconImageClass;
    }

    public String getIconImageClass() {
        return IconImageClass;
    }

	public String getInitialDeployerClassName() {
		return initialDeployerClassName;
	}

	public void setInitialDeployerClassName(String initialDeployerClassName) {
		this.initialDeployerClassName = initialDeployerClassName;
	}

	public String getDeployerClassName() {
		return deployerClassName;
	}

	public void setDeployerClassName(String deployerClassName) {
		this.deployerClassName = deployerClassName;
	}

	public String getUndeployerClassName() {
		return undeployerClassName;
	}

	public void setUndeployerClassName(String undeployerClassName) {
		this.undeployerClassName = undeployerClassName;
	}

	public boolean isPersistApplicationEndPointMetaData() {
		return persistApplicationEndPointMetaData;
	}

	public void setPersistApplicationEndPointMetaData(boolean persistApplicationEndPointMetaData) {
		this.persistApplicationEndPointMetaData = persistApplicationEndPointMetaData;
	}
}
