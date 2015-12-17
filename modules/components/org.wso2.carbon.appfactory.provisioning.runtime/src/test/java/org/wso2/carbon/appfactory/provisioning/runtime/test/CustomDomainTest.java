/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.appfactory.provisioning.runtime.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.appfactory.provisioning.runtime.KubernetesRuntimeProvisioningService;
import org.wso2.carbon.appfactory.provisioning.runtime.RuntimeProvisioningException;

import java.util.HashSet;
import java.util.Set;

public class CustomDomainTest {

    TestUtils testUtils;
    KubernetesRuntimeProvisioningService afKubClient;
    Set<String> domains;
    private static final Log log = LogFactory.getLog(CustomDomainTest.class);

    @BeforeClass
    public void initialize() throws RuntimeProvisioningException {
        testUtils = new TestUtils();
        afKubClient = new KubernetesRuntimeProvisioningService(testUtils.getAppCtx());
        testUtils.createNamespace();
        testUtils.createDeployment();
        testUtils.createService();
        domains = new HashSet<String>();
        domains.add("app1.wso2.com");
        domains.add("app2.wso2.com");
    }

    /**
     * Test add CustomDomain operation
     */
    @Test(groups = {"org.wso2.carbon.appfactory.provisioning.runtime"},
            description = "Adding custom Domain")
    public void testAddCustomDomain() throws RuntimeProvisioningException {
        afKubClient.addCustomDomain(domains);
        Set<String> domains =  afKubClient.getCustomDomains();
        Assert.assertNotNull(domains);
        Assert.assertTrue(domains.contains("app1.wso2.com"));
        Assert.assertTrue(domains.contains("app2.wso2.com"));
    }

    /**
     * Test get CustomDomain operation
     */
    @Test(dependsOnMethods = "testAddCustomDomain", groups = {"org.wso2.carbon.appfactory.provisioning.runtime"},
            description = "retrieving custom Domain")
    public void testGetCustomDomain() throws RuntimeProvisioningException {
        Set<String> domains =  afKubClient.getCustomDomains();
        Assert.assertNotNull(domains);
        Assert.assertTrue(domains.contains("app1.wso2.com"));
        Assert.assertTrue(domains.contains("app2.wso2.com"));
    }

    /**
     * Test update CustomDomain operation
     */
    @Test(dependsOnMethods = { "testAddCustomDomain", "testGetCustomDomain" },
            groups = {"org.wso2.carbon.appfactory.provisioning.runtime"},
            description = "updating custom Domain")
    public void testUpdateCustomDomain() throws RuntimeProvisioningException {
        afKubClient.updateCustomDomain("app1.wso2.com", "updatedapp1.wso2.com");
        Set<String> domains = afKubClient.getCustomDomains();
        Assert.assertNotNull(domains);
        Assert.assertTrue(domains.contains("updatedapp1.wso2.com"));
    }

    /**
     * Test delete CustomDomain operation
     */
    @Test(dependsOnMethods = { "testGetCustomDomain", "testUpdateCustomDomain"},
            groups = {"org.wso2.carbon.appfactory.provisioning.runtime"},
            description = "deleting custom Domain")
    public void testDeleteCustomDomain() throws RuntimeProvisioningException {
        afKubClient.deleteCustomDomain("updatedapp1.wso2.com");
        Set<String> domains = afKubClient.getCustomDomains();
        Assert.assertFalse(domains.contains("updatedapp1.wso2.com"));
    }

    @AfterClass
    public void cleanup() throws InterruptedException {
        testUtils.deleteNamespace();
        Thread.sleep(30000);
    }
}
