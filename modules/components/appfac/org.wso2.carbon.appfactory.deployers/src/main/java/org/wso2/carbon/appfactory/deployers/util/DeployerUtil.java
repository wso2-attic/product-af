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

package org.wso2.carbon.appfactory.deployers.util;

import java.io.File;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;

public class DeployerUtil {
	
	public static String getParameter(Map<String,String[]> parameters, String parameterName){
        if(!parameters.containsKey(parameterName)){
            return null;
        }else{
            String[] values = parameters.get(parameterName);
            if(values.length == 0){
                return null;
            }else{
                return values[0];
            }
        }
    }
	
	public static String[] getParameterValues(Map metadata, String key) {
        if (metadata.get(key) == null) {
            return null;
        }
        if (metadata.get(key) instanceof String[]) {
            return (String[]) metadata.get(key);
        } else if (metadata.get(key) instanceof String) {
            return new String[]{metadata.get(key).toString()};
        }

        return null;
    }
	
	public static String getParameterValue(Map metadata, String key) {
        if (metadata.get(key) == null) {
            return null;
        }
        if (metadata.get(key) instanceof String[]) {
            String[] values = (String[]) metadata.get(key);
            if (values.length > 0) {
                return values[0];
            }
            return null;
        } else if (metadata.get(key) instanceof String) {
            return metadata.get(key).toString();
        }

        return null;
    } 
	
	public static String generateTenantJenkinsUrl(String jobName, String tenantDomain, String jenkinsUrl) {
		//      return jenkinsUrl + File.separator + Constants.TENANT_SPACE + File.separator + tenantDomain +
		//              Constants.JENKINS_WEBAPPS + "/job/" + jobName + "/buildWithParameters";
		return jenkinsUrl + File.separator + "t" + File.separator + tenantDomain +
				"/webapps/jenkins" + "/job/" + jobName + "/buildWithParameters";
	}
	
	public static String getJenkinsHome() throws NamingException {
        InitialContext ic = new InitialContext();

        // thats everything from the context.xml and from the global configuration
        Context xmlContext = (Context) ic.lookup("java:comp/env");

        return (String) xmlContext.lookup("JENKINS_HOME");
    }
	
	
	
	public static String getAppFactoryConfigurationProperty(String path) throws AppFactoryException{
		String property = AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(path);
		return property;
	}
    
    public static String[] getAppFactoryConfigurationProperties(String path) throws AppFactoryException{
		String[] properties = AppFactoryUtil.getAppfactoryConfiguration().getProperties(path);
		return properties;
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
    
    
    public static String getAdminPasswordForRepository(String repoType) throws AppFactoryException {
		return getAppFactoryConfigurationProperty("RepositoryProviderConfig."+repoType+".Property.AdminPassword");
	}

    public static String getAdminUserNameForRepository(String repoType) throws AppFactoryException {
		return getAppFactoryConfigurationProperty("RepositoryProviderConfig."+repoType+".Property.AdminUserName");
	}

}
