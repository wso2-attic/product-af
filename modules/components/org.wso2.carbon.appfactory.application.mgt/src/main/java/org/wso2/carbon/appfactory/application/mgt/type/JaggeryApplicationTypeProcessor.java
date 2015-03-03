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

package org.wso2.carbon.appfactory.application.mgt.type;

import org.apache.axiom.om.OMElement;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeManager;
import org.wso2.carbon.appfactory.core.dao.JDBCApplicationDAO;
import org.wso2.carbon.appfactory.core.dto.CartridgeCluster;
import org.wso2.carbon.appfactory.core.util.AppFactoryCoreUtil;
import org.wso2.carbon.appfactory.s4.integration.StratosRestService;
import org.wso2.carbon.appfactory.utilities.project.ProjectUtils;
import org.wso2.carbon.appfactory.utilities.version.AppVersionStrategyExecutor;
import org.wso2.carbon.context.CarbonContext;

import java.io.File;
import java.util.Map;

/**
 * PHP application type processor
 */
public class JaggeryApplicationTypeProcessor extends AbstractFreeStyleApplicationTypeProcessor {
    private static final Log log = LogFactory.getLog(JaggeryApplicationTypeProcessor.class);
 @Override
    public void generateApplicationSkeleton(String applicationId, String workingDirectory) throws AppFactoryException {
        ProjectUtils.generateProjectArchetype(applicationId, workingDirectory,
                ProjectUtils.getArchetypeRequest(applicationId,
                        getProperty(MAVEN_ARCHETYPE_REQUEST)));
        File pomFile = new File(workingDirectory + File.separator + AppFactoryConstants.DEFAULT_POM_FILE);
        boolean result = FileUtils.deleteQuietly(pomFile);
        if (!result){
            log.warn("Error while deleting pom.xml for application id : " + applicationId);
        }
    }

    @Override
    public void doVersion(String applicationId, String targetVersion, String currentVersion,
                                              String workingDirectory) throws AppFactoryException {
        File workDir = new File(workingDirectory);
        for (File file : workDir.listFiles()) {
            if (file.isDirectory() && file.getName().contains("-")) {
                String newName = changeFileName(file.getName(), targetVersion);
                File newFile =
                    new File(file.getAbsolutePath().replace(file.getName(),
                                                            newName));
                file.renameTo(newFile);
            }
        }
    }

    private static String changeFileName(String name, String changedVersion) throws AppFactoryException {

        String applicationName = name;
        String artifactVersionXPath = "-" + AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(ARTIFACT_VERSION_XPATH);
        if(name.lastIndexOf(artifactVersionXPath) != -1) {
            applicationName = name.substring(0, name.lastIndexOf(artifactVersionXPath));
        } else if (name.lastIndexOf("-") != -1) {
            applicationName = name.substring(0, name.lastIndexOf("-"));
        }
        return applicationName + "-" + changedVersion;
    }

}
