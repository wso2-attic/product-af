package com.complete_scenario2;

import java.util.HashMap;
import java.util.List;

import com.virtusa.isq.vtaf.aspects.VTAFRecoveryMethods;
import com.virtusa.isq.vtaf.runtime.SeleniumTestBase;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import com.virtusa.isq.vtaf.runtime.VTAFTestListener;


/**
 *  Class Scenario2 implements corresponding test suite
 *  Each test case is a test method in this class.
 */

@Listeners (VTAFTestListener.class)
public class Scenario2 extends SeleniumTestBase {



    /**
     * Data provider for Test case initialization.
     * @return data table
     */
    @DataProvider(name = "initialization")
    public Object[][] dataTable_initialization() {     	
    	return this.getDataTable("initializeTenantLogin");
    }

    /**
     * Data driven test case initialization.
     *
     * @throws Exception the exception
     */
    @VTAFRecoveryMethods(onerrorMethods = {}, recoveryMethods = {}) 
    @Test (dataProvider = "initialization")
    public final void initialization(final String initializeTenantLogin_TenantAdminName, final String initializeTenantLogin_TenantAdminPassword, final String initializeTenantLogin_TenantDomain, final String initializeTenantLogin_AppFactoryUserCommonPassword, final String initializeTenantLogin_mainURL, final String initializeTenantLogin_defaultPassword) throws Exception {	
    	store("AFTenantAdmin_Name","String",initializeTenantLogin_TenantAdminName);
    	store("AFTenantAdmin_Password","String",initializeTenantLogin_TenantAdminPassword);
    	store("AFTenantDomain","String",initializeTenantLogin_TenantDomain);
    	store("AFUserCommonPassword","String",initializeTenantLogin_AppFactoryUserCommonPassword);
    	store("AFMainURL","String",initializeTenantLogin_mainURL);
    	store("AFDefaultPassword","String",initializeTenantLogin_defaultPassword);
    } 
    	

    /**
     * Data provider for Test case tc_AppCreation.
     * @return data table
     */
    @DataProvider(name = "tc_AppCreation")
    public Object[][] dataTable_tc_AppCreation() {     	
    	return this.getDataTable("dt_Scenario2");
    }

    /**
     * Data driven test case tc_AppCreation.
     *
     * @throws Exception the exception
     */
    @VTAFRecoveryMethods(onerrorMethods = {}, recoveryMethods = {}) 
    @Test (dataProvider = "tc_AppCreation")
    public final void tc_AppCreation(final boolean dt_Scenario2_IsExecuting, final boolean dt_Scenario2_IsExecuting_AppCreation, final boolean dt_Scenario2_IsExecuting_DevFunctions, final boolean dt_Scenario2_IsExecuting_AppOwnerQaFunctions, final boolean dt_Scenario2_IsExecutingResourceCURD, final boolean dt_Scenario2_IsExecutingUploadApp, final String dt_Scenario2_appType, final String dt_Scenario2_adminUsername, final String dt_Scenario2_domainName, final String dt_Scenario2_defaultPassword, final String dt_Scenario2_loginErrorMsg, final String dt_Scenario2_dbUserPwdErrorMsg, final String dt_Scenario2_userListMain, final String dt_Scenario2_userListUploadApp, final String dt_Scenario2_userListEdit, final String dt_Scenario2_userListDelete, final String dt_Scenario2_usernames, final String dt_Scenario2_userInitials, final String dt_Scenario2_wrongPassword, final String dt_Scenario2_wrongUsername, final String dt_Scenario2_UsernameErrorMsg, final String dt_Scenario2_PasswordErrorMsg, final String dt_Scenario2_adminPassword, final String dt_Scenario2_appName, final String dt_Scenario2_appKey, final String dt_Scenario2_appDescription, final String dt_Scenario2_editDescription, final String dt_Scenario2_appImagePath, final String dt_Scenario2_editAppImagePath, final String dt_Scenario2_appPath, final String dt_Scenario2_appCreationMethod, final String dt_Scenario2_appCreationMsg, final String dt_Scenario2_gitMsg, final String dt_Scenario2_jenkinsMsg, final String dt_Scenario2_issueTrackerMsg, final String dt_Scenario2_cloudMsg, final String dt_Scenario2_sameAppKeyErrorMsg, final String dt_Scenario2_addMemberMsg, final String dt_Scenario2_removeMemberMsg, final String dt_Scenario2_wrongDateErrorMsg, final String dt_Scenario2_promoteDevTestMsg, final String dt_Scenario2_DeployTestMsg, final String dt_Scenario2_appStageDev, final String dt_Scenario2_appStageTest, final String dt_Scenario2_appStagePro, final String dt_Scenario2_actionPromote, final String dt_Scenario2_actionDemote, final String dt_Scenario2_OverviewTabVisibilityDev, final String dt_Scenario2_RepoandBuildTabVisibilityDev, final String dt_Scenario2_TeamTabVisibilityDev, final String dt_Scenario2_databaseTabVisibilityDev, final String dt_Scenario2_LifecycleManagementTabVisibilityDev, final String dt_Scenario2_ResourceTabVisibilityDev, final String dt_Scenario2_IssuesTabVisibilityDev, final String dt_Scenario2_LogsTabVisibilityDev, final String dt_Scenario2_buildStatus, final String dt_Scenario2_deployStatus, final String dt_Scenario2_versionnumber, final String dt_Scenario2_newVersionNumber, final String dt_Scenario2_buildNumber, final String dt_Scenario2_buildNumberOne, final String dt_Scenario2_buildNumberTwo, final String dt_Scenario2_buildNumberThree, final String dt_Scenario2_deployNumberTwo, final String dt_Scenario2_deployNumberOne, final String dt_Scenario2_dbName, final String dt_Scenario2_dbEnvironment, final String dt_Scenario2_dbAllEnvYes, final String dt_Scenario2_dbAllEnvNo, final String dt_Scenario2_dsName, final String dt_Scenario2_dbDriver, final String dt_Scenario2_dsEnv, final String dt_Scenario2_dbURL, final String dt_Scenario2_dbUsername, final String dt_Scenario2_dbPassword, final String dt_Scenario2_dsDesc, final String dt_Scenario2_dsPassword, final String dt_Scenario2_internalAPIName, final String dt_Scenario2_internalAPIVersion, final String dt_Scenario2_exAPIName, final String dt_Scenario2_authAPI, final String dt_Scenario2_exAPIURL, final String dt_Scenario2_propName, final String dt_Scenario2_proDesc, final String dt_Scenario2_propRegEnv, final String dt_Scenario2_proValue, final String dt_Scenario2_issueSummary, final String dt_Scenario2_issueDesc, final String dt_Scenario2_versionIssueRelatedTo, final String dt_Scenario2_issueType, final String dt_Scenario2_issueTypeNew, final String dt_Scenario2_issueTypeBug, final String dt_Scenario2_issuePriority, final String dt_Scenario2_issuePriorityNew, final String dt_Scenario2_issuePriorityNormal, final String dt_Scenario2_issueAsignee, final String dt_Scenario2_issueSeverity, final String dt_Scenario2_versionToDemote, final String dt_Scenario2_demoteComment, final String dt_Scenario2_issueStatusInitial, final String dt_Scenario2_issueStatusNew, final String dt_Scenario2_issueStatusResolved, final String dt_Scenario2_issueStatusFinal, final String dt_Scenario2_issueStatusClosed, final String dt_Scenario2_subDomain, final String dt_Scenario2_serviceName, final String dt_Scenario2_domain, final String dt_Scenario2_lawPauseLevel, final String dt_Scenario2_mediumPauseLevel, final String dt_Scenario2_highPauseLevel, final String dt_Scenario2_sampleText, final int dt_Scenario2_waitTimeForAppLoadingInSec, final String dt_Scenario2_appPathNew, final String dt_Scenario2_newUploadVersion, final String dt_Scenario2_dbTempPrivUnTicked, final String dt_Scenario2_dbTempPrivTicked, final String dt_Scenario2_dbNOtVisible, final String dt_Scenario2_dbVisible, final String dt_Scenario2_versionVisibilityTrue, final String dt_Scenario2_versionVisibilityFalse, final String dt_Scenario2_ETADisability, final String dt_Scenario2_StageVisibilityTrue, final String dt_Scenario2_StageVisibilityFalse, final String dt_Scenario2_addNewBtnVisibilityTrue, final String dt_Scenario2_addNewBtnVisibilityFalse, final String dt_Scenario2_OverviewTabVisibilityQA, final String dt_Scenario2_RepoandBuildTabVisibilityQA, final String dt_Scenario2_TeamTabVisibilityQA, final String dt_Scenario2_LifecycleManagementTabVisibilityQA, final String dt_Scenario2_ResourceTabVisibilityQA, final String dt_Scenario2_IssuesTabVisibilityQA, final String dt_Scenario2_databaseTabVisibilityQA, final String dt_Scenario2_LogsTabVisibilityQA, final String dt_Scenario2_OverviewTabVisibilityDEVOPS, final String dt_Scenario2_RepoandBuildTabVisibilityDEVOPS, final String dt_Scenario2_TeamTabVisibilityDEVOPS, final String dt_Scenario2_LifecycleManagementTabVisibilityDEVOPS, final String dt_Scenario2_ResourceTabVisibilityDEVOPS, final String dt_Scenario2_IssuesTabVisibilityDEVOPS, final String dt_Scenario2_databaseTabVisibilityDEVOPS, final String dt_Scenario2_LogsTabVisibilityDEVOPS, final String dt_Scenario2_ETASaveTestingStageDev, final String dt_Scenario2_appLogoSubPath, final String dt_Scenario2_appLogoMainPath, final String dt_Scenario2_prodKeyVisibilityDev, final String dt_Scenario2_prodKeyVisibilityQa, final String dt_Scenario2_prodKeyVisibilityDevOps, final String dt_Scenario2_prodKeyVisibilityAppOwner, final String dt_Scenario2_SandKeyVisibilityDev, final String dt_Scenario2_SandKeyVisibilityQa, final String dt_Scenario2_SandKeyVisibilityDevOps, final String dt_Scenario2_SandKeyVisibilityAppOwner, final String dt_Scenario2_DevDBVisibilityDevOps, final String dt_Scenario2_DevDBUserVisibilityDevOps, final String dt_Scenario2_DevDBTempVisibilityDevOps, final String dt_Scenario2_passWord) throws Exception {	
    	//Checking the executing status of the application type
    	if(dt_Scenario2_IsExecuting){
    	//Checking the executing AppCreation
    	if(dt_Scenario2_IsExecuting_AppCreation){
    	String varMainURL = retrieveString("AFMainURL");
    	String varTenantAdmin_Password = retrieveString("AFTenantAdmin_Password");
    	String varUserName = retrieveString(dt_Scenario2_adminUsername);
    	String varPassword = retrieveString("AFDefaultPassword");
    	String varUserCommonPassword = retrieveString("AFUserCommonPassword");
    	String varDomainName = retrieveString("AFTenantDomain");
    	String varDomain = "@"+varDomainName+".com";
    	Common.biscomp_LoginInvalid(this, varUserName+varDomain,dt_Scenario2_wrongPassword,dt_Scenario2_loginErrorMsg,varMainURL);
    	Common.biscomp_Login(this, varUserName+varDomain,varTenantAdmin_Password,varMainURL);
    	//Assigning users names
    	Common.biscomp_AssigningUsernames(this, dt_Scenario2_userListMain,dt_Scenario2_userInitials);
    	String varUserList = retrieveString("keyUserList");
    	//Importing users
    	Common.biscomp_ImportMembers(this, varUserList,varUserCommonPassword,varUserCommonPassword);
    	//Wait for users to be loaded
    	pause("5000");
    	//Assigning roles to the imported users
    	Common.biscomp_AssignUserRoles(this, varUserList);
    	//Assigning user names, importing and changing the roles
    	Common.biscomp_AssigningUsernames(this, dt_Scenario2_userListEdit,dt_Scenario2_userInitials);
    	String varUserList_EditRole = retrieveString("keyUserList");
    	Common.biscomp_ImportMembers(this, varUserList_EditRole,varUserCommonPassword,varUserCommonPassword);
    	//Assigning a role
    	Common.biscomp_AssignUserRoles(this, varUserList_EditRole);
    	Common.biscomp_ChangingUserRole(this, varUserList_EditRole);
    	//Assigning user name for removing user
    	Common.biscomp_AssigningUsernames(this, dt_Scenario2_userListDelete,dt_Scenario2_userInitials);
    	String varUserList_RemoveUser = retrieveString("keyUserList");
    	Common.biscomp_ImportMembers(this, varUserList_RemoveUser,varUserCommonPassword,varUserCommonPassword);
    	//removing user
    	Common.biscomp_RemoveUser(this, varUserList_RemoveUser);
    	//Importing user with specail includes in the username
    	Common.biscomp_ImportMembers(this, dt_Scenario2_wrongUsername,varUserCommonPassword,varUserCommonPassword);
    	//wating for error message
    	pause("2500");
    	//Verifying the error message
    	checkObjectProperty("ImportMembers.lbl_ErrorMsg","textContent",dt_Scenario2_UsernameErrorMsg,false,"null");
    	clickAt("HomePage.btn_Home","0,0");
    	//Waiting for page loading
    	pause("2000");
    	//Importing user with different passwords
    	Common.biscomp_ImportMembers(this, dt_Scenario2_userListEdit,varUserCommonPassword,dt_Scenario2_wrongPassword);
    	//wait for error message
    	checkObjectProperty("ImportMembers.lbl_PwdErrorMsg","textContent",dt_Scenario2_PasswordErrorMsg,false,"null");
    	//Tenantt admin login out
    	Common.biscomp_Logout(this);
    	//App Owner login in to system
    	String varAppOwnerUsername = retrieveString("AF_AppOwner");
    	Common.biscomp_Login(this, varAppOwnerUsername+varDomain,varUserCommonPassword,varMainURL);
    	//Wait for page loading
    	pause("5000");
    	Common.biscomp_CreateApplication(this, dt_Scenario2_appName,dt_Scenario2_appKey,dt_Scenario2_appDescription,dt_Scenario2_appImagePath,dt_Scenario2_appType,dt_Scenario2_appPath,dt_Scenario2_appCreationMethod);
    	String varAppName = retrieveString("keyAppName");
    	String varAppKey = retrieveString("keyAppKey");
    	//Wait for application loading
    	pause("3000");
    	//Verifying created application is listed in the home page
    	Common.biscomp_VerifyingCreatedApplication(this, varAppName,dt_Scenario2_appType,dt_Scenario2_waitTimeForAppLoadingInSec);
    	//Refreshing the page for wall notification
    	keyPress("HomePage.btn_Home","F5");
    	//Wait for wall notification laoding
    	pause("3000");
    	//Verifying the posts on notification wall
    	Common.biscomp_VerifyingNotificationWallPost(this, dt_Scenario2_appCreationMsg,dt_Scenario2_gitMsg,dt_Scenario2_jenkinsMsg,dt_Scenario2_issueTrackerMsg,dt_Scenario2_cloudMsg,varAppName,varAppKey);
    	Common.biscomp_CreateAppWithSameAppKey(this, varAppKey,dt_Scenario2_sameAppKeyErrorMsg);
    	Common.biscomp_SelectApp(this, varAppName);
    	//Wait for page loading
    	pause("4000");
    	Common.biscomp_AddMembers(this, varUserList,dt_Scenario2_addMemberMsg,varAppOwnerUsername);
    	Common.biscomp_AddMembers(this, varUserList_EditRole,dt_Scenario2_addMemberMsg,varAppOwnerUsername);
    	//Removing a user from the application
    	Common.biscomp_RemoveUserFromApp(this, varUserList_EditRole,dt_Scenario2_removeMemberMsg);
    	//Editing the application image and description
    	Common.biscomp_EditApplicationData(this, dt_Scenario2_editAppImagePath,dt_Scenario2_editDescription,dt_Scenario2_appLogoSubPath,dt_Scenario2_appLogoMainPath);
    	//AppOwner loginOut
    	Common.biscomp_Logout(this);
    	} else {
    	writeToReport("This Sections is not tested in this iteration");
    	}
    	} else if(!dt_Scenario2_IsExecuting){
    	String AppType = dt_Scenario2_appType;
    	writeToReport(AppType+" type is not tested in this iteration");
    	}
    } 
    	

