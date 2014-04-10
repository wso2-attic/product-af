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
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Slave;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.NodeProperty;
import hudson.slaves.OfflineCause;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;

import java.io.IOException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementing because i need to plug custom launcher and retention strategy
 */
public class DynamicSlave extends Slave {
    private static final Logger logger = Logger.getLogger(DynamicSlave.class.getName());

    @DataBoundConstructor
    public DynamicSlave(String name, String nodeDescription, String remoteFS, String numExecutors, Mode mode, String labelString, int idleTerminationInMinutes, int maxAttachedTimeInMinutes) throws Descriptor.FormException, IOException {
        super(name, nodeDescription, remoteFS, numExecutors, mode, labelString,
                new DynamicSlaveComputerLauncher(name),
                new DynamicSlaveRetentionStrategy(idleTerminationInMinutes, maxAttachedTimeInMinutes), Collections.<NodeProperty<?>>emptyList());
        logger.info("Launching dynamic slave in cloud.." + name);
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    public void terminate() {

        try {
            super.getComputer().disconnect(new ByRetentionPolicy("The computer is disconnected by dynamic slave retention policy"));
            //disconnect the computer first
            try {
                super.getComputer().waitUntilOffline();
                logger.info("Successfully disconnected the computer " + this.getNodeName());
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, "Interrupted while waiting for computer disconnect", e);
            }
            // Remove the node from hudson.
            Hudson.getInstance().removeNode(this);

            ComputerLauncher launcher = getLauncher();

            // If this is a dynamic slave computer launcher, terminate the launcher.
            if (launcher instanceof DynamicSlaveComputerLauncher) {
                ((DynamicSlaveComputerLauncher) launcher).terminate();
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to terminate slave instance: "
                    + this.getNodeName(), e);
        }
        logger.info("The dynamic slave computer " + this.getNodeName() + " is terminated successfully");
    }

    @Extension
    public static class DescriptorImpl extends SlaveDescriptor {
        @Override
        public String getDisplayName() {
            return "Dynamic Slave";
        }
    }

    @Override
    public Computer createComputer() {
        return new DynamicSlaveComputer(this);
    }

    public static class ByRetentionPolicy extends OfflineCause {
        @Exported
        public final String message;

        public ByRetentionPolicy(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return message;
        }
    }
}
