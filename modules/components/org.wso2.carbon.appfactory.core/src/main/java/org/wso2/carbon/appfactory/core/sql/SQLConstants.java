/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.appfactory.core.sql;

/**
 * Contains SQL statements used by {@link org.wso2.carbon.appfactory.core.dao.JDBCApplicationDAO}
 */
public class SQLConstants {

    //**************************************************Insert Queries
    public static final String ADD_APPLICATION_SQL = "INSERT INTO AF_APPLICATION (APPLICATION_KEY, APPLICATION_NAME," +
                                                     "TENANT_ID) VALUES(?,?,?)";
    public static final String ADD_APPLICATION_REPOSITORY_SQL = "INSERT INTO AF_REPOSITORY (VERSION_ID,IS_FORKED," +
                                                                "GIT_USER_ID,TENANT_ID) VALUES(?,?,?,?)";
    public static final String ADD_BUILD_STATUS_SQL = "INSERT INTO AF_BUILD (REPOSITORY_ID, TENANT_ID) VALUES(?,?)";
    public static final String ADD_DEPLOY_STATUS_SQL = "INSERT INTO AF_DEPLOY (REPOSITORY_ID, ENVIRONMENT, TENANT_ID) " +
                                                       "VALUES(?,?,?)";
    public static final String ADD_VERSION = "INSERT INTO AF_VERSION (APPLICATION_ID,VERSION_NAME,STAGE,TENANT_ID) " +
                                             "VALUES(?,?,?,?)";
    public static final String ADD_RESOURCE_SQL = "INSERT INTO AF_RESOURCE (APPLICATION_ID,RESOURCE_NAME," +
                                                  "RESOURCE_TYPE, ENVIRONMENT,DESCRIPTION,TENANT_ID) " +
                                                  "VALUES(?,?,?,?,?,?)";
    public static final String ADD_CARTRIDGE_CLUSTER_SQL = "INSERT INTO AF_CARTRIDGE_CLUSTER (CLUSTER_ID, " +
                                                           "LB_CLUSTER_ID, ACTIVE_IP, TENANT_ID) VALUES(?,?,?,?)";


    //**************************************************Select Queries
    public static final String GET_APPLICATION_SQL = "SELECT ID FROM AF_APPLICATION " +
            "WHERE APPLICATION_KEY=? AND " +
            "TENANT_ID=?";
    public static final String GET_APPLICATION_VERSION_ID_SQL = "SELECT ID FROM " +
            "AF_VERSION " +
            "WHERE APPLICATION_ID=? AND " +
            "VERSION_NAME=?";
    public static final String GET_ALL_APPLICATIONS_SQL = "SELECT APPLICATION_KEY," +
            "STATUS FROM AF_APPLICATION WHERE TENANT_ID=?";
    public static final String GET_APPLICATION_VERSION_SQL =
            "SELECT VERSION_NAME,STAGE,PROMOTE_STATUS,AUTO_BUILD, AUTO_DEPLOY,SUBDOMAIN FROM AF_VERSION JOIN " +
            "(SELECT ID FROM AF_APPLICATION WHERE APPLICATION_KEY=? AND TENANT_ID = ?) AS AF_APP ON " +
            "AF_APP.ID=AF_VERSION.APPLICATION_ID WHERE VERSION_NAME=?";

    public static final String GET_APPLICATION_VERSION_PER_USER =
            "SELECT * FROM ((SELECT ID,VERSION_NAME,APPLICATION_ID,STAGE, PROMOTE_STATUS,AUTO_BUILD,AUTO_DEPLOY,SUBDOMAIN" +
            " FROM AF_VERSION) AS VERSION JOIN (SELECT ID FROM AF_APPLICATION WHERE APPLICATION_KEY=? AND TENANT_ID = ?)" +
            " AS AF_APP ON AF_APP.ID=VERSION.APPLICATION_ID JOIN (SELECT VERSION_ID FROM AF_REPOSITORY WHERE " +
            "GIT_USER_ID = ? AND TENANT_ID = ? AND IS_FORKED = 1) AS AF_REPO ON VERSION.ID=AF_REPO.VERSION_ID)";

