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
