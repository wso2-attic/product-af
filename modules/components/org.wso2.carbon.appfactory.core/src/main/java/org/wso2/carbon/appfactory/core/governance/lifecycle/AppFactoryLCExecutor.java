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

package org.wso2.carbon.appfactory.core.governance.lifecycle;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.bam.BamDataPublisher;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeManager;
import org.wso2.carbon.appfactory.core.util.AppFactoryCoreUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.registry.extensions.aspects.utils.LifecycleConstants;
import org.wso2.carbon.governance.registry.extensions.aspects.utils.StatCollection;
import org.wso2.carbon.governance.registry.extensions.executors.ServiceVersionExecutor;
import org.wso2.carbon.governance.registry.extensions.interfaces.Execution;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourcePath;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.jdbc.utils.Transaction;

import javax.xml.stream.XMLStreamException;
import java.util.HashMap;
import java.util.Map;

import static org.wso2.carbon.governance.registry.extensions.executors.utils.Utils.populateParameterMap;

//import org.wso2.carbon.registry.metadata.exception.MetadataException;
//import org.wso2.carbon.registry.metadata.models.endpoint.HTTPEndpointV1;
//import org.wso2.carbon.registry.metadata.models.service.HTTPServiceV1;
//import org.wso2.carbon.registry.metadata.models.version.ServiceVersionV1;


/**
 * Lifecycle executor to handle application lifecycles.
 * This executor will invoke when lifecycle state change from one state to another
 */
public class AppFactoryLCExecutor implements Execution {

    private static final Log log = LogFactory.getLog(ServiceVersionExecutor.class);

    @Override
    public void init(@SuppressWarnings("rawtypes") Map args) {
        // TODO Auto-generated method stub

    }

    public boolean execute(RequestContext requestContext, String currentState, String targetState) {

        boolean returnStatus = true;

        // Absolute path for the current application
        // (i.e. /_system/governance/repository/applications/$Application/$Version )
        String resourcePath = requestContext.getResource().getPath();

        // Variable to store new path
        String newPath;

        // Now we are going to get the list of parameters from the context and add it to a map
        Map<String, String> currentParameterMap = new HashMap<String, String>();

        // Here we are populating the parameter map that was given from the UI
        if (!populateParameterMap(requestContext, currentParameterMap)) {
            log.error("Failed to populate the parameter map");
            return false;
        }

        // Getting values from map
        final String version = currentParameterMap.get(AppFactoryConstants.APPLICATION_VERSION);
        final String comment = currentParameterMap.get("comment");
        final String adStatus = currentParameterMap.get("autodeployment");
        final String user = currentParameterMap.get("user");
        final String action = currentParameterMap.get("action");

        // new path will holds "/$Application/$Stage/$Version/appinfo"
        newPath = resourcePath.substring((AppFactoryConstants.REGISTRY_GOVERNANCE_PATH +
                AppFactoryConstants.REGISTRY_APPLICATION_PATH).length());

        // 0th element is "", 1st element is app name , 2nd element is $Version
        String newPathArray[] = newPath.split(RegistryConstants.PATH_SEPARATOR);

        String currentAppVersion = newPathArray[2];
        String currentAppID = newPathArray[1];

//        Removing app version cache related code.
        //clear the version cache
//        AppVersionCache.getAppVersionCache().clearCacheForAppId(currentAppID);
        // if the app is trunk then we need version.
        if ((AppFactoryConstants.TRUNK).equals(currentAppVersion)) {
            // Append version from here
            if (version != null) {
                try {
                    // Set resource to requestContext
                    Resource newResource = requestContext.getRegistry().get(resourcePath);
                    requestContext.setResource(newResource);
                    requestContext.setResourcePath(new ResourcePath(resourcePath));
                } catch (RegistryException e) {
                    log.error("Can not perform transition", e);
                }
            } else {
                log.error("Can not find application version. " +
                        "Application version is required to perform lifecycle operation");
                returnStatus = false;
            }
        } else {
            // Application is not a trunk version. So it can have version with it or user can define version
            Boolean isTransactionStarted = Transaction.isStarted();
            Boolean isTransactionSuccess = true;
            try {
                if (!isTransactionStarted) {
                    requestContext.getRegistry().beginTransaction();
                }
                // Set resource to requestContext
                requestContext.setResource(null);
                Resource newResource = requestContext.getRegistry().get(resourcePath);

                updateStats(requestContext, resourcePath, comment, adStatus, user, action, newResource);

                int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
                String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
                requestContext.setResource(newResource);
                requestContext.setResourcePath(new ResourcePath(resourcePath));
                String appVersion = (version != null) ? version : currentAppVersion;
                //editApplicationOnLifeCycleChange(applicationId, appVersion, currentState, targetState);

                updateBAMStats(currentAppID, currentAppID, System.currentTimeMillis(),
                        "" + tenantId, user, version, targetState);

                String appType = AppFactoryCoreUtil.getApplicationType(currentAppID, tenantDomain);
                // if target lifecycle stage is production(last stage) and apptype is supporting persisting endpoint as
                // as a HTTP service,
                // we persist application endpoint as a HTTP service.
                if (ApplicationTypeManager.getInstance().getApplicationTypeBean(appType).
		                isPersistApplicationEndPointMetaData() &&
                        AppFactoryUtil.getNextLifeCycleStage(targetState) == null) {
                    persistApplicationEndpointMetaData(requestContext.getRegistry(), appVersion,
                            currentAppID, targetState, user);
                }
            } catch (RegistryException e) {
                isTransactionSuccess = false;
                log.error("Can not perform transition", e);
                returnStatus = false;
            } catch (XMLStreamException e) {
                isTransactionSuccess = false;
                log.error("Can Read the registry resource", e);
                returnStatus = false;
            } catch (JaxenException e) {
                isTransactionSuccess = false;
                log.error("Can not edit the job", e);
            } catch (AppFactoryException e) {
                isTransactionSuccess = false;
                log.error("Can not perform the transition", e);
            } finally {
                if (!isTransactionStarted) {
                    if (isTransactionSuccess) {
                        try {
                            requestContext.getRegistry().commitTransaction();
                        } catch (RegistryException e) {
                            log.error("Could not Commit to the registry", e);
                            returnStatus = false;
                        }
                    } else {
                        try {
                            requestContext.getRegistry().rollbackTransaction();
                        } catch (RegistryException e) {
                            log.error("Could not Rollback", e);
                            returnStatus = false;
                        }
                    }
                }
            }
        }
        return returnStatus;
    }

