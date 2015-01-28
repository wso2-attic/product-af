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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.eventing.email.NotificationEmailSender;
import org.wso2.carbon.appfactory.eventing.jms.TopicPublisher;
import org.wso2.carbon.appfactory.eventing.social.SocialActivityEventDispatcher;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.HashMap;
import java.util.Map;

public class EventNotifier {

    public static final String APPFACTORY_EVENT_NOTIFIER_THREAD = "APPFACTORY_EVENT_NOTIFIER_THREAD";
    private static final Log log = LogFactory.getLog(EventNotifier.class);

    private static EventNotifier eventNotifier = new EventNotifier();
    private Map<Event.EventDispatchType, EventDispatcher> dispatcherMap = new HashMap<Event.EventDispatchType, EventDispatcher>();

    private EventNotifier() {
        dispatcherMap.put(Event.EventDispatchType.SOCIAL_ACTIVITY, new SocialActivityEventDispatcher());
        dispatcherMap.put(Event.EventDispatchType.GUARANTEED_DELIVERY, new TopicPublisher());
        dispatcherMap.put(Event.EventDispatchType.EMAIL, new NotificationEmailSender());
    }

    public static EventNotifier getInstance() {
        return eventNotifier;
    }

    /**
     * Notifying the received events to the related Event Dispatcher
     * @param event
     * @throws AppFactoryEventException
     */
    public void notify(final Event event) throws AppFactoryEventException {
        final int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        final String userName = inferUserName(event);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Event.EventDispatchType[] eventDispatchTypes = event.getEventDispatchTypes();
                if (eventDispatchTypes == null) {
                    log.error("Event dispatch type is not defined in received event.");
                    return;
                }

                try {
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext privilegedCarbonContext =
                            PrivilegedCarbonContext.getThreadLocalCarbonContext();
                    privilegedCarbonContext.setTenantId(tenantId);
                    privilegedCarbonContext.getTenantDomain(true);
                    privilegedCarbonContext.setUsername(userName);

                    for (Event.EventDispatchType eventDispatchType : eventDispatchTypes) {
                        if (eventDispatchType != Event.EventDispatchType.SOCIAL_ACTIVITY) {
                            EventDispatcher eventDispatcher = dispatcherMap.get(eventDispatchType);
                            if (eventDispatcher != null) {
                                try {
                                    eventDispatcher.dispatchEvent(event);
                                } catch (AppFactoryEventException e) {
                                    //todo: retry logic
                                    log.error("Failed to dispatch event with error:" + e.getMessage(), e);
                                }
                            } else {
                                log.error("Failed to find event dispatcher for dispatch type:" + eventDispatchType);
                            }
                        }
                    }
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }

                for (Event.EventDispatchType eventDispatchType : eventDispatchTypes) {
                    if (eventDispatchType == Event.EventDispatchType.SOCIAL_ACTIVITY) {
                        EventDispatcher eventDispatcher = dispatcherMap.get(eventDispatchType);
                        try {
                            eventDispatcher.dispatchEvent(event);
                        } catch (AppFactoryEventException e) {
                            log.error("Failed to dispatch event with error:" + e.getMessage(), e);
                        }
                    }
                }

            }
        });
        thread.setName(APPFACTORY_EVENT_NOTIFIER_THREAD);
        thread.start();
    }
    
	private String inferUserName(Event event) {

		String userName = CarbonContext.getThreadLocalCarbonContext()
				.getUsername();
		if ((userName == null || StringUtils.isBlank(userName))
				&& StringUtils.isNotBlank(event.getSender())) {
			// if user name is not in carbon context this means event was
			// originated from outside (e.g. Build failed)
			String[] splits = event.getSender().split("@");
			if (splits.length > 0) {
				userName = splits[0];
			}
		}

		return userName;
	}    
    
}
