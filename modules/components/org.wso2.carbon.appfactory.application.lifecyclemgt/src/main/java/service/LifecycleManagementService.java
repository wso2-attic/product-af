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

@Path("/lifecyclemanagementservice/")
public interface LifecycleManagementService {

    @GET
    @Path("/lifecycleObject/{appKey}/{appVersion}/")
    @Produces({"application/json"})
    public Lifecycle getCurrentLifeCycle(@PathParam("appKey") String appKey,
                                         @PathParam("appVersion") String appVersion)
            throws AppFactoryException,LifecycleManagementException;

    @GET
    @Path("/nextstage/{lifecycleName}/{currentStage}/")
    public String getNextStage(@PathParam("lifecycleName") String lifecycleName,
                               @PathParam("currentStage") String currentStage)
            throws LifecycleManagementException,AppFactoryException;

    @GET
    @Path("/prestage/{lifecycleName}/{currentStage}/")
    public String getPreStage(@PathParam("lifecycleName") String lifecycleName,
                              @PathParam("currentStage") String currentStage)
            throws LifecycleManagementException,AppFactoryException;

    @GET
    @Path("/lifecyclemap/")
    @Produces({"application/json"})
    public Lifecycle[] getAllLifecycle() throws LifecycleManagementException, AppFactoryException;

    @POST
    @Path("/lifecycleSet/{appKey}/{lifecycleName}/{tenantDomain}/")
    public boolean setAppLifecycle(@PathParam("appKey") String appKey,
                                   @PathParam("lifecycleName") String lifecycleName,
                                   @PathParam("tenantDomain") String tenantDomain)
                                   throws LifecycleManagementException;

    @POST
    @Path("/lifecycle/{appKey}/{appVersion}/{tenantDomain}/")
    public boolean setAppVersionLifecycle(@PathParam("appKey") String appKey,
                                          @PathParam("appVersion") String appVersion,
                                          @PathParam("tenantDomain") String tenantDomain)
            throws LifecycleManagementException, AppFactoryException;

    @GET
    @Path("/lifecycleCheck/{appKey}/")
    public boolean isAppLCChanged(@PathParam("appKey") String appKey);

}
