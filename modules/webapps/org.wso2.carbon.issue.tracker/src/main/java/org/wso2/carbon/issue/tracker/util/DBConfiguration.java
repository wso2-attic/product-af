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
package org.wso2.carbon.issue.tracker.util;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;
import org.apache.tomcat.jdbc.pool.DataSource;

/**
 * Represents Database configuration details
 */
public class DBConfiguration {
    static Logger log = Logger.getLogger(DBConfiguration.class);
    private static DataSource dataSource = null;

    private static void Initialize() {
        // lookup for datasource and intialize datasource var
        dataSource = lookupDataSource("jdbc/IssueTrackerDB");

    }

    /**
     * Lookup the data source, this datasource must be declared in the
     * master-datasources.xml
     *
     * @param dataSourceName - name of the datasource
     * @return
     */
    private static DataSource lookupDataSource(String dataSourceName) {
        try {
            Context initCtx = new InitialContext();

            return (DataSource) initCtx.lookup(dataSourceName);
        } catch (Exception e) {
            throw new RuntimeException("Error in looking up data source: "
                    + e.getMessage(), e);
        }
    }

    /**
     * Returns the db connection for the datasource
     *
     * @return
     */
    public static Connection getDBConnection() {

        if (dataSource == null) {
            Initialize();
        }
        return doGetConnection();
    }

    private static Connection doGetConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            String msg = "Error while getting Connection for Datasource "
                    + dataSource.getName();
            log.error(msg + " " + e.getMessage());
        }
        return null;
    }

}
