package com.virtusa.isq.vtaf.runtime.pages;

/**
 *  Class GitblitHome implements corresponding UI page
 *  UI objects in the page are stored in the class.
 */

public enum GitblitHome {

        ele_lblWelcome("css=div[class='span7']>div>h2"), lnk_Gitblit("//img[@src='logo.png']");

    private String searchPath;
  
    /**
    *  Page GitblitHome.
    */
    private GitblitHome(final String psearchPath) {
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