package com.virtusa.isq.vtaf.runtime.pages;

/**
 *  Class ApplicationDatabaseConfigurations implements corresponding UI page
 *  UI objects in the page are stored in the class.
 */

public enum ApplicationDatabaseConfigurations {

        ele_IsDBUserAvailableUnderDBUsersSection("//*[text()='Database Users']/../ul/li[<param_userSection>]/ul/li/div/i/../../following-sibling::li/div/ul/li/a[contains(text(),'<param_dbUsername>')]");

    private String searchPath;
  
    /**
    *  Page ApplicationDatabaseConfigurations.
    */
    private ApplicationDatabaseConfigurations(final String psearchPath) {
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