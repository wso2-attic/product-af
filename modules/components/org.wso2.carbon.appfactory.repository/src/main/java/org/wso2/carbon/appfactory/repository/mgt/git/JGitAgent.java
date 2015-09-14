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

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryMgtException;
import org.wso2.carbon.appfactory.repository.mgt.client.SCMAgent;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * JGit implementation of Git GitAgent
 */
public class JGitAgent implements GitAgent {
    private static final Log log = LogFactory.getLog(JGitAgent.class);
    private static final String STRING_CURRENT_DIR = ".";
    private static final String REMOTE_REF_PREFIX = "refs/remotes/origin/";
    private static final String HEAD_REF_PREFIX = "refs/heads/";
    private static final String FETCH_HEAD = "FETCH_HEAD";
    private String username;
    private String password;

    /**
     * Add file contents to the git index
     *
     * @param filePattern         repository-relative path of file/directory to add (with <code>/</code> as separator)
     * @param update              If set to true, the command only matches {@code filepattern} against already tracked
     *                            files in the index rather than the working tree. That means that it will never stage
     *                            new files, but that it will stage modified new contents of tracked files and that it
     *                            will remove files from the index if the corresponding files in the working tree have
     *                            been removed. In contrast to the git command line a {@code filepattern} must exist
     *                            also if update is set to true as there is no concept of a working directory here.
     * @param repositoryDirectory repository directory where .git exists.
     * @return success
     * @throws RepositoryMgtException if error while branching
     */
    public boolean add(String filePattern, boolean update, File repositoryDirectory) throws RepositoryMgtException {
        try {
            Repository repository = getLocalRepository(repositoryDirectory);
            Git git = new Git(repository);
            git.add().addFilepattern(filePattern)
                    .setUpdate(update)
                    .call();
            return true;
        } catch (IOException e) {
            String msg =
                    "Error while adding files due to " +
                            e.getMessage() + " from IOException";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        } catch (GitAPIException e) {
            String msg =
                    "Error while adding files due to " +
                            e.getMessage() + " from GitAPIException";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        }

    }

    /**
     * Add file contents to the git index
     *
     * @param fileToAdd           file to add
     * @param update              If set to true, the command only matches file pattern of the {@code fileToAdd} against already tracked
     *                            files in the index rather than the working tree. That means that it will never stage
     *                            new files, but that it will stage modified new contents of tracked files and that it
     *                            will remove files from the index if the corresponding files in the working tree have
     *                            been removed. In contrast to the git command line a {@code fileToAdd} must exist
     *                            also if update is set to true as there is no concept of a working directory here.
     * @param repositoryDirectory repository directory where .git exists.
     * @return success
     * @throws RepositoryMgtException if error while branching
     */
    @Override
    public boolean add(File fileToAdd, boolean update, File repositoryDirectory) throws RepositoryMgtException {
        String relativePath = getRelativePath(fileToAdd, repositoryDirectory);
        if (relativePath != null) {
            if (relativePath.isEmpty()) {
                relativePath = STRING_CURRENT_DIR;
            }
            return add(relativePath, update, repositoryDirectory);
        } else {
            String msg = "Given file/folder doesn't exists in the repository";
            log.error(msg);
            throw new RepositoryMgtException(msg);
        }

    }

    /**
     * Create branch
     *
     * @param remoteRepoUrl remote repository url
     * @param newBranchName new branch name
     * @param branchFrom    name of the branch that going to branch from
     * @param repoFile      repository directory where {@code branchFrom} exists. If not exists will get cloned using
     *                      {@code remoteRepoUrl}
     * @return success
     * @throws RepositoryMgtException if error while branching
     */
    public boolean branch(String remoteRepoUrl, String newBranchName, String branchFrom, File repoFile) throws RepositoryMgtException {
        try {
            Git gitRepo = getGitRepository(remoteRepoUrl, repoFile);
            gitRepo.branchCreate()
                    .setName(newBranchName)
                    .setStartPoint(branchFrom).call();
            return true;
        } catch (RepositoryMgtException e) {
            String msg =
                    "Error while creating branch : " + newBranchName + " due to " +
                            e.getMessage() + " from RepositoryMgtException";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        } catch (GitAPIException e) {
            String msg =
                    "Error while creating branch : " + newBranchName + " due to " +
                            e.getMessage() + " from GitAPIException";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        }
    }

    /**
     * Create branch form remote references
     *
     * @param remoteRepoUrl remote repository url
     * @param newBranchName new branch name
     * @param branchFrom    name of the branch that going to branch from
     * @param repoFile      repository directory where {@code branchFrom} exists. If not exists will get cloned using
     *                      {@code remoteRepoUrl}
     * @return success
     * @throws RepositoryMgtException if error while branching
     */
    @Override
    public boolean remoteBranch(String remoteRepoUrl, String newBranchName, String branchFrom, File repoFile) throws RepositoryMgtException {
        return branch(remoteRepoUrl,newBranchName,REMOTE_REF_PREFIX + branchFrom,repoFile);
    }

