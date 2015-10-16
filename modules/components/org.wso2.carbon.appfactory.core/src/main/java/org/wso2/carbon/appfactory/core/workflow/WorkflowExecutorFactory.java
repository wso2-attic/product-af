/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appfactory.core.workflow;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.workflow.dto.TenantCreationWorkflowDTO;
import org.wso2.carbon.appfactory.core.workflow.dto.WorkflowDTO;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The factory class is creating workflow dto object and workflow executor object
 */
public class WorkflowExecutorFactory {

    private static Log log = LogFactory.getLog(WorkflowExecutorFactory.class);
    private Map<WorkflowConstant.WorkflowType, WorkflowExecutor> workflowExecutorMap;
    private static final WorkflowExecutorFactory workflowExecutorFactory = new WorkflowExecutorFactory();

    private WorkflowExecutorFactory() {
        try {
            loadWorkflowConfiguration();
        } catch (AppFactoryException e) {
            String message = "Unable to load workflow configuration or create workflow type objects";
            log.error(message, e);
        }
    }

    public static WorkflowExecutorFactory getInstance() {
        return workflowExecutorFactory;
    }

    /**
     * Load configured class to the class loader and
     * create instance of the particular class
     */
    public void loadWorkflowConfiguration() throws AppFactoryException {
        if (log.isDebugEnabled()) {
            String message = "Load configured class to class loader and create instance of that particular class "
                    + "dynamically";
            log.debug(message);
        }

        workflowExecutorMap = new ConcurrentHashMap<WorkflowConstant.WorkflowType, WorkflowExecutor>();
        String executorClass;
        WorkflowExecutor workflowExecutor;
        try {
            executorClass = AppFactoryUtil.getAppfactoryConfiguration()
                    .getFirstProperty(WorkflowConstant.TENANT_CREATION);
        } catch (AppFactoryException e) {
            String message =
                    "Unable to read executor class from the appfactory.xml configuration, the workflow type : "
                            + WorkflowConstant.WorkflowType.TENANT_CREATION;
            throw new AppFactoryException(message, e);
        }

        if (StringUtils.isBlank(executorClass)) {
            executorClass = WorkflowConstant.TENANT_CREATION_DEFAULT_WORKFLOW_CLASS;
            String message = "The tenant creation executor class is not define in appfactory.xml because tenant "
                    + "creation default workflow is " + "executing. The tenant creation default workflow class is : "
                    + executorClass;
            log.warn(message);
        }

        try {
            Class tenantCreationExecutorClass = WorkflowExecutorFactory.class.getClassLoader().loadClass(executorClass);
            workflowExecutor = (WorkflowExecutor) tenantCreationExecutorClass.newInstance();
            workflowExecutorMap.put(WorkflowConstant.WorkflowType.TENANT_CREATION, workflowExecutor);
        } catch (ClassNotFoundException e) {
            String message = "Unable to find configured class : " + executorClass + " in the class loader, when "
                    + "initializing the executor class for workflow type : "
                    + WorkflowConstant.WorkflowType.TENANT_CREATION;
            throw new AppFactoryException(message, e);
        } catch (InstantiationException e) {
            String message = "Unable to initiate object of the class : " + executorClass + "when initializing the "
                    + "object of the executor class for workflow type : "
                    + WorkflowConstant.WorkflowType.TENANT_CREATION;
            throw new AppFactoryException(message, e);
        } catch (IllegalAccessException e) {
            String message = "Unable to access the class" + executorClass + " for create new instance when accessing "
                    + "the executor class for workflow type : " + WorkflowConstant.WorkflowType.TENANT_CREATION;
            throw new AppFactoryException(message, e);
        }
    }

    /**
     * Return workflow object by given workflow type
     *
     * @param workflowType type of the workflow
     * @return workflow object
     */
    public WorkflowExecutor getWorkflowExecutor(WorkflowConstant.WorkflowType workflowType) {
        return workflowExecutorMap.get(workflowType);
    }

    /**
     * Create workflow data transfer object by given workflow type
     *
     * @param workflowType type of the workflow
     * @return workflow DTO object
     */
    public WorkflowDTO createWorkflowDTO(WorkflowConstant.WorkflowType workflowType) {
        WorkflowDTO workflowDTO;
        switch (workflowType) {
            case TENANT_CREATION:
                workflowDTO = new TenantCreationWorkflowDTO();
                break;
            default:
                String message = "The type of workflow not support, the workflow type : " + workflowType;
                throw new IllegalArgumentException(message);
            }

        return workflowDTO;
    }

}
