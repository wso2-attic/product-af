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

package org.wso2.carbon.appfactory.stratos.services;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.stratos.util.AppFactoryS4ListenersUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * <p>
 * Takes care of the adding registry resources ( i.e. Properties as defined in Appfactory) a given cloud. This service
 * is meant to be run on Stratos Controller.
 * </p>
 * 
 * <p>
 * Rational of having this service hosted on the Stratos Controller ( as oppose to use relevant Carbon Admin services to
 * achieve end objective) is to tighten the security around resource management.
 * </p>
 * 
 * <p>
 * To have the resource management done via WS-API (of Registry) means the users/roles have to be provisioned with wider
 * security permissions, which in-turn results a larger security hole. With the introduction of this service AF can add
 * properties in a secure manner ( since having a more restrictive AF related permission assigned to roles/users)
 * </p>
 */
public class AppFactoryResourceManagementService {

	private static Log log = LogFactory.getLog(AppFactoryResourceManagementService.class);

	private static final String allowedRegistryPath = "dependencies";

	/**
	 * Deleting resource for given resource in appFactoryResource object
	 * 
	 * @param applicationId
	 * @param appFactoryResource
	 * @throws AppFactoryException
	 */
	public void deleteResource(String applicationId, AppFactoryResource appFactoryResource) throws AppFactoryException {
		Registry registry = getRegistry(applicationId);
		String path =
		              RegistryConstants.PATH_SEPARATOR + allowedRegistryPath + RegistryConstants.PATH_SEPARATOR +
		                      applicationId + RegistryConstants.PATH_SEPARATOR + appFactoryResource.getResourcePath();
		try {
			if (registry.resourceExists(path)) {
				registry.delete(path);
			}
		} catch (RegistryException e) {
			String errorMsg =
			                  "Resource:" + appFactoryResource.getResourcePath() + " for application:" + applicationId +
			                          " already exists.";
			log.error(errorMsg);
			throw new AppFactoryException(errorMsg);
		}
	}

	/**
	 * 
	 * Adding resource as a single resource, collection or resources under collection with properties, description,
	 * media type.
	 * 
	 * @param applicationId
	 * @param appFactoryResource
	 * @throws AppFactoryException
	 */
	public void addResource(String applicationId, AppFactoryResource appFactoryResource) throws AppFactoryException {
		addOrUpdateResource(applicationId, appFactoryResource, false);
	}

	/**
	 * Updating resource as a single resource, collection or resources under collection with properties, description,
	 * media type.
	 * 
	 * @param applicationId
	 * @param appFactoryResource
	 * @throws AppFactoryException
	 */
	public void updateResource(String applicationId, AppFactoryResource appFactoryResource) throws AppFactoryException {
		addOrUpdateResource(applicationId, appFactoryResource, true);
	}

	/**
	 * 
	 * @param applicationId
	 * @param appFactoryResource
	 * @throws AppFactoryException
	 */
	public void addOrUpdateResource(String applicationId, AppFactoryResource appFactoryResource)
	                                                                                            throws AppFactoryException {
		addOrUpdateResource(applicationId, appFactoryResource, true);
	}

	/**
	 * 
	 * @param applicationId
	 * @param appFactoryResource
	 * @throws AppFactoryException
	 */
	public void addResources(String applicationId, AppFactoryResource[] appFactoryResource) throws AppFactoryException {
		for (AppFactoryResource appFactoryResourceTmp : appFactoryResource) {
			addResource(applicationId, appFactoryResourceTmp);
		}
	}

	/**
	 * 
	 * @param applicationId
	 * @param appFactoryResource
	 * @throws AppFactoryException
	 */
	public void updateResources(String applicationId, AppFactoryResource[] appFactoryResource)
	                                                                                          throws AppFactoryException {
		for (AppFactoryResource appFactoryResourceTmp : appFactoryResource) {
			updateResource(applicationId, appFactoryResourceTmp);
		}
	}

	/**
	 * Add or Update multiple resources
	 * 
	 * @param applicationId
	 * @param appFactoryResource
	 * @throws AppFactoryException
	 */
	public void addOrUpdateResources(String applicationId, AppFactoryResource[] appFactoryResource)
	                                                                                               throws AppFactoryException {
		for (AppFactoryResource appFactoryResourceTmp : appFactoryResource) {
			addOrUpdateResource(applicationId, appFactoryResourceTmp, true);
		}

	}
	
