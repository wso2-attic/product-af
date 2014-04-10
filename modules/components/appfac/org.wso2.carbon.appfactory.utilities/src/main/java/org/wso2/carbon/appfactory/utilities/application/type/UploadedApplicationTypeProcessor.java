package org.wso2.carbon.appfactory.utilities.application.type;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.utilities.project.ProjectUtils;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * Application processor for uploaded application type eg:- war files
 * 
 *
 */
public class UploadedApplicationTypeProcessor extends AbstractApplicationTypeProcessor {

	private static Log log = LogFactory.getLog(UploadedApplicationTypeProcessor.class);

	@Override
	public void doVersion(String applicationID, String targetVersion, String currentVersion,
	                      String workingDirectory) throws AppFactoryException {

		try {
			String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
			String applicationExtenstion =
					ProjectUtils.getApplicationExtenstion(applicationID,
					                                      tenantDomain);
			String uploadedFileName =
					applicationID + "-" + targetVersion + "." +
							applicationExtenstion;
	
	        copyUploadedAppToRepositoryLocation(uploadedFileName, workingDirectory);
	        
	        if (log.isDebugEnabled()) {
				log.debug("Version creation hanlded for Uploaded application type with application key -" + applicationID);
			}
	        
		} catch (IOException e) {
			log.error(e);
			throw new AppFactoryException("Error when creating version of uploaded application", e);
		}

	}

	@Override
	public void generateApplicationSkeleton(String applicationID, String workingDirectory)
			throws AppFactoryException {

		try {
			String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
			String applicationExtenstion =
					ProjectUtils.getApplicationExtenstion(applicationID,
					                                      tenantDomain);
			String uploadedFileName = applicationID + "." + applicationExtenstion;
			copyUploadedAppToRepositoryLocation(uploadedFileName , workingDirectory);
			
			if (log.isDebugEnabled()) {
				log.debug("Application skeleton creation hanlded for Uploaded application type with application key -" + applicationID);
			}
		} catch (IOException e) {
			log.error(e);
			throw new AppFactoryException("Error when generating uploaded application skeleton", e);
		}
	}

	private void copyUploadedAppToRepositoryLocation(String uploadedFileName, String workingDirectory) throws IOException{
		
		File sourceFile =
				new File(getUploadedApplicationTmpPath() + File.separator +
				         uploadedFileName);
		File desFile = new File(workingDirectory + File.separator + uploadedFileName);
		FileUtils.copyFile(sourceFile, desFile);

		if (log.isDebugEnabled()) {
			log.debug("Uploaded application file "+ sourceFile.getName() + " successfully copied to location - " +
			          desFile.getAbsolutePath());
		}
		sourceFile.delete();
		
	}
	
	private String getUploadedApplicationTmpPath() {
		return CarbonUtils.getCarbonRepository() + File.separator + "jaggeryapps/appmgt/" +
				AppFactoryConstants.UPLOADED_APPLICATION_TMP_FOLDER_NAME;
	}

	@Override
    public List<File> getPreVersionDeleteableFiles(String applicationID, String targetVersion,
                                                   String currentVersion, String workingDir) throws AppFactoryException {
	    
		String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
		String applicationExtenstion;
        applicationExtenstion = ProjectUtils.getApplicationExtenstion(applicationID,
	        		                                      tenantDomain);
        List<File> deletableFiles = new ArrayList<File>();
	    deletableFiles.add(new File(CarbonUtils.getTmpDir() + File.separator + applicationID + File.separator + applicationID +"."+ applicationExtenstion));
	    return deletableFiles;
    }
}
