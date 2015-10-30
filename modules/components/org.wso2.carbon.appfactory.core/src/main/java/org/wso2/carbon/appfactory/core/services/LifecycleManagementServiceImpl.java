package org.wso2.carbon.appfactory.core.services;

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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.bean.CheckListItemBean;
import org.wso2.carbon.appfactory.core.bean.LifecycleInfoBean;
import org.wso2.carbon.appfactory.core.bean.StageBean;
import org.wso2.carbon.appfactory.core.dao.LifecycleDAO;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.lcm.services.LifeCycleManagementService;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.*;

/**
 * Contains the implementation of the LifecycleManagementService
 */

public class LifecycleManagementServiceImpl{
    public static final String LC_STATE_ELEMENT = "state";
    public static final String LC_DATA_MODEL_ELEMENT = "datamodel";
    public static final String LC_ATTRIBUTE_ID = "id";
    private static final String LC_DATA_ELEMENT = "data";
    private static final String LC_ITEM_ELEMENT = "item";
    private static final String LC_ATTRIBUTE_NAME = "name";
    private static final String LC_SCXML_ELEMENT = "scxml";
    private static Map<String, LifecycleInfoBean> lifecycleMap;
    Log log = LogFactory.getLog(LifecycleManagementServiceImpl.class);
    private static LifecycleManagementServiceImpl lifecycleManagementService;

    private LifecycleManagementServiceImpl() throws AppFactoryException {
        init();
    }

    public static LifecycleManagementServiceImpl getInstance() throws AppFactoryException {
            if (lifecycleManagementService == null) {
                lifecycleManagementService = new LifecycleManagementServiceImpl();
            }
        return lifecycleManagementService;
    }
    /**
     * Method to retrieve life cycles and their details from the registry
     *
     * @return collection of lifecycle objects
     */
    public LifecycleInfoBean[] getAllLifeCycles() {
        return lifecycleMap.values().toArray(new LifecycleInfoBean[lifecycleMap.size()]);
    }

    /**
     * Method to add the life cycles and their details to a map
     */
    private void init() throws AppFactoryException {
        if (lifecycleMap == null) {
            lifecycleMap = new HashMap<String, LifecycleInfoBean>();
            LifeCycleManagementService lifeCycleManagementService = new LifeCycleManagementService();
            String[] lifecycleNameList;
            try {
                lifecycleNameList = lifeCycleManagementService.getLifecycleList();
            } catch (Exception e) {
                String errorMsg = "Error occurred while getting the list of lifecycle from LifeCycleManagementService";
                log.error(errorMsg, e);
                throw new AppFactoryException(errorMsg);
            }
            for (String LifecycleName : lifecycleNameList) {
                LifecycleInfoBean lifecycle = new LifecycleInfoBean();
                lifecycle.setLifecycleName(LifecycleName);
                lifecycle.setStages(getAllStages(LifecycleName));
                lifecycle.setBuildStageName(AppFactoryUtil.getBuildingStage(LifecycleName));
                if (log.isDebugEnabled()) {
                    log.debug("Setting building stage:" + lifecycle.getBuildStageName() + " of the lifecycle:"
                            + LifecycleName);
                }
                lifecycle.setDisplayName(AppFactoryUtil.getLifecycleDisplayName(LifecycleName));
                if (log.isDebugEnabled()) {
                    log.debug("Setting display name:" + lifecycle.getDisplayName() + " of the lifecycle:"
                            + LifecycleName);
                }
                lifecycleMap.put(LifecycleName, lifecycle);
            }
        }
    }