	/**
	 * General method for update add and delete operation.
	 * 
	 * @param applicationId
	 * @param appFactoryResource
	 * @param isUpdate
	 * @throws AppFactoryException
	 */
	private void addOrUpdateResource(String applicationId, AppFactoryResource appFactoryResource, boolean isUpdate)
	                                                                                                               throws AppFactoryException {
		try {

			Registry registry = getRegistry(applicationId);

			if (!isBasePathExist(applicationId)) {
				String path =
				              RegistryConstants.PATH_SEPARATOR + allowedRegistryPath +
				                      RegistryConstants.PATH_SEPARATOR + applicationId;
				Resource resource = registry.newCollection();
				registry.put(path, resource);
			}

			String path =
			              RegistryConstants.PATH_SEPARATOR + allowedRegistryPath + RegistryConstants.PATH_SEPARATOR +
			                      applicationId + RegistryConstants.PATH_SEPARATOR +
			                      appFactoryResource.getResourcePath();

			Resource resource = null;
			if (registry.resourceExists(path)) {
				if (isUpdate) {
					resource = registry.get(path);
					if(!appFactoryResource.isCollection()){
						if (appFactoryResource.getResourceContent() != null && !appFactoryResource.getResourceContent().isEmpty()) {
							resource.setContent(appFactoryResource.getResourceContent());
						}
					} else {
					String errorMsg =
					                  "Resource:" + appFactoryResource.getResourcePath() + " for application:" +
					                          applicationId + " already exists.";
					log.error(errorMsg);
					throw new AppFactoryException(errorMsg);
					}
				}
			} else {
				if (appFactoryResource.isCollection()) {
					resource = registry.newCollection();
				} else {
					resource = registry.newResource();
					resource.setContent(appFactoryResource.getResourceContent());
				}
			}

			if (appFactoryResource.getDescription() != null && !appFactoryResource.getDescription().isEmpty()) {
				resource.setDescription(appFactoryResource.getDescription());
			}
			if (appFactoryResource.getMediaType() != null && !appFactoryResource.getMediaType().isEmpty()) {
				resource.setMediaType(appFactoryResource.getMediaType());
			}

			if (appFactoryResource.getResourceProperties() != null &&
			    appFactoryResource.getResourceProperties().length != 0) {
				List<ResourceProperty> resourceProperties = Arrays.asList(appFactoryResource.getResourceProperties());
				for (ResourceProperty resourceProperty : resourceProperties) {
					resource.addProperty(resourceProperty.getPropertyName(), resourceProperty.getPropertyValue());
				}
			}
			registry.put(path, resource);
			if (appFactoryResource.getAppFactoryResources() != null &&
			    appFactoryResource.getAppFactoryResources().length > 0 && appFactoryResource.isCollection()) {
				List<AppFactoryResource> appFactoryResources =
				                                               Arrays.asList(appFactoryResource.getAppFactoryResources());
				for (AppFactoryResource appFacRes : appFactoryResources) {
					addResource(applicationId, appFacRes);
				}
			}

			}catch (Exception e) {
			String errorMsg =
			                  "Failed to add resource:" + appFactoryResource.getResourcePath() + " for application:" +
			                          applicationId;
			log.error(errorMsg, e);
			throw new AppFactoryException(errorMsg, e);

		}

	}

	/**
	 * 
	 * 
	 * 
	 * @param applicationId
	 * @param appFactoryResource
	 * @return
	 * @throws AppFactoryException
	 */
	private Registry getRegistry(String applicationId) throws AppFactoryException {
		Registry registry = null;
		if (isAccessAllowed()) {
			authorizeAppIdRole(applicationId);

			int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
			try {

				registry = AppFactoryS4ListenersUtil.getRegistryService().getGovernanceSystemRegistry(tenantId);

			} catch (Exception e) {
				String errorMsg = "Failed to get registry object for given tenant " + tenantId;
				log.error(errorMsg, e);
				throw new AppFactoryException(errorMsg, e);

			}
		} else {

			String errorMsg = "User is not authorized to add resource to application:" + applicationId;
			log.error(errorMsg);
			throw new AppFactoryException(errorMsg);

		}
		return registry;
	}

