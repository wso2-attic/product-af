package org.wso2.carbon.issue.tracker.server;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Service class defines, operations related to Project related services
 */
@Path("/{tenantDomain}/report")
public interface ReportService {

    @GET
    @Path("/{projectKey}/type")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getStatsOfProjectByType(@PathParam("tenantDomain") String tenantDomain,
                                         @PathParam("projectKey") String projectKey);

}