    /**
     * Method to get next stage name
     *
     * @param lifecycleName name of the lifecycle
     * @param currentStage  current stage of the application
     * @return next stage name
     */
    public String getNextStage(String lifecycleName, String currentStage) throws AppFactoryException {
        String nextStage = null;
        LifecycleInfoBean lifecycle = lifecycleMap.get(lifecycleName);
        if (lifecycle == null) {
            String msg = "Unable to load lifecycle details of life cycle :" + lifecycleName;
            log.error(msg);
            throw new AppFactoryException(msg);
        } else {
            Iterator<StageBean> stages = lifecycle.getStages().iterator();
            while (stages.hasNext()) {
                //if the currentStage equals to the stage name of the current stage object go to next stage object
                if (stages.next().getStageName().equals(currentStage)) {
                    if (stages.hasNext()) {
                        nextStage = stages.next().getStageName();
                    } else {
                        //if there is no any next stage to currentStage LifecycleManagementException is thrown
                        String msg =
                                "There is no stage after " + currentStage + " stage in the lifecycle :" + lifecycleName;
                        log.error(msg);
                        throw new AppFactoryException(msg);
                    }
                    break;
                }
            }
        }
        if (nextStage == null) {
            String msg = "There is no stage called " + currentStage + " in the lifecycle :" + lifecycleName;
            log.error(msg);
            throw new AppFactoryException(msg);
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
    public String getPreviousStage(String lifecycleName, String currentStage) throws AppFactoryException {
        String previousStage = null;
        LifecycleInfoBean lifecycle = lifecycleMap.get(lifecycleName);
        if (lifecycle == null) {
            String msg = "Unable to load lifecycle details of life cycle :" + lifecycleName;
            log.error(msg);
            throw new AppFactoryException(msg);
        } else {
            ListIterator<StageBean> stages = lifecycle.getStages().listIterator();
            while (stages.hasNext()) {
                //if the currentStage equals to the stage name of the current stage object go to previous stage object
                if (stages.next().getStageName().equals(currentStage)) {
                    stages.previous();
                    if (stages.hasPrevious()) {
                        previousStage = stages.previous().getStageName();
                    } else {
                        //if there is no any previous stage to currentStage LifecycleManagementException is thrown
                        String msg = "There is no stage before " + currentStage + " stage in the lifecycle :"
                                + lifecycleName;
                        log.error(msg);
                        throw new AppFactoryException(msg);
                    }
                    break;
                }
            }
        }
        if (previousStage == null) {
            String msg = "There is no stage called " + currentStage + " in the lifecycle :" + lifecycleName;
            log.error(msg);
            throw new AppFactoryException(msg);
        }
        return previousStage;
    }

    /**
     * Method to attach lifecycle to an application
     *
     * @param appKey       application key
     * @param tenantDomain tenant domain
     */
    public String getFirstStageByApplication(String appKey,String tenantDomain) throws AppFactoryException{
        return getCurrentLifecycle(appKey,tenantDomain).getStages().get(0).getStageName();
    }

    /**
     * Method to get stages (with checklist items)of a lifecycle
     *
     * @param lifecycleName life cycle name
     * @return array of stage objects
     */
    private List<StageBean> getAllStages(String lifecycleName) throws AppFactoryException {
        List<StageBean> stages = new ArrayList<StageBean>();
        //The lifecycle configuration file is retrieved from the registry. Therefore there's no need to
        // validate it again.
        String lifecycleXml = LifecycleDAO.getInstance().getLifeCycleConfiguration(lifecycleName);
        OMElement configurationElement;
        try {
            configurationElement = AXIOMUtil.stringToOM(lifecycleXml);

        } catch (XMLStreamException e) {
            String msg = "Unable to read the lifecycle configuration from registry for lifecycle :" + lifecycleName;
            log.error(msg, e);
            throw new AppFactoryException(msg);
        }

        OMElement typeElement = configurationElement.getFirstElement();
        configurationElement = typeElement.getFirstElement();
        Iterator scxmlElement = configurationElement.getChildrenWithName(new QName(LC_SCXML_ELEMENT));

        while (scxmlElement.hasNext()) {

            OMElement lifecycleElement = (OMElement) scxmlElement.next();
            Iterator stateElements = lifecycleElement.getChildrenWithName(new QName(LC_STATE_ELEMENT));

            while (stateElements.hasNext()) {

                OMElement nextStage = (OMElement) stateElements.next();
                String stageName = nextStage.getAttributeValue(new QName(LC_ATTRIBUTE_ID));

                StageBean stage = new StageBean();
                stage.setStageName(stageName);
                stages.add(stage);

                if (log.isDebugEnabled()) {
                    log.debug("Stage :" + stageName + " is successfully added to stage list of lifecycle :"
                            + lifecycleName);
                }

                Iterator dataModelElement = nextStage.getChildrenWithName(new QName(LC_DATA_MODEL_ELEMENT));

                List<CheckListItemBean> checkListItems = getCheckListItems(dataModelElement, stageName);
                stage.setCheckListItems(checkListItems);
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
    private LifecycleInfoBean getLifeCycleByName(String lifecycleName) {
        return lifecycleMap.get(lifecycleName);
    }

    /**
     * Method to retrieve lifecycle details of a given appVersion artifact
     *
     * @param appKey application key
     * @return lifecycle object
     */
    public LifecycleInfoBean getCurrentLifecycle(String appKey, String tenantDomain)
            throws AppFactoryException {
        LifecycleInfoBean lifecycle;
        String lifecycleName = LifecycleDAO.getInstance().getArtifactLifecycleName(appKey, tenantDomain);
        if (lifecycleName == null) {
            String errorMsg =
                    "Unable to load the lifecycle of the application :" + appKey + " of the tenant :" + tenantDomain;
            log.error(errorMsg);
            throw new AppFactoryException(errorMsg);
        } else {
            lifecycle = getLifeCycleByName(lifecycleName);
            if (lifecycle == null) {
                String errorMsg = "Unable to load the lifecycle details of the lifecycle :" + lifecycleName;
                log.error(errorMsg);
                throw new AppFactoryException(errorMsg);
            }
        }
        return lifecycle;
    }

    /**
     * Method to set lifecycle name of an appInfo artifact
     *
     * @param appKey        application key
     * @param lifecycleName life cycle name
     * @param tenantDomain  tenant domain
     */
    public void setAppLifecycle(String appKey, String lifecycleName, String tenantDomain) throws AppFactoryException {
        try {
            GenericArtifact appInfoArtifact = LifecycleDAO.getInstance().getAppInfoArtifact(appKey, tenantDomain);

            if (appInfoArtifact != null && LifecycleDAO.getInstance().isAppLifecycleChangeValid(appKey, lifecycleName,
                    tenantDomain)) {
                if (appInfoArtifact.getLifecycleName() != null && appInfoArtifact.getLifecycleName()
                        .equals(lifecycleName)) {
                    String msg = "Unable to update the lifecycle of the application :" + appKey + " of the tenant :"
                            + tenantDomain + ". It has already been updated with the lifecycle :" + lifecycleName;
                    log.error(msg);
                    throw new AppFactoryException(msg);
                } else {
                    LifecycleDAO.getInstance().setAppInfoLifecycleName(appKey, lifecycleName, tenantDomain);
                    // dao.updateAppVersionList(appKey, tenantDomain);
                    if (log.isDebugEnabled()) {
                        log.debug("Lifecycle :" + lifecycleName + " for the application :" + appKey + " of the tenant :"
                                + tenantDomain + " is successfully added.");
                    }
                }
            } else {
                String errorMsg =
                        "Lifecycle can not be changed in the application :" + appKey + " of the tenant :" + tenantDomain
                                + " into lifecycle :" + lifecycleName;
                log.error(errorMsg);
                throw new AppFactoryException(errorMsg);
            }

        } catch (GovernanceException e) {
            String errorMsg =
                    "Error while loading the lifecycle name " + lifecycleName + " to the application " + appKey
                            + "of the tenant :" + tenantDomain;
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg);
        }
    }

    /**
     * Method to get checklist items of a given stage
     *
     * @param dataModelElement xml element with check list items
     * @return array of stage objects
     */
    private List<CheckListItemBean> getCheckListItems(Iterator dataModelElement, String stageName) {

        List<CheckListItemBean> checkListItems = new ArrayList<CheckListItemBean>();

        OMElement nextDataModel = (OMElement) dataModelElement.next();

        Iterator dataElement = nextDataModel.getChildrenWithName(new QName(LC_DATA_ELEMENT));
        while (dataElement.hasNext()) {

            OMElement nextData = (OMElement) dataElement.next();
            Iterator checkElements = nextData.getChildrenWithName(new QName(LC_ITEM_ELEMENT));

            while (checkElements.hasNext()) {
                OMElement nextItem = (OMElement) checkElements.next();
                String itemName = nextItem.getAttributeValue(new QName(LC_ATTRIBUTE_NAME));

                CheckListItemBean checkListItem = new CheckListItemBean();
                checkListItem.setCheckItemName(itemName);
                checkListItems.add(checkListItem);

                if (log.isDebugEnabled()) {
                    log.debug("Checklist item :" + itemName
                            + "is successfully added to checklist item list of the stage :" + stageName);
                }
            }

        }
        return checkListItems;
    }

    public String getBuildStageName(String appKey, String tenantDomain) throws AppFactoryException {
        return getCurrentLifecycle(appKey,tenantDomain).getBuildStageName();
    }
}



