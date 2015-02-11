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

package org.wso2.carbon.appfactory.eventing;

public class Event {
    public static enum EventDispatchType {
        EMAIL, SOCIAL_ACTIVITY, GUARANTEED_DELIVERY
    }

    public static enum Category {
        INFO, ERROR, WARN, ACTION
    }

    public static enum State {
        START, COMPLETE, DELETE
    }

    private EventDispatchType[] eventDispatchTypes;
    private Category category;
    private long timestamp = System.currentTimeMillis();
    private String messageTitle;
    private String messageBody;
    private String sender;
    private String target;
    private String type;
    private State state;
    private String correlationKey;

    public EventDispatchType[] getEventDispatchTypes() {
        return eventDispatchTypes;
    }

    public void setEventDispatchTypes(EventDispatchType[] eventDispatchTypes) {
        this.eventDispatchTypes = eventDispatchTypes;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getMessageTitle() {
        return messageTitle;
    }

    public void setMessageTitle(String messageTitle) {
        this.messageTitle = messageTitle;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCorrelationKey() {
        return correlationKey;
    }

    public void setCorrelationKey(String correlationKey) {
        this.correlationKey = correlationKey;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}
