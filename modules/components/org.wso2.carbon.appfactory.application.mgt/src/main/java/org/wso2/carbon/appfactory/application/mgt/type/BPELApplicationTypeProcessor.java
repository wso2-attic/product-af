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

package org.wso2.carbon.appfactory.application.mgt.type;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.utilities.version.AppVersionStrategyExecutor;

import java.io.File;
import java.util.Map;

/**
 * BPEL Application type processor
 */
public class BPELApplicationTypeProcessor extends AbstractApplicationTypeProcessor {

    public BPELApplicationTypeProcessor(String type) {
        super(type);
    }

    @Override
    public void doVersion(String applicationId, String targetVersion, String currentVersion, String workingDirectory)
			throws AppFactoryException {
        AppVersionStrategyExecutor.doVersionForBPEL(applicationId, targetVersion, new File(workingDirectory));
    }

    @Override
    public OMElement configureBuildJob(OMElement jobConfigTemplate, Map<String, String> parameters,
                                       String projectType)
            throws AppFactoryException {
        //Currently does not have and bpel application type so no need to implement
        return null;
    }
}
