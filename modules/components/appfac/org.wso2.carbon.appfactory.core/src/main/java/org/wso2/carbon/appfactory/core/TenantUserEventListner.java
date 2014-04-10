package org.wso2.carbon.appfactory.core;

import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.dto.UserInfo;

public abstract class TenantUserEventListner implements Comparable<TenantUserEventListner>{
	
	protected String identifier;

	public String getIdentifier() {
		return identifier;
	}
	
	/**
	 * This method should be called when the user is assigned a role for the firstime.
	 * @param userName name of the user
	 * @param roles roles to be added
	 * @param tenantDomain tenantDomain that user belongs to 
	 * @throws AppFactoryException
	 */
	public abstract void onUserRoleAddition(UserInfo user,String tenantDomain) throws AppFactoryException;
	
	/**
	 * This should be called when the roles of user are updated
	 * @param user user to be updated
	 * @param tenantDomain tenant domain that user belongs to
	 * @throws AppFactoryException
	 */
	public abstract void onUserUpdate(UserInfo user,String tenantDomain) throws AppFactoryException;

	/**
	 * This should be called when the user is deleted from the tenant
	 * @param user
	 * @param tenantDomnain
	 * @throws AppFactoryException
	 */
	public abstract void onUserDeletion(UserInfo user,String tenantDomnain) throws AppFactoryException;
	

}
