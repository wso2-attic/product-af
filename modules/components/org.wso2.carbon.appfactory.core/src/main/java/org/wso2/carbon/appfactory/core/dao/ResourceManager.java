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

import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.dto.Resource;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to store and retrieve databases, users, templates and datasources info from/to
 * App Factory db
 */
public class ResourceManager {

    private static JDBCResourceDAO resourceDAO = JDBCResourceDAO.getInstance();

    public static final String DATABASE_RESOURCE = "DATABASE";
    public static final String DATABASE_USER_RESOURCE = "DATABASE_USER";
    public static final String DATABASE_PERMISSION_TEMPLATE_RESOURCE = "DATABASE_TEMPLATE";
    public static final String DATASOURCE_RESOURCE = "DATASOURCE";
    public static final String PROPERTY_RESOURCE = "PROPERTY";

    /**
     * Add database info
     *
     * @param dbName        database name
     * @param applicationId application id
     * @param environment   environment
     * @param description   description
     * @return true if database added successfully otherwise false
     * @throws AppFactoryException
     */
    public static boolean addDatabase(String dbName, String applicationId, String environment,
                                      String description) throws AppFactoryException {
        return resourceDAO
            .addResource(applicationId, dbName, DATABASE_RESOURCE, environment, description);
    }

    /**
     * Add database user info
     *
     * @param dbUserName    database user name
     * @param applicationId application id
     * @param environment   environment
     * @return true if database user added successfully otherwise false
     * @throws AppFactoryException
     */
    public static boolean addDatabaseUser(String dbUserName, String applicationId,
                                          String environment) throws AppFactoryException {
        return resourceDAO
            .addResource(applicationId, dbUserName, DATABASE_USER_RESOURCE, environment, "");
    }

    /**
     * Add permission template info
     *
     * @param templateName  template name
     * @param applicationId application id
     * @param environment   environment
     * @return true if permission template added successfully otherwise false
     * @throws AppFactoryException
     */
    public static boolean addDatabaseUserPermissionTemplate(String templateName,
                                                            String applicationId,
                                                            String environment)
        throws AppFactoryException {
        return resourceDAO.addResource(applicationId, templateName,
                                       DATABASE_PERMISSION_TEMPLATE_RESOURCE, environment, "");
    }

    /**
     * Add datasource info
     *
     * @param datasourceName datasource name
     * @param applicationId  application id
     * @param environment    environment
     * @param description    description
     * @return true if datasource added successfully otherwise false
     * @throws AppFactoryException
     */
    public static boolean addDatasource(String datasourceName, String applicationId,
                                        String environment, String description)
        throws AppFactoryException {
        return resourceDAO
            .addResource(applicationId, datasourceName, DATASOURCE_RESOURCE, environment,
                         description);
    }

    /**
     * Get all database info
     *
     * @param applicationId application id
     * @param environment   environment
     * @return array of Resource
     * @throws AppFactoryException
     */
    public static Resource[] getAllDatabasesInfo(final String applicationId,
                                                 final String environment)
        throws AppFactoryException {
        return resourceDAO.getResources(applicationId, DATABASE_RESOURCE, environment);
    }

    /**
     * Get all datasource info
     *
     * @param applicationId application id
     * @param environment   environment
     * @return array of Resource
     * @throws AppFactoryException
     */
    public static Resource[] getAllDatasourceInfo(final String applicationId,
                                                  final String environment)
        throws AppFactoryException {
        return resourceDAO.getResources(applicationId, DATASOURCE_RESOURCE, environment);
    }

    /**
     * Get all database names
     *
     * @param applicationId application id
     * @param environment   environment
     * @return array of String
     * @throws AppFactoryException
     */
    public static String[] getAllDatabases(final String applicationId, final String environment)
        throws AppFactoryException {
        List<String> dbs = new ArrayList<String>();
        Resource[] resources =
            resourceDAO.getResources(applicationId, DATABASE_RESOURCE, environment);
        for (Resource resource : resources) {
            dbs.add(resource.getName());
        }
        return dbs.toArray(new String[dbs.size()]);
    }

