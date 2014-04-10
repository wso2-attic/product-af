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

import hudson.model.Slave;
import hudson.slaves.SlaveComputer;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;

import java.io.IOException;

/**
 * It represents the computer that is launched in dynamic slave cloud
 */
public class DynamicSlaveComputer extends SlaveComputer {
    public DynamicSlaveComputer(Slave slave) {
        super(slave);
    }

    @Override
    public DynamicSlave getNode() {
        return (DynamicSlave) super.getNode();
    }

    @Override
    public HttpResponse doDoDelete() throws IOException {
        checkPermission(DELETE);
        if (getNode() != null)
            getNode().terminate();
        return new HttpRedirect("..");
    }
}
