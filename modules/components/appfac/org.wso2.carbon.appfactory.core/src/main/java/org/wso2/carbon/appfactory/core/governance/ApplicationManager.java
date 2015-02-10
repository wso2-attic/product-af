/*
 * Copyright 2005-2015 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.appfactory.core.governance;

import java.util.ArrayList;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.dao.JDBCApplicationDAO;
import org.wso2.carbon.appfactory.core.dto.Application;
import org.wso2.carbon.appfactory.core.util.Constants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;

/**
 * This class is repsonsbile of handling applications. 
 * It has a ThreadLocal GenericArtifactManager.
 * It uses JDBCApplicationDAO for retrieving runtime data of applications.
 */
public class ApplicationManager {

	private static final Log log = LogFactory.getLog(ApplicationManager.class);

	private static ApplicationManager instance = new ApplicationManager();
	
    private ApplicationManager() {
    	
    }
    
    public static ApplicationManager getInstance() {
    	return instance;
    }

    /**
     * Method to retrive application type
     * @param applicationId
     * @return application type
     */
    public String getApplicationType(String applicationId) throws AppFactoryException {
    	UserRegistry userRegistry =
			    (UserRegistry) CarbonContext.getThreadLocalCarbonContext()
					    .getRegistry(RegistryType.SYSTEM_GOVERNANCE);

	   
	    String path =
			    AppFactoryConstants.REGISTRY_APPLICATION_PATH + "/" + applicationId + "/" +
			    "appinfo";

	    if (log.isDebugEnabled()) {
		    log.debug("Username for registry : " + userRegistry.getUserName() + " Tenant ID : " +
		              userRegistry.getTenantId());
		    log.debug("Username from carbon context : " +
		              CarbonContext.getThreadLocalCarbonContext().getUsername());
	    }
	    try {
	    	GovernanceUtils.loadGovernanceArtifacts(userRegistry);
	 	    GenericArtifactManager artifactManager = new GenericArtifactManager(userRegistry, "application");

		    if (!userRegistry.resourceExists(path)) {
			    return null;
		    }

		    Resource resource = userRegistry.get(path);
		    GenericArtifact artifact = artifactManager.getGenericArtifact(resource.getUUID());
		    //GenericArtifact artifact = artifactManager.get().getGenericArtifact(resource.getUUID());
		    String applicationType = artifact.getAttribute("application_type");
		    return applicationType;
	    } catch (GovernanceException e) {
		    throw new AppFactoryException("Error while getting Application type for application: " + applicationId, e);
	    } catch (RegistryException e) {
		    throw new AppFactoryException("Error while getting Application type for application: " + applicationId, e);
	    }
    }
    
    public Application[] getAllApplicaitonsOfUser(String userName) throws AppFactoryException {
    	UserRegistry userRegistry =
                (UserRegistry) CarbonContext.getThreadLocalCarbonContext()
                                            .getRegistry(RegistryType.SYSTEM_GOVERNANCE);
        
    	CarbonContext context = CarbonContext.getThreadLocalCarbonContext();
        ArrayList<Application> applications = new ArrayList<Application>();
        try {
        	GovernanceUtils.loadGovernanceArtifacts(userRegistry);
     	    GenericArtifactManager artifactManager = new GenericArtifactManager(userRegistry, "application");
     	
            String[] roles =
                             context.getUserRealm().getUserStoreManager()
                                    .getRoleListOfUser(userName);
            for (String role : roles) {
                if (role.startsWith(AppFactoryConstants.APP_ROLE_PREFIX)) {
                    String appkeyFromPerAppRoleName = AppFactoryUtil.getAppkeyFromPerAppRoleName(role);
                    applications.add(getApplication(appkeyFromPerAppRoleName, artifactManager, userRegistry));
                }
            }
            return applications.toArray(new Application[applications.size()]);
        } catch (UserStoreException e) {
            String message = "Failed to retrieve applications of the user" + userName;
            throw new AppFactoryException(message, e);
        } catch (RegistryException e) {
            String message = "Failed to retrieve applications of the user" + userName;
            throw new AppFactoryException(message, e);
        }
    }
    
    public Application getApplicationInfo(String applicationId) throws AppFactoryException {
    	UserRegistry userRegistry =
                (UserRegistry) CarbonContext.getThreadLocalCarbonContext()
                                            .getRegistry(RegistryType.SYSTEM_GOVERNANCE);
    	try {
	        GovernanceUtils.loadGovernanceArtifacts(userRegistry);
	        GenericArtifactManager artifactManager = new GenericArtifactManager(userRegistry, "application");
	        return getApplication(applicationId, artifactManager, userRegistry);
        } catch (RegistryException e) {
	        throw new AppFactoryException("Error while loading GenericArtifactManager", e);
        }
    }
    
