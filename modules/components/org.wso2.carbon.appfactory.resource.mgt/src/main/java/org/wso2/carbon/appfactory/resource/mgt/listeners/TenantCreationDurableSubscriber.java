/*
 *
 *  Copyright 2014 WSO2, Inc. (http://wso2.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.appfactory.resource.mgt.listeners;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.eventing.AppFactoryEventException;
import org.wso2.carbon.appfactory.eventing.utils.Util;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

/**
 * Durable subscriber for topics  in MB - tenant creation.
 * TenantCreatioMessageListener is set as the message listener
 */
public class TenantCreationDurableSubscriber {
    private static Log log = LogFactory.getLog(TenantCreationDurableSubscriber.class);
    private String subscriptionId;
    private String topicName;

    public TenantCreationDurableSubscriber(String topicName, String subscriptionId)
            throws AppFactoryException {
        this.topicName = topicName;
	    this.subscriptionId = subscriptionId;
        try {
            subscribe();
        } catch (AppFactoryEventException e) {
	        throw new AppFactoryException("Subscriber activation failed for topic : " + topicName + " and subscription " +
	                                      "id :  " + subscriptionId, e);
        }
    }

    /**
     * Subscribe as a durable subscriber to the topic.
     *
     * @throws AppFactoryEventException
     */
    public void subscribe() throws AppFactoryEventException {
        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, AppFactoryConstants.ANDES_ICF);
        properties.put(AppFactoryConstants.CF_NAME_PREFIX + AppFactoryConstants.CF_NAME, Util.getTCPConnectionURL());
        properties.put(CarbonConstants.REQUEST_BASE_CONTEXT, "true");
        properties.put(AppFactoryConstants.TOPIC, topicName);
        TopicConnectionFactory connFactory;
        TopicConnection topicConnection;
        TopicSession topicSession;
        TopicSubscriber topicSubscriber;
        InitialContext ctx;
        try {
            ctx = new InitialContext(properties);
            connFactory = (TopicConnectionFactory) ctx.lookup(AppFactoryConstants.CF_NAME);
            topicConnection = connFactory.createTopicConnection();
            topicSession = topicConnection.createTopicSession(false, TopicSession.CLIENT_ACKNOWLEDGE);
            Topic topic;
            try {
                topic = (Topic) ctx.lookup(topicName);
            } catch (NamingException e) {
                topic = topicSession.createTopic(topicName);
            }
	        topicSubscriber = topicSession.createDurableSubscriber(topic, subscriptionId);
            topicSubscriber.setMessageListener(
                    new TenantCreationMessageListener(topicConnection, topicSession, topicSubscriber));
            topicConnection.start();
	        if (log.isDebugEnabled()) {
		        log.debug("Durable Subscriber created for topic " + topicName + " with subscription id : " +
		                  subscriptionId);
	        }
        } catch (NamingException e) {
	        throw new AppFactoryEventException("Failed to subscribe to topic : " + topicName + " with subscription id" +
	                                           " : " + subscriptionId, e);
        } catch (JMSException e) {
	        throw new AppFactoryEventException("Failed to subscribe to topic : " + topicName + " with subscription id" +
	                                           " : " + subscriptionId, e);
        }
    }
}