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
