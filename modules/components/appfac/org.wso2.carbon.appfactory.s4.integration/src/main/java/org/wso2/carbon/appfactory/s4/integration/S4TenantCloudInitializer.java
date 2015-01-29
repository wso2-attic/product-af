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
package org.wso2.carbon.appfactory.s4.integration;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.tenant.mgt.stub.TenantMgtAdminServiceExceptionException;
import org.apache.stratos.tenant.mgt.stub.TenantMgtAdminServiceStub;
import org.apache.stratos.tenant.mgt.stub.beans.xsd.TenantInfoBean;
import org.codehaus.jackson.map.ObjectMapper;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.beans.RuntimeBean;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.TenantCloudInitializer;
import org.wso2.carbon.appfactory.core.util.CloudConstants;
import org.wso2.carbon.appfactory.s4.integration.internal.ServiceReferenceHolder;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Map;

/**
 * This creates tenant in particular environment in S2 based cloud
 */
public class S4TenantCloudInitializer implements TenantCloudInitializer {
	private static final Log log = LogFactory
			.getLog(S4TenantCloudInitializer.class);
	private StratosRestService restService;
	private TenantMgtAdminServiceStub stub;

	/**
	 * This method will be called for creating tenant in a stage
	 * 
	 * @param properties
	 *            key value pairs supplied by task
	 */
	@Override
	public void onTenantCreation(Map<String, String> properties) {

		try {
			init(properties);

			addTenant(properties);

			String stage = properties.get(CloudConstants.STAGE.getValue());
			RuntimeBean[] runtimeBeans;

			try {
				String runtimesJson = properties.get(CloudConstants.RUNTIMES.getValue());
				ObjectMapper mapper = new ObjectMapper();
				runtimeBeans = mapper.readValue(runtimesJson, RuntimeBean[].class);
			} catch (IOException e) {
				String msg = "Error while converting the json string to a runtime bean";
				log.error(msg, e);
				throw new AppFactoryException(msg, e); //TODO: Do we need to throw this? Discuss...
			}

			for (RuntimeBean runtimeBean : runtimeBeans) {
				String repoURL = createGitRepository(runtimeBean, properties);
				subscribe(runtimeBean, stage, repoURL, properties);
			}
			log.info("successfully created tenant in " + stage);
		} catch (AppFactoryException e) {
			String msg = "Can not continue tenant creation due to "
					+ e.getLocalizedMessage();
			log.error(msg, e);
		}

	}

	private String generateRepoUrlFromTemplate(String pattern, int tenantId,
			String stage) {
		String s = pattern.replace("{@stage}", stage) + File.separator
				+ Integer.toString(tenantId);
		log.info("**************************************generated repo URL: "
				+ s + "******************************");
		return s;

	}

	private void init(Map<String, String> properties)
			throws AppFactoryException {
		/*
		 * String serverURL =
		 * properties.get(AppFactoryTenantCloudInitializerTask.SERVER_URL);
		 * String superAdmin =
		 * properties.get(AppFactoryTenantCloudInitializerTask
		 * .SUPER_TENANT_ADMIN); String superAdminPassword =
		 * properties.get(AppFactoryTenantCloudInitializerTask
		 * .SUPER_TENANT_ADMIN_PASSWORD); try { commandLineService =
		 * RestCommandLineService.getInstance();
		 * commandLineService.login(serverURL, superAdmin, superAdminPassword,
		 * false); } catch (Exception e) { throw new
		 * AppFactoryException("Could not initialize the Rest Client", e); }
		 */

		String serviceEPR = properties
				.get(CloudConstants.SERVER_URL.getValue())
				+ "/services/TenantMgtAdminService";
		try {
			stub = new TenantMgtAdminServiceStub(ServiceReferenceHolder
					.getInstance().getConfigurationContextService()
					.getClientConfigContext(), serviceEPR);
			CarbonUtils
					.setBasicAccessSecurityHeaders(
							properties
									.get(CloudConstants.SUPER_TENANT_ADMIN.getValue()),
							properties
									.get(CloudConstants.SUPER_TENANT_ADMIN_PASSWORD.getValue()),
							stub._getServiceClient());
		} catch (AxisFault axisFault) {
			String msg = "Error while initializing TenantMgt Admin Service Stub ";
			log.error(msg, axisFault);
		}
	}