    /**
     * Get all permission template names
     *
     * @param applicationId application id
     * @param environment   environment
     * @return array of String
     * @throws AppFactoryException
     */
    public static String[] getAllDatabasePrivilegeTemplates(final String applicationId,
                                                            final String environment)
        throws AppFactoryException {
        List<String> dbTemplates = new ArrayList<String>();
        Resource[] resources = resourceDAO
            .getResources(applicationId, DATABASE_PERMISSION_TEMPLATE_RESOURCE, environment);
        for (Resource resource : resources) {
            dbTemplates.add(resource.getName());
        }
        return dbTemplates.toArray(new String[dbTemplates.size()]);
    }

    /**
     * Get all database user names
     *
     * @param applicationId application id
     * @param environment   environment
     * @return array of String
     * @throws AppFactoryException
     */
    public static String[] getAllDatabaseUsers(final String applicationId, final String environment)
        throws AppFactoryException {

        List<String> dbUsers = new ArrayList<String>();
        Resource[] resources =
            resourceDAO.getResources(applicationId, DATABASE_USER_RESOURCE, environment);
        for (Resource resource : resources) {
            dbUsers.add(resource.getName());
        }
        return dbUsers.toArray(new String[dbUsers.size()]);
    }

    /**
     * @param databaseName
     * @param environment
     * @param tenantDomain
     * @return
     * @throws AppFactoryException
     */
    public static boolean isDatabaseExistForTenant(final String databaseName, final String environment,
                                                   final String tenantDomain) throws AppFactoryException {
        return resourceDAO.isDataBaseExistsForTenant(databaseName, DATABASE_RESOURCE, environment, tenantDomain);
    }

    /**
     * Check whether the database user is exist
     *
     * @param dbUserName    database user
     * @param applicationId application id
     * @param environment   environment
     * @return true if database user is exist
     * @throws AppFactoryException
     */
    public static boolean isDatabaseUserExist(final String dbUserName, final String applicationId,
                                              final String environment) throws AppFactoryException {
        return resourceDAO
            .isResourceExists(applicationId, dbUserName, DATABASE_USER_RESOURCE, environment);
    }

    /**
     * Check whether the permission template is exist
     *
     * @param templateName  template name
     * @param applicationId application id
     * @param environment   environment
     * @return true if permission template is exist
     * @throws AppFactoryException
     */
    public static boolean isDatabaseUserPermissionTemplateExist(final String templateName,
                                                                final String applicationId,
                                                                final String environment)
        throws AppFactoryException {
        return resourceDAO
            .isResourceExists(applicationId, templateName,
                              DATABASE_PERMISSION_TEMPLATE_RESOURCE,
                              environment);
    }

    /**
     * Check whether the datasource is exist
     *
     * @param datasourceName datasource name
     * @param applicationId  application id
     * @param environment    environment
     * @return true if datasource is exist
     * @throws AppFactoryException
     */
    public static boolean isDatasourceExist(final String datasourceName, final String applicationId,
                                            final String environment) throws AppFactoryException {
        return resourceDAO
            .isResourceExists(applicationId, datasourceName, DATASOURCE_RESOURCE, environment);
    }

    /**
     * Delete database
     *
     * @param dbName        database name
     * @param applicationId application id
     * @param environment   environment
     * @return true if database deleted successfully
     * @throws AppFactoryException
     */
    public static boolean deleteDatabase(final String dbName, final String applicationId,
                                         final String environment) throws AppFactoryException {
        return resourceDAO.deleteResource(applicationId, dbName, DATABASE_RESOURCE, environment);
    }

    /**
     * Delete database user
     *
     * @param dbUserName    database user name
     * @param applicationId application id
     * @param environment   environment
     * @return true if database user deleted successfully
     * @throws AppFactoryException
     */
    public static boolean deleteDatabaseUser(final String dbUserName, final String applicationId,
                                             final String environment) throws AppFactoryException {
        return resourceDAO
            .deleteResource(applicationId, dbUserName, DATABASE_USER_RESOURCE, environment);
    }

    /**
     * Delete permission template
     *
     * @param templateName  template name
     * @param applicationId application id
     * @param environment   environment
     * @return true if permission template deleted successfully
     * @throws AppFactoryException
     */
    public static boolean deleteDatabaseUserPermissionTemplate(final String templateName,
                                                               final String applicationId,
                                                               final String environment)
        throws AppFactoryException {
        return resourceDAO.deleteResource(applicationId, templateName,
                                          DATABASE_PERMISSION_TEMPLATE_RESOURCE, environment);
    }

