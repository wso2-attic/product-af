/*
 * Copyright 2011-2012 WSO2, Inc. (http://wso2.com)
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

package util;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@XmlRootElement(name = "Stage")
@XmlAccessorType(XmlAccessType.FIELD)
public class Stage {
    private String stageName;
    private List<CheckListItem> items;

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public List<CheckListItem> getItmes() {
        if(items == null) {
            return Collections.emptyList();
        }
        return this.items;
    }

    public void setItmes(List<CheckListItem> itmes) {
        if(this.items == null) {
            this.items = new ArrayList<CheckListItem>();
        }
        this.items.addAll(itmes);
    }

}

