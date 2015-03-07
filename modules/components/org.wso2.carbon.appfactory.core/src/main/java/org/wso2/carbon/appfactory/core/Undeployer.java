/*
 *
 *  Copyright 2014 WSO2, Inc. (http://wso2.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * 
 */
package org.wso2.carbon.appfactory.core;

import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.beans.RuntimeBean;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeBean;

/**
 * Interface is used to undeploy application artifacts
 */
public interface Undeployer {

    /**
     * Undeploy artifacts for given parameters
     *
     * @param deployerType
     * @param applicationId
     * @param applicationType
     * @param version
     * @param lifecycleStage
     * @param applicationTypeBean
     * @param runtimeBean
     * @throws AppFactoryException
     */
    void undeployArtifact(String deployerType, String applicationId,
                          String applicationType, String version, String lifecycleStage,
                          ApplicationTypeBean applicationTypeBean, RuntimeBean runtimeBean) throws AppFactoryException;

}
