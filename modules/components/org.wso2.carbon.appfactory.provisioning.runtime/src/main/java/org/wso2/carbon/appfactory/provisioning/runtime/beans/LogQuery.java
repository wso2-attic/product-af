/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appfactory.provisioning.runtime.beans;

public class LogQuery {

    private boolean isFollowing = false;
    private int previousRecordsCount = -1;
    private int durationInHours = -1;

    public LogQuery(boolean isFollowing, int previousRecords, int timeStamp) {
        this.isFollowing = isFollowing;
        this.previousRecordsCount = previousRecords;
        this.durationInHours = timeStamp;
    }

    public boolean getIsFollowing() {
        return isFollowing;
    }

    public int getPreviousRecordsCount() {
        return previousRecordsCount;
    }

    public int getDurationInHours() {
        return durationInHours;
    }

    public void setIsFollowing(boolean following) {
        this.isFollowing = following;
    }

    public void setPreviousRecordsCount(int previousRecordsCount) {
        this.previousRecordsCount = previousRecordsCount;
    }

    public void setDurationInHours(int durationInHours) {
        this.durationInHours = durationInHours;
    }
}
