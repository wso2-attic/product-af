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

/**
 * Interface contains operations to be done prior to repository operations
 */
public interface BranchingStrategy {
    /**
     * This method will be used to prepare repository after creating repository such as adding skeleton
     * project.
     *
     * @param appId applicationKey
     * @param url   Repository URL
     * @param tenantDomain Tenant domain of the application
     * @throws RepositoryMgtException
     */
    public void prepareRepository(String appId, String url, String tenantDomain)
            throws RepositoryMgtException;

    /**
     * This method will be executed while creating new branch
     *
     * @param appId           applicationKey
     * @param currentVersion  current version from which to branch
     * @param targetVersion   target version
     * @param currentRevision Revision to branch
     * @param tenantDomain the tenant name of the application
     * @throws RepositoryMgtException
     */
    public void doRepositoryBranch(String appId, String currentVersion, String targetVersion,
                                   String currentRevision, String tenantDomain) throws RepositoryMgtException;

    /**
     * This method will be executed while creating new tag
     *
     * @param appId           applicationKey
     * @param currentVersion  current version from which to tag
     * @param targetVersion   target version
     * @param currentRevision Revision to tag
     * @param tenantDomain    the tenant name of the application
     * @throws RepositoryMgtException
     */
    public void doRepositoryTag(String appId, String currentVersion, String targetVersion,
                                String currentRevision, String tenantDomain) throws RepositoryMgtException;

    /**
     * Setter for RepositoryProvider
     *
     * @param provider
     */
    public void setRepositoryProvider(RepositoryProvider provider);

    /**
     * Getter for RepositoryProvider
     *
     * @return
     */
    public RepositoryProvider getRepositoryProvider();

    /**
     * Method to retrieve repository url for Application version
     *
     * @param applicationKey Application key
     * @param version Application Version
     * @param tenantDomain Tenant of the application
     * @return Return URL of application version
     * @throws RepositoryMgtException
     */
    public String getURLForAppVersion(String applicationKey, String version, String tenantDomain)
            throws RepositoryMgtException;


}