    /**
     * Create branch from local references (HEAD , FETCH_HEAD etc..)
     *
     * @param remoteRepoUrl remote repository url
     * @param newBranchName new branch name
     * @param branchFrom    name of the branch that going to branch from
     * @param repoFile      repository directory where {@code branchFrom} exists.
     * @return success
     * @throws RepositoryMgtException
     */
    @Override
    public boolean directBranch(String remoteRepoUrl, String newBranchName, String branchFrom, File repoFile) throws RepositoryMgtException {
        return branch(remoteRepoUrl,newBranchName,branchFrom,repoFile);
    }

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
     * @throws RepositoryMgtException if error while branching
     */
    @Override
    public boolean checkout(String remoteRepoUrl, String checkoutBranch, boolean createBranch, boolean forceCheckout, File repoFile) throws RepositoryMgtException {
        try {
            Git gitRepo = getGitRepository(remoteRepoUrl, repoFile);
            gitRepo.checkout()
                    .setName(checkoutBranch)
                    .setCreateBranch(createBranch)
                    .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                    .setStartPoint(checkoutBranch)
                    .setForce(forceCheckout)
                    .call();
            return true;
        } catch (RepositoryMgtException e) {
            String msg =
                    "Error while checkout : " + checkoutBranch + " due to " +
                            e.getMessage() + " from RepositoryMgtException";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        } catch (GitAPIException e) {
            String msg =
                    "Error while checkout : " + checkoutBranch + " due to " +
                            e.getMessage() + " from GitAPIException";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        }

    }

    /**
     * Clone a repository into {@code cloneDirectory}
     *
     * @param remoteRepoUrl  remote repository url
     * @param noCheckout     checkout or not
     * @param cloneDirectory Directory to clone
     * @return newly created {@code Git} object with associated repository
     * @throws RepositoryMgtException if error while cloning
     */
    public Git clone(String remoteRepoUrl, boolean noCheckout, File cloneDirectory) throws RepositoryMgtException {
        try {
            return Git.cloneRepository()
                    .setURI(remoteRepoUrl)
                    .setDirectory(cloneDirectory)
                    .setNoCheckout(noCheckout)
                    .setCredentialsProvider(getCredentialsProvider()).call();
        } catch (GitAPIException e) {
            String msg =
                    "Error while cloning the repository from : " + remoteRepoUrl + " due to " +
                            e.getMessage() + " from GitAPIException";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        }
    }

    /**
     * Clone a repository into {@code cloneDirectory}
     *
     * @param remoteRepoUrl  remote repository url
     * @param noCheckout     checkout or not
     * @param cloneDirectory Directory to clone
     * @return success
     * @throws RepositoryMgtException if error while cloning
     */
    @Override
    public boolean cloneRepo(String remoteRepoUrl, boolean noCheckout, File cloneDirectory) throws RepositoryMgtException {
        clone(remoteRepoUrl, noCheckout, cloneDirectory);
        return true;
    }


    /**
     * Git commit
     *
     * @param commitMsg           the commit message
     * @param all                 If set to true the Commit command automatically stages files that have been modified
     *                            and deleted, but new files not known by the repository are not affected.
     * @param repositoryDirectory repository directory where .git exists.
     * @return success
     * @throws RepositoryMgtException if error while branching
     */
    @Override
    public boolean commit(String commitMsg, boolean all, File repositoryDirectory) throws RepositoryMgtException {
        try {
            Repository repository = getLocalRepository(repositoryDirectory);
            Git git = new Git(repository);
            git.commit()
                    .setMessage(commitMsg)
                    .setAll(all)
                    .call();
            return true;
        } catch (IOException e) {
            String msg =
                    "Error while committing due to " +
                            e.getMessage() + " from IOException";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        } catch (GitAPIException e) {
            String msg =
                    "Error while committing due to " +
                            e.getMessage() + " from GitAPIException";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        }
    }

    /**
     * Fetch a branch to FETCH_HEAD
     *
     * @param remoteRepoUrl remote repository url
     * @param fetchBranch   branch to fetch
     * @param repoFile      repository directory where .git exists. If not exists will get cloned using {@code
     *                      remoteRepoUrl}
     * @return success
     * @throws RepositoryMgtException
     */
    public boolean fetch(String remoteRepoUrl, String fetchBranch, File repoFile) throws RepositoryMgtException {
        try {
            Git gitRepo = getGitRepository(remoteRepoUrl, repoFile);
            gitRepo.fetch().setRemote(remoteRepoUrl)
                    .setRefSpecs(new RefSpec(HEAD_REF_PREFIX + fetchBranch))
                    .setCredentialsProvider(getCredentialsProvider())
                    .call();
            return true;
        } catch (RepositoryMgtException e) {
            String msg =
                    "Error while fetching : " + fetchBranch + " due to " +
                            e.getMessage() + " from GitAPIException";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        } catch (GitAPIException e) {
            String msg =
                    "Error while fetching : " + fetchBranch + " due to " +
                            e.getMessage() + " from GitAPIException";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        }
    }

