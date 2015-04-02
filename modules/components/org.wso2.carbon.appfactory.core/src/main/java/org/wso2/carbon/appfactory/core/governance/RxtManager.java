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

package org.wso2.carbon.appfactory.core.governance;

import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.dao.JDBCApplicationDAO;
import org.wso2.carbon.appfactory.core.deploy.Artifact;
import org.wso2.carbon.appfactory.core.dto.BuildStatus;
import org.wso2.carbon.appfactory.core.dto.DeployStatus;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactFilter;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifactImpl;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class RxtManager {
    /*
     * todo: this class was moved from appfactory.governance to appfactory.core
	 * to remove the
	 * dependancy from core to governance.
	 * appfactory.core should be refactored to store only necessary classes
	 */

    private static Log log = LogFactory.getLog(RxtManager.class);
    private static RxtManager rxtManager = new RxtManager();

    /**
     * Returns the instance of RxtManager
     * @return instance of RxtManager
     */
    public static RxtManager getInstance(){
        return rxtManager;
    }

    private RxtManager(){}

    /**
     * This method will add the given newValue as the value of the key,
     * replacing the existing value
     *
     * @param applicationId the Id of the current application
     * @param version       version of the current application
     * @param key           the attribute key that is been updated
     * @param newValue      the new value of the attribute key
     * @throws AppFactoryException
     */
    public void updateAppVersionRxt(String applicationId, String version, String []key,
                                    String[] newValue, String tenantDomain) throws AppFactoryException {

        // creating a unique key for an application in a tenant
        String tenant_appLock = tenantDomain.concat("_").concat(applicationId);

        synchronized (tenant_appLock.intern()) {
            GenericArtifactImpl artifact = getAppVersionArtifact(applicationId, version, tenantDomain);
            if (log.isDebugEnabled()) {
                log.debug("Updating application version rxt with keys : " + Arrays.toString(key) + " values : "
                        + Arrays.toString(newValue) + " applicationId : " + applicationId + " version : " + version
                        + " in tenant domain : " + tenantDomain);
            }

            try {
                for (int i = 0; i < key.length; i++) {
                    String currentVal = artifact.getAttribute(key[i]);
                    if (currentVal == null) {
                        artifact.addAttribute(key[i], newValue[i]);
                    } else {
                        artifact.setAttribute(key[i], newValue[i]);
                    }
                }

                UserRegistry userRegistry = getUserRegistry(tenantDomain);
                GovernanceUtils.loadGovernanceArtifacts(userRegistry);
                GenericArtifactManager artifactManager =
                        new GenericArtifactManager(userRegistry,
                                "appversion");
                artifactManager.updateGenericArtifact(artifact);

            } catch (RegistryException e) {
                String errorMsg = "Error while updating the artifact " + applicationId;
                log.error(errorMsg, e);
                throw new AppFactoryException(errorMsg, e);
           } catch (Exception e) {
                String errorMsg =
                        String.format("Unable to get tenant id for %s", tenantDomain);
                log.error(errorMsg, e);
                throw new AppFactoryException(errorMsg, e);
            }
        }
    }
    
    
    /**
     * This method will add the given newValue as the value of the key,
     * replacing the existing value
     *
     * @param applicationId the Id of the current application
     * @param version       version of the current application
     * @param key           the attribute key that is been updated
     * @param newValue      the new value of the attribute key
     * @throws AppFactoryException
     */
    public void updateAppVersionForUserRxt(String applicationId, String version, String userName, String key[],
                                    String newValue[], String tenantDomain) throws AppFactoryException {

        // creating a unique key for an application in a tenant
        String tenant_appLock = tenantDomain.concat("_").concat(applicationId).concat(userName);

        synchronized (tenant_appLock.intern()){
            GenericArtifactImpl artifact = getAppVersionForUserArtifact(applicationId, version, userName, tenantDomain);
            if (log.isDebugEnabled()) {
                log.debug("Updating application version for user rxt with keys : " + Arrays.toString(key) + " values : "
                        + Arrays.toString(newValue) + "applicationId : " + applicationId + " version : " + version
                        + " in tenant domain : " + tenantDomain);
            }

            try {
            	for (int i = 0; i < key.length; i++) {
                    String currentVal = artifact.getAttribute(key[i]);
                    if (currentVal == null) {
                        artifact.addAttribute(key[i], newValue[i]);
                    } else {
                        artifact.setAttribute(key[i], newValue[i]);
                    }
                }
                
                UserRegistry userRegistry = getUserRegistry(tenantDomain);
                GovernanceUtils.loadGovernanceArtifacts(userRegistry);
                GenericArtifactManager artifactManager =
                        new GenericArtifactManager(userRegistry,
                                "repouser");
                artifactManager.updateGenericArtifact(artifact);
            } catch (RegistryException e) {
                String errorMsg = "Error while updating the artifact " + applicationId;
                log.error(errorMsg, e);
                throw new AppFactoryException(errorMsg, e);
            } catch (Exception e) {
                String errorMsg =
                        String.format("Unable to get tenant id for %s", tenantDomain);
                log.error(errorMsg, e);
                throw new AppFactoryException(errorMsg, e);
            }
        }

    }
    
    
    

    /**
     * This method will add the given newValue as the value of the key,
     * replacing the existing value
     *
     * @param applicationId the Id of the current application
     * @param version       version of the current application
     * @param key           the attribute key that is been updated
     * @param newValue      the new value of the attribute key
     * @throws AppFactoryException
     */
    public void updateAppVersionRxt(String applicationId, String version, String key,
                                    String newValue, String tenantDomain) throws AppFactoryException {

        // creating a unique key for an application in a tenant
        String tenant_appLock = tenantDomain.concat("_").concat(applicationId);

        synchronized (tenant_appLock.intern()){
            GenericArtifactImpl artifact = getAppVersionArtifact(applicationId, version, tenantDomain);
            if (log.isDebugEnabled()) {
                log.debug("Updating application version rxt with key : " + key + " value : " + newValue
                        + " applicationId : " + applicationId + " version : " + version
                        + " in tenant domain : " + tenantDomain);
            }

            try {
                artifact.setAttribute(key, newValue);
                UserRegistry userRegistry = getUserRegistry(tenantDomain);
                GovernanceUtils.loadGovernanceArtifacts(userRegistry);
                GenericArtifactManager artifactManager =
                        new GenericArtifactManager(userRegistry,
                                "appversion");
                artifactManager.updateGenericArtifact(artifact);
            } catch (RegistryException e) {
                String errorMsg = "Error while updating the artifact " + applicationId;
                log.error(errorMsg, e);
                throw new AppFactoryException(errorMsg, e);
            } 
        }

    }
    
    
    /**
     * This method will append the given newValues as values for the key given
     *
     * @param applicationId the ID of the current application
     * @param version       the version of the current application
     * @param key           the attribute key that is been updated
     * @param newValues     array of new values for the attribute key
     * @throws AppFactoryException
     */
    public void updateAppVersionRxt(String applicationId, String version, String key,
                                    String[] newValues,String tenantDomain) throws AppFactoryException {

        // creating a unique key for an application in a tenant
        String tenant_appLock = tenantDomain.concat("_").concat(applicationId);

        synchronized (tenant_appLock.intern()) {
            if (log.isDebugEnabled()) {
                log.debug("Updating application version rxt with key : " + key + " value : " + Arrays.toString(newValues)
                        + " applicationId : " + applicationId + " version : " + version
                        + " in tenant domain : " + tenantDomain);
            }
            GenericArtifactImpl artifact = getAppVersionArtifact(applicationId, version, tenantDomain);

                try {
                    for (String value : newValues) {
                        artifact.addAttribute(key, value);
                    }

                    UserRegistry userRegistry = getUserRegistry(tenantDomain);
                    GovernanceUtils.loadGovernanceArtifacts(userRegistry);
                    GenericArtifactManager artifactManager =
                            new GenericArtifactManager(userRegistry,
                                    "appversion");
                    artifactManager.updateGenericArtifact(artifact);
                } catch (RegistryException e) {
                    String errorMsg = "Error while updating the artifact " + applicationId;
                    log.error(errorMsg, e);
                    throw new AppFactoryException(errorMsg, e);
                } 
        }

    }



    /**
     * This method returns the stage of a given application version
     *
     * @param applicationId the ID of the current application
     * @param appVersion    the version of the current application
     * @return the stage of the given application version
     * @throws AppFactoryException
     */
    public String getStage(String applicationId, String appVersion,String tenantDomain) throws AppFactoryException {
        String stage;
        GenericArtifactImpl artifact = getAppVersionArtifact(applicationId, appVersion, tenantDomain);
        try {
            stage = artifact.getLifecycleState();
        } catch (GovernanceException e) {
            String errorMsg = "Error while getting  the lifecycle state of artifact " + applicationId;
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }
        return stage;
    }

	

    /**
     * @param applicationId the ID of the current application
     * @param version       the version of the current application
     * @return generic artifact implementation of the artifact that matches the
     *         given applicationId, stage and version
     * @throws AppFactoryException
     */
    private  GenericArtifactImpl getAppVersionArtifact(String applicationId,
                                                      String version,String tenantDomain) throws AppFactoryException {
        GenericArtifactImpl artifact;
        try {

        	UserRegistry userRegistry = getUserRegistry(tenantDomain);
            
            Resource resource =
                    userRegistry.get(AppFactoryConstants.REGISTRY_APPLICATION_PATH +
                            RegistryConstants.PATH_SEPARATOR + applicationId +
                            RegistryConstants.PATH_SEPARATOR + version);
            GovernanceUtils.loadGovernanceArtifacts(userRegistry);
            GenericArtifactManager artifactManager =
                    new GenericArtifactManager(userRegistry,
                            "appversion");
            artifact = (GenericArtifactImpl) artifactManager.getGenericArtifact(resource.getUUID());

        } catch (RegistryException e) {
            String errorMsg =
                    String.format("Unable to load the application information for applicaiton id: %s",
                            applicationId);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }

        if (artifact == null) {
            String errorMsg = "Failed to get generic artifact implementation of application version artifact with " +
                    "tenant domain : " + tenantDomain + " applicationId : " + applicationId + " version : " + version;
            log.error(errorMsg);
            throw new AppFactoryException(errorMsg);
        }
        return artifact;
    }




    /**
     * @param applicationId the ID of the current application
     * @param version       the version of the current application
     * @return generic artifact implementation of the artifact that matches the
     *         given applicationId, stage and version
     * @throws AppFactoryException
     */
    private  GenericArtifactImpl getAppVersionForUserArtifact(String applicationId,
                                                      String version,String userName,String tenantDomain) throws AppFactoryException {
        GenericArtifactImpl artifact;
        try {

        	UserRegistry userRegistry = getUserRegistry(tenantDomain);
            
			Resource resource = userRegistry
					.get(AppFactoryConstants.REGISTRY_APPLICATION_PATH
							+ RegistryConstants.PATH_SEPARATOR + applicationId
							+ RegistryConstants.PATH_SEPARATOR + userName
							+ RegistryConstants.PATH_SEPARATOR + version);
            GovernanceUtils.loadGovernanceArtifacts(userRegistry);
            GenericArtifactManager artifactManager =
                    new GenericArtifactManager(userRegistry,
                            "repouser");
            artifact = (GenericArtifactImpl) artifactManager.getGenericArtifact(resource.getUUID());

        } catch (RegistryException e) {
            String errorMsg =
                    String.format("Unable to load the application information for applicaiton id: %s",
                            applicationId);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }
        if (artifact == null) {
            String errorMsg = "Failed to get generic artifact implementation of application version for user artifact with " +
                    "tenant domain : " + tenantDomain + " applicationId : " + applicationId + " version : " +
                    version + " username : " + userName;
            log.error(errorMsg);
            throw new AppFactoryException(errorMsg);
        }
        return artifact;
    }
    
    
    
	/**
	 * 
	 * To get Artifact detail given application id, version
	 * 
	 * @param applicationId
	 * @param version
	 * @param tenantDomain
	 * @return
	 * @throws AppFactoryException
	 */
	public Artifact getAppVersionDetailArtifact(String applicationId, String version, String tenantDomain)
	                                                                                                      throws AppFactoryException {
		GenericArtifact genericArtifact = getAppVersionArtifact(applicationId, version, tenantDomain);
		Artifact artifact = null;
		try {
			artifact = getArtifactByGenericArtifact(genericArtifact);
		} catch (GovernanceException e) {
			log.debug(e.getMessage());
		}

		return artifact;

	}

    /**
     * @param applicationId the ID of the current application
     * @param version       the version of the current application
     * @param key           the key is one of element what we need to get the value
     * @return keyValue the keyValue is the returned value for the given key
     * @throws AppFactoryException
     */
    public String getAppVersionRxtValue(String applicationId, String version,
                                        String key,String tenantDomain) throws AppFactoryException {
        GenericArtifactImpl artifact;
        String keyValue;
        try {
            artifact = getAppVersionArtifact(applicationId, version,tenantDomain);
            keyValue = artifact.getAttribute(key);

        } catch (RegistryException e) {
            String errorMsg =
                    String.format("Unable to load the application information for applicaiton id: %s",
                            applicationId);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }

        return keyValue;
    }


    /**
     * Retrieves App Version RXTs from tenant's registry
     *
     * @param domainName    tenant domain of the application
     * @param applicationId {@code applicationId}
     * @return list of  Artifact
     * @throws AppFactoryException
     * @throws RegistryException
     */
    public List<Artifact> getAppVersionRxtForApplication(String domainName, final String applicationId)
            throws AppFactoryException,
            RegistryException {
        UserRegistry userRegistry = getUserRegistry(domainName);
        return getAppVersionRXTFromRegistry(userRegistry, applicationId);
    }

    /**
     *
     * @param domainName
     * @param applicationId
     * @param userName
     * @return
     * @throws AppFactoryException
     * @throws RegistryException
     */
    public List<Artifact> getRepoUserRxtForApplicationOfUser(String domainName, String applicationId, String userName)
            throws AppFactoryException,
            RegistryException  {

       
        try {
        	UserRegistry userRegistry = getUserRegistry(domainName);
            return getrepoUserRXTFromRegistry(userRegistry, applicationId,
                    userName.split("@")[0],domainName);
        } catch (RegistryException e) {
            String errorMsg = String.format("Unable to get tenant id for %s", domainName);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }

        
    }

    /**
     * private method to get repo user rxt from registry
     *
     * @param userRegistry
     * @param applicationId
     * @param userName
     * @param domainName
     * @return
     * @throws AppFactoryException
     * @throws RegistryException
     */
    private List<Artifact> getrepoUserRXTFromRegistry(UserRegistry userRegistry, final String applicationId, final String userName, String domainName) throws  AppFactoryException,
            RegistryException  {
        GovernanceUtils.loadGovernanceArtifacts(userRegistry);
        GenericArtifactManager artifactManager =
                new GenericArtifactManager(userRegistry,
                        "repouser");

        GenericArtifactFilter artifactFilter = new GenericArtifactFilter() {
            @Override
            public boolean matches(GenericArtifact artifact) throws GovernanceException {
                if(artifact != null && artifact.getAttribute("repouser_name") != null && artifact.getAttribute("repouser_key") != null) {
                    return artifact.getAttribute("repouser_name").equals(userName) && artifact.getAttribute("repouser_key").equals(applicationId);
                } else {
                    return false;
                }
            }
        };

        GenericArtifact[] allArtifacts = artifactManager.findGenericArtifacts(artifactFilter);
        final List<Artifact> artifactList = new ArrayList<Artifact>();

        if(allArtifacts != null){
            for (GenericArtifact genericArtifact : allArtifacts) {
                artifactList.add(getRepoUserArtifactByGenericArtifact(genericArtifact,domainName));
            }

        }

        return artifactList;
    }

    /**
     * Private method to get App Version from given User registry
     */
    private List<Artifact> getAppVersionRXTFromRegistry(UserRegistry userRegistry,final String applicationId) throws AppFactoryException,
            RegistryException {
        GovernanceUtils.loadGovernanceArtifacts(userRegistry);
        GenericArtifactManager artifactManager =
                new GenericArtifactManager(userRegistry,
                        "appversion");


//        Used the proper governance API method for searching. Commented out the old code segment.
        GenericArtifactFilter artifactFilter = new GenericArtifactFilter() {
            @Override
            public boolean matches(GenericArtifact artifact) throws GovernanceException {
                if(artifact != null && artifact.getAttribute("appversion_key") != null) {
                    return artifact.getAttribute("appversion_key").equals(applicationId);
                } else {
                    return false;
                }
            }
        };

        GenericArtifact[] allArtifacts = artifactManager.findGenericArtifacts(artifactFilter);
        final List<Artifact> artifactList = new ArrayList<Artifact>();

        if(allArtifacts != null){
            for (GenericArtifact genericArtifact : allArtifacts) {
                artifactList.add(getArtifactByGenericArtifact(genericArtifact));
            }

        }

        return artifactList;
    }


    private boolean isCollection(Resource res) {
        return (res instanceof Collection);
    }

    private Artifact getArtifactByGenericArtifact(GenericArtifact paramGenericArtifact)
            throws GovernanceException, AppFactoryException {
        JDBCApplicationDAO applicationDAO=JDBCApplicationDAO.getInstance();

        String applicationKey = paramGenericArtifact.getAttribute("appversion_key");
        String version = paramGenericArtifact.getAttribute("appversion_version");

        String autoBuildStr = paramGenericArtifact.getAttribute("appversion_isAutoBuild");
        String autoDeployStr = paramGenericArtifact.getAttribute("appversion_isAutoDeploy");
        String productionMappedDomain = paramGenericArtifact.getAttribute("appversion_prodmappedsubdomain");
        String stage = paramGenericArtifact.getLifecycleState();

        boolean isAutoDeploy = (autoDeployStr == null) ? false : Boolean.valueOf(autoDeployStr);
        boolean isAutoBuild = autoBuildStr == null ? false : Boolean.valueOf(autoBuildStr);

        BuildStatus buildStatus;
        DeployStatus deployStatus;
        try {
            buildStatus=applicationDAO.getBuildStatus(applicationKey,version,false,null);
            deployStatus=applicationDAO.getDeployStatus(applicationKey,version,stage,
                    false,null);
        } catch (AppFactoryException e) {
            String errorMsg = "Error while retrieving build and deploy status for "+applicationKey;
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }
        String promoteStatus ;
        String currentBuildStatus ;

        String lastBuildStatus = null;
        if(buildStatus.getLastBuildId()!=null && buildStatus.getLastBuildStatus()!=null){
            lastBuildStatus = "build "+ buildStatus.getLastBuildId()+" "
                    +buildStatus.getLastBuildStatus();
        }
        String lastDeployedId = deployStatus.getLastDeployedId();
        currentBuildStatus=buildStatus.getCurrentBuildId();
        promoteStatus=applicationDAO.getApplicationVersion(applicationKey,version).getPromoteStatus();
        Artifact artifact =  new Artifact(applicationKey, lastBuildStatus, version, isAutoBuild, isAutoDeploy,
                lastDeployedId, stage,currentBuildStatus,promoteStatus);
        artifact.setProductionMappedDomain(productionMappedDomain);
        return artifact;
    }

    /**
     * Utility method to create an artifact from a generic repouser artifact
     *
     * @param paramGenericArtifact
     * @param domainName
     * @return artifact created from repouser rxt
     * @throws GovernanceException
     */
    private Artifact getRepoUserArtifactByGenericArtifact(GenericArtifact paramGenericArtifact, String domainName)
            throws GovernanceException, AppFactoryException {
        JDBCApplicationDAO applicationDAO=JDBCApplicationDAO.getInstance();
        String applicationKey = paramGenericArtifact.getAttribute("repouser_key");
        String userId = paramGenericArtifact.getAttribute("repouser_name");
        String repoURL = paramGenericArtifact.getAttribute("repouser_repoURL");
        String version = paramGenericArtifact.getAttribute("repouser_version");

        String autoBuildStr = paramGenericArtifact.getAttribute("repouser_isAutoBuild");
        String autoDeployStr = paramGenericArtifact.getAttribute("repouser_isAutoDeploy");
        

        boolean isAutoDeploy = (autoDeployStr == null) ? false : Boolean.valueOf(autoDeployStr);
        boolean isAutoBuild = autoBuildStr == null ? false : Boolean.valueOf(autoBuildStr);
        BuildStatus buildStatus;
        DeployStatus deployStatus;
        try {
            buildStatus=applicationDAO.getBuildStatus(applicationKey,version,true,userId);
            deployStatus=applicationDAO.getDeployStatus(applicationKey,version,
                    getStage(applicationKey,version,domainName),
                    true,userId);
        } catch (AppFactoryException e) {
            String errorMsg = "Error while retrieving build and deploy status for "+applicationKey;
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }
        String currentBuildStatus ;

        String lastBuildStatus = null;
        if(buildStatus.getLastBuildId()!=null && buildStatus.getLastBuildStatus()!=null){
            lastBuildStatus = "build "+ buildStatus.getLastBuildId()+" "
                    +buildStatus.getLastBuildStatus();
        }
        String lastDeployedId = deployStatus.getLastDeployedId();
        currentBuildStatus=buildStatus.getCurrentBuildId();

        return new Artifact(applicationKey, userId, version, isAutoBuild, isAutoDeploy,repoURL,lastDeployedId,lastBuildStatus,currentBuildStatus, null);
    }

    /**
     * Generic method to add new registry artifact.
     *
     * @param rxt
     * @param qname
     * @param newValueMap - values to be saved
     * @throws AppFactoryException
     */
    public void addNewArtifact(String rxt, String qname, Map<String, String> newValueMap)
            throws AppFactoryException {
        try {
        	UserRegistry userRegistry = getUserRegistry();
        	
            GovernanceUtils.loadGovernanceArtifacts(userRegistry);
            GenericArtifactManager manager = new GenericArtifactManager(userRegistry, rxt);
            GenericArtifact artifact =
                    manager.newGovernanceArtifact(new QName(qname));

            Set<Entry<String, String>> newValueEntrySet = newValueMap.entrySet();

            for (Entry<String, String> newValues : newValueEntrySet) {
                artifact.addAttribute(newValues.getKey(), newValues.getValue());
            }
            manager.addGenericArtifact(artifact);

        } catch (RegistryException e) {
            String errorMsg = String.format("Unable to add new artifact to the rxt %s", rxt);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }
    }

    public String addArtifact(String key, String info, String lifecycleAttribute) throws AppFactoryException {
        RegistryUtils.recordStatistics(key, info, lifecycleAttribute);
        try {
        	UserRegistry userRegistry = getUserRegistry();
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_COALESCING, true);
        XMLStreamReader reader = null;

            reader = factory.createXMLStreamReader(new StringReader(info));


        GenericArtifactManager manager = new GenericArtifactManager(userRegistry, key);
        GenericArtifact artifact = manager.newGovernanceArtifact(
                new StAXOMBuilder(reader).getDocumentElement());

        // want to save original content, so set content here
        artifact.setContent(info.getBytes());

        manager.addGenericArtifact(artifact);
        if (lifecycleAttribute != null) {
            String lifecycle = artifact.getAttribute(lifecycleAttribute);
            if (lifecycle != null) {
                artifact.attachLifecycle(lifecycle);
            }
        }
        return "/_system/governance"+ artifact.getPath();
        } catch (Exception e) {
            String errorMsg = "Error adding artifact";
            throw new AppFactoryException(errorMsg, e);
        }
    }

    /**
     * Generic method to retrieve artifact.
     *
     * @param resourcePath
     * @param rxt
     * @return {@link GenericArtifact}
     * @throws AppFactoryException
     */
    public GenericArtifact getArtifact(String resourcePath, String rxt) throws AppFactoryException {

        try {
        	UserRegistry userRegistry = getUserRegistry();
            if (userRegistry.resourceExists(resourcePath)) {
                Resource resource = userRegistry.get(resourcePath);
                GovernanceUtils.loadGovernanceArtifacts(userRegistry);

                GenericArtifactManager artifactManager =
                        new GenericArtifactManager(userRegistry,
                                rxt);

                return artifactManager.getGenericArtifact(resource.getUUID());
            }
            return null;

        } catch (RegistryException e) {
            String errorMsg =
                    String.format("Unable to load the artidact information for recource path: %s for rxt: %s",
                            resourcePath, rxt);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }
    }
