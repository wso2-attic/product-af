/*
 * Copyright 2005-2013 WSO2, Inc. (http://wso2.com)
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
 * limitations under the License.
 */

package org.wso2.carbon.appfactory.core.registry;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.RemoteRegistryService;
import org.wso2.carbon.appfactory.core.dto.Dependency;

import org.wso2.carbon.appfactory.stratos.listeners.stub.AppFactoryResourceManagementServiceAppFactoryExceptionException;
import org.wso2.carbon.appfactory.stratos.listeners.stub.AppFactoryResourceManagementServiceStub;
import org.wso2.carbon.appfactory.stratos.services.xsd.AppFactoryResource;
import org.wso2.carbon.appfactory.stratos.services.xsd.ResourceProperty;

import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.rmi.RemoteException;

/**
 * Service implementation to enable CRUD operation to remote registries
 */
public class AppFacRemoteRegistryAccessService implements RemoteRegistryService {

    private static final Log log = LogFactory.getLog(AppFacRemoteRegistryAccessService.class);
    
    @Override
    public boolean addOrUpdateResources(String serverURL, String cookie, String applicationId, AppFacResource[] appFactoryResources) throws AppFactoryException{
    	if (serverURL == null) {
            throw new AppFactoryException("ServerURL can not be null.");
        }
        if (!serverURL.endsWith("/")) {
            serverURL = serverURL + "/";
        }
        serverURL = serverURL + "AppFactoryResourceManagementService";
        AppFactoryResourceManagementServiceStub stub = null;
        try {
            stub = new AppFactoryResourceManagementServiceStub(serverURL);
            stub._getServiceClient().getOptions().setManageSession(true);
            stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
            
            stub.addOrUpdateResources(applicationId, convertResourceObjects(appFactoryResources));
            
            return true;
        } catch (RemoteException e) {
            String message =
                    "Error occurred while adding registry resources to server:" + serverURL;
            log.error(message, e);
            throw new AppFactoryException(message, e);
        } catch (AppFactoryResourceManagementServiceAppFactoryExceptionException e) {
            String message =
                    "Error occurred while adding registry resources to server:" + serverURL;
            log.error(message, e);
        } finally {
            if (stub != null) {
                try {
                    stub._getServiceClient().cleanupTransport();
                    stub.cleanup();
                } catch (AxisFault axisFault) {
                    // ignore exception, just log and move on
                    log.warn("Failed to cleanup stub.", axisFault);
                }
            }
        }
        return false;
    }
 
    @Override
    public boolean addOrUpdateResource(String serverURL, String cookie, String applicationId, AppFacResource appFactoryResource) throws AppFactoryException{
    	if (serverURL == null) {
            throw new AppFactoryException("ServerURL can not be null.");
        }
        if (!serverURL.endsWith("/")) {
            serverURL = serverURL + "/";
        }
        serverURL = serverURL + "AppFactoryResourceManagementService";
        AppFactoryResourceManagementServiceStub stub = null;
        try {
            stub = new AppFactoryResourceManagementServiceStub(serverURL);
            stub._getServiceClient().getOptions().setManageSession(true);
            stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
            
            stub.addOrUpdateResource(applicationId, convertResourceObject(appFactoryResource));
            
            return true;
        } catch (RemoteException e) {
            String message =
                    "Error occurred while adding registry resource :" + appFactoryResource.getResourceName() + " to server:" + serverURL;
            log.error(message, e);
            throw new AppFactoryException(message, e);
        } catch (AppFactoryResourceManagementServiceAppFactoryExceptionException e) {
            String message =
                    "Error occurred while adding registry property:" + appFactoryResource.getResourceName() + " to server:" + serverURL;
            log.error(message, e);
        } finally {
            if (stub != null) {
                try {
                    stub._getServiceClient().cleanupTransport();
                    stub.cleanup();
                } catch (AxisFault axisFault) {
                    // ignore exception, just log and move on
                    log.warn("Failed to cleanup stub.", axisFault);
                }
            }
        }
        return false;
    }
    
    
   
