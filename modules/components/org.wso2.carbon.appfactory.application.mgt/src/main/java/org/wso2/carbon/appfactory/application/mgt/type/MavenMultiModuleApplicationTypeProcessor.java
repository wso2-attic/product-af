/*
 * Copyright 2005-2014 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.appfactory.application.mgt.type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.utilities.version.AppVersionStrategyExecutor;

import java.io.File;

/**
 * Application Type Processor for Maven Multi module application types
 */
public class MavenMultiModuleApplicationTypeProcessor extends MavenBasedApplicationTypeProcessor {
	private static final Log log = LogFactory.getLog(MavenMultiModuleApplicationTypeProcessor.class);

    public MavenMultiModuleApplicationTypeProcessor(String type) {
        super(type);
    }

    @Override
	public void doVersion(String applicationId, String targetVersion, String currentVersion,
	                      String workingDirectory) throws AppFactoryException {
		AppVersionStrategyExecutor.doVersionForMultiModuleMVN(targetVersion, new File(workingDirectory));
	}
}
