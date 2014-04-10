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

import com.gitblit.Constants;
import com.gitblit.models.RepositoryModel;
import com.gitblit.models.UserModel;
import com.gitblit.utils.RpcUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryMgtException;
import org.wso2.carbon.appfactory.repository.provider.common.AbstractRepositoryProvider;
import org.wso2.carbon.context.CarbonContext;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * GITBlit specific repository manager implementation for git
 */
public class GITBlitBasedGITRepositoryProvider extends AbstractRepositoryProvider {
    private static final Log log = LogFactory.getLog(GITBlitBasedGITRepositoryProvider.class);

    public static final String BASE_URL = "RepositoryProviderConfig.git.Property.BaseURL";
    public static final String GITBLIT_ADMIN_USERNAME =
            "RepositoryProviderConfig.git.Property.AdminUserName";
    public static final String GITBLIT_ADMIN_PASS =
            "RepositoryProviderConfig.git.Property.AdminPassword";
    public static final String REPO_TYPE = "git";

    private boolean isCreated = true;
    private boolean isDeleted = true;
    private boolean isForked  = true;

    public static final String TYPE = "git";

    /**
     * {@inheritDoc}
     */
    @Override
    public String createRepository(String applicationKey, String tenantDomain) throws RepositoryMgtException {
        String repoName=tenantDomain+"/"+applicationKey + ".git";
        String repoCreateUrl = config.getFirstProperty(BASE_URL);
        String adminUsername = config.getFirstProperty(GITBLIT_ADMIN_USERNAME);
        String adminPassword = config.getFirstProperty(GITBLIT_ADMIN_PASS);
        //Create the gitblit repository model
        RepositoryModel model = new RepositoryModel();
        model.name = repoName;
        //authenticated users can clone, push and view the repository
        model.accessRestriction = Constants.AccessRestrictionType.VIEW;
        model.isBare=true; // TODO: temporaryly added for demo purpose, need to fixed with new gitblit
        try {
            isCreated = RpcUtils.createRepository(model, repoCreateUrl, adminUsername,
                                                  adminPassword.toCharArray());
            if (isCreated) {
                String url = getAppRepositoryURL(applicationKey, tenantDomain);
                return url;
            } else {
                String msg = "Repository is not created for " + applicationKey + " due to remote server error";
                log.error(msg);
                throw new RepositoryMgtException(msg);
            }
        } catch (IOException e) {
            String msg = "Repository is not created for " + applicationKey + " due to " + e.getLocalizedMessage();
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        }
      
    }

    @Override
    public boolean deleteRepository(String applicationKey, String tenantDomain) throws RepositoryMgtException {
        CarbonContext ct=CarbonContext.getThreadLocalCarbonContext();
        String repoName=tenantDomain + "/" + applicationKey + ".git";
        String repoUrl = config.getFirstProperty(BASE_URL);
        String adminUsername = config.getFirstProperty(GITBLIT_ADMIN_USERNAME);
        String adminPassword = config.getFirstProperty(GITBLIT_ADMIN_PASS);
        //Create the gftblit repository model
        RepositoryModel model = new RepositoryModel();
        model.name = repoName;
        //authenticated users can clone, push and view the repository
        model.accessRestriction = Constants.AccessRestrictionType.VIEW;
        model.isBare=true; // TODO: temporaryly added for demo purpose, need to fixed with new gitblit
        try {
            RepositoryModel retrievedRepo = findRepository(model.name, repoUrl, adminUsername, adminPassword);
            isDeleted = RpcUtils.deleteRepository(retrievedRepo, repoUrl, adminUsername,
                    adminPassword.toCharArray());
        } catch (IOException e) {
            String msg = "Repository is not deleted for " + applicationKey + " due to " + e.getLocalizedMessage();
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        }
        return isDeleted;
    }