	private boolean isBasePathExist(String applicationId) throws RegistryException, AppFactoryException {
		Registry registry = getRegistry(applicationId);
		return registry.resourceExists(RegistryConstants.PATH_SEPARATOR + allowedRegistryPath +
		                               RegistryConstants.PATH_SEPARATOR + applicationId);
	}

	/**
	 * Return true if the 'This' (- currently logged in user) meets following
	 * criteria:
	 * <ul>
	 * <li>The User belongs to the team of this application.
	 * <li>
	 * <li>He/she (rather the role has) should have the permission to create <b>OR</b> update resources in this stage.</li>
	 * </ul>
	 * 
	 * @return true of the above criteria is met, false otherwise.
	 * @throws AppFactoryException
	 *             an error
	 */
	private boolean isAccessAllowed() throws AppFactoryException {
		String currentCloudStage = System.getProperty(AppFactoryConstants.CLOUD_STAGE);
		if (currentCloudStage == null || currentCloudStage.isEmpty()) {
			log.error(AppFactoryConstants.CLOUD_STAGE + " system variable is not set.");
			throw new AppFactoryException(AppFactoryConstants.CLOUD_STAGE + " system variable is not set.");
		}
		String currentUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
		if (currentUser == null) {
			return false;
		}

		try {
			AuthorizationManager authorizationManager =
			                                            AppFactoryS4ListenersUtil.getRealmService()
			                                                                     .getTenantUserRealm(CarbonContext.getThreadLocalCarbonContext()
			                                                                                                      .getTenantId())
			                                                                     .getAuthorizationManager();
			boolean isApplicationUser =
			                            authorizationManager.isUserAuthorized(currentUser,
			                                                                  AppFactoryConstants.PER_APP_ROLE_PERMISSION,
			                                                                  CarbonConstants.UI_PERMISSION_ACTION);
			boolean hasAddPropertyPermission =
			                                   authorizationManager.isUserAuthorized(currentUser,
			                                                                         AppFactoryConstants.PERMISSION_RESOURCE_CREATE +
			                                                                                 currentCloudStage,
			                                                                         CarbonConstants.UI_PERMISSION_ACTION);
			boolean hasUpdatePropertyPermission =
			                                      authorizationManager.isUserAuthorized(currentUser,
			                                                                            AppFactoryConstants.PERMISSION_RESOURCE_UPDATE_IN +
			                                                                                    currentCloudStage,
			                                                                            CarbonConstants.UI_PERMISSION_ACTION);

			return isApplicationUser && (hasAddPropertyPermission || hasUpdatePropertyPermission);
		} catch (UserStoreException e) {
			String errorMsg = "Error occurred while getting authorization manager.";
			log.error(errorMsg, e);
			throw new AppFactoryException(errorMsg, e);
		}
	}

	/**
	 * Adds role to this Cloud stage with permission to add/retrieve/delete
	 * resources in '_system/governance/dependencies/{appId}'.
	 * 
	 * @param appId
	 *            The application ID
	 * @throws AppFactoryException
	 *             an error
	 */
	private void authorizeAppIdRole(String appId) throws AppFactoryException {
		try {
			AuthorizationManager authorizationManager =
			                                            AppFactoryS4ListenersUtil.getRealmService()
			                                                                     .getTenantUserRealm(CarbonContext.getThreadLocalCarbonContext()
			                                                                                                      .getTenantId())
			                                                                     .getAuthorizationManager();
			String applicationRole = AppFactoryUtil.getRoleNameForApplication(appId);

			String baseResourcePath = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + "/dependencies/" + appId;
			if (!authorizationManager.isRoleAuthorized(applicationRole, baseResourcePath, ActionConstants.GET)) {
				authorizationManager.authorizeRole(applicationRole, baseResourcePath, ActionConstants.GET);
			}
			if (!authorizationManager.isRoleAuthorized(applicationRole, baseResourcePath, ActionConstants.PUT)) {
				authorizationManager.authorizeRole(applicationRole, baseResourcePath, ActionConstants.PUT);
			}
			if (!authorizationManager.isRoleAuthorized(applicationRole, baseResourcePath, ActionConstants.DELETE)) {
				authorizationManager.authorizeRole(applicationRole, baseResourcePath, ActionConstants.DELETE);
			}
		} catch (UserStoreException e) {
			String errorMsg = "Error occurred while getting authorization manager.";
			log.error(errorMsg, e);
			throw new AppFactoryException(errorMsg, e);
		}
	}

}
