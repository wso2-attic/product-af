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

import impl.LifecycleManagementException;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import util.Lifecycle;

import javax.ws.rs.*;
import java.util.Collection;

/**
 * Contains operations related to lifecycle management of applications
 */
@Path("/lifecycleManagementService/")
public interface LifecycleManagementService {

    @GET
    @Path("/lifecycleObject/{appKey}/{appVersion}/{tenantDomain}/")
    @Produces({ "application/json" })
    public Lifecycle getCurrentLifeCycle(@PathParam("appKey") String appKey, @PathParam("appVersion") String appVersion,
            @PathParam("tenantDomain") String tenantDomain) throws AppFactoryException, LifecycleManagementException;

    @GET
    @Path("/nextStage/{lifecycleName}/{currentStage}/")
    public String getNextStage(@PathParam("lifecycleName") String lifecycleName,
            @PathParam("currentStage") String currentStage) throws LifecycleManagementException, AppFactoryException;

    @GET
    @Path("/previousStage/{lifecycleName}/{currentStage}/")
    public String getPreviousStage(@PathParam("lifecycleName") String lifecycleName,
            @PathParam("currentStage") String currentStage) throws LifecycleManagementException, AppFactoryException;

    @GET
    @Path("/lifecycleMap/")
    @Produces({ "application/json" })
    public Collection<Lifecycle> getAllLifeCycles() throws LifecycleManagementException, AppFactoryException;

    @POST
    @Path("/setLifecycle/{appKey}/{lifecycleName}/{tenantDomain}/")
    public boolean setAppLifecycle(@PathParam("appKey") String appKey, @PathParam("lifecycleName") String lifecycleName,
            @PathParam("tenantDomain") String tenantDomain) throws LifecycleManagementException, AppFactoryException;

    @POST
    @Path("/SetAppVersion/{appKey}/{appVersion}/{tenantDomain}/")
    public boolean setAppVersionLifecycle(@PathParam("appKey") String appKey,
            @PathParam("appVersion") String appVersion, @PathParam("tenantDomain") String tenantDomain)
            throws LifecycleManagementException, AppFactoryException;

    @GET
    @Path("/lifecycleIsChanged/{appKey}/{tenantDomain}/")
    public boolean isAppLCChanged(@PathParam("appKey") String appKey, @PathParam("tenantDomain") String tenantDomain)
            throws AppFactoryException;

}