    public boolean putRegistryCollection(String serverURL, String cookie, String resourcePath,
                                         String description, String mediaType)
            throws AppFactoryException {
        try {
            WSRegistryServiceClient wsclient = new WSRegistryServiceClient(serverURL, cookie);
            String absolutePath = getDependenciesPath(resourcePath);
            Collection collection = null;

            if (wsclient.resourceExists(absolutePath)) {
                throw new AppFactoryException("Cannot create collection a resource already exists");
            } else {
                collection = wsclient.newCollection();
            }
            collection.setMediaType(mediaType);
            collection.setDescription(description);
            wsclient.put(absolutePath, collection);
            return true;
        } catch (RegistryException e) {
            String message =
                    "Error occured while adding dependency value to " + resourcePath +
                            " from registry " + serverURL;
            log.error(message, e);
            throw new AppFactoryException(message, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateRegistryResourceDescription(String serverURL, String cookie,
                                                     String resourcePath, String description)
            throws AppFactoryException {
        try {
            WSRegistryServiceClient wsclient = new WSRegistryServiceClient(serverURL, cookie);
            String absoluteResourcePath = getDependenciesPath(resourcePath);
            Resource resource = getResourceObject(absoluteResourcePath, wsclient);
            resource.setDescription(description);
            wsclient.put(absoluteResourcePath, resource);
            return true;
        } catch (RegistryException e) {
            String message =
                    "Error occured while updating dependency description to " +
                            resourcePath + " from registry " + serverURL;
            log.error(message, e);
            throw new AppFactoryException(message, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean putRegistryResource(String serverURL, String cookie, String resourcePath,
                                       String value, String description, String mediaType)
            throws AppFactoryException {
        try {
            WSRegistryServiceClient wsclient = new WSRegistryServiceClient(serverURL, cookie);
            String absoluteResourcePath = getDependenciesPath(resourcePath);
            Resource resource = getResourceObject(absoluteResourcePath, wsclient);
            resource.setContent(value);
            if (description!=null && !description.isEmpty()) {
                resource.setDescription(description);
            }
            if (mediaType!=null && !mediaType.isEmpty()) {
                resource.setMediaType(mediaType);
            }
            wsclient.put(absoluteResourcePath, resource);
            return true;
        } catch (RegistryException e) {
            String message =
                    "Error occured while adding dependency value to " + resourcePath +
                            " from registry " + serverURL;
            log.error(message, e);
            throw new AppFactoryException(message, e);
        }
    }

    @Override
    public boolean putRegistryProperty(String serverURL, String cookie, String appId, String name, String value, String description, String mediaType, boolean isCollection) throws AppFactoryException {
        if (serverURL == null) {
            throw new AppFactoryException("ServerURL can not be null.");
        }
        if (!serverURL.endsWith("/")) {
            serverURL = serverURL + "/";
        }
        serverURL = serverURL + "AppFactoryResourceManagementService";
        AppFactoryResourceManagementServiceStub stub = null;
        try {
            stub = new AppFactoryResourceManagementServiceStub(serverURL);
            stub._getServiceClient().getOptions().setManageSession(true);
            stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
            //stub.addProperty(appId, name, value, description, mediaType, isCollection);
            return true;
        } catch (RemoteException e) {
            String message =
                    "Error occurred while adding registry property:" + name + " to server:" + serverURL;
            log.error(message, e);
            throw new AppFactoryException(message, e);
        /*} catch (AppFactoryResourceManagementServiceAppFactoryExceptionException e) {
            String message =
                    "Error occurred while adding registry property:" + name + " to server:" + serverURL;
            log.error(message, e);
        */}finally {
            if (stub != null) {
                try {
                    stub._getServiceClient().cleanupTransport();
                    stub.cleanup();
                } catch (AxisFault axisFault) {
                    // ignore exception, just log and move on
                    log.warn("Failed to cleanup stub.", axisFault);
                }
            }
        }
        //return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteRegistryResource(String serverURL, String cookie, String resourcePath)
            throws AppFactoryException {
        try {
            WSRegistryServiceClient wsclient = new WSRegistryServiceClient(serverURL, cookie);
            String absoluteResourcePath = getDependenciesPath(resourcePath);
            if (wsclient.resourceExists(absoluteResourcePath)) {
                wsclient.delete(absoluteResourcePath);
            }
            return true;
        } catch (RegistryException e) {
            String message =
                    "Error occured while deleting dependency value from " + resourcePath +
                            " from registry " + serverURL;
            log.error(message, e);
            throw new AppFactoryException(message, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean resourceExists(String serverURL, String cookie, String resourcePath)
            throws AppFactoryException {
        try {
            WSRegistryServiceClient wsclient = new WSRegistryServiceClient(serverURL, cookie);
            return wsclient.resourceExists(getDependenciesPath(resourcePath));
        } catch (RegistryException e) {
            String message =
                    "Error occured while check the existance of the path " + resourcePath +
                            " from registry " + serverURL;
            log.error(message, e);
            throw new AppFactoryException(message, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRegistyResourceValue(String serverURL, String cookie, String resourcePath)
            throws AppFactoryException {
        String value = null;
        try {
            WSRegistryServiceClient wsclient = new WSRegistryServiceClient(serverURL, cookie);
            String absoluteResourcePath = getDependenciesPath(resourcePath);
            if (wsclient.resourceExists(absoluteResourcePath)) {
                Resource resource = wsclient.get(absoluteResourcePath);
                value = getResourceContent(resource);
            }
            return value;
        } catch (RegistryException e) {
            String message =
                    "Error occured while retriving dependency value from " + resourcePath +
                            " from registry " + serverURL;
            log.error(message, e);
            throw new AppFactoryException(message, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dependency[] getAllRegistryResources(String serverURL, String cookie, String resourcePath)
            throws AppFactoryException {
        Registry governanceRegistry;
        Dependency[] dependencies = new Dependency[0];

        try {
            WSRegistryServiceClient wsclient = new WSRegistryServiceClient(serverURL, cookie);
            String dependencyCollectionPath = getDependenciesPath(resourcePath);

            if (wsclient.resourceExists(dependencyCollectionPath)) {
                Resource dependencyParent = wsclient.get(dependencyCollectionPath);

                if (dependencyParent instanceof Collection) {
                    Collection collection = (Collection) dependencyParent;
                    String[] children = collection.getChildren();

                    if (children == null) {
                        log.warn("No resources were found as dependencies");
                        return dependencies;
                    }

                    dependencies = new Dependency[children.length];

                    for (int i = 0; i < children.length; i++) {
                        String childPath = children[i];
                        Resource child = wsclient.get(childPath);

                        Dependency element = new Dependency();
                        element.setName(RegistryUtils.getResourceName(child.getPath()));
                        element.setDescription(child.getDescription());
                        element.setValue(getResourceContent(child));
                        element.setMediaType(child.getMediaType());

                        dependencies[i] = element;
                    }
                } else {
                    log.warn("No resources were found as dependencies");
                }
            }

        } catch (RegistryException e) {
            String msg = "Unable to get the dependencies from registry";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        return dependencies;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dependency getRegistryResource(String serverURL, String cookie, String resourcePath)
            throws AppFactoryException {
        Registry governanceRegistry;
        Dependency dependency = null;

        try {
            WSRegistryServiceClient wsclient = new WSRegistryServiceClient(serverURL, cookie);
            String dependencyPath = getDependenciesPath(resourcePath);

            if (wsclient.resourceExists(dependencyPath)) {
                Resource resource = wsclient.get(dependencyPath);

                dependency = new Dependency();
                dependency.setName(RegistryUtils.getResourceName(resource.getPath()));
                dependency.setDescription(resource.getDescription());
                dependency.setValue(getResourceContent(resource));
                dependency.setMediaType(resource.getMediaType());

            } else {
                log.warn("No resource was found");
            }
            return dependency;

        } catch (RegistryException e) {
            String msg = "Unable to get the dependency from registry";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void copyNonExistingResources(String sourceServerUrl, String sourcePath, String sourceServerCookie,
                                         String destServerUrl, String destPath, String destServerCookie, String appId) throws AppFactoryException {
        log.debug("Copying non existing resources of application : " + appId +
                  " from : " + sourceServerUrl + "to : " + destServerUrl);
        try {
            WSRegistryServiceClient sourceWsClient = new WSRegistryServiceClient(sourceServerUrl, sourceServerCookie);
            WSRegistryServiceClient destWsClient = new WSRegistryServiceClient(destServerUrl, destServerCookie);
            
            if (sourceWsClient.resourceExists(sourcePath)) {
                String[] sourceResourcesPaths = sourceWsClient.getCollectionContent(sourcePath);
                
                for (String path : sourceResourcesPaths) { 
                    String propertyName = path.substring(path.lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1); 
                    if (!destWsClient.resourceExists(path)) {
                        Resource resource = sourceWsClient.get(path);
                        if(resource instanceof org.wso2.carbon.registry.api.Collection){
                        	putRegistryProperty(destServerUrl, destServerCookie, appId, propertyName, getResourceContent(resource), resource.getDescription(), resource.getMediaType() ,true);
                        	String[] childs = ((org.wso2.carbon.registry.api.Collection) resource).getChildren();
                        	for (int i = 0; i < childs.length; i++) {
                        		String[] elms = childs[i].split("/");
                        		propertyName = elms[elms.length-2] + "/" + elms[elms.length-1];
                        		resource = sourceWsClient.get(childs[i]); 
                        		putRegistryProperty(destServerUrl, destServerCookie, appId, propertyName, getResourceContent(resource), resource.getDescription(), resource.getMediaType(),false);
                            }
                        }else{
                        	putRegistryProperty(destServerUrl, destServerCookie, appId, propertyName, getResourceContent(resource), resource.getDescription(), resource.getMediaType() ,false);
                        }
                    }
                }
            }

        } catch (RegistryException e) {
            String msg = "Unable to get the dependency from registry";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (Exception e) {
            String msg = "Unable to get the dependency from registry";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }


    }

    /**
     * Get resource content based on content type
     *
     * @param resource
     * @return
     * @throws AppFactoryException
     */
    private String getResourceContent(Resource resource) throws AppFactoryException {
        try {
            if (resource.getContent() != null) {
                if (resource.getContent() instanceof String) {
                    return (String) resource.getContent();
                } else if (resource.getContent() instanceof byte[]) {
                    return new String((byte[]) resource.getContent());
                }
            }
        } catch (RegistryException e) {
            String msg = "Unable to read the resource content";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        return null;
    }

    /**
     * Return full path to resource
     *
     * @param resourcePath relative resource path
     * @return
     */
    private String getDependenciesPath(String resourcePath) {

        return RegistryUtils.getAbsolutePathToOriginal(resourcePath,
                RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);
    }

    /**
     * Check if the given path exists in registry and if so return that
     * instance, else return a new instance
     *
     * @param wsclient     WSRegistryServiceClient instance
     * @return Resource instance
     * @throws RegistryException
     */
    private Resource getResourceObject(String absolutePathToOriginal,
                                       WSRegistryServiceClient wsclient) throws RegistryException {
        Resource prop;

        if (wsclient.resourceExists(absolutePathToOriginal)) {
            prop = wsclient.get(absolutePathToOriginal);
        } else {
            prop = wsclient.newResource();
        }
        return prop;
    }
    
    private AppFactoryResource[] convertResourceObjects(AppFacResource[] appFacResource){
    	AppFactoryResource[] appFactoryResources = new AppFactoryResource[appFacResource.length];
    	for (int i = 0; i < appFactoryResources.length; i++) {
    		AppFactoryResource appFactoryResource = convertResourceObject(appFacResource[i]);
    		appFactoryResources[i] = appFactoryResource ;
        }
    	return appFactoryResources ;
    }
    private AppFactoryResource convertResourceObject( AppFacResource appFacResource){
    	AppFactoryResource appFactoryResource = new AppFactoryResource();
    	appFactoryResource.setResourceName(appFacResource.getResourceName());
    	appFactoryResource.setResourceContent(appFacResource.getResourceContent());
    	appFactoryResource.setMediaType(appFacResource.getMediaType());
    	appFactoryResource.setDescription(appFacResource.getDescription());
    	appFactoryResource.setCollection(appFacResource.isCollection());
    	
    	if(appFacResource.getResoProperties()!=null){
    		ResourceProperty[] resourceProperties = new  ResourceProperty[appFacResource.getResoProperties().length];
        	ResoProperty[] resoProperties = appFacResource.getResoProperties();
        	int a = 0 ;
        	
        	for (ResoProperty resoProperty : resoProperties) {
        		ResourceProperty resourceProperty = new ResourceProperty();
        		resourceProperty.setPropertyName(resoProperty.getPropertyName());
        		resourceProperty.setPropertyValue(resoProperty.getPropertyValue());
        		resourceProperties[a] = resourceProperty ;
        		a++ ;
            }
        	appFactoryResource.setResourceProperties(resourceProperties);
        	
    	}
    	
    	if(appFacResource.getAppFacResources()!=null){
    		AppFactoryResource[] appFactoryResources = new AppFactoryResource[appFacResource.getAppFacResources().length];
    		AppFacResource[] appFacRes = appFacResource.getAppFacResources();
    		int b = 0 ;
    		for (AppFacResource appFacResource2 : appFacRes) {
    			AppFactoryResource appFactoryResource2 = convertResourceObject(appFacResource2);
    			appFactoryResources[b] = appFactoryResource2 ;
            }
    		appFactoryResource.setAppFactoryResources(appFactoryResources);
    	}
    	return appFactoryResource ;
    	
    }

}
	



