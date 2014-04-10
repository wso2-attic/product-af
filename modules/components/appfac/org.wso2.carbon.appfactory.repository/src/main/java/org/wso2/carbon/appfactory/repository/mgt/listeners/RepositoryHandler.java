package org.wso2.carbon.appfactory.repository.mgt.listeners;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.ApplicationEventsHandler;
import org.wso2.carbon.appfactory.core.dto.Application;
import org.wso2.carbon.appfactory.core.dto.UserInfo;
import org.wso2.carbon.appfactory.core.dto.Version;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryMgtException;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryProvider;
import org.wso2.carbon.appfactory.repository.mgt.internal.Util;

public class RepositoryHandler  extends ApplicationEventsHandler{
	
	private static final int priority = 100;
	private static final Log log = LogFactory.getLog(RepositoryHandler.class);

	@Override
	public void onCreation(Application application, String userName,
			String tenantDomain) throws AppFactoryException {
		String url = null;
        RepositoryProvider provider = Util.getRepositoryProvider(application.getRepositoryType());
		try {
			provider.createRepository(application.getId(), tenantDomain);
			url = provider.getAppRepositoryURL(application.getId(), tenantDomain);
			provider.getBranchingStrategy().prepareRepository(application.getId(), url, tenantDomain);
		} catch (RepositoryMgtException e) {
			log.error("Error while preparing repository", e);
		}
	}

    @Override
    public void onDeletion(Application application, String userName,
                           String tenantDomain) throws AppFactoryException {
    	RepositoryProvider provider = Util.getRepositoryProvider(application.getRepositoryType());
		try {
			provider.deleteRepository(application.getId(), tenantDomain);
		} catch (RepositoryMgtException e) {
			log.error("Error while preparing repository", e);
		}
    }

	@Override
	public void onUserAddition(Application application, UserInfo user,
			String tenantDomain) throws AppFactoryException {
		//Fork the existing repo
        RepositoryProvider provider = Util.getRepositoryProvider(application.getRepositoryType());
        String repoUrl = null;
       // provider.createFork(repoUrl);

	}

	@Override
	public void onUserDeletion(Application application, UserInfo user,
			String tenantDomain) throws AppFactoryException {
		//no actions needed
	}

	@Override
	public void onUserUpdate(Application application, UserInfo user,
			String tenantDomain) throws AppFactoryException {
		//no actions needed
	}

	@Override
	public void onRevoke(Application application, String tenantDomain)
			throws AppFactoryException {
		//no actions needed
	}

	@Override
	public void onVersionCreation(Application application, Version source,
			Version target, String tenantDomain, String userName)
			throws AppFactoryException {
		//no actions needed
	}

	@Override
	public void onLifeCycleStageChange(Application application,
			Version version, String previosStage, String nextStage,
			String tenantDomain) throws AppFactoryException {
		//no actions needed
	}

	@Override
	public int getPriority() {
		return priority;
	}

    @Override
    public boolean hasExecuted(Application application, String userName, String tenantDomain) throws AppFactoryException {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

	@Override
	public void onForking(Application application, String version,
			String userName, String tenantDomain) throws AppFactoryException {
		// TODO Auto-generated method stub
		
	}

}
