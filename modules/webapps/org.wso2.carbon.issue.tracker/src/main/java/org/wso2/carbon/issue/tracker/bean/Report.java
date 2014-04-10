package org.wso2.carbon.issue.tracker.bean;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Report {
    private String type;
    private String issueCount;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIssueCount() {
        return issueCount;
    }

    public void setIssueCount(String count) {
        this.issueCount = count;
    }
}
