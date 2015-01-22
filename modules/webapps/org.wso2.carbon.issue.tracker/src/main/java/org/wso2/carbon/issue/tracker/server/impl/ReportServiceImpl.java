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
import org.wso2.carbon.issue.tracker.bean.ReportResponse;
import org.wso2.carbon.issue.tracker.delegate.DAODelegate;
import org.wso2.carbon.issue.tracker.server.ReportService;
import org.wso2.carbon.issue.tracker.util.TenantUtils;
import org.wso2.carbon.user.api.UserStoreException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.sql.SQLException;
import java.util.List;

public class ReportServiceImpl implements ReportService {
    private static final Log log = LogFactory.getLog(ReportServiceImpl.class);

    @Context
    private UriInfo ui;


    @Override
    public Response getStatsOfProjectByType(String tenantDomain, String projectKey) {
        Response response = null;

        try {

            int tenantId = TenantUtils.getTenantId(tenantDomain);
            if (tenantId <= 0) {
                throw new WebApplicationException(
                        new IllegalArgumentException(
                                "invalid organization id"));
            }

            List<ReportResponse> reports =
                    DAODelegate.getReportInstance()
                            .getReportByIssueType(projectKey, tenantId);

            GenericEntity<List<ReportResponse>> entity = new GenericEntity<List<ReportResponse>>(reports) {
            };
            response = Response.ok().entity(entity).build();

        } catch (SQLException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (UserStoreException use) {
            throw new WebApplicationException(use, Response.Status.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

}
