/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.wso2.carbon.appfactory.core.deploy;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.manager.BasicScmManager;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.git.gitexe.GitExeScmProvider;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.wso2.carbon.appfactory.common.AppFactoryException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Repository client that wrapping maven SCM plugin
 */
public class GitSCMClient {
    private static final Log log = LogFactory.getLog(GitSCMClient.class);
    private ScmManager scmManager;
    private String username;
    private String password;

    public GitSCMClient() {
        scmManager = new BasicScmManager();
        scmManager.setScmProvider("git",new GitExeScmProvider());
    }

    public void init(String username, String password) throws AppFactoryException {
        this.username = username;
        this.password = password;
    }
    public void close() {
    }

    private ScmRepository getRepository(String url) throws AppFactoryException {
        ScmRepository repository;
        try {
            repository = scmManager.makeScmRepository(url);
            repository.getProviderRepository().setUser(username);
            repository.getProviderRepository().setPassword(password);
            repository.getProviderRepository().setPushChanges(true);
        } catch (ScmRepositoryException e) {
            String msg = "Could not able to create repository object";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (NoSuchScmProviderException e) {
            String msg = "There is no repository provider for " + url +
                         " install required dependencies";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        return repository;
    }

    public boolean checkOut(String url, File checkOutDirectory)
            throws AppFactoryException {
        CheckOutScmResult checkOutScmResult;
        ScmRepository repository = getRepository(url);
        try {
            checkOutScmResult = scmManager.checkOut(repository, new ScmFileSet(checkOutDirectory));
        } catch (ScmException e) {
            String msg = "Error in executing checkout operation on " + url;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        return processResult(checkOutScmResult, null);
    }

    public boolean checkIn(String url, File checkInDirectory, String msg)
            throws AppFactoryException {
        CheckInScmResult checkInScmResult;
        ScmRepository repository = getRepository(url);
        repository.getProviderRepository().setPushChanges(true);
        try {
            checkInScmResult = scmManager.checkIn(repository, new ScmFileSet(checkInDirectory), msg);
        } catch (ScmException e) {
            String message = "Error in executing checkIn operation on " + url;
            log.error(msg, e);
            throw new AppFactoryException(message, e);
        }
        return processResult(checkInScmResult, null);
    }

    private boolean processResult(ScmResult result, File workingDirectory)
            throws AppFactoryException {
        if (workingDirectory != null) {
            try {
                FileUtils.deleteDirectory(workingDirectory);
            } catch (IOException e) {
                String msg = "Error in deleting working directory " + workingDirectory;
                log.error(msg, e);
                throw new AppFactoryException(msg, e);
            }
        }
        boolean success = false;
        if (result != null) {
            if (result.isSuccess()) {
                success = true;
            } else {
                log.error("Error in executing command  CommandLine error is  "
                          + result.getCommandOutput());
            }
        }
        return success;
    }

    /**
     * Method to add files to the given svn repository.
     *
     * @param url         the svn repository url
     * @param currentFile the file that needs to be added to the svn repository
     * @return processResult Result in executing command
     * @throws org.wso2.carbon.appfactory.common.AppFactoryException if add operation in SCM
     */
    public boolean add(String url, File currentFile) throws AppFactoryException {
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
            throw new AppFactoryException(msg, e);
        }
        return processResult(addScmResult, null);
    }
    public boolean remove(String url,File currentFile,String msg) throws AppFactoryException{
        ScmRepository repository = getRepository(url);
        ScmFileSet fileSet = new ScmFileSet(currentFile.getParentFile(), currentFile);
        RemoveScmResult result;

        try {
            result = scmManager.remove(repository,fileSet,msg);
        } catch (ScmException e) {
            String errorMsg = "Unable to remove file from repository";
            log.error(errorMsg,e);
            throw new AppFactoryException(errorMsg,e);

        }
        return processResult(result,null);
    }

    public boolean update(String url, File currentFile) throws AppFactoryException{
        ScmRepository repository = getRepository(url);
        ScmFileSet fileSet = new ScmFileSet(currentFile.getParentFile(),currentFile);
        UpdateScmResult result;

        try{
            result = scmManager.update(repository,fileSet);
        } catch (ScmException e) {
            String errorMsg = "Unable to update file from repository";
            log.error(errorMsg,e);
            throw new AppFactoryException(errorMsg,e);
        }
        return processResult(result,null);
    }

}
