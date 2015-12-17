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

import java.util.List;
import java.util.Set;

public interface RuntimeProvisioningService {

    /**
     * Set application details for the context
     *
     * @param applicationContext application details
     * @throws RuntimeProvisioningException
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws RuntimeProvisioningException;

    /**
     * Create an organization for given tenant details
     *
     * @param tenantInfo details of the tenant
     * @throws RuntimeProvisioningException
     */
    public void createOrganization(TenantInfo tenantInfo) throws RuntimeProvisioningException;

    /**
     * Update an organization details
     *
     * @param tenantInfo details of the tenant
     * @throws RuntimeProvisioningException
     */
    public void updateOrganization(TenantInfo tenantInfo) throws RuntimeProvisioningException;

    /**
     * Delete an organization related details
     *
     * @param tenantInfo details of the tenant
     * @throws RuntimeProvisioningException
     */
    public void deleteOrganization(TenantInfo tenantInfo) throws RuntimeProvisioningException;

    /**
     * Archive an organization
     *
     * @param tenantInfo details of the tenant
     * @throws RuntimeProvisioningException
     */
    public void archiveOrganization(TenantInfo tenantInfo) throws RuntimeProvisioningException;

    /**
     * Create a new build
     *
     * @param buildConfiguration build related details
     * @return build id
     * @throws RuntimeProvisioningException
     */
    public int createBuild(BuildConfiguration buildConfiguration) throws RuntimeProvisioningException;

    /**
     * Provide details about build status
     *
     * @param buildId id of the build
     * @return Status of the build
     * @throws RuntimeProvisioningException
     */
    public String getBuildStatus(String buildId) throws RuntimeProvisioningException;

    /**
     * Provide build related logs
     *
     * @param buildId id of the build
     * @return logs of the build process
     * @throws RuntimeProvisioningException
     */
    public String getBuildLog(String buildId) throws RuntimeProvisioningException;

    /**
     * Provide build related history details
     *
     * @return history of the build
     * @throws RuntimeProvisioningException
     */
    public List<String> getBuildHistory() throws RuntimeProvisioningException;

    /**
     * Cancel already triggered build
     *
     * @param buildId
     * @return id of the build
     * @throws RuntimeProvisioningException
     */
    public boolean cancelBuild(String buildId) throws RuntimeProvisioningException;

    /**
     * Deploy an application
     *
     * @param deploymentConfig details of the deployment
     * @return list of endpoints
     * @throws RuntimeProvisioningException
     */
    public List<String> deployApplication(DeploymentConfig deploymentConfig) throws RuntimeProvisioningException;

    /**
     * Provide deployment related details
     *
     * @return Whether deployment fail or not
     * @throws RuntimeProvisioningException
     */
    public boolean getDeploymentStatus(DeploymentConfig deploymentConfig) throws RuntimeProvisioningException;

    /**
     * Provide runtime log stream
     *
     * @return log out put stream
     * @throws RuntimeProvisioningException
     */
    public DeploymentLogs streamRuntimeLogs() throws RuntimeProvisioningException;

    /**
     * Provide snapshot logs
     *
     * @param query query related details
     * @return Snapshot logs of application
     * @throws RuntimeProvisioningException
     */
    public DeploymentLogs getRuntimeLogs(LogQuery query)
            throws RuntimeProvisioningException;

    /**
     * Set runtime variables
     *
     * @param runtimeProperties runtime properties
     * @throws RuntimeProvisioningException
     */
    public void setRuntimeProperties(List<RuntimeProperty> runtimeProperties, DeploymentConfig deploymentConfig)
            throws RuntimeProvisioningException;

    /**
     * Update existing runtime properties
     *
     * @param runtimeProperty runtime property
     * @throws RuntimeProvisioningException
     */
    public void updateRuntimeProperties(List<RuntimeProperty> runtimeProperty, DeploymentConfig deploymentConfig)
            throws RuntimeProvisioningException;

    /**
     * Provide application specific runtime properties
     *
     * @return List of runtime properties
     * @throws RuntimeProvisioningException
     */
    public List<RuntimeProperty> getRuntimeProperties() throws RuntimeProvisioningException;

    /**
     * Adding a custom domain mapping to a particular application
     *
     * @param domains set of domains
     * @throws RuntimeProvisioningException
     */
    public boolean addCustomDomain(Set<String> domains) throws RuntimeProvisioningException;

    /**
     * Update a certain custom domain mapping for a particular application version
     *
     * @param oldDomain old domain name to be changed
     * @param newDomain new domain name to be changed to
     * @throws RuntimeProvisioningException
     */
    public void updateCustomDomain(String oldDomain, String newDomain) throws RuntimeProvisioningException;

    /**
     * Return custom domain mappings of a certain application version
     *
     * @return set of domains
     * @throws RuntimeProvisioningException
     */
    public Set<String> getCustomDomains() throws RuntimeProvisioningException;

    /**
     * Delete a certain custom domain mapping
     *
     * @param domain domain name
     * @throws RuntimeProvisioningException
     */
    public boolean deleteCustomDomain(String domain) throws RuntimeProvisioningException;
}
