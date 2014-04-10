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

import hudson.Extension;
import hudson.model.*;
import hudson.slaves.Cloud;
import hudson.slaves.NodeProvisioner;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the entity(Cluster manager jenkins) that dynamically provision a computer
 */
public class DynamicSlaveCloud extends Cloud {
    private static final Logger logger = Logger.getLogger(DynamicSlaveCloud.class.getName());
    private static final String DISPLAY_NAME = "Dynamic Slave";
    private String masterURL;
    private String masterUsername;
    private String masterPassword;
    private String label;
    private String rootFS;
    private int numberOfExecutors;
    private int idleTerminationInMinute;
    private int allowedMaxSlaveCount;
    private int maxAttachedTimeInMinutes;

    public int getMaxAttachedTimeInMinutes() {
        return maxAttachedTimeInMinutes;
    }

    public void setMaxAttachedTimeInMinutes(int maxAttachedTimeInMinutes) {
        this.maxAttachedTimeInMinutes = maxAttachedTimeInMinutes;
    }

    public int getAllowedMaxSlaveCount() {
        return allowedMaxSlaveCount;
    }

    public void setAllowedMaxSlaveCount(int allowedMaxSlaveCount) {
        this.allowedMaxSlaveCount = allowedMaxSlaveCount;
    }

    public int getNumberOfExecutors() {
        return numberOfExecutors;
    }

    public void setNumberOfExecutors(int numberOfExecutors) {
        this.numberOfExecutors = numberOfExecutors;
    }

    public int getIdleTerminationInMinute() {
        return idleTerminationInMinute;
    }

    public void setIdleTerminationInMinute(int idleTerminationInMinute) {
        this.idleTerminationInMinute = idleTerminationInMinute;
    }

    @DataBoundConstructor
    public DynamicSlaveCloud(String masterURL, String masterUsername, String masterPassword, String label, String rootFS, int numberOfExecutors, int idleTerminationInMinute, int allowedMaxSlaveCount) {
        super(DynamicSlaveCloud.DISPLAY_NAME);
        this.masterURL = masterURL;
        this.masterUsername = masterUsername;
        this.masterPassword = masterPassword;
        this.label = label;
        this.rootFS = rootFS;
        this.numberOfExecutors = numberOfExecutors;
        this.idleTerminationInMinute = idleTerminationInMinute;
        this.allowedMaxSlaveCount = allowedMaxSlaveCount;
        logger.info("Registering cluster manager(jenkins): " + masterURL);
    }

    @Override
    public Collection<NodeProvisioner.PlannedNode> provision(Label label, int excessWorkload) {
        int launchedNodes = Hudson.getInstance().getNodes().size();
        logger.info("Already have " + launchedNodes + " nodes.Attempting to launching one more");
        List<NodeProvisioner.PlannedNode> list = new ArrayList<NodeProvisioner.PlannedNode>();
        try {
            if (launchedNodes < getAllowedMaxSlaveCount()) {
                while (excessWorkload > 0) {

                    final int numExecutors = Math.min(excessWorkload, 2);
                    excessWorkload -= numExecutors;
                    logger.info("Provisioning Jenkins Slave on dynamic slave cloud with " + numExecutors +
                            " executors. Remaining excess workload: " + excessWorkload + " executors)");
                    list.add(new NodeProvisioner.PlannedNode(this.getDisplayName(), Computer.threadPoolForRemoting
                            .submit(new Callable<Node>() {
                                public Node call() throws Exception {
                                    DynamicSlave s = doProvision(numExecutors);
                                    Hudson.getInstance().addNode(s);
                                    return s;
                                }
                            }), numExecutors));

                }
            } else {
                logger.log(Level.SEVERE, "Slave computer max limit is reached [" + launchedNodes + "/" + getAllowedMaxSlaveCount() + "]");
            }

        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to create instances on dynamic slave cloud", e);
            return Collections.emptyList();
        }

        return list;
    }

