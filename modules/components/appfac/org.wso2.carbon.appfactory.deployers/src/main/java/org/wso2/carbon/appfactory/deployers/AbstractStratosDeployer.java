package org.wso2.carbon.appfactory.deployers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.plexus.util.FileUtils;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.deployers.clients.AppfactoryRepositoryClient;
import org.wso2.carbon.appfactory.deployers.util.DeployerUtil;


public abstract class AbstractStratosDeployer extends AbstractDeployer {

	private static final Log log = LogFactory.getLog(AbstractStratosDeployer.class);
	
	protected void deploy(String artifactType, File[] artifactsToDeploy, Map<String, String[]> parameters, Boolean notify)
            throws AppFactoryException {
        

        String applicatonId = DeployerUtil.getParameter(parameters, AppFactoryConstants.APPLICATION_ID);
        String version = DeployerUtil.getParameter(parameters, AppFactoryConstants.APPLICATION_VERSION);
        String deployStage = DeployerUtil.getParameter(parameters, AppFactoryConstants.DEPLOY_STAGE);
        String tenantDomain = getTenantDomain();
        
        String condition = applicatonId+"-"+version+"-"+deployStage+"-"+tenantDomain;
        synchronized (condition.intern()) {

	        if (AppFactoryConstants.APPLICATION_TYPE_ESB.equals(artifactType)) {
	        	ExtendedUploadItem[] uploadItems = new ExtendedUploadItem[artifactsToDeploy.length];
	        	for (int i = 0; i < uploadItems.length; i++) {
	        		File artifact = artifactsToDeploy[i];
	        		DataHandler dataHandler = new DataHandler(new FileDataSource(artifact));
	
	        		ExtendedUploadItem dataItem = new ExtendedUploadItem(dataHandler,artifact);
	        		dataItem.setFileName(artifact.getName());
	        		uploadItems[i] = dataItem;
	        	} 
	        	uploadESBApp(uploadItems, parameters); 
	       } else {
	        	File artifactToDeploy = artifactsToDeploy[0];
	            String fileName = artifactToDeploy.getName();
	            DataHandler dataHandler = new DataHandler(new FileDataSource(artifactToDeploy));
	            addToGitRepo(fileName, dataHandler, parameters,artifactType, getServerDeploymentPaths(artifactType), null);
	        }        
	        
	        if (notify){
            	postDeploymentNoifier("", applicatonId, version, artifactType, deployStage, tenantDomain);
            }
	        
        }
	}

	public void uploadESBApp(ExtendedUploadItem[] uploadData, Map metadata)
            throws AppFactoryException {
		log.info("Extended Uploaded Data length - " + uploadData.length);
		for (ExtendedUploadItem uploadItem : uploadData) {
			log.info("Upload item " + uploadItem.getFileName());
			String filePath = uploadItem.getFile().getAbsolutePath().toLowerCase();
			log.info("Upload file item path is " + filePath);

			if (filePath.contains(AppFactoryConstants.ESB_ARTIFACT_PREFIX)) {
				String fileDir = uploadItem.getFile().getParent();
				String fileRepoLocation = fileDir.split(AppFactoryConstants.ESB_ARTIFACT_PREFIX)[1];
				log.info("repo file location is - " + fileRepoLocation);
				addToGitRepo(uploadItem.getFileName(), uploadItem.getDataHandler(), metadata, AppFactoryConstants.APPLICATION_TYPE_ESB,
				             getServerDeploymentPaths(AppFactoryConstants.APPLICATION_TYPE_ESB), fileRepoLocation);
			}
		}
	}

