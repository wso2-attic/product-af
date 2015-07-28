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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.ldap.ReadWriteLDAPUserStoreManager;
import org.wso2.carbon.user.core.profile.ProfileConfigurationManager;

import java.util.Map;

public class AppFactoryCustomUserStoreManager extends ReadWriteLDAPUserStoreManager {
    private static final Log log = LogFactory.getLog(AppFactoryCustomUserStoreManager.class);

    public AppFactoryCustomUserStoreManager() {
    }

    public AppFactoryCustomUserStoreManager(RealmConfiguration realmConfig,
                                     Map<String, Object> properties, ClaimManager claimManager,
                                     ProfileConfigurationManager profileManager, UserRealm realm,
                                     Integer tenantId) throws UserStoreException {
        super(realmConfig, properties, claimManager, profileManager, realm,
              tenantId);
    }

    public AppFactoryCustomUserStoreManager(RealmConfiguration realmConfig,
                                     ClaimManager claimManager,
                                     ProfileConfigurationManager profileManager)
            throws UserStoreException {
        super(realmConfig, claimManager, profileManager);
    }

    @Override
    public void doUpdateCredentialByAdmin(String userName, Object newCredential)
            throws UserStoreException {
        if (newCredential != null && !newCredential.equals("")) {
            super.doUpdateCredentialByAdmin(userName, newCredential);
        }
    }



}
