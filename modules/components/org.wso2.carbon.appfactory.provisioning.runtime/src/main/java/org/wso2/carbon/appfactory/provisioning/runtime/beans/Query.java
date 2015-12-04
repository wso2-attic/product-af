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

public class Query {

    private String following = "false";
    private String previousRecords = "false";
    private String timeStamp = "false";

    public String getFollowing() {
        return following;
    }

    public String getPreviousRecords() {
        return previousRecords;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setFollowing(String following) {
        this.following = following;
    }

    public void setPreviousRecords(String previousRecords) {
        this.previousRecords = previousRecords;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
