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

package org.wso2.carbon.appfactory.git.repository.provider;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryMgtException;
import org.wso2.carbon.appfactory.repository.provider.common.AbstractRepositoryProvider;
import org.wso2.carbon.appfactory.repository.provider.common.bean.Permission;
import org.wso2.carbon.appfactory.repository.provider.common.bean.PermissionType;
import org.wso2.carbon.appfactory.repository.provider.common.bean.Repository;

import java.io.IOException;
import java.util.ArrayList;

/**
 * SCM-manager specific repository manager implementation for git
 */
public class SCMManagerBasedGITRepositoryProvider extends AbstractRepositoryProvider {
    private static final Log log = LogFactory.getLog(SCMManagerBasedGITRepositoryProvider.class);

    public static final String REST_GET_REPOSITORY_URI = "/repositories/git/";
    public static final String TYPE = "git";

    /**
     * {@inheritDoc}
     */
    @Override
    public String createRepository(String applicationKey, String tenantDomain) throws RepositoryMgtException {

        HttpClient client = getClient();
        PostMethod post = new PostMethod(getServerURL() + REST_BASE_URI +
                                         REST_CREATE_REPOSITORY_URI);
        Repository repository = new Repository();
        repository.setName(applicationKey);
        repository.setType("git");

        Permission permission = new Permission();
        permission.setGroupPermission(true);
        permission.setName(applicationKey);
        permission.setType(PermissionType.WRITE);
        ArrayList<Permission> permissions = new ArrayList<Permission>();
        permissions.add(permission);

        repository.setPermissions(permissions);


        post.setRequestEntity(new ByteArrayRequestEntity(getRepositoryAsString(repository)));
        post.setDoAuthentication(true);
        post.addRequestHeader("Content-Type", "application/xml;charset=UTF-8");

        String url;
        try {
            client.executeMethod(post);
        } catch (IOException e) {
            String msg = "Error while invoking the web service";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        } finally {
            post.releaseConnection();
        }
        if (post.getStatusCode() == HttpStatus.SC_CREATED) {
            url = getAppRepositoryURL(applicationKey, tenantDomain);
        } else {
            String msg = "Repository creation is failed for " + applicationKey +
                         " server returned status " + post.getStatusText();
            log.error(msg);
            throw new RepositoryMgtException(msg);
        }

        return url;
    }

    @Override
    public boolean deleteRepository(String applicationKey, String tenantDomain) throws RepositoryMgtException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean deleteForkedRepository(String applicationKey, String userName, String tenantDomain) throws RepositoryMgtException {
        return false;
    }

	@Override
	public boolean deleteForkedRepositoriesForApplication(String applicationKey, String tenantDomain)
			throws RepositoryMgtException {
		return false;
	}

	@Override
    public boolean repoExists(String applicationKey, String tenantDomain) throws RepositoryMgtException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAppRepositoryURL(String applicationKey, String tenantDomain) throws RepositoryMgtException {
        HttpClient client = getClient();
        GetMethod get = new GetMethod(getServerURL() + REST_BASE_URI + REST_GET_REPOSITORY_URI
                                      + applicationKey);
        get.setDoAuthentication(true);
        get.addRequestHeader("Content-Type", "application/xml;charset=UTF-8");
        String repository = null;
        try {
            client.executeMethod(get);
            if (get.getStatusCode() == HttpStatus.SC_OK) {
                repository = getRepositoryFromStream(get.getResponseBodyAsStream()).getUrl();
            } else if (get.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                if (log.isDebugEnabled()) {
                    log.debug("Repository is not found " + applicationKey);
                }
            } else {
                String msg = "Repository action is failed for " + applicationKey +
                             " server returned status " + get.getStatusText();
                log.error(msg);
                throw new RepositoryMgtException(msg);
            }
        } catch (IOException e) {
            String msg = "Error while invoking the service";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        } finally {
            HttpConnectionManager manager = client.getHttpConnectionManager();
            if (manager instanceof SimpleHttpConnectionManager) {
                ((SimpleHttpConnectionManager) manager).shutdown();
            }
        }
        return repository;
    }

    /**
     * @return client for accessing repo
     * @throws RepositoryMgtException if client initilaization fails
     */

//    Unwanted since the abstract class has the implementation
    /*@Override
    public AppfactoryRepositoryClient getRepositoryClient() throws RepositoryMgtException {
        this.appfactoryRepositoryClient = null;
        this.appfactoryRepositoryClient = new AppfactoryRepositoryClient(getType());
        try {
            this.appfactoryRepositoryClient.init(Util.getConfiguration().getFirstProperty(AppFactoryConstants.SERVER_ADMIN_NAME),
                                                 Util.getConfiguration().getFirstProperty(AppFactoryConstants.SERVER_ADMIN_PASSWORD));
        } catch (RepositoryMgtException e) {
            String msg = "Error while invoking the service";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        }
        return this.appfactoryRepositoryClient;
    }
*/
    @Override
    protected String getType() {
        return TYPE;
    }

    @Override
    public boolean createTenantRepo(String tenantId) throws RepositoryMgtException {
        // TODO Implement repo creation for tenant here
        return false;
    }

	@Override
	public boolean deleteTenantRepo(String tenantId)
			throws RepositoryMgtException {
		 // TODO Implement repo deletion for tenant here
		return false;
	}

    @Override
    public void deleteStratosArtifactRepository(String repoName) throws RepositoryMgtException {

    }

    @Override
    public String createForkRepo(String applicationKey, String userName) throws RepositoryMgtException {
        return null;
    }

    @Override
    public String getForkedAppRepositoryURL(String repoName, String tenantDomain, String userId) throws RepositoryMgtException {
        return null;
    }

	@Override
	public void forkBranch(String applicationKey, String userName, String branch)
			throws RepositoryMgtException {
		// TODO Auto-generated method stub
		
	}
}
