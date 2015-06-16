/*
 *
 *  Copyright 2014 WSO2, Inc. (http://wso2.com)
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
 * 
 */

package org.wso2.carbon.appfactory.deployers;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.beans.RuntimeBean;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.Undeployer;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeBean;

import java.io.File;

/**
 * This class is used to undeploy artifacts in Git repository.
 */
public abstract class AbstractStratosUndeployer implements Undeployer {
    private int tenantId;
    private String tenantDomain;

    /**
     * Undeploy the artifacts from stratos storage repository provider
     *
     * @param deployerType
     * @param applicationId
     * @param applicationType
     * @param version
     * @param lifecycleStage
     * @param applicationTypeBean
     * @param runtimeBean
     * @throws AppFactoryException
     */
    public abstract void undeployArtifact(String deployerType, String applicationId,
                                          String applicationType, String version, String lifecycleStage,
                                          ApplicationTypeBean applicationTypeBean, RuntimeBean runtimeBean)
                                          throws AppFactoryException;

    /**
     * Returns file/folder to delete from depsync repository based on the passed parameters
     *
     * @param applicationId           application Id
     * @param version                 version
     * @param fileExtension           file extension of the application type
     * @param applicationRootLocation checked out directory
     * @return
     */
    public File getFileToDelete(String applicationId, String version, String fileExtension,
                                File applicationRootLocation) {
        String fileName = applicationId + AppFactoryConstants.MINUS + version;
        if (AppFactoryConstants.TRUNK.equals(version)) {
            fileName = applicationId + AppFactoryConstants.SNAPSHOT;
        }
        if (StringUtils.isNotBlank(fileExtension)) {
            fileName = fileName + AppFactoryConstants.DOT_SEPERATOR + fileExtension;
        }
        return new File(applicationRootLocation, fileName);
    }

    /**
     * Get base url of the stratos storage repository provider
     *
     * @return
     * @throws AppFactoryException
     */
    public String getBaseRepoUrl() throws AppFactoryException {
        return AppFactoryUtil.getAppfactoryConfiguration().
                getFirstProperty(AppFactoryConstants.PAAS_ARTIFACT_REPO_PROVIDER_BASE_URL);
    }

     /**
     *Generates the repository url of the stratos storage repository provider based on the passed parameters.
     *
     * @param runtimeBean
     * @param applicationId
     * @param stage
     * @return
     * @throws AppFactoryException
     */
    public abstract String generateRepoUrl(RuntimeBean runtimeBean, String applicationId, String stage)
            throws AppFactoryException;

    /**
     * Get s2git admin password
     *
     * @return admin password
     * @throws AppFactoryException
     */
    public String getAdminPassword() throws AppFactoryException {
        return AppFactoryUtil.getAppfactoryConfiguration().
                getFirstProperty(AppFactoryConstants.PAAS_ARTIFACT_REPO_PROVIDER_ADMIN_PASSWORD);
    }

    /**
     * Get s2git admin username
     *
     * @return admin user name
     * @throws AppFactoryException
     */
    public String getAdminUserName() throws AppFactoryException {
        return AppFactoryUtil.getAppfactoryConfiguration().
                getFirstProperty(AppFactoryConstants.PAAS_ARTIFACT_REPO_PROVIDER_ADMIN_USER_NAME);
    }
}