    /**
     * Add branch from {@code mainRepoUrl} to {@code forkRepoUrl}
     * @param forkRepoUrl   fork repo url
     * @param mainRepoUrl   main repo url
     * @param branchToFetch branch name the main repo to fetch
     * @param newBranchName new branch name for the forked repo
     * @param repoDir       Directory to clone and fetch
     * @return success
     * @throws RepositoryMgtException
     */
    public boolean fetchBranchToForkRepo(String forkRepoUrl , String mainRepoUrl , String branchToFetch , String newBranchName ,File repoDir) throws RepositoryMgtException {
        try {
            String branch = newBranchName;
            if(branch == null || branch.isEmpty()) {
                branch = branchToFetch;
            }
            clone(forkRepoUrl, true, repoDir);
            fetch(mainRepoUrl,branchToFetch,repoDir);
            branch(forkRepoUrl,branch,FETCH_HEAD,repoDir);
            return true;
        } catch (RepositoryMgtException e) {
            String msg =
                    "Error while forking repository branch : " + branchToFetch + " due to " +
                            e.getMessage() + " from RepositoryMgtException";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        }
    }

    /**
     * Push changes to remote repository
     *
     * @param remoteRepoUrl remote repository url
     * @param pushBranch    branch to push
     * @param repoFile      repository directory where .git exists. If not exists will get cloned using {@code
     *                      remoteRepoUrl}
     * @return success
     * @throws RepositoryMgtException if error while branching
     */
    @Override
    public boolean push(String remoteRepoUrl, String pushBranch, File repoFile) throws RepositoryMgtException {
        try {
            Git gitRepo = getGitRepository(remoteRepoUrl, repoFile);
            Iterable<PushResult> pushResults = gitRepo.push()
                    .setRemote(remoteRepoUrl)
                    .setRefSpecs(new RefSpec("refs/heads/" + pushBranch))
                    .setCredentialsProvider(getCredentialsProvider())
                    .call();
            // we need to verify if git push was successful. Here we can check RemoteRefUpdate status is rejected or not.
            boolean pushed = true;
            for (PushResult pushResult : pushResults) {
                if (pushResult.getRemoteUpdates().size() > 0) {
                    Collection<RemoteRefUpdate> refUpdates = pushResult.getRemoteUpdates();
                    if (refUpdates != null && refUpdates.size() > 0) {
                        for (RemoteRefUpdate refUpdate : refUpdates) {
                            if (refUpdate.getStatus() == RemoteRefUpdate.Status.REJECTED_OTHER_REASON ||
                                    refUpdate.getStatus() == RemoteRefUpdate.Status.REJECTED_NODELETE ||
                                    refUpdate.getStatus() == RemoteRefUpdate.Status.REJECTED_NONFASTFORWARD ||
                                    refUpdate.getStatus() == RemoteRefUpdate.Status.REJECTED_REMOTE_CHANGED) {
                                pushed = false;
                                log.warn("Failed to push artifacts on repo:" + remoteRepoUrl + " due to " +
                                        refUpdate.getMessage());
                                break;
                            }
                        }
                    }
                }
            }
            return pushed;
        } catch (RepositoryMgtException e) {
            String msg =
                    "Error while pushing  : " + pushBranch + " due to " +
                            e.getMessage() + " from RepositoryMgtException";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        } catch (GitAPIException e) {
            String msg =
                    "Error while pushing : " + pushBranch + " due to " +
                            e.getMessage() + " from GitAPIException";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        }

    }


    /**
     * Remove files from the working tree and from the index. If the deletion of the file had done by using other direct
     * file operation such as {@link org.apache.commons.io.FileUtils#deleteDirectory(java.io.File)}, use {@link
     * #add(String, boolean, java.io.File)} by passing the argument {@code update} as true
     *
     * @param filePattern         repository-relative path of file to remove (with <code>/</code> as separator)
     * @param repositoryDirectory repository directory where .git exists.
     * @return success
     * @throws RepositoryMgtException if error while branching
     */
    public boolean remove(String filePattern, File repositoryDirectory) throws RepositoryMgtException {
        try {
            Repository repository = getLocalRepository(repositoryDirectory);
            Git git = new Git(repository);
            git.rm().addFilepattern(filePattern).call();
            return true;
        } catch (IOException e) {
            String msg =
                    "Error while removing files due to " +
                            e.getMessage() + " from IOException";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        } catch (GitAPIException e) {
            String msg =
                    "Error while removing files due to " +
                            e.getMessage() + " from IOException";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        }
    }

