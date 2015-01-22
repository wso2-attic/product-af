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

package org.wso2.carbon.issue.tracker.server.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.common.util.StringUtils;
import org.wso2.carbon.appfactory.common.bam.BamDataPublisher;
import org.wso2.carbon.issue.tracker.bean.Issue;
import org.wso2.carbon.issue.tracker.bean.IssueResponse;
import org.wso2.carbon.issue.tracker.bean.Project;
import org.wso2.carbon.issue.tracker.bean.ResponseBean;
import org.wso2.carbon.issue.tracker.bean.Version;
import org.wso2.carbon.issue.tracker.dao.IssueDAO;
import org.wso2.carbon.issue.tracker.dao.VersionDAO;
import org.wso2.carbon.issue.tracker.delegate.DAODelegate;
import org.wso2.carbon.issue.tracker.server.ProjectService;
import org.wso2.carbon.issue.tracker.util.IssueTrackerException;
import org.wso2.carbon.issue.tracker.util.TenantUtils;
import org.wso2.carbon.user.api.UserStoreException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.sql.SQLException;
import java.util.List;


public class ProjectServiceImpl implements ProjectService {
    private static final Log log = LogFactory.getLog(ProjectServiceImpl.class);

    @Context
    private UriInfo ui;

    /**
     * @param tenantDomain Domain Name
     * @return {@link Response}
     */
    @Override
    public Response getAllProject(String tenantDomain) {
        Response response = null;

        try {

            int tenantId = TenantUtils.getTenantId(tenantDomain);
            if (tenantId <= 0) {
                throw new WebApplicationException(
                        new IllegalArgumentException(
                                "invalid organization id"));
            }

            List<Project> projects =
                    DAODelegate.getProjectInstance()
                            .getProjectsByOrganizationId(tenantId);

            GenericEntity<List<Project>> entity = new GenericEntity<List<Project>>(projects) {
            };
            response = Response.ok().entity(entity).build();

        } catch (SQLException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (UserStoreException use) {
            throw new WebApplicationException(use, Response.Status.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    /**
     * @param tenantDomain Domain Name
     * @param projectKey   Project Key
     * @return {@link Response}
     */
    @Override
    public Response getProject(String tenantDomain, String projectKey) {
        try {
            int tenantId = TenantUtils.getTenantId(tenantDomain);
            Project project = DAODelegate.getProjectInstance().get(projectKey, tenantId);
            ResponseBean responseBean = new ResponseBean();

            if (project != null) {
                return Response.ok().entity(project).build();
            } else {
                responseBean.setSuccess(false);
                responseBean.setMessage("Invalid Project Key");
                return Response.status(Status.NOT_FOUND).entity(responseBean).build();
            }

        } catch (SQLException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (UserStoreException use) {
            throw new WebApplicationException(use, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * @param tenantDomain Domain Name
     * @param project      Project Key
     * @return {@link Response}
     */
    @Override
    public Response addProject(String tenantDomain, Project project) {
        if (StringUtils.isEmpty(project.getName())) {
            throw new WebApplicationException(
                    new IllegalArgumentException(
                            "project name cannot be empty"));
        }

        if (StringUtils.isEmpty(project.getOwner())) {
            throw new WebApplicationException(
                    new IllegalArgumentException(
                            "project owner cannot be empty"));
        }

        if (StringUtils.isEmpty(project.getKey())) {
            throw new WebApplicationException(
                    new IllegalArgumentException(
                            "project key cannot be empty"));
        }

        try {
            int tenantId = TenantUtils.getTenantId(tenantDomain);

            if ((tenantId <= 0)) {
                throw new WebApplicationException(
                        new IllegalArgumentException(
                                "invalid tenant"));
            }
            project.setOrganizationId(tenantId);
            int projectId = DAODelegate.getProjectInstance().add(project);

            String response = "id=" + projectId;
            if (projectId > 0) {
                return Response.ok().entity(response).build();
            } else {
                return Response.notModified().entity(response).build();
            }

        } catch (SQLException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (UserStoreException use) {
            throw new WebApplicationException(use, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * @param tenantDomain Domain Name
     * @param projectKey   Project Key
     * @param project      {@link Project}
     * @return {@link Response}
     */
    @Override
    public Response editProject(String tenantDomain, String projectKey, Project project) {

        if (StringUtils.isEmpty(project.getName())) {
            throw new WebApplicationException(
                    new IllegalArgumentException(
                            "project name cannot be empty"));
        }

        if (StringUtils.isEmpty(project.getOwner())) {
            throw new WebApplicationException(
                    new IllegalArgumentException(
                            "project owner cannot be empty"));
        }

        if (StringUtils.isEmpty(projectKey)) {
            throw new WebApplicationException(
                    new IllegalArgumentException(
                            "project key cannot be empty"));
        }

        ResponseBean response = new ResponseBean();

        try {
            int tenantId = TenantUtils.getTenantId(tenantDomain);
            if (tenantId <= 0) {
                throw new WebApplicationException(
                        new IllegalArgumentException(
                                "invalid organization id"));
            }
            project.setOrganizationId(tenantId);
            //check weather the project exists before proceeding.
            project.setKey(projectKey);
            if (DAODelegate.getProjectInstance().update(project)) {
                response.setSuccess(true);
                return Response.ok().entity(response).build();
            } else {
                response.setSuccess(false);
                response.setMessage("Data hasnt persist successfully");
                return Response.notModified().entity(response).build();
            }

        } catch (SQLException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (UserStoreException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }

    }

    /**
     * @param tenantDomain Domain Name
     * @param projectKey   Project Key
     * @return {@link Response}
     */
    @Override
    public Response getAllVersionsOfProject(String tenantDomain, String projectKey) {

        Response response = null;

        try {

            int tenantId = TenantUtils.getTenantId(tenantDomain);
            if (tenantId <= 0) {
                throw new WebApplicationException(
                        new IllegalArgumentException(
                                "invalid organization id"));
            }

            List<Version> versions =
                    DAODelegate.getVersionInstance()
                            .getVersionListOfProjectByProjectKey(projectKey, tenantId);
            GenericEntity<List<Version>> entity = new GenericEntity<List<Version>>(versions) {
            };
            response = Response.ok().entity(entity).build();
        } catch (IssueTrackerException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (SQLException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (UserStoreException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }

        return response;

    }

    /**
     * @param tenantDomain Domain Name
     * @param projectKey   Project Key
     * @return {@link Response}
     */
    @Override
    public Response getAllIssuesOfProject(String tenantDomain, String projectKey) {
        Response response = null;
        try {
            int tenantId = TenantUtils.getTenantId(tenantDomain);
            if (tenantId <= 0) {
                throw new WebApplicationException(
                        new IllegalArgumentException(
                                "invalid organization id"));
            }

            List<IssueResponse> issues = DAODelegate.getIssueInstance().getAllIssuesOfProjectVersion(projectKey,
                    tenantId, null);
            GenericEntity<List<IssueResponse>> entity = new GenericEntity<List<IssueResponse>>(issues) {
            };

            response = Response.ok().entity(entity).build();
        } catch (SQLException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (UserStoreException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    @Override
    public Response getAllIssuesOfProjectVersion(String tenantDomain, String projectKey, String version) {
        Response response = null;
        try {
            int tenantId = TenantUtils.getTenantId(tenantDomain);
            if (tenantId <= 0) {
                throw new WebApplicationException(
                        new IllegalArgumentException(
                                "invalid organization id"));
            }

            List<IssueResponse> issues = DAODelegate.getIssueInstance().getAllIssuesOfProjectVersion(projectKey,
                    tenantId, version);
            GenericEntity<List<IssueResponse>> entity = new GenericEntity<List<IssueResponse>>(issues) {
            };

            response = Response.ok().entity(entity).build();
        } catch (SQLException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (UserStoreException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    /**
     * @param tenantDomain Domain Name
     * @param projectKey   Project Key
     * @param issue        {@link Issue}
     * @return {@link Response}
     */
    @Override
    public Response addNewIssueToProject(String tenantDomain, String projectKey, Issue issue) {
        if (log.isDebugEnabled()) {
            log.debug("Executing addNewIssueToProject, created by: " + issue.getReporter());
        }
        if (StringUtils.isEmpty(projectKey)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Project ID cannot be empty!").build();
        }

        if (StringUtils.isEmpty(issue.getSummary())) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Issue summary cannot be empty!").build();
        }

        if (StringUtils.isEmpty(issue.getReporter())) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Issue reporter cannot be empty!").build();
        }

        if (StringUtils.isEmpty(issue.getType())) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Issue Type cannot be empty!").build();
        }

        if (StringUtils.isEmpty(issue.getPriority())) {
            issue.setPriority("NORMAL");
        }

        if (StringUtils.isEmpty(issue.getStatus())) {
            issue.setStatus("OPEN");
        }

        IssueDAO issueDAO = DAODelegate.getIssueInstance();
        ResponseBean response = new ResponseBean();
        String issueKey = null;


        try {
            int tenantId = TenantUtils.getTenantId(tenantDomain);
            if (tenantId <= 0) {
                throw new WebApplicationException(
                        new IllegalArgumentException(
                                "invalid organization id"));
            }

            issueKey = issueDAO.add(issue, projectKey, tenantId);

            log.info("Issue Key: " + issueKey);
            Project project = DAODelegate.getProjectInstance().get(projectKey, tenantId);

            if (issueKey != null) {
                response.setSuccess(true);
                BamDataPublisher publisher = BamDataPublisher.getInstance();

                publisher.PublishIssueEvent(issueKey, project.getName(), projectKey, issue.getVersion(),
                        System.currentTimeMillis(), "" + tenantId, issue.getType(), issue.getPriority(),
                        issue.getStatus(), issue.getReporter(), issue.getAssignee(), "ADD", issue.getSeverity(),
                        issue.getCreatedTime(), issue.getUpdatedTime());
                return Response.ok().entity(issueKey).type(MediaType.APPLICATION_JSON).build();
            } else {
                response.setSuccess(false);
                response.setMessage("Issue is not successfully inserted.");
                return Response.notModified().type(MediaType.APPLICATION_JSON_TYPE).entity(response).build();
            }

        } catch (SQLException e) {
            String msg = "Error while add Issue to Project, " + e.getMessage();
            log.error(msg, e);
            response.setSuccess(false);
            response.setMessage(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).type(MediaType.APPLICATION_JSON_TYPE).build();
        } catch (UserStoreException e) {
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).type(MediaType.APPLICATION_JSON_TYPE).build();
        } catch (Exception e) {
            // do nothing
            return Response.ok().entity(issueKey).type(MediaType.APPLICATION_JSON).build();
        }
    }

    /**
     * @param tenantDomain Domain Name
     * @param projectKey   Project Key
     * @param version      {@link Version}
     * @return {@link Response}
     */
    @Override
    public Response addNewVersionToProject(String tenantDomain, String projectKey, Version version) {
        if (log.isDebugEnabled()) {
            log.debug("Executing addNewVersionToProject, project versoin: " + version.getVersion());
        }
        if (StringUtils.isEmpty(projectKey)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Project!").build();
        }

        if (StringUtils.isEmpty(version.getVersion())) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Project Version cannot be empty!").build();
        }

        //version.setProjectK(projectId);
        ResponseBean response = new ResponseBean();
        VersionDAO versionDAO = DAODelegate.getVersionInstance();
        try {

            int tenantId = TenantUtils.getTenantId(tenantDomain);
            if (tenantId <= 0) {
                throw new WebApplicationException(
                        new IllegalArgumentException(
                                "invalid organization id"));
            }

            boolean isInserted = versionDAO.addVersionForProject(version, projectKey, tenantId);
            response.setSuccess(isInserted);

            if (isInserted) {
                return Response.ok().entity(response).type(MediaType.APPLICATION_JSON).build();
            } else {
                response.setMessage("Version is not successfully inserted.");
                return Response.notModified().type(MediaType.APPLICATION_JSON_TYPE).entity(response).build();
            }
        } catch (SQLException e) {
            String msg = "Error while add Version to Project, " + e.getMessage();
            response.setSuccess(false);
            response.setMessage(msg);
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).type(MediaType.APPLICATION_JSON_TYPE).build();
        } catch (UserStoreException e) {
            String msg = "Error while add Version to Project, " + e.getMessage();
            response.setSuccess(false);
            response.setMessage(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
    }

    public Response deleteProject(String tenantDomain, String projectKey) {
        try {
            int tenantId = TenantUtils.getTenantId(tenantDomain);

            if (projectKey != null) {
                DAODelegate.getProjectInstance().delete(projectKey, tenantId);
                ResponseBean responseBean = new ResponseBean();
            }

           /* if (project != null) {
                return Response.ok().entity(project).build();
            } else {
                responseBean.setSuccess(false);
                responseBean.setMessage("Invalid Project Key");
                return Response.status(Status.NOT_FOUND).entity(responseBean).build();
            }*/

        } catch (SQLException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (UserStoreException use) {
            throw new WebApplicationException(use, Response.Status.INTERNAL_SERVER_ERROR);
        }
        return Response.ok().build();
    }

    @Override
    public Response deleteVersionForProject(String projectId, String version) {
        try {
            DAODelegate.getVersionInstance().deleteVersionForProject(projectId, version);
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return Response.ok().build();
    }

    @Override
    public Response deleteProjectVersions(String projectId) {
        try {
            DAODelegate.getVersionInstance().deleteProjectVersions(projectId);
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