/**
     * Generic method to retrieve artifact.
     *
     * @param resourcePath
     * @param rxtName
     * @return {@link GenericArtifact}
     * @throws AppFactoryException
     */
    public void deleteArtifact(String resourcePath, String rxtName) throws AppFactoryException {

        try {
        	UserRegistry userRegistry = getUserRegistry();
            if (userRegistry.resourceExists(resourcePath)) {
                Resource resource = userRegistry.get(resourcePath);
                GovernanceUtils.loadGovernanceArtifacts(userRegistry);

                GenericArtifactManager artifactManager =
                        new GenericArtifactManager(userRegistry,
                                rxtName);
                artifactManager.removeGenericArtifact(resource.getUUID());
            }

        } catch (RegistryException e) {
            String errorMsg =
                    String.format("Unable to delete the resource " + rxtName + " from path : " + resourcePath);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }
    }

    /**
     * Returns saved values of the given {@code artifact} as a Map.
     *
     * @param artifact
     * @return saved values as a {@link Map}
     * @throws AppFactoryException
     */
    public Map<String, String> readArtifact(GenericArtifact artifact) throws AppFactoryException {
        try {
            String[] attributeKeys = artifact.getAttributeKeys();
            Map<String, String> readValues = new HashMap<String, String>();
            for (String key : attributeKeys) {
                readValues.put(key, artifact.getAttribute(key));
            }
            return readValues;
        } catch (RegistryException e) {
            String errorMsg = "Erro reading Artifact infromation";
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }
    }

    /**
     * Generic method to update the given {@code updatableArtiafact} with new values provided by {@code newValueMap}
     *
     * @param updatableArtiafact
     * @param rxt
     * @param qname
     * @param newValueMap
     * @throws AppFactoryException
     */
    public void updateExistingArtifact(GenericArtifact updatableArtiafact, String rxt, String qname,
                                       Map<String, String> newValueMap) throws AppFactoryException {
        try {
        	UserRegistry userRegistry = getUserRegistry();
        	GovernanceUtils.loadGovernanceArtifacts(userRegistry);
            GenericArtifactManager artifactManager = new GenericArtifactManager(userRegistry, rxt);

            //FIXME: here update is done by first removing exising one and adding new one.
            // make it a real update.
            artifactManager.removeGenericArtifact(updatableArtiafact.getId());
            addNewArtifact(rxt, qname, newValueMap);
        } catch (RegistryException e) {
            String errorMsg =
                    String.format("Error While updating existing resgistry artifact: %s",
                            rxt);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }

    }

    /**
     * Returns all the saved artifacts related to ETA.
     *
     * @param applicationKey
     * @param stage
     * @param version
     * @return
     * @throws AppFactoryException
     */
    public List<GenericArtifact> getETAArtifacts(String applicationKey, String stage,
                                                 String version)
            throws AppFactoryException {
        try {
        	UserRegistry userRegistry = getUserRegistry();
        	GovernanceUtils.loadGovernanceArtifacts(userRegistry);

            String resourcePath = AppFactoryConstants.REGISTRY_APPLICATION_PATH +
                    RegistryConstants.PATH_SEPARATOR + applicationKey +
                    RegistryConstants.PATH_SEPARATOR + "eta" +
                    RegistryConstants.PATH_SEPARATOR + stage +
                    RegistryConstants.PATH_SEPARATOR + version;

            List<GenericArtifact> etaArtifacts = new ArrayList<GenericArtifact>();
            if (!userRegistry.resourceExists(resourcePath)) {
                log.debug("No ETA information to load for applikation key -" + applicationKey + " stage -" + stage + " version -" + version);
                return etaArtifacts;
            }

            Resource resource = userRegistry.get(resourcePath);

            GenericArtifactManager artifactManager =
                    new GenericArtifactManager(userRegistry, "eta");

            if (resource instanceof Collection) {
                String[] userChildren = ((Collection) resource).getChildren();

                for (String userChild : userChildren) {
                    if (!userRegistry.resourceExists(userChild)) {
                        continue;
                    }

                    Resource user = userRegistry.get(userChild);

                    if (user instanceof Collection) {
                        String etaChild = ((Collection) user).getChildren()[0];

                        Resource eta = userRegistry.get(etaChild);
                        if (!isCollection(eta)) {
                            etaArtifacts.add(artifactManager.getGenericArtifact(eta.getUUID()));
                        }
                    }

                }
            }
            return etaArtifacts;
        } catch (RegistryException e) {
            String errorMsg =
                    String.format("Error While retrieving eta information for : %s version : %s",
                            applicationKey, version);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }
    }

    public List<Artifact> getAppVersionRxtsInStage(final String applicationId, final String stage)
            throws RegistryException, AppFactoryException {
        List<Artifact> artifactList;
        List<Artifact> artifactsInStageList = new ArrayList<Artifact>();
        try {
            //artifactList = getAppVersionRxtForApplication(applicationId);     // check the tenant flow
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            artifactList = getAppVersionRxtForApplication(tenantDomain, applicationId);     // check the tenant flow
        } catch (AppFactoryException e) {
            String errorMsg = "Error While getting versions of application " + applicationId;
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }
        for (Artifact artifact : artifactList) {
            if (stage.equals(artifact.getStage())) {
                artifactsInStageList.add(artifact);
            }

        }
        return artifactsInStageList;
    }
    
    private UserRegistry getUserRegistry(String tenantDomain) throws RegistryException {
    	ServiceHolder.getTenantRegistryLoader().loadTenantRegistry(CarbonContext.getThreadLocalCarbonContext().getTenantId());
        return (UserRegistry)CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.SYSTEM_GOVERNANCE);
    }
    
    private UserRegistry getUserRegistry() throws RegistryException {
    	ServiceHolder.getTenantRegistryLoader().loadTenantRegistry(CarbonContext.getThreadLocalCarbonContext().getTenantId());
        return (UserRegistry)CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.SYSTEM_GOVERNANCE);
    }

}
