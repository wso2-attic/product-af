package com.virtusa.isq.vtaf.runtime.pages;

/**
 *  Class Jenkins implements corresponding UI page
 *  UI objects in the page are stored in the class.
 */

public enum Jenkins {

        tf_UserName("//input[@id='j_username']"), tf_Password("//input[@name='j_password' and @type='password']"), btn_Login("//button[@id='yui-gen1-button' and @type='button']"), frm_body("id='_yuiResizeMonitor'"), lnk_AppVersionName("//table[@id='projectstatus']/tbody/tr[@id='job_<param_AppNameSimple>-<param_VersionNumber>-default']/td[3]/a"), lnk_LastUpdateBuildVersion("//td[@id='main-panel']/ul/li[3]/a"), lbl_LastBuildNumber("//td[@id='main-panel']/h1[contains(text(),'Build #<param_BuildNumber>')]"), lnk_BuldNumber("//a[@href='../buildNumber' and text()='Build number']"), lbl_BuildVersionFinalVerify("//*[text()='<param_BuildVersion>']"), lnk_Logout("css=b:contains(\"log out\")");

    private String searchPath;
  
    /**
    *  Page Jenkins.
    */
    private Jenkins(final String psearchPath) {
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