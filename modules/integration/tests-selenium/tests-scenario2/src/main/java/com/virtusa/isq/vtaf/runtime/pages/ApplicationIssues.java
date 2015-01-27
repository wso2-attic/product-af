package com.virtusa.isq.vtaf.runtime.pages;

/**
 *  Class ApplicationIssues implements corresponding UI page
 *  UI objects in the page are stored in the class.
 */

public enum ApplicationIssues {

        lnk_TrackIssues("link=Track Issues"), ele_lblBug("//strong[text()='<param_VersionNumber>']/../../div[3]"), ele_lblFeature("//strong[text()='<param_VersionNumber>']/../../div[4]"), ele_lblVulnerability("//strong[text()='<param_VersionNumber>']/../../div[5]"), btn_NewIssue("link=New Issue"), tf_Summary("css=input[id=\"summary\"]"), ele_ddVersion("css=div[id='s2id_version']>a>div>b"), lbl_Version("//div[text()='<param_Version>']"), tf_IssueDescription("css=textarea[id=\"description\"]"), ele_ddIssueType("css=div[id='s2id_type']>a>div>b"), lbl_IssueType("//div[text()='<param_IssueType>']"), ele_ddIssuePriority("css=div[id='s2id_priority']>a>div>b"), lbl_IssuePriority("//div[text()='<param_IssuePriority>']"), ele_ddIssueStatus("css=div[id='s2id_issue_status']>a>div>b"), lbl_IssueStatus("//div[text()='<param_IssueStatus>']"), ele_ddIssueAssignee("css=div[id='s2id_assignee']>a>div>b"), lbl_IssueAssignee("//div[text()='<param_IssueAssignee>']"), ele_ddSeverity("css=div[id='s2id_severity']>a>div>b"), lbl_Severity("//div[text()='<param_IssueSeverity>']"), btn_AddIssue("//input[@id='saveIssue']"), ele_CreatedIssue("//div[@class='list_col_content truncate issue_summary']"), btn_Edit("//div[contains(text(),'<param_IssueSummary>')]/../../td[11]/a"), lbl_IssueId("//div[contains(text(),'<param_IssueSummary>')]/../../td"), btn_UpdateIssue("//input[@id='editIssue']"), ele_IssueStatus("//div[contains(text(),'<param_IssueSummary>')]/../../td[7]"), ele_IssueType("//div[contains(text(),'<param_IssueSummary>')]/../../td[4]"), ele_IssuePriority("//div[contains(text(),'<param_IssueSummary>')]/../../td[5]");

    private String searchPath;
  
    /**
    *  Page ApplicationIssues.
    */
    private ApplicationIssues(final String psearchPath) {
        this.searchPath = psearchPath;
    }
    
    /**
     *  Get search path.
     * @param searchPath search path.
     */
    public final String getSearchPath() {
        return searchPath;
    }
}