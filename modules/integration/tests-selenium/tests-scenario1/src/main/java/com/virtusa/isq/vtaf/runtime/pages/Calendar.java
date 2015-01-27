package com.virtusa.isq.vtaf.runtime.pages;

/**
 *  Class Calendar implements corresponding UI page
 *  UI objects in the page are stored in the class.
 */

public enum Calendar {

        lbl_Date("//a[text()='<param_Date>']"), ele_ddMonth("//select[@title='Change the month']"), ele_ddYear("//select[@title='Change the year']");

    private String searchPath;
  
    /**
    *  Page Calendar.
    */
    private Calendar(final String psearchPath) {
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