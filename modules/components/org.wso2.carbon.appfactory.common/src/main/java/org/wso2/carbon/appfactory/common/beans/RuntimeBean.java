/*
 *
 *  * Copyright 2014 WSO2, Inc. (http://wso2.com)
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.wso2.carbon.appfactory.common.beans;

/**
 * Bean class to store application runtime data
 */
public class RuntimeBean {

	//property which holds the runtime name
	private String runtimeName;
	//Fully qualified class name of application deployer.
	private String deployerClassName;
	//Defining the artifact storage URL where the application artifacts is stored
	private String paasRepositoryURLPattern;
	//Prefix of the cartridge type
	private String aliasPrefix;
	//Prefix of the cartridge type
	private String cartridgeTypePrefix;
	//Deployment policy is used for subscription
	private String deploymentPolicy;
	//Autoscale policy is used for subscription
	private String autoscalePolicy;
	//repository URL
	private String repoURL;
	//data cartridge type is type of cartridge
	private String dataCartridgeType;
	//prefix of data cartridge type
	private String dataCartridgeAlias;
	//This is used to define whether subscription is required at the time of deployment
	private boolean subscribeOnDeployment;
	//Fully qualified class name of application undeployer
	private String undeployerClassName;

	public String getRuntimeName() {
		return runtimeName;
	}

	public void setRuntimeName(String runtimeName) {
		this.runtimeName = runtimeName;
	}

	public String getDeployerClassName() {
		return deployerClassName;
	}

	public void setDeployerClassName(String deployerClassName) {
		this.deployerClassName = deployerClassName;
	}

	public String getPaasRepositoryURLPattern() {
		return paasRepositoryURLPattern;
	}

	public void setPaasRepositoryURLPattern(String paasRepositoryURLPattern) {
		this.paasRepositoryURLPattern = paasRepositoryURLPattern;
	}

	public String getAliasPrefix() {
		return aliasPrefix;
	}

	public void setAliasPrefix(String aliasPrefix) {
		this.aliasPrefix = aliasPrefix;
	}

	public String getCartridgeTypePrefix() {
		return cartridgeTypePrefix;
	}

	public void setCartridgeTypePrefix(String cartridgeTypePrefix) {
		this.cartridgeTypePrefix = cartridgeTypePrefix;
	}

	public String getDeploymentPolicy() {
		return deploymentPolicy;
	}

	public void setDeploymentPolicy(String deploymentPolicy) {
		this.deploymentPolicy = deploymentPolicy;
	}

	public String getAutoscalePolicy() {
		return autoscalePolicy;
	}

	public void setAutoscalePolicy(String autoscalePolicy) {
		this.autoscalePolicy = autoscalePolicy;
	}

	public String getRepoURL() {
		return repoURL;
	}

	public void setRepoURL(String repoURL) {
		this.repoURL = repoURL;
	}

	public String getDataCartridgeType() {
		return dataCartridgeType;
	}

	public void setDataCartridgeType(String dataCartridgeType) {
		this.dataCartridgeType = dataCartridgeType;
	}

	public String getDataCartridgeAlias() {
		return dataCartridgeAlias;
	}

	public void setDataCartridgeAlias(String dataCartridgeAlias) {
		this.dataCartridgeAlias = dataCartridgeAlias;
	}

	public boolean getSubscribeOnDeployment() {
		return subscribeOnDeployment;
	}

	public void setSubscribeOnDeployment(boolean subscribeOnDeployment) {
		this.subscribeOnDeployment = subscribeOnDeployment;
	}

	public String getUndeployerClassName() {
		return undeployerClassName;
	}

	public void setUndeployerClassName(String undeployerClassName) {
		this.undeployerClassName = undeployerClassName;
	}
}
