package org.wso2.carbon.appfactory.deployers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.Deployer;
import org.wso2.carbon.appfactory.deployers.build.api.BuildStatusProvider;
import org.wso2.carbon.appfactory.deployers.util.DeployerUtil;


/**
 * 
 * This abstract class is used to handle the deployment pattern. 
 * 
 * ex : deploy latest artifact, promoting a application or label last success as PROMOTED.
 * There are few more method should implement by extending to get done some deployment using this abstraction.
 *
 */
public abstract class AbstractDeployer implements Deployer {

	private static final Log log = LogFactory.getLog(AbstractDeployer.class);

	protected String adminUserName;
	protected String adminPassword;
	protected String appfactoryServerURL;
	protected String storagePath;
	protected String tempPath;
	protected BuildStatusProvider buildStatusProvider;
	protected String tenantDomain;
	protected int tenantID;

	/**
	 * This method can be used to deploy a promoted artifact to a stage
	 * We have used this method after first promote action of an application, so the artifact
	 * deployed in first promote action will be deployed in the next promote action
	 */
	public void deployPromotedArtifact(Map<String, String[]> parameters) throws Exception {

		String artifactType = DeployerUtil.getParameter(parameters, AppFactoryConstants.ARTIFACT_TYPE);
		String applicationId = DeployerUtil.getParameter(parameters, AppFactoryConstants.APPLICATION_ID);
		String stageName = DeployerUtil.getParameter(parameters, AppFactoryConstants.DEPLOY_STAGE);
		String version = DeployerUtil.getParameter(parameters, AppFactoryConstants.APPLICATION_VERSION);
		String tenantDomain = getTenantDomain();
		String pathToPromotedArtifact =   getArtifactStoragePath(applicationId, version, artifactType, stageName, tenantDomain);

		File[] fileToDeploy = getArtifact(pathToPromotedArtifact, artifactType);

		deploy(artifactType, fileToDeploy, parameters, false);
	}

	/**
	 * This will deploy the latest successfully built artifact of the given job
	 * 
	 * @param parameters
	 *            request
	 * @throws
	 */
	public void deployLatestSuccessArtifact(Map<String, String[]> parameters) throws AppFactoryException {

		String applicationId = DeployerUtil.getParameter(parameters, AppFactoryConstants.APPLICATION_ID);
		String stageName = DeployerUtil.getParameter(parameters, AppFactoryConstants.DEPLOY_STAGE);
		String deployAction = DeployerUtil.getParameter(parameters, AppFactoryConstants.DEPLOY_ACTION);
		String artifactType = DeployerUtil.getParameter(parameters, AppFactoryConstants.ARTIFACT_TYPE);
		String version = DeployerUtil.getParameter(parameters, AppFactoryConstants.APPLICATION_VERSION);
		String tenantDomain = getTenantDomain();

		try {

			String path =
			              this.getSuccessfulArtifactTempStoragePath(applicationId, version, artifactType, stageName,
			                                                        tenantDomain);

			File[] artifactToDeploy = getLastBuildArtifact(path, artifactType);

			if (deployAction.equalsIgnoreCase(AppFactoryConstants.DEPLOY_ACTION_LABEL_ARTIFACT)) {
				deploy(artifactType, artifactToDeploy, parameters, true);
				log.debug("Making last successful build as PROMOTED");
				labelLastSuccessAsPromoted(applicationId, version, artifactType,stageName, tenantDomain);
			} else {
				deploy(artifactType, artifactToDeploy, parameters, true);
			}
			
			
		} catch (Exception e) {
			String errMsg = "Error when calling deployLatestSuccessArtifact " + e.getMessage();
			log.error(errMsg, e);
			throw new AppFactoryException(errMsg, e);
		}
	}

	/**
	 * Handling Exception under this class root
	 * @param msg
	 * @throws AppFactoryException
	 */
	protected void handleException(String msg) throws AppFactoryException {
		log.error(msg);
		throw new AppFactoryException(msg);
	}

