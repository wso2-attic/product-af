/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appfactory.eventing.social;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.NativeObject;
import org.wso2.carbon.appfactory.eventing.Event;


public class SocialActivityBuilder {
    Log log = LogFactory.getLog(SocialActivityBuilder.class);
    private Event event;

    public SocialActivityBuilder(Event event) {
        this.event = event;
    }
    /*
    {"verb":"post",
    "object":{"objectType":"review","content":"new comment.","rating":2},
    "target":{"id":"ebook:c5db8856-cd30-4d5f-8678-f9db793578c7"},
    "actor":{"id":"man@man.com","objectType":"person"},
    "id":"04fb5d8b-b4a1-48b9-8469-789b026baddb"}
    */
    public NativeObject buildActivity() {
        NativeObject nativeObject = new NativeObject();

        // set published element
        nativeObject.put("published", nativeObject, this.event.getTimestamp());

        // set actor object
        NativeObject actor = new NativeObject();
        actor.put("id", actor, event.getSender());
        actor.put("objectType", actor, "person");
        nativeObject.put("actor", nativeObject, actor);

        // set payload object
        NativeObject payload = new NativeObject();
        payload.put("title", payload, this.event.getMessageTitle());
        payload.put("content", payload, this.event.getMessageBody());
        payload.put("category", payload, this.event.getCategory());
        nativeObject.put("object", nativeObject, payload);

        // set verb
        nativeObject.put("verb", nativeObject, "post");

        //set target
        NativeObject target = new NativeObject();
        target.put("id", target, this.event.getTarget());
        nativeObject.put("target", nativeObject, target);

        //set properties
        NativeObject properties = new NativeObject();
        properties.put("type", properties, this.event.getType());
        properties.put("state", properties, this.event.getState());
        properties.put("correlationKey", properties, this.event.getCorrelationKey());
        nativeObject.put("properties", nativeObject, properties);

        if(log.isDebugEnabled()){
            log.debug("Event to be published to social wall: "+event.toString());
        }
        return nativeObject;
    }


}
