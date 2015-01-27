package com.virtusa.isq.vtaf.runtime.pages;

/**
 *  Class TestPage implements corresponding UI page
 *  UI objects in the page are stored in the class.
 */

public enum TestPage {

        lbl_HelloJSP("//h1[text()='Hello JSP']"), btn_ClickMe("css=input[type=\"submit\"]"), win_TestPage("index=1");

    private String searchPath;
  
    /**
    *  Page TestPage.
    */
    private TestPage(final String psearchPath) {
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