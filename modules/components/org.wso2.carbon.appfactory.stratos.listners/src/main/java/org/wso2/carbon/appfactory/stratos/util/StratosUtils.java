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

package org.wso2.carbon.appfactory.stratos.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.common.beans.application.signup.ApplicationSignUpBean;
import org.apache.stratos.common.beans.artifact.repository.ArtifactRepositoryBean;
import org.apache.stratos.common.util.CommonUtil;
import org.apache.stratos.manager.service.stub.domain.application.signup.ApplicationSignUp;
import org.apache.stratos.manager.service.stub.domain.application.signup.ArtifactRepository;
import org.apache.stratos.messaging.domain.application.Application;
import org.apache.stratos.messaging.domain.application.ClusterDataHolder;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility Methods extracted from Stratos
 */
public class StratosUtils {
    private static Log log = LogFactory.getLog(StratosUtils.class);
    /**
     * Encrypt artifact repository passwords.
     *
     * @param applicationSignUp Application Signup
     * @param applicationKey    Application Key
     */
    public static void encryptRepositoryPasswords(ApplicationSignUp applicationSignUp, String applicationKey) {
        if (applicationSignUp.getArtifactRepositories() != null) {
            for (ArtifactRepository artifactRepository : applicationSignUp.getArtifactRepositories()) {
                if (artifactRepository != null) {
                    String repoPassword = artifactRepository.getRepoPassword();
                    if ((StringUtils.isNotBlank(repoPassword))) {
                        String encryptedRepoPassword = CommonUtil.encryptPassword(repoPassword,
                                                                                  applicationKey);
                        artifactRepository.setRepoPassword(encryptedRepoPassword);

                        if (log.isDebugEnabled()) {
                            log.debug(String.format("Artifact repository password encrypted: [application-id] %s " +
                                                    "[tenant-id] %d [repo-url] %s",
                                                    applicationSignUp.getApplicationId(),
                                                    applicationSignUp.getTenantId(), artifactRepository.getRepoUrl()));
                        }
                    }
                }
            }
        }
    }


    /**
     * Find application cluster ids.
     *
     * @param application Application
     * @return list of cluster Ids
     */
    public static List<String> findApplicationClusterIds(Application application) {
        List<String> clusterIds = new ArrayList<String>();
        for (ClusterDataHolder clusterDataHolder : application.getClusterDataRecursively()) {
            clusterIds.add(clusterDataHolder.getClusterId());
        }
        return clusterIds;
    }

    public static ApplicationSignUp convertApplicationSignUpBeanToStubApplicationSignUp(
            ApplicationSignUpBean applicationSignUpBean) {

        if (applicationSignUpBean == null) {
            return null;
        }
        ApplicationSignUp applicationSignUp = new ApplicationSignUp();

        if (applicationSignUpBean.getArtifactRepositories() != null) {
            List<ArtifactRepository> artifactRepositoryList = new ArrayList<ArtifactRepository>();
            for (ArtifactRepositoryBean artifactRepositoryBean : applicationSignUpBean.getArtifactRepositories()) {
                ArtifactRepository artifactRepository = new ArtifactRepository();

                artifactRepository.setAlias(artifactRepositoryBean.getAlias());
                artifactRepository.setPrivateRepo(artifactRepositoryBean.isPrivateRepo());
                artifactRepository.setRepoUrl(artifactRepositoryBean.getRepoUrl());
                artifactRepository.setRepoUsername(artifactRepositoryBean.getRepoUsername());
                artifactRepository.setRepoPassword(artifactRepositoryBean.getRepoPassword());

                artifactRepositoryList.add(artifactRepository);
            }
            ArtifactRepository[] artifactRepositoryArray = artifactRepositoryList.toArray(new ArtifactRepository[
                                                                                                  artifactRepositoryList.size()]);
            applicationSignUp.setArtifactRepositories(artifactRepositoryArray);
        }
        return applicationSignUp;
    }

    public static org.apache.stratos.common.beans.TenantInfoBean convertCarbonTenantInfoBeanToTenantInfoBean(
            TenantInfoBean carbonTenantInfoBean) {

        if (carbonTenantInfoBean == null) {
            return null;
        }

        org.apache.stratos.common.beans.TenantInfoBean tenantInfoBean =
                new org.apache.stratos.common.beans.TenantInfoBean();
        tenantInfoBean.setTenantId(carbonTenantInfoBean.getTenantId());
        tenantInfoBean.setTenantDomain(carbonTenantInfoBean.getTenantDomain());
        tenantInfoBean.setActive(carbonTenantInfoBean.isActive());
        tenantInfoBean.setAdmin(carbonTenantInfoBean.getAdmin());
        tenantInfoBean.setEmail(carbonTenantInfoBean.getEmail());
        tenantInfoBean.setAdminPassword(carbonTenantInfoBean.getAdminPassword());
        tenantInfoBean.setFirstName(carbonTenantInfoBean.getFirstname());
        tenantInfoBean.setLastName(carbonTenantInfoBean.getLastname());
        tenantInfoBean.setCreatedDate(carbonTenantInfoBean.getCreatedDate().getTimeInMillis());
        return tenantInfoBean;
    }


}


