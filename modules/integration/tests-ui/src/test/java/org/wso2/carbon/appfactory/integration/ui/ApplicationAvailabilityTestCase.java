/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.appfactory.integration.ui;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.selenium.appfactory.appmanagement.AppManagementPage;
import org.wso2.carbon.automation.api.selenium.appfactory.home.AppHomePage;
import org.wso2.carbon.automation.api.selenium.appfactory.home.AppLogin;
import org.wso2.carbon.automation.core.BrowserManager;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;

import static org.testng.Assert.assertTrue;

public class ApplicationAvailabilityTestCase extends AppFactoryIntegrationTestCase {
    private WebDriver driver;
    private UserInfo userInfo;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        driver = BrowserManager.getWebDriver();
        userInfo = UserListCsvReader.getUserInfo(0);
        driver.get(getLoginURL());
    }

    @Test(groups = "wso2.af", description = "checking added application is available at the home page")
    public void testAppAvailableHomePage() throws Exception {
        AppLogin appLogin = new AppLogin(driver);
        AppHomePage appHomePage = appLogin.loginAs(userName(), password());
        String appName = AppCredentialsGenerator.getAppName();
        assertTrue(appHomePage.isApplicationAvailable(appName), "Application is not added Successfully");
    }

    @Test(groups = "wso2.af", description = "checking added application details are available at the overview Page")
    public void testAppDetailsOverviewPage() throws Exception {
        AppHomePage appHomePage = new AppHomePage(driver);
        String appName = AppCredentialsGenerator.getAppName();
        String appKey = AppCredentialsGenerator.getAppKey();
        appHomePage.gotoApplicationManagementPage(appName);
        AppManagementPage appManagementPage = new AppManagementPage(driver);
        int charAt = userInfo.getUserName().indexOf('@');
        String appOwnerName = userInfo.getUserName().substring(0, charAt).toUpperCase();
        assertTrue(appManagementPage.isAppDetailsAvailable("git", appOwnerName, "this is a test app",
                "JAX-RS Application", appKey), "Application Details are incorrect in App Management page");
    }

    @Test(groups = "wso2.af", description = "checking Description Edit")
    public void testEditDescription() throws Exception {
        AppManagementPage appManagementPage = new AppManagementPage(driver);
        appManagementPage.editApplicationDetails("notepad edit cycle");
        assertTrue(appManagementPage.isEdited("notepad edit cycle")
                , "Application description edit unsuccessful");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }
}
