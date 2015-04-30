package org.wso2.appfactory.tests.scenarios;/*
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.appfactory.integration.test.utils.AFConstants;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTest;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestUtils;
import org.wso2.appfactory.integration.test.utils.rest.GovernanceRestClient;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;

public class GovernanceTestCase extends AFIntegrationTest {

    private static GovernanceRestClient governanceRestClient = null;
    private static String applicationKey = null;
    private static final String INITIAL_STAGE = "Development";
    private static final String TAG_NAME = "";
    private static final String DEPLOY_ACTION = "deploy";
    private static final String CHECK_LIST_ITEM_NAME = "Design Review Done*";
    private static final String ITEM_CHECKED = "true";
    private static String initialVersion = null;
    private static String firstVersion = null;

    private static final Log log = LogFactory.getLog(GovernanceTestCase.class);

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        String tenantAdmin = AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_TENANT_ADMIIN);
        String tenantAdminPassword = AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_TENANT_ADMIN_PASSWORD);
        String afUrl = AFIntegrationTestUtils.getPropertyValue(AFConstants.URLS_APPFACTORY);
        governanceRestClient = new GovernanceRestClient(afUrl, tenantAdmin, tenantAdminPassword);
        applicationKey = AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_APP_KEY);
        initialVersion = AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_VERSION_ONE_SRC);
        firstVersion = AFIntegrationTestUtils.getPropertyValue(AFConstants.DEFAULT_APP_VERSION_ONE_TARGET);
    }

    //do version
    //itemcheck
    //promote
    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.PLATFORM})
    @Test(description = "Check list item selection")
    public void itemCheckedTestCase() throws Exception {
        governanceRestClient.itemChecked(applicationKey, INITIAL_STAGE, initialVersion, CHECK_LIST_ITEM_NAME, ITEM_CHECKED);
        log.info("Item checking successfully triggered");
    }
}

