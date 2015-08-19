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

package org.wso2.carbon.appfactory.core.apptype;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.appfactory.common.AppFactoryException;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This is a interface provide all the  information related to application type
 */
public interface ApplicationTypeProcessor {

    /**
     * Do a version on the artifact that can be found in workingDirectory
     *
     * @param targetVersion    target version of the application
     * @param currentVersion   current version of the application
     * @param workingDirectory working directory path of the application
     * @throws org.wso2.carbon.appfactory.common.AppFactoryException
     */
    public void doVersion(String applicationId, String targetVersion, String currentVersion,
                          String workingDirectory) throws AppFactoryException;

    /**
     * Generate a sample application  in working directory
     *
     * @param applicationID    application id
     * @param workingDirectory working directory path of the application
     */
    public void generateApplicationSkeleton(String applicationID, String workingDirectory) throws AppFactoryException;

	/**
	 * Setter for properties
	 *
	 * @param properties
	 */
    public void setProperties(Properties properties);

	/**
	 * Getter for properties
	 *
	 * @param name
	 * @return
	 */
    public Object getProperty(String name);

    /**
     * New branch will be created based on existing branch. This method returns
     * list of files which should be deleted before creating new branch.
     *
     * @param appId          application id
     * @param targetVersion  target version
     * @param currentVersion current version of the application
     * @param absolutePath   absolute path of the application
     * @return list of files
     * @throws org.wso2.carbon.appfactory.common.AppFactoryException
     */
    public List<File> getPreVersionDeleteableFiles(String appId, String targetVersion,
	                                               String currentVersion, String absolutePath) throws AppFactoryException;

    /**
     * Returns the deployed url
     *
     * @param tenantDomain       domain of the tenant
     * @param applicationID      application id
     * @param applicationVersion application version
     * @param stage              life cycle stage of the application
     * @return deployed url
     * @throws AppFactoryException
     */
    public String getDeployedURL(String tenantDomain, String applicationID,
	                             String applicationVersion, String stage) throws AppFactoryException;

    /**
     * This method Configures the build job with project related data
     *
     * @param jobConfigTemplate default build job template
     * @param parameters        parameters of the application
     * @param projectType       type of the project
     * @return Configured template as a OMElement
     * @throws AppFactoryException
     */
    public OMElement configureBuildJob(OMElement jobConfigTemplate, Map<String, String> parameters,
                                       String projectType)
            throws AppFactoryException;

    /**
     * Validates the application type.
     * @param uploadedFileName uploaded file name
     * @return ApplicationTypeValidationStatus with validation status
     * @throws UnsupportedOperationException if validation is not required for the app type
     */
    public ApplicationTypeValidationStatus validate(String uploadedFileName) throws UnsupportedOperationException;

	/**
	 * This is generate deployable file according to the artifact type.
	 *
	 * @param rootPath Storage path of the artifacts to generate deployable file
	 * @param applicationId application id of the application
	 * @param version version of the application
	 * @param stage stage to deploy the file
	 * @throws AppFactoryException
	 */
	public void generateDeployableFile(String rootPath, String applicationId,
	                                   String version, String stage) throws AppFactoryException;
}
