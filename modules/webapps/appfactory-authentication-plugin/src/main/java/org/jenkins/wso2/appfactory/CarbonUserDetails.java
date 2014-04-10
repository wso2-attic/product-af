package org.jenkins.wso2.appfactory;

import org.acegisecurity.GrantedAuthority;

public class CarbonUserDetails extends  org.acegisecurity.userdetails.User {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String project;
	
	public CarbonUserDetails(String username, String password, GrantedAuthority[] authorities) {
	        super(username, password, true, true, true, true, authorities);
	}
}
