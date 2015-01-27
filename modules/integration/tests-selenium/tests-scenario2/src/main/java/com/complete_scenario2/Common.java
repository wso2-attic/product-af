package com.complete_scenario2;

import com.virtusa.isq.vtaf.runtime.SeleniumTestBase;
import org.openqa.selenium.By;

/**
 *  Class Common contains reusable business components 
 *  Each method in this class correspond to a reusable business component.
 */
public class Common {

    /**
     *  Business component biscomp_Logout.
     */
    /**
     *  Business component biscomp_Logout.
     */
    public final static void biscomp_Logout(final SeleniumTestBase caller) throws Exception {
        //Sign out from the application
        caller.click("HomePage.lnk_UserName");
        caller.click("HomePage.lnk_SignOut");
        caller.pause("3000");
        if(caller.checkElementPresent("HomePage.lnk_UserName")){
        caller.click("HomePage.lnk_UserName");
        caller.click("HomePage.lnk_SignOut");
        //Verifying sign out is successful
        caller.checkElementPresent("Login.tf_UserName",false,"");
        }	
    }
    /**
     *  Business component biscomp_AssigningUsernames.
     */
    public final static void biscomp_AssigningUsernames(final SeleniumTestBase caller, final String param_UserList, final String param_UserInitials) throws Exception {
        //Assigining usernames to the users
        String varUserList = "";
        //Get the number of items
        int varMemberCount = 0;String[] members=param_UserList.split(",");varMemberCount=members.length;
        int varCount = 0;
        //Untill member array items ends select roles
        for(;varCount<varMemberCount;varCount++){
        if(members[varCount].contains("_QA")){
        String varNewUser = "";varNewUser=param_UserInitials+caller.generateData("Alphanumeric",3)+"_qa";
        //Store User name
        caller.store(members[varCount],"String",varNewUser);
        if(varCount!=varMemberCount-1){
        int varTemp = 1;varUserList=varUserList.concat(varNewUser).concat(",");
        } else {
        int varTemp = 1;varUserList=varUserList.concat(varNewUser);
        }
        } else if(members[varCount].contains("_DevOps")){
        String varNewUser = "";varNewUser=param_UserInitials+caller.generateData("Alphanumeric",3)+"_devops";
        //Store User name
        caller.store(members[varCount],"String",varNewUser);
        if(varCount!=varMemberCount-1){
        int varTemp = 1;varUserList=varUserList.concat(varNewUser).concat(",");
        } else {
        int varTemp = 1;varUserList=varUserList.concat(varNewUser);
        }
        } else if(members[varCount].contains("_AppOwner")){
        String varNewUser = "";varNewUser=param_UserInitials+caller.generateData("Alphanumeric",3)+"_appowner";
        //Store User name
        caller.store(members[varCount],"String",varNewUser);
        if(varCount!=varMemberCount-1){
        int varTemp = 1;varUserList=varUserList.concat(varNewUser).concat(",");
        } else {
        int varTemp = 1;varUserList=varUserList.concat(varNewUser);
        }
        } else if(members[varCount].contains("_DEV")){
        String varNewUser = "";varNewUser=param_UserInitials+caller.generateData("Alphanumeric",3)+"_dev";
        //Store User name
        caller.store(members[varCount],"String",varNewUser);
        if(varCount!=varMemberCount-1){
        int varTemp = 1;varUserList=varUserList.concat(varNewUser).concat(",");
        } else {
        int varTemp = 1;varUserList=varUserList.concat(varNewUser);
        }
        }
        }
        caller.store("keyUserList","String",varUserList);	
    }
    /**
     *  Business component biscomp_AssignUserRoles.
     */
    public final static void biscomp_AssignUserRoles(final SeleniumTestBase caller, final String param_UserList) throws Exception {
        //Selecting roles for users
        int varMemberCount = 0;String[] members=param_UserList.split(",");varMemberCount=members.length;
        int varCount = 0;
        caller.pause("3000");
        //Until member array items ends select roles(pause time for applicatin load)
        for(;varCount<varMemberCount;varCount++){
        caller.pause("6000");
        caller.clickAt("ManageMembers.chk_User","param_UserName_PARAM:" + members[varCount],"10,10");
        caller.clickAt("ManageMembers.btn_UserRoleEdit","10,10");
        caller.pause("1000");
        if(members[varCount].contains("_devops")){
        caller.click("ManageMembers.chk_DevOps");
        String varRole = "DevOps";
        caller.store("keyRole","String",varRole);
        } else if(members[varCount].contains("_qa")){
        caller.click("ManageMembers.chk_QA");
        String varRole = "QA";
        caller.store("keyRole","String",varRole);
        } else if(members[varCount].contains("_appowner")){
        caller.click("ManageMembers.chk_ApplicationOwner");
        String varRole = "Application Owner";
        caller.store("keyRole","String",varRole);
        } else if(members[varCount].contains("_dev")){
        caller.click("ManageMembers.chk_Developer");
        String varRole = "Developer";
        caller.store("keyRole","String",varRole);
        }
        caller.click("ManageMembers.btn_SaveRole");
        caller.pause("2000");
        String varRole = caller.retrieveString("keyRole");
        caller.pause("8000");
        caller.checkElementPresent("ApplicationTeam.ele_lblUserwithRole","param_Username_PARAM:" + members[varCount] + "_PARAM," + "param_Role_PARAM:" + varRole,false,"");
        }	
    }
    /**
     *  Business component biscomp_ChangingUserRole.
     */
    public final static void biscomp_ChangingUserRole(final SeleniumTestBase caller, final String param_UserList) throws Exception {
        //Selecting roles for users
        int varMemberCount = 0;String[] members=param_UserList.split(",");varMemberCount=members.length;
        int varCount = 0;
        //Until member array items ends select roles
        for(;varCount<varMemberCount;varCount++){
        caller.click("ManageMembers.chk_User","param_UserName_PARAM:" + members[varCount]);
        caller.click("ManageMembers.btn_UserRoleEdit");
        caller.pause("1000");
        if(members[varCount].contains("_dev")){
        caller.click("ManageMembers.chk_Developer");
        caller.pause("1000");
        caller.click("ManageMembers.chk_QA");
        String varRole = "QA";
        caller.store("keyRole","String",varRole);
        }
        caller.click("ManageMembers.btn_SaveRole");
        String varRole = caller.retrieveString("keyRole");
        caller.checkElementPresent("ApplicationTeam.ele_lblUserwithRole","param_Username_PARAM:" + members[varCount] + "_PARAM," + "param_Role_PARAM:" + varRole,false,"");
        }	
    }
    /**
     *  Business component biscomp_RemoveUser.
     */
    public final static void biscomp_RemoveUser(final SeleniumTestBase caller, final String param_UserList) throws Exception {
        //Selecting roles for users
        int varMemberCount = 0;String[] members=param_UserList.split(",");varMemberCount=members.length;
        int varCount = 0;
        //Until member array items ends select roles
        for(;varCount<varMemberCount;varCount++){
        caller.click("ManageMembers.chk_User","param_UserName_PARAM:" + members[varCount]);
        caller.click("ManageMembers.btn_UserDelete");
        caller.click("ManageMembers.btn_ConfirmDeleteOk");
        caller.checkElementPresent("ManageMembers.ele_lblMemberName","param_MemberName_PARAM:" + members[varCount],false,"");
        }	
    }
    /**
     *  Business component biscomp_RemoveUserFromApp.
     */
    public final static void biscomp_RemoveUserFromApp(final SeleniumTestBase caller, final String param_UserList, final String param_RemovedMsg) throws Exception {
        //Delete Team members
        caller.click("ApplicationHome.lnk_Team");
        //Assign members text string in to an array
        int varMemberCount = 0;String[] members=param_UserList.split(",");varMemberCount=members.length;
        int varCount = 0;
        //Untill member array items ends select users
        for(;varCount<varMemberCount;varCount++){
        caller.click("ApplicationTeam.chk_User","param_MemberName_PARAM:" + members[varCount]);
        //Click delete button
        caller.click("ApplicationTeam.btn_DeleteMember");
        caller.click("ApplicationTeam.btn_PopUpOk");
        //Refreshing the page
        caller.pause("2000");
        caller.keyPress("HomePage.btn_Home","F5");
        caller.pause("1000");
        caller.checkObjectProperty("ApplicationTeam.ele_TeamOtherMemberName","param_MemberOtherName_PARAM:" + members[varCount],"ELEMENTPRESENT","false",false,"");
        caller.checkElementPresent("Common.lbl_NotificationWallMsg","param_Message_PARAM:" + members[varCount]+" "+param_RemovedMsg,false,"");
        }	
    }
    /**
     *  Business component biscomp_ImportMembers.
     */
    public final static void biscomp_ImportMembers(final SeleniumTestBase caller, final String param_User, final String param_DefaultPassword, final String param_PasswordConf) throws Exception {
        //Import members in to domain
        caller.clickAt("HomePage.btn_ManageUsers","10,10");
        caller.clickAt("ManageMembers.btn_ImportMembers","10,10");
        caller.type("ImportMembers.tf_UserList",param_User);
        caller.type("ImportMembers.tf_DefaultPassword",param_DefaultPassword);
        caller.pause("1000");
        caller.type("ImportMembers.tf_ConfirmDefaultPassword",param_PasswordConf);
        caller.pause("1000");
        caller.clickAt("ImportMembers.btn_Import","10,10");
        caller.pause("2000");	
    }
    /**
     *  Business component biscomp_VerifyImportedMembers.
     */
    public final static void biscomp_VerifyImportedMembers(final SeleniumTestBase caller, final String param_ImportUsersList) throws Exception {
        //Verify entered domain members were saved
        int varValueCount = 12;String[] userArray=param_ImportUsersList.split(",");varValueCount=userArray.length;
        int varCount = 0;
        for(;varCount<varValueCount;varCount++){
        String varName = userArray[varCount];
        caller.checkElementPresent("ManageMembers.ele_lblMemberName","param_MemberName_PARAM:" + varName,false,"");
        }	
    }
    /**
     *  Business component biscomp_Login.
     */
    public final static void biscomp_Login(final SeleniumTestBase caller, final String param_Username, final String param_Password, final String param_MainURL) throws Exception {
        //Login in to the Application
        caller.open("<param_MainURL>","param_MainURL_PARAM:" + param_MainURL,"10000");
        caller.type("Login.tf_UserName",param_Username);
        caller.type("Login.tf_Password",param_Password);
        caller.click("Login.btn_SignIn");
        caller.pause("3000");
        if(caller.checkElementPresent("Login.tf_OldPassword")){
        caller.type("Login.tf_OldPassword",param_Password);
        caller.type("Login.tf_NewPassword",param_Password);
        caller.type("Login.tf_ConfirmNewPassword",param_Password);
        caller.click("Login.btn_Submit");
        } else if(caller.checkElementPresent("Login.lnk_AppCloud")){
        caller.click("Login.lnk_AppCloud");
        }	
    }
    /**
     *  Business component biscomp_CreateApplication.
     */
    public final static void biscomp_CreateApplication(final SeleniumTestBase caller, final String param_AppName, final String param_AppKey, final String param_AppDescription, final String param_AppImagePath, final String param_AppType, final String param_AppPath, final String param_AppCreationMethod) throws Exception {
        //Create an application
        caller.click("HomePage.btn_AddNewApplication");
        String varAppType = param_AppType;
        if(varAppType.equals("Java Web Application")){
        String varAppName = "WebApp "+param_AppName+caller.generateData("Alphanumeric",3);
        caller.store("keyAppName","String",varAppName);
        caller.type("AddNewApplication.tf_AppName",varAppName);
        } else if(varAppType.equals("JAX-RS Service")){
        String varAppName = "JaxRSApp_"+param_AppName+caller.generateData("Alphanumeric",3);
        caller.store("keyAppName","String",varAppName);
        caller.type("AddNewApplication.tf_AppName",varAppName);
        } else if(varAppType.equals("JAX-WS Service")){
        String varAppName = "JaxwSApp_"+param_AppName+caller.generateData("Alphanumeric",3);
        caller.store("keyAppName","String",varAppName);
        caller.type("AddNewApplication.tf_AppName",varAppName);
        } else if(varAppType.equals("Jaggery Application")){
        String varAppName = "JagApp"+param_AppName+caller.generateData("Alphanumeric",3);
        caller.store("keyAppName","String",varAppName);
        caller.type("AddNewApplication.tf_AppName",varAppName);
        } else if(varAppType.equals("WSO2 Data Service")){
        String varAppName = "DSApp_"+param_AppName+caller.generateData("Alphanumeric",3);
        caller.store("keyAppName","String",varAppName);
        caller.type("AddNewApplication.tf_AppName",varAppName);
        } else if(varAppType.equals("WAR")){
        String varAppName = "WarApp_"+param_AppName+caller.generateData("Alphanumeric",3);
        caller.store("keyAppName","String",varAppName);
        caller.type("AddNewApplication.tf_AppName",varAppName);
        } else if(varAppType.equals("Jaggery App")){
        String varAppName = "JagApp_"+param_AppName+caller.generateData("Alphanumeric",3);
        caller.store("keyAppName","String",varAppName);
        caller.type("AddNewApplication.tf_AppName",varAppName);
        }
        String varAppKey = param_AppKey+caller.generateData("Alphanumeric",3);
        caller.type("AddNewApplication.tf_AppKey",varAppKey);
        caller.store("keyAppKey","String",varAppKey);
        if("Create".equals(param_AppCreationMethod)){
        caller.click("AddNewApplication.rdo_CreateApp");
        caller.clickAt("AddNewApplication.ele_ddCreateAppType","1,1");
        caller.type("AddNewApplication.tf_SearchAppType",param_AppType);
        caller.clickAt("AddNewApplication.ele_ApplicationType","param_ApplicationType_PARAM:" + param_AppType,"1,1");
        } else if("Upload".equals(param_AppCreationMethod)){
        caller.click("AddNewApplication.rdo_UploadApp");
        caller.clickAt("AddNewApplication.ele_ddUploadAppType","1,1");
        caller.type("AddNewApplication.tf_SearchAppType",param_AppType);
        caller.clickAt("AddNewApplication.ele_ApplicationType","param_ApplicationType_PARAM:" + param_AppType,"1,1");
        caller.type("AddNewApplication.btn_BrowseAppUpload",param_AppPath);
        /*
        SetVariable
          name=fireEvent
          type=String
          paramValue="KEY%type="+param_AppPath
         ClickAt
          object=AddNewApplication.btn_BrowseAppUpload
          coordinates=1,1
         FireEvent
          event=@fireEvent
          waitTime=1000
        Pause
         ms=3000
         FireEvent
          event=KEY%key=\t
          waitTime=1000
         FireEvent
          event=KEY%key=\t
          waitTime=2000
        Pause
         ms=3000
         FireEvent
          event=KEY%key=\n
          waitTime=2000
        */
        caller.pause("3000");
        }
        if(param_AppImagePath!=null){
        caller.type("AddNewApplication.btn_BrowseImage",param_AppImagePath);
        /*
        Click
          object=AddNewApplication.btn_BrowseImage
         SetVariable
          name=fireEvent
          type=String
          paramValue="KEY%type="+param_AppImagePath
         FireEvent
          event=@fireEvent
          waitTime=2000
         FireEvent
          event=KEY%key=\t
          waitTime=2000
         FireEvent
          event=KEY%key=\t
          waitTime=2000
         FireEvent
          event=KEY%key=\n
          waitTime=2000
        */
        }
        if(param_AppDescription!=null){
        caller.type("AddNewApplication.tf_Description",param_AppDescription);
        }
        if("Upload".equals(param_AppCreationMethod)){
        caller.clickAt("AddNewApplication.btn_UploadApplication","1,1");
        //Wait for application loading
        caller.pause("8000");
        int varCount = 1;
        for(;varCount<=10;varCount++){
        if(caller.checkElementPresent("AddNewApplication.ele_Spinner")){
        caller.pause("8000");
        } else if(varCount==5){
        caller.fail("File is not uploaded.");
        } else {
        int varTemp = 1;break;
        }
        }
        }
        if("Create".equals(param_AppCreationMethod)){
        caller.clickAt("AddNewApplication.btn_CreateApplication","1,1");
        }
        /*
        CheckElementPresent
        	object=AddNewApplication.ele_IconSpinner
        	fail=false
        	customErrorMessage=
        */
        //Wait for page loading
        caller.pause("5000");	
    }
    /**
     *  Business component biscomp_CreateAppWithSameAppKey.
     */
    public final static void biscomp_CreateAppWithSameAppKey(final SeleniumTestBase caller, final String param_AppKey, final String param_SameAppKeyErrorMsg) throws Exception {
        //Create an application
        caller.click("HomePage.btn_AddNewApplication");
        String appName = "test"+caller.generateData("Alphanumeric",3);
        caller.type("AddNewApplication.tf_AppName",appName);
        caller.type("AddNewApplication.tf_AppKey",param_AppKey);
        caller.fireEvent("KEY%key=\t","2000");
        //Waiting for error message
        caller.pause("3000");
        //Verifying the ErrorMessage
        caller.checkObjectProperty("AddNewApplication.lbl_AppCreationErrorMsg","textContent",param_SameAppKeyErrorMsg,false,"");
        caller.clickAt("HomePage.btn_Home","0,0");
        //Waiting for page loading
        caller.pause("8000");	
    }
    /**
     *  Business component biscomp_VerifyingNotificationWallPost.
     */
    public final static void biscomp_VerifyingNotificationWallPost(final SeleniumTestBase caller, final String param_AppCreationMsg, final String param_GITRepoMsg, final String param_JenkinsMsg, final String param_IssueTrackerMsg, final String param_CloudMsg, final String param_AppName, final String param_AppKey) throws Exception {
        //Verifying the post on notification wall
        int varCount = 1;
        for(;varCount<=10;varCount++){
        if(caller.checkElementPresent("Common.ele_NotificationWallEntry")){
        int varTemp = 1;break;
        } else if(varCount==10){
        caller.fail("Notification Wall Messages are not displayed");
        } else {
        caller.pause("2000");
        caller.keyPress("HomePage.btn_Home","F5");
        }
        }
        //App creation message
        String CreateInProgress = "Application "+param_AppName+" "+param_AppCreationMsg;
        caller.checkElementPresent("Common.lbl_NotificationWallMsg","param_Message_PARAM:" + CreateInProgress,false,"");
        //GIT repo message
        String GITRepoMsg = param_GITRepoMsg+" "+param_AppKey;
        caller.checkElementPresent("Common.lbl_NotificationWallMsg","param_Message_PARAM:" + GITRepoMsg,false,"");
        //Jenkins Message
        String JenkinsMsg = param_JenkinsMsg+" "+param_AppKey;
        caller.checkElementPresent("Common.lbl_NotificationWallMsg","param_Message_PARAM:" + JenkinsMsg,false,"");
        //Issue Tracker Message
        String IssueTrackerMsg = param_IssueTrackerMsg+" "+param_AppKey;
        caller.checkElementPresent("Common.lbl_NotificationWallMsg","param_Message_PARAM:" + IssueTrackerMsg,false,"");
        //Cloud Env Message
        String CloudMsg = "Application "+param_AppKey+" "+param_CloudMsg;
        caller.checkElementPresent("Common.lbl_NotificationWallMsg","param_Message_PARAM:" + CloudMsg,false,"");	
    }
    /**
     *  Business component biscomp_VerifyingCreatedApplication.
     */
    public final static void biscomp_VerifyingCreatedApplication(final SeleniumTestBase caller, final String param_AppName, final String param_AppType, final int param_WaitTimeInSec) throws Exception {
        //Loop will run as per given time & click the application name
        int varCount = 1;
        for(;varCount<=param_WaitTimeInSec;varCount++){
        if(caller.checkElementPresent("HomePage.ele_ApplicationName","param_AppName_PARAM:" + param_AppName)){
        caller.checkElementPresent("HomePage.ele_ApplicationName","param_AppName_PARAM:" + param_AppName,false,"");
        //Verifying the Type of the created application
        caller.checkObjectProperty("HomePage.lbl_ApplicationType","param_ApplicationName_PARAM:" + param_AppName,"textContent",param_AppType,false,"");
        int varTemp = 1;break;
        } else if(varCount==param_WaitTimeInSec){
        caller.fail("Application is not created Yet");
        } else {
        caller.keyPress("HomePage.btn_Home","F5");
        }
        caller.pause("10000");
        }	
    }
    /**
     *  Business component biscomp_VerifyingBuildDeploystatus.
     */
    public final static void biscomp_VerifyingBuildDeploystatus(final SeleniumTestBase caller, final String param_AppStage, final String param_BuildStatus, final String param_DeploymentStatus, final String param_VersionNumber) throws Exception {
        //Verifying the status of auto build and auto deploy and the stage of application
        Common.biscomp_SelectingStageAndVersion(caller, param_AppStage,param_VersionNumber);
        //Verifying the application stage
        String varAppStage = caller.getStringProperty("ApplicationReposandBuilds.ele_AppStage","TEXT:");
        if(varAppStage.contains(param_AppStage)){
        if(caller.checkElementPresent("ApplicationReposandBuilds.ele_ExpandIcon")){
        caller.clickAt("ApplicationReposandBuilds.ele_ExpandIcon","1,1");
        }
        //Wait for Build
        caller.pause("15000");
        caller.keyPress("HomePage.btn_Home","F5");
        //Waiting for page loading
        caller.pause("5000");
        //Verifying the application Build Status
        if(caller.checkElementPresent("ApplicationReposandBuilds.ele_ExpandIcon")){
        caller.clickAt("ApplicationReposandBuilds.ele_ExpandIcon","1,1");
        }
        caller.checkObjectProperty("ApplicationReposandBuilds.lbl_BuildStatus","textContent",param_BuildStatus,false,"");
        //Wait for Deploy
        caller.pause("10000");
        caller.keyPress("HomePage.btn_Home","F5");
        //Waiting for page loading
        caller.pause("3000");
        //Verifying the application Deploy Status
        String varDeployStatus = "Build 1 "+param_DeploymentStatus;
        if(caller.checkElementPresent("ApplicationReposandBuilds.ele_ExpandIcon")){
        caller.clickAt("ApplicationReposandBuilds.ele_ExpandIcon","1,1");
        }
        caller.checkObjectProperty("ApplicationReposandBuilds.lbl_DeployStatus","textContent",varDeployStatus,false,"");
        } else {
        caller.fail("Correct application stage is not selected");
        }	
    }
    /**
     *  Business component biscomp_LaunchingApp.
     */
    public final static void biscomp_LaunchingApp(final SeleniumTestBase caller, final String param_AppStage, final String param_VersionNumber, final String param_SamleText, final String param_AppType, final String param_DomainName, final String param_AppKey) throws Exception {
        //Launching the application and verifyig the pop-up window
        caller.clickAt("ApplicationHome.lnk_RepoandBuild","10,10");
        //Wait for application loading
        caller.pause("5000");
        caller.clickAt("ApplicationReposandBuilds.ele_ddStageList","10,10");
        caller.clickAt("ApplicationReposandBuilds.lbl_AppStage","param_Stage_PARAM:" + param_AppStage,"10,10");
        //Waiting for page loading
        caller.pause("5000");
        caller.clickAt("ApplicationReposandBuilds.ele_ddVersionList","10,10");
        caller.clickAt("ApplicationReposandBuilds.lbl_VersionNumber","param_VersionNumber_PARAM:" + param_VersionNumber,"10,10");
        caller.clickAt("ApplicationReposandBuilds.btn_Launch","10,10");
        //Wait for window loading
        caller.pause("5000");
        Common.biscomp_VerifyingSampleAppIndexPage(caller, param_SamleText,param_AppType,param_AppStage,param_DomainName,param_AppKey,param_VersionNumber);	
    }
    /**
     *  Business component biscomp_SelectApp.
     */
    public final static void biscomp_SelectApp(final SeleniumTestBase caller, final String param_AppName) throws Exception {
        //Selecting the application
        caller.click("HomePage.ele_ApplicationName","param_AppName_PARAM:" + param_AppName);	
    }
    /**
     *  Business component biscomp_AddMembers.
     */
    public final static void biscomp_AddMembers(final SeleniumTestBase caller, final String param_UsernameList, final String param_InvitationMsg, final String param_LoggedUser) throws Exception {
        //Accessing the Team tab
        String varUserList = "";
        caller.clickAt("ApplicationHome.lnk_Team","10,10");
        caller.click("ApplicationTeam.btn_AddMembers");
        String usernameList = param_UsernameList;
        //Assign members text string in to an array
        int varMemberCount = 0;String[] members=usernameList.split(",");varMemberCount=members.length;
        int varCount = 0;
        //Untill member array items ends select roles
        for(;varCount<varMemberCount;varCount++){
        String varUser = members[varCount];
        if(param_LoggedUser.contains("appowner")){
        if(!varUser.contains("appowner")){
        caller.type("ApplicationTeam.tf_Usernames",varUser);
        caller.click("ApplicationTeam.btn_AddToList");
        caller.checkElementPresent("ApplicationTeam.ele_TeamAdminMemberName","param_AdminMemberName_PARAM:" + varUser,false,"");
        int varTemp = 1;varUserList=varUserList.concat(varUser).concat(", ");
        }
        } else {
        caller.type("ApplicationTeam.tf_Usernames",varUser);
        caller.click("ApplicationTeam.btn_AddToList");
        caller.checkElementPresent("ApplicationTeam.ele_TeamAdminMemberName","param_AdminMemberName_PARAM:" + varUser,false,"");
        int varTemp = 1;varUserList=varUserList.concat(varUser).concat(", ");
        }
        }
        int varTemp1 = 1;varUserList = varUserList.substring(0, varUserList.length()-2);
        caller.store("keyUserList","String",varUserList);
        //Inviting Members
        caller.click("ApplicationTeam.btn_Invite");
        caller.click("ApplicationHome.lnk_Team");
        //Verify added user in the team page
        int varCount2 = 0;
        for(;varCount2<varMemberCount;varCount2++){
        String varUser = members[varCount2];
        caller.checkElementPresent("ApplicationTeam.ele_AddedMember","param_MemberName_PARAM:" + varUser,false,"");
        caller.pause("1000");
        }
        caller.keyPress("HomePage.btn_Home","F5");
        caller.pause("1000");
        caller.checkElementPresent("Common.lbl_NotificationWallMsg","param_Message_PARAM:" + varUserList+" "+param_InvitationMsg,false,"");	
    }
    /**
     *  Business component biscomp_CreateBranch.
     */
    public final static void biscomp_CreateBranch(final SeleniumTestBase caller, final String param_AppStage, final String param_VersionNumber, final String param_NewVersionNumber) throws Exception {
        //Accessing the 'Repo & Build' page
        caller.clickAt("ApplicationHome.lnk_RepoandBuild","10,10");
        //Wait for application loading
        caller.pause("3000");
        Common.biscomp_SelectingStageAndVersion(caller, param_AppStage,param_VersionNumber);
        if(caller.checkElementPresent("ApplicationReposandBuilds.ele_ExpandIcon")){
        caller.click("ApplicationReposandBuilds.ele_ExpandIcon");
        }
        caller.click("ApplicationReposandBuilds.btn_CreateBranchMain");
        caller.type("ApplicationReposandBuilds.tf_NewVersionNumber",param_NewVersionNumber);
        caller.click("ApplicationReposandBuilds.btn_CreateBranchSub");
        //Wait for Branch creation
        caller.pause("10000");
        caller.clickAt("ApplicationReposandBuilds.ele_ddVersionList","10,10");
        caller.checkElementPresent("ApplicationReposandBuilds.lbl_VersionNumber","param_VersionNumber_PARAM:" + param_NewVersionNumber,false,"null");
        caller.clickAt("ApplicationReposandBuilds.lbl_VersionNumber","param_VersionNumber_PARAM:" + param_VersionNumber,"10,10");	
    }
    /**
     *  Business component biscomp_SelectingStageAndVersion.
     */
    public final static void biscomp_SelectingStageAndVersion(final SeleniumTestBase caller, final String param_AppStage, final String param_VersionNumber) throws Exception {
        caller.clickAt("ApplicationReposandBuilds.ele_ddStageList","10,10");
        caller.clickAt("ApplicationReposandBuilds.lbl_AppStage","param_Stage_PARAM:" + param_AppStage,"10,10");
        //Waiting for version loading
        caller.pause("5000");
        caller.clickAt("ApplicationReposandBuilds.ele_ddVersionList","10,10");
        caller.clickAt("ApplicationReposandBuilds.lbl_VersionNumber","param_VersionNumber_PARAM:" + param_VersionNumber,"10,10");	
    }
    /**
     *  Business component biscomp_ManualBuildAndDeploy.
     */
    public final static void biscomp_ManualBuildAndDeploy(final SeleniumTestBase caller, final String param_Pause, final String param_BuildNumber, final String param_BuildStatus, final String param_DeployStatus, final String param_DeployNumber) throws Exception {
        //Manually building and deploying
        caller.clickAt("ApplicationReposandBuilds.btn_Build","1,1");
        //Waiting for build number to be updated
        caller.pause(param_Pause);
        caller.keyPress("HomePage.btn_Home","F5");
        //Waiting for page loading
        caller.pause("3000");
        caller.checkObjectProperty("ApplicationReposandBuilds.lbl_BuildStatus","textContent",param_BuildStatus,false,"");
        caller.checkObjectProperty("ApplicationReposandBuilds.lbl_BuildNumber","textContent",param_BuildNumber,false,"");
        caller.clickAt("ApplicationReposandBuilds.btn_Deploy","1,1");
        //Waiting for deploy number to be updated
        caller.pause(param_Pause);
        caller.keyPress("HomePage.btn_Home","F5");
        //Waiting for page loading
        caller.pause("3000");
        String varDeployStatus = "Build "+param_DeployNumber+" Deployed";
        caller.checkObjectProperty("ApplicationReposandBuilds.lbl_DeployStatus","textContent",varDeployStatus,false,"");	
    }
    /**
     *  Business component biscomp_BuildButtonFunction.
     */
    public final static void biscomp_BuildButtonFunction(final SeleniumTestBase caller, final String param_AppStage, final String param_VersionNumber, final String param_Buildnumber, final String param_BuildStatus, final String param_DeployedNumber) throws Exception {
        //Verifying the status of auto build and auto deploy and the stage of application
        Common.biscomp_SelectingStageAndVersion(caller, param_AppStage,param_VersionNumber);
        //Verifying the application stage
        String varAppStage = caller.getStringProperty("ApplicationReposandBuilds.ele_AppStage","TEXT:");
        if(varAppStage.contains(param_AppStage)){
        if(caller.checkElementPresent("ApplicationReposandBuilds.ele_ExpandIcon")){
        caller.click("ApplicationReposandBuilds.ele_ExpandIcon");
        }
        caller.clickAt("ApplicationReposandBuilds.btn_Build","10,10");
        //verify the spinning button
        caller.checkElementPresent("ApplicationReposandBuilds.lbl_BuildSpinningIcon",false,"");
        //Wait for Build ID get update
        caller.pause("15000");
        caller.keyPress("HomePage.btn_Home","F5");
        if(caller.checkElementPresent("ApplicationReposandBuilds.ele_ExpandIcon")){
        caller.click("ApplicationReposandBuilds.ele_ExpandIcon");
        }
        caller.checkObjectProperty("ApplicationReposandBuilds.lbl_MasterRepoBuildNumber","textContent",param_Buildnumber,false,"");
        caller.checkObjectProperty("ApplicationReposandBuilds.lbl_MasterRepoBuildStatus","textContent",param_BuildStatus,false,"");
        //Wait for Build ID get update
        caller.pause("10000");
        caller.keyPress("HomePage.btn_Home","F5");
        //Wait for page loading
        caller.pause("3000");
        if(caller.checkElementPresent("ApplicationReposandBuilds.ele_ExpandIcon")){
        caller.click("ApplicationReposandBuilds.ele_ExpandIcon");
        }
        caller.checkElementPresent("ApplicationReposandBuilds.lbl_DeployedNumber","param_DeplyedNumber_PARAM:" + param_DeployedNumber,false,"");
        }	
    }
    /**
     *  Business component biscomp_CreateDatabase.
     */
    public final static void biscomp_CreateDatabase(final SeleniumTestBase caller, final String param_DatabaseName, final String param_DatabaseEnvironment, final String param_DefaultPassword, final String param_AllEnv) throws Exception {
        //click Database link
        caller.click("ApplicationHome.lnk_Databases");
        //Wait for page loading
        caller.pause("5000");
        caller.click("CreateNewDatabase.btn_AddNewDatabase");
        String varDBName = "DB"+caller.generateData("Alphanumeric",3);
        caller.store("keyDBName","String",varDBName);
        caller.type("CreateNewDatabase.tf_DBName",varDBName);
        if(param_AllEnv.equals("NO")){
        caller.clickAt("CreateNewDatabase.ele_ddDBEnvironment","10,10");
        caller.clickAt("CreateNewDatabase.ele_ListItemDBEnvironment","param_DatabaseEnvironment_PARAM:" + param_DatabaseEnvironment,"10,10");
        } else if(param_AllEnv.equals("YES")){
        caller.clickAt("ApplicationResources.chk_AllEnv","10,10");
        }
        caller.type("CreateNewDatabase.tf_DefaultPassword",param_DefaultPassword);
        caller.type("CreateNewDatabase.tf_ConfirmPassword",param_DefaultPassword);
        caller.clickAt("CreateNewDatabase.btn_CreateDatabase","10,10");
        //Wait for page load
        caller.pause("5000");	
    }
    /**
     *  Business component biscomp_CreateDBWithDBUserAndDBTemplate.
     */
    public final static void biscomp_CreateDBWithDBUserAndDBTemplate(final SeleniumTestBase caller, final String param_DatabaseName, final String param_DbUsername, final String param_DBUserPasswordValid, final String param_DBUserPasswordInvalid, final String param_AppStage, final String param_DBUserPwdErrorMsg) throws Exception {
        //click Database link
        caller.click("ApplicationHome.lnk_Databases");
        //Wait for page loading
        caller.pause("5000");
        caller.click("CreateNewDatabase.btn_AddNewDatabase");
        String varDBName = "DB"+caller.generateData("Alphanumeric",3);
        caller.store("keyDBName","String",varDBName);
        caller.type("CreateNewDatabase.tf_DBName",varDBName);
        caller.click("CreateNewDatabase.chk_AdvancedOptions");
        caller.pause("2000");
        caller.clickAt("CreateNewDatabase.ele_ddUser","10,10");
        //Wait for list loading
        caller.pause("2000");
        caller.clickAt("CreateNewDatabase.lbl_CreateNewDBUser","10,10");
        //Wait for page loading
        caller.pause("2000");
        String varDBUsername = "DBU"+caller.generateData("Alphanumeric",2);
        caller.store("keyDBUsername","String",varDBUsername);
        caller.type("CreateNewDatabase.tf_NewUserUsername",varDBUsername);
        //Entering invalid password and verifying the error message
        caller.type("CreateNewDatabase.tf_NewUserPassword",param_DBUserPasswordInvalid);
        caller.type("CreateNewDatabase.tf_NewUserRepeatPassword",param_DBUserPasswordInvalid);
        caller.clickAt("CreateNewDatabase.btn_CreateDBUser","10,10");
        //Verifying the Error Message
        caller.checkElementPresent("CreateNewDatabase.lbl_ErrorUserPassword","param_DBUserPwdErrorMsg_PARAM:" + param_DBUserPwdErrorMsg,false,"null");
        //Entering valid password
        caller.type("CreateNewDatabase.tf_NewUserPassword",param_DBUserPasswordValid);
        caller.type("CreateNewDatabase.tf_NewUserRepeatPassword",param_DBUserPasswordValid);
        caller.clickAt("CreateNewDatabase.btn_CreateDBUser","10,10");
        //Wait for db user creation
        caller.pause("3000");
        caller.checkElementPresent("CreateNewDatabase.ele_SelectedDBUser","param_SelectedDBUser_PARAM:" + varDBUsername,false,"null");
        caller.clickAt("CreateNewDatabase.ele_ddPermissionTemplate","10,10");
        caller.clickAt("CreateNewDatabase.lbl_CreateNewDBTemplate","10,10");
        String varDBTemplateName = "DBT_"+caller.generateData("Alphanumeric",2);
        caller.store("keyDBTemplateName","String",varDBTemplateName);
        caller.type("CreateNewDatabase.tf_NewpermissionTemplateName",varDBTemplateName);
        caller.click("CreateNewDBTemplate.chk_AlterPriv");
        caller.click("CreateNewDBTemplate.chk_AlterRoutinePriv");
        caller.clickAt("CreateNewDatabase.btn_CreateTemplate","10,10");
        //Wait for DB Template creation
        caller.pause("3000");
        caller.checkElementPresent("CreateNewDatabase.ele_SelectedDBTemplate","param_SelectedDBTemplate_PARAM:" + varDBTemplateName+"@"+param_AppStage,false,"null");
        caller.clickAt("CreateNewDatabase.btn_CreateDatabase","10,10");	
    }
    /**
     *  Business component biscomp_VerifyingDatabases.
     */
    public final static void biscomp_VerifyingDatabases(final SeleniumTestBase caller, final String param_DatabaseName, final String param_Username) throws Exception {
        //Verifying the created database is displayed in the database tab page
        caller.checkElementPresent("ApplicationResources.lnk_Database","param_DatabaseName_PARAM:" + param_DatabaseName+"_"+param_Username+"_com",false,"");	
    }
    /**
     *  Business component biscomp_CreateDatasource.
     */
    public final static void biscomp_CreateDatasource(final SeleniumTestBase caller, final String param_DatasourceName, final String param_DatasourceDescription, final String param_DBDriver, final String param_DSEnv, final String param_DBURL, final String param_DBUsername, final String param_DatasourcePassword) throws Exception {
        if(caller.checkElementPresent("ApplicationResources.btn_AddNewDatasource")){
        caller.clickAt("ApplicationResources.btn_AddNewDatasource","10,10");
        } else {
        caller.clickAt("ApplicationResources.btn_AddDatasource","10,10");
        }
        String varDSName = "DS"+caller.generateData("Alphanumeric",3);
        caller.store("keyDSName","String",varDSName);
        caller.type("CreateDatasource.tf_DatasourceName",varDSName);
        if(param_DatasourceDescription!=null){
        caller.type("CreateDatasource.ara_Description",param_DatasourceDescription);
        }
        if(param_DBDriver!=null){
        caller.clickAt("CreateDatasource.ele_ddDBDriver","10,10");
        caller.clickAt("CreateDatasource.lbl_DatabaseDriver","param_DatabaseDriver_PARAM:" + param_DBDriver,"10,10");
        }
        if(param_DSEnv!=null){
        caller.clickAt("CreateDatasource.ele_ddDSEnvironment","10,10");
        caller.clickAt("CreateDatasource.lbl_DSEnvironment","param_DSEnv_PARAM:" + param_DSEnv,"10,10");
        }
        if(param_DBUsername!=null){
        caller.clickAt("CreateDatasource.ele_ddDBUsername","10,10");
        caller.clickAt("CreateDatasource.lbl_DBUsername","param_DBUsername_PARAM:" + param_DBUsername,"10,10");
        }
        //Waitign for username to be loaded
        caller.pause("3000");
        caller.type("CreateDatasource.tf_DatasourcePassword",param_DatasourcePassword);
        caller.clickAt("CreateDatasource.btn_CreateDatasource","10,10");
        //Waiting for datasource creation
        caller.pause("5000");	
    }
    /**
     *  Business component biscomp_Promote.
     */
    public final static void biscomp_Promote(final SeleniumTestBase caller, final String param_Stage, final String param_versionNumber) throws Exception {
        //Promoting a version to next stage
        caller.clickAt("ApplicationLifeCycleManagement.ele_ddStage","1,1");
        caller.clickAt("ApplicationLifeCycleManagement.lbl_Stage","param_Stage_PARAM:" + param_Stage,"1,1");
        //Wait for page loading
        caller.pause("5000");
        if(param_Stage.contains("Development")){
        caller.click("ApplicationLifeCycleManagement.chk_CodeCompleted","param_Versionnumber_PARAM:" + param_versionNumber);
        caller.click("ApplicationLifeCycleManagement.chk_DesignReviewDone","param_Versionnumber_PARAM:" + param_versionNumber);
        caller.click("ApplicationLifeCycleManagement.chk_CodeReviewDone","param_Versionnumber_PARAM:" + param_versionNumber);
        } else if(param_Stage.contains("Testing")){
        caller.click("ApplicationLifeCycleManagement.chk_SmokeTestPassed","param_Versionnumber_PARAM:" + param_versionNumber);
        caller.click("ApplicationLifeCycleManagement.chk_TestCasesPassed","param_Versionnumber_PARAM:" + param_versionNumber);
        }
        caller.clickAt("ApplicationLifeCycleManagement.btn_Promote","param_VersionNumber_PARAM:" + param_versionNumber,"1,1");
        //Wait for promoting
        caller.pause("8000");	
    }
    /**
     *  Business component biscomp_Demote.
     */
    public final static void biscomp_Demote(final SeleniumTestBase caller, final String param_Stage, final String param_VersionToDemote, final String param_IssueSummary, final String param_DemoteComment) throws Exception {
        caller.clickAt("ApplicationLifeCycleManagement.ele_ddStage","1,1");
        caller.clickAt("ApplicationLifeCycleManagement.lbl_Stage","param_Stage_PARAM:" + param_Stage,"1,1");
        if("Production".equals(param_Stage)){
        caller.click("ApplicationLifeCycleManagement.btn_DemotePro","param_VersionNumber_PARAM:" + param_VersionToDemote);
        } else if("Testing".equals(param_Stage)){
        caller.click("ApplicationLifeCycleManagement.btn_DemoteTest","param_VersionNumber_PARAM:" + param_VersionToDemote);
        //wait for page loading
        caller.pause("3000");
        }
        if(caller.checkElementPresent("ApplicationLifeCycleManagement.chk_Issue","param_IssueSummary_PARAM:" + param_IssueSummary)){
        if(param_IssueSummary!=null){
        caller.clickAt("ApplicationLifeCycleManagement.chk_Issue","param_IssueSummary_PARAM:" + param_IssueSummary,"10,10");
        }
        }
        String varDemoteComment = param_DemoteComment+caller.generateData("Alphanumeric",3);
        caller.store("keyDemoteComment","String",varDemoteComment);
        caller.type("ApplicationLifeCycleManagement.tf_Comment",varDemoteComment);
        caller.click("ApplicationLifeCycleManagement.btn_DemoteSub");
        //Wait for demoting
        caller.pause("5000");	
    }
    /**
     *  Business component biscomp_VerifyingVersionListedUnderStage.
     */
    public final static void biscomp_VerifyingVersionListedUnderStage(final SeleniumTestBase caller, final String param_Stage, final String param_VersionNumber, final String param_VersionVisibility) throws Exception {
        //Verifying paticular version is listed under  a certain stage
        caller.clickAt("ApplicationLifeCycleManagement.ele_ddStage","1,1");
        //Waiting for list loading
        caller.pause("3000");
        caller.clickAt("ApplicationLifeCycleManagement.lbl_Stage","param_Stage_PARAM:" + param_Stage,"1,1");
        //Waiting for list loading
        caller.pause("3000");
        caller.checkObjectProperty("ApplicationLifeCycleManagement.lbl_VerisonNumber","param_VersionNumber_PARAM:" + param_VersionNumber,"ELEMENTPRESENT",param_VersionVisibility,false,"");	
    }
    /**
     *  Business component biscomp_VerifyingVersionNotListedUnderStage.
     */
    public final static void biscomp_VerifyingVersionNotListedUnderStage(final SeleniumTestBase caller, final String param_Stage) throws Exception {
        //Verifying paticular version is listed under  a certain stage
        caller.clickAt("ApplicationLifeCycleManagement.ele_ddStage","1,1");
        //Waiting for list loading
        caller.pause("3000");
        caller.clickAt("ApplicationLifeCycleManagement.lbl_Stage","param_Stage_PARAM:" + param_Stage,"1,1");
        //Waiting for list loading
        caller.pause("3000");
        caller.checkElementPresent("ApplicationLifeCycleManagement.lbl_NoVersionListed",false,"");	
    }
    /**
     *  Business component biscomp_EditIssue.
     */
    public final static void biscomp_EditIssue(final SeleniumTestBase caller, final String param_IssueSummary, final String param_IssueDescription, final String param_Version, final String param_IssueType, final String param_IssuePriority, final String param_IssueStatus, final String param_IssueAssignee, final String param_IssueSeverity) throws Exception {
        //Editing the Issue
        caller.clickAt("ApplicationHome.lnk_Issues","1,1");
        caller.selectFrame("frames.frm_iframe");
        caller.click("ApplicationIssues.btn_Edit","param_IssueSummary_PARAM:" + param_IssueSummary);
        if(param_IssueDescription!=null){
        caller.type("ApplicationIssues.tf_IssueDescription",param_IssueDescription);
        }
        if(param_IssueType!=null){
        caller.clickAt("ApplicationIssues.ele_ddIssueType","1,1");
        caller.clickAt("ApplicationIssues.lbl_IssueType","param_IssueType_PARAM:" + param_IssueType,"1,1");
        }
        if(param_IssuePriority!=null){
        caller.clickAt("ApplicationIssues.ele_ddIssuePriority","1,1");
        caller.clickAt("ApplicationIssues.lbl_IssuePriority","param_IssuePriority_PARAM:" + param_IssuePriority,"1,1");
        }
        if(param_IssueStatus!=null){
        caller.clickAt("ApplicationIssues.ele_ddIssueStatus","1,1");
        caller.clickAt("ApplicationIssues.lbl_IssueStatus","param_IssueStatus_PARAM:" + param_IssueStatus,"1,1");
        }
        if(param_IssueAssignee!=null){
        caller.clickAt("ApplicationIssues.ele_ddIssueAssignee","1,1");
        caller.clickAt("ApplicationIssues.lbl_IssueAssignee","param_IssueAssignee_PARAM:" + param_IssueAssignee,"1,1");
        }
        if(param_Version!=null){
        caller.clickAt("ApplicationIssues.ele_ddVersion","1,1");
        caller.clickAt("ApplicationIssues.lbl_Version","param_Version_PARAM:" + param_Version,"1,1");
        }
        if(param_IssueSeverity!=null){
        caller.clickAt("ApplicationIssues.ele_ddSeverity","1,1");
        caller.clickAt("ApplicationIssues.lbl_Severity","param_IssueSeverity_PARAM:" + param_IssueSeverity,"1,1");
        }
        caller.clickAt("ApplicationIssues.btn_UpdateIssue","1,1");	
    }
    /**
     *  Business component biscomp_CreateIssue.
     */
    public final static void biscomp_CreateIssue(final SeleniumTestBase caller, final String param_IssueSummary, final String param_IssueDescription, final String param_Version, final String param_IssueType, final String param_IssuePriority, final String param_IssueStatus, final String param_IssueAssignee, final String param_IssueSeverity) throws Exception {
        //Creating a issue
        caller.clickAt("ApplicationHome.lnk_Issues","10,10");
        //Wait for page loading
        caller.pause("2500");
        caller.selectFrame("frames.frm_iframe");
        caller.clickAt("ApplicationIssues.btn_NewIssue","10,10");
        String varIssueSummary = "Issue_"+param_IssueSummary+caller.generateData("Alphanumeric",3);
        caller.store("keyIssueSummary","String",varIssueSummary);
        caller.type("ApplicationIssues.tf_Summary",varIssueSummary);
        if(param_IssueDescription!=null){
        caller.type("ApplicationIssues.tf_IssueDescription",param_IssueDescription);
        }
        caller.clickAt("ApplicationIssues.ele_ddIssueType","1,1");
        caller.clickAt("ApplicationIssues.lbl_IssueType","param_IssueType_PARAM:" + param_IssueType,"1,1");
        caller.clickAt("ApplicationIssues.ele_ddIssuePriority","1,1");
        caller.clickAt("ApplicationIssues.lbl_IssuePriority","param_IssuePriority_PARAM:" + param_IssuePriority,"1,1");
        caller.clickAt("ApplicationIssues.ele_ddIssueStatus","1,1");
        caller.clickAt("ApplicationIssues.lbl_IssueStatus","param_IssueStatus_PARAM:" + param_IssueStatus,"1,1");
        if(param_IssueAssignee!=null){
        caller.clickAt("ApplicationIssues.ele_ddIssueAssignee","1,1");
        caller.clickAt("ApplicationIssues.lbl_IssueAssignee","param_IssueAssignee_PARAM:" + param_IssueAssignee,"1,1");
        }
        caller.clickAt("ApplicationIssues.ele_ddVersion","1,1");
        caller.clickAt("ApplicationIssues.lbl_Version","param_Version_PARAM:" + param_Version,"1,1");
        caller.clickAt("ApplicationIssues.ele_ddSeverity","1,1");
        caller.clickAt("ApplicationIssues.lbl_Severity","param_IssueSeverity_PARAM:" + param_IssueSeverity,"1,1");
        caller.clickAt("ApplicationIssues.btn_AddIssue","1,1");	
    }
    /**
     *  Business component biscomp_CraeteCustomURL.
     */
    public final static void biscomp_CraeteCustomURL(final SeleniumTestBase caller, final String param_SubDomain) throws Exception {
        //Creating custom URL by adding sub domain
        caller.clickAt("ApplicationHome.lnk_Overview","1,1");
        caller.clickAt("ApplicationOverview.ele_SubDomain","1,1");
        caller.type("ApplicationOverview.tf_SubDomain",param_SubDomain);
        caller.clickAt("ApplicationOverview.btn_Save","1,1");	
    }
    /**
     *  Business component biscomp_VerifyingSampleAppIndexPage.
     */
    public final static void biscomp_VerifyingSampleAppIndexPage(final SeleniumTestBase caller, final String param_SampleText, final String param_AppType, final String param_VersionStage, final String param_DomainName, final String param_AppKey, final String param_VersionNumber) throws Exception {
        //Accessing the new window
        caller.pause("5000");
        caller.selectWindow("Window.win_NewWindow","param_NewWindowNumber_PARAM:1");
        if("Java Web Application".equals(param_AppType)){
        if("Production".equals(param_VersionStage)){
        caller.navigateToURL("https://appserver.appfactory.private.wso2.com:9443/t/<param_DomainName>.com/webapps/<param_AppKey>-<param_VersionNumber>/","param_DomainName_PARAM:" + param_DomainName + "_PARAM," + "param_AppKey_PARAM:" + param_AppKey + "_PARAM," + "param_VersionNumber_PARAM:" + param_VersionNumber,"5000");
        }
        //Verifying the sample text
        caller.checkElementPresent("sampleIndex.lbl_SampleText","param_SampleText_PARAM:" + param_SampleText,false,"");
        //Closing the new window
        caller.fireEvent("KEY%key=alt+F4","2000");
        } else if("JAX-RS Service".equals(param_AppType)){
        String pageURL = caller.getDriver().getCurrentUrl();
        if(pageURL.contains("wadl")){
        caller.writeToReport("Correct URL is loaded");
        } else {
        caller.fail("Correct URL is not loaded");
        }
        //Closing the new window
        caller.fireEvent("KEY%key=alt+F4","2000");
        } else if("JAX-WS Service".equals(param_AppType)){
        String pageURL = caller.getDriver().getCurrentUrl();
        if(pageURL.contains("wsdl")){
        caller.writeToReport("Correct URL is loaded");
        } else {
        caller.fail("Correct URL is not loaded");
        }
        //Closing the new window
        caller.fireEvent("KEY%key=alt+F4","2000");
        } else if("Jaggery Application".equals(param_AppType)){
        caller.checkElementPresent("sampleIndex.lbl_SampleText","param_SampleText_PARAM:" + param_SampleText,false,"");
        } else if("WAR".equals(param_AppType)){
        caller.checkElementPresent("sampleIndex.lbl_SampleText","param_SampleText_PARAM:" + param_SampleText,false,"");
        //Closing the new window
        caller.fireEvent("KEY%key=alt+F4","2000");
        caller.pause("2000");
        } else if("WSO2 Data Service".equals(param_AppType)){
        //Closing the new window
        caller.fireEvent("KEY%key=alt+F4","2000");
        }	
    }
    /**
     *  Business component biscomp_ResolvingIssue.
     */
    public final static void biscomp_ResolvingIssue(final SeleniumTestBase caller, final String param_IssueSummary, final String param_IssueStatus) throws Exception {
        //Resolving a raised issue
        caller.clickAt("ApplicationHome.lnk_Issues","1,1");
        caller.selectFrame("frames.frm_iframe");
        caller.click("ApplicationIssues.btn_Edit","param_IssueSummary_PARAM:" + param_IssueSummary);
        caller.clickAt("ApplicationIssues.ele_ddIssueStatus","1,1");
        caller.clickAt("ApplicationIssues.lbl_IssueStatus","param_IssueStatus_PARAM:" + param_IssueStatus,"1,1");
        caller.clickAt("ApplicationIssues.btn_UpdateIssue","1,1");
        //Wait for page loading
        caller.pause("5000");	
    }
    /**
     *  Business component biscomp_CreatingInternalAPI.
     */
    public final static void biscomp_CreatingInternalAPI(final SeleniumTestBase caller, final String param_APIName, final String param_APIVersion, final String param_AppKey) throws Exception {
        //Creating a external API
        caller.clickAt("ApplicationHome.lnk_ResourceTab","1,1");
        caller.clickAt("ApplicationResources.btn_GoToApiManager","1,1");
        caller.selectWindow("Window.win_NewWindow","param_NewWindowNumber_PARAM:1");
        //Wait for page loading
        caller.pause("5000");
        caller.clickAt("APIManager.lnk_APIs","10,10");
        //Waiting for page loading
        caller.pause("3000");
        caller.click("APIManager.lnk_APIName","param_APIName_PARAM:" + param_APIName + "_PARAM," + "param_APIVersion_PARAM:" + param_APIVersion);
        //Waiting for page loading
        caller.pause("3000");
        caller.keyPress("APIManager.lst_Applications","F5");
        caller.pause("2000");
        caller.select("APIManager.lst_Applications",param_AppKey);
        /*
        ClickAt
         object=APIManager.lst_Applications
         coordinates=1,1
        ClickAt
         object=APIManager.ele_AppKey
         param_AppKey=@param_AppKey
         coordinates=1,1
        */
        caller.checkObjectProperty("APIManager.lst_Applications","SELECTEDOPTION",param_AppKey,false,"");
        caller.click("APIManager.btn_Subscribe");
        //Waiting for the pop-up window
        caller.pause("5000");
        caller.checkElementPresent("APIManager.lbl_PopUPMsg",false,"");
        caller.click("APIManager.btn_GoToMySubscriptions");
        //Wait for page loadin
        caller.pause("5000");
        caller.clickAt("APIManager.ele_ddApplicationList","1,1");
        caller.clickAt("APIManager.lbl_AppKey","param_AppKey_PARAM:" + param_AppKey,"1,1");
        //Waiting for page loading
        caller.pause("3000");
        if(caller.checkElementPresent("APIManager.btn_GenerateProduction")){
        caller.click("APIManager.btn_GenerateProduction");
        //Waiting for production ke to be generated
        caller.pause("5000");
        }
        if(caller.checkElementPresent("APIManager.btn_GenerateSandbox")){
        caller.click("APIManager.btn_GenerateSandbox");
        //Waiting for production key to be generated
        caller.pause("5000");
        }
        String varProductionKey = caller.getStringProperty("APIManager.lbl_ProductionKey","TEXT:");
        caller.store("keyProductionKey","String",varProductionKey);
        String varSandboxKey = caller.getStringProperty("APIManager.lbl_KeySandbox","TEXT:");
        caller.store("keySandboxKey","String",varSandboxKey);
        //Closing the API Manger window
        caller.fireEvent("KEY%key=alt+F4","2000");	
    }
    /**
     *  Business component biscomp_CreatingExternalAPI.
     */
    public final static void biscomp_CreatingExternalAPI(final SeleniumTestBase caller, final String param_ExternalAPIName, final String param_AppStage, final String param_AuthAPI, final String param_APIURL) throws Exception {
        //Creating External API
        caller.clickAt("ApplicationResources.lnk_APITab","1,1");
        caller.pause("2000");
        caller.clickAt("ApplicationResources.btn_AddExternalAPI","10,10");
        caller.pause("2000");
        String varExternalAPIName = "ExAPI_"+param_ExternalAPIName+caller.generateData("Alphanumeric",2);
        caller.store("keyExternalAPIName","String",varExternalAPIName);
        caller.type("ApplicationResources.tf_APIName",varExternalAPIName);
        caller.clickAt("ApplicationResources.ele_ddStage","1,1");
        caller.clickAt("ApplicationResources.lbl_Appstage","param_AppStage_PARAM:" + param_AppStage,"1,1");
        caller.clickAt("ApplicationResources.ele_ddAuthAPI","1,1");
        caller.clickAt("ApplicationResources.lbl_AuthAPI","param_AuthAPI_PARAM:" + param_AuthAPI,"1,1");
        caller.type("ApplicationResources.tf_APIURL",param_APIURL);
        caller.clickAt("ApplicationResources.btn_CreateAPI","1,1");	
    }
    /**
     *  Business component biscomp_CreatingProperty.
     */
    public final static void biscomp_CreatingProperty(final SeleniumTestBase caller, final String param_PropertyName, final String param_AppStage, final String param_PropertyDescription, final String param_PropertyValue, final String param_AllEnv) throws Exception {
        //Creating a property
        String propertyName = "prop_"+param_PropertyName+caller.generateData("Alphanumeric",2);
        caller.store("KeyPropertyName","String",propertyName);
        if(caller.checkElementPresent("ApplicationResources.btn_AddProperty")){
        caller.clickAt("ApplicationResources.btn_AddProperty","1,1");
        } else if(caller.checkElementPresent("ApplicationResources.btn_AddProperties")){
        caller.clickAt("ApplicationResources.btn_AddProperties","1,1");
        }
        caller.type("ApplicationResources.tf_PropertyName",propertyName);
        if(param_AllEnv.equals("NO")){
        caller.clickAt("ApplicationResources.ele_ddRegEnv","1,1");
        caller.clickAt("ApplicationResources.lbl_AppStage","param_AppStage_PARAM:" + param_AppStage,"1,1");
        } else if(param_AllEnv.equals("YES")){
        caller.clickAt("ApplicationResources.chk_AllEnv","1,1");
        }
        if(param_PropertyDescription!=null){
        caller.type("ApplicationResources.tf_PropertyDescription",param_PropertyDescription);
        }
        caller.type("ApplicationResources.tf_PropertyValue",param_PropertyValue);
        caller.clickAt("ApplicationResources.btn_CreateProperty","1,1");	
    }
    /**
     *  Business component biscomp_VerifyingResourcesAfterPromoting.
     */
    public final static void biscomp_VerifyingResourcesAfterPromoting(final SeleniumTestBase caller, final String param_DSName, final String param_PropertyName, final String param_StagePromotedTo) throws Exception {
        //Verifying the resource are mapped when promoted a version
        caller.clickAt("ApplicationHome.lnk_ResourceTab","1,1");
        //Waiting for page loading
        caller.pause("6000");
        caller.checkElementPresent("ApplicationResources.lbl_DSNamePromotedStageOverviewTab","param_Stage_PARAM:" + param_StagePromotedTo + "_PARAM," + "param_DSName_PARAM:" + param_DSName,false,"");
        caller.clickAt("ApplicationResources.lnk_Databases","1,1");
        //Waiting for page loading
        caller.pause("6000");
        caller.checkElementPresent("ApplicationResources.lbl_DSNamePromotedStageDBTab","param_Stage_PARAM:" + param_StagePromotedTo + "_PARAM," + "param_DSName_PARAM:" + param_DSName,false,"");
        caller.clickAt("ApplicationResources.lnk_PropertiesTab","1,1");
        //Waiting for page loading
        caller.pause("2500");
        caller.checkElementPresent("ApplicationResources.lbl_PropertyNamePromotedStage","param_Stage_PARAM:" + param_StagePromotedTo + "_PARAM," + "param_ProName_PARAM:" + param_PropertyName,false,"");	
    }
    /**
     *  Business component biscomp_Retire.
     */
    public final static void biscomp_Retire(final SeleniumTestBase caller, final String param_Stage, final String param_VersionNumber, final String param_AppCreationMethod) throws Exception {
        //Retire the selected versions
        caller.clickAt("ApplicationLifeCycleManagement.ele_ddStage","1,1");
        caller.clickAt("ApplicationLifeCycleManagement.lbl_Stage","param_Stage_PARAM:" + param_Stage,"1,1");
        //Wait for page loading
        caller.pause("5000");
        if(param_AppCreationMethod.equals("Upload")){
        caller.clickAt("ApplicationLifeCycleManagement.btn_RetireUploadApp","param_VersionNumber_PARAM:" + param_VersionNumber,"1,1");
        } else if(param_AppCreationMethod.equals("Create")){
        caller.clickAt("ApplicationLifeCycleManagement.btn_RetireCreateApp","param_VersionNumber_PARAM:" + param_VersionNumber,"1,1");
        }
        //Wait for page loading
        caller.pause("2500");
        caller.clickAt("ApplicationLifeCycleManagement.chk_NoOneUsingApp","param_VersionNumber_PARAM:" + param_VersionNumber,"10,10");
        caller.clickAt("ApplicationLifeCycleManagement.btn_RetireSub","param_VersionNumber_PARAM:" + param_VersionNumber,"1,1");
        //Wating for page loading
        caller.pause("5000");	
    }
    /**
     *  Business component biscomp_VerifyingPropertiesInAllEnv.
     */
    public final static void biscomp_VerifyingPropertiesInAllEnv(final SeleniumTestBase caller, final String param_PropertyName, final String param_DevStage, final String param_TestStage, final String param_ProStage) throws Exception {
        //Verifying the properties are created in all environment
        caller.checkElementPresent("ApplicationResources.lnk_PropertyStage","param_PropertyName_PARAM:" + param_PropertyName + "_PARAM," + "param_AppStage_PARAM:" + param_DevStage,false,"");
        caller.checkElementPresent("ApplicationResources.lnk_PropertyStage","param_PropertyName_PARAM:" + param_PropertyName + "_PARAM," + "param_AppStage_PARAM:" + param_TestStage,false,"");
        caller.checkElementPresent("ApplicationResources.lnk_PropertyStage","param_PropertyName_PARAM:" + param_PropertyName + "_PARAM," + "param_AppStage_PARAM:" + param_ProStage,false,"");	
    }
    /**
     *  Business component biscomp_VerifyingDatabaseAllEnv.
     */
    public final static void biscomp_VerifyingDatabaseAllEnv(final SeleniumTestBase caller, final String param_DBName, final String param_DevStage, final String param_TestStage, final String param_ProStage, final String param_TenantAdminName) throws Exception {
        //Verifying the databases are created in all environment
        caller.checkElementPresent("ApplicationResources.lnk_DBStage","param_DBName_PARAM:" + param_DBName + "_PARAM," + "param_TenantAdminName_PARAM:" + param_TenantAdminName + "_PARAM," + "param_AppStage_PARAM:" + param_DevStage,false,"");
        caller.checkElementPresent("ApplicationResources.lnk_DBStage","param_DBName_PARAM:" + param_DBName + "_PARAM," + "param_TenantAdminName_PARAM:" + param_TenantAdminName + "_PARAM," + "param_AppStage_PARAM:" + param_TestStage,false,"");
        caller.checkElementPresent("ApplicationResources.lnk_DBStage","param_DBName_PARAM:" + param_DBName + "_PARAM," + "param_TenantAdminName_PARAM:" + param_TenantAdminName + "_PARAM," + "param_AppStage_PARAM:" + param_ProStage,false,"");	
    }
    /**
     *  Business component biscomp_CreateVersionUploadApp.
     */
    public final static void biscomp_CreateVersionUploadApp(final SeleniumTestBase caller, final String param_AppPath, final String param_UploadedVersion) throws Exception {
        //Create version for the upload type applications
        caller.clickAt("ApplicationHome.lnk_DeployedVersions","1,1");
        //Wait for page loading
        caller.pause("5000");
        caller.type("ApplicationDeployedVersions.btn_Browse",param_AppPath);
        caller.pause("3000");
        caller.clickAt("ApplicationDeployedVersions.btn_Upload","1,1");
        //Wait for page loading
        caller.pause("10000");
        caller.checkElementPresent("ApplicationDeployedVersions.lbl_Version","param_VersionNumber_PARAM:" + param_UploadedVersion,false,"");	
    }
    /**
     *  Business component biscomp_VerifyingRetiredVersion.
     */
    public final static void biscomp_VerifyingRetiredVersion(final SeleniumTestBase caller, final String param_Stage, final String param_VersionNumber) throws Exception {
        //Verifying the retired version is not listed under production stage
        caller.clickAt("ApplicationHome.lnk_LifeCycleManagement","1,1");
        //Wait for page loading
        caller.pause("5000");
        if(caller.checkElementPresent("ApplicationLifeCycleManagement.ele_ddStage")){
        caller.clickAt("ApplicationLifeCycleManagement.ele_ddStage","1,1");
        //Waiting for list loading
        caller.pause("3000");
        caller.clickAt("ApplicationLifeCycleManagement.lbl_Stage","param_Stage_PARAM:" + param_Stage,"1,1");
        //Waiting for list loading
        caller.pause("3000");
        }
        caller.checkObjectProperty("ApplicationLifeCycleManagement.lbl_VerisonNumber","param_VersionNumber_PARAM:" + param_VersionNumber,"ELEMENTPRESENT","FALSE",false,"");	
    }
    /**
     *  Business component biscomp_VerifyingLifecycleHistoryUploadApp.
     */
    public final static void biscomp_VerifyingLifecycleHistoryUploadApp(final SeleniumTestBase caller, final String param_VerisonNumber, final String param_Stage) throws Exception {
        //Verifying the history is visible for the uploaded applications
        caller.clickAt("ApplicationHome.lnk_LifeCycleManagement","1,1");
        //Wait for page loading
        caller.pause("5000");
        caller.clickAt("ApplicationLifeCycleManagement.ele_ddStage","1,1");
        caller.clickAt("ApplicationLifeCycleManagement.lbl_Stage","param_Stage_PARAM:" + param_Stage,"1,1");
        //Wait for page loading
        caller.pause("2500");
        caller.clickAt("ApplicationLifeCycleManagement.btn_LifecycleHistory","param_VersionNumber_PARAM:" + param_VerisonNumber,"10,10");
        caller.checkElementPresent("ApplicationLifeCycleManagement.lbl_UploadTypeHistoryTestToPro",false,"");
        caller.checkElementPresent("ApplicationLifeCycleManagement.lbl_UploadTypeHistoryDevToTest",false,"");	
    }
    /**
     *  Business component biscomp_LoginInvalid.
     */
    public final static void biscomp_LoginInvalid(final SeleniumTestBase caller, final String param_Username, final String param_Password, final String param_ErrorMsg, final String param_MainURL) throws Exception {
        //Login in with invalid user credentials
        caller.open("<param_MainURL>","param_MainURL_PARAM:" + param_MainURL,"8000");
        caller.type("Login.tf_UserName",param_Username);
        caller.type("Login.tf_Password",param_Password);
        caller.click("Login.btn_SignIn");
        caller.pause("2500");
        //Verifying the error message
        caller.checkObjectProperty("Login.lbl_LoginErrorMsg","textContent",param_ErrorMsg,false,"");	
    }
    /**
     *  Business component biscomp_EditApplicationData.
     */
    public final static void biscomp_EditApplicationData(final SeleniumTestBase caller, final String param_AppImagePath, final String param_Description, final String param_AppLogoSubPath, final String param_AppLogoMainPath) throws Exception {
        //Editing application Image
        caller.clickAt("ApplicationHome.lnk_Overview","1,1");
        //Waiting for application loading
        caller.pause("2500");
        /*
        ClickAt
         object=ApplicationOverview.ele_AppIcon
         coordinates=10,10
        */
        caller.click("ApplicationOverview.btn_ImgEditPencil");
        caller.type("ApplicationOverview.btn_SelectFile",param_AppImagePath);
        caller.pause("8000");
        //Verifying uploaded Image
        caller.checkImagePresent("ApplicationOverview.Img_AppLogo","param_AppLogoMainPath_PARAM:" + param_AppLogoMainPath,false,false,"null");
        caller.clickAt("ApplicationOverview.ele_Desc","10,10");
        caller.click("ApplicationOverview.btn_DescEditPencil");
        caller.pause("3000");
        caller.type("ApplicationOverview.tf_Description",param_Description);
        caller.pause("3000");
        caller.clickAt("ApplicationOverview.btn_SaveDesc","1,1");
        //Wait for saving description
        caller.pause("2000");
        //Verifying the edited description
        caller.checkObjectProperty("ApplicationOverview.lbl_Description","textContent",param_Description,false,"null");
        caller.clickAt("HomePage.btn_Home","1,1");
        //Waiting for application home page loading
        caller.pause("5000");
        //Verifying the image in home page
        caller.checkImagePresent("HomePage.Img_AppLogoSub","param_AppLogoSubPath_PARAM:" + param_AppLogoSubPath,false,false,"null");	
    }
    /**
     *  Business component biscomp_VerifyingTabVisibility.
     */
    public final static void biscomp_VerifyingTabVisibility(final SeleniumTestBase caller, final String param_OverviewTabVisibility, final String param_RepoandBuildTabVisibility, final String param_TeamTabVisibility, final String param_LifecycleManagementTabVisibility, final String param_ResourceTabVisibility, final String param_IssuesTabVisibility, final String param_LogsTabVisibility, final String param_DatabaseTabVisibility) throws Exception {
        //Verifying the visibility of the tabs
        caller.checkObjectProperty("ApplicationHome.lnk_Overview","ELEMENTPRESENT",param_OverviewTabVisibility,false,"null");
        caller.checkObjectProperty("ApplicationHome.lnk_RepoandBuild","ELEMENTPRESENT",param_RepoandBuildTabVisibility,false,"null");
        caller.checkObjectProperty("ApplicationHome.lnk_Team","ELEMENTPRESENT",param_TeamTabVisibility,false,"null");
        caller.checkObjectProperty("ApplicationHome.lnk_LifeCycleManagement","ELEMENTPRESENT",param_LifecycleManagementTabVisibility,false,"null");
        caller.checkObjectProperty("ApplicationHome.lnk_ResourceTab","ELEMENTPRESENT",param_ResourceTabVisibility,false,"null");
        caller.checkObjectProperty("ApplicationHome.lnk_Issues","ELEMENTPRESENT",param_IssuesTabVisibility,false,"null");
        caller.checkObjectProperty("ApplicationHome.lnk_Logs","ELEMENTPRESENT",param_LogsTabVisibility,false,"null");
        caller.checkObjectProperty("ApplicationHome.lnk_Databases","ELEMENTPRESENT",param_DatabaseTabVisibility,false,"");	
    }
    /**
     *  Business component biscomp_AddingVersionTFork.
     */
    public final static void biscomp_AddingVersionTFork(final SeleniumTestBase caller, final String param_AppStage, final String param_VersionNumber, final String param_DomainName, final String param_ForkedUser, final String param_AppKey) throws Exception {
        caller.clickAt("ApplicationReposandBuilds.ele_ddStageList","10,10");
        caller.clickAt("ApplicationReposandBuilds.lbl_AppStage","param_Stage_PARAM:" + param_AppStage,"10,10");
        //Waiting for page loading
        caller.pause("5000");
        caller.clickAt("ApplicationReposandBuilds.ele_ddVersionList","10,10");
        caller.clickAt("ApplicationReposandBuilds.lbl_VersionNumber","param_VersionNumber_PARAM:" + param_VersionNumber,"10,10");
        //Waiting for page loading
        caller.pause("5000");
        caller.clickAt("ApplicationReposandBuilds.btn_AddFork","param_VersionNumber_PARAM:" + param_VersionNumber,"10,10");
        //Waiting for page loading
        caller.pause("5000");
        //Verifying the fork repository label
        caller.checkElementPresent("ApplicationReposandBuilds.lbl_ForkedRepo",false,"null");
        caller.checkElementPresent("ApplicationReposandBuilds.ele_ForkURL","param_DomainName_PARAM:" + param_DomainName + "_PARAM," + "param_ForkedUser_PARAM:" + param_ForkedUser + "_PARAM," + "param_AppKey_PARAM:" + param_AppKey,false,"null");
        //Verifying the App wall notifications
        String BuildStartedForkRepoMsg = "Build started for "+param_VersionNumber+" in forked repo by "+param_ForkedUser;
        caller.checkElementPresent("Common.lbl_NotificationWallMsg","param_Message_PARAM:" + BuildStartedForkRepoMsg,false,"null");
        String BuildForkRepoSuccessMsg = param_VersionNumber+" forked repo built successfully";	
    }
    /**
     *  Business component biscomp_VerifyingLifcycleHistory.
     */
    public final static void biscomp_VerifyingLifcycleHistory(final SeleniumTestBase caller, final String param_Versionnumber, final String param_Action, final String param_ActionFrom, final String param_ActionTo, final String param_Stage) throws Exception {
        //Verifying history is shown
        caller.clickAt("ApplicationLifeCycleManagement.ele_ddStage","1,1");
        //Waiting for list loading
        caller.pause("3000");
        caller.clickAt("ApplicationLifeCycleManagement.lbl_Stage","param_Stage_PARAM:" + param_Stage,"1,1");
        //Waiting for list loading
        caller.pause("3000");
        caller.clickAt("ApplicationLifeCycleManagement.btn_LifecycleHistory","param_VersionNumber_PARAM:" + param_Versionnumber,"10,10");
        caller.checkElementPresent("ApplicationLifeCycleManagement.lbl_LifecycleHistoryEntry","param_Action_PARAM:" + param_Action + "_PARAM," + "param_ActionFrom_PARAM:" + param_ActionFrom + "_PARAM," + "param_ActionTo_PARAM:" + param_ActionTo,false,"");	
    }
    /**
     *  Business component biscomp_LaunchingAtOverviewPage.
     */
    public final static void biscomp_LaunchingAtOverviewPage(final SeleniumTestBase caller, final String param_VersionNumber, final String param_SampleText, final int param_WaitTimeInSec, final String param_AppType, final String param_AppKey, final String param_DomainName, final String param_VersionStage) throws Exception {
        //Launching application at overview page after promoting
        caller.clickAt("ApplicationHome.lnk_Overview","1,1");
        //Wait for page loading
        caller.pause("6000");
        if(caller.checkElementPresent("ApplicationOverview.btn_AcceptDeploy","param_VersionNumber_PARAM:" + param_VersionNumber)){
        caller.clickAt("ApplicationOverview.btn_AcceptDeploy","param_VersionNumber_PARAM:" + param_VersionNumber,"10,10");
        }
        //Wait for page loading
        caller.pause("8000");
        int varCount = 1;
        for(;varCount<=param_WaitTimeInSec;varCount++){
        if(caller.checkElementPresent("ApplicationOverview.lnk_Open","param_VersionNumber_PARAM:" + param_VersionNumber)){
        caller.clickAt("ApplicationOverview.lnk_Open","param_VersionNumber_PARAM:" + param_VersionNumber,"1,1");
        int varTemp = 1;break;
        } else if(varCount==param_WaitTimeInSec){
        caller.fail("Application is not deployed yet");
        } else {
        caller.keyPress("HomePage.btn_Home","F5");
        caller.pause("3000");
        }
        }
        //Verifying the sample page
        Common.biscomp_VerifyingSampleAppIndexPage(caller, param_SampleText,param_AppType,param_VersionStage,param_DomainName,param_AppKey,param_VersionNumber);	
    }
    /**
     *  Business component biscomp_AddDBUser.
     */
    public final static void biscomp_AddDBUser(final SeleniumTestBase caller, final String param_DBUserPassword, final String param_DBUserPasswordConf, final String param_AppStage, final String paramDBname, final String param_UserName) throws Exception {
        //click db link
        caller.clickAt("ApplicationHome.lnk_Databases","10,10");
        //click on edit database
        caller.clickAt("CreateNewDatabase.btn_EditDatabase","param_DBStage_PARAM:" + param_AppStage + "_PARAM," + "paramDBname_PARAM:" + paramDBname + "_PARAM," + "param_UserName_PARAM:" + param_UserName,"10,10");
        caller.clickAt("CreateNewDBUser.btn_AddnewUser","10,10");
        String varDBUsername = "DBU"+caller.generateData("Alphanumeric",2);
        caller.store("keyDBUsername2","String",varDBUsername);
        caller.type("CreateNewDBUser.tf_NewUser",varDBUsername);
        caller.type("CreateNewDBUser.tf_newUserPassWord",param_DBUserPassword);
        caller.pause("3000");
        caller.type("CreateNewDBUser.tf_NewRepeatPassWord",param_DBUserPasswordConf);
        caller.pause("2000");
        caller.clickAt("CreateNewDBUser.btn_CreateUser","10,10");
        caller.pause("2000");	
    }
    /**
     *  Business component biscomp_DBTemplate.
     */
    public final static void biscomp_DBTemplate(final SeleniumTestBase caller, final String param_AppStage) throws Exception {
        //Creating a Database Template
        caller.clickAt("ApplicationResources.btn_AddNewTemplate","10,10");
        String varDBTemplateName = "DBT"+caller.generateData("Alphanumeric",2);
        caller.store("keyDBTemplateName2","String",varDBTemplateName);
        caller.type("ApplicationResources.tf_DBTemplateName",varDBTemplateName);
        caller.clickAt("ApplicationResources.ele_ddAppStage","10,10");
        caller.clickAt("ApplicationResources.lbl_DBEnvironment","param_AppStage_PARAM:" + param_AppStage,"10,10");
        caller.clickAt("ApplicationResources.btn_CreateTemplate","10,10");	
    }
    /**
     *  Business component biscomp_UpdatingDeletingDB.
     */
    public final static void biscomp_UpdatingDeletingDB(final SeleniumTestBase caller, final String param_DBName, final String param_Appstage, final String param_DBUsername, final String param_UserName) throws Exception {
        //Updating the database
        caller.clickAt("ApplicationHome.lnk_Databases","10,10");
        //Wait for page loading
        caller.pause("3000");
        //click on edit database
        caller.clickAt("CreateNewDatabase.btn_EditDatabase","param_DBStage_PARAM:" + param_Appstage + "_PARAM," + "paramDBname_PARAM:" + param_DBName + "_PARAM," + "param_UserName_PARAM:" + param_UserName,"10,10");
        caller.clickAt("ApplicationResources.btn_AttachDBUser","Param_DBUsername_PARAM:" + param_DBUsername,"10,10");
        caller.pause("3000");
        //Verifying the attached user
        caller.checkElementPresent("ApplicationResources.lbl_AttachedDBUsername","param_DBUsername_PARAM:" + param_DBUsername,false,"null");
        //Detaching the Attached DB user
        caller.clickAt("ApplicationResources.btn_DetachDBUser","Param_DBUsername_PARAM:" + param_DBUsername,"10,10");
        caller.pause("5000");
        //Verify the Detached user is not listed under db users
        caller.checkObjectProperty("ApplicationResources.lbl_DetachUser","Param_DBUsername_PARAM:" + param_DBUsername,"ELEMENTPRESENT","false",false,"null");
        //Deletign the Database
        caller.clickAt("ApplicationResources.btn_DeleteDB","1,1");
        caller.clickAt("ApplicationResources.btn_OK","10,10");
        caller.pause("3000");
        caller.clickAt("ApplicationHome.lnk_Databases","10,10");
        //Verifying the Deleted DB is not displayed
        caller.checkObjectProperty("CreateNewDatabase.lnk_Database","param_AppStage_PARAM:" + param_Appstage + "_PARAM," + "param_DatabaseName_PARAM:" + param_DBName + "_PARAM," + "param_UserName_PARAM:" + param_UserName,"ELEMENTPRESENT","false",false,"null");	
    }
    /**
     *  Business component biscomp_UpdateTemplate.
     */
    public final static void biscomp_UpdateTemplate(final SeleniumTestBase caller, final String param_DBStage, final String param_DatabaseName, final String param_Username) throws Exception {
        caller.clickAt("ApplicationHome.lnk_Databases","10,10");
        //click the edit link
        caller.clickAt("CreateNewDatabase.btn_EditDatabase","param_DBStage_PARAM:" + param_DBStage + "_PARAM," + "paramDBname_PARAM:" + param_DatabaseName + "_PARAM," + "param_UserName_PARAM:" + param_Username,"10,10");
        caller.clickAt("EditDatabase.btn_EditPrivileges","10,10");
        //UNTICK YHE GRANT ANG DROP
        caller.clickAt("EditDBTemplate.chk_GrantPriv","10,10");
        caller.clickAt("EditDBTemplate.chk_DropPriv","10,10");
        caller.clickAt("EditDBTemplate.btn_Save","10,10");	
    }
    /**
     *  Business component biscomp_DeleteDBUser.
     */
    public final static void biscomp_DeleteDBUser(final SeleniumTestBase caller, final String param_Appstage, final String param_DBUsername, final String param_UserName, final String param_DBStage, final String paramDBname) throws Exception {
        caller.clickAt("ApplicationHome.lnk_Databases","1,1");
        //Wait for  page loading
        caller.pause("3000");
        caller.clickAt("CreateNewDatabase.btn_EditDatabase","param_DBStage_PARAM:" + param_DBStage + "_PARAM," + "paramDBname_PARAM:" + paramDBname + "_PARAM," + "param_UserName_PARAM:" + param_UserName,"10,10");
        //Wait for  page loading
        caller.pause("3000");
        caller.clickAt("ApplicationResources.btn_DeleteDBUser","Param_DBUsername_PARAM:" + param_DBUsername,"10,10");
        caller.pause("3000");
        caller.keyPress("HomePage.btn_Home","F5");
        //Verifying the deleted user is not displayed
        caller.checkObjectProperty("CreateNewDatabase.lbl_DBUser","param_DBUserName_PARAM:" + param_DBUsername,"ELEMENTPRESENT","false",false,"null");	
    }
    /**
     *  Business component biscomp_UpdatingDeletingTemplate.
     */
    public final static void biscomp_UpdatingDeletingTemplate(final SeleniumTestBase caller, final String param_Appstage, final String param_DBTemplateName) throws Exception {
        //Updating the Database Template
        caller.clickAt("ApplicationHome.lnk_Databases","10,10");
        caller.clickAt("ApplicationResources.lnk_DBTemplateName","param_Appstage_PARAM:" + param_Appstage + "_PARAM," + "param_DBTemplateName_PARAM:" + param_DBTemplateName,"10,10");
        caller.click("CreateNewDBTemplate.chk_InsertPrivilege");
        caller.click("CreateNewDBTemplate.chk_CreatePriv");
        caller.click("CreateNewDBTemplate.chk_DropPriv");
        caller.click("CreateNewDBTemplate.chk_LockTablesPriv");
        caller.clickAt("ApplicationResources.btn_DBTempSaveChanges","10,10");
        caller.clickAt("ApplicationResources.lnk_DBTemplateName","param_Appstage_PARAM:" + param_Appstage + "_PARAM," + "param_DBTemplateName_PARAM:" + param_DBTemplateName,"10,10");
        caller.checkObjectProperty("CreateNewDBTemplate.chk_InsertPrivilege","checked","false",false,"null");
        caller.checkObjectProperty("CreateNewDBTemplate.chk_CreatePriv","checked","false",false,"null");
        caller.checkObjectProperty("CreateNewDBTemplate.chk_DropPriv","checked","false",false,"null");
        caller.checkObjectProperty("CreateNewDBTemplate.chk_LockTablesPriv","checked","false",false,"null");
        caller.clickAt("ApplicationResources.btn_DeleteDBTemplate","10,10");
        caller.clickAt("ApplicationResources.btn_OK","10,10");
        caller.checkObjectProperty("ApplicationResources.lnk_DBTemplateName","param_Appstage_PARAM:" + param_Appstage + "_PARAM," + "param_DBTemplateName_PARAM:" + param_DBTemplateName,"ELEMENTPRESENT","false",false,"null");	
    }
    /**
     *  Business component biscomp_AddETA.
     */
    public final static void biscomp_AddETA(final SeleniumTestBase caller, final String param_VerisonNumber, final String param_WrongDateErrorMsg, final String param_Stage) throws Exception {
        //Adding the ETA and verifying calendar
        caller.clickAt("ApplicationHome.lnk_LifeCycleManagement","1,1");
        //Waiting for list loading
        caller.pause("3000");
        caller.clickAt("ApplicationLifeCycleManagement.ele_ddStage","1,1");
        caller.clickAt("ApplicationLifeCycleManagement.lbl_Stage","param_Stage_PARAM:" + param_Stage,"1,1");
        caller.click("ApplicationLifeCycleManagement.tf_CompletionDate","param_VersionNumber_PARAM:" + param_VerisonNumber);
        caller.checkElementPresent("ApplicationLifeCycleManagement.ele_Calendar",false,"");
        caller.clickAt("ApplicationLifeCycleManagement.btn_CloseCalendar","1,1");
        String varPreviousDate = caller.generateData("date|yyyy-MM-dd", -1);
        caller.type("ApplicationLifeCycleManagement.tf_CompletionDate","param_VersionNumber_PARAM:" + param_VerisonNumber,varPreviousDate);
        caller.clickAt("ApplicationLifeCycleManagement.btn_Save","param_VersionNumber_PARAM:" + param_VerisonNumber,"1,1");
        //Wait for saving
        caller.pause("1000");
        //Verifying the Error Message
        caller.checkObjectProperty("ApplicationLifeCycleManagement.lbl_WrongDateErrorMsg","textContent",param_WrongDateErrorMsg,false,"");
        String varNextDate = caller.generateData("date|yyyy-MM-dd", 1);
        caller.store("keyNextDate","String",varNextDate);
        caller.type("ApplicationLifeCycleManagement.tf_CompletionDate","param_VersionNumber_PARAM:" + param_VerisonNumber,varNextDate);
        caller.clickAt("ApplicationLifeCycleManagement.btn_Save","param_VersionNumber_PARAM:" + param_VerisonNumber,"1,1");
        //Wait for saving
        caller.pause("2000");
        caller.checkObjectProperty("ApplicationLifeCycleManagement.lbl_ETA","param_VersionNumber_PARAM:" + param_VerisonNumber,"textContent",varNextDate,false,"");	
    }
    /**
     *  Business component biscomp_EditETA.
     */
    public final static void biscomp_EditETA(final SeleniumTestBase caller, final String param_Stage, final String param_VersionNumber, final String param_CurrentETA) throws Exception {
        //Editing ETA
        caller.clickAt("ApplicationHome.lnk_LifeCycleManagement","1,1");
        //Waiting for list loading
        caller.pause("3000");
        caller.clickAt("ApplicationLifeCycleManagement.ele_ddStage","1,1");
        caller.clickAt("ApplicationLifeCycleManagement.lbl_Stage","param_Stage_PARAM:" + param_Stage,"1,1");
        //Verifying the ETA added
        caller.checkObjectProperty("ApplicationLifeCycleManagement.lbl_ETA","param_VersionNumber_PARAM:" + param_VersionNumber,"textContent",param_CurrentETA,false,"");
        caller.click("ApplicationLifeCycleManagement.btn_EditETA","param_VersionNumber_PARAM:" + param_VersionNumber);
        String varNewDate = caller.generateData("date|yyyy-MM-dd", 2);
        caller.store("keyNewDate","String",varNewDate);
        caller.type("ApplicationLifeCycleManagement.tf_CompletionDate","param_VersionNumber_PARAM:" + param_VersionNumber,varNewDate);
        caller.clickAt("ApplicationLifeCycleManagement.btn_Save","param_VersionNumber_PARAM:" + param_VersionNumber,"1,1");
        //Wait for saving
        caller.pause("3000");
        caller.checkObjectProperty("ApplicationLifeCycleManagement.lbl_ETA","param_VersionNumber_PARAM:" + param_VersionNumber,"textContent",varNewDate,false,"");	
    }
}
