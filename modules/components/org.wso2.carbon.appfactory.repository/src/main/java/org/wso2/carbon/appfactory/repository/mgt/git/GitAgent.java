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

import java.io.File;

/**
 * Interface for git clients
 */
public interface GitAgent {

    /**
     * Add file contents to the git index
     *
     * @param fileToAdd           file to add
     * @param update              If set to true, the command only matches file pattern of the {@code fileToAdd} against already tracked
     *                            files in the index rather than the working tree.
     * @param repositoryDirectory repository directory where .git exists.
     * @return success
     * @throws RepositoryMgtException
     */
    public boolean add(File fileToAdd, boolean update, File repositoryDirectory)
            throws RepositoryMgtException;

    /**
     * Clone a repository into {@code cloneDirectory}
     *
     * @param remoteRepoUrl  remote repository url
     * @param noCheckout     checkout or not
     * @param cloneDirectory Directory to clone
     * @return success
     * @throws RepositoryMgtException if error while cloning
     */
    public boolean cloneRepo(String remoteRepoUrl, boolean noCheckout, File cloneDirectory)
            throws RepositoryMgtException;

    /**
     * Create branch from remote references
     *
     * @param remoteRepoUrl remote repository url
     * @param newBranchName new branch name
     * @param branchFrom    name of the branch that going to branch from
     * @param repoFile      repository directory where {@code branchFrom} exists.
     * @return success
     * @throws RepositoryMgtException
     */
    public boolean remoteBranch(String remoteRepoUrl, String newBranchName, String branchFrom, File repoFile)
            throws RepositoryMgtException;

    /**
     * Create branch from local references
     *
     * @param remoteRepoUrl remote repository url
     * @param newBranchName new branch name
     * @param branchFrom    name of the branch that going to branch from
     * @param repoFile      repository directory where {@code branchFrom} exists.
     * @return success
     * @throws RepositoryMgtException
     */
    public boolean directBranch(String remoteRepoUrl, String newBranchName, String branchFrom, File repoFile)
            throws RepositoryMgtException;


    /**
     * Checkout a branch
     *
     * @param remoteRepoUrl  remote repository url
     * @param checkoutBranch branch or commit to checkout.
     * @param createBranch   Specify whether to create a new branch while checking out
     * @param forceCheckout  force checkout
     * @param repoFile       repository directory where .git exists. If not exists will get cloned using {@code
     *                       remoteRepoUrl}
     * @return success
     * @throws RepositoryMgtException
     */
    public boolean checkout(String remoteRepoUrl, String checkoutBranch, boolean createBranch, boolean forceCheckout, File repoFile)
            throws RepositoryMgtException;


    /**
     * Add branch from {@code mainRepoUrl} to {@code forkRepoUrl}
     *
     * @param forkRepoUrl   fork repo url
     * @param mainRepoUrl   main repo url
     * @param branchToFetch branch name the main repo to fetch
     * @param newBranchName new branch name for the forked repo
     * @param repoDir       Directory to clone and fetch
     * @return success
     * @throws RepositoryMgtException
     */
    public boolean fetchBranchToForkRepo(String forkRepoUrl , String mainRepoUrl , String branchToFetch , String newBranchName ,File repoDir)
            throws RepositoryMgtException;

    /**
     * Push changes to remote repository
     *
     * @param remoteRepoUrl remote repository url
     * @param pushBranch    branch to
     * @param repoFile      repository directory where .git exists. If not exists will get cloned using {@code
     *                      remoteRepoUrl}
     * @return success
     * @throws RepositoryMgtException if error while branching
     */
    public boolean push(String remoteRepoUrl, String pushBranch, File repoFile) throws RepositoryMgtException;

    /**
     * Remove files from the working tree and from the index. See the implementation for more details
     *
     * @param fileToDelete        file/folder to delete
     * @param repositoryDirectory repository directory where .git exists.
     * @return success
     * @throws RepositoryMgtException if error while branching
     */
    public boolean remove(File fileToDelete, File repositoryDirectory)
            throws RepositoryMgtException;

    /**
     * Git commit
     *
     * @param commitMsg           the commit message
     * @param all                 If set to true the Commit command automatically stages files that have been modified
     *                            and deleted, but new files not known by the repository are not affected.
     * @param repositoryDirectory repository directory where .git exists.
     * @return success
     * @throws RepositoryMgtException if error while pushing to remote
     */
    public boolean commit(String commitMsg, boolean all, File repositoryDirectory)
            throws RepositoryMgtException;

    /**
     * Git tag
     *
     * @param version             the tag name used for the {@code tag}
     * @param tagMsg              the tag message used for the {@code tag}
     * @param repositoryDirectory repository directory where .git exists.
     * @return success
     * @throws RepositoryMgtException if error while branching
     */
    public boolean tag(String version, String tagMsg, File repositoryDirectory)
            throws RepositoryMgtException;

    public void setCredentials(String username, String password);
}
