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
@Path("/{tenantDomain}/user")
public interface UserService {

    @GET
//    @Path("/users")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAllUsers(@PathParam("tenantDomain") String tenantDomain);

}
