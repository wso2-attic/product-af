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
import org.wso2.carbon.appfactory.eventing.AppFactoryEventException;
import org.wso2.carbon.appfactory.eventing.Event;
import org.wso2.carbon.appfactory.eventing.Event.Category;
import org.wso2.carbon.appfactory.eventing.EventNotifier;
import org.wso2.carbon.appfactory.eventing.utils.Util;


public class AppCreationEventBuilderUtil {


    private static Log log = LogFactory.getLog(AppCreationEventBuilderUtil.class);


    //Application creation started event
    public static void invokeAppCreationStartedEvent(String appId, String createdBy, String title, String description, String status) {
        Category eventStatus;
        if (status.equals("INFO")) {
            eventStatus = Category.INFO;
        } else {
            eventStatus = Category.ERROR;
        }
        try {
            EventNotifier.getInstance().notify(AppCreationEventBuilderUtil.buildAppCreationStartEvent(appId, createdBy, title, description, eventStatus));
        } catch (AppFactoryEventException e) {
            log.error("Failed to notify application creation event.", e);
        }
    }

    /**
     * @param appId       application key
     * @param createdBy   username of the user who created the application
     * @param title       notification title
     * @param description notification description
     * @param status      notification category whether it is SUCCESS or ERROR
     * @return event that will be triggered when the application creation is started
     */
    public static Event buildAppCreationStartEvent(String appId, String createdBy, String title, String description, Category status) {
        Event event = new Event();
        Event.EventDispatchType[] eventDispatchTypes;

        if (status == Category.INFO) {
            eventDispatchTypes =
                    new Event.EventDispatchType[]{Event.EventDispatchType.SOCIAL_ACTIVITY};
        } else {

            eventDispatchTypes =
                    new Event.EventDispatchType[]{
                            Event.EventDispatchType.SOCIAL_ACTIVITY,
                            Event.EventDispatchType.GUARANTEED_DELIVERY};
        }

        event.setEventDispatchTypes(eventDispatchTypes);
        event.setSender(createdBy);
        event.setCategory(status);
        event.setTarget(createdBy);
        event.setMessageBody(description);
        event.setMessageTitle(title);
        return event;
    }


    /**
     * @param title       application key
     * @param description notification description
     * @param category    notification category whether it is SUCCESS or ERROR
     * @return event that will be triggered during intermediatries of application creation such as after the
     * jenkins space is created, git repo is created, cloud env.s are created etc.
     */
    public static Event buildApplicationCreationEvent(String title, String description,
                                                      Category category) {
        Event event = new Event();
        if (category == (Category.INFO)) {
            Event.EventDispatchType[] eventDispatchTypes =
                    {Event.EventDispatchType.SOCIAL_ACTIVITY};
            event.setEventDispatchTypes(eventDispatchTypes);
        } else {
            Event.EventDispatchType[] eventDispatchTypes =
                    {
                            Event.EventDispatchType.GUARANTEED_DELIVERY,
                            Event.EventDispatchType.SOCIAL_ACTIVITY,
                            Event.EventDispatchType.EMAIL};
            event.setEventDispatchTypes(eventDispatchTypes);
        }
        String sender = Util.getSender();
        event.setSender(sender);
        event.setCategory(category);
        event.setTarget(sender);
        // event.setTarget("APPLICATION_CREATION");
        event.setMessageTitle(title);
        event.setMessageBody(description);
        return event;
    }


    /**
     * @param appId       application key
     * @param title       notification title
     * @param description notification description
     * @return event that will be triggered when the application is created
     */
    public static Event buildAppCreationStatusEventToAppWall(String appId, String title, String description) {
        Event event = new Event();

        Event.EventDispatchType[] eventDispatchTypes =
                {
                        Event.EventDispatchType.SOCIAL_ACTIVITY
                };
        event.setEventDispatchTypes(eventDispatchTypes);
        String sender = Util.getSender();
        event.setSender(sender);
        event.setCategory(Category.INFO);
        event.setTarget(appId);
        event.setMessageTitle(title);
        event.setMessageBody(description);
        return event;
    }


    /**
     * @param appId       application key
     * @param title       notification title
     * @param description notification description
     * @param username    username of the user who triggered the app creation
     * @return event that is triggered when the application creation is successfully completed
     */
    public static Event buildApplicationCreationCompletedEvent(String appId, String title, String description, String username) {
        Event event = new Event();

        Event.EventDispatchType[] eventDispatchTypes =
                {
                        Event.EventDispatchType.SOCIAL_ACTIVITY
                };
        event.setEventDispatchTypes(eventDispatchTypes);
        String sender = Util.getSender();
        event.setSender(sender);
        event.setCategory(Category.INFO);
        event.setTarget(username);
        event.setMessageTitle(title);
        event.setMessageBody(description);
        return event;
    }

    // This creates an event with a target value as appId
    public static Event buildApplicationCreationEvent(String appId, String title,
                                                      String description, Category category) {
        Event event = new Event();
        Event.EventDispatchType[] eventDispatchTypes = {Event.EventDispatchType.SOCIAL_ACTIVITY};
        event.setEventDispatchTypes(eventDispatchTypes);
        String sender = Util.getSender();
        event.setSender(sender);
        event.setCategory(category);
        event.setTarget(appId);
        event.setMessageTitle(title);
        event.setMessageBody(description);
        return event;
    }


    /**
     *
     * @param username application key
     * @param title notification title
     * @param description notification description
     * @param deletedBy username of the user who deleted the app
     * @param category notification category whether it is SUCCESS or ERROR
     * @return the event that will be triggered when an application is deleted
     */
    public static Event buildApplicationDeletionEventForUser(String username, String title, String description, String deletedBy, Category category) {
        Event event = new Event();
        Event.EventDispatchType[] eventDispatchTypes =
                new Event.EventDispatchType[]{
                        Event.EventDispatchType.SOCIAL_ACTIVITY
                };
        event.setEventDispatchTypes(eventDispatchTypes);
        event.setSender(deletedBy);
        event.setCategory(Category.INFO);
        event.setTarget(username);
        event.setMessageTitle(title);
        event.setMessageBody(description);

        return event;
    }

    /**
     *
     * @param applicationId application key
     * @param title notification title
     * @param description notification description
     * @param deletedBy username of the user who deleted the app
     * @param category notification category whether it is SUCCESS or ERROR
     * @return the event that will be triggered when an application is deleted
     */
    public static Event buildApplicationDeletionEventForApplication(String applicationId, String title, String description, String deletedBy, Category category) {
        Event event = new Event();
        Event.EventDispatchType[] eventDispatchTypes =
                new Event.EventDispatchType[]{
                        Event.EventDispatchType.SOCIAL_ACTIVITY
                };
        event.setEventDispatchTypes(eventDispatchTypes);
        event.setSender(deletedBy);
        event.setCategory(Category.INFO);
        event.setTarget(applicationId);
        event.setMessageTitle(title);
        event.setMessageBody(description);
        event.setState(Event.State.DELETE);
        return event;
    }

}
