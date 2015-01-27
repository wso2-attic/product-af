package com.virtusa.isq.vtaf.runtime.pages;

/**
 *  Class ManageMembers implements corresponding UI page
 *  UI objects in the page are stored in the class.
 */

public enum ManageMembers {

        btn_ImportMembers("css=a[id=\"btnAddMembers\"]"), ele_ResultRow("css=li[class='list_row_item cleanable']"), ele_lblMemberName("//dt[contains(text(),'<param_MemberName>')]"), btn_UserRoleEdit("css=a[id='jsroleAssignPopup']"), btn_UserDelete("//a[@id='removeUsers']"), chk_FirstUser("css=ul[id*='<param_UserInitial>']>li>div>input"), chk_User("css=ul[id='<param_UserName>']>li>div>input"), lbl_Role("css=ul[id='<param_UserName>']>li:nth-child(3)>div"), ele_RolePanel("css=div[id='qtip-0'][aria-hidden='false']"), chk_Developer("css=label[data-role='developer']"), chk_DevOps("css=label[data-role='devops']"), chk_QA("css=label[data-role='qa']"), chk_ApplicationOwner("css=label[data-role='appowner']"), chk_CXO("css=label[data-role='cxo']"), chk_RoleCommon("css=label[data-role='<param_Role>']"), chk_RoleCheckStatus("css=label[data-role='<param_Role>']>span"), btn_SaveRole("css=button[id=\"saveUserRoles\"]"), btn_ConfirmDeleteCancel("link=Cancel"), btn_ConfirmDeleteOk("link=Ok"), ele_emptyRole("css=ul[id='<param_Username>']>li:nth-child(3)>div");

    private String searchPath;
  
    /**
    *  Page ManageMembers.
    */
    private ManageMembers(final String psearchPath) {
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