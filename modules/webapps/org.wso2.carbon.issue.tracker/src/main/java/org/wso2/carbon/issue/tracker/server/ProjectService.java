/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *    WSO2 Inc. licenses this file to you under the Apache License,
 *    Version 2.0 (the "License"); you may not use this file except
 *    in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package org.wso2.carbon.issue.tracker.server;

import org.wso2.carbon.issue.tracker.bean.Issue;
import org.wso2.carbon.issue.tracker.bean.Project;
import org.wso2.carbon.issue.tracker.bean.ResponseBean;
import org.wso2.carbon.issue.tracker.bean.Version;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * Service class defines, operations related to Project related services
 */
@Path("/{tenantDomain}/project")
public interface ProjectService {

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAllProject(@PathParam("tenantDomain") String tenantDomain);

    @GET
    @Path("/{projectKey}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getProject(@PathParam("tenantDomain") String tenantDomain, @PathParam("projectKey") String projectKey);

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces(MediaType.APPLICATION_JSON)
    public Response addProject(@PathParam("tenantDomain") String tenantDomain, Project project);

    @POST
    @Path("/{projectKey}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response editProject(@PathParam("tenantDomain") String tenantDomain, @PathParam("projectKey") String projectKey, Project project);

    @GET
    @Path("/{projectKey}/version")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAllVersionsOfProject(@PathParam("tenantDomain") String tenantDomain, @PathParam("projectKey") String projectKey);

    @GET
    @Path("/{projectKey}/issue")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAllIssuesOfProject(@PathParam("tenantDomain") String tenantDomain, @PathParam("projectKey") String projectKey);

    @GET
    @Path("/{projectKey}/issue/{version}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAllIssuesOfProjectVersion(@PathParam("tenantDomain") String tenantDomain,
                                                 @PathParam("projectKey") String projectKey,
                                                 @PathParam("version") String version);

    @POST
    @Path("/{projectKey}/issue")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response addNewIssueToProject(@PathParam("tenantDomain") String tenantDomain, @PathParam("projectKey") String projectKey, Issue issue);

    @POST
    @Path("/{projectKey}/version")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response addNewVersionToProject(@PathParam("tenantDomain") String tenantDomain, @PathParam("projectKey") String projectKey, Version version);

    @DELETE
    @Path("/{projectKey}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteProject(@PathParam("tenantDomain") String tenantDomain, @PathParam("projectKey") String projectKey);

    @DELETE
    @Path("/{projectId}/{version}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteVersionForProject(@PathParam("projectId") String projectId, @PathParam("version") String version);

    @DELETE
    @Path("/{projectId}/{version}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteProjectVersions(@PathParam("projectId") String projectId);
}
