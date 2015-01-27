package com.virtusa.isq.vtaf.runtime.pages;

/**
 *  Class CreateNewDBUser implements corresponding UI page
 *  UI objects in the page are stored in the class.
 */

public enum CreateNewDBUser {

        tf_Username("//input[@id='Username']"), tf_Password("//input[@id='Password']"), tf_RepeatPassword("//input[@id='repeatPassword']"), ele_ddDBEnvironment("//a[@class='select2-choice']"), ele_ListItemDBEnvironment("//div[text()='<param_environment>']"), btn_CreateDBUser("//input[@value='Create DB User']"), lnk_Cancel("//a[text()='Cancel']"), ele_Environment("//input[@id='s2id_autogen2']"), btn_AddnewUser("//span[@id='add_new_user_btn']"), tf_NewUser("//input[@id='db_username']"), tf_newUserPassWord("//input[@id='new_user_password']"), tf_NewRepeatPassWord("//input[@id='new_user_repeat_password']"), btn_CreateUser("xpath=(//input[@name='Submit'])[2]");

    private String searchPath;
  
    /**
    *  Page CreateNewDBUser.
    */
    private CreateNewDBUser(final String psearchPath) {
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