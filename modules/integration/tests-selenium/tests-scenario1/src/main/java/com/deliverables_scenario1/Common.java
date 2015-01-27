package com.deliverables_scenario1;

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
        //Verifying sign out is successful
        caller.checkElementPresent("Login.tf_UserName",false,"");	
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
        String varNewUser = "";varNewUser=param_UserInitials+caller.generateData("Alphanumeric",3)+"_QA";
        //Store User name
        caller.store(members[varCount],"String",varNewUser);
        if(varCount!=varMemberCount-1){
        int varTemp = 1;varUserList=varUserList.concat(varNewUser).concat(",");
        } else {
        int varTemp = 1;varUserList=varUserList.concat(varNewUser);
        }
        } else if(members[varCount].contains("_DEV")){
        String varNewUser = "";varNewUser=param_UserInitials+caller.generateData("Alphanumeric",3)+"_DEV";
        //Store User name
        caller.store(members[varCount],"String",varNewUser);
        if(varCount!=varMemberCount-1){
        int varTemp = 1;varUserList=varUserList.concat(varNewUser).concat(",");
        } else {
        int varTemp = 1;varUserList=varUserList.concat(varNewUser);
        }
        } else if(members[varCount].contains("_AppOwner")){
        String varNewUser = "";varNewUser=param_UserInitials+caller.generateData("Alphanumeric",3)+"_AppOwner";
        //Store User name
        caller.store(members[varCount],"String",varNewUser);
        if(varCount!=varMemberCount-1){
        int varTemp = 1;varUserList=varUserList.concat(varNewUser).concat(",");
        } else {
        int varTemp = 1;varUserList=varUserList.concat(varNewUser);
        }
        } else if(members[varCount].contains("_DevOps")){
        String varNewUser = "";varNewUser=param_UserInitials+caller.generateData("Alphanumeric",3)+"_DevOps";
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
        //Until member array items ends select roles
        for(;varCount<varMemberCount;varCount++){
        caller.click("ManageMembers.chk_User","param_UserName_PARAM:" + members[varCount]);
        caller.click("ManageMembers.btn_UserRoleEdit");
        if(members[varCount].contains("_DEV")){
        caller.click("ManageMembers.chk_Developer");
        String varRole = "Developer";
        caller.store("keyRole","String",varRole);
        } else if(members[varCount].contains("_QA")){
        caller.click("ManageMembers.chk_QA");
        String varRole = "QA";
        caller.store("keyRole","String",varRole);
        } else if(members[varCount].contains("_AppOwner")){
        caller.click("ManageMembers.chk_ApplicationOwner");
        String varRole = "Application Owner";
        caller.store("keyRole","String",varRole);
        } else if(members[varCount].contains("_DevOps")){
        caller.click("ManageMembers.chk_DevOps");
        String varRole = "DevOps";
        caller.store("keyRole","String",varRole);
        }
        caller.click("ManageMembers.btn_SaveRole");
        String varRole = caller.retrieveString("keyRole");
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
        if(members[varCount].contains("_DEV")){
        caller.click("ManageMembers.chk_Developer");
        caller.click("ManageMembers.chk_QA");
        String varRole = "Qa";
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
    public final static void biscomp_RemoveUserFromApp(final SeleniumTestBase caller, final String param_UserList) throws Exception {
        //Delete Team members
        caller.click("ApplicationHome.lnk_Team");
        //Assign members text string in to an array
        int varMemberCount = 0;String[] members=param_UserList.split(",");varMemberCount=members.length;
        int varCount = 0;
        //Untill member array items ends select users
        for(;varCount<varMemberCount;varCount++){
        String varUser = caller.retrieveString(members[varCount]);
        caller.click("ApplicationTeam.chk_User","param_MemberName_PARAM:" + varUser);
        }
        //Click delete button
        caller.click("ApplicationTeam.btn_DeleteMember");
        caller.click("ApplicationTeam.btn_PopUpOk");
        //Page is not getting refreshed, navigate to overview and come back to update the page
        caller.pause("6000");
        caller.click("ApplicationHome.lnk_Overview");
        caller.click("ApplicationHome.lnk_Team");
        //Untill member array items ends select users
        int varCount2 = 0;
        //Verify page load
        caller.checkElementPresent("ApplicationTeam.btn_DeleteMember",false,"");
        caller.pause("5000");
        for(;varCount2<varMemberCount;varCount2++){
        String varUser = caller.retrieveString(members[varCount2]);
        caller.checkObjectProperty("ApplicationTeam.ele_TeamOtherMemberName","param_MemberOtherName_PARAM:" + varUser,"ELEMENTPRESENT","false",false,"");
        }	
    }
    /**
     *  Business component biscomp_ImportMembers.
     */
    public final static void biscomp_ImportMembers(final SeleniumTestBase caller, final String param_User, final String param_DefaultPassword, final String param_PasswordConf) throws Exception {
        //Import members in to domain
        caller.click("HomePage.btn_ManageUsers");
        caller.click("ManageMembers.btn_ImportMembers");
        caller.type("ImportMembers.tf_UserList",param_User);
        caller.type("ImportMembers.tf_DefaultPassword",param_DefaultPassword);
        caller.type("ImportMembers.tf_ConfirmDefaultPassword",param_PasswordConf);
        caller.click("ImportMembers.btn_Import");	
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
     *  Business component biscomp_LoginCloud.
     */
    /**
     *  Business component biscomp_LoginCloud.
     */
    public final static void biscomp_LoginCloud(final SeleniumTestBase caller) throws Exception {
    	
    }
    /**
     *  Business component biscomp_Login.
     */
    public final static void biscomp_Login(final SeleniumTestBase caller, final String param_Username, final String param_Password, final String param_MainURL) throws Exception {
        //Login in to the Application
        caller.open("<param_MainURL>","param_MainURL_PARAM:" + param_MainURL,"8000");
        caller.type("Login.tf_UserName",param_Username);
        caller.type("Login.tf_Password",param_Password);
        caller.click("Login.btn_SignIn");
        if(!caller.checkElementPresent("HomePage.btn_AddNewApplication") && !caller.checkElementPresent("HomePage.ele_Search")){
        caller.pause("1000");
        if(caller.checkElementPresent("Login.tf_OldPassword")){
        caller.type("Login.tf_OldPassword",param_Password);
        caller.type("Login.tf_NewPassword",param_Password);
        caller.type("Login.tf_ConfirmNewPassword",param_Password);
        caller.click("Login.btn_Submit");
        } else if(caller.checkElementPresent("Login.lnk_AppCloud")){
        caller.click("Login.lnk_AppCloud");
        }
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
        caller.clickAt("AddNewApplication.ele_ddCreateAppType","10,10");
        caller.type("AddNewApplication.tf_SearchAppType",param_AppType);
        caller.clickAt("AddNewApplication.ele_ApplicationType","param_ApplicationType_PARAM:" + param_AppType,"10,10");
        } else if("Upload".equals(param_AppCreationMethod)){
        caller.click("AddNewApplication.rdo_UploadApp");
        caller.clickAt("AddNewApplication.ele_ddUploadAppType","10,10");
        caller.type("AddNewApplication.tf_SearchAppType",param_AppType);
        caller.clickAt("AddNewApplication.ele_ApplicationType","param_ApplicationType_PARAM:" + param_AppType,"10,10");
        caller.type("AddNewApplication.btn_BrowseAppUpload",param_AppPath);
        caller.pause("3000");
        }
        if(param_AppImagePath!=null){
        caller.type("AddNewApplication.btn_BrowseImage",param_AppImagePath);
        }
        if(param_AppDescription!=null){
        caller.type("AddNewApplication.tf_Description",param_AppDescription);
        }
        if("Upload".equals(param_AppCreationMethod)){
        caller.clickAt("AddNewApplication.btn_UploadApplication","10,10");
        //Waiting to application get uploaded
        caller.pause("10000");
        int varCount = 1;
        for(;varCount<=10;varCount++){
        if(caller.checkElementPresent("AddNewApplication.ele_Spinner")){
        caller.pause("3000");
        } else if(varCount==10){
        caller.fail("File is not uploaded.");
        } else {
        int varTemp = 1;break;
        }
        }
        }
        if("Create".equals(param_AppCreationMethod)){
        caller.clickAt("AddNewApplication.btn_CreateApplication","10,10");
        }
        //Wait for page loading
        caller.pause("10000");	
    }
    /**
     *  Business component biscomp_CreateAppWithSameAppKey.
     */
    public final static void biscomp_CreateAppWithSameAppKey(final SeleniumTestBase caller, final String param_AppKey, final String param_SameAppKeyErrorMsg) throws Exception {
        //Create an application
        caller.click("HomePage.btn_AddNewApplication");
        caller.type("AddNewApplication.tf_AppName","test+caller.generateData(\"Alphanumeric\",3)");
        caller.type("AddNewApplication.tf_AppKey",param_AppKey);
        caller.fireEvent("KEY%key=\t","2000");
        //Waiting for error message
        caller.pause("3000");
        //Verifying the ErrorMessage
        caller.checkObjectProperty("AddNewApplication.lbl_AppCreationErrorMsg","textContent",param_SameAppKeyErrorMsg,false,"");
        caller.clickAt("HomePage.btn_Home","10,10");
        //Waiting for page loading
        caller.pause("8000");	
    }
    /**
     *  Business component biscomp_VerifyingNotificationWallPost.
     */
    public final static void biscomp_VerifyingNotificationWallPost(final SeleniumTestBase caller, final String param_AppCreationMsg, final String param_GITRepoMsg, final String param_JenkinsMsg, final String param_IssueTrackerMsg, final String param_CloudMsg) throws Exception {
        //Verifying the post on notification wall
        //App creation message
        caller.checkElementPresent("Common.lbl_NotificationWallMsg","param_Message_PARAM:" + param_AppCreationMsg,false,"");
        //GIT repo message
        caller.checkElementPresent("Common.lbl_NotificationWallMsg","param_Message_PARAM:" + param_GITRepoMsg,false,"");
        //Jenkins Message
        caller.checkElementPresent("Common.lbl_NotificationWallMsg","param_Message_PARAM:" + param_JenkinsMsg,false,"");
        //Issue Tracker Message
        caller.checkElementPresent("Common.lbl_NotificationWallMsg","param_Message_PARAM:" + param_IssueTrackerMsg,false,"");
        //Cloud Env Message
        caller.checkElementPresent("Common.lbl_NotificationWallMsg","param_Message_PARAM:" + param_CloudMsg,false,"");	
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
        caller.keyPress("HomePage.btn_AddNewApplication","F5");
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
        caller.click("ApplicationReposandBuilds.ele_ExpandIcon");
        }
        //Wait for Build
        caller.pause("15000");
        caller.keyPress("HomePage.btn_Home","F5");
        //Waiting for page loading
        caller.pause("5000");
        //Verifying the application Build Status
        caller.checkObjectProperty("ApplicationReposandBuilds.lbl_BuildStatus","textContent",param_BuildStatus,false,"");
        //Wait for Deploy
        caller.pause("10000");
        caller.keyPress("HomePage.btn_Home","F5");
        //Waiting for page loading
        caller.pause("3000");
        //Verifying the application Deploy Status
        String varDeployStatus = "Build 1 "+param_DeploymentStatus;
        caller.checkObjectProperty("ApplicationReposandBuilds.lbl_DeployStatus","textContent",varDeployStatus,false,"");
        } else {
        caller.fail("Correct application stage is not selected");
        }	
    }
    /**
     *  Business component biscomp_LaunchingApp.
     */
    public final static void biscomp_LaunchingApp(final SeleniumTestBase caller, final String param_AppStage, final String param_VersionNumber, final String param_SamleText, final String param_AppType) throws Exception {
        //Launching the application and verifyig the pop-up window
        caller.clickAt("ApplicationHome.lnk_RepoandBuild","10,10");
        //Wait for application loading
        caller.pause("6000");
        //Wait for application loading
        caller.pause("3000");
        caller.clickAt("ApplicationReposandBuilds.ele_ddStageList","10,10");
        caller.pause("4000");
        caller.checkElementPresent("ApplicationReposandBuilds.lbl_AppStage","param_Stage_PARAM:" + param_AppStage,false,"");
        //Waiting for page loading
        caller.pause("4000");
        caller.clickAt("ApplicationReposandBuilds.lbl_AppStage","param_Stage_PARAM:" + param_AppStage,"10,10");
        //Waiting for page loading
        caller.pause("5000");
        caller.clickAt("ApplicationReposandBuilds.ele_ddVersionList","10,10");
        //wait for page load
        caller.pause("2000");
        caller.clickAt("ApplicationReposandBuilds.lbl_VersionNumber","param_VersionNumber_PARAM:" + param_VersionNumber,"10,10");
        //wait for page load
        caller.pause("2000");
        caller.clickAt("ApplicationReposandBuilds.btn_Launch","10,10");
        //Wait for window loading
        caller.pause("5000");
        Common.biscomp_VerifyingSampleAppIndexPage(caller, param_SamleText,param_AppType);
        //Waiting for page loading
        caller.pause("3000");	
    }
    /**
     *  Business component biscomp_SelectApp.
     */
    public final static void biscomp_SelectApp(final SeleniumTestBase caller, final String param_AppName) throws Exception {
        //Selecting the application
        caller.click("HomePage.ele_ApplicationName","param_AppName_PARAM:" + param_AppName);
        //Wait for application loading
        caller.pause("3000");	
    }
    /**
     *  Business component biscomp_AddMembers.
     */
    public final static void biscomp_AddMembers(final SeleniumTestBase caller, final String param_UsernameList, final String param_InvitationMsg) throws Exception {
        //Accessing thr Team tab
        String varUserList = "";
        caller.click("ApplicationHome.lnk_Team");
        caller.click("ApplicationTeam.btn_AddMembers");
        String usernameList = param_UsernameList;
        //Assign members text string in to an array
        int varMemberCount = 0;String[] members=usernameList.split(",");varMemberCount=members.length;
        int varCount = 0;
        //Untill member array items ends select roles
        for(;varCount<varMemberCount;varCount++){
        String varUser = members[varCount];
        caller.type("ApplicationTeam.tf_Usernames",varUser);
        caller.click("ApplicationTeam.btn_AddToList");
        caller.checkElementPresent("ApplicationTeam.ele_TeamAdminMemberName","param_AdminMemberName_PARAM:" + varUser,false,"");
        }
        //Inviting Members
        caller.click("ApplicationTeam.btn_Invite");
        caller.click("ApplicationHome.lnk_Team");
        //Verify added user in the team page
        int varCount2 = 0;
        for(;varCount2<varMemberCount;varCount2++){
        String varUser = members[varCount2];
        caller.checkElementPresent("ApplicationTeam.ele_AddedMember","param_MemberName_PARAM:" + varUser,false,"");
        caller.pause("1000");
        caller.checkElementPresent("Common.lbl_NotificationWallMsg","param_Message_PARAM:" + varUser+" "+param_InvitationMsg,false,"");
        caller.store("keyUserList","String",varUserList);
        }	
    }
    /**
     *  Business component biscomp_CreateBranch.
     */
    public final static void biscomp_CreateBranch(final SeleniumTestBase caller, final String param_AppStage, final String param_VersionNumber, final String param_NewVersionNumber) throws Exception {
        //Accessing the 'Repo & Build' page
        caller.click("ApplicationHome.lnk_RepoandBuild");
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
        caller.pause("8000");
        caller.clickAt("ApplicationReposandBuilds.ele_ddVersionList","10,10");
        //Wait for application loading
        caller.pause("3000");
        caller.checkElementPresent("ApplicationReposandBuilds.lbl_VersionNumber","param_VersionNumber_PARAM:" + param_NewVersionNumber,false,"");
        //wait for page load
        caller.pause("3000");
        caller.clickAt("ApplicationReposandBuilds.lbl_VersionNumber","param_VersionNumber_PARAM:" + param_NewVersionNumber,"10,10");	
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
        //Waiting for page loading
        caller.pause("3000");
        //Manually building and deploying
        caller.clickAt("ApplicationReposandBuilds.btn_Build","10,10");
        //Waiting for build number to be updated
        caller.pause(param_Pause);
        caller.keyPress("ApplicationReposandBuilds.btn_Build","F5");
        //Waiting for page loading
        caller.pause("3000");
        caller.checkObjectProperty("ApplicationReposandBuilds.lbl_BuildStatus","textContent",param_BuildStatus,false,"");
        //Waiting for page loading
        caller.pause("2000");
        caller.checkObjectProperty("ApplicationReposandBuilds.lbl_BuildNumber","textContent",param_BuildNumber,false,"");
        //Waiting for page loading
        caller.pause("2000");
        caller.clickAt("ApplicationReposandBuilds.btn_Deploy","10,10");
        //Waiting for deploy number to be updated
        caller.pause(param_Pause);
        caller.keyPress("ApplicationReposandBuilds.btn_Build","F5");
        //Waiting for page loading
        caller.pause("3000");
        String varDeployStatus = "Build "+param_DeployNumber+" Deployed";
        //Waiting for page loading
        caller.pause("3000");
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
        caller.clickAt("ApplicationReposandBuilds.btn_ForkBuild","10,10");
        //Wait for Build ID get update
        caller.pause("15000");
        caller.keyPress("ApplicationReposandBuilds.btn_ForkBuild","F5");
        if(caller.checkElementPresent("ApplicationReposandBuilds.ele_ExpandIcon")){
        caller.click("ApplicationReposandBuilds.ele_ExpandIcon");
        }
        caller.checkObjectProperty("ApplicationReposandBuilds.lbl_MasterRepoBuildNumber","textContent",param_Buildnumber,false,"");
        //Waiting for page loading
        caller.pause("2000");
        caller.checkObjectProperty("ApplicationReposandBuilds.lbl_MasterRepoBuildStatus","textContent",param_BuildStatus,false,"");
        //Waiting for page loading
        caller.pause("2000");
        caller.checkElementPresent("ApplicationReposandBuilds.lbl_DeployedNumber","param_DeplyedNumber_PARAM:" + param_DeployedNumber,false,"");
        }	
    }
    /**
     *  Business component biscomp_CreateDatabase.
     */
    public final static void biscomp_CreateDatabase(final SeleniumTestBase caller, final String param_DatabaseEnvironment, final String param_DefaultPassword, final String param_AllEnv) throws Exception {
        //click Database link
        caller.click("ApplicationHome.lnk_Databases");
        //Wait for page loading
        caller.pause("5000");
        caller.click("CreateNewDatabase.btn_AddNewDatabase");
        String varDBName = "DB_"+caller.generateData("Alphanumeric",2);
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
        //wait for page load
        caller.pause("3000");
        caller.clickAt("CreateNewDatabase.btn_CreateDatabase","10,10");
        //Wait for page load
        caller.pause("5000");	
    }
    /**
     *  Business component biscomp_CreateDBWithDBUserAndDBTemplate.
     */
    public final static void biscomp_CreateDBWithDBUserAndDBTemplate(final SeleniumTestBase caller, final String param_DBUserPasswordValid, final String param_DBStage) throws Exception {
        //click Database link
        caller.click("ApplicationHome.lnk_Databases");
        //Wait for page loading
        caller.pause("5000");
        caller.click("CreateNewDatabase.btn_AddNewDatabase");
        String varDBName = "DB_"+caller.generateData("Alphanumeric",2);
        caller.store("keyDBName","String",varDBName);
        caller.type("CreateNewDatabase.tf_DBName",varDBName);
        caller.click("CreateNewDatabase.chk_AdvancedOptions");
        caller.clickAt("CreateNewDatabase.ele_ddUser","1,1");
        //Wait for list loading
        caller.pause("2000");
        caller.clickAt("CreateNewDatabase.lbl_CreateNewDBUser","10,10");
        //Wait for page loading
        caller.pause("2000");
        String varDBUsername = "DBU_"+caller.generateData("Alphanumeric",3);
        caller.store("keyDBUsername","String",varDBUsername);
        caller.type("CreateNewDatabase.tf_NewUserUsername",varDBUsername);
        //Entering invalid password and verifying the error message
        caller.type("CreateNewDatabase.tf_NewUserPassword",param_DBUserPasswordValid);
        caller.type("CreateNewDatabase.tf_NewUserRepeatPassword",param_DBUserPasswordValid);
        caller.clickAt("CreateNewDatabase.btn_CreateDBUser","10,10");
        //Wait for db user creation
        caller.pause("3000");
        caller.checkElementPresent("CreateNewDatabase.ele_SelectedDBUser","param_SelectedDBUser_PARAM:" + varDBUsername,false,"");
        caller.clickAt("CreateNewDatabase.ele_ddPermissionTemplate","10,10");
        caller.clickAt("CreateNewDatabase.lbl_CreateNewDBTemplate","10,10");
        String varDBTemplateName = "DBTemp_"+caller.generateData("Alphanumeric",3);
        caller.store("keyDBTemplateName","String",varDBTemplateName);
        caller.type("CreateNewDatabase.tf_NewpermissionTemplateName",varDBTemplateName);
        caller.clickAt("CreateNewDatabase.btn_CreateTemplate","10,10");
        //Wait for DB Template creation
        caller.pause("3000");
        caller.checkElementPresent("CreateNewDatabase.ele_SelectedDBTemplate","param_SelectedDBTemplate_PARAM:" + varDBTemplateName+"@"+param_DBStage,false,"");
        //wait for page load
        caller.pause("3000");
        caller.clickAt("CreateNewDatabase.btn_CreateDatabase","10,10");
        //wait for page load
        caller.pause("3000");	
    }
    /**
     *  Business component biscomp_VerifyingDatabases.
     */
    public final static void biscomp_VerifyingDatabases(final SeleniumTestBase caller, final String param_DatabaseName, final String param_Username, final String param_DBStage, final String param_DBUserName) throws Exception {
        //wait for page load
        caller.pause("3000");
        //verify App stage, database and username
        caller.checkElementPresent("CreateNewDatabase.lnk_Database","param_AppStage_PARAM:" + param_DBStage + "_PARAM," + "param_DatabaseName_PARAM:" + param_DatabaseName + "_PARAM," + "param_UserName_PARAM:" + param_Username,false,"");
        //wait for page load
        caller.pause("3000");
        //click the edit link
        caller.click("CreateNewDatabase.btn_EditDatabase","param_DBStage_PARAM:" + param_DBStage + "_PARAM," + "paramDBname_PARAM:" + param_DatabaseName + "_PARAM," + "param_UserName_PARAM:" + param_Username);
        //wait for page load
        caller.pause("3000");
        //verify DB user
        caller.checkElementPresent("CreateNewDatabase.lbl_DBUser","param_DBUserName_PARAM:" + param_DBUserName,false,"");	
    }
    /**
     *  Business component biscomp_CreateDatasource.
     */
    public final static void biscomp_CreateDatasource(final SeleniumTestBase caller, final String param_DatasourceName, final String param_DatasourceDescription, final String param_DBDriver, final String param_DSEnv, final String param_DBURL, final String param_DBUsername, final String param_DatasourcePassword) throws Exception {
        if(caller.checkElementPresent("ApplicationResources.btn_AddDatasource")){
        caller.clickAt("ApplicationResources.btn_AddDatasource","10,10");
        } else {
        caller.clickAt("ApplicationResources.btn_AddNewDatasource","10,10");
        }
        String varDSName = "DS_"+caller.generateData("Alphanumeric",3);
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
        caller.clickAt("ApplicationLifeCycleManagement.ele_ddStage","10,10");
        caller.clickAt("ApplicationLifeCycleManagement.lbl_Stage","param_Stage_PARAM:" + param_Stage,"10,10");
        //Wait for page loading
        caller.pause("7000");
        if(param_Stage.contains("Development")){
        caller.click("ApplicationLifeCycleManagement.chk_CodeCompleted","param_Versionnumber_PARAM:" + param_versionNumber);
        caller.click("ApplicationLifeCycleManagement.chk_DesignReviewDone","param_Versionnumber_PARAM:" + param_versionNumber);
        caller.click("ApplicationLifeCycleManagement.chk_CodeReviewDone","param_Versionnumber_PARAM:" + param_versionNumber);
        } else if(param_Stage.contains("Testing")){
        caller.click("ApplicationLifeCycleManagement.chk_SmokeTestPassed","param_Versionnumber_PARAM:" + param_versionNumber);
        caller.click("ApplicationLifeCycleManagement.chk_TestCasesPassed","param_Versionnumber_PARAM:" + param_versionNumber);
        }
        caller.clickAt("ApplicationLifeCycleManagement.btn_Promote","param_VersionNumber_PARAM:" + param_versionNumber,"10,10");
        //Wait for promoting
        caller.pause("8000");	
    }
    /**
     *  Business component biscomp_BuildNewVersionForUnmap.
     */
    public final static void biscomp_BuildNewVersionForUnmap(final SeleniumTestBase caller, final String param_NewBranch, final String param_Stage, final String param_versionNumber) throws Exception {
        caller.clickAt("ApplicationHome.lnk_RepoandBuild","10,10");
        //waiting for page load
        caller.pause("3000");
        caller.clickAt("ApplicationReposandBuilds.btn_CreateBranchMain","10,10");
        caller.type("ApplicationReposandBuilds.tf_NewVersionNumber",param_NewBranch);
        caller.pause("2000");
        caller.clickAt("ApplicationReposandBuilds.btn_CreateBranchSub","10,10");
        //waiting for page load
        caller.pause("5000");
        caller.clickAt("ApplicationHome.lnk_LifeCycleManagement","10,10");
        caller.pause("8000");
        Common.biscomp_Promote(caller, param_Stage,param_versionNumber);
        caller.pause("6000");	
    }
    /**
     *  Business component biscomp_Demote.
     */
    public final static void biscomp_Demote(final SeleniumTestBase caller, final String param_Stage, final String param_VersionToDemote, final String param_IssueSummary, final String param_DemoteComment) throws Exception {
        caller.clickAt("ApplicationLifeCycleManagement.ele_ddStage","10,10");
        caller.clickAt("ApplicationLifeCycleManagement.lbl_Stage","param_Stage_PARAM:" + param_Stage,"10,10");
        if("Production".equals(param_Stage)){
        caller.click("ApplicationLifeCycleManagement.btn_DemotePro","param_VersionNumber_PARAM:" + param_VersionToDemote);
        } else if("Testing".equals(param_Stage)){
        caller.click("ApplicationLifeCycleManagement.btn_DemoteTest","param_VersionNumber_PARAM:" + param_VersionToDemote);
        }
        if(caller.checkElementPresent("ApplicationLifeCycleManagement.chk_Issue","param_IssueSummary_PARAM:" + param_IssueSummary)){
        if(param_IssueSummary!=null){
        caller.click("ApplicationLifeCycleManagement.chk_Issue","param_IssueSummary_PARAM:" + param_IssueSummary);
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
        caller.clickAt("ApplicationLifeCycleManagement.ele_ddStage","10,10");
        //Waiting for list loading
        caller.pause("3000");
        caller.clickAt("ApplicationLifeCycleManagement.lbl_Stage","param_Stage_PARAM:" + param_Stage,"10,10");
        //Waiting for list loading
        caller.pause("3000");
        caller.checkObjectProperty("ApplicationLifeCycleManagement.lbl_VerisonNumber","param_VersionNumber_PARAM:" + param_VersionNumber,"ELEMENTPRESENT",param_VersionVisibility,false,"");	
    }
    /**
     *  Business component biscomp_EditIssue.
     */
    public final static void biscomp_EditIssue(final SeleniumTestBase caller, final String param_IssueSummary, final String param_IssueDescription, final String param_Version, final String param_IssueType, final String param_IssuePriority, final String param_IssueStatus, final String param_IssueAssignee, final String param_IssueSeverity) throws Exception {
        //Editing the Issue
        caller.clickAt("ApplicationHome.lnk_Issues","10,10");
        caller.selectFrame("frames.frm_iframe");
        caller.click("ApplicationIssues.btn_Edit","param_IssueSummary_PARAM:" + param_IssueSummary);
        if(param_IssueDescription!=null){
        caller.type("ApplicationIssues.tf_IssueDescription",param_IssueDescription);
        }
        if(param_IssueType!=null){
        caller.clickAt("ApplicationIssues.ele_ddIssueType","10,10");
        caller.clickAt("ApplicationIssues.lbl_IssueType","param_IssueType_PARAM:" + param_IssueType,"10,10");
        }
        if(param_IssuePriority!=null){
        caller.clickAt("ApplicationIssues.ele_ddIssuePriority","10,10");
        caller.clickAt("ApplicationIssues.lbl_IssuePriority","param_IssuePriority_PARAM:" + param_IssuePriority,"10,10");
        }
        if(param_IssueStatus!=null){
        caller.clickAt("ApplicationIssues.ele_ddIssueStatus","10,10");
        caller.clickAt("ApplicationIssues.lbl_IssueStatus","param_IssueStatus_PARAM:" + param_IssueStatus,"10,10");
        }
        if(param_IssueAssignee!=null){
        caller.clickAt("ApplicationIssues.ele_ddIssueAssignee","10,10");
        caller.clickAt("ApplicationIssues.lbl_IssueAssignee","param_IssueAssignee_PARAM:" + param_IssueAssignee,"10,10");
        }
        if(param_Version!=null){
        caller.clickAt("ApplicationIssues.ele_ddVersion","10,10");
        caller.clickAt("ApplicationIssues.lbl_Version","param_Version_PARAM:" + param_Version,"10,10");
        }
        if(param_IssueSeverity!=null){
        caller.clickAt("ApplicationIssues.ele_ddSeverity","10,10");
        caller.clickAt("ApplicationIssues.lbl_Severity","param_IssueSeverity_PARAM:" + param_IssueSeverity,"10,10");
        }
        caller.clickAt("ApplicationIssues.btn_UpdateIssue","10,10");	
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
        caller.clickAt("ApplicationIssues.ele_ddIssueType","10,10");
        caller.clickAt("ApplicationIssues.lbl_IssueType","param_IssueType_PARAM:" + param_IssueType,"10,10");
        caller.clickAt("ApplicationIssues.ele_ddIssuePriority","10,10");
        caller.clickAt("ApplicationIssues.lbl_IssuePriority","param_IssuePriority_PARAM:" + param_IssuePriority,"10,10");
        caller.clickAt("ApplicationIssues.ele_ddIssueStatus","10,10");
        caller.clickAt("ApplicationIssues.lbl_IssueStatus","param_IssueStatus_PARAM:" + param_IssueStatus,"10,10");
        if(param_IssueAssignee!=null){
        caller.clickAt("ApplicationIssues.ele_ddIssueAssignee","10,10");
        caller.clickAt("ApplicationIssues.lbl_IssueAssignee","param_IssueAssignee_PARAM:" + param_IssueAssignee,"10,10");
        }
        caller.clickAt("ApplicationIssues.ele_ddVersion","10,10");
        caller.clickAt("ApplicationIssues.lbl_Version","param_Version_PARAM:" + param_Version,"10,10");
        caller.clickAt("ApplicationIssues.ele_ddSeverity","10,10");
        caller.clickAt("ApplicationIssues.lbl_Severity","param_IssueSeverity_PARAM:" + param_IssueSeverity,"10,10");
        caller.clickAt("ApplicationIssues.btn_AddIssue","10,10");	
    }
    /**
     *  Business component biscomp_CraeteCustomURL.
     */
    public final static void biscomp_CraeteCustomURL(final SeleniumTestBase caller, final String param_SubDomain) throws Exception {
        //Creating custom URL by adding sub domain
        caller.clickAt("ApplicationHome.lnk_Overview","10,10");
        caller.clickAt("ApplicationOverview.ele_SubDomain","10,10");
        caller.type("ApplicationOverview.tf_SubDomain",param_SubDomain);
        caller.clickAt("ApplicationOverview.btn_Save","10,10");
        //======================need to navigate and verify the smple page====================	
    }
    /**
     *  Business component biscomp_VerifyingSampleAppIndexPage.
     */
    public final static void biscomp_VerifyingSampleAppIndexPage(final SeleniumTestBase caller, final String param_SampleText, final String param_AppType) throws Exception {
        //Accessing the new window
        caller.pause("5000");
        caller.selectWindow("Window.win_NewWindow","param_NewWindowNumber_PARAM:1");
        if("Java Web Application".equals(param_AppType)){
        //Verifying the sample text
        caller.checkElementPresent("sampleIndex.lbl_SampleText","param_SampleText_PARAM:" + param_SampleText,false,"");
        //Closing the new window
        caller.fireEvent("KEY%key=alt+F4","2000");
        } else if("JAX-RS Service".equals(param_AppType)){
        /*
        SetVariable
          name=pageURL
          type=String
          paramValue=caller.getDriver().getCurrentUrl()
         If
          expression=@pageURL.contains("wadl")
          WriteToReport
           comment=Correct URL is loaded
         Else
          Fail
           message=Correct URL is not loaded
         EndIf
        */
        caller.checkElementPresent("sampleIndex.lbl_SampleTextJAXRS","param_SampleText_PARAM:" + param_SampleText,false,"");
        //Closing the new window
        caller.fireEvent("KEY%key=alt+F4","2000");
        } else if("JAX-WS Service".equals(param_AppType)){
        /*
        SetVariable
          name=pageURL
          type=String
          paramValue=caller.getDriver().getCurrentUrl()
         If
          expression=@pageURL.contains("wsdl")
          WriteToReport
           comment=Correct URL is loaded
         Else
          Fail
           message=Correct URL is not loaded
         EndIf
        */
        caller.checkElementPresent("sampleIndex.lbl_SampleText","param_SampleText_PARAM:" + param_SampleText,false,"");
        //Closing the new window
        caller.fireEvent("KEY%key=alt+F4","2000");
        } else if("Jaggery Application".equals(param_AppType)){
        caller.checkElementPresent("sampleIndex.lbl_SampleText","param_SampleText_PARAM:" + param_SampleText,false,"");
        //Closing the new window
        caller.fireEvent("KEY%key=alt+F4","2000");
        } else if("WSO2 Data Service".equals(param_AppType)){
        String pageURL = caller.getDriver().getCurrentUrl();
        if(pageURL.contains("wsdl")){
        caller.writeToReport("Correct URL is loaded");
        } else {
        caller.fail("Correct URL is not loaded");
        }
        //Closing the new window
        caller.fireEvent("KEY%key=alt+F4","2000");
        }
        //Waiting for page loading
        caller.pause("3000");	
    }
    /**
     *  Business component biscomp_ResolvingIssue.
     */
    public final static void biscomp_ResolvingIssue(final SeleniumTestBase caller, final String param_IssueSummary, final String param_IssueStatus) throws Exception {
        //Resolving a raised issue
        caller.clickAt("ApplicationHome.lnk_Issues","10,10");
        caller.selectFrame("frames.frm_iframe");
        caller.click("ApplicationIssues.btn_Edit","param_IssueSummary_PARAM:" + param_IssueSummary);
        caller.clickAt("ApplicationIssues.ele_ddIssueStatus","10,10");
        caller.clickAt("ApplicationIssues.lbl_IssueStatus","param_IssueStatus_PARAM:" + param_IssueStatus,"10,10");
        caller.clickAt("ApplicationIssues.btn_UpdateIssue","10,10");
        //Wait for page loading
        caller.pause("5000");	
    }
    /**
     *  Business component biscomp_CreatingInternalAPI.
     */
    public final static void biscomp_CreatingInternalAPI(final SeleniumTestBase caller, final String param_APIName, final String param_APIVersion, final String param_AppKey) throws Exception {
        //Creating a external API
        caller.clickAt("ApplicationHome.lnk_ResourceTab","10,10");
        caller.pause("3000");
        caller.clickAt("ApplicationResources.btn_GoToApiManager","10,10");
        caller.pause("3000");
        caller.selectWindow("Window.win_NewWindow","param_NewWindowNumber_PARAM:1");
        //Wait for page loading
        caller.pause("5000");
        caller.clickAt("APIManager.lnk_APIs","10,10");
        //Waiting for page loading
        caller.pause("3000");
        caller.clickAt("APIManager.lnk_APIName","param_APIName_PARAM:" + param_APIName + "_PARAM," + "param_APIVersion_PARAM:" + param_APIVersion,"10,10");
        //Waiting for page loading
        caller.pause("3000");
        caller.keyPress("APIManager.lst_Applications","F5");
        caller.pause("2000");
        caller.select("APIManager.lst_Applications",param_AppKey);
        /*
        ClickAt
         object=APIManager.lst_Applications
         coordinates=10,10
        ClickAt
         object=APIManager.ele_AppKey
         param_AppKey=@param_AppKey
         coordinates=10,10
        */
        caller.checkObjectProperty("APIManager.lst_Applications","SELECTEDOPTION",param_AppKey,false,"");
        caller.click("APIManager.btn_Subscribe");
        //Waiting for the pop-up window
        caller.pause("5000");
        caller.checkElementPresent("APIManager.lbl_PopUPMsg",false,"");
        caller.click("APIManager.btn_GoToMySubscriptions");
        //Wait for page loadin
        caller.pause("5000");
        caller.clickAt("APIManager.ele_ddApplicationList","10,10");
        caller.clickAt("APIManager.lbl_AppKey","param_AppKey_PARAM:" + param_AppKey,"10,10");
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
        caller.clickAt("ApplicationResources.lnk_APITab","10,10");
        caller.pause("3000");
        caller.clickAt("ApplicationResources.btn_AddExternalAPI","10,10");
        caller.pause("2000");
        String varExternalAPIName = "ExAPI_"+param_ExternalAPIName+caller.generateData("Alphanumeric",2);
        caller.store("keyExternalAPIName","String",varExternalAPIName);
        caller.type("ApplicationResources.tf_APIName",varExternalAPIName);
        caller.clickAt("ApplicationResources.ele_ddStage","10,10");
        caller.clickAt("ApplicationResources.lbl_Appstage","param_AppStage_PARAM:" + param_AppStage,"10,10");
        caller.clickAt("ApplicationResources.ele_ddAuthAPI","10,10");
        caller.clickAt("ApplicationResources.lbl_AuthAPI","param_AuthAPI_PARAM:" + param_AuthAPI,"10,10");
        caller.type("ApplicationResources.tf_APIURL",param_APIURL);
        caller.clickAt("ApplicationResources.btn_CreateAPI","10,10");	
    }
    /**
     *  Business component biscomp_CreatingProperty.
     */
    public final static void biscomp_CreatingProperty(final SeleniumTestBase caller, final String param_PropertyName, final String param_AppStage, final String param_PropertyDescription, final String param_PropertyValue, final String param_AllEnv) throws Exception {
        //Creating a property
        String propertyName = "prop_"+caller.generateData("Alphanumeric",2);
        caller.store("KeyPropertyName","String",propertyName);
        if(caller.checkElementPresent("ApplicationResources.btn_AddProperty")){
        caller.clickAt("ApplicationResources.btn_AddProperty","10,10");
        } else if(caller.checkElementPresent("ApplicationResources.btn_AddProperties")){
        caller.clickAt("ApplicationResources.btn_AddProperties","10,10");
        }
        caller.type("ApplicationResources.tf_PropertyName",propertyName);
        if(param_AllEnv.equals("NO")){
        caller.clickAt("ApplicationResources.ele_ddRegEnv","10,10");
        caller.clickAt("ApplicationResources.lbl_AppStage","param_AppStage_PARAM:" + param_AppStage,"10,10");
        } else if(param_AllEnv.equals("YES")){
        caller.clickAt("ApplicationResources.chk_AllEnv","10,10");
        }
        if(param_PropertyDescription!=null){
        caller.type("ApplicationResources.tf_PropertyDescription",param_PropertyDescription);
        }
        caller.type("ApplicationResources.tf_PropertyValue",param_PropertyValue);
        caller.clickAt("ApplicationResources.btn_CreateProperty","10,10");	
    }
    /**
     *  Business component biscomp_VerifyingResourcesAfterPromoting.
     */
    public final static void biscomp_VerifyingResourcesAfterPromoting(final SeleniumTestBase caller, final String param_DSName, final String param_PropertyName, final String param_StagePromotedTo, final String param_LanguageType) throws Exception {
        //Verifying the resource are mapped when promoted a version
        caller.clickAt("ApplicationHome.lnk_ResourceTab","10,10");
        //Waiting for page loading
        caller.pause("6000");
        if(param_LanguageType.equals("Java")){
        caller.checkElementPresent("ApplicationResources.lbl_DSNamePromotedStageOverviewTab","param_DSName_PARAM:" + param_DSName,false,"");
        }
        caller.clickAt("ApplicationResources.btn_DataSourse","10,10");
        //Waiting for page loading
        caller.pause("2500");
        if(param_LanguageType.equals("Java")){
        caller.checkElementPresent("ApplicationResources.lbl_DSNamePromotedStageDBTab","param_DSName_PARAM:" + param_DSName,false,"");
        }
        caller.clickAt("ApplicationResources.lnk_PropertiesTab","10,10");
        //Waiting for page loading
        caller.pause("2500");
        caller.checkElementPresent("ApplicationResources.lbl_PropertyNamePromotedStage","param_PropertyName_PARAM:" + param_PropertyName + "_PARAM," + "param_AppStage_PARAM:" + param_StagePromotedTo,false,"");	
    }
    /**
     *  Business component biscomp_Retire.
     */
    public final static void biscomp_Retire(final SeleniumTestBase caller, final String param_Stage, final String param_VersionNumber, final String param_AppCreationMethod, final String param_NewVersion) throws Exception {
        //Retire the selected versions
        caller.clickAt("ApplicationLifeCycleManagement.ele_ddStage","10,10");
        caller.clickAt("ApplicationLifeCycleManagement.lbl_Stage","param_Stage_PARAM:" + param_Stage,"10,10");
        //Wait for page loading
        caller.pause("5000");
        if(param_AppCreationMethod.equals("Upload")){
        caller.clickAt("ApplicationLifeCycleManagement.btn_RetireUploadApp","param_VersionNumber_PARAM:" + param_VersionNumber,"10,10");
        } else if(param_AppCreationMethod.equals("Create")){
        caller.clickAt("ApplicationLifeCycleManagement.btn_RetireCreateApp","param_VersionNumber_PARAM:" + param_VersionNumber,"10,10");
        }
        //Wait for page loading
        caller.pause("2500");
        caller.clickAt("ApplicationLifeCycleManagement.chk_NoOneUsingApp","10,10");
        caller.clickAt("ApplicationLifeCycleManagement.btn_PromoteSub","param_VersionNumber_PARAM:" + param_VersionNumber,"10,10");
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
    public final static void biscomp_VerifyingDatabaseAllEnv(final SeleniumTestBase caller, final String param_DBName, final String param_DevStage, final String param_TestStage, final String param_ProStage, final String param_Username) throws Exception {
        //Verifying the databases are created in all environment
        caller.checkElementPresent("CreateNewDatabase.lnk_Database","param_AppStage_PARAM:" + param_DevStage + "_PARAM," + "param_DatabaseName_PARAM:" + param_DBName + "_PARAM," + "param_UserName_PARAM:" + param_Username,false,"");
        caller.checkElementPresent("CreateNewDatabase.lnk_Database","param_AppStage_PARAM:" + param_TestStage + "_PARAM," + "param_DatabaseName_PARAM:" + param_DBName + "_PARAM," + "param_UserName_PARAM:" + param_Username,false,"");
        caller.pause("2000");
        caller.checkElementPresent("CreateNewDatabase.lnk_Database","param_AppStage_PARAM:" + param_ProStage + "_PARAM," + "param_DatabaseName_PARAM:" + param_DBName + "_PARAM," + "param_UserName_PARAM:" + param_Username,false,"");	
    }
    /**
     *  Business component biscomp_CreateVersionUploadApp.
     */
    public final static void biscomp_CreateVersionUploadApp(final SeleniumTestBase caller, final String param_AppPath, final String param_UploadedVersion) throws Exception {
        //Create version for the upload type applications
        caller.clickAt("ApplicationHome.lnk_DeployedVersions","10,10");
        //Wait for page loading
        caller.pause("5000");
        caller.type("ApplicationDeployedVersions.btn_Browse",param_AppPath);
        /*
        SetVariable
         name=fireEvent
         type=String
         paramValue="KEY%type="+param_AppPath
        Click
         object=ApplicationDeployedVersions.btn_Browse
        FireEvent
         event=@fireEvent
         waitTime=1000
        Pause
         ms=3000
        FireEvent
         event=KEY%key=\t
         waitTime=1000
        Pause
         ms=3000
        FireEvent
         event=KEY%key=\t
         waitTime=1000
        Pause
         ms=3000
        FireEvent
         event=KEY%key=\n
         waitTime=1000
        */
        caller.pause("3000");
        caller.clickAt("ApplicationDeployedVersions.btn_Upload","10,10");
        //Wait for page loading
        caller.pause("10000");
        caller.checkElementPresent("ApplicationDeployedVersions.lbl_Version","param_VersionNumber_PARAM:" + param_UploadedVersion,false,"");	
    }
    /**
     *  Business component biscomp_VerifyingRetiredVersion.
     */
    public final static void biscomp_VerifyingRetiredVersion(final SeleniumTestBase caller, final String param_Stage, final String param_VersionNumber) throws Exception {
        //Verifying the retired version is not listed under production stage
        caller.clickAt("ApplicationHome.lnk_LifeCycleManagement","10,10");
        //Wait for page loading
        caller.pause("5000");
        if(caller.checkElementPresent("ApplicationLifeCycleManagement.ele_ddStage")){
        caller.clickAt("ApplicationLifeCycleManagement.ele_ddStage","10,10");
        //Waiting for list loading
        caller.pause("3000");
        caller.clickAt("ApplicationLifeCycleManagement.lbl_Stage","param_Stage_PARAM:" + param_Stage,"10,10");
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
        caller.clickAt("ApplicationHome.lnk_LifeCycleManagement","10,10");
        //Wait for page loading
        caller.pause("5000");
        caller.clickAt("ApplicationLifeCycleManagement.ele_ddStage","10,10");
        caller.clickAt("ApplicationLifeCycleManagement.lbl_Stage","param_Stage_PARAM:" + param_Stage,"10,10");
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
        caller.clickAt("Login.btn_SignIn","10,10");
        caller.pause("2500");
        //Verifying the error message
        caller.checkObjectProperty("Login.lbl_LoginErrorMsg","textContent",param_ErrorMsg,false,"");	
    }
    /**
     *  Business component biscomp_EditApplicationData.
     */
    public final static void biscomp_EditApplicationData(final SeleniumTestBase caller, final String param_AppImagePath, final String param_Description) throws Exception {
        //Editing application Image
        caller.clickAt("ApplicationHome.lnk_Overview","10,10");
        //Waiting for application loading
        caller.pause("5000");
        caller.clickAt("ApplicationOverview.btn_ImgEditPencil","10,10");
        caller.type("ApplicationOverview.btn_SelectFile",param_AppImagePath);
        caller.pause("3000");
        //Verifying uploaded Image
        caller.checkImagePresent("ApplicationOverview.Img_AppLogo",false,false,"");
        caller.clickAt("ApplicationOverview.btn_DescEditPencil","10,10");
        caller.type("ApplicationOverview.tf_Description",param_Description);
        caller.clickAt("ApplicationOverview.btn_SaveDesc","10,10");
        //Verifying the edited description
        caller.checkObjectProperty("ApplicationOverview.lbl_Description","textContent",param_Description,false,"");
        caller.clickAt("HomePage.btn_Home","10,10");
        //Waiting for application home page loading
        caller.pause("5000");
        //Verifying the image in home page
        caller.checkImagePresent("HomePage.Img_AppLogoSub",false,false,"");	
    }
    /**
     *  Business component biscomp_VerifyingTabVisibility.
     */
    public final static void biscomp_VerifyingTabVisibility(final SeleniumTestBase caller, final String param_OverviewTabVisibility, final String param_RepoandBuildTabVisibility, final String param_TeamTabVisibility, final String param_LifecycleManagementTabVisibility, final String param_ResourceTabVisibility, final String param_IssuesTabVisibility, final String param_LogsTabVisibility) throws Exception {
        //Verifying the visibility of the tabs
        caller.checkObjectProperty("ApplicationHome.lnk_Overview","ELEMENTPRESENT",param_OverviewTabVisibility,false,"");
        caller.checkObjectProperty("ApplicationHome.lnk_RepoandBuild","ELEMENTPRESENT",param_RepoandBuildTabVisibility,false,"");
        caller.checkObjectProperty("ApplicationHome.lnk_Team","ELEMENTPRESENT",param_TeamTabVisibility,false,"");
        caller.checkObjectProperty("ApplicationHome.lnk_LifeCycleManagement","ELEMENTPRESENT",param_LifecycleManagementTabVisibility,false,"");
        caller.checkObjectProperty("ApplicationHome.lnk_ResourceTab","ELEMENTPRESENT",param_ResourceTabVisibility,false,"");
        caller.checkObjectProperty("ApplicationHome.lnk_Issues","ELEMENTPRESENT",param_IssuesTabVisibility,false,"");
        caller.checkObjectProperty("ApplicationHome.lnk_Logs","ELEMENTPRESENT",param_LogsTabVisibility,false,"");	
    }
    /**
     *  Business component biscomp_AddingVersionTFork.
     */
    public final static void biscomp_AddingVersionTFork(final SeleniumTestBase caller, final String param_AppStage, final String param_VersionNumber, final String param_DomainName, final String param_ForkedUser, final String param_AppName) throws Exception {
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
        caller.pause("2000");
        //Verifying the fork repository label
        caller.checkElementPresent("ApplicationReposandBuilds.lbl_ForkedRepo",false,"");
        caller.checkElementPresent("ApplicationReposandBuilds.ele_ForkURL","param_DomainName_PARAM:" + param_DomainName + "_PARAM," + "param_ForkedUser_PARAM:" + param_ForkedUser + "_PARAM," + "param_AppName_PARAM:" + param_AppName,false,"");
        //Verifying the App wall notifications
        caller.checkElementPresent("Common.lbl_NotificationWallMsg","param_Message_PARAM:\"Branch \"+param_AppStage+\" created\"",false,"");
        caller.checkElementPresent("Common.lbl_NotificationWallMsg","param_Message_PARAM:param_AppStage+\" master repo built successfully\"",false,"");
        caller.checkElementPresent("Common.lbl_NotificationWallMsg","param_Message_PARAM:\"Build started for \"+param_AppStage+\" in master repo\"",false,"");	
    }
    /**
     *  Business component biscomp_VerifyingLifcycleHistory.
     */
    public final static void biscomp_VerifyingLifcycleHistory(final SeleniumTestBase caller, final String param_Versionnumber, final String param_Action, final String param_ActionFrom, final String param_ActionTo, final String param_Stage) throws Exception {
        //Verifying history is shown
        caller.clickAt("ApplicationLifeCycleManagement.ele_ddStage","10,10");
        //Waiting for list loading
        caller.pause("3000");
        caller.clickAt("ApplicationLifeCycleManagement.lbl_Stage","param_Stage_PARAM:" + param_Stage,"10,10");
        //Waiting for list loading
        caller.pause("3000");
        caller.clickAt("ApplicationLifeCycleManagement.btn_LifecycleHistory","param_VersionNumber_PARAM:" + param_Versionnumber,"10,10");
        caller.checkElementPresent("ApplicationLifeCycleManagement.lbl_LifecycleHistoryEntry","param_Action_PARAM:" + param_Action + "_PARAM," + "param_ActionFrom_PARAM:" + param_ActionFrom + "_PARAM," + "param_ActionTo_PARAM:" + param_ActionTo,false,"");	
    }
    /**
     *  Business component biscomp_LaunchingAtOverviewPage.
     */
    public final static void biscomp_LaunchingAtOverviewPage(final SeleniumTestBase caller, final String param_VersionNumber, final String param_SampleText, final int param_WaitTimeInSec, final String param_AppType) throws Exception {
        //Launching application at overview page after promoting
        caller.clickAt("ApplicationHome.lnk_Overview","10,10");
        //Wait for page loading
        caller.pause("5000");
        caller.clickAt("ApplicationOverview.btn_AcceptDeploy","param_VersionNumber_PARAM:" + param_VersionNumber,"10,10");
        //Wait for page loading
        caller.pause("10000");
        int varCount = 1;
        for(;varCount<=param_WaitTimeInSec;varCount++){
        if(caller.checkElementPresent("ApplicationOverview.lnk_Open"," param_VersionNumber_PARAM:" +param_VersionNumber)){
        caller.clickAt("ApplicationOverview.lnk_Open","param_VersionNumber_PARAM:" + param_VersionNumber,"10,10");
        int varTemp = 1;break;
        } else if(varCount==param_WaitTimeInSec){
        caller.fail("Application is not created Yet");
        } else {
        caller.pause("5000");
        caller.keyPress("HomePage.btn_AddNewApplication","F5");
        }
        caller.pause("10000");
        }
        //Verifying the sample page
        Common.biscomp_VerifyingSampleAppIndexPage(caller, param_SampleText,param_AppType);	
    }
    /**
     *  Business component biscomp_AddDBUser.
     */
    public final static void biscomp_AddDBUser(final SeleniumTestBase caller, final String param_DBUserPassword, final String param_DBUserPasswordConf, final String param_AppStage) throws Exception {
        caller.clickAt("ApplicationResources.btn_AddNewDBUser","10,10");
        String varDBUsername = "DBUser_"+caller.generateData("Alphanumeric",2);
        caller.store("keyDBUsername2","String",varDBUsername);
        caller.type("ApplicationResources.tf_DBUsername",varDBUsername);
        caller.type("ApplicationResources.tf_DBUserPassword",param_DBUserPassword);
        caller.type("ApplicationResources.tf_DBUserPasswordConf",param_DBUserPasswordConf);
        caller.clickAt("ApplicationResources.ele_ddAppStage","10,10");
        caller.clickAt("ApplicationResources.lbl_DBEnvironment","param_AppStage_PARAM:" + param_AppStage,"10,10");
        caller.clickAt("ApplicationResources.btn_CreateDBUser","10,10");	
    }
    /**
     *  Business component biscomp_DBTemplate.
     */
    public final static void biscomp_DBTemplate(final SeleniumTestBase caller, final String param_AppStage) throws Exception {
        //Creating a Database Template
        caller.clickAt("ApplicationResources.btn_AddNewTemplate","10,10");
        String varDBTemplateName = "DBTemplate_"+caller.generateData("Alphanumeric",2);
        caller.store("keyDBTemplateName2","String",varDBTemplateName);
        caller.type("ApplicationResources.tf_DBTemplateName",varDBTemplateName);
        caller.clickAt("ApplicationResources.ele_ddAppStage","10,10");
        caller.clickAt("ApplicationResources.lbl_DBEnvironment","param_AppStage_PARAM:" + param_AppStage,"10,10");
        caller.clickAt("ApplicationResources.btn_CreateTemplate","10,10");	
    }
    /**
     *  Business component biscomp_UpdatingDeletingDB.
     */
    public final static void biscomp_UpdatingDeletingDB(final SeleniumTestBase caller, final String param_DBName, final String param_Appstage, final String param_DBUsername, final String param_DBTemplate) throws Exception {
        //Updating the database
        caller.clickAt("ApplicationResources.lnk_DatabaseName","param_Appstage_PARAM:" + param_Appstage + "_PARAM," + "param_DBName_PARAM:" + param_DBName,"10,10");
        //Wait for page loading
        caller.pause("3000");
        caller.clickAt("ApplicationResources.ele_ddDBUsername","10,10");
        caller.clickAt("ApplicationResources.lbl_DBUsername","param_DBUsername_PARAM:" + param_DBUsername,"10,10");
        caller.clickAt("ApplicationResources.ele_ddDBTemplate","10,10");
        caller.clickAt("ApplicationResources.lbl_DBTemplate","param_DBTemplate_PARAM:" + param_DBTemplate,"10,10");
        caller.clickAt("ApplicationResources.btn_AttachUser","10,10");
        caller.clickAt("ApplicationResources.lnk_DatabaseName","param_Appstage_PARAM:" + param_Appstage + "_PARAM," + "param_DBName_PARAM:" + param_DBName,"10,10");
        //Wait for page loading
        caller.pause("3000");
        //Verifying the attached user
        caller.checkElementPresent("ApplicationResources.lbl_AttachedDBUsername","param_DBUsername_PARAM:" + param_DBUsername,false,"");
        //Deleting the Attached DB user
        caller.clickAt("ApplicationResources.btn_DeleteAttachedDBUser","param_DBUsername_PARAM:" + param_DBUsername,"10,10");
        //Verifying the DB user is deleted
        caller.checkObjectProperty("ApplicationResources.lbl_AttachedDBUsername","param_DBUsername_PARAM:" + param_DBUsername,"ELEMENTPRESENT","false",false,"");
        //Deletign the Database
        caller.clickAt("ApplicationResources.btn_DeleteDB","10,10");
        caller.clickAt("ApplicationResources.btn_OK","10,10");
        //Verifying the Deleted DB is not displayed
        caller.checkObjectProperty("ApplicationResources.lnk_DatabaseName","param_Appstage_PARAM:" + param_Appstage + "_PARAM," + "param_DBName_PARAM:" + param_DBName,"ELEMENTPRESENT","false",false,"");	
    }
    /**
     *  Business component biscomp_DeleteDBUser.
     */
    public final static void biscomp_DeleteDBUser(final SeleniumTestBase caller, final String param_Appstage, final String param_DBUsername) throws Exception {
        //Deleting Database User
        caller.clickAt("ApplicationHome.lnk_ResourceTab","10,10");
        caller.clickAt("ApplicationResources.lnk_Databases","10,10");
        //Wait for  page loading
        caller.pause("3000");
        caller.clickAt("ApplicationResources.lnk_DBUsername","param_Appstage_PARAM:" + param_Appstage + "_PARAM," + "param_DBUsername_PARAM:" + param_DBUsername,"10,10");
        caller.clickAt("ApplicationResources.btn_DeleteDBUser","10,10");
        caller.clickAt("ApplicationResources.btn_OK","10,10");
        //Verifying the deleted user is not displayed
        caller.checkObjectProperty("ApplicationResources.lnk_DBUsername","param_Appstage_PARAM:" + param_Appstage + "_PARAM," + "param_DBUsername_PARAM:" + param_DBUsername,"ELEMENTPRESENT","false",false,"");	
    }
    /**
     *  Business component biscomp_UpdatingDeletingTemplate.
     */
    public final static void biscomp_UpdatingDeletingTemplate(final SeleniumTestBase caller, final String param_Appstage, final String param_DBTemplateName) throws Exception {
        //Updating the Database Template
        caller.clickAt("ApplicationHome.lnk_ResourceTab","10,10");
        //Wait for page loading
        caller.pause("3000");
        caller.clickAt("ApplicationResources.lnk_Databases","10,10");
        caller.clickAt("ApplicationResources.lnk_DBTemplateName","param_Appstage_PARAM:" + param_Appstage + "_PARAM," + "param_DBTemplateName_PARAM:" + param_DBTemplateName,"10,10");
        caller.click("CreateNewDBTemplate.chk_InsertPrivilege");
        caller.click("CreateNewDBTemplate.chk_CreatePriv");
        caller.click("CreateNewDBTemplate.chk_DropPriv");
        caller.click("CreateNewDBTemplate.chk_LockTablesPriv");
        caller.clickAt("ApplicationResources.btn_DBTempSaveChanges","10,10");
        caller.clickAt("ApplicationResources.lnk_DBTemplateName","param_Appstage_PARAM:" + param_Appstage + "_PARAM," + "param_DBTemplateName_PARAM:" + param_DBTemplateName,"10,10");
        caller.checkObjectProperty("CreateNewDBTemplate.chk_InsertPrivilege","ATTR:Value","off",false,"");
        caller.checkObjectProperty("CreateNewDBTemplate.chk_CreatePriv","ATTR:Value","off",false,"");
        caller.checkObjectProperty("CreateNewDBTemplate.chk_DropPriv","ATTR:Value","off",false,"");
        caller.checkObjectProperty("CreateNewDBTemplate.chk_LockTablesPriv","ATTR:Value","off",false,"");
        caller.clickAt("ApplicationResources.btn_DeleteDBTemplate","10,10");
        caller.clickAt("ApplicationResources.btn_OK","10,10");
        caller.checkObjectProperty("ApplicationResources.lnk_DBTemplateName","param_Appstage_PARAM:" + param_Appstage + "_PARAM," + "param_DBTemplateName_PARAM:" + param_DBTemplateName,"ELEMENTPRESENT","false",false,"");	
    }
    /**
     *  Business component biscomp_MinimizeAppWall.
     */
    /**
     *  Business component biscomp_MinimizeAppWall.
     */
    public final static void biscomp_MinimizeAppWall(final SeleniumTestBase caller) throws Exception {
        //verify App wall is not minimize
        String varDisplayBefore = caller.getStringProperty("Common.ele_SlideBar","style");
        if(!varDisplayBefore.contains("block")){
        caller.writeToReport("notifiacation is   not minimized");
        }
        //click on minimize button
        caller.click("Common.btn_MinimizeButton");
        String varDisplayAfter = caller.getStringProperty("Common.ele_SlideBar","style");
        if(!varDisplayAfter.contains("none")){
        caller.writeToReport("notifiacation is  minimized");
        }	
    }
}
