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

package org.wso2.carbon.appfactory.utilities.project;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.shared.invoker.*;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeBean;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeManager;
import org.wso2.carbon.appfactory.core.dao.ApplicationDAO;
import org.wso2.carbon.appfactory.core.dao.JDBCApplicationDAO;
import org.wso2.carbon.appfactory.core.util.AppFactoryCoreUtil;
import org.wso2.carbon.appfactory.core.util.CommonUtil;
import org.wso2.carbon.appfactory.core.util.Constants;
import org.wso2.carbon.appfactory.utilities.internal.ServiceReferenceHolder;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifactImpl;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.*;
import java.util.*;

/**
 * ProjectUtil class holds the utility methods to generate projects
 */
public class ProjectUtils {
    private static final Log log = LogFactory.getLog(ProjectUtils.class);
    public static JDBCApplicationDAO applicationDAO = JDBCApplicationDAO.getInstance();

    /**
     * Run a maven command
     * @param goals goals of the maven command
     * @param invokerOutputHandler output handler for the maven command
     * @param baseDir base directory to run maven command
     * @param mavenOPTs maven opts for the command
     * @param properties properties for the command
     * @return result of the invocation
     * @throws AppFactoryException Either when maven home is not set or invocation of the command fails
     */
    public static InvocationResult runMavenCommand(List<String> goals, InvocationOutputHandler invokerOutputHandler, File baseDir,
                                                   String mavenOPTs, Properties properties) throws AppFactoryException {
        //Check whether the maven home is set. If not, can not proceed further.
        String mavenHome = System.getenv(AppFactoryConstants.SYSTEM_VARIABLE_M2_HOME);
        if (StringUtils.isBlank(mavenHome)) {
            mavenHome = System.getenv(AppFactoryConstants.SYSTEM_VARIABLE_M3_HOME);
            if (StringUtils.isBlank(mavenHome)) {
                String msg = "valid maven installation is not found with M2_HOME or M3_HOME environment variable";
                log.error(msg);
                throw new AppFactoryException(msg);
            }
        }
        InvocationRequest request = new DefaultInvocationRequest();
        request.setBaseDirectory(baseDir);
        request.setShowErrors(true);
        request.setGoals(goals);
        if(mavenOPTs != null) {
            request.setMavenOpts(mavenOPTs);
        }
        if(properties != null) {
            request.setProperties(properties);
        }
        try{
            Invoker invoker = new DefaultInvoker();
            InvocationOutputHandler outputHandler = new SystemOutHandler();
            invoker.setErrorHandler(outputHandler);
            invoker.setMavenHome(new File(mavenHome));
            invoker.setOutputHandler(invokerOutputHandler);
            return invoker.execute(request);
        } catch (MavenInvocationException e) {
            String msg = "Failed to invoke maven command inside " + baseDir.getAbsolutePath();
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
    }

    /**
     * This method generates the archetype for the projects
     *
     * @param appId            Id of the application
     * @param filePath         Root path of the application
     * @param archetypeRequest Maven archetype request
     * @throws AppFactoryException
     */
    public static boolean generateProjectArchetype(final String appId, String filePath, String archetypeRequest)
            throws AppFactoryException {


        File workDir = new File(filePath);

        //Checking whether the app directory exists. If not, the previous process has failed. Hence returning
        if (!workDir.exists()) {
            log.warn(String.format("Work directory for application key : %s does not exist", appId));
            return false;
        }
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        File archetypeDir = new File(
                CarbonUtils.getTmpDir() + File.separator + tenantDomain + File.separator + appId + File.separator +
                AppFactoryConstants.MAVEN_ARCHETYPE_DIR);
        archetypeDir.mkdirs();
        List<String> goals = new ArrayList<String>();
        goals.add(AppFactoryConstants.GOAL_MAVEN_ARCHETYPE_GENERATE);

        InvocationResult result = null;
        try {

            InvocationOutputHandler invokerOutputHandler = new InvocationOutputHandler() {
                @Override
                public void consumeLine(String s) {
                    log.info(appId + ":" + s);
                }
            };
            result = runMavenCommand(goals, invokerOutputHandler, archetypeDir, archetypeRequest, null);

        } finally {
            if (result != null && result.getExitCode() == 0) {
                log.info("Maven archetype generation completed successfully");
                return true;
            }
        }
        return false;
    }

    public static boolean initialDeployArtifactGeneration(String appId, File projectDir, File initialArtifact,
                                                          File workDir, List<String> goals)
            throws AppFactoryException {
        boolean isSuccess = false;
        String applicationType = ApplicationDAO.getInstance().getApplicationType(appId);
        if (AppFactoryCoreUtil.isBuildServerRequiredProject(applicationType)) {
            isSuccess = generateDeployArtifact(appId, projectDir, initialArtifact, goals);
            if(isSuccess) {
                moveDepolyArtifact(initialArtifact, workDir.getParentFile(), appId);
            }
        }
        return isSuccess;
    }

    /**
     * Move deploy artifact to a new path
     *
     * @param deployAtrifact
     * @param parentFile
     * @param appId id of the application
     * @throws AppFactoryException
     */
    private static void moveDepolyArtifact(File deployAtrifact, File parentFile, String appId) throws
                                                                                               AppFactoryException {
        try {
            String deployArtifactPath = parentFile.getAbsolutePath() + File.separator + appId
                                        + AppFactoryConstants.AF_ARCHETYPE_INITIAL_ARTIFACT_LOCATION;
            File deployArtifactFile = new File(deployArtifactPath);
            if (deployArtifactFile.exists()) {
                FileUtils.forceDelete(deployArtifactFile);
            }
            FileUtils.moveDirectoryToDirectory(deployAtrifact, parentFile, false);
        } catch (IOException e) {
            String msg = "Error while moving deploy artifact from "+ deployAtrifact.getAbsolutePath()
                         + " to " + parentFile.getAbsolutePath();
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
    }

    /**
     * Generate the initial artifact
     * @param appId application key
     * @param projectDir base directory to run assembly plugin
     * @param initialArtifact Folder contains initial artifact
     * @param goals Goals to execute to generate initial artifact
     * @return Root directory in which deploy artifact is available
     * @throws AppFactoryException
     */
    private static boolean generateDeployArtifact(final String appId, final File projectDir, final File initialArtifact, List<String> goals)
            throws AppFactoryException {

        boolean isSuccess = false;
        InvocationResult result = null;
        InvocationOutputHandler invocationOutputHandler = new InvocationOutputHandler() {
            @Override
            public void consumeLine(String s) {
                log.info(appId + ":" + s);
            }
        };
        try {
            result = runMavenCommand(goals, invocationOutputHandler, projectDir, null, null);
            if(initialArtifact.exists()){
                isSuccess = true;
            }else{
                throw new AppFactoryException("Deployable artifact has not generated in path "+ initialArtifact.getAbsolutePath());
            }
        } finally {
            if (result == null || result.getExitCode() != 0) {
                isSuccess = false;
                log.info("Deployable artifact generation completed successfully");
            }
        }
        return isSuccess;
    }

    /**
     *
     * @param path
     * @throws AppFactoryException
     */
    public static void configureFinalName(String path) throws AppFactoryException {
        File artifactDir = new File(path);
        String[] fileExtension = {AppFactoryConstants.XML_EXTENSION};
        List<File> fileList = (List<File>) FileUtils.listFiles(artifactDir, fileExtension, true);
        for (File file : fileList) {
            setArtifactName(file);
        }
    }

    /**
     *
     * @param file
     * @throws AppFactoryException
     */
    private static void setArtifactName(File file) throws AppFactoryException {
        MavenXpp3Reader mavenXpp3Reader = new MavenXpp3Reader();
        Model model;
        try {
            if (file.getName().equals(AppFactoryConstants.DEFAULT_POM_FILE)) {
                FileInputStream stream = new FileInputStream(file);
                try {
                    model = mavenXpp3Reader.read(stream);
                    if (model.getBuild() != null && model.getBuild().getFinalName() != null) {
                        model.getBuild().setFinalName(AppFactoryConstants.ARTIFACT_NAME);
                    }
                    MavenXpp3Writer writer = new MavenXpp3Writer();
                    writer.write(new FileWriter(file), model);
                } catch (Exception e) {
                    String msg = "Error while configuring final name in Application";
                    log.error(msg, e);
                    throw new AppFactoryException(msg, e);
                } finally {
                    if (stream != null) {
                        stream.close();
                    }
                }
            }
        } catch (IOException e) {
            String msg = "Error while getting pom.xml";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
    }

    /**
     *
     * @param srcPath
     * @param workPath
     * @throws AppFactoryException
     */
    public static void copyArchetypeToTrunk(String srcPath, String workPath) throws AppFactoryException {
        File srcDir = new File(srcPath);
        File destDir = new File(workPath);
        File[] files = srcDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.exists()) {
                    String msg = "Source directory does not exist";
                    log.error(msg);
                    throw new AppFactoryException(msg);
                }
                try {
                    FileUtils.copyDirectory(file, destDir, false);
                } catch (IOException e) {
                    String msg = "Error while copying the archetype to trunk";
                    log.error(msg, e);
                    throw new AppFactoryException(msg, e);
                }
            }
        }
    }

