package com.virtusa.isq.vtaf.runtime.pages;

/**
 *  Class ApplicationDeployedVersions implements corresponding UI page
 *  UI objects in the page are stored in the class.
 */

public enum ApplicationDeployedVersions {

        btn_Browse("//input[@id='uploaded_application']"), btn_Upload("//input[@id='appcreation']"), btn_Deploy("//td[text()='<param_VersionNumber>']/../td[3]/a"), lbl_Version("//td[text()='<param_VersionNumber>']"), lbl_VersionStage("//td[text()='param_VersionNumber']/../td[2][text()='<param_AppStage>']"), btn_Launch("//td[text()='<param_VersionNumber>']/../td[4]/a");

    private String searchPath;
  
    /**
    *  Page ApplicationDeployedVersions.
    */
    private ApplicationDeployedVersions(final String psearchPath) {
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