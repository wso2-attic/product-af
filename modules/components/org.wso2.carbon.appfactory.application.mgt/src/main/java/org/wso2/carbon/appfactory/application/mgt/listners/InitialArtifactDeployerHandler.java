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
import org.apache.stratos.messaging.message.receiver.tenant.TenantManager;
import org.wso2.carbon.appfactory.application.mgt.internal.ServiceReferenceHolder;
import org.wso2.carbon.appfactory.application.mgt.util.Util;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.ApplicationEventsHandler;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeBean;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeManager;
import org.wso2.carbon.appfactory.core.dto.Application;
import org.wso2.carbon.appfactory.core.dto.UserInfo;
import org.wso2.carbon.appfactory.core.dto.Version;
import org.wso2.carbon.appfactory.core.governance.ApplicationManager;
import org.wso2.carbon.appfactory.core.util.AppFactoryCoreUtil;
import org.wso2.carbon.appfactory.deployers.InitialArtifactDeployer;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

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
        String stage = isUploadableAppType ?
                       WordUtils.capitalize(AppFactoryConstants.ApplicationStage.PRODUCTION.getStageStrValue()) :
                       WordUtils.capitalize(AppFactoryConstants.ApplicationStage.DEVELOPMENT.getStageStrValue());
		List<NameValuePair> params = AppFactoryCoreUtil.getDeployParameterMap(application.getId(), application.getType(),
		                                         stage,AppFactoryConstants. ORIGINAL_REPOSITORY);

		//TODO - Fix properly in 2.2.0-M1
		params.add(new NameValuePair("tenantUserName", userName + "@" + tenantDomain));
		Map<String, String[]> deployInfoMap = new HashMap<String, String[]>();
		for (Iterator<NameValuePair> ite = params.iterator() ; ite.hasNext() ;  ) {
			NameValuePair pair = ite.next();
			deployInfoMap.put(pair.getName(), new String[]{pair.getValue()});
		}

		int tenantId = -1;
		try {
			tenantId = Util.getRealmService().getTenantManager().getTenantId(tenantDomain);
		    InitialArtifactDeployer deployer = new InitialArtifactDeployer(deployInfoMap, tenantId, tenantDomain);
		    deployer.deployLatestSuccessArtifact(deployInfoMap);
		} catch (UserStoreException e) {
			throw new AppFactoryException("Initial code committing error " + application.getName() , e);
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
