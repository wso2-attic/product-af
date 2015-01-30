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

package org.wso2.carbon.appfactory.jenkins.build;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.ContinuousIntegrationSystemDriver;
import org.wso2.carbon.appfactory.core.dto.Statistic;
import org.wso2.carbon.appfactory.core.util.CommonUtil;
import org.wso2.carbon.appfactory.jenkins.build.internal.ServiceContainer;
import org.wso2.carbon.appfactory.utilities.project.ProjectUtils;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This driver integrates Jenkins CI and Appfactory. Refer
 * {@link ContinuousIntegrationSystemDriver} for more information.
 *
 * @scr.reference name="appfactory.configuration" interface=
 * "org.wso2.carbon.appfactory.common.AppFactoryConfiguration"
 * cardinality="1..1" policy="dynamic"
 * bind="setAppFactoryConfiguration"
 * unbind="unsetAppFactoryConfiguration"
 */
public class JenkinsCISystemDriver implements ContinuousIntegrationSystemDriver {

    /**
     * Used to connect to jenkins server
     */
    private RestBasedJenkinsCIConnector connector;

    /**
     * These global roles names should be defined in role based strategy (
     * jenkins ci) plugin.
     * Any user added to jenkins will be assigned with these roles.
     * Typical usage of having such roles is to control access at a global level
     * ( e.g. defining slaves, admin access)
     */
    private String[] defaultGlobalRoles;

    private static final Log log = LogFactory.getLog(JenkinsCISystemDriver.class);

    public JenkinsCISystemDriver(RestBasedJenkinsCIConnector connector, String[] defaultGlobalRoles) {
        this.connector = connector;
        this.defaultGlobalRoles = defaultGlobalRoles;
    }

