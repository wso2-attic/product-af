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
package org.wso2.carbon.appfactory.repository.mgt.client;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.scm.*;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.branch.BranchScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.mkdir.MkdirScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.embed.Embedder;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryMgtException;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Repository client that wrapping maven SCM plugin
 */
public class SCMAgent implements DefaultAgent {
	private static final Log log = LogFactory.getLog(SCMAgent.class);
	private static final String BASE_WORKING_DIR_NAME = "scmclient";
    private StringBuilder repositoryType;
	private ScmManager scmManager;
	private Embedder plexus;
	private String username;
	private String password;

	/**
	 * Maven embedder should be initialized per jvm and assigned to client
	 *
	 * @param embedder
	 */
	public SCMAgent(String repoType, Embedder embedder) {
        this.repositoryType = new StringBuilder("scm");
        repositoryType.append(":").append(repoType).append(":");
		// creating plexus per client
		plexus = embedder;
	}

	/**
	 * Initialize the client with username and password
	 *
	 * @param username
	 * @param password
	 * @throws RepositoryMgtException
	 */
	@Override
    public void init(String username, String password) throws RepositoryMgtException {
		this.username = username;
		this.password = password;
	}

	@Override
    public void close() {
		// Do nothing
	}

	private File getWorkingDirectory() throws RepositoryMgtException {
		// working directory should be unique to allow parallel operation
		File temp = getFileWithRandomName();
		if (!temp.exists()) {
			if (!temp.mkdirs()) {
				String msg = "Error in creating working directory";
				log.error(msg);
				throw new RepositoryMgtException(msg);
			}
		}
		// generating random name,so there is no chance to get directory that already exists
		return temp;
	}

	private File getFileWithRandomName() {
		String randomFileName = UUIDGenerator.generateUUID();
		return new File(CarbonUtils.getTmpDir() + File.separator + BASE_WORKING_DIR_NAME + File.separator +
		                randomFileName);
	}

	/**
	 * Create a directory in repository
	 *
	 * @param baseURL
	 *            repository url
	 * @param dirName
	 *            directory name to create
	 * @return true if the operation is successful
	 * @throws RepositoryMgtException
	 */
	public boolean mkdir(String baseURL, String dirName) throws RepositoryMgtException {
		MkdirScmResult result;
		ScmRepository repository = getRepository(baseURL);
		ScmFileSet fileSet;
		File workDir = getWorkingDirectory();
		fileSet = new ScmFileSet(workDir, new File(dirName));
		try {
			result = scmManager.mkdir(repository, fileSet, "creating directory" + dirName, false);
		} catch (ScmException e) {
			String msg = "Could not able to execute mkdir on " + baseURL;
			log.error(msg, e);
			throw new RepositoryMgtException(msg, e);
		}
		return processResult(result, workDir);
	}

	private ScmRepository getRepository(String url) throws RepositoryMgtException {
		ScmRepository repository;
		scmManager = null;
		try {
			scmManager = (ScmManager) plexus.lookup(ScmManager.ROLE);
			repository = scmManager.makeScmRepository(url);
			repository.getProviderRepository().setUser(username);
			repository.getProviderRepository().setPassword(password);
			repository.getProviderRepository().setPushChanges(true);
		} catch (ScmRepositoryException e) {
			String msg = "Could not able to create repository object";
			log.error(msg, e);
			throw new RepositoryMgtException(msg, e);
		} catch (NoSuchScmProviderException e) {
			String msg = "There is no repository provider for " + url + " install required dependencies";
			log.error(msg, e);
			throw new RepositoryMgtException(msg, e);
		} catch (ComponentLookupException e) {
			String msg = "Error in looking up ScmManager";
			log.error(msg, e);
			throw new RepositoryMgtException(msg, e);
		}
		return repository;
	}

	/**
	 * Branch the repository to a particular version
	 *
	 * @param baseURL
	 *            repository url
	 * @param version
	 *            name of the branch
	 * @param checkOutDirectoryToBranch
	 *            Directory contains code to check in into new branch
	 * @return
	 * @throws RepositoryMgtException
	 */
	@Override
    public boolean branch(String baseURL, String version, String checkOutDirectoryToBranch)
	                                                                                       throws RepositoryMgtException {
		BranchScmResult result;
		File checkOutDirectory = new File(checkOutDirectoryToBranch);
		return branch(baseURL,version,checkOutDirectory);
	}

