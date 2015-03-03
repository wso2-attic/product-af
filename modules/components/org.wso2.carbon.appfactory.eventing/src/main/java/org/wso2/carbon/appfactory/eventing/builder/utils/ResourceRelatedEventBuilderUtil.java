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

package org.wso2.carbon.appfactory.eventing.builder.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.eventing.Event;
import org.wso2.carbon.appfactory.eventing.Event.Category;

public class ResourceRelatedEventBuilderUtil {


    private static Log log = LogFactory.getLog(ResourceRelatedEventBuilderUtil.class);



    /**
     *
     * @param appKey application key
     * @param updatedBy  user who updated database privilege
     * @param title notification title
     * @param description notification description
     * @param correlationKey in the format of ApplicationKey-stageName-template-templateName
     * @param category notification category whether it is SUCCESS or ERROR
     * @return event that will be triggered when a database user privilege is modification started
     */
    public static Event resourceUpdateStartEvent(String appKey, String updatedBy, String title, String description, String correlationKey, String category) {

        Category eventStatus;
        if ("INFO".equals(category)) {
            eventStatus = Category.INFO;
        } else {
            eventStatus = Category.ERROR;
        }

        Event event = new Event();
        Event.EventDispatchType[] eventDispatchTypes =
                new Event.EventDispatchType[]{
                        Event.EventDispatchType.SOCIAL_ACTIVITY
                };
        event.setEventDispatchTypes(eventDispatchTypes);
        event.setSender(updatedBy);
        event.setCategory(eventStatus);
        event.setTarget(appKey);
        event.setMessageTitle(title);
        event.setMessageBody(description);
        event.setState(Event.State.START);
        event.setCorrelationKey(correlationKey);
        return event;
    }


    /**
     *
     * @param appKey application key
     * @param updatedBy  user who updated database privilege
     * @param title notification title
     * @param description notification description
     * @param correlationKey in the format of ApplicationKey-stageName-template-templateName
     * @param category notification category whether it is SUCCESS or ERROR
     * @return event that will be triggered when a database user privilege is modified
     */
    public static Event resourceUpdateCompletionEvent(String appKey, String updatedBy, String title, String description, String correlationKey, String category) {

        Category eventStatus;
        if ("INFO".equals(category)) {
            eventStatus = Category.INFO;
        } else {
            eventStatus = Category.ERROR;
        }

        Event event = new Event();
        Event.EventDispatchType[] eventDispatchTypes =
                new Event.EventDispatchType[]{
                        Event.EventDispatchType.SOCIAL_ACTIVITY
                };
        event.setEventDispatchTypes(eventDispatchTypes);
        event.setSender(updatedBy);
        event.setCategory(eventStatus);
        event.setTarget(appKey);
        event.setMessageTitle(title);
        event.setMessageBody(description);
        event.setState(Event.State.COMPLETE);
        event.setCorrelationKey(correlationKey);
        return event;
    }


    /**
     *
     * @param appKey application key
     * @param createdBy  user who created property
     * @param title notification title
     * @param description notification description
     * @param correlationKey in the format of ApplicationKey-stageName-property-propertyname
     * @param category notification category whether it is SUCCESS or ERROR
     * @return event that will be triggered when a property creation started
     */
    public static Event resourceCreationStartedEvent(String appKey, String createdBy, String title, String description, String correlationKey, String category) {

        Category eventStatus;
        if ("INFO".equals(category)) {
            eventStatus = Category.INFO;
        } else {
            eventStatus = Category.ERROR;
        }

        Event event = new Event();
        Event.EventDispatchType[] eventDispatchTypes =
                new Event.EventDispatchType[]{
                        Event.EventDispatchType.SOCIAL_ACTIVITY
                };
        event.setEventDispatchTypes(eventDispatchTypes);
        event.setSender(createdBy);
        event.setCategory(eventStatus);
        event.setTarget(appKey);
        event.setMessageTitle(title);
        event.setMessageBody(description);
        event.setState(Event.State.START);
        event.setCorrelationKey(correlationKey);
        return event;
    }


    /**
     *
     * @param appKey application key
     * @param createdBy  user who created property
     * @param title notification title
     * @param description notification description
     * @param correlationKey in the format of ApplicationKey-stageName-property-propertyname
     * @param category notification category whether it is SUCCESS or ERROR
     * @return event that will be triggered when a property creation started
     */
    public static Event resourceCreationCompletedEvent(String appKey, String createdBy, String title, String description, String correlationKey, String category) {

        Category eventStatus;
        if ("INFO".equals(category)) {
            eventStatus = Category.INFO;
        } else {
            eventStatus = Category.ERROR;
        }

        Event event = new Event();
        Event.EventDispatchType[] eventDispatchTypes =
                new Event.EventDispatchType[]{
                        Event.EventDispatchType.SOCIAL_ACTIVITY
                };
        event.setEventDispatchTypes(eventDispatchTypes);
        event.setSender(createdBy);
        event.setCategory(eventStatus);
        event.setTarget(appKey);
        event.setMessageTitle(title);
        event.setMessageBody(description);
        event.setState(Event.State.COMPLETE);
        event.setCorrelationKey(correlationKey);
        return event;
    }


