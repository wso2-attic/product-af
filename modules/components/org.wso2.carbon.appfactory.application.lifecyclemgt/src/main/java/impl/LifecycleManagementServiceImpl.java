/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */
package impl;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import service.LifecycleManagementService;
import util.CheckListItem;
import util.Lifecycle;
import util.Stage;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.*;


/**
 * Contains the implementation of the LifecycleManagementService
 */
@WebService
public class LifecycleManagementServiceImpl implements LifecycleManagementService {
    private static final String LC_DATA_ELEMENT = "data";
    private static final String LC_ITEM_ELEMENT = "item";
    private static final String LC_ATTRIBUTE_NAME = "name";
    HashMap<String, Lifecycle> lifecycleMap = null;
    Log log = LogFactory.getLog(LifecycleManagementServiceImpl.class);

    public LifecycleManagementServiceImpl() throws AppFactoryException {
        createLifecycleMap();
    }

    /**
     * Method to retrieve life cycles and their details from the registry
     *
     * @return lifecycle object array
     */
    public Collection<Lifecycle> getAllLifeCycles() throws AppFactoryException {
        return lifecycleMap.values();
    }


    /**
     * Method to retrieve life cycles and their details and map them
     */
    private void createLifecycleMap() throws AppFactoryException {
        lifecycleMap = new HashMap<String, Lifecycle>();
        LifecycleDAO lifecycleDAO = new LifecycleDAO();
        String[] lifecycleNameList = lifecycleDAO.getLifeCycleList();
        for (String LifecycleName : lifecycleNameList) {
            Lifecycle lifecycle = new Lifecycle();
            lifecycle.setLifecycleName(LifecycleName);
            lifecycle.setStages(getAllStages(LifecycleName));
            lifecycleMap.put(LifecycleName, lifecycle);
        }

    }

    /**
     * Method to get next stage name
     *
     * @param lifecycleName name of the lifecycle
     * @param currentStage  current stage of the application
     * @return next stage name
     */
    public String getNextStage(String lifecycleName, String currentStage)
            throws LifecycleManagementException, AppFactoryException {
        String nextStage;
        Lifecycle lifecycle = lifecycleMap.get(lifecycleName);
        if (lifecycle == null) {
            String msg = "Unable to load lifecycle details of life cycle :" + lifecycleName;
            log.error(msg);
            throw new LifecycleManagementException(msg);
        } else {
            ArrayList<Stage> stagesList = (ArrayList<Stage>) lifecycle.getStages();
            ListIterator<Stage> stages = stagesList.listIterator();
            while (stages.hasNext()) {
                if (stages.next().getStageName().equals(currentStage)) {
                    break;
                }
            }
            nextStage = stages.next().getStageName();

        }

        return nextStage;
    }

    /**
     * Method to get previous stage name
     *
     * @param lifecycleName name of the lifecycle
     * @param currentStage  current stage of the application
     * @return previous stage name
     */
    public String getPreviousStage(String lifecycleName, String currentStage)
            throws LifecycleManagementException, AppFactoryException {
        String preStage;
        Lifecycle lifecycle = lifecycleMap.get(lifecycleName);
        if (lifecycle == null) {
            String msg = "Unable to load lifecycle details of life cycle :" + lifecycleName;
            log.error(msg);
            throw new LifecycleManagementException(msg);
        } else {
            ArrayList<Stage> stagesList = (ArrayList<Stage>) lifecycle.getStages();
            ListIterator<Stage> stages = stagesList.listIterator();
            while (stages.hasNext()) {
                if (stages.next().getStageName().equals(currentStage)) {
                    break;
                }
            }
            preStage = stages.previous().getStageName();
        }
        return preStage;
    }

    /**
     * Method to attach lifecycle to an application
     *
     * @param appKey       application key
     * @param appVersion   application version
     * @param tenantDomain tenant domain
     * @return true/false
     */
    public boolean setAppVersionLifecycle(String appKey, String appVersion, String tenantDomain)
            throws LifecycleManagementException, AppFactoryException {
        LifecycleDAO dao = new LifecycleDAO();
        dao.updateAppVersionLifeCycle(appKey, tenantDomain, appVersion);
        return true;
    }

    /**
     * Method to get stages (with checklist items)of a lifecycle
     *
     * @param lifecycleName life cycle name
     * @return array of stage objects
     */
    private List<Stage> getAllStages(String lifecycleName) throws AppFactoryException {
        List<Stage> stages = new ArrayList<Stage>();

        LifecycleDAO dao = new LifecycleDAO();

        String lifecycleXml = dao.getLifeCycleConfiguration(lifecycleName);
        OMElement configurationElement;
        try {
            configurationElement = AXIOMUtil.stringToOM(lifecycleXml);
        } catch (XMLStreamException e) {
            String msg = "Unable to load the lifecycle configuration from registry for lifecycle :" + lifecycleName;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);

        }
        OMElement typeElement = configurationElement.getFirstElement();
        configurationElement = typeElement.getFirstElement();
        Iterator scxmlElement = configurationElement.getChildrenWithName(new QName(AppFactoryConstants.LC_SCXML_ELEMENT));

