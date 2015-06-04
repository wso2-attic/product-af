/*
 *
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.carbon.appfactory.jenkins.build.strategy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.jenkins.build.JenkinsCIConstants;
import org.wso2.carbon.appfactory.jenkins.build.internal.ServiceContainer;

/**
 * Created by samith on 5/12/15.
 */
public class ClusterSelectingStrategyImpl implements ClusterSelectingStrategy {
    private static final Log log = LogFactory
            .getLog(ClusterSelectingStrategyImpl.class);

    @Override
    public String getBucketClusterId(int bucketId) {
        String clusterId = ServiceContainer.getAppFactoryConfiguration().getFirstProperty(
                JenkinsCIConstants.JENKINS_LB_BUCKET_LIST + "." + Integer.toString(bucketId));
        if (log.isDebugEnabled()) {
            log.debug("Cluster :" + clusterId + " is selected for bucket id: " + bucketId);
        }
        return clusterId;
    }
}
