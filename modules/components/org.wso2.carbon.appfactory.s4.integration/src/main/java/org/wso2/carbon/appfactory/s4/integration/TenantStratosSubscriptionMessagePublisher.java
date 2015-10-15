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

package org.wso2.carbon.appfactory.s4.integration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.eventing.AppFactoryEventException;
import org.wso2.carbon.appfactory.eventing.utils.Util;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Map;
import java.util.Properties;

/**
 *  Publish messages to the topic "subscribe_tenant_in_stratos" in MB to do  stratos subscriptions with tenant creation.
 */
public class TenantStratosSubscriptionMessagePublisher {

    Log log = LogFactory.getLog(TenantStratosSubscriptionMessagePublisher.class);

    private String topicName;
    private InitialContext ctx;
    private TopicConnectionFactory connFactory;

    public TenantStratosSubscriptionMessagePublisher(String topicName) {
        this.topicName=topicName;
    }

	/**
	 * Publish message to MB/ActiveMQ Queue
	 * @param runtimeJson runtimebeans as a json
	 * @param tenantInfoJson tenantInfoBean as a json
	 * @param restServiceProperties propertyMap
	 * @param stageJson stages
	 * @throws AppFactoryEventException
	 */
    public void publishMessage(String runtimeJson, String tenantInfoJson, Map<String, String> restServiceProperties,
                               String stageJson) throws AppFactoryEventException {
        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, AppFactoryConstants.ANDES_ICF);
        properties.put(AppFactoryConstants.CF_NAME_PREFIX + AppFactoryConstants.CF_NAME, Util.getTCPConnectionURL());
        properties.put(AppFactoryConstants.TOPIC, topicName);
        TopicConnection topicConnection = null;
        TopicSession topicSession = null;
        try {
            ctx = new InitialContext(properties);
            connFactory = (TopicConnectionFactory) ctx.lookup(AppFactoryConstants.CF_NAME);
            topicConnection = connFactory.createTopicConnection();
            topicConnection.start();
            topicSession = topicConnection.createTopicSession(false, TopicSession.CLIENT_ACKNOWLEDGE);
            Topic topic = topicSession.createTopic(topicName);
            MapMessage mapMessage = topicSession.createMapMessage();
            mapMessage.setString(AppFactoryConstants.STAGE, stageJson);
            mapMessage.setString(AppFactoryConstants.RUNTIMES_INFO, runtimeJson);
            mapMessage.setString(AppFactoryConstants.TENANT_INFO, tenantInfoJson);
            javax.jms.TopicPublisher topicPublisher = topicSession.createPublisher(topic);
            topicPublisher.publish(mapMessage);

            //TODO remove this log
            log.info("Message with Id:" + mapMessage.getJMSMessageID() + " was successfully published to the" +
                     " topic " + topicName);


            if (log.isDebugEnabled()) {
                log.debug("Message with Id:" + mapMessage.getJMSMessageID() + " was successfully published to the" +
                          " topic " + topicName);
            }
        } catch (NamingException e) {
            String msg = "Failed to initialize InitialContext";
            throw new AppFactoryEventException(msg, e);
        } catch (JMSException e) {
            String msg = "Failed to publish message due to " + e.getMessage();
            throw new AppFactoryEventException(msg, e);
        } finally {
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


}
