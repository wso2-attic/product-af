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

package org.wso2.carbon.appfactory.application.mgt.service.applicationqueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.application.mgt.service.ApplicationInfoBean;
import org.wso2.carbon.appfactory.application.mgt.util.Util;
import org.wso2.carbon.appfactory.core.queue.AppFactoryQueueException;
import org.wso2.carbon.appfactory.core.queue.ExecutionEngine;

/**
 * Using the generic queue execution engine in appfactory core.
 * <p/>
 * AppCreator is a Singleton class.
 */
public class ApplicationCreator {

    private static final String DELAY_TIME_PROPERTY = "ApplicationCreatorDelay";
    private static final String QUEUE_LENGTH_PROPERTY = "ApplicationCreatorQueueLength";
    private static final String EXECUTOR_COUNT_PROPERTY = "ApplicationCreationExecutors";
    

    private final Log log = LogFactory.getLog(ApplicationCreator.class);
    private static ApplicationCreator appCreator = null;

    private int timeDelay = 2000;
    private int queueLength = -1;
    private int executorCount = 1 ;

    private ApplicationExecutor applicationExecutor = null;
    private ExecutionEngine<ApplicationInfoBean> executionEngine = null;

    private ApplicationCreator() throws AppFactoryQueueException {
        // Executor interface implementation
        this.applicationExecutor = new ApplicationExecutor();
        // ExecutionEngine instance
        this.executionEngine =
                new ExecutionEngine<ApplicationInfoBean>(applicationExecutor, true,
                                                         true, getTimeDelay(),
                                                         getQueueLength(),getParellalExecutorCount());

    }

    public ExecutionEngine<ApplicationInfoBean> getExecutionEngine() {
        return executionEngine;
    }

    /**
     * @return ApplicationCreator instance. This class is Singleton.
     * @throws AppFactoryQueueException
     */
    public static synchronized ApplicationCreator getInstance() throws AppFactoryQueueException {
        if (ApplicationCreator.appCreator == null) {
            ApplicationCreator.appCreator = new ApplicationCreator();
        }
        return ApplicationCreator.appCreator;
    }

    /**
     * Read timedelay property from appfactory.xml
     *
     * @return return timeDelay ;
     */
    public int getTimeDelay() {
        try {
            // Read application creation time delay from the Appfactory.xml
            String[] delayValues =
                    Util.getConfiguration()
                            .getProperties(ApplicationCreator.DELAY_TIME_PROPERTY);
            if (delayValues != null && delayValues.length > 0) {
                int tmp = Integer.parseInt(delayValues[0]);
                if (tmp > 0) {
                    this.timeDelay = tmp;
                }
            }
        } catch (Exception e) {
            log.warn("Default timedelay was set for the queue. Timedelay property load from appfactory.xml error : " +
                     e.getMessage());
        }
        return this.timeDelay;
    }
    
    
    public int getParellalExecutorCount(){
    	try {
            // Read parellal executor count from the Appfactory.xml
            String[] count =
                    Util.getConfiguration()
                            .getProperties(ApplicationCreator.EXECUTOR_COUNT_PROPERTY);
            if (count != null && count.length > 0) {
                int tmp = Integer.parseInt(count[0]);
                if (tmp > 0) {
                    this.executorCount = tmp;
                }
            }
        } catch (Exception e) {
            log.warn("Default executor count was set for the queue. executor count property load from appfactory.xml error : " +
                     e.getMessage());
        }
        return this.executorCount;
    	
    }

    /**
     * Read Queuelength property from appfactory.xml
     *
     * @return
     */
    public int getQueueLength() {
        try {
            // Read application creation queue length from the Appfactory.xml
            String[] lengthValues =
                    Util.getConfiguration()
                            .getProperties(ApplicationCreator.QUEUE_LENGTH_PROPERTY);
            if (lengthValues != null && lengthValues.length > 0) {
                int tmp = Integer.parseInt(lengthValues[0]);
                if (tmp > 0) {
                    this.queueLength = tmp;
                }
            }
        } catch (Exception e) {
            log.warn("Default length was set for the queue. QueueLength property load from appfactory.xml error : " +
                     e.getMessage());
        }
        return this.queueLength;
    }

}
