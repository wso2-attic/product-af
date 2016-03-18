/*
*  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.appfactory.core.dao;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.cache.JDBCResourceCacheManager;
import org.wso2.carbon.appfactory.core.dto.Resource;
import org.wso2.carbon.appfactory.core.sql.SQLConstants;
import org.wso2.carbon.appfactory.core.sql.SQLParameterConstants;
import org.wso2.carbon.appfactory.core.util.AppFactoryDBUtil;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.Container;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.DeploymentConfig;
import org.wso2.carbon.appfactory.provisioning.runtime.beans.ServiceProxy;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class for managing resource data
 */
public class JDBCResourceDAO {
    private static final Log log = LogFactory.getLog(JDBCResourceDAO.class);

    private static final String RESOURCE_NAME = "RESOURCE_NAME";
    private static final String DESCRIPTION = "DESCRIPTION";
    private static JDBCResourceDAO jdbcResourceDAO = new JDBCResourceDAO();

    private JDBCResourceDAO() {

    }

    public static JDBCResourceDAO getInstance() {
        return jdbcResourceDAO;
    }

    /**
     * Add resource to an application
     *
     * @param applicationKey application key
     * @param resourceName   application name
     * @param resourceType   resource type
     * @param environment    environment
     * @param description    description
     * @return true if resource added successfully
     * @throws AppFactoryException
     */
    public boolean addResource(String applicationKey, String resourceName, String resourceType,
                               String environment, String description) throws AppFactoryException {
        Connection databaseConnection = null;
        PreparedStatement preparedStatement = null;
        try {
            databaseConnection = AppFactoryDBUtil.getConnection();
            preparedStatement = databaseConnection.prepareStatement(SQLConstants.ADD_RESOURCE_SQL);
            int applicationId = JDBCApplicationDAO.getInstance().getAutoIncrementAppID(applicationKey,
                                                                                       databaseConnection);
            preparedStatement.setInt(1, applicationId);
            preparedStatement.setString(2, resourceName);
            preparedStatement.setString(3, resourceType);
            preparedStatement.setString(4, environment);
            preparedStatement.setString(5, description);
            preparedStatement.setInt(6, CarbonContext.getThreadLocalCarbonContext().getTenantId());
            preparedStatement.execute();
            int affectedRow = preparedStatement.getUpdateCount();
            if (affectedRow > 0) {
                databaseConnection.commit();

                // clear the cache
                JDBCResourceCacheManager.clearCache(applicationKey, environment, resourceType);
                if (log.isDebugEnabled()) {
                    log.debug("Cache cleared for resource type : " + resourceType + " of application key : " +
                              applicationKey + " in : " + environment);
                }
                return true;
            }

        } catch (SQLException e) {
            try {
                if (databaseConnection != null) {
                    databaseConnection.rollback();
                }
            } catch (SQLException e1) {
                String msg =
                        "Error while rolling back resource addition for : " + resourceName + " of resource type : " +
                        resourceType + " of application key : " + applicationKey + " in : " + environment;
                log.error(msg, e1);
            }
            String msg = "Error while adding resource : " + resourceName + " of resource type : " + resourceType +
                         " of application key : " + applicationKey + " in : " + environment;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } finally {
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }
        return false;
    }

    /**
     * Check whether the resource is exist
     *
     * @param applicationKey application key
     * @param resourceName   resource name
     * @param resourceType   resource type
     * @param environment    environment
     * @return true if resource is exist
     * @throws AppFactoryException
     */
    public boolean isResourceExists(String applicationKey, String resourceName, String resourceType,
                                    String environment) throws AppFactoryException {
        if (JDBCResourceCacheManager.isResourceExist(applicationKey, environment, resourceType, resourceName)) {
            return true;
        }
        Connection databaseConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resourcesRS = null;
        try {
            databaseConnection = AppFactoryDBUtil.getConnection();
            preparedStatement = databaseConnection
                    .prepareStatement(SQLConstants.GET_RESOURCES_BY_NAME_AND_TYPE_AND_ENV);
            preparedStatement.setString(1, applicationKey);
            preparedStatement.setInt(2, CarbonContext.getThreadLocalCarbonContext().getTenantId());
            preparedStatement.setString(3, resourceType);
            preparedStatement.setString(4, environment);
            preparedStatement.setString(5, resourceName);
            resourcesRS = preparedStatement.executeQuery();
            if (resourcesRS.next()) {

                //Here we are retrieving only a single resource from database. But we can add only a list of resources
                // of a particular resource type. So here we cant add the retrieved resource to the cache
                return true;
            }
        } catch (SQLException e) {
            String msg = "Error while checking availability of resource : " + resourceName + " of resource type : " +
                         resourceType + " of application : " + applicationKey + " in : " + environment;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } finally {
            AppFactoryDBUtil.closeResultSet(resourcesRS);
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }
        return false;
    }

