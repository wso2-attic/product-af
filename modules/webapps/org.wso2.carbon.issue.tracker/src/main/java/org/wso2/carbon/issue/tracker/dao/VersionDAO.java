/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.issue.tracker.dao;

import org.wso2.carbon.issue.tracker.bean.Version;
import org.wso2.carbon.issue.tracker.util.IssueTrackerException;

import java.sql.SQLException;
import java.util.List;

/**
 * Defines the database operations for a {@link Version}
 */
public interface VersionDAO {

    /**
     * Add new {@link Version} to DB
     * @param version {@link Version}
     * @param projectKey Project Key
     * @param tenantId  tenant ID
     * @return returns version is successfully added or not
     * @throws SQLException
     */
    public boolean addVersionForProject(Version version, String projectKey, int tenantId) throws SQLException;

    /**
     * Get Version list of Project by project key
     * @param projectKey  project key
     * @param tenantId tenant id
     * @return  {@link List<Version>}
     * @throws IssueTrackerException
     * @throws SQLException
     */
    public List<Version> getVersionListOfProjectByProjectKey(String projectKey, int tenantId) throws IssueTrackerException, SQLException;

    /**
     * Delete the given version of the project
     * @param projectId
     * @param version
     * @return
     * @throws SQLException
     */
    public boolean deleteVersionForProject(String projectId, String version) throws SQLException;

    public int deleteProjectVersions(String projectId) throws SQLException;

}
