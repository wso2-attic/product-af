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

package org.wso2.carbon.appfactory.jenkins.artifact.storage;

/**
 * Config object used to pass configs to PersistArtifactHandler
 * .
 */
public class PersistArtifactHandlerConfig {

    private String jobName;
    private String applicationArtifactExtension;
    private String tagName;
    private String persistentStoragePath;

    public String getApplicationArtifactExtension() {
        return applicationArtifactExtension;
    }

    public void setApplicationArtifactExtension(String applicationArtifactExtension) {
        this.applicationArtifactExtension = applicationArtifactExtension;
    }

    public String getPersistentStoragePath() {
        return persistentStoragePath;
    }

    public void setPersistentStoragePath(String persistentStoragePath) {
        this.persistentStoragePath = persistentStoragePath;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
}
