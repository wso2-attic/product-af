/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */
package org.wso2.carbon.appfactory.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

import javax.sql.DataSource;
import java.io.File;

/**
 * Database creator for populating AF DB at start up and for unit test
 */
public class AFDatabaseCreator extends DatabaseCreator {
    private static final Log log = LogFactory.getLog(AFDatabaseCreator.class);
    private DataSource dataSource;

    public AFDatabaseCreator(DataSource dataSource) {
        super(dataSource);
        this.dataSource = dataSource;
    }

    @Override
    public void createRegistryDatabase() throws Exception {
        String dBType = DatabaseCreator.getDatabaseType(this.dataSource.getConnection());
        String dBScriptLocation = getDbScriptLocation(dBType);
        File dBScriptFile = new File(dBScriptLocation);
        if (dBScriptFile.exists()) {
            super.createRegistryDatabase();
            if (log.isDebugEnabled()) {
                log.debug("Populating database from " + dBScriptLocation);
            }
        } else {
            log.warn("Could not found DB script in " + dBScriptLocation + " and skipping DB creation");
        }
    }

    @Override
    protected String getDbScriptLocation(String databaseType) {
        String dBScriptName = databaseType.concat(".sql");
        String carbonHome = System.getProperty("carbon.home");

        return carbonHome + (File.separator + "dbscripts" + File.separator + "appfactory" + File
                .separator + dBScriptName);
    }
}
