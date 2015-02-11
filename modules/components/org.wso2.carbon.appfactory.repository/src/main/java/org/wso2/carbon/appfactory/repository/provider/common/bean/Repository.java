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

package org.wso2.carbon.appfactory.repository.provider.common.bean;

import java.io.Serializable;
import java.util.List;

/**
 *
 *
 */
public class Repository implements Serializable {

    private Long lastModified;
    private String url;
    private String id;
    private String description;
    private List<Permission> permissions;
    private String contact;
    private String type;
    private boolean publicReadable;
    private String name;
    private Long creationDate;


    public java.lang.Long getLastModified() {
        return this.lastModified;
    }

    public void setLastModified(java.lang.Long _lastModified) {
        this.lastModified = _lastModified;
    }

    public java.lang.String getUrl() {
        return this.url;
    }

    public void setUrl(java.lang.String _url) {
        this.url = _url;
    }

    public java.lang.String getId() {
        return this.id;
    }

    public void setId(java.lang.String _id) {
        this.id = _id;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String _description) {
        this.description = _description;
    }

    public java.util.List<Permission> getPermissions() {
        return this.permissions;
    }

    public void setPermissions(java.util.List<Permission> _permissions) {
        this.permissions = _permissions;
    }

    public java.lang.String getContact() {
        return this.contact;
    }

    public void setContact(java.lang.String _contact) {
        this.contact = _contact;
    }

    public java.lang.String getType() {
        return this.type;
    }

    public void setType(java.lang.String _type) {
        this.type = _type;
    }

    public boolean getPublicReadable() {
        return this.publicReadable;
    }

    public void setPublicReadable(boolean _publicReadable) {
        this.publicReadable = _publicReadable;
    }

    public java.lang.String getName() {
        return this.name;
    }

    public void setName(java.lang.String _name) {
        this.name = _name;
    }

    public java.lang.Long getCreationDate() {
        return this.creationDate;
    }

    public void setCreationDate(java.lang.Long _creationDate) {
        this.creationDate = _creationDate;
    }

}