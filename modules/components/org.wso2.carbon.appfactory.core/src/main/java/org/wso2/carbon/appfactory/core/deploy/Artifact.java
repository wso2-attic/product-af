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
package org.wso2.carbon.appfactory.core.deploy;

/**
 * DTO to represent all the artifact related information.
 * 
 * @author shamika
 * 
 */
public class Artifact {

    private String applicationKey;

    private String lastBuildStatus;

    private String version;

    private boolean autoBuild = false;

    private boolean autoDeploy = false;

    private String lastDeployedId;

    private String stage;

    private String currentBuildStatus;

    private String promoteStatus;

    private String repoURL;

    private String userId;

    private String productionMappedDomain;

    public String getCurrentBuildStatus() {
        return currentBuildStatus;
    }

    public void setCurrentBuildStatus(String currentBuildStatus) {
        this.currentBuildStatus = currentBuildStatus;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public Artifact(String applicationKey, String lastBuildStatus, String version,
                    boolean isAutoBuild, boolean isAutoDeploy, String lastDeployedId, String stage,
                    String currentBuildStatus, String promoteStatus) {
        super();
        this.applicationKey = applicationKey;
        this.lastBuildStatus = lastBuildStatus;
        this.version = version;
        this.autoBuild = isAutoBuild;
        this.autoDeploy = isAutoDeploy;
        this.lastDeployedId = lastDeployedId;
        this.stage = stage;
        this.currentBuildStatus = currentBuildStatus;
        this.promoteStatus = promoteStatus;
    }

    public Artifact(String applicationKey, String userId, String version, boolean isAutoBuild,
                    boolean isAutoDeploy, String repoURL, String lastDeployedId,
                    String lastBuildStatus, String currentBuildStatus, String promoteStatus) {
        super();
        this.applicationKey = applicationKey;
        this.userId = userId;
        this.version = version;
        this.autoBuild = isAutoBuild;
        this.autoDeploy = isAutoDeploy;
        this.repoURL = repoURL;
        this.lastBuildStatus = lastBuildStatus;
        this.lastDeployedId = lastDeployedId;
        this.currentBuildStatus = currentBuildStatus;
        this.promoteStatus = promoteStatus;
    }

    public String getApplicationKey() {
        return applicationKey;
    }

    public void setApplicationKey(String applicationKey) {
        this.applicationKey = applicationKey;
    }

    public String getLastBuildStatus() {
        return lastBuildStatus;
    }

    public void setLastBuildStatus(String lastSuccessBuildStatus) {
        this.lastBuildStatus = lastSuccessBuildStatus;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLastDeployedId() {
        return lastDeployedId;
    }

    public void setLastDeployedId(String lastDeployedId) {
        this.lastDeployedId = lastDeployedId;
    }

    public boolean isAutoBuild() {
        return autoBuild;
    }

    public void setAutoBuild(boolean autoBuild) {
        this.autoBuild = autoBuild;
    }

    public boolean isAutoDeploy() {
        return autoDeploy;
    }

    public void setAutoDeploy(boolean autoDeploy) {
        this.autoDeploy = autoDeploy;
    }

    public String getRepoURL() {
        return repoURL;
    }

    public void setRepoURL(String repoURL) {
        this.repoURL = repoURL;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProductionMappedDomain() {
        return productionMappedDomain;
    }

    public void setProductionMappedDomain(String productionMappedDomain) {
        this.productionMappedDomain = productionMappedDomain;
    }


    public String getPromoteStatus() {
        return promoteStatus;
    }

    public void setPromoteStatus(String promoteStatus) {
        this.promoteStatus = promoteStatus;
    }

    

}
