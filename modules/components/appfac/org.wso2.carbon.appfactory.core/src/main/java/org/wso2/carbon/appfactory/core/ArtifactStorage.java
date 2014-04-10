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

package org.wso2.carbon.appfactory.core;

import java.io.File;

import org.wso2.carbon.appfactory.common.AppFactoryException;

import javax.activation.DataHandler;

/**
 * This is the artifact storage.
 * The built artifacts are stored in this storage for retrieval by the deployment service
 */
public interface ArtifactStorage {
    
    /**
     * This will retrieve the artifact as a file.
     * This is a blocking call.
     * 
     * @param applicationId Application Id
     * @param version Version of the artifact
     * @param revision The revision of the artifacct
     * @param tenantDomain Tenant domain of application
     * @return
     */
    public File retrieveArtifact(String applicationId, String version, String revision, String tenantDomain) throws AppFactoryException;

    /**
     *
     * @param applicationId
     * @param version
     * @param revision
     * @param buildId
     * @param tenantDomain
     * @return
     * @throws AppFactoryException
     */
    public File retrieveArtifact(String applicationId, String version, String revision, String buildId, String tenantDomain) throws AppFactoryException;

    /**
     * This will store the artifact in the artifact storage.
     * 
     * @param applicationId Application Id
     * @param version  Version of the artifact
     * @param revision The revision of the artifact
     * @param data The artifact
     */
    public void storeArtifact(String applicationId, String version, String revision, DataHandler data, String fileName) throws AppFactoryException;

    /**
     * This will store the artifact in artifact storage, considering the buildId
     * @param applicationId
     * @param version
     * @param revision
     * @param buildId
     * @param data
     * @param fileName
     * @throws AppFactoryException
     */
    public void storeArtifact(String applicationId, String version, String revision, String buildId, DataHandler data, String fileName) throws AppFactoryException;

}
