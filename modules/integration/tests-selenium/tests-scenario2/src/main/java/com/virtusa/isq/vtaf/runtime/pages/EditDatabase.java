package com.virtusa.isq.vtaf.runtime.pages;

/**
 *  Class EditDatabase implements corresponding UI page
 *  UI objects in the page are stored in the class.
 */

public enum EditDatabase {

        ele_IsDBUserListedUnderDatabaseUsers("//*[text()='Database User']/../../../following-sibling::li/ul/li/div[contains(text(),'<param_dbaseUser>')]"), btn_EditPrivileges("//span[contains(text(),'Edit Privileges')]"), chk_EditSelectPrivilege("//input[@id='editselectPriv']"), chk_EditInsertPrivilege("//input[@id='editinsertPriv']"), chk_EditUpdatePrivilege("//input[@id='editupdatePriv']"), btn_DeleteDB("//a[@id='btn_delete_db']"), btn_PopUpOK("//a[@class='btn main big modal_action' and text()='Ok']"), btn_PopUpCancel("//a[@class='btn sub big modal_cancel']"), ele_lblDBNameHeader("//*[contains(text(),'<param_DBName>')]");

    private String searchPath;
  
    /**
    *  Page EditDatabase.
    */
    private EditDatabase(final String psearchPath) {
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