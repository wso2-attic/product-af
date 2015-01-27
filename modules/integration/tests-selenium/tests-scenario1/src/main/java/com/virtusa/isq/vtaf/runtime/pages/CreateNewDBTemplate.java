package com.virtusa.isq.vtaf.runtime.pages;

/**
 *  Class CreateNewDBTemplate implements corresponding UI page
 *  UI objects in the page are stored in the class.
 */

public enum CreateNewDBTemplate {

        tf_TemplateName("//input[@id='templateName']"), ele_ddDBEnvironment("//a[@class='select2-choice']"), ele_ListItemDBEnvironment("//div[text()='<param_environment>']"), btn_CreateTemplate("//input[@value='Create Template']"), lnk_Cancel("//a[text()='Cancel']"), chk_SelectPrivilege("css=input[id=\"selectPriv\"]"), chk_SelectPriv("css=input[id=\"selectPriv\"]"), chk_InsertPriv("css=input[id=\"insertPriv\"]"), chk_InsertPrivilege("css=input[id=\"insertPriv\"]"), chk_UpdatePriv("css=input[id=\"updatePriv\"]"), chk_UpdatePrivilege("css=input[id=\"updatePriv\"]"), chk_DeletePriv("css=input[id=\"deletePriv\"]"), chk_CreatePriv("css=input[id=\"createPriv\"]"), chk_DropPriv("css=input[id=\"dropPriv\"]"), chk_GrantPriv("css=input[id=\"grantPriv\"]"), chk_ReferencesPriv("css=input[id=\"referencesPriv\"]"), chk_IndexPriv("css=input[id=\"indexPriv\"]"), chk_AlterPriv("css=input[id=\"alterPriv\"]"), chk_CreateTempTable("css=input[id=\"createTmpTablePriv\"]"), chk_LockTablesPriv("css=input[id=\"lockTablesPriv\"]"), chk_CreateViewPriv("css=input[id=\"createViewPriv\"]"), chk_ShowViewPriv("css=input[id=\"showViewPriv\"]"), chk_CreateRoutinePriv("css=input[id=\"createRoutinePriv\"]"), chk_AlterRoutinePriv("css=input[id=\"alterRoutinePriv\"]"), chk_ExecutePriv("css=input[id=\"executePriv\"]"), chk_EventPriv("css=input[id=\"eventPriv\"]"), chk_TriggerPriv("css=input[id=\"triggerPriv\"]"), btn_Save("css=input[name='Submit']");

    private String searchPath;
  
    /**
    *  Page CreateNewDBTemplate.
    */
    private CreateNewDBTemplate(final String psearchPath) {
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