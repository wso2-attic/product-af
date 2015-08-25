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

package bean;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Details about stages in lifecycle configuration
 */
@XmlRootElement(name = "Stage")
@XmlAccessorType(XmlAccessType.FIELD)
public class StageBean {
    private String stageName;
    //List of ChecklistItems objects from ChecklistItem class
    private Set<CheckListItemBean> checkListItems;

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public Set<CheckListItemBean> getCheckListItems() {
        if (checkListItems == null) {
            return Collections.emptySet();
        }
        return this.checkListItems;
    }

    public void setCheckListItems(Set<CheckListItemBean> checkListItems) {
        if (this.checkListItems == null) {
            this.checkListItems = new HashSet<CheckListItemBean>();
        }
        this.checkListItems.addAll(checkListItems);
    }

}