	/**
	 * Handling Exception under this class root
	 * @param msg
	 * @throws AppFactoryException
	 */
	public void handleException(String msg, Exception e) throws AppFactoryException {
		log.error(msg, e);
		throw new AppFactoryException(msg, e);
	}

	/**
	 * This method will be used to retrieve the artifact in the given path
	 * 
	 * @param path
	 *            path were artifact has been stored
	 * @param artifactType
	 *            artifact type (car/war)
	 * @return the artifact
	 * @throws AppFactoryException
	 */
	protected File[] getArtifact(String path, String artifactType) throws AppFactoryException {
		String[] fileExtension = new String[0];

		List<File> fileList = new ArrayList<File>();
		if (AppFactoryConstants.APPLICATION_TYPE_JAXWS.equals(artifactType) ||
		    AppFactoryConstants.APPLICATION_TYPE_JAXRS.equals(artifactType)) {
			fileExtension = new String[] { AppFactoryConstants.APPLICATION_TYPE_WAR };
		} else if (AppFactoryConstants.APPLICATION_TYPE_JAGGERY.equals(artifactType)) {
			fileExtension = new String[] { AppFactoryConstants.APPLICATION_TYPE_ZIP };
		} else if (AppFactoryConstants.APPLICATION_TYPE_DBS.equals(artifactType)) {
			fileExtension =
			                new String[] { AppFactoryConstants.APPLICATION_TYPE_DBS,
			                              AppFactoryConstants.APPLICATION_TYPE_XML };
		} else if (AppFactoryConstants.APPLICATION_TYPE_BPEL.equals(artifactType)) {
			fileExtension = new String[] { AppFactoryConstants.APPLICATION_TYPE_ZIP };
		} else if (AppFactoryConstants.APPLICATION_TYPE_PHP.equals(artifactType)) {
			File phpAppParentDirectory = new File(path + File.separator + "archive");
			for (File phpAppDir : phpAppParentDirectory.listFiles()) {
				if (phpAppDir.isDirectory() && phpAppDir.getName().contains("-")) {
					fileList.add(phpAppDir.getAbsoluteFile());
				}
			}
		} else if (AppFactoryConstants.APPLICATION_TYPE_ESB.equals(artifactType)) {
			fileExtension = new String[] { AppFactoryConstants.APPLICATION_TYPE_XML };
		} else if (AppFactoryConstants.APPLICATION_TYPE_UPLOADED_WAR.equals(artifactType)) {
			fileExtension = new String[] { AppFactoryConstants.APPLICATION_TYPE_WAR };
		} else if (AppFactoryConstants.APPLICATION_TYPE_UPLOADED_JAGGERY.equals(artifactType)) {
			fileExtension = new String[] { AppFactoryConstants.APPLICATION_TYPE_ZIP };
		} else {
			fileExtension = new String[] { artifactType };
		}

		fileList.addAll((List<File>) FileUtils.listFiles(new File(path), fileExtension, true));

		if (!(fileList.size() > 0)) {
			String errMsg = "No built artifact found ";
			log.error(errMsg);
			throw new AppFactoryException(errMsg);
		}

		return fileList.toArray(new File[fileList.size()]);
	}

	/**
	 * Returns the artifacts related to last build location.
	 * 
	 * @param path
	 * @param artifactType
	 * @return
	 * @throws AppFactoryException
	 */
	protected File[] getLastBuildArtifact(String path, String artifactType) throws AppFactoryException {
		// Archive folder is considered for freestyle projects.
		if (AppFactoryConstants.APPLICATION_TYPE_ESB.equals(artifactType)) {
			path = path + File.separator + "archive";
		}
		return getArtifact(path, artifactType);
	}

