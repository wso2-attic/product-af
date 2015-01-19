/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package org.wso2.carbon.appfactory.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

public class DependencyUtil {
    private static final Log log = LogFactory.getLog(DependencyUtil.class);

    public static int getTenantId(String applicationKey) throws AppFactoryException {
        RealmService realmService = ServiceHolder.getRealmService();
        if (realmService == null) {
            throw new AppFactoryException("Unable to find the realm service");
        }
        TenantManager tenantManager = realmService.getTenantManager();
        int tenantId;
        try {
            tenantId = tenantManager.getTenantId(applicationKey);

            if (tenantId == MultitenantConstants.INVALID_TENANT_ID) {
                String msg = "Invalid tenant id returned";
                log.error(msg);
                throw new AppFactoryException(msg);
            }
        } catch (UserStoreException e) {
            String msg = "Unable to get the tenant id for application key : " + applicationKey;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        return tenantId;
    }

    public static String getMountPoint(String stage) throws AppFactoryException {
        AppFactoryConfiguration configuration = ServiceHolder.getAppFactoryConfiguration();

        String referenceName = Constants.MOUNT_POINT_PREFIX + stage + Constants.MOUNT_POINT_SUFFIX;
        if (configuration.getFirstProperty(referenceName) == null) {
            String msg = "Could not locate the mount point for stage : " + stage;
            log.error(msg);
            throw new AppFactoryException(msg);
        }
        return configuration.getFirstProperty(referenceName);
    }

    public static String getDependenciesPath(String name) throws AppFactoryException {
        //            We store all these resources under a collection called dependencies.
//            The path looks like "/dependencies/foo". dev would be our development mount point
        //
        return RegistryConstants.PATH_SEPARATOR + Constants.DEPENDENCIES_HOME + RegistryConstants.PATH_SEPARATOR+  name;
    }
    public static String getMountedDependenciesPath(String name,String stage) throws AppFactoryException {
        //            We store all these resources under a collection called dependencies.
//            The path looks like "/dependencies/dev/foo". dev would be our development mount point
        return RegistryConstants.PATH_SEPARATOR + Constants.DEPENDENCIES_HOME +
               RegistryConstants.PATH_SEPARATOR +DependencyUtil.getMountPoint(stage)+RegistryConstants.PATH_SEPARATOR  +name;
    }

    public static Registry getGovernanceRegistry(int tenantId) throws AppFactoryException {
        TenantRegistryLoader tenantRegistryLoader = ServiceHolder.getTenantRegistryLoader();
        RegistryService registryService = ServiceHolder.getRegistryService();

        if (registryService == null) {
            String msg = "Unable to find registry service";
            log.error(msg);
            return null;
        }
        if(tenantRegistryLoader == null){
            String msg = "Unable to find the tenant registry loader service";
            log.error(msg);
            throw new AppFactoryException(msg);
        }

        try {
            tenantRegistryLoader.loadTenantRegistry(tenantId);
            return registryService.getGovernanceSystemRegistry(tenantId);
        } catch (RegistryException e) {
            String msg = "Unable to get the governance registry instance";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }

    }

    public static String getResourceContent(Resource resource) throws AppFactoryException{
        try {
            if(resource.getContent() != null){
                if(resource.getContent() instanceof String){
                    return (String) resource.getContent();
                }else if(resource.getContent() instanceof byte[]){
                    return new String((byte[]) resource.getContent());
                }
            }
        } catch (RegistryException e) {
            String msg = "Unable to read the resource content";
            log.error(msg,e);
            throw new AppFactoryException(msg,e);
        }
        return null;
    }

    public static String getDependenciesRootPath(String stage) throws AppFactoryException {
        //dev/dependencies/
        String rootDependencyPath=RegistryConstants.PATH_SEPARATOR +Constants.DEPENDENCIES_HOME+
                                  RegistryConstants.PATH_SEPARATOR+DependencyUtil.getMountPoint(stage);
        return rootDependencyPath;
    }

    /**
     * Method to create mount points for dependency management
     *
     * @throws AppFactoryException
     */
    public static void createDependenciesMountPoints() throws AppFactoryException {
        String stages[]=ServiceHolder.getAppFactoryConfiguration().getProperties(Constants.DEPLOYMENT_STAGES);
        String dependencyMountPoint;
        for(String stage:stages){
            dependencyMountPoint=Constants.DEPENDENCIES_HOME+RegistryConstants.PATH_SEPARATOR+DependencyUtil.getMountPoint(stage);
            Registry registry;
            try {
               registry=ServiceHolder.getRegistryService().getGovernanceSystemRegistry(MultitenantConstants.SUPER_TENANT_ID);
            } catch (RegistryException e) {
                String msg ="Could not get governance registry ";
                log.error(msg,e);
                throw new AppFactoryException(msg,e);
            }
            try {
                if(!registry.resourceExists(dependencyMountPoint)){
                try {
                    Collection collection=registry.newCollection();
                    registry.put(dependencyMountPoint,collection);
                } catch (RegistryException e) {
                    String msg ="Could not create a collection ";
                    log.error(msg,e);
                    throw new AppFactoryException(msg,e);
                } catch (org.wso2.carbon.registry.api.RegistryException e1) {
                    String msg ="Could not add collection to "+dependencyMountPoint;
                    log.error(msg,e1);
                    throw new AppFactoryException(msg,e1);
                }
                }
            } catch (RegistryException e) {
                String msg ="Error while checking existance of collection location "+dependencyMountPoint;
                log.error(msg,e);
                throw new AppFactoryException(msg,e);
            }

        }
    }
}
