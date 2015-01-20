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

package org.wso2.carbon.appfactory.ext.appserver.dbs.deployment.listener;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEvent;
import org.apache.axis2.engine.AxisObserver;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.appfactory.application.mgt.stub.ApplicationManagementServiceStub;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.eventing.Event;

import org.wso2.carbon.appfactory.eventing.EventNotifier;
import org.wso2.carbon.appfactory.eventing.builder.utils.ContinousIntegrationEventBuilderUtil;
import org.wso2.carbon.appfactory.ext.appserver.LeaderElector;
import org.wso2.carbon.appfactory.ext.internal.ServiceHolder;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.api.TenantManager;

import java.io.File;
import java.util.ArrayList;

/**
 * @scr.component name="org.wso2.carbon.appfactory.appserver.dbs.deployment.listener.ServiceDeploymentListener"
 * immediate="true"
 *
 */

public class ServiceDeploymentListener implements AxisObserver {

    private static final Log log = LogFactory.getLog(ServiceDeploymentListener.class);

    @Override
    public void init(AxisConfiguration axisConfiguration) {

    }

    @Override
    public void serviceUpdate(AxisEvent axisEvent, AxisService axisService) {
        int eventType = axisEvent.getEventType();
        String serviceName = axisService.getName();
	    String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
	    String stage= System.getProperty(AppFactoryConstants.CLOUD_STAGE);

        if (eventType == AxisEvent.SERVICE_DEPLOY) {
            if (!JavaUtils.isTrue(axisService.getParameterValue(CarbonConstants.HIDDEN_SERVICE_PARAM_NAME))) {
                log.info("Deploying Axis2 service: " + serviceName);
                String infoTitle = serviceName + " deployed.";
                try {
                    String correlationKey = org.wso2.carbon.appfactory.eventing.utils.Util.deploymentCorrelationKey
                            (extractAppId(serviceName), stage, extractVersion(serviceName), tenantDomain);

                     EventNotifier.getInstance().notify(ContinousIntegrationEventBuilderUtil.buildObtainDbsDeploymentStatusEvent(
                             extractAppId(serviceName), infoTitle, "", Event.Category.INFO, correlationKey));
                } catch (Exception e) {
                    log.error("Failed to notify the Service Deploy deployment success event " + e.getMessage(), e);
                }
            } else {
                log.debug("Deploying hidden Axis2 service : " + serviceName);
            }

            if (tenantDomain == MultitenantConstants.SUPER_TENANT_DOMAIN_NAME ||
                !LeaderElector.getInstance().isIsNotifyEligible()){
                return;
            }

	        TenantManager manager = ServiceHolder.getInstance().getRealmService().getTenantManager();
	        try {
		        int tenantId = manager.getTenantId(tenantDomain);
		        String afUrl = AppFactoryUtil.getAppfactoryConfiguration().
                        getFirstProperty(AppFactoryConstants.APPFACTORY_SERVER_URL);
		        ApplicationManagementServiceStub stub =
				        new ApplicationManagementServiceStub(afUrl + "ApplicationManagementService");
		        AppFactoryUtil.setAuthHeaders(stub._getServiceClient(), manager.getTenant(tenantId).getAdminName() + "@" + tenantDomain);

                long artifactLastModifiedTime =0 ;
                String docBase ;
                if(axisService.getFileName()!=null){
                    docBase = axisService.getFileName().toString().split(":")[1];
                    artifactLastModifiedTime = new File(docBase).lastModified();
                }

		        stub.updateApplicationDeploymentSuccessStatus(extractAppId(serviceName), extractVersion(serviceName),
                        stage, tenantDomain, artifactLastModifiedTime);

	        } catch (Exception e) {
		        if(log.isDebugEnabled()){
			        log.debug("Failed to notify the service deployment success event to Appfactory " + e.getMessage(), e);
		        }
		        log.error("Failed to notify the service deployment success event to Appfactory ");
	        }
        }
    }

    @Override
    public void serviceGroupUpdate(AxisEvent axisEvent, AxisServiceGroup axisServiceGroup) {

    }

    @Override
    public void moduleUpdate(AxisEvent axisEvent, AxisModule axisModule) {

    }

    @Override
    public void addParameter(Parameter parameter) throws AxisFault {

    }

    @Override
    public void removeParameter(Parameter parameter) throws AxisFault {

    }

    @Override
    public void deserializeParameters(OMElement omElement) throws AxisFault {

    }

    @Override
    public Parameter getParameter(String s) {
        return null;
    }

    @Override
    public ArrayList<Parameter> getParameters() {
        return null;
    }

    @Override
    public boolean isParameterLocked(String s) {
        return false;
    }

	/**
	 * Extracts the application ID from the docbase.
	 * @param serviceName
	 * @return appId
	 */
	private String extractAppId(String serviceName) {
		String[] splits = serviceName.split("-");

		String appId = null;
		if (splits.length > 0) {
			appId = splits[0];

		} else {
			appId = serviceName;
		}
		return appId;

	}

	/**
	 * Extracts version from context
	 * @param serviceName
	 * @return
	 */
	private String extractVersion(String serviceName) throws AppFactoryException {
		String [] splits = serviceName.split("-");
		String artifactVersionName = AppFactoryUtil.getAppfactoryConfiguration().
				getFirstProperty(AppFactoryConstants.TRUNK_SERVICES_ARTIFACT_VERSION_NAME);
		String sourceVersionName = AppFactoryUtil.getAppfactoryConfiguration().
				getFirstProperty(AppFactoryConstants.TRUNK_SERVICES_SOURCE_VERSION_NAME);

		String version = null;
		if (splits.length > 1){
			version = splits[splits.length -1];
		}

		if (version != null && artifactVersionName != null && version.equalsIgnoreCase(artifactVersionName)){
			return sourceVersionName;
		}

		return version;
	}
}


