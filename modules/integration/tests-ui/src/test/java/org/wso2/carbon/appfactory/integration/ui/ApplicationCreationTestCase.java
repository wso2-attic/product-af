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
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.selenium.appfactory.appmanagement.AddNewAppPage;
import org.wso2.carbon.automation.api.selenium.appfactory.home.AppHomePage;
import org.wso2.carbon.automation.api.selenium.appfactory.home.AppLogin;
import org.wso2.carbon.automation.core.BrowserManager;
import org.wso2.carbon.automation.core.ProductConstant;

import java.io.File;

public class ApplicationCreationTestCase extends AppFactoryIntegrationTestCase {

    public String appName = super.applicationName();
    public String appKey = super.applicationKey();

    @BeforeTest(alwaysRun = true, groups = "wso2.af",
                description = "Create a new Application according to the passing values")
    public void createApplication() throws Exception {
        WebDriver driver = BrowserManager.getWebDriver();
        driver.get(getLoginURL());
        try {
            AppLogin appLogin = new AppLogin(driver);
            AppHomePage appHomePage = appLogin.loginAs(userName(), password());
            appHomePage.gotoAddNewAppPage();
            AddNewAppPage addNewAppPage = new AddNewAppPage(driver);
            AppCredentialsGenerator.setAppKey(appKey);
            AppCredentialsGenerator.setAppName(appName);
            String iconPath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                              File.separator + "AF" + File.separator + "images" + File.separator + "build.png";
            addNewAppPage.createAnApplication(appName, appKey, iconPath, "this is a test app",
                                              "JAX-RS Application", "Git");
        } finally {
            driver.quit();
        }
    }
}
