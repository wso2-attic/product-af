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
 * DTO for build information
 */
public class BuildStatus {
    private String lastBuildId;

    public String getCurrentBuildId() {
        return currentBuildId;
    }

    public void setCurrentBuildId(String currentBuildId) {
        this.currentBuildId = currentBuildId;
    }

    private String currentBuildId;
    private String lastBuildStatus;
    private long lastBuildTime;

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

    public long getLastBuildTime() {
        return lastBuildTime;
    }

    public void setLastBuildTime(long lastBuildTime) {
        this.lastBuildTime = lastBuildTime;
    }

    @Override
    public String toString() {
        return "BuildStatus{" +
                "lastBuildId='" + lastBuildId + '\'' +
                ", currentBuildId='" + currentBuildId + '\'' +
                ", lastBuildStatus='" + lastBuildStatus + '\'' +
                ", lastBuildTime=" + lastBuildTime +
                '}';
    }
}
