/*
 *
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.carbon.appfactory.jenkins.extentions;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.maven.AbstractMavenBuild;
import hudson.maven.local_repo.LocalRepositoryLocator;
import hudson.maven.local_repo.LocalRepositoryLocatorDescriptor;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.wso2.carbon.appfactory.jenkins.Constants;

import java.io.File;

/**
 * Maven repository locator
 */
public class AFLocalRepositoryLocator extends LocalRepositoryLocator {

    public static final String DEFAULT_REPO_PATH_SUFFIX = ".repository";

    @DataBoundConstructor
    public AFLocalRepositoryLocator() {
    }

    @Override
    public FilePath locate(AbstractMavenBuild build) {
        String jenkinsHome = EnvVars.masterEnvVars.get(Constants.JENKINS_HOME);
        // First we are setting repo path a default repo path
        String repoPath = jenkinsHome + File.separator + DEFAULT_REPO_PATH_SUFFIX;
        String tenantGroup = build.getParent().getParent().getFullName();
        String tenantRepositoryDirPattern = getDescriptor().getTenantRepositoryDirPattern(tenantGroup);
        if (StringUtils.isNotEmpty(tenantRepositoryDirPattern)) {
            repoPath = tenantRepositoryDirPattern;
        }
        // No need to create or check the existence of the directory. Jenkins will create the directory
        return new FilePath(new File(repoPath));
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * Descriptor implementation related to Repository locator. The descriptor's configuration file can be found at
     * $JENKINS_HOME/org.wso2.carbon.appfactory.jenkins.extentions.AFLocalRepositoryLocator.xml
     */
    @Extension
    public static class DescriptorImpl extends LocalRepositoryLocatorDescriptor {

        public static final String FIELD_TENANT_REPOSITORY_DIR_PATTERN = "tenantRepositoryDirPattern";
        public static final String FIELD_PRE_CONFIGURED_MVN_REPO_ARCHIVE = "preConfiguredMvnRepoArchive";

        public DescriptorImpl() {
            super();
            load();
        }

        private String tenantRepositoryDirPattern;
        private String preConfiguredMvnRepoArchive;

        public String getPreConfiguredMvnRepoArchive() {
            return preConfiguredMvnRepoArchive;
        }

        public void setPreConfiguredMvnRepoArchive(String preConfiguredMvnRepoArchive) {
            this.preConfiguredMvnRepoArchive = preConfiguredMvnRepoArchive;
        }

        public void setTenantRepositoryDirPattern(String tenantRepositoryDirPattern) {
            this.tenantRepositoryDirPattern = tenantRepositoryDirPattern;
        }

        public String getTenantRepositoryDirPattern() {
            return tenantRepositoryDirPattern;
        }

        /**
         * Returns the directory to use as the maven repository for particular {@code tenantDomain}.
         * If directory pattern contains {@value org.wso2.carbon.appfactory.jenkins.Constants#PLACEHOLDER_JEN_HOME}
         * or {@value org.wso2.carbon.appfactory.jenkins.Constants#PLACEHOLDER_TENANT_IDENTIFIER} it will replace
         * them with JENKINS_HOME and tenant domain respectively.
         *
         * ex: if $JENKINS_HOME is /mnt/Jenkins_Home and "tenantRepositoryDirPattern" is set as below in the descriptor
         * (this descriptor file can found at
         * $JENKINS_HOME/org.wso2.carbon.appfactory.jenkins.extentions.AFLocalRepositoryLocator.xml).
         *
         * <tenantRepositoryDirPattern>$JENKINS_HOME/jobs/$TENANT_IDENTIFIER/repository</tenantRepositoryDirPattern>
         * Maven repository for tenant domain foo.com will be configured at /mnt/Jenkins_Home/foo.com/repository
         *
         * @param tenantDomain tenant Domain
         * @return directory to use as the maven repository for particular {@code tenantDomain}
         */
        public String getTenantRepositoryDirPattern(String tenantDomain) {
            String jenkinsHome = EnvVars.masterEnvVars.get(Constants.JENKINS_HOME);
            return this.tenantRepositoryDirPattern.replace(Constants.PLACEHOLDER_JEN_HOME, jenkinsHome).replace
                    (Constants.PLACEHOLDER_TENANT_IDENTIFIER, tenantDomain);
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            setTenantRepositoryDirPattern(formData.getString(FIELD_TENANT_REPOSITORY_DIR_PATTERN));
            setPreConfiguredMvnRepoArchive(formData.getString(FIELD_PRE_CONFIGURED_MVN_REPO_ARCHIVE));
            save();
            return super.configure(req, formData);
        }

        @Override
        public String getDisplayName() {
            return "Local to the tenant workspace";
        }
    }
}