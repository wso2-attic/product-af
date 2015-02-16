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
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.beans.RuntimeBean;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.task.AppFactoryTenantCloudInitializerTask;
import org.wso2.carbon.appfactory.eventing.AppFactoryEventException;
import org.wso2.carbon.appfactory.eventing.utils.Util;
import org.wso2.carbon.context.CarbonContext;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
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
    private static final String ANDES_ICF = "org.wso2.andes.jndi.PropertiesFileInitialContextFactory";
    private static final String CF_NAME_PREFIX = "connectionfactory.";
    private static final String CF_NAME = "qpidConnectionfactory";
    private static final String TOPIC = "topic";
    private static final String STAGE = "stage";
    private static final String CARTRIDGE_TYPE_PREFIX = "cartridgeTypePrefix";
    private static final String ALIAS_PREFIX = "aliasPrefix";
    private static final String REPO_URL = "repoURL";
    private static final String DATA_CARTRIDGE_ALIAS = "dataCartridgeAlias";
    private static final String DATA_CARTRIDGE_TYPE = "dataCartridgeType";
    private static final String AUTO_SCALE_POLICY = "autoscalePolicy";
    private static final String DEPLOYMENT_POLICY = "deploymentPolicy";
    private static final String TENANT_ADMIN_FOR_REST = "tenantAdminForRest";
    private static final String SERVER_URL_FOR_REST = "serverURLForRest";
    private static final String TENANT_ADMIN_PASSWORD_FOR_REST = "tenantAdminPasswordForRest";


    private String topicName;
    private InitialContext ctx;
    private TopicConnectionFactory connFactory;

    public TenantStratosSubscriptionMessagePublisher(String topicName) {
        this.topicName=topicName;
    }

    /**
     *
     * @param runtimeBean - contains all the data need to subscribe to stratos environment.
     * @param restServiceProperties - contains all the data to do the rest call.
     * @throws AppFactoryEventException
     */
    public void publishMessage(RuntimeBean runtimeBean, Map<String, String> restServiceProperties,String stage) throws
                                                                                                   AppFactoryEventException {
        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, ANDES_ICF);
        properties.put(CF_NAME_PREFIX + CF_NAME, Util.getTCPConnectionURL());
        properties.put(TOPIC, topicName);
        TopicConnection topicConnection = null;
        TopicSession topicSession = null;
        try {
            ctx = new InitialContext(properties);
            connFactory = (TopicConnectionFactory) ctx.lookup(CF_NAME);
            topicConnection = connFactory.createTopicConnection();
            topicConnection.start();
            topicSession = topicConnection.createTopicSession(false, TopicSession.CLIENT_ACKNOWLEDGE);
            Topic topic = topicSession.createTopic(topicName);
            MapMessage mapMessage = topicSession.createMapMessage();
            mapMessage.setString(STAGE, stage);
            mapMessage.setString(CARTRIDGE_TYPE_PREFIX, runtimeBean.getCartridgeTypePrefix());
            mapMessage.setString(ALIAS_PREFIX, runtimeBean.getAliasPrefix());
            mapMessage.setString(REPO_URL, runtimeBean.getRepoURL());
            mapMessage.setString(AppFactoryConstants.PAAS_ARTIFACT_STORAGE_REPOSITORY_PROVIDER_ADMIN_USER_NAME, AppFactoryUtil.getAppfactoryConfiguration().
                    getFirstProperty(AppFactoryConstants.PAAS_ARTIFACT_STORAGE_REPOSITORY_PROVIDER_ADMIN_USER_NAME));
            mapMessage.setString(AppFactoryConstants.PAAS_ARTIFACT_STORAGE_REPOSITORY_PROVIDER_ADMIN_PASSWORD, AppFactoryUtil.getAppfactoryConfiguration().
                    getFirstProperty(AppFactoryConstants.PAAS_ARTIFACT_STORAGE_REPOSITORY_PROVIDER_ADMIN_PASSWORD));
            mapMessage.setString(DATA_CARTRIDGE_TYPE, runtimeBean.getDataCartridgeType());
            mapMessage.setString(DATA_CARTRIDGE_ALIAS, runtimeBean.getDataCartridgeAlias());
            mapMessage.setString(AUTO_SCALE_POLICY, runtimeBean.getAutoscalePolicy());
            mapMessage.setString(DEPLOYMENT_POLICY, runtimeBean.getDeploymentPolicy());

            mapMessage.setString(SERVER_URL_FOR_REST, restServiceProperties
                    .get(AppFactoryTenantCloudInitializerTask.SERVER_URL));
            mapMessage.setString(TENANT_ADMIN_FOR_REST, restServiceProperties
                    .get(AppFactoryTenantCloudInitializerTask.ADMIN_USERNAME));
            mapMessage.setString(TENANT_ADMIN_PASSWORD_FOR_REST, restServiceProperties
                    .get(AppFactoryTenantCloudInitializerTask.ADMIN_PASSWORD));
            mapMessage.setString(AppFactoryTenantCloudInitializerTask.TENANT_DOMAIN, restServiceProperties
                    .get(AppFactoryTenantCloudInitializerTask.TENANT_DOMAIN));
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
        } catch (AppFactoryException e) {
            String msg = "Failed to read Appfactory Configuration ";
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