	/**
	 * Method labels the last successful build as PROMOTED by copying last successful build into PROMOTED location.
	 * 
	 * @param jobName
	 * @param artifactType
	 * @throws AppFactoryException
	 * @throws IOException
	 * @throws InterruptedException
	 */
    public void labelLastSuccessAsPromoted(String applicationId, String version,
                                           String artifactType, String stage, String tenantDomain)
                                                                                                  throws AppFactoryException,
                                                                                                  IOException,
                                                                                                  InterruptedException {

		String lastSucessBuildFilePath =
		                                 getSuccessfulArtifactTempStoragePath(applicationId, version, artifactType,
		                                                                      stage, tenantDomain);
		log.debug("Last success build path is :" + lastSucessBuildFilePath);

		String dest = getArtifactStoragePath(applicationId, version, artifactType, stage, tenantDomain);

		File destDir = new File(dest);
		if (destDir.exists()) {
			FileUtils.cleanDirectory(destDir.getParentFile());
		}
		if (!destDir.mkdirs()) {
			log.error("Unable to create promoted tag for application :" + applicationId + ", version :" + version);
			throw new AppFactoryException(
			                              "Error occured while creating dir for last successful as PROMOTED: application :" +
			                                      applicationId + ", version :" + version);
		}

		File[] lastSucessFiles = getLastBuildArtifact(lastSucessBuildFilePath, artifactType);
		for (File lastSucessFile : lastSucessFiles) {
			File lastSuccessArtifactPath = lastSucessFile;
			String promotedDestDirPath =
			                             destDir.getAbsolutePath() +
			                                     File.separator +
			                                     getPromotedDestinationPathForApplication(lastSucessFile.getParent(),
			                                                                              artifactType);

			File promotedDestDir = new File(promotedDestDirPath);
			if (!promotedDestDir.exists()) {
				promotedDestDir.mkdirs();
			}

			File destinationFile =
			                       new File(promotedDestDir.getAbsolutePath() + File.separator +
			                                lastSuccessArtifactPath.getName());

			if (lastSuccessArtifactPath.isDirectory()) {
				FileUtils.copyDirectory(lastSuccessArtifactPath, destinationFile);
			} else {
				FileUtils.copyFile(lastSuccessArtifactPath, destinationFile);
			}
			log.debug("labeled the lastSuccessful as PROMOTED");
		}
	}
	
	
	protected String getAdminUsername() {
		return adminUserName;
	}

	protected String getAdminUsername(String applicationId) {
		return adminUserName + "@" + applicationId;
	}

	protected String getServerAdminPassword() {
		return adminPassword;
	}

	public void setAdminUserName(String adminUserName) {
		this.adminUserName = adminUserName;
	}

	public void setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
	}

	public void setAppfactoryServerURL(String appfactoryServerURL) {
		this.appfactoryServerURL = appfactoryServerURL;
	}

	public void setStoragePath(String storagePath) {
		this.storagePath = storagePath;
	}

	public String getStoragePath() {
		return this.storagePath;
	}

	public void setTempPath(String tempPath) {
		this.tempPath = tempPath;
	}

	public int getTenantID() {
		return tenantID;
	}

	public void setTenantID(int tenantID) {
		this.tenantID = tenantID;
	}

	public String getTenantDomain() {
		return tenantDomain;
	}

	public void setTenantDomain(String tenantDomain) {
		this.tenantDomain = tenantDomain;
	}
	
	
	

	public abstract String getSuccessfulArtifactTempStoragePath(String applicationId, String applicationVersion,
	                                                            String artifactType, String stage, String tenantDomain)
	                                                                                                                   throws AppFactoryException;

	public abstract String getArtifactStoragePath(String applicationId, String applicationVersion, String artifactType,
	                                              String stage, String tenantDomain) throws AppFactoryException;

	public abstract String getPromotedDestinationPathForApplication(String filepath, String artifactType);

	public abstract void postDeploymentNoifier(String message, String applicationId, String applicationVersion,
	                                           String artifactType, String stage, String tenantDomain);

	protected abstract void deploy(String artifactType, File[] artifactsToDeploy, Map<String, String[]> parameters,
	                               Boolean notify) throws AppFactoryException;
}
