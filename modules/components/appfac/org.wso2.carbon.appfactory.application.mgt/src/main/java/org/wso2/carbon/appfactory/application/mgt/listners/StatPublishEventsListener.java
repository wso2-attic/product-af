package org.wso2.carbon.appfactory.application.mgt.listners;


import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.application.mgt.service.ApplicationManagementException;
import org.wso2.carbon.appfactory.application.mgt.util.Util;
import org.wso2.carbon.appfactory.bam.integration.BamDataPublisher;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.ApplicationEventsHandler;
import org.wso2.carbon.appfactory.core.dto.Application;
import org.wso2.carbon.appfactory.core.dto.UserInfo;
import org.wso2.carbon.appfactory.core.dto.Version;
import org.apache.commons.logging.Log;
import org.wso2.carbon.user.api.UserStoreException;

public class StatPublishEventsListener extends ApplicationEventsHandler {

    private static Log log = LogFactory.getLog(StatPublishEventsListener.class);


    @Override
    public void onCreation(Application application, String userName,
                           String tenantDomain) throws AppFactoryException {

         // Publish stats to BAM
        try {
			BamDataPublisher publisher = new BamDataPublisher();
			String tenantId = null;

			try {
			    tenantId = "" + Util.getRealmService().getTenantManager().getTenantId(tenantDomain);
			} catch (UserStoreException e) {
			    String errorMsg = "Unable to get tenant ID for bam stats : " + e.getMessage();
			    log.error(errorMsg, e);
			    throw new AppFactoryException(errorMsg, e);
			}

			String applicationName = application.getName();
			String applicationId = application.getId();
			String applicationDescription = application.getDescription();
			String applicationType = application.getType();
			String repositoryType = application.getRepositoryType();

			log.info("Publishing app creation stats to bam");

			publisher.PublishAppCreationEvent(applicationName, applicationId,
			        applicationDescription, applicationType, repositoryType,
			        System.currentTimeMillis(), "" + tenantId, userName);

			// TODO: Get initial version and stage from configuration
			publisher.PublishAppVersionEvent(applicationName, applicationId,
			        System.currentTimeMillis(), tenantId, userName, "trunk", "Development" );
		} catch (Exception e) {
			log.error("Failed to publish stats for " + application.getId() + e.getMessage(), e);
			//not throwing
		}
    }

    @Override
    public void onDeletion(Application application, String userName, String tenantDomain) throws AppFactoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onUserAddition(Application application, UserInfo user, String tenantDomain) throws AppFactoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onUserDeletion(Application application, UserInfo user, String tenantDomain) throws AppFactoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onUserUpdate(Application application, UserInfo user, String tenantDomain) throws AppFactoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onRevoke(Application application, String tenantDomain) throws AppFactoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onVersionCreation(Application application, Version source, Version target, String tenantDomain, String userName) throws AppFactoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onLifeCycleStageChange(Application application, Version version, String previosStage, String nextStage, String tenantDomain) throws AppFactoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getPriority() {
        return 2;  //To change body of implemented methods use File | Settings | File Templates.
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
