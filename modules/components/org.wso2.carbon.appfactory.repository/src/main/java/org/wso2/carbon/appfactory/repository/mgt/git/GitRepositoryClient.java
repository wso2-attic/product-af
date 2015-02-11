/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
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
package org.wso2.carbon.appfactory.repository.mgt.git;

import org.wso2.carbon.appfactory.repository.mgt.RepositoryMgtException;
import org.wso2.carbon.appfactory.repository.mgt.client.AppfactoryRepositoryClient;

import java.io.File;

/**
 * Git Repository Client
 */
public class GitRepositoryClient extends AppfactoryRepositoryClient {
    private GitAgent gitAgent;

    public GitRepositoryClient(GitAgent gitAgent) {
        this.gitAgent = gitAgent;
    }

    /**
     * Clone a repository into {@code metadataDirectory}.
     *
     * @see org.wso2.carbon.appfactory.repository.mgt.client.AppfactoryRepositoryClient#retireveMetadata(String, boolean, java.io.File)
     */
    @Override
    public boolean retireveMetadata(String remoteRepoUrl, boolean noCheckout, File metadataDirectory) throws RepositoryMgtException {
        return gitAgent.cloneRepo(remoteRepoUrl, noCheckout, metadataDirectory);
    }

    @Override
    public boolean branch(String remoteRepoUrl, String newBranchName, String branchFrom, File repoFile) throws RepositoryMgtException {
        return gitAgent.remoteBranch(remoteRepoUrl, newBranchName, branchFrom, repoFile);
    }

    @Override
    public boolean tag(String remoteRepoUrl, String version, String tagMsg, String revision, File repositoryDirectory) throws RepositoryMgtException {
        return gitAgent.tag(version,tagMsg,repositoryDirectory);
    }

    @Override
    public boolean checkOut(String remoteRepoUrl, String revision, File checkOutDirectory) throws RepositoryMgtException {
        return gitAgent.checkout(remoteRepoUrl, revision, false, true, checkOutDirectory);
    }

    @Override
    public void init(String username, String password) throws RepositoryMgtException {
        gitAgent.setCredentials(username,password);
    }

    @Override
    public void close() {

    }

    @Override
    public boolean add(String remoteRepoUrl, File fileToAdd, boolean recursively, boolean updateOnly, File repositoryDirectory) throws RepositoryMgtException {
        return gitAgent.add(fileToAdd ,updateOnly,repositoryDirectory);

    }

    @Override
    public boolean commitLocally(String commitMsg, boolean all, File repositoryDirectory) throws RepositoryMgtException {
        return gitAgent.commit(commitMsg, all, repositoryDirectory);
    }

    @Override
    public boolean pushLocalCommits(String remoteRepoUrl, String pushBranch, File repoFile) throws RepositoryMgtException {
        return gitAgent.push(remoteRepoUrl, pushBranch, repoFile);
    }

    @Override
    public boolean delete(String remoteRepoUrl, File fileToDelete, String msg, File repositoryDirectory) throws RepositoryMgtException {
        return gitAgent.remove(fileToDelete,repositoryDirectory);
    }

    /**
     * Not supported method. Implemented for the sake of consistency.
     *
     * @throws RepositoryMgtException
     */
    @Override
    @Deprecated
    public boolean checkin(String remoteRepoUrl, File checkInDirectory, String msg, boolean forceCheckin) throws RepositoryMgtException {
        throw new RepositoryMgtException("Not supported method : "+ this.getClass().getName());
    }

    /**
     * Not supported method. Implemented for the sake of consistency.
     *
     * @throws RepositoryMgtException
     */
    @Override
    @Deprecated
    public boolean branchCheckin(String remoteRepoUrl, File checkInDirectory, String msg, String targetVersion) throws RepositoryMgtException {
        throw new RepositoryMgtException("Not supported method : "+ this.getClass().getName());
    }

    @Override
    public boolean fetchBranchToForkRepo(String forkRepoUrl, String mainRepoUrl, String branchToFetch, String newBranchName, File repoDir) throws RepositoryMgtException {
        return gitAgent.fetchBranchToForkRepo(forkRepoUrl,mainRepoUrl,branchToFetch,newBranchName,repoDir);
    }

}