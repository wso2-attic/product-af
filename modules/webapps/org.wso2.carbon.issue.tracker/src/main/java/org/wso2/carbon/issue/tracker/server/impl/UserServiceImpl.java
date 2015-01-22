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

    @Override
    public Response getAppUsers(String tenantDomain, String uniqueKey) {
        Response response = null;
        try {
            String[] userNames = TenantUtils.getUsersOftheApplication(uniqueKey, tenantDomain);

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
