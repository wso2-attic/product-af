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
package org.wso2.carbon.appfactory.repository.mgt.client;

import org.wso2.carbon.appfactory.repository.mgt.RepositoryMgtException;

import java.io.File;

/**
 * Client specific to appfactory which sets url to scm:{svn or git}:http://...
 * Example usage:
 * <p/>
 * DefaultAgent defaultAgent = new SCMAgent(repoType , plexus);
 * DefaultRepositoryClient client = new DefaultRepositoryClient(defaultAgent);
 * File temp=new File("/path/to/file/to/checkout");
 * client.init("user","password");
 * client.doSomething();
 * client.close();
 */
public class DefaultRepositoryClient extends AppfactoryRepositoryClient {
    private DefaultAgent defaultAgent;

    public DefaultRepositoryClient(DefaultAgent defaultAgent) {
        this.defaultAgent = defaultAgent;
    }

    @Override
    public void init(String username, String password) throws RepositoryMgtException {
        defaultAgent.init(username, password);
    }

    @Override
    public void close() {
        defaultAgent.close();
    }

    @Override
    public boolean add(String remoteRepoUrl, File fileToAdd, boolean recursively, boolean updateOnly, File repositoryDirectory) throws RepositoryMgtException {
        if (recursively) {
            return defaultAgent.addRecursively(remoteRepoUrl, fileToAdd);
        } else {
            return defaultAgent.add(remoteRepoUrl, fileToAdd);
        }
    }

    @Override
    public boolean delete(String remoteRepoUrl, File fileToDelete, String msg, File repositoryDirectory) throws RepositoryMgtException {
        return defaultAgent.delete(remoteRepoUrl, fileToDelete, msg);
    }

    @Override
    public boolean branch(String remoteRepoUrl, String newBranchName, String branchFrom, File repoFile) throws RepositoryMgtException {
        return defaultAgent.branch(remoteRepoUrl, newBranchName, repoFile);
    }

    @Override
    public boolean checkOut(String remoteRepoUrl, String revision, File checkOutDirectory) throws RepositoryMgtException {
        return defaultAgent.checkOut(remoteRepoUrl, checkOutDirectory, revision);
    }

    /**
     * Not supported method. Implemented for the sake of consistency.
     *
     * @throws RepositoryMgtException
     */
    @Override
    @Deprecated
    public boolean commitLocally(String commitMsg, boolean all, File repositoryDirectory) throws RepositoryMgtException {
        throw new RepositoryMgtException("Not supported method : "+ this.getClass().getName());
    }

    /**
     * Not supported method. Implemented for the sake of consistency.
     *
     * @throws RepositoryMgtException
     */
    @Override
    @Deprecated
    public boolean pushLocalCommits(String remoteRepoUrl, String pushBranch, File repoFile) throws RepositoryMgtException {
        throw new RepositoryMgtException("Not supported method : "+ this.getClass().getName());
    }

    /**
     * Not supported method. Implemented for the sake of consistency.
     *
     * @throws RepositoryMgtException
     */
    @Override
    @Deprecated
    public boolean retireveMetadata(String remoteRepoUrl, boolean noCheckout, File metadataDirectory) throws RepositoryMgtException {
        throw new RepositoryMgtException("Not supported method : "+ this.getClass().getName());
    }

    @Override
    public boolean tag(String remoteRepoUrl, String version, String tagMsg, String revision, File repositoryDirectory) throws RepositoryMgtException {
        return defaultAgent.tag(remoteRepoUrl, version, revision);
    }

    @Override
    public boolean checkin(String remoteRepoUrl, File checkInDirectory, String msg, boolean forceCheckin) throws RepositoryMgtException {
        if (forceCheckin) {
            return defaultAgent.forceCheckIn(remoteRepoUrl, checkInDirectory, msg);
        } else {
            return defaultAgent.checkIn(remoteRepoUrl, checkInDirectory, msg);
        }
    }

    @Override
    public boolean branchCheckin(String remoteRepoUrl, File checkInDirectory, String msg, String targetVersion) throws RepositoryMgtException {
        return defaultAgent.checkIn(remoteRepoUrl, checkInDirectory, msg, targetVersion);
    }

    @Override
    public boolean fetchBranchToForkRepo(String forkRepoUrl, String mainRepoUrl, String branchToFetch, String newBranchName, File repoDir) throws RepositoryMgtException {
        throw new RepositoryMgtException("Not supported method : "+ this.getClass().getName());
    }
}