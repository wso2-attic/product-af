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
package org.wso2.carbon.appfactory.issuetracking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.wso2.carbon.appfactory.issuetracking.beans.GenericIssue;
import org.wso2.carbon.appfactory.issuetracking.beans.GenericIssueType;
import org.wso2.carbon.appfactory.issuetracking.beans.IssueRepositoryConnector;
import org.wso2.carbon.appfactory.issuetracking.beans.IssueSummary;
import org.wso2.carbon.appfactory.issuetracking.exception.IssueTrackerException;

/**
 *
 *
 */
public class IssueRepository {
    private IssueRepositoryConnector connector;
    private static IssueRepository issueRepository;

    private IssueRepository() {
       // this.connector = new AppFactoryRedmineIssueTrackerConnector(ServiceContainer.getAppFactoryConfiguration());
        // this.connector.setConfiguration(ServiceContainer.getAppFactoryConfiguration());

    }

    public String reportIssue(GenericIssue genericIssue, String appID) throws
            IssueTrackerException {
        String issueID;
        issueID = connector.reportIssue(genericIssue, getProjectKey(appID));
        return issueID;
    }

    private String getProjectKey(String appID) {
        String projectKey;

        projectKey = connector.getProjectApplicationMapping().getProjectKey(appID);

        return projectKey;
    }

    public String updateIssue(GenericIssue genericIssue, String appID)
            throws IssueTrackerException {
        String issueID;
        issueID = connector.updateIssue(genericIssue, getProjectKey(appID));
        return issueID;
    }

    public List<GenericIssue> getAllIssuesOfProject(String appId) throws IssueTrackerException {
        List<GenericIssue> list;
        list = connector.getAllIssuesOfProject(getProjectKey(appId));
        return list;
    }

    public List<GenericIssue> getAllIssuesWithParameters(String queryString) throws IssueTrackerException {
        List<GenericIssue> list=null;
        if (!(queryString == null || queryString.isEmpty())) {
            Map<String, String> pParameters= new HashMap<String, String>();
            String[] pairs = queryString.split("&");
            for (String pair : pairs) {
                String[] pairArray = pair.split("=");
                pParameters.put(pairArray[0], pairArray[1]);
            }
            list = connector.getAllIssuesWithParameters(pParameters);
        }
        return list;
    }

    public GenericIssue getIssueByKey(String key, String appID) throws IssueTrackerException {
        GenericIssue issue;
        issue = connector.getIssueByKey(key, getProjectKey(appID));
        return issue;
    }

    public String[] getIssueStatus() throws IssueTrackerException {
        String[] statuses;
        statuses = connector.getIssueStatuses();
        return statuses;
    }

    public GenericIssueType[] getIssueTypes() throws IssueTrackerException {
        GenericIssueType[] types;
        types = connector.getIssueTypes();
        return types;
    }

    public static IssueRepository getIssueRepository() {
        if (issueRepository == null) {
            issueRepository = new IssueRepository();
        }
        return issueRepository;
    }

    public IssueRepositoryConnector getConnector() {
        return connector;
    }

    public void setConnector(IssueRepositoryConnector connector) {
        this.connector = connector;
    }

    public String[] getAvailableAssignees(String appID) throws IssueTrackerException {
        String[] assignees;
        assignees = this.connector.getAvailableAssignees(getProjectKey(appID));
        return assignees;
    }

    public String getUrlForReportIssue(String appID)
            throws IssueTrackerException {
        String url;
        url = this.connector.getUrlForReportIssue(getProjectKey(appID));
        return url;
    }
    
    public UserIssues[] getAssignerIssueCount() throws IssueTrackerException {
    	return connector.getAssignerIssueCount();
    }
    
    public UserIssues[] getReporterIssueCount() throws IssueTrackerException {
    	return connector.getReporterIssueCount();
    }
    
    public IssueSummary[] getIssuesSummary(String appID) throws IssueTrackerException {
    	List<IssueSummary> issueSummaries = connector.getIssuesSummary(appID);
    	return issueSummaries.toArray(new IssueSummary[issueSummaries.size()]);
    }
}
