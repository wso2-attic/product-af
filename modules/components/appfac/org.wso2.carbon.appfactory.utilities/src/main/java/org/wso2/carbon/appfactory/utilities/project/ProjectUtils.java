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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.shared.invoker.*;
import org.codehaus.plexus.util.FileUtils;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.ApplicationTypeProcessor;
import org.wso2.carbon.appfactory.core.deploy.Artifact;
import org.wso2.carbon.appfactory.core.dto.Application;
import org.wso2.carbon.appfactory.core.dto.Version;
import org.wso2.carbon.appfactory.core.governance.RxtManager;
import org.wso2.carbon.appfactory.utilities.application.ApplicationTypeManager;
import org.wso2.carbon.appfactory.utilities.internal.ServiceReferenceHolder;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifactImpl;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProjectUtils {

    private static final Log log = LogFactory.getLog(ProjectUtils.class);
    private static String ARCHETYPE_DIR = "archetypeDir";

    public static void generateCAppArchetype(final String appId, String filePath, String archetypeRequest)
            throws AppFactoryException {
        generateProjectArchetype(appId, filePath, getArchetypeRequest(appId, archetypeRequest));
    }

    public static void generateWebAppArchetype(final String appId, String filePath, String archetypeRequest)
            throws AppFactoryException {
        generateProjectArchetype(appId, filePath, getArchetypeRequest(appId, archetypeRequest));
    }

    public static void generateJaxWebAppArchetype(final String appId, String filePath, String archetypeRequest)
            throws AppFactoryException {
        generateProjectArchetype(appId, filePath, getArchetypeRequest(appId, archetypeRequest));
    }

    public static void generateJaxRsWebAppArchetype(final String appId, String filePath, String archetypeRequest)
            throws AppFactoryException {
        generateProjectArchetype(appId, filePath, getArchetypeRequest(appId, archetypeRequest));
    }

    public static void generateJaggeryAppArchetype(final String appId, String filePath, String archetypeRequest)
            throws AppFactoryException {
        generateProjectArchetype(appId, filePath, getArchetypeRequest(appId, archetypeRequest));
    }

    public static void generateDBSAppArchetype(final String appId, String filePath, String archetypeRequest)
            throws AppFactoryException {
        generateProjectArchetype(appId, filePath, getArchetypeRequest(appId, archetypeRequest));
        //remove pom.xml from the root
        deletePOMFile(filePath);
    }

    private static void deletePOMFile(String filePath) throws AppFactoryException {
        try {
            File file = new File(filePath + "/pom.xml");
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            String msg = "Could not delete pom.xml in " + filePath;
            log.error(msg);
            throw new AppFactoryException(msg);
        }
    }

    public static void generateProjectArchetype(final String appId, String filePath, String archetypeRequest) throws AppFactoryException {

//        Check whether the maven home is set. If not, can not proceed further.
        String MAVEN_HOME;
        if ((MAVEN_HOME = System.getenv("M2_HOME")) == null) {
            if ((MAVEN_HOME = System.getenv("M3_HOME")) == null) {
                String msg = "valid maven installation is not found with M2_HOME or M3_HOME environment variable";
                log.error(msg);
                throw new AppFactoryException(msg);
            }
        }

        File workDir = new File(filePath);
//        Checking whether the app directory exists. If not, the previous process has failed. Hence returning
        if (!workDir.exists()) {
            log.warn(String.format("Work directory for application id : %s does not exist", appId));
            return;
        }

        File archetypeDir = new File(CarbonUtils.getTmpDir() + File.separator + appId + File.separator + ARCHETYPE_DIR);
        archetypeDir.mkdirs();

        List<String> goals = new ArrayList<String>();
        goals.add("archetype:generate");

        InvocationRequest request = new DefaultInvocationRequest();
        //request.setBaseDirectory(workDir);
        request.setBaseDirectory(archetypeDir);
        request.setShowErrors(true);
        request.setGoals(goals);
        request.setMavenOpts(archetypeRequest);

        InvocationResult result = null;
        try {
            Invoker invoker = new DefaultInvoker();

            InvocationOutputHandler outputHandler = new SystemOutHandler();
            invoker.setErrorHandler(outputHandler);
            invoker.setMavenHome(new File(MAVEN_HOME));
            invoker.setOutputHandler(new InvocationOutputHandler() {
                @Override
                public void consumeLine(String s) {
                    log.info(appId + ":" + s);
                }
            });

            result = invoker.execute(request);
        } catch (MavenInvocationException e) {
            String msg = "Failed to invoke maven archetype generation";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } finally {
            if (result != null && result.getExitCode() == 0) {
                log.info("Maven archetype generation completed successfully");
                configureFinalName(archetypeDir.getAbsolutePath());
                copyArchetypeToTrunk(archetypeDir.getAbsolutePath(), workDir.getAbsolutePath());
                try {
                    FileUtils.deleteDirectory(archetypeDir);
                } catch (IOException e) {
                    log.error("Error deleting archetype directory " + e.getMessage(), e);
                }
            }
        }
    }

    private static void configureFinalName(String path) {
        File artifactDir = new File(path);
        MavenXpp3Reader mavenXpp3Reader = new MavenXpp3Reader();
        Model model;
        try {
            String[] fileExtension = {"xml"};
            List<File> fileList = (List<File>) org.apache.commons.io.FileUtils.listFiles(artifactDir,
                    fileExtension, true);

            for (File file : fileList) {

                if (file.getName().equals("pom.xml")) {
                    FileInputStream stream = new FileInputStream(file);
                    model = mavenXpp3Reader.read(stream);
                    if (model.getBuild() != null && model.getBuild().getFinalName() != null) {
                        model.getBuild().setFinalName("${artifactId}-${version}");
                    }
                    if (stream != null) {
                        stream.close();
                    }
                    MavenXpp3Writer writer = new MavenXpp3Writer();
                    writer.write(new FileWriter(file), model);
                }
            }
        } catch (Exception e) {
            //TODO
            e.printStackTrace();
        }
    }

    private static void copyArchetypeToTrunk(String srcPath, String workPath)
            throws AppFactoryException {
        File srcDir = new File(srcPath);
        File destDir = new File(workPath);

        for (File file : srcDir.listFiles()) {
            if (!file.exists()) {
                String msg = "Source directory does not exist";
                log.error(msg);
                throw new AppFactoryException(msg);
            } else {
                try {
                    copyDir(file, destDir);
                } catch (IOException e) {
                    String msg = "Error while copying the archetype to trunk";
                    log.error(msg, e);
                    throw new AppFactoryException(msg, e);
                }
            }
        }

    }

    private static void copyDir(File src, File dest) throws IOException {
        if (src.isDirectory()) {
            if (!dest.exists()) {
                dest.mkdirs();
            }
            String files[] = src.list();
            for (String file : files) {
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                copyDir(srcFile, destFile);
            }
        } else {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];

            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            in.close();
            out.close();
            if (log.isDebugEnabled()) {
                log.debug("File copied from " + src + " to " + dest);
            }
        }
    }

    /*TODO: The best way to do this is to read these from a file. Then we have the option of not changing the code when these parameters change*/
    public static String getArchetypeRequest(String appId, String archetypeConfigProps) throws AppFactoryException {
        if (archetypeConfigProps == null) {
            String msg = "Could not find the maven archetype configuration";
            log.error(msg);
            throw new AppFactoryException(msg);
        }

        String replacement = " -DartifactId=" + appId;
        if (archetypeConfigProps.contains("-DartifactId=")) {
            String currentArtifactId = archetypeConfigProps.substring(archetypeConfigProps.indexOf("-DartifactId=")).split(" ")[0];
            archetypeConfigProps = archetypeConfigProps.replace(currentArtifactId, replacement);
        } else {
            archetypeConfigProps = archetypeConfigProps + replacement;
        }
        return archetypeConfigProps;

/*
//Commenting out the rest because we read the props from the appfactory config

        StringBuilder optsBuilder = new StringBuilder();

        optsBuilder.append(" -DarchetypeGroupId=org.wso2.carbon.appfactory.maven.archetype");
        optsBuilder.append(" -DarchetypeArtifactId=af-archetype");
        optsBuilder.append(" -DarchetypeVersion=1.0.0");
        optsBuilder.append(" -DgroupId=org.wso2.af");
        optsBuilder.append(" -Dversion=1.0.1");
        optsBuilder.append(" -DinteractiveMode=false");
        optsBuilder.append(" -DarchetypeCatalog=local");
        optsBuilder.append(" -DartifactId=").append(appid);


        return optsBuilder.toString();*/
    }

    /**
     * Returns the type of the application for a given the application Id and tenant name
     *
     * @param applicationId Id of the application
     * @param tenantDomain  Tenant domain of the application
     * @return the application type
     * @throws AppFactoryException If invalid application or application type is not available
     */
    public static String getApplicationType(String applicationId, String tenantDomain) throws AppFactoryException {
        GenericArtifactImpl artifact = getApplicationArtifact(applicationId, tenantDomain);

        if (artifact == null) {
            String errorMsg =
                    String.format("Unable to find applcation information for id : %s",
                            applicationId);
            log.error(errorMsg);
            throw new AppFactoryException(errorMsg);
        }

        try {
            return artifact.getAttribute("application_type");
        } catch (RegistryException e) {
            String errorMsg =
                    String.format("Unable to find the application type for application " +
                            "id: %s",
                            applicationId);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }
    }

    /**
     * Provides information about an application.
     *
     * @param applicationId id of the application
     * @return {@link Application}
     * @throws AppFactoryException Throws error when unable
     *                             to extract application information
     */
    public static Application getApplicationInfo(String applicationId, String domainName) throws AppFactoryException {

        GenericArtifactImpl artifact = getApplicationArtifact(applicationId, domainName);

        if (artifact == null) {
            String errorMsg =
                    String.format("Unable to find application information for id : %s",
                            applicationId);
            log.error(errorMsg);
            throw new AppFactoryException(errorMsg);
        }
        return getAppInfoFromRXT(artifact);
    }

    public static Application getAppInfoFromRXT(GenericArtifact artifact) throws
            AppFactoryException {
        Application appInfo;

        try {
            appInfo =
                    new Application(artifact.getAttribute("application_key"),
                            artifact.getAttribute("application_name"),
                            artifact.getAttribute("application_type"),
                            artifact.getAttribute("application_repositorytype"),
                            artifact.getAttribute("application_description"),artifact.getAttribute("application_owner"));

            String branchCount = artifact.getAttribute("application_branchcount");
            if (branchCount != null) {
                appInfo.setBranchCount(Integer.valueOf(branchCount));
            }

            String[] prodVersions = artifact.getAttributes("application_prodVersions");
            if (prodVersions != null && prodVersions.length > 0) {
                appInfo.setProduction(Boolean.TRUE);
            }

        } catch (GovernanceException e) {
            String errorMsg =
                    String.format("Unable to extract information from RXT: %s",
                            artifact.getId());
            log.error(errorMsg);
            throw new AppFactoryException(errorMsg);
        }

        return appInfo;
    }

    /**
     * Returns all available versions of a application
     *
     * @param applicationId Id of the application
     * @return an Array of {@link Version}
     * @throws AppFactoryException if an error occurres
     */
    public static Version[] getVersions(String applicationId,String domainName) throws AppFactoryException {
        List<Version> versions = new ArrayList<Version>();

        RxtManager rxtManager = new RxtManager();
        /*try {
            RegistryService registryService =
                                              ServiceReferenceHolder.getInstance()
                                                                    .getRegistryService();
            UserRegistry userRegistry = registryService.getGovernanceSystemRegistry();
            // child nodes of this will contains folders for all life cycles (
            // e.g. QA, Dev, Prod)
            Resource application =
                                   userRegistry.get(AppFactoryConstants.REGISTRY_APPLICATION_PATH +
                                                    File.separator + applicationId);

            if (application != null && application instanceof Collection) {

                // Contains paths to life cycles (.e.g .../<appid>/dev,
                // .../<appid>/qa , .../<appid>/prod )
                String[] definedLifeCyclePaths = ((Collection) application).getChildren();

                for (String lcPath : definedLifeCyclePaths) {

                    Resource versionsInLCResource = userRegistry.get(lcPath);
                    if (versionsInLCResource != null && versionsInLCResource instanceof Collection) {

                        // contains paths to a versions (e.g.
                        // .../<appid>/<lifecycle>/trunk,
                        // .../<appid>/<lifecycle>/1.0.1 )*/
        List<Artifact> artifactList;
        try {
            artifactList = rxtManager.getAppVersionRxtForApplication(domainName,applicationId);
        } catch (RegistryException e) {
            String errorMsg =
                    String.format("Unable to load the application version information for applicaiton id: %s",
                            applicationId);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }

        for (Artifact artifact : artifactList) {
            // extract the name of the resource ( which will be
            // the version id)
            String lifecycleStage = artifact.getStage();
            String versionId = artifact.getVersion();
            Version version = new Version(versionId);
            version.setLifecycleStage(lifecycleStage);
            versions.add(version);
        }


        /*}

            }

        }*/

        /*} catch (RegistryException e) {
            String errorMsg =
                              String.format("Unable to load the application information for applicaiton id: %s",
                                            applicationId);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }*/

        return versions.toArray(new Version[versions.size()]);
    }

    public static String[] getVersionPaths(String applicationId) throws AppFactoryException {
        List<String> versionPaths = new ArrayList<String>();
        try {
            RegistryService registryService =
                    ServiceReferenceHolder.getInstance()
                            .getRegistryService();
            UserRegistry userRegistry = registryService.getGovernanceSystemRegistry(CarbonContext.getThreadLocalCarbonContext().getTenantId());
            // child nodes of this will contains folders for all life cycles (
            // e.g. QA, Dev, Prod)
            Resource application =
                    userRegistry.get(AppFactoryConstants.REGISTRY_APPLICATION_PATH +
                            "/" + applicationId);

            if (application != null && application instanceof Collection) {

                // Contains paths to life cycles (.e.g .../<appid>/dev,
                // .../<appid>/qa , .../<appid>/prod )
                String[] definedLifeCyclePaths = ((Collection) application).getChildren();

                for (String lcPath : definedLifeCyclePaths) {

                    Resource versionsInLCResource = userRegistry.get(lcPath);
                    if (versionsInLCResource != null && versionsInLCResource instanceof Collection) {

                        // contains paths to a versions (e.g.
                        // .../<appid>/<lifecycle>/trunk,
                        // .../<appid>/<lifecycle>/1.0.1 )
                        for (String version : ((Collection) versionsInLCResource).getChildren()) {
                            versionPaths.add(version);
                        }
                    }

                }

            }

        } catch (RegistryException e) {
            String errorMsg =
                    String.format("Unable to load the application information for applicaiton id: %s",
                            applicationId);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }
        return versionPaths.toArray(new String[versionPaths.size()]);
    }

    /**
     * A Util method to load an Application artifact from the registry.
     *
     * @param applicationId the application Id
     * @param tenantDomain  the tenant name of the application
     * @return a {@link GenericArtifactImpl} representing the application or
     *         null if application (by the id is not in registry)
     * @throws AppFactoryException if an error occurs.
     */
    public static GenericArtifactImpl getApplicationArtifact(String applicationId, String tenantDomain)
            throws AppFactoryException {
        GenericArtifact artifact = null;
        try {
            if(log.isDebugEnabled()){
                log.debug("Tenant Domain : " + tenantDomain);
            }

            RegistryService registryService =
                    ServiceReferenceHolder.getInstance()
                            .getRegistryService();
            UserRegistry userRegistry = registryService.getGovernanceSystemRegistry(
                    ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(tenantDomain));
            String path = AppFactoryConstants.REGISTRY_APPLICATION_PATH + "/" + applicationId + "/" + "appinfo";

            if (log.isDebugEnabled()) {
                log.debug("Username for registry :" + userRegistry.getUserName() + " Tenant ID : " + userRegistry.getTenantId());
                log.debug("Username from carbon context :" + CarbonContext.getThreadLocalCarbonContext().getUsername());
            }
            if(!userRegistry.resourceExists(path)){
                return null;
            }

            Resource resource = userRegistry.get(path);
            artifact = getApplicationRXTManager(tenantDomain).getGenericArtifact(resource.getUUID());

        } catch (RegistryException e) {
            String errorMsg =
                    String.format("Unable to load the application information for applicaiton id: %s",
                            applicationId);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        } catch (UserStoreException e) {
            String errorMsg =
                    String.format("User Registration Error for applicaiton id: %s",
                            applicationId);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }

        return (GenericArtifactImpl) artifact;
    }

    /**
     * Return App Info RXT Manager for a tenant
     *
     * @param tenantDomain
     * @return
     * @throws AppFactoryException
     */
    public static GenericArtifactManager getApplicationRXTManager(String tenantDomain) throws AppFactoryException {

        RegistryService registryService =
                ServiceReferenceHolder.getInstance()
                        .getRegistryService();
        UserRegistry userRegistry = null;
        GenericArtifactManager artifactManager;
        try {
            userRegistry = registryService.getGovernanceSystemRegistry(ServiceReferenceHolder.getInstance().
                    getRealmService().getTenantManager().getTenantId(tenantDomain));
            GovernanceUtils.loadGovernanceArtifacts(userRegistry);
            artifactManager =
                    new GenericArtifactManager(userRegistry,
                            "application");
        } catch (RegistryException e) {
            String errorMsg =
                    String.format("Error while getting application RXT for: %s in tenant: %s", tenantDomain);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        } catch (UserStoreException e) {
            String errorMsg =
                    String.format("Error while getting tenant id for %s",
                            tenantDomain);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }

        return artifactManager;
    }

    /**
     * Returns the type of the repository for a given application id and tenant
     *
     * @param applicationId Id of the application
     * @param tenantDomain  Tenant name of the application
     * @return Repository Type
     * @throws AppFactoryException If Unable to find Application or Repository Type
     */
    public static String getRepositoryType(String applicationId, String tenantDomain) throws AppFactoryException {

        GenericArtifactImpl artifact = getApplicationArtifact(applicationId, tenantDomain);

        if (artifact == null) {
            String errorMsg =
                    String.format("Unable to find applcation information for id : %s",
                            applicationId);
            log.error(errorMsg);
            throw new AppFactoryException(errorMsg);
        }

        try {
            return artifact.getAttribute("application_repositorytype");
        } catch (RegistryException e) {
            String errorMsg =
                    String.format("Unable to find the repository type for application id: %s",
                            applicationId);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }
    }

    public static void generateGitIgnore(String absolutePath) throws AppFactoryException {
        File path = new File(absolutePath + File.separator + ".gitignore");
        String ignoreContent = "*\n" +
                "\n" +
                "!.gitignore";
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(path);
            os.write(ignoreContent.getBytes());
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
                os.close();
            } catch (IOException e) {
                //ignore
            }
        }

    }

    /**
     * Updates appinfo rxt the no branch count by retrieving all the branches created for the given application.
     *
     * @param applicationId
     * @throws AppFactoryException
     */
    public static void updateBranchCount(String applicationId) throws AppFactoryException {


        RegistryService registryService =
                ServiceReferenceHolder.getInstance()
                        .getRegistryService();
        UserRegistry userRegistry;
        try {
            userRegistry = registryService.getGovernanceSystemRegistry(CarbonContext.getThreadLocalCarbonContext().getTenantId());
        } catch (RegistryException e) {
            String errorMsg =
                    String.format("Unable to load userRegistry");
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }
        updateBranchCount(userRegistry, applicationId, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
    }

    private static void updateBranchCount(UserRegistry userRegistry, String applicationId,
                                          String domainName) throws AppFactoryException {
        GenericArtifact artifact = null;
        try {
            Resource resource =
                    userRegistry.get(AppFactoryConstants.REGISTRY_APPLICATION_PATH +
                            "/" + applicationId + "/" + "appinfo");
            GovernanceUtils.loadGovernanceArtifacts(userRegistry);
            GenericArtifactManager artifactManager =
                    new GenericArtifactManager(userRegistry,
                            "application");
            artifact = artifactManager.getGenericArtifact(resource.getUUID());

            RxtManager rxtManager = new RxtManager();
            List<Artifact> appVersions = rxtManager.getAppVersionRxtForApplication(domainName,
                    applicationId);
            String newBranchCount = String.valueOf(appVersions.size());

            artifact.setAttribute("application_branchcount", newBranchCount);

            artifactManager.updateGenericArtifact(artifact);

            log.info(String.format("Application - %s Branch count is updated to - %s", applicationId, newBranchCount));
        } catch (RegistryException e) {
            String errorMsg =
                    String.format("Unable to load the application information for applicaiton id: %s",
                            applicationId);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }
    }

    public static void updateBranchCount(String domainName, String applicationId) throws AppFactoryException {
        RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
        UserRegistry userRegistry;
        try {
            userRegistry = registryService.getGovernanceSystemRegistry(ServiceReferenceHolder.getInstance().
                    getRealmService().getTenantManager().getTenantId(domainName));
            updateBranchCount(userRegistry, applicationId,domainName);
        } catch (UserStoreException e) {
            String errorMsg =
                    String.format("Unable to get tenant id for domain: %s",
                            domainName);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        } catch (RegistryException e) {
            String errorMsg =
                    String.format("Unable to load userRegistry");
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }
    }

    public static void generateBPELArchetype(String applicationKey, String absolutePath, String archetypeRequest) throws AppFactoryException {
        generateProjectArchetype(applicationKey, absolutePath, getArchetypeRequest(applicationKey, archetypeRequest));
    }

    public static void generatePHPArchetype(String applicationKey, String absolutePath, String archetypeRequest) throws AppFactoryException {
        generateProjectArchetype(applicationKey, absolutePath, getArchetypeRequest(applicationKey, archetypeRequest));
        //delete pom.xm generated by maven archetype
        deletePOMFile(absolutePath);
    }

    public static void generateESBArchetype(String applicationKey, String absolutePath, String archetypeRequest) throws AppFactoryException {
        generateProjectArchetype(applicationKey, absolutePath, getArchetypeRequest(applicationKey, archetypeRequest));
        //delete pom.xm generated by maven archetype
        //deletePOMFile(absolutePath);
    }

    /**
     * Adds given version as a production version for the given application.
     * Updates appinfo rxt
     *
     * @param applicationId
     * @param version
     * @throws AppFactoryException
     */
    public static void addProductionVersion(String applicationId, String version)
            throws AppFactoryException {
        GenericArtifact artifact = null;
        try {
            RegistryService registryService =
                    ServiceReferenceHolder.getInstance()
                            .getRegistryService();
            UserRegistry userRegistry = registryService.getGovernanceSystemRegistry(CarbonContext.getThreadLocalCarbonContext().getTenantId());
            Resource resource =
                    userRegistry.get(AppFactoryConstants.REGISTRY_APPLICATION_PATH +
                            "/" + applicationId + "/" + "appinfo");
            GovernanceUtils.loadGovernanceArtifacts(userRegistry);
            GenericArtifactManager artifactManager =
                    new GenericArtifactManager(userRegistry,
                            "application");
            artifact = artifactManager.getGenericArtifact(resource.getUUID());

            String[] prodVersionsArr = artifact.getAttributes("application_productionVersions");
            List<String> prodVersions = Arrays.asList(prodVersionsArr);

            if (!prodVersions.contains(version)) {
                prodVersions.add(version);
                artifact.setAttributes("application_prodVersions",
                        prodVersions.toArray(new String[prodVersions.size()]));
                artifactManager.updateGenericArtifact(artifact);
                log.info(String.format("Application - %s updated with new production version - %s",
                        applicationId, version));
            }
        } catch (RegistryException e) {
            String errorMsg =
                    String.format("Unable to load the application information for applicaiton id: %s",
                            applicationId);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }

    }

    /**
     * Removes given version from  production versions of given application.
     * Updates appinfo rxt.
     *
     * @param applicationId
     * @param version
     * @throws AppFactoryException
     */
    public static void removeProductionVersion(String applicationId, String version) throws AppFactoryException {
        GenericArtifact artifact = null;
        try {
            RegistryService registryService =
                    ServiceReferenceHolder.getInstance()
                            .getRegistryService();
            UserRegistry userRegistry = registryService.getGovernanceSystemRegistry(CarbonContext.getThreadLocalCarbonContext().getTenantId());
            Resource resource =
                    userRegistry.get(AppFactoryConstants.REGISTRY_APPLICATION_PATH +
                            "/" + applicationId + "/" + "appinfo");
            GovernanceUtils.loadGovernanceArtifacts(userRegistry);
            GenericArtifactManager artifactManager =
                    new GenericArtifactManager(userRegistry,
                            "application");
            artifact = artifactManager.getGenericArtifact(resource.getUUID());

            String[] prodVersionsArr = artifact.getAttributes("application_productionVersions");
            List<String> prodVersions = Arrays.asList(prodVersionsArr);

            if (!prodVersions.contains(version)) {
                prodVersions.remove(version);
                artifact.setAttributes("application_prodVersions",
                        prodVersions.toArray(new String[prodVersions.size()]));
                artifactManager.updateGenericArtifact(artifact);
                log.info(String.format("Application - %s updated with new production version - %s",
                        applicationId, version));
            }
        } catch (RegistryException e) {
            String errorMsg =
                    String.format("Unable to load the application information for applicaiton id: %s",
                            applicationId);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }

    }
    
    public static String getApplicationExtenstion(String applicationID,
			String tenantDomain) throws AppFactoryException {
		
    	String applicationType = getApplicationType(applicationID, tenantDomain);
    	ApplicationTypeProcessor applicationTypeProcessor = ApplicationTypeManager.getInstance().getApplicationTypeProcessor(applicationType);
		return applicationTypeProcessor.getFileExtension();
	}
}
