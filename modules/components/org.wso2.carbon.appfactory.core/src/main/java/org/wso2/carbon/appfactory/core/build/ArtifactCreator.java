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

package org.wso2.carbon.appfactory.core.build;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.dao.JDBCAppVersionDAO;
import org.wso2.carbon.appfactory.core.deploy.ApplicationDeployer;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.appfactory.core.util.AppFactoryCoreUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * This service is used to do operation against artifacts.
 */
public class ArtifactCreator extends AbstractAdmin {

    private static Log log = LogFactory.getLog(ArtifactCreator.class);

    /**
     * If the artifact is buildable , then this operation trigger the build that given Continuous Intergration System
     * and if the artifact auto deploy states is true then from the intergration system should do the deployment
     * <p/>
     * If it is non-buildable then it create artifact and check the auto deployment property before deployment.
     *
     * @param applicationId
     * @param version
     * @param revision
     * @param doDeploy      - indicates whether this is triggered manually or by build
     * @param deployStage
     * @param tagName
     * @throws AppFactoryException
     */
    public void createArtifact(String applicationId, String version, String revision, boolean doDeploy,
                               String deployStage, String tagName, String repoFrom) throws AppFactoryException {

        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String tenantUserName =
                MultitenantUtils.getTenantAwareUsername(CarbonContext.getThreadLocalCarbonContext().getUsername())
                + UserCoreConstants.TENANT_DOMAIN_COMBINER + tenantDomain;
        try {
            //doDeploy - indicates that it is an commit or not
            log.info("Artifact Create Service triggered....");


            String applicationType = AppFactoryCoreUtil.getApplicationType(applicationId, tenantDomain);
            boolean appIsBuilServerRequired = AppFactoryCoreUtil.isBuildServerRequiredProject(applicationType);

            boolean performBuild = true;
            boolean performDeploy = JDBCAppVersionDAO.getInstance().getAutoDeployStatusOfVersion(applicationId, version);
            if (doDeploy) {
                //triggered by auto commit
                performBuild = JDBCAppVersionDAO.getInstance().getAutoBuildStatusOfVersion(applicationId, version);
                if (log.isDebugEnabled()) {
                    log.debug("Triggered by auto commit performBuild: " + performBuild
                              + ", performDeploy : " + performDeploy
                              + ", application id : " + applicationId +", version : " + version
                              + ", tenant domain : " +tenantDomain+ ", repoFrom " + repoFrom
                              +" triggered by user: "+tenantUserName);
                }
            } else {
                //triggered by manual build
                if (log.isDebugEnabled()) {
                    log.debug("Triggered by manual build " + performBuild
                              + ", performDeploy : " + performDeploy
                              + ", application id : " + applicationId +", version : " + version
                              + ", tenant domain : " +tenantDomain+ ", repoFrom " + repoFrom
                              + " triggered by user: "+tenantUserName);
                }
            }

            if (appIsBuilServerRequired && performBuild) {
                if (ServiceHolder.getContinuousIntegrationSystemDriver() == null) {
                    throw new AppFactoryException("There is no any ContinuousIntegrationSystem register to build" +
                                                  " artifacts");
                }
                ServiceHolder.getContinuousIntegrationSystemDriver().startBuild(applicationId, version, performDeploy,
                              deployStage, tagName, tenantDomain,tenantUserName, repoFrom);
                log.info("Start a build job [Buildable Artifact] in CI to application id : " + applicationId +
                         ", version : " + version + ", tenant domain : " + tenantDomain +", repoFrom : " + repoFrom
                         +" triggered by user: "+tenantUserName);

            }
            if (!appIsBuilServerRequired && performDeploy) {
                ApplicationDeployer applicationDeployer = new ApplicationDeployer();
                applicationDeployer.deployArtifact(applicationId, deployStage, version, tagName, "deploy", repoFrom);
                log.info("Start artifact deploying  job for Application ID : " + applicationId + " , version " + version
                         + ", tenantDomain : " + tenantDomain + ", repoFrom " + repoFrom
                         +" triggered by user: "+tenantUserName);
            }
        } catch (RegistryException e) {
            String errMsg =
                    "Error when try to get the application type from registry that given " +
                    "application id : " + applicationId +", version : " + version + ", tenant domain : " + tenantDomain
                    +" triggered by user: "+tenantUserName ;
            log.error(errMsg, e);
            throw new AppFactoryException(errMsg, e);
        }
    }
}
