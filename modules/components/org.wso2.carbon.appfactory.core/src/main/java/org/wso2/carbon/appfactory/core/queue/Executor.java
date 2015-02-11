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

import java.io.Serializable;

/**
 * When the ExecutionEngine execute the process, it wants implementation of this
 * interface. It calls this execute method. So implementer of this interface can
 * get the calls from the engine thread in given cycle as the availability of
 * items
 * in the queue.
 *
 * @param <T> the type of elements held in this queue that implements the
 *            Serializable interface.
 * @see ExecutionEngine
 */
public interface Executor<T extends Serializable> {
    /**
     * This method will be call by the ExecutionEngine in the given cycle as
     * availability of items in the queue.
     *
     * @param t the type of elements held in this queue that implements the
     *          Serializable interface.
     * @throws Exception
     */
    void execute(T t) throws Exception;
}
