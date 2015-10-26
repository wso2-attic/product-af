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

package org.wso2.carbon.appfactory.lifecycle.mgt.service;

import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.lifecycle.mgt.bean.LifecycleInfoBean;

/**
 * Contains operations related to lifecycle mgt of applications
 */
public interface LifecycleManagementService {

    /**
     * Method to retrieve get lifecycle with details for a given app version of an application
     *
     * @param appKey       application key
     * @param appVersion   application version
     * @param tenantDomain tenant domain
     * @return life cycle object with stages and checklist items
     */
    public LifecycleInfoBean getCurrentAppVersionLifeCycle(String appKey, String appVersion, String tenantDomain)
            throws AppFactoryException;

    /**
     * Method to get next stage name for a given stage in a given lifecycle
     *
     * @param lifecycleName name of the lifecycle
     * @param currentStage  current stage of the application
     * @return next stage name
     */
    public String getNextStage(String lifecycleName, String currentStage) throws AppFactoryException;

    /**
     * Method to get previous stage name for a given stage in a given lifecycle
     *
     * @param lifecycleName name of the lifecycle
     * @param currentStage  current stage of the application
     * @return previous stage name
     */
    public String getPreviousStage(String lifecycleName, String currentStage) throws AppFactoryException;

    /**
     * Method to retrieve life cycles and their details from the registry
     *
     * @return collection of lifecycle objects
     */
    public LifecycleInfoBean[] getAllLifeCycles();

    /**
     * Method to set lifecycle name of an appInfo artifact of a given application
     *
     * @param appKey        application key
     * @param lifecycleName life cycle name
     * @param tenantDomain  tenant domain
     */
    public void setAppLifecycle(String appKey, String lifecycleName, String tenantDomain) throws AppFactoryException;

    /**
     * Method to attach lifecycle to an application of a given app version
     * (This is used to set the lifecycle name of a branch if the user has changed the lifecycle before)
     *
     * @param appKey       application key
     * @param appVersion   application version
     * @param tenantDomain tenant domain
     */
    public void updateAppVersionLifecycle(String appKey, String appVersion, String tenantDomain)
            throws AppFactoryException;

}
