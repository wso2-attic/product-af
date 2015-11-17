/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appfactory.core.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Details about life cycles in lifecycle configuration
 */
public class LifecycleInfoBean {
    private String lifecycleName;
    //List of Stage objects from Stage class
    private List<StageBean> stages;

    public String getBuildStageName() {
        return buildStageName;
    }

    public void setBuildStageName(String buildStageName) {
        this.buildStageName = buildStageName;
    }

    //store which stage the build happens(stored in appfactory.xml)
    private String buildStageName;
    //store which displaying name of the lifecycle(stored in appfactory.xml)
    private String displayName;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getLifecycleName() {
        return this.lifecycleName;
    }

    public void setLifecycleName(String lifecycleName) {
        this.lifecycleName = lifecycleName;
    }

    public List<StageBean> getStages() {
        if (stages == null) {
            return Collections.emptyList();
        }
        return this.stages;
    }

    public void setStages(List<StageBean> stages) {
        if (this.stages == null) {
            this.stages = new ArrayList<StageBean>();
        }
        this.stages.addAll(stages);
    }
}
