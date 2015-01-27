package com.virtusa.isq.vtaf.runtime.pages;

/**
 *  Class ApplicationOverview implements corresponding UI page
 *  UI objects in the page are stored in the class.
 */

public enum ApplicationOverview {

        lbl_AppKey("//*[text()='Application Key']/../p"), lbl_AppType("css=p[id=\"apptype\"]"), lbl_AppDescription("css=p[id=\"description\"]"), lbl_RepoType("css=p[id=\"repotype\"]"), lbl_AppOwner("css=p[id='appowner']>p"), tf_EditAppDescription("css=textarea[id=\"appDescriptionEdit_textarea\"]"), tf_DisabledAppDescription("css=textarea[id=\"appDescriptionEdit\"]"), btn_SaveDescription("css=input[value=\"Save\"]"), ele_ToolTipDescription("//div[@id='qtip-0-content' and text()='Click here to edit the description']"), lnk_VersionNumber("//strong[@class='version_number overview_version' and text()='<param_VersionNumber>']"), lnk_BrowseURL("css=span[class=\"icon-globe \"]"), lbl_LifeCycleState("//strong[text()='<param_VersionNumber>']/../../../li[2]/p[text()='<param_LifeCycleState>']"), lbl_CompletionDate("//strong[text()='<param_VersionNumber>']/../../../li[3]/p/time[text()='<param_Date>']"), lbl_BuildStatus("//strong[text()='<param_VersionNumber>']/../../../li[4]/p/span/strong[text()='<param_BuildStatus>']"), btn_Test("//strong[text()='<versionNo>']/../../../li[5]/p/a"), lbl_Bugs("//strong[text()='<param_VersionNumber>']/../../../li[5]/div/ul/li/a"), lbl_NumberofUsers("css=ul[id='userCountList']>li>a"), lbl_NumberofDatasources("//ul[@id='resourceCountList']/li/a/span"), lbl_NumberofProperties("//span[@id='propCount']"), lbl_BuildVersionNumber("css=strong[class=\"version_number overview_version\"]:contains(\"<param_VersionNumber>\")"), lbl_Vulnerability("//strong[text()='<param_VersionNumber>']/../../../li[5]/div/ul/li[3]/a  "), lbl_Features("//strong[text()='<param_VersionNumber>']/../../../li[5]/div/ul/li[2]/a"), lbl_Team("//ul[@id='userCountList']/li/a[text()='<param_NumberOfMembers> Members']"), win_AppOverviewIndex("index=0"), lbl_NumberofDatabases("//span[@id='dbCount']"), lbl_NumberofAPIs("//span[@id='apiCount']"), lbl_IssuesCount("//p[@class='version']/strong[contains(text(),'<param_VersionNumber>')]/../../../li[5]/p"), ele_ArrowIndicator("//strong[@class='version_number overview_version' and contains(text(),'<param_VersionNumber>')]"), lbl_DatasourcesCount("//ul[@id='resourceCountList']/li/a"), lbl_DatabasesCount("//ul[@id='resourceCountList']/li[2]/a"), lbl_PropertiesCount("//ul[@id='resourceCountList']/li[4]/a"), lbl_LifeCycleStateVerifyText("//strong[text()='<param_VersionNumber>']/../../../li[2]/p"), lbl_LifeCycleStateVerification("//strong[text()='<param_VersionNumber>']/../../../li[2]/p"), lbl_LifeCycleStateHeading("css=div[id='repositories_and_builds_list_content']>ul>li:nth-child(2)"), lbl_VersionHeading("css=div[id='repositories_and_builds_list_content']>ul>li:nth-child(1)"), lbl_CompletionDateHeading("css=div[id='repositories_and_builds_list_content']>ul>li:nth-child(3)"), lbl_BuildStatusHeading("css=div[id='repositories_and_builds_list_content']>ul>li:nth-child(4)"), lbl_IssuesHeading("css=div[id='repositories_and_builds_list_content']>ul>li:nth-child(5)"), lbl_CurrentStatusLoadIcon("css=span[class='icon-spinner icon-spin icon-large']"), ele_lblApplicationType("//*[@id='apptype' and contains(text(),'<param_ApplicationType>')]"), ele_lblDescription("//*[@id='description' and contains(text(),'<param_Description>')]"), ele_lblRepositoryType("//*[@id='repotype' and contains(text(),'<param_RepositoryType>')]"), ele_lblDatasources("//ul[@id='resourceCountList']/li/a/span[contains(text(),'<param_DataSourceCount>')]"), lnk_Datasources("//ul[@id='resourceCountList']/li/a"), ele_lblEditDescription("//*[text()='<param_EditDescription>' and @id='description']"), ele_SubDomain("css=span[id='sub_domain_hover']>span"), tf_SubDomain("css=input[id='sub_domain_edit']"), btn_Save("css=button[id='sub_domain_save_btn']"), btn_ImgEditPencil("//span[@id='appName']/../div/div[2]/span"), btn_DescEditPencil("//span[@id='description_hover']/../span[2]"), tf_Description("//textarea[@id='description_edit']"), btn_SaveDesc("//button[@id='description_save_btn']"), lbl_Description("//span[@id='description_hover']"), btn_SelectFile("//input[@id='icon']"), Img_AppLogo("C:\\AF_Files\\ImagePath\\AppLogoMain.jpg"), btn_AcceptDeploy("//span[contains(text(),'<param_VersionNumber>')]/../../../td[5]/a[contains(text(),'Accept & Deploy')]"), lnk_Open("//span[contains(text(),'<param_VersionNumber>')]/../../../td[5]/a[contains(text(),'Open')]"), btn_Deploy("//span[contains(text(),'<param_VersionNumber>')]/../../../td[5]/a[contains(text(),'Deploy')]");

    private String searchPath;
  
    /**
    *  Page ApplicationOverview.
    */
    private ApplicationOverview(final String psearchPath) {
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