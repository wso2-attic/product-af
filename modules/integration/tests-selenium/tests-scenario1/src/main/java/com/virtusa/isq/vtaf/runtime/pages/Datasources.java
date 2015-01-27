package com.virtusa.isq.vtaf.runtime.pages;

/**
 *  Class Datasources implements corresponding UI page
 *  UI objects in the page are stored in the class.
 */

public enum Datasources {

        lnk_DatasourceName("//div[@id='content']/li/ul/li/div/ul/li/a[contains(text(),'<param_DatasourceName>')]"), lbl_DatabaseURL("//div[@id='content']/li/ul/li/div/ul/li/a[contains(text(),'<param_DatasourceName>')]/../../../../following-sibling::li[2]/div/dl/dt/i/../../../../following-sibling::li[2]/div/dl/dd"), btn_DeleteDatasource("css=a[id=\"delete_button_div\"]"), btn_PopUpOK("link=Ok"), btn_PopUpCancel("link=Cancel"), ele_ddStage("//div[@id='s2id_stage']"), lbl_DataSourceStageTesting("//div[@class='select2-result-label' and contains(text(),'Testing')]"), datasourcerow("//ul[@class='list_row  ']"), lbl_DatasourceDatabaseURL("//dd[text()='<param_DatabaseURL>']"), lbl_DatasourceStage("//div[@class='select2-result-label' and contains(text(),'<param_DatasourceStage>')] "), ele_ddDatasourceStage("//div[@id='s2id_stage']/a "), ele_lblSelectedDatabaseURLForEdit("//div/a/span[contains(text(),'jdbc:mysql://rss')]"), ele_lnkDataSourceName("//div[@id='content']/li/ul/li/div/ul/a[contains(text(),'<DataSourceName>')]");

    private String searchPath;
  
    /**
    *  Page Datasources.
    */
    private Datasources(final String psearchPath) {
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