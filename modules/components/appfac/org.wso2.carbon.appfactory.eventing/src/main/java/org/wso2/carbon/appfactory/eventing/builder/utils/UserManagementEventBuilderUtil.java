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
import org.wso2.carbon.appfactory.eventing.utils.Util;

public class UserManagementEventBuilderUtil {


    private static Log log = LogFactory.getLog(UserManagementEventBuilderUtil.class);


    /**
     *
     * @param appId application key
     * @param title notification title
     * @param description notification description
     * @param category notification category whether it is SUCCESS or ERROR
     * @return event that will be triggered when a user is added to the app
     */
    public static Event buildUserAdditionToAppEvent(String appId, String title, String description,
                                                    Category category) {
        return buildUserUpdateEvent(appId, title, description, category);
    }


    /**
     *
     * @param appId application key
     * @param title notification title
     * @param description notification description
     * @param category notification category whether it is SUCCESS or ERROR
     * @return event that will be triggered when a user is deleted from an app
     */
    public static Event buildUserDeletionFromAppEvent(String appId, String title, String description,
                                                      Category category) {
        return buildUserUpdateEvent(appId, title, description, category);

    }

    /**
     *
     * @param appId application key
     * @param title notification title
     * @param description notification description
     * @param category notification category whether it is SUCCESS or ERROR
     * @return event that will be triggered when an app user is updated
     */
    private static Event buildUserUpdateEvent(String appId, String title, String description,
                                              Category category) {
        Event event = new Event();
        Event.EventDispatchType[] eventDispatchTypes;
        if (category == Category.INFO) {
            eventDispatchTypes =
                    new Event.EventDispatchType[]{Event.EventDispatchType.SOCIAL_ACTIVITY};
        } else {
            eventDispatchTypes =
                    new Event.EventDispatchType[]{
                            Event.EventDispatchType.SOCIAL_ACTIVITY,
                            Event.EventDispatchType.GUARANTEED_DELIVERY};
        }
        event.setEventDispatchTypes(eventDispatchTypes);
        String sender = Util.getSender();
        event.setSender(sender);
        event.setCategory(category);
        event.setTarget(appId);
        event.setMessageBody(description);
        event.setMessageTitle(title);
        return event;
    }


}
