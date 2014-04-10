package org.wso2.carbon.issue.tracker.server.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.issue.tracker.bean.User;
import org.wso2.carbon.issue.tracker.server.UserService;
import org.wso2.carbon.issue.tracker.util.TenantUtils;
import org.wso2.carbon.user.api.UserStoreException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

public class UserServiceImpl implements UserService {
    private static final Log log = LogFactory.getLog(UserServiceImpl.class);

    @Context
    private UriInfo ui;


    @Override
    public Response getAllUsers(String tenantDomain) {
        Response response = null;
        try {
            String[] userNames = TenantUtils.getUsersOfTenant(tenantDomain);

            List<User> userList = new ArrayList<User>();

            for (String userName : userNames) {
                User user = new User();
                user.setName(userName);

                userList.add(user);
            }
            GenericEntity<List<User>> entity = new GenericEntity<List<User>>(userList) {};
            response = Response.ok(entity).build();
        } catch (UserStoreException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
        return response;
    }
}
