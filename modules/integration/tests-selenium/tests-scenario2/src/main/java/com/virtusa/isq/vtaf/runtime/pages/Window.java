package com.virtusa.isq.vtaf.runtime.pages;

/**
 *  Class Window implements corresponding UI page
 *  UI objects in the page are stored in the class.
 */

public enum Window {

        win_NewWindow("index=<param_NewWindowNumber>");

    private String searchPath;
  
    /**
    *  Page Window.
    */
    private Window(final String psearchPath) {
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