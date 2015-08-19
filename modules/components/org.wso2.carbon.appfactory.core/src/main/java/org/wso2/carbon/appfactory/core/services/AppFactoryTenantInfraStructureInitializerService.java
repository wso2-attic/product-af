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
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.appfactory.core.runtime.RuntimeManager;
import org.wso2.carbon.appfactory.core.task.AppFactoryTenantRepositoryInitializerTask;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;

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
    public static final String APP_FACTORY_TASK_MANAGER = "appfactory.task.manager";
    public static final String REPOSITORY_INITIALIZER_TASK = "org.wso2.carbon.appfactory.core.task" +
            ".AppFactoryTenantRepositoryInitializerTask";
    public static final String BUILD_MANAGER_INITIALIZER_TASK = "org.wso2.carbon.appfactory.core.task" +
            ".AppFactoryTenantBuildManagerInitializerTask";
    public static final String TENANT_CREATION_NOTIFICATION_INITIALIZER_TASK = "org.wso2.carbon.appfactory.core.task" +
            ".AppFactoryTenantCreationNotificationInitializerTask";
    public static final String CLOUD_INITIALIZER_TASK = "org.wso2.carbon.appfactory.core.task" +
            ".AppFactoryTenantCloudInitializerTask";
    private String ENVIRONMENT = "ApplicationDeployment.DeploymentStage";
    private TaskManager taskManager;

    public AppFactoryTenantInfraStructureInitializerService() throws AppFactoryException {
        try {
            taskManager = ServiceHolder.getInstance().getTaskService().getTaskManager
                    (APP_FACTORY_TASK_MANAGER);
        } catch (TaskException e) {
            String msg = "Error while getting " + APP_FACTORY_TASK_MANAGER;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
    }

    /**
     * Used to initialize a repository manager
     *
     * @param tenantDomain
     * @param usagePlan
     * @return
     */
    public boolean initializeRepositoryManager(String tenantDomain, String usagePlan) throws AppFactoryException {
        TaskInfo.TriggerInfo triggerInfo = getTriggerWithDalay();

        String taskName = "repository-init-" + tenantDomain;
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(AppFactoryTenantRepositoryInitializerTask.TENANT_DOMAIN, tenantDomain);
        properties.put(AppFactoryTenantRepositoryInitializerTask.TENANT_USAGE_PLAN, usagePlan);
        TaskInfo taskInfo = new TaskInfo(taskName, AppFactoryTenantInfraStructureInitializerService.REPOSITORY_INITIALIZER_TASK,
                properties, triggerInfo);
        try {
            taskManager.registerTask(taskInfo);
        } catch (TaskException e) {
            String msg = "Error while registering " + taskName;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        try {
            taskManager.scheduleTask(taskName);
        } catch (TaskException e) {
            String msg = "Error while scheduling " + taskName;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
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
        TaskInfo.TriggerInfo triggerInfo = getTriggerWithDalay();
        String taskName = "build-init-" + tenantDomain;
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(AppFactoryTenantRepositoryInitializerTask.TENANT_DOMAIN, tenantDomain);
        properties.put(AppFactoryTenantRepositoryInitializerTask.TENANT_USAGE_PLAN, usagePlan);
        TaskInfo taskInfo = new TaskInfo(taskName, AppFactoryTenantInfraStructureInitializerService.BUILD_MANAGER_INITIALIZER_TASK,
                properties, triggerInfo);
        try {
            taskManager.registerTask(taskInfo);
        } catch (TaskException e) {
            String msg = "Error while registering " + taskName;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        try {
            taskManager.scheduleTask(taskName);
        } catch (TaskException e) {
            String msg = "Error while scheduling " + taskName;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
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
        TaskInfo.TriggerInfo triggerInfo = getTriggerWithDalay();
        String taskName = "notification-init-" + bean.getTenantDomain();
        Map<String, String> properties = new HashMap<String, String>();
	    ObjectMapper mapper = new ObjectMapper();
	    String tenantInfoJson;

	    try {
		    tenantInfoJson = mapper.writeValueAsString(bean);
	    } catch (IOException e) {
		    String msg = "Error while converting the tenant info bean to a json string";
		    log.error(msg, e);
		    throw new AppFactoryException(msg, e);
	    }

	    properties.put(AppFactoryConstants.TENANT_INFO, tenantInfoJson);

        TaskInfo taskInfo = new TaskInfo(taskName, AppFactoryTenantInfraStructureInitializerService.
		        TENANT_CREATION_NOTIFICATION_INITIALIZER_TASK, properties, triggerInfo);
        try {
            taskManager.registerTask(taskInfo);
        } catch (TaskException e) {
            String msg = "Error while registering " + taskName;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        try {
            taskManager.rescheduleTask(taskInfo.getName());
        } catch (TaskException e) {
            String msg = "Error while scheduling " + taskName;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
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
        AppFactoryConfiguration configuration = ServiceHolder.getAppFactoryConfiguration();
        String serverURL = configuration.getFirstProperty(ENVIRONMENT + "." + stage + "." + "TenantMgtUrl");

        TaskInfo.TriggerInfo triggerInfo = getTriggerWithDalay();
        String taskName = "cloud-init-" + stage + "-" + bean.getTenantDomain();
        Map<String, String> properties = new HashMap<String, String>();

        Map<String, RuntimeBean> runtimeBeanMap = RuntimeManager.getInstance().getRuntimeBeanMap();
        ArrayList<RuntimeBean> runtimeBeansArrayList = new ArrayList<RuntimeBean>();

        for (Map.Entry<String, RuntimeBean> runtimeName : runtimeBeanMap.entrySet()) {
            RuntimeBean runtimeBean = RuntimeManager.getInstance().getRuntimeBean(runtimeName.getKey());
            if(!runtimeBean.getSubscribeOnDeployment()){
                runtimeBeansArrayList.add(runtimeBean);
            }
        }

        String runtimeJson;
	    String tenantInfoJson;
	    ObjectMapper mapper = new ObjectMapper();
        try {
            runtimeJson = mapper.writeValueAsString(runtimeBeansArrayList.toArray());
        } catch (IOException e) {
            String msg = "Error while converting the runtime bean to a json string";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
	    try {
		    tenantInfoJson = mapper.writeValueAsString(bean);
	    } catch (IOException e) {
		    String msg = "Error while converting the tenant info bean to a json string";
		    log.error(msg, e);
		    throw new AppFactoryException(msg, e);
	    }

        properties.put(AppFactoryConstants.RUNTIMES, runtimeJson);
        properties.put(AppFactoryConstants.TENANT_INFO, tenantInfoJson);
        properties.put(AppFactoryConstants.STAGE,stage);
        TaskInfo taskInfo = new TaskInfo(taskName, AppFactoryTenantInfraStructureInitializerService.CLOUD_INITIALIZER_TASK,
                properties, triggerInfo);
        try {
            taskManager.registerTask(taskInfo);
        } catch (TaskException e) {
            String msg = "Error while registering " + taskName;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        try {
            taskManager.rescheduleTask(taskInfo.getName());
        } catch (TaskException e) {
            String msg = "Error while scheduling " + taskName;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        return true;
    }

    private TaskInfo.TriggerInfo getTriggerWithDalay() {
        TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo();
        //trigger immediately after 5s and one time
        Calendar triggerTime=Calendar.getInstance();
        triggerTime.roll(Calendar.SECOND, 5);
        triggerInfo.setStartTime(triggerTime.getTime());
        triggerInfo.setRepeatCount(0);
        return triggerInfo;
    }
}
