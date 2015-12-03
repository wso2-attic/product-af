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

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;

import java.util.List;

public class ApplicationContext {

    private List<Pod> pods;
    private List<Service> services;

    public List<Pod> getPods() {
        return pods;
    }

    public List<Service> getServices() {
        return services;
    }

    public void setPods(List<Pod> pods) {
        this.pods = pods;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }
}
