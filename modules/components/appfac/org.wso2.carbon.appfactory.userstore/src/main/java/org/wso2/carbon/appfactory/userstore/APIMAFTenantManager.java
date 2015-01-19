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

package org.wso2.carbon.appfactory.userstore;

import java.util.Map;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.tenant.Tenant;

public class APIMAFTenantManager extends AppFactoryTenantManager {
    
    public APIMAFTenantManager(OMElement omElement, Map<String, Object> properties) throws Exception {
        super(omElement, properties);
    }

    @Override
    public String[] getAllTenantDomainStrOfUser(String username) throws UserStoreException {
        return new String[0];
    }

    @Override
    protected String[] getTenantDomains(String userDN) throws UserStoreException {
        return new String[0];
    }

    @Override
    public Tenant[] getAllTenants() throws UserStoreException {
        return new Tenant[0];
    }
    
}
