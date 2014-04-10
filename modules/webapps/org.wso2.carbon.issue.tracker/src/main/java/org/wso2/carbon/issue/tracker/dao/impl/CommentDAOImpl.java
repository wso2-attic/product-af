package org.wso2.carbon.issue.tracker.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.issue.tracker.bean.Comment;
import org.wso2.carbon.issue.tracker.dao.CommentDAO;
import org.wso2.carbon.issue.tracker.util.Constants;
import org.wso2.carbon.issue.tracker.util.DBConfiguration;
import org.wso2.carbon.issue.tracker.util.ISQLConstants;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link CommentDAO}
 */
public class CommentDAOImpl implements CommentDAO {

    private static final Log log = LogFactory.getLog(CommentDAOImpl.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Comment> getCommentsForIssue(int issueId, int tenantId) throws SQLException {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;

        String selectSQL = ISQLConstants.GET_COMMENTS_FOR_ISSUE;
        List<Comment> comments = new ArrayList<Comment>();

        try {
            dbConnection = DBConfiguration.getDBConnection();
            preparedStatement = dbConnection.prepareStatement(selectSQL);
            preparedStatement.setInt(1, issueId);
            preparedStatement.setInt(2, tenantId);

            // execute select SQL statement
            rs = preparedStatement.executeQuery();

            while (rs.next()) {

                Comment comment = new Comment();
                comment.setId(rs.getInt("ID"));
                comment.setDescription(rs.getString("DESCRIPTION"));

                Timestamp createdTime = rs.getTimestamp("CREATED_TIME");
                String createdTimeStr = Constants.DATE_FORMAT.format(createdTime);
                comment.setCreatedTime(createdTimeStr);

                Timestamp updatedTime = rs.getTimestamp("UPDATED_TIME");
                if (updatedTime != null) {
                    String updatedTimeStr = Constants.DATE_FORMAT.format(updatedTime);
                    comment.setUpdatedTime(updatedTimeStr);
                }

                comment.setCreator(rs.getString("CREATOR"));

                comments.add(comment);
            }

        } catch (SQLException e) {
            String msg = "Error while getting comment from DB, issueID: " + issueId;
            log.error(msg, e);
            throw e;
        } finally {
            if (rs != null){
                rs.close();
            }
            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }

        }
        return comments;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addCommentForIssue(Comment comment, String uniqueKey, int tenantId) throws SQLException {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        String insertTableSQL = ISQLConstants.ADD_COMMENT_FOR_ISSUE;

        boolean isInserted = false;
        try {
            dbConnection = DBConfiguration.getDBConnection();
            preparedStatement = dbConnection.prepareStatement(insertTableSQL);

            preparedStatement.setString(1, comment.getDescription());
            preparedStatement.setTimestamp(2, getCurrentTimeStamp());
            preparedStatement.setNull(3, Types.INTEGER);
            preparedStatement.setString(4, comment.getCreator());
            preparedStatement.setInt(5, tenantId);
            preparedStatement.setString(6, uniqueKey);

            // execute insert SQL statement
            isInserted = preparedStatement.executeUpdate() == 1 ? true : false;

            if (log.isDebugEnabled()) {
                log.debug("Record is inserted into COMMENT table!");
            }

        } catch (SQLException e) {
            String msg = "Error while adding comment to DB, commentID: " + comment.getId();
            log.error(msg, e);
            throw e;
        } finally {

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }
        }
        return isInserted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteCommentByCommentId(String issuePkey, int commentId, int tenantId) throws SQLException {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        String deleteSQL = ISQLConstants.DELETE_COMMENT_BY_COMMENT_ID;
        boolean isDeleted = false;
        try {
            dbConnection = DBConfiguration.getDBConnection();
            preparedStatement = dbConnection.prepareStatement(deleteSQL);
            preparedStatement.setInt(1, tenantId);
            preparedStatement.setInt(2, commentId);
            // execute delete SQL statement
            isDeleted = preparedStatement.executeUpdate() == 1 ? true : false;

            if (log.isDebugEnabled()) {
                log.debug("Record is deleted from COMMENT table!");
            }

        } catch (SQLException e) {
            String msg = "Error while deleting comment from DB, commentID: " + commentId;
            log.error(msg, e);
            throw e;
        } finally {

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }
        }
        return isDeleted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean editComment(Comment comment, String uniqueKey, int tenantId) throws SQLException {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        boolean isUpdated = false;

        String updateTableSQL = ISQLConstants.UPDATE_COMMENT;
        try {
            dbConnection = DBConfiguration.getDBConnection();
            preparedStatement = dbConnection.prepareStatement(updateTableSQL);

            preparedStatement.setString(1, comment.getDescription());
            preparedStatement.setTimestamp(2, getCurrentTimeStamp());
            preparedStatement.setInt(3, comment.getId());
            preparedStatement.setString(4, comment.getCreator());
            preparedStatement.setInt(5, tenantId);

            // execute update SQL stetement
            isUpdated = preparedStatement.executeUpdate() == 1 ? true : false;

            if (log.isDebugEnabled()) {
                log.debug("Record is updated to COMMENT  table!");
            }

        } catch (SQLException e) {
            String msg = "Error while editing comment to DB, commentID: " + comment.getId();
            log.error(msg, e);
            throw e;
        } finally {

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }
        }
        return isUpdated;
    }

    /**
     * Get current time to log DB
     *
     * @return {@link Timestamp}
     */
    private static Timestamp getCurrentTimeStamp() {
        java.util.Date today = new java.util.Date();
        return new Timestamp(today.getTime());
    }

}
