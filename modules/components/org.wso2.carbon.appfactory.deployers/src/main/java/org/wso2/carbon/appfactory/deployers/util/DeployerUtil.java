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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.util.Map;

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
		return jenkinsUrl + File.separator + "t" + File.separator + tenantDomain +
				"/webapps/jenkins" + "/job/" + jobName + "/buildWithParameters";
	}

	public static String getJenkinsHome() throws NamingException {
        InitialContext ic = new InitialContext();
        // thats everything from the context.xml and from the global configuration
        Context xmlContext = (Context) ic.lookup("java:comp/env");
        return (String) xmlContext.lookup("JENKINS_HOME");
    }

}
