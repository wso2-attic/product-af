package com.virtusa.isq.vtaf.runtime.pages;

/**
 *  Class TrackIssues implements corresponding UI page
 *  UI objects in the page are stored in the class.
 */

public enum TrackIssues {

        btn_NewIssue("//a[contains(text(), \"New Issue\")]"), ele_ddProject("//div[@id=\"s2id_projectKey\"]/a/div"), tf_Summary("css=input[id=\"summary\"]"), tf_Description("css=textarea[id=\"description\"]"), ele_ddType("//div[@id=\"s2id_type\"]/a/div"), ele_ddPriority("//div[@id=\"s2id_priority\"]/a/div"), ele_ddStatus("//div[@id=\"s2id_issue_status\"]/a/div"), ele_ddAssignee("css=select[id=\"assignee\"]"), ele_ddVersion("//div[@id=\"s2id_version\"]/a/div"), ele_ddSeverity("//div[@id=\"s2id_severity\"]/a/div"), btn_AddIssue("css=input[id=\"saveIssue\"]"), lnk_Issues("link=Issues"), win_TrackIssues("index=1"), ele_lblType("//div[@class='list_col_content' and contains(text(),'<param_Summary>')]/../../li[4]/div"), btn_Home("css=a[class=\"icon-home\"]"), ele_DropDownValue("//div[@class='select2-result-label' and text()='<param_DropDownValue>'] "), btn_EditIssue("css=input[type=\"button\"][value=\"Edit Issue\"]"), btn_UpdateIssue("css=input[id=\"editIssue\"][value=\"Update Issue\"]"), ele_lblIssueStatus("//div[@class='list_col_content' and contains(text(),'<param_SummaryName>')]/../../li[7]/div[contains(text(),'<param_IssueStatus>')]"), ele_ApplicationDropdown("css=div[id='select2-drop']>ul>li"), ele_TblRow("css=li[class='list_row_item first_list_row_item']>ul"), lbl_IssueKeybyTblRow("css=li[class='list_row_item first_list_row_item']>ul:nth-child(<param_tblRowNumber>)>li>div>a"), lbl_IssueKey("//a[text()='<param_IssueKey>']");

    private String searchPath;
  
    /**
    *  Page TrackIssues.
    */
    private TrackIssues(final String psearchPath) {
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