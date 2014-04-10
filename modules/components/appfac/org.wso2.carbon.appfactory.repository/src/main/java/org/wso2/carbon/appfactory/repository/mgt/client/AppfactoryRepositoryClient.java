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

import org.codehaus.plexus.embed.Embedder;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryMgtException;

import java.io.File;

/**
 * Client specific to appfactory which sets url to scm:{svn or git}:http://...
 * Example usage:
 * <p/>
 * AppfactoryRepositoryClient client=new AppfactoryRepositoryClient("svn");
 * File temp=new File("/path/to/file/to/checkout");
 * client.init("user","password");
 * client.checkOut("https://svn.appfactory/app1", temp, "1209");
 * client.close();
 */
public class AppfactoryRepositoryClient {
    private StringBuilder repositoryType;
    private SCMClient scmClient;

    public AppfactoryRepositoryClient(String repoType, Embedder embedder) {
        this.repositoryType = new StringBuilder("scm");
        repositoryType.append(":").append(repoType).append(":");
        scmClient = new SCMClient(embedder);
    }

    /**
     * Create a directory in repository
     *
     * @param baseURL
     * @param dirName
     * @return
     * @throws RepositoryMgtException
     */
    public boolean mkdir(String baseURL, String dirName) throws RepositoryMgtException {
    	 
        return scmClient.mkdir(getSCMURL(baseURL), dirName);
    }

    /**
     * Branch the repository with files in dir
     *
     * @param baseURL
     * @param version
     * @param dir
     * @return
     * @throws RepositoryMgtException
     */
    public boolean branch(String baseURL, String version, String dir)
            throws RepositoryMgtException {
        return scmClient.branch(getSCMURL(baseURL), version, dir);
    }

    /**
     * Do the commit with add/delete the resource to the repository
     *
     * @param baseURL
     * @param checkInDirectory
     * @param msg
     * @return
     * @throws RepositoryMgtException
     */
    public boolean forceCheckIn(String baseURL, File checkInDirectory, String msg)
            throws RepositoryMgtException {
        return scmClient.forceCheckIn(getSCMURL(baseURL), checkInDirectory, msg);
    }

    /**
     * Do the commit with add/delete the resource to the repository
     *
     * @param baseURL
     * @param checkInDirectory
     * @param targetVersion
     * @param msg
     * @return
     * @throws RepositoryMgtException
     */
    public boolean forceCheckIn(String baseURL, File checkInDirectory, String targetVersion,
                                String msg)
            throws RepositoryMgtException {
        return scmClient.forceCheckIn(getSCMURL(baseURL), checkInDirectory, msg, targetVersion);
    }

    /**
     * Tag the repository
     *
     * @param baseURL
     * @param version
     * @param revision
     * @return
     * @throws RepositoryMgtException
     */
    public boolean tag(String baseURL, String version, String revision)
            throws RepositoryMgtException {
        return scmClient.tag(getSCMURL(baseURL), version, revision);
    }

    /**
     * Checkout a repository
     *
     * @param url
     * @param checkOutDirectory
     * @param revision
     * @return
     * @throws RepositoryMgtException
     */
    public boolean checkOut(String url, File checkOutDirectory, String revision)
            throws RepositoryMgtException {
        return scmClient.checkOut(getSCMURL(url), checkOutDirectory, revision);
    }

    /**
     * Check out a branch
     *
     * @param url
     * @param checkOutDirectory
     * @param version
     * @return
     * @throws RepositoryMgtException
     */
    public boolean checkOutVersion(String url, File checkOutDirectory, String version)
            throws RepositoryMgtException {
        return scmClient.checkOut(getSCMURL(url), checkOutDirectory, version);
    }

    private String getSCMURL(String genericURL) {
        StringBuilder scmURL = new StringBuilder(repositoryType);
        return scmURL.append(genericURL.trim()).toString();
    }

    /**
     * Assign user credential for repository
     *
     * @param username
     * @param password
     * @throws RepositoryMgtException
     */
    public void init(String username, String password) throws RepositoryMgtException {
        scmClient.init(username, password);
    }

    public void close() {
        scmClient.close();
    }

    /**
     * Check in to a repository
     *
     * @param url
     * @param checkInDirectory
     * @param msg
     * @return
     * @throws RepositoryMgtException
     */
    public boolean checkIn(String url, File checkInDirectory, String msg)
            throws RepositoryMgtException {
        return scmClient.checkIn(getSCMURL(url), checkInDirectory, msg);
    }

    /**
     * Add files to repository
     *
     * @param url
     * @param trunk
     * @return
     * @throws RepositoryMgtException
     */
    public boolean add(String url, File trunk) throws RepositoryMgtException {
        return scmClient.add(getSCMURL(url), trunk);
    }

    /**
     * Deletes files from repository
     *
     * @param url
     * @param file
     * @param msg
     * @return
     * @throws RepositoryMgtException
     */
    public boolean delete(String url, File file, String msg) throws RepositoryMgtException {
        return scmClient.delete(getSCMURL(url), file, msg);
    }

    /**
     * Add files recursively to a repository
     *
     * @param url
     * @param trunk
     * @return
     * @throws RepositoryMgtException
     */
    public boolean addRecursively(String url, String trunk) throws RepositoryMgtException {
        return scmClient.addRecursively(getSCMURL(url), trunk, null);
    }

    /**
     * Checkin to a branch
     *
     * @param sourceURL
     * @param workDir
     * @param msg
     * @param targetVersion
     * @throws RepositoryMgtException
     */
    public void checkIn(String sourceURL, File workDir, String msg, String targetVersion)
            throws RepositoryMgtException {
        scmClient.checkIn(getSCMURL(sourceURL), workDir, msg, targetVersion);
    }
}
