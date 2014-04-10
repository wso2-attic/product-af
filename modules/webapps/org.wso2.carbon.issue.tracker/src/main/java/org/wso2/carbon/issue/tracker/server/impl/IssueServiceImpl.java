package org.wso2.carbon.issue.tracker.server.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.common.util.StringUtils;
import org.wso2.carbon.appfactory.bam.integration.BamDataPublisher;
import org.wso2.carbon.issue.tracker.bean.*;
import org.wso2.carbon.issue.tracker.dao.CommentDAO;
import org.wso2.carbon.issue.tracker.dao.IssueDAO;
import org.wso2.carbon.issue.tracker.dao.SearchDAO;
import org.wso2.carbon.issue.tracker.delegate.DAODelegate;
import org.wso2.carbon.issue.tracker.server.IssueService;
import org.wso2.carbon.issue.tracker.util.Constants;
import org.wso2.carbon.issue.tracker.util.TenantUtils;
import org.wso2.carbon.issue.tracker.util.IssueUtils;
import org.wso2.carbon.user.api.UserStoreException;

import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;

/**
 * Implementation of {@link IssueService}
 */
public class IssueServiceImpl implements IssueService {

    private static final Log log = LogFactory.getLog(IssueServiceImpl.class);

    /**
     * Get issues and comments for a given issue id
     *
     * @param tenantDomain Tenant domain name
     * @param uniqueKey    Unique key of issue which need to retrieve
     * @return {@link Response}
     */
    @Override
    public Response getIssue(String tenantDomain, String uniqueKey) {
        if (log.isDebugEnabled()) {
            log.debug("Executing getIssue, uniqueKey: " + uniqueKey);
        }
        IssueDAO issueDAO = DAODelegate.getIssueInstance();
        CommentDAO commentDAO = DAODelegate.getCommentInstance();

        List<Comment> comments = null;
        try {
            int tenantId = TenantUtils.getTenantId(tenantDomain);
            if (tenantId <= 0) {
                throw new WebApplicationException(
                        new IllegalArgumentException(
                                "invalid organization id"));
            }

            IssueResponse issueResponse = issueDAO.getIssueByKey(uniqueKey, tenantId);

            if (issueResponse != null)
                comments = commentDAO.getCommentsForIssue(issueResponse.getIssue().getId(), tenantId);     // get all comments related to given issue

            if (comments != null && comments.size() == 1) {
                comments.add(new Comment());
            }

            if (issueResponse != null) {
                issueResponse.setComments(comments);
            }
            return Response.ok().entity(issueResponse).type(MediaType.APPLICATION_JSON_TYPE).build();
        } catch (Exception e) {
            String msg = "Error while get comments for issue";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
    }


    /**
     * Edit issue details
     *
     * @param tenantDomain Tenant domain name
     * @param uniqueKey    Unique key of issue which need to retrieve
     * @param issue        {@link Issue}
     * @return {@link Response}
     */
    @Override
    public Response editIssue(String tenantDomain, String uniqueKey, Issue issue) {

        if (log.isDebugEnabled()) {
            log.debug("Executing editIssue, created by: " + issue.getReporter());
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

        issue.setKey(uniqueKey);
        IssueDAO issueDAO = DAODelegate.getIssueInstance();
        ResponseBean responseBean = new ResponseBean();
        int tenantId;

        try {

            tenantId = TenantUtils.getTenantId(tenantDomain);
            if (tenantId <= 0) {
                throw new WebApplicationException(
                        new IllegalArgumentException(
                                "invalid organization id"));
            }

            boolean isInserted = issueDAO.update(issue, tenantId);
            responseBean.setSuccess(isInserted);

            if (isInserted) {
                BamDataPublisher publisher = new BamDataPublisher();

                String projectKey = IssueUtils.getProjectKey(uniqueKey);
                Project project = DAODelegate.getProjectInstance().get(projectKey, tenantId);

                publisher.PublishIssueEvent(issue.getKey(), project.getName(), projectKey, issue.getVersion(),
                        System.currentTimeMillis(), "" + tenantId, issue.getType(), issue.getPriority(),
                        issue.getStatus(), issue.getReporter(), issue.getAssignee(), "UPDATE",
                        issue.getSeverity(), issue.getCreatedTime(), issue.getUpdatedTime());
            	
                return Response.ok().entity(responseBean).type(MediaType.APPLICATION_JSON).build();
            } else {
                responseBean.setMessage("Issue is not successfully updated.");
                return Response.notModified().type(MediaType.APPLICATION_JSON_TYPE).entity(responseBean).build();
            }
        } catch (SQLException e) {
            String msg = "Error while edit Issue to Project, " + e.getMessage();
            log.error(msg, e);
            responseBean.setSuccess(false);
            responseBean.setMessage(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(responseBean).type(MediaType.APPLICATION_JSON_TYPE).build();
        } catch (UserStoreException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(responseBean).type(MediaType.APPLICATION_JSON_TYPE).build();
        } catch (Exception e) {
            // do nothing
            return Response.ok().entity(responseBean).type(MediaType.APPLICATION_JSON).build();
        }
    }

    /**
     * Add new comment to given issue
     *
     * @param tenantDomain Tenant domain name
     * @param uniqueKey    Comment's, issue id
     * @param comment      {@link Comment}
     * @return {@link Response}, Returns HTTP/1.1 200 for successfully added comment else returns internal server error HTTP/1.1 500
     */
    @Override
    public Response addNewCommentForIssue(String tenantDomain, String uniqueKey, Comment comment) {
        if (log.isDebugEnabled()) {
            log.debug("Executing addNewCommentForIssue, created by: " + comment.getCreator());
        }

        if (StringUtils.isEmpty(comment.getDescription())) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Comment cannot be empty").build();
        }

        if (StringUtils.isEmpty(comment.getCreator())) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Comment creator cannot be empty").build();
        }

        CommentDAO commentDAO = DAODelegate.getCommentInstance();
        ResponseBean responseBean = new ResponseBean();

        try {

            int tenantId = TenantUtils.getTenantId(tenantDomain);
            if (tenantId <= 0) {
                throw new WebApplicationException(
                        new IllegalArgumentException(
                                "invalid organization id"));
            }
            boolean isInserted = commentDAO.addCommentForIssue(comment, uniqueKey, tenantId);
            if (isInserted) {
                responseBean.setSuccess(true);
                return Response.ok().entity(responseBean).type(MediaType.APPLICATION_JSON).build();
            } else {
                responseBean.setSuccess(false);
                responseBean.setMessage("Data is not successfully inserted");
                return Response.notModified().type(MediaType.APPLICATION_JSON_TYPE).entity(responseBean).build();
            }
        } catch (SQLException e) {
            String msg = "Error while add comments for issue, " + e.getMessage();
            log.error(msg, e);
            responseBean.setSuccess(false);
            responseBean.setMessage(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.APPLICATION_JSON_TYPE).build();
        } catch (UserStoreException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
    }


    /**
     * Edit comment to given issue
     *
     * @param tenantDomain Tenant domain name
     * @param uniqueKey    Issue id of comment, which need to edit
     * @param commentId    Comment id
     * @param comment      {@link Comment}
     * @return {@link Response}, Returns HTTP/1.1 200 for successfully edited comment else
     *         returns internal server error HTTP/1.1 500
     */
    @Override
    public Response modifyCommentForIssue(String tenantDomain, String uniqueKey, int commentId, Comment comment) {
        if (log.isDebugEnabled()) {
            log.debug("Executing modifyCommentForIssue, CommentId: " + commentId);
        }

        if (StringUtils.isEmpty(comment.getDescription())) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Comment cannot be empty").build();
        }

        if (StringUtils.isEmpty(comment.getCreator())) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Comment creator cannot be empty").build();
        }

        if (commentId == 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid comment ID").build();
        }

        CommentDAO commentDAO = DAODelegate.getCommentInstance();
        ResponseBean responseBean = new ResponseBean();

        try {
            int tenantId = TenantUtils.getTenantId(tenantDomain);
            if (tenantId <= 0) {
                throw new WebApplicationException(
                        new IllegalArgumentException(
                                "invalid organization id"));
            }
            comment.setId(commentId);

            boolean isUpdated = commentDAO.editComment(comment, uniqueKey, tenantId);
            if (isUpdated) {
                responseBean.setSuccess(true);
                return Response.ok(responseBean).build();
            } else {
                responseBean.setMessage("Data is not successfully updated.");
                responseBean.setSuccess(false);
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(responseBean).build();
            }
        } catch (SQLException e) {
            String msg = "Error while edit comments, " + e.getMessage();
            log.error(msg, e);
            responseBean.setSuccess(false);
            responseBean.setMessage(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.APPLICATION_JSON_TYPE).build();
        } catch (UserStoreException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
    }


    /**
     * Deleted comment from DB
     *
     * @param tenantDomain Tenant domain name
     * @param uniqueKey    Issue id of comment, which need to delete
     * @param commentId    Comment id of comment, which need to delete
     * @return {@link Response}, Returns HTTP/1.1 200 for successfully edited comment else
     *         returns internal server error HTTP/1.1 500
     */
    @Override
    public Response deleteComment(String tenantDomain, String uniqueKey, int commentId) {
        if (log.isDebugEnabled()) {
            log.debug("Executing deleteComment, commentID: " + commentId);
        }

        if (commentId == 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid comment ID").build();
        }
        ResponseBean responseBean = new ResponseBean();
        CommentDAO commentDAO = DAODelegate.getCommentInstance();
        try {
            int tenantId = TenantUtils.getTenantId(tenantDomain);
            if (tenantId <= 0) {
                throw new WebApplicationException(
                        new IllegalArgumentException(
                                "invalid organization id"));
            }

            boolean result = commentDAO.deleteCommentByCommentId(uniqueKey, commentId, tenantId);
            responseBean.setSuccess(result);
            if (result) {
                return Response.ok().entity(responseBean).build();
            } else {
                responseBean.setMessage("Invalid credentials.");
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(responseBean).build();
            }
        } catch (SQLException e) {
            String msg = "Error while delete comments, " + e.getMessage();
            log.error(msg, e);
            responseBean.setSuccess(false);
            responseBean.setMessage(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.APPLICATION_JSON_TYPE).build();
        } catch (UserStoreException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
    }

    /**
     * Search Issues of given project
     *
     * @param tenantDomain Tenant domain name
     * @param searchBean   {@link SearchBean}
     * @return
     */
    @Override
    public Response searchIssue(String tenantDomain, SearchBean searchBean) {
        SearchDAO searchDAO = DAODelegate.getSearchInstance();
        List<SearchResponse> list = null;

        String status = searchBean.getIssueStatus();
        String issueType = searchBean.getIssueType();
        String priority = searchBean.getPriority();
        String severity = searchBean.getSeverity();

        if (StringUtils.isEmpty(status) || status.equals("-1"))
            searchBean.setIssueStatus(null);
        if (StringUtils.isEmpty(issueType) || issueType.equals("-1"))
            searchBean.setIssueType(null);
        if (StringUtils.isEmpty(priority) || priority.equals("-1"))
            searchBean.setPriority(null);
        if (StringUtils.isEmpty(severity) || severity.equals("-1"))
            searchBean.setSeverity(null);

        try {
            int tenantId = TenantUtils.getTenantId(tenantDomain);

            if (tenantId <= 0) {
                throw new WebApplicationException(
                        new IllegalArgumentException(
                                "invalid organization id"));
            }

            searchBean.setOrganizationId(tenantId);

            ResponseBean responseBean = new ResponseBean();

            if (searchBean.getSearchType() == Constants.ALL_ISSUE) {
                responseBean.setSuccess(true);
                list = searchDAO.searchIssueBySummaryContent(searchBean);
            } else {
                list = searchDAO.searchIssue(searchBean);
            }

        } catch (Exception e) {
            String msg = "Error while searching Issues";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        GenericEntity entity = new GenericEntity<List<SearchResponse>>(list) {
        };
        return Response.ok().entity(entity).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Override
    public Response deleteIssuesOfProject(String projectId) {
        try {
            DAODelegate.getIssueInstance().deleteIssuesOfProject(projectId);
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
