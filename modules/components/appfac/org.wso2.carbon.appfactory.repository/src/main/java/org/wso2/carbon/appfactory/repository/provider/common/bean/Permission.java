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

/**
 *
 *
 */
public class Permission implements java.io.Serializable {

    private boolean _groupPermission;
    private PermissionType _type;
    private java.lang.String _name;

    public boolean getGroupPermission() {
        return this._groupPermission;
    }

    public void setGroupPermission(boolean _groupPermission) {
        this._groupPermission = _groupPermission;
    }

    public PermissionType getType() {
        return this._type;
    }

    public void setType(PermissionType _type) {
        this._type = _type;
    }

    public java.lang.String getName() {
        return this._name;
    }

    public void setName(java.lang.String _name) {
        this._name = _name;
    }
}
