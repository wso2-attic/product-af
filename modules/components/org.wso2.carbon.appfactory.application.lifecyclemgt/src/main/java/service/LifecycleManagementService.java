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

package service;

import bean.LifecycleBean;
import impl.LifecycleManagementException;
import org.wso2.carbon.appfactory.common.AppFactoryException;

import javax.ws.rs.*;
import java.util.Collection;

/**
 * Contains operations related to lifecycle management of applications
 */
@Path("/lifecycleManagementService/")
public interface LifecycleManagementService {

    /**
     * Method to retrieve get lifecycle with details for a given app version of an application
     *
     * @param appKey       application key
     * @param appVersion   application version
     * @param tenantDomain tenant domain
     * @return life cycle object with stages and checklist items
     */
    @GET
    @Path("/getCurrentLifeCycle/{appKey}/{appVersion}/{tenantDomain}/")
    @Produces({ "application/json" })
    public LifecycleBean getCurrentLifeCycle(@PathParam("appKey") String appKey,
            @PathParam("appVersion") String appVersion,
            @PathParam("tenantDomain") String tenantDomain) throws AppFactoryException, LifecycleManagementException;

    /**
     * Method to get next stage name for a given stage in a given lifecycle
     *
     * @param lifecycleName name of the lifecycle
     * @param currentStage  current stage of the application
     * @return next stage name
     */
    @GET
    @Path("/getNextStage/{lifecycleName}/{currentStage}/")
    public String getNextStage(@PathParam("lifecycleName") String lifecycleName,
            @PathParam("currentStage") String currentStage) throws LifecycleManagementException, AppFactoryException;

    /**
     * Method to get previous stage name for a given stage in a given lifecycle
     *
     * @param lifecycleName name of the lifecycle
     * @param currentStage  current stage of the application
     * @return previous stage name
     */
    @GET
    @Path("/getPreviousStage/{lifecycleName}/{currentStage}/")
    public String getPreviousStage(@PathParam("lifecycleName") String lifecycleName,
            @PathParam("currentStage") String currentStage) throws LifecycleManagementException, AppFactoryException;

    /**
     * Method to retrieve life cycles and their details from the registry
     *
     * @return collection of lifecycle objects
     */
    @GET
    @Path("/getAllLifeCycles/")
    @Produces({ "application/json" })
    public Collection<LifecycleBean> getAllLifeCycles() throws LifecycleManagementException, AppFactoryException;

    /**
     * Method to set lifecycle name of an appInfo artifact of a given application
     *
     * @param appKey        application key
     * @param lifecycleName life cycle name
     * @param tenantDomain  tenant domain
     */
    @POST
    @Path("/setAppLifecycle/{appKey}/{lifecycleName}/{tenantDomain}/")
    public void setAppLifecycle(@PathParam("appKey") String appKey, @PathParam("lifecycleName") String lifecycleName,
            @PathParam("tenantDomain") String tenantDomain) throws LifecycleManagementException, AppFactoryException;

    /**
     * Method to attach lifecycle to an application of a given app version
     * (This is used to set the lifecycle name of a branch if the user has changed the lifecycle before)
     *
     * @param appKey       application key
     * @param appVersion   application version
     * @param tenantDomain tenant domain
     */
    @POST
    @Path("/setAppVersionLifecycle/{appKey}/{appVersion}/{tenantDomain}/")
    public void setAppVersionLifecycle(@PathParam("appKey") String appKey,
            @PathParam("appVersion") String appVersion, @PathParam("tenantDomain") String tenantDomain)
            throws LifecycleManagementException, AppFactoryException;

    /**
     * Method to check whether application life cycle is changed by the user or not.
     * (This is used to check whether the user has changed the lifecycle before creating a new branch for an app)
     *
     * @param appKey application key
     * @return true/false
     */
    @GET
    @Path("/isAppLCChanged/{appKey}/{tenantDomain}/")
    public boolean isAppLCChanged(@PathParam("appKey") String appKey, @PathParam("tenantDomain") String tenantDomain)
            throws AppFactoryException;

}
