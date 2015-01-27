package com.virtusa.isq.vtaf.runtime.pages;

/**
 *  Class NewSubDomain implements corresponding UI page
 *  UI objects in the page are stored in the class.
 */

public enum NewSubDomain {

;

    private String searchPath;
  
    /**
    *  Page NewSubDomain.
    */
    private NewSubDomain(final String psearchPath) {
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