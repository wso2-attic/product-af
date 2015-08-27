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

package org.wso2.carbon.appfactory.git.repository.provider;

import com.gitblit.Constants;
import com.gitblit.models.RepositoryModel;
import com.gitblit.models.UserModel;
import com.gitblit.utils.RpcUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryMgtException;
import org.wso2.carbon.appfactory.repository.mgt.client.AppfactoryRepositoryClient;
import org.wso2.carbon.appfactory.repository.provider.common.AbstractRepositoryProvider;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * GITBlit specific repository manager implementation for git
 */
public class GITBlitBasedGITRepositoryProvider extends AbstractRepositoryProvider {
	private static final Log log = LogFactory.getLog(GITBlitBasedGITRepositoryProvider.class);

	public static final String BASE_URL = "RepositoryProviderConfig.git.Property.BaseURL";
	public static final String REPO_TYPE = "git";

	private boolean isCreated = true;

    public static final String TYPE = "git";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String createRepository(String applicationKey, String tenantDomain)
	                                                                          throws RepositoryMgtException {
		String repoName = tenantDomain + "/" + applicationKey + ".git";
		String repoCreateUrl = config.getFirstProperty(BASE_URL);
		String adminUsername = config.getFirstProperty(AppFactoryConstants.GITBLIT_ADMIN_USERNAME);
		String adminPassword = config.getFirstProperty(AppFactoryConstants.GITBLIT_ADMIN_PASSWORD);
		// Create the gitblit repository model
		RepositoryModel model = new RepositoryModel();
		model.name = repoName;
		// authenticated users can clone, push and view the repository
		model.accessRestriction = Constants.AccessRestrictionType.VIEW;
		model.isBare = true; // TODO: temporaryly added for demo purpose, need
							 // to fixed with new gitblit
		try {
			isCreated =
			            RpcUtils.createRepository(model, repoCreateUrl, adminUsername,
			                                      adminPassword.toCharArray());
			if (isCreated) {
				return getAppRepositoryURL(applicationKey, tenantDomain);
			} else {
				String msg =
				             "Repository is not created for " + applicationKey +
				                     " due to remote server error";
				log.error(msg);
				throw new RepositoryMgtException(msg);
			}
		} catch (IOException e) {
			String msg =
			             "Repository is not created for " + applicationKey + " due to " +
			                     e.getLocalizedMessage();
			log.error(msg, e);
			throw new RepositoryMgtException(msg, e);
		}

	}

	@Override
	public boolean deleteRepository(String applicationKey, String tenantDomain)
	                                                                           throws RepositoryMgtException {
		String repoName = tenantDomain + "/" + applicationKey + ".git";
		String repoUrl = config.getFirstProperty(BASE_URL);
		String adminUsername = config.getFirstProperty(AppFactoryConstants.GITBLIT_ADMIN_USERNAME);
		String adminPassword = config.getFirstProperty(AppFactoryConstants.GITBLIT_ADMIN_PASSWORD);
		// Create the gftblit repository model
		RepositoryModel model = new RepositoryModel();
		model.name = repoName;
		// authenticated users can clone, push and view the repository
		model.accessRestriction = Constants.AccessRestrictionType.VIEW;
		model.isBare = true; // TODO: temporaryly added for demo purpose, need
							 // to fixed with new gitblit
        boolean isDeleted;
        try {
			RepositoryModel retrievedRepo =
			                                findRepository(model.name, repoUrl, adminUsername,
			                                               adminPassword);
			isDeleted =
			            RpcUtils.deleteRepository(retrievedRepo, repoUrl, adminUsername,
			                                      adminPassword.toCharArray());
		} catch (IOException e) {
			String msg =
			             "Repository is not deleted for " + applicationKey + " due to " +
			                     e.getLocalizedMessage();
			log.error(msg);
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
			throw new RepositoryMgtException(msg, e);
		}
		return isDeleted;
	}

