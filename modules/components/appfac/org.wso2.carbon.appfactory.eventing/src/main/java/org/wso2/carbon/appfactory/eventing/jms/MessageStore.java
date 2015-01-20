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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.eventing.utils.Util;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageStore {
    Log log = LogFactory.getLog(MessageStore.class);
    private Map<String, Map<String, MapMessage>> messageMap;
    private static MessageStore messageStore = null;

    private MessageStore() {
        messageMap = new HashMap<String, Map<String, MapMessage>>();
    }

    public static MessageStore getInstance() {
        if (messageStore == null) {
            synchronized (MessageStore.class) {
                if (messageStore == null) {
                    messageStore = new MessageStore();
                }
            }
        }
        return messageStore;
    }


    public MapMessage[] getMessages(String topic, String subscriberId) {
        String subscriptionId = Util.getUniqueSubscriptionId(topic, subscriberId);
        Map<String, MapMessage> messages = messageMap.get(subscriptionId);
        if (messages != null) {
            List<MapMessage> messageList = new ArrayList<MapMessage>(messages.values());
            return messageList.toArray(new MapMessage[messageList.size()]);

        }
        return new MapMessage[0];
    }


    public void acknowledgeMessage(String topic, String subscriberId, String messageId) {
        String subscriptionId = Util.getUniqueSubscriptionId(topic, subscriberId);
        Map<String, MapMessage> messages = messageMap.get(subscriptionId);
        if (messages != null) {
            try {
                MapMessage mapMessage = messages.get(messageId);
                if (mapMessage != null) {
                    mapMessage.acknowledge();
                    messages.remove(messageId);
                    if (log.isDebugEnabled()) {
                        log.debug("Message with id:" + messageId + " was acknowledged successfully.");
                    }
                    return;
                }
            } catch (JMSException e) {
                log.error("Failed to acknowledge message:" + messageId, e);
                // ignore throwing exception as we do not ack the message.
            }
            // we remove the message from map to avoid memory leaks.
            // This message will be delivered back to client from message broker since we do not acknowledge.
            messages.remove(messageId);
        }
    }

    public void addMessage(String subscriptionId, MapMessage message) {
        Map<String, MapMessage> messages = messageMap.get(subscriptionId);
        if (messages == null) {
            messageMap.put(subscriptionId, new HashMap<String, MapMessage>());
        }
        try {
            messageMap.get(subscriptionId).put(message.getJMSMessageID(), message);
        } catch (JMSException e) {
            log.error("Error occurred while storing message temporally.", e);
        }
    }

    public void removeSubscriptionMap(String subscriptionId) {
        messageMap.remove(subscriptionId);
    }

}