    private void persistApplicationEndpointMetaData(Registry registry, String version, String applicationId,
                                                    String stage, String user) throws AppFactoryException {
      /*  String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String applicationURL = AppFactoryCoreUtil.getApplicationUrl(applicationId, version, stage, tenantDomain);
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        //We get the user name from the CC since the parameter user has the tenant aware user name
        String userName = CarbonContext.getThreadLocalCarbonContext().getUsername();

        // We get a non ch-rooted registry to this method(since we use requestContext.getRegistry()).
        // We need to pass a governance registry to the metadata API. hence getting a governance registry here.
        // We pass the user name and tenant id since we need a tenant user governance registry
        Registry governanceRegistry;
        try {
            // We use the system user to add the artifacts
            governanceRegistry = GovernanceUtils.getGovernanceUserRegistry(registry, CarbonConstants
                    .REGISTRY_SYSTEM_USERNAME, tenantId);
        } catch (RegistryException e) {
            String errorMessage = "Unable to get a governance registry instance for publishing endpoint data";
            log.error(errorMessage,e);
            throw new AppFactoryException(errorMessage,e);
        }

        try {
            // We check whether there is a service with the given name
            Map<String,String> searchCriteria = new HashMap<String, String>();
            searchCriteria.put("name",applicationId);
            HTTPServiceV1 services[] = HTTPServiceV1.find(governanceRegistry,searchCriteria);

            if(services != null && services.length > 0){
                // This means an already existing service is there. So we update that service
                // We only expect one service hence taking the first
                ServiceVersionV1 serviceVersionV1 = services[0].newVersion(version);
                serviceVersionV1.setProperty("isSecured", "false");
                ServiceVersionV1.add(governanceRegistry, serviceVersionV1);

                // we have to add endpoint again because metadata service API expects it.
                addNewEndpointToServiceVersion(version, applicationId, applicationURL, governanceRegistry,
                                               serviceVersionV1);
            }else{
                // No existing service. So we have to add it.
                ServiceVersionV1 httpV1 = new ServiceVersionV1(governanceRegistry, version);
                httpV1.setProperty("isSecured", "false");

                // We are adding a new service
                HTTPServiceV1 http1 = new HTTPServiceV1(governanceRegistry, applicationId, httpV1);
                http1.setOwner(user);
                HTTPServiceV1.add(governanceRegistry, http1);

                // we have to add endpoint again because metadata service API expects it.
                addNewEndpointToServiceVersion(version, applicationId, applicationURL, governanceRegistry, httpV1);
            }
        } catch (MetadataException e) {
            String errorMsg = "Failed to add HTTPService for application:" + applicationId + " with endpoint:" +
                    applicationURL + " in tenant:" + tenantDomain;
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }*/
    }

//    private void addNewEndpointToServiceVersion(String version, String applicationId, String applicationURL,
//                                                Registry governanceRegistry, ServiceVersionV1 serviceVersionV1)
//            throws MetadataException {
//        HTTPEndpointV1 ep = new HTTPEndpointV1(governanceRegistry, applicationId + "-" + version);
//        ep.setUrl(applicationURL);
//        serviceVersionV1.addEndpoint(ep);
//    }

