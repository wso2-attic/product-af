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

import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.appfactory.ext.internal.ServiceHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ndatasource.common.DataSourceConstants;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.common.spi.DataSourceReader;
import org.wso2.carbon.ndatasource.core.*;
import org.wso2.carbon.ndatasource.core.utils.DataSourceUtils;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.exceptions.ResourceNotFoundException;
import org.wso2.carbon.utils.ConfigurationContextService;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a  {@link DataSourceRepository} implementation that keeps
 * the dataSource information per application,initialize all the dataSources from all applications
 * and if there is a change in dataSource of application in one node ,
 * it will send a cluster message to notify other nodes.
 * To enable this DataSourceRepository add following to carbon.xml
 * <CarbonDataSourceRepository>org.wso2.carbon.appfactory.ext.datasource.ApplicationAwareDataSourceRepository</CarbonDataSourceRepository>
 */
public class ApplicationAwareDataSourceRepository extends DataSourceRepository {
    public static final String DATA_SOURCES_REPOSITORY_BASE_PATH = "/repository/components/org" +
            ".wso2.carbon.appfactory.ext.datasource";
    public static final String AF_DUMMY_VERSION = "-1.1.1";
    private static Log log = LogFactory.getLog(DataSourceRepository.class);
    private Registry registry;

    private Map<String, Map<String, CarbonDataSource>> dataSources;
    private Marshaller dsmMarshaller;

    private Unmarshaller dsmUnMarshaller;

    public Marshaller getDsmMarshaller() {
        return dsmMarshaller;
    }

    public Unmarshaller getDsmUnMarshaller() {
        return dsmUnMarshaller;
    }

    public ApplicationAwareDataSourceRepository(int tenantId) throws DataSourceException {
        super(tenantId);
       /* Map to hold map of datasources of application*/
        this.dataSources = new HashMap<String, Map<String, CarbonDataSource>>();
        try {
            JAXBContext ctx = JAXBContext.newInstance(DataSourceMetaInfo.class);
            this.dsmMarshaller = ctx.createMarshaller();
            this.dsmUnMarshaller = ctx.createUnmarshaller();
        } catch (JAXBException e) {
            throw new DataSourceException(
                    "Error creating data source meta info marshaller/unmarshaller: "
                            + e.getMessage(), e);
        }
    }


    @Override
    public void initRepository() throws DataSourceException {
        super.initRepository();
        this.refreshAllUserDataSources();
    }

    @Override
    public void refreshAllUserDataSources() throws DataSourceException {
        super.refreshAllUserDataSources();
        this.updateAllUserDataSources(false);
    }

