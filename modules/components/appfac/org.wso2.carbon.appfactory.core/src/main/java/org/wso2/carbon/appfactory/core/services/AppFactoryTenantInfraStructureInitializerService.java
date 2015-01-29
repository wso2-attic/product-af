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
package org.wso2.carbon.appfactory.core.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.beans.RuntimeBean;
import org.wso2.carbon.appfactory.core.TenantBuildManagerInitializer;
import org.wso2.carbon.appfactory.core.TenantCloudInitializer;
import org.wso2.carbon.appfactory.core.TenantCreationNotificationInitializer;
import org.wso2.carbon.appfactory.core.TenantRepositoryManagerInitializer;
import org.wso2.carbon.appfactory.core.dto.TenantInfoBean;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.appfactory.core.runtime.RuntimeManager;
import org.wso2.carbon.appfactory.core.util.CloudConstants;
import org.wso2.carbon.core.AbstractAdmin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Service used to initialize all the 3rd party tools on tenant creation
 * .
 */
public class AppFactoryTenantInfraStructureInitializerService extends AbstractAdmin {
    private static final Log log = LogFactory.getLog(AppFactoryTenantInfraStructureInitializerService.class);

    public static final String REPOSITORY_INITIALIZER_TASK = "org.wso2.carbon.appfactory.core.task" +
            ".AppFactoryTenantRepositoryInitializerTask";
    public static final String BUILD_MANAGER_INITIALIZER_TASK = "org.wso2.carbon.appfactory.core.task" +
            ".AppFactoryTenantBuildManagerInitializerTask";
    public static final String TENANT_CREATION_NOTIFICATION_INITIALIZER_TASK = "org.wso2.carbon.appfactory.core.task" +
            ".AppFactoryTenantCreationNotificationInitializerTask";
    public static final String CLOUD_INITIALIZER_TASK = "org.wso2.carbon.appfactory.core.task" +
            ".AppFactoryTenantCloudInitializerTask";


    public AppFactoryTenantInfraStructureInitializerService() throws AppFactoryException {

    }

    /**
     * Used to initialize a repository manager
     *
     * @param tenantDomain
     * @param usagePlan
     * @return
     */
    public boolean initializeRepositoryManager(String tenantDomain, String usagePlan) throws AppFactoryException {

        if (log.isDebugEnabled()) {
            log.debug( "repository-init-" + tenantDomain);
        }

        for (TenantRepositoryManagerInitializer initializer : ServiceHolder.getInstance().
                getTenantRepositoryManagerInitializerList()) {
            initializer.onTenantCreation(tenantDomain, usagePlan);
        }
        return true;
    }

    /**
     * Used to initialize build manager
     *
     * @param tenantDomain
     * @param usagePlan
     * @return
     */
    public boolean initializeBuildManager(String tenantDomain, String usagePlan) throws AppFactoryException {

        if (log.isDebugEnabled()) {
            log.debug("build-init-" + tenantDomain);
        }

        for (TenantBuildManagerInitializer initializer : ServiceHolder.getInstance().
                getTenantBuildManagerInitializerList()) {
            initializer.onTenantCreation(tenantDomain, usagePlan);
        }
        return true;
    }
    
    /**
     * Used to extend Tenant Creation
     *
     * @param bean  with tenant details
     * @return true if the operation is success
     * @throws AppFactoryException
     */
    public boolean notifyTenantCreationListners(TenantInfoBean bean) throws AppFactoryException {

        if (log.isDebugEnabled()) {
            log.debug("notification-init-" + bean.getTenantDomain());
        }

        org.apache.stratos.tenant.mgt.stub.beans.xsd.TenantInfoBean tenantInfoBean
                = new org.apache.stratos.tenant.mgt.stub.beans.xsd.TenantInfoBean();
        tenantInfoBean.setCreatedDate(Calendar.getInstance());
        tenantInfoBean.setUsagePlan(bean.getUsagePlan());
        tenantInfoBean.setTenantDomain(bean.getTenantDomain());
        tenantInfoBean.setSuccessKey(bean.getSuccessKey());
        tenantInfoBean.setActive(true);
        tenantInfoBean.setAdmin(bean.getAdmin());
        tenantInfoBean.setAdminPassword(bean.getAdminPassword());
        tenantInfoBean.setEmail(bean.getEmail());
        tenantInfoBean.setFirstname(bean.getFirstname());
        tenantInfoBean.setLastname(bean.getLastname());

        for (TenantCreationNotificationInitializer initializer : ServiceHolder.getInstance().
                getTenantCreationNotificationInitializerList()) {
            initializer.onTenantCreation(tenantInfoBean);
        }

        return true;
    }

