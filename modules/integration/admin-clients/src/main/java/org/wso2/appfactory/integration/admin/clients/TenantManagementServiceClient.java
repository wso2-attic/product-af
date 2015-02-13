/*
 * Copyright 2015 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.appfactory.integration.admin.clients;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.integration.common.admin.client.utils.AuthenticateStubUtil;
import org.wso2.carbon.tenant.mgt.stub.TenantMgtAdminServiceExceptionException;
import org.wso2.carbon.tenant.mgt.stub.TenantMgtAdminServiceStub;
import org.wso2.carbon.tenant.mgt.stub.beans.xsd.TenantInfoBean;

import java.rmi.RemoteException;

/**
 * Admin client to invoke org.wso2.carbon.tenant.mgt.stub.TenantMgtAdminServiceStub
 */
public class TenantManagementServiceClient {
    private static final String SERVICE = "services/TenantMgtAdminService";
    private TenantMgtAdminServiceStub tenantMgtAdminServiceStub;

    /**
     * Construct authenticated TenantMgtAdminServiceStub
     *
     * @param backEndURL    backend url
     * @param sessionCookie session cookie
     * @throws AxisFault
     */
    public TenantManagementServiceClient(String backEndURL, String sessionCookie) throws AxisFault {
        String endPoint = backEndURL + SERVICE;
        tenantMgtAdminServiceStub = new TenantMgtAdminServiceStub(endPoint);
        AuthenticateStubUtil.authenticateStub(sessionCookie, tenantMgtAdminServiceStub);
    }

    /**
     * Add tenant
     *
     * @param tenantInfoBean tenant info
     * @throws RemoteException
     * @throws TenantMgtAdminServiceExceptionException
     */
    public String addTenant(TenantInfoBean tenantInfoBean)
        throws RemoteException, TenantMgtAdminServiceExceptionException {
        return tenantMgtAdminServiceStub.addTenant(tenantInfoBean);
    }

    /**
     * Get tenant
     *
     * @param tenantDomain tenant domain
     * @return tenant info
     * @throws TenantMgtAdminServiceExceptionException
     * @throws RemoteException
     */
    public TenantInfoBean getTenant(String tenantDomain)
        throws TenantMgtAdminServiceExceptionException, RemoteException {
        return tenantMgtAdminServiceStub.getTenant(tenantDomain);
    }

    /**
     * Activate Tenant
     *
     * @param tenantDomain tenant domain
     * @throws RemoteException
     * @throws TenantMgtAdminServiceExceptionException
     */
    public void activateTenant(String tenantDomain)
        throws RemoteException, TenantMgtAdminServiceExceptionException {
        tenantMgtAdminServiceStub.activateTenant(tenantDomain);
    }

}

