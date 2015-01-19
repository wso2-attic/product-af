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
package org.wso2.carbon.appfactory.core.dependency;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.util.DependencyUtil;
import org.wso2.carbon.appfactory.core.dto.Dependency;
import org.wso2.carbon.core.AbstractAdmin;

/**
 * This class represents the admin service class for data sources.
 */
public class DependencyManagementAdminService extends AbstractAdmin {
    private static final Log log = LogFactory.getLog(DependencyManagementAdminService.class);


    public boolean addDependency(String applicationKey, String stage, String name, String description,
                                 String mediaType, String value)
            throws AppFactoryException {

        int tenantId = DependencyUtil.getTenantId(applicationKey);

        try {
            RegistryPersistManager.addToRegistry(stage, name, value, description, mediaType, tenantId);
        } catch (Exception e) {
            String msg = "Unable to create resource :" + name + " for stage : " + stage;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        return true;
    }


    public boolean removeDependency(String applicationKey, String stage, String name) throws AppFactoryException {

        int tenantId = DependencyUtil.getTenantId(applicationKey);

        try {
            RegistryPersistManager.removeFromRegistry(stage, name, tenantId);
        } catch (Exception e) {
            String msg = "Unable to remove the resource from registry";
            log.error(msg,e);
            throw new AppFactoryException(msg,e);
        }
        return true;
    }

    public Dependency[] getAllDependency(String applicationKey,String stage) throws AppFactoryException {

        int tenantId = DependencyUtil.getTenantId(applicationKey);

        try {
            return RegistryPersistManager.getAllDependencyPaths(tenantId,stage);
        } catch (Exception e) {
            String msg = "Unable to get all the dependencies from the registry";
            log.error(msg,e);
            throw new AppFactoryException(msg,e);
        }
    }

    public Dependency getDependency(String applicationKey, String stage, String resourceName) throws AppFactoryException {

        int tenantId = DependencyUtil.getTenantId(applicationKey);

        try {
            return RegistryPersistManager.getDependencyPath(tenantId, stage, resourceName);
        } catch (Exception e) {
            String msg = "Unable to get all the dependencies from the registry";
            log.error(msg,e);
            throw new AppFactoryException(msg,e);
        }
    }

    public boolean updateDependency(String applicationKey, String stage, String name, String description,
                                    String mediaType, String value) throws AppFactoryException {
        int tenantId = DependencyUtil.getTenantId(applicationKey);

        try {
            RegistryPersistManager.updateResource(stage, name, value, description, mediaType, tenantId);
        } catch (Exception e) {
            String msg = "Unable to update resource :" + name + " for stage : " + stage;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        return true;
    }
}
