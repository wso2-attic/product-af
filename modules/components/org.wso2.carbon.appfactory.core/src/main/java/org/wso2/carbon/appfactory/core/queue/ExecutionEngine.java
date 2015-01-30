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

package org.wso2.carbon.appfactory.core.queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;

/**
 * ExecutionEngine is an entry in this process. When any code point needs some
 * queue operation, can be used this Queue Execution Engine. This has
 * synchronized queue, user can put the Serializable custom bean to the queue
 * and specify the time delay for the queue element execution cycle. Executor
 * interface is given to implement the user own execute method.
 * <p/>
 * <p/>
 * Examples of expected usage:
 * <p/>
 * <blockquote>
 * <p/>
 * <pre>
 *
 *  //Create an ExecutionEngine and start.
 *  boolean isAutoStart = false ;
 *  boolean isAutoRestart = false ;
 *  //Time Delay in Millisecond
 *  int timeDelay = 100 ;
 *  int queueLength - 100 ;
 *
 *  //ExecutorImpl is an implementation of the Executor to do the task when the executor do execute.
 *  Executor executor = new ExecutorImpl();
 *  ExecutionEngine executionEngine =  new ExecutionEngine<CustomBean>(executor,isAutoStart,
 *  															isAutoRestart,timeDelay,queueLength);
 *  executionEngine.startEngine();
 *
 *  //Put an element to the queue
 *  CustomBean customBean = new CustomBean();
 *  executionEngine.getSynchQueue().put(customBean);
 *
 * </pre>
 * <p/>
 * </blockquote>
 *
 * @param <T> the type of elements held in this queue that implements the
 *            Serializable interface.
 */
public class ExecutionEngine<T extends Serializable> {

    private SynchQueue<T> synchQueue = null;
    private Executor<T> executor = null;

    private long callDelay = 0;
    private int executorCount = 1 ;
    private boolean isAutoRestart = false;
    private boolean isMultiExecutor = false ;

    private QueueProcessor<T> queueProcessor = null;

    private final Log log = LogFactory.getLog(ExecutionEngine.class);
    
    /**
     * @param executor    is an interface that wants to implement to get the call for
     *                    execute.
     * @param isAutoStart is true then server will be stated when create an instance of
     *                    ExecutionEngine.
     * @param callDelay   is the time delay in between the poll of queue.
     * @param queueLength is maximum number of elements that can held by the queue.
     * @param executorCount is the count that parellaly invoke from the processor.
     * @throws AppFactoryQueueException
     */
    public ExecutionEngine(Executor<T> executor, boolean isAutoStart, boolean isAutoRestart,
                           long callDelay, int queueLength, int executorCount) throws AppFactoryQueueException {
        this.synchQueue = new SynchQueue<T>(queueLength);
        this.executor = executor;
        this.callDelay = callDelay;
        this.isAutoRestart = isAutoRestart;
        this.executorCount = executorCount ;
        if (isAutoStart) {
            startEngine();
        }

    }
    

    /**
     * @param executor    is an interface that wants to implement to get the call for
     *                    execute.
     * @param isAutoStart is true then server will be stated when create an instance of
     *                    ExecutionEngine.
     * @param callDelay   is the time delay in between the poll of queue.
     * @param queueLength is maximum number of elements that can held by the queue.
     * @throws AppFactoryQueueException
     */
    public ExecutionEngine(Executor<T> executor, boolean isAutoStart, boolean isAutoRestart,
                           long callDelay, int queueLength) throws AppFactoryQueueException {
        this.synchQueue = new SynchQueue<T>(queueLength);
        this.executor = executor;
        this.callDelay = callDelay;
        this.isAutoRestart = isAutoRestart;
        if (isAutoStart) {
            startEngine();
        }

    }

    /**
     * @param executor    is an interface that wants to implement to get the call for
     *                    execute.
     * @param isAutoStart is true then server will be stated when create an instance of
     *                    ExecutionEngine.
     * @param callDelay   is the time delay in between the poll of queue.
     * @throws AppFactoryQueueException
     */
    public ExecutionEngine(Executor<T> executor, boolean isAutoStart, boolean isAutoRestart,
                           long callDelay) throws AppFactoryQueueException {

        this.executor = executor;
        this.callDelay = callDelay;
        this.isAutoRestart = isAutoRestart;
        if (isAutoStart) {
            startEngine();
        }

    }

    /**
     * @param executor    is an interface that wants to implement to get the call for
     *                    execute.
     * @param isAutoStart is true then server will be stated when create an instance of
     *                    ExecutionEngine.
     * @param queueLength is maximum number of elements that can held by the queue.
     * @throws AppFactoryQueueException
     */
    public ExecutionEngine(Executor<T> executor, boolean isAutoStart, boolean isAutoRestart,
                           int queueLength) throws AppFactoryQueueException {

        this.synchQueue = new SynchQueue<T>(queueLength);
        this.executor = executor;
        this.isAutoRestart = isAutoRestart;
        if (isAutoStart) {
            startEngine();
        }

    }

    /**
     * @param executor    is an interface that wants to implement to get the call for
     *                    execute.
     * @param isAutoStart is true then server will be stated when create an instance of
     *                    ExecutionEngine.
     * @throws AppFactoryQueueException
     */
    public ExecutionEngine(Executor<T> executor, boolean isAutoStart, boolean isAutoRestart)
            throws AppFactoryQueueException {

        this.synchQueue = new SynchQueue<T>();
        this.executor = executor;
        this.isAutoRestart = isAutoRestart;
        if (isAutoStart) {
            startEngine();
        }

    }

    public SynchQueue<T> getSynchQueue() {
        return synchQueue;
    }

    public long getCallDelay() {
        return callDelay;
    }

    public void setCallDelay(long callDelay) {
        this.callDelay = callDelay;
    }

    public boolean isAutoRestart() {
        return isAutoRestart;
    }

    public Executor<T> getExecutor() {
        return executor;
    }
    
    public int getExecutorCount() {
		return executorCount;
	}

	public void setExecutorCount(int executorCount) {
		this.executorCount = executorCount;
	}

	public boolean isMultiExecutor() {
		return isMultiExecutor;
	}

	public void setMultiExecutor(boolean isMultiExecutor) {
		this.isMultiExecutor = isMultiExecutor;
	}

	/**
     * Start the Execution Engine if not set isAutoStart = true
     *
     * @throws AppFactoryQueueException
     */
    public synchronized void startEngine() throws AppFactoryQueueException {
        try {
            if (this.queueProcessor == null) {
                this.queueProcessor = new QueueProcessor<T>(this);
            }
            if (!this.queueProcessor.isAlive()) {
                this.queueProcessor = new QueueProcessor<T>(this);
                this.queueProcessor.start();
            }
        } catch (Exception e) {
            String errorMsg = "Error occured when starting the execution thread, " + e.getMessage();
            log.error(errorMsg, e);
            throw new AppFactoryQueueException(errorMsg, e);
        }
    }

}
