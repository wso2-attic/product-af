package com.virtusa.isq.vtaf.runtime.pages;

/**
 *  Class ApplicationReposandBuilds implements corresponding UI page
 *  UI objects in the page are stored in the class.
 */

public enum ApplicationReposandBuilds {

        btn_Build("//a[@class='btn main small build_action']"), btn_Deploy("//a[@class='btn main small deploy_action']"), ele_BuildMsg("css=p:contains(\"Build has been triggered succesfully - Refresh the page in few seconds.\")"), ele_DeployMsg("css=p:contains(\"Deployment has been submitted successfully - Refresh the page in few seconds.\")"), tf_NewVersionNumber("css=input[name='create_branch']"), btn_CreateBranchMain("link=Create Branch"), btn_CreateBranchSub("css=input[class='btn main small create_branch_button']"), lbl_DeployStatus("//span[@class='deployment_status']"), btn_CreateBranchTrunk("//section[@id='repositories_and_builds_list']/ul/li/div/div/a"), ele_BranchErrorMsg("css=div[id='undefined-sticky-wrapper']>div>div>div>div>p"), lbl_CreatedBranch("css=strong[class='version_number']:contains('<param_Version>')"), lbl_BranchStage("//strong[@class='version_number' and text()='<param_VerisonNumber>']/../span/i[3]"), btn_Settings("//ul[@class='inline_list']/li[4]/a"), chk_AutoDeploy("//input[@name='auto_deploy']"), chk_AutoBuild("//input[@name='auto_build']"), btn_Save("//strong[@class='version_number' and text()='<param_VersionNumber>']/../../../li[3]/div/ul/li[4]/div/form/div[2]/input"), btn_Cancel("//strong[@class='version_number' and text()='<param_VersionNumber>']/../../../li[3]/div/ul/li[4]/div/form/div[2]/a"), lnk_BuildServer("link=Build Server"), ele_BuildUIButtonPanel("//strong[@class='version_number' and text()='<param_VersionNumber>']/../../../li[3]/div/ul"), btn_Test("//strong[@class='version_number' and text()='<param_VersionNumber>']/../../../li[4]/div/ul/li/a"), btn_CopyURL("//strong[@class='version_number' and text()='<param_VersionNumber>']/../../ul/li/a"), lnk_BrowseURL("//strong[@class='version_number' and text()='<param_VersionNumber>']/../../ul/li[2]/a"), lbl_VersionHeading("css=section[id='repositories_and_builds_list']>ul>li>h2"), lbl_LastBuildHeading("css=section[id='repositories_and_builds_list']>ul>li:nth-child(3)>h2"), lbl_LastDeploymentHeading("css=section[id='repositories_and_builds_list']>ul>li:nth-child(4)>h2"), ele_lblBranchVersion("//*[contains(text(),'<param_VersionNumber>')]"), btn_CreateBranch("//strong[contains(text(),'trunk')]/../../div/div/a[contains(text(),'Create Branch')]/../div/form/div[2]/input[@value='Create Branch']"), ele_BranchPanel("//div[@class='icon_link_popover popover_form highlight']"), ele_lblErrorMessage("//p[contains(text(),'<param_ErrorMessage>')]"), ele_lblBuildAndSuccesfulMsg("//strong[contains(text(),'<param_VersionNumber>')]/../../../li[3]/p[contains(text(),'<BuildNumber>')]/span[contains(text(),'successful')]"), ele_lblDeployStatus("//strong[contains(text(),'<param_VersionNumber>')]/../../../li[4]/p[contains(text(),'<BuildNumber>')]"), ele_AppStage("//div[@id='s2id_stagelist_masterrepo']/a/span"), ele_ExpandIcon("css=i[class='icon-chevron-right']"), lbl_BuildStatus("//span[@class='build_status']/span[2]"), lbl_BuildNumber("//span[@class='build_status']/span"), ele_ddStageList("//div[@id='s2id_stagelist_masterrepo']/a/div/b"), ele_ddVersionList("//div[@id='s2id_versionlist_masterrepo']/a/div/b"), lbl_AppStage("//div[@class='select2-result-label' and contains(text(),'<param_Stage>')]"), lbl_VersionNumber("//div[text()='<param_VersionNumber>']"), btn_Launch("link=Launch"), btn_Fork("//button[@id='tenant_jenkins_url']"), ele_ForkedRepo("//i[@class='icon-code-fork fork-icon-colored']"), btn_AddFork("//a[@id='fork' and @original-version='<param_VersionNumber>']"), lbl_ForkedRepo("//*[text()='Forked Repository']"), ele_ForkURL("//a[@data-clipboard-text='https://git.appfactory.private.wso2.com:8443/git/~<param_DomainName>.com/<param_ForkedUser>/<param_AppKey>.git']"), btn_ForkBuild("//a[@class='btn main small buildfork_action']"), lbl_ForkBuildStatus("//div[@id='repositories_and_builds_list_master']/div/div/div/div/table/tbody/tr[2]/td[2]/div/span"), lbl_MasterRepoBuildNumber("//div[@id='repositories_and_builds_list_master']/div/div/div/div/table/tbody/tr[5]/td[2]/div/span/span"), lbl_MasterRepoBuildStatus("//div[@id='repositories_and_builds_list_master']/div/div/div/div/table/tbody/tr[5]/td[2]/div/span/span[2]"), lbl_DeployedNumber("//div[@id='repositories_and_builds_list_master']/div/div/div/div/table/tbody/tr[5]/td[3]/div/span[contains(text(),'Build <param_DeplyedNumber> Deployed')]"), btn_SaveSettings("//input[@value='Save']"), lbl_BuildSpinningIcon("xpath=(//span[@class='icon-spinner icon-spin'])[1]");

    private String searchPath;
  
    /**
    *  Page ApplicationReposandBuilds.
    */
    private ApplicationReposandBuilds(final String psearchPath) {
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