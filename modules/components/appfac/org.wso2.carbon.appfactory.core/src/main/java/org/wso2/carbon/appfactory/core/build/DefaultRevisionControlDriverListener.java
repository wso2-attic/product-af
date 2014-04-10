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

package org.wso2.carbon.appfactory.core.build;

import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.BuildDriver;
import org.wso2.carbon.appfactory.core.RevisionControlDriverListener;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;

public class DefaultRevisionControlDriverListener implements RevisionControlDriverListener {

    public void onGetSourceCompleted(String applicationId, String version, String revision, String tenantDomain)
                                                                                           throws AppFactoryException {
        BuildDriver buildDriver = ServiceHolder.getBuildDriver();
        DefaultBuildDriverListener listener = new DefaultBuildDriverListener();
        buildDriver.buildArtifact(applicationId, version, revision, listener, tenantDomain);
    }

}
