package org.wso2.carbon.issue.tracker.server;

import org.wso2.carbon.issue.tracker.bean.Comment;
import org.wso2.carbon.issue.tracker.bean.Issue;
import org.wso2.carbon.issue.tracker.bean.SearchBean;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * Service class defines, operations related to Issue related services
 */

@Path("/{tenantDomain}/issue")
public interface IssueService {

    @GET
    @Path("/{uniqueKey}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getIssue(@PathParam("tenantDomain") String tenantDomain, @PathParam("uniqueKey") String uniqueKey);

    @POST
    @Path("/{uniqueKey}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response editIssue(@PathParam("tenantDomain") String tenantDomain, @PathParam("uniqueKey") String uniqueKey, Issue issue);

    @POST
    @Path("/{uniqueKey}/comment")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response addNewCommentForIssue(@PathParam("tenantDomain") String tenantDomain, @PathParam("uniqueKey") String uniqueKey, Comment comment);

    @POST
    @Path("/{uniqueKey}/comment/{commentId}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response modifyCommentForIssue(@PathParam("tenantDomain") String tenantDomain, @PathParam("uniqueKey") String uniqueKey, @PathParam("commentId") int commentId, Comment comment);

    @DELETE
    @Path("/{uniqueKey}/comment/{commentId}/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteComment(@PathParam("tenantDomain") String tenantDomain, @PathParam("uniqueKey") String uniqueKey, @PathParam("commentId") int commentId);

    @POST
    @Path("/search")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchIssue(@PathParam("tenantDomain") String tenantDomain, SearchBean searchBean);

    @DELETE
    @Path("/{projectId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteIssuesOfProject(@PathParam("projectId") String projectId);
}
