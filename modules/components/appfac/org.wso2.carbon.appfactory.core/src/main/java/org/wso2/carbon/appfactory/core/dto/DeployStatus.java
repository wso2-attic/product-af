/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.appfactory.core.dto;

/**
 * DTO for deploy information
 */
public class DeployStatus {
    private String lastDeployedId;
    private String lastDeployedStatus;
    private long lastDeployedTime;

    @Override
    public String toString() {
        return "DeployStatus{" +
                "lastDeployedId='" + lastDeployedId + '\'' +
                ", lastDeployedStatus='" + lastDeployedStatus + '\'' +
                ", lastDeployedTime=" + lastDeployedTime +
                '}';
    }

    public String getLastDeployedId() {
        return lastDeployedId;
    }

    public void setLastDeployedId(String lastDeployedId) {
        this.lastDeployedId = lastDeployedId;
    }

    public String getLastDeployedStatus() {
        return lastDeployedStatus;
    }

    public void setLastDeployedStatus(String lastDeployedStatus) {
        this.lastDeployedStatus = lastDeployedStatus;
    }

    public long getLastDeployedTime() {
        return lastDeployedTime;
    }

    public void setLastDeployedTime(long lastDeployedTime) {
        this.lastDeployedTime = lastDeployedTime;
    }
}
