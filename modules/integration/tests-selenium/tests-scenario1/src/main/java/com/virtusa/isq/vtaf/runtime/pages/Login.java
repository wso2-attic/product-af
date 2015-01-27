package com.virtusa.isq.vtaf.runtime.pages;

/**
 *  Class Login implements corresponding UI page
 *  UI objects in the page are stored in the class.
 */

public enum Login {

        tf_UserName("css=input[id=\"username\"]"), tf_Password("css=input[id=\"password\"]"), btn_SignIn("//button[contains(text(),'Sign In')]"), lnk_SignUp("css=a[href=\"https://cloudmgt.cloudpreview.wso2.com/cloudmgt/site/pages/register.jag\"]"), lbl_LoginFailedErrorMsg("//*[contains(text(),'Login failed. Please recheck the username and password and try again')]"), tf_OldPassword("css=input[id='old_password']"), tf_NewPassword("css=input[id='password']"), tf_ConfirmNewPassword("css=input[id='password2']"), btn_Submit("css=button[id='btnSubmit']"), lnk_AppCloud("link=Go to App Cloud"), ele_lblErrorLoging("//*[text()='This field is required.']"), lbl_LoginErrorMsg("//div[@id='loginError']");

    private String searchPath;
  
    /**
    *  Page Login.
    */
    private Login(final String psearchPath) {
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