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

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.wso2.carbon.appfactory.provisioning.runtime.KubernetesRuntimeProvisioningService;
import org.wso2.carbon.appfactory.provisioning.runtime.RuntimeProvisioningException;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.ApplicationContext;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.TenantInfo;

public class LoggingTest {

    ApplicationContext appCtx;
    KubernetesRuntimeProvisioningService afKubClient;

    @BeforeSuite
    public void createNameSpace() throws RuntimeProvisioningException {
        appCtx = this.getApplicationContext();
        afKubClient = new KubernetesRuntimeProvisioningService(appCtx);
        afKubClient.createOrganization(appCtx.getTenantInfo());
    }


    /**
     * Test Logging operation
     */

    @Test(groups = {"org.wso2.carbon.appfactory.provisioning.runtime"}, description = "Kub Logging")
    public void testDeployment() throws RuntimeProvisioningException {

        System.out.println("################## Starting logging test case ################");
        ApplicationContext appCtx = this.getApplicationContext();
        KubernetesRuntimeProvisioningService afKubClient = new KubernetesRuntimeProvisioningService(appCtx);
        afKubClient.getRuntimeLogs(null, null);
    }


    private ApplicationContext getApplicationContext(){

        TenantInfo tenant = new TenantInfo();
        tenant.setTenantId(1);
        tenant.setTenantDomain("wso2.org");

        ApplicationContext applicationCtx = new ApplicationContext();
        applicationCtx.setCurrentStage("dev");
        applicationCtx.setName("kasuns-java-webapp");
        applicationCtx.setVersion("1.0.0");
        applicationCtx.setTenantInfo(tenant);
        applicationCtx.setId("kasuns-java-webapp");

        return applicationCtx;
    }
}
