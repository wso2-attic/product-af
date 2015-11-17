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

package org.wso2.carbon.appfactory.core.util;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.beans.RuntimeBean;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeBean;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeManager;
import org.wso2.carbon.appfactory.core.dao.ApplicationDAO;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.appfactory.core.runtime.RuntimeManager;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppFactoryCoreUtil {

    private static final Log log = LogFactory.getLog(AppFactoryCoreUtil.class);
	private static long tenantIdleTimeMillis;
	private static final int DEFAULT_TENANT_IDLE_MINS = 30;
	private static Set<String> currentLoadingTenants = new HashSet<String>();

	//Need tenantIdleTime to check whether the tenant is in idle state in loadTenantConfig method
	static {
		tenantIdleTimeMillis =
				Long.parseLong(System.getProperty(
						org.wso2.carbon.utils.multitenancy.MultitenantConstants.TENANT_IDLE_TIME,
						String.valueOf(DEFAULT_TENANT_IDLE_MINS)))
				* 60 * 1000;
	}

    public static String getStage (String applicationId, String version) throws AppFactoryException {
	    	String stage = null;
	        try {
	        	ServiceHolder.getTenantRegistryLoader().loadTenantRegistry(CarbonContext.getThreadLocalCarbonContext().getTenantId());
	        	RegistryService registryService = ServiceHolder.getRegistryService();
	            UserRegistry userRegistry = registryService.getGovernanceSystemRegistry(CarbonContext.getThreadLocalCarbonContext().getTenantId());
	            // child nodes of this will contains folders for all life cycles (
	            // e.g. QA, Dev, Prod)
	            Resource application =
	                    userRegistry.get(AppFactoryConstants.REGISTRY_APPLICATION_PATH +
                                RegistryConstants.PATH_SEPARATOR + applicationId);

	            if (application != null && application instanceof Collection) {

	                // Contains paths to life cycles (.e.g .../<appid>/dev,
	                // .../<appid>/qa , .../<appid>/prod )
	                String[] definedLifeCyclePaths = ((Collection) application).getChildren();

	                for (String lcPath : definedLifeCyclePaths) {

	                    Resource versionsInLCResource = userRegistry.get(lcPath);
	                    if (versionsInLCResource != null && versionsInLCResource instanceof Collection) {

	                        // contains paths to a versions (e.g.
	                        // .../<appid>/<lifecycle>/trunk,
	                        // .../<appid>/<lifecycle>/1.0.1 )
	                        for(String currentVersion :((Collection) versionsInLCResource).getChildren()) {
	                            stage = lcPath.substring(lcPath.lastIndexOf("/") + 1);
                                String versionOnly = currentVersion.substring(currentVersion.lastIndexOf("/") + 1);

	                            if (versionOnly.equals(version)) {
	                            	return stage;
	                            }
	                        }
	                    }
	                }
	            }
	        } catch (RegistryException e) {
	            String errorMsg = String.format("Unable to load the application information for application id: %s",
	                                  applicationId);
	            log.error(errorMsg, e);
	            throw new AppFactoryException(errorMsg, e);
	        }
	        return stage;
	    }

	/**
	 *
	 * @param applicationId
	 * @param tenantDomain
	 * @return
	 * @throws RegistryException
	 */
	public static String getApplicationType(String applicationId, String tenantDomain) throws RegistryException {
		try {
			String applicationType;
			ServiceHolder.getTenantRegistryLoader().loadTenantRegistry(CarbonContext.getThreadLocalCarbonContext().getTenantId());
        	RegistryService registryService = ServiceHolder.getRegistryService();
			UserRegistry userRegistry =
			                            registryService.getGovernanceSystemRegistry(
                                                ServiceHolder.getRealmService().getTenantManager()
                                                        .getTenantId(tenantDomain));
			Resource resource =
			                    userRegistry.get(AppFactoryConstants.REGISTRY_APPLICATION_PATH +
                                        RegistryConstants.PATH_SEPARATOR + applicationId +
                                        RegistryConstants.PATH_SEPARATOR + "appinfo");

			GovernanceUtils.loadGovernanceArtifacts(userRegistry);
			GenericArtifactManager artifactManager = new GenericArtifactManager(userRegistry, "application");
			GenericArtifact artifact = artifactManager.getGenericArtifact(resource.getUUID());
			applicationType = artifact.getAttribute("application_type");

			return applicationType;
		} catch (RegistryException e) {
			log.error(e);
			throw e;
		} catch (UserStoreException e) {
			String errorMsg = String.format("Unable to get tenant id for %s", tenantDomain);
			log.error(errorMsg, e);
			throw new RegistryException(errorMsg, e);
		}
	}

	/**
	 * Check whether the application is uploadable or not
	 * @param type app type
	 * @return
	 */
	public static boolean isUplodableAppType(String type) throws AppFactoryException{
		return  ApplicationTypeManager.getInstance().getApplicationTypeBean(type).isUploadableAppType();
	}

    /**
     * Check whether the application is allowed for domain mapping or not
     *
     * @param type app type
     * @return whether the application is allowed for domain mapping or not
     */
    public static boolean isDomainMappingAllowedAppType(String type) throws AppFactoryException {
        return ApplicationTypeManager.getInstance().getApplicationTypeBean(type).isAllowDomainMapping();
    }

	/**
	 * Check whether this artifact is buildable or non-buildable. It is defined
	 * in appfactory config file
	 *
	 * @param applicationType
	 * @return
	 * @throws AppFactoryException
	 */
	public static boolean isBuildable(String applicationType) throws AppFactoryException {
		return ApplicationTypeManager.getInstance().getApplicationTypeBean(applicationType).isBuildable();
	}

	/**
	 * Checks whether the given application type requires a build server to handle build and deployments or not.
	 * If a project configured as 'buildable' or the jobTemplate as 'freestyle' then it is considered as build server required project.
	 * @param applicationType
	 * @return
	 * @throws AppFactoryException
	 */
	public static boolean isBuildServerRequiredProject(String applicationType) throws AppFactoryException{
		return (isBuildable(applicationType) || isFreestyleNonBuilableProject(applicationType));
	}

	public static boolean isFreestyleNonBuilableProject(String applicationType) throws AppFactoryException {
		String isAppFreeStyle = ApplicationTypeManager.getInstance().getApplicationTypeBean(
				applicationType).getBuildJobTemplate();
		if (isAppFreeStyle != null && isAppFreeStyle.equals("freestyle")) {
			return true;
		}
		return false;

	}

    /**
     * Generate the url of the given application
     *
     * @param applicationId Unique ID of the user application
     * @param version       Version of the user application
     * @param stage         Current stage of the user application
     * @param tenantDomain  Tenant domain which application belongs to
     * @return Generated url of the user application or null if application type is null for id in registry
     * @throws {@link AppFactoryException} If there is an issue in generating application url
     */
    public static String getApplicationUrl(String applicationId, String version, String stage,
                                           String tenantDomain) throws AppFactoryException {
	    String type = ApplicationDAO.getInstance().getApplicationType(applicationId);
	    try {
	        if(type != null) {
		        return ApplicationTypeManager.getInstance().getApplicationTypeBean(type)
		                                     .getProcessor().
						        getDeployedURL(tenantDomain, applicationId, version, stage);
	        }
        } catch (NullPointerException e) {
            String msg = "Error while retriving application url";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
	    return null;
    }

    /**
     * Generate the unique cartridge alias
     *
     * @param applicationId Unique ID of the user application
     * @param tenantDomain  Tenant domain which application belongs to
     * @return Generated cartridge alias
     */
    public static String getCartridgeAlias(String applicationId, String tenantDomain) {
        if (StringUtils.isBlank(applicationId) || StringUtils.isBlank(tenantDomain)) {
            return null;
        }
        tenantDomain = tenantDomain.replace(AppFactoryConstants.DOT_SEPERATOR, AppFactoryConstants.SUBSCRIPTION_ALIAS_DOT_REPLACEMENT);
        return applicationId + tenantDomain;
    }


	public static List<NameValuePair> getDeployParameterMap(String appId, String artifactType, String stage, String repoFrom) throws AppFactoryException{

		List<NameValuePair> parameters = new ArrayList<NameValuePair>();
		parameters.add(new NameValuePair(AppFactoryConstants.ARTIFACT_TYPE, artifactType));
		ApplicationTypeBean applicationTypeBean = ApplicationTypeManager.getInstance()
		                                                                .getApplicationTypeBean(artifactType);
		if (applicationTypeBean == null) {
			throw new AppFactoryException(
					"Application Type details cannot be found for Artifact Type : " +
					artifactType + " stage : " + stage);
		}

		String runtimeNameForAppType = applicationTypeBean.getRuntimes()[0];
		RuntimeBean runtimeBean = RuntimeManager.getInstance().getRuntimeBean(runtimeNameForAppType);

		if (runtimeBean == null) {
			throw new AppFactoryException(
					"Runtime details cannot be found for Artifact Type : " + artifactType +
					", stage : " + stage);
		}
		parameters.add(new NameValuePair(AppFactoryConstants.RUNTIME_NAME_FOR_APPTYPE,runtimeNameForAppType));
		parameters.add(new NameValuePair(AppFactoryConstants.DEPLOY_STAGE, stage));
		parameters.add(new NameValuePair(AppFactoryConstants.REPOSITORY_FROM, repoFrom));
		parameters.add(new NameValuePair(AppFactoryConstants.ARTIFACT_TYPE, artifactType));
		parameters.add(new NameValuePair(AppFactoryConstants.APPLICATION_ID, appId));

		addAppTypeParameters(parameters, applicationTypeBean);
		addRunTimeParameters(stage, parameters, runtimeBean);

		return parameters;

	}


	/**
	 * Add runtime specific parameters to the parameter map
	 * @param stage current stage of the application version
	 * @param parameters list of name value pair to sent to jenkins
	 * @param runtimeBean runtime bean that we need to add parameters from
	 */
	private static void addRunTimeParameters(String stage, List<NameValuePair> parameters, RuntimeBean runtimeBean) {
		parameters.add(new NameValuePair(AppFactoryConstants.RUNTIME_CARTRIDGE_ALIAS_PREFIX,
		                                 runtimeBean.getCartridgeAliasPrefix() + stage));
		parameters.add(new NameValuePair(AppFactoryConstants.RUNTIME_CARTRIDGE_TYPE_PREFIX,
		                                 runtimeBean.getCartridgeTypePrefix() + stage));
		parameters.add(new NameValuePair(AppFactoryConstants.PAAS_REPOSITORY_URL_PATTERN,
		                                 runtimeBean.getPaasRepositoryURLPattern()));
		parameters.add(new NameValuePair(AppFactoryConstants.RUNTIME_DEPLOYMENT_POLICY,
		                                 runtimeBean.getDeploymentPolicy()));
		parameters.add(new NameValuePair(AppFactoryConstants.RUNTIME_AUTOSCALE_POLICY,
		                                 runtimeBean.getAutoscalePolicy()));
		parameters.add(new NameValuePair(AppFactoryConstants.RUNTIME_DATA_CARTRIDGE_TYPE,
		                                 runtimeBean.getDataCartridgeType()));
		parameters.add(new NameValuePair(AppFactoryConstants.RUNTIME_DATA_CARTRIDGE_ALIAS,
		                                 runtimeBean.getDataCartridgeAlias()));
		parameters.add(new NameValuePair(AppFactoryConstants.RUNTIME_SUBSCRIBE_ON_DEPLOYMENT,
		                                 Boolean.toString(runtimeBean.getSubscribeOnDeployment())));
	}

	/**
	 * Add application type parameters to the map
	 *
	 * @param parameters parameter map to send to the jenkins
	 * @param applicationTypeBean application type bean object
	 */
	private static void addAppTypeParameters(List<NameValuePair> parameters, ApplicationTypeBean applicationTypeBean) {
		parameters.add(new NameValuePair(AppFactoryConstants.APPLICATION_EXTENSION,
		                                      applicationTypeBean.getExtension()));
		parameters.add(new NameValuePair(AppFactoryConstants.DEPLOYER_CLASSNAME,
		                                      applicationTypeBean.getDeployerClassName()));
		parameters.add(new NameValuePair(AppFactoryConstants.SERVER_DEPLOYMENT_PATHS,
		                                      applicationTypeBean.getServerDeploymentPath()));
	}

	/**
	 * load tenant axis configuration
	 *
	 * @param tenantDomain tenant domain
	 */
	public static void loadTenantAxisConfiguration(String tenantDomain) {
		final String finalTenantDomain = tenantDomain;
		ConfigurationContext ctx =
				ServiceHolder.getInstance().getConfigContextService().getServerConfigContext();

		//Cannot use the tenantDomain directly because it's getting locked in createTenantConfigurationContext()
		// method in TenantAxisUtils
		String accessFlag = tenantDomain + "@WSO2";

		long lastAccessed = TenantAxisUtils.getLastAccessed(tenantDomain, ctx);
		//Only if the tenant is in unloaded state, we do the loading
		if (System.currentTimeMillis() - lastAccessed >= tenantIdleTimeMillis) {
			synchronized (accessFlag.intern()) {
				// Currently loading tenants are added to a set.
				// If a tenant domain is in the set it implies that particular tenant is being loaded.
				// Therefore if and only if the set does not contain the tenant.
				if (!currentLoadingTenants.contains(tenantDomain)) {
					//Only one concurrent request is allowed to add to the currentLoadingTenants
					currentLoadingTenants.add(tenantDomain);
					ctx.getThreadPool().execute(new Runnable() {
						public void run() {
							Thread.currentThread()
							      .setName("APPFactory-loadTenantConfig-thread");
							try {
								PrivilegedCarbonContext.startTenantFlow();
								ConfigurationContext ctx =
										ServiceHolder.getInstance().getConfigContextService()
										             .getServerConfigContext();
								TenantAxisUtils.getTenantAxisConfiguration(finalTenantDomain, ctx);
							} catch (Exception e) {
								log.error("Error while creating axis configuration for tenant " +
								          finalTenantDomain, e);
							} finally {
								//only after the tenant is loaded completely, the tenant domain is removed from the set
								currentLoadingTenants.remove(finalTenantDomain);
								PrivilegedCarbonContext.endTenantFlow();
							}
						}
					});
				}
			}
		}
	}
}
