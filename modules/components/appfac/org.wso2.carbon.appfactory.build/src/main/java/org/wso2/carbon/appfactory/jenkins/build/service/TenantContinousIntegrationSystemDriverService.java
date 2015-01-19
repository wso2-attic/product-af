/*
 * Copyright 2005-2013 WSO2, Inc. (http://wso2.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.appfactory.jenkins.build.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.util.AppFactoryCoreUtil;
import org.wso2.carbon.appfactory.jenkins.build.internal.ServiceContainer;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

/**
 * This service class is used for tenant based job integrations
 */
public class TenantContinousIntegrationSystemDriverService extends AbstractAdmin {

	private static Log log = LogFactory.getLog(TenantContinousIntegrationSystemDriverService.class);

	/**
	 * When create a application, this operation is triggered to create a job in
	 * continuous intergration system if it is buildable artifact only.
	 * 
	 * @param applicationId
	 * @param version
	 * @param revision
	 * @throws AppFactoryException
	 */
	public void createJob(String applicationId, String version, String revision)
			throws AppFactoryException {

		try {
			String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
			String applicationType =
					AppFactoryCoreUtil.getApplicationType(applicationId,
					                                      tenantDomain);

			//Check the archetype is a buildable or non-buildable from appfactory configuration.
			boolean appIsBuildServerRequired = AppFactoryCoreUtil.isBuildServerRequiredProject(applicationType);
			if (appIsBuildServerRequired) {
				ServiceContainer.getJenkinsCISystemDriver().createJob(applicationId, version,
				                                                      revision, tenantDomain, "", "", "");
			}
		} catch (RegistryException e) {
			String errMsg =
					"Error when try to get the application type from regsistry that given application id and tenant domain : " +
							e.getMessage();
			log.error(errMsg, e);
			throw new AppFactoryException(errMsg, e);
		}

	}

    /**
     * This method can be used to delete a job related to an application version
     *
     * @param applicationId
     * @param version
     * @throws AppFactoryException
     */
    public void DeleteJob(String applicationId, String version) throws AppFactoryException {

        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        // Job name will be '<ApplicationId>-<version>-default'
        String jobName = ServiceContainer.getJenkinsCISystemDriver().getJobName(applicationId, version, null);
        ServiceContainer.getJenkinsCISystemDriver().deleteJob(jobName, version, tenantDomain);
    }

    /**
     *
     * This method can be used to check whether a job exists for the given application version
     * @param applicationId
     * @param version
     * @return
     * @throws AppFactoryException
     */
    ///// TODO register service as osgi
    public boolean isJobExist(String applicationId, String version) throws AppFactoryException{
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String jobName = ServiceContainer.getJenkinsCISystemDriver().getJobName(applicationId, version, null);
        return ServiceContainer.getJenkinsCISystemDriver().isJobExists(applicationId, version, CarbonContext.
                getThreadLocalCarbonContext().getTenantDomain());
    }
}
