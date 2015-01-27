package com.virtusa.isq.vtaf.runtime.pages;

/**
 *  Class Frames implements corresponding UI page
 *  UI objects in the page are stored in the class.
 */

public enum frames {

        frm_iframe("//div[@id='notification-slider-container']/../iframe"), frm_Parent("parent");

    private String searchPath;
  
    /**
    *  Page frames.
    */
    private frames(final String psearchPath) {
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