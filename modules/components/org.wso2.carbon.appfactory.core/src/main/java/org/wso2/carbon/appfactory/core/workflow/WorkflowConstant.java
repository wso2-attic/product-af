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

/**
 * WorkflowDTO related constant values
 */
public class WorkflowConstant {

    public static final String TENANT_CREATION = "WorkflowExtensions.WorkflowType.TenantCreation.Property.ClassName";
    public static final String BPS_SERVER_URL = "WorkflowExtensions.BPS.URL";
    public static final String TENANT_CREATION_DEFAULT_WORKFLOW_CLASS =
            "org.wso2.carbon.appfactory.core.workflow.TenantCreationDefaultWorkflowExecutor";
    public static final String CREATE_TENANT_BPEL_NAME = "/CreateTenant";

    public enum WorkflowType {
        TENANT_CREATION
    }

}