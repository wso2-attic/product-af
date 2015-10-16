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

package org.wso2.carbon.appfactory.repository.mgt;

import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.repository.mgt.client.AppfactoryRepositoryClient;

/**
 * Every repository provider should implement this interface
 */
public interface RepositoryProvider {
    /**
     * Create a repository for a application key
     *
     * @param applicationKey for the creating app
     * @param tenantDomain   Tenant domain of application
     * @return url for created repository
     * @throws RepositoryMgtException when repository creation fails
     */
    public String createRepository(String applicationKey, String tenantDomain) throws RepositoryMgtException;

    /**
     * Delete a repository for a application key
     *
     * @param applicationKey for the deleting app
     * @param tenantDomain   Tenant domain of application
     * @return delete repository result
     * @throws RepositoryMgtException when repository deletion fails
     */
    public boolean deleteRepository(String applicationKey, String tenantDomain) throws RepositoryMgtException;


    /**
     * Delete a forked repository for a particular user in the application
     *
     * @param applicationKey for the deleting app
     * @param userName of the forked repo owner
     * @param tenantDomain of the forked repo owner
     * @return delete fork repository result
     * @throws RepositoryMgtException when forked repository deletion fails
     */
    public boolean deleteForkedRepository(String applicationKey, String userName, String tenantDomain) throws RepositoryMgtException;

	/**
	 * Delete all forked repositories for a particular application
	 *
	 * @param applicationKey for the deleting app
	 * @param tenantDomain of the forked repo owner
	 * @return delete fork repository result
	 * @throws RepositoryMgtException when forked repository deletion fails
	 */
	public boolean deleteForkedRepositoriesForApplication(String applicationKey, String tenantDomain) throws RepositoryMgtException;

    /**
     * Check whether a repository exists for application of the tenant
     * @param applicationKey    
     * @param tenantDomain
     * @return
     * @throws RepositoryMgtException
     */
    public boolean repoExists(String applicationKey, String tenantDomain) throws RepositoryMgtException;

    public String getAppRepositoryURL(String applicationKey, String tenantDomain) throws RepositoryMgtException;

    /**
     * This is the method  used to set appfactory configuration by appfactory.
     * Repository provider should implement this method to use the appfactory configs meaning fully
     *
     * @param configuration given by appfactory.xml
     */
    public void setConfiguration(AppFactoryConfiguration configuration);

    /**
     * Getter for  AppfactoryRepositoryClient
     *
     * @return
     * @throws RepositoryMgtException
     */
    public AppfactoryRepositoryClient getRepositoryClient() throws RepositoryMgtException;

    /**
     * Getter for BranchingStrategy
     *
     * @return
     */
    public BranchingStrategy getBranchingStrategy();

    /**
     * Getter for AppfactoryRepositoryClient
     *
     * @param client
     */
    public void setAppfactoryRepositoryClient(AppfactoryRepositoryClient client);

    /**
     * Setter for BranchingStrategy
     *
     * @param branchingStrategy
     */
    public void setBranchingStrategy(BranchingStrategy branchingStrategy);

    /**
     * This is to create a fork out of the existing repo
     *
     * @param repoUrl
     */
   // public void createFork(String repoUrl);

    /**
     * Provision user to the user store of repository
     *
     * @param applicationKey
     * @param username
     * @throws RepositoryMgtException
     */
    void provisionUser(String applicationKey, String username) throws RepositoryMgtException;
    public boolean createTenantRepo(String tenantId) throws RepositoryMgtException;
    public boolean deleteTenantRepo(String tenantId) throws RepositoryMgtException;

    /**
     * Delete the given repository from stratos artifact repo
     * @param repoName
     * @return
     * @throws RepositoryMgtException
     */
    public void deleteStratosArtifactRepository(String repoName) throws RepositoryMgtException;

    /**
     *
     * @param applicationKey
     * @throws RepositoryMgtException
     */
    public String createForkRepo (String applicationKey, String userName) throws RepositoryMgtException;
    
    public void forkBranch(String applicationKey, String userName, String branch) throws RepositoryMgtException;

    public String getForkedAppRepositoryURL(String applicationKey, String tenantDomain, String userId) throws RepositoryMgtException;
}
