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
package org.wso2.carbon.appfactory.repository.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.eventing.AppFactoryEventException;
import org.wso2.carbon.appfactory.eventing.Event;

import org.wso2.carbon.appfactory.eventing.EventNotifier;
import org.wso2.carbon.appfactory.eventing.builder.utils.RepoCreationEventBuilderUtil;
import org.wso2.carbon.appfactory.repository.mgt.internal.Util;

/**
 * This is a class to manage all the repository operation by getting relevant
 * repository provider
 * .
 */
public class RepositoryManager {
    private static final Log log = LogFactory.getLog(RepositoryManager.class);

    /**
     * Create a repository
     * 
     * @param applicationKey
     *            Application key
     * @param type
     *            Repository type
     * @param tenantDomain
     *            Tenant domain of repository
     * @return url for created repository
     * @throws RepositoryMgtException
     */
    public String createRepository(String applicationKey, String type, String tenantDomain)
                                                                                           throws RepositoryMgtException {
        String url = null;
        RepositoryProvider provider = Util.getRepositoryProvider(type);
        if (provider != null) {
            url = provider.createRepository(applicationKey, tenantDomain);
        } else {
            handleException(new StringBuilder().append("Repository provider for the type ")
                                               .append(type).append(" not found").toString());
        }

        return url;
    }

    /**
     * 
     * @param applicationKey
     * @param type
     * @param tenantDomain
     * @return
     * @throws RepositoryMgtException
     */
    public boolean deleteRepository(String applicationKey, String type, String tenantDomain)
                                                                                            throws RepositoryMgtException {
        boolean deleted = false;
        RepositoryProvider provider = Util.getRepositoryProvider(type);
        if (provider != null) {
            deleted = provider.deleteRepository(applicationKey, tenantDomain);
        } else {
            handleException(new StringBuilder().append("Repository provider for the type ")
                                               .append(type).append(" not found").toString());
        }

        return deleted;
    }

    /**
     * 
     * @param msg
     * @throws RepositoryMgtException
     */
    private void handleException(String msg) throws RepositoryMgtException {
        log.error(msg);
        throw new RepositoryMgtException(msg);
    }

    /**
     * 
     * @param appId
     * @param type
     * @param tenantName
     * @return
     * @throws RepositoryMgtException
     */
    public String getAppRepositoryURL(String appId, String type, String tenantName)
                                                                                   throws RepositoryMgtException {
        RepositoryProvider provider = Util.getRepositoryProvider(type);
        if (provider != null) {
            return provider.getAppRepositoryURL(appId, tenantName);
        } else {
            handleException(new StringBuilder().append("Repository provider for the type ")
                                               .append(type).append(" not found").toString());
        }
        return null;
    }

    /**
     * 
     * 
     * @param applicationKey
     * @param version
     * @param type
     * @param tenantName
     * @return
     * @throws RepositoryMgtException
     */
    public String getURLForAppversion(String applicationKey, String version, String type,
                                      String tenantName) throws RepositoryMgtException {
        RepositoryProvider provider = Util.getRepositoryProvider(type);

        if (provider != null) {
            return provider.getBranchingStrategy().getURLForAppVersion(applicationKey, version,
                                                                       tenantName);
        } else {
            handleException(new StringBuilder().append("Repository provider for the type ")
                                               .append(type).append(" not found").toString());
        }
        return null;
    }

    /**
     * 
     * 
     * @param appId
     * @param type
     * @param currentVersion
     * @param targetVersion
     * @param currentRevision
     * @param tenantDomain
     * @throws RepositoryMgtException
     */
    public void branch(String appId, String type, String currentVersion, String targetVersion,
                       String currentRevision, String tenantDomain) throws RepositoryMgtException {
        String correlationKey = tenantDomain + "-" + appId + "-" + type + "-" + currentVersion +
                                    "-" + targetVersion + "-" + currentRevision;
        String infoMessage = "Branch " + targetVersion + " creation started" ;
        try {
            EventNotifier.getInstance()
                    .notify(RepoCreationEventBuilderUtil.buildBranchCreationStartEvent(appId, infoMessage,
                            "",
                            Event.Category.INFO, correlationKey));
        } catch (AppFactoryEventException e) {
            log.error("Failed to notify Branch creation event", e);
            // do not throw again.
        }

        RepositoryProvider provider = Util.getRepositoryProvider(type);
        if (provider != null) {
            provider.getBranchingStrategy().doRepositoryBranch(appId, currentVersion,
                                                               targetVersion, currentRevision,
                                                               tenantDomain);
        } else {
            handleException(new StringBuilder().append("Repository provider for the type ")
                                               .append(type).append(" not found").toString());
        }

    }