    private void updateAllUserDataSources(boolean unregister) throws DataSourceException {
        try {
            if (getRegistry().resourceExists(
                    DATA_SOURCES_REPOSITORY_BASE_PATH)) {
                org.wso2.carbon.registry.api.Collection dsCollection = (org.wso2.carbon.registry
                        .api.Collection) this.getRegistry().get(
                        DATA_SOURCES_REPOSITORY_BASE_PATH);
                String[] dsmPaths = dsCollection.getChildren();
                //application collection
                for (String dsmPath : dsmPaths) {
                    try {
                        this.updateApplicationDataSource(dsmPath);
                    } catch (DataSourceException e) {
                        log.error("Error in updating data source [remove:" + unregister +
                                "] at path '" + dsmPath + "': " + e.getMessage(), e);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataSourceException(
                    "Error in getting all data sources from repository: " + e.getMessage(), e);
        }
    }

    private void updateApplicationDataSource(String path) throws DataSourceException {
        String applicationID = this.getApplicationIDFromPath(path);
        org.wso2.carbon.registry.api.Collection dsCollection;
        try {
            dsCollection = (org.wso2.carbon.registry.api.Collection) getRegistry().get(path);
        } catch (RegistryException e) {
            String msg = "Error in getting data source [application:" + applicationID +
                    "] at path '" + path + "': " + e.getMessage();
            log.error(msg, e);
            throw new DataSourceException(msg, e);
        }
        String[] dsmPaths;
        try {
            //collection of ds config of an application
            dsmPaths = dsCollection.getChildren();
            for (String dsmPath : dsmPaths) {
                try {
                    this.updateDataSource(resourceNameFromPath(dsmPath), applicationID, false);
                } catch (DataSourceException e) {
                    log.error("Error in updating data source [application:" + applicationID +
                            "] at path '" + dsmPath + "': " + e.getMessage(), e);
                }
            }
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            String msg = "Error while getting application Collection at " + path;
            log.error(msg, e);
        }

    }

    private String getApplicationIDFromPath(String path) {
        return path.substring(path.lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1);
    }

    @Override
    public void unregisterAllUserDataSources() throws DataSourceException {
        this.updateAllUserDataSources(true);
    }


    public void refreshUserDataSource(String dsName, String applicationID) throws
            DataSourceException {
        if (log.isDebugEnabled()) {
            log.debug("Refreshing data source: " + dsName);
        }
        this.updateDataSource(dsName, applicationID, false);
    }


    public Collection<CarbonDataSource> getAllDataSources(String applicationID) {
        return getApplicationDatasourceMap(applicationID).values();
    }


    public CarbonDataSource getDataSource(String dsName, String applicationID) {
        return getApplicationDatasourceMap(applicationID).get(dsName);
    }


    public void addDataSource(DataSourceMetaInfo dsmInfo,
                              String applicationID) throws DataSourceException {
        if (log.isDebugEnabled()) {
            log.debug("Adding data source: " + dsmInfo.getName());
        }
        if (!dsmInfo.isSystem()) {
            this.persistDataSource(dsmInfo, applicationID);
        }
        this.registerDataSource(dsmInfo, applicationID);
        if (!dsmInfo.isSystem()) {
            this.notifyClusterDSChange(dsmInfo.getName(), applicationID);
        }

    }


    public void deleteDataSource(String dsName, String applicationID) throws DataSourceException {
        if (log.isDebugEnabled()) {
            log.debug("Deleting data source: " + dsName);
        }
        CarbonDataSource cds = this.getDataSource(dsName, applicationID);
        if (cds == null) {
            throw new DataSourceException("Data source does not exist: " + dsName);
        }
        if (cds.getDSMInfo().isSystem()) {
            throw new DataSourceException("System data sources cannot be deleted: " + dsName);
        }
        this.removePersistedDataSource(dsName, applicationID);
        this.unregisterDataSource(dsName, applicationID);
        this.notifyClusterDSChange(dsName, applicationID);
    }

    @Override
    public boolean testDataSourceConnection(DataSourceMetaInfo dsmInfo) throws DataSourceException {
        return super.testDataSourceConnection(dsmInfo);
    }

    private String resourceNameFromPath(String path) {
        return path.substring(path.lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1);
    }

    private synchronized void updateDataSource(String dsName, String appllicationID,
                                               boolean unregister) throws DataSourceException {
        String dsmPath = DATA_SOURCES_REPOSITORY_BASE_PATH + RegistryConstants.PATH_SEPARATOR +
                appllicationID + RegistryConstants.PATH_SEPARATOR
                + dsName;
        try {
            DataSourceMetaInfo dsmInfo = this.getDataSourceMetaInfoFromRegistryPath(dsmPath);
            CarbonDataSource currentCDS = this.getDataSource(dsName, appllicationID);
            DataSourceMetaInfo currentDsmInfo = null;
            if (currentCDS != null) {
                currentDsmInfo = currentCDS.getDSMInfo();
            }
            if (unregister) {
                this.unregisterDataSource(dsName, appllicationID);
            } else {
                if (DataSourceUtils.nullAllowEquals(dsmInfo, currentDsmInfo)) {
                    if (log.isDebugEnabled()) {
                        log.debug("No update change for data source: " + dsName);
                    }
                    return;
                }
                if (dsmInfo != null) {
                    this.registerDataSource(dsmInfo, appllicationID);
                } else {
                    this.unregisterDataSource(dsName, appllicationID);
                }
            }
        } catch (Exception e) {
            throw new DataSourceException("Error in updating data source '" + dsName +
                    "' from registry [remove:" + unregister + "]: " + e.getMessage(), e);
        }
    }

    /**
     * registering as JND resource and putting into in-memory map
     *
     * @param dsmInfo       data source information
     * @param applicationID application key
     * @throws DataSourceException
     */
    private synchronized void registerDataSource(DataSourceMetaInfo dsmInfo, String applicationID
    ) throws DataSourceException {
        /* if a data source is already registered with the given name, unregister it first */
        CarbonDataSource currentCDS = this.getDataSource(dsmInfo.getName());
        if (currentCDS != null) {
            /* if the data source is a system data source, throw exception */
            if (dsmInfo.isSystem()) {
                throw new DataSourceException("System datasource " + dsmInfo.getName() + "can not be updated.");
            }
            this.unregisterDataSource(currentCDS.getDSMInfo().getName(), applicationID);
        }
        if (log.isDebugEnabled()) {
            log.debug("Registering data source: " + dsmInfo.getName());
        }
        Object dsObject = null;
        boolean isDataSourceFactoryReference = false;
        DataSourceStatus dsStatus;
        try {
            JNDIConfig jndiConfig = dsmInfo.getJndiConfig();
            if (jndiConfig != null) {
                isDataSourceFactoryReference = jndiConfig.isUseDataSourceFactory();
            }
            dsObject = this.createDataSourceObject(dsmInfo, isDataSourceFactoryReference);
            this.registerJNDI(dsmInfo, dsObject, applicationID);
            dsStatus = new DataSourceStatus(DataSourceConstants.DataSourceStatusModes.ACTIVE, null);
        } catch (Exception e) {
            String msg = e.getMessage();
            log.error(msg, e);
            dsStatus = new DataSourceStatus(DataSourceConstants.DataSourceStatusModes.ERROR, msg);
        }
        /* Creating DataSource object , if dsObject is a Reference */
        if (isDataSourceFactoryReference) {
            dsObject = this.createDataSourceObject(dsmInfo, false);
        }
        CarbonDataSource cds = new CarbonDataSource(dsmInfo, dsStatus, dsObject);
        this.getApplicationDatasourceMap(applicationID).put(cds.getDSMInfo().getName(), cds);
    }

    private void registerJNDI(DataSourceMetaInfo dsmInfo, Object dsObject, String applicationID)
            throws DataSourceException {
        String initialApplicationName = PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getApplicationName();
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(this.getTenantId());
          /*  Have to add dummy version to comply with AF app naming rule */
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setApplicationName
                    (applicationID + AF_DUMMY_VERSION);
            JNDIConfig jndiConfig = dsmInfo.getJndiConfig();
            if (jndiConfig == null) {
                return;
            }
            InitialContext context;
            try {
                context = new InitialContext(jndiConfig.extractHashtableEnv());
            } catch (NamingException e) {
                throw new DataSourceException("Error creating JNDI initial context: " +
                        e.getMessage(), e);
            }
            this.checkAndCreateJNDISubContexts(context, jndiConfig.getName());

            try {
                context.rebind(jndiConfig.getName(), dsObject);
            } catch (NamingException e) {
                throw new DataSourceException("Error in binding to JNDI with name '" +
                        jndiConfig.getName() + "' - " + e.getMessage(), e);
            }
        } finally {
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setApplicationName
                    (initialApplicationName);
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private void unregisterJNDI(DataSourceMetaInfo dsmInfo, String applicationID) {
        String initialApplicationName = PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getApplicationName();
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(this.getTenantId());
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setApplicationName
                    (applicationID + AF_DUMMY_VERSION);
            JNDIConfig jndiConfig = dsmInfo.getJndiConfig();
            if (jndiConfig == null) {
                return;
            }
            try {
                InitialContext context = new InitialContext(jndiConfig.extractHashtableEnv());
                context.unbind(jndiConfig.getName());
            } catch (NamingException e) {
                log.error("Error in unregistering JNDI name: " +
                        jndiConfig.getName() + " - " + e.getMessage(), e);
            }
        } finally {
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setApplicationName
                    (initialApplicationName);
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private void unregisterDataSource(String dsName, String applicationID) {
        CarbonDataSource cds = this.getDataSource(dsName, applicationID);
        if (cds == null) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Unregistering data source: " + dsName);
        }
        this.unregisterJNDI(cds.getDSMInfo(), applicationID);
        this.getApplicationDatasourceMap(applicationID).remove(dsName);
    }

    private Map<String, CarbonDataSource> getApplicationDatasourceMap(String applicationID) {
        Map<String, CarbonDataSource> applicationDatasourceMap = dataSources.get(applicationID);
        if (applicationDatasourceMap == null) {
            applicationDatasourceMap = new HashMap<String, CarbonDataSource>();
            dataSources.put(applicationID, applicationDatasourceMap);
        }
        return applicationDatasourceMap;
    }

    private void removePersistedDataSource(String dsName,
                                           String applicationID) throws DataSourceException {
        try {
            this.getRegistry().beginTransaction();
            String path = DATA_SOURCES_REPOSITORY_BASE_PATH + RegistryConstants.PATH_SEPARATOR + applicationID + RegistryConstants.PATH_SEPARATOR +
                    dsName;
            if (this.getRegistry().resourceExists(path)) {
                this.getRegistry().delete(path);
            }
            this.getRegistry().commitTransaction();
        } catch (Exception e) {
            try {
                this.getRegistry().rollbackTransaction();
            } catch (RegistryException e1) {
                log.error("Error in rollback transaction in removing data source:" +
                        e1.getMessage(), e1);
            }
            throw new DataSourceException("Error in removing data source: " + dsName +
                    " - " + e.getMessage(), e);
        }
    }

    private void persistDataSource(DataSourceMetaInfo dsmInfo,
                                   String applicationID) throws DataSourceException {
        try {
            Element element = DataSourceUtils.
                    convertDataSourceMetaInfoToElement(dsmInfo, this.getDsmMarshaller());
            DataSourceUtils.secureSaveElement(element);

            Resource resource = this.getRegistry().newResource();
            resource.setContentStream(DataSourceUtils.elementToInputStream(element));
            this.getRegistry().put(DATA_SOURCES_REPOSITORY_BASE_PATH +
                    RegistryConstants.PATH_SEPARATOR + applicationID + RegistryConstants.PATH_SEPARATOR +
                    dsmInfo.getName(), resource);
        } catch (Exception e) {
            throw new DataSourceException("Error in persisting data source: " +
                    dsmInfo.getName() + " - " + e.getMessage(), e);
        }
    }

    private void notifyClusterDSChange(String dsName, String applicationID) throws
            DataSourceException {
        if (log.isDebugEnabled()) {
            log.debug("Notifying cluster DS change: " + dsName + " for " + applicationID);
        }
        ConfigurationContextService configCtxService = ServiceHolder.getInstance().getConfigContextService();
        if (configCtxService == null) {
            throw new DataSourceException("ConfigurationContextService not available " +
                    "for notifying the cluster");
        }
        ConfigurationContext configCtx = configCtxService.getServerConfigContext();
        ClusteringAgent agent = configCtx.getAxisConfiguration().getClusteringAgent();
        if (log.isDebugEnabled()) {
            log.debug("Clustering Agent: " + agent);
        }
        if (agent != null) {
            NDataSourceCLusterMessage msg = new NDataSourceCLusterMessage();
            msg.setTenantId(this.getTenantId());
            msg.setDsName(dsName);
            msg.setApplicationName(applicationID);
            try {
                agent.sendMessage(msg, true);
            } catch (ClusteringFault e) {
                throw new DataSourceException("Error in sending out cluster message: " +
                        e.getMessage(), e);
            }
        }
    }

    /*borrowed from DataSourceRepository*/
    private synchronized Registry getRegistry() throws DataSourceException {
        if (this.registry == null) {
            this.registry = DataSourceUtils.getConfRegistryForTenant(this.getTenantId());
            if (log.isDebugEnabled()) {
                log.debug("[datasources] Retrieving the governance registry for tenant: " +
                        this.getTenantId());
            }
        }
        return registry;
    }

    /*borrowed from DataSourceRepository*/
    private DataSourceMetaInfo getDataSourceMetaInfoFromRegistryPath(String path)
            throws Exception {
        InputStream in = null;
        try {
            this.getRegistry().beginTransaction();
            if (this.getRegistry().resourceExists(path)) {
                Resource resource;
                try {
                    resource = this.getRegistry().get(path);
                } catch (ResourceNotFoundException e) {
                    /* this step is as a precaution, because sometimes even though the
                     * resource is deleted, "resourceExists" returns true */
                    return null;
                }
                in = resource.getContentStream();
                Document doc = DataSourceUtils.convertToDocument(in);
                /* only super tenant will lookup secure vault information for system data sources,
			     * others are not allowed to */
                DataSourceUtils.secureResolveDocument(doc, false);
                this.getRegistry().commitTransaction();
                return (DataSourceMetaInfo) this.getDsmUnMarshaller().unmarshal(doc);
            } else {
                return null;
            }
        } catch (Exception e) {
            this.getRegistry().rollbackTransaction();
            throw e;
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /*borrowed from DataSourceRepository*/
    private Object createDataSourceObject(DataSourceMetaInfo dsmInfo, boolean isUseDataSourceFactory)
            throws DataSourceException {
        DataSourceReader dsReader = DataSourceManager.getInstance().getDataSourceReader(
                dsmInfo.getDefinition().getType());
        if (dsReader == null) {
            throw new DataSourceException("A data source reader cannot be found for the type '" +
                    dsmInfo.getDefinition().getType() + "'");
        }
		/* sets the current data source's (tenantId + ":" + name) as a thread local value
		 * so it can be read by data source readers */
        DataSourceUtils.setCurrentDataSourceId(this.getTenantId() + ":" + dsmInfo.getName());
        return dsReader.createDataSource(DataSourceUtils.elementToString(
                (Element) dsmInfo.getDefinition().getDsXMLConfiguration()), isUseDataSourceFactory);
    }

    /*borrowed from DataSourceRepository*/
    private void checkAndCreateJNDISubContexts(Context context, String jndiName)
            throws DataSourceException {
        String[] tokens = jndiName.split("/");
        Context tmpCtx;
        String token;
        for (int i = 0; i < tokens.length - 1; i++) {
            token = tokens[i];
            tmpCtx = this.lookupJNDISubContext(context, token);
            if (tmpCtx == null) {
                try {
                    tmpCtx = context.createSubcontext(token);
                } catch (NamingException e) {
                    throw new DataSourceException(
                            "Error in creating JNDI subcontext '" + context +
                                    "/" + token + ": " + e.getMessage(), e);
                }
            }
            context = tmpCtx;
        }
    }

    /*borrowed from DataSourceRepository*/
    private Context lookupJNDISubContext(Context context, String jndiName)
            throws DataSourceException {
        try {
            Object obj = context.lookup(jndiName);
            if (!(obj instanceof Context)) {
                throw new DataSourceException("Non JNDI context already exists at '" +
                        context + "/" + jndiName);
            }
            return (Context) obj;
        } catch (NamingException e) {
            return null;
        }
    }
}
