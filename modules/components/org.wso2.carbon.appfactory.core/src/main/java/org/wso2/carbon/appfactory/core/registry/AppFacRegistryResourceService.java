/*
*  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.appfactory.core.registry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeBean;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeManager;
import org.wso2.carbon.appfactory.core.dto.Dependency;
import org.wso2.carbon.appfactory.core.governance.dao.RxtApplicationDAO;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.appfactory.core.runtime.RuntimeManager;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.api.GhostResource;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.caching.RegistryCacheKey;
import org.wso2.carbon.registry.core.config.DataBaseConfiguration;
import org.wso2.carbon.registry.core.config.Mount;
import org.wso2.carbon.registry.core.config.RemoteConfiguration;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import javax.cache.Cache;
import java.util.ArrayList;
import java.util.List;

/**
 * This is an OSGi service that enables CRUD operations to work with mounted Startos manager registries to App Factory
 */
public class AppFacRegistryResourceService {

    private static final Log log = LogFactory.getLog(AppFacRegistryResourceService.class);

    /**
     * Return all the resources under given resource path
     *
     * @param resourcePath resource path
     * @return array of Dependency objects
     * @throws AppFactoryException
     */
    public Dependency[] getAllResources(String resourcePath) throws AppFactoryException {
        List<Dependency> dependencyList = new ArrayList<Dependency>();

        try {
            UserRegistry registry = getRegistry();

            if (registry.resourceExists(resourcePath)) {
                Resource dependencyParent = registry.get(resourcePath);

                if (dependencyParent instanceof Collection) {
                    Collection collection = (Collection) dependencyParent;
                    String[] children = collection.getChildren();

                    if (children == null) {
                        if (log.isDebugEnabled()) {
                            log.debug("No resources were found as dependencies in resource path : " + resourcePath);
                        }
                        return dependencyList.toArray(new Dependency[dependencyList.size()]);
                    }

                    for (int i = 0; i < children.length; i++) {
                        String childPath = children[i];
                        if (registry.resourceExists(childPath)) {
                            Resource child = registry.get(childPath);

                            Dependency element = new Dependency();
                            element.setName(RegistryUtils.getResourceName(child.getPath()));
                            element.setDescription(child.getDescription());
                            element.setValue(getResourceContent(child));
                            element.setMediaType(child.getMediaType());

                            dependencyList.add(element);
                        }
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("No resources were found as dependencies in resource path : " + resourcePath);
                    }
                }
            }

        } catch (RegistryException e) {
            String msg = "Unable to get the dependencies from registry resource path : " + resourcePath;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }

        return dependencyList.toArray(new Dependency[dependencyList.size()]);
    }

    /**
     * Retrieves resource value from a given registry location
     *
     * @param resourcePath resource path
     * @return value
     * @throws AppFactoryException
     */
    public String getResourceValue(String resourcePath) throws AppFactoryException {
        String value = null;
        try {
            UserRegistry registry = getRegistry();
            if (registry.resourceExists(resourcePath)) {
                Resource resource = registry.get(resourcePath);
                value = getResourceContent(resource);
            }
            return value;
        } catch (RegistryException e) {
            String msg = "Error occurred while retrieving dependency value from " + resourcePath;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
    }

    /**
     * Get Registry
     *
     * @return UserRegistry object
     * @throws AppFactoryException
     */
    private UserRegistry getRegistry() throws AppFactoryException {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            RegistryService registryService = ServiceHolder.getRegistryService();
            return registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME, tenantId);
        } catch (RegistryException e) {
            String msg = "Unable to access the registry of the tenant : " + tenantId;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
    }

    /**
     * Get resource content based on content type
     *
     * @param resource resource
     * @return content string
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
            String msg = "Unable to read the content of the resource";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        if (log.isDebugEnabled()) {
            log.debug("getResourceContent() method returns null when the content of the resource does not exist");
        }
        return null;
    }

    /**
     * Remove the cache of the given path from registry
     * This method is copied from governance component in location "https://github.com/wso2-dev/carbon-governance/blob/
     * master/components/governance/org.wso2.carbon.governance.custom.lifecycles.checklist/src/main/java/org/wso2/carbon/
     * governance/custom/lifecycles/checklist/util/LifecycleBeanPopulator.java"
     *
     * @param resourcePath the resource path to be cleared from the cache
     * @throws AppFactoryException
     */
    public void removeRegistryCache(String resourcePath) throws AppFactoryException {
        Registry registry = getRegistry();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Cache<RegistryCacheKey, GhostResource> cache =
                RegistryUtils.getResourceCache(RegistryConstants.REGISTRY_CACHE_BACKED_ID);
        RegistryCacheKey cacheKey;

        if (registry.getRegistryContext().getRemoteInstances().size() > 0) {
            for (Mount mount : registry.getRegistryContext().getMounts()) {
                if (resourcePath.startsWith(mount.getPath())) {
                    for (RemoteConfiguration configuration : registry.getRegistryContext().getRemoteInstances()) {
                        DataBaseConfiguration databaseConfiguration = registry.getRegistryContext().getDBConfig(
                                configuration.getDbConfig());
                        String connectionId = (databaseConfiguration.getUserName() != null
                                ? databaseConfiguration.getUserName().split(
                                "@")[0] : databaseConfiguration.getUserName()) + "@" + databaseConfiguration.getDbUrl();
                        cacheKey = RegistryUtils.buildRegistryCacheKey(connectionId, tenantId, resourcePath);

                        if (cache.containsKey(cacheKey)) {
                            cache.remove(cacheKey);
                            if (log.isDebugEnabled()) {
                                log.debug("Cache cleared for resource path " + resourcePath);
                            }
                        } else {
                            // delete parent path
                            String parentPath =
                                StringUtils.substring(resourcePath, 0, StringUtils.lastIndexOf(resourcePath, "/"));
                            cacheKey = RegistryUtils.buildRegistryCacheKey(connectionId, tenantId, parentPath);
                            if (cache.containsKey(cacheKey)) {
                                cache.remove(cacheKey);
                                if (log.isDebugEnabled()) {
                                    log.debug("Cache cleared for parent path " + parentPath);
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * Method to check whether this is a application showing data sources
     * @param applicationId
     * @return
     */
    public boolean checkDataSourceSupport(String applicationId) throws AppFactoryException {
        String type = RxtApplicationDAO.getInstance().getApplicationType(applicationId);
        ApplicationTypeBean appType = ApplicationTypeManager.getInstance().getApplicationTypeBean(type);
        String[] runTimes = appType.getRuntimes();
        boolean runtimeDS = Boolean.parseBoolean(RuntimeManager.getInstance().getRuntimeBean(runTimes[0]).
                getProperty(AppFactoryConstants.SUPPORT_DATASOURCE));
        boolean appTypeDS = false;
        if (appType.getProperty(AppFactoryConstants.SUPPORT_DATASOURCE) != null &&
                appType.getProperty(AppFactoryConstants.SUPPORT_DATASOURCE).toString() == "true"){
            appTypeDS = true;
        }
        if (runtimeDS && appTypeDS) {
            return true;
        }
        return false;
    }
}