	/**
	 *
	 * @param applicationKey for the deleting app
	 * @param userName of the forked repo owner
	 * @param tenantDomain of the forked repo owner
	 * @return
	 * @throws RepositoryMgtException
	 */
    @Override
    public boolean deleteForkedRepository(String applicationKey, String userName, String tenantDomain)
		    throws RepositoryMgtException {

	    String tenantAwareUserName = MultitenantUtils.getTenantAwareUsername(userName);
	    String repoName =
			    "~" + tenantDomain + File.separator + tenantAwareUserName + File.separator + applicationKey + ".git";
	    String repoUrl = config.getFirstProperty(BASE_URL);
	    String adminUsername = config.getFirstProperty(AppFactoryConstants.GITBLIT_ADMIN_USERNAME);
	    String adminPassword = config.getFirstProperty(AppFactoryConstants.GITBLIT_ADMIN_PASSWORD);
	    // Create the gftblit repository model
	    RepositoryModel model = new RepositoryModel();
	    model.name = repoName;
	    // authenticated users can clone, push and view the repository
	    model.accessRestriction = Constants.AccessRestrictionType.VIEW;
	    model.isBare = true; // TODO: temporaryly added for demo purpose, need
	    // to fixed with new gitblit
	    boolean isDeleted;
	    try {
		    RepositoryModel retrievedRepo = findRepository(model.name, repoUrl, adminUsername, adminPassword);
		    isDeleted = RpcUtils.deleteRepository(retrievedRepo, repoUrl, adminUsername, adminPassword.toCharArray());
	    } catch (IOException e) {
		    throw new RepositoryMgtException(
				    "Forked Repository is not deleted for applicartion : " + applicationKey + " and user :  " +
				    userName, e);
	    }
	    return isDeleted;
    }


    @Override
	public boolean repoExists(String applicationKey, String tenantDomain)
	                                                                     throws RepositoryMgtException {
		// TODO implement method
		RepositoryModel retrievedRepo;
		String repoName = tenantDomain + "/" + applicationKey + ".git";
		String repoUrl = config.getFirstProperty(BASE_URL);
		String adminUsername = config.getFirstProperty(AppFactoryConstants.GITBLIT_ADMIN_USERNAME);
		String adminPassword = config.getFirstProperty(AppFactoryConstants.GITBLIT_ADMIN_PASSWORD);
		// Create the gftblit repository model
		RepositoryModel model = new RepositoryModel();
		model.name = repoName;
		// authenticated users can clone, push and view the repository
		model.accessRestriction = Constants.AccessRestrictionType.VIEW;
		model.isBare = true; // TODO: temporaryly added for demo purpose, need
							 // to fixed with new gitblit
		try {
			retrievedRepo = findRepository(model.name, repoUrl, adminUsername, adminPassword);
		} catch (IOException e) {
			String msg =
			             "Repository is not deleted for " + applicationKey + " due to " +
			                     e.getLocalizedMessage();
			log.error(msg, e);
			throw new RepositoryMgtException(msg, e);
		}
		return retrievedRepo != null;
	}

