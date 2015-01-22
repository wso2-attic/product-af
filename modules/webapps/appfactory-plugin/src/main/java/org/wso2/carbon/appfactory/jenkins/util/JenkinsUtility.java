/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *    WSO2 Inc. licenses this file to you under the Apache License,
 *    Version 2.0 (the "License"); you may not use this file except
 *    in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package org.wso2.carbon.appfactory.jenkins.util;

import org.wso2.carbon.appfactory.common.AppFactoryConstants;

public class JenkinsUtility {
	public static String getJobName(String applicationId, String version) {
        // Job name will be '<ApplicationId>-<version>-default'
        return applicationId.concat("-").concat(version).concat("-").concat("default");
    }
	
	public static String getJobName(String applicationId, String version,String userName, String repoFrom) {
        // Job name will be '<ApplicationId>-<version>-default'
		if(AppFactoryConstants.FORK_REPOSITORY.equals(repoFrom)){
			return applicationId.concat("-").concat(version).concat("-").concat("default").concat("-").concat(userName);
        }
		return applicationId.concat("-").concat(version).concat("-").concat("default");
    }
	
	public static String getApplicationId(String jobName) {
        // Job name will be '<ApplicationId>-<version>-default'
		String[] jobValues = jobName.split("-");
        return jobValues[0];
    }
	public static String getVersion(String jobName) {
        // Job name will be '<ApplicationId>-<version>-default'
		String[] jobValues = jobName.split("-");
        return jobValues[1];
    }
}
