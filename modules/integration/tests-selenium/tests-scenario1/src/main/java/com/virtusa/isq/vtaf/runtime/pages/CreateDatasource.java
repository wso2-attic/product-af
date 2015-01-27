package com.virtusa.isq.vtaf.runtime.pages;

/**
 *  Class CreateDatasource implements corresponding UI page
 *  UI objects in the page are stored in the class.
 */

public enum CreateDatasource {

        tf_DatasourceName("//input[@id='datasource_name']"), ara_Description("css=textarea[id='datasource_description']"), lbl_DatabaseURL("//div[contains(text(),'<param_DatabaseURL>')]"), lbl_DatabaseDriver("//span[text()='<param_DatabaseDriver>']"), tf_DatasourceUsername("//input[@id='datasource_username_text']"), ele_ddDatasourceUsername("//div[@id='s2id_datasource_username_select']/a"), tf_DatasourcePassword("//input[@id='datasource_password']"), btn_CreateDatasource("//input[@type='submit']"), ele_ddDatabaseDriver("//div[@id='s2id_database_driver']"), ele_ddDatabaseURL("//div[@id='s2id_datasource_url_select']/a"), lbl_DatasourceName("//a[text()='<DataSource>']"), btn_UpdateDatasource("css=input[name=\"Submit\"]"), btn_DeleteDatasource("css=a[id=\"delete_button_div\"]"), lnk_Cancel("link=Cancel"), lbl_DatasourceUsername("//div[@id='select2-drop']/ul/li/div[contains(text(),'<param_DatasourceUsername>')]"), ele_lblSelectedDatabaseURL("//div/a/span[contains(text(),'jdbc:mysql://localhost:3306')]"), ele_lblDatabaseDriver("//div[text()='<param_DatabaseDriver>']"), ele_DatabaseDriverDownArrow("//strong[contains(text(),'Database Driver')]/../../div/a/div"), ele_DatasourceUsernameDownArrow("//strong[contains(text(),'Username')]/../../div/div/a/div"), lbl_Username("//div[contains(text(),'<param_DatasourceUsername>')]"), ele_lblSelectedUsername("//strong[contains(text(),'Username')]/../../div/div/a/span[contains(text(),'<pram_SelectedUsername>')]"), ele_ddDSEnvironment("css=div[id='s2id_rssInstances']>a>div>b"), lbl_DSEnvironment("//div[text()='<param_DSEnv>']"), ele_ddDBDriver("css=div[id='s2id_database_driver']>a>div>b"), lbl_DBDriver("//div[text()='<param_DBDriver>']"), ele_ddDBURL("css=div[id='s2id_datasource_url_select']>a>div>b"), ele_ddDBUsername("css=div[id='s2id_datasource_username_select']>a>div>b"), lbl_DBUsername("//div[text()='<param_DBUsername>']");

    private String searchPath;
  
    /**
    *  Page CreateDatasource.
    */
    private CreateDatasource(final String psearchPath) {
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