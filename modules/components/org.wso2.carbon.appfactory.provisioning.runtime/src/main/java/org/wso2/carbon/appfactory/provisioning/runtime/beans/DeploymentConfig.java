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

import java.util.List;
import java.util.Map;

public class DeploymentConfig {

    private String deploymentName;
    private Integer replicas;
    private Map<String, String> podTemplateSpecLabels;
    private List<Container> containers;

    public void setDeploymentName(String deploymentName) {
        this.deploymentName = deploymentName;
    }

    public void setReplicas(Integer replicas) {
        this.replicas = replicas;
    }

    public void setPodTemplateSpecLables(Map<String, String> podTemplateSpecLabels) {
        this.podTemplateSpecLabels = podTemplateSpecLabels;
    }

    public void setContainers(Container container){
        this.containers.add(container);
    }

    public void setContainers(List<Container> containers) {
        this.containers = containers;
    }

    public String getDeploymentName() {
        return deploymentName;
    }

    public Integer getReplicas() {
        return replicas;
    }

    public Map<String, String> getPodTemplateSpecLables() {
        return podTemplateSpecLabels;
    }

    public List<Container> getContainers() {
        return containers;
    }
}
