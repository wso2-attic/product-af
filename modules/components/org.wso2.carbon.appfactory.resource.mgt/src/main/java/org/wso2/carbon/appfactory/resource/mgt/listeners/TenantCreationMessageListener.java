/*
 * Copyright 2015 WSO2, Inc. (http://wso2.com)
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package org.wso2.carbon.appfactory.resource.mgt.listeners;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.resource.mgt.internal.ServiceHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.stratos.common.exception.StratosException;
import org.wso2.carbon.tenant.mgt.core.TenantPersistor;
import org.wso2.carbon.tenant.mgt.util.TenantMgtUtil;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.jms.*;
import java.io.IOException;

/**
 * Listens to topic  in the Message Broker and when a new message arrived (for tenant creation), executes the
 * onMessage method. CLIENT_ACKNOWLEDGE is used.
 * This class is set as the Message Listener in TenantCreationDurableSubscriber class.
 */
public class TenantCreationMessageListener implements MessageListener {
	private static Log log = LogFactory.getLog(TenantCreationMessageListener.class);
	protected TopicConnection topicConnection;
	protected TopicSession topicSession;
	protected TopicSubscriber topicSubscriber;

	public TenantCreationMessageListener(TopicConnection topicConnection, TopicSession topicSession,
	                                     TopicSubscriber topicSubscriber) {
		this.topicConnection = topicConnection;
		this.topicSession = topicSession;
		this.topicSubscriber = topicSubscriber;
	}

	/**
	 * @param message - map message which contains data to tenant creation via a rest call.
	 */
	@Override
	public void onMessage(Message message) {

		TenantInfoBean tenantInfoBean = null;
		MapMessage mapMessage;
		if (message instanceof MapMessage) {
			mapMessage = (MapMessage) message;
			String tenantInfoJson = null;
			try {
				tenantInfoJson = mapMessage.getString(AppFactoryConstants.TENANT_INFO);
				ObjectMapper mapper = new ObjectMapper();
				tenantInfoBean = mapper.readValue(tenantInfoJson, TenantInfoBean.class);

				if (log.isDebugEnabled()) {
					log.debug("Received a message for tenant domain " + tenantInfoBean.getTenantDomain());
				}
				mapMessage.acknowledge();
			} catch (JMSException e) {
				log.error("Error while getting message content.", e);
				throw new RuntimeException(e);
			} catch (JsonParseException e) {
				log.error("Error while converting the json to object.", e);
				throw new RuntimeException(e);
			} catch (JsonMappingException e) {
				log.error("Error while converting the json to object.", e);
				throw new RuntimeException(e);
			} catch (IOException e) {
				log.error("Error while converting the json to object.", e);
				throw new RuntimeException(e);
			}
		}

		try {
			int tenantId = ServiceHolder.getRealmService().getTenantManager().getTenantId(tenantInfoBean.getTenantDomain());
			if (tenantId == -1) {
				addTenant(tenantInfoBean);
			} else {
				if (log.isDebugEnabled()) {
					log.debug("Tenant Already exist, skipping the tenant addition. Tenant domain : " +
					          tenantInfoBean.getTenantDomain() + "and tenant Id : " + tenantInfoBean.getTenantId());
				}
			}

		} catch (JMSException e) {
			String msg = "Can not read received map massage";
			log.error(msg, e);
			throw new RuntimeException(e);
		} catch (AppFactoryException e) {
			String msg = "Can not create tenant";
			log.error(msg, e);
			throw new RuntimeException(e);
		} catch (Exception e) {
			String msg = "Can not create tenant";
			log.error(msg, e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * super admin adds a tenant
	 *
	 * @param tenantInfoBean tenant info bean
	 * @return UUID
	 * @throws Exception if error in adding new tenant.
	 */
	public String addTenant(TenantInfoBean tenantInfoBean) throws Exception {
		try {
			PrivilegedCarbonContext.startTenantFlow();
			PrivilegedCarbonContext.getThreadLocalCarbonContext()
			                       .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
			PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
			Tenant tenant = TenantMgtUtil.initializeTenant(tenantInfoBean);
			TenantPersistor persistor = new TenantPersistor();
			// not validating the domain ownership, since created by super tenant
			int tenantId = persistor.persistTenant(tenant, false, tenantInfoBean.getSuccessKey(),
			                                       tenantInfoBean.getOriginatedService(), false);
			tenantInfoBean.setTenantId(tenantId);
			TenantMgtUtil.addClaimsToUserStoreManager(tenant);
			//Notify tenant addition
			try {
				TenantMgtUtil.triggerAddTenant(tenantInfoBean);
			} catch (StratosException e) {
				String msg = "Error in notifying tenant addition.";
				log.error(msg, e);
				throw new Exception(msg, e);
			}

			TenantMgtUtil.activateTenantInitially(tenantInfoBean, tenantId);
			return TenantMgtUtil.prepareStringToShowThemeMgtPage(tenant.getId());
		} finally {
			PrivilegedCarbonContext.endTenantFlow();
		}
	}
}