    /**
     * Used to initialize cloud in different stages
     *
     * @param bean  with tenant details
     * @param stage Environment
     * @return true if the operation is success
     * @throws AppFactoryException
     */
    public boolean initializeCloudManager(TenantInfoBean bean, String stage) throws AppFactoryException {

        if (log.isDebugEnabled()) {
            log.debug("cloud-init-" + stage + "-" + bean.getTenantDomain());
        }

        AppFactoryConfiguration configuration = ServiceHolder.getAppFactoryConfiguration();
        Map<String, String> properties = new HashMap<String, String>();
        String serverURL = configuration.getFirstProperty(CloudConstants.ENVIRONMENT.getValue() + "." + stage + "." + "TenantMgtUrl");

        properties.put(CloudConstants.SUPER_TENANT_ADMIN.getValue(),
                configuration.getFirstProperty(AppFactoryConstants.SERVER_ADMIN_NAME));
        properties.put(CloudConstants.SUPER_TENANT_ADMIN_PASSWORD.getValue(),
                configuration.getFirstProperty(AppFactoryConstants.SERVER_ADMIN_PASSWORD));


        Map<String, RuntimeBean> runtimeBeanMap = RuntimeManager.getInstance().getRuntimeBeanMap();
        ArrayList<RuntimeBean> runtimeBeansArrayList = new ArrayList<RuntimeBean>();

        for (Map.Entry<String, RuntimeBean> runtimeName : runtimeBeanMap.entrySet()) {
            RuntimeBean runtimeBean = RuntimeManager.getInstance().getRuntimeBean(runtimeName.getKey());
            if(!runtimeBean.getSubscribeOnDeployment()){
                runtimeBeansArrayList.add(runtimeBean);
            }
        }

        String json;
        try {
            ObjectMapper mapper = new ObjectMapper();
            json = mapper.writeValueAsString(runtimeBeansArrayList.toArray());
        } catch (IOException e) {
            String msg = "Error while converting the runtime bean to a json string";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        properties.put(CloudConstants.RUNTIMES.getValue(), json);

        properties.put(CloudConstants.TENANT_USAGE_PLAN.getValue(), bean.getUsagePlan());
        properties.put(CloudConstants.TENANT_USAGE_PLAN.getValue(), bean.getUsagePlan());
        properties.put(CloudConstants.TENANT_DOMAIN.getValue(), bean.getTenantDomain());
        properties.put(CloudConstants.TENANT_ID.getValue(), String.valueOf(bean.getTenantId()));
        properties.put(CloudConstants.SUCCESS_KEY.getValue(), bean.getSuccessKey());
        properties.put(CloudConstants.ADMIN_USERNAME.getValue(), bean.getAdmin());
        properties.put(CloudConstants.ADMIN_PASSWORD.getValue(), bean.getAdminPassword());
        properties.put(CloudConstants.ADMIN_EMAIL.getValue(), bean.getEmail());
        properties.put(CloudConstants.ADMIN_FIRST_NAME.getValue(), bean.getFirstname());
        properties.put(CloudConstants.ADMIN_LAST_NAME.getValue(), bean.getLastname());
        properties.put(CloudConstants.ORIGINATED_SERVICE.getValue(),"Apache Stratos Controller");
        properties.put(CloudConstants.SERVER_URL.getValue(), serverURL);
        properties.put(CloudConstants.STAGE.getValue(),stage);

        for (TenantCloudInitializer initializer : ServiceHolder.getInstance().
                getTenantCloudInitializer()) {
            initializer.onTenantCreation(properties);
        }

        return true;
    }
}
