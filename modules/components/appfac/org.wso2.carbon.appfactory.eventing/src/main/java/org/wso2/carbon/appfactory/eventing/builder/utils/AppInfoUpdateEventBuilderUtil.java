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

public class AppInfoUpdateEventBuilderUtil {

    private static Log log = LogFactory.getLog(AppInfoUpdateEventBuilderUtil.class);

    public static void createDescriptionUpdateCompletedEvent(String appId, String updatedBy, String title, String description, String status){
        Category eventStatus;
        if (status.equals("INFO")) {
            eventStatus = Category.INFO;
        } else {
            eventStatus = Category.ERROR;
        }
        try {
            EventNotifier.getInstance().notify(AppInfoUpdateEventBuilderUtil.descriptionUpdateCompletedEvent(appId, updatedBy, title, description, eventStatus));
        } catch (AppFactoryEventException e) {
            log.error("Failed to notify description updated completed event.", e);
        }
    }

    /**
     *
     * @param appId application key
     * @param updatedBy username of the user who updates the app description
     * @param title notification title
     * @param description notification description
     * @param category event status whether it is a SUCCESS or ERROR message
     * @return event that is triggered when the app description update is completed
     */
    public static Event descriptionUpdateCompletedEvent(String appId, String updatedBy, String title, String description, Category category) {
        Event event = new Event();
        Event.EventDispatchType[] eventDispatchTypes =
                new Event.EventDispatchType[]{
                        Event.EventDispatchType.SOCIAL_ACTIVITY
                };
        event.setEventDispatchTypes(eventDispatchTypes);
        event.setSender(updatedBy);
        event.setCategory(category);
        event.setTarget(appId);
        event.setMessageTitle(title);
        event.setMessageBody(description);
        return event;
    }

    /**
     * When the custom URL or the custom domain mapping is completed, this event will be triggered.
     * @param appKey applicationKey
     * @param title notification title
     * @param description notification description,
     * @param status event status whether it is a SUCCESS or ERROR message
     */
    public static void createDomainMappingCompletedEvent(String appKey, String title, String description, String status) {
        Category eventStatus;
        if (status.equals("INFO")) {
            eventStatus = Category.INFO;
        } else {
            eventStatus = Category.ERROR;
        }
        try {
            EventNotifier.getInstance().notify(AppInfoUpdateEventBuilderUtil.createDomainMappingCompletedEvent(appKey, title, description, eventStatus));
        } catch (AppFactoryEventException e) {
            log.error("Failed to notify domain mapping completed event.", e);
        }
    }


    /**
     * When the custom URL or the custom domain mapping is completed, this event will be triggered.
     * @param appKey applicationKey
     * @param title notification title
     * @param description notification description,
     * @param status event status whether it is a SUCCESS or ERROR message
     */
    //Event that will be triggered when the domain mapping is completed with the success or failure message
    public static Event createDomainMappingCompletedEvent(String appKey, String title, String description, Category status) {
        Event event = new Event();
        Event.EventDispatchType[] eventDispatchTypes =
                new Event.EventDispatchType[]{
                        Event.EventDispatchType.SOCIAL_ACTIVITY
                };
        event.setEventDispatchTypes(eventDispatchTypes);
        String sender = Util.getSender();
        event.setSender(sender);
        event.setCategory(status);
        event.setTarget(appKey);
        event.setMessageTitle(title);
        event.setMessageBody(description);

        return event;
    }

}
