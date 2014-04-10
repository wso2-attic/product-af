/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package org.wso2.carbon.appfactory.application.mgt.service;

import java.io.Serializable;

import org.wso2.carbon.appfactory.core.dto.Application;

/**
 * .
 */
public class ApplicationInfoBean implements Serializable {
    private String name;
    private String description;
    private String applicationKey;
    private String ownerUserName;
    private String applicationType;
    private String repositoryType;
    private String[] versions;
	private int branchCount = 0;
	private boolean isProduction = false;

    public ApplicationInfoBean() {

    }

    public ApplicationInfoBean(String name, String description, String applicationKey,
                               String ownerUserName, String applicationType,
                               String repositoryType) {
        this.name = name;
        this.description = description;
        this.applicationKey = applicationKey;
        this.ownerUserName = ownerUserName;
        this.applicationType = applicationType;
        this.repositoryType = repositoryType;

    }
    
    /**
     * Constructor to create ApplicationInfoBean from {@link Application}
     */
    public ApplicationInfoBean(Application applicaiton){
    	this.name = applicaiton.getName();
        this.description = applicaiton.getDescription();
        this.applicationKey = applicaiton.getId();
        this.applicationType = applicaiton.getType();
        this.repositoryType = applicaiton.getRepositoryType();
        this.setBranchCount(applicaiton.getBranchCount());
        this.setProduction(applicaiton.isProduction());
    }

    public String getOwnerUserName() {
        return ownerUserName;
    }

    public void setOwnerUserName(String ownerUserName) {
        this.ownerUserName = ownerUserName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getApplicationKey() {
        return applicationKey;
    }

    public void setApplicationKey(String applicationKey) {
        this.applicationKey = applicationKey;
    }

    public String[] getVersions() {
        return versions;
    }

    public void setVersions(String[] versions) {
        this.versions = versions;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public String getRepositoryType() {
        return repositoryType;
    }

    public void setRepositoryType(String repositoryType) {
        this.repositoryType = repositoryType;
    }

    @Override
    public String toString() {
        return " [Application key : " + getApplicationKey() + "] ";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        ApplicationInfoBean appBeanObject = (ApplicationInfoBean) obj;
        return this.getApplicationKey() != null &&
               this.getApplicationKey().equals(appBeanObject.getApplicationKey());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result =
                prime * result +
                ((this.applicationKey == null) ? 0 : this.applicationKey.hashCode());

        return result;
    }

	/**
	 * @return the branchCount
	 */
    public int getBranchCount() {
	    return branchCount;
    }

	/**
	 * @param branchCount the branchCount to set
	 */
    public void setBranchCount(int branchCount) {
	    this.branchCount = branchCount;
    }

	/**
	 * @return the isProduction
	 */
    public boolean isProduction() {
	    return isProduction;
    }

	/**
	 * @param isProduction the isProduction to set
	 */
    public void setProduction(boolean isProduction) {
	    this.isProduction = isProduction;
    }

}
