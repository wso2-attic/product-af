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
 * Abstract class for represent Repository client for AppFactory
 * Example usage:
 * <p/>
 * AppfactoryRepositoryClient client=new ConcreteAppfactoryRepositoryClient(clientAgent);
 * File temp=new File("/path/to/file/to/checkout");
 * client.init("user","password");
 * client.doSomeOperations();
 * client.close();
 */
public abstract class AppfactoryRepositoryClient {

    /**
     * Set credentials
     *
     * @param username username
     * @param password password
     * @throws RepositoryMgtException
     */
    public abstract void init(String username, String password) throws RepositoryMgtException;

    public abstract void close();

    /**
     * Add to VCS
     *
     * @param remoteRepoUrl       remote repository url
     * @param fileToAdd           file to add
     * @param recursively         not valid for git
     * @param updateOnly          not valid for svn
     * @param repositoryDirectory repository directory
     * @return success
     * @throws RepositoryMgtException
     */
    public abstract boolean add(String remoteRepoUrl, File fileToAdd, boolean recursively, boolean updateOnly, File repositoryDirectory) throws RepositoryMgtException;

    /**
     * Delete from VCS
     *
     * @param remoteRepoUrl remote repository url
     * @param fileToDelete  file to delete
     * @param msg           delete msg
     * @return success
     * @throws RepositoryMgtException
     */
    public abstract boolean delete(String remoteRepoUrl, File fileToDelete, String msg, File repositoryDirectory) throws RepositoryMgtException;

    /**
     * Create branch
     *
     * @param remoteRepoUrl remote repository url
     * @param newBranchName new branch name
     * @param branchFrom    name of the branch that going to branch from
     * @param repoFile      repository directory where {@code branchFrom} exists.
     * @return success
     */
    public abstract boolean branch(String remoteRepoUrl, String newBranchName, String branchFrom, File repoFile)
            throws RepositoryMgtException;

    /**
     * Checkout a branch
     *
     * @param remoteRepoUrl     remote repository url
     * @param revision          revision, branch or commit to checkout.
     * @param checkOutDirectory repository directory
     * @return success
     */
    public abstract boolean checkOut(String remoteRepoUrl, String revision, File checkOutDirectory)
            throws RepositoryMgtException;

    /**
     * Commit locally May support only for some Version Controller Systems.(like Git)
     *
     * @param commitMsg           the commit message
     * @param all                 If set to true the Commit command automatically stages files that have been modified
     *                            and deleted, but new files not known by the repository are not affected.
     * @param repositoryDirectory repository directory where .git exists.
     * @return success
     * @throws RepositoryMgtException if error while branching or when invocation is invalid
     */
    public abstract boolean commitLocally(String commitMsg, boolean all, File repositoryDirectory)
            throws RepositoryMgtException;

    /**
     * Send local commits to remote May support only for some Version Controller Systems.(like Git)
     *
     * @param remoteRepoUrl remote repository url
     * @param pushBranch    branch to
     * @param repoFile      repository directory where .git exists. If not exists will get cloned using {@code
     *                      remoteRepoUrl}
     * @return success
     * @throws RepositoryMgtException if error while pushing to remote or when invocation is invalid
     */
    public abstract boolean pushLocalCommits(String remoteRepoUrl, String pushBranch, File repoFile) throws RepositoryMgtException;

    /**
     * Retrieve and save repository metadata into {@code metadataDirectory} May support only for some Version Controller
     * Systems.(like Git)
     *
     * @param remoteRepoUrl     remote repository url
     * @param noCheckout        checkout or not
     * @param metadataDirectory Directory to save metadata
     * @return newly created {@code Git} object with associated repository
     */
    public abstract boolean retireveMetadata(String remoteRepoUrl, boolean noCheckout, File metadataDirectory)
            throws RepositoryMgtException;

    /**
     * Checkin to a repository May support only for some Version Controller Systems (like svn).
     *
     *
     * @param remoteRepoUrl     remote repository url
     * @param checkInDirectory  directory to checkin
     * @param msg               checkin/commit msg
     * @param forceCheckin      whether to do force checkin or not
     * @throws RepositoryMgtException
     */
    public abstract boolean checkin(String remoteRepoUrl, File checkInDirectory, String msg, boolean forceCheckin)
            throws RepositoryMgtException;

    /**
     * Checkin Check in to a branch. May support only for some Version Controller Systems (like svn).
     *
     *
     * @param remoteRepoUrl     remote repository url
     * @param checkInDirectory  directory to checkin
     * @param msg               checkin/commit msg
     * @param targetVersion     target version to checkin
     * @return success
     * @throws RepositoryMgtException
     */
    public abstract boolean branchCheckin(String remoteRepoUrl, File checkInDirectory, String msg, String targetVersion)
            throws RepositoryMgtException;

    /**
     * Add branch from {@code mainRepoUrl} to {@code forkRepoUrl}
     * May support only for some Version Controller Systems.(like Git)
     *
     * @param forkRepoUrl   fork repo url
     * @param mainRepoUrl   main repo url
     * @param branchToFetch branch name the main repo to fetch
     * @param newBranchName new branch name for the forked repo
     * @param repoDir       Directory to clone and fetch
     * @return success
     * @throws RepositoryMgtException
     */
    public abstract boolean fetchBranchToForkRepo(String forkRepoUrl , String mainRepoUrl , String branchToFetch , String newBranchName ,File repoDir)
            throws RepositoryMgtException;

    /**
     * Tag
     *
     * @param remoteRepoUrl       remote repository url
     * @param version             the tag name
     * @param tagMsg              the tag message
     * @param revision            Revision number to tag
     * @param repositoryDirectory repository directory
     * @return success
     * @throws RepositoryMgtException
     */
    public abstract boolean tag(String remoteRepoUrl, String version, String tagMsg, String revision, File repositoryDirectory)
            throws RepositoryMgtException;
}