    /**
     *
     * @param appId
     * @param archetypeConfigProps
     * @return
     * @throws AppFactoryException
     */
    /*TODO: The best way to do this is to read these from a file. Then we have the option of not changing the code when these parameters change*/
    public static String getArchetypeRequest(String appId, String archetypeConfigProps) throws AppFactoryException {
        if (archetypeConfigProps == null) {
            String msg = "Could not find the maven archetype configuration";
            log.error(msg);
            throw new AppFactoryException(msg);
        }
        String replacement = AppFactoryConstants.MAVEN_ARTIFACT_ID_REPLACEMENT + appId;
        if (archetypeConfigProps.contains(AppFactoryConstants.MAVEN_ARTIFACT_ID)) {
            String currentArtifactId = archetypeConfigProps.substring(archetypeConfigProps.indexOf
                    (AppFactoryConstants.MAVEN_ARTIFACT_ID)).split(AppFactoryConstants.WHITE_SPACE)[0];
            archetypeConfigProps = archetypeConfigProps.replace(currentArtifactId, replacement);
        } else {
            archetypeConfigProps = archetypeConfigProps + replacement;
        }
        return archetypeConfigProps;
    }

    /**
     *
     * @param applicationId
     * @return
     * @throws AppFactoryException
     */
    public static String[] getVersionPaths(String applicationId) throws AppFactoryException {
        List<String> versionPaths = new ArrayList<String>();
        try {
            RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
            UserRegistry userRegistry = registryService.getGovernanceSystemRegistry(CarbonContext.
                    getThreadLocalCarbonContext().getTenantId());

            // child nodes of this will contains folders for all life cycles (e.g. QA, Dev, Prod)
            Resource application = userRegistry.get(AppFactoryConstants.REGISTRY_APPLICATION_PATH + RegistryConstants.
                    PATH_SEPARATOR + applicationId);
            if (application != null && application instanceof Collection) {

                // Contains paths to life cycles (.e.g .../<appid>/dev, .../<appid>/qa , .../<appid>/prod )
                String[] definedLifeCyclePaths = ((Collection) application).getChildren();
                for (String lcPath : definedLifeCyclePaths) {
                    Resource versionsInLCResource = userRegistry.get(lcPath);
                    if (versionsInLCResource != null && versionsInLCResource instanceof Collection) {

                        // contains paths to a versions (e.g. .../<appid>/<lifecycle>/trunk, .../<appid>/<lifecycle>/1.0.1 )
                        Collections.addAll(versionPaths, ((Collection) versionsInLCResource).getChildren());
                    }
                }
            }

        } catch (RegistryException e) {
            String errorMsg = String.format("Unable to load the application information for application key : %s",
                                            applicationId);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }
        return versionPaths.toArray(new String[versionPaths.size()]);
    }

