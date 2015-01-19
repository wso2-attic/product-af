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
 * Read the elements from the queue and execute the element are done by the
 * QueueProcessor. Process running until it has some interruption or state
 * change in isProcessStopped=true. If isAutoRestart=true in executionEngine,
 * then
 * QueueProcessor will be restarted if it is crashed.
 *
 * @param <T> the type of elements held in this queue that implements the
 *            Serializable interface.
 * @see ExecutionEngine
 */
public class QueueProcessor<T extends Serializable> extends Thread {

    private final Log log = LogFactory.getLog(QueueProcessor.class);

    private ExecutionEngine<T> executionEngine = null;
    private boolean isProcessStopped = false;

    public QueueProcessor(ExecutionEngine<T> executionEngine) {
        this.executionEngine = executionEngine;
    }

    public boolean isProcessStopped() {
        return isProcessStopped;
    }

    public void setProcessStopped(boolean isProcessStopped) {
        this.isProcessStopped = isProcessStopped;
    }

    // Thread run method
    public void run() {
        try {
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                log.error("Thread interruption in AppCration Process.");
            }
            execute();
        } catch (Exception e) {
            String errorMsg =
                    "Execution thread got the " + e.getMessage() +
                    " and application creation thread is stopped.";
            log.error(errorMsg, e);
            // Here not going to throw an exception. If isAutoRestart=true then
            // it will be restarted.
        }

        // isAutoRestart=true, then user wants to auto restart when server is
        // stopped by exception.
        // isProcessStopped=true means, user stopped the service by manually.
        // Then restart process will not be happened.
        if (this.executionEngine.isAutoRestart() && !isProcessStopped) {
            String infoMsg = "Execution thread restarting ...";
            log.info(infoMsg);
            new QueueProcessor<T>(this.executionEngine).start();
        }

    }

    /**
     * Execution process is done in this method
     *
     * @throws AppFactoryQueueException
     */
    public void execute() throws Exception {
        // Running this loop to poll and execute the item from the queue.
        while (true && !isProcessStopped) {

            
        	//ExecutorCount is the how many executor can run at once to this process.
        	for (int i = 0; i < executionEngine.getExecutorCount(); i++) {
        		// Poll the item from the queue. If the queue is empty, this
                // call wait until an item is come to the queue.
        		T t = executionEngine.getSynchQueue().poll();

                log.info("ExecuteEngine Execute an element. Element Info : " + t.toString());
                if(executionEngine.isMultiExecutor()){
                	new ThreadExecutor<T>(executionEngine.getExecutor(),t).start();
                }else{
                	executionEngine.getExecutor().execute(t);
                }
            }
        	
            

            // Waiting a specific time interval before poll again from the
            // queue. Default is 0.
            if (executionEngine.getCallDelay() > 0) {
                try {
                    Thread.sleep(executionEngine.getCallDelay());
                } catch (Exception e) {
                    String errorMsg = "Error occured in thread sleep, " + e.getMessage();
                    log.error(errorMsg, e);
                }
            }

        }
    }
}
class ThreadExecutor<T extends Serializable> extends Thread{
	private Executor<T> executor = null ;
	private T t = null ;
	public ThreadExecutor(Executor<T> executor,T t) {
	    this.executor = executor ;
	    this.t = t ;
	    this.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler(){
	        public void uncaughtException(Thread t, Throwable e) {
	            System.out.println("exception " + e + " from thread " + t);
	        }
	    });
    }
	public void run(){
		try {
	        this.executor.execute(t) ;
        } catch (Exception e) {
	        throw new RuntimeException();
        }
	}
}