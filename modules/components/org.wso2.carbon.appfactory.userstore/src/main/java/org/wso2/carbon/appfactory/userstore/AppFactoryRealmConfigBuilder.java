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

package org.wso2.carbon.appfactory.userstore;

import org.apache.axiom.util.UIDGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.TenantMgtConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.config.RealmConfigXMLProcessor;
import org.wso2.carbon.user.core.config.multitenancy.MultiTenantRealmConfigBuilder;
import org.wso2.carbon.user.core.config.multitenancy.SimpleRealmConfigBuilder;
import org.wso2.carbon.user.core.ldap.LDAPConstants;
import org.wso2.carbon.user.core.tenant.Tenant;

import java.util.Map;

public class AppFactoryRealmConfigBuilder implements
MultiTenantRealmConfigBuilder {

	private static Log logger = LogFactory
			.getLog(AppFactoryRealmConfigBuilder.class);

	@Override
	public RealmConfiguration getRealmConfigForTenantToCreateRealm(
			RealmConfiguration bootStrapConfig,
			RealmConfiguration persistedConfig, int tenantId)
					throws UserStoreException {
		RealmConfiguration realmConfiguration = null;
		try {
			realmConfiguration = bootStrapConfig.cloneRealmConfiguration();
			realmConfiguration.setAdminUserName(persistedConfig
					.getAdminUserName());
			realmConfiguration.setAdminPassword(persistedConfig
					.getAdminPassword());

			String groupSearchBaseValue = persistedConfig
					.getUserStoreProperty(LDAPConstants.GROUP_SEARCH_BASE);
			realmConfiguration.getUserStoreProperties().put(
					LDAPConstants.GROUP_SEARCH_BASE, groupSearchBaseValue);
			logger.info("Get the tenant ( tenant id=" + tenantId + ") detlail.");
		} catch (Exception e) {
			String errMsg = "Error occured when merge the tenant specific detail and common configuration : "
					+ e.getMessage();
			logger.error(errMsg, e);
			throw new UserStoreException(errMsg, e);
		}
		return realmConfiguration;
	}

	@Override
	public RealmConfiguration getRealmConfigForTenantToPersist(
			RealmConfiguration bootStrapConfig,
			TenantMgtConfiguration tenantMgtConfig, Tenant tenantInfo,
			int tenantId) throws UserStoreException {

		try {
			RealmConfiguration ldapRealmConfig = bootStrapConfig
					.cloneRealmConfiguration();

			// Clear the whole details of the configuration file to update the
			// tenant specific properties values only.
			ldapRealmConfig.getRealmProperties().clear();
			ldapRealmConfig.getAuthzProperties().clear();
			ldapRealmConfig.getUserStoreProperties().clear();
            
			ldapRealmConfig.setAdminUserName(tenantInfo.getAdminName());
			ldapRealmConfig.setAdminPassword(UIDGenerator.generateUID());
			ldapRealmConfig.setTenantId(tenantId);

			Map<String, String> authz = ldapRealmConfig.getAuthzProperties();
			authz.put(
					UserCoreConstants.RealmConfig.PROPERTY_ADMINROLE_AUTHORIZATION,
					CarbonConstants.UI_ADMIN_PERMISSION_COLLECTION);

			Map<String, String> userStoreProperties = ldapRealmConfig
					.getUserStoreProperties();

			String partitionDN = tenantMgtConfig
					.getTenantStoreProperties()
					.get(UserCoreConstants.TenantMgtConfig.PROPERTY_ROOT_PARTITION);
			String organizationName = tenantInfo.getDomain();
			// eg: o=cse.rog
			String organizationRDN = tenantMgtConfig
					.getTenantStoreProperties()
					.get(UserCoreConstants.TenantMgtConfig.PROPERTY_ORGANIZATIONAL_ATTRIBUTE)
					+ "=" + organizationName;
			// eg: ou=users
			String orgSubContextAttribute = tenantMgtConfig
					.getTenantStoreProperties()
					.get(UserCoreConstants.TenantMgtConfig.PROPERTY_ORG_SUB_CONTEXT_ATTRIBUTE);

			// if read ldap group is enabled, set the tenant specific group
			// search base
			if (("true").equals(bootStrapConfig
					.getUserStoreProperty(LDAPConstants.READ_LDAP_GROUPS))) {
				// eg: ou=groups
				String groupContextRDN = orgSubContextAttribute + "="
						+ LDAPConstants.GROUP_CONTEXT_NAME;
				// eg: ou=users,o=cse.org, dc=cloud, dc=com
				String groupSearchBase = groupContextRDN + ","
						+ organizationRDN + "," + partitionDN;

				userStoreProperties.put(LDAPConstants.GROUP_SEARCH_BASE,
						groupSearchBase);
			}
			logger.info("Get the tenant ( tenant id=" + tenantId + ") detlail.");

			return ldapRealmConfig;

		} catch (Exception e) {
			String errorMessage = "Error while building tenant specific Realm Configuration.";
			logger.error(errorMessage, e);
			throw new UserStoreException(errorMessage, e);
		}
	}

	@Override
	public RealmConfiguration getRealmConfigForTenantToCreateRealmOnTenantCreation(
			RealmConfiguration bootStrapConfig,
			RealmConfiguration persistedConfig, int tenantId)
					throws UserStoreException {
		logger.info("getRealmConfigForTenantToCreateRealmOnTenantCreation");
		return persistedConfig;
	}

}
