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

import hudson.Plugin;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.ExportedBean;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.Deployer;
import org.wso2.carbon.appfactory.core.Undeployer;
import org.wso2.carbon.appfactory.deployers.util.DeployerUtil;
import org.wso2.carbon.appfactory.jenkins.util.JenkinsUtility;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@ExportedBean
public class AppfactoryArtifactStoragePlugin extends Plugin {

    private static final Log log = LogFactory.getLog(AppfactoryArtifactStoragePlugin.class);
    private static final String UNDEPLOY_ARTIFACT_ACTION="/undeployArtifact";
    /**
     * This method serves the requests coming under <jenkins-url>/plugin/<plugin-name>
     * @param req request
     * @param rsp response
     * @throws IOException
     * @throws ServletException
     */
    public void doDynamic(StaplerRequest req, StaplerResponse rsp)
            throws IOException, ServletException {

//        First we check the action.
//        The action get tag names does not depend on the deployer.
        String action = req.getRestOfPath();
        if ("/getTagNamesOfPersistedArtifacts".equals(action)) {

            Utils.getTagNamesOfPersistedArtifacts(req, rsp);
        }else{
//        First we check what is the class that we need to invoke

            String jobName = req.getParameter(AppFactoryConstants.JOB_NAME);
            String stage = req.getParameter(AppFactoryConstants.DEPLOY_ACTION);
            String applicationId = JenkinsUtility.getApplicationId(jobName);
            String version = JenkinsUtility.getVersion(jobName);

            Deployer deployer;
            String runtime = null;
            
            try {
                Map<String, String[]> map = Utils.getParameterMapFromRequest(req);

                String className = DeployerUtil.getParameter(map, AppFactoryConstants.RUNTIME_DEPLOYER_CLASSNAME);
                runtime = DeployerUtil.getParameter(map, AppFactoryConstants.RUNTIME_NAME_FOR_APPTYPE);
                map.put(AppFactoryConstants.APPLICATION_ID, new String[] { applicationId });
                map.put(AppFactoryConstants.APPLICATION_VERSION, new String[] { version });

                if (StringUtils.isEmpty(className)) {
                    String msg = "Deployer Class name is not passed with parameters.";
                    log.error(msg);
                    throw new ServletException(msg);
                }

                ClassLoader loader = getClass().getClassLoader();
                Class<?> customCodeClass = Class.forName(className, true, loader);
                deployer = (Deployer) customCodeClass.newInstance();
                if ("/deployLatestSuccessArtifact".equals(action)) {
                    deployer.deployLatestSuccessArtifact(map);
                } else if("/deployPromotedArtifact".equals(action)) {
                    deployer.deployPromotedArtifact(map);
                } else if (UNDEPLOY_ARTIFACT_ACTION.equals(action)) {
                    className = DeployerUtil.getParameter(map, AppFactoryConstants.RUNTIME_UNDEPLOYER_CLASSNAME);
                    customCodeClass = Class.forName(className, true, loader);
                    Object undeployerObj = customCodeClass.newInstance();
                    if (undeployerObj instanceof Undeployer) {
                        Undeployer undeployer = (Undeployer) undeployerObj;
                        undeployer.undeployArtifact(map);
                    } else {
                        String msg = "Expected class name for action " + action + " of runtime : " + runtime
                                     + " in stage : " + stage + " should be a implementation of " + Undeployer.class
                                     + ", but we found " + className;
                        throw new ServletException(msg);
                    }
                } else {
                    rsp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    throw new ServletException("Invalid action");
                }
            } catch (AppFactoryException e) {
                String msg = "Action " + action + "failed for the job name : " + jobName + ", application id : "
                             + applicationId + ", version : " + version + " in  stage : " + stage + " and runtime : "
                             + runtime;
                throw new ServletException(msg, e);
            } catch (ClassNotFoundException e) {
                String msg = "Action " + action + "failed for the job name : " + jobName + ", application id : "
                             + applicationId + ", version : " + version + " in  stage : " + stage + " and runtime : "
                             + runtime;
                throw new ServletException(msg, e);
            } catch (InstantiationException e) {
                String msg = "Action " + action + "failed for the job name : " + jobName + ", application id : "
                             + applicationId + ", version : " + version + " in  stage : " + stage + " and runtime : "
                             + runtime;
                throw new ServletException(msg, e);
            } catch (IllegalAccessException e) {
                String msg = "Action " + action + "failed for the job name : " + jobName + ", application id : "
                             + applicationId + ", version : " + version + " in  stage : " + stage + " and runtime : "
                             + runtime;
                throw new ServletException(msg, e);
            } catch (Exception e) {
                String msg = "Action " + action + "failed for the job name : " + jobName + ", application id : "
                             + applicationId + ", version : " + version + " in  stage : " + stage + " and runtime : "
                             + runtime;
                throw new ServletException(msg, e);
            }

        }
    }
}