    public File getArtifact(String applicationId, String version, String artifactName, String tenantDomain)
            throws AppFactoryException {
        return this.connector.getArtifact(getJobName(applicationId, version, ""), artifactName, tenantDomain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createJob(String applicationId, String version, String revision, String tenantDomain, String userName, String repoURL, String repoFrom)
            throws AppFactoryException {
        Map<String, String> parameters = new HashMap<String, String>();

        String repoType = "";
        String applicationType ="";

       
        repoType = ProjectUtils.getRepositoryType(applicationId, tenantDomain);
        applicationType = CommonUtil.getApplicationType(applicationId, tenantDomain);

 
		if (log.isDebugEnabled()) {
			log.debug(String.format(
					"repo url for application id:%s, version: %s, "
							+ "repository type: %s, url: %s", applicationId,
					version, repoType, repoURL));
		}
         
	    parameters.put(JenkinsCIConstants.REPOSITORY_URL, repoURL);
	    parameters.put(JenkinsCIConstants.REPOSITORY_TYPE, repoType);
	    parameters.put(JenkinsCIConstants.APPLICATION_EXTENSION, applicationType);

     

        parameters.put(JenkinsCIConstants.MAVEN3_CONFIG_NAME,
                ServiceContainer.getAppFactoryConfiguration()
                        .getFirstProperty(JenkinsCIConstants.MAVEN3_CONFIG_NAME_CONFIG_SELECTOR));
        parameters.put(JenkinsCIConstants.REPOSITORY_ACCESS_CREDENTIALS_USERNAME,
                ServiceContainer.getAppFactoryConfiguration()
                        .getFirstProperty(JenkinsCIConstants.JENKINS_SERVER_ADMIN_USERNAME));
        parameters.put(JenkinsCIConstants.REPOSITORY_ACCESS_CREDENTIALS_PASSWORD,
                ServiceContainer.getAppFactoryConfiguration()
                        .getFirstProperty(JenkinsCIConstants.JENKINS_SERVER_ADMIN_PASSWORD));

        parameters.put(JenkinsCIConstants.APPLICATION_ID, applicationId);
        parameters.put(JenkinsCIConstants.APPLICATION_VERSION, version);
        parameters.put(JenkinsCIConstants.APPLICATION_USER, userName);
        parameters.put(JenkinsCIConstants.APPLICATION_EXTENSION, applicationType);
        parameters.put(JenkinsCIConstants.REPOSITORY_FROM, repoFrom);

        this.connector.createJob(getJobName(applicationId, version,userName, repoFrom), parameters, tenantDomain);

    }

    /**
     * {@inheritDoc}
     */
    public void deleteJob(String applicationId, String version, String tenantDomain) throws AppFactoryException {
    	connector.deleteJob(getJobName(applicationId, version, ""), tenantDomain);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getAllJobNames( String tenantDomain) throws AppFactoryException {
        return connector.getAllJobs(tenantDomain);
    }

    /**
     * {@inheritDoc}
     */
    public void startBuild(String applicationId, String version, boolean doDeploy, String stageName, String tagName, String tenantDomain, String userName, String repoFrom)
            throws AppFactoryException {
        connector.startBuild(applicationId, version, doDeploy, stageName, tagName, tenantDomain, userName,repoFrom);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isJobExists(String applicationId, String version, String tenantDomain) throws AppFactoryException {
        return connector.isJobExists(applicationId, version, tenantDomain);
    }

  
    public String getJobName(String applicationId, String version, String revision) {
        // Job name will be '<ApplicationId>-<version>-default'
        return applicationId.concat("-").concat(version).concat("-").concat("default");
    }
    
    
    public String getJobName(String applicationId, String version,String userName, String repoFrom) {
        // Job name will be '<ApplicationId>-<version>-default'
		if(AppFactoryConstants.FORK_REPOSITORY.equals(repoFrom)){
			return applicationId.concat("-").concat(version).concat("-").concat("default").concat("-").concat(userName);
        }
		return applicationId.concat("-").concat(version).concat("-").concat("default");
    }

    /**
     * Additional method to be used by {@link JenkinsApplicationEventsListener}.
     * Method will add a role (in role strategy plugin) to match jobs created
     * for specified application.
     * (when a user is added/invited to a application,
     * {@link JenkinsApplicationEventsListener} will assign
     * the role associated with application to user.
     * <p>
     * <b>NOTE: This method assumes role-strategy plugin is correctly installed
     * in jenkins server
     * </p>
     *
     * @param applicationId application Id
     * @throws AppFactoryException if an error occurs
     */
    public void setupApplicationAccount(String applicationId, String tenantDomain) throws AppFactoryException {
        String applicationSelectorRegEx = applicationId.concat(".*");
        String projectRolePermissions=ServiceContainer.getAppFactoryConfiguration()
                .getFirstProperty(JenkinsCIConstants.PROJECT_ROLE_PERMISSIONS_CONFIG_SELECTOR);
       String[]permissions= projectRolePermissions.split(",");

        connector.createRole(applicationId, applicationSelectorRegEx,permissions, tenantDomain);
    }

    /**
     * Configures given list of users in jenkins server ( role strategy plugin)
     * by assigning correct application specific roles and set of global roles.
     * These role should be already defined in jenkins server.
     * <p>
     * <b>NOTE: This method assumes role-strategy plugin is correctly installed
     * in jenkins server
     * </p>
     *
     * @param applicationId Application Id
     * @param userIds       set of users (Ids)
     * @throws AppFactoryException if a error occurs
     */
    public void addUsersToApplication(String applicationId, String[] userIds, String tenantDomain)
            throws AppFactoryException {
        connector.assignUsers(userIds, new String[]{applicationId}, defaultGlobalRoles, tenantDomain);
    }

    /**
     * edit the existing jog configuration  when lifeCycle change
     *
     * @param applicationId Application Id
     * @param version       Application version
     * @param updateState   (addAD/removeAD) flag to remove or add Auto Deploy trigger configurations
     * @param pollingPeriod AD pollingPeriod
     * @throws AppFactoryException if a error occurs
     */
    @Deprecated
    public void editADJobConfiguration(String applicationId, String version,  String updateState,
                                  int pollingPeriod, String tenantDomain) throws AppFactoryException {
        connector.editJob(getJobName(applicationId, version, ""), updateState, pollingPeriod, tenantDomain);

    }
    
    public void setJobAutoBuildable(String applicationId, String version,  boolean isAutoBuild,
                                       int pollingPeriod, String tenantDomain) throws AppFactoryException {
        String repositoryType=ProjectUtils.getRepositoryType(applicationId, tenantDomain);
                     connector.setJobAutoBuildable(getJobName(applicationId, version, ""),
                             repositoryType, isAutoBuild, pollingPeriod, tenantDomain);
    }

    /**
     * {@inheritDoc}. returns global build statistics and load information.
     */
    @Override
    public Statistic[] getGlobalStatistics(Map<String, String> parameters, String tenantDomain)
            throws AppFactoryException {
        // get load statistics
        List<Statistic> stats = connector.getOverallLoad(tenantDomain);
        // get build statistics for all jobs
        List<String> allJobNames = connector.getAllJobs(tenantDomain);
        Collections.addAll(stats, getBuildStatistics(allJobNames, tenantDomain));
        return stats.toArray(new Statistic[stats.size()]);
    }

    /**
     * {@inheritDoc}. Currently method returns build statistics only.
     */
    @Override
    public Statistic[] getApplicationStatistics(String applicationId, Map<String, String> parameters, String tenantDomain)
            throws AppFactoryException {
        List<String> jobsNames = connector.getJobNames(applicationId, tenantDomain);
        return getBuildStatistics(jobsNames, tenantDomain);
    }
    
    public String getJsonTree(String jobName,String treeStructure, String tenantDomain)throws AppFactoryException{
        return connector.getJsonTree(jobName, treeStructure, tenantDomain);
    }

    /**
     * Returns a set of {@link Statistic} indicating total number builds each
     * each build out come (e.g. Sucessfull, failed, aborted etc)
     *
     * @param jobNames Name of the job definitions to check
     * @return an array of {@link Statistic}s
     * @throws AppFactoryException if an error occurs
     */
    private Statistic[] getBuildStatistics(List<String> jobNames, String tenantDomain) throws AppFactoryException {

        // Build status against the count.
        Map<BuildResult, Integer> buildStatusCount = new HashMap<BuildResult, Integer>();

        for (BuildResult br : BuildResult.values()) {
            buildStatusCount.put(br, 0);
        }

        for (String jobName : jobNames) {
            List<String> buildUrls = connector.getBuildUrls(jobName, tenantDomain);
            for (String url : buildUrls) {
                String status = connector.getbuildStatus(url, tenantDomain);
                BuildResult buildStatus = BuildResult.convert(status);
                if (buildStatus != null) {// if unidentified build status was given by jenkins.
                    // increment the existing value (or start the count at 1)
                    Integer currentCount = buildStatusCount.get(buildStatus);
                    buildStatusCount.put(buildStatus, currentCount + 1);

                }

            }
        }

        Statistic[] stats = new Statistic[buildStatusCount.size()];
        int index = 0;
        for (BuildResult bs : buildStatusCount.keySet()) {
            Statistic buildStat = new Statistic(bs.getName(), buildStatusCount.get(bs).toString());
            stats[index++] = buildStat;
        }

        return stats;

    }

	public void setJobAutoDeployable(String applicationId, String version, boolean isAutoDeployable, String tenantDomain)
            throws AppFactoryException {
		connector.setJobAutoDeployable(getJobName(applicationId, version, ""), isAutoDeployable, tenantDomain);
    }

	/**
	 * Removes permission to access given application for given set of users in jenkins build system.
	 * @param applicationKey - key of the application.
	 * @param users - users to be removed from application access.
	 * @throws AppFactoryException 
	 */
	public void removeUsersFromApplication(String applicationKey, String[] users, String tenantDomain) throws AppFactoryException {
	    connector.unAssignUsers(applicationKey, users, tenantDomain);
	    
    }



}
