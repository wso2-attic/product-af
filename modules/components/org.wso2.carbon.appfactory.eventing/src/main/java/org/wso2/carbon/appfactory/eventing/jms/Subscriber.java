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

import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.appfactory.eventing.AppFactoryEventException;
import org.wso2.carbon.appfactory.eventing.utils.Util;

public class Subscriber {
    private String subscriptionId;
    public static final String ANDES_ICF = "org.wso2.andes.jndi.PropertiesFileInitialContextFactory";
    private static final String CF_NAME_PREFIX = "connectionfactory.";
    private static final String CF_NAME = "andesConnectionfactory";
    private String topicName = "APPLICATION_CREATION";
    TopicConnectionFactory connFactory;
    TopicConnection topicConnection;
    InitialContext ctx = null;
    MessageListener messageListener;
    private static Log log = LogFactory.getLog(Subscriber.class);

    public Subscriber(String topicName, String subscriptionId, MessageListener messageListener) {
        this.topicName = topicName;
        this.subscriptionId = subscriptionId;
        this.messageListener = messageListener;
    }

    public void subscribe() throws AppFactoryEventException {

        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, ANDES_ICF);
        properties.put(CF_NAME_PREFIX + CF_NAME, Util.getTCPConnectionURL());
        properties.put(CarbonConstants.REQUEST_BASE_CONTEXT, "true");
        properties.put("topic", topicName);

        try {
            ctx = new InitialContext(properties);
            // Lookup connection factory
            connFactory = (TopicConnectionFactory) ctx.lookup(CF_NAME);
            topicConnection = connFactory.createTopicConnection();
            TopicSession topicSession = topicConnection.createTopicSession(false, TopicSession.CLIENT_ACKNOWLEDGE);
            // create durable subscriber with subscription ID
            Topic topic = null;
            try {
                topic = (Topic) ctx.lookup(topicName);
            } catch (NamingException e) {
                topic = topicSession.createTopic(topicName);
            }
            TopicSubscriber topicSubscriber = topicSession.createDurableSubscriber(topic, subscriptionId);
            topicSubscriber.setMessageListener(messageListener);
            topicConnection.start();
        } catch (NamingException e) {
            String errorMsg = "Failed to subscribe to topic:" + topicName + " due to " + e.getMessage();
            throw new AppFactoryEventException(errorMsg, e);
        } catch (JMSException e) {
            String errorMsg = "Failed to subscribe to topic:" + topicName + " due to " + e.getMessage();
            throw new AppFactoryEventException(errorMsg, e);
        }


    }

    public void stopSubscription() throws AppFactoryEventException {
        try {
            if (topicConnection != null) {
                topicConnection.stop();
                topicConnection.close();
            }
        } catch (JMSException e) {
            String errorMsg = "Failed to un-subscribe due to:" + e.getMessage();
            if (log.isDebugEnabled()) {
                log.debug(errorMsg, e);
            }
        }
    }


    @Override
    public String toString() {
        return "Subscriber [messageListener=" + messageListener
                + ", subscriptionId=" + subscriptionId + ", topicName="
                + topicName + "]";
    }


}
