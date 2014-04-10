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

import static org.testng.Assert.assertTrue;

public class OverviewTestCase extends AppFactoryIntegrationTestCase {

    private WebDriver driver;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        driver = BrowserManager.getWebDriver();
        driver.get(getLoginURL());
    }

    @Test(groups = "wso2.af", description = "adding a version from the trunk")
    public void testAddVersionToTrunk() throws Exception {
        AppLogin appLogin = new AppLogin(driver);
        AppHomePage appHomePage = appLogin.loginAs(userName(), password());
        String appName = AppCredentialsGenerator.getAppName();
        appHomePage.gotoApplicationManagementPage(appName);
        AppManagementPage appManagementPage = new AppManagementPage(driver);
        assertTrue(appManagementPage.isBuildDetailsAccurate("1.0.3")
                , "Application Build Successfully");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }
}
