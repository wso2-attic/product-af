package org.wso2.carbon.appfactory.stratos.listeners.dto;

import java.util.List;

public class CloudRegistryResource {

	private String resourcePath;
	private boolean governanceRegResource;
	private List<String> actions;
	private List<String> stages;
	private List<String> roles;

	public String getResourcePath() {
		return resourcePath;
	}

	public void setResourcePath(String resourcePath) {
		this.resourcePath = resourcePath;
	}

	public boolean isGovernanceRegResource() {
		return governanceRegResource;
	}

	public void setGovernanceRegResource(boolean governanceRegResource) {
		this.governanceRegResource = governanceRegResource;
	}

	public List<String> getActions() {
		return actions;
	}

	public void setActions(List<String> actions) {
		this.actions = actions;
	}

	public List<String> getStages() {
		return stages;
	}

	public void setStages(List<String> stages) {
		this.stages = stages;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

}
