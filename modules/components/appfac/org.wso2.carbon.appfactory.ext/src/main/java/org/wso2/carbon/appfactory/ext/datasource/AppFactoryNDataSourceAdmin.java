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

import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.CarbonDataSource;
import org.wso2.carbon.ndatasource.core.DataSourceInfo;
import org.wso2.carbon.ndatasource.core.DataSourceManager;
import org.wso2.carbon.ndatasource.core.services.WSDataSourceInfo;
import org.wso2.carbon.ndatasource.core.services.WSDataSourceMetaInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin Service to manage datasource in application aware manner
 */
public class AppFactoryNDataSourceAdmin extends AbstractAdmin {

    @SuppressWarnings("UnusedDeclaration")
    public WSDataSourceInfo[] getAllDataSources(String applicationID) throws DataSourceException {
        List<WSDataSourceInfo> result = new ArrayList<WSDataSourceInfo>();
        for (CarbonDataSource cds : getDataSourceRepository().getAllDataSources(applicationID)) {
            result.add(new WSDataSourceInfo(new DataSourceInfo(
                    cds.getDSMInfo(), cds.getDSStatus())));
        }
        return result.toArray(new WSDataSourceInfo[result.size()]);
    }

    @SuppressWarnings("UnusedDeclaration")
    public WSDataSourceInfo getDataSource(String dsName, String applicationID) throws
            DataSourceException {
        CarbonDataSource cds = getDataSourceRepository().getDataSource(dsName, applicationID);
        if (cds == null) {
            return null;
        }
        return new WSDataSourceInfo(new DataSourceInfo(cds.getDSMInfo(), cds.getDSStatus()));
    }

    @SuppressWarnings("UnusedDeclaration")
    public WSDataSourceInfo[] getAllDataSourcesForType(String dsType,
                                                       String applicationID) throws
            DataSourceException {
        List<WSDataSourceInfo> result = new ArrayList<WSDataSourceInfo>();
        for (CarbonDataSource cds : getDataSourceRepository().getAllDataSources(applicationID)) {
            if (dsType.equals(cds.getDSMInfo().getDefinition().getType())) {
                result.add(new WSDataSourceInfo(
                        new DataSourceInfo(cds.getDSMInfo(), cds.getDSStatus())));
            }
        }
        return result.toArray(new WSDataSourceInfo[result.size()]);
    }

    @SuppressWarnings("UnusedDeclaration")
    public String[] getDataSourceTypes() throws DataSourceException {
        List<String> dsTypes = DataSourceManager.getInstance().getDataSourceTypes();
        return dsTypes.toArray(new String[dsTypes.size()]);
    }

    @SuppressWarnings("UnusedDeclaration")
    public boolean reloadAllDataSources() throws DataSourceException {
        getDataSourceRepository().refreshAllUserDataSources
                ();
        return true;
    }

    @SuppressWarnings("UnusedDeclaration")
    public boolean reloadDataSource(String dsName, String applicationID) throws
            DataSourceException {
        getDataSourceRepository().refreshUserDataSource(dsName,
                applicationID);
        return true;
    }

    @SuppressWarnings("UnusedDeclaration")
    public boolean addDataSource(WSDataSourceMetaInfo dsmInfo,
                                 String applicationID) throws DataSourceException {
        getDataSourceRepository().addDataSource(
                dsmInfo.extractDataSourceMetaInfo(), applicationID);
        return true;
    }

    @SuppressWarnings("UnusedDeclaration")
    public boolean deleteDataSource(String dsName, String applicationID) throws
            DataSourceException {
        getDataSourceRepository().deleteDataSource(dsName, applicationID);
        return true;
    }

    @SuppressWarnings("UnusedDeclaration")
    public boolean testDataSourceConnection(WSDataSourceMetaInfo dsmInfo) throws DataSourceException {
        return getDataSourceRepository().
                testDataSourceConnection(dsmInfo.extractDataSourceMetaInfo());
    }

    public static ApplicationAwareDataSourceRepository getDataSourceRepository() throws DataSourceException {

        return ((ApplicationAwareDataSourceRepository) DataSourceManager.getInstance()
                .getDataSourceRepository());
    }
}
