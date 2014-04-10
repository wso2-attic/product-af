package org.wso2.carbon.appfactory.jenkins.artifact.storage;

import hudson.FilePath;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.catalina.util.ParameterMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.jenkins.AppfactoryPluginManager;
import org.wso2.carbon.appfactory.jenkins.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.xml.stream.XMLStreamException;


public class Utils {
    private static final Log log = LogFactory.getLog(Utils.class);
    private static AppfactoryPluginManager.DescriptorImpl descriptor = new AppfactoryPluginManager.DescriptorImpl();

    /**
     * Request should have the job-name that user wants to get the tag names of
     * This will send the list of tag names that user has asked to persist for the given job
     *
     * @param req request
     * @param rsp response
     */
    public static void getTagNamesOfPersistedArtifacts(StaplerRequest req, StaplerResponse rsp) {

        String storagePath = descriptor.getStoragePath();
        String jobName = req.getParameter(Constants.JOB_NAME);

        //artifact storage structure : <storage-path>/<job-name>/<tag-name>/artifact
        File jobDir = new File(storagePath + File.separator + jobName);
        String[] identifiers = jobDir.list();
        if (jobDir.exists() && identifiers.length > 0) {
            try {
                PrintWriter writer = rsp.getWriter();
                for (String identifier : identifiers) {
                    writer.write(identifier + ",");
                }
                writer.flush();
                writer.close();
            } catch (IOException e) {
                log.error("Error while adding identifiers to response", e);
            }
        } else {
            log.info("No artifacts are tagged to persists for job " + jobName);
        }
    }

    /**
     * This is a method to get a parameter map from the stapelerRequest.
     *
     * @param request the staplerReqeust. This is the request that is sent to jenkins
     * @return Map that contains the servlet request parameters. Also the StaplerRequest.getRootPath() value is added in to this map
     */
    public static Map<String, String[]> getParameterMapFromRequest(StaplerRequest request) {
        
        Map<String, String[]> retMap = new HashMap<String, String[]>(); 
        Map<String, String[]> currentMap = request.getParameterMap();

        // Check whether the map in request is not null
        if (currentMap != null) 
        {
            retMap.putAll(currentMap); 
            
            // We will be adding the root path as a parameter in this map. This is read by other classes
            String rootPath = request.getRootPath();
            retMap.put("rootPath", new String[]{rootPath});
        }

        return retMap;
    }
    
    public static String getRepositoryProviderProperty(String stage, String propertyName, String appType) 
    		throws AppFactoryException{
    	String repoProperty = getAppFactoryConfigurationProperty("ApplicationDeployment.DeploymentStage." + stage + 
    				".Deployer.ApplicationType." + appType + ".RepositoryProvider.Property." + propertyName);
		
    	if ( StringUtils.isBlank(repoProperty)){
    	    repoProperty = getAppFactoryConfigurationProperty("ApplicationDeployment.DeploymentStage." + stage + 
					".Deployer.ApplicationType.*.RepositoryProvider.Property." + propertyName);
    	}
    	
		return repoProperty;
	}
    
    public static String getDeployerClassName(String stage, String appType) throws AppFactoryException{
		String className = getAppFactoryConfigurationProperty("ApplicationDeployment.DeploymentStage." + stage + 
				".Deployer.ApplicationType." + appType + ".ClassName");
		
		if (StringUtils.isBlank(className)){
		    className = getAppFactoryConfigurationProperty("ApplicationDeployment.DeploymentStage." + stage + 
		                                       ".Deployer.ApplicationType.*.ClassName");
		}
		
		return className;
	}
    
    /**
     * NOTE : THIS IS A FLAWED METHOD.
     * @param stage
     * @param appType
     * @return
     * @throws AppFactoryException
     */
    public static boolean isAppTypeExistsInDeployers(String stage, String appType) throws AppFactoryException{
    	String deployerApptypeContent = Utils.getAppFactoryConfigurationProperty
				("ApplicationDeployment.DeploymentStage." + stage + ".Deployer.ApplicationType." + appType);
		if(deployerApptypeContent == null){
			return false;
		}
		return true;
    }
    
    public static String getEnvironmentVariable(String variableName){
    	String variableValue = null;
    	try {
			InitialContext iniCtxt = new InitialContext();
			Context env = (Context) iniCtxt.lookup("java:comp/env");
			variableValue = (String) env.lookup(variableName);
		} catch (NamingException e) {
			log.error("Unable to read " + variableName + " from the environment");
		}
    	return variableValue;
    }
    
    public static String getJobConfigElement(String jobName) throws NamingException, JaxenException, XMLStreamException, FileNotFoundException{
    	String jenkinsHome = null;
    	String appType = null;
    	InitialContext iniCtxt = new InitialContext();
		Context env = (Context) iniCtxt.lookup("java:comp/env");
		jenkinsHome = (String) env.lookup(Constants.JENKINS_HOME);    	
    	String jenkinsJobConfigPath = jenkinsHome + "/jobs/" + jobName + "/config.xml";
    	OMElement documentElement = new StAXOMBuilder(jenkinsJobConfigPath).getDocumentElement(); 
		AXIOMXPath xpathExpression = new AXIOMXPath (Constants.JOB_CONFIG_XPATH);
		OMElement node = (OMElement)xpathExpression.selectSingleNode(documentElement);
		appType = node.getText();
		return appType;
	}
    
    public static String getAppFactoryConfigurationProperty(String path) throws AppFactoryException{
		String property = AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(path);
		return property;
	}
    
    public static String[] getAppFactoryConfigurationProperties(String path) throws AppFactoryException{
		String[] properties = AppFactoryUtil.getAppfactoryConfiguration().getProperties(path);
		return properties;
	}

}
