/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.appfactory.git;

import com.gitblit.models.UserModel;

import java.util.Calendar;
import java.util.Date;

/**
 * UserModel holder for keeping the only ones that has UserModel with non expired backend cookie
 */
public class GitBlitUserModelHolder {
    private UserModel userModel;
    private Date createdDate;

    public GitBlitUserModelHolder(UserModel userModel) {
        setUserModel(userModel);
    }

    public UserModel getUserModel() {
        //TODO:make the expiration time configurable
        if (Calendar.getInstance().getTime().getTime() - getCreatedDate().getTime() > 60000 * 5) {
            return null;
        }
        return userModel;
    }

    public void setUserModel(UserModel userModel) {
        setCreatedDate(Calendar.getInstance().getTime());
        this.userModel = userModel;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
}
