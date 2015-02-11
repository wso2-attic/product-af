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

import org.wso2.carbon.appfactory.common.AppFactoryException;

/**
 * Queue specific exception.
 */
public class AppFactoryQueueException extends AppFactoryException {

    private static final long serialVersionUID = 3483445727038506437L;

    public AppFactoryQueueException() {
        // TODO Auto-generated constructor stub
    }

    public AppFactoryQueueException(String s) {
        super(s);
        // TODO Auto-generated constructor stub
    }

    public AppFactoryQueueException(Throwable throwable) {
        super(throwable);
        // TODO Auto-generated constructor stub
    }

    public AppFactoryQueueException(String s, Throwable throwable) {
        super(s, throwable);
        // TODO Auto-generated constructor stub
    }

}
