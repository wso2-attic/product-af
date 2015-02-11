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

public class GenericIssue {


    /*  private String projectKey;*/
    private String summary;
    private String description;
    private String reporter;
    private String type;
    /* private String priority;*/
    /* private AbstractVersion[] affectVersions;*/
    /* private String[] attachmentNames;*/
    /*private AbstractComponent[] components;*/
    /*  private Calendar dueDate;*/
    /* private AbstractVersion[] fixVersions;*/
    private String assignee;
    /*  private Calendar created;*/
    private String issueKey;
    private String url;
    private String status;
    /* private Calendar lastUpdated;
  private String severity;*/

    
    
    private String targetVersion ;
    
    

     public String getTargetVersion() {
		return targetVersion;
	}

	public void setTargetVersion(String targetVersion) {
		this.targetVersion = targetVersion;
	}

	public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    /*

    public Calendar getCreated() {
        return created;
    }

    public void setCreated(Calendar created) {
        this.created = created;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }


    public String getIssueKey() {
        return issueKey;
    }

    public void setIssueKey(String issueKey) {
        this.issueKey = issueKey;
    }*/

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReporter() {
        return reporter;
    }

    public void setReporter(String reporter) {
        this.reporter = reporter;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /* public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public AbstractVersion[] getAffectVersions() {
        return affectVersions;
    }

    public void setAffectVersions(AbstractVersion[] affectVersions) {
        this.affectVersions = affectVersions;
    }

    public String[] getAttachmentNames() {
        return attachmentNames;
    }

    public void setAttachmentNames(String[] attachmentNames) {
        this.attachmentNames = attachmentNames;
    }

    public AbstractComponent[] getComponents() {
        return components;
    }

    public void setComponents(AbstractComponent[] components) {
        this.components = components;
    }

    public Calendar getDueDate() {
        return dueDate;
    }

    public void setDueDate(Calendar dueDate) {
        this.dueDate = dueDate;
    }

    public AbstractVersion[] getFixVersions() {
        return fixVersions;
    }

    public void setFixVersions(AbstractVersion[] fixVersions) {
        this.fixVersions = fixVersions;
    }*/

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /*  public Calendar getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Calendar lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }*/

    public String getIssueKey() {
        return issueKey;
    }

    public void setIssueKey(String issueKey) {
        this.issueKey = issueKey;
    }
}
