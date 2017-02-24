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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.services.AppFactoryTenantInfraStructureInitializerService;
import org.wso2.carbon.appfactory.core.workflow.dto.TenantCreationWorkflowDTO;
import org.wso2.carbon.appfactory.core.workflow.dto.WorkflowDTO;

/**
 * The executor class for the default workflow execution
 */
public class TenantCreationDefaultWorkflowExecutor implements WorkflowExecutor {

    private static Log log = LogFactory.getLog(TenantCreationDefaultWorkflowExecutor.class);

    /**
     * Execute tenant creation workflowDTO process
     *
     * @param workflowDTO the tenant creation workflowDTO dto
     * @throws AppFactoryException exception throw if not success the execution
     */
    @Override public void execute(WorkflowDTO workflowDTO) throws AppFactoryException {
        if (log.isDebugEnabled()) {
            String message = "Executing tenant creation default workflow for the tenant domain : " + workflowDTO
                    .getTenantDomain();
            log.debug(message);
        }

        if (!(workflowDTO instanceof TenantCreationWorkflowDTO)) {
            String message = "TenantCreationWorkflowDTO type is expected but unexpected type is passed to the execute"
                    + " method for the tenant domain : " +
                    workflowDTO.getTenantDomain();
            if (log.isDebugEnabled()) {
                log.debug(message);
            }

            throw new IllegalArgumentException(message);
        }

        TenantCreationWorkflowDTO tenantCreationWorkflow = (TenantCreationWorkflowDTO) workflowDTO;

        AppFactoryTenantInfraStructureInitializerService tenantInfraStructureInitializerService =
                new AppFactoryTenantInfraStructureInitializerService();

        try {
            if (log.isDebugEnabled()) {
                String message = "Executing the initializeRepositoryManager for tenant domain : " + workflowDTO
                        .getTenantDomain();
                log.debug(message);
            }
            tenantInfraStructureInitializerService.initializeRepositoryManager(tenantCreationWorkflow.getTenantDomain(),
                    tenantCreationWorkflow.getTenantInfoBean().getUsagePlan());
        } catch (AppFactoryException e) {
            String message =
                    "Can not initialize repository manager for the tenant domain : " + workflowDTO.getTenantDomain();
            throw new AppFactoryException(message, e);
        }

        try {
            if (log.isDebugEnabled()) {
                String message =
                        "Executing the initializeBuildManager for tenant domain : " + workflowDTO.getTenantDomain();
                log.debug(message);
            }
            tenantInfraStructureInitializerService.initializeBuildManager(tenantCreationWorkflow.getTenantDomain(),
                    tenantCreationWorkflow.getTenantInfoBean().getUsagePlan());
        } catch (AppFactoryException e) {
            String message =
                    "Can not initialize build manager for the tenant domain : " + workflowDTO.getTenantDomain();
            throw new AppFactoryException(message, e);
        }

        try {
            if (log.isDebugEnabled()) {
                String message =
                        "Executing the initializeCloudManager for tenant domain : " + workflowDTO.getTenantDomain();
                log.debug(message);
            }
            tenantInfraStructureInitializerService.initializeCloudManager(tenantCreationWorkflow.getTenantInfoBean());
        } catch (AppFactoryException e) {
            String message =
                    "Can not initialize cloud manager for the tenant domain : " + workflowDTO.getTenantDomain();
            throw new AppFactoryException(message, e);
        }

    }

}
