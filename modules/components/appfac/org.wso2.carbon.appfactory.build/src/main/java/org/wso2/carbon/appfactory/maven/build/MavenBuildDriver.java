/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package org.wso2.carbon.appfactory.maven.build;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.maven.shared.invoker.SystemOutHandler;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.BuildDriver;
import org.wso2.carbon.appfactory.core.BuildDriverListener;
import org.wso2.carbon.appfactory.utilities.project.ProjectUtils;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MavenBuildDriver implements BuildDriver {
    private static final Log log = LogFactory.getLog(MavenBuildDriver.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void buildArtifact(String applicationId, String version, String revision,
                              BuildDriverListener listener, String tenantDomain) throws AppFactoryException {
    	log.info("Build started for " + applicationId);
        File workDir = AppFactoryUtil.getApplicationWorkDirectory(applicationId, version, revision);
        String pomFilePath = workDir.getAbsolutePath() + File.separator+ "pom.xml";
        File pomFile = new File(pomFilePath);
        if (!pomFile.exists()) {
        	String errorMessage = "pom.xml does not exist";
        	log.error(errorMessage);
            listener.onBuildFailure(applicationId, version, null, null, errorMessage, tenantDomain);
            try {
				FileUtils.deleteDirectory(workDir);
			} catch (IOException e) {
				log.error("Error deleting work directory " + e.getMessage(), e);
			}
			return;
        }
        executeMavenGoal(workDir.getAbsolutePath(), applicationId, version, listener, tenantDomain);
    }

    // TODO : Run in a thread pool
    private void executeMavenGoal(String applicationPath, final String appId, String version,
                                  BuildDriverListener listener, String tenantDomain)
            throws AppFactoryException {
        InvocationRequest request = new DefaultInvocationRequest();
        String MAVEN_HOME;
        request.setShowErrors(true);

        try {
	        request.setPomFile(new File(applicationPath + File.separator+ "pom.xml"));
	
	        List<String> goals = new ArrayList<String>();
	        goals.add("clean");
	        goals.add("install");
	
	        request.setGoals(goals);
	        Invoker invoker = new DefaultInvoker();
	        InvocationOutputHandler outputHandler = new SystemOutHandler();
	        invoker.setErrorHandler(outputHandler);
	        if ((MAVEN_HOME = System.getenv("M2_HOME")) == null) {
	            if ((MAVEN_HOME = System.getenv("M3_HOME")) == null) {
	                String msg = "valid maven installation is not found with M2_HOME or M3_HOME " +
                                 "environment variable";
	                log.error(msg);
	                throw new AppFactoryException(msg);
	            }
	        }
	        invoker.setMavenHome(new File(MAVEN_HOME));
	        invoker.setOutputHandler(new InvocationOutputHandler() {
	            @Override
	            public void consumeLine(String s) {
	                log.info(appId + ":" + s);
	            }
	        });
        
            InvocationResult result = invoker.execute(request);
			if (result != null && result.getExitCode() == 0) {
				log.info("Build successful");
				File targetDir = new File(applicationPath);
                String applicationType = ProjectUtils.getApplicationType(appId, tenantDomain);
                String[] fileExtension = { applicationType };
				List<File> fileList = (List<File>) FileUtils.listFiles(targetDir, fileExtension, true);
				File builtArtifact = fileList.get(0);
                String buildId = String.valueOf(UUID.randomUUID());
                FileDataSource dataSource = new FileDataSource(builtArtifact.getAbsolutePath());
                DataHandler dataHandler = new DataHandler(dataSource);
                listener.onBuildSuccessful(appId, version, null, buildId, dataHandler,
                                           builtArtifact.getName(), tenantDomain);
			} else {
				final String errorMessage = "No maven Application found at " + applicationPath;
				log.error(errorMessage);
				listener.onBuildFailure(appId, version, null, null, errorMessage, tenantDomain);
			} 
        } catch (MavenInvocationException e) {
        	log.error(e.getMessage(), e);
        	listener.onBuildFailure(appId, version, null, null, e.getMessage(), tenantDomain);
        } finally {
        	try {
				FileUtils.deleteDirectory(new File(applicationPath));
			} catch (IOException e) {
				log.error("Error deleting work directory " + e.getMessage(), e);
			}
        }
    }  
    
    

}