	private void addTenant(Map<String, String> properties)
			throws AppFactoryException {
		/*
		 * String tenantDomain =
		 * properties.get(AppFactoryTenantCloudInitializerTask.TENANT_DOMAIN);
		 * String adminUsername =
		 * properties.get(AppFactoryTenantCloudInitializerTask.ADMIN_USERNAME);
		 * String adminFirstName =
		 * properties.get(AppFactoryTenantCloudInitializerTask.ADMIN_LAST_NAME);
		 * String adminLastName =
		 * properties.get(AppFactoryTenantCloudInitializerTask
		 * .ADMIN_FIRST_NAME); String adminPassword =
		 * properties.get(AppFactoryTenantCloudInitializerTask .ADMIN_PASSWORD);
		 * String adminEmail =
		 * properties.get(AppFactoryTenantCloudInitializerTask .ADMIN_EMAIL);
		 * String serverURL =
		 * properties.get(AppFactoryTenantCloudInitializerTask.SERVER_URL); try
		 * { commandLineService.addTenant(adminUsername, adminFirstName,
		 * adminLastName, adminPassword, tenantDomain, adminEmail); } catch
		 * (CommandException e) { String msg =
		 * "Error while creating a tenant in " + serverURL; throw new
		 * AppFactoryException(msg, e); }
		 */
		TenantInfoBean tenantInfoBean = new TenantInfoBean();
		tenantInfoBean.setCreatedDate(Calendar.getInstance());
		tenantInfoBean.setUsagePlan(properties
				.get(CloudConstants.TENANT_USAGE_PLAN.getValue()));
		tenantInfoBean.setTenantDomain(properties
				.get(CloudConstants.TENANT_DOMAIN.getValue()));
		tenantInfoBean.setSuccessKey(properties
				.get(CloudConstants.SUCCESS_KEY.getValue()));
		tenantInfoBean.setActive(true);
		tenantInfoBean.setAdmin(properties
				.get(CloudConstants.ADMIN_USERNAME.getValue()));
		tenantInfoBean.setAdminPassword(properties
				.get(CloudConstants.ADMIN_PASSWORD.getValue()));
		tenantInfoBean.setEmail(properties
				.get(CloudConstants.ADMIN_EMAIL.getValue()));
		tenantInfoBean.setFirstname(properties
				.get(CloudConstants.ADMIN_FIRST_NAME.getValue()));
		tenantInfoBean.setLastname(properties
				.get(CloudConstants.ADMIN_LAST_NAME.getValue()));
		tenantInfoBean.setOriginatedService(properties
				.get(CloudConstants.ORIGINATED_SERVICE.getValue()));
		tenantInfoBean.setTenantId(Integer.parseInt(properties
				.get(CloudConstants.TENANT_ID.getValue())));
		try {
			stub.addTenant(tenantInfoBean);
			/* if (log.isDebugEnabled()) { */
			log.info("Called TenantMgt Admin Service in "
					+ properties.get(CloudConstants.SERVER_URL.getValue())
					+ " with " + tenantInfoBean);
			/* } */

		} catch (RemoteException e) {
			String msg = "Error while adding tenant "
					+ tenantInfoBean.getTenantDomain();
			log.error(msg, e);
		} catch (TenantMgtAdminServiceExceptionException e) {
			String msg = "Error while invoking TenantMgtAdminService for "
					+ tenantInfoBean.getTenantDomain();
			log.error(msg, e);
		}
	}

