package org.wso2.carbon.issue.tracker.bean;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * ResponseID object of Issue
 */
@XmlRootElement
public class IssueResponse {
    private List<Comment> comments;
    private Issue issue;
    private String version;
    private String projectName;
    private String projectKey;

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public Issue getIssue() {
        return issue;
    }

    public void setIssue(Issue issue) {
        this.issue = issue;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }
}