    private DynamicSlave doProvision(int numExecutors) throws DynamicSlaveException {
        String name = "dynamic-slave-" + UUID.randomUUID().toString();
        try {
            return new DynamicSlave(name, "This is a slave automatically launched by dynamic slave cloud", getRootFS(), String.valueOf(numExecutors), Node.Mode.NORMAL,
                    getLabel(), getIdleTerminationInMinute(), getMaxAttachedTimeInMinutes());
        } catch (Descriptor.FormException e) {
            String msg = "Error while reading values from configs";
            logger.log(Level.WARNING, msg, e);
            throw new DynamicSlaveException(msg, e);
        } catch (IOException e) {
            String msg = "Error while reading node properties";
            logger.log(Level.WARNING, msg, e);
            throw new DynamicSlaveException(msg, e);
        }
    }

    @Override
    public boolean canProvision(Label label) {
        if (label == null) {
            logger.info("Request is for the job that does not have label.Dispatching the request " +
                    "to dynamic slave cloud");
            return true;
        }
        return getLabel().equals(label.getName());
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getRootFS() {
        return rootFS;
    }

    public void setRootFS(String rootFS) {
        this.rootFS = rootFS;
    }

    public String getMasterURL() {
        return masterURL;
    }

    public void setMasterURL(String masterURL) {
        this.masterURL = masterURL;
    }

    public String getMasterUsername() {
        return masterUsername;
    }

    public void setMasterUsername(String masterUsername) {
        this.masterUsername = masterUsername;
    }

    public String getMasterPassword() {
        return masterPassword;
    }

    public void setMasterPassword(String masterPassword) {
        this.masterPassword = masterPassword;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    public static DynamicSlaveCloud get() {
        return Hudson.getInstance().clouds.get(DynamicSlaveCloud.class);
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<Cloud> {
        private String masterURL;
        private String masterUsername;
        private String masterPassword;
        private String label;
        private String rootFS;
        private int numberOfExecutors;
        private int idleTerminationInMinute;
        private int allowedMaxSlaveCount;
        private int maxAttachedTimeInMinutes;

        public String getMasterURL() {
            return masterURL;
        }

        public void setMasterURL(String masterURL) {
            this.masterURL = masterURL;
        }

        public String getMasterUsername() {
            return masterUsername;
        }

        public void setMasterUsername(String masterUsername) {
            this.masterUsername = masterUsername;
        }

        public String getMasterPassword() {
            return masterPassword;
        }

        public void setMasterPassword(String masterPassword) {
            this.masterPassword = masterPassword;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getRootFS() {
            return rootFS;
        }

        public void setRootFS(String rootFS) {
            this.rootFS = rootFS;
        }

        public int getNumberOfExecutors() {
            return numberOfExecutors;
        }

        public void setNumberOfExecutors(int numberOfExecutors) {
            this.numberOfExecutors = numberOfExecutors;
        }

        public int getIdleTerminationInMinute() {
            return idleTerminationInMinute;
        }

        public void setIdleTerminationInMinute(int idleTerminationInMinute) {
            this.idleTerminationInMinute = idleTerminationInMinute;
        }

        public int getAllowedMaxSlaveCount() {
            return allowedMaxSlaveCount;
        }

        public void setAllowedMaxSlaveCount(int allowedMaxSlaveCount) {
            this.allowedMaxSlaveCount = allowedMaxSlaveCount;
        }

        public int getMaxAttachedTimeInMinutes() {
            return maxAttachedTimeInMinutes;
        }

        public void setMaxAttachedTimeInMinutes(int maxAttachedTimeInMinutes) {
            this.maxAttachedTimeInMinutes = maxAttachedTimeInMinutes;
        }

        @Override
        public String getDisplayName() {
            return DynamicSlaveCloud.DISPLAY_NAME;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            masterURL = json.getString("masterURL");
            masterUsername = json.getString("masterUsername");
            masterPassword = json.getString("masterPassword");
            label = json.getString("label");
            rootFS = json.getString("rootFS");
            numberOfExecutors = json.getInt("numberOfExecutors");
            idleTerminationInMinute = json.getInt("idleTerminationInMinute");
            allowedMaxSlaveCount = json.getInt("allowedMaxSlaveCount");
            maxAttachedTimeInMinutes = json.getInt("maxAttachedTimeInMinutes");
            save();
            return super.configure(req, json);
        }
    }

}
