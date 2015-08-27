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
package org.wso2.carbon.appfactory.webdavsvn.svn.repository.provider;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryMgtException;
import org.wso2.carbon.appfactory.repository.provider.common.AbstractRepositoryProvider;

import java.io.IOException;

/**
 * Repository manager for Apache2+mod_dav+mod_svn setup
 */
public class Apache2WebDAVSVNRepositoryProvider extends AbstractRepositoryProvider {

    public static final String BASE_URL = "BaseURL";
    public static final String BACKEND_URL = "BackendURL";
    public static final String CREATE_REPO_EPR = "/createRepo.php";
    public static final String CREATE_REPO_REQUEST_REPO_NAME_PARAMETER_NAME = "name";
    public static final String REPO_TYPE = "svn";

    /*    public static final String HOST_NAME="RemoteHostName";
public static final String REMOTE_USER_NAME="RemoteUserName";
public static final String REMOTE_USER_PASSWORD="RemoteUserPassword";
public static final String REMOTE_PARENT_REPO_LOCATION="RemoteRepositoryParentLocation";
public static final String REMOTE_HOST_SUPER_USER_PASSWORD="RemoteHostSuperUserPassword";*/
    /* private SSHClient client;*/
    private static final Log log = LogFactory.getLog(Apache2WebDAVSVNRepositoryProvider.class);

    public Apache2WebDAVSVNRepositoryProvider() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createRepository(String applicationKey, String tenantDomain) throws RepositoryMgtException {
        /*String fullRepositoryPath=configuration.getFirstProperty(getPropertyKey(REMOTE_PARENT_REPO_LOCATION))+"/"+applicationKey;
        String username=configuration.getFirstProperty(getPropertyKey(REMOTE_USER_NAME));
        String password=configuration.getFirstProperty(getPropertyKey(REMOTE_USER_PASSWORD));
        String hostName=configuration.getFirstProperty(getPropertyKey(HOST_NAME));
        client=new SSHClient();
        client.createRepositoryRemotely(fullRepositoryPath,configuration.getFirstProperty(getPropertyKey(REMOTE_HOST_SUPER_USER_PASSWORD)),username,password,hostName);
        return getAppRepositoryURL(applicationKey);*/

        HttpClient client = getClient();
        GetMethod get = new GetMethod(getBackendUrl() + CREATE_REPO_EPR);
        NameValuePair[] repositoryName = new NameValuePair[1];
        repositoryName[0] = new NameValuePair();
        repositoryName[0].setName(CREATE_REPO_REQUEST_REPO_NAME_PARAMETER_NAME);
        repositoryName[0].setValue(applicationKey);
        get.setQueryString(repositoryName);
        get.setDoAuthentication(true);
        try {
            client.executeMethod(get);
            if (get.getStatusCode() == HttpStatus.SC_CREATED) {
                return getAppRepositoryURL(applicationKey, tenantDomain);
            } else {
                String msg = "Repository creation is failed for " + applicationKey
                             + " server returned status " +
                             get.getStatusText();
                log.error(msg);
                throw new RepositoryMgtException(msg);
            }
        } catch (IOException e) {
            String msg = "Error while invoking the web service";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        } finally {
            get.releaseConnection();
        }
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
    public boolean repoExists(String applicationKey, String tenantDomain) throws RepositoryMgtException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public String getAppRepositoryURL(String applicationKey, String tenantDomain) throws RepositoryMgtException {
        return getConfig().getFirstProperty(getPropertyKey(BASE_URL)) + "/" + REPO_TYPE +
                "/" + tenantDomain + "/" +applicationKey;
    }

    protected String getPropertyKey(String name) {
        StringBuilder key = new StringBuilder(AppFactoryConstants.REPOSITORY_PROVIDER_CONFIG);
        key.append(".").append(REPO_TYPE).append(".").append("Property").append(".").append(name);
        return key.toString();
    }

    private String getBackendUrl() {
        return getConfig().getFirstProperty(getPropertyKey(BACKEND_URL));
    }

    @Override
    protected String getType() {
        return REPO_TYPE;
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
