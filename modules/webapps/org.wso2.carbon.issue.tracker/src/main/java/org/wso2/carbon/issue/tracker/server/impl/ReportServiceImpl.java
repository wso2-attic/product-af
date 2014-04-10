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
