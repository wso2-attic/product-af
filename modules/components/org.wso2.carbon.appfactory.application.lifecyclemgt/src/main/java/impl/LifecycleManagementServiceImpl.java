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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

@WebService
public class LifecycleManagementServiceImpl implements LifecycleManagementService {
    HashMap<String, Lifecycle> lifecycleMap = null;
    Log log = LogFactory.getLog(LifecycleManagementServiceImpl.class);

    /**
     * Method to retrieve life cycles and their details from the registry
     *
     * @return lifecycle object array
     */
    public Lifecycle[] getAllLifecycle() throws AppFactoryException {
        if(lifecycleMap == null) {
            createLifecycleMap();
        }
        LifecycleDAO lifecycleDAO = new LifecycleDAO();
        String[] lifecycleNameList = lifecycleDAO.getLifeCycleList();
        Lifecycle[] lifecycleList = new Lifecycle[lifecycleNameList.length];
        for(int count = 0; count<lifecycleMap.size();count++){
            Lifecycle lifecycle = lifecycleMap.get(lifecycleNameList[count]);
            if (lifecycle != null) {
                lifecycleList[count] = lifecycle;
            }
        }
        return lifecycleList;
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
     * @param lifecycleName name of the life cycle
     * @param currentStage  current stage of the application
     * @return next stage name
     */
    public String getNextStage(String lifecycleName, String currentStage)
            throws LifecycleManagementException, AppFactoryException {
        String nextStage;
        createLifecycleMap();
        Lifecycle lifecycle = lifecycleMap.get(lifecycleName);
        if (lifecycle == null) {
            String msg = "Unable to load life cycle details of life cycle :" + lifecycleName;
            log.error(msg);
            throw new LifecycleManagementException(msg);
        } else {
            List<Stage> stages = lifecycle.getStages();
            int sLength = stages.size();
            int count = 0;
            while (count < sLength) {
                if (stages.get(count).getStageName().equals(currentStage)) {
                    count++;
                    break;
                }
                count++;
            }
            if (count < sLength) {
                nextStage = stages.get(count).getStageName();
            }else{
                String msg = "Unable to load stage details of the next stage of :"+currentStage+
                        " life cycle :" +lifecycleName+".There is no next stage for "+currentStage;
                log.error(msg);
                throw new LifecycleManagementException(msg);
            }
        }

        return nextStage;
    }

    /**
     * Method to get previous stage name
     *
     * @param lifecycleName name of the life cycle
     * @param currentStage  current stage of the application
     * @return previous stage name
     */
    public String getPreStage(String lifecycleName, String currentStage)
            throws LifecycleManagementException, AppFactoryException {
        String preStage;
        createLifecycleMap();
        Lifecycle lifecycle = lifecycleMap.get(lifecycleName);
        if (lifecycle == null) {
            String msg = "Unable to load life cycle details of life cycle :" +lifecycleName;
            log.error(msg);
            throw new LifecycleManagementException(msg);
        }
        else{
            List<Stage> stages = lifecycle.getStages();
            int sLength = stages.size();
            int count = 0;
            while (count < sLength) {
                if (stages.get(count).getStageName().equals(currentStage)) {
                    count--;
                    break;
                }
                count++;
            }
            if (count >= 0) {
                preStage = stages.get(count).getStageName();
            }else{
                String msg = "Unable to load stage details of the previous stage of :"+currentStage+
                        " life cycle :" +lifecycleName+".There is no previous stage for "+currentStage;
                log.error(msg);
                throw new LifecycleManagementException(msg);
            }
        }
        return preStage;
    }

    /**
     * Method to attach life cycle to an application
     *
     * @param appKey        application key
     * @param appVersion    application version
     * @param tenantDomain tenant domain
     * @return true/false
     */
    public boolean setAppVersionLifecycle(String appKey,String appVersion,String tenantDomain)
            throws LifecycleManagementException, AppFactoryException {
        LifecycleDAO dao = new LifecycleDAO();
        PrivilegedCarbonContext carbonContext;
        try {
            //-----------------------------------------------------------------------------------
            PrivilegedCarbonContext.startTenantFlow();
            carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain(tenantDomain, true);
            //-----------------------------------------------------------------------------------
            GenericArtifact artifact = dao.getAppArtifact(appKey,appVersion);
            if (artifact == null) {
                String msg = "Unable to load application details of application :" +appKey+
                        "application version"+appVersion;
                log.error(msg);
                throw new LifecycleManagementException(msg);
            }else{
                String appInfoLifecycle =
                        dao.getAppArtifact(appKey, AppFactoryConstants.APPLICATION_ARTIFACT_NAME).getLifecycleName();
                if((artifact.getLifecycleName().equals(appInfoLifecycle)) || appInfoLifecycle == null) {
                    String msg = "Unable to update life cycle of the application :" +appKey;
                    log.error(msg);
                    throw new LifecycleManagementException(msg);
                }else {
                    dao.updateAppInfo(appKey, tenantDomain, appVersion);
                }
            }
        }
        catch (AppFactoryException e) {
            String msg = "Error while attaching the lifecycle to application "+appKey+"of the application " +
                    "version :"+appVersion;
            log.error(msg);
            throw new AppFactoryException(msg);
        } catch (GovernanceException e) {
            String msg ="Error while loading the lifecycle name of the application "+appKey;
            log.error(msg);
            throw new AppFactoryException(msg);
        }finally{
            PrivilegedCarbonContext.endTenantFlow();
        }
        return true;
    }

    /**
     * Method to get stages (with checklist items)of a life cycle
     * @param lifecycleName life cycle name
     * @return array of stage objects
     */
    private List<Stage> getAllStages(String lifecycleName) throws AppFactoryException {
        List<Stage> stages = new ArrayList<Stage>();
        String SCXML_ELEMENT = "scxml";
        String STATE_ELEMENT = "state";
        String DATA_MODEL_ELEMENT = "datamodel";
        String ATTRIBUTE_ID = "id";

        LifecycleDAO dao = new LifecycleDAO();

        String lifecycleXml = dao.getLifeCycleConfiguration(lifecycleName);
        OMElement configurationElement;
        try {
            configurationElement = AXIOMUtil.stringToOM(lifecycleXml);
        }
        catch (XMLStreamException e) {
            String msg = "Unable to load registry files related to " + lifecycleName;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);

        }
        OMElement typeElement = configurationElement.getFirstElement();
        configurationElement = typeElement.getFirstElement();
        Iterator scxmlElement = configurationElement.getChildrenWithName(new QName(SCXML_ELEMENT));

        if (scxmlElement.hasNext()) {
            while (scxmlElement.hasNext()) {

                OMElement lifecycleElement = (OMElement) scxmlElement.next();
                Iterator stateElements = lifecycleElement.getChildrenWithName(new QName(STATE_ELEMENT));

                while (stateElements.hasNext()) {

                    OMElement nextStage = (OMElement) stateElements.next();
                    String stageName = nextStage.getAttributeValue(new QName(ATTRIBUTE_ID));

                    Stage stage = new Stage();
                    stage.setStageName(stageName);
                    stages.add(stage);
                    Iterator dataModelElement = nextStage.getChildrenWithName(new QName(DATA_MODEL_ELEMENT));

                    List<CheckListItem> checkListItems = getCheckListItems(dataModelElement);
                    stage.setItmes(checkListItems);

                }
            }
        }

        return stages;
    }

