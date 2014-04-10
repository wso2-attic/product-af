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

package org.wso2.carbon.cloudmgt.users.beans;

import java.util.ArrayList;
import java.util.List;

import org.wso2.carbon.user.core.Permission;

public class RoleBean {
    private String roleName;
    private List<String> users;
    private List<Permission> authorizedPermissions;
    private List<Permission> deniedPermissions;

    public RoleBean(String roleName) {
        this.roleName = roleName;
        users = new ArrayList<String>();
        authorizedPermissions = new ArrayList<Permission>();
        deniedPermissions = new ArrayList<Permission>();
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public List<String> getUsers() {
        return users;
    }

    public List<Permission> getPermissions(boolean isAuthorizedPermissions) {
        if (isAuthorizedPermissions) {
            return authorizedPermissions;
        } else {
            return deniedPermissions;
        }
    }

    public void addUser(String user) {
        if (user != null && !"".equals(user.trim())) {
            users.add(user);
        }
    }

    public void addPermission(Permission permission, boolean isAuthorizedPermissions) {
        if (permission != null) {
            if (isAuthorizedPermissions) {
                authorizedPermissions.add(permission);
            } else {
                deniedPermissions.add(permission);
            }
        }
    }

}