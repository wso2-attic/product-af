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
import org.wso2.carbon.appfactory.core.dao.JDBCAppVersionDAO;
import org.wso2.carbon.appfactory.core.dao.JDBCApplicationDAO;
import org.wso2.carbon.appfactory.core.dto.Version;
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

    private RxtManager() {
    }

    /**
     * Returns the instance of RxtManager
     *
     * @return instance of RxtManager
     */
    public static RxtManager getInstance() {
        return rxtManager;
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

        synchronized (tenant_appLock.intern()) {
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
                GenericArtifactManager artifactManager = new GenericArtifactManager(userRegistry, "repouser");
                artifactManager.updateGenericArtifact(artifact);
            } catch (RegistryException e) {
                String errorMsg = "Error while updating the artifact " + applicationId;
                log.error(errorMsg, e);
                throw new AppFactoryException(errorMsg, e);
            } catch (Exception e) {
                String errorMsg = String.format("Unable to get tenant id for %s", tenantDomain);
                log.error(errorMsg, e);
                throw new AppFactoryException(errorMsg, e);
            }
        }

    }

    /**
     * @param applicationId the ID of the current application
     * @param version       the version of the current application
     * @return generic artifact implementation of the artifact that matches the
     * given applicationId, stage and version
     * @throws AppFactoryException
     */
    private GenericArtifactImpl getAppVersionForUserArtifact(String applicationId, String version, String userName,
                                                             String tenantDomain) throws AppFactoryException {
        GenericArtifactImpl artifact;
        try {
            UserRegistry userRegistry = getUserRegistry(tenantDomain);
            Resource resource = userRegistry.get(AppFactoryConstants.REGISTRY_APPLICATION_PATH
                                             + RegistryConstants.PATH_SEPARATOR + applicationId
                                             + RegistryConstants.PATH_SEPARATOR + userName
                                             + RegistryConstants.PATH_SEPARATOR + version);
            GovernanceUtils.loadGovernanceArtifacts(userRegistry);
            GenericArtifactManager artifactManager = new GenericArtifactManager(userRegistry, "repouser");
            artifact = (GenericArtifactImpl) artifactManager.getGenericArtifact(resource.getUUID());
        } catch (RegistryException e) {
            String errorMsg = String.format("Unable to load the application information for applicaiton id: %s",
                                  applicationId);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }
        if (artifact == null) {
            String errorMsg = "Failed to get generic artifact implementation of application version for user artifact " +
                              "with tenant domain : " + tenantDomain + " applicationId : " + applicationId + " version : " +
                    version + " username : " + userName;
            log.error(errorMsg);
            throw new AppFactoryException(errorMsg);
        }
        return artifact;
    }

    private boolean isCollection(Resource res) {
        return (res instanceof Collection);
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
            GenericArtifact artifact = manager.newGovernanceArtifact(new QName(qname));
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
	    if (log.isDebugEnabled()) {
		    log.debug("calling add artifact with key : " + key + ", info : " + info + ", lifecycleAttribute : " +
		              lifecycleAttribute);
	    }
        RegistryUtils.recordStatistics(key, info, lifecycleAttribute);
        try {
            UserRegistry userRegistry = getUserRegistry();
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.IS_COALESCING, true);
            XMLStreamReader reader = null;
            reader = factory.createXMLStreamReader(new StringReader(info));
	        GovernanceUtils.loadGovernanceArtifacts(userRegistry);
            GenericArtifactManager manager = new GenericArtifactManager(userRegistry, key);
            GenericArtifact artifact = manager.newGovernanceArtifact(new StAXOMBuilder(reader).getDocumentElement());

            // want to save original content, so set content here
            artifact.setContent(info.getBytes());
            manager.addGenericArtifact(artifact);
            if (lifecycleAttribute != null) {
                String lifecycle = artifact.getAttribute(lifecycleAttribute);
                if (lifecycle != null) {
                    artifact.attachLifecycle(lifecycle);
                }
            }
            return "/_system/governance" + artifact.getPath();
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
                GenericArtifactManager artifactManager = new GenericArtifactManager(userRegistry, rxt);
                return artifactManager.getGenericArtifact(resource.getUUID());
            }
            return null;
        } catch (RegistryException e) {
            String errorMsg = String.format("Unable to load the artidact information for recource path: %s for rxt: %s",
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
                GenericArtifactManager artifactManager = new GenericArtifactManager(userRegistry, rxtName);
                artifactManager.removeGenericArtifact(resource.getUUID());
            }
        } catch (RegistryException e) {
            String errorMsg = String.format("Unable to delete the resource " + rxtName + " from path : " +
                                            resourcePath);
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
            String errorMsg = String.format("Error While updating existing resgistry artifact: %s", rxt);
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
                log.debug("No ETA information to load for applikation key -" + applicationKey + " stage -" + stage +
                          " version -" + version);
                return etaArtifacts;
            }
            Resource resource = userRegistry.get(resourcePath);
            GenericArtifactManager artifactManager = new GenericArtifactManager(userRegistry, "eta");
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
            String errorMsg = String.format("Error While retrieving eta information for : %s version : %s",
                                  applicationKey, version);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }
    }

    private UserRegistry getUserRegistry(String tenantDomain) throws RegistryException {
        ServiceHolder.getTenantRegistryLoader().loadTenantRegistry(
                CarbonContext.getThreadLocalCarbonContext().getTenantId());
        return (UserRegistry) CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.SYSTEM_GOVERNANCE);
    }

    private UserRegistry getUserRegistry() throws RegistryException {
        ServiceHolder.getTenantRegistryLoader().loadTenantRegistry(
                CarbonContext.getThreadLocalCarbonContext().getTenantId());
        return (UserRegistry) CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.SYSTEM_GOVERNANCE);
    }

}