    private void addToGitRepo(String fileName, DataHandler dataHandler, Map metadata, String appTypeName, String serverDeploymentPath, String relativePathFragment)
            throws AppFactoryException {
        
        
               
        String applicationId = DeployerUtil.getParameterValue(metadata, AppFactoryConstants.APPLICATION_ID);

        int tenantId = getTenantID();

		String gitRepoUrl = generateRepoUrl(applicationId, metadata, tenantId, appTypeName);
		String stageName = DeployerUtil.getParameterValue(metadata, AppFactoryConstants.DEPLOY_STAGE);

		String applicationAdmin = getAdminUserName(stageName, appTypeName);
		String defaultPassword = getAdminPassword(stageName, appTypeName);

		
        //Create the temporary directory first. without this we can't proceed
        File tempApptypeDirectory = new File(tempPath + File.separator + stageName + File.separator + appTypeName);  //<tempdir>/war , <tempdir>/jaxrs, 
        if (!tempApptypeDirectory.exists()) {
            if (!tempApptypeDirectory.mkdirs()) {
                String msg = "Unable to create temp directory : " + tempApptypeDirectory.getAbsolutePath();
                handleException(msg);
            }
        }

		
		String appTypeDirectory = tempApptypeDirectory.getAbsolutePath() + File.separator + serverDeploymentPath; // tempdir/<war>webapps ,tempdir/jaggery/jaggeryapps, //tempdir/esb/synapse-config
		String deployableFileName =  null ;    

		if ( StringUtils.isBlank(relativePathFragment)){
		    deployableFileName = appTypeDirectory + File.separator + fileName; // tempdir/webapps/myapp.war , tempdir/jappgeryapps/myapp.war
		
		}else{
		    
		    deployableFileName = appTypeDirectory + File.separator + relativePathFragment + File.separator + fileName; //<tempdir>/synapse-config/proxy-services/MyProxy.xml
		
		}
		
		

		AppfactoryRepositoryClient repositoryClient = new AppfactoryRepositoryClient("git");
		try {
			repositoryClient.init(applicationAdmin, defaultPassword);

			File appTypeFile = new File(appTypeDirectory);
			
			if (tempApptypeDirectory.isDirectory() && tempApptypeDirectory.list().length > 0) {
				// This means that there are files in the given location. We
				// check whether they are previous checkouts
				if (!repositoryClient.update(gitRepoUrl, appTypeFile)) {
					// This means that the update has failed. So we clean this
					// directory.
					File[] filesToDelete = tempApptypeDirectory.listFiles();

					if (filesToDelete != null) {
						for (File fileToDelete : filesToDelete) {
							try {
								if (fileToDelete.isDirectory()) {
									FileUtils.deleteDirectory(fileToDelete);
								} else {
									FileUtils.forceDelete(fileToDelete);
								}
							} catch (IOException e) {
								// We ignore any exception here.
							}
						}
					}

					// Now we take the checkout
					repositoryClient.checkOut(gitRepoUrl, tempApptypeDirectory);
				}
			} else {
			    // this means no files exists, so we straight away check out the repo
				repositoryClient.checkOut(gitRepoUrl, tempApptypeDirectory); 
			}
			
			
			File deployableFile = new File(deployableFileName);
			if (!deployableFile.getParentFile().exists()) {
				log.debug("deployableFile.getParentFile() doesn't exists: " + deployableFile.getParentFile());
				if (!deployableFile.getParentFile().mkdirs()) {
					String msg = "Unable to create parent path of the deployable file " + deployableFile.getAbsolutePath(); 
					// unable to create <tempdir>/war/webapps, <tempdir>/jaggery/jaggeryapps
					// or <tempdir>/esb/synapse-config/default/proxy-services
					handleException(msg);
				}
			}

			
			
			
			
			
			// If there is a file in repo, we delete it first
			if (deployableFile.exists()) {
				repositoryClient.remove(gitRepoUrl, deployableFile,
				                        "Removing the old file to add the new one");
				// Checking and removing the local file
				if (deployableFile.exists()) {
					deployableFile.delete();
				}
				// repositoryClient.checkIn(gitRepoUrl, applicationTempLocation,
				// "Removing the old file to add the new one");

				try {
					deployableFile = new File(deployableFileName);
					// check weather directory exists.
					if (!deployableFile.getParentFile().isDirectory()) {
						log.debug("parent directory : " +
						          deployableFile.getParentFile().getAbsolutePath() +
						          " doesn't exits creating again");
						if (!deployableFile.getParentFile().mkdirs()) {
							throw new IOException("Unable to re-create " +
							                      deployableFile.getParentFile().getAbsolutePath());
						}

					}
					if (!deployableFile.createNewFile()) {
						throw new IOException("unable re-create the target file : " +
						                      deployableFile.getAbsolutePath());
					}

					if (deployableFile.canWrite()) {
						log.debug("Successfully re-created a writable file : " + deployableFileName);
					} else {
						String errorMsg = "re-created file is not writable: " + deployableFileName;
						log.error(errorMsg);
						throw new IOException(errorMsg);
					}

				} catch (IOException e) {
					log.error("Unable to create the new file after deleting the old: " +
					          deployableFile.getAbsolutePath(), e);
					throw new AppFactoryException(e);
				}
			}

			copyFilesToGit(dataHandler, deployableFile);

			if (repositoryClient.add(gitRepoUrl, new File(deployableFileName))) {
				if (!repositoryClient.checkIn(gitRepoUrl, tempApptypeDirectory,
				                              "Adding the artifact to the repo")) {
					String msg = "Unable to commit files to git";
					handleException(msg);
				}
			} else {
				String msg = "Unable to add files to git";
				handleException(msg);
			}
		} catch (AppFactoryException e) {
			String msg = "Unable to copy files to git location";
			handleException(msg, e);
		}
	}

    private void copyFilesToGit(DataHandler datahandler, File destinationFile)
            throws AppFactoryException {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(destinationFile);

            datahandler.writeTo(fileOutputStream);
        } catch (FileNotFoundException e) {
            log.error(e);
            throw new AppFactoryException(e);
        } catch (IOException e) {
            log.error(e);
            throw new AppFactoryException(e);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    protected String generateRepoUrl(String applicationId, Map metadata, int tenantId, String appType) throws AppFactoryException {
    	String stage = DeployerUtil.getParameterValue(metadata, AppFactoryConstants.DEPLOY_STAGE);
    	String baseUrl = getBaseRepoUrl(stage, appType);
        String template = getBaseRepoUrlPattern(stage, appType);

        String gitRepoUrl = baseUrl + "git/" + template + File.separator + tenantId + ".git";
        gitRepoUrl = gitRepoUrl.replace("{@stage}", stage);
        log.debug("------------------- Git URL : " +  gitRepoUrl);
        return gitRepoUrl;
    }

    @Override
    public void unDeployArtifact(Map<String, String[]> requestParameters) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

	public String getPromotedDestinationPathForApplication(String filepath, String artifactType) {

		if (AppFactoryConstants.APPLICATION_TYPE_ESB.equals(artifactType)) {
			if (filepath.contains(AppFactoryConstants.ESB_ARTIFACT_PREFIX)) {
				String esbRelativeFilePath =
				                             filepath.split(AppFactoryConstants.ESB_ARTIFACT_PREFIX)[1];
				return AppFactoryConstants.ESB_ARTIFACT_PREFIX + File.separator + esbRelativeFilePath;
			}
		}
		return "";
	}
	
	protected abstract String getBaseRepoUrl(String stage, String appType) throws AppFactoryException;

    protected abstract String getBaseRepoUrlPattern(String stage, String appType) throws AppFactoryException;

    protected abstract String getAdminUserName(String stage, String appType) throws AppFactoryException;

    protected abstract String getAdminPassword(String stage, String appType) throws AppFactoryException;   
    
    protected abstract String getServerDeploymentPaths(String appType) throws AppFactoryException; 
    
    
}

