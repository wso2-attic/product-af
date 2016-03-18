/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.appfactory.provisioning.runtime.beans;

import io.fabric8.kubernetes.api.model.VolumeMount;

import java.util.List;
import java.util.Map;

public class Container {

    private String containerName;
    private String baseImageName;
    private String baseImageVersion;
    private String imageId;
    private int replicaNumber;
    private String serviceUrl;
    private Map<String,String> envVariables;
    private String probe;
    private List<ServiceProxy> serviceProxies;
    private List<String> labels;
    private List<VolumeMount> volumeMounts;

    public List<ServiceProxy> getServiceProxies() {
        return serviceProxies;
    }

    public void setServiceProxies(List<ServiceProxy> serviceProxies) {
        this.serviceProxies = serviceProxies;
    }

    public String getBaseImageName() {
        return baseImageName;
    }

    public void setBaseImageName(String baseImageName) {
        this.baseImageName = baseImageName;
    }

    public String getBaseImageVersion() {
        return baseImageVersion;
    }

    public void setBaseImageVersion(String baseImageVersion) {
        this.baseImageVersion = baseImageVersion;
    }

    public String getContainerName() {
        return containerName;
    }

    public Map<String, String> getEnvVariables() {
        return envVariables;
    }

    public void setEnvVariables(Map<String, String> envVariables) {
        this.envVariables = envVariables;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public int getReplicaNumber() {
        return replicaNumber;
    }

    public void setReplicaNumber(int replicaNumber) {
        this.replicaNumber = replicaNumber;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public String getProbe() {
        return probe;
    }

    public void setProbe(String probe) {
        this.probe = probe;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public List getVolumeMounts() {
        return volumeMounts;
    }

    public void setVolumeMounts(List<VolumeMount> volumeMounts) {
        this.volumeMounts = volumeMounts;
    }
}
