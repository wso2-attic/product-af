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
import org.wso2.carbon.appfactory.eventing.utils.EventingConstants;
import org.wso2.carbon.appfactory.eventing.utils.Util;

public class RepoCreationEventBuilderUtil {


    private static Log log = LogFactory.getLog(RepoCreationEventBuilderUtil.class);

    /**
     *
     * @param appId application key
     * @param title notification title
     * @param description notification description
     * @param category notification category whether it is SUCCESS or ERROR
     * @param correlationKey correlation key for build start event
     * @return event that is triggered when a branch is creation is started
     */
    public static Event buildBranchCreationStartEvent(String appId, String title, String description,
                                                         Category category, String correlationKey) {
        Event event = new Event();
        Event.EventDispatchType[] eventDispatchTypes;
        String sender = Util.getSender();
        if (category == Category.INFO) {
            eventDispatchTypes =
                    new Event.EventDispatchType[]{Event.EventDispatchType.SOCIAL_ACTIVITY};
            event.setTarget(appId);
        } else {
            eventDispatchTypes =
                    new Event.EventDispatchType[]{
                            Event.EventDispatchType.SOCIAL_ACTIVITY,
                            Event.EventDispatchType.GUARANTEED_DELIVERY};
            event.setTarget(sender);
        }
        event.setEventDispatchTypes(eventDispatchTypes);
        event.setSender(sender);
        event.setMessageBody(description);
        event.setCategory(category);
        event.setMessageTitle(title);
        event.setCorrelationKey(correlationKey);
        return event;
    }

    /**
     *
     * @param appId application key
     * @param title notification title
     * @param description notification description
     * @param category notification category whether it is SUCCESS or ERROR
     * @param correlationKey correlation key for build start event
     * @return event that is triggered when a branch is creation is finished
     */
    public static Event buildBranchCreationCompleteEvent(String appId, String title, String description,
                                                 Category category, String correlationKey) {
        Event event = new Event();
        Event.EventDispatchType[] eventDispatchTypes;
        String sender = Util.getSender();
        if (category == Category.INFO) {
            eventDispatchTypes =
                    new Event.EventDispatchType[]{Event.EventDispatchType.SOCIAL_ACTIVITY};
            event.setTarget(appId);
        } else {
            eventDispatchTypes =
                    new Event.EventDispatchType[]{
                            Event.EventDispatchType.SOCIAL_ACTIVITY,
                            Event.EventDispatchType.GUARANTEED_DELIVERY};
            event.setTarget(sender);
        }
        event.setEventDispatchTypes(eventDispatchTypes);
        event.setSender(sender);
        event.setMessageBody(description);
        event.setCategory(category);
        event.setMessageTitle(title);
        event.setCorrelationKey(correlationKey);
        return event;
    }


    /**
     *
     * @param appId application key
     * @param title notification title
     * @param description notification description
     * @param category notification category whether it is SUCCESS or ERROR
     * @param forkBranchUser user who created the branch of the fork
     * @return event that will be triggered when an available branch is getting added to an already created forked repo
     */
    public static Event buildBranchForkingEvent(String appId, String title, String description,
                                                Category category, String forkBranchUser) {
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
        String sender = Util.getSender(forkBranchUser);
        event.setSender(sender);
        event.setCategory(category);
        event.setTarget(appId + EventingConstants.FORK_USER_CONTEXT + sender);
        event.setMessageTitle(title);
        event.setMessageBody(description);
        return event;
    }


    /**
     *
     * @param appId application key
     * @param title notification title
     * @param description notification description
     * @param category notification category whether it is SUCCESS or ERROR
     * @return event that will be triggered when a repo is forked by a user
     */
    public static Event buildCreateForkRepoEvent(String appId, String title, String description,  Category category) {
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
        event.setMessageTitle(title);
        event.setMessageBody(description);
        return event;
    }


}
