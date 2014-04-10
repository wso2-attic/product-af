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

import org.wso2.carbon.appfactory.common.AppFactoryException;

import java.io.File;
import java.util.List;
import java.util.Properties;

/**
 * This is a interface provide all the  information related to application type
 */
public interface ApplicationTypeProcessor {
    /**
     * Return the name that can be used to uniquely identify the application type
     *
     * @return
     */
    public String getName();

    /**
     * set the name that can be used to uniquely identify the application type
     *
     * @param name
     */
    public void setName(String name);

    /**
     * Return the Display name that can be used to represent the application type in UI
     *
     * @return
     */
    public String getDisplayName();

    /**
     * Set the Display name that can be used to represent the application type in UI
     *
     * @param displayName
     */
    public void setDisplayName(String displayName);

    /**
     * Return the File extension of diployable artifact
     *
     * @return
     */
    public String getFileExtension();

    /**
     * Set the File extension of diployable artifact
     *
     * @param name
     */
    public void setFileExtension(String name);

    /**
     * Do a version on the artifact that can be found in workingDirectory
     *
     * @param targetVersion
     * @param currentVersion
     * @param workingDirectory
     * @throws AppFactoryException
     */
    public void doVersion(String applicationId, String targetVersion,String currentVersion,String workingDirectory ) throws AppFactoryException;

    /**
     *  Generate a sample application  in working directory
     *
     * @param applicationID
     * @param workingDirectory
     */
    public void generateApplicationSkeleton(String applicationID,String workingDirectory) throws AppFactoryException;

    /**
     * Getter method to get the description of application type
     *
     * @return
     */
    public String getDescreption();

    /**
     * Setter method to set the description of application type
     *
     * @param descreption
     */
    public void setDescreption(String descreption);

    /**
     * Getter method to get build job template name
     *
     * @return
     */
    public String getBuildJobTemplate();

    /**
     * Setter method to get build job template name
     *
     * @param buildJobTemplate represents build template name{freestyle,maven}
     */
    public void setBuildJobTemplate(String buildJobTemplate);

    public void setProperties(Properties properties);

    public Object getProperty(String name);

    /**
     * New branch will be created based on existing branch. This method returns 
     * list of files which should be deleted before creating new branch.
     * @param appId
     * @param targetVersion
     * @param currentVersion
     * @param absolutePath
     * @return
     * @throws AppFactoryException
     */
	public List<File> getPreVersionDeleteableFiles(String appId, String targetVersion,
                                                     String currentVersion, String absolutePath) throws AppFactoryException;
    
}
