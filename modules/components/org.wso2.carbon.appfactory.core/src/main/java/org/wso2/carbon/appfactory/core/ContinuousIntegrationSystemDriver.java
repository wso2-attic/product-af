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
import org.wso2.carbon.appfactory.core.dto.Statistic;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Defines the contact what needs to be implemented by any CI Driver (i.e.
 * Jenkins, Bambo)
 * 
 * 
 */
public interface ContinuousIntegrationSystemDriver {
	
	
	public File getArtifact(String applicationId, String version, String artifactName, String tenantDomain) throws AppFactoryException;

    /**
     * Setup CI job for given application and versions
     * 
     * @param applicationId Id of the application
     * @param version version id
     * @param revision revision (This parameters is deprecated and need to be removed in future)
     * @param tenantDomain Tenant domain of application
     * @param userName TODO
     * @param repoURL TODO
     * @throws AppFactoryException if a error occurs
     */
    public void createJob(String applicationId, String version, String revision, String tenantDomain, String userName,
                          String repoURL, String repoFrom) throws AppFactoryException;

	/**
	 * Removes a specified job from CI System.
	 *
	 * @param applicationId Application ID
	 * @param version Version
	 * @param tenantDomain Tenant Domain
	 * @param userName username is given if this is for delete a fork repo build job other wise null or empty
	 * @throws AppFactoryException
	 */
    public void deleteJob(String applicationId, String version, String tenantDomain,
                          String userName) throws AppFactoryException;

    /**
     * Returns jobs available in CI System.
     * 
     * @return A {@link List} of job names
     * @throws AppFactoryException
     *             If an error occurs
     */
    public List<String> getAllJobNames( String tenantDomain) throws AppFactoryException;

    /**
     * Starts building the specified CI job.
     * 
     * @param applicationId
     *            Application Key
     * @param version
     *            Version
     * @throws AppFactoryException
     *             if an error occurs
     */
    public void startBuild(String applicationId, String version, boolean doDeploy, String stageName, String tagName,
                           String tenantDomain, String userName, String repoFrom) throws AppFactoryException;

    /**
     * Checks weather a specified job is available on CI system.
     * 
     * @param applicationId Application Key
     * @param version Version
     * @param userName username is given if this is for delete a fork repo build job other wise null or empty
     * @return true if job is available, false otherwise
     * @throws AppFactoryException
     *             if error occurs
     */
    public boolean isJobExists(String applicationId, String version, String tenantDomain, String userName) throws AppFactoryException;

	/**
	 * Constructs a job name based on supplied parameter. Rational of this
	 * method is to enable CI driver to have control over the job naming scheme.
	 *
	 * @param applicationId application ID
	 * @param version version of the application
	 * @param userName tenant aware username used for a fork. If this is not for a for value will be null or empty
	 * @return
	 */
    public String getJobName(String applicationId, String version, String userName);

    /**
     * Change the Auto Deployment configurations of the given job
     * @param applicationId
     * @param version
     * @param updateState
     * @param pollingPeriod
     * @throws AppFactoryException
     */
    public void editADJobConfiguration(String applicationId, String version, String updateState,
                                      int pollingPeriod, String tenantDomain) throws AppFactoryException;

    /**
     * Provides a array of {@link Statistic} about the ci server.
     * 
     * @param parameters
     *            any parameters that might be useful for stat calculation.
     * @return a list of {@link Statistic}
     * @throws AppFactoryException
     *             an error
     */
    public Statistic[] getGlobalStatistics(Map<String, String> parameters, String tenantDomain)
                                                                          throws AppFactoryException;;

    /**
     * Provides a array of {@link Statistic} about the builds related to a
     * specified application.
     * 
     * @param applicationId
     *            Id of the application.
     * 
     * @param parameters
     *            any parameters that might be useful for stat calculation.
     * @return a list of {@link Statistic}
     * @throws AppFactoryException
     *             an error
     */
    public Statistic[] getApplicationStatistics(String applicationId, Map<String, String> parameters, String tenantDomain)
                                                                                            throws AppFactoryException;;

    /**
     * Provides a String in a form of a JSON object with the information requested from Jenkins remote API.
     * @param jobName the name of the job, which you need the information about eg: applicationKey-trunk-default
     * @param treeStructure the structure of the returning JSON object eg: builds[param1,param2param3]
     * @return a String in a form of a JSON object
     * @throws AppFactoryException
     */
	public String getJsonTree(String jobName,String treeStructure, String tenantDomain)throws AppFactoryException;
}
