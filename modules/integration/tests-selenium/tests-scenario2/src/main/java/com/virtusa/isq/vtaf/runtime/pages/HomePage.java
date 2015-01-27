package com.virtusa.isq.vtaf.runtime.pages;

/**
 *  Class HomePage implements corresponding UI page
 *  UI objects in the page are stored in the class.
 */

public enum HomePage {

        lnk_UserName("css=div[class='dropdown user right']>a "), lnk_SignOut("link=Sign Out"), btn_AddNewApplication("link=Add New Application"), ele_ApplicationName("//a[text()='<param_AppName>']"), lbl_ApplicationNumberofBranches("//div[@class='app_name col truncate']/h2/a[@title='<param_ApplicationName>']/../../../div[3]"), lbl_ApplicationType("//div[@class='app_name col truncate']/h2/a[@title='<param_ApplicationName>']/../../../div[2]"), btn_ManageUsers("css=span[class=\"icon-group\"]"), lbl_LoadingIcon("css=span[id=\"main-spinner\"]"), btn_Home("link=Home"), ele_Search("//input[@id='search']"), Img_AppLogoSub("<param_AppLogoSubPath>");

    private String searchPath;
  
    /**
    *  Page HomePage.
    */
    private HomePage(final String psearchPath) {
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