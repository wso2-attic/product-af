package com.virtusa.isq.vtaf.runtime.pages;

/**
 *  Class SampleIndex implements corresponding UI page
 *  UI objects in the page are stored in the class.
 */

public enum sampleIndex {

        lbl_SampleText("//*[text()='<param_SampleText>']");

    private String searchPath;
  
    /**
    *  Page sampleIndex.
    */
    private sampleIndex(final String psearchPath) {
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