    /**
     * Method to retieve application
     * @param applicationId - Applicaiton ID
     * @return Information object or null if application by the id is not in registry
     * @throws AppFactoryException
     */
	private Application getApplication(String applicationId,
	                                   GenericArtifactManager artifactManager,
	                                   UserRegistry userRegistry) throws AppFactoryException {
	    String path =
		              AppFactoryConstants.REGISTRY_APPLICATION_PATH + "/" + applicationId + "/" +
		                      "appinfo";
		
		if (log.isDebugEnabled()) {
			log.debug("Username for registry :" + userRegistry.getUserName() + " Tenant ID : " +
			          userRegistry.getTenantId());
			log.debug("Username from carbon context :" +
			          CarbonContext.getThreadLocalCarbonContext().getUsername());
		}
		try {
			
	 	    
			if (!userRegistry.resourceExists(path)) {
				return null;
			}

			Resource resource = userRegistry.get(path);
			
			GenericArtifact artifact = artifactManager.getGenericArtifact(resource.getUUID());
			Application application=null;
			if(artifact !=null) {
				application = getAppInfoFromRXT(artifact);

				// from DB
				JDBCApplicationDAO applicationDAO = JDBCApplicationDAO.getInstance();
				application.setBranchCount(applicationDAO.getBranchCount(application.getId()));
				// set application creation status, if not found, consider as
				// completed because previously
				// created applications does not contain this attribute
				String applicationCreationStatus =
						applicationDAO.getApplicationCreationStatus(application.getId())
						              .name();
				if (applicationCreationStatus != null) {
					application.setApplicationCreationStatus(
							Constants.ApplicationCreationStatus.valueOf(applicationCreationStatus));
				} else {
					application.setApplicationCreationStatus(
							Constants.ApplicationCreationStatus.COMPLETED);
				}
			}
			return application;
		} catch (GovernanceException e) {
			throw new AppFactoryException("Error while getting Application Info service ", e);
		} catch (RegistryException e) {
			throw new AppFactoryException("Error while getting Application Info service ", e);
		} 
	}
		

	//TODO
	public void deleteApplication(String applicationId) {
		
	}
	

	private Application getAppInfoFromRXT(GenericArtifact artifact) throws AppFactoryException {
		Application appInfo;
		try {
			appInfo =
			          new Application(
			                          artifact.getAttribute(AppFactoryConstants.RXT_KEY_APPINFO_KEY),
			                          artifact.getAttribute(AppFactoryConstants.RXT_KEY_APPINFO_NAME),
			                          artifact.getAttribute(AppFactoryConstants.RXT_KEY_APPINFO_TYPE),
			                          artifact.getAttribute(AppFactoryConstants.RXT_KEY_APPINFO_REPO_TYPE),
			                          artifact.getAttribute(AppFactoryConstants.RXT_KEY_APPINFO_DESC),
			                          artifact.getAttribute(AppFactoryConstants.RXT_KEY_APPINFO_REPO_ACCESSABILITY),
			                          artifact.getAttribute(AppFactoryConstants.RXT_KEY_APPINFO_OWNER),
			                          artifact.getAttribute(AppFactoryConstants.RXT_KEY_APPINFO_DEFAULT_DOMAIN));

			String customDomainVerified =
			                              artifact.getAttribute(AppFactoryConstants.RXT_KEY_APPINFO_CUSTOM_URL_VERIFICATION);
			if (customDomainVerified != null) {
				appInfo.setcustomUrlVerificationCode(customDomainVerified);
			}

			String repoAccessability =
			                           artifact.getAttribute(AppFactoryConstants.RXT_KEY_APPINFO_REPO_ACCESSABILITY);
			if (repoAccessability != null) {
				appInfo.setRepoAccessability(repoAccessability);
			}

			String[] prodVersions =
			                        artifact.getAttributes(AppFactoryConstants.RXT_KEY_APPINFO_PROD_VERSION);
			if (!ArrayUtils.isEmpty(prodVersions) && prodVersions.length > 0) {
				appInfo.setProduction(Boolean.TRUE);
			}

			String customDomain =
			                      artifact.getAttribute(AppFactoryConstants.RXT_KEY_APPINFO_CUSTOM_URL);
			if (customDomain != null) {
				appInfo.setCustomUrl(customDomain);
			}
	   } catch (GovernanceException e) {
			String errorMsg =
			                  String.format("Unable to extract information from RXT: %s",
			                                artifact.getId());
			log.error(errorMsg, e);
			throw new AppFactoryException(errorMsg, e);
		}

		return appInfo;
	}
    

}
