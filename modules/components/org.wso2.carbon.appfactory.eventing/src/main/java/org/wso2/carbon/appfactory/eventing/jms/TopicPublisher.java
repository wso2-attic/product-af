package org.wso2.carbon.appfactory.eventing.jms;

/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.appfactory.eventing.AppFactoryEventException;
import org.wso2.carbon.appfactory.eventing.Event;
import org.wso2.carbon.appfactory.eventing.EventDispatcher;
import org.wso2.carbon.appfactory.eventing.utils.Util;
import org.wso2.carbon.context.CarbonContext;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;
import java.util.UUID;

public class TopicPublisher implements EventDispatcher {
    Log log = LogFactory.getLog(TopicPublisher.class);
    public static final String QPID_ICF = "org.wso2.andes.jndi.PropertiesFileInitialContextFactory";
    private static final String CF_NAME_PREFIX = "connectionfactory.";
    private static final String CF_NAME = "qpidConnectionfactory";
    public static final String DEFAULT_SUBSCRIPTION = "default_subscription";
    public static final String MESSAGE_TITLE = "messageTitle";
    public static final String MESSAGE_BODY = "messageBody";
    private InitialContext ctx;
    private TopicConnectionFactory connFactory;
    TopicSubscriber topicSubscriber;

    public void publishMessage(Event event) throws AppFactoryEventException {
        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, QPID_ICF);
        properties.put(CF_NAME_PREFIX + CF_NAME, Util.getTCPConnectionURL());
        properties.put(CarbonConstants.REQUEST_BASE_CONTEXT, "true");
        try {
            ctx = new InitialContext(properties);
            connFactory = (TopicConnectionFactory) ctx.lookup(CF_NAME);
        } catch (NamingException e) {
            throw new AppFactoryEventException("Failed to initialize InitialContext.", e);
        }

        TopicConnection topicConnection = null;
        TopicSession topicSession = null;
        try {
            topicConnection = connFactory.createTopicConnection();
            topicSession = topicConnection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
            // Send message
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            Topic topic = topicSession.createTopic(event.getTarget());

            //Until MB supports 'Dynamic Topics' we have to create a subscription, therefore forcing Message broker to
            // create the topic.
            String defaultSubscriptionId = tenantDomain + "/" + DEFAULT_SUBSCRIPTION + UUID.randomUUID();
            topicSubscriber = topicSession.createDurableSubscriber(topic, defaultSubscriptionId);
            // We are unsubscribing from the Topic as soon as
            topicSession.unsubscribe(defaultSubscriptionId);

            // create the message to send
            MapMessage mapMessage = topicSession.createMapMessage();
            mapMessage.setString(MESSAGE_TITLE, event.getMessageTitle());
            mapMessage.setString(MESSAGE_BODY, event.getMessageBody());
            javax.jms.TopicPublisher topicPublisher = topicSession.createPublisher(topic);
            topicConnection.start();
            topicPublisher.publish(mapMessage);
            if (log.isDebugEnabled()) {
                log.debug("Message with Id:" + mapMessage.getJMSMessageID() + ", with title:" + event.getMessageTitle()
                        + " was successfully published.");
            }

        } catch (JMSException e) {
            log.error("Failed to publish message due to " + e.getMessage(), e);
            throw new AppFactoryEventException("Failed to publish message due to " + e.getMessage(), e);
        } finally {
            if (topicSubscriber != null) {
                try {
                    topicSubscriber.close();
                } catch (JMSException e) {
                    log.error("Failed to close default topic subscriber", e);
                }
            }
            if (topicSession != null) {
                try {
                    topicSession.close();
                } catch (JMSException e) {
                    log.error("Failed to close topic session", e);
                }
            }
            if (topicConnection != null) {
                try {
                    topicConnection.close();
                } catch (JMSException e) {
                    log.error("Failed to close topic connection", e);
                }
            }
        }
    }


    @Override
    public void dispatchEvent(Event event) throws AppFactoryEventException {
        publishMessage(event);
    }
}