    public boolean isDataBaseExistsForTenant(String databaseName, String resourceType, String environment,
                                             String tenantDomain) throws AppFactoryException {
        String tenantDatabaseName = null;
        if (StringUtils.isNotBlank(tenantDomain) &&
            !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            tenantDatabaseName = databaseName + AppFactoryConstants.UNDER_SCORE + tenantDomain.replace(
                    AppFactoryConstants.DOT, AppFactoryConstants.UNDER_SCORE);
        } else if (StringUtils.isBlank(tenantDomain)) {
            String msg = "Tenant domain should have a value to check existence of resource : " + databaseName + " of " +
                         "resource type: " + resourceType + " in : " + environment;
            log.error(msg);
            throw new AppFactoryException(msg);
        }
        Connection databaseConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resourcesRS = null;
        try {
            databaseConnection = AppFactoryDBUtil.getConnection();
            preparedStatement = databaseConnection
                    .prepareStatement(SQLConstants.GET_DATABASE_BY_NAME_AND_ENVIRONMENT);
            preparedStatement.setString(1, resourceType);
            preparedStatement.setString(2, environment);
            preparedStatement.setString(3, tenantDatabaseName);
            resourcesRS = preparedStatement.executeQuery();
            if (resourcesRS.next()) {

                //Here we are retrieving only a single resource from database. But we can add only a list of resources
                // of a particular resource type. So here we cant add the retrieved resource to the cache
                return true;
            }
        } catch (SQLException e) {
            String msg = "Error while checking availability of resource : " + databaseName + " of resource type : " +
                         resourceType + " of tenant : " + tenantDomain + " in : " + environment;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } finally {
            AppFactoryDBUtil.closeResultSet(resourcesRS);
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }
        return false;
    }

    /**
     * Delete a resource
     *
     * @param applicationKey application key
     * @param resourceName   resource name
     * @param resourceType   resource type
     * @param environment    environment
     * @return true if resources deleted successfully
     * @throws AppFactoryException
     */
    public boolean deleteResource(String applicationKey, String resourceName, String resourceType,
                                  String environment) throws AppFactoryException {
        Connection databaseConnection = null;
        PreparedStatement preparedStatement = null;
        try {
            databaseConnection = AppFactoryDBUtil.getConnection();
            preparedStatement =
                    databaseConnection.prepareStatement(SQLConstants.DELETE_RESOURCE_SQL);
            int applicationId = JDBCApplicationDAO.getInstance().getAutoIncrementAppID(applicationKey,
                                                                                       databaseConnection);
            preparedStatement.setInt(1, applicationId);
            preparedStatement.setString(2, resourceName);
            preparedStatement.setString(3, resourceType);
            preparedStatement.setString(4, environment);
            preparedStatement.execute();
            int affectedRow = preparedStatement.getUpdateCount();
            if (affectedRow > 0) {
                databaseConnection.commit();

                JDBCResourceCacheManager.clearCache(applicationKey, environment, resourceType);
                if (log.isDebugEnabled()) {
                    log.debug("Cache cleared for resource type : " + resourceType + " of application : " +
                              applicationKey + " in : " + environment);
                }

                return true;
            }
        } catch (SQLException e) {
            try {
                if (databaseConnection != null) {
                    databaseConnection.rollback();
                }
            } catch (SQLException e1) {
                String msg =
                        "Error while rolling back resource deletion for : " + resourceName + " of resource type : " +
                        resourceType + " of application : " + applicationKey + " in : " + environment;
                log.error(msg, e1);
            }
            String msg = "Error while deleting resource : " + resourceName + " of resource type : " + resourceType +
                         " of application : " + applicationKey + " in : " + environment;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } finally {
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }
        return false;
    }