    public static final String GET_APPLICATION_LAST_DEPLOY_SQL =
            "SELECT REPOSITORY_ID, LAST_DEPLOYED_BUILD_ID, LAST_DEPLOY_STATUS, LAST_DEPLOY_TIME FROM (SELECT REPOSITORY_ID," +
            " LAST_DEPLOYED_BUILD_ID, LAST_DEPLOY_STATUS, LAST_DEPLOY_TIME FROM AF_DEPLOY WHERE ENVIRONMENT=?) AS deploy" +
            " JOIN (SELECT ID, VERSION_ID FROM AF_REPOSITORY WHERE IS_FORKED = ? AND GIT_USER_ID = ? OR " +
            "(GIT_USER_ID IS NULL AND ? IS NULL)) AS repo ON deploy.REPOSITORY_ID = repo.ID JOIN " +
            "(SELECT ID, APPLICATION_ID FROM AF_VERSION WHERE VERSION_NAME = ?) AS version ON repo.VERSION_ID = version.ID" +
            " JOIN (SELECT ID FROM AF_APPLICATION WHERE APPLICATION_KEY=? AND TENANT_ID=?) AS app ON version.APPLICATION_ID = app.ID";

    public static final String GET_APPLICATION_LAST_BUILD_SQL =
            "SELECT LAST_BUILD_ID, LAST_BUILD_STATUS, LAST_BUILD_TIME FROM (SELECT LAST_BUILD_ID, LAST_BUILD_STATUS, REPOSITORY_ID, " +
            "LAST_BUILD_TIME FROM AF_BUILD) AS build JOIN (SELECT ID, VERSION_ID FROM AF_REPOSITORY WHERE IS_FORKED = ?" +
            " AND GIT_USER_ID = ? OR (GIT_USER_ID IS NULL AND ? IS NULL)) AS repo ON build.REPOSITORY_ID = repo.ID JOIN " +
            "(SELECT ID, APPLICATION_ID FROM AF_VERSION WHERE VERSION_NAME = ?) AS version ON repo.VERSION_ID = version.ID" +
            " JOIN (SELECT ID FROM AF_APPLICATION WHERE APPLICATION_KEY=? AND TENANT_ID=?) AS app ON version.APPLICATION_ID = app.ID";

    public static final String GET_ALL_VERSIONS_OF_APPLICATION =
            "SELECT * FROM ((SELECT ID,VERSION_NAME,APPLICATION_ID,STAGE, PROMOTE_STATUS,AUTO_BUILD,AUTO_DEPLOY,SUBDOMAIN " +
            "FROM AF_VERSION) AS VERSION JOIN (SELECT ID FROM AF_APPLICATION WHERE APPLICATION_KEY=? AND TENANT_ID = ?)" +
            " AS AF_APP ON AF_APP.ID=VERSION.APPLICATION_ID)";

    public static final String GET_APPLICATION_REPOSITORY_ID_SQL = "SELECT ID FROM " +
            "AF_REPOSITORY WHERE VERSION_ID=? AND IS_FORKED=0 AND GIT_USER_ID IS NULL";
    public static final String GET_ALL_APPLICATION_REPOSITORY_ID_SQL = "SELECT ID FROM " +
            "AF_REPOSITORY WHERE VERSION_ID IN (?)";
    public static final String GET_FORKED_APPLICATION_REPOSITORY_ID_SQL = "SELECT ID " +
            "FROM AF_REPOSITORY WHERE VERSION_ID=? AND IS_FORKED=1 AND GIT_USER_ID=?";
    public static final String GET_APPLICATION_CREATION_STATUS_SQL = "SELECT STATUS FROM " +
            "AF_APPLICATION WHERE APPLICATION_KEY=? AND TENANT_ID=?";

    // NOTE: we have put %s inside IN clause since place holders of the IN clause should be created dynamically.
    public static final String GET_APPLICATION_CREATION_STATUS_BY_APPKEYS_SQL = "SELECT APPLICATION_KEY ,STATUS FROM " +
            "AF_APPLICATION WHERE " +
            "TENANT_ID=? AND APPLICATION_KEY IN(%s) ";
    public static final String GET_APPLICATION_BRANCH_COUNT_SQL = "SELECT COUNT(*) AS " +
            "BRANCH_COUNT FROM AF_REPOSITORY REPO,AF_VERSION VERSION,AF_APPLICATION APP WHERE APP.APPLICATION_KEY=? " +
            "AND APP.TENANT_ID=? AND VERSION.APPLICATION_ID=APP.ID AND REPO.VERSION_ID=VERSION.ID AND REPO.IS_FORKED=?";
    public static final String GET_RESOURCES_BY_TYPE_AND_ENV = "SELECT RESOURCE.RESOURCE_NAME, " +
            "RESOURCE.DESCRIPTION FROM AF_RESOURCE RESOURCE,AF_APPLICATION APP WHERE " +
            "APP.APPLICATION_KEY=? AND APP.TENANT_ID=? AND RESOURCE.RESOURCE_TYPE=? AND " +
            "RESOURCE.ENVIRONMENT=? AND APP.ID=RESOURCE.APPLICATION_ID";
    public static final String GET_RESOURCES_BY_NAME_AND_TYPE_AND_ENV = "SELECT RESOURCE" +
            ".RESOURCE_NAME" +
            " FROM " +
            "AF_RESOURCE RESOURCE,AF_APPLICATION APP " +
            "WHERE APP.APPLICATION_KEY=? AND APP.TENANT_ID=? " +
            "AND RESOURCE.RESOURCE_TYPE=? AND APP.ID=RESOURCE.APPLICATION_ID " +
            "AND RESOURCE.ENVIRONMENT=? AND RESOURCE.RESOURCE_NAME=?";
    public static final String GET_DATABASE_BY_NAME_AND_ENVIRONMENT = "SELECT RESOURCE.RESOURCE_NAME FROM AF_RESOURCE" +
            " RESOURCE WHERE RESOURCE.RESOURCE_TYPE=? AND RESOURCE.ENVIRONMENT=? AND RESOURCE.RESOURCE_NAME=?";
    public static final String GET_ALL_APPLICATION_VERSION_ID="SELECT ID FROM AF_VERSION WHERE " +
            "APPLICATION_ID=?";
    public static final String GET_CARTRIDGE_CLUSTER_BY_CLUSTER_ID_SQL = "SELECT CLUSTER_ID, LB_CLUSTER_ID, " +
            "ACTIVE_IP FROM AF_CARTRIDGE_CLUSTER WHERE CLUSTER_ID=?";

    //SQL to check whether there is an application with the given applicationName and tenantId
    public static final String IS_APPLICATION_NAME_EXISTS = "SELECT APPLICATION_NAME FROM AF_APPLICATION WHERE " +
                                                            "APPLICATION_NAME = ? AND TENANT_ID = ?";
    //SQL to get the application name of the given application key
    public static final String GET_APPLICATION_NAME_SQL = "SELECT APPLICATION_NAME FROM AF_APPLICATION WHERE " +
                                                          "APPLICATION_KEY = ? AND TENANT_ID = ?";

    public static final String GET_ALL_VERSION_NAMES_OF_APPLICATION = "SELECT VERSION_NAME FROM AF_VERSION JOIN " +
            "(SELECT ID FROM AF_APPLICATION WHERE APPLICATION_KEY = ? AND TENANT_ID = ? ) AS AF_APP ON AF_APP.ID=AF_VERSION.APPLICATION_ID";


    //**************************************************Delete Queries
    public static final String DELETE_APPLICATION_REPOSITORY_SQL = "DELETE FROM AF_REPOSITORY " +
            "WHERE ID=?";
    public static final String DELETE_BUILD_STATUS_SQL ="DELETE FROM AF_BUILD WHERE " +
            "REPOSITORY_ID=?";
    public static final String DELETE_DEPLOY_STATUS_SQL = "DELETE FROM AF_DEPLOY WHERE " +
            "REPOSITORY_ID=?";
    public static final String DELETE_APPLICATION_SQL = "DELETE FROM AF_APPLICATION WHERE " +
            "APPLICATION_KEY=? AND TENANT_ID=?";
    public static final String DELETE_APPLICATION_VERSION = "DELETE FROM AF_VERSION WHERE ID=?";
    public static final String DELETE_RESOURCE_SQL = "DELETE FROM AF_RESOURCE " +
            "WHERE APPLICATION_ID=? AND RESOURCE_NAME=? AND RESOURCE_TYPE=? AND ENVIRONMENT=?";
    public static final String DELETE_ALL_RESOURCES_SQL = "DELETE FROM AF_RESOURCE WHERE " +
            "APPLICATION_ID=?";


    //**************************************************Update Queries
    public static final String UPDATE_LAST_BUILD_STATUS_SQL = "UPDATE AF_BUILD SET " +
            "LAST_BUILD_ID=?,LAST_BUILD_STATUS=?,LAST_BUILD_TIME=? WHERE REPOSITORY_ID=?";
    public static final String UPDATE_DEPLOYED_BUILD_ID_IN_DEPLOY_STATUS_SQL = "UPDATE AF_DEPLOY SET " +
            "LAST_DEPLOYED_BUILD_ID=? WHERE REPOSITORY_ID=? AND ENVIRONMENT=?";
    public static final String UPDATE_DEPLOY_STATUS_SQL = "UPDATE AF_DEPLOY SET " +
            "LAST_DEPLOY_STATUS=?,LAST_DEPLOY_TIME=? WHERE REPOSITORY_ID=? AND ENVIRONMENT=?";
    public static final String UPDATE_PROMOTE_STATUS_OF_VERSION = "UPDATE AF_VERSION SET " +
            "PROMOTE_STATUS=? WHERE APPLICATION_ID=? AND VERSION_NAME=?";
    public static final String UPDATE_STAGE_OF_VERSION = "UPDATE AF_VERSION SET " +
            "STAGE=? WHERE APPLICATION_ID=? AND VERSION_NAME=?";
    public static final String UPDATE_AUTO_BUILD_STATUS_OF_VERSION = "UPDATE AF_VERSION SET " +
            "AUTO_BUILD=? WHERE APPLICATION_ID=? AND VERSION_NAME=?";
    public static final String UPDATE_AUTO_DEPLOY_STATUS_OF_VERSION = "UPDATE AF_VERSION SET " +
            "AUTO_DEPLOY=? WHERE APPLICATION_ID=? AND VERSION_NAME=?";
    public static final String UPDATE_SUBDOMAIN_OF_VERSION = "UPDATE AF_VERSION SET " +
            "SUBDOMAIN=? WHERE APPLICATION_ID=? AND VERSION_NAME=?";
    public static final String UPDATE_APPLICATION_CREATION_STATUS_SQL = "UPDATE " +
            "AF_APPLICATION SET STATUS=? WHERE APPLICATION_KEY=? AND TENANT_ID=?";
    public static final String UPDATE_CARTRIDGE_CLUSTER_SQL = "UPDATE AF_CARTRIDGE_CLUSTER SET LB_CLUSTER_ID=?, " +
            "ACTIVE_IP=? WHERE CLUSTER_ID=?";
    public static final String UPDATE_RESOURCE = "UPDATE AF_RESOURCE SET DESCRIPTION =? WHERE APPLICATION_ID = " +
            "(SELECT ID FROM AF_APPLICATION WHERE APPLICATION_KEY=? AND TENANT_ID=?) AND RESOURCE_TYPE=? AND" +
            " RESOURCE_NAME=? AND ENVIRONMENT=? AND TENANT_ID=?";
}
