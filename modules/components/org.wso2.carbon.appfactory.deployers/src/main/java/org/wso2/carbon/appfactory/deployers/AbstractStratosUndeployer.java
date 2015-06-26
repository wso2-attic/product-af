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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.Undeployer;

import java.io.File;
import java.util.Collection;
import java.util.Map;

/**
 * This class is used to undeploy artifacts in Git repository.
 */
public abstract class AbstractStratosUndeployer implements Undeployer {

    protected static final String DEFAULT_SNAPSHOT = "-default-SNAPSHOT";
    private int tenantId;
    private String tenantDomain;

    /**
     * Undeploy the artifacts from stratos storage repository provider
     *
     * @param parameters this map contains values related to artifact which is going to be undeployed. eg :
     *                   application type, deployer type, stage, version, application id etc.
     * @throws AppFactoryException
     */
    public abstract void undeployArtifact(Map<String, String[]> parameters) throws AppFactoryException;

    public Collection getFilesToDelete(String applicationId, String version, String fileExtension,
                                       File applicationRootLocation) {
        return FileUtils.listFiles(applicationRootLocation,
                                   new ArtifactFileFilter(applicationId, version, fileExtension),
                                   null);
    }

    /**
     * Get base url of the stratos storage repository provider
     *
     * @return
     * @throws AppFactoryException
     */
    public abstract  String getBaseRepoUrl() throws AppFactoryException;

    /**
     * Generates the repository url of the stratos storage repository provider based on the passed parameters.
     *
     * @param parameters this map contains values related to artifact which is going to be undeployed. eg :
     *                   application type, deployer type, stage, version, application id etc.
     * @return
     * @throws AppFactoryException
     */
    public abstract String generateRepoUrl(Map parameters)
            throws AppFactoryException;

    /**
     * Get s2git admin password
     *
     * @return admin password
     * @throws AppFactoryException
     */
    public abstract String getS2AdminPassword() throws AppFactoryException;

    /**
     * Get s2git admin username
     *
     * @return admin user name
     * @throws AppFactoryException
     */
    public abstract String gets2AdminUserName() throws AppFactoryException;

    /**
     * Used to filter artifact(s)/ corresponding to specified application id, version and file extension
     */
    private static class ArtifactFileFilter implements IOFileFilter {

        private String fileName;

        /**
         * Constructor of the class.
         *
         * @param applicationId application Id
         * @param version       version
         * @param extension     file extension
         */

        public ArtifactFileFilter(String applicationId, String version, String extension) {
            if (AppFactoryConstants.TRUNK.equals(version)) {
                fileName = applicationId + DEFAULT_SNAPSHOT;
            } else {
                fileName = applicationId + AppFactoryConstants.MINUS + version;
            }
            fileName = fileName + AppFactoryConstants.DOT_SEPERATOR + extension;
        }

        /**
         * Only files are accepted (not directories). they should match the expected file name.
         *
         * @param file file to be checked
         */
        @Override
        public boolean accept(File file) {
            return file.isFile() && file.getName().equals(fileName);
        }

        /**
         * No directories are accepted.
         *
         * @param dir  the directory File to check
         * @param name the filename within the directory to check
         */
        @Override
        public boolean accept(File dir, String name) {
            return false;
        }
    }
}
