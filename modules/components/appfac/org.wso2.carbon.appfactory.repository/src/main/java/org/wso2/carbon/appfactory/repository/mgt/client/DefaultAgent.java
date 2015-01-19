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

import org.wso2.carbon.appfactory.repository.mgt.RepositoryMgtException;

import java.io.File;

/**
 * Default Agent interface for handling repository operations
 */
public interface DefaultAgent {
    void init(String username, String password) throws RepositoryMgtException;

    void close();

    boolean branch(String baseURL, String version, String checkOutDirectoryToBranch)
            throws RepositoryMgtException;

    boolean branch(String baseURL, String version, File checkOutDirectory)
            throws RepositoryMgtException;

    boolean tag(String baseURL, String version, String revision) throws RepositoryMgtException;

    boolean checkOut(String url, File checkOutDirectory, String revision) throws RepositoryMgtException;

    boolean checkIn(String url, File checkInDirectory, String msg) throws RepositoryMgtException;

    boolean forceCheckIn(String url, File checkInDirectory, String msg) throws RepositoryMgtException;

    boolean add(String url, File currentFile) throws RepositoryMgtException;

    boolean delete(String url, File currentFile, String msg) throws RepositoryMgtException;

    boolean addRecursively(String url, String currentFilePath)
            throws RepositoryMgtException;

    boolean addRecursively(String url, File currentFile)
            throws RepositoryMgtException;

    boolean checkIn(String sourceURL, File workDir, String msg, String targetVersion)
            throws RepositoryMgtException;
}
