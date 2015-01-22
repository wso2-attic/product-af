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

import java.sql.SQLException;
import java.util.List;

import org.wso2.carbon.issue.tracker.bean.Comment;
import org.wso2.carbon.issue.tracker.bean.Issue;
import org.wso2.carbon.issue.tracker.bean.IssueResponse;

/**
 * Defines the database operations for a {@link Issue}
 */
public interface IssueDAO {


    /**
     * Add Issue to DB
     * @param issue {@link Issue}
     * @param projectKey Project Key
     * @param tenantId Tenant ID
     * @return Issue unique key
     * @throws SQLException
     */
    public String add(Issue issue, String projectKey, int tenantId) throws SQLException;

    /**
     * Update Issue to DB
     * @param issue {@link Issue}
     * @param tenantId Tenant ID
     * @return Is issue is successfully updated or not
     * @throws SQLException
     */
    public boolean update(Issue issue, int tenantId) throws SQLException;

    /**
     * Get Issue by unique issue key
     * @param uniqueKey
     * @param tenantId Tenant ID
     * @return {@link IssueResponse}
     * @throws SQLException
     */
    public IssueResponse getIssueByKey(String uniqueKey, int tenantId) throws SQLException;

    /**
     * Get Issue By ID
     * @param id Issue ID
     * @param tenantId Tenant ID
     * @return {@link Issue}
     * @throws SQLException
     */
    public Issue getIssueById(int id, int tenantId) throws SQLException;

    /**
     * Get All Issues of Project By project key
     * @param projectKey Project Key
     * @param tenantId Tenant ID
     * @return {@link List<Issue>}
     * @throws SQLException
     */
    public List<IssueResponse> getAllIssuesOfProjectVersion(String projectKey, int tenantId, String version) throws SQLException;

    public int deleteIssuesOfProject(String projectId) throws SQLException;

}
