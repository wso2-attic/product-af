package org.wso2.carbon.appfactory.jenkins.build;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.*;
import org.wso2.carbon.appfactory.core.dto.UserInfo;
import org.wso2.carbon.appfactory.jenkins.build.internal.ServiceContainer;

public class JenkinsTenantUserEventListner extends TenantUserEventListner  {
	private Log log=LogFactory.getLog(JenkinsTenantUserEventListner.class);
	@Override
	public int compareTo(TenantUserEventListner arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void onUserDeletion(UserInfo userInfo, String tenantDomain)
			throws AppFactoryException {
		// TODO Auto-generated method stub
//		log.info("******************on user deletion event listner for jenkins is called");
//		  ServiceContainer.getJenkinsCISystemDriver()
//          .removeUsersFromApplication("", new String[]{userInfo.getUserName()}, tenantDomain);
	}

	@Override
	public void onUserRoleAddition(UserInfo userInfo, String tenantDomain)
			throws AppFactoryException {
		// TODO Auto-generated method stub
//		log.info("******************on user role addition event listner for jenkins is called");
//		  ServiceContainer.getJenkinsCISystemDriver()
//          .addUsersToApplication("",
//                  new String[]{userInfo.getUserName()}, tenantDomain);
		
	}

	@Override
	public void onUserUpdate(UserInfo userInfo, String tenantDomain)
			throws AppFactoryException {
		// TODO Auto-generated method stub
//		log.info("******************on user update event listner for jenkins is called");
 		
	}

}
