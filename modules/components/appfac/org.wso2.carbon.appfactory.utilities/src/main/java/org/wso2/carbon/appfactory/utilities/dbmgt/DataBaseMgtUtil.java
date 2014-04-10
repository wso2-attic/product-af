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
package org.wso2.carbon.appfactory.utilities.dbmgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.utilities.internal.ServiceReferenceHolder;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactFilter;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for storing and retrieving RSS database,user,template information in registry
 */
public class DataBaseMgtUtil {
    private static final Log log = LogFactory.getLog(DataBaseMgtUtil.class);

    private static class Database {
        public static final String NAME = "database_name";
        public static final String ENVIRONMENT = "database_environment";
        public static final String APP_KEY = "database_appkey";
    }

    private static class DatabaseUser {
        public static final String NAME = "databaseuser_name";
        public static final String ENVIRONMENT = "databaseuser_environment";
        public static final String APP_KEY = "databaseuser_appkey";
    }

    private static class DatabaseUserPermissionTemplate {
        public static final String NAME = "databasetemplate_name";
        public static final String ENVIRONMENT = "databasetemplate_environment";
        public static final String APP_KEY = "databasetemplate_appkey";
    }

    public static boolean addDatabase(String dbName, String applicationId, String environment,
                                      String tenantDomain
    ) throws
            AppFactoryException {
        UserRegistry userRegistry = getUserRegistry(tenantDomain);
        try {
            GenericArtifactManager artifactManager = new GenericArtifactManager(userRegistry, "db");
            GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName(dbName));
            artifact.setAttribute(Database.NAME, dbName);
            artifact.setAttribute(Database.APP_KEY, applicationId);
            artifact.setAttribute(Database.ENVIRONMENT, environment);
            artifactManager.addGenericArtifact(artifact);
        } catch (Exception e) {
            String msg = "Error while persisting the db meta data " + dbName;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        return true;
    }

    public static boolean addDatabaseUser(String dbUserName, String applicationId,
                                          String environment, String tenantDomain) throws AppFactoryException {
        UserRegistry userRegistry = getUserRegistry(tenantDomain);
        try {
            GenericArtifactManager artifactManager = new GenericArtifactManager(userRegistry, "user");
            GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName(dbUserName));
            artifact.setAttribute(DatabaseUser.NAME, dbUserName);
            artifact.setAttribute(DatabaseUser.APP_KEY, applicationId);
            artifact.setAttribute(DatabaseUser.ENVIRONMENT, environment);
            artifactManager.addGenericArtifact(artifact);
        } catch (RegistryException e) {
            String msg = "Error while persisting the db user meta data " + dbUserName;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        return true;
    }

    public static boolean addDatabaseUserPermissionTemplate(String dbPermissionTemplate,
                                                            String applicationId,
                                                            String environment, String tenantDomain) throws AppFactoryException {
        UserRegistry userRegistry = getUserRegistry(tenantDomain);
        try {
            GenericArtifactManager artifactManager = new GenericArtifactManager(userRegistry, "template");
            GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName(dbPermissionTemplate));
            artifact.setAttribute(DatabaseUserPermissionTemplate.NAME, dbPermissionTemplate);
            artifact.setAttribute(DatabaseUserPermissionTemplate.APP_KEY, applicationId);
            artifact.setAttribute(DatabaseUserPermissionTemplate.ENVIRONMENT, environment);
            artifactManager.addGenericArtifact(artifact);
        } catch (RegistryException e) {
            String msg = "Error while persisting the db permission template meta data " +
                    "" + dbPermissionTemplate;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        return true;
    }

