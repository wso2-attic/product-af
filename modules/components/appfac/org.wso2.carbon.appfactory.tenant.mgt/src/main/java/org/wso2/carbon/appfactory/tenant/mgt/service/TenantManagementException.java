package org.wso2.carbon.appfactory.tenant.mgt.service;

public class TenantManagementException extends Exception{

	private static final long serialVersionUID = 1L;

	public TenantManagementException() {
    }

    public TenantManagementException(String s) {
        super(s);
    }

    public TenantManagementException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public TenantManagementException(Throwable throwable) {
        super(throwable);
    }

}