	private RepositoryModel findRepository(String name, String url, String account, String password)
	                                                                                                throws IOException {
		Map<String, RepositoryModel> repositories =
		                                            RpcUtils.getRepositories(url, account,
		                                                                     password.toCharArray());
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
	public String getAppRepositoryURL(String applicationKey, String tenantDomain)
	                                                                             throws RepositoryMgtException {
		return config.getFirstProperty(BASE_URL) + REPO_TYPE + "/" + tenantDomain + "/" +
		       applicationKey + ".git";
	}

	public void createFork(String repoUrl) {
		RepositoryModel repositoryModel =
		                                  new RepositoryModel("repo2", "repo2 description",
		                                                      "admin", null);
		// RepositoryModel repositoryModel = new RepositoryModel("repo1",
		// "repo1 description", "admin", null);

		UserModel userModel = new UserModel("user2");

		Boolean isforked = null;
		try {
			isforked =
			           RpcUtils.forkRpository(repositoryModel, userModel, repoUrl, "admin",
			                                  "admin".toCharArray());
		} catch (IOException e) {
			e.printStackTrace(); // To change body of catch statement use File |
								 // Settings | File Templates.
		}
		System.out.println("Is Forked: " + isforked);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getType() {
		return TYPE;
	}

	public boolean createTenantRepo(String tenantId) throws RepositoryMgtException {
		String defaultTenantRepo = tenantId + "/defApp";
		String repoCreateUrl =
		                       config.getFirstProperty(BASE_URL) +
		                               "rpc?req=CREATE_REPOSITORY&name=/" + defaultTenantRepo;
		String repoDeleteUrl =
		                       config.getFirstProperty(BASE_URL) +
		                               "rpc?req=DELETE_REPOSITORY&name=/" + defaultTenantRepo;
		String adminUsername = config.getFirstProperty(AppFactoryConstants.GITBLIT_ADMIN_USERNAME);
		String adminPassword = config.getFirstProperty(AppFactoryConstants.GITBLIT_ADMIN_PASSWORD);

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
				// String url = getAppRepositoryURL(defaultTenantRepo);
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
	public boolean deleteTenantRepo(String tenantId) throws RepositoryMgtException {
		String defaultTenantRepo = tenantId + "/defApp";
		String repoDeleteUrl =
		                       config.getFirstProperty(BASE_URL) +
		                               "rpc?req=DELETE_REPOSITORY&name=/" + defaultTenantRepo;
		String adminUsername = config.getFirstProperty(AppFactoryConstants.GITBLIT_ADMIN_USERNAME);
		String adminPassword = config.getFirstProperty(AppFactoryConstants.GITBLIT_ADMIN_PASSWORD);
		// Create the gitblit repository model
		RepositoryModel model = new RepositoryModel();
		model.name = defaultTenantRepo;
		// authenticated users can clone, push and view the repository
		model.accessRestriction = Constants.AccessRestrictionType.VIEW;
		try {
			boolean isDeleted =
			                    RpcUtils.deleteRepository(model, repoDeleteUrl, adminUsername,
			                                              adminPassword.toCharArray());
			if (!isDeleted) {
				log.info("Tenant Repsitory is not deleted for " + tenantId);
			}
			return isDeleted;
		} catch (IOException e) {
			String msg =
			             "Tenant Repsitory is not deleted for " + tenantId + " due to " +
			                     e.getLocalizedMessage();
			log.error(msg, e);
			throw new RepositoryMgtException(msg, e);
		}
	}

	@Override
	public void deleteStratosArtifactRepository(String repoName) throws RepositoryMgtException {
		String repoUrl = config.getFirstProperty(AppFactoryConstants.PAAS_ARTIFACT_REPO_PROVIDER_BASE_URL);
		String adminUsername = config.getFirstProperty(AppFactoryConstants.PAAS_ARTIFACT_REPO_PROVIDER_ADMIN_USER_NAME);
		String adminPassword = config.getFirstProperty(AppFactoryConstants.PAAS_ARTIFACT_REPO_PROVIDER_ADMIN_PASSWORD);

		RepositoryModel model = new RepositoryModel();
		model.name = repoName;

		try {
			RepositoryModel retrievedRepo = findRepository(model.name, repoUrl, adminUsername, adminPassword);
			RpcUtils.deleteRepository(retrievedRepo, repoUrl, adminUsername, adminPassword.toCharArray());
		} catch (IOException e) {
			String msg = "Repository is not deleted for " + repoName + " due to " + e.getLocalizedMessage();
			throw new RepositoryMgtException(msg, e);
		}
	}

	/**
	 * Creating a fork repository.
	 * 
	 */
	@Override
	public String createForkRepo(String applicationKey, String userName)
	                                                                    throws RepositoryMgtException {

		CarbonContext threadLocalCarbonContext = CarbonContext.getThreadLocalCarbonContext();
		String domainName = threadLocalCarbonContext.getTenantDomain();

		// Creating the repository model
		RepositoryModel repositoryModel = new RepositoryModel();
		repositoryModel.name = domainName + "/" + applicationKey + ".git";

		// Creating the user model
		UserModel userModel = new UserModel(userName);
		String userId = MultitenantUtils.getTenantAwareUsername(userName);
		userModel.username = domainName + "/" + userId;

		String adminUsername = config.getFirstProperty(AppFactoryConstants.GITBLIT_ADMIN_USERNAME);
		String adminPassword = config.getFirstProperty(AppFactoryConstants.GITBLIT_ADMIN_PASSWORD);
		String gitBaseUrl = config.getFirstProperty(BASE_URL);
		try {
            boolean isForked = RpcUtils.forkRpository(repositoryModel, userModel, gitBaseUrl,
                    adminUsername, adminPassword.toCharArray());
			if (isForked) {

				String url = getForkedAppRepositoryURL(applicationKey, domainName, userId);

				// the default restriction type is authenticated PUSH.
				// Changing the restriction type to authenticated VIEW, CLONE
				// and PUSH
				String repoName = url.split("/git/")[1];
				changeAccessRestriction(repoName, gitBaseUrl, adminUsername, adminPassword);
				return url;
			} else {
				String msg =
				             "Forked repository was not created for " + userName +
				                     " due to remote server error";
				log.error(msg);
				throw new RepositoryMgtException(msg);
			}
		} catch (IOException e) {
			String msg =
			             "Forked repository was not created for " + userName + " due to " +
			                     e.getLocalizedMessage();
			log.error(msg, e);
			throw new RepositoryMgtException(msg, e);
		}
	}

	@Override
	public String getForkedAppRepositoryURL(String applicationKey, String tenantDomain,
	                                        String userId) throws RepositoryMgtException {
		return config.getFirstProperty(BASE_URL) + REPO_TYPE + "/~" + tenantDomain + "/" + userId +
		       "/" + applicationKey + ".git";
	}

	public void changeAccessRestriction(String repositoryName, String gitBaseUrl,
	                                    String adminUserName, String adminPassword)
	                                                                               throws RepositoryMgtException {
		RepositoryModel repositoryModel = new RepositoryModel();
		repositoryModel.name = repositoryName;
		repositoryModel.accessRestriction = Constants.AccessRestrictionType.VIEW;

		try {
			RpcUtils.updateRepository(repositoryName, repositoryModel, gitBaseUrl, adminUserName,
			                          adminPassword.toCharArray());
		} catch (IOException e) {
			String msg =
			             "Error while updating the repository " + repositoryName + " due to " +
			                     e.getLocalizedMessage();
			log.error(msg, e);
			throw new RepositoryMgtException(msg, e);
		}
	}

	/**
	 * Implementation of forkBranch is used to add new branches to the existing forked repository. This use JGit client to execute composite of commands. 
	 * 
	 */
    public void forkBranch(String applicationKey, String userName, String branch)
            throws RepositoryMgtException {

        File repoDir = null;
        try {

            CarbonContext threadLocalCarbonContext = CarbonContext.getThreadLocalCarbonContext();
            String domainName = threadLocalCarbonContext.getTenantDomain();

            String userId = MultitenantUtils.getTenantAwareUsername(userName);

            String forkedRepoURL = getForkedAppRepositoryURL(applicationKey, domainName, userId);
            String mainRepoURL = getAppRepositoryURL(applicationKey, domainName);
            String appFacHome = System.getProperty("carbon.home");

            //Creating temporary  path to get clone meta data to appfactory side
            repoDir =
                    new File(appFacHome + File.separator + "tmpfork" + File.separator + domainName +
                            File.separator + userId + File.separator + applicationKey +
                            File.separator + branch);

            try {
                FileUtils.forceMkdir(repoDir);
            } catch (IOException e) {
                log.error("Error creating work directory at location" + repoDir.getAbsolutePath(), e);
            }
            AppfactoryRepositoryClient repositoryClient = getRepositoryClient();
            repositoryClient.fetchBranchToForkRepo(forkedRepoURL, mainRepoURL, branch, branch, repoDir);    // fetch branch
            boolean isSuccessful = repositoryClient.pushLocalCommits(forkedRepoURL, branch, repoDir);                              // push fetched branch to forked repo
            if (!isSuccessful) {
                String errorMsg = "Failed to complete git push because remote git server rejected the push command.";
                log.error(errorMsg);
                throw new RepositoryMgtException(errorMsg);
            }
        } catch (RepositoryMgtException e) {
            String msg =
                    "Error while forking the repository branch : " + branch + " due to " +
                            e.getMessage() + " from RepositoryMgtException";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        } finally {
            try {
                FileUtils.deleteDirectory(repoDir);
            } catch (IOException e) {
                log.error("Error when deleting files", e);
                try {
                    try {
                        log.warn("Sleeping for the moment to delete tmp files...");
                        Thread.sleep(5000);
                    } catch (InterruptedException e1) {
                        log.warn("Error while thread sleeping");
                    }
                    FileUtils.deleteDirectory(repoDir);
                } catch (IOException e1) {
                    log.error("Error when deleting files secondly", e);
                }
            }
        }

    }


    @Override
    public AppfactoryRepositoryClient getRepositoryClient() throws RepositoryMgtException {
        String userName = config.getFirstProperty((AppFactoryConstants.GITBLIT_ADMIN_USERNAME).
                replace("{@type}", getType()));
        String password = config.getFirstProperty((AppFactoryConstants.GITBLIT_ADMIN_PASSWORD).
                replace("{@type}", getType()));
        appfactoryRepositoryClient.init(userName, password);
        return this.appfactoryRepositoryClient;
    }

}
