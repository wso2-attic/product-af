/*
 * Copyright 2004,2013 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.appfactory.ext.listener;

import org.wso2.carbon.appfactory.ext.Util;
import org.wso2.carbon.user.api.ClaimMapping;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractClaimManagerListener;


public class AppFactoryClaimManagerListener extends AbstractClaimManagerListener {
    private int executionOrderId = 10;

    public void setExecutionOrderId(int executionOrderId) {
        this.executionOrderId = executionOrderId;
    }

    @Override
    public int getExecutionOrderId() {
        return executionOrderId;
    }

    @Override
    public boolean getAttributeName(String claimURI) throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed()) {
            Util.checkAuthorizationForUserRealm();
        }
        return super.getAttributeName(claimURI);
    }

    @Override
    public boolean getClaim(String claimURI) throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed()) {
            Util.checkAuthorizationForUserRealm();
        }
        return super.getClaim(claimURI);
    }

    @Override
    public boolean getClaimMapping(String claimURI) throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed()) {
            Util.checkAuthorizationForUserRealm();
        }
        return super.getClaimMapping(claimURI);
    }

    @Override
    public boolean getAllSupportClaimMappingsByDefault() throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed()) {
            Util.checkAuthorizationForUserRealm();
        }
        return super.getAllSupportClaimMappingsByDefault();
    }

    @Override
    public boolean getAllClaimMappings() throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed()) {
            Util.checkAuthorizationForUserRealm();
        }
        return super.getAllClaimMappings();
    }

    @Override
    public boolean getAllClaimMappings(String dialectUri) throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed()) {
            Util.checkAuthorizationForUserRealm();
        }
        return super.getAllClaimMappings(dialectUri);
    }

    @Override
    public boolean getAllRequiredClaimMappings() throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed()) {
            Util.checkAuthorizationForUserRealm();
        }
        return super.getAllRequiredClaimMappings();
    }

    @Override
    public boolean getAllClaimUris() throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed()) {
            Util.checkAuthorizationForUserRealm();
        }
        return super.getAllClaimUris();
    }

    @Override
    public boolean addNewClaimMapping(ClaimMapping mapping) throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed()) {
            Util.checkAuthorizationForUserRealm();
        }
        return super.addNewClaimMapping(mapping);
    }

    @Override
    public boolean deleteClaimMapping(ClaimMapping mapping) throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed()) {
            Util.checkAuthorizationForUserRealm();
        }
        return super.deleteClaimMapping(mapping);
    }

    @Override
    public boolean updateClaimMapping(ClaimMapping mapping) throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed()) {
            Util.checkAuthorizationForUserRealm();
        }
        return super.updateClaimMapping(mapping);
    }

    @Override
    public boolean getAttributeName(String domainName, String claimURI) throws UserStoreException {
        if (!Util.isRequestFromSystemCode() && Util.isApplicationSpecificRequest() && !Util.isUserMgtPermissionsAllowed()) {
            Util.checkAuthorizationForUserRealm();
        }
        return super.getAttributeName(domainName, claimURI);
    }
}
