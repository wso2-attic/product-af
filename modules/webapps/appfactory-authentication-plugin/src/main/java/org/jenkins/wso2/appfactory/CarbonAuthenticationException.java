package org.jenkins.wso2.appfactory;

import org.acegisecurity.AuthenticationException;

public class CarbonAuthenticationException extends AuthenticationException {

	public CarbonAuthenticationException(String msg) {
		super(msg);
	}

	public CarbonAuthenticationException(String msg, Throwable t) {
		super(msg, t);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
