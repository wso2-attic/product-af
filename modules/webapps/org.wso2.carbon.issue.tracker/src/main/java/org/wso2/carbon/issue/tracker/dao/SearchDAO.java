package org.wso2.carbon.issue.tracker.dao;

import org.wso2.carbon.issue.tracker.bean.SearchBean;
import org.wso2.carbon.issue.tracker.bean.SearchResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Defines the database operations for a {@link SearchBean}
 */
public interface SearchDAO {
    /**
     * Search issues BY_PROJECT_NAME, BY_ASSIGNEE, BY_OWNER
     *
     * @param searchBean {@link SearchBean}
     * @return list of {@link List}
     * @throws SQLException
     */
    public List<SearchResponse> searchIssue(SearchBean searchBean) throws SQLException;

    /**
     * Search issues summary content
     *
     * @param searchBean
     * @return list of {@link List}
     * @throws SQLException
     */
    public List<SearchResponse> searchIssueBySummaryContent(SearchBean searchBean) throws SQLException;
}
