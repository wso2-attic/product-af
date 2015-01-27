package com.virtusa.isq.vtaf.runtime.pages;

/**
 *  Class AddNewApplication implements corresponding UI page
 *  UI objects in the page are stored in the class.
 */

public enum AddNewApplication {

        tf_AppName("css=input[id=\"applicationName\"]"), tf_AppKey("css=input[id=\"applicationKey\"]"), btn_BrowseImage("css=input[value=\"Browse...\"]"), tf_Description("css=textarea[id=\"applicationDescription\"]"), btn_CreateApplication("//input[@id='appcreation']"), btn_Cancel("css=a:contains(\"Cancel\")"), ele_ApplicationType("//div[@class='select2-result-label' and contains(text(),'<param_ApplicationType>')]"), ele_ddCreateAppType("css=div[id='s2id_applicationType']>a>div>b"), ele_TeamAdminMemberName("//div[@class='list_col_content']/div/dl/dt[text()='<param_AdminMemberName>']"), ele_TeamOtherMemberName("//div[@class='list_col_content']/div/dl/dt[text()=' <param_MemberOtherName>']"), rdo_CreateApp("css=input[value='create_application']"), rdo_UploadApp("css=input[value='upload_war_file']"), btn_BrowseAppUpload("css=input[id='uploaded_application']"), ele_ddUploadAppType("//input[@id='s2id_autogen4']/../a/div/b"), btn_UploadApplication("//input[@id='appupload']"), lbl_AppCreationErrorMsg("//div[@id='appcreationerrormsg']/div/div/p"), ele_Spinner("//span[@id='progressSpinner']"), tf_SearchAppType("//div[@id='select2-drop']/div/input"), ele_IconSpinner("xpath=(//span[@class='icon-spinner icon-spin'])[1]");

    private String searchPath;
  
    /**
    *  Page AddNewApplication.
    */
    private AddNewApplication(final String psearchPath) {
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