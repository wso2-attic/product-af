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

package org.wso2.appfactory.dynamicslave;

import hudson.model.TaskListener;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.SlaveComputer;
import jenkins.model.Jenkins;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.context.CarbonContext;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A launcher implementation that used to launch slave in dynamic slave cloud
 */
public class DynamicSlaveComputerLauncher extends ComputerLauncher {
    private static final Logger logger = Logger.getLogger(DynamicSlaveComputerLauncher.class
            .getName());
    public static final String JENKINS_SERVER_ADMIN_USERNAME = "JenkinsServerAdminUsername";
    public static final String JENKINS_SERVER_ADMIN_PASSWORD = "JenkinsServerAdminPassword";
    private final String name;

    public String getName() {
        return name;
    }

    public DynamicSlaveComputerLauncher(String name) {
        this.name = name;
    }

    @Override
    public void launch(SlaveComputer computer, TaskListener listener) throws IOException, InterruptedException {
        DynamicSlaveComputer dynamicSlaveComputer = (DynamicSlaveComputer) computer;
        JenkinsClient client = JenkinsClient.getClientInstance();
        logger.info("...................Launching slave.................. " + name);

        if (!isJobExists(getName(), client)) {
            logger.info("Creating launching task in dynamic cluster manager for " + getName());
            Map<String, String> jobParameters = new HashMap<String, String>();

            try {
                jobParameters.put(JenkinsClient.MASTER_URL,getRootUrl());

                jobParameters.put(JenkinsClient.SLAVE_NAME, dynamicSlaveComputer.getNode().getNodeName());

                jobParameters.put("slaveAdmin", AppFactoryUtil.getAppfactoryConfiguration()
                        .getFirstProperty(JENKINS_SERVER_ADMIN_USERNAME));

            } catch (AppFactoryException e) {
                String msg = "Error while reading username property from appfactory.xml ";
                logger.log(Level.SEVERE, msg, e);
                return;
            } catch (DynamicSlaveException e) {
                String msg = "Error while getting job parameters ";
                logger.log(Level.SEVERE, msg, e);
                return;
            }
            try {
                if (createJob(getName(), JenkinsClient.getJobTemplate(jobParameters), client)) {
                    logger.info("Launching task in dynamic cluster manager for " + getName() + " is created successfully");
                    if (schedulingJob(getName(), client)) {
                        logger.info("Launching task in dynamic cluster manager for " + getName() + " is scheduled successfully");
                        logger.info("Waiting until slave joins.....................");
                        computer.waitUntilOnline();
                        logger.info("Connected to slave");
                    } else {
                        logger.info("Scheduling launching task in dynamic cluster manager for " + getName() + " is failed");
                    }
                } else {
                    logger.info("Creating launching task in dynamic cluster manager for " + getName() + " is failed");
                }
            } catch (DynamicSlaveException e) {
                String msg = "Error while requesting a slave " + getName();
                logger.log(Level.SEVERE, msg, e);
            }
        } else {
            logger.info("Already the computer is launched");
        }
    }

    private String getRootUrl() throws DynamicSlaveException {
        String rootURL=Jenkins.getInstance().getRootUrl();
        if(rootURL==null){
            //rootURL is null because it is not configured in hudson.tasks.Mailer.xml or there is
            // no request
            String hostName=System.getProperty("carbon.local.ip");
            String httpsPort=System.getProperty("mgt.transport.https.port");
            InitialContext iniCtxt;
            try {
                iniCtxt = new InitialContext();
            } catch (NamingException e) {
                String msg="Error while getting initial context ";
               logger.log(Level.SEVERE,msg,e);
                throw new DynamicSlaveException(msg,e);
            }
            Context env = null;
            try {
                env = (Context) iniCtxt.lookup("java:comp/env");
            } catch (NamingException e) {
                String msg="Error while getting java:comp/env context ";
                logger.log(Level.SEVERE,msg,e);
                throw new DynamicSlaveException(msg,e);
            }
            String tenantDomain = null;
            try {
                tenantDomain = (String) env.lookup("TENANT_DOMAIN");
            } catch (NamingException e) {
                String msg="Error while getting TENANT_DOMAIN context ";
                logger.log(Level.SEVERE,msg,e);
                throw new DynamicSlaveException(msg,e);
            }
            rootURL=String.format("https://%s:%s/t/%s/webapps/jenkins/",hostName,httpsPort,
                    tenantDomain);
        }
            return rootURL;
    }

    private boolean schedulingJob(String name, JenkinsClient client) throws DynamicSlaveException {
        Map<String, String> triggerJobParameters = new HashMap<String, String>();
        try {
            triggerJobParameters.put("slavePassword", AppFactoryUtil.getAppfactoryConfiguration()
                    .getFirstProperty(JENKINS_SERVER_ADMIN_PASSWORD));
        } catch (AppFactoryException e) {
            String msg = "Error while reading password from appfactory.xml ";
            logger.log(Level.SEVERE, msg, e);
            throw new DynamicSlaveException(msg, e);
        }
        String response = client.post_xml(String.format("job/%s/buildWithParameters", name), null, triggerJobParameters);
        return response != null;
    }

    private boolean createJob(String name, String jobTemplate, JenkinsClient client) throws DynamicSlaveException {
        logger.info("...........creating job in remote jenkins..............");
        String response;
        Map<String, String> createJobParameters = new HashMap<String, String>();
        createJobParameters.put("name", name);
        response = client.post_xml("createItem", jobTemplate, createJobParameters);
        return response != null;
    }

    private boolean isJobExists(String jobName, JenkinsClient client) {
        Map<String, String> iSExistQuery = new HashMap<String, String>();
        iSExistQuery.put("xpath", "/*/displayName");
        return client.get(String.format("job/%s/api/xml", jobName), iSExistQuery) != null;
    }

    @Override
    public void afterDisconnect(SlaveComputer computer, TaskListener listener) {

    }

    public void terminate() {
        JenkinsClient client = JenkinsClient.getClientInstance();
        if (client.post(String.format("job/%s/doDelete", name)) != null) {
            logger.info("Cleaned up launching task " + name);
        } else {
            logger.info("Cleaning  up launching task " + name + " is failed");
        }

    }
}
