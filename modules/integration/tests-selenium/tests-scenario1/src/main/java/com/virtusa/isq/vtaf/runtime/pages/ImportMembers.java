package com.virtusa.isq.vtaf.runtime.pages;

/**
 *  Class ImportMembers implements corresponding UI page
 *  UI objects in the page are stored in the class.
 */

public enum ImportMembers {

        tf_UserList("css=textarea[id=\"users\"]"), tf_DefaultPassword("css=input[id=\"password\"]"), tf_ConfirmDefaultPassword("css=input[id=\"password2\"]"), btn_Import("css=button[id=\"btnImport\"]"), lbl_ErrorMsg("//div[@id='reposBuild']/div/div/p"), lbl_PwdErrorMsg("//span[@id='pwdError']");

    private String searchPath;
  
    /**
    *  Page ImportMembers.
    */
    private ImportMembers(final String psearchPath) {
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