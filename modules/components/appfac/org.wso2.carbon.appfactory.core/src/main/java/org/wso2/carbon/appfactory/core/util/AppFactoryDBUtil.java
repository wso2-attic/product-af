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

package org.wso2.carbon.appfactory.core.util;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Contains use full methods for DB related operations
 */
public class AppFactoryDBUtil {
    private static final Log log = LogFactory.getLog(AppFactoryDBUtil.class);
    private static volatile DataSource dataSource;
    private static final String DB_CHECK_SQL = "SELECT * FROM AF_APPLICATION";

    public static DataSource getDataSource() {
        return dataSource;
    }

    public static void setDataSource(DataSource dataSource) {
        AppFactoryDBUtil.dataSource = dataSource;
    }


    public static void initializeDatasource() throws AppFactoryException {
        AppFactoryConfiguration configuration = ServiceHolder.getAppFactoryConfiguration();
        String datasourceName = configuration.getFirstProperty(AppFactoryConstants.DATASOURCE_NAME);
        if (datasourceName != null) {
            InitialContext context;
            try {
                context = new InitialContext();
            } catch (NamingException e) {
                String msg = "Could get JNDI initial context.Unable to get datasource for appfactory";
                log.error(msg, e);
                throw new AppFactoryException(msg, e);
            }
            try {
                AppFactoryDBUtil.dataSource = (DataSource) context.lookup(datasourceName);
            } catch (NamingException e) {
                String msg = "Could not found data source " + datasourceName + ".Please make sure the " +
                        "datasource is configured in appfactory.xml";
                log.error(msg, e);
                throw new AppFactoryException(msg, e);
            }
        } else {
            //This is only needed for unit test .
            DBConfiguration dbConfiguration;
            dbConfiguration = getDBConfig(configuration);
            String dbUrl = dbConfiguration.getDbUrl();
            String driver = dbConfiguration.getDriverName();
            String username = dbConfiguration.getUserName();
            String password = dbConfiguration.getPassword();
            if (dbUrl == null || driver == null || username == null || password == null) {
                log.warn("Required DB configuration parameters unspecified. So App Factory " +
                        "will not work as expected.");
            }

            BasicDataSource basicDataSource = new BasicDataSource();
            basicDataSource.setDriverClassName(driver);
            basicDataSource.setUrl(dbUrl);
            basicDataSource.setUsername(username);
            basicDataSource.setPassword(password);
            dataSource = basicDataSource;
        }
        setupDatabase();
    }

    private static void setupDatabase() throws AppFactoryException {
        if (System.getProperty("setup") != null) {
            AFDatabaseCreator databaseCreator = new AFDatabaseCreator(dataSource);
            if (!databaseCreator.isDatabaseStructureCreated(DB_CHECK_SQL)) {
                try {
                    databaseCreator.createRegistryDatabase();
                } catch (Exception e) {
                    String msg = "Could not populate database for appfactory";
                    log.error(msg, e);
                    throw new AppFactoryException(msg, e);
                }
            } else {
                log.warn("AppFactory database already exists.Skipping database creation");
            }

        }
    }

    private static DBConfiguration getDBConfig(AppFactoryConfiguration configuration) {
        DBConfiguration dbConfiguration = new DBConfiguration();
        dbConfiguration.setDbUrl(configuration.getFirstProperty(DBConfiguration.DB_URL));
        dbConfiguration.setDriverName(configuration.getFirstProperty(DBConfiguration.DB_DRIVER));
        dbConfiguration.setUserName(configuration.getFirstProperty(DBConfiguration.DB_USER));
        dbConfiguration.setPassword(configuration.getFirstProperty(DBConfiguration.DB_PASSWORD));
        return dbConfiguration;
    }

    private static class DBConfiguration {
        private static final String DB_DRIVER = AppFactoryConstants.DB_CONFIG + ".Driver";
        private static final String DB_URL = AppFactoryConstants.DB_CONFIG + ".URL";
        private static final String DB_USER = AppFactoryConstants.DB_CONFIG + ".Username";
        private static final String DB_PASSWORD = AppFactoryConstants.DB_CONFIG + ".Password";
        private String dbUrl;
        private String userName;
        private String password;
        private String driverName;

        public String getDbUrl() {
            return dbUrl;
        }

        public void setDbUrl(String dbUrl) {
            this.dbUrl = dbUrl;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getDriverName() {
            return driverName;
        }

        public void setDriverName(String driverName) {
            this.driverName = driverName;
        }
    }

    public static synchronized Connection getConnection() throws SQLException {
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        return connection;
    }

    /**
     * This method is used to close the database connection.
     * The intended use of this method is within a finally block.
     * This method will log any exceptions that is occurred while closing the database connection.
     *
     * @param connection The database connection which needs to be closed.
     */
    public static void closeConnection(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            // This method is called within a finally block. Hence we do not throw an error from here
            String msg = "Could not close db connection";
            log.error(msg, e);
        }
    }

    /**
     * This method is used to close the prepared statement.
     * The intended use of this method is within a finally block.
     * This method will log any exceptions that is occurred while closing the prepared statement.
     *
     * @param preparedStatement The prepared statement which need to be closed.
     */
    public static void closePreparedStatement(PreparedStatement preparedStatement) {
        try {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        } catch (SQLException e) {
            // This method is called within a finally block. Hence we do not throw an error from here
            String msg = "Could not close PreparedStatement";
            log.error(msg, e);
        }
    }

    /**
     * This method is used to close the database result set.
     * The intended use of this method is within a finally block.
     * This method will log any exceptions that is occurred while closing the database result set.
     *
     * @param resultSet The database result set that needs to be closed.
     */
    public static void closeResultSet(ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (SQLException e) {
            // This method is called within a finally block. Hence we do not throw an error from here
            String msg = "Could not close resultSet";
            log.error(msg, e);
        }
    }
}