    /**
     * Branch the repository to a particular version
     *
     * @param baseURL
     *            repository url
     * @param version
     *            name of the branch
     * @param checkOutDirectory
     *            Directory contains code to check in into new branch
     * @return
     * @throws RepositoryMgtException
     */
    @Override
    public boolean branch(String baseURL, String version, File checkOutDirectory)
            throws RepositoryMgtException {
        baseURL = getSCMURL(baseURL);
        BranchScmResult result;
        ScmRepository repository = getRepository(baseURL);
        try {
            result = scmManager.branch(repository, new ScmFileSet(checkOutDirectory), version);
        } catch (ScmException e) {
            String msg = "Error in executing branch operation on " + baseURL;
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        }
        return processResult(result, null);
    }

	/**
	 * Gives the status of files in @param checkOutDirectory
	 *
	 * @param baseURL
	 *            Repository url
	 * @param version
	 *            Branch name
	 * @param checkOutDirectoryToBranch
	 *            Directory contains files to be compared against the source repository
	 * @return
	 * @throws RepositoryMgtException
	 */
	private StatusScmResult status(String baseURL, String version, String checkOutDirectoryToBranch)
	                                                                                                throws RepositoryMgtException {
		StatusScmResult result;
		File checkOutDirectory = new File(checkOutDirectoryToBranch);
		ScmRepository repository = getRepository(baseURL);
		try {
			result = scmManager.status(repository, new ScmFileSet(checkOutDirectory));
			result.getChangedFiles().get(1).getStatus().toString();
		} catch (ScmException e) {
			String msg = "Error in executing branch operation on " + baseURL;
			log.error(msg, e);
			throw new RepositoryMgtException(msg, e);
		}
		return result;
	}

	/**
	 * Tag the repository with a name
	 *
	 * @param baseURL
	 *            Repository url
	 * @param version
	 *            name of the tag
	 * @param revision
	 *            Revision number to tag
	 * @return
	 * @throws RepositoryMgtException
	 */
	@Override
    public boolean tag(String baseURL, String version, String revision) throws RepositoryMgtException {
        baseURL = getSCMURL(baseURL);
		TagScmResult result = null;
		ScmRepository repository = getRepository(baseURL);
		File checkOutDirectory = getWorkingDirectory();
		try {
			// look at comment in branch,same is true here
			CheckOutScmResult checkOutScmResult =
			                                      scmManager.checkOut(repository, new ScmFileSet(checkOutDirectory),
			                                                          new ScmRevision(revision));
			if (checkOutScmResult.getProviderMessage() == null) {
				result = scmManager.tag(repository, new ScmFileSet(checkOutDirectory), version);
			}
		} catch (ScmException e) {
			String msg = "Error in executing tag operation on " + baseURL;
			log.error(msg, e);
			throw new RepositoryMgtException(msg, e);
		}
		return processResult(result, checkOutDirectory);
	}

	/**
	 * Checkout a repository to local file system
	 *
	 * @param url
	 *            Repository URL
	 * @param checkOutDirectory
	 *            Directory to checkout
	 * @param revision
	 *            Revision to checkout
	 * @return
	 * @throws RepositoryMgtException
	 */
	@Override
    public boolean checkOut(String url, File checkOutDirectory, String revision) throws RepositoryMgtException {
        url = getSCMURL(url);
		if (AppFactoryConstants.TRUNK.equals(revision)) {
			revision = "";
		}

		CheckOutScmResult checkOutScmResult;
		ScmRepository repository = getRepository(url);
		try {
			checkOutScmResult =
			                    scmManager.checkOut(repository, new ScmFileSet(checkOutDirectory),
			                                        new ScmRevision(revision));
		} catch (ScmException e) {
			String msg = "Error in executing checkout operation on " + url;
			log.error(msg, e);
			throw new RepositoryMgtException(msg, e);
		}
		return processResult(checkOutScmResult, null);
	}

	/**
	 * Check in files in local file system to repository
	 *
	 * @param url
	 *            Repository URL
	 * @param checkInDirectory
	 *            Directory contains file to checkin
	 * @param msg
	 *            Message
	 * @return
	 * @throws RepositoryMgtException
	 */
	@Override
    public boolean checkIn(String url, File checkInDirectory, String msg) throws RepositoryMgtException {
        url = getSCMURL(url);
		CheckInScmResult checkInScmResult;
		ScmRepository repository = getRepository(url);
		repository.getProviderRepository().setPushChanges(true);
		try {
			checkInScmResult = scmManager.checkIn(repository, new ScmFileSet(checkInDirectory), msg);
		} catch (ScmException e) {
			String message = "Error in executing checkIn operation on " + url;
			log.error(msg, e);
			throw new RepositoryMgtException(message, e);
		}
		return processResult(checkInScmResult, null);
	}

