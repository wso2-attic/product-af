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

package bean;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Details about life cycles in lifecycle configuration
 */
@XmlRootElement(name = "Lifecycle")
@XmlAccessorType(XmlAccessType.FIELD)
public class LifecycleBean {
    private String lifecycleName;
    //List of Stage objects from Stage class
    private Set<StageBean> stages;

    public String getLifecycleName() {
        return this.lifecycleName;
    }

    public void setLifecycleName(String lifecycleName) {
        this.lifecycleName = lifecycleName;
    }

    public Set<StageBean> getStages() {
        if (stages == null) {
            return Collections.emptySet();
        }
        return this.stages;
    }

    public void setStages(Set<StageBean> stages) {
        if (this.stages == null) {
            this.stages = new HashSet<StageBean>();
        }
        this.stages.addAll(stages);
    }
}
