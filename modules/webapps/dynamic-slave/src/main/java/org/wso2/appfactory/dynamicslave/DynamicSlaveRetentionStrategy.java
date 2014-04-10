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

import hudson.model.Descriptor;
import hudson.slaves.RetentionStrategy;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import static hudson.util.TimeUnit2.MINUTES;

/**
 * This is the retention strategy for dynamic slave.It will terminate the computer in following instances
 * <p/>
 * 1.Computer is idle more than idleTerminationMinutes
 * 2.Computer is up more than maxAttachedTimeInMinutes
 */
public class DynamicSlaveRetentionStrategy extends RetentionStrategy<DynamicSlaveComputer> {
    public final int idleTerminationMinutes;
    public final int maxAttachedTimeInMinutes;
    private static final Logger LOGGER = Logger
            .getLogger(DynamicSlaveRetentionStrategy.class.getName());

    public DynamicSlaveRetentionStrategy(int idleTerminationMinutes, int maxAttachedTimeInMinutes) {
        this.idleTerminationMinutes = idleTerminationMinutes;
        this.maxAttachedTimeInMinutes = maxAttachedTimeInMinutes;
    }

    @Override
    public long check(DynamicSlaveComputer c) {
        if (c.getNode() == null) {
            return 1;
        }
        // NOTE: 'c.getConnectTime()' refers to when the Jenkins slave was launched.
        long upTime = (System.currentTimeMillis() - c.getConnectTime());
        // If we just launched this computer, check back after 1 min.

        if (upTime <
                MINUTES.toMillis(idleTerminationMinutes)) {
            return 1;
        }

        // If the computer is offline, terminate it.
        if (c.isOffline()) {
            LOGGER.info("Disconnecting offline computer " + c.getName());
            c.getNode().terminate();
            return 1;
        }

        // Terminate the computer if it is idle for longer than
        // 'idleTerminationMinutes'.
        if (c.isIdle()) {
            final long idleMilliseconds =
                    System.currentTimeMillis() - c.getIdleStartMilliseconds();

            if (idleMilliseconds > MINUTES.toMillis(idleTerminationMinutes)) {
                LOGGER.info("Disconnecting idle computer " + c.getName());
                c.getNode().terminate();
            }
        }
        //Terminate the computer if it is running more than allowed time
        if (upTime > MINUTES.toMillis(maxAttachedTimeInMinutes)) {
            LOGGER.info("Disconnecting computer that is running more than allowed time " + c.getName());
            //put a alert note on log file
            try {
                OutputStream out = c.openLogFile();
                try {
                    out.flush();
                    out.write("ALERT!!!: Disconnecting computer that is running more than allowed time !!!! ".getBytes());
                    out.flush();
                } finally {
                    out.close();
                }


            } catch (IOException e) {
                LOGGER.info("Error while getting log file of  " + c.getName());
            }
            c.getNode().terminate();
        }

        return 1;
    }

    public static class DescriptorImpl extends Descriptor<RetentionStrategy<?>> {
        @Override
        public String getDisplayName() {
            return "Dynamic Slave Retention Strategy";
        }
    }

    @Override
    public void start(DynamicSlaveComputer c) {

        c.connect(false);
    }
}
