/*
 * Copyright 2014 WSO2, Inc. (http://wso2.com)
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
* Holds information about Cartridge Cluster
*/
public class CartridgeCluster {

    private String id;
    private String clusterId;
    private String lbClusterId;
    private String activeIP;

    /**
     * No Argument Constructor
     */
    public CartridgeCluster() {
        // NOP
    }

    /**
     * Constructor
     * @param id identifier of the object
     * @param clusterId clusterId of the member cluster
     * @param lbClusterId load balancer clusterId of the member
     * @param activeIP publicly accessible IP address of the member cluster
     */
    public CartridgeCluster(String id, String clusterId, String lbClusterId, String activeIP) {
        this.id = id;
        this.clusterId = clusterId;
        this.lbClusterId = lbClusterId;
        this.activeIP = activeIP;
    }

    /**
     * @return identifier of the object
     */
    public String getId() {
        return id;
    }

    /**
     * @param id identifier of the object
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return clusterId of the member cluster
     */
    public String getClusterId() {
        return clusterId;
    }

    /**
     * @param clusterId clusterId of the member cluster
     */
    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    /**
     * @return load balancer cluster Id of the member cluster
     */
    public String getLbClusterId() {
        return lbClusterId;
    }

    /**
     * @param lbClusterId load balancer clusterId of the member
     */
    public void setLbClusterId(String lbClusterId) {
        this.lbClusterId = lbClusterId;
    }

    /**
     * @return publicly accessible IP address of the member cluster
     */
    public String getActiveIP() {
        return activeIP;
    }

    /**
     * @param activeIP publicly accessible IP address of the member cluster
     */
    public void setActiveIP(String activeIP) {
        this.activeIP = activeIP;
    }

}