    /**
     * Method to retrieve details of a life cycle
     *
     * @param lifecycleName the name of lifecycle, that should be associated with the application
     * @return life cycle
     */
    private Lifecycle getLifeCycleByName(String lifecycleName) throws AppFactoryException {
        if (lifecycleMap == null) {
            createLifecycleMap();
        }
        return lifecycleMap.get(lifecycleName);
    }


    /**
     * Method to retrieve details of a lifecycle
     *
     * @param appKey application key
     * @return lifecycle object
     */
    public Lifecycle getCurrentLifeCycle(String appKey,String appVersion)
            throws AppFactoryException, LifecycleManagementException {
        Lifecycle lifecycle;
        LifecycleDAO dao = new LifecycleDAO();
        String lifecycleName = dao.getLifeCycleName(appKey,appVersion);
        if(lifecycleName == null){
            String errorMsg = "Failed to load life cycle of application :"+appKey+", application version :"+appVersion;
            log.error(errorMsg);
            throw new AppFactoryException(errorMsg);
        }else {
            lifecycle = getLifeCycleByName(lifecycleName);
        }
        return lifecycle;
    }

    /**
     * Method to retrieve details of a lifecycle
     *
     * @param appKey application key
     * @param lifecycleName life cycle name
     * @param tenantDomain tenant domain
     * @return true/false
     */
    public boolean setAppLifecycle(String appKey,String lifecycleName,String tenantDomain)
            throws LifecycleManagementException {
        LifecycleDAO dao = new LifecycleDAO();
        boolean status = false;
        try {
            GenericArtifact artifact = dao.getAppArtifact(appKey, AppFactoryConstants.APPLICATION_ARTIFACT_NAME);
            if (artifact != null && dao.isAppLifecycleChangeValid(appKey)) {
                if (artifact.getLifecycleName() != null && artifact.getLifecycleName().equals(lifecycleName)){
                    String msg = "Unable to update life cycle of the application :" +appKey+".It has been " +
                            "updated already with the life cycle :"+lifecycleName;
                    log.error(msg);
                    throw new LifecycleManagementException(msg);
                }else{
                    dao.setAppInfoLifecycleName(appKey, lifecycleName, tenantDomain);
                    status = true;
                }
            }
        }
        catch (AppFactoryException e) {
            log.error("Error while attaching the lifecycle name "+lifecycleName+" to application "+appKey+".");
        }
        catch (GovernanceException e) {
            log.error("Error while loading the lifecycle name "+lifecycleName+" of application "+appKey+".");
        }
        return status;
    }

