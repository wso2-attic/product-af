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
package org.wso2.carbon.appfactory.repository.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.repository.mgt.internal.Util;

/**
 * This is a class to manage all the repository operation by getting relevant repository provider
 * .
 */
public class RepositoryManager {
    private static final Log log = LogFactory.getLog(RepositoryManager.class);

    /**
     * Create a repository
     * @param applicationKey Application key
     * @param type Repository type
     * @param tenantDomain  Tenant domain of repository
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
            handleException((new StringBuilder()).
                    append("Repository provider for the type ").
                    append(type).
                    append(" not found").toString());
        }

        return url;
    }

    public boolean deleteRepository(String applicationKey, String type, String tenantDomain)
            throws RepositoryMgtException {
        boolean deleted = false;
        RepositoryProvider provider = Util.getRepositoryProvider(type);
        if (provider != null) {
            deleted = provider.deleteRepository(applicationKey, tenantDomain);
        } else {
            handleException((new StringBuilder()).
                    append("Repository provider for the type ").
                    append(type).
                    append(" not found").toString());
        }

        return deleted;
    }

    private void handleException(String msg) throws RepositoryMgtException {
        log.error(msg);
        throw new RepositoryMgtException(msg);
    }

    public String getAppRepositoryURL(String appId, String type, String tenantName) throws RepositoryMgtException {
        RepositoryProvider provider = Util.getRepositoryProvider(type);
        if (provider != null) {
            return provider.getAppRepositoryURL(appId, tenantName);
        } else {
            handleException((new StringBuilder()).
                    append("Repository provider for the type ").
                    append(type).
                    append(" not found").toString());
        }
        return null;
    }

    public String getURLForAppversion(String applicationKey, String version, String type, String tenantName)
            throws RepositoryMgtException {
        RepositoryProvider provider = Util.getRepositoryProvider(type);

        if (provider != null) {
            return provider.getBranchingStrategy().getURLForAppVersion(applicationKey, version, tenantName);
        } else {
            handleException((new StringBuilder()).
                    append("Repository provider for the type ").
                    append(type).
                    append(" not found").toString());
        }
        return null;
    }

    public void branch(String appId, String type, String currentVersion, String targetVersion,
                       String currentRevision, String tenantDomain) throws RepositoryMgtException {
        RepositoryProvider provider = Util.getRepositoryProvider(type);
        if (provider != null) {
            provider.getBranchingStrategy().doRepositoryBranch(appId, currentVersion, targetVersion,
                                                               currentRevision, tenantDomain);
        } else {
            handleException((new StringBuilder()).
                    append("Repository provider for the type ").
                    append(type).
                    append(" not found").toString());
        }

    }

    public void tag(String appId, String type, String currentVersion, String targetVersion,
                    String currentRevision, String tenantDomain) throws RepositoryMgtException {
        RepositoryProvider provider = Util.getRepositoryProvider(type);
        if (provider != null) {
            Util.getRepositoryProvider(type).getBranchingStrategy().doRepositoryTag(appId, currentVersion, targetVersion, currentRevision, tenantDomain);
        } else {
            handleException((new StringBuilder()).
                    append("Repository provider for the type ").
                    append(type).
                    append(" not found").toString());
        }
    }

    public RepositoryProvider getRepositoryProvider(String type) {
        return Util.getRepositoryProvider(type);
    }

    public void provisionUser(String applicationKey, String type, String username)
            throws RepositoryMgtException {
        RepositoryProvider provider = Util.getRepositoryProvider(type);
        if (provider != null) {
            provider.provisionUser(applicationKey, username);
        } else {
            handleException((new StringBuilder()).
                    append("Repository provider failed to provision user").
                    append(username).
                    append(" for the type ").
                    append(type).
                    append(" not found").toString());
        }
    }
    public Boolean createTenantRepo(String tenantId,String type) throws RepositoryMgtException {
        Boolean result = null;
        RepositoryProvider provider = Util.getRepositoryProvider(type);
        if (provider != null) {
            result = provider.createTenantRepo(tenantId);
        } else {
            handleException((new StringBuilder()).
                    append("Repository provider for the type ").
                    append(type).
                    append(" not found").toString());
        }

        return result;
        
    }
    
    public Boolean deleteTenantRepo(String tenantId,String type) throws RepositoryMgtException {
        Boolean result = null;
        RepositoryProvider provider = Util.getRepositoryProvider(type);
        if (provider != null) {
            result = provider.deleteTenantRepo(tenantId);
        } else {
            handleException((new StringBuilder()).
                    append("Repository provider for the type ").
                    append(type).
                    append(" not found").toString());
        }
        return result;
        
    }

    public boolean repositoryExists(String applicationKey, String type, String tenantDomain) throws RepositoryMgtException {
        boolean repoExits = false;
        RepositoryProvider provider = Util.getRepositoryProvider(type);
        if (provider != null) {
            repoExits = provider.repoExists(applicationKey, tenantDomain);
        } else {
            handleException((new StringBuilder()).
                    append("Repository provider for the type ").
                    append(type).
                    append(" not found").toString());
        }
        return repoExits;
    }

    public String createFork(String parentRepoUrl, String userName, String type) throws RepositoryMgtException {

        String forkedRepoURL = null;
        RepositoryProvider provider = Util.getRepositoryProvider(type);
        if (provider != null) {
            forkedRepoURL = provider.createForkRepo(parentRepoUrl, userName);
        } else {
            handleException((new StringBuilder()).
                    append("Repository provider for the type ").
                    append(type).
                    append(" not found").toString());
        }

        return forkedRepoURL;

    }
}
