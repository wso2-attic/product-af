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

import org.wso2.carbon.issue.tracker.bean.Comment;

import java.sql.SQLException;
import java.util.List;

/**
 * Defines the database operations for a {@link Comment}
 */
public interface CommentDAO {

    /**
     * Get all comments for given Issue ID
     *
     * @param issueId Issue ID
     * @param tenantId Tenant ID
     * @return List of comments of given issue id
     * @throws SQLException
     */
    public List<Comment> getCommentsForIssue(int issueId, int tenantId) throws SQLException;

    /**
     * Add comment to a given issue
     *
     * @param comment   {@link Comment}
     * @param uniqueKey Issue unique key of comment
     * @param tenantId Tenant ID
     * @return Is comment successfully inserted or not
     * @throws SQLException
     */
    public boolean addCommentForIssue(Comment comment, String uniqueKey, int tenantId) throws SQLException;

    /**
     * Delete Comment by ID
     *
     * @param uniqueKey Issue unique key of comment
     * @param commentId Comment ID
     * @param tenantId Tenant ID
     * @return Comment is successfully deleted or not
     * @throws SQLException
     */
    public boolean deleteCommentByCommentId(String uniqueKey, int commentId, int tenantId) throws SQLException;

    /**
     * Edit Comment based on given comment
     *
     * @param comment   {@link Comment}
     * @param uniqueKey Issue unique key of comment
     * @param tenantId Tenant ID
     * @return Comment is successfully updated or not
     * @throws SQLException
     */
    public boolean editComment(Comment comment, String uniqueKey, int tenantId) throws SQLException;

}
