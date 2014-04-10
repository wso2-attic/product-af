/**
 *
 */
package org.wso2.carbon.issue.tracker.dao.impl;

import org.apache.cxf.common.util.StringUtils;
import org.apache.log4j.Logger;
import org.wso2.carbon.issue.tracker.bean.Report;
import org.wso2.carbon.issue.tracker.bean.ReportResponse;
import org.wso2.carbon.issue.tracker.dao.ReportDAO;
import org.wso2.carbon.issue.tracker.util.DBConfiguration;
import org.wso2.carbon.issue.tracker.util.ISQLConstants;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link org.wso2.carbon.issue.tracker.dao.ProjectDAO}
 */
public class ReportDAOImpl implements ReportDAO {

    private static Logger logger = Logger.getLogger(ReportDAOImpl.class);

    /**
    * {@inheritDoc}
    */
    @Override
    public List<ReportResponse> getReportByIssueType(String projectKey, int tenantId) throws SQLException{
        PreparedStatement st = null;
        Connection dbConnection = null;
        ResultSet rs = null;
        List<ReportResponse> reportResponses = new ArrayList<ReportResponse>();

        Map<String,List<Report>> tempResponseMap = new HashMap<String, List<Report>>();

        try {
            dbConnection = DBConfiguration.getDBConnection();

            st = dbConnection.prepareStatement(ISQLConstants.REPORT_ISSUES_BY_TYPE);
            if (StringUtils.isEmpty(projectKey))
                st.setNull(1, Types.INTEGER);
            else
                st.setString(1, projectKey);

            st.setInt(2, tenantId);

            rs = st.executeQuery();
            while (rs.next()) {
                Report report = new Report();

                report.setType(rs.getString("ISSUE_TYPE"));
                report.setIssueCount(rs.getString("ISSUE_COUNT"));

                String version = rs.getString("VERSION");
                if(tempResponseMap.containsKey(version)){
                    tempResponseMap.get(version).add(report);
                }else{
                    List<Report> reportList = new ArrayList<Report>();
                    reportList.add(report);

                    tempResponseMap.put(version,reportList);
                }

            }

        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            throw e;

        } finally {
            closeStatement(st, dbConnection);

        }
        prepareResponse(projectKey, reportResponses, tempResponseMap);
        return reportResponses;
    }

    private void prepareResponse(String projectKey, List<ReportResponse> reportResponses, Map<String, List<Report>> tempResponseMap) {
        for (Map.Entry<String, List<Report>> entry : tempResponseMap.entrySet()) {
            ReportResponse reportResponse = new ReportResponse();
            reportResponse.setProjectKey(projectKey);
            reportResponse.setProjectVersion(entry.getKey());
            reportResponse.setReportList(entry.getValue());

            reportResponses.add(reportResponse);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Report> getReportByPriority(String projectKey, int tenantId) throws SQLException {
//        We haven't implemented this so far. Hence returning null
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Report> getReportByReporter(String projectKey, int tenantId) throws SQLException {
//        We haven't implemented this so far. Hence returning null
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Report> getReportByStatus(String projectKey, int tenantId) throws SQLException {
//        We haven't implemented this so far. Hence returning null
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Report> getReportByAssignee(String projectKey, int tenantId) throws SQLException {
//        We haven't implemented this so far. Hence returning null
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Report> getReportByVersion(String projectKey, int tenantId) throws SQLException {
//        We haven't implemented this so far. Hence returning null
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Report> getReportBySeverity(String projectKey, int tenantId) throws SQLException {
//        We haven't implemented this so far. Hence returning null
        return null;
    }

    private void closeStatement(PreparedStatement st, Connection dbConnection) {
        try {
            if (st != null) {
                st.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }
        } catch (SQLException ignore) {

        }
    }
}
