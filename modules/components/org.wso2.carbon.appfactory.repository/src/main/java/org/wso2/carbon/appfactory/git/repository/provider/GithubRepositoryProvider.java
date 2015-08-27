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
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryMgtException;
import org.wso2.carbon.appfactory.repository.mgt.client.AppfactoryRepositoryClient;
import org.wso2.carbon.appfactory.repository.mgt.internal.Util;
import org.wso2.carbon.appfactory.repository.provider.common.AbstractRepositoryProvider;
import org.wso2.carbon.appfactory.repository.provider.common.bean.Permission;
import org.wso2.carbon.appfactory.repository.provider.common.bean.PermissionType;
import org.wso2.carbon.appfactory.repository.provider.common.bean.Repository;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


/**
 * repository manager implementation for github
 */
public class GithubRepositoryProvider extends AbstractRepositoryProvider {
    private static final Log log = LogFactory.getLog(GithubRepositoryProvider.class);

    public static final String GITHUB_AUTH_TOKEN =
            "RepositoryProviderConfig.git.Property.GithubAdminAuthToken";
    public static final String GITHUB_ORG =
            "RepositoryProviderConfig.git.Property.GithubOrganization";
    public static final String TYPE = "git";
    public static final String REPOSITORY_PROVIDER_CONFIG_GIT_PROPERTY_GITHUB_ADMIN_USER_NAME =
            "RepositoryProviderConfig.git.Property.GithubAdminUserName";
    public static final String REPOSITORY_PROVIDER_CONFIG_GIT_PROPERTY_GITHUB_ADMIN_PASSWORD =
            "RepositoryProviderConfig.git.Property.GithubAdminPassword";
    public static final String GITHUB_API = "https://api.github.com";

    AppFactoryConfiguration appFactoryConfiguration = Util.getConfiguration();

    private String githubAuthtoken = appFactoryConfiguration.getFirstProperty(GITHUB_AUTH_TOKEN);
    private String ORG_NAME = appFactoryConfiguration.getFirstProperty(GITHUB_ORG);

