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

package org.wso2.carbon.appfactory.s4.integration;


import org.wso2.carbon.appfactory.common.AppFactoryException;

/**
 * Every repository provider for S2 should implement this interface.
 *
 */
public interface RepositoryProvider {

    /**
     * Sets the base URL of the repository server. Repositories will be created on the given server.
     * @param baseUrl the base URL of the repository server
     */
    public void setBaseUrl(String baseUrl);

    /**
     * Sets the admin user name of the repository server
     * @param adminUsername the admin user name
     */
    public void setAdminUsername(String adminUsername);

    /**
     * Sets the admin password of the repository server
     * @param adminPassword the admin password
     */
    public void setAdminPassword(String adminPassword);

    /**
     * Sets the name of the repository to be created
     * @param repoName name of the repository
     */
    public void setRepoName(String repoName);
    /**
     * Create a repository in the given space
     *
     * @return url for created repository
     * @throws AppFactoryException when repository creation fails
     */
    public String createRepository() throws AppFactoryException;

    public boolean isRepoExist() throws AppFactoryException;
}