    /**
     * Delete datasource
     *
     * @param datasourceName datasource name
     * @param applicationId  application id
     * @param environment    environment
     * @return true if datasource deleted successfully
     * @throws AppFactoryException
     */
    public static boolean deleteDatasource(final String datasourceName, final String applicationId,
                                           final String environment) throws AppFactoryException {
        return resourceDAO
            .deleteResource(applicationId, datasourceName, DATASOURCE_RESOURCE, environment);
    }

    /**
     * Add property to the database
     *
     * @param propName       name of the property, which needs to be added
     * @param applicationKey key of the application, to which the property belongs
     * @param environment    environment, to which the property belongs
     * @param description    description of the property
     * @param resourceType   type of the property
     * @return true if add property operation is success
     * @throws AppFactoryException
     */
    public static boolean addProperty(String propName, String applicationKey, String environment, String description,
                                      String resourceType) throws AppFactoryException {
        return resourceDAO.addResource(applicationKey, propName, resourceType, environment, description);
    }

    /**
     * Delete a given property from database
     *
     * @param applicationKey key of the application, to which the property belongs
     * @param propName       name of the property, which needs to be added
     * @param environment    environment, to which the property belongs
     * @param resourceType   type of the property
     * @return true if delete property operation is success
     * @throws AppFactoryException
     */
    public static boolean deleteProperty(String applicationKey, String propName, String environment,
                                         String resourceType) throws AppFactoryException {
        return resourceDAO.deleteResource(applicationKey, propName, resourceType, environment);
    }

    /**
     * Copy the given type of properties of an application from one stage to another stage
     *
     * @param applicationKey key of the application, to which the property belongs
     * @param sourceStage    the environment stage, from which the property needs to be copied
     * @param targetStage    the environment stage, to which the property needs to be copied
     * @param resourceType   type of the property
     * @throws AppFactoryException
     */
    public static void copyNewProperties(String applicationKey, String sourceStage, String targetStage,
                                         String resourceType) throws AppFactoryException {
        Resource[] resources = resourceDAO.getResources(applicationKey, resourceType, sourceStage);
        for (Resource resource : resources) {
            resourceDAO.addResource(applicationKey, resource.getName(), resourceType, targetStage,
                    resource.getDescription());
        }
    }

    /**
     * Update the description of a given property
     *
     * @param applicationKey key of the application, to which the property belongs
     * @param resourceType   type of the property, which needs to be updated
     * @param resourceName   name of the property, which needs to be updated
     * @param environment    environment, to which the property belongs
     * @param description    description about the property
     * @return true if update property operation is success
     * @throws AppFactoryException
     */
    public static boolean updateProperty(String applicationKey, String resourceType, String resourceName,
                                         String environment, String description) throws AppFactoryException {
        return resourceDAO.updateResource(applicationKey, resourceType, resourceName, environment, description);
    }

    /**
     * Check whether a property exists in the database
     *
     * @param applicationKey key of the application, to which the property belongs
     * @param resourceName   name of the property, which needs to be checked
     * @param resourceType   type of the property, which needs to be checked
     * @param environment    environment, to which the property belongs
     * @return true if property exists
     * @throws AppFactoryException
     */
    public static boolean isPropertyExists(String applicationKey, String resourceName, String resourceType,
                                           String environment) throws AppFactoryException {
        return resourceDAO.isResourceExists(applicationKey, resourceName, resourceType, environment);
    }

    /**
     * Get all the properties of a given type in given environment for an application
     *
     * @param applicationKey key of the application, which properties need to be retrieved
     * @param resourceType   type of the properties, which need to be retrieved
     * @param environment    environment of the properties, which need to be retrieved
     * @return array of resources
     * @throws AppFactoryException
     */
    public static Resource[] getPropertiesInEnvironment(String applicationKey, String resourceType, String environment)
            throws AppFactoryException {
        return resourceDAO.getResources(applicationKey, resourceType, environment);
    }

}
