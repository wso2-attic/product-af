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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.utilities.version.AppVersionStrategyExecutor;

import java.io.File;
import java.util.Map;

/**
 * Application type implementation for Web App
 */
public class MavenBasedApplicationTypeProcessor extends AbstractApplicationTypeProcessor {
    private static final Log log = LogFactory.getLog(MavenBasedApplicationTypeProcessor.class);

    public MavenBasedApplicationTypeProcessor(String type) {
        super(type);
    }

    @Override
    public void doVersion(String applicationId, String targetVersion, String currentVersion, String workingDirectory)
            throws AppFactoryException {
        AppVersionStrategyExecutor.doVersionForMVN(targetVersion, new File(workingDirectory));
    }


    @Override
    public OMElement configureBuildJob(OMElement jobConfigTemplate, Map<String, String> parameters, String projectType)
            throws AppFactoryException {

        if (jobConfigTemplate == null) {
            String msg =
                    "Class loader is unable to find the jenkins job configuration template for Maven application types";
            log.error(msg);
            throw new AppFactoryException(msg);

        }
        // configure repo data
        jobConfigTemplate = configureRepositoryData(jobConfigTemplate, parameters);

        // set the maven 3 config name
        setValueUsingXpath(jobConfigTemplate,
                           AppFactoryConstants.MAVEN3_CONFIG_NAME_XAPTH_SELECTOR,
                           parameters.get(AppFactoryConstants.MAVEN3_CONFIG_NAME));

        // Support for post build listener residing in jenkins server
        setValueUsingXpath(jobConfigTemplate,
                           AppFactoryConstants.PUBLISHERS_APPFACTORY_POST_BUILD_APP_EXTENSION_XPATH_SELECTOR,
                           parameters.get(AppFactoryConstants.APPTYPE_EXTENSION));

        setValueUsingXpath(jobConfigTemplate,
                           AppFactoryConstants.PUBLISHERS_APPFACTORY_POST_BUILD_APP_ID_XPATH_SELECTOR,
                           parameters.get(AppFactoryConstants.APPLICATION_ID));

        setValueUsingXpath(jobConfigTemplate,
                           AppFactoryConstants.PUBLISHERS_APPFACTORY_POST_BUILD_APP_VERSION_XPATH_SELECTOR,
                           parameters.get(AppFactoryConstants.APPLICATION_VERSION));

        setValueUsingXpath(jobConfigTemplate,
                           AppFactoryConstants.PUBLISHERS_APPFACTORY_POST_BUILD_USERNAME_XPATH_SELECTOR,
                           parameters.get(AppFactoryConstants.APPLICATION_USER));

        setValueUsingXpath(jobConfigTemplate,
                           AppFactoryConstants.PUBLISHERS_APPFACTORY_POST_BUILD_REPOFROM_XPATH_SELECTOR,
                           parameters.get(AppFactoryConstants.REPOSITORY_FROM));

        setValueUsingXpath(jobConfigTemplate,
                           AppFactoryConstants.APPLICATION_TRIGGER_PERIOD,
                           parameters.get(AppFactoryConstants.APPLICATION_POLLING_PERIOD));

        return jobConfigTemplate;
    }
}
