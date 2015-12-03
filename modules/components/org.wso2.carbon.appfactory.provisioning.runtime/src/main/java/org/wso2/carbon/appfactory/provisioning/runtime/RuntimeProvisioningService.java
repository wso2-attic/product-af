/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.appfactory.provisioning.runtime;

import org.wso2.carbon.appfactory.provisioning.runtime.beans.*;

import java.io.OutputStream;
import java.util.List;
import java.util.Set;

public interface RuntimeProvisioningService {

    public void setApplicationContext(ApplicationContext applicationContext) throws RuntimeProvisioningException;

    public void createOrganization(TenantInfo tenantInfo) throws RuntimeProvisioningException;

    public void updateOrganization(TenantInfo tenantInfo) throws RuntimeProvisioningException;

    public void deleteOrganization(TenantInfo tenantInfo) throws RuntimeProvisioningException;

    public void archiveOrganization(TenantInfo tenantInfo) throws RuntimeProvisioningException;

    public int createBuild(BuildConfigaration config) throws RuntimeProvisioningException;

    public String getBuildStatus(String buildId) throws RuntimeProvisioningException;

    public String getBuildLog(String buildId) throws RuntimeProvisioningException;

    public List<String> getBuildHistory() throws RuntimeProvisioningException;

    public boolean cancelBuild(String buildId) throws RuntimeProvisioningException;

    public List<String> deployApplication(DeploymentConfig config) throws RuntimeProvisioningException;

    public boolean getDeploymentStatus() throws RuntimeProvisioningException;

    public OutputStream streamRuntimeLogs() throws RuntimeProvisioningException;

    public String getRuntimeLogs(Query query) throws RuntimeProvisioningException;

    public void setRuntimeProperties(List<RuntimeProperty> runtimeProperties) throws RuntimeProvisioningException;

    public void updateRuntimeProperties(RuntimeProperty runtimeProperty) throws RuntimeProvisioningException;

    public List<RuntimeProperty> getRuntimeProperties() throws RuntimeProvisioningException;

    public void addCustomDomain(Set<String> domains) throws RuntimeProvisioningException;

    public void updateCustomDomain(String domain) throws RuntimeProvisioningException;

    public Set<String> getCustomDomains() throws RuntimeProvisioningException;

    public void deleteCustomDomain(String domain)throws RuntimeProvisioningException;
}
