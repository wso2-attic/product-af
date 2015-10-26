/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.appfactory.lifecycle.mgt.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Details about stages in lifecycle configuration
 */
public class StageBean {
    private String stageName;
    //List of ChecklistItems objects from ChecklistItem class
    private List<CheckListItemBean> checkListItems;

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public List<CheckListItemBean> getCheckListItems() {
        if (checkListItems == null) {
            return Collections.emptyList();
        }
        return this.checkListItems;
    }

    public void setCheckListItems(List<CheckListItemBean> checkListItems) {
        if (this.checkListItems == null) {
            this.checkListItems = new ArrayList<CheckListItemBean>();
        }
        this.checkListItems.addAll(checkListItems);
    }

}

