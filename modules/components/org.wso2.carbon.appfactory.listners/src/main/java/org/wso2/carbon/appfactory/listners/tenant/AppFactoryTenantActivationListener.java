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

package org.wso2.carbon.appfactory.listners.tenant;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.listners.util.Util;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.stratos.common.exception.StratosException;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.wso2.carbon.stratos.common.util.CommonUtil;

/**
 * Copying required permissions and other information to each tenant
 * (application)
 * This class is deprecated.
 */
@Deprecated
public class AppFactoryTenantActivationListener implements TenantMgtListener {
    private static final Log log = LogFactory.getLog(AppFactoryTenantActivationListener.class);
    private static final String COMPONENTS_PATH = "/repository/components";
    private static final String PERMISSIONS_PATH = "/permission";
    private static final String THEME_PATH = "/repository/theme";

    private static final String DOMAIN_CONFORMATION_FLAG =
        "/repository/components/org.wso2.carbon.domain-confirmation-flag";
    private static final String CLOUD_SERVICE =
        "/repository/components/org.wso2.stratos/cloud-manager/cloud-services";
    private static final String ORIGINATED_SERVICE =
        "/repository/components/org.wso2.carbon.originated-service/originatedService";

    private static final String DEV_MOUNT =
        "ApplicationDeployment.DeploymentStage.Development.MountPoint";
    private static final String TEST_MOUNT =
        "ApplicationDeployment.DeploymentStage.Testing.MountPoint";
    private static final String PROD_MOUNT =
        "ApplicationDeployment.DeploymentStage.Production.MountPoint";

    public static final int ORDER = 50;

    @Override
    public void onTenantCreate(TenantInfoBean tenantInfoBean) throws StratosException {
        RegistryService registryService = Util.getRegistryService();
        AppFactoryConfiguration configuration = Util.getConfiguration();

        if (registryService == null) {
            String msg = "Unable to find the registry service";
            log.error(msg);
            throw new StratosException(msg);
        }

        // Using the registry to copy all the information to dev,test and prod
        // locations
        try {
            Registry superTenantRegistry = registryService.getGovernanceSystemRegistry();
            Registry tenantRegistry =
                registryService.getGovernanceSystemRegistry(tenantInfoBean.getTenantId());

            String devMount = configuration.getFirstProperty(DEV_MOUNT);
            String testMount = configuration.getFirstProperty(TEST_MOUNT);
            String prodMount = configuration.getFirstProperty(PROD_MOUNT);

            // Copying the resource to dev mount
            copyResources(superTenantRegistry, tenantRegistry, devMount,
                          tenantInfoBean.getTenantId());
            // Copying the resources to test mount
            copyResources(superTenantRegistry, tenantRegistry, testMount,
                          tenantInfoBean.getTenantId());
            // Copying the resources to prod mount
            copyResources(superTenantRegistry, tenantRegistry, prodMount,
                          tenantInfoBean.getTenantId());

        } catch (RegistryException e) {
            String msg = "Unable to get tenant registry";
            log.error(msg, e);
            throw new StratosException(msg, e);
        }
    }

    private void copyResources(Registry superTenantRegistry, Registry tenantRegistry, String mount,
                               int tenantId) throws RegistryException {

        // Copying the components collection to dev,test and prod environments
        copyComponents(superTenantRegistry, mount);

        // Copying the resources inside components
        copyTenantResources(superTenantRegistry, mount, tenantId);

        // Copying the permissions
        copyPermissions(superTenantRegistry, mount);

        // Doing the copy operation. Need to do a recursive copy since we need
        // to copy the value
        copyThemes(tenantRegistry, mount);
    }

    private void copyComponents(Registry superTenantRegistry, String mount)
    throws RegistryException {
        // We copy the repository/components path if that is not there.
        // This will be done only for the 1st time. all the other times, this
        // should be false
        if (mount != null) {
            String mountPath = RegistryConstants.PATH_SEPARATOR + mount + COMPONENTS_PATH;
            if (!superTenantRegistry.resourceExists(mountPath)) {
                superTenantRegistry.copy(COMPONENTS_PATH, mountPath);
            }
        }
    }

