package org.wso2.carbon.issue.tracker.delegate;

import org.wso2.carbon.issue.tracker.dao.*;
import org.wso2.carbon.issue.tracker.dao.impl.*;


/**
 * Singleton class to get DAO object
 */
public class DAODelegate {

    private static CommentDAO commentInstance = new CommentDAOImpl();
    private static IssueDAO issueInstance = new IssueDAOImpl();
    private static ProjectDAO projectInstance = new ProjectDAOImpl();
    private static VersionDAO versionInstance = new VersionDAOImpl();
    private static SearchDAO searchInstance = new SearchDAOImpl();
    private static ReportDAO reportInstance = new ReportDAOImpl();

    /**
     * Get CommentDAO object
     *
     * @return {@link CommentDAO}
     */
    public static CommentDAO getCommentInstance() {
        return commentInstance;
    }

    /**
     * Get IssueDAOImpl object
     *
     * @return {@link IssueDAOImpl}
     */
    public static IssueDAO getIssueInstance() {
        return issueInstance;
    }

    /**
     * Get ProjectDAO object
     *
     * @return {@link ProjectDAO}
     */
    public static ProjectDAO getProjectInstance() {
        return projectInstance;
    }

    /**
     * Get VersionDAO object
     *
     * @return {@link VersionDAO}
     */
    public static VersionDAO getVersionInstance() {
        return versionInstance;
    }

    public static SearchDAO getSearchInstance() {
        return searchInstance;
    }

    public static ReportDAO getReportInstance() {
        return reportInstance;
    }
}
