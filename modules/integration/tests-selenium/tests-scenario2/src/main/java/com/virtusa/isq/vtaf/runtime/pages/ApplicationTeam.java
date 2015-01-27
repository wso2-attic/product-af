package com.virtusa.isq.vtaf.runtime.pages;

/**
 *  Class ApplicationTeam implements corresponding UI page
 *  UI objects in the page are stored in the class.
 */

public enum ApplicationTeam {

        btn_AddMembers("//a[@id='btnAddMembers']"), tf_Usernames("css=input[id=\"allUsersList\"]"), btn_AddToList("css=button[id=\"addToListBtn\"]"), btn_Invite("css=button[id=\"btn_nvite_users\"]"), ele_ErrorMsg("//div[@id='inviteMembersForAppPage']/div/div/p[contains(text(),'<param_Username> has not been assigned roles - Assign roles and add user to application')]"), ele_Member("//*[@id='<MemberName>']/li[2]/div"), lbl_TeamMembers("//div[@class='list_col_content']/div/dl/dd[text()='']"), ele_TeamMemberUserName("//div[@class='list_col_content']/div/dl/dd[text()='<MemberUserName>']"), ele_TeamMemberName("//div[@class='list_col_content']/div/dl/dt[text()=' <MemberName>']"), ele_TeamAdminMemberName("//div[@class='list_col_content']/div/dl/dt[text()='<param_AdminMemberName>']"), ele_TeamOtherMemberName("//div[@class='list_col_content']/div/dl/dt[text()=' <param_MemberOtherName>']"), ele_TeamMemberRole("//dt[contains(text(),'<MemberName>')]/../../../../../li[3]/div"), btn_DeleteMember("//a[@id='removeUsers']"), btn_PopUpOk("css=div[id='buttonRowContainer']>ul>li:nth-child(2)>a"), ele_MemberCount("css=ul[id=\"userListContainer\"]>li"), btn_PopUpCancel("link=Cancel"), chk_User("//div[@class='list_col_content']/div/dl/dt[text()=' <param_MemberName>']/../../../../../li/div/input"), chk_FirstMember("css=input[id='ck_0']"), ele_TeamMemberCount("//li[@class='list_row_item cleanable']"), ele_AddedMember("//dt[contains(text(),'<param_MemberName>')]"), btn_MemberDelete("//a[@data-id='<param_MemberName>']"), ele_lblUserwithRole("//li/div/div/dl/dt[contains(text(),'<param_Username>')]/../../../../../li[3]/div[contains(text(),'<param_Role>')]");

    private String searchPath;
  
    /**
    *  Page ApplicationTeam.
    */
    private ApplicationTeam(final String psearchPath) {
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