    /**
     * Get all resources
     *
     * @param applicationKey application key
     * @param resourceType   resource type
     * @param environment    environment
     * @return array of Resource
     * @throws AppFactoryException
     */
    public Resource[] getResources(String applicationKey, String resourceType, String environment)
            throws AppFactoryException {

        // Get resources from cache
        List<Resource> resources = JDBCResourceCacheManager.getResourcesFromCache(
                applicationKey, environment, resourceType);
        if (log.isDebugEnabled()) {
            log.debug("Retrieved resources from cache for the resource type : " + resourceType + " of application : " +
                      applicationKey + " in : " + environment);
        }

        // If no resources from cache
        if (resources.isEmpty()) {
            Connection databaseConnection = null;
            PreparedStatement preparedStatement = null;
            ResultSet resourcesRS = null;
            try {
                databaseConnection = AppFactoryDBUtil.getConnection();
                preparedStatement =
                        databaseConnection.prepareStatement(SQLConstants.GET_RESOURCES_BY_TYPE_AND_ENV);
                preparedStatement.setString(1, applicationKey);
                preparedStatement.setInt(2, CarbonContext.getThreadLocalCarbonContext().getTenantId());
                preparedStatement.setString(3, resourceType);
                preparedStatement.setString(4, environment);
                resourcesRS = preparedStatement.executeQuery();
                Resource resource;
                while (resourcesRS.next()) {
                    resource = new Resource();
                    resource.setName(resourcesRS.getString(RESOURCE_NAME));
                    resource.setDescription(resourcesRS.getString(DESCRIPTION));
                    resources.add(resource);
                }
                String cacheKey = JDBCResourceCacheManager.addResourcesToCache(
                        applicationKey, resourceType, environment, resources);
                if (log.isDebugEnabled()) {
                    log.debug("Resources of resource type : " + resourceType + " of application : " + applicationKey +
                              " added to the cache with cache key : " + cacheKey + " in : " + environment);
                }
            } catch (SQLException e) {
                String msg = "Error while getting resources of resource type : " + resourceType + " of application : " +
                             applicationKey + " in environment : " + environment;
                log.error(msg, e);
                throw new AppFactoryException(msg, e);
            } finally {
                AppFactoryDBUtil.closeResultSet(resourcesRS);
                AppFactoryDBUtil.closePreparedStatement(preparedStatement);
                AppFactoryDBUtil.closeConnection(databaseConnection);
            }
        }
        return resources.toArray(new Resource[resources.size()]);
    }

    /**
     * update resources
     *
     * @param applicationKey application key
     * @param resourceType   resource type
     * @param resourceName   resource Name
     * @param environment    environment
     * @param description    description
     * @return status of update operation
     * @throws AppFactoryException
     */

    public boolean updateResource(String applicationKey, String resourceType, String resourceName, String environment,
                                  String description) throws AppFactoryException {
        Connection databaseConnection = null;
        PreparedStatement preparedStatement = null;

        try {
            databaseConnection = AppFactoryDBUtil.getConnection();
            preparedStatement = databaseConnection.prepareStatement(SQLConstants.UPDATE_RESOURCE);
            preparedStatement.setString(1, description);
            preparedStatement.setString(2, applicationKey);
            preparedStatement.setInt(3, CarbonContext.getThreadLocalCarbonContext().getTenantId());
            preparedStatement.setString(4, resourceType);
            preparedStatement.setString(5, resourceName);
            preparedStatement.setString(6, environment);
            preparedStatement.setInt(7, CarbonContext.getThreadLocalCarbonContext().getTenantId());
            preparedStatement.execute();
            if (preparedStatement.getUpdateCount() > 0) {
                databaseConnection.commit();

                JDBCResourceCacheManager.clearCache(applicationKey, environment, resourceType);
                if (log.isDebugEnabled()) {
                    log.debug("Cache cleared for resource type : " + resourceType + " of application : " +
                            applicationKey + " in : " + environment);
                }
                return true;
            }
        } catch (SQLException e) {
            String msg = "Error while updating the database for the resource type : " + resourceType +
                    " with the name : " + resourceName + " for application : " + applicationKey +
                    " in environment : " + environment;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } finally {
            AppFactoryDBUtil.closePreparedStatement(preparedStatement);
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }
        return false;
    }

