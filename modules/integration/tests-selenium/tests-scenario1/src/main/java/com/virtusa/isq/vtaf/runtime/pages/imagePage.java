package com.virtusa.isq.vtaf.runtime.pages;

/**
 *  Class ImagePage implements corresponding UI page
 *  UI objects in the page are stored in the class.
 */

public enum imagePage {

;

    private String searchPath;
  
    /**
    *  Page imagePage.
    */
    private imagePage(final String psearchPath) {
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