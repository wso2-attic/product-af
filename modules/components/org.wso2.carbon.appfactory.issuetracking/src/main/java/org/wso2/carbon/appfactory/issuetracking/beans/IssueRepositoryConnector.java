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
package org.wso2.carbon.appfactory.issuetracking.beans;


import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.issuetracking.UserIssues;
import org.wso2.carbon.appfactory.issuetracking.exception.IssueTrackerException;

import java.util.List;
import java.util.Map;

public interface IssueRepositoryConnector {
	
	/**
	 * Read summary of the issues per application.
	 * @param projectID
	 * @return
	 * @throws IssueTrackerException
	 */
	public List<IssueSummary> getIssuesSummary(String projectID)throws IssueTrackerException;

    public String reportIssue(GenericIssue genericIssue, String projectID)
            throws IssueTrackerException;

    public String updateIssue(GenericIssue genericIssue, String projectID)
            throws IssueTrackerException;

    public List<GenericIssue> getAllIssuesOfProject(String project) throws IssueTrackerException;
    
    public List<GenericIssue> getAllIssuesWithParameters(Map<String, String> pParameters) throws IssueTrackerException;

    public boolean createProject(Project project) throws IssueTrackerException;

    public boolean deleteProject(Project project) throws IssueTrackerException;

    public boolean addUserToProject(GenericUser user, Project project) throws IssueTrackerException;
    
    public boolean removeUserFromProject(GenericUser user, Project project) throws IssueTrackerException;
    
    public boolean updateUserOfProject(GenericUser user,Project project) throws IssueTrackerException;

    public ProjectApplicationMapping getProjectApplicationMapping();

    public String[] getIssueStatuses() throws IssueTrackerException;

    public GenericIssueType[] getIssueTypes() throws IssueTrackerException;

    public GenericIssue getIssueByKey(String key, String projectID) throws IssueTrackerException;

    public String[] getAvailableAssignees(String projectID) throws IssueTrackerException;

    public AppFactoryConfiguration getConfiguration();

    public void setConfiguration(AppFactoryConfiguration configuration);

    public void createVersionInProject(Project project,Version version)
            throws IssueTrackerException;
    
    public String getUrlForReportIssue(String project)
            throws IssueTrackerException;
    
    public UserIssues[] getAssignerIssueCount() throws IssueTrackerException;
    
    public UserIssues[] getReporterIssueCount() throws IssueTrackerException;
}
