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

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
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
    @Path("/getCurrentAppVersionLifeCycle/{appKey}/{appVersion}/{tenantDomain}/")
    @Produces({ "application/json" })
    @Consumes({ "application/json" , "application/xml" })
    public Response getCurrentAppVersionLifeCycle(@PathParam("appKey") String appKey,
            @PathParam("appVersion") String appVersion, @PathParam("tenantDomain") String tenantDomain);

    /**
     * Method to get next stage name for a given stage in a given lifecycle
     *
     * @param lifecycleName name of the lifecycle
     * @param currentStage  current stage of the application
     * @return next stage name
     */
    @GET
    @Path("/getNextStageName/{lifecycleName}/{currentStage}/")
    @Produces({ "application/json" })
    @Consumes({ "application/json" , "application/xml" })
    public Response getNextStageName(@PathParam("lifecycleName") String lifecycleName,
            @PathParam("currentStage") String currentStage);

    /**
     * Method to get previous stage name for a given stage in a given lifecycle
     *
     * @param lifecycleName name of the lifecycle
     * @param currentStage  current stage of the application
     * @return previous stage name
     */
    @GET
    @Path("/getPreviousStageName/{lifecycleName}/{currentStage}/")
    @Produces({ "application/json" })
    @Consumes({ "application/json" , "application/xml" })
    public Response getPreviousStageName(@PathParam("lifecycleName") String lifecycleName,
            @PathParam("currentStage") String currentStage);

    /**
     * Method to retrieve life cycles and their details from the registry
     *
     * @return collection of lifecycle objects
     */
    @GET
    @Path("/getAllLifeCycles/")
    @Produces({ "application/json" })
    public Collection<LifecycleBean> getAllLifeCycles();

    /**
     * Method to set lifecycle name of an appInfo artifact of a given application
     *
     * @param appKey        application key
     * @param lifecycleName life cycle name
     * @param tenantDomain  tenant domain
     */
    @POST
    @Path("/setAppLifecycle/{appKey}/{lifecycleName}/{tenantDomain}/")
    @Produces({ "application/json" })
    @Consumes({ "application/json" , "application/xml" })
    public Response setAppLifecycle(@PathParam("appKey") String appKey,
            @PathParam("lifecycleName") String lifecycleName, @PathParam("tenantDomain") String tenantDomain);

    /**
     * Method to attach lifecycle to an application of a given app version
     * (This is used to set the lifecycle name of a branch if the user has changed the lifecycle before)
     *
     * @param appKey       application key
     * @param appVersion   application version
     * @param tenantDomain tenant domain
     * @return response
     */
    @POST
    @Path("/updateAppVersionLifecycle/{appKey}/{appVersion}/{tenantDomain}/")
    @Produces({ "application/json" })
    @Consumes({ "application/json" , "application/xml" })
    public Response updateAppVersionLifecycle(@PathParam("appKey") String appKey,
            @PathParam("appVersion") String appVersion, @PathParam("tenantDomain") String tenantDomain);

}
