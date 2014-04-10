package org.wso2.carbon.appfactory.core.registry;

public class AppFacResource {
	private String resourceName;
	private String resourceContent;
	private String description;
	private String mediaType;
	private boolean isCollection = false;

	private ResoProperty[] resoProperties = null;

	private AppFacResource[] appFacResources = null;

	public AppFacResource(String resourceName, String resourceContent) {
		this.resourceName = resourceName;
		this.resourceContent = resourceContent;
	}

	public String getResourceName() {
		return this.resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
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