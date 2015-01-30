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


public class IssueTrackerEventBuilderUtil {


    private static Log log = LogFactory.getLog(IssueTrackerEventBuilderUtil.class);

    /**
     *
     * @param appKey application key
     * @param issueReportedBy user who reported the issue
     * @param title notification title
     * @param description notification description
     * @param category notification category whether it is SUCCESS or ERROR
     * @return event that will be triggered when an issue is created
     */
    public static Event issueCreatedEvent(String appKey, String issueReportedBy, String title, String description, String category) {

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
        event.setSender(issueReportedBy);
        event.setCategory(eventStatus);
        event.setTarget(appKey);
        event.setMessageTitle(title);
        event.setMessageBody(description);
        return event;
    }

}