    /**
     * Remove files from the working tree and from the index. If the deletion of the file had done by using other direct
     * file operation such as {@link org.apache.commons.io.FileUtils#deleteDirectory(java.io.File)}, use {@link
     * #add(String, boolean, java.io.File)} by passing the argument {@code update} as true
     *
     * @param fileToDelete        file/folder to delete
     * @param repositoryDirectory repository directory where .git exists.
     * @return success
     * @throws RepositoryMgtException if error while branching
     */
    @Override
    public boolean remove(File fileToDelete, File repositoryDirectory) throws RepositoryMgtException {
        String relativePath = getRelativePath(fileToDelete, repositoryDirectory);
        if (relativePath != null) {
            if (relativePath.isEmpty()) {
                relativePath = STRING_CURRENT_DIR;
            }
            return remove(relativePath, repositoryDirectory);
        } else {
            String msg = "Given file/folder doesn't exists in the repository";
            log.error(msg);
            throw new RepositoryMgtException(msg);
        }

    }

    @Override
    public void setCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }


    /**
     * Git tag
     *
     * @param version             the tag name used for the {@code tag}
     * @param tagMsg              the tag message used for the {@code tag}
     * @param repositoryDirectory repository directory where .git exists.
     * @return success
     * @throws RepositoryMgtException if error while branching
     */
    @Override
    public boolean tag(String version, String tagMsg, File repositoryDirectory) throws RepositoryMgtException {
        try {
            Repository repository = getLocalRepository(repositoryDirectory);
            Git git = new Git(repository);
            git.tag()
                .setMessage(tagMsg)
                .setName(version)
                .call();
            return true;
        } catch (IOException e) {
            String msg = "Error while tagging :" + version + " due to " +
                    e.getMessage() + " from IOException";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        } catch (GitAPIException e) {
            String msg = "Error while tagging :" + version + " due to " +
                    e.getMessage() + " from IOException";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        }
    }

    /**
     * Get credentials
     *
     * @return credential provider
     */
    private UsernamePasswordCredentialsProvider getCredentialsProvider() {
        return new UsernamePasswordCredentialsProvider(username,password);
    }

    /**
     * Get Git repository
     *
     * @param remoteRepoUrl       remote repository url
     * @param repositoryDirectory repository directory to look for .git file
     * @return newly created {@code Git} object with associated repository
     * @throws RepositoryMgtException if repository is not in the checkout directory and cannot clone form {@code
     *                                remoteRepoUrl}
     */
    private Git getGitRepository(String remoteRepoUrl, File repositoryDirectory) throws RepositoryMgtException {

        Git git;
        try {
            Repository repository = getLocalRepository(repositoryDirectory);
            git = new Git(repository);
        } catch (IOException e) {               // There is no git repository inside the @repositoryDirectory
            try {
                FileUtils.deleteDirectory(repositoryDirectory);
                FileUtils.forceMkdir(repositoryDirectory);
                // Try clone
                git = clone(remoteRepoUrl, true, repositoryDirectory);
            } catch (IOException e1) {
                String msg = "Error in deleting working directory to clone : " + repositoryDirectory;
                log.error(msg, e1);
                throw new RepositoryMgtException(msg, e1);
            } catch (RepositoryMgtException e1) {
                String msg = "Repository not found in : " + repositoryDirectory.getName();
                log.error(msg, e1);
                throw new RepositoryMgtException(msg, e1);
            }
        }
        return git;
    }

    /**
     * Get local repository
     *
     * @param repositoryDir local repo directory
     * @return a repository matching this configuration.
     * @throws IOException if there is no git repo in the given {@code repositoryDir}
     */
    private Repository getLocalRepository(File repositoryDir) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        return builder.addCeilingDirectory(repositoryDir)
                .setWorkTree(repositoryDir).findGitDir(repositoryDir)
                .setMustExist(true).build();
    }

    /**
     * Get relative path of {@code file} against {@code baseFile}
     *
     * @param file     The file to be relativized against {@code baseFile}
     * @param baseFile base file
     * @return null if {@code file} isn't relative to {@code baseFile}, else the relative path.
     */
    private String getRelativePath(File file, File baseFile) {
        String relativePath = null;
        URI baseUri = baseFile.toURI();
        URI fileUri = file.toURI();
        if (fileUri.getPath().startsWith(baseUri.getPath())) {
            relativePath = baseUri.relativize(fileUri).getPath();
        }
        return relativePath;
    }
}