        if (scxmlElement.hasNext()) {
            while (scxmlElement.hasNext()) {

                OMElement lifecycleElement = (OMElement) scxmlElement.next();
                Iterator stateElements = lifecycleElement.getChildrenWithName(new QName(AppFactoryConstants.LC_STATE_ELEMENT));

                while (stateElements.hasNext()) {

                    OMElement nextStage = (OMElement) stateElements.next();
                    String stageName = nextStage.getAttributeValue(new QName(AppFactoryConstants.LC_ATTRIBUTE_ID));

                    Stage stage = new Stage();
                    stage.setStageName(stageName);
                    stages.add(stage);
                    Iterator dataModelElement = nextStage.getChildrenWithName(new QName(AppFactoryConstants.LC_DATA_MODEL_ELEMENT));

                    List<CheckListItem> checkListItems = getCheckListItems(dataModelElement);
                    stage.setItmes(checkListItems);

                }
            }
        }

        return stages;
    }

    /**
     * Method to retrieve details of a lifecycle
     *
     * @param lifecycleName the name of lifecycle, that should be associated with the application
     * @return life cycle
     */
    private Lifecycle getLifeCycleByName(String lifecycleName) throws AppFactoryException {
        return lifecycleMap.get(lifecycleName);
    }


    /**
     * Method to retrieve lifecycle details of a given appVersion artifact
     *
     * @param appKey application key
     * @return lifecycle object
     */
    public Lifecycle getCurrentLifeCycle(String appKey, String appVersion, String tenantDomain)
            throws AppFactoryException, LifecycleManagementException {
        Lifecycle lifecycle;
        LifecycleDAO dao = new LifecycleDAO();
        String lifecycleName = dao.getLifeCycleName(appKey, appVersion, tenantDomain);
        if (lifecycleName == null) {
            String errorMsg = "Failed to load lifecycle of application :" + appKey + ", application version :"
                    + appVersion + "of the tenant :" + tenantDomain;
            log.error(errorMsg);
            throw new AppFactoryException(errorMsg);
        } else {
            lifecycle = getLifeCycleByName(lifecycleName);
        }
        return lifecycle;
    }

    /**
     * Method to set lifecycle name of an appInfo artifact
     *
     * @param appKey        application key
     * @param lifecycleName life cycle name
     * @param tenantDomain  tenant domain
     * @return true/false
     */
    public boolean setAppLifecycle(String appKey, String lifecycleName, String tenantDomain)
            throws LifecycleManagementException, AppFactoryException {
        LifecycleDAO dao = new LifecycleDAO();
        boolean status = false;
        try {
            GenericArtifact artifact =
                    dao.getAppArtifact(appKey, AppFactoryConstants.APPLICATION_ARTIFACT_NAME, tenantDomain);
            if (artifact != null && dao.isAppLifecycleChangeValid(appKey, tenantDomain)) {
                if (artifact.getLifecycleName() != null && artifact.getLifecycleName().equals(lifecycleName)) {
                    String msg = "Unable to update lifecycle of the application :" + appKey + "of the tenant :" + tenantDomain + ".It has been " +
                            "updated already with the lifecycle :" + lifecycleName;
                    log.error(msg);
                    throw new LifecycleManagementException(msg);
                } else {
                    dao.setAppInfoLifecycleName(appKey, lifecycleName, tenantDomain);
                    status = true;
                }
            }
        } catch (AppFactoryException e) {
            String errorMsg = "Error while attaching the lifecycle name " + lifecycleName + " of application " + appKey + "of the tenant :" + tenantDomain;
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        } catch (GovernanceException e) {
            String errorMsg = "Error while loading the lifecycle name " + lifecycleName + " of application " + appKey + "of the tenant :" + tenantDomain;
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }
        return status;
    }

    /**
     * Method to get checklist items of a given stage
     *
     * @param dataModelElement xml element with check list items
     * @return array of stage objects
     */
    private List<CheckListItem> getCheckListItems(Iterator dataModelElement) {

        List<CheckListItem> checkListItems = new ArrayList<CheckListItem>();

        OMElement nextDataModel = (OMElement) dataModelElement.next();

        Iterator dataElement = nextDataModel.getChildrenWithName(new QName(LC_DATA_ELEMENT));
        while (dataElement.hasNext()) {

            OMElement nextData = (OMElement) dataElement.next();
            Iterator checkElements = nextData.getChildrenWithName(new QName(LC_ITEM_ELEMENT));

            while (checkElements.hasNext()) {
                OMElement nextItem = (OMElement) checkElements.next();
                String itemName = nextItem.getAttributeValue(new QName(LC_ATTRIBUTE_NAME));

                CheckListItem checkListItem = new CheckListItem();
                checkListItem.setCheckItemName(itemName);
                checkListItems.add(checkListItem);
            }

        }
        return checkListItems;
    }

    /**
     * Method to check whether application life cycle is changed by the user or not
     *
     * @param appKey application key
     * @return true/false
     */
    public boolean isAppLCChanged(String appKey, String tenantDomain) throws AppFactoryException {
        boolean status = false;
        LifecycleDAO lifecycleDAO = new LifecycleDAO();
        PrivilegedCarbonContext carbonContext;
        try {
            PrivilegedCarbonContext.startTenantFlow();
            carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain(tenantDomain, true);

            if (lifecycleDAO.getAppArtifact(appKey, AppFactoryConstants.APPLICATION_ARTIFACT_NAME, tenantDomain).
                    getLifecycleName() != null) {
                status = true;
            }
        } catch (AppFactoryException e) {
            String errorMsg = "Error while loading lifecycle artifact details of the application :" + appKey + "of the tenant :" + tenantDomain;
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        } catch (GovernanceException e) {
            String errorMsg = "Error while checking lifecycle of the application :" + appKey + "of the tenant :" + tenantDomain;
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return status;
    }

}