    /**
     * {@inheritDoc}
     */
    @Override
    public String createRepository(String applicationKey, String tenantDomain) throws RepositoryMgtException {

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

        HttpClient client = new HttpClient();
        PostMethod post = new PostMethod("https://api.github.com/orgs/" + ORG_NAME + "/repos");

        post.setDoAuthentication(true);
        post.addRequestHeader("Authorization", "Basic " + githubAuthtoken);

        StringRequestEntity requestEntity;
        try {
            requestEntity = new StringRequestEntity(
                    "  {\n  \"name\":\"" + applicationKey + "\",\n\"auto_init\":\"true\"\n}",
                    "application/json",
                    "UTF-8");
        } catch (UnsupportedEncodingException e) {
            String msg = "Error while invoking gitHub API";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        }
        post.setRequestEntity(requestEntity);

        try {
            client.executeMethod(post);
            log.debug("HTTP status " + post.getStatusCode()
                      + " creating repo\n\n");

            if (post.getStatusCode() == HttpStatus.SC_CREATED) {
                log.debug("Repository creation successful");
            } else if (post.getStatusCode() == HttpStatus.SC_UNPROCESSABLE_ENTITY) {
                String msg = "Repository creation is failed for" + applicationKey +
                             ". Repository with the same name already exists";
                log.error(msg);
                throw new RepositoryMgtException(msg);
            } else {
                String msg = "Repository creation is failed for" + applicationKey +
                             "Server returned status:" + post.getStatusCode();
                log.error(msg);
                throw new RepositoryMgtException(msg);
            }

        } catch (IOException e) {
            String msg = "Error while invoking gitHub API";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        } finally {
            post.releaseConnection();
        }

        createTeam(applicationKey);
        return getAppRepositoryURL(applicationKey, tenantDomain);
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

    /**
     * @param applicationKey for the created app
     * @param tenantDomain Tenant domain of application
     * @return URL for the repo
     * @throws RepositoryMgtException in an error
     */
    @Override
    public String getAppRepositoryURL(String applicationKey, String tenantDomain) throws RepositoryMgtException {
        return "https://github.com/" + ORG_NAME + "/" + tenantDomain + "/" + applicationKey + ".git";
    }

    /**
     * @return client for accessing repo
     * @throws RepositoryMgtException if client initilaization fails
     *                                <p/>
     *                                This method has been overridden since the username and password properties are different from others
     */
    @Override
    public AppfactoryRepositoryClient getRepositoryClient() throws RepositoryMgtException {
        //this.appfactoryRepositoryClient = null;
        // this.appfactoryRepositoryClient = new AppfactoryRepositoryClient(getType());
        try {
            this.appfactoryRepositoryClient.init(appFactoryConfiguration.getFirstProperty(REPOSITORY_PROVIDER_CONFIG_GIT_PROPERTY_GITHUB_ADMIN_USER_NAME),
                                                 appFactoryConfiguration.getFirstProperty(REPOSITORY_PROVIDER_CONFIG_GIT_PROPERTY_GITHUB_ADMIN_PASSWORD));

        } catch (RepositoryMgtException e) {
            String msg = "Error while invoking the service";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        }
        return this.appfactoryRepositoryClient;
    }

    protected String getType() {
        return TYPE;
    }

    @Override
    public void provisionUser(String applicationKey, String username)
            throws RepositoryMgtException {

        if (!isUserRegisteredInGit(username)) {
            String msg = "User:" + username + " account is not registered with github.";
            log.error(msg);
            throw new RepositoryMgtException(msg);
        }
        String teamName = "team_" + applicationKey;
        String teamId = getTeamId(teamName);
        if (teamId == null) {
            String msg = "Team is not created for application:" + applicationKey + " in github.";
            log.error(msg);
            throw new RepositoryMgtException(msg);
        }
        HttpClient client = new HttpClient();
        PutMethod put = new PutMethod("https://api.github.com/teams/" + teamId + "/members/" + username);

        put.setDoAuthentication(true);
        put.addRequestHeader("Authorization", "Basic " + githubAuthtoken);


        try {
            client.executeMethod(put);
            log.debug("HTTP status " + put.getStatusCode() + " creating repo\n\n");

            if (put.getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                log.debug("Repository creation successful");
            } else {
                String msg = "provisioning user:" + username + " is failed for" + applicationKey +
                             "Server returned status:" + put.getStatusCode();
                log.error(msg);
                throw new RepositoryMgtException(msg);
            }

        } catch (IOException e) {
            String msg = "Error while invoking gitHub API";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        } finally {
            put.releaseConnection();
        }
    }

    private String getTeamId(String teamName) throws RepositoryMgtException {
        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod("https://api.github.com/orgs/" + ORG_NAME + "/teams");

        get.setDoAuthentication(true);
        get.addRequestHeader("Authorization", "Basic " + githubAuthtoken);
        try {
            client.executeMethod(get);
            if (get.getStatusCode() == HttpStatus.SC_OK) {

                String responseBody = get.getResponseBodyAsString();

                JSONParser parser = new JSONParser();

                JSONArray jsonArray = (JSONArray) parser.parse(responseBody);
                if (jsonArray != null) {
                    for (Object object : jsonArray) {
                        if (object instanceof JSONObject) {
                            JSONObject jsonObject = (JSONObject) object;
                            String name = (String) jsonObject.get("name");
                            if (teamName.equals(name)) {
                                Long teamId = (Long) jsonObject.get("id");
                                return Long.toString(teamId);
                            }
                        }
                    }
                }
            }

        } catch (IOException e) {
            String msg = "Error while invoking gitHub API";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        } catch (ParseException e) {
            String msg = "Error while parsing github team names";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        } finally {
            get.releaseConnection();
        }
        return null;
    }

    public boolean isUserRegisteredInGit(String username) throws RepositoryMgtException {

        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod("https://api.github.com/users/" + username);

        try {
            client.executeMethod(get);
            return get.getStatusCode() == HttpStatus.SC_OK;

        } catch (IOException e) {
            String msg = "Error while invoking gitHub API";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        } finally {
            get.releaseConnection();
        }
    }

    private void createTeam(String applicationKey) throws RepositoryMgtException {
        HttpClient client = new HttpClient();
        PostMethod post = new PostMethod("https://api.github.com/orgs/" + ORG_NAME + "/teams");
       // https://api.github.com/teams/627395/members/manishagele

        post.setDoAuthentication(true);
        post.addRequestHeader("Authorization", "Basic " + githubAuthtoken);

        StringRequestEntity requestEntity;
        try {
            requestEntity = new StringRequestEntity(
                    "{\n" +
                    "  \"name\": \"team_" + applicationKey + "\",\n" +
                    "  \"permission\": \"push\",\n" +
                    "  \"repo_names\": [\n" +
                    "    \"" + ORG_NAME + "/" + applicationKey + "\"\n" +
                    "  ]\n" +
                    "}",
                    "application/json",
                    "UTF-8");
        } catch (UnsupportedEncodingException e) {
            String msg = "Error while invoking gitHub API";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        }
        post.setRequestEntity(requestEntity);

        try {
            client.executeMethod(post);
            log.debug("HTTP status " + post.getStatusCode()
                      + " creating team\n\n");

            if (post.getStatusCode() == HttpStatus.SC_CREATED) {
                log.debug("team creation successful");
            } else if (post.getStatusCode() == HttpStatus.SC_UNPROCESSABLE_ENTITY) {
                String msg = "team creation is failed for" + applicationKey +
                             ". team with the same name already exists";
                log.error(msg);
                throw new RepositoryMgtException(msg);
            } else {
                String msg = "team creation is failed for" + applicationKey +
                             "Server returned status:" + post.getStatusCode();
                log.error(msg);
                throw new RepositoryMgtException(msg);
            }

        } catch (IOException e) {
            String msg = "Error while invoking gitHub API";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        } finally {
            post.releaseConnection();
        }
    }

    public boolean addMembersToTeam(String teamId, String memberName){
        HttpClient client = new HttpClient();
        PostMethod post = new PostMethod("/orgs/" + ORG_NAME + "/teams");
       // https://api.github.com/teams/627395/members/manishagele

        post.setDoAuthentication(true);
        post.addRequestHeader("Authorization", "Basic " + githubAuthtoken);
       return true;
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
