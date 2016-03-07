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

package org.wso2.carbon.appfactory.application.mgt.type;

import org.apache.axiom.om.OMElement;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.UnzipUtility;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeManager;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeValidationStatus;
import org.wso2.carbon.appfactory.utilities.project.ProjectUtils;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Uploaded Jaggery ApplicationType Processor
 */
public class UploadedJaggeryApplicationTypeProcessor extends UploadedApplicationTypeProcessor {
    private static Log log = LogFactory.getLog(UploadedJaggeryApplicationTypeProcessor.class);

    public UploadedJaggeryApplicationTypeProcessor(String type) {
        super(type);
    }

    public ApplicationTypeValidationStatus validate(String uploadedFileName) {
        File zipFile = new File(getUploadedApplicationTmpPath() + File.separator +
                                uploadedFileName);
        if(!zipFile.exists()) {
            return new ApplicationTypeValidationStatus(false ,"File does not exist");
        } else if(!zipFile.canRead()) {
            return new ApplicationTypeValidationStatus(false ,"File can\'t be read");
        }
        return new ApplicationTypeValidationStatus(true ,"Successfully Validated!");
    }

    @Override
    public OMElement configureBuildJob(OMElement jobConfigTemplate, Map<String, String> parameters, String projectType)
            throws AppFactoryException {
        jobConfigTemplate = super.configureBuildJob(jobConfigTemplate, parameters, projectType);
        setValueUsingXpath(jobConfigTemplate, AppFactoryConstants.ARTIFACT_ARCHIVER_CONFIG_NAME_XAPTH_SELECTOR,
                           "**/*.*");
        setValueUsingXpath(jobConfigTemplate,
                           AppFactoryConstants.PUBLISHERS_APPFACTORY_POST_BUILD_APP_EXTENSION_XPATH_SELECTOR,
                           "jaggery");
        return jobConfigTemplate;


    }

    @Override
    public List<File> getPreVersionDeleteableFiles(String applicationID, String targetVersion,
                                                   String currentVersion, String workingDir) throws AppFactoryException {
        File currentCheckout = new File(CarbonUtils.getTmpDir() + File.separator +
                                        AppFactoryConstants.REPOSITORY_BRANCH + File.separator +
                                        applicationID + File.separator + targetVersion);
        File[] files = currentCheckout.listFiles();
        return Arrays.asList(files);
    }

    /**
     * Here it will copy extracted {@code uploadedFileName} (.zip) file to the {@code workingDirectory} and will
     * commit that extracted file into the s2git repo.
     * Otherwise cartridge agent will fail to pull the changes from s2 git, since when we drop a .zip file into the
     * jaggery apps directory of the AS server, it will unzip that zip file. Then cartridge agent will detect that as
     * a local change(in git status command) in the cloned directory and will fail to pull latest changes.
     * @param uploadedFileName
     * @param workingDirectory
     * @throws IOException
     */
    @Override
    public void copyUploadedAppToLocation(String uploadedFileName, String workingDirectory) throws IOException {
        File sourceFile =
                new File(getUploadedApplicationTmpPath() + File.separator +
                         uploadedFileName);
        String desFileExtractName = uploadedFileName.replace(AppFactoryConstants.UPPLOADABLE_SUFFIX, "");
        desFileExtractName = desFileExtractName.substring(0, desFileExtractName.lastIndexOf("."));
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String tmpExtractLocation = CarbonUtils.getTmpDir() + File.separator +"tmpExtract" + File.separator +
                                    tenantDomain + File.separator + desFileExtractName;
        File tmpExtractedFile = new File(tmpExtractLocation);
        FileUtils.forceMkdir(tmpExtractedFile);
        if(log.isDebugEnabled()){
            log.debug("Starting to extract source file : "+sourceFile.getAbsolutePath() + ", to directory : "
                      + tmpExtractLocation);
        }
        UnzipUtility.unzip(sourceFile.getAbsolutePath(), tmpExtractLocation);
        copyUploadedAppToRepositoryLocation(tmpExtractedFile, desFileExtractName, workingDirectory);
        copyUplodedAppToDepolyArtifactLocation(tmpExtractedFile, desFileExtractName, workingDirectory);
        FileUtils.forceDelete(sourceFile);
        FileUtils.forceDelete(tmpExtractedFile);

    }

    private void copyUploadedAppToRepositoryLocation(File sourceFile, String uploadedFileName, String workingDirectory)
            throws IOException {
        File desFile = new File(workingDirectory + File.separator +
                                uploadedFileName.replace(AppFactoryConstants.UPPLOADABLE_SUFFIX, ""));
        FileUtils.copyDirectory(sourceFile, desFile);

        if (log.isDebugEnabled()) {
            log.debug("Uploaded application file " + sourceFile.getName() + " successfully copied to location - " +
                      desFile.getAbsolutePath());
        }
    }

    private void copyUplodedAppToDepolyArtifactLocation(File sourceFile, String uploadedFileName,
                                                        String deployArtifactLocation) throws IOException {
        File desFile = new File(deployArtifactLocation + "_deploy_artifact" + File.separator +
                                uploadedFileName.replace(AppFactoryConstants.UPPLOADABLE_SUFFIX, ""));
        FileUtils.copyDirectory(sourceFile, desFile);
        if (log.isDebugEnabled()) {
            log.debug("Copied uploaded application from " + sourceFile.getName() + " to deploy artifact location " +
                      desFile.getAbsolutePath());
        }
    }
}
