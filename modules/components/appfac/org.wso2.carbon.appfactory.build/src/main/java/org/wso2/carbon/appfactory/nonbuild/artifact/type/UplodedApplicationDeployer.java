/**
 * 
 */
package org.wso2.carbon.appfactory.nonbuild.artifact.type;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.nonbuild.artifact.ArtifactGeneratorFactory;
import org.wso2.carbon.appfactory.nonbuild.artifact.DeployableArtifact;
import org.wso2.carbon.appfactory.utilities.project.ProjectUtils;
import org.wso2.carbon.context.CarbonContext;

/**
 * 
 *
 */
public class UplodedApplicationDeployer extends DeployableArtifact {

	private static Log log = LogFactory.getLog(UplodedApplicationDeployer.class);

	public UplodedApplicationDeployer(String rootPath, String applicationId, String version,
	                                  String stage) {
		super(rootPath, applicationId, version, stage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.wso2.carbon.appfactory.nonbuild.artifact.DeployableArtifact#
	 * generateDeployableFile()
	 */
	@Override
	public void generateDeployableFile() throws AppFactoryException {

		String artifactFileName = null;
		String applicationExtenstion = ProjectUtils.getApplicationExtenstion(getApplicationId(), CarbonContext.getThreadLocalCarbonContext().getTenantDomain());	
		if (getVersion().equals("trunk")) {
			artifactFileName = getApplicationId() + "." + applicationExtenstion;
			
		} else {
			artifactFileName = getApplicationId() + "-" + getVersion() + "." + applicationExtenstion;
		}
		
		String uploadedAppSrcFile =
		                              getRootPath() + File.separator +
		                                      ArtifactGeneratorFactory.appfactoryGitTmpFolder + File.separator + artifactFileName;

		String uploadedApptmpFolder =
		                              getRootPath() + File.separator +
		                                      ArtifactGeneratorFactory.deployableAtrifactFolder;
		                                      
		try {
			
			FileUtils.copyFileToDirectory(new File(uploadedAppSrcFile), new File(uploadedApptmpFolder));
		} catch (IOException e) {
			String errMsg =
			                "Error when copying folder from src to artifact tmp : " +
			                        e.getMessage();
			log.error(errMsg, e);
			throw new AppFactoryException(errMsg, e);
		}

	}
}
