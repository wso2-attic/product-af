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
import org.wso2.carbon.appfactory.jenkins.extentions.AFLocalRepositoryLocator;
import org.wso2.carbon.appfactory.jenkins.util.JenkinsUtility;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Map;

@ExportedBean
public class AppfactoryArtifactStoragePlugin extends Plugin {

    private static final Log log = LogFactory.getLog(AppfactoryArtifactStoragePlugin.class);
    private AFLocalRepositoryLocator.DescriptorImpl descriptor = new AFLocalRepositoryLocator.DescriptorImpl();

    private static final String UNDEPLOY_ARTIFACT_ACTION="/undeployArtifact";
    private static final String DEPLOY_PROMOTED_ARTIFACT_ACTION = "/deployPromotedArtifact";
    private static final String DEPLOY_LATEST_SUCCESS_ARTIFACT_ACTION = "/deployLatestSuccessArtifact";

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
        } else if("/extractMvnRepo".equalsIgnoreCase(action)){
            String tenantDomain = req.getParameter(AppFactoryConstants.TENANT_DOMAIN);
            extractPreConfiguredMavenRepo(tenantDomain);
        }else{
//        First we check what is the class that we need to invoke

            String jobName = req.getParameter(AppFactoryConstants.JOB_NAME);
            String stage = req.getParameter(AppFactoryConstants.DEPLOY_ACTION);
            String applicationId = JenkinsUtility.getApplicationId(jobName);
            String version = JenkinsUtility.getVersion(jobName);

            String runtime = null;
            String className = null;

            try {
                Map<String, String[]> map = Utils.getParameterMapFromRequest(req);
                map.put(AppFactoryConstants.APPLICATION_ID, new String[] { applicationId });
                map.put(AppFactoryConstants.APPLICATION_VERSION, new String[] { version });
                runtime = DeployerUtil.getParameter(map, AppFactoryConstants.RUNTIME_NAME_FOR_APPTYPE);
                ClassLoader loader = getClass().getClassLoader();

                if (DEPLOY_LATEST_SUCCESS_ARTIFACT_ACTION.equals(action)) {
                    className = DeployerUtil.getParameter(map, AppFactoryConstants.DEPLOYER_CLASSNAME);
                    Deployer deployer = getDeployer(className, loader);
                    deployer.deployLatestSuccessArtifact(map);
                } else if(DEPLOY_PROMOTED_ARTIFACT_ACTION.equals(action)) {
                    className = DeployerUtil.getParameter(map, AppFactoryConstants.DEPLOYER_CLASSNAME);
                    Deployer deployer = getDeployer(className, loader);
                    deployer.deployPromotedArtifact(map);
                } else {
                    rsp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    throw new ServletException("Invalid action");
                }
            } catch (AppFactoryException e) {
                String msg = "Action " + action + "failed for the job name : " + jobName + ", application id : "
                             + applicationId + ", version : " + version + " in  stage : " + stage + " and runtime : "
                             + runtime;
                log.error(msg,e);
                throw new ServletException(msg, e);
            } catch (ClassNotFoundException e) {
                String msg = "Action " + action + "failed for the job name : " + jobName + ", application id : "
                             + applicationId + ", version : " + version + " in  stage : " + stage + " and runtime : "
                             + runtime;
                log.error(msg,e);
                throw new ServletException(msg, e);
            } catch (InstantiationException e) {
                String msg = "Action " + action + "failed for the job name : " + jobName + ", application id : "
                             + applicationId + ", version : " + version + " in  stage : " + stage + " and runtime : "
                             + runtime+".\n Error occurred while getting an instance of the provided class: "+className;
                log.error(msg,e);
                throw new ServletException(msg, e);
            } catch (IllegalAccessException e) {
                String msg = "Action " + action + "failed for the job name : " + jobName + ", application id : "
                             + applicationId + ", version : " + version + " in  stage : " + stage + " and runtime : "
                             + runtime;
                log.error(msg,e);
                throw new ServletException(msg, e);
            } catch (Exception e) {
                String msg = "Action " + action + "failed for the job name : " + jobName + ", application id : "
                             + applicationId + ", version : " + version + " in  stage : " + stage + " and runtime : "
                             + runtime;
                log.error(msg,e);
                throw new ServletException(msg, e);
            }

        }
    }

    /**
     * Get undeployer based on the parameters passed.
     *
     * @param className Undeployer class name. Should be an implementation of {@link org.wso2.carbon.appfactory.core.Undeployer}
     * @param loader    class loader
     * @return Runtime undeployer object.
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ServletException
     */
    private Undeployer getUndeployer(String className, ClassLoader loader)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException, ServletException {
        Object undeployerObj = getClassInstance(loader, className);
        if (undeployerObj instanceof Undeployer) {
            return (Undeployer) undeployerObj;
        } else {
            String msg = "Undeployer class name should be a implementation of " + Undeployer.class
                         + ", but we found " + className;
            throw new ServletException(msg);
        }
    }

    /**
     * Get deployer based on the parameters passed.
     *
     * @param className Deployer class name. Should be an implementation of {@link org.wso2.carbon.appfactory.core.Deployer}
     * @param loader    class loader
     * @return Runtime deployer object based on the parameter map
     * @throws ServletException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private Deployer getDeployer(String className, ClassLoader loader)
            throws ServletException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        Object deployerObject = getClassInstance(loader, className);
        if (deployerObject instanceof Deployer) {
            return (Deployer) deployerObject;
        } else {
            String msg = "Deployer class name should be a implementation of " + Deployer.class
                         + ", but we found " + className;
            throw new ServletException(msg);
        }
    }

    /**
     * Loads the {@code className} in the runtime.
     *
     * @param loader    class loader
     * @param className class name to be loaded
     * @return Returns a new instance of the class represented by this {@code className} object
     * @throws ServletException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private Object getClassInstance(ClassLoader loader, String className)
            throws ServletException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (StringUtils.isEmpty(className)) {
            throw new ServletException("Name of the class to be loaded, is not passed with parameters for " +
                                       "deployment/undeployment.");
        }
        Class<?> customCodeClass = Class.forName(className, true, loader);
        return customCodeClass.newInstance();
    }

    /**
     * Extract the zip file which contains a pre-configured maven repository
     *
     * @param tenantDomain tenantDomain
     * @throws IOException an error
     */
    private void extractPreConfiguredMavenRepo(String tenantDomain) throws IOException {
        File repoArchive =
                new File(descriptor.getPreConfiguredMvnRepoArchive());
        if (repoArchive.canRead()) {
            Utils.unzip(repoArchive.getAbsolutePath(), descriptor.getTenantRepositoryDirPattern(tenantDomain));
        } else {
            log.warn("unable to find pre-configured maven repository achieve at : " +
                     repoArchive.getAbsolutePath());
        }

    }

}