    @Override
    public boolean repoExists(String applicationKey, String tenantDomain) throws RepositoryMgtException {
        //TODO implement method
        RepositoryModel retrievedRepo = null;
        CarbonContext ct=CarbonContext.getThreadLocalCarbonContext();
        String repoName=tenantDomain + "/" + applicationKey + ".git";
        String repoUrl = config.getFirstProperty(BASE_URL);
        String adminUsername = config.getFirstProperty(GITBLIT_ADMIN_USERNAME);
        String adminPassword = config.getFirstProperty(GITBLIT_ADMIN_PASS);
        //Create the gftblit repository model
        RepositoryModel model = new RepositoryModel();
        model.name = repoName;
        //authenticated users can clone, push and view the repository
        model.accessRestriction = Constants.AccessRestrictionType.VIEW;
        model.isBare=true; // TODO: temporaryly added for demo purpose, need to fixed with new gitblit
        try {
            retrievedRepo = findRepository(model.name, repoUrl, adminUsername, adminPassword);
        } catch (IOException e) {
            String msg = "Repository is not deleted for " + applicationKey + " due to " + e.getLocalizedMessage();
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        }
        return retrievedRepo != null;
    }

    private RepositoryModel findRepository(String name, String url, String account, String password) throws IOException {
        Map<String, RepositoryModel> repositories = RpcUtils.getRepositories(url, account, password.toCharArray());
        RepositoryModel retrievedRepository = null;
        for (RepositoryModel model : repositories.values()) {
            if (model.name.equalsIgnoreCase(name)) {
                retrievedRepository = model;
                break;
            }
        }
        return retrievedRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAppRepositoryURL(String applicationKey, String tenantDomain) throws RepositoryMgtException {
        return config.getFirstProperty(BASE_URL) + REPO_TYPE + "/" + tenantDomain + "/" +applicationKey + ".git";
    }


    public void createFork(String repoUrl) {
        RepositoryModel repositoryModel = new RepositoryModel("repo2", "repo2 description", "admin", null);
        //RepositoryModel repositoryModel = new RepositoryModel("repo1", "repo1 description", "admin", null);


        UserModel userModel = new UserModel("user2");


        Boolean isforked = null;
        try {
            isforked = RpcUtils.forkRpository(repositoryModel, userModel, repoUrl, "admin", "admin".toCharArray());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        System.out.println("Is Forked: " + isforked);
    }

    /**
     * Get the forked repos of the logged in user
     * @param username
     * @param password
     * @return
     */
    private List<RepositoryModel> getForkedReposOfUser(String username, String password) throws RepositoryMgtException {
        List<RepositoryModel> forkedRepos = null;
        String repoBaseUrl = config.getFirstProperty(BASE_URL);
        Map<String, RepositoryModel> map = null;
        try {
            map = RpcUtils.getRepositories(repoBaseUrl, username, password.toCharArray());
        } catch (IOException e) {
            String msg =
                    "Error while getting repositories due to "  + e.getLocalizedMessage();
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        }
        for (Map.Entry<String, RepositoryModel> entry : map.entrySet())
        {
            Boolean isOwner = entry.getValue().isOwner(username);
            if(isOwner){
                forkedRepos.add(entry.getValue());
            }

        }
        return forkedRepos;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected String getType() {
        return TYPE;
    }
    
    public boolean createTenantRepo(String tenantId) throws RepositoryMgtException {
        String defaultTenantRepo=tenantId+"/defApp";
        String repoCreateUrl =
                               config.getFirstProperty(BASE_URL) +
                                       "rpc?req=CREATE_REPOSITORY&name=/" + defaultTenantRepo;
        String repoDeleteUrl =
                config.getFirstProperty(BASE_URL) +
                        "rpc?req=DELETE_REPOSITORY&name=/" + defaultTenantRepo;
        String adminUsername = config.getFirstProperty(GITBLIT_ADMIN_USERNAME);
        String adminPassword = config.getFirstProperty(GITBLIT_ADMIN_PASS);

        // Create the gitblit repository model
        RepositoryModel model = new RepositoryModel();
        model.name = defaultTenantRepo;
        // authenticated users can clone, push and view the repository
        model.accessRestriction = Constants.AccessRestrictionType.VIEW;
        try {
            isCreated =
                        RpcUtils.createRepository(model, repoCreateUrl, adminUsername,
                                                  adminPassword.toCharArray());

            if (isCreated) {
                //String url = getAppRepositoryURL(defaultTenantRepo);
                RpcUtils.deleteRepository(model, repoDeleteUrl, adminUsername,
                                          adminPassword.toCharArray());

                return true;
            } else {
                String msg =
                             "Tenant Repsitory is not created for " + tenantId +
                                     " due to remote server error";
                log.error(msg);
                throw new RepositoryMgtException(msg);
                

            }
        } catch (IOException e) {
            String msg =
                         "Tenant Repsitory is not created for " + tenantId + " due to " +
                                 e.getLocalizedMessage();
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        }

         

    }

	@Override
	public boolean deleteTenantRepo(String tenantId)
			throws RepositoryMgtException {
		   String defaultTenantRepo=tenantId+"/defApp";
	        String repoDeleteUrl =
	                config.getFirstProperty(BASE_URL) +
	                        "rpc?req=DELETE_REPOSITORY&name=/" + defaultTenantRepo;
	        String adminUsername = config.getFirstProperty(GITBLIT_ADMIN_USERNAME);
	        String adminPassword = config.getFirstProperty(GITBLIT_ADMIN_PASS);
	        // Create the gitblit repository model
	        RepositoryModel model = new RepositoryModel();
	        model.name = defaultTenantRepo;
	        // authenticated users can clone, push and view the repository
	        model.accessRestriction = Constants.AccessRestrictionType.VIEW;
	        try {
	        	 boolean isDeleted = RpcUtils.deleteRepository(model, repoDeleteUrl, adminUsername,
	                                          adminPassword.toCharArray());
	        	 if(!isDeleted) {
	        		log.info("Tenant Repsitory is not deleted for "+tenantId);
	        	 } 
	        	 return isDeleted;
	        } catch (IOException e) {
	            String msg = "Tenant Repsitory is not deleted for " + tenantId + " due to " +
	                                 e.getLocalizedMessage();
	            log.error(msg, e);
	            throw new RepositoryMgtException(msg, e);
	        }
	}

    @Override
    public String createForkRepo(String parentRepoUrl, String userName)
            throws RepositoryMgtException {
        RepositoryModel repositoryModel = new RepositoryModel();
        //https://user2@127.0.0.1:8443/r/~user2/repo1.git
        String[] tmp = parentRepoUrl.split("/git/");
        repositoryModel.name = tmp[1];
        String repoName = repositoryModel.name.split("/")[1];

        String adminUsername = config.getFirstProperty(GITBLIT_ADMIN_USERNAME);
        String adminPassword = config.getFirstProperty(GITBLIT_ADMIN_PASS);
        String repoUrl = config.getFirstProperty(BASE_URL);
        UserModel userModel = new UserModel(userName);
        String[] temp = userName.split("@");
        String userId = temp[0];
        String tenantId = temp[1];
        userModel.username = tenantId + "/" + userId;
        try {
            isForked = RpcUtils.forkRpository(repositoryModel, userModel, repoUrl,adminUsername,
                                    adminPassword.toCharArray());
            if (isForked) {
                String url = getForkedAppRepositoryURL(repoName, tenantId, userId);
                return url;
            } else {
                String msg = "Forked repository was not created for " + userName + " due to remote server error";
                log.error(msg);
                throw new RepositoryMgtException(msg);
            }
        } catch (IOException e) {
            String msg = "Forked repository was not created for " + userName + " due to " +
                    e.getLocalizedMessage();
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        }
    }

    @Override
    public String getForkedAppRepositoryURL(String repoName, String tenantDomain, String userId) throws RepositoryMgtException {
        return config.getFirstProperty(BASE_URL) + REPO_TYPE + "/~" + tenantDomain + "/" +userId + "/" + repoName;
    }


}
