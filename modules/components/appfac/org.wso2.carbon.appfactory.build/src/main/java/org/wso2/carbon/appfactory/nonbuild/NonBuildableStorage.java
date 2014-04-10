package org.wso2.carbon.appfactory.nonbuild;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.Deployer;
import org.wso2.carbon.appfactory.core.Storage;

/**
 * 
 * This class is a storage implementation for non-buildable artifacts.
 * 
 */
public class NonBuildableStorage extends Storage {

	private static Log log = LogFactory.getLog(NonBuildableStorage.class);

	private Deployer deployer = new NonBuildableArtifactDeployer();

	@Override
	public String[] getTagNamesOfPersistedArtifacts(String applicationId, String version, String revision,
	                                                String tenantDomain) throws AppFactoryException {
		// Currently we are not support tag artifacts.
		return null;
	}

	@Override
	public void deployLatestSuccessArtifact(String applicationId, String version, String revision, String artifactType,
	                                        String stage, String tenantDomain, String userName, String deployAction)
	                                                                                                                throws AppFactoryException {

		try {
			Map<String, String[]> paramList = new HashMap<String, String[]>();

			paramList.put(AppFactoryConstants.APPLICATION_ID, new String[] { applicationId });
			paramList.put(AppFactoryConstants.APPLICATION_VERSION, new String[] { version });
			paramList.put(AppFactoryConstants.ARTIFACT_TYPE, new String[] { artifactType });
			paramList.put(AppFactoryConstants.DEPLOY_STAGE, new String[] { stage });
			paramList.put(AppFactoryConstants.DEPLOY_ACTION, new String[] { deployAction });
			paramList.put(AppFactoryConstants.TENANT_DOMAIN, new String[] { tenantDomain });
			paramList.put(AppFactoryConstants.USER_NAME, new String[] { userName });

			deployer.deployLatestSuccessArtifact(paramList);

		} catch (Exception e) {
			String errMsg = "Error when do the deploy the artifact : " + e.getMessage();
			log.error(errMsg, e);
			throw new AppFactoryException(errMsg, e);
		}

	}

	@Override
	public void deployPromotedArtifact(String applicationId, String version, String revision, String artifactType,
	                                   String stage, String tenantDomain, String userName) throws AppFactoryException {

		try {
			Map<String, String[]> paramList = new HashMap<String, String[]>();

			paramList.put(AppFactoryConstants.APPLICATION_ID, new String[] { applicationId });
			paramList.put(AppFactoryConstants.APPLICATION_VERSION, new String[] { version });
			paramList.put(AppFactoryConstants.ARTIFACT_TYPE, new String[] { artifactType });
			paramList.put(AppFactoryConstants.DEPLOY_STAGE, new String[] { stage });
			paramList.put(AppFactoryConstants.TENANT_DOMAIN, new String[] { tenantDomain });
			paramList.put(AppFactoryConstants.USER_NAME, new String[] { userName });

			this.deployer.deployPromotedArtifact(paramList);
		} catch (Exception e) {
			String errMsg = "Error when do the deploy the promoted artifact : " + e.getMessage();
			log.error(errMsg, e);
			throw new AppFactoryException(errMsg, e);
		}
	}

}