	private String createGitRepository(RuntimeBean runtimeBean,
	                                   Map<String, String> properties) throws AppFactoryException {
		String repoUrl;
		try {
			String stage = properties.get(CloudConstants.STAGE.getValue());
			int tenantID = Integer.parseInt(properties.get(CloudConstants.TENANT_ID.getValue()));
			String repoProviderClassName = AppFactoryUtil.getAppfactoryConfiguration().
					getFirstProperty(AppFactoryConstants.PAAS_ARTIFACT_STORAGE_REPOSITORY_PROVIDER_CLASS_NAME);
			ClassLoader loader = getClass().getClassLoader();
			Class<?> repoProviderClass = Class.forName(repoProviderClassName, true, loader);
			RepositoryProvider repoProvider = (RepositoryProvider) repoProviderClass.newInstance();
			repoProvider.setBaseUrl(AppFactoryUtil.getAppfactoryConfiguration().
					getFirstProperty(AppFactoryConstants.PAAS_ARTIFACT_STORAGE_REPOSITORY_PROVIDER_BASE_URL));
			repoProvider.setAdminUsername(AppFactoryUtil.getAppfactoryConfiguration().
					getFirstProperty(AppFactoryConstants.PAAS_ARTIFACT_STORAGE_REPOSITORY_PROVIDER_ADMIN_USER_NAME));
			repoProvider.setAdminPassword(AppFactoryUtil.getAppfactoryConfiguration().
					getFirstProperty(AppFactoryConstants.PAAS_ARTIFACT_STORAGE_REPOSITORY_PROVIDER_ADMIN_PASSWORD));
			repoProvider.setRepoName(generateRepoUrlFromTemplate(
					runtimeBean.getPaasRepositoryURLPattern(), tenantID, stage));

			repoUrl = repoProvider.createRepository();
			log.info("***************************repo url 1:" + repoUrl
					+ "******************");
		} catch (InstantiationException e) {
			String msg = "Unable to create repository";
			throw new AppFactoryException(msg, e);
		} catch (IllegalAccessException e) {
			String msg = "Unable to create repository";
			throw new AppFactoryException(msg, e);
		} catch (AppFactoryException e) {
			String msg = "Unable to create repository";
			throw new AppFactoryException(msg, e);
		} catch (ClassNotFoundException e) {
			String msg = "PAAS artifact repository provider class not found";
			throw new AppFactoryException(msg, e);
		}
		return repoUrl;
	}

	/**
	 * Subscribe to the stratos cartridge
	 * @param runtimeBean runtime bean
	 * @param stage current stage
	 * @param repoURL repository url for artifact storage
	 * @param properties map of properties
	 * @throws AppFactoryException
	 */
	private void subscribe(RuntimeBean runtimeBean, String stage, String repoURL, Map<String, String> properties)
			throws AppFactoryException {
		String serverURL = properties
				.get(CloudConstants.SERVER_URL.getValue());
		String tenantAdmin = properties
				.get(CloudConstants.ADMIN_USERNAME.getValue());
		String tenantAdminPassword = properties
				.get(CloudConstants.ADMIN_PASSWORD.getValue());
		String tenantDomain = properties
				.get(CloudConstants.TENANT_DOMAIN.getValue());
		String username = tenantAdmin + "@" + tenantDomain;

		restService = new StratosRestService(serverURL, username,
				tenantAdminPassword);

		restService.subscribe(runtimeBean.getCartridgeTypePrefix() + stage,
		                      runtimeBean.getAliasPrefix() + stage + tenantDomain.replace(".", "dot"),
		                      repoURL, true,
		                      AppFactoryUtil.getAppfactoryConfiguration().
				                      getFirstProperty(AppFactoryConstants.PAAS_ARTIFACT_STORAGE_REPOSITORY_PROVIDER_ADMIN_USER_NAME),
		                      AppFactoryUtil.getAppfactoryConfiguration().
				                      getFirstProperty(AppFactoryConstants.PAAS_ARTIFACT_STORAGE_REPOSITORY_PROVIDER_ADMIN_PASSWORD),
		                      runtimeBean.getDataCartridgeType(),
		                      runtimeBean.getDataCartridgeAlias(),
		                      runtimeBean.getAutoscalePolicy(),
		                      runtimeBean.getDeploymentPolicy());

	}
}
