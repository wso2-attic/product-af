/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.carbon.appfactory.core.dto;

public class BuildandDeployStatus {

    private String lastBuildId;
    private String lastBuildStatus;
    private String lastDeployedId;

    public BuildandDeployStatus(String buildId, String buildStatus, String deployedId) {
        this.lastBuildId = buildId;
        this.lastBuildStatus = buildStatus;
        this.lastDeployedId = deployedId;
    }

    public String getLastBuildId() {
        return lastBuildId;
    }

    public void setLastBuildId(String lastBuildId) {
        this.lastBuildId = lastBuildId;
    }

    public String getLastBuildStatus() {
        return lastBuildStatus;
    }

    public void setLastBuildStatus(String lastBuildStatus) {
        this.lastBuildStatus = lastBuildStatus;
    }

    public String getLastDeployedId() {
        return lastDeployedId;
    }

    public void setLastDeployedId(String lastDeployedId) {
        this.lastDeployedId = lastDeployedId;
    }

}
