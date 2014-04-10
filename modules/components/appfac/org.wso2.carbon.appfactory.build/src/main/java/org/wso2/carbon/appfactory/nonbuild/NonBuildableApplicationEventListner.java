package org.wso2.carbon.appfactory.nonbuild;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.ApplicationEventsHandler;
import org.wso2.carbon.appfactory.core.deploy.ApplicationDeployer;
import org.wso2.carbon.appfactory.core.dto.Application;
import org.wso2.carbon.appfactory.core.dto.UserInfo;
import org.wso2.carbon.appfactory.core.dto.Version;
import org.wso2.carbon.appfactory.jenkins.build.JenkinsApplicationEventsListener;
import org.wso2.carbon.appfactory.utilities.project.ProjectUtils;

/**
 * 
 * This event trigger in the process of application creation.
 * 
 */
public class NonBuildableApplicationEventListner extends ApplicationEventsHandler {

	private static Log log = LogFactory.getLog(JenkinsApplicationEventsListener.class);

	@Override
	public void onCreation(Application application, String userName, String tenantDomain) throws AppFactoryException {
		
		if (AppFactoryUtil.isBuildable(application.getType())) {
			return;
		}
		
		log.info("Application Creation(Non-Build) event recieved for : " + application.getId() + " " +
				application.getName());
		ApplicationDeployer applicationDeployer = new ApplicationDeployer();
		applicationDeployer.deployArtifact(application.getId(), "Development", "trunk", "", "deploy");

	}

    @Override
    public void onDeletion(Application application, String userName, String tenantDomain) throws AppFactoryException {
    	
    	if (AppFactoryUtil.isBuildable(application.getType())) {
			return;
		}
    	
        // deleting the artifacts deployed
        ApplicationDeployer applicationDeployer = new ApplicationDeployer();
        applicationDeployer.undeployAllArtifactsOfAppFromDepSyncGitRepo(application.getId(), application.getType(), ProjectUtils.getVersions(application.getId(), tenantDomain));
    }

	@Override
	public void onUserAddition(Application application, UserInfo user, String tenantDomain) throws AppFactoryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUserDeletion(Application application, UserInfo user, String tenantDomain) throws AppFactoryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUserUpdate(Application application, UserInfo user, String tenantDomain) throws AppFactoryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRevoke(Application application, String tenantDomain) throws AppFactoryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onVersionCreation(Application application, Version source, Version target, String tenantDomain,
	                              String userName) throws AppFactoryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLifeCycleStageChange(Application application, Version version, String previosStage, String nextStage,
	                                   String tenantDomain) throws AppFactoryException {
		// TODO Auto-generated method stub

	}

	@Override
	public int getPriority() {
		// TODO Auto-generated method stub
		return 1;
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
