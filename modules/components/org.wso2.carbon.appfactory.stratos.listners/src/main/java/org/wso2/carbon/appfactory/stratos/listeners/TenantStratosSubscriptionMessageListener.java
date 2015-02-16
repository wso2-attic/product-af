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
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.task.AppFactoryTenantCloudInitializerTask;
import org.wso2.carbon.appfactory.s4.integration.StratosRestService;

import javax.jms.*;

/**
 * Listens to topic "subscribe_tenant_in_stratos" in the Message Broker and when a new message arrived, executes the
 * onMessage method. AUTO_ACKNOWLEDGE is used.
 * This class is set as the Message Listener in TenantStratosSubscriptionDurableSubscriber class.
 */
public class TenantStratosSubscriptionMessageListener implements MessageListener {
    Log log = LogFactory.getLog(TenantStratosSubscriptionMessageListener.class);

    private StratosRestService restService;
    private TopicConnection topicConnection;
    private TopicSession topicSession;
    private TopicSubscriber topicSubscriber;
    private int currentMsgCount = 0;
    public static final String TENANT_DOMAIN = "tenantDomain";
    private static final String STAGE = "stage";
    private static final String CARTRIDGE_TYPE_PREFIX = "cartridgeTypePrefix";
    private static final String ALIAS_PREFIX = "aliasPrefix";
    private static final String REPO_URL = "repoURL";
    private static final String ADMIN_USER_NAME = "adminUserName";
    private static final String ADMIN_PASSWORD = "adminPassword";
    private static final String DATA_CARTRIDGE_ALIAS = "dataCartridgeAlias";
    private static final String DATA_CARTRIDGE_TYPE = "dataCartridgeType";
    private static final String AUTO_SCALE_POLICY = "autoscalePolicy";
    private static final String DEPLOYMENT_POLICY = "deploymentPolicy";
    private static final String TENANT_ADMIN_FOR_REST = "tenantAdminForRest";
    private static final String SERVER_URL_FOR_REST = "serverURLForRest";
    private static final String TENANT_ADMIN_PASSWORD_FOR_REST = "tenantAdminPasswordForRest";


    public TenantStratosSubscriptionMessageListener(TopicConnection topicConnection, TopicSession topicSession,
                                                    TopicSubscriber topicSubscriber) {
        this.topicConnection = topicConnection;
        this.topicSession = topicSession;
        this.topicSubscriber = topicSubscriber;
    }

    /**
     * @param message - map message which contains data to stratos subscriptions via a rest call.
     */
    @Override
    public void onMessage(Message message) {
        //TODO remove this log
        log.info("message received to topic name : " + topicSubscriber.toString() + ">>>>>>>>>>>>");
        if (log.isDebugEnabled()) {
            if (message instanceof MapMessage) {
                try {
                    String alias = ((MapMessage) message).getString(ALIAS_PREFIX);
                    String dataCartridgeType = ((MapMessage) message).getString(DATA_CARTRIDGE_TYPE);
                    log.debug("Received a message with alias " + alias + "for the data cartridge type " +
                              dataCartridgeType);
                } catch (JMSException e) {
                    log.error("Error while getting message content.", e);
                    throw new RuntimeException(e);
                }
            }
        }
        MapMessage mapMessage;
        if (message instanceof MapMessage) {
            mapMessage = (MapMessage) message;
            try {
                currentMsgCount++;
                String serverURL = mapMessage.getString(SERVER_URL_FOR_REST);
                String tenantAdmin = mapMessage.getString(TENANT_ADMIN_FOR_REST);
                String tenantAdminPassword = mapMessage.getString(TENANT_ADMIN_PASSWORD_FOR_REST);
                String tenantDomain = mapMessage.getString(AppFactoryTenantCloudInitializerTask.TENANT_DOMAIN);
                String username = tenantAdmin + "@" + tenantDomain;
                restService = new StratosRestService(serverURL, username, tenantAdminPassword);
                restService.subscribe(mapMessage.getString(CARTRIDGE_TYPE_PREFIX) + mapMessage.getString(STAGE),
                                      mapMessage.getString(ALIAS_PREFIX) + mapMessage.getString(STAGE) +
                                      tenantDomain.replace(AppFactoryConstants.DOT_SEPERATOR,
                                                           AppFactoryConstants.SUBSCRIPTION_ALIAS_DOT_REPLACEMENT),
                                      mapMessage.getString(REPO_URL), true,
                                      mapMessage.getString(AppFactoryConstants
                                                                 .PAAS_ARTIFACT_STORAGE_REPOSITORY_PROVIDER_ADMIN_USER_NAME),
                                      mapMessage.getString(AppFactoryConstants
                                                                   .PAAS_ARTIFACT_STORAGE_REPOSITORY_PROVIDER_ADMIN_PASSWORD),
                                      mapMessage.getString(DATA_CARTRIDGE_TYPE),
                                      mapMessage.getString(DATA_CARTRIDGE_ALIAS),
                                      mapMessage.getString(AUTO_SCALE_POLICY),
                                      mapMessage.getString(DEPLOYMENT_POLICY));
                //TODO remove this log
                log.info("subscription done in environment : " + mapMessage
                        .getString(STAGE) + "of tenant :" + tenantDomain);
                mapMessage.acknowledge();
            } catch (JMSException e) {
                String msg = "Can not read received map massage at count " + currentMsgCount;
                log.error(msg, e);
                throw new RuntimeException(e);
            } catch (AppFactoryException e) {
                String msg = "Can not subscribe to stratos cartridge";
                log.error(msg, e);
                throw new RuntimeException(e);
            }
        }
    }
}