    /**
     * 
     * @param appId
     * @param type
     * @param currentVersion
     * @param targetVersion
     * @param currentRevision
     * @param tenantDomain
     * @throws RepositoryMgtException
     */
    public void tag(String appId, String type, String currentVersion, String targetVersion,
                    String currentRevision, String tenantDomain) throws RepositoryMgtException {
        RepositoryProvider provider = Util.getRepositoryProvider(type);
        if (provider != null) {
            Util.getRepositoryProvider(type)
                .getBranchingStrategy()
                .doRepositoryTag(appId, currentVersion, targetVersion, currentRevision,
                                 tenantDomain);
        } else {
            handleException(new StringBuilder().append("Repository provider for the type ")
                                               .append(type).append(" not found").toString());
        }
    }

    /**
     * 
     * @param type
     * @return
     */
    public RepositoryProvider getRepositoryProvider(String type) {
        return Util.getRepositoryProvider(type);
    }

    /**
     * 
     * @param applicationKey
     * @param type
     * @param username
     * @throws RepositoryMgtException
     */
    public void provisionUser(String applicationKey, String type, String username)
                                                                                  throws RepositoryMgtException {
        RepositoryProvider provider = Util.getRepositoryProvider(type);
        if (provider != null) {
            provider.provisionUser(applicationKey, username);
        } else {
            handleException(new StringBuilder().append("Repository provider failed to provision user")
                                               .append(username).append(" for the type ")
                                               .append(type).append(" not found").toString());
        }
    }

    /**
     * 
     * 
     * @param tenantId
     * @param type
     * @return
     * @throws RepositoryMgtException
     */
    public Boolean createTenantRepo(String tenantId, String type) throws RepositoryMgtException {
        Boolean result = null;
        RepositoryProvider provider = Util.getRepositoryProvider(type);
        if (provider != null) {
            result = provider.createTenantRepo(tenantId);
        } else {
            handleException(new StringBuilder().append("Repository provider for the type ")
                                               .append(type).append(" not found").toString());
        }

        return result;

    }

    /**
     * 
     * 
     * @param tenantId
     * @param type
     * @return
     * @throws RepositoryMgtException
     */
    public Boolean deleteTenantRepo(String tenantId, String type) throws RepositoryMgtException {
        Boolean result = null;
        RepositoryProvider provider = Util.getRepositoryProvider(type);
        if (provider != null) {
            result = provider.deleteTenantRepo(tenantId);
        } else {
            handleException(new StringBuilder().append("Repository provider for the type ")
                                               .append(type).append(" not found").toString());
        }
        return result;

    }

    /**
     * 
     * 
     * @param applicationKey
     * @param type
     * @param tenantDomain
     * @return
     * @throws RepositoryMgtException
     */
    public boolean repositoryExists(String applicationKey, String type, String tenantDomain)
                                                                                            throws RepositoryMgtException {
        boolean repoExits = false;
        RepositoryProvider provider = Util.getRepositoryProvider(type);
        if (provider != null) {
            repoExits = provider.repoExists(applicationKey, tenantDomain);
        } else {
            handleException(new StringBuilder().append("Repository provider for the type ")
                                               .append(type).append(" not found").toString());
        }
        return repoExits;
    }

    /**
     * 
     * 
     * @param applicationKey
     * @param userName
     * @param type
     * @return
     * @throws RepositoryMgtException
     */
    public String createFork(String applicationKey, String userName, String type)
                                                                                 throws RepositoryMgtException {

        String forkedRepoURL = null;
        RepositoryProvider provider = Util.getRepositoryProvider(type);
        if (provider != null) {
            forkedRepoURL = provider.createForkRepo(applicationKey, userName);
            String title = applicationKey + " was forked by " + userName;
            try {
                EventNotifier.getInstance().notify(RepoCreationEventBuilderUtil.buildCreateForkRepoEvent(applicationKey, title, "", Event.Category.INFO));
            } catch (AppFactoryEventException e) {
                log.error("Failed to notify application fork event " + e.getMessage(), e);
            }
        } else {
            handleException(new StringBuilder().append("Repository provider for the type ")
                                               .append(type).append(" not found").toString());
        }

        return forkedRepoURL;

    }

    /**
     * Adding new branch to the existing forked repository
     * 
     * @param applicationKey
     * @param userName
     * @param type
     * @param version
     * @throws RepositoryMgtException
     */
    public void createForkBranch(String applicationKey, String userName, String type, String version)
                                                                                                     throws RepositoryMgtException {

        RepositoryProvider provider = Util.getRepositoryProvider(type);
        if (provider != null) {
            provider.forkBranch(applicationKey, userName, version);
        } else {
            handleException(new StringBuilder().append("Repository provider for the type ")
                                               .append(type).append(" not found").toString());
        }

    }
}