	/**
	 * Do the commit with add/delete the resource to the repository
	 *
	 * @param url
	 * @param checkInDirectory
	 * @param msg
	 * @return
	 * @throws RepositoryMgtException
	 */
	@Override
    public boolean forceCheckIn(String url, File checkInDirectory, String msg) throws RepositoryMgtException {
        url = getSCMURL(url);
		CheckInScmResult checkInScmResult;
		ScmRepository repository = getRepository(url);
		repository.getProviderRepository().setPushChanges(true);
		try {
			StatusScmResult statusScmResult = scmManager.status(repository, new ScmFileSet(checkInDirectory));
			List<ScmFile> scmFiles = statusScmResult.getChangedFiles();

			for (ScmFile scmFile : scmFiles) {
				if (scmFile.getStatus() == ScmFileStatus.UNKNOWN) {
					addRecursively(url, checkInDirectory.getAbsolutePath() + "/" + scmFile.getPath());
				} else if (scmFile.getStatus() == ScmFileStatus.MISSING) {
					delete(url, new File(checkInDirectory.getAbsolutePath() + "/" + scmFile.getPath()), msg);
				}
			}

			checkInScmResult = scmManager.checkIn(repository, new ScmFileSet(checkInDirectory), msg);
		} catch (ScmException e) {
			String message = "Error in executing checkIn operation on " + url;
			log.error(msg, e);
			throw new RepositoryMgtException(message, e);
		}
		return processResult(checkInScmResult, null);
	}

	/**
	 * Helper method to delete the working directory
	 * and process the result of operation
	 *
	 * @param result
	 * @param workingDirectory
	 * @return
	 * @throws RepositoryMgtException
	 */
	private boolean processResult(ScmResult result, File workingDirectory) throws RepositoryMgtException {

		deleteWorkingDirectory(workingDirectory);

		boolean success = false;
		if (result != null) {
			if (result.isSuccess()) {
				success = true;
			} else {
				log.error("Error in executing command  CommandLine error is  " + result.getProviderMessage());
				log.error("Error in executing git command : " + result.getCommandOutput());
			}
		}
		return success;
	}

	private void deleteWorkingDirectory(File workingDirectory) throws RepositoryMgtException {
		if (workingDirectory != null) {
			try {
				FileUtils.deleteDirectory(workingDirectory);
			} catch (IOException e) {
				String msg = "Error in deleting working directory " + workingDirectory;
				log.error(msg, e);
				throw new RepositoryMgtException(msg, e);
			}
		}
	}

	/**
	 * Method to add files to the given svn repository.
	 *
	 * @param url
	 *            the svn repository url
	 * @param currentFile
	 *            the file that needs to be added to the svn repository
	 * @return processResult Result in executing command
	 * @throws RepositoryMgtException
	 *             if add operation in SCM
	 */
	@Override
    public boolean add(String url, File currentFile) throws RepositoryMgtException {
        url = getSCMURL(url);
		AddScmResult addScmResult;
		ScmFileSet fileSet;
		ScmRepository repository = getRepository(url);

		ArrayList<File> childrenList = new ArrayList<File>();
		File[] files = currentFile.listFiles();

		if (files != null && files.length > 0) {
			childrenList.add(currentFile);
			Collections.addAll(childrenList, files);
			fileSet = new ScmFileSet(currentFile.getParentFile(), childrenList);
		} else {
			fileSet = new ScmFileSet(currentFile.getParentFile(), currentFile);
		}

		try {
			addScmResult = scmManager.add(repository, fileSet);
		} catch (ScmException e) {
			String msg = "Error in executing add operation on " + url;
			log.error(msg, e);
			throw new RepositoryMgtException(msg, e);
		}
		return processResult(addScmResult, null);
	}