    public static String[] getAllDatabases(final String applicationID, final String environment, String tenantDomain
    ) throws
            AppFactoryException {
        UserRegistry registry = getUserRegistry(tenantDomain);
        List<String> dbs = new ArrayList<String>();
        try {
            GenericArtifactManager artifactManager = new GenericArtifactManager(registry, "db");
            GenericArtifactFilter artifactFilter = new GenericArtifactFilter() {
                @Override
                public boolean matches(GenericArtifact artifact) throws GovernanceException {
                    if (artifact != null && artifact.getAttribute(Database.APP_KEY) != null) {
                        return artifact.getAttribute(Database.APP_KEY).equals(applicationID) &&
                                artifact.getAttribute(Database.ENVIRONMENT).equals(environment);
                    } else {
                        return false;
                    }
                }
            };
            GenericArtifact[] allArtifacts = artifactManager.findGenericArtifacts(artifactFilter);

            for (GenericArtifact artifact : allArtifacts) {
                dbs.add(artifact.getAttribute(Database.NAME));
            }
        } catch (RegistryException e) {
            String msg = "Error while getting all databases ";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        return dbs.toArray(new String[dbs.size()]);
    }

    public static String[] getAllDatabasePrivilegeTemplates(final String applicationID,
                                                            final String environment, String tenantDomain
    ) throws
            AppFactoryException {
        UserRegistry registry = getUserRegistry(tenantDomain);
        List<String> dbs = new ArrayList<String>();
        try {
            GenericArtifactManager artifactManager = new GenericArtifactManager(registry, "template");
            GenericArtifactFilter artifactFilter = new GenericArtifactFilter() {
                @Override
                public boolean matches(GenericArtifact artifact) throws GovernanceException {
                    if (artifact != null && artifact.getAttribute(DatabaseUserPermissionTemplate.APP_KEY)
                            != null) {
                        return artifact.getAttribute(DatabaseUserPermissionTemplate.APP_KEY).equals
                                (applicationID) &&
                                artifact.getAttribute(DatabaseUserPermissionTemplate.ENVIRONMENT).equals
                                        (environment);
                    } else {
                        return false;
                    }
                }
            };
            GenericArtifact[] allArtifacts = artifactManager.findGenericArtifacts(artifactFilter);

            for (GenericArtifact artifact : allArtifacts) {
                dbs.add(artifact.getAttribute(DatabaseUserPermissionTemplate.NAME));
            }
        } catch (RegistryException e) {
            String msg = "Error while getting all database privilege templates ";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        return dbs.toArray(new String[dbs.size()]);
    }

    public static String[] getAllDatabaseUsers(final String applicationID,
                                               final String environment, String tenantDomain
    ) throws
            AppFactoryException {
        UserRegistry registry = getUserRegistry(tenantDomain);
        List<String> dbs = new ArrayList<String>();
        try {
            GenericArtifactManager artifactManager = new GenericArtifactManager(registry, "user");
            GenericArtifactFilter artifactFilter = new GenericArtifactFilter() {
                @Override
                public boolean matches(GenericArtifact artifact) throws GovernanceException {
                    if (artifact != null && artifact.getAttribute(DatabaseUser.APP_KEY) != null) {
                        return artifact.getAttribute(DatabaseUser.APP_KEY).equals(applicationID) &&
                                artifact.getAttribute(DatabaseUser.ENVIRONMENT).equals(environment);
                    } else {
                        return false;
                    }
                }
            };
            GenericArtifact[] allArtifacts = artifactManager.findGenericArtifacts(artifactFilter);

            for (GenericArtifact artifact : allArtifacts) {
                dbs.add(artifact.getAttribute(DatabaseUser.NAME));
            }
        } catch (RegistryException e) {
            String msg = "Error while getting all database users";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        return dbs.toArray(new String[dbs.size()]);
    }

    public static boolean isDatabaseExist(final String dbName, final String applicationID,
                                          final String environment, String tenantDomain) throws AppFactoryException {
        try {
            GenericArtifactManager artifactManager = new GenericArtifactManager(getUserRegistry(tenantDomain),
                    "db");
            GenericArtifactFilter artifactFilter = new GenericArtifactFilter() {
                @Override
                public boolean matches(GenericArtifact artifact) throws GovernanceException {
                    if (artifact != null && artifact.getAttribute(Database.APP_KEY) !=
                            null) {
                        return artifact.getAttribute(Database.APP_KEY).equals(applicationID) &&
                                artifact.getAttribute(Database.ENVIRONMENT).equals
                                        (environment)
                                && artifact.getAttribute(Database.NAME).equals(dbName)
                                ;
                    } else {
                        return false;
                    }
                }
            };

            return artifactManager.findGenericArtifacts(artifactFilter)[0] != null;
        } catch (RegistryException e) {
            String msg = "Error while checking existance of the database " + dbName;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
    }

    public static boolean deleteDatabase(final String dbName, final String applicationId, final String environment,
                                         String tenantDomain) throws AppFactoryException {
        UserRegistry registry = getUserRegistry(tenantDomain);

        try {
            GenericArtifactManager artifactManager = new GenericArtifactManager(registry, "db");
            GenericArtifactFilter artifactFilter = new GenericArtifactFilter() {
                @Override
                public boolean matches(GenericArtifact artifact) throws GovernanceException {
                    if (artifact != null && artifact.getAttribute(Database.APP_KEY) !=
                            null) {
                        return artifact.getAttribute(Database.APP_KEY).equals
                                (applicationId) &&
                                artifact.getAttribute(Database.ENVIRONMENT).equals
                                        (environment)
                                && artifact.getAttribute(Database.NAME).equals
                                (dbName);
                    } else {
                        return false;
                    }
                }
            };
            GenericArtifact[] allArtifacts = artifactManager.findGenericArtifacts(artifactFilter);
            if (allArtifacts.length == 1) {
                artifactManager.removeGenericArtifact(allArtifacts[0].getId());
                return true;
            }
        } catch (RegistryException e) {
            String msg = "Error while deleting the database " + dbName;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        return false;
    }

    public static boolean deleteDatabaseUser(final String dbUserName, final String applicationId,
                                             final String environment, String tenantDomain) throws AppFactoryException {
        UserRegistry registry = getUserRegistry(tenantDomain);
        List<String> dbs = new ArrayList<String>();
        try {
            GenericArtifactManager artifactManager = new GenericArtifactManager(registry, "user");
            GenericArtifactFilter artifactFilter = new GenericArtifactFilter() {
                @Override
                public boolean matches(GenericArtifact artifact) throws GovernanceException {
                    if (artifact != null && artifact.getAttribute(DatabaseUser
                            .APP_KEY) !=
                            null) {
                        return artifact.getAttribute(DatabaseUser.APP_KEY).equals
                                (applicationId) &&
                                artifact.getAttribute(DatabaseUser.ENVIRONMENT)
                                        .equals
                                                (environment) && artifact.getAttribute
                                (DatabaseUser.NAME).equals(dbUserName);
                    } else {
                        return false;
                    }
                }
            };
            GenericArtifact[] allArtifacts = artifactManager.findGenericArtifacts(artifactFilter);

            if (allArtifacts.length == 1) {
                artifactManager.removeGenericArtifact(allArtifacts[0].getId());
                return true;
            }
        } catch (RegistryException e) {
            String msg = "Error while deleting the user " + dbUserName;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        return false;
    }

    public static boolean deleteDatabaseUserPermissionTemplate(final String dbPermissionTemplate,
                                                               final String applicationId,
                                                               final String environment, String tenantDomain) throws AppFactoryException {
        UserRegistry registry = getUserRegistry(tenantDomain);
        List<String> dbs = new ArrayList<String>();
        try {
            GenericArtifactManager artifactManager = new GenericArtifactManager(registry, "template");
            GenericArtifactFilter artifactFilter = new GenericArtifactFilter() {
                @Override
                public boolean matches(GenericArtifact artifact) throws GovernanceException {
                    if (artifact != null && artifact.getAttribute(DatabaseUserPermissionTemplate
                            .APP_KEY) !=
                            null) {
                        return artifact.getAttribute(DatabaseUserPermissionTemplate
                                .APP_KEY).equals
                                (applicationId) &&
                                artifact.getAttribute(DatabaseUserPermissionTemplate
                                        .ENVIRONMENT).equals
                                        (environment)
                                && artifact.getAttribute(DatabaseUserPermissionTemplate
                                .NAME).equals
                                (dbPermissionTemplate);
                    } else {
                        return false;
                    }
                }
            };
            GenericArtifact[] allArtifacts = artifactManager.findGenericArtifacts(artifactFilter);

            if (allArtifacts.length == 1) {
                artifactManager.removeGenericArtifact(allArtifacts[0].getId());
            }
        } catch (RegistryException e) {
            String msg = "Error while deleting the user permission template " + dbPermissionTemplate;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        return false;
    }

    private static UserRegistry getUserRegistry(String tenantDomain) throws AppFactoryException {

        UserRegistry registry;
        try {
            registry = ServiceReferenceHolder.getInstance().getRegistryService()
                    .getGovernanceSystemRegistry(ServiceReferenceHolder.getInstance()
                            .getRealmService().getTenantManager().getTenantId(tenantDomain));
        } catch (RegistryException e) {
            String msg = "Could not able to get registry for " + tenantDomain;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (UserStoreException e) {
            String msg = "Error while getting the tenant id for " + tenantDomain;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        return registry;
    }
}
