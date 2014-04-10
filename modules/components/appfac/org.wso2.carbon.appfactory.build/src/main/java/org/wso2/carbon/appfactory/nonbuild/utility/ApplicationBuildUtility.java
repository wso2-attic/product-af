package org.wso2.carbon.appfactory.nonbuild.utility;


import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.deploy.Artifact;
import org.wso2.carbon.appfactory.core.governance.RxtManager;
import org.wso2.carbon.appfactory.deployers.util.DeployerUtil;

public class ApplicationBuildUtility {

	public ApplicationBuildUtility() {
		
	}
	
	public static Artifact getArtifactDetail(String applicationId,
            String version,String tenantDomain){
		RxtManager rxtManager = new RxtManager();
		Artifact artifact = null ;
		try {
			artifact = rxtManager.getAppVersionDetailArtifact(applicationId, version, tenantDomain);
		} catch (AppFactoryException e) {
			
		}
		return artifact ;
	}
	
	public static String getAppFactoryHome() throws AppFactoryException {
        try {
			String carbonHome = System.getProperty("carbon.home");
			return carbonHome ;
		} catch (Exception e) {
			throw new AppFactoryException(e);
		}
    }
	
	public static String getS2GitRepositoryProviderProperty(String stage, String propertyName, String appType) 
    		throws AppFactoryException{
    	String repoProperty = DeployerUtil.getAppFactoryConfigurationProperty("ApplicationDeployment.DeploymentStage." + stage + 
    				".Deployer.ApplicationType." + appType + ".RepositoryProvider.Property." + propertyName);
		
    	if ( StringUtils.isBlank(repoProperty)){
    	    repoProperty = DeployerUtil.getAppFactoryConfigurationProperty("ApplicationDeployment.DeploymentStage." + stage + 
					".Deployer.ApplicationType.*.RepositoryProvider.Property." + propertyName);
    	}
    	
		return repoProperty;
	}
	public static String getServerDeploymentPathPerApp(String appType) throws AppFactoryException{
		return DeployerUtil.getAppFactoryConfigurationProperty("ApplicationType." + 
				appType + ".Property.ServerDeploymentPaths");
		
	} 
	
	
	public static String getSourceRepositoryProviderProperty(String propertyName) 
    		throws AppFactoryException{
    	String repoProperty = DeployerUtil.getAppFactoryConfigurationProperty("RepositoryProviderConfig.RepositoryProviderConfig."+propertyName);
		return repoProperty;
	}

}