    /**
     * Data provider for Test case tc_DevFunctions.
     * @return data table
     */
    @DataProvider(name = "tc_DevFunctions")
    public Object[][] dataTable_tc_DevFunctions() {     	
    	return this.getDataTable("dt_Scenario2");
    }

    /**
     * Data driven test case tc_DevFunctions.
     *
     * @throws Exception the exception
     */
    @VTAFRecoveryMethods(onerrorMethods = {}, recoveryMethods = {}) 
    @Test (dataProvider = "tc_DevFunctions")
    public final void tc_DevFunctions(final boolean dt_Scenario2_IsExecuting, final boolean dt_Scenario2_IsExecuting_AppCreation, final boolean dt_Scenario2_IsExecuting_DevFunctions, final boolean dt_Scenario2_IsExecuting_AppOwnerQaFunctions, final boolean dt_Scenario2_IsExecutingResourceCURD, final boolean dt_Scenario2_IsExecutingUploadApp, final String dt_Scenario2_appType, final String dt_Scenario2_adminUsername, final String dt_Scenario2_domainName, final String dt_Scenario2_defaultPassword, final String dt_Scenario2_loginErrorMsg, final String dt_Scenario2_dbUserPwdErrorMsg, final String dt_Scenario2_userListMain, final String dt_Scenario2_userListUploadApp, final String dt_Scenario2_userListEdit, final String dt_Scenario2_userListDelete, final String dt_Scenario2_usernames, final String dt_Scenario2_userInitials, final String dt_Scenario2_wrongPassword, final String dt_Scenario2_wrongUsername, final String dt_Scenario2_UsernameErrorMsg, final String dt_Scenario2_PasswordErrorMsg, final String dt_Scenario2_adminPassword, final String dt_Scenario2_appName, final String dt_Scenario2_appKey, final String dt_Scenario2_appDescription, final String dt_Scenario2_editDescription, final String dt_Scenario2_appImagePath, final String dt_Scenario2_editAppImagePath, final String dt_Scenario2_appPath, final String dt_Scenario2_appCreationMethod, final String dt_Scenario2_appCreationMsg, final String dt_Scenario2_gitMsg, final String dt_Scenario2_jenkinsMsg, final String dt_Scenario2_issueTrackerMsg, final String dt_Scenario2_cloudMsg, final String dt_Scenario2_sameAppKeyErrorMsg, final String dt_Scenario2_addMemberMsg, final String dt_Scenario2_removeMemberMsg, final String dt_Scenario2_wrongDateErrorMsg, final String dt_Scenario2_promoteDevTestMsg, final String dt_Scenario2_DeployTestMsg, final String dt_Scenario2_appStageDev, final String dt_Scenario2_appStageTest, final String dt_Scenario2_appStagePro, final String dt_Scenario2_actionPromote, final String dt_Scenario2_actionDemote, final String dt_Scenario2_OverviewTabVisibilityDev, final String dt_Scenario2_RepoandBuildTabVisibilityDev, final String dt_Scenario2_TeamTabVisibilityDev, final String dt_Scenario2_databaseTabVisibilityDev, final String dt_Scenario2_LifecycleManagementTabVisibilityDev, final String dt_Scenario2_ResourceTabVisibilityDev, final String dt_Scenario2_IssuesTabVisibilityDev, final String dt_Scenario2_LogsTabVisibilityDev, final String dt_Scenario2_buildStatus, final String dt_Scenario2_deployStatus, final String dt_Scenario2_versionnumber, final String dt_Scenario2_newVersionNumber, final String dt_Scenario2_buildNumber, final String dt_Scenario2_buildNumberOne, final String dt_Scenario2_buildNumberTwo, final String dt_Scenario2_buildNumberThree, final String dt_Scenario2_deployNumberTwo, final String dt_Scenario2_deployNumberOne, final String dt_Scenario2_dbName, final String dt_Scenario2_dbEnvironment, final String dt_Scenario2_dbAllEnvYes, final String dt_Scenario2_dbAllEnvNo, final String dt_Scenario2_dsName, final String dt_Scenario2_dbDriver, final String dt_Scenario2_dsEnv, final String dt_Scenario2_dbURL, final String dt_Scenario2_dbUsername, final String dt_Scenario2_dbPassword, final String dt_Scenario2_dsDesc, final String dt_Scenario2_dsPassword, final String dt_Scenario2_internalAPIName, final String dt_Scenario2_internalAPIVersion, final String dt_Scenario2_exAPIName, final String dt_Scenario2_authAPI, final String dt_Scenario2_exAPIURL, final String dt_Scenario2_propName, final String dt_Scenario2_proDesc, final String dt_Scenario2_propRegEnv, final String dt_Scenario2_proValue, final String dt_Scenario2_issueSummary, final String dt_Scenario2_issueDesc, final String dt_Scenario2_versionIssueRelatedTo, final String dt_Scenario2_issueType, final String dt_Scenario2_issueTypeNew, final String dt_Scenario2_issueTypeBug, final String dt_Scenario2_issuePriority, final String dt_Scenario2_issuePriorityNew, final String dt_Scenario2_issuePriorityNormal, final String dt_Scenario2_issueAsignee, final String dt_Scenario2_issueSeverity, final String dt_Scenario2_versionToDemote, final String dt_Scenario2_demoteComment, final String dt_Scenario2_issueStatusInitial, final String dt_Scenario2_issueStatusNew, final String dt_Scenario2_issueStatusResolved, final String dt_Scenario2_issueStatusFinal, final String dt_Scenario2_issueStatusClosed, final String dt_Scenario2_subDomain, final String dt_Scenario2_serviceName, final String dt_Scenario2_domain, final String dt_Scenario2_lawPauseLevel, final String dt_Scenario2_mediumPauseLevel, final String dt_Scenario2_highPauseLevel, final String dt_Scenario2_sampleText, final int dt_Scenario2_waitTimeForAppLoadingInSec, final String dt_Scenario2_appPathNew, final String dt_Scenario2_newUploadVersion, final String dt_Scenario2_dbTempPrivUnTicked, final String dt_Scenario2_dbTempPrivTicked, final String dt_Scenario2_dbNOtVisible, final String dt_Scenario2_dbVisible, final String dt_Scenario2_versionVisibilityTrue, final String dt_Scenario2_versionVisibilityFalse, final String dt_Scenario2_ETADisability, final String dt_Scenario2_StageVisibilityTrue, final String dt_Scenario2_StageVisibilityFalse, final String dt_Scenario2_addNewBtnVisibilityTrue, final String dt_Scenario2_addNewBtnVisibilityFalse, final String dt_Scenario2_OverviewTabVisibilityQA, final String dt_Scenario2_RepoandBuildTabVisibilityQA, final String dt_Scenario2_TeamTabVisibilityQA, final String dt_Scenario2_LifecycleManagementTabVisibilityQA, final String dt_Scenario2_ResourceTabVisibilityQA, final String dt_Scenario2_IssuesTabVisibilityQA, final String dt_Scenario2_databaseTabVisibilityQA, final String dt_Scenario2_LogsTabVisibilityQA, final String dt_Scenario2_OverviewTabVisibilityDEVOPS, final String dt_Scenario2_RepoandBuildTabVisibilityDEVOPS, final String dt_Scenario2_TeamTabVisibilityDEVOPS, final String dt_Scenario2_LifecycleManagementTabVisibilityDEVOPS, final String dt_Scenario2_ResourceTabVisibilityDEVOPS, final String dt_Scenario2_IssuesTabVisibilityDEVOPS, final String dt_Scenario2_databaseTabVisibilityDEVOPS, final String dt_Scenario2_LogsTabVisibilityDEVOPS, final String dt_Scenario2_ETASaveTestingStageDev, final String dt_Scenario2_appLogoSubPath, final String dt_Scenario2_appLogoMainPath, final String dt_Scenario2_prodKeyVisibilityDev, final String dt_Scenario2_prodKeyVisibilityQa, final String dt_Scenario2_prodKeyVisibilityDevOps, final String dt_Scenario2_prodKeyVisibilityAppOwner, final String dt_Scenario2_SandKeyVisibilityDev, final String dt_Scenario2_SandKeyVisibilityQa, final String dt_Scenario2_SandKeyVisibilityDevOps, final String dt_Scenario2_SandKeyVisibilityAppOwner, final String dt_Scenario2_DevDBVisibilityDevOps, final String dt_Scenario2_DevDBUserVisibilityDevOps, final String dt_Scenario2_DevDBTempVisibilityDevOps, final String dt_Scenario2_passWord) throws Exception {	
    	//Checking the executing status of the application type
    	if(dt_Scenario2_IsExecuting){
    	if(dt_Scenario2_IsExecuting_DevFunctions){
    	//Retrieving values
    	String varDevUsername = retrieveString("AF_DEV");
    	String varPassword = retrieveString("AFDefaultPassword");
    	String varDomainName = retrieveString("AFTenantDomain");
    	String varUserCommonPassword = retrieveString("AFUserCommonPassword");
    	String varTenantAdmin_Password = retrieveString("AFTenantAdmin_Password");
    	String varDomain = "@"+varDomainName+".com";
    	String varAppName = retrieveString("keyAppName");
    	String varAppKey = retrieveString("keyAppKey");
    	String varMainURL = retrieveString("AFMainURL");
    	String varTenantAdminName = retrieveString("AFTenantAdmin_Name");
    	//Developer user login
    	Common.biscomp_Login(this, varDevUsername+varDomain,varUserCommonPassword,varMainURL);
    	//Verifying the created application is visible
    	Common.biscomp_VerifyingCreatedApplication(this, varAppName,dt_Scenario2_appType,dt_Scenario2_waitTimeForAppLoadingInSec);
    	//Verify the 'Add New Application' button is not visible
    	checkObjectProperty("HomePage.btn_AddNewApplication","ELEMENTPRESENT",dt_Scenario2_addNewBtnVisibilityFalse,false,"null");
    	Common.biscomp_SelectApp(this, varAppName);
    	//Verifying the Application tab visibility
    	Common.biscomp_VerifyingTabVisibility(this, dt_Scenario2_OverviewTabVisibilityDev,dt_Scenario2_RepoandBuildTabVisibilityDev,dt_Scenario2_TeamTabVisibilityDev,dt_Scenario2_LifecycleManagementTabVisibilityDev,dt_Scenario2_ResourceTabVisibilityDev,dt_Scenario2_IssuesTabVisibilityDev,dt_Scenario2_LogsTabVisibilityDev,dt_Scenario2_databaseTabVisibilityDev);
    	//Verifying the Fork functionality
    	clickAt("ApplicationHome.lnk_RepoandBuild","10,10");
    	//Wait for page loading
    	pause("5000");
    	clickAt("ApplicationReposandBuilds.btn_Fork","10,10");
    	//Wait for page loading
    	pause("10000");
    	keyPress("ApplicationReposandBuilds.btn_ForkBuild","F5");
    	pause("2000");
    	checkElementPresent("ApplicationReposandBuilds.lbl_ForkedRepo",false,"null");
    	Common.biscomp_CreateBranch(this, dt_Scenario2_appStageDev,dt_Scenario2_versionnumber,dt_Scenario2_newVersionNumber);
    	String BranchCreatedMsg = "Branch "+dt_Scenario2_newVersionNumber+" creation completed";
    	//Wait for page loading
    	pause("5000");
    	checkElementPresent("Common.lbl_NotificationWallMsg","param_Message_PARAM:" + BranchCreatedMsg,false,"null");
    	String BranchCreatedSuccessMsg = dt_Scenario2_newVersionNumber+" of master repo built successfully by "+varDevUsername;
    	//Wait for page loading
    	pause("5000");
    	checkElementPresent("Common.lbl_NotificationWallMsg","param_Message_PARAM:" + BranchCreatedSuccessMsg,false,"null");
    	String BuildStartedMsg = "Build started for "+dt_Scenario2_newVersionNumber+" in master repo by "+varDevUsername;
    	//Wait for page loading
    	pause("5000");
    	checkElementPresent("Common.lbl_NotificationWallMsg","param_Message_PARAM:" + BuildStartedMsg,false,"null");
    	//Verifying the build and deploy status
    	Common.biscomp_VerifyingBuildDeploystatus(this, dt_Scenario2_appStageDev,dt_Scenario2_buildStatus,dt_Scenario2_deployStatus,dt_Scenario2_versionnumber);
    	//Wait for page loading
    	pause("5000");
    	checkElementPresent("Common.lbl_NotificationWallMsg","param_Message_PARAM:" + dt_Scenario2_newVersionNumber+" of master repo built successfully by "+varDevUsername,false,"null");
    	String buildMsg = "Build started for "+dt_Scenario2_newVersionNumber+" in master repo by "+varDevUsername;
    	//Wait for page loading
    	pause("5000");
    	checkElementPresent("Common.lbl_NotificationWallMsg","param_Message_PARAM:" + buildMsg,false,"null");
    	//Add New version to the fork
    	Common.biscomp_AddingVersionTFork(this, dt_Scenario2_appStageDev,dt_Scenario2_newVersionNumber,varDomainName,varDevUsername,varAppKey);
    	//Wait for build status update
    	pause("10000");
    	keyPress("HomePage.btn_Home","F5");
    	//Wait for page loading
    	pause("3000");
    	//Build forked repo
    	clickAt("ApplicationReposandBuilds.btn_ForkBuild","10,10");
    	//Wait for build status update
    	pause("15000");
    	keyPress("HomePage.btn_Home","F5");
    	//Wait for page loading
    	pause("3000");
    	String buildStatusMsg = "Build "+dt_Scenario2_buildNumber+" "+dt_Scenario2_buildStatus;
    	checkObjectProperty("ApplicationReposandBuilds.lbl_ForkBuildStatus","textContent",buildStatusMsg,false,"null");
    	//Verifying the notification wall Messages for forked repo built
    	String forkBuildStartedMsg = "Build started for "+dt_Scenario2_newVersionNumber+" in forked repo by "+varDevUsername;
    	//Wait for page loading
    	pause("3000");
    	checkElementPresent("Common.lbl_NotificationWallMsg","param_Message_PARAM:" + forkBuildStartedMsg,false,"null");
    	String forkedBuiltSuccessMsg = dt_Scenario2_newVersionNumber+" of forked repo built successfully by "+varDevUsername;
    	checkElementPresent("Common.lbl_NotificationWallMsg","param_Message_PARAM:" + forkedBuiltSuccessMsg,false,"null");
    	//Master repo Building and deploying
    	Common.biscomp_BuildButtonFunction(this, dt_Scenario2_appStageDev,dt_Scenario2_newVersionNumber,dt_Scenario2_buildNumberTwo,dt_Scenario2_buildStatus,dt_Scenario2_deployNumberTwo);
    	//Verify the wall notofocation for build and deploy
    	checkElementPresent("Common.lbl_BuildNotificationMsgMaster","param_VersionNumber_PARAM:" + dt_Scenario2_newVersionNumber + "_PARAM," + "param_Username_PARAM:" + varDevUsername + "_PARAM," + "param_BuildNumber_PARAM:" + dt_Scenario2_buildNumberTwo,false,"null");
    	//Switch off auto deploy and checking the behaviour
    	if(checkElementPresent("ApplicationReposandBuilds.ele_ExpandIcon")){
    	click("ApplicationReposandBuilds.ele_ExpandIcon");
    	}
    	click("ApplicationReposandBuilds.btn_Settings");
    	click("ApplicationReposandBuilds.chk_AutoDeploy");
    	clickAt("ApplicationReposandBuilds.btn_SaveSettings","10,10");
    	//Wait for page loading
    	pause("3000");
    	if(checkElementPresent("ApplicationReposandBuilds.ele_ExpandIcon")){
    	click("ApplicationReposandBuilds.ele_ExpandIcon");
    	}
    	click("ApplicationReposandBuilds.btn_Settings");
    	checkObjectProperty("ApplicationReposandBuilds.chk_AutoDeploy","checked","false",false,"null");
    	//Build button behaviour when auto deploy off
    	Common.biscomp_BuildButtonFunction(this, dt_Scenario2_appStageDev,dt_Scenario2_newVersionNumber,dt_Scenario2_buildNumberThree,dt_Scenario2_buildStatus,dt_Scenario2_deployNumberTwo);
    	//Verify the wall notofocation for build and deploy
    	checkElementPresent("Common.lbl_BuildNotificationMsgMaster","param_VersionNumber_PARAM:" + dt_Scenario2_newVersionNumber + "_PARAM," + "param_Username_PARAM:" + varDevUsername + "_PARAM," + "param_BuildNumber_PARAM:" + dt_Scenario2_buildNumberThree,false,"null");
    	//Auto build Off
    	if(checkElementPresent("ApplicationReposandBuilds.ele_ExpandIcon")){
    	click("ApplicationReposandBuilds.ele_ExpandIcon");
    	}
    	click("ApplicationReposandBuilds.btn_Settings");
    	click("ApplicationReposandBuilds.chk_AutoBuild");
    	clickAt("ApplicationReposandBuilds.btn_SaveSettings","10,10");
    	//Wait for page loading
    	pause("3000");
    	if(checkElementPresent("ApplicationReposandBuilds.ele_ExpandIcon")){
    	click("ApplicationReposandBuilds.ele_ExpandIcon");
    	}
    	click("ApplicationReposandBuilds.btn_Settings");
    	checkObjectProperty("ApplicationReposandBuilds.chk_AutoDeploy","checked","false",false,"null");
    	checkObjectProperty("ApplicationReposandBuilds.chk_AutoBuild","checked","false",false,"null");
    	//Switching on auto deploy
    	if(checkElementPresent("ApplicationReposandBuilds.ele_ExpandIcon")){
    	click("ApplicationReposandBuilds.ele_ExpandIcon");
    	}
    	click("ApplicationReposandBuilds.btn_Settings");
    	click("ApplicationReposandBuilds.chk_AutoDeploy");
    	clickAt("ApplicationReposandBuilds.btn_SaveSettings","10,10");
    	//Wait for page loading
    	pause("3000");
    	if(checkElementPresent("ApplicationReposandBuilds.ele_ExpandIcon")){
    	click("ApplicationReposandBuilds.ele_ExpandIcon");
    	}
    	click("ApplicationReposandBuilds.btn_Settings");
    	checkObjectProperty("ApplicationReposandBuilds.chk_AutoDeploy","checked","true",false,"null");
    	//Switching ON auto build
    	if(checkElementPresent("ApplicationReposandBuilds.ele_ExpandIcon")){
    	click("ApplicationReposandBuilds.ele_ExpandIcon");
    	}
    	click("ApplicationReposandBuilds.btn_Settings");
    	click("ApplicationReposandBuilds.chk_AutoBuild");
    	clickAt("ApplicationReposandBuilds.btn_SaveSettings","10,10");
    	//Wait for page loading
    	pause("3000");
    	if(checkElementPresent("ApplicationReposandBuilds.ele_ExpandIcon")){
    	click("ApplicationReposandBuilds.ele_ExpandIcon");
    	}
    	click("ApplicationReposandBuilds.btn_Settings");
    	checkObjectProperty("ApplicationReposandBuilds.chk_AutoBuild","checked","true",false,"null");
    	//ETA update
    	Common.biscomp_AddETA(this, dt_Scenario2_newVersionNumber,dt_Scenario2_wrongDateErrorMsg,dt_Scenario2_appStageDev);
    	String varNextDate = retrieveString("keyNextDate");
    	//ETA Change
    	Common.biscomp_EditETA(this, dt_Scenario2_appStageDev,dt_Scenario2_newVersionNumber,varNextDate);
    	clickAt("ApplicationHome.lnk_LifeCycleManagement","1,1");
    	//Waitng for page loading
    	pause("3000");
    	//Promoting the version
    	Common.biscomp_Promote(this, dt_Scenario2_appStageDev,dt_Scenario2_newVersionNumber);
    	//Verifying the promoted version is listed under Testing stage
    	clickAt("ApplicationLifeCycleManagement.ele_ddStage","1,1");
    	//Waiting for list loading
    	pause("3000");
    	clickAt("ApplicationLifeCycleManagement.lbl_Stage","param_Stage_PARAM:" + dt_Scenario2_appStageTest,"1,1");
    	//Waiting for list loading
    	pause("3000");
    	checkElementPresent("ApplicationLifeCycleManagement.lbl_VerisonNumber","param_VersionNumber_PARAM:" + dt_Scenario2_newVersionNumber,false,"null");
    	//Verifying the ETA save button is not available for develper at testing stage
    	checkObjectProperty("ApplicationLifeCycleManagement.btn_Save","param_VersionNumber_PARAM:" + dt_Scenario2_newVersionNumber,"ELEMENTPRESENT",dt_Scenario2_ETASaveTestingStageDev,false,"null");
    	//Verifying history is shown
    	clickAt("ApplicationLifeCycleManagement.btn_LifecycleHistory","param_VersionNumber_PARAM:" + dt_Scenario2_newVersionNumber,"10,10");
    	checkElementPresent("ApplicationLifeCycleManagement.lbl_LifecycleHistoryEntry","param_Action_PARAM:" + dt_Scenario2_actionPromote + "_PARAM," + "param_ActionFrom_PARAM:" + dt_Scenario2_appStageDev + "_PARAM," + "param_ActionTo_PARAM:" + dt_Scenario2_appStageTest,false,"null");
    	clickAt("ApplicationLifeCycleManagement.lnk_CloseHistory","1,1");
    	//Verifying the promoted version is not listed under develpment
    	clickAt("ApplicationLifeCycleManagement.ele_ddStage","10,10");
    	//Waiting for list loading
    	pause("3000");
    	clickAt("ApplicationLifeCycleManagement.lbl_Stage","param_Stage_PARAM:" + dt_Scenario2_appStageDev,"10,10");
    	//Waiting for list loading
    	pause("3000");
    	checkElementPresent("ApplicationLifeCycleManagement.lbl_NoVersionListed",false,"null");
    	//Wait for notification loading
    	pause("8000");
    	keyPress("HomePage.btn_Home","F5");
    	pause("8000");
    	//Verifying the notification wall for promoting
    	checkElementPresent("Common.lbl_NotificationWallMsg","param_Message_PARAM:" + dt_Scenario2_newVersionNumber+" "+dt_Scenario2_promoteDevTestMsg,false,"null");
    	pause("10000");
    	checkElementPresent("Common.lbl_NotificationWallMsg","param_Message_PARAM:" + dt_Scenario2_newVersionNumber+" "+dt_Scenario2_DeployTestMsg,false,"null");
    	//Developer Login Out
    	Common.biscomp_Logout(this);
    	} else {
    	writeToReport("This Sections is not tested in this iteration");
    	}
    	} else if(!dt_Scenario2_IsExecuting){
    	String AppType = dt_Scenario2_appType;
    	writeToReport(AppType+" type is not tested in this iteration");
    	}
    } 
    	

    /**
     * Data provider for Test case tc_AppOwnerQaFunctions.
     * @return data table
     */
    @DataProvider(name = "tc_AppOwnerQaFunctions")
    public Object[][] dataTable_tc_AppOwnerQaFunctions() {     	
    	return this.getDataTable("dt_Scenario2");
    }

    /**
     * Data driven test case tc_AppOwnerQaFunctions.
     *
     * @throws Exception the exception
     */
    @VTAFRecoveryMethods(onerrorMethods = {}, recoveryMethods = {}) 
    @Test (dataProvider = "tc_AppOwnerQaFunctions")
    public final void tc_AppOwnerQaFunctions(final boolean dt_Scenario2_IsExecuting, final boolean dt_Scenario2_IsExecuting_AppCreation, final boolean dt_Scenario2_IsExecuting_DevFunctions, final boolean dt_Scenario2_IsExecuting_AppOwnerQaFunctions, final boolean dt_Scenario2_IsExecutingResourceCURD, final boolean dt_Scenario2_IsExecutingUploadApp, final String dt_Scenario2_appType, final String dt_Scenario2_adminUsername, final String dt_Scenario2_domainName, final String dt_Scenario2_defaultPassword, final String dt_Scenario2_loginErrorMsg, final String dt_Scenario2_dbUserPwdErrorMsg, final String dt_Scenario2_userListMain, final String dt_Scenario2_userListUploadApp, final String dt_Scenario2_userListEdit, final String dt_Scenario2_userListDelete, final String dt_Scenario2_usernames, final String dt_Scenario2_userInitials, final String dt_Scenario2_wrongPassword, final String dt_Scenario2_wrongUsername, final String dt_Scenario2_UsernameErrorMsg, final String dt_Scenario2_PasswordErrorMsg, final String dt_Scenario2_adminPassword, final String dt_Scenario2_appName, final String dt_Scenario2_appKey, final String dt_Scenario2_appDescription, final String dt_Scenario2_editDescription, final String dt_Scenario2_appImagePath, final String dt_Scenario2_editAppImagePath, final String dt_Scenario2_appPath, final String dt_Scenario2_appCreationMethod, final String dt_Scenario2_appCreationMsg, final String dt_Scenario2_gitMsg, final String dt_Scenario2_jenkinsMsg, final String dt_Scenario2_issueTrackerMsg, final String dt_Scenario2_cloudMsg, final String dt_Scenario2_sameAppKeyErrorMsg, final String dt_Scenario2_addMemberMsg, final String dt_Scenario2_removeMemberMsg, final String dt_Scenario2_wrongDateErrorMsg, final String dt_Scenario2_promoteDevTestMsg, final String dt_Scenario2_DeployTestMsg, final String dt_Scenario2_appStageDev, final String dt_Scenario2_appStageTest, final String dt_Scenario2_appStagePro, final String dt_Scenario2_actionPromote, final String dt_Scenario2_actionDemote, final String dt_Scenario2_OverviewTabVisibilityDev, final String dt_Scenario2_RepoandBuildTabVisibilityDev, final String dt_Scenario2_TeamTabVisibilityDev, final String dt_Scenario2_databaseTabVisibilityDev, final String dt_Scenario2_LifecycleManagementTabVisibilityDev, final String dt_Scenario2_ResourceTabVisibilityDev, final String dt_Scenario2_IssuesTabVisibilityDev, final String dt_Scenario2_LogsTabVisibilityDev, final String dt_Scenario2_buildStatus, final String dt_Scenario2_deployStatus, final String dt_Scenario2_versionnumber, final String dt_Scenario2_newVersionNumber, final String dt_Scenario2_buildNumber, final String dt_Scenario2_buildNumberOne, final String dt_Scenario2_buildNumberTwo, final String dt_Scenario2_buildNumberThree, final String dt_Scenario2_deployNumberTwo, final String dt_Scenario2_deployNumberOne, final String dt_Scenario2_dbName, final String dt_Scenario2_dbEnvironment, final String dt_Scenario2_dbAllEnvYes, final String dt_Scenario2_dbAllEnvNo, final String dt_Scenario2_dsName, final String dt_Scenario2_dbDriver, final String dt_Scenario2_dsEnv, final String dt_Scenario2_dbURL, final String dt_Scenario2_dbUsername, final String dt_Scenario2_dbPassword, final String dt_Scenario2_dsDesc, final String dt_Scenario2_dsPassword, final String dt_Scenario2_internalAPIName, final String dt_Scenario2_internalAPIVersion, final String dt_Scenario2_exAPIName, final String dt_Scenario2_authAPI, final String dt_Scenario2_exAPIURL, final String dt_Scenario2_propName, final String dt_Scenario2_proDesc, final String dt_Scenario2_propRegEnv, final String dt_Scenario2_proValue, final String dt_Scenario2_issueSummary, final String dt_Scenario2_issueDesc, final String dt_Scenario2_versionIssueRelatedTo, final String dt_Scenario2_issueType, final String dt_Scenario2_issueTypeNew, final String dt_Scenario2_issueTypeBug, final String dt_Scenario2_issuePriority, final String dt_Scenario2_issuePriorityNew, final String dt_Scenario2_issuePriorityNormal, final String dt_Scenario2_issueAsignee, final String dt_Scenario2_issueSeverity, final String dt_Scenario2_versionToDemote, final String dt_Scenario2_demoteComment, final String dt_Scenario2_issueStatusInitial, final String dt_Scenario2_issueStatusNew, final String dt_Scenario2_issueStatusResolved, final String dt_Scenario2_issueStatusFinal, final String dt_Scenario2_issueStatusClosed, final String dt_Scenario2_subDomain, final String dt_Scenario2_serviceName, final String dt_Scenario2_domain, final String dt_Scenario2_lawPauseLevel, final String dt_Scenario2_mediumPauseLevel, final String dt_Scenario2_highPauseLevel, final String dt_Scenario2_sampleText, final int dt_Scenario2_waitTimeForAppLoadingInSec, final String dt_Scenario2_appPathNew, final String dt_Scenario2_newUploadVersion, final String dt_Scenario2_dbTempPrivUnTicked, final String dt_Scenario2_dbTempPrivTicked, final String dt_Scenario2_dbNOtVisible, final String dt_Scenario2_dbVisible, final String dt_Scenario2_versionVisibilityTrue, final String dt_Scenario2_versionVisibilityFalse, final String dt_Scenario2_ETADisability, final String dt_Scenario2_StageVisibilityTrue, final String dt_Scenario2_StageVisibilityFalse, final String dt_Scenario2_addNewBtnVisibilityTrue, final String dt_Scenario2_addNewBtnVisibilityFalse, final String dt_Scenario2_OverviewTabVisibilityQA, final String dt_Scenario2_RepoandBuildTabVisibilityQA, final String dt_Scenario2_TeamTabVisibilityQA, final String dt_Scenario2_LifecycleManagementTabVisibilityQA, final String dt_Scenario2_ResourceTabVisibilityQA, final String dt_Scenario2_IssuesTabVisibilityQA, final String dt_Scenario2_databaseTabVisibilityQA, final String dt_Scenario2_LogsTabVisibilityQA, final String dt_Scenario2_OverviewTabVisibilityDEVOPS, final String dt_Scenario2_RepoandBuildTabVisibilityDEVOPS, final String dt_Scenario2_TeamTabVisibilityDEVOPS, final String dt_Scenario2_LifecycleManagementTabVisibilityDEVOPS, final String dt_Scenario2_ResourceTabVisibilityDEVOPS, final String dt_Scenario2_IssuesTabVisibilityDEVOPS, final String dt_Scenario2_databaseTabVisibilityDEVOPS, final String dt_Scenario2_LogsTabVisibilityDEVOPS, final String dt_Scenario2_ETASaveTestingStageDev, final String dt_Scenario2_appLogoSubPath, final String dt_Scenario2_appLogoMainPath, final String dt_Scenario2_prodKeyVisibilityDev, final String dt_Scenario2_prodKeyVisibilityQa, final String dt_Scenario2_prodKeyVisibilityDevOps, final String dt_Scenario2_prodKeyVisibilityAppOwner, final String dt_Scenario2_SandKeyVisibilityDev, final String dt_Scenario2_SandKeyVisibilityQa, final String dt_Scenario2_SandKeyVisibilityDevOps, final String dt_Scenario2_SandKeyVisibilityAppOwner, final String dt_Scenario2_DevDBVisibilityDevOps, final String dt_Scenario2_DevDBUserVisibilityDevOps, final String dt_Scenario2_DevDBTempVisibilityDevOps, final String dt_Scenario2_passWord) throws Exception {	
    	//Checking the executing status of the application type
    	if(dt_Scenario2_IsExecuting){
    	if(dt_Scenario2_IsExecuting_AppOwnerQaFunctions){
    	//Retrieving values
    	String varAppOwnerUsername = retrieveString("AF_AppOwner");
    	String varDevOpsUsername = retrieveString("AF_DevOps");
    	String varDevUsername = retrieveString("AF_DEV");
    	String varQaUsername = retrieveString("AF_QA");
    	String varPassword = retrieveString("AFDefaultPassword");
    	String varDomainName = retrieveString("AFTenantDomain");
    	String varDomain = "@"+varDomainName+".com";
    	String varMainURL = retrieveString("AFMainURL");
    	String varAppName = retrieveString("keyAppName");
    	String varAppKey = retrieveString("keyAppKey");
    	String varTenantAdminName = retrieveString("AFTenantAdmin_Name");
    	String varUserCommonPassword = retrieveString("AFUserCommonPassword");
    	String varTenantAdmin_Password = retrieveString("AFTenantAdmin_Password");
    	//QA user login
    	Common.biscomp_Login(this, varQaUsername+varDomain,varUserCommonPassword,varMainURL);
    	//Verifying the created application is visible
    	Common.biscomp_VerifyingCreatedApplication(this, varAppName,dt_Scenario2_appType,dt_Scenario2_waitTimeForAppLoadingInSec);
    	//Verify the 'Add New Application' button is not visible
    	checkObjectProperty("HomePage.btn_AddNewApplication","ELEMENTPRESENT",dt_Scenario2_addNewBtnVisibilityFalse,false,"null");
    	Common.biscomp_SelectApp(this, varAppName);
    	//Verifying the Application tab visibility
    	Common.biscomp_VerifyingTabVisibility(this, dt_Scenario2_OverviewTabVisibilityQA,dt_Scenario2_RepoandBuildTabVisibilityQA,dt_Scenario2_TeamTabVisibilityQA,dt_Scenario2_LifecycleManagementTabVisibilityQA,dt_Scenario2_ResourceTabVisibilityQA,dt_Scenario2_IssuesTabVisibilityQA,dt_Scenario2_LogsTabVisibilityQA,dt_Scenario2_databaseTabVisibilityQA);
    	//Accessing Lifecycle Management Tab
    	clickAt("ApplicationHome.lnk_LifeCycleManagement","1,1");
    	//Wait for page loading
    	pause("5000");
    	//Verifying the promoted version is listed under testing
    	Common.biscomp_VerifyingVersionListedUnderStage(this, dt_Scenario2_appStageTest,dt_Scenario2_newVersionNumber,dt_Scenario2_versionVisibilityTrue);
    	//Launching by Qa
    	Common.biscomp_LaunchingAtOverviewPage(this, dt_Scenario2_newVersionNumber,dt_Scenario2_sampleText,dt_Scenario2_waitTimeForAppLoadingInSec,dt_Scenario2_appType,varAppKey,varDomainName,dt_Scenario2_appStageTest);
    	//Selecting main window
    	selectWindow("Window.win_NewWindow","param_NewWindowNumber_PARAM:0");
    	pause("2000");
    	//Qa Creating an Issue
    	Common.biscomp_CreateIssue(this, dt_Scenario2_issueSummary,dt_Scenario2_issueDesc,dt_Scenario2_versionIssueRelatedTo,dt_Scenario2_issueType,dt_Scenario2_issuePriority,dt_Scenario2_issueStatusInitial,dt_Scenario2_issueAsignee,dt_Scenario2_issueSeverity);
    	//Verifying the created issue is listed under issues
    	String varIssueSummary = retrieveString("keyIssueSummary");
    	selectFrame("frames.frm_Parent");
    	clickAt("ApplicationHome.lnk_Issues","1,1");
    	selectFrame("frames.frm_Parent");
    	selectFrame("frames.frm_iframe");
    	checkObjectProperty("ApplicationIssues.ele_CreatedIssue","textContent",varIssueSummary,false,"null");
    	selectFrame("frames.frm_Parent");
    	//Editing the issue type and priority
    	Common.biscomp_EditIssue(this, varIssueSummary,dt_Scenario2_issueDesc,dt_Scenario2_versionIssueRelatedTo,dt_Scenario2_issueTypeNew,dt_Scenario2_issuePriorityNew,dt_Scenario2_issueStatusInitial,dt_Scenario2_issueAsignee,dt_Scenario2_issueSeverity);
    	selectFrame("frames.frm_Parent");
    	clickAt("ApplicationHome.lnk_Issues","1,1");
    	selectFrame("frames.frm_Parent");
    	selectFrame("frames.frm_iframe");
    	//Verifying the eidted issue type is displayed
    	checkObjectProperty("ApplicationIssues.ele_IssueType","param_IssueSummary_PARAM:" + varIssueSummary,"textContent",dt_Scenario2_issueTypeBug,false,"null");
    	//Verifying the edited issue priority displayed
    	checkObjectProperty("ApplicationIssues.ele_IssuePriority","param_IssueSummary_PARAM:" + varIssueSummary,"textContent",dt_Scenario2_issuePriorityNormal,false,"null");
    	selectFrame("frames.frm_Parent");
    	clickAt("ApplicationHome.lnk_LifeCycleManagement","1,1");
    	//Waiting for page loading
    	pause("3000");
    	Common.biscomp_VerifyingLifcycleHistory(this, dt_Scenario2_newVersionNumber,dt_Scenario2_actionPromote,dt_Scenario2_appStageDev,dt_Scenario2_appStageTest,dt_Scenario2_appStageTest);
    	//QA Demoting the version
    	Common.biscomp_Demote(this, dt_Scenario2_appStageTest,dt_Scenario2_newVersionNumber,varIssueSummary,dt_Scenario2_demoteComment);
    	String varDemoteComment = retrieveString("keyDemoteComment");
    	clickAt("ApplicationHome.lnk_LifeCycleManagement","1,1");
    	//Waiting for page loading
    	pause("3000");
    	Common.biscomp_VerifyingVersionListedUnderStage(this, dt_Scenario2_appStageDev,dt_Scenario2_newVersionNumber,dt_Scenario2_versionVisibilityTrue);
    	//Verifying the Lifecycle history
    	Common.biscomp_VerifyingLifcycleHistory(this, dt_Scenario2_newVersionNumber,dt_Scenario2_actionDemote,dt_Scenario2_appStageTest,dt_Scenario2_appStageDev,dt_Scenario2_appStageDev);
    	//QA User login Out
    	Common.biscomp_Logout(this);
    	//Developer User login
    	Common.biscomp_Login(this, varDevUsername+varDomain,varUserCommonPassword,varMainURL);
    	Common.biscomp_SelectApp(this, varAppName);
    	//Wait for page loading
    	pause("2000");
    	//Developer resovling the
    	Common.biscomp_EditIssue(this, varIssueSummary,dt_Scenario2_issueDesc,dt_Scenario2_versionIssueRelatedTo,dt_Scenario2_issueTypeNew,dt_Scenario2_issuePriorityNew,dt_Scenario2_issueStatusNew,dt_Scenario2_issueAsignee,dt_Scenario2_issueSeverity);
    	selectFrame("frames.frm_Parent");
    	clickAt("ApplicationHome.lnk_Issues","1,1");
    	selectFrame("frames.frm_Parent");
    	selectFrame("frames.frm_iframe");
    	//Verifying the issue status is updated as RESOLVED
    	checkObjectProperty("ApplicationIssues.ele_IssueStatus","param_IssueSummary_PARAM:" + varIssueSummary,"textContent",dt_Scenario2_issueStatusResolved,false,"null");
    	selectFrame("frames.frm_Parent");
    	clickAt("ApplicationHome.lnk_LifeCycleManagement","1,1");
    	//Waiting for list loading
    	pause("3000");
    	//Promoting the version to Testing
    	Common.biscomp_Promote(this, dt_Scenario2_appStageDev,dt_Scenario2_newVersionNumber);
    	//Verifying the promoted version is not list under development
    	Common.biscomp_VerifyingVersionNotListedUnderStage(this, dt_Scenario2_appStageDev);
    	//Verifying prmoted Version is listed under Testing stage
    	Common.biscomp_VerifyingVersionListedUnderStage(this, dt_Scenario2_appStageTest,dt_Scenario2_newVersionNumber,dt_Scenario2_versionVisibilityTrue);
    	clickAt("ApplicationLifeCycleManagement.ele_ddStage","1,1");
    	//Waiting for list loading
    	pause("3000");
    	clickAt("ApplicationLifeCycleManagement.lbl_Stage","param_Stage_PARAM:" + dt_Scenario2_appStageTest,"1,1");
    	//Waiting for list loading
    	pause("3000");
    	//Verifying the ETA save button is not available for develper at testing stage
    	checkObjectProperty("ApplicationLifeCycleManagement.btn_Save","param_VersionNumber_PARAM:" + dt_Scenario2_newVersionNumber,"ELEMENTPRESENT",dt_Scenario2_ETASaveTestingStageDev,false,"null");
    	//Verifying the lifecycle history details-Promote->Demote->Promote
    	clickAt("ApplicationLifeCycleManagement.btn_LifecycleHistory","param_VersionNumber_PARAM:" + dt_Scenario2_newVersionNumber,"10,10");
    	checkElementPresent("ApplicationLifeCycleManagement.ele_LifecycleEntriesMultiple","param_1stAction_PARAM:" + dt_Scenario2_actionPromote + "_PARAM," + "param_2ndAction_PARAM:" + dt_Scenario2_actionDemote + "_PARAM," + "param_3rdAction_PARAM:" + dt_Scenario2_actionPromote,false,"null");
    	//Verifying the notification wall for promoting
    	checkElementPresent("Common.lbl_NotificationWallMsg","param_Message_PARAM:" + dt_Scenario2_newVersionNumber+" promoting from "+dt_Scenario2_appStageDev+" to "+dt_Scenario2_appStageTest+".",false,"null");
    	checkElementPresent("Common.lbl_NotificationWallMsg","param_Message_PARAM:" + dt_Scenario2_newVersionNumber+" deployed in "+dt_Scenario2_appStageTest+" stage",false,"null");
    	//Developer logs out
    	Common.biscomp_Logout(this);
    	//QA user login
    	Common.biscomp_Login(this, varQaUsername+varDomain,varUserCommonPassword,varMainURL);
    	Common.biscomp_SelectApp(this, varAppName);
    	//Wait for page loading
    	pause("2000");
    	//Launch the application
    	Common.biscomp_LaunchingAtOverviewPage(this, dt_Scenario2_newVersionNumber,dt_Scenario2_sampleText,dt_Scenario2_waitTimeForAppLoadingInSec,dt_Scenario2_appType,varAppKey,varDomainName,dt_Scenario2_appStageTest);
    	//Selecting Main Window
    	selectWindow("Window.win_NewWindow","param_NewWindowNumber_PARAM:0");
    	pause("2000");
    	//Closing the issue
    	Common.biscomp_EditIssue(this, varIssueSummary,dt_Scenario2_issueDesc,dt_Scenario2_versionIssueRelatedTo,dt_Scenario2_issueTypeNew,dt_Scenario2_issuePriorityNew,dt_Scenario2_issueStatusFinal,dt_Scenario2_issueAsignee,dt_Scenario2_issueSeverity);
    	selectFrame("frames.frm_Parent");
    	clickAt("ApplicationHome.lnk_Issues","1,1");
    	selectFrame("frames.frm_Parent");
    	selectFrame("frames.frm_iframe");
    	//Verifying the issue status is updated as CLOSED
    	checkObjectProperty("ApplicationIssues.ele_IssueStatus","param_IssueSummary_PARAM:" + varIssueSummary,"textContent",dt_Scenario2_issueStatusClosed,false,"null");
    	selectFrame("frames.frm_Parent");
    	clickAt("ApplicationHome.lnk_LifeCycleManagement","1,1");
    	//Waiting for list loading
    	pause("3000");
    	//Tester promoting version to production stage
    	Common.biscomp_Promote(this, dt_Scenario2_appStageTest,dt_Scenario2_newVersionNumber);
    	//Verifying the promoted version is not list under Testing
    	Common.biscomp_VerifyingVersionNotListedUnderStage(this, dt_Scenario2_appStageTest);
    	//Verifying prmoted Version is listed under Production stage
    	Common.biscomp_VerifyingVersionListedUnderStage(this, dt_Scenario2_appStagePro,dt_Scenario2_newVersionNumber,dt_Scenario2_versionVisibilityTrue);
    	//Wait for page loading
    	pause("8000");
    	//Verifying the notification wall for promoting
    	checkElementPresent("Common.lbl_NotificationWallMsg","param_Message_PARAM:" + dt_Scenario2_newVersionNumber+" promoting from "+dt_Scenario2_appStageTest+" to "+dt_Scenario2_appStagePro+".",false,"null");
    	//Tester logs out
    	Common.biscomp_Logout(this);
    	//DevOps logs in
    	Common.biscomp_Login(this, varDevOpsUsername+varDomain,varUserCommonPassword,varMainURL);
    	//Verifying the created application is visible
    	Common.biscomp_VerifyingCreatedApplication(this, varAppName,dt_Scenario2_appType,dt_Scenario2_waitTimeForAppLoadingInSec);
    	//Verify the 'Add New Application' button is not visible
    	checkObjectProperty("HomePage.btn_AddNewApplication","ELEMENTPRESENT",dt_Scenario2_addNewBtnVisibilityFalse,false,"null");
    	Common.biscomp_SelectApp(this, varAppName);
    	//Wait for page loading
    	pause("2000");
    	//Verifying the Application tab visibility
    	Common.biscomp_VerifyingTabVisibility(this, dt_Scenario2_OverviewTabVisibilityDEVOPS,dt_Scenario2_RepoandBuildTabVisibilityDEVOPS,dt_Scenario2_TeamTabVisibilityDEVOPS,dt_Scenario2_LifecycleManagementTabVisibilityDEVOPS,dt_Scenario2_ResourceTabVisibilityDEVOPS,dt_Scenario2_IssuesTabVisibilityDEVOPS,dt_Scenario2_LogsTabVisibilityDEVOPS,dt_Scenario2_databaseTabVisibilityDEVOPS);
    	//click lifecycle maneger
    	clickAt("ApplicationHome.lnk_LifeCycleManagement","10,10");
    	//DevOps promotes the app to production
    	Common.biscomp_Promote(this, dt_Scenario2_appStagePro,dt_Scenario2_newVersionNumber);
    	//Launch the version
    	Common.biscomp_LaunchingAtOverviewPage(this, dt_Scenario2_newVersionNumber,dt_Scenario2_sampleText,dt_Scenario2_waitTimeForAppLoadingInSec,dt_Scenario2_appType,varAppKey,varDomainName,dt_Scenario2_appStagePro);
    	//Selecting Main window
    	selectWindow("Window.win_NewWindow","param_NewWindowNumber_PARAM:0");
    	pause("2000");
    	clickAt("ApplicationHome.lnk_LifeCycleManagement","1,1");
    	//Verifying history records
    	Common.biscomp_VerifyingLifcycleHistory(this, dt_Scenario2_newVersionNumber,dt_Scenario2_actionPromote,dt_Scenario2_appStageTest,dt_Scenario2_appStagePro,dt_Scenario2_appStagePro);
    	clickAt("ApplicationHome.lnk_LifeCycleManagement","1,1");
    	//Wait for page loading
    	pause("3000");
    	//retire the version
    	Common.biscomp_Retire(this, dt_Scenario2_appStagePro,dt_Scenario2_newVersionNumber,dt_Scenario2_appCreationMethod);
    	//Verifying the promoted version is not list under Production
    	Common.biscomp_VerifyingRetiredVersion(this, dt_Scenario2_appStagePro,dt_Scenario2_newVersionNumber);
    	//Wait for page loading
    	pause("8000");
    	//Verifying the notification wall for promoting
    	checkElementPresent("Common.lbl_NotificationWallMsg","param_Message_PARAM:" + dt_Scenario2_newVersionNumber+" retiring from "+dt_Scenario2_appStagePro+".",false,"null");
    	//Login Out
    	Common.biscomp_Logout(this);
    	} else {
    	writeToReport("This Sections is not tested in this iteration");
    	}
    	} else if(!dt_Scenario2_IsExecuting){
    	String AppType = dt_Scenario2_appType;
    	writeToReport(AppType+" type is not tested in this iteration");
    	}
    } 
    	

    /**
     * Data provider for Test case tc_ResourceCURD.
     * @return data table
     */
    @DataProvider(name = "tc_ResourceCURD")
    public Object[][] dataTable_tc_ResourceCURD() {     	
    	return this.getDataTable("dt_Scenario2");
    }

    /**
     * Data driven test case tc_ResourceCURD.
     *
     * @throws Exception the exception
     */
    @VTAFRecoveryMethods(onerrorMethods = {}, recoveryMethods = {}) 
    @Test (dataProvider = "tc_ResourceCURD")
    public final void tc_ResourceCURD(final boolean dt_Scenario2_IsExecuting, final boolean dt_Scenario2_IsExecuting_AppCreation, final boolean dt_Scenario2_IsExecuting_DevFunctions, final boolean dt_Scenario2_IsExecuting_AppOwnerQaFunctions, final boolean dt_Scenario2_IsExecutingResourceCURD, final boolean dt_Scenario2_IsExecutingUploadApp, final String dt_Scenario2_appType, final String dt_Scenario2_adminUsername, final String dt_Scenario2_domainName, final String dt_Scenario2_defaultPassword, final String dt_Scenario2_loginErrorMsg, final String dt_Scenario2_dbUserPwdErrorMsg, final String dt_Scenario2_userListMain, final String dt_Scenario2_userListUploadApp, final String dt_Scenario2_userListEdit, final String dt_Scenario2_userListDelete, final String dt_Scenario2_usernames, final String dt_Scenario2_userInitials, final String dt_Scenario2_wrongPassword, final String dt_Scenario2_wrongUsername, final String dt_Scenario2_UsernameErrorMsg, final String dt_Scenario2_PasswordErrorMsg, final String dt_Scenario2_adminPassword, final String dt_Scenario2_appName, final String dt_Scenario2_appKey, final String dt_Scenario2_appDescription, final String dt_Scenario2_editDescription, final String dt_Scenario2_appImagePath, final String dt_Scenario2_editAppImagePath, final String dt_Scenario2_appPath, final String dt_Scenario2_appCreationMethod, final String dt_Scenario2_appCreationMsg, final String dt_Scenario2_gitMsg, final String dt_Scenario2_jenkinsMsg, final String dt_Scenario2_issueTrackerMsg, final String dt_Scenario2_cloudMsg, final String dt_Scenario2_sameAppKeyErrorMsg, final String dt_Scenario2_addMemberMsg, final String dt_Scenario2_removeMemberMsg, final String dt_Scenario2_wrongDateErrorMsg, final String dt_Scenario2_promoteDevTestMsg, final String dt_Scenario2_DeployTestMsg, final String dt_Scenario2_appStageDev, final String dt_Scenario2_appStageTest, final String dt_Scenario2_appStagePro, final String dt_Scenario2_actionPromote, final String dt_Scenario2_actionDemote, final String dt_Scenario2_OverviewTabVisibilityDev, final String dt_Scenario2_RepoandBuildTabVisibilityDev, final String dt_Scenario2_TeamTabVisibilityDev, final String dt_Scenario2_databaseTabVisibilityDev, final String dt_Scenario2_LifecycleManagementTabVisibilityDev, final String dt_Scenario2_ResourceTabVisibilityDev, final String dt_Scenario2_IssuesTabVisibilityDev, final String dt_Scenario2_LogsTabVisibilityDev, final String dt_Scenario2_buildStatus, final String dt_Scenario2_deployStatus, final String dt_Scenario2_versionnumber, final String dt_Scenario2_newVersionNumber, final String dt_Scenario2_buildNumber, final String dt_Scenario2_buildNumberOne, final String dt_Scenario2_buildNumberTwo, final String dt_Scenario2_buildNumberThree, final String dt_Scenario2_deployNumberTwo, final String dt_Scenario2_deployNumberOne, final String dt_Scenario2_dbName, final String dt_Scenario2_dbEnvironment, final String dt_Scenario2_dbAllEnvYes, final String dt_Scenario2_dbAllEnvNo, final String dt_Scenario2_dsName, final String dt_Scenario2_dbDriver, final String dt_Scenario2_dsEnv, final String dt_Scenario2_dbURL, final String dt_Scenario2_dbUsername, final String dt_Scenario2_dbPassword, final String dt_Scenario2_dsDesc, final String dt_Scenario2_dsPassword, final String dt_Scenario2_internalAPIName, final String dt_Scenario2_internalAPIVersion, final String dt_Scenario2_exAPIName, final String dt_Scenario2_authAPI, final String dt_Scenario2_exAPIURL, final String dt_Scenario2_propName, final String dt_Scenario2_proDesc, final String dt_Scenario2_propRegEnv, final String dt_Scenario2_proValue, final String dt_Scenario2_issueSummary, final String dt_Scenario2_issueDesc, final String dt_Scenario2_versionIssueRelatedTo, final String dt_Scenario2_issueType, final String dt_Scenario2_issueTypeNew, final String dt_Scenario2_issueTypeBug, final String dt_Scenario2_issuePriority, final String dt_Scenario2_issuePriorityNew, final String dt_Scenario2_issuePriorityNormal, final String dt_Scenario2_issueAsignee, final String dt_Scenario2_issueSeverity, final String dt_Scenario2_versionToDemote, final String dt_Scenario2_demoteComment, final String dt_Scenario2_issueStatusInitial, final String dt_Scenario2_issueStatusNew, final String dt_Scenario2_issueStatusResolved, final String dt_Scenario2_issueStatusFinal, final String dt_Scenario2_issueStatusClosed, final String dt_Scenario2_subDomain, final String dt_Scenario2_serviceName, final String dt_Scenario2_domain, final String dt_Scenario2_lawPauseLevel, final String dt_Scenario2_mediumPauseLevel, final String dt_Scenario2_highPauseLevel, final String dt_Scenario2_sampleText, final int dt_Scenario2_waitTimeForAppLoadingInSec, final String dt_Scenario2_appPathNew, final String dt_Scenario2_newUploadVersion, final String dt_Scenario2_dbTempPrivUnTicked, final String dt_Scenario2_dbTempPrivTicked, final String dt_Scenario2_dbNOtVisible, final String dt_Scenario2_dbVisible, final String dt_Scenario2_versionVisibilityTrue, final String dt_Scenario2_versionVisibilityFalse, final String dt_Scenario2_ETADisability, final String dt_Scenario2_StageVisibilityTrue, final String dt_Scenario2_StageVisibilityFalse, final String dt_Scenario2_addNewBtnVisibilityTrue, final String dt_Scenario2_addNewBtnVisibilityFalse, final String dt_Scenario2_OverviewTabVisibilityQA, final String dt_Scenario2_RepoandBuildTabVisibilityQA, final String dt_Scenario2_TeamTabVisibilityQA, final String dt_Scenario2_LifecycleManagementTabVisibilityQA, final String dt_Scenario2_ResourceTabVisibilityQA, final String dt_Scenario2_IssuesTabVisibilityQA, final String dt_Scenario2_databaseTabVisibilityQA, final String dt_Scenario2_LogsTabVisibilityQA, final String dt_Scenario2_OverviewTabVisibilityDEVOPS, final String dt_Scenario2_RepoandBuildTabVisibilityDEVOPS, final String dt_Scenario2_TeamTabVisibilityDEVOPS, final String dt_Scenario2_LifecycleManagementTabVisibilityDEVOPS, final String dt_Scenario2_ResourceTabVisibilityDEVOPS, final String dt_Scenario2_IssuesTabVisibilityDEVOPS, final String dt_Scenario2_databaseTabVisibilityDEVOPS, final String dt_Scenario2_LogsTabVisibilityDEVOPS, final String dt_Scenario2_ETASaveTestingStageDev, final String dt_Scenario2_appLogoSubPath, final String dt_Scenario2_appLogoMainPath, final String dt_Scenario2_prodKeyVisibilityDev, final String dt_Scenario2_prodKeyVisibilityQa, final String dt_Scenario2_prodKeyVisibilityDevOps, final String dt_Scenario2_prodKeyVisibilityAppOwner, final String dt_Scenario2_SandKeyVisibilityDev, final String dt_Scenario2_SandKeyVisibilityQa, final String dt_Scenario2_SandKeyVisibilityDevOps, final String dt_Scenario2_SandKeyVisibilityAppOwner, final String dt_Scenario2_DevDBVisibilityDevOps, final String dt_Scenario2_DevDBUserVisibilityDevOps, final String dt_Scenario2_DevDBTempVisibilityDevOps, final String dt_Scenario2_passWord) throws Exception {	
    	//Checking the executing status of the application type
    	if(dt_Scenario2_IsExecuting){
    	if(dt_Scenario2_IsExecutingResourceCURD){
    	//Retrieving values
    	String varMainURL = retrieveString("AFMainURL");
    	String varUserName = retrieveString(dt_Scenario2_adminUsername);
    	String varTenantAdminName = retrieveString("AFTenantAdmin_Name");
    	String varDomainName = retrieveString("AFTenantDomain");
    	String varUserCommonPassword = retrieveString("AFUserCommonPassword");
    	String varTenantAdmin_Password = retrieveString("AFTenantAdmin_Password");
    	String varDomain = "@"+varDomainName+".com";
    	String varPassword = retrieveString("AFDefaultPassword");
    	if(dt_Scenario2_appType.equals("Java Web Application")){
    	String varLanguageType = "Java";
    	store("keyLanguageType","String",varLanguageType);
    	} else if(dt_Scenario2_appType.equals("JAX-RS Service")){
    	String varLanguageType = "Java";
    	store("keyLanguageType","String",varLanguageType);
    	} else if(dt_Scenario2_appType.equals("JAX-WS Service")){
    	String varLanguageType = "Java";
    	store("keyLanguageType","String",varLanguageType);
    	} else if(dt_Scenario2_appType.equals("Jaggery Application")){
    	String varLanguageType = "NonJava";
    	store("keyLanguageType","String",varLanguageType);
    	} else if(dt_Scenario2_appType.equals("WSO2 Data Service")){
    	String varLanguageType = "NonJava";
    	store("keyLanguageType","String",varLanguageType);
    	} else if(dt_Scenario2_appType.equals("WAR")){
    	String varLanguageType = "Java";
    	store("keyLanguageType","String",varLanguageType);
    	} else if(dt_Scenario2_appType.equals("Jaggery App")){
    	String varLanguageType = "NonJava";
    	store("keyLanguageType","String",varLanguageType);
    	}
    	String varLanguageType = retrieveString("keyLanguageType");
    	//Tenant Admin Login
    	Common.biscomp_Login(this, varUserName+varDomain,varTenantAdmin_Password,varMainURL);
    	//Assigning users names
    	Common.biscomp_AssigningUsernames(this, dt_Scenario2_userListMain,dt_Scenario2_userInitials);
    	String varUserList = retrieveString("keyUserList");
    	//Importing users
    	Common.biscomp_ImportMembers(this, varUserList,varUserCommonPassword,varUserCommonPassword);
    	//Wait for users to be loaded
    	pause("5000");
    	//Assigning roles to the imported users
    	Common.biscomp_AssignUserRoles(this, varUserList);
    	String varAppOwnerUsername = retrieveString("AF_AppOwner");
    	String varDevOpsUsername = retrieveString("AF_DevOps");
    	String varDevUsername = retrieveString("AF_DEV");
    	String varQaUsername = retrieveString("AF_QA");
    	//TenantAdmin Login out
    	Common.biscomp_Logout(this);
    	//AppOwner Login
    	Common.biscomp_Login(this, varAppOwnerUsername+varDomain,varUserCommonPassword,varMainURL);
    	Common.biscomp_CreateApplication(this, dt_Scenario2_appName,dt_Scenario2_appKey,dt_Scenario2_appDescription,dt_Scenario2_appImagePath,dt_Scenario2_appType,dt_Scenario2_appPath,dt_Scenario2_appCreationMethod);
    	String varAppName = retrieveString("keyAppName");
    	String varAppKey = retrieveString("keyAppKey");
    	//Verifying created application is listed in the home page
    	Common.biscomp_VerifyingCreatedApplication(this, varAppName,dt_Scenario2_appType,dt_Scenario2_waitTimeForAppLoadingInSec);
    	//Refreshing the page for wall notification
    	keyPress("HomePage.btn_Home","F5");
    	//Wait for wall notification laoding
    	pause("3000");
    	Common.biscomp_VerifyingNotificationWallPost(this, dt_Scenario2_appCreationMsg,dt_Scenario2_gitMsg,dt_Scenario2_jenkinsMsg,dt_Scenario2_issueTrackerMsg,dt_Scenario2_cloudMsg,varAppName,varAppKey);
    	Common.biscomp_SelectApp(this, varAppName);
    	//Wait for app loading
    	pause("4000");
    	if(dt_Scenario2_appCreationMethod.equals("Create")){
    	clickAt("ApplicationHome.lnk_RepoandBuild","10,10");
    	Common.biscomp_CreateBranch(this, dt_Scenario2_appStageDev,dt_Scenario2_versionnumber,dt_Scenario2_newVersionNumber);
    	}
    	//Add Members to the Application
    	Common.biscomp_AddMembers(this, varUserList,dt_Scenario2_addMemberMsg,varAppOwnerUsername);
    	//Creating a Internal API
    	Common.biscomp_CreatingInternalAPI(this, dt_Scenario2_internalAPIName,dt_Scenario2_internalAPIVersion,varAppKey);
    	//Retrieving production and sandbox keys
    	String varProductionKey = retrieveString("keyProductionKey");
    	String varSandbox = retrieveString("keySandboxKey");
    	selectWindow("Window.win_NewWindow","param_NewWindowNumber_PARAM:0");
    	clickAt("ApplicationHome.lnk_ResourceTab","10,10");
    	//Wait for page loading
    	pause("3000");
    	clickAt("ApplicationResources.lnk_APITab","10,10");
    	//Wait for page loading
    	pause("3000");
    	//Verifying the created aPI is displayed at API tab in th resource page
    	checkElementPresent("ApplicationResources.lbl_InternalAPIName","param_APIName_PARAM:" + dt_Scenario2_internalAPIName,false,"null");
    	//Synchronising generated production and sandbox keys
    	clickAt("ApplicationResources.btn_SyncKeys","10,10");
    	//Waiting for keys to be synchronising
    	pause("5000");
    	//Verifying the production key and Sandbox key
    	checkObjectProperty("ApplicationResources.lbl_ProductionKey","textContent",varProductionKey,false,"null");
    	checkObjectProperty("ApplicationResources.lbl_SandboxKey","textContent",varSandbox,false,"null");
    	//Creating External API
    	Common.biscomp_CreatingExternalAPI(this, dt_Scenario2_exAPIName,dt_Scenario2_appStageDev,dt_Scenario2_authAPI,dt_Scenario2_exAPIURL);
    	String varExternalAPIName = retrieveString("keyExternalAPIName");
    	//Verifying The created external API is listed in the API tab of resource page
    	checkElementPresent("ApplicationResources.lbl_ExternalAPIName","param_APIName_PARAM:" + varExternalAPIName,false,"null");
    	//AppOwner logs out
    	Common.biscomp_Logout(this);
    	if(dt_Scenario2_appCreationMethod.equals("Create")){
    	//Developer login
    	Common.biscomp_Login(this, varDevUsername+varDomain,varUserCommonPassword,varMainURL);
    	} else if(dt_Scenario2_appCreationMethod.equals("Upload")){
    	Common.biscomp_Login(this, varDevOpsUsername+varDomain,varUserCommonPassword,varMainURL);
    	}
    	Common.biscomp_SelectApp(this, varAppName);
    	//Wait for page loading
    	pause("3000");
    	clickAt("ApplicationHome.lnk_ResourceTab","10,10");
    	//Wait for page loading
    	pause("3000");
    	Common.biscomp_CreateDatabase(this, dt_Scenario2_dbName,dt_Scenario2_dbEnvironment,varPassword,dt_Scenario2_dbAllEnvNo);
    	String varDBName = retrieveString("keyDBName");
    	//Wait for page loading
    	pause("3000");
    	checkElementPresent("CreateNewDatabase.lnk_Database","param_AppStage_PARAM:" + dt_Scenario2_appStageDev + "_PARAM," + "param_DatabaseName_PARAM:" + varDBName + "_PARAM," + "param_UserName_PARAM:" + varDomainName,false,"null");
    	//Creating DB with advanced option checkbox checked
    	Common.biscomp_CreateDBWithDBUserAndDBTemplate(this, dt_Scenario2_dbName,dt_Scenario2_dbUsername,varPassword,dt_Scenario2_wrongPassword,dt_Scenario2_appStageDev,dt_Scenario2_dbUserPwdErrorMsg);
    	//Retrieving the DB Name,DB user name and DB Template name
    	String varDBName1 = retrieveString("keyDBName");
    	String varDBUsername1 = retrieveString("keyDBUsername");
    	String varDBTemplateName1 = retrieveString("keyDBTemplateName");
    	//Wait for page loading
    	pause("5000");
    	clickAt("ApplicationHome.lnk_Databases","10,10");
    	//Wait for page loading
    	pause("5000");
    	//database verification
    	//Verifying the created Databases
    	checkElementPresent("CreateNewDatabase.lnk_Database","param_AppStage_PARAM:" + dt_Scenario2_appStageDev + "_PARAM," + "param_DatabaseName_PARAM:" + varDBName + "_PARAM," + "param_UserName_PARAM:" + varDomainName,false,"null");
    	pause("2000");
    	checkElementPresent("CreateNewDatabase.lnk_Database","param_AppStage_PARAM:" + dt_Scenario2_appStageDev + "_PARAM," + "param_DatabaseName_PARAM:" + varDBName1 + "_PARAM," + "param_UserName_PARAM:" + varDomainName,false,"null");
    	clickAt("ApplicationHome.lnk_Databases","10,10");
    	//Wait for page loading
    	pause("5000");
    	//click on edit database
    	clickAt("CreateNewDatabase.btn_EditDatabase","param_DBStage_PARAM:" + dt_Scenario2_appStageDev + "_PARAM," + "paramDBname_PARAM:" + varDBName1 + "_PARAM," + "param_UserName_PARAM:" + varDomainName,"10,10");
    	//Verifying the created DB User
    	checkElementPresent("CreateNewDatabase.lbl_DBUser","param_DBUserName_PARAM:" + varDBUsername1,false,"");
    	clickAt("ApplicationHome.lnk_Databases","10,10");
    	//Wait for page loading
    	pause("3000");
    	//click on edit database
    	clickAt("CreateNewDatabase.btn_EditDatabase","param_DBStage_PARAM:" + dt_Scenario2_appStageDev + "_PARAM," + "paramDBname_PARAM:" + varDBName + "_PARAM," + "param_UserName_PARAM:" + varDomainName,"10,10");
    	//Creating DB User
    	Common.biscomp_AddDBUser(this, dt_Scenario2_passWord,dt_Scenario2_passWord,dt_Scenario2_appStageDev,varDBName,varDomainName);
    	String varDBUsername2 = retrieveString("keyDBUsername2");
    	clickAt("ApplicationHome.lnk_Databases","10,10");
    	Common.biscomp_UpdatingDeletingDB(this, varDBName,dt_Scenario2_appStageDev,varDBUsername1,varDomainName);
    	//Wait for page loading
    	pause("5000");
    	//delete DB user
    	Common.biscomp_DeleteDBUser(this, dt_Scenario2_appStageDev,varDBUsername2,varDomainName,dt_Scenario2_appStageDev,varDBName1);
    	//Updating and deleting databse templates
    	Common.biscomp_UpdateTemplate(this, dt_Scenario2_appStageDev,varDBName1,varDomainName);
    	if(varLanguageType.equals("Java")){
    	clickAt("ApplicationHome.lnk_ResourceTab","10,10");
    	//Wait for page loading
    	pause("2000");
    	//Creating Datasources
    	Common.biscomp_CreateDatasource(this, dt_Scenario2_dsName,dt_Scenario2_dsDesc,dt_Scenario2_dbDriver,dt_Scenario2_dsEnv,dt_Scenario2_dbURL,dt_Scenario2_dbUsername,varPassword);
    	String varDSNameDBTab = retrieveString("keyDSName");
    	//Verifying the added datasource is displayed at database tab
    	checkElementPresent("ApplicationResources.lbl_DBTabDatasourceName","param_DSName_PARAM:" + varDSNameDBTab,false,"null");
    	//Go to Dtasoursetab
    	clickAt("ApplicationResources.btn_DataSourse","10,10");
    	//Wait for page loading
    	pause("2000");
    	//Deleting the created datasource
    	clickAt("ApplicationResources.lnk_DatasourceStage","param_DSName_PARAM:" + varDSNameDBTab,"10,10");
    	//Wait for page loading
    	pause("2000");
    	clickAt("ApplicationResources.btn_DeleteDS","10,10");
    	clickAt("ApplicationResources.btn_OK","10,10");
    	//Wait for page loading
    	pause("5000");
    	//Verifying the Deleted database is not visible.
    	checkObjectProperty("ApplicationResources.lbl_DBTabDatasourceName","param_DSName_PARAM:" + varDSNameDBTab,"ELEMENTPRESENT",dt_Scenario2_dbNOtVisible,false,"null");
    	}
    	//Wait for page loading
    	pause("3000");
    	clickAt("ApplicationHome.lnk_ResourceTab","10,10");
    	//Wait for page loading
    	pause("3000");
    	//Verifying the created APi is visible for the developer
    	checkElementPresent("ApplicationResources.lbl_InternalAPIName","param_APIName_PARAM:" + dt_Scenario2_internalAPIName,false,"null");
    	//Verifying The created external API is listed in the API tab of resource page
    	checkElementPresent("ApplicationResources.lbl_ExternalAPIName","param_APIName_PARAM:" + varExternalAPIName,false,"null");
    	clickAt("ApplicationResources.lnk_APITab","10,10");
    	//Wait for page loading
    	pause("3000");
    	//Verifying only sandbox keys are displayed
    	checkObjectProperty("ApplicationResources.lbl_ProductionKey","ELEMENTPRESENT",dt_Scenario2_prodKeyVisibilityDev,false,"null");
    	checkObjectProperty("ApplicationResources.lbl_SandboxKey","ELEMENTPRESENT",dt_Scenario2_SandKeyVisibilityDev,false,"null");
    	if(dt_Scenario2_appCreationMethod.equals("Create")){
    	clickAt("ApplicationHome.lnk_LifeCycleManagement","10,10");
    	//Wait for page loading
    	pause("3000");
    	//Promoting the version to Testing
    	Common.biscomp_Promote(this, dt_Scenario2_appStageDev,dt_Scenario2_newVersionNumber);
    	//Verifying the promoted version is not list under development
    	Common.biscomp_VerifyingVersionNotListedUnderStage(this, dt_Scenario2_appStageDev);
    	//Verifying prmoted Version is listed under Testing stage
    	Common.biscomp_VerifyingVersionListedUnderStage(this, dt_Scenario2_appStageTest,dt_Scenario2_newVersionNumber,dt_Scenario2_versionVisibilityTrue);
    	clickAt("ApplicationLifeCycleManagement.ele_ddStage","10,10");
    	//Waiting for list loading
    	pause("3000");
    	clickAt("ApplicationLifeCycleManagement.lbl_Stage","param_Stage_PARAM:" + dt_Scenario2_appStageTest,"10,10");
    	//Waiting for list loading
    	pause("3000");
    	//Verifying the ETA save button is not available for develper at testing stage
    	checkObjectProperty("ApplicationLifeCycleManagement.btn_Save","param_VersionNumber_PARAM:" + dt_Scenario2_newVersionNumber,"ELEMENTPRESENT",dt_Scenario2_ETASaveTestingStageDev,false,"null");
    	//Dev Logs out
    	Common.biscomp_Logout(this);
    	//QA Logs in
    	Common.biscomp_Login(this, varQaUsername+varDomain,varUserCommonPassword,varMainURL);
    	Common.biscomp_SelectApp(this, varAppName);
    	//Wait for page loading
    	pause("3000");
    	clickAt("ApplicationHome.lnk_Databases","10,10");
    	//Wait for page loading
    	pause("3000");
    	//Wait for page loading
    	pause("3000");
    	checkElementPresent("CreateNewDatabase.lnk_Database","param_AppStage_PARAM:" + dt_Scenario2_appStageDev + "_PARAM," + "param_DatabaseName_PARAM:" + varDBName1 + "_PARAM," + "param_UserName_PARAM:" + varDomainName,false,"null");
    	//Wait for page loading
    	pause("3000");
    	clickAt("ApplicationHome.lnk_Databases","10,10");
    	//Wait for page loading
    	pause("5000");
    	clickAt("ApplicationResources.btn_AddNewDatabase","10,10");
    	String varDBName_QA = "DB"+generateData("Alphanumeric",3);
    	type("CreateNewDatabase.tf_DBName",varDBName_QA);
    	clickAt("CreateNewDatabase.ele_ddDBEnvironment","10,10");
    	//Verifying
    	checkObjectProperty("CreateNewDatabase.ele_ListItemDBEnvironment","param_DatabaseEnvironment_PARAM:" + dt_Scenario2_appStageDev,"ELEMENTPRESENT",dt_Scenario2_StageVisibilityFalse,false,"null");
    	checkObjectProperty("CreateNewDatabase.ele_ListItemDBEnvironment","param_DatabaseEnvironment_PARAM:" + dt_Scenario2_appStagePro,"ELEMENTPRESENT",dt_Scenario2_StageVisibilityFalse,false,"null");
    	checkElementPresent("CreateNewDatabase.ele_ListItemDBEnvironment","param_DatabaseEnvironment_PARAM:" + dt_Scenario2_appStageTest,false,"null");
    	clickAt("CreateNewDatabase.ele_ListItemDBEnvironment","param_DatabaseEnvironment_PARAM:" + dt_Scenario2_appStageTest,"10,10");
    	clickAt("CreateNewDatabase.chk_AdvancedOptions","10,10");
    	//Wait for page loading
    	pause("4000");
    	clickAt("CreateNewDatabase.ele_ddUser","1,1");
    	//Wait for list loading
    	pause("4000");
    	clickAt("CreateNewDatabase.lbl_CreateNewDBUser","10,10");
    	//Wait for page loading
    	pause("4000");
    	String varDBUsername_QA = "DBU"+generateData("Alphanumeric",2);
    	store("keyDBUsername_QA","String",varDBUsername_QA);
    	type("CreateNewDatabase.tf_NewUserUsername",varDBUsername_QA);
    	//Entering password
    	type("CreateNewDatabase.tf_NewUserPassword",varPassword);
    	type("CreateNewDatabase.tf_NewUserRepeatPassword",varPassword);
    	clickAt("CreateNewDatabase.btn_CreateDBUser","10,10");
    	pause("3000");
    	clickAt("CreateNewDatabase.ele_ddPermissionTemplate","1,1");
    	pause("3000");
    	clickAt("CreateNewDatabase.lbl_CreateNewDBTemplate","10,10");
    	String varDBTemplateName_QA = "DBT"+generateData("Alphanumeric",2);
    	store("keyDBTemplateName_QA","String",varDBTemplateName_QA);
    	type("CreateNewDatabase.tf_NewpermissionTemplateName",varDBTemplateName_QA);
    	clickAt("CreateNewDatabase.btn_CreateTemplate","10,10");
    	//Wait for DB Template creation
    	pause("3000");
    	clickAt("CreateNewDatabase.btn_CreateDatabase","10,10");
    	//Wait for DB creation
    	pause("5000");
    	checkElementPresent("CreateNewDatabase.lnk_Database","param_AppStage_PARAM:" + dt_Scenario2_appStageTest + "_PARAM," + "param_DatabaseName_PARAM:" + varDBName_QA + "_PARAM," + "param_UserName_PARAM:" + varDomainName,false,"null");
    	clickAt("ApplicationHome.lnk_Databases","10,10");
    	//Wait for page loading
    	pause("5000");
    	clickAt("CreateNewDatabase.btn_EditDatabase","param_DBStage_PARAM:" + dt_Scenario2_appStageTest + "_PARAM," + "paramDBname_PARAM:" + varDBName_QA + "_PARAM," + "param_UserName_PARAM:" + varDomainName,"10,10");
    	//check the user name
    	checkElementPresent("CreateNewDatabase.lbl_DBUser","param_DBUserName_PARAM:" + varDBUsername_QA,false,"null");
    	//Verifying the API Details
    	clickAt("ApplicationHome.lnk_ResourceTab","10,10");
    	//Wait for page loading
    	pause("5000");
    	checkElementPresent("ApplicationResources.lbl_InternalAPIName","param_APIName_PARAM:" + dt_Scenario2_internalAPIName,false,"null");
    	clickAt("ApplicationResources.lnk_APITab","10,10");
    	//Wait for page loading
    	pause("6000");
    	//Verifying only sandbox keys are displayed
    	checkObjectProperty("ApplicationResources.lbl_ProductionKey","ELEMENTPRESENT",dt_Scenario2_prodKeyVisibilityQa,false,"null");
    	checkObjectProperty("ApplicationResources.lbl_SandboxKey","ELEMENTPRESENT",dt_Scenario2_SandKeyVisibilityQa,false,"null");
    	pause("2000");
    	//Accessing the lifecycle managment Tab
    	clickAt("ApplicationHome.lnk_LifeCycleManagement","10,10");
    	//Wait for page loading
    	pause("2000");
    	//QA Promoting to production
    	Common.biscomp_Promote(this, dt_Scenario2_appStageTest,dt_Scenario2_newVersionNumber);
    	//Verifying the promoted version is not list under Testing
    	Common.biscomp_VerifyingVersionNotListedUnderStage(this, dt_Scenario2_appStageTest);
    	//Verifying prmoted Version is listed under Testing stage
    	Common.biscomp_VerifyingVersionListedUnderStage(this, dt_Scenario2_appStagePro,dt_Scenario2_newVersionNumber,dt_Scenario2_versionVisibilityTrue);
    	//QA logs out
    	Common.biscomp_Logout(this);
    	//DevOps logs in
    	Common.biscomp_Login(this, varDevOpsUsername+varDomain,varUserCommonPassword,varMainURL);
    	//Selecting the application
    	Common.biscomp_SelectApp(this, varAppName);
    	//Wait for app loading
    	pause("4000");
    	clickAt("ApplicationHome.lnk_Databases","10,10");
    	//Wait for page loading
    	pause("5000");
    	//Verifying DB, DB User andDB Template Created by Dev user are not visible to DevOps user
    	checkObjectProperty("ApplicationResources.lnk_DatabaseName","param_Appstage_PARAM:" + dt_Scenario2_appStageDev + "_PARAM," + "param_DBName_PARAM:" + varDBName1,"ELEMENTPRESENT",dt_Scenario2_DevDBVisibilityDevOps,false,"null");
    	checkObjectProperty("ApplicationResources.lnk_DBUsername","param_Appstage_PARAM:" + dt_Scenario2_appStageDev + "_PARAM," + "param_DBUsername_PARAM:" + varDBUsername1,"ELEMENTPRESENT",dt_Scenario2_DevDBUserVisibilityDevOps,false,"null");
    	checkObjectProperty("ApplicationResources.lnk_DBTemplateName","param_Appstage_PARAM:" + dt_Scenario2_appStageDev + "_PARAM," + "param_DBTemplateName_PARAM:" + varDBTemplateName1,"ELEMENTPRESENT",dt_Scenario2_DevDBTempVisibilityDevOps,false,"null");
    	//Verifying DB, DB User andDB Template Created by QA user are visible to DevOps user
    	checkElementPresent("CreateNewDatabase.lnk_Database","param_AppStage_PARAM:" + dt_Scenario2_appStageTest + "_PARAM," + "param_DatabaseName_PARAM:" + varDBName_QA + "_PARAM," + "param_UserName_PARAM:" + varDomainName,false,"null");
    	//Wait for page loading
    	pause("5000");
    	clickAt("ApplicationHome.lnk_Databases","10,10");
    	//Wait for page loading
    	pause("5000");
    	clickAt("ApplicationResources.btn_AddNewDatabase","10,10");
    	String varDBName_DevOps = "DB"+generateData("Alphanumeric",3);
    	type("CreateNewDatabase.tf_DBName",varDBName_DevOps);
    	clickAt("CreateNewDatabase.ele_ddDBEnvironment","10,10");
    	//Only 'Production' should be listed under environment drop down
    	checkObjectProperty("CreateNewDatabase.ele_ListItemDBEnvironment","param_DatabaseEnvironment_PARAM:" + dt_Scenario2_appStageDev,"ELEMENTPRESENT",dt_Scenario2_StageVisibilityFalse,false,"null");
    	checkObjectProperty("CreateNewDatabase.ele_ListItemDBEnvironment","param_DatabaseEnvironment_PARAM:" + dt_Scenario2_appStageTest,"ELEMENTPRESENT",dt_Scenario2_StageVisibilityFalse,false,"null");
    	checkElementPresent("CreateNewDatabase.ele_ListItemDBEnvironment","param_DatabaseEnvironment_PARAM:" + dt_Scenario2_appStagePro,false,"null");
    	clickAt("CreateNewDatabase.ele_ListItemDBEnvironment","param_DatabaseEnvironment_PARAM:" + dt_Scenario2_appStagePro,"10,10");
    	click("CreateNewDatabase.chk_AdvancedOptions");
    	//Wait for list loading
    	pause("2000");
    	clickAt("CreateNewDatabase.ele_ddUser","1,1");
    	//Wait for list loading
    	pause("2000");
    	clickAt("CreateNewDatabase.lbl_CreateNewDBUser","10,10");
    	//Wait for page loading
    	pause("2000");
    	String varDBUsername_DevOps = "DBU"+generateData("Alphanumeric",2);
    	store("keyDBUsername_DevOps","String",varDBUsername_DevOps);
    	type("CreateNewDatabase.tf_NewUserUsername",varDBUsername_DevOps);
    	//Entering password
    	type("CreateNewDatabase.tf_NewUserPassword",varPassword);
    	type("CreateNewDatabase.tf_NewUserRepeatPassword",varPassword);
    	clickAt("CreateNewDatabase.btn_CreateDBUser","10,10");
    	pause("3000");
    	clickAt("CreateNewDatabase.ele_ddPermissionTemplate","1,1");
    	clickAt("CreateNewDatabase.lbl_CreateNewDBTemplate","10,10");
    	String varDBTemplateName_DevOps = "DBT"+generateData("Alphanumeric",2);
    	store("keyDBTemplateName_DevOps","String",varDBTemplateName_DevOps);
    	type("CreateNewDatabase.tf_NewpermissionTemplateName",varDBTemplateName_DevOps);
    	clickAt("CreateNewDatabase.btn_CreateTemplate","10,10");
    	//Wait for DB Template creation
    	pause("3000");
    	clickAt("CreateNewDatabase.btn_CreateDatabase","10,10");
    	//Wait for db creation
    	pause("5000");
    	checkElementPresent("CreateNewDatabase.lnk_Database","param_AppStage_PARAM:" + dt_Scenario2_appStagePro + "_PARAM," + "param_DatabaseName_PARAM:" + varDBName_DevOps + "_PARAM," + "param_UserName_PARAM:" + varDomainName,false,"null");
    	clickAt("ApplicationHome.lnk_Databases","10,10");
    	//Wait for page loading
    	pause("5000");
    	clickAt("CreateNewDatabase.btn_EditDatabase","param_DBStage_PARAM:" + dt_Scenario2_appStagePro + "_PARAM," + "paramDBname_PARAM:" + varDBName_DevOps + "_PARAM," + "param_UserName_PARAM:" + varDomainName,"10,10");
    	//Wait for page loading
    	pause("3000");
    	checkElementPresent("CreateNewDatabase.lbl_DBUser","param_DBUserName_PARAM:" + varDBUsername_DevOps,false,"null");
    	//Verifying the API Details
    	clickAt("ApplicationHome.lnk_ResourceTab","10,10");
    	//Wait for page loading
    	pause("6000");
    	checkElementPresent("ApplicationResources.lbl_InternalAPIName","param_APIName_PARAM:" + dt_Scenario2_internalAPIName,false,"null");
    	clickAt("ApplicationResources.lnk_APITab","10,10");
    	//Wait for page loading
    	pause("3000");
    	//Verifying only sandbox keys are displayed
    	checkObjectProperty("ApplicationResources.lbl_ProductionKey","ELEMENTPRESENT",dt_Scenario2_prodKeyVisibilityDevOps,false,"null");
    	checkObjectProperty("ApplicationResources.lbl_SandboxKey","ELEMENTPRESENT",dt_Scenario2_SandKeyVisibilityDevOps,false,"null");
    	}
    	pause("2000");
    	clickAt("ApplicationHome.lnk_LifeCycleManagement","10,10");
    	//Wait for page loading
    	pause("3000");
    	//DevOps Retiring the version
    	Common.biscomp_Retire(this, dt_Scenario2_appStagePro,dt_Scenario2_newVersionNumber,dt_Scenario2_appCreationMethod);
    	//Devop logs out
    	Common.biscomp_Logout(this);
    	} else {
    	writeToReport("This Sections is not tested in this iteration");
    	}
    	} else if(!dt_Scenario2_IsExecuting){
    	String AppType = dt_Scenario2_appType;
    	writeToReport(AppType+" type is not tested in this iteration");
    	}
    } 
    	

    /**
     * Data provider for Test case tc_UploadApps.
     * @return data table
     */
    @DataProvider(name = "tc_UploadApps")
    public Object[][] dataTable_tc_UploadApps() {     	
    	return this.getDataTable("dt_UploadApp","initializeTenantLogin");
    }

    /**
     * Data driven test case tc_UploadApps.
     *
     * @throws Exception the exception
     */
    @VTAFRecoveryMethods(onerrorMethods = {}, recoveryMethods = {}) 
    @Test (dataProvider = "tc_UploadApps")
    public final void tc_UploadApps(final boolean dt_UploadApp_IsExecuting, final boolean dt_UploadApp_IsExecutingUploadApp, final String dt_UploadApp_appType, final String dt_UploadApp_adminUsername, final String dt_UploadApp_domainName, final String dt_UploadApp_defaultPassword, final String dt_UploadApp_loginErrorMsg, final String dt_UploadApp_dbUserPwdErrorMsg, final String dt_UploadApp_userListMain, final String dt_UploadApp_userListUploadApp, final String dt_UploadApp_userListEdit, final String dt_UploadApp_userListDelete, final String dt_UploadApp_usernames, final String dt_UploadApp_userInitials, final String dt_UploadApp_wrongPassword, final String dt_UploadApp_wrongUsername, final String dt_UploadApp_UsernameErrorMsg, final String dt_UploadApp_PasswordErrorMsg, final String dt_UploadApp_adminPassword, final String dt_UploadApp_appName, final String dt_UploadApp_appKey, final String dt_UploadApp_appDescription, final String dt_UploadApp_editDescription, final String dt_UploadApp_appImagePath, final String dt_UploadApp_editAppImagePath, final String dt_UploadApp_appPath, final String dt_UploadApp_appCreationMethod, final String dt_UploadApp_appCreationMsg, final String dt_UploadApp_gitMsg, final String dt_UploadApp_jenkinsMsg, final String dt_UploadApp_issueTrackerMsg, final String dt_UploadApp_cloudMsg, final String dt_UploadApp_sameAppKeyErrorMsg, final String dt_UploadApp_addMemberMsg, final String dt_UploadApp_removeMemberMsg, final String dt_UploadApp_wrongDateErrorMsg, final String dt_UploadApp_promoteDevTestMsg, final String dt_UploadApp_DeployTestMsg, final String dt_UploadApp_appStageDev, final String dt_UploadApp_appStageTest, final String dt_UploadApp_appStagePro, final String dt_UploadApp_actionPromote, final String dt_UploadApp_actionDemote, final String dt_UploadApp_OverviewTabVisibilityDev, final String dt_UploadApp_RepoandBuildTabVisibilityDev, final String dt_UploadApp_TeamTabVisibilityDev, final String dt_UploadApp_databaseTabVisibilityDev, final String dt_UploadApp_LifecycleManagementTabVisibilityDev, final String dt_UploadApp_ResourceTabVisibilityDev, final String dt_UploadApp_IssuesTabVisibilityDev, final String dt_UploadApp_LogsTabVisibilityDev, final String dt_UploadApp_buildStatus, final String dt_UploadApp_deployStatus, final String dt_UploadApp_versionnumber, final String dt_UploadApp_newVersionNumber, final String dt_UploadApp_buildNumber, final String dt_UploadApp_buildNumberOne, final String dt_UploadApp_buildNumberTwo, final String dt_UploadApp_buildNumberThree, final String dt_UploadApp_deployNumberTwo, final String dt_UploadApp_deployNumberOne, final String dt_UploadApp_dbName, final String dt_UploadApp_dbEnvironment, final String dt_UploadApp_dbAllEnvYes, final String dt_UploadApp_dbAllEnvNo, final String dt_UploadApp_dsName, final String dt_UploadApp_dbDriver, final String dt_UploadApp_dsEnv, final String dt_UploadApp_dbURL, final String dt_UploadApp_dbUsername, final String dt_UploadApp_dbPassword, final String dt_UploadApp_dsDesc, final String dt_UploadApp_dsPassword, final String dt_UploadApp_internalAPIName, final String dt_UploadApp_internalAPIVersion, final String dt_UploadApp_exAPIName, final String dt_UploadApp_authAPI, final String dt_UploadApp_exAPIURL, final String dt_UploadApp_propName, final String dt_UploadApp_proDesc, final String dt_UploadApp_propRegEnv, final String dt_UploadApp_proValue, final String dt_UploadApp_issueSummary, final String dt_UploadApp_issueDesc, final String dt_UploadApp_versionIssueRelatedTo, final String dt_UploadApp_issueType, final String dt_UploadApp_issueTypeNew, final String dt_UploadApp_issueTypeBug, final String dt_UploadApp_issuePriority, final String dt_UploadApp_issuePriorityNew, final String dt_UploadApp_issuePriorityNormal, final String dt_UploadApp_issueAsignee, final String dt_UploadApp_issueSeverity, final String dt_UploadApp_versionToDemote, final String dt_UploadApp_demoteComment, final String dt_UploadApp_issueStatusInitial, final String dt_UploadApp_issueStatusNew, final String dt_UploadApp_issueStatusResolved, final String dt_UploadApp_issueStatusFinal, final String dt_UploadApp_issueStatusClosed, final String dt_UploadApp_subDomain, final String dt_UploadApp_serviceName, final String dt_UploadApp_domain, final String dt_UploadApp_lawPauseLevel, final String dt_UploadApp_mediumPauseLevel, final String dt_UploadApp_highPauseLevel, final String dt_UploadApp_sampleText, final int dt_UploadApp_waitTimeForAppLoadingInSec, final String dt_UploadApp_appPathNew, final String dt_UploadApp_newUploadVersion, final String dt_UploadApp_dbTempPrivUnTicked, final String dt_UploadApp_dbTempPrivTicked, final String dt_UploadApp_dbNOtVisible, final String dt_UploadApp_dbVisible, final String dt_UploadApp_versionVisibilityTrue, final String dt_UploadApp_versionVisibilityFalse, final String dt_UploadApp_ETADisability, final String dt_UploadApp_StageVisibilityTrue, final String dt_UploadApp_StageVisibilityFalse, final String dt_UploadApp_addNewBtnVisibilityTrue, final String dt_UploadApp_addNewBtnVisibilityFalse, final String dt_UploadApp_OverviewTabVisibilityQA, final String dt_UploadApp_RepoandBuildTabVisibilityQA, final String dt_UploadApp_TeamTabVisibilityQA, final String dt_UploadApp_LifecycleManagementTabVisibilityQA, final String dt_UploadApp_ResourceTabVisibilityQA, final String dt_UploadApp_IssuesTabVisibilityQA, final String dt_UploadApp_databaseTabVisibilityQA, final String dt_UploadApp_LogsTabVisibilityQA, final String dt_UploadApp_OverviewTabVisibilityDEVOPS, final String dt_UploadApp_RepoandBuildTabVisibilityDEVOPS, final String dt_UploadApp_TeamTabVisibilityDEVOPS, final String dt_UploadApp_LifecycleManagementTabVisibilityDEVOPS, final String dt_UploadApp_ResourceTabVisibilityDEVOPS, final String dt_UploadApp_IssuesTabVisibilityDEVOPS, final String dt_UploadApp_databaseTabVisibilityDEVOPS, final String dt_UploadApp_LogsTabVisibilityDEVOPS, final String dt_UploadApp_ETASaveTestingStageDev, final String dt_UploadApp_appLogoSubPath, final String dt_UploadApp_appLogoMainPath, final String dt_UploadApp_prodKeyVisibilityDev, final String dt_UploadApp_prodKeyVisibilityQa, final String dt_UploadApp_prodKeyVisibilityDevOps, final String dt_UploadApp_prodKeyVisibilityAppOwner, final String dt_UploadApp_SandKeyVisibilityDev, final String dt_UploadApp_SandKeyVisibilityQa, final String dt_UploadApp_SandKeyVisibilityDevOps, final String dt_UploadApp_SandKeyVisibilityAppOwner, final String dt_UploadApp_DevDBVisibilityDevOps, final String dt_UploadApp_DevDBUserVisibilityDevOps, final String dt_UploadApp_DevDBTempVisibilityDevOps, final String dt_UploadApp_passWord, final String initializeTenantLogin_TenantAdminName, final String initializeTenantLogin_TenantAdminPassword, final String initializeTenantLogin_TenantDomain, final String initializeTenantLogin_AppFactoryUserCommonPassword, final String initializeTenantLogin_mainURL, final String initializeTenantLogin_defaultPassword) throws Exception {	
    	//Checking the executing status of the application type
    	if(dt_UploadApp_IsExecuting){
    	if(dt_UploadApp_IsExecutingUploadApp){
    	if(dt_UploadApp_appCreationMethod.equals("Upload")){
    	String varMainURL = retrieveString("AFMainURL");
    	String varTenantAdmin_Password = retrieveString("AFTenantAdmin_Password");
    	String varUserName = retrieveString(dt_UploadApp_adminUsername);
    	String varUserCommonPassword = retrieveString("AFUserCommonPassword");
    	String varPassword = retrieveString("AFDefaultPassword");
    	String varDomainName = retrieveString("AFTenantDomain");
    	String varDomain = "@"+varDomainName+".com";
    	Common.biscomp_LoginInvalid(this, varUserName+varDomain,dt_UploadApp_wrongPassword,dt_UploadApp_loginErrorMsg,varMainURL);
    	Common.biscomp_Login(this, varUserName+varDomain,varTenantAdmin_Password,varMainURL);
    	//Waiting for page loading
    	pause("6000");
    	//Assigning users names
    	Common.biscomp_AssigningUsernames(this, dt_UploadApp_userListUploadApp,dt_UploadApp_userInitials);
    	String varUserList = retrieveString("keyUserList");
    	//Waiting for page loading
    	pause("6000");
    	//Importing users
    	Common.biscomp_ImportMembers(this, varUserList,varUserCommonPassword,varUserCommonPassword);
    	//Wait for users to be loaded
    	pause("5000");
    	//Assigning roles to the imported users
    	Common.biscomp_AssignUserRoles(this, varUserList);
    	//Waiting for page loading
    	pause("6000");
    	//Assigning user names, importing and changing the roles
    	Common.biscomp_AssigningUsernames(this, dt_UploadApp_userListEdit,dt_UploadApp_userInitials);
    	String varUserList_EditRole = retrieveString("keyUserList");
    	//Waiting for page loading
    	pause("6000");
    	Common.biscomp_ImportMembers(this, varUserList_EditRole,varUserCommonPassword,varUserCommonPassword);
    	//Waiting for page loading
    	pause("6000");
    	//Assigning a role
    	Common.biscomp_AssignUserRoles(this, varUserList_EditRole);
    	//Waiting for page loading
    	pause("6000");
    	Common.biscomp_ChangingUserRole(this, varUserList_EditRole);
    	//Waiting for page loading
    	pause("6000");
    	//Assigning user name for removing user
    	Common.biscomp_AssigningUsernames(this, dt_UploadApp_userListDelete,dt_UploadApp_userInitials);
    	String varUserList_RemoveUser = retrieveString("keyUserList");
    	//Waiting for page loading
    	pause("6000");
    	Common.biscomp_ImportMembers(this, varUserList_RemoveUser,varUserCommonPassword,varUserCommonPassword);
    	//Waiting for page loading
    	pause("6000");
    	//removing user
    	Common.biscomp_RemoveUser(this, varUserList_RemoveUser);
    	//Waiting for page loading
    	pause("4000");
    	//Importing user with specail includes in the username
    	Common.biscomp_ImportMembers(this, dt_UploadApp_wrongUsername,varUserCommonPassword,varUserCommonPassword);
    	//wating for error message
    	pause("2500");
    	//Verifying the error message
    	checkObjectProperty("ImportMembers.lbl_ErrorMsg","textContent",dt_UploadApp_UsernameErrorMsg,false,"null");
    	clickAt("HomePage.btn_Home","0,0");
    	//Waiting for page loading
    	pause("2000");
    	//Importing user with different passwords
    	Common.biscomp_ImportMembers(this, dt_UploadApp_userListEdit,varUserCommonPassword,dt_UploadApp_wrongPassword);
    	//wait for error message
    	checkObjectProperty("ImportMembers.lbl_PwdErrorMsg","textContent",dt_UploadApp_PasswordErrorMsg,false,"null");
    	//Tenantt admin login out
    	Common.biscomp_Logout(this);
    	String varDevOpsUsername = retrieveString("AF_DevOps");
    	String varAppOwnerUsername = retrieveString("AF_AppOwner");
    	String varAppOwnerPassword = retrieveString("AFDefaultPassword");
    	//App Owner login in to system
    	Common.biscomp_Login(this, varAppOwnerUsername+varDomain,varUserCommonPassword,varMainURL);
    	//Wait for page loading
    	pause("5000");
    	Common.biscomp_CreateApplication(this, dt_UploadApp_appName,dt_UploadApp_appKey,dt_UploadApp_appDescription,dt_UploadApp_appImagePath,dt_UploadApp_appType,dt_UploadApp_appPath,dt_UploadApp_appCreationMethod);
    	String varAppName = retrieveString("keyAppName");
    	String varAppKey = retrieveString("keyAppKey");
    	keyPress("HomePage.btn_Home","F5");
    	//Wait for application loading
    	pause("3000");
    	//Verifying created application is listed in the home page
    	Common.biscomp_VerifyingCreatedApplication(this, varAppName,dt_UploadApp_appType,dt_UploadApp_waitTimeForAppLoadingInSec);
    	//Refreshing the page for wall notification
    	keyPress("HomePage.btn_Home","F5");
    	//Wait for wall notification laoding
    	pause("3000");
    	//Verifying the posts on notification wall
    	Common.biscomp_VerifyingNotificationWallPost(this, dt_UploadApp_appCreationMsg,dt_UploadApp_gitMsg,dt_UploadApp_jenkinsMsg,dt_UploadApp_issueTrackerMsg,dt_UploadApp_cloudMsg,varAppName,varAppKey);
    	/*
    				Call
    					businessComponent=Common.biscomp_CreateAppWithSameAppKey
    					param_AppKey=@varAppKey
    					param_SameAppKeyErrorMsg=@dt_UploadApp_sameAppKeyErrorMsg
    	*/
    	Common.biscomp_SelectApp(this, varAppName);
    	//Wait for page loading
    	pause("10000");
    	Common.biscomp_AddMembers(this, varUserList,dt_UploadApp_addMemberMsg,varAppOwnerUsername);
    	Common.biscomp_AddMembers(this, varUserList_EditRole,dt_UploadApp_addMemberMsg,varUserCommonPassword);
    	//Removing a user from the application
    	Common.biscomp_RemoveUserFromApp(this, varUserList_EditRole,dt_UploadApp_removeMemberMsg);
    	//Editing the application image and description
    	Common.biscomp_EditApplicationData(this, dt_UploadApp_editAppImagePath,dt_UploadApp_editDescription,dt_UploadApp_appLogoSubPath,dt_UploadApp_appLogoMainPath);
    	//Wait for page loading
    	pause("4000");
    	//AppOwner loginOut
    	Common.biscomp_Logout(this);
    	//DevOps Login
    	Common.biscomp_Login(this, varDevOpsUsername+varDomain,varUserCommonPassword,varMainURL);
    	//Wait for page loading
    	pause("8000");
    	Common.biscomp_SelectApp(this, varAppName);
    	//DevOps Wall notttification Verification
    	String NotificationWallMsg3 = dt_UploadApp_versionIssueRelatedTo+" deployed in "+dt_UploadApp_appStagePro+" stage";
    	checkElementPresent("Common.lbl_NotificationWallMsg","param_Message_PARAM:" + NotificationWallMsg3,false,"null");
    	//Wait for page loading
    	pause("8000");
    	Common.biscomp_LaunchingAtOverviewPage(this, dt_UploadApp_versionToDemote,dt_UploadApp_sampleText,dt_UploadApp_waitTimeForAppLoadingInSec,dt_UploadApp_appType,varAppKey,varDomainName,dt_UploadApp_versionIssueRelatedTo);
    	selectWindow("Window.win_NewWindow","param_NewWindowNumber_PARAM:0");
    	//Wait for page loading
    	pause("4000");
    	//DevOps Log out
    	Common.biscomp_Logout(this);
    	//Wait for page loading
    	pause("2000");
    	} else {
    	writeToReport("This is not upload type application");
    	}
    	} else {
    	writeToReport("This Sections is not tested in this iteration");
    	}
    	} else if(!dt_UploadApp_IsExecuting){
    	String AppType = dt_UploadApp_appType;
    	writeToReport(AppType+" type is not tested in this iteration");
    	}
    } 
    	

    public final Object[][] getDataTable(final String... tableNames) {
        String[] tables = tableNames;
        return this.getTableArray(getVirtualDataTable(tables));
    }

}