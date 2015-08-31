/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.appfactory.application.mgt.listners;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang.WordUtils;
import org.wso2.carbon.appfactory.application.mgt.util.Util;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.ApplicationEventsHandler;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeManager;
import org.wso2.carbon.appfactory.core.dto.Application;
import org.wso2.carbon.appfactory.core.dto.UserInfo;
import org.wso2.carbon.appfactory.core.dto.Version;
import org.wso2.carbon.appfactory.core.util.AppFactoryCoreUtil;
import org.wso2.carbon.appfactory.deployers.InitialArtifactDeployer;
import org.wso2.carbon.user.api.UserStoreException;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The handler that call right after RepositoryHandler. It calls the InitialDeployer to commit
 * the built artifact.
 */
public class InitialArtifactDeployerHandler extends ApplicationEventsHandler {

	@Override
	public int getPriority() {
		return priority;
	}

	public InitialArtifactDeployerHandler(String identifier, int priority) {
		super(identifier, priority);
	}


	@Override
	public void onCreation(Application application, String userName, String tenantDomain,
	                                 boolean isUploadableAppType) throws AppFactoryException {
        String version = isUploadableAppType ? "1.0.0" : "trunk";
        String stage = isUploadableAppType ?
                       WordUtils.capitalize(AppFactoryConstants.ApplicationStage.PRODUCTION.getStageStrValue()) :
                       WordUtils.capitalize(AppFactoryConstants.ApplicationStage.DEVELOPMENT.getStageStrValue());
        List<NameValuePair> params = AppFactoryCoreUtil.getDeployParameterMap(application.getId(),
                                                                              application.getType(),
                                                                              stage,
                                                                              AppFactoryConstants.ORIGINAL_REPOSITORY);

		//TODO - Fix properly in 2.2.0-M1
		params.add(new NameValuePair(AppFactoryConstants.TENANT_USER_NAME, userName ));
		Map<String, String[]> deployInfoMap = new HashMap<String, String[]>();
		for (Iterator<NameValuePair> ite = params.iterator() ; ite.hasNext() ;  ) {
			NameValuePair pair = ite.next();
			deployInfoMap.put(pair.getName(), new String[]{pair.getValue()});
		}

		int tenantId = -1;
		String initialDeployerClassName = null;
		try {
			AppFactoryConfiguration appfactoryConfiguration = AppFactoryUtil.getAppfactoryConfiguration();

			String paasRepositoryProviderClassName = appfactoryConfiguration.getFirstProperty(
					AppFactoryConstants.PAAS_ARTIFACT_REPO_PROVIDER_CLASS_NAME);
			String stratosServerURL = appfactoryConfiguration.getFirstProperty(AppFactoryConstants.DEPLOYMENT_STAGES
			                          + AppFactoryConstants.DOT_SEPERATOR + stage + AppFactoryConstants.DOT_SEPERATOR
			                          + AppFactoryConstants.TENANT_MGT_URL);
			tenantId = Util.getRealmService().getTenantManager().getTenantId(tenantDomain);

			deployInfoMap.put(AppFactoryConstants.TENANT_DOMAIN, new String[] { tenantDomain });
            deployInfoMap.put(AppFactoryConstants.APPLICATION_VERSION, new String[] { version });
			deployInfoMap.put("tenantId", new String[] { Integer.toString(tenantId) });
			deployInfoMap.put(AppFactoryConstants.PAAS_ARTIFACT_REPO_PROVIDER_CLASS_NAME,new String[]{paasRepositoryProviderClassName});
			deployInfoMap.put(AppFactoryConstants.STRATOS_SERVER_URL,new String[]{stratosServerURL});
			initialDeployerClassName = ApplicationTypeManager.getInstance()
			                                                        .getApplicationTypeBean(application.getType())
			                                                        .getInitialDeployerClassName();
			InitialArtifactDeployer deployer;
			if(initialDeployerClassName != null){
				ClassLoader loader = getClass().getClassLoader();
				Class<?> initialDeployerClass = Class.forName(initialDeployerClassName, true, loader);
				Object instance = initialDeployerClass.getDeclaredConstructor(Map.class, int.class, String.class)
				                                      .newInstance(deployInfoMap, tenantId, tenantDomain);
				if(instance instanceof InitialArtifactDeployer){
					deployer = (InitialArtifactDeployer) instance;
				} else {
					throw new AppFactoryException(initialDeployerClassName + "is not a implementation of "
					                              + InitialArtifactDeployer.class);
				}
			} else {
				deployer = new InitialArtifactDeployer(deployInfoMap, tenantId, tenantDomain);
			}
		    deployer.deployLatestSuccessArtifact(deployInfoMap);
		} catch (UserStoreException e) {
			throw new AppFactoryException("Initial code committing error " + application.getName() , e);
		} catch (ClassNotFoundException e) {
			throw new AppFactoryException("Initial deployer class : " + initialDeployerClassName
			                              + " not found " + application.getName() , e);
		} catch (InstantiationException e) {
			throw new AppFactoryException("Cannot create instance of initial deployer class : "
			                              + initialDeployerClassName + " not found " + application.getName() , e);
		} catch (IllegalAccessException e) {
			throw new AppFactoryException("Cannot access initial deployer class : " + initialDeployerClassName
			                              + " not found " + application.getName() , e);
		} catch (NoSuchMethodException e) {
			throw new AppFactoryException("Cannot create instance of initial deployer class : "
			                              + initialDeployerClassName + " not found " + application.getName() , e);
		} catch (InvocationTargetException e) {
			throw new AppFactoryException("Cannot create instance of initial deployer class : "
			                              + initialDeployerClassName + " not found " + application.getName() , e);
		}
	}

	@Override
	public void onDeletion(Application application, String userName, String tenantDomain)
			throws AppFactoryException {

	}

	@Override
	public void onUserAddition(Application application, UserInfo user,
	                                     String tenantDomain) throws AppFactoryException {

	}

	@Override
	public void onUserDeletion(Application application, UserInfo user,
	                                     String tenantDomain) throws AppFactoryException {

	}

	@Override
	public void onUserUpdate(Application application, UserInfo user, String tenantDomain)
			throws AppFactoryException {

	}

	@Override
	public void onRevoke(Application application, String tenantDomain)
			throws AppFactoryException {

	}

	@Override
	public void onVersionCreation(Application application, Version source, Version target,
	                                        String tenantDomain, String userName)
			throws AppFactoryException {

	}

	@Override
	public void onFork(Application application, String userName, String tenantDomain,
	                             String version, String[] forkedUsers) throws AppFactoryException {

	}

	@Override
	public void onLifeCycleStageChange(Application application, Version version,
	                                             String previosStage, String nextStage,
	                                             String tenantDomain) throws AppFactoryException {

	}

	@Override
	public boolean hasExecuted(Application application, String userName,
	                                     String tenantDomain) throws AppFactoryException {
		return false;
	}
}
