/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.appfactory.core;

import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.dto.Application;
import org.wso2.carbon.appfactory.core.dto.UserInfo;
import org.wso2.carbon.appfactory.core.dto.Version;

public abstract class ApplicationEventsHandler implements Comparable<ApplicationEventsHandler> {

	// Holds the name of the external system
	protected String identifier;
	
	protected int priority;	

	public ApplicationEventsHandler(String identifier, int priority) {
		super();
		this.identifier = identifier;
		this.priority = priority;
	}

	/**
	 * Get the external system identifier
	 * 
	 * @return identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Invoked after a application is created
	 * 
	 * @param application
	 *            {@link Application}
	 * @param userName
	 *            User name
	 * @param tenantDomain
	 *            Tenant domain of application
	 * @param isUploadableAppType
	 *            application is uploadable 
	 * @throws AppFactoryException
	 */
	public abstract void onCreation(Application application, String userName, String tenantDomain, boolean isUploadableAppType)
	                                                                                              throws AppFactoryException;

	/**
	 * Invoked when application needs to be deleted
	 * 
	 * @param application
	 *            {@link Application}
	 * @param userName
	 *            User name
	 * @param tenantDomain
	 *            Tenant domain of application
	 * @throws AppFactoryException
	 */
	public abstract void onDeletion(Application application, String userName, String tenantDomain)
	                                                                                              throws AppFactoryException;

	/**
	 * Invoked after adding a user to a application
	 * 
	 * @param application
	 * @param user
	 * @throws AppFactoryException
	 */
	public abstract void onUserAddition(Application application, UserInfo user, String tenantDomain)
	                                                                                                throws AppFactoryException;

	/**
	 * Invoked after removing a user from an application.
	 * 
	 * @param application
	 * @param user
	 * @throws AppFactoryException
	 */
	public abstract void onUserDeletion(Application application, UserInfo user, String tenantDomain)
	                                                                                                throws AppFactoryException;

	/**
	 * Invoked after updating a user of an application.
	 * 
	 * @param application
	 * @param user
	 * @throws AppFactoryException
	 */
	public abstract void onUserUpdate(Application application, UserInfo user, String tenantDomain)
	                                                                                              throws AppFactoryException;

	/**
	 * Invoked after revoking an application
	 * 
	 * @param application
	 * @throws AppFactoryException
	 */
	public abstract void onRevoke(Application application, String tenantDomain) throws AppFactoryException;

	/**
	 * Invoked after creating a new application version using an existing.
	 * 
	 * @param application
	 *            The Application.
	 * @param source
	 *            source version.
	 * @param target
	 *            target/new version
	 * @param tenantDomain
	 *            tenant domain
	 * @throws AppFactoryException
	 *             if an error occurs
	 */
	public abstract void onVersionCreation(Application application, Version source, Version target,
	                                       String tenantDomain, String userName) throws AppFactoryException;

	
	/**
	 * Invoked after a application is folked by user
	 * 
	 * @param application
	 *            {@link Application}
	 * @param userName
	 *            User name
	 * @param tenantDomain
	 *            Tenant domain of application
	 * @param version TODO
	 * @param forkedUsers TODO
	 * @throws AppFactoryException
	 */
	public abstract void onFork(Application application,String userName, String tenantDomain, String version, String[] forkedUsers) throws AppFactoryException;
	       
	
	
	
	/**
	 * Invoked after lifecycle stage change
	 * 
	 * @param application
	 * @throws AppFactoryException
	 */
	public abstract void onLifeCycleStageChange(Application application, Version version, String previosStage,
	                                            String nextStage, String tenantDomain) throws AppFactoryException;

	/**
	 * The priority given to Listener.
	 * <p>
	 * e.g. If listener X has priority 10 and Y has 20. Listner Y will given program control before X when application
	 * event occurs.
	 * 
	 * @return The priority
	 */
	public int getPriority() {
		return this.priority;
	}

	/**
	 * Check whether the listener has been executed against the given application
	 * 
	 * @param application
	 * @param userName
	 * @param tenantDomain
	 * @return
	 * @throws AppFactoryException
	 */
	public abstract boolean hasExecuted(Application application, String userName, String tenantDomain)
	                                                                                                  throws AppFactoryException;

	public int compareTo(ApplicationEventsHandler o) {

		return (this.getPriority() > o.getPriority() ? -1 : (this.getPriority() == o.getPriority() ? 0 : 1));
	}

}
