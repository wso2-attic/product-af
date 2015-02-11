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

class EventConsumer {
    Log log = LogFactory.getLog(EventConsumer.class);
    private String uid;

    Map<String, Subscriber> subscriptions = new ConcurrentHashMap<String, Subscriber>();

    public EventConsumer(String uid) {
        this.uid = uid;
    }

    public void startSubscription(String topic) throws AppFactoryEventException {
        String subscriptionId = Util.getUniqueSubscriptionId(topic, uid);
        if (log.isDebugEnabled()) {
            log.debug("Trying to create a subscription with Id:" + subscriptionId);
        }
        if (!isSubscriptionStarted(subscriptionId)) {
            Subscriber subscriber = new Subscriber(topic, subscriptionId, new AFMessageListener(subscriptionId));
            subscriber.subscribe();
            subscriptions.put(subscriptionId, subscriber);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Subscription with id:" + subscriptionId + " already created. No subscriber is created again.");
            }
        }

    }

    private boolean isSubscriptionStarted(String subscriptionId) {
        return subscriptions.containsKey(subscriptionId);
    }

    public void stopSubscription(String topic) throws AppFactoryEventException {
        String subscriptionId = Util.getUniqueSubscriptionId(topic, uid);
        if (isSubscriptionStarted(subscriptionId)) {
            subscriptions.get(subscriptionId).stopSubscription();
            subscriptions.remove(subscriptionId);
            MessageStore.getInstance().removeSubscriptionMap(subscriptionId);
        }
    }

    public void stopAllSubscriptions() throws AppFactoryEventException {

        for (String subscriptionId : subscriptions.keySet()) {

            if (isSubscriptionStarted(subscriptionId)) {
                subscriptions.get(subscriptionId).stopSubscription();
                subscriptions.remove(subscriptionId);
                MessageStore.getInstance().removeSubscriptionMap(subscriptionId);
            }

        }
    }

    public void removeSubscription(String topic) throws AppFactoryEventException {
        String subscriptionId = Util.getUniqueSubscriptionId(topic, uid);
        if (isSubscriptionStarted(subscriptionId)) {
            subscriptions.get(subscriptionId).stopSubscription();
            subscriptions.remove(subscriptionId);
        }
    }

}