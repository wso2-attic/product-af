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

package org.wso2.carbon.appfactory.core.workflow;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.workflow.dto.TenantCreationWorkflowDTO;
import org.wso2.carbon.appfactory.core.workflow.dto.WorkflowDTO;

import java.util.HashMap;
import java.util.Map;

/**
 * The factory class is creating workflow dto object and workflow executor object
 */
public class WorkflowExecutorFactory {

    private Map<String, WorkflowExecutor> workflowExecutorMap;
    private static Log log = LogFactory.getLog(WorkflowExecutorFactory.class);
    private static final WorkflowExecutorFactory workflowExecutorFactory = new WorkflowExecutorFactory();

    private WorkflowExecutorFactory() {
        try {
            loadWorkflowConfiguration();
        } catch (AppFactoryException e) {
            String message = "Unable to load workflow configuration and ";
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

        workflowExecutorMap = new HashMap<String, WorkflowExecutor>();
        String executorClass;
        WorkflowExecutor workflowExecutor;
        try {
            executorClass = AppFactoryUtil.getAppfactoryConfiguration()
                    .getFirstProperty(WorkflowConstant.TENANT_CREATION);
        } catch (AppFactoryException e) {
            String message = "Unable to read executor class from the appfactory.xml configuration";
            throw new AppFactoryException(message, e);
        }

        if (StringUtils.isBlank(executorClass)) {
            executorClass = WorkflowConstant.DEFAULT_WORKFLOW_CLASS;
            String message = "The executor class is not define in appfactory.xml because default workflow is "
                    + "executing. The default workflow class is : " + executorClass;
            log.warn(message);
        }

        try {
            Class tenantCreationExecutorClass = WorkflowExecutorFactory.class.getClassLoader().loadClass(executorClass);
            workflowExecutor = (WorkflowExecutor) tenantCreationExecutorClass.newInstance();
            workflowExecutorMap.put(WorkflowConstant.WF_TYPE_AF_TENANT_CREATION, workflowExecutor);
        } catch (ClassNotFoundException e) {
            String message = "Unable to find configured class from the source code : class name : " + executorClass;
            throw new AppFactoryException(message, e);
        } catch (InstantiationException e) {
            String message = "Can not initiate object of the class : class name : " + executorClass;
            throw new AppFactoryException(message, e);
        } catch (IllegalAccessException e) {
            String message = "Can not access the class to create new instance : class name : " + executorClass;
            throw new AppFactoryException(message, e);
        }
    }

    /**
     * Return workflow object by given workflow type
     *
     * @param workflowType type of the workflow
     * @return workflow object
     */
    public WorkflowExecutor getWorkflowExecutor(String workflowType) {
        return workflowExecutorMap.get(workflowType);
    }

    /**
     * Create workflow data transfer object by given workflow type
     *
     * @param workflowType type of the workflow
     * @return workflow DTO object
     */
    public WorkflowDTO createWorkflowDTO(String workflowType) {
        WorkflowDTO workflowDTO;
        if (WorkflowConstant.WF_TYPE_AF_TENANT_CREATION.equals(workflowType)) {
            workflowDTO = new TenantCreationWorkflowDTO();
        } else {
            String message = "The type of workflow not support : workflow type : " + workflowType;
            throw new IllegalArgumentException(message);
        }

        return workflowDTO;
    }

}