    /**
     *
     * @param appKey application key
     * @param deletedBy  user who deleted property
     * @param title notification title
     * @param description notification description
     * @param correlationKey in the format of ApplicationKey-stageName-property-propertyname
     * @param category notification category whether it is SUCCESS or ERROR
     * @return event that will be triggered when a property deletion started
     */
    public static Event resourceDeletionStartedEvent(String appKey, String deletedBy, String title, String description, String correlationKey, String category) {

        Category eventStatus;
        if ("INFO".equals(category)) {
            eventStatus = Category.INFO;
        } else {
            eventStatus = Category.ERROR;
        }

        Event event = new Event();
        Event.EventDispatchType[] eventDispatchTypes =
                new Event.EventDispatchType[]{
                        Event.EventDispatchType.SOCIAL_ACTIVITY
                };
        event.setEventDispatchTypes(eventDispatchTypes);
        event.setSender(deletedBy);
        event.setCategory(eventStatus);
        event.setTarget(appKey);
        event.setMessageTitle(title);
        event.setMessageBody(description);
        event.setState(Event.State.START);
        event.setCorrelationKey(correlationKey);
        return event;
    }

    /**
     *
     * @param appKey application key
     * @param deletedBy  user who deleted property
     * @param title notification title
     * @param description notification description
     * @param correlationKey in the format of ApplicationKey-stageName-property-propertyname
     * @param category notification category whether it is SUCCESS or ERROR
     * @return event that will be triggered when a property deletion completed
     */
    public static Event resourceDeletionCompletedEvent(String appKey, String deletedBy, String title, String description, String correlationKey, String category) {

        Category eventStatus;
        if ("INFO".equals(category)) {
            eventStatus = Category.INFO;
        } else {
            eventStatus = Category.ERROR;
        }

        Event event = new Event();
        Event.EventDispatchType[] eventDispatchTypes =
                new Event.EventDispatchType[]{
                        Event.EventDispatchType.SOCIAL_ACTIVITY
                };
        event.setEventDispatchTypes(eventDispatchTypes);
        event.setSender(deletedBy);
        event.setCategory(eventStatus);
        event.setTarget(appKey);
        event.setMessageTitle(title);
        event.setMessageBody(description);
        event.setState(Event.State.COMPLETE);
        event.setCorrelationKey(correlationKey);
        return event;
    }

    /**
     *
     * @param appKey application key
     * @param attachedBy who attached the user
     * @param title notification title
     * @param description notification description
     * @param category notification category whether it is SUCCESS or ERROR
     * @return event that will be triggered when a new user is attached to a database
     */
    public static Event attachNewUserEvent(String appKey, String attachedBy, String title, String description, String category) {
        Category eventStatus;
        if ("INFO".equals(category)) {
            eventStatus = Category.INFO;
        } else {
            eventStatus = Category.ERROR;
        }

        Event event = new Event();
        Event.EventDispatchType[] eventDispatchTypes =
                new Event.EventDispatchType[]{
                        Event.EventDispatchType.SOCIAL_ACTIVITY
                };
        event.setEventDispatchTypes(eventDispatchTypes);
        event.setSender(attachedBy);
        event.setCategory(eventStatus);
        event.setTarget(appKey);
        event.setMessageTitle(title);
        event.setMessageBody(description);
        return event;
    }


    /**
     *
     * @param appKey application key
     * @param detachedBy who attached the user
     * @param title notification title
     * @param description notification description
     * @param category notification category whether it is SUCCESS or ERROR
     * @return event that will be triggered when a user is detached from the database
     */
    public static Event detachUserEvent(String appKey, String detachedBy, String title, String description, String category) {
        Category eventStatus;
        if ("INFO".equals(category)) {
            eventStatus = Category.INFO;
        } else {
            eventStatus = Category.ERROR;
        }

        Event event = new Event();
        Event.EventDispatchType[] eventDispatchTypes =
                new Event.EventDispatchType[]{
                        Event.EventDispatchType.SOCIAL_ACTIVITY
                };
        event.setEventDispatchTypes(eventDispatchTypes);
        event.setSender(detachedBy);
        event.setCategory(eventStatus);
        event.setTarget(appKey);
        event.setMessageTitle(title);
        event.setMessageBody(description);
        return event;
    }
}