    private void copyTenantResources(Registry superTenantRegistry, String mount, int tenantId)
    throws RegistryException {
        if (mount != null) {
            // Copying the domain Conformation flag
            String domainConformationMountPath =
                RegistryConstants.PATH_SEPARATOR + mount +
                DOMAIN_CONFORMATION_FLAG;
            superTenantRegistry.copy(DOMAIN_CONFORMATION_FLAG, domainConformationMountPath);

            // Copying the originated service flag
            String originatedServicePath =
                RegistryConstants.PATH_SEPARATOR + mount +
                ORIGINATED_SERVICE;
            superTenantRegistry.copy(ORIGINATED_SERVICE, originatedServicePath);

            // Copying the cloud service resources
            String cloudServiceMountPath =
                RegistryConstants.PATH_SEPARATOR + mount +
                CLOUD_SERVICE +
                RegistryConstants.PATH_SEPARATOR + tenantId;
            String cloudServiceOriginalPath =
                CLOUD_SERVICE + RegistryConstants.PATH_SEPARATOR +
                tenantId;
            superTenantRegistry.copy(cloudServiceOriginalPath, cloudServiceMountPath);
        }
    }

    private void copyPermissions(Registry superTenantRegistry, String mount)
    throws RegistryException {
        if (mount != null) {
            String mountPath = RegistryConstants.PATH_SEPARATOR + mount + PERMISSIONS_PATH;
            if (!superTenantRegistry.resourceExists(mountPath)) {
                superTenantRegistry.copy(PERMISSIONS_PATH, mountPath);
            }
        }
    }

    private void copyThemes(Registry tenantRegistry, String mount) throws RegistryException {
        if (mount != null) {
            String targetPrefix = RegistryConstants.PATH_SEPARATOR + mount;
            copyResourceRecursively(tenantRegistry, THEME_PATH, targetPrefix);
        }
    }

    @Override
    public void onTenantUpdate(TenantInfoBean tenantInfoBean) throws StratosException {
        // Do nothing
    }

    @Override
    public void onTenantRename(int i, String s, String s1) throws StratosException {
        // Do nothing
    }

    @Override
    public void onTenantInitialActivation(int i) throws StratosException {
        // Do nothing
    }

    @Override
    public void onTenantActivation(int i) throws StratosException {
        // Do nothing
    }

    @Override
    public void onTenantDeactivation(int i) throws StratosException {
        // Do nothing
    }

    @Override
    public void onSubscriptionPlanChange(int i, String s, String s1) throws StratosException {
        // Do nothing
    }

    @Override
    public int getListenerOrder() {
        return ORDER;
    }
 
    private void copyResourceRecursively(Registry tenantRegistry, String path, String targetPrefix)
    throws RegistryException {

        String targetPath = targetPrefix + path;
        Resource resource = tenantRegistry.get(path);

        tenantRegistry.put(targetPath, resource);

        if (resource instanceof Collection) {
            Collection collection = (Collection) resource;
            String[] children = collection.getChildren();

            if (children != null) {
                for (String child : children) {
                    copyResourceRecursively(tenantRegistry, child, targetPrefix);
                }
            }
        } else {
            UserRegistry registry;

            if (tenantRegistry instanceof UserRegistry) {
                registry = (UserRegistry) tenantRegistry;
            } else {
                return;
            }
            String fullPath =
                RegistryUtils.getAbsolutePathToOriginal(path,
                                                        RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);
            CommonUtil.setAnonAuthorization(fullPath, registry.getUserRealm());

            String targetFullPath =
                RegistryUtils.getAbsolutePathToOriginal(targetPath,
                                                        RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);
            CommonUtil.setAnonAuthorization(targetFullPath, registry.getUserRealm());
        }
    }
}
