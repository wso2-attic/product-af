package com.virtusa.isq.vtaf.runtime.pages;

/**
 *  Class DbUser implements corresponding UI page
 *  UI objects in the page are stored in the class.
 */

public enum DbUser {

        btn_DeleteUser("//a[text()='Delete User']"), btn_PopUpOK("//a[@class='btn main big modal_action' and text()='Ok']"), btn_PopUpCancel("//a[@class='btn sub big modal_cancel']"), ele_DeleteDBUserPromptMessage("//dd[contains(text(),'Are you sure you want to delete the user <param_DBUserName>')]");

    private String searchPath;
  
    /**
    *  Page DbUser.
    */
    private DbUser(final String psearchPath) {
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