    /**
     * Publish stats to BAM
     * @param appName  Application Name
     * @param appKey Application Key
     * @param timestamp Timestamp
     * @param tenantId Tenant ID
     * @param userName User Name
     * @param appVersion Application Version
     * @param stage Stage
     */
    private void updateBAMStats(String appName, String appKey, double timestamp, String tenantId,
                             String userName , String appVersion, String stage) throws AppFactoryException {
        BamDataPublisher bamDataPublisher = BamDataPublisher.getInstance();
        bamDataPublisher.PublishAppVersionEvent(appName, appKey, System.currentTimeMillis(),
                "" + tenantId, userName, appVersion, stage);

    }

    private void updateStats(RequestContext requestContext, String resourcePath, String comment, String adStatus, String user, String action, Resource newResource) throws XMLStreamException, RegistryException, JaxenException {
        //updatStats(requestContext, resourcePath, comment, user, action, newResource);

        //Edit content when lifecycle change
        StAXOMBuilder builder = new StAXOMBuilder(newResource.getContentStream());
        OMElement configurations = builder.getDocumentElement();

        AXIOMXPath axiomxPath = new AXIOMXPath("//m:autodeployment");
        axiomxPath.addNamespace("m", "http://www.wso2.org/governance/metadata");
        Object selectedObject = axiomxPath.selectSingleNode(configurations);
        if (selectedObject != null) {
            OMElement selectedNode = (OMElement) selectedObject;
            selectedNode.setText(adStatus);
            String inputConfiguration = configurations.toString();
            newResource.setContent(inputConfiguration);
            requestContext.getRegistry().put(resourcePath, newResource);
        }

        //change to update history to Development stage only. All the history detail stored under Development stage.
        StatCollection statCollection = (StatCollection) requestContext.getProperty(LifecycleConstants.STAT_COLLECTION);

        OMElement dataElement = AXIOMUtil.stringToOM("<data></data>");

        //user element
        OMElement userElement = AXIOMUtil.stringToOM("<user></user>");
        userElement.setText(user);

        //action element
        OMElement actionElement = AXIOMUtil.stringToOM("<action></action>");
        actionElement.setText(action);

        //comment element
        OMElement commentElement = AXIOMUtil.stringToOM("<comment></comment>");
        if (comment != null) {
            commentElement.setText(comment);
        }

        //add every thing to data element
        dataElement.addChild(userElement);
        dataElement.addChild(actionElement);
        dataElement.addChild(commentElement);

        //this will write the data tag to the action.executors.executor.operations.data
        statCollection.addExecutors(this.getClass().getName(), dataElement);

        newResource.setProperty(LifecycleConstants.REGISTRY_LIFECYCLE_HISTORY_ORIGINAL_PATH,
                statCollection.getOriginalPath());
    }

}