    /**
     * Add deployement config details
     *
     * @param deploymentConfig deployement config
     */
    public void addDeploymentConfig(DeploymentConfig deploymentConfig) throws AppFactoryException {
        Connection connection = null;
        PreparedStatement addDeploymentPreparedStatement = null;

        try {
            connection = AppFactoryDBUtil.getConnection();
            addDeploymentPreparedStatement = connection.prepareStatement(SQLConstants.ADD_DEPLOYMENT);
            addDeploymentPreparedStatement.setString(1, deploymentConfig.getDeploymentName());
            addDeploymentPreparedStatement.setInt(2, deploymentConfig.getReplicas());
            addDeploymentPreparedStatement.executeUpdate();

            List<Container> containerList = deploymentConfig.getContainers();
            addContainer(containerList, deploymentConfig.getDeploymentName(), connection);
            if(addDeploymentPreparedStatement.getUpdateCount() > 0){
                connection.commit();
            }

        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException rollbackExcpetion) {
                String message = "Error while rolling back the added deployment : "
                        + deploymentConfig.getDeploymentName();
                log.error(message, rollbackExcpetion);
            }
            String message = "Error while adding database for deployement config : "
                    + deploymentConfig.getDeploymentName();
            log.error(message, e);
            throw new AppFactoryException(message, e);
        } finally {
            AppFactoryDBUtil.closePreparedStatement(addDeploymentPreparedStatement);
            AppFactoryDBUtil.closeConnection(connection);
        }
    }

    /**
     * Get deployement config
     *
     * @param applicationId id of the application
     * @param stage         stage of the application
     * @return deployement config
     */
    public DeploymentConfig getDeploymentConfig(String applicationId, String stage) throws AppFactoryException {
        Connection databaseConnection = null;
        PreparedStatement getDeploymentPreparedStatement = null;
        ResultSet deployementResultSet = null;

        DeploymentConfig deploymentConfig = new DeploymentConfig();

        try {
            databaseConnection = AppFactoryDBUtil.getConnection();
            getDeploymentPreparedStatement = databaseConnection.prepareStatement(SQLConstants.GET_DEPLOYEMENET);

            String deploymentName = applicationId + "-" + stage;
            getDeploymentPreparedStatement.setString(1, deploymentName);

            deployementResultSet = getDeploymentPreparedStatement.executeQuery();

            int deployementId = 0;

            while (deployementResultSet.next()) {
                deployementId = deployementResultSet.getInt(SQLParameterConstants.COLUMN_NAME_DEPLOYMENT_ID);
                deploymentConfig.setDeploymentName(
                        deployementResultSet.getString(SQLParameterConstants.COLUMN_NAME_DEPLOYMENT_NAME));
                deploymentConfig.setReplicas(deployementResultSet.getInt(SQLParameterConstants.COLUMN_NAME_REPLICAS));
            }

            List<Container> containers = getContainers(deployementId, databaseConnection);
            deploymentConfig.setContainers(containers);

        } catch (SQLException e) {
            String message = "Error while getting deployement config for application id : " + applicationId
                    + " in stage : " + stage;
            throw new AppFactoryException(message, e);
        } finally {
            AppFactoryDBUtil.closeResultSet(deployementResultSet);
            AppFactoryDBUtil.closePreparedStatement(getDeploymentPreparedStatement);
            AppFactoryDBUtil.closeConnection(databaseConnection);
        }

        return deploymentConfig;
    }

    /**
     * Add containers to deployment config
     *
     * @param containerList   list of containers
     * @param deployementName name of the deployement
     * @param connection      database connection
     */
    private void addContainer(List<Container> containerList, String deployementName, Connection connection) throws SQLException{
        PreparedStatement addContainerPreparedStatement = null;
        try {
            addContainerPreparedStatement = connection.prepareStatement(SQLConstants.ADD_CONTAINERS);
            for (Container container : containerList) {
                addContainerPreparedStatement.setString(1, container.getBaseImageName());
                addContainerPreparedStatement.setString(2, container.getBaseImageVersion());
                addContainerPreparedStatement.setString(3, deployementName);
                addContainerPreparedStatement.executeUpdate();

                List<ServiceProxy> serviceProxyList = container.getServiceProxies();
                addServiceProxy(serviceProxyList, container, deployementName, connection);
            }
        } catch (SQLException e) {
            String message = "Error while adding the database for container to deployment : " + deployementName;
            log.error(message, e);
            throw new SQLException(message, e);
        } finally {
            AppFactoryDBUtil.closePreparedStatement(addContainerPreparedStatement);
        }

    }

    /**
     * Add service proxies to container
     *
     * @param serviceProxyList list of service proxies
     * @param container        container details
     * @param deploymentName   name of the deployment
     * @param connection       database connection
     */
    private void addServiceProxy(List<ServiceProxy> serviceProxyList, Container container, String deploymentName,
            Connection connection) throws SQLException {
        PreparedStatement addProxiesPreparedStatement = null;
        try {
            addProxiesPreparedStatement = connection.prepareStatement(SQLConstants.ADD_SERVICE_PROXIES);
            for (ServiceProxy serviceProxy : serviceProxyList) {
                addProxiesPreparedStatement.setString(1, serviceProxy.getServiceName());
                addProxiesPreparedStatement.setString(2, serviceProxy.getServiceProtocol());
                addProxiesPreparedStatement.setInt(3, serviceProxy.getServicePort());
                addProxiesPreparedStatement.setInt(4, serviceProxy.getServiceBackendPort());
                addProxiesPreparedStatement.setString(5, container.getBaseImageName());
                addProxiesPreparedStatement.setString(6, container.getBaseImageVersion());
                addProxiesPreparedStatement.setString(7, deploymentName);
                addProxiesPreparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            String message = "Error while adding the database for service proxies to deployement : " + deploymentName;
            log.error(message, e);
            throw new SQLException(message, e);
        } finally {
            AppFactoryDBUtil.closePreparedStatement(addProxiesPreparedStatement);
        }

    }

    /**
     * Get container
     *
     * @param deploymentId id of the deployment
     * @param databaseConnection dabase connection
     * @return list of containers
     */
    private List<Container> getContainers(int deploymentId, Connection databaseConnection) throws SQLException{
        PreparedStatement getContainerPreparedStatement = null;
        ResultSet containerResultSet = null;
        List<Container> containers = new ArrayList<Container>();

        try {
            getContainerPreparedStatement = databaseConnection.prepareStatement(SQLConstants.GET_CONTAINER);
            getContainerPreparedStatement.setInt(1, deploymentId);
            containerResultSet = getContainerPreparedStatement.executeQuery();

            //Adding container details
            while (containerResultSet.next()) {
                Container container = new Container();
                container.setBaseImageName(
                        containerResultSet.getString(SQLParameterConstants.COLUMN_NAME_BASEIMAGE_NAME));
                container.setBaseImageVersion(
                        containerResultSet.getString(SQLParameterConstants.COLUMN_NAME_BASEIMAGE_VERSION));

                int containerId = containerResultSet.getInt(SQLParameterConstants.COLUMN_NAME_CONTAINER_ID);
                List<ServiceProxy> serviceProxies = getServiceProxies(containerId, databaseConnection);
                container.setServiceProxies(serviceProxies);
                containers.add(container);
            }

        } catch (SQLException e) {
            String message = "Error while getting containers";
            log.error(message, e);
            throw new SQLException(message, e);
        } finally {
            AppFactoryDBUtil.closeResultSet(containerResultSet);
            AppFactoryDBUtil.closePreparedStatement(getContainerPreparedStatement);
        }

        return containers;

    }

    /**
     * Get service proxies
     *
     * @param containerId        id of the container
     * @param databaseConnection database connetion
     * @return list of service proxies
     */
    private List<ServiceProxy> getServiceProxies(int containerId, Connection databaseConnection) throws SQLException{
        PreparedStatement getServicePreparedStatement = null;
        ResultSet serviceResultSet = null;
        List<ServiceProxy> serviceProxies = new ArrayList<ServiceProxy>();

        try {
            getServicePreparedStatement = databaseConnection.prepareStatement(SQLConstants.GET_SERVICE_PROXY);
            getServicePreparedStatement.setInt(1, containerId);
            serviceResultSet = getServicePreparedStatement.executeQuery();

            //Adding service proxy details
            while (serviceResultSet.next()) {
                ServiceProxy serviceProxy = new ServiceProxy();
                serviceProxy.setServiceName(serviceResultSet.getString(SQLParameterConstants.COLUMN_NAME_SERVICE_NAME));
                serviceProxy.setServiceProtocol(
                        serviceResultSet.getString(SQLParameterConstants.COLUMN_NAME_SERVICE_PROTOCOL));
                serviceProxy.setServicePort(serviceResultSet.getInt(SQLParameterConstants.COLUMN_NAME_SERVICE_PORT));
                serviceProxy.setServiceBackendPort(
                        serviceResultSet.getInt(SQLParameterConstants.COLUMN_NAME_SERVICE_BACKEND_PORT));
                serviceProxies.add(serviceProxy);
            }

        } catch (SQLException e) {
            String message = "Error while getting service proxies";
            log.error(message, e);
            throw new SQLException(message, e);
        } finally {
            AppFactoryDBUtil.closeResultSet(serviceResultSet);
            AppFactoryDBUtil.closePreparedStatement(getServicePreparedStatement);
        }

        return serviceProxies;

    }

}