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

package org.wso2.carbon.appfactory.eventing.jms;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.eventing.AppFactoryEventException;
import org.wso2.carbon.appfactory.eventing.utils.Util;

public class SubscriptionManager {
    Log log = LogFactory.getLog(SubscriptionManager.class);
    private static SubscriptionManager subscriptionManager;

    private Map<String, org.wso2.carbon.appfactory.eventing.jms.Subscriber> subscriberMap;
    private Map<String, EventConsumer> eventConsumers =
                                                        new ConcurrentHashMap<String, EventConsumer>();

    private SubscriptionManager() {
        subscriberMap = new ConcurrentHashMap<String, org.wso2.carbon.appfactory.eventing.jms.Subscriber>();
    }

    public static SubscriptionManager getInstance() {
        if (subscriptionManager == null) {
            synchronized (SubscriptionManager.class) {
                if (subscriptionManager == null) {
                    subscriptionManager = new SubscriptionManager();
                }
            }
        }
        return subscriptionManager;
    }

    public void startSubscription(String topic, String uid) throws AppFactoryEventException {

        EventConsumer consumer = eventConsumers.get(uid);
        if (consumer == null) {
            consumer = new EventConsumer(uid);
            eventConsumers.put(uid, consumer);
            consumer.startSubscription(topic);

            if (log.isDebugEnabled()) {
                log.debug("New subscription is started for topic:" + topic +
                        " and for user:" + uid);
            }
        }

    }

    public boolean isSubscriptionStarted(String topic, String subscriberId) {
        return subscriberMap.get(Util.getUniqueSubscriptionId(topic, subscriberId)) != null;
    }

    public void stopAllSubscriptions(String uid) throws AppFactoryEventException {
		if (log.isDebugEnabled()) {
            log.debug("Stopping All subscriptions for  = " + uid);
        }
        EventConsumer consumer = eventConsumers.get(uid);

        if (consumer != null) {
            consumer.stopAllSubscriptions();
        } else {
            if (log.isDebugEnabled()) {
                log.debug("unable to find event consumer by uid: [" + uid +
                        "] therefore, unable to stop all subscriptions.");
            }

        }
    }

    public void stopSubscription(String topic, String uid) throws AppFactoryEventException {

        EventConsumer consumer = eventConsumers.get(uid);

        if (consumer != null) {
            consumer.stopSubscription(topic);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("unable to find event consumer by uid: [" + uid +
                        "] therefore, unable to stop subscription for topic : " + topic);
            }
        }

    }


}
