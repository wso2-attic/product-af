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
