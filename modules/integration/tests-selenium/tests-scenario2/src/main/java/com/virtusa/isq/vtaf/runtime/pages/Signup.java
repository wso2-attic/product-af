package com.virtusa.isq.vtaf.runtime.pages;

/**
 *  Class Signup implements corresponding UI page
 *  UI objects in the page are stored in the class.
 */

public enum Signup {

        tf_FirstName("css=input[id=\"firstName\"]"), tf_LastName("css=input[id=\"lastName\"]"), tf_Email("css=input[id=\"email\"]"), tf_Organization("css=input[id=\"organization\"]"), tf_Domain("css=input[id=\"domainName\"]"), tf_AdminUserName("css=input[id=\"adminUsername\"]"), tf_Password("css=input[id=\"password\"]"), tf_ConfirmPassword("css=input[id=\"password2\"]"), tf_WordVerification("css=input[id=\"captcha-user-answer\"]"), lbl_DomainExistErrorMsg("css=span[for=\"domainName\"]"), chk_AppCloud("css=input[id=\"app_cloud\"]"), chk_IntegrationCloud("css=input[id=\"integration_cloud\"]"), chk_APICloud("css=input[id=\"api_cloud\"]"), chk_AcceptAgreement("css=input[id=\"activateButton\"]"), btn_Submit("css=input[id=\"submitbtn\"]"), lbl_SubscriptionErrorMsg("//span[contains(text(), \"Select atleast one subscription.\")]"), lbl_InvalidEmailErrorMsg("//span[contains(text(), \"Please enter a valid email address.\")]");

    private String searchPath;
  
    /**
    *  Page Signup.
    */
    private Signup(final String psearchPath) {
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