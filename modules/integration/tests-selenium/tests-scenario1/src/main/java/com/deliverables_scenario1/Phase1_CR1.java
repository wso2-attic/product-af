package com.deliverables_scenario1;

import java.util.HashMap;
import java.util.List;

import com.virtusa.isq.vtaf.aspects.VTAFRecoveryMethods;
import com.virtusa.isq.vtaf.runtime.SeleniumTestBase;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import com.virtusa.isq.vtaf.runtime.VTAFTestListener;


/**
 *  Class Phase1_CR1 implements corresponding test suite
 *  Each test case is a test method in this class.
 */

@Listeners (VTAFTestListener.class)
public class Phase1_CR1 extends SeleniumTestBase {



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
    public final void initialization(final String initializeTenantLogin_TenantAdminName, final String initializeTenantLogin_TenantAdminPassword, final String initializeTenantLogin_TenantDomain, final String initializeTenantLogin_AppFactoryUserCommonPassword, final String initializeTenantLogin_mainURL, final String initializeTenantLogin_DefaultPassword) throws Exception {	
    	store("AFTenantAdmin_Name","String",initializeTenantLogin_TenantAdminName);
    	store("AFTenantAdmin_Password","String",initializeTenantLogin_TenantAdminPassword);
    	store("AFTenantDomain","String",initializeTenantLogin_TenantDomain);
    	store("AFUserCommonPassword","String",initializeTenantLogin_AppFactoryUserCommonPassword);
    	store("AFMainURL","String",initializeTenantLogin_mainURL);
    	store("AFDefaultPassword","String",initializeTenantLogin_DefaultPassword);
    } 
    	

    /**
     * Data provider for Test case tenantAdminScenario1.
     * @return data table
     */
    @DataProvider(name = "tenantAdminScenario1")
    public Object[][] dataTable_tenantAdminScenario1() {     	
    	return this.getDataTable("dtScenario1","initializeTenantLogin");
    }