    /**
     * Returns the type of the repository for a given application id and tenant
     *
     * @param applicationId Id of the application
     * @param tenantDomain  Tenant name of the application
     * @return Repository Type or null if artifact is null for the id and tenant domain in registry)
     * @throws AppFactoryException If Unable to find Application or Repository Type
     */
    public static String getRepositoryType(String applicationId, String tenantDomain) throws AppFactoryException {
        GenericArtifactImpl artifact = CommonUtil.getApplicationArtifact(applicationId, tenantDomain);
        try {
            if (artifact != null) {
                return artifact.getAttribute(AppFactoryConstants.RXT_KEY_APPINFO_REPO_TYPE);
            }
        } catch (RegistryException e) {
            String errorMsg = String.format("Unable to find the repository type for application key : %s",
                                            applicationId);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }
        return null;
    }

    /**
     *
     * @param absolutePath
     * @throws AppFactoryException
     */
    public static void generateGitIgnore(String absolutePath) throws AppFactoryException {
        File path = new File(absolutePath + File.separator + AppFactoryConstants.GIT_IGNORE_FILE);
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(path);
            os.write(AppFactoryConstants.GIT_IGNORE_CONTENT.getBytes());
            os.flush();
        } catch (FileNotFoundException e) {
            String errorMsg = "'Could not create file .gitignore";
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        } catch (IOException e) {
            String errorMsg = "'Could not write to .gitignore";
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                //ignore this catch clause
                log.warn("Error while creating file .gitignore");
            }
        }
    }

    /**
     * This method updates the application creation status
     *
     * @param applicationId  id of the application
     * @param creationStatus application creation status
     * @throws AppFactoryException
     */
    public static void updateApplicationCreationStatus(String applicationId, Constants.ApplicationCreationStatus
            creationStatus) throws AppFactoryException {
        applicationDAO.setApplicationCreationStatus(applicationId, creationStatus);
    }

    /**
     * Adds given version as a production version for the given application.
     * Updates appinfo rxt
     *
     * @param applicationId id of the application
     * @param version       new version
     * @throws AppFactoryException
     */
    public static void addProductionVersion(String applicationId, String version)
            throws AppFactoryException {
        GenericArtifact artifact;
        try {
            RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
            UserRegistry userRegistry = registryService.getGovernanceSystemRegistry(CarbonContext.
                    getThreadLocalCarbonContext().getTenantId());
            Resource resource = userRegistry.get(AppFactoryConstants.REGISTRY_APPLICATION_PATH +
                                                 RegistryConstants.PATH_SEPARATOR + applicationId + RegistryConstants.PATH_SEPARATOR +
                                                 AppFactoryConstants.RXT_KEY_APPINFO);
            GovernanceUtils.loadGovernanceArtifacts(userRegistry);
            GenericArtifactManager artifactManager = new GenericArtifactManager(userRegistry,
                                                                                AppFactoryConstants.RXT_KEY_APPINFO_APPLICATION);
            artifact = artifactManager.getGenericArtifact(resource.getUUID());
            String[] prodVersionsArr = artifact.getAttributes(AppFactoryConstants.RXT_KEY_APPINFO_PRODUCTION_VERSION);
            List<String> prodVersions = Arrays.asList(prodVersionsArr);
            if (!prodVersions.contains(version)) {
                prodVersions.add(version);
                artifact.setAttributes(AppFactoryConstants.RXT_KEY_APPINFO_PROD_VERSION,
                                       prodVersions.toArray(new String[prodVersions.size()]));
                artifactManager.updateGenericArtifact(artifact);
                log.info(String.format("Application - %s updated with new production version - %s", applicationId,
                                       version));
            }
        } catch (RegistryException e) {
            String errorMsg = String.format("Unable to load the application information for applicaiton id: %s",
                                            applicationId);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }
    }

    /**
     * Removes given version from  production versions of given application.
     * Updates appinfo rxt.
     *
     * @param applicationId id of the application
     * @param version       new version
     * @throws AppFactoryException
     */
    public static void removeProductionVersion(String applicationId, String version) throws AppFactoryException {
        GenericArtifact artifact;
        try {
            RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
            UserRegistry userRegistry = registryService.getGovernanceSystemRegistry(CarbonContext.
                    getThreadLocalCarbonContext().getTenantId());
            Resource resource = userRegistry.get(AppFactoryConstants.REGISTRY_APPLICATION_PATH +
                                                 RegistryConstants.PATH_SEPARATOR + applicationId + RegistryConstants.PATH_SEPARATOR
                                                 + AppFactoryConstants.RXT_KEY_APPINFO);
            GovernanceUtils.loadGovernanceArtifacts(userRegistry);
            GenericArtifactManager artifactManager = new GenericArtifactManager(userRegistry,
                                                                                AppFactoryConstants.RXT_KEY_APPINFO_APPLICATION);
            artifact = artifactManager.getGenericArtifact(resource.getUUID());
            String[] prodVersionsArr = artifact.getAttributes(AppFactoryConstants.RXT_KEY_APPINFO_PRODUCTION_VERSION);
            List<String> prodVersions = Arrays.asList(prodVersionsArr);
            if (!prodVersions.contains(version)) {
                prodVersions.remove(version);
                artifact.setAttributes(AppFactoryConstants.RXT_KEY_APPINFO_PROD_VERSION,
                                       prodVersions.toArray(new String[prodVersions.size()]));
                artifactManager.updateGenericArtifact(artifact);
                log.info(String.format("Application - %s updated with new production version - %s", applicationId,
                                       version));
            }
        } catch (RegistryException e) {
            String errorMsg = String.format("Unable to load the application information for applicaiton id: %s",
                                            applicationId);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }
    }

    /**
     * Method returns the default extension (eg:- war, zip) of given {@code applicationID}'s application type
     *
     * @param applicationID id of the application
     * @param tenantDomain  domain of the current tenant
     * @return Application type - String
     * @throws AppFactoryException
     */
    public static String getApplicationExtenstion(String applicationID,
                                                  String tenantDomain) throws AppFactoryException {

        String applicationType = ApplicationDAO.getInstance().getApplicationType(applicationID);
        ApplicationTypeBean applicationTypeProcessor = ApplicationTypeManager.getInstance().getApplicationTypeBean(
                applicationType);
        return applicationTypeProcessor.getExtension();
    }
}