    /**
     * Method to get checklist items of a given stage
     *
     * @param dataModelElement xml element with check list items
     * @return array of stage objects
     */
    private List<CheckListItem> getCheckListItems(Iterator dataModelElement){
        String DATA_ELEMENT = "data";
        String ITEM_ELEMENT = "item";
        String ATTRIBUTE_NAME = "name";

        List<CheckListItem> checkListItems = new ArrayList<CheckListItem>();

        OMElement nextDataModel = (OMElement) dataModelElement.next();

        Iterator dataElement = nextDataModel.getChildrenWithName(new QName(DATA_ELEMENT));
        while (dataElement.hasNext()) {

            OMElement nextData = (OMElement) dataElement.next();
            Iterator checkElements = nextData.getChildrenWithName(new QName(ITEM_ELEMENT));

            while (checkElements.hasNext()) {
                OMElement nextItem = (OMElement) checkElements.next();
                String itemName = nextItem.getAttributeValue(new QName(ATTRIBUTE_NAME));

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
    public boolean isAppLCChanged(String appKey){
        boolean status = false;
        LifecycleDAO lifecycleDAO = new LifecycleDAO();
        PrivilegedCarbonContext carbonContext;
        try {
            //-----------------------------------------------------------------------------------
            PrivilegedCarbonContext.startTenantFlow();
            carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain("xyz.com", true);
            //-----------------------------------------------------------------------------------
            if(lifecycleDAO.getAppArtifact(appKey,AppFactoryConstants.APPLICATION_ARTIFACT_NAME).getLifecycleName()!=null){
                status = true;
            }
        } catch (AppFactoryException e) {
            log.error("Error while loading life cycle artifact details");
        } catch (GovernanceException e) {
            log.error("Error while checking life cycle of the application :"+appKey);
        }finally{
            PrivilegedCarbonContext.endTenantFlow();
        }
        return status;
    }

}



