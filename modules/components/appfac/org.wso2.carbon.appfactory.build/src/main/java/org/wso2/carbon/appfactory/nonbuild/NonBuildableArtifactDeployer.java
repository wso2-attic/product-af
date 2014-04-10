package org.wso2.carbon.appfactory.nonbuild;

import java.io.File;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.deploy.ApplicationDeployer;
import org.wso2.carbon.appfactory.deployers.AbstractStratosDeployer;
import org.wso2.carbon.appfactory.deployers.util.DeployerUtil;
import org.wso2.carbon.appfactory.nonbuild.artifact.ArtifactGeneratorFactory;
import org.wso2.carbon.appfactory.nonbuild.artifact.DeployableArtifact;
import org.wso2.carbon.appfactory.nonbuild.utility.ApplicationBuildUtility;
import org.wso2.carbon.context.CarbonContext;

/**
 * <p>Implements how a non-buildable artifact needs to be deployed in Stratos. 
 * This class has the knowledge about how to collect (non-buildable) artifacts from the file system.</p>
 * 
 * <b>Note</b>:Logic in this class is meant to be executed in Appfactory VM ( as oppose to executing on Build server. i.e. Jenkins).
 */
public class NonBuildableArtifactDeployer extends AbstractStratosDeployer {

	private static Log log = LogFactory.getLog(NonBuildableArtifactDeployer.class);
    /**
     * {@inheritDoc}
     */
	@Override
	public void deployLatestSuccessArtifact(Map<String, String[]> parameters) throws AppFactoryException {

		String applicationId = DeployerUtil.getParameter(parameters, AppFactoryConstants.APPLICATION_ID);
		String stageName = DeployerUtil.getParameter(parameters, AppFactoryConstants.DEPLOY_STAGE);
		//String deployAction = DeployerUtil.getParameter(parameters, AppFactoryConstants.DEPLOY_ACTION);
		String artifactType = DeployerUtil.getParameter(parameters, AppFactoryConstants.ARTIFACT_TYPE);
		String version = DeployerUtil.getParameter(parameters, AppFactoryConstants.APPLICATION_VERSION);

		String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
		int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
		super.setTenantDomain(tenantDomain);
		super.setTenantID(tenantId);
		super.setTempPath(getArtifactTempPath(applicationId, version, artifactType, stageName, tenantDomain));
		super.setStoragePath(getArtifactStoragePath(applicationId, version, artifactType, stageName, tenantDomain));

		ArtifactGeneratorFactory artifactGeneratorFactory = ArtifactGeneratorFactory.newInstance();
		
		String tmpStgPath =
		                    getSuccessfulArtifactTempStoragePath(applicationId, version, artifactType, stageName,
		                                                         tenantDomain);
		DeployableArtifact deployableArtifact =
		                                        artifactGeneratorFactory.generateDeployableArtifact(tmpStgPath,
		                                                                                            applicationId,
		                                                                                            version, stageName,
		                                                                                            artifactType,
		                                                                                            tenantDomain);
		deployableArtifact.generateDeployableFile();

		super.deployLatestSuccessArtifact(parameters);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getBaseRepoUrl(String stage, String appType) throws AppFactoryException {

		return ApplicationBuildUtility.getS2GitRepositoryProviderProperty(stage, "BaseURL", appType);
	}
	
    /**
     * {@inheritDoc}
     */
	@Override
	protected String getBaseRepoUrlPattern(String stage, String appType) throws AppFactoryException {

		return ApplicationBuildUtility.getS2GitRepositoryProviderProperty(stage, "URLPattern", appType);
	}

    /**
     * {@inheritDoc}
     */
	@Override
	protected String getAdminPassword(String stage, String appType) throws AppFactoryException {
		return DeployerUtil.getRepositoryProviderProperty(stage, "AdminUserName", appType);
	}

    /**
     * {@inheritDoc}
     */
	@Override
	protected String getAdminUserName(String stage, String appType) throws AppFactoryException {
		return DeployerUtil.getRepositoryProviderProperty(stage, "AdminPassword", appType);
	}

    /**
     * {@inheritDoc}
     */
	protected String getAppFactoryGitAdminPassword() throws AppFactoryException {
		return ApplicationBuildUtility.getSourceRepositoryProviderProperty("AdminUserName");
	}

	protected String getAppFactoryGitAdminUserName() throws AppFactoryException {
		return ApplicationBuildUtility.getSourceRepositoryProviderProperty("AdminPassword");
	}

	@Override
	protected String getServerDeploymentPaths(String appType) throws AppFactoryException {
		return DeployerUtil.getAppFactoryConfigurationProperty("ApplicationType." + appType +
		                                                       ".Property.ServerDeploymentPaths");
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public String getSuccessfulArtifactTempStoragePath(String applicationId, String applicationVersion,
	                                                   String artifactType, String stage, String tenantDomain)
	                                                                                                          throws AppFactoryException {
		String home = ApplicationBuildUtility.getAppFactoryHome();
		String path =
		              home + File.separator + "nonbuildstorage" + File.separator + "appfactory" + File.separator +
		                      tenantDomain + File.separator + stage + File.separator + applicationId + "-" +
		                      applicationVersion;
		return path;
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public String getArtifactStoragePath(String applicationId, String applicationVersion, String artifactType,
	                                     String stage, String tenantDomain) throws AppFactoryException {
		String home = ApplicationBuildUtility.getAppFactoryHome();
		String path =
		              home + File.separator + "nonbuildstorage" + File.separator + "s2storage" + File.separator +
		                      tenantDomain + File.separator + applicationId;
		return path;
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public void postDeploymentNoifier(String message, String applicationId, String applicationVersion,
	                                  String artifactType, String stage, String tenantDomain) {
		ApplicationDeployer applicationDeployer = new ApplicationDeployer();
		try {
			applicationDeployer.updateDeploymentInformation(applicationId, stage, applicationVersion, "", tenantDomain);
		} catch (AppFactoryException e) {
			String errMsg = "Error when call notifier : " + e.getMessage();
			log.error(errMsg, e);
		}

	}
	
	
	/**
	 * This is regarding get the artifact store temp path
	 * 
	 * @param applicationId
	 * @param applicationVersion
	 * @param artifactType
	 * @param stage
	 * @param tenantDomain
	 * @return
	 * @throws AppFactoryException
	 */
	private String getArtifactTempPath(String applicationId, String applicationVersion, String artifactType, String stage,
	                          String tenantDomain) throws AppFactoryException {
		String home = ApplicationBuildUtility.getAppFactoryHome();
		String path =
		              home + File.separator + "nonbuildstorage" + File.separator + "s2tmp" + File.separator +
		                      tenantDomain;
		return path;
	}

	@Override
    public void deployTaggedArtifact(Map<String, String[]> requestParameters) throws Exception {
	    // TODO Auto-generated method stub
	    
    }
}
