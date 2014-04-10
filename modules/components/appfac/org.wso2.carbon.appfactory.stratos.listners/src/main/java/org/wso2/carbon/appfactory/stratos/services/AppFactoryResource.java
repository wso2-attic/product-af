package org.wso2.carbon.appfactory.stratos.services;

public class AppFactoryResource {
	private String resourceName;
	private String resourceContent;
	private String description;
	private String mediaType;
	private boolean isCollection = false;

	public boolean isCollection() {
		return this.isCollection;
	}

	public void setCollection(boolean isCollection) {
		this.isCollection = isCollection;
	}

	private ResourceProperty[] resourceProperties = null;

	private AppFactoryResource[] appFactoryResources = null;

	public AppFactoryResource(String resourceName, String resourceContent) {
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

	public ResourceProperty[] getResourceProperties() {
		return this.resourceProperties;
	}

	public void setResourceProperties(ResourceProperty[] resourceProperties) {
		this.resourceProperties = resourceProperties;
	}

	public AppFactoryResource[] getAppFactoryResources() {
		return this.appFactoryResources;
	}

	public void setAppFactoryResources(AppFactoryResource[] appFactoryResources) {
		this.appFactoryResources = appFactoryResources;
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
}