package com.virtusa.isq.vtaf.runtime.pages;

/**
 *  Class CreateNewDatabase implements corresponding UI page
 *  UI objects in the page are stored in the class.
 */

public enum CreateNewDatabase {

        ele_ddDBEnvironment("//a[@class='select2-choice']"), ele_ListItemDBEnvironment("//div[text()='<param_DatabaseEnvironment>']"), tf_DBName("//input[@id='databaseName']"), tf_DefaultPassword("//input[@id='databaseUserPassword']"), tf_ConfirmPassword("//input[@id='confirmDatabaseUserPassword']"), chk_AdvancedOptions("//input[@id='advancecheckbox']"), btn_CreateDatabase("//input[@value='Create Database']"), lnk_Cancel("css=div.buttonrow>a"), ele_ErrorMessageLabel1("//*[@class='message-content-area']"), ele_ErrorMessageLabel2("//*[@class='message-content-area'][2]"), ele_ErrorMessageLabel3("//*[@class='message-content-area'][3]"), ele_ddUser("//div[@id='s2id_js_db_user']/a/div/b"), ele_ListItemDBUser("//div[@class='select2-result-label'][contains(text(),'<param_username>')]"), lbl_CreateNewDBUser("//div[@class='select2-result-label'][contains(text(),'Create New User')]"), ele_ddPermissionTemplate("//div[@id='s2id_js_db_template']/a/div/b"), ele_ListItemDBTemplate("//div[@class='select2-result-label'][contains(text(),'<param_DBTemplate>')]"), tf_NewUserPassword("//input[@id='new_user_password']"), tf_NewUserRepeatPassword("//input[@id='new_user_repeat_password']"), tf_NewUserUsername("//input[@id='db_username']"), btn_CreateDBUser("//input[@value='Create DB User']"), ele_lblHeading("//*[text()='Create New Database']"), lnk_CancelUser("//a[@class='js_cancel_user' and text()='Cancel']"), tf_NewpermissionTemplateName("//input[@id='template_name']"), lnk_CancelTemplate("//a[@class='js_cancel_template' and text()='Cancel']"), ele_ddDatabaseEnvironment("css=div[id='s2id_rssInstances']>a>div>b"), ele_lblErrorMessage("//div[@class='left message-container']/p[contains(text(),'<param_ErrorMessage>')]"), btn_CreateTemplate("//input[@value='Create Template']"), lbl_CreateNewDBTemplate("//div[@class='select2-result-label'][contains(text(),'Create New Template')]"), lbl_ErrorUserPassword("//div[@class='left message-container']/p[contains(text(),'Invalid Password - Capitalization matters. Use 6 to 15 characters.')]"), ele_SelectedDBUser("//div[@id='s2id_js_db_user']/a/span[contains(text(),'<param_SelectedDBUser>')]"), ele_SelectedDBTemplate("//div[@id='s2id_js_db_template']/a/span[contains(text(),'<param_SelectedDBTemplate>')]"), btn_AddNewDatabase("link=Add New Database"), lnk_Database("//span[text()='<param_AppStage>']/../../td[text()='<param_DatabaseName>_<param_UserName>_com']"), btn_EditDatabase("//span[text()='<param_DBStage>']/../../td[text()='<paramDBname>_<param_UserName>_com']/../td[4]/a"), lbl_DBUser("//div[@class='list_col_content'][contains(text(),'<param_DBUserName>')]");

    private String searchPath;
  
    /**
    *  Page CreateNewDatabase.
    */
    private CreateNewDatabase(final String psearchPath) {
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