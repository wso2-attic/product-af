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

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.userstore.internal.OTLDAPUtil;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.ldap.LDAPConstants;
import org.wso2.carbon.user.core.tenant.Tenant;

import javax.naming.directory.DirContext;
import java.util.Map;

public class OTAppFactoryTenantManager extends AppFactoryTenantManager {
    private static Log log = LogFactory.getLog(OTAppFactoryTenantManager.class);

    public OTAppFactoryTenantManager(OMElement omElement,
                                     Map<String, Object> properties) throws Exception {
        super(omElement, properties);
    }

    @Override
    protected void createOrganizationalUnit(String orgName, Tenant tenant,
                                            DirContext initialDirContext)
            throws UserStoreException {
        tenant.setAdminName(doConvert(tenant.getAdminName()));
        super.createOrganizationalUnit(orgName, tenant, initialDirContext);
    }

    private String doConvert(String email) throws UserStoreException {
        String searchBase = realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
        return OTLDAPUtil.getUserIdFromEmail(email, ldapConnectionSource, searchBase);
    }

    @Override
    public String[] getAllTenantDomainStrOfUser(String username)
            throws org.wso2.carbon.user.api.UserStoreException {
        username = doConvert(username);
        return super.getAllTenantDomainStrOfUser(username);
    }
}
