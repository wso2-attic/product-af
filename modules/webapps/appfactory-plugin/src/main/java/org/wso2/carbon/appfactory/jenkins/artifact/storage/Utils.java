/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *    WSO2 Inc. licenses this file to you under the Apache License,
 *    Version 2.0 (the "License"); you may not use this file except
 *    in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package org.wso2.carbon.appfactory.jenkins.artifact.storage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.jenkins.AppfactoryPluginManager;
import org.wso2.carbon.appfactory.jenkins.Constants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;


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

        String jobName = req.getParameter(Constants.JOB_NAME);
        String tenantUserName = req.getParameter(AppFactoryConstants.TENANT_USER_NAME);
        String tenantDomain = MultitenantUtils.getTenantDomain(tenantUserName);
        String storagePath = descriptor.getStoragePath(tenantDomain);
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
                log.error("Error while adding identifiers to response for job: " + jobName +" tenant: "+tenantDomain, e);
            }
        } else {
            log.info("No artifacts are tagged to persists for job: " + jobName +" tenant: "+tenantDomain);
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

}
