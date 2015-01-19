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

import org.wso2.carbon.appfactory.common.AppFactoryException;

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
    private GitSCMClient scmClient;

    public AppfactoryRepositoryClient(String repoType) {
        this.repositoryType = new StringBuilder("scm");
        repositoryType.append(":").append(repoType).append(":");
        scmClient = new GitSCMClient();
    }

    public boolean checkOut(String url, File checkOutDirectory)
            throws AppFactoryException {
        return scmClient.checkOut(getSCMURL(url), checkOutDirectory);
    }

    private String getSCMURL(String genericURL) {
        StringBuilder scmURL = new StringBuilder(repositoryType);
        return scmURL.append(genericURL.trim()).toString();
    }

    public void init(String username, String password) throws AppFactoryException {
        scmClient.init(username, password);
    }

    public void close() {
        scmClient.close();
    }

    public boolean checkIn(String url, File checkInDirectory, String msg)
            throws AppFactoryException {
        return scmClient.checkIn(getSCMURL(url), checkInDirectory, msg);
    }

    public boolean add(String url, File trunk) throws AppFactoryException {
        return scmClient.add(getSCMURL(url), trunk);
    }
    public boolean remove(String url, File trunk,String msg) throws AppFactoryException {
        return scmClient.remove(getSCMURL(url), trunk,msg);
    }
    public boolean update(String url,File trunk)throws AppFactoryException{
        return scmClient.update(url,trunk);
    }
}