    /**
     * Data driven test case tenantAdminScenario1.
     *
     * @throws Exception the exception
     */
    @VTAFRecoveryMethods(onerrorMethods = {}, recoveryMethods = {}) 
    @Test (dataProvider = "tenantAdminScenario1")
    public final void tenantAdminScenario1(final boolean dtScenario1_IsExecuting, final boolean dtScenario1_cloudEnvExecution, final String dtScenario1_appType, final String dtScenario1_adminUsername, final String dtScenario1_domainName, final String dtScenario1_defaultPassword, final String dtScenario1_userList, final String dtScenario1_cloudUser, final String dtScenario1_usernames, final String dtScenario1_userInitials, final String dtScenario1_adminPassword, final String dtScenario1_appName, final String dtScenario1_appKey, final String dtScenario1_appDescription, final String dtScenario1_appImagePath, final String dtScenario1_appPath, final String dtScenario1_appCreationMethod, final String dtScenario1_appStageDev, final String dtScenario1_appStageTest, final String dtScenario1_appStagePro, final String dtScenario1_buildStatus, final String dtScenario1_deployStatus, final String dtScenario1_versionnumber, final String dtScenario1_newVersionNumber, final String dtScenario1_versionRetire, final String dtScenario1_buildNumber, final String dtScenario1_deployNumber, final String dtScenario1_dbName, final String dtScenario1_dbEnvironment, final String dtScenario1_dbAllEnvYes, final String dtScenario1_dbAllEnvNo, final String dtScenario1_dsName, final String dtScenario1_dbDriver, final String dtScenario1_dsEnv, final String dtScenario1_dbURL, final String dtScenario1_dbUsername, final String dtScenario1_dbPassword, final String dtScenario1_dsDesc, final String dtScenario1_dsPassword, final String dtScenario1_internalAPIName, final String dtScenario1_internalAPIVersion, final String dtScenario1_exAPIName, final String dtScenario1_authAPI, final String dtScenario1_exAPIStage, final String dtScenario1_exAPIURL, final String dtScenario1_propName, final String dtScenario1_proDesc, final String dtScenario1_propRegEnv, final String dtScenario1_proValue, final String dtScenario1_issueSummary, final String dtScenario1_issueDesc, final String dtScenario1_versionIssueRelatedTo, final String dtScenario1_issueType, final String dtScenario1_issuePriority, final String dtScenario1_issueAsignee, final String dtScenario1_issueSeverity, final String dtScenario1_versionToDemote, final String dtScenario1_demoteComment, final String dtScenario1_versionVisibilityTrue, final String dtScenario1_versionVisibilityFalse, final String dtScenario1_issueStatusInitial, final String dtScenario1_issueStatusNew, final String dtScenario1_issueStatusResolved, final String dtScenario1_subDomain, final String dtScenario1_serviceName, final String dtScenario1_domain, final String dtScenario1_lawPauseLevel, final String dtScenario1_mediumPauseLevel, final String dtScenario1_highPauseLevel, final String dtScenario1_sampleText, final int dtScenario1_waitTimeForAppLoadingInSec, final String dtScenario1_appPathNew, final String dtScenario1_newUploadVersion, final String initializeTenantLogin_TenantAdminName, final String initializeTenantLogin_TenantAdminPassword, final String initializeTenantLogin_TenantDomain, final String initializeTenantLogin_AppFactoryUserCommonPassword, final String initializeTenantLogin_mainURL, final String initializeTenantLogin_DefaultPassword) throws Exception {	
    	//Checking the executing status of the application type
    	if(dtScenario1_IsExecuting){
    	//Retrive values
    	String varMainURL = retrieveString("AFMainURL");
    	String varUserName = retrieveString("AFTenantAdmin_Name");
    	String varPassword = retrieveString("AFDefaultPassword");
    	String varTenantAdmin_Password = retrieveString("AFTenantAdmin_Password");
    	String varUserCommonPassword = retrieveString("AFUserCommonPassword");
    	String varDomainName = retrieveString("AFTenantDomain");
    	String varDomain = "@"+varDomainName+".com";
    	/*
    	SetVariable
    	  name=varDomainNameDB
    	  type=String
    	  paramValue=varDomainName
    	*/
    	if(dtScenario1_appType.equals("Java Web Application")){
    	String varLanguageType = "Java";
    	store("keyLanguageType","String",varLanguageType);
    	} else if(dtScenario1_appType.equals("JAX-RS Service")){
    	String varLanguageType = "Java";
    	store("keyLanguageType","String",varLanguageType);
    	} else if(dtScenario1_appType.equals("JAX-WS Service")){
    	String varLanguageType = "Java";
    	store("keyLanguageType","String",varLanguageType);
    	} else if(dtScenario1_appType.equals("Jaggery Application")){
    	String varLanguageType = "Java";
    	store("keyLanguageType","String",varLanguageType);
    	} else if(dtScenario1_appType.equals("WSO2 Data Service")){
    	String varLanguageType = "Java";
    	store("keyLanguageType","String",varLanguageType);
    	} else if(dtScenario1_appType.equals("WAR")){
    	String varLanguageType = "NonJava";
    	store("keyLanguageType","String",varLanguageType);
    	} else if(dtScenario1_appType.equals("Jaggery App")){
    	String varLanguageType = "NonJava";
    	store("keyLanguageType","String",varLanguageType);
    	}
    	String varLanguageType = retrieveString("keyLanguageType");
    	if(dtScenario1_cloudEnvExecution){
    	Common.biscomp_LoginCloud(this);
    	} else if(!dtScenario1_cloudEnvExecution){
    	//Tenan Admin user login in to the System
    	Common.biscomp_Login(this, varUserName+varDomain,varTenantAdmin_Password,varMainURL);
    	//Waiting for page load
    	pause("5000");
    	//Assigning usernames
    	Common.biscomp_AssigningUsernames(this, dtScenario1_userList,dtScenario1_userInitials);
    	String varUserList = retrieveString("keyUserList");
    	//Importing users
    	Common.biscomp_ImportMembers(this, varUserList,dtScenario1_defaultPassword,dtScenario1_defaultPassword);
    	//Wait for application loading
    	pause(dtScenario1_lawPauseLevel);
    	Common.biscomp_AssignUserRoles(this, varUserList);
    	click("HomePage.btn_Home");
    	}
    	//Creating a new application
    	Common.biscomp_CreateApplication(this, dtScenario1_appName,dtScenario1_appKey,dtScenario1_appDescription,dtScenario1_appImagePath,dtScenario1_appType,dtScenario1_appPath,dtScenario1_appCreationMethod);
    	String varAppName = retrieveString("keyAppName");
    	String varAppKey = retrieveString("keyAppKey");
    	//Wait for application loading
    	pause("3000");
    	//Verifying created application is listed in the home page
    	Common.biscomp_VerifyingCreatedApplication(this, varAppName,dtScenario1_appType,dtScenario1_waitTimeForAppLoadingInSec);
    	Common.biscomp_SelectApp(this, varAppName);
    	if(dtScenario1_appCreationMethod.equals("Create")){
    	clickAt("ApplicationHome.lnk_RepoandBuild","1,1");
    	//Wait for application loading
    	pause("8000");
    	//Verifying te build and deploy status of trunk
    	Common.biscomp_VerifyingBuildDeploystatus(this, dtScenario1_appStageDev,dtScenario1_buildStatus,dtScenario1_deployStatus,dtScenario1_versionnumber);
    	//Wait for application loading
    	pause("8000");
    	//Launching the Trunk
    	Common.biscomp_LaunchingApp(this, dtScenario1_appStageDev,dtScenario1_versionnumber,dtScenario1_sampleText,dtScenario1_appType);
    	selectWindow("Window.win_NewWindow","param_NewWindowNumber_PARAM:0");
    	}
    	String varUserList = retrieveString("keyUserList");
    	if(dtScenario1_cloudEnvExecution){
    	Common.biscomp_AddMembers(this, dtScenario1_cloudUser,"invited to the application");
    	}
    	//Adding members to the application
    	Common.biscomp_AddMembers(this, varUserList,"invited to the application");
    	if(dtScenario1_appCreationMethod.equals("Upload")){
    	Common.biscomp_CreateVersionUploadApp(this, dtScenario1_appPathNew,dtScenario1_newUploadVersion);
    	} else if(dtScenario1_appCreationMethod.equals("Create")){
    	/*
    	//create dummy branch for unmapping
    			Call
    				businessComponent=Common.biscomp_CreateBranch
    				param_AppStage=@dtScenario1_appStageDev
    				param_VersionNumber=@dtScenario1_versionnumber
    				param_NewVersionNumber=@dtScenario1_versionRetire
    	//Wait for application loading
    			Pause
    				ms=3000
    			ClickAt
    				object=ApplicationHome.lnk_LifeCycleManagement
    				coordinates=10,10
    			Call
    				businessComponent=Common.biscomp_Promote
    				param_Stage=@dtScenario1_appStageDev
    				param_versionNumber=@dtScenario1_versionRetire
    			Call
    				businessComponent=Common.biscomp_Promote
    				param_Stage=@dtScenario1_appStageTest
    				param_versionNumber=@dtScenario1_versionRetire
    	*/
    	//Waiting for page load
    	pause("5000");
    	//Creating the branch
    	Common.biscomp_CreateBranch(this, dtScenario1_appStageDev,dtScenario1_versionnumber,dtScenario1_newVersionNumber);
    	//Verifying the build and deploy status of created branch
    	Common.biscomp_VerifyingBuildDeploystatus(this, dtScenario1_appStageDev,dtScenario1_buildStatus,dtScenario1_deployStatus,dtScenario1_newVersionNumber);
    	//Launching the created branch
    	Common.biscomp_LaunchingApp(this, dtScenario1_appStageDev,dtScenario1_newVersionNumber,dtScenario1_sampleText,dtScenario1_appType);
    	selectWindow("Window.win_NewWindow","param_NewWindowNumber_PARAM:0");
    	if(!dtScenario1_appType.equals("WSO2 Data Service")){
    	//Manually building and deploying the branch
    	Common.biscomp_ManualBuildAndDeploy(this, dtScenario1_highPauseLevel,dtScenario1_buildNumber,dtScenario1_buildStatus,dtScenario1_deployStatus,dtScenario1_deployNumber);
    	//Wait for aplication deploy
    	pause("8000");
    	//Launching the version after manually building and deploying
    	Common.biscomp_LaunchingApp(this, dtScenario1_appStageDev,dtScenario1_newVersionNumber,dtScenario1_sampleText,dtScenario1_appType);
    	selectWindow("Window.win_NewWindow","param_NewWindowNumber_PARAM:0");
    	}
    	}
    	//Creating a database
    	Common.biscomp_CreateDBWithDBUserAndDBTemplate(this, dtScenario1_dbPassword,dtScenario1_dbEnvironment);
    	String varDBName = retrieveString("keyDBName");
    	String varDBUsername = retrieveString("keyDBUsername");
    	//Wait for page load
    	pause("5000");
    	//Verifying the created database
    	Common.biscomp_VerifyingDatabases(this, varDBName,varDomainName,dtScenario1_dbEnvironment,varDBUsername);
    	//Accessing the resource tab
    	clickAt("ApplicationHome.lnk_ResourceTab","10,10");
    	//Wait for page load
    	pause("5000");
    	if(varLanguageType.equals("Java")){
    	//Creating a datasource at overview tab in the resource page
    	Common.biscomp_CreateDatasource(this, dtScenario1_dsName,dtScenario1_dsDesc,dtScenario1_dbDriver,dtScenario1_dsEnv,dtScenario1_dbURL,dtScenario1_dbUsername,dtScenario1_dsPassword);
    	String varDSNameOverviewTab = retrieveString("keyDSName");
    	store("keyDSNameOverviewTab","String",varDSNameOverviewTab);
    	//Wait for page load
    	pause("5000");
    	//Verifying the added datasource is displayed at database tab
    	checkElementPresent("ApplicationResources.lbl_DBTabDatasourceName","param_DSName_PARAM:" + varDSNameOverviewTab,false,"");
    	clickAt("ApplicationResources.ele_tabOverview","10,10");
    	//Wait for page load
    	pause("5000");
    	//Verifying  the added datasource is displayed at overview tab
    	checkElementPresent("ApplicationResources.lbl_OverviewTabDatasourceName","param_DSName_PARAM:" + varDSNameOverviewTab,false,"");
    	//Creating a datasource at database tab in the resource page
    	clickAt("ApplicationResources.btn_DataSourse","10,10");
    	//Wait for page loa
    	pause("5000");
    	Common.biscomp_CreateDatasource(this, dtScenario1_dsName,dtScenario1_dsDesc,dtScenario1_dbDriver,dtScenario1_dsEnv,dtScenario1_dbURL,dtScenario1_dbUsername,dtScenario1_dsPassword);
    	String varDSNameDBTab = retrieveString("keyDSName");
    	store("keyDSName","String",varDSNameDBTab);
    	//Verifying the added datasource is displayed at database tab
    	checkElementPresent("ApplicationResources.lbl_DBTabDatasourceName","param_DSName_PARAM:" + varDSNameDBTab,false,"");
    	clickAt("ApplicationResources.ele_tabOverview","10,10");
    	//Wait for page load
    	pause("5000");
    	//Verifying  the added datasource is displayed at overview tab
    	checkElementPresent("ApplicationResources.lbl_OverviewTabDatasourceName","param_DSName_PARAM:" + varDSNameDBTab,false,"");
    	}
    	if(varLanguageType.equals("NonJava")){
    	clickAt("ApplicationHome.lnk_ResourceTab","10,10");
    	//Wait for page loading
    	pause("3000");
    	checkObjectProperty("ApplicationResources.btn_AddDatasource","ELEMENTPRESENT","false",false,"");
    	clickAt("ApplicationResources.lnk_Databases","10,10");
    	//Wait for page loading
    	pause("3000");
    	checkObjectProperty("ApplicationResources.btn_AddNewDatasource","ELEMENTPRESENT","false",false,"");
    	}
    	String varDSNameOverviewTab = retrieveString("keyDSNameOverviewTab");
    	//Creating a Internal API
    	Common.biscomp_CreatingInternalAPI(this, dtScenario1_internalAPIName,dtScenario1_internalAPIVersion,varAppKey);
    	//Retrieving production and sandbox keys
    	String varProductionKey = retrieveString("keyProductionKey");
    	String varSandbox = retrieveString("keySandboxKey");
    	selectWindow("Window.win_NewWindow","param_NewWindowNumber_PARAM:0");
    	clickAt("ApplicationHome.lnk_ResourceTab","10,10");
    	clickAt("ApplicationResources.lnk_APITab","10,10");
    	//Verifying the created aPI is displayed at API tab in th resource page
    	checkElementPresent("ApplicationResources.lbl_InternalAPIName","param_APIName_PARAM:" + dtScenario1_internalAPIName,false,"");
    	//Synchronising generated production and sandbox keys
    	click("ApplicationResources.btn_SyncKeys");
    	//Waiting for keys to be synchronising
    	pause("5000");
    	//Verifying the production key and Sandbox key
    	checkObjectProperty("ApplicationResources.lbl_ProductionKey","textContent",varProductionKey,false,"");
    	checkObjectProperty("ApplicationResources.lbl_SandboxKey","textContent",varSandbox,false,"");
    	//Creating External API
    	Common.biscomp_CreatingExternalAPI(this, dtScenario1_exAPIName,dtScenario1_exAPIStage,dtScenario1_authAPI,dtScenario1_exAPIURL);
    	String varExternalAPIName = retrieveString("keyExternalAPIName");
    	//Verifying The created external API is listed in the API tab of resource page
    	checkElementPresent("ApplicationResources.lbl_ExternalAPIName","param_APIName_PARAM:" + varExternalAPIName,false,"");
    	clickAt("ApplicationHome.lnk_ResourceTab","10,10");
    	//Wait for page load
    	pause("5000");
    	//Creating a property at overview tab in the resource page
    	Common.biscomp_CreatingProperty(this, dtScenario1_propName,dtScenario1_propRegEnv,dtScenario1_proDesc,dtScenario1_proValue,dtScenario1_dbAllEnvNo);
    	String varPropName_overviewTab = retrieveString("KeyPropertyName");
    	//Verifying the created property displayed at overview tab in the resource tab
    	checkElementPresent("ApplicationResources.lbl_PropertyName","param_PropertyName_PARAM:" + varPropName_overviewTab,false,"");
    	//Accessing Properties tab
    	clickAt("ApplicationResources.lnk_PropertiesTab","10,10");
    	//Verifying the created property is listed in the properties tab of resource page
    	checkElementPresent("ApplicationResources.lbl_PropertyName","param_PropertyName_PARAM:" + varPropName_overviewTab,false,"");
    	//Creating a property at Properties tab in the resource page
    	Common.biscomp_CreatingProperty(this, dtScenario1_propName,dtScenario1_propRegEnv,dtScenario1_proDesc,dtScenario1_proValue,dtScenario1_dbAllEnvNo);
    	String varPropName_propertiesTab = retrieveString("KeyPropertyName");
    	//Verifying the created property is listed in the properties tab of resource page
    	checkElementPresent("ApplicationResources.lbl_PropertyName","param_PropertyName_PARAM:" + varPropName_propertiesTab,false,"");
    	clickAt("ApplicationHome.lnk_ResourceTab","10,10");
    	//Wait for page loading
    	pause("5000");
    	//Verifying the created property is listed in the overview tab of resource page
    	checkElementPresent("ApplicationResources.lbl_PropertyName","param_PropertyName_PARAM:" + varPropName_propertiesTab,false,"");
    	//Creating properties in all environment
    	clickAt("ApplicationHome.lnk_ResourceTab","10,10");
    	//Wait for page loading
    	pause("5000");
    	Common.biscomp_CreatingProperty(this, dtScenario1_propName,"",dtScenario1_proDesc,dtScenario1_proValue,dtScenario1_dbAllEnvYes);
    	String varPropName_propertiesAllEnv = retrieveString("KeyPropertyName");
    	clickAt("ApplicationResources.lnk_PropertiesTab","10,10");
    	//Wait for page loading
    	pause("5000");
    	Common.biscomp_VerifyingPropertiesInAllEnv(this, varPropName_propertiesAllEnv,dtScenario1_appStageDev,dtScenario1_appStageTest,dtScenario1_appStagePro);
    	clickAt("ApplicationHome.lnk_ResourceTab","10,10");
    	//Wait for page loading
    	pause("5000");
    	//Creating databse in all environments
    	Common.biscomp_CreateDatabase(this, "",dtScenario1_dbPassword,dtScenario1_dbAllEnvYes);
    	//Wait for page loading
    	pause("10000");
    	String varDBNameAllEnv = retrieveString("keyDBName");
    	//verify database in all environment
    	Common.biscomp_VerifyingDatabaseAllEnv(this, varDBNameAllEnv,dtScenario1_appStageDev,dtScenario1_appStageTest,dtScenario1_appStagePro,varDomainName);
    	if(dtScenario1_appCreationMethod.equals("Create")){
    	clickAt("ApplicationHome.lnk_LifeCycleManagement","10,10");
    	//Wait for page loading
    	pause("5000");
    	//Promoting the version
    	Common.biscomp_Promote(this, dtScenario1_appStageDev,dtScenario1_newVersionNumber);
    	//WAit for page load
    	pause("5000");
    	Common.biscomp_VerifyingVersionListedUnderStage(this, dtScenario1_appStageTest,dtScenario1_newVersionNumber,dtScenario1_versionVisibilityTrue);
    	//Verifying all the resourses are mapped to new stage
    	Common.biscomp_VerifyingResourcesAfterPromoting(this, varDSNameOverviewTab,varPropName_overviewTab,dtScenario1_appStageDev,varLanguageType);
    	}
    	//Creating an issue
    	Common.biscomp_CreateIssue(this, dtScenario1_issueSummary,dtScenario1_issueDesc,dtScenario1_newVersionNumber,dtScenario1_issueType,dtScenario1_issuePriority,dtScenario1_issueStatusInitial,dtScenario1_issueAsignee,dtScenario1_issueSeverity);
    	//Verifying the created issue is listed under issues
    	String varIssueSummary = retrieveString("keyIssueSummary");
    	clickAt("ApplicationHome.lnk_Issues","10,10");
    	selectFrame("frames.frm_Parent");
    	selectFrame("frames.frm_iframe");
    	checkObjectProperty("ApplicationIssues.ele_CreatedIssue","textContent",varIssueSummary,false,"");
    	selectFrame("frames.frm_Parent");
    	if(dtScenario1_appCreationMethod.equals("Create")){
    	clickAt("ApplicationHome.lnk_LifeCycleManagement","10,10");
    	//wait for page loading
    	pause("5000");
    	//Demoting the Veriosn
    	Common.biscomp_Demote(this, dtScenario1_appStageTest,dtScenario1_newVersionNumber,varIssueSummary,dtScenario1_demoteComment);
    	String varDemoteComment = retrieveString("keyDemoteComment");
    	}
    	//Resolving the created issue
    	Common.biscomp_ResolvingIssue(this, varIssueSummary,dtScenario1_issueStatusNew);
    	//Verifying the status is upted correctly for the resolved issue
    	checkObjectProperty("ApplicationIssues.ele_IssueStatus","param_IssueSummary_PARAM:" + varIssueSummary,"textContent",dtScenario1_issueStatusResolved,false,"");
    	selectFrame("frames.frm_Parent");
    	if(dtScenario1_appCreationMethod.equals("Create")){
    	//Accessing Lifecycle Management Page
    	clickAt("ApplicationHome.lnk_LifeCycleManagement","10,10");
    	//Promoting to testing stage
    	Common.biscomp_Promote(this, dtScenario1_appStageDev,dtScenario1_newVersionNumber);
    	//wait for page loading
    	pause("5000");
    	//Verifying the promoted version is listed under paticular stage
    	Common.biscomp_VerifyingVersionListedUnderStage(this, dtScenario1_appStageTest,dtScenario1_newVersionNumber,dtScenario1_versionVisibilityTrue);
    	//Wait for page loading
    	pause("10000");
    	//Launiching the promoted application
    	Common.biscomp_LaunchingApp(this, dtScenario1_appStageTest,dtScenario1_newVersionNumber,dtScenario1_sampleText,dtScenario1_appType);
    	//wait for page loading
    	pause("5000");
    	selectWindow("Window.win_NewWindow","param_NewWindowNumber_PARAM:0");
    	clickAt("ApplicationHome.lnk_LifeCycleManagement","10,10");
    	pause("4000");
    	//Promote to production
    	Common.biscomp_Promote(this, dtScenario1_appStageTest,dtScenario1_newVersionNumber);
    	//Verifying the promoted version is listed under paticular stage
    	Common.biscomp_VerifyingVersionListedUnderStage(this, dtScenario1_appStagePro,dtScenario1_newVersionNumber,dtScenario1_versionVisibilityTrue);
    	//Wait for page loading
    	pause("15000");
    	//Launching promoted application
    	Common.biscomp_LaunchingApp(this, dtScenario1_appStagePro,dtScenario1_newVersionNumber,dtScenario1_sampleText,dtScenario1_appType);
    	selectWindow("Window.win_NewWindow","param_NewWindowNumber_PARAM:0");
    	}
    	if(dtScenario1_appCreationMethod.equals("Upload")){
    	Common.biscomp_VerifyingLifecycleHistoryUploadApp(this, dtScenario1_newVersionNumber,dtScenario1_appStagePro);
    	}
    	clickAt("ApplicationHome.lnk_LifeCycleManagement","10,10");
    	//wait for page loading
    	pause("5000");
    	Common.biscomp_Retire(this, dtScenario1_appStagePro,dtScenario1_newVersionNumber,dtScenario1_appCreationMethod,"null");
    	Common.biscomp_VerifyingRetiredVersion(this, dtScenario1_appStagePro,dtScenario1_newVersionNumber);
    	//verify the minimize appwall
    	Common.biscomp_MinimizeAppWall(this);
    	//log out
    	Common.biscomp_Logout(this);
    	} else if(!dtScenario1_IsExecuting){
    	String AppType = dtScenario1_appType;
    	writeToReport(AppType+" type is not tested in this iteration");
    	}
    } 
    	

    public final Object[][] getDataTable(final String... tableNames) {
        String[] tables = tableNames;
        return this.getTableArray(getVirtualDataTable(tables));
    }

}