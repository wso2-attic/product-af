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
