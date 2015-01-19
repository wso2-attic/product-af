/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package org.wso2.carbon.appfactory.jenkins.build.service;

import java.util.List;

import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.jenkins.build.internal.ServiceContainer;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;

public class ContinousIntegrationSystemDriverService extends AbstractAdmin {

    public void deleteJob(String jobName, String version) throws AppFactoryException {
//        Getting the tenant domain from CarbonContext
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        ServiceContainer.getJenkinsCISystemDriver().deleteJob(jobName, version, tenantDomain);
    }

    public List<String> getAllJobNames() throws AppFactoryException {

//        Getting the tenant domain from CarbonContext
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        return ServiceContainer.getJenkinsCISystemDriver().getAllJobNames(tenantDomain);
    }

    public void startBuild(String applicationId, String version, boolean doDeploy, String deployStage,String userName)
            throws AppFactoryException {
        ///////// TODO get the tagname as parameter, temp passing a random value as tagName
        String tagName = "tag"+(Math.random()*100);

//        Getting the tenant domain from CarbonContext
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        ServiceContainer.getJenkinsCISystemDriver().startBuild(applicationId, version, doDeploy, deployStage,
                                                               tagName, tenantDomain, userName, AppFactoryConstants.ORIGINAL_REPOSITORY);
    }

    public boolean isJobExists(String applicationId, String version) throws AppFactoryException {

//        Getting the tenant domain from CarbonContext
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        return ServiceContainer.getJenkinsCISystemDriver().isJobExists(applicationId, version, tenantDomain);
    }

    public String getJobName(String applicationId, String version, String revision) {
        return ServiceContainer.getJenkinsCISystemDriver().getJobName(applicationId, version,
                                                                      revision);
    }
}
