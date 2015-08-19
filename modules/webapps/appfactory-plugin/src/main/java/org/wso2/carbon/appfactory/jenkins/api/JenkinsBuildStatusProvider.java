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

package org.wso2.carbon.appfactory.jenkins.api;

import hudson.model.Job;
import jenkins.model.Jenkins;
import org.wso2.carbon.appfactory.deployers.build.api.BuildStatusProvider;
import org.wso2.carbon.appfactory.deployers.build.api.BuildStatusProviderException;
import org.wso2.carbon.appfactory.jenkins.util.JenkinsUtility;

public class JenkinsBuildStatusProvider implements BuildStatusProvider {

	public static final String FOLDER_JB_SEPARATOR = "/";

	@Override
    public String getLastSuccessfulBuildId(String applicationId, String version, String userName, String repoFrom,
                                           String tenantDomain) throws BuildStatusProviderException {

    	String jobName = JenkinsUtility.getJobName(applicationId, version);
	    Job job = (Job)Jenkins.getInstance().getItemByFullName(tenantDomain + FOLDER_JB_SEPARATOR + jobName);
	    return job.getLastSuccessfulBuild().getId();
    }

}
