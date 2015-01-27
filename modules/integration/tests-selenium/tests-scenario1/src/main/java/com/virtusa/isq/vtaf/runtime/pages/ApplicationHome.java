package com.virtusa.isq.vtaf.runtime.pages;

/**
 *  Class ApplicationHome implements corresponding UI page
 *  UI objects in the page are stored in the class.
 */

public enum ApplicationHome {

        lnk_Overview("link=Overview"), lnk_RepoandBuild("link=Repos & Builds"), lnk_Team("link=Team"), lnk_LifeCycleManagement("link=Lifecycle Management"), lnk_ResourceTab("css=a[id=\"menu_dbAdmin\"]"), lnk_Issues("link=Issues"), win_ApplicationHome("index=0"), lbl_LoadIcon("css=span[class=\"icon-spinner icon-spin icon-large spin-large\"]"), lnk_DeployedVersions("link=Deployed Versions"), lnk_Logs("link=Logs"), lnk_Databases("link=Databases");

    private String searchPath;
  
    /**
    *  Page ApplicationHome.
    */
    private ApplicationHome(final String psearchPath) {
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