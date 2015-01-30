/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.appfactory.ext.datasource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.CarbonDataSource;
import org.wso2.carbon.ndatasource.core.DataSourceInfo;
import org.wso2.carbon.ndatasource.core.DataSourceManager;
import org.wso2.carbon.ndatasource.core.services.WSDataSourceInfo;
import org.wso2.carbon.ndatasource.core.services.WSDataSourceMetaInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Admin Service to manage datasource in application aware manner
 */
public class AppFactoryNDataSourceAdmin extends AbstractAdmin {
    private static Log log = LogFactory.getLog(AppFactoryNDataSourceAdmin.class);

    @SuppressWarnings("UnusedDeclaration")
    public WSDataSourceInfo[] getAllDataSources(String applicationID) throws DataSourceException {
        try {
            List<WSDataSourceInfo> result = new ArrayList<WSDataSourceInfo>();
            for (CarbonDataSource cds : getDataSourceRepository().getAllDataSources(applicationID)) {
                result.add(new WSDataSourceInfo(new DataSourceInfo(
                        cds.getDSMInfo(), cds.getDSStatus())));
            }
            return result.toArray(new WSDataSourceInfo[result.size()]);

        } catch (DataSourceException e) {
            log.error(e);
            throw e;
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void deleteAllDataSources(String applicationID) throws DataSourceException {
        Collection<CarbonDataSource> allDataSources;
        String failedDataSources = "";  // this is used to track deletion failed data sources

        if (log.isDebugEnabled()) {
            log.debug("Deleting all resources of application: " + applicationID);
        }

        // Getting all data sources associated with the application
        try {
            allDataSources = getDataSourceRepository().getAllDataSources(applicationID);
        } catch (DataSourceException e) {
            String errMsg = "Error occurred while getting data sources for application: " + applicationID;
            log.error(errMsg, e);
            throw new DataSourceException(errMsg, e);
        }

        // delete data sources
        if (allDataSources != null) {
            // Here we are adding names of the all the data sources to an ArrayList since, if we iterate through the
            // allDataSources and call getDataSourceRepository().deleteDataSource(),
            // we will do Map modification while iterating the same map, which is not allowed.
            // TODO:refactor the code to allow delete the datasources while iterate through the all allDataSources
            // collection
            List<String> dsNames = new ArrayList<String>();
            for (CarbonDataSource dataSource : allDataSources) {
                dsNames.add(dataSource.getDSMInfo().getName());
            }

            for (String dsName : dsNames) {
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Deleting data source: "+dsName+" application: " + applicationID);
                    }
                    getDataSourceRepository().deleteDataSource(dsName, applicationID);
                    log.info("Deleted data source: "+dsName+" from application: " + applicationID);
                } catch (DataSourceException e) {
                    // we are not throwing an exception here since we should try to delete other resources
                    log.error("Error occurred while deleting data source: " + dsName + " from application: " +
                               applicationID, e);
                    failedDataSources = failedDataSources + dsName + ", ";
                }
            }// end of for loop

        } else {
            log.info("There currently there are no datasources associated with application: "+applicationID);
        }

        if (!failedDataSources.isEmpty()) {
            String errMsg = "Error occurred while deleting data sources: " + failedDataSources +
                            " for application: " + applicationID;
            log.error(errMsg);
            throw new DataSourceException(errMsg);
        } else {
            log.info("Successfully deleted all the data sources of application: " + applicationID);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public WSDataSourceInfo getDataSource(String dsName, String applicationID) throws
            DataSourceException {
        try {
            CarbonDataSource cds = getDataSourceRepository().getDataSource(dsName, applicationID);
            if (cds == null) {
                return null;
            }
            return new WSDataSourceInfo(new DataSourceInfo(cds.getDSMInfo(), cds.getDSStatus()));
        } catch (DataSourceException e) {
            log.error(e);
            throw e;
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public WSDataSourceInfo[] getAllDataSourcesForType(String dsType,
                                                       String applicationID) throws
            DataSourceException {
        try {
            List<WSDataSourceInfo> result = new ArrayList<WSDataSourceInfo>();
            for (CarbonDataSource cds : getDataSourceRepository().getAllDataSources(applicationID)) {
                if (dsType.equals(cds.getDSMInfo().getDefinition().getType())) {
                    result.add(new WSDataSourceInfo(
                            new DataSourceInfo(cds.getDSMInfo(), cds.getDSStatus())));
                }
            }
            return result.toArray(new WSDataSourceInfo[result.size()]);
        } catch (DataSourceException e) {
            log.error(e);
            throw e;
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public String[] getDataSourceTypes() throws DataSourceException {
        try {
            List<String> dsTypes = DataSourceManager.getInstance().getDataSourceTypes();
            return dsTypes.toArray(new String[dsTypes.size()]);
        } catch (DataSourceException e) {
            log.error(e);
            throw e;
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public boolean reloadAllDataSources() throws DataSourceException {
        try {
            getDataSourceRepository().refreshAllUserDataSources
                    ();
            return true;
        } catch (DataSourceException e) {
            log.error(e);
            throw e;
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public boolean reloadDataSource(String dsName, String applicationID) throws
            DataSourceException {
        try {
            getDataSourceRepository().refreshUserDataSource(dsName,
                    applicationID);
            return true;
        } catch (DataSourceException e) {
            log.error(e);
            throw e;
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public boolean addDataSource(WSDataSourceMetaInfo dsmInfo,
                                 String applicationID) throws DataSourceException {
        try {
            getDataSourceRepository().addDataSource(
                    dsmInfo.extractDataSourceMetaInfo(), applicationID);
            return true;
        } catch (DataSourceException e) {
            log.error(e);
            throw e;
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public boolean deleteDataSource(String dsName, String applicationID) throws
            DataSourceException {
        try {
            getDataSourceRepository().deleteDataSource(dsName, applicationID);
            return true;
        } catch (DataSourceException e) {
            log.error(e);
            throw e;
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public boolean testDataSourceConnection(WSDataSourceMetaInfo dsmInfo) throws DataSourceException {
        try {
            return getDataSourceRepository().
                    testDataSourceConnection(dsmInfo.extractDataSourceMetaInfo());
        } catch (DataSourceException e) {
            log.error(e);
            throw e;
        }
    }

    public static ApplicationAwareDataSourceRepository getDataSourceRepository() throws DataSourceException {
        try {
            return ((ApplicationAwareDataSourceRepository) DataSourceManager.getInstance()
                    .getDataSourceRepository());
        } catch (DataSourceException e) {
            log.error(e);
            throw e;
        }
    }
}
