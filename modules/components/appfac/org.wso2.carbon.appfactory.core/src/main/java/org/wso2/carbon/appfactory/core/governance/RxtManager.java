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
import org.wso2.carbon.appfactory.core.deploy.Artifact;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.context.CarbonContext;
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
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.UserStoreException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.*;
import java.util.Map.Entry;

public class RxtManager {
    /*
     * todo: this class was moved from appfactory.governance to appfactory.core
	 * to remove the
	 * dependancy from core to governance.
	 * appfactory.core should be refactored to store only necessary classes
	 */

    private static Log log = LogFactory.getLog(RxtManager.class);
    
    
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

        synchronized (tenant_appLock) {
            GenericArtifactImpl artifact = getAppVersionArtifact(applicationId, version, tenantDomain);
            log.info("=============== updating rxt =============== key:" + key + " value:" + newValue);
            if (artifact == null) {
                String errorMsg =
                        String.format("Unable to find appversion information for id : %s",
                                applicationId);
                log.error(errorMsg);
                throw new AppFactoryException(errorMsg);
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

                RegistryService registryService = ServiceHolder.getRegistryService();
                UserRegistry userRegistry = registryService.getGovernanceSystemRegistry(
                        ServiceHolder.getRealmService().getTenantManager().getTenantId(tenantDomain));
                GovernanceUtils.loadGovernanceArtifacts(userRegistry);
                GenericArtifactManager artifactManager =
                        new GenericArtifactManager(userRegistry,
                                "appversion");
                artifactManager.updateGenericArtifact(artifact);

            } catch (RegistryException e) {
                String errorMsg = "Error while updating the artifact " + applicationId;
                log.error(errorMsg, e);
                throw new AppFactoryException(errorMsg, e);
            } catch (UserStoreException e) {
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

        synchronized (tenant_appLock){
            GenericArtifactImpl artifact = getAppVersionArtifact(applicationId, version, tenantDomain);
            log.info("=============== updating rxt =============== key:" + key + " value:" + newValue);
            if (artifact == null) {
                String errorMsg =
                        String.format("Unable to find appversion information for id : %s",
                                applicationId);
                log.error(errorMsg);
                throw new AppFactoryException(errorMsg);
            }
            try {
                String currentVal = artifact.getAttribute(key);
                if (currentVal == null) {
                    artifact.addAttribute(key, newValue);
                } else {
                    artifact.setAttribute(key, newValue);
                }
                RegistryService registryService = ServiceHolder.getRegistryService();
                UserRegistry userRegistry = registryService.getGovernanceSystemRegistry(
                        ServiceHolder.getRealmService().getTenantManager().getTenantId(tenantDomain));
                GovernanceUtils.loadGovernanceArtifacts(userRegistry);
                GenericArtifactManager artifactManager =
                        new GenericArtifactManager(userRegistry,
                                "appversion");
                artifactManager.updateGenericArtifact(artifact);
            } catch (RegistryException e) {
                String errorMsg = "Error while updating the artifact " + applicationId;
                log.error(errorMsg, e);
                throw new AppFactoryException(errorMsg, e);
            } catch (UserStoreException e) {
                String errorMsg =
                        String.format("Unable to get tenant id for %s", tenantDomain);
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

        synchronized (tenant_appLock) {
                GenericArtifactImpl artifact = getAppVersionArtifact(applicationId, version, tenantDomain);
                log.info("=============== updating rxt =============== key:" + key + " with " +
                        newValues.length + " values");
                if (artifact == null) {
                    String errorMsg =
                            String.format("Unable to find appversion information for id : %s",
                                    applicationId);
                    log.error(errorMsg);
                    throw new AppFactoryException(errorMsg);
                }
                try {
                    for (String value : newValues) {
                        artifact.addAttribute(key, value);
                    }

                    RegistryService registryService = ServiceHolder.getRegistryService();
                    UserRegistry userRegistry = registryService.getGovernanceSystemRegistry(
                            ServiceHolder.getRealmService().getTenantManager().getTenantId(tenantDomain));
                    GovernanceUtils.loadGovernanceArtifacts(userRegistry);
                    GenericArtifactManager artifactManager =
                            new GenericArtifactManager(userRegistry,
                                    "appversion");
                    artifactManager.updateGenericArtifact(artifact);
                } catch (RegistryException e) {
                    String errorMsg = "Error while updating the artifact " + applicationId;
                    log.error(errorMsg, e);
                    throw new AppFactoryException(errorMsg, e);
                } catch (UserStoreException e) {
                    String errorMsg =
                            String.format("Unable to get tenant id for %s", tenantDomain);
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
        //String[] versionPaths = getVersionPaths(applicationId);
        String stage;
        /*// path to a version is in the structure .../<appid>/<lifecycle>/1.0.1 )
        if (versionPaths != null) {
			for (String path : versionPaths) {
				String[] s = path.trim().split(RegistryConstants.PATH_SEPARATOR);
				if (appVersion.equals(s[s.length - 1])) {
					// get the <lifecycle>
					return s[s.length - 2];
				}
			}
		}
		return null;*/
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

	/*private String[] getVersionPaths(String applicationId) throws AppFactoryException {
		List<String> versionPaths = new ArrayList<String>();
		try {
			RegistryService registryService = ServiceHolder.getRegistryService();
			UserRegistry userRegistry = registryService.getGovernanceSystemRegistry();
			// child nodes of this will contains folders for all life cycles (
			// e.g. QA, Dev, Prod)
			Resource application =
			                       userRegistry.get(AppFactoryConstants.REGISTRY_APPLICATION_PATH +
			                                        RegistryConstants.PATH_SEPARATOR +
			                                        applicationId);

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
						Collections.addAll(versionPaths,
						                   ((Collection) versionsInLCResource).getChildren());
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
	}*/

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

            RegistryService registryService = ServiceHolder.getRegistryService();
            UserRegistry userRegistry = registryService.getGovernanceSystemRegistry(
                    ServiceHolder.getRealmService().getTenantManager().getTenantId(tenantDomain));
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
        } catch (UserStoreException e) {
            String errorMsg =
                    String.format("Unable to get tenant id for %s", tenantDomain);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
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
     * Retrieves artifact information related to all the versions of the given
     * {@code applicationId}
     *
     * @param applicationId {@code applicationId}
     * @return list of {@link Artifact}
     * @throws AppFactoryException
     * @throws RegistryException
     */
//    TODO:should not be used. check and remove.
    public List<Artifact> getAppVersionRxtForApplication(final String applicationId)
            throws AppFactoryException,
            RegistryException {

        RegistryService registryService = ServiceHolder.getRegistryService();
        UserRegistry userRegistry = registryService.getGovernanceSystemRegistry();

        return getAppVersionRXTFromRegistry(userRegistry, applicationId);
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

        RegistryService registryService;

        registryService = ServiceHolder.getRegistryService();

        UserRegistry userRegistry;
        try {
            userRegistry = registryService.getGovernanceSystemRegistry(
                    ServiceHolder.getRealmService().getTenantManager().getTenantId(domainName));
        } catch (UserStoreException e) {
            String errorMsg = String.format("Unable to get tenant id for %s", domainName);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }

        return getAppVersionRXTFromRegistry(userRegistry, applicationId);
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

//        final List<Artifact> artifactList = new ArrayList<Artifact>();
//        List<String> versionUUID = new ArrayList<String>();
//        String applicationPath = AppFactoryConstants.REGISTRY_APPLICATION_PATH +
//                RegistryConstants.PATH_SEPARATOR + applicationId;
//        Resource appIDCollection =
//                userRegistry.get(applicationPath);
//        if (appIDCollection instanceof Collection) {
//            String[] appVersionsCollection = ((Collection) appIDCollection).getChildren();
//
//            if (appVersionsCollection != null) {
//                for (String appVersionResource : appVersionsCollection) {
//                    if (!userRegistry.resourceExists(appVersionResource)) {
//                        continue;
//                    }
//                    Resource child = userRegistry.get(appVersionResource);
//                    if (!isCollection(child) &&
//                            !child.getPath().equals(applicationPath + RegistryConstants.PATH_SEPARATOR + "appinfo")) {
//                        versionUUID.add(child.getUUID());
//                    }
//                }
//            }
//        }
//        for (String uuid : versionUUID) {
//            GenericArtifact paramGenericArtifact = artifactManager.getGenericArtifact(uuid);
//            artifactList.add(getArtifactByGenericArtifact(paramGenericArtifact));
//        }
        return artifactList;
    }


    private boolean isCollection(Resource res) {
        return (res instanceof Collection);
    }

    private Artifact getArtifactByGenericArtifact(GenericArtifact paramGenericArtifact)
            throws GovernanceException {

        String applicationKey = paramGenericArtifact.getAttribute("appversion_key");
        String lastBuildStatus = paramGenericArtifact.getAttribute("appversion_LastBuildStatus");
        String currentBuildStatus = paramGenericArtifact.getAttribute("appversion_CurrentBuildStatus");
        String version = paramGenericArtifact.getAttribute("appversion_version");

        String autoBuildStr = paramGenericArtifact.getAttribute("appversion_isAutoBuild");
        String autoDeployStr = paramGenericArtifact.getAttribute("appversion_isAutoDeploy");
        String lastDeployedId = paramGenericArtifact.getAttribute("appversion_lastdeployedid");
        String stage = paramGenericArtifact.getLifecycleState();

        boolean isAutoDeploy = (autoDeployStr == null) ? false : Boolean.valueOf(autoDeployStr);
        boolean isAutoBuild = autoBuildStr == null ? false : Boolean.valueOf(autoBuildStr);

        return new Artifact(applicationKey, lastBuildStatus, version, isAutoBuild, isAutoDeploy,
                lastDeployedId, stage,currentBuildStatus);
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
        RegistryService registryService = ServiceHolder.getRegistryService();
        try {
            UserRegistry registry = registryService.getGovernanceSystemRegistry(CarbonContext.getThreadLocalCarbonContext().getTenantId());

            GovernanceUtils.loadGovernanceArtifacts(registry);
            GenericArtifactManager manager = new GenericArtifactManager(registry, rxt);
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
            RegistryService registryService = ServiceHolder.getRegistryService();
            UserRegistry userRegistry = registryService.getGovernanceSystemRegistry(CarbonContext.getThreadLocalCarbonContext().getTenantId());
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

        RegistryService registryService = ServiceHolder.getRegistryService();
        try {
            UserRegistry userRegistry = registryService.getGovernanceSystemRegistry(CarbonContext.getThreadLocalCarbonContext().getTenantId());

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

        RegistryService registryService = ServiceHolder.getRegistryService();
        try {
            UserRegistry userRegistry = registryService.getGovernanceSystemRegistry(CarbonContext.getThreadLocalCarbonContext().getTenantId());

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
            RegistryService registryService = ServiceHolder.getRegistryService();
            UserRegistry userRegistry = registryService.getGovernanceSystemRegistry(CarbonContext.getThreadLocalCarbonContext().getTenantId());
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
        RegistryService registryService = ServiceHolder.getRegistryService();
        try {
            UserRegistry userRegistry = registryService.getGovernanceSystemRegistry(CarbonContext.getThreadLocalCarbonContext().getTenantId());
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
}
