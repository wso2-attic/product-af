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

package org.wso2.carbon.appfactory.stratos.listeners;

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
 * Durable subscriber for topic "subscribe_tenant_in_stratos" in MB.
 * TenantStratosSubscriptionMessageListener is set as the message listener
 */
public class TenantStratosSubscriptionDurableSubscriber {
    private static Log log = LogFactory.getLog(TenantStratosSubscriptionDurableSubscriber.class);
    private String subscriptionId;
    private String topicName;
    TopicConnectionFactory connFactory;
    TopicConnection topicConnection = null;
    TopicSession topicSession = null;
    TopicSubscriber topicSubscriber = null;
    InitialContext ctx = null;

    public TenantStratosSubscriptionDurableSubscriber(String topicName, String subscriptionId)
            throws AppFactoryException {
        this.topicName = topicName;
        this.subscriptionId = subscriptionId;
        try {
            subscribe();
        } catch (AppFactoryEventException e) {
            String msg = "Subscriber activation failed for topic" + topicName + " and subscription id " +
                         subscriptionId;
            throw new AppFactoryException(msg, e);
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
        properties.put(CarbonConstants.REQUEST_BASE_CONTEXT, true);
        properties.put(AppFactoryConstants.TOPIC, topicName);
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
            topicSubscriber.setMessageListener(new TenantStratosSubscriptionMessageListener(topicConnection,
                                                                                            topicSession,
                                                                                            topicSubscriber));
	        topicConnection.start();
            if (log.isDebugEnabled()) {
                log.debug("Durable Subscriber created for topic " + topicName + " with subscription id" +
                          subscriptionId);
            }
        } catch (NamingException e) {
            String errorMsg = "Failed to subscribe to topic:" + topicName + " due to " + e.getMessage();
            throw new AppFactoryEventException(errorMsg, e);
        } catch (JMSException e) {
            String errorMsg = "Failed to subscribe to topic:" + topicName + " due to " + e.getMessage();
            throw new AppFactoryEventException(errorMsg, e);
        }
    }

    /**
     * Stops all subscriptions
     *
     * @throws AppFactoryEventException
     */
    public void stopSubscription() throws AppFactoryEventException {
        try {
            if (topicConnection != null) {
                topicConnection.stop();
                topicConnection.close();
            }
        } catch (JMSException e) {
            String errorMsg = "Failed to un-subscribe due to:" + e.getMessage();
            throw new AppFactoryEventException(errorMsg, e);
        }
    }


}
