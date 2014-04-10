package org.wso2.carbon.cloudmgt.users.beans;

import org.apache.commons.lang.ArrayUtils;
import org.wso2.carbon.cloudmgt.users.util.UserMgtUtil;


public class UserInfoBean {
	private String userName;
	private String firstName;
	private String lastName;
	private String email;
	/** Name to be displayed in front end */
	private String displayName;
	private String[] roles;

	public UserInfoBean(String userName, String firstName, String lastName, 
			String email, String displayName,String[] roles) {
		this.userName = userName;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.displayName = displayName;
		String everyOneRoleName = UserMgtUtil.getRealmService()
				.getBootstrapRealmConfiguration().getEveryOneRoleName();
		if(ArrayUtils.contains(roles,everyOneRoleName)){
			this.roles=(String[]) ArrayUtils.removeElement(roles, everyOneRoleName);
		}else{
			this.roles=roles;

		}
		
 	}

	public String[] getRoles() {
		return roles;
	}

	public void setRoles(String[] roles) {
		this.roles = roles;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
}