	/**
	 * Method to delete files to the given repository.
	 *
	 * @param url
	 *            the repository url
	 * @param currentFile
	 *            the file that needs to be added to the repository
	 * @return processResult Result in executing command
	 * @throws RepositoryMgtException
	 *             if add operation in SCM
	 */
	@Override
    public boolean delete(String url, File currentFile, String msg) throws RepositoryMgtException {
        url = getSCMURL(url);
		ScmResult scmResult;
		ScmFileSet fileSet;

		ScmRepository repository = getRepository(url);

		ArrayList<File> childrenList = new ArrayList<File>();
		File[] files = currentFile.listFiles();

		if (files != null && files.length > 0) {
			childrenList.add(currentFile);
			Collections.addAll(childrenList, files);
			fileSet = new ScmFileSet(currentFile.getParentFile(), childrenList);
		} else {
			fileSet = new ScmFileSet(currentFile.getParentFile(), currentFile);
		}

		try {
			scmResult = scmManager.remove(repository, fileSet, msg);

		} catch (ScmException e) {
			String errorMsg = "Error in executing add operation on " + url;
			log.error(errorMsg, e);
			throw new RepositoryMgtException(errorMsg, e);

		}
		return processResult(scmResult, null);
	}

	/**
	 * Method to add files to the given svn repository.
	 * Note that this method adds all the sub files and folders to the repo as well.
	 *
	 * @param url
	 *            the svn repository url
	 * @param currentFilePath
	 *            the file that needs to be added to the svn repository
	 * @return processResult Result in executing command
	 * @throws RepositoryMgtException
	 *             if add operation fails
	 */
	@Override
    public boolean addRecursively(String url, String currentFilePath) throws RepositoryMgtException {
		File currentFile = new File(currentFilePath);
        return addRecursively(url,currentFile);

	}

    /**
     * Method to add files to the given svn repository.
     * Note that this method adds all the sub files and folders to the repo as well.
     *
     * @param url
     *            the svn repository url
     * @param currentFile
     *            the file that needs to be added to the svn repository
     * @return processResult Result in executing command
     * @throws RepositoryMgtException
     *             if add operation fails
     */
    @Override
    public boolean addRecursively(String url, File currentFile)
            throws RepositoryMgtException {

        url = getSCMURL(url);
        ScmRepository repository = getRepository(url);

        boolean outcome = true;

        if (currentFile.isDirectory()) {

            File[] onlySubDirectries = currentFile.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory() && isValidName(pathname.getName());
                }
            });

            File[] onlyFiles = currentFile.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile() && isValidName(pathname.getName());
                }
            });

            try {
                List<File> allChildren = new ArrayList<File>();
                Collections.addAll(allChildren, onlySubDirectries);
                Collections.addAll(allChildren, onlyFiles);

                if (!allChildren.isEmpty()) {
                    ScmFileSet allFilesInCurrentLevel = new ScmFileSet(currentFile, allChildren);
                    AddScmResult addScmResult = scmManager.add(repository, allFilesInCurrentLevel);
                    outcome = processResult(addScmResult, null);
                }

                if (ArrayUtils.isNotEmpty(onlySubDirectries)) {
                    for (File subDir : onlySubDirectries) {
                        outcome = outcome && addRecursively(url, subDir.getAbsolutePath());
                    }
                }

            } catch (ScmException e) {
                String msg = "Error in executing add operation on " + url;
                log.error(msg, e);
                throw new RepositoryMgtException(msg, e);
            }

        }

        return outcome;

    }

	private boolean isValidName(String name) {
		return (!name.contains(".svn")) && (!name.contains(".git"));
	}


	/**
	 * Checkin to a branch
	 *
	 * @param sourceURL
	 * @param workDir
	 * @param msg
	 * @param targetVersion
	 * @return
	 * @throws RepositoryMgtException
	 */
	@Override
    public boolean checkIn(String sourceURL, File workDir, String msg, String targetVersion)
	                                                                                        throws RepositoryMgtException {
        sourceURL = getSCMURL(sourceURL);
		CheckInScmResult checkInScmResult;
		ScmRepository repository = getRepository(sourceURL);
		repository.getProviderRepository().setPushChanges(true);
		try {
			checkInScmResult =
			                   scmManager.checkIn(repository, new ScmFileSet(workDir), new ScmBranch(targetVersion),
			                                      msg);
		} catch (ScmException e) {
			String message = "Error in executing checkIn operation on " + sourceURL;
			log.error(msg, e);
			throw new RepositoryMgtException(message, e);
		}
		return processResult(checkInScmResult, null);
	}

    private String getSCMURL(String genericURL) {
        StringBuilder scmURL = new StringBuilder(repositoryType);
        return scmURL.append(genericURL.trim()).toString();
    }

}
