package org.wso2.carbon.appfactory.stratos.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.stratos.listeners.dto.CloudRegistryResource;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

public class ListenerUtils {

	private static final Log log = LogFactory.getLog(ListenerUtils.class);

	/**
	 * Read the configurations and parse
	 * 
	 * @return
	 * @throws AppFactoryException
	 */
	public static List<CloudRegistryResource> getCloudResourcePermissions()
	                                                                       throws AppFactoryException {
		List<CloudRegistryResource> resources = new ArrayList<CloudRegistryResource>();
		AppFactoryConfiguration configurations = AppFactoryS4ListenersUtil.getConfiguration();
		String[] registryPaths =
		                         configurations.getProperties(AppFactoryConstants.CLOUD_RESOURCE_PERMISSION);

		if (registryPaths.length == 0) {
			String msg =
			             AppFactoryConstants.CLOUD_RESOURCE_PERMISSION +
			                     " configuration not defined";
			log.warn(msg);
			return resources;
		}

		for (int i = 0; i < registryPaths.length; i++) {
			CloudRegistryResource resource = new CloudRegistryResource();

			String registryPath = registryPaths[i].trim();
			String govRegPath =
			                    configurations.getProperties(AppFactoryConstants.CLOUD_RESOURCE_PERMISSION +
			                                                 AppFactoryConstants.FULLSTOP +
			                                                 registryPath +
			                                                 AppFactoryConstants.FULLSTOP +
			                                                 AppFactoryConstants.GOVERNANCE_REGISTRY)[0].trim();
			String permission =
			                    configurations.getProperties(AppFactoryConstants.CLOUD_RESOURCE_PERMISSION +
			                                                 AppFactoryConstants.FULLSTOP +
			                                                 registryPath +
			                                                 AppFactoryConstants.FULLSTOP +
			                                                 AppFactoryConstants.PERMISSION)[0].trim();
			// TODO validate against roles defined in appfactory xml
			String stages =
			                configurations.getProperties(AppFactoryConstants.CLOUD_RESOURCE_PERMISSION +
			                                             AppFactoryConstants.FULLSTOP +
			                                             registryPath +
			                                             AppFactoryConstants.FULLSTOP +
			                                             AppFactoryConstants.STAGES)[0].trim();
			// TODO validate against roles defined in appfactory xml
			String roles =
			               configurations.getProperties(AppFactoryConstants.CLOUD_RESOURCE_PERMISSION +
			                                            AppFactoryConstants.FULLSTOP +
			                                            registryPath +
			                                            AppFactoryConstants.FULLSTOP +
			                                            AppFactoryConstants.ROLES)[0].trim();
			try {
				resource.setResourcePath(registryPath);
				resource.setGovernanceRegResource(Boolean.parseBoolean(govRegPath));
				resource.setActions(Arrays.asList(permission.split("\\s*,\\s*"))); // To
				                                                                   // remove
				                                                                   // white
				                                                                   // spaces
				                                                                   // while
				                                                                   // trimming
				resource.setStages(Arrays.asList(stages.split("\\s*,\\s*")));
				resource.setRoles(Arrays.asList(roles.split("\\s*,\\s*")));
			} catch (RuntimeException e) {
				String msg =
				             " Error while parsing CloudResourcePermissions configurations. Please check configurations syntax";
				log.error(msg, e);
				throw new AppFactoryException(msg, e);
			}
			resources.add(resource);
		}
		return resources;
	}

	/**
	 * Aquire the tenant reigstry object
	 * 
	 * @param tenantId
	 * @return
	 * @throws AppFactoryException
	 */
	public static Registry getTenantRegistryObj(int tenantId) throws AppFactoryException {
		try {
			AppFactoryS4ListenersUtil.getTenantRegistryLoader().loadTenantRegistry(tenantId);
			return AppFactoryS4ListenersUtil.getRegistryService()
			                              .getGovernanceSystemRegistry(tenantId);
		} catch (RegistryException e) {
			String msg = "Error while retriving tenant registry for tenant id " + tenantId;
			log.error(msg, e);
			throw new AppFactoryException(msg, e);
		}
	}

	/**
	 * Create the path under /_system/governance
	 * 
	 * @param tenantId
	 * @param resourcePath
	 * @throws AppFactoryException
	 */
	public static void createDependenciesPath(int tenantId, String resourcePath)
	                                                                            throws AppFactoryException {
		Registry tenantRegistry = getTenantRegistryObj(tenantId);

		Resource collection;
		try {
			if (tenantRegistry.resourceExists(resourcePath)) {
				collection = tenantRegistry.get(resourcePath);
			} else {
				collection = tenantRegistry.newCollection();
			}
			tenantRegistry.put(resourcePath, collection);
		} catch (RegistryException e) {
			String msg = "Error while creating registry collection " + resourcePath;
			log.error(msg, e);
			throw new AppFactoryException(msg, e);
		}

	}

	/**
	 * Retrieve the correct action corresponds to the configuration value
	 * 
	 * @param action
	 * @param logMsg
	 * @return
	 * @throws AppFactoryException
	 */
	public static String getActionConstant(String action, String logMsg) throws AppFactoryException {
		if (action.toLowerCase().equals("get")) {
			return ActionConstants.GET;
		} else if (action.toLowerCase().equals("delete")) {
			return ActionConstants.DELETE;
		} else if (action.toLowerCase().equals("put")) {
			return ActionConstants.PUT;
		} else {
			String msg = " Invalid action " + action + logMsg;
			log.error(msg);
			return null;
		}
	}
}
