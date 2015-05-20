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
import org.wso2.carbon.appfactory.core.dao.JDBCApplicationDAO;
import org.wso2.carbon.appfactory.core.deploy.Artifact;
import org.wso2.carbon.appfactory.core.dto.Version;
import org.wso2.carbon.appfactory.core.governance.ApplicationManager;
import org.wso2.carbon.appfactory.core.governance.RxtManager;
import org.wso2.carbon.appfactory.core.util.AppFactoryCoreUtil;
import org.wso2.carbon.appfactory.core.util.CommonUtil;
import org.wso2.carbon.appfactory.core.util.Constants;
import org.wso2.carbon.appfactory.utilities.internal.ServiceReferenceHolder;
import org.wso2.carbon.base.MultitenantConstants;
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
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.CarbonUtils;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * ProjectUtil class holds the utility methods to generate projects
 */
public class ProjectUtils {
    private static final Log log = LogFactory.getLog(ProjectUtils.class);
	public static JDBCApplicationDAO applicationDAO = JDBCApplicationDAO.getInstance();

	/**
	 * This method generates the archetype for the projects
	 * @param appId Id of the application
	 * @param filePath Root path of the application
	 * @param archetypeRequest Maven archetype request
	 * @throws AppFactoryException
	 */
    public static void generateProjectArchetype(final String appId, String filePath, String archetypeRequest)
		    throws AppFactoryException {

//      Check whether the maven home is set. If not, can not proceed further.
	    String mavenHome = System.getenv(AppFactoryConstants.SYSTEM_VARIABLE_M2_HOME);
	    if (StringUtils.isBlank(mavenHome)) {
		    mavenHome = System.getenv(AppFactoryConstants.SYSTEM_VARIABLE_M3_HOME);
		    if (StringUtils.isBlank(mavenHome)) {
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
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        File archetypeDir = new File(
                CarbonUtils.getTmpDir() + File.separator + tenantDomain + File.separator + appId + File.separator +
                AppFactoryConstants.MAVEN_ARCHETYPE_DIR);
        archetypeDir.mkdirs();

        List<String> goals = new ArrayList<String>();
        goals.add(AppFactoryConstants.GOAL_MAVEN_ARCHETYPE_GENERATE);

        InvocationRequest request = new DefaultInvocationRequest();
        request.setBaseDirectory(archetypeDir);
        request.setShowErrors(true);
        request.setGoals(goals);
        request.setMavenOpts(archetypeRequest);

        InvocationResult result = null;
        try {
            Invoker invoker = new DefaultInvoker();

            InvocationOutputHandler outputHandler = new SystemOutHandler();
            invoker.setErrorHandler(outputHandler);
            invoker.setMavenHome(new File(mavenHome));
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

                if (AppFactoryCoreUtil.isBuildServerRequiredProject(ApplicationManager.getInstance().getApplicationType(appId))) {
                    File deployArtifact = generateDeployArtifact(appId, archetypeDir.getAbsolutePath(), mavenHome);
                    moveDepolyArtifact(deployArtifact, workDir.getParentFile());
                }
                configureFinalName(archetypeDir.getAbsolutePath());
                copyArchetypeToTrunk(archetypeDir.getAbsolutePath(), workDir.getAbsolutePath());
	            boolean deleteResult = FileUtils.deleteQuietly(archetypeDir);
                if(!deleteResult){
	                log.warn("Error while deleting the archetype directory");
                }

            }
        }
    }

    /**
     * Move deploy artifact to a new path
     *
     * @param deployAtrifact
     * @param parentFile
     * @throws AppFactoryException
     */
    private static void moveDepolyArtifact(File deployAtrifact, File parentFile) throws AppFactoryException{
        try {
            FileUtils.moveDirectoryToDirectory(deployAtrifact, parentFile, false);
        } catch (IOException e) {
            String msg = "Error while moving deploy artifact from "+ deployAtrifact.getAbsolutePath()
                         + " to " + parentFile.getAbsolutePath();
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
    }

    /**
     * Generate the deploy artifact
     *
     * @param appId application key
     * @param archetypeDir Parent directory where the project has been created
     * @param mavenHome maven home
     * @return Root directory in which deploy artifact is available
     * @throws AppFactoryException
     */
    private static File generateDeployArtifact(final String appId, final String archetypeDir, String mavenHome)
            throws AppFactoryException {
        File projectDir = new File(archetypeDir + File.separator + appId);
        List<String> newGoals = new ArrayList<String>();
        newGoals.add("clean");
        newGoals.add("install");
        newGoals.add("-f assembly.xml");
        InvocationRequest deployArtifactCreateReq = new DefaultInvocationRequest();
        deployArtifactCreateReq.setBaseDirectory(projectDir);
        deployArtifactCreateReq.setShowErrors(true);
        deployArtifactCreateReq.setGoals(newGoals);
        InvocationResult result = null;
        Invoker invoker = new DefaultInvoker();
        InvocationOutputHandler outputHandler = new SystemOutHandler();
        invoker.setErrorHandler(outputHandler);
        invoker.setMavenHome(new File(mavenHome));
        invoker.setOutputHandler(new InvocationOutputHandler() {
            @Override
            public void consumeLine(String s) {
                log.info(appId + ":" + s);
            }
        });
        try {
            result = invoker.execute(deployArtifactCreateReq);
            File deployArtifact = new File(archetypeDir + File.separator + appId + "_deploy_artifact");
            if(deployArtifact.exists()){
                return deployArtifact;
            }else{
                throw new AppFactoryException("Deployable artifact has not generated in path "+ deployArtifact.getAbsolutePath());
            }
        } catch (MavenInvocationException e) {
            String msg = "Failed to invoke deployable artifact generation";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } finally {
            if (result != null && result.getExitCode() == 0) {
                try {
                    File builtArtifactDir = new File(projectDir + "/built_artifact/");
                    FileUtils.deleteDirectory(builtArtifactDir);
                    File assemblyFile = new File(projectDir + "/assembly.xml");
                    FileUtils.forceDelete(assemblyFile);
                    File assemblyDescriptorFile = new File(projectDir + "/bin.xml");
                    FileUtils.forceDelete(assemblyDescriptorFile);
                } catch (IOException e) {
                    String msg = "Error occurred while deleting files used in deploy artifact generation";
                    log.error(msg, e);
                    throw new AppFactoryException(msg, e);
                }
                log.info("Deployable artifact generation completed successfully");
            }
        }
    }

    /**
     *
     * @param path
     * @throws AppFactoryException
     */
    private static void configureFinalName(String path) throws AppFactoryException {
        File artifactDir = new File(path);
	        String[] fileExtension = { AppFactoryConstants.APPLICATION_TYPE_XML };
	        List<File> fileList = (List<File>) FileUtils.listFiles(artifactDir, fileExtension, true);
            for (File file : fileList) {
                setArtifactName(file);
            }
    }

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

	private static void copyArchetypeToTrunk(String srcPath, String workPath) throws AppFactoryException {
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

    /*TODO: The best way to do this is to read these from a file. Then we have the option of not changing the code when these parameters change*/
    public static String getArchetypeRequest(String appId, String archetypeConfigProps) throws AppFactoryException {
        if (archetypeConfigProps == null) {
            String msg = "Could not find the maven archetype configuration";
            log.error(msg);
            throw new AppFactoryException(msg);
        }
	    String replacement = AppFactoryConstants.MAVEN_ARTIFACT_ID_REPLACEMENT + appId;
	    if (archetypeConfigProps.contains(AppFactoryConstants.MAVEN_ARTIFACT_ID)) {
		    String currentArtifactId =
				    archetypeConfigProps.substring(archetypeConfigProps.indexOf(AppFactoryConstants.MAVEN_ARTIFACT_ID))
				                        .split(AppFactoryConstants.WHITE_SPACE)[0];
		    archetypeConfigProps = archetypeConfigProps.replace(currentArtifactId, replacement);
	    } else {
		    archetypeConfigProps = archetypeConfigProps + replacement;
	    }
        return archetypeConfigProps;
    }

    /**
     * Returns all available versions of a application
     *
     * @param applicationId Id of the application
     * @return an Array of {@link Version}
     * @throws AppFactoryException if an error occurs
     */
    public static Version[] getVersions(String applicationId,String domainName) throws AppFactoryException {
        List<Version> versions = new ArrayList<Version>();
        List<Artifact> artifactList;
                    try {
                        artifactList = RxtManager.getInstance().getAppVersionRxtForApplication(domainName, applicationId);
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
        return versions.toArray(new Version[versions.size()]);
    }

    public static String[] getVersionPaths(String applicationId) throws AppFactoryException {
        List<String> versionPaths = new ArrayList<String>();
        try {
            RegistryService registryService =
                    ServiceReferenceHolder.getInstance()
                            .getRegistryService();
            UserRegistry userRegistry = registryService.getGovernanceSystemRegistry(CarbonContext.
                    getThreadLocalCarbonContext().getTenantId());
            // child nodes of this will contains folders for all life cycles (
            // e.g. QA, Dev, Prod)
            Resource application =
                    userRegistry.get(AppFactoryConstants.REGISTRY_APPLICATION_PATH +
                                     RegistryConstants.PATH_SEPARATOR + applicationId);

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
                        Collections.addAll(versionPaths, ((Collection) versionsInLCResource).getChildren());
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
            String errorMsg =
                    String.format("Unable to find the repository type for application id: %s",
                            applicationId);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }
	    return null;
    }

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
	            if (os != null){
		            os.close();
	            }
            } catch (IOException e) {
                //ignore this catch clause
	            log.warn("Error while creating file .gitignore");
            }
        }

    }

    /**
     * Updates appinfo rxt the no branch count by retrieving all the branches created for the given application.
     *
     * @param applicationId Id of the application
     * @throws AppFactoryException
     */
    public static void updateBranchCount(String applicationId) throws AppFactoryException {

	    RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
	    UserRegistry userRegistry;
        try {
	        userRegistry = registryService
			        .getGovernanceSystemRegistry(CarbonContext.getThreadLocalCarbonContext().getTenantId());
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
        GenericArtifact artifact;
        try {
	        Resource resource =
			        userRegistry.get(AppFactoryConstants.REGISTRY_APPLICATION_PATH +
			                         RegistryConstants.PATH_SEPARATOR + applicationId +
			                         RegistryConstants.PATH_SEPARATOR + AppFactoryConstants.RXT_KEY_APPINFO);
	        GovernanceUtils.loadGovernanceArtifacts(userRegistry);
	        GenericArtifactManager artifactManager =
			        new GenericArtifactManager(userRegistry,
			                                   AppFactoryConstants.RXT_KEY_APPINFO_APPLICATION);
	        artifact = artifactManager.getGenericArtifact(resource.getUUID());
            List<Artifact> appVersions = RxtManager.getInstance().getAppVersionRxtForApplication(domainName, applicationId);
            String newBranchCount = String.valueOf(appVersions.size());
            artifact.setAttribute(AppFactoryConstants.RXT_KEY_APPINFO_BRANCHCOUNT, newBranchCount);
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

	/**
	 * This method updates the application creation status
	 *
	 * @param applicationId id of the application
	 * @param creationStatus application creation status
	 * @throws AppFactoryException
	 */
	public static void updateApplicationCreationStatus(String applicationId,
	                                                   Constants.ApplicationCreationStatus creationStatus)
			throws AppFactoryException {
		applicationDAO.setApplicationCreationStatus(applicationId, creationStatus);
	}

    /**
     * Adds given version as a production version for the given application.
     * Updates appinfo rxt
     *
     * @param applicationId id of the application
     * @param version new version
     * @throws AppFactoryException
     */
    public static void addProductionVersion(String applicationId, String version)
            throws AppFactoryException {
        GenericArtifact artifact;
        try {
            RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
	        UserRegistry userRegistry = registryService
			        .getGovernanceSystemRegistry(CarbonContext.getThreadLocalCarbonContext().getTenantId());
	        Resource resource = userRegistry.get(AppFactoryConstants.REGISTRY_APPLICATION_PATH +
	                                             RegistryConstants.PATH_SEPARATOR + applicationId +
	                                             RegistryConstants.PATH_SEPARATOR +
	                                             AppFactoryConstants.RXT_KEY_APPINFO);
	        GovernanceUtils.loadGovernanceArtifacts(userRegistry);
	        GenericArtifactManager artifactManager =
			        new GenericArtifactManager(userRegistry,
			                                   AppFactoryConstants.RXT_KEY_APPINFO_APPLICATION);
	        artifact = artifactManager.getGenericArtifact(resource.getUUID());

            String[] prodVersionsArr = artifact.getAttributes(AppFactoryConstants.RXT_KEY_APPINFO_PRODUCTION_VERSION);
            List<String> prodVersions = Arrays.asList(prodVersionsArr);

            if (!prodVersions.contains(version)) {
                prodVersions.add(version);
                artifact.setAttributes(AppFactoryConstants.RXT_KEY_APPINFO_PROD_VERSION,
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
     * @param applicationId id of the application
     * @param version new version
     * @throws AppFactoryException
     */
    public static void removeProductionVersion(String applicationId, String version) throws AppFactoryException {
        GenericArtifact artifact;
        try {
            RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
            UserRegistry userRegistry = registryService.getGovernanceSystemRegistry(CarbonContext.
                    getThreadLocalCarbonContext().getTenantId());
	        Resource resource =
			        userRegistry.get(AppFactoryConstants.REGISTRY_APPLICATION_PATH +
			                         RegistryConstants.PATH_SEPARATOR + applicationId +
			                         RegistryConstants.PATH_SEPARATOR + AppFactoryConstants.RXT_KEY_APPINFO);
	        GovernanceUtils.loadGovernanceArtifacts(userRegistry);
            GenericArtifactManager artifactManager =
                    new GenericArtifactManager(userRegistry,
                            AppFactoryConstants.RXT_KEY_APPINFO_APPLICATION);
            artifact = artifactManager.getGenericArtifact(resource.getUUID());

            String[] prodVersionsArr = artifact.getAttributes(AppFactoryConstants.RXT_KEY_APPINFO_PRODUCTION_VERSION);
            List<String> prodVersions = Arrays.asList(prodVersionsArr);

            if (!prodVersions.contains(version)) {
                prodVersions.remove(version);
                artifact.setAttributes(AppFactoryConstants.RXT_KEY_APPINFO_PROD_VERSION,
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
     * Method returns the default extension (eg:- war, zip) of given {@code applicationID}'s application type
     * @param applicationID id of the application
     * @param tenantDomain domain of the current tenant
     * @return Application type - String
     * @throws AppFactoryException
     */
    public static String getApplicationExtenstion(String applicationID,
			String tenantDomain) throws AppFactoryException {

    	String applicationType = ApplicationManager.getInstance().getApplicationType(applicationID);
    	ApplicationTypeBean applicationTypeProcessor = ApplicationTypeManager.getInstance().getApplicationTypeBean(applicationType);
		return applicationTypeProcessor.getExtension();
	}
}
