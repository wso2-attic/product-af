package com.virtusa.isq.vtaf.runtime.pages;

/**
 *  Class EditDBTemplate implements corresponding UI page
 *  UI objects in the page are stored in the class.
 */

public enum EditDBTemplate {

        btn_DeleteTemplate("//a[@id='btn_drop_template']"), btn_PopUPOk("//a[@class='btn main big modal_action' and text()='Ok']"), btn_PopUpCancel("//a[@class='btn sub big modal_cancel']"), lnk_EditPrivilage("css=span[onclick*=\"<param_UserName>\"]"), chk_SelectPriv("css=input[id=\"editselectPriv\"]"), chk_InsertPriv("css=input[id=\"editinsertPriv\"]"), chk_UpdatePriv("css=input[id=\"editupdatePriv\"]"), chk_DeletePriv("css=input[id=\"editdeletePriv\"]"), chk_CreatePriv("css=input[id=\"editcreatePriv\"]"), chk_DropPriv("css=input[id=\"editdropPriv\"]"), chk_GrantPriv("css=input[id=\"editgrantPriv\"]"), chk_ReferencesPriv("css=input[id=\"editreferencesPriv\"]"), chk_IndexPriv("css=input[id=\"editindexPriv\"]"), chk_AlterPriv("css=input[id=\"editalterPriv\"]"), chk_CreateTempTable("css=input[id=\"editcreateTmpTablePriv\"]"), chk_LockTablesPriv("css=input[id=\"editlockTablesPriv\"]"), chk_CreateViewPriv("css=input[id=\"editcreateViewPriv\"]"), chk_ShowViewPriv("css=input[id=\"editshowViewPriv\"]"), chk_CreateRoutinePriv("css=input[id=\"editcreateRoutinePriv\"]"), chk_AlterRoutinePriv("css=input[id=\"editalterRoutinePriv\"]"), chk_ExecutePriv("css=input[id=\"editexecutePriv\"]"), chk_EventPriv("css=input[id=\"editeventPriv\"]"), chk_TriggerPriv("css=input[id=\"edittriggerPriv\"]"), btn_Save("css=input[id=\"btn_save_user_prev\"]"), btn_Cancel("css=a[id=\"btn_edit_cancel\"]"), ele_DeleteDBTemplatePromptMessage("//dd[contains(text(),'Are you sure you want to delete the tempalte <param_TempName>')]"), btn_SaveChanges("//input[@value='Save Changes' and @name='Submit']");

    private String searchPath;
  
    /**
    *  Page EditDBTemplate.
    */
    private EditDBTemplate(final String psearchPath) {
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