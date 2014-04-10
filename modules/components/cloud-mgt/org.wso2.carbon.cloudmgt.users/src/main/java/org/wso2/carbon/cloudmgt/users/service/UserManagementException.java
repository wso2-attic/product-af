package org.wso2.carbon.cloudmgt.users.service;

public class UserManagementException extends Exception{

	private static final long serialVersionUID = 1L;

	public UserManagementException() {
    }

    public UserManagementException(String s) {
        super(s);
    }

    public UserManagementException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public UserManagementException(Throwable throwable) {
        super(throwable);
    }

}
