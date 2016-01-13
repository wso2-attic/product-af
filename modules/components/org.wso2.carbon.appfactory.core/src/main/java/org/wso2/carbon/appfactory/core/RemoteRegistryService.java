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
package org.wso2.carbon.appfactory.core;

import org.wso2.carbon.appfactory.core.dto.Dependency;
import org.wso2.carbon.appfactory.core.registry.AppFacResource;
import org.wso2.carbon.appfactory.common.AppFactoryException;

public interface RemoteRegistryService {

	/**
	 * Add or update resources in registry
	 *
	 * @param serverURL           registry residing server url
	 * @param username            logged user of the session
	 * @param applicationId       application id
	 * @param appFactoryResources resource object
	 * @return boolean
	 * @throws AppFactoryException
	 */
	public abstract boolean addOrUpdateResources(String serverURL, String username, String applicationId,
	                                             AppFacResource[] appFactoryResources) throws AppFactoryException;

	/**
	 * Add or update resource in registry
	 *
	 * @param serverURL           registry residing server url
	 * @param username            logged user of the session
	 * @param applicationId       application id
	 * @param appFactoryResources resource
	 * @return boolean
	 * @throws AppFactoryException
	 */
	public abstract boolean addOrUpdateResource(String serverURL, String username, String applicationId,
	                                            AppFacResource appFactoryResources) throws AppFactoryException;

	/**
	 * Retrieves resource value from a given registry location
	 * 
	 * @param serverURL
	 *            registry residing server url
	 * @param cookie
	 *            session cookie
	 * @param resourcePath
	 *            relative path to the registry resource i.e except
	 *            "/_system/governance"
	 * @return value
	 * @throws AppFactoryException
	 */
	public abstract String getRegistyResourceValue(String serverURL, String cookie,
	                                               String resourcePath) throws AppFactoryException;

    /**
     * Adds resource to a given registry location
     *
     * @param serverURL registry residing server url
     * @param username logged user of the session
     * @param appId application id
     * @param name resource name
     * @param value resource value
     * @param description resource description
     * @param mediaType resource media type
     * @param isCollection whether this is a collection or a property
     * @return boolean
     * @throws AppFactoryException
     */
    public abstract boolean putRegistryProperty(String serverURL, String username,
	                                            String appId, String name, String value,
	                                            String description, String mediaType, boolean isCollection)
	                                                                                 throws AppFactoryException;

	/**
	 * Delete resource from a given registry location
	 *
	 * @param serverURL registry residing server url
	 * @param username logged user of the session
	 * @param appId application id
	 * @param name resource name
	 * @param value resource value
	 * @param description resource description
	 * @param mediaType recource media type
	 * @param isCollection whether this is a collection or a property
	 * @return boolean
	 * @throws AppFactoryException
	 */
	public abstract boolean deleteRegistryResource(String serverURL, String username, String appId, String name,
	                                               String value, String description, String mediaType,
	                                               boolean isCollection) throws AppFactoryException;

	/**
	 * Check if the given resource is exists in the registry
	 *
	 * @param serverURL    registry residing server url
	 * @param username     logged user of the session
	 * @param resourcePath relative path to the registry resource i.e except
	 *                     "/_system/governance"
	 * @return existence as true or false
	 * @throws AppFactoryException
	 */
	public abstract boolean resourceExists(String serverURL, String username, String resourcePath)
	                                                                                            throws AppFactoryException;

	/**
	 * Return all the resources under given resource path
	 * 
	 * @param serverURL
	 *            registry residing server url
	 * @param cookie
	 *            session cookie
	 * @param resourcePath
	 *            relative path to the registry resource i.e except
	 *            "/_system/governance"
	 * @return array of Dependency objects
	 * @throws AppFactoryException
	 */
	public abstract Dependency[] getAllRegistryResources(String serverURL, String cookie,
	                                                     String resourcePath)
	                                                                         throws AppFactoryException;

	/**
	 * Return the resource of given resource path
	 * 
	 * @param serverURL
	 *            registry residing server url
	 * @param cookie
	 *            session cookie
	 * @param resourcePath
	 *            relative path to the registry resource i.e except
	 *            "/_system/governance"
	 * @return array of Dependency objects
	 * @throws AppFactoryException
	 */
	public Dependency getRegistryResource(String serverURL, String cookie, String resourcePath)
	                                                                                           throws AppFactoryException;

	/**
	 * Copies resources exists in source stage to target stage
	 *
	 * @param sourceServerUrl url of the source server
	 * @param sourcePath      registry path in the source registry
	 * @param destServerUrl   Url of target server
	 * @param appId           Application id
	 * @param username        logged user of the session
	 * @throws AppFactoryException
	 */
	public abstract void copyNonExistingResources(String sourceServerUrl, String sourcePath,
	                                              String destServerUrl, String appId, String username)
			throws AppFactoryException;

}