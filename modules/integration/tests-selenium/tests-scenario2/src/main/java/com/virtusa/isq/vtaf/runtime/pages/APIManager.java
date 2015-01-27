package com.virtusa.isq.vtaf.runtime.pages;

/**
 *  Class APIManager implements corresponding UI page
 *  UI objects in the page are stored in the class.
 */

public enum APIManager {

        lnk_APIName("link=<param_APIName> (<param_APIVersion>)"), lst_Applications("css=select[id=\"application-list\"]"), btn_Subscribe("css=button[id=\"subscribe-button\"]"), btn_GoToMySubscriptions("//a[text()='Go to My Subscriptions']"), lbl_PopUPMsg("//*[text()='Subscription Successful']"), btn_GenerateProduction("css=button[data-keytype=\"PRODUCTION\"]"), btn_GenerateSandbox("css=button[data-keytype=\"SANDBOX\"]"), lbl_ProductionKey("//h3[contains(text(),'Keys - Production')]/../div/div/div/div/div[4]/span"), lbl_KeySandbox("//h3[contains(text(),'Keys - Production')]/../div[2]/div/div/div/div[4]/span"), ele_ddApplicationList("//div[@id='s2id_appListSelected']/a/span/b"), lbl_AppKey("//div[text()='<param_AppKey>']"), lnk_APIs("link=APIs"), ele_AppKey("//select[@id='application-list']/optgroup/option[contains(text(),'<param_AppKey>')]");

    private String searchPath;
  
    /**
    *  Page APIManager.
    */
    private APIManager(final String psearchPath) {
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