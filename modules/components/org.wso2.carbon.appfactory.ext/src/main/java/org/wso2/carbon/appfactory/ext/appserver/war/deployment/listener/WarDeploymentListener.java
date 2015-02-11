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

package org.wso2.carbon.appfactory.ext.appserver.war.deployment.listener;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.application.mgt.stub.ApplicationManagementServiceStub;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.eventing.AppFactoryEventException;
import org.wso2.carbon.appfactory.eventing.Event;

import org.wso2.carbon.appfactory.eventing.EventNotifier;
import org.wso2.carbon.appfactory.eventing.builder.utils.ContinousIntegrationEventBuilderUtil;
import org.wso2.carbon.appfactory.ext.appserver.LeaderElector;
import org.wso2.carbon.appfactory.ext.internal.ServiceHolder;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.TenantManager;

import java.io.File;

/**
 * Intercepts life cycle events related to web applications. and notifies Appfactory
 */
public class WarDeploymentListener implements LifecycleListener {

    private static final Log log = LogFactory.getLog(WarDeploymentListener.class);

    public void lifecycleEvent(LifecycleEvent lifecycleEvent) {
        if(!LeaderElector.getInstance().isIsNotifyEligible()){
            return;
        }

        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        boolean isEndTenantFlowNeeded = false;

        try {

            if (!(lifecycleEvent.getSource() instanceof StandardContext)) {
                return;
            }

            StandardContext context = (StandardContext) lifecycleEvent.getSource();

            /*
             * Try to extract the tenant domain from context path
             *.Discard the event if there is no valid tenant.
             */
            if (tenantDomain == null || MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                if (context.getPath().contains("/t/")) {
                    tenantDomain = context.getPath().split("/t/")[1].split("/")[0];
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                    isEndTenantFlowNeeded = true;
                } else {
                    return;
                }
            }

            // TenantDomain should have a value by now
            String stage = System.getProperty(AppFactoryConstants.CLOUD_STAGE);
            String lifeCycleEventType = lifecycleEvent.getType();

            //Filtering Lifecycle events
            if (Lifecycle.AFTER_START_EVENT.equals(lifeCycleEventType) ||
                    Lifecycle.AFTER_STOP_EVENT.equals(lifeCycleEventType)) {

                if (StringUtils.isNotEmpty(tenantDomain)) {
                    String appId = extractAppId(context.getPath());
                    String appVersion = null;
                    String docBase = context.getDocBase();
                    try {
                        appVersion = extractVersion(context.getPath());
                    } catch (AppFactoryException e) {
                        log.error("Failed to retrieve the stage" + e.getMessage(), e);
                    }

                    handleEvent(tenantDomain, appId, appVersion, stage, lifecycleEvent, docBase);
                }
            }
        } finally {
            //end tenant flow only if it's started
            if (isEndTenantFlowNeeded) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

    }

    private void handleEvent(String tenantDomain, String appId, String appVersion, String stage,
                             LifecycleEvent lifecycleEvent, String docBase) {

        if (log.isDebugEnabled()) {
            log.debug(String.format("life cycle event intercepted:  Tenant Domain = {%s} ,  Application ID = {%s}, " +
                    "Life Cycle State = {%s}", tenantDomain, appId, lifecycleEvent.getLifecycle().getState().name()));
        }

        switch (lifecycleEvent.getLifecycle().getState()) {
            case STARTED:
                onApplicationStarted(tenantDomain, appId, appVersion, stage, docBase);
                break;
            case FAILED:
                onApplicationFailed(tenantDomain, appId, appVersion, stage);
                break;
            default:
                if (log.isDebugEnabled()) {
                    log.debug(String.format("life cycle event is not sent to appfactory messaging system:  Tenant " +
                                    "Domain = {%s} ,  Application ID = {%s}, Application Version = {%s},  Life Cycle " +
                                    "State = {%s}", tenantDomain, appId, appVersion,
                            lifecycleEvent.getLifecycle().getState().name()));
                }
        }

    }

    private void onApplicationStarted(String tenantDomain, String appId, String appVersion, String stage,
                                      String docBase) {

        long artifactLastModifiedTime = new File(docBase).lastModified();

        try {
            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                return;
            }

            TenantManager manager = ServiceHolder.getInstance().getRealmService().getTenantManager();
            int tenantId = manager.getTenantId(tenantDomain);

            String afUrl = AppFactoryUtil.getAppfactoryConfiguration().
                    getFirstProperty(AppFactoryConstants.APPFACTORY_SERVER_URL);
            ApplicationManagementServiceStub stub =
                    new ApplicationManagementServiceStub(afUrl + "ApplicationManagementService");
	        AppFactoryUtil.setAuthHeaders(stub._getServiceClient(), manager.getTenant(tenantId).getAdminName() + "@" + tenantDomain);

            if (log.isDebugEnabled()) {
                log.debug("Notifing deployment success status of appid : " + appId + ", version : " + appVersion +
                          ", tenantDomain : " + tenantDomain);
            }

            stub.updateApplicationDeploymentSuccessStatus(appId, appVersion, stage, tenantDomain,
                    artifactLastModifiedTime);

            if (log.isDebugEnabled()) {
                log.debug("Notified deployment success status of appid : " + appId + ", version : " + appVersion +
                          ", tenantDomain : " + tenantDomain);
            }

        } catch (Exception e) {
            log.error("Failed to notify the Application deployment success event to Appfactory " + e.getMessage(), e);
        }
    }

    private void onApplicationFailed(String tenantDomain, String appId, String appVersion, String stage) {
        String msg = appVersion + " deployment failed in " + stage + " stage";
        sendNotification(tenantDomain, appId, appVersion, stage, msg, "", Event.Category.ERROR);
    }

    private void sendNotification(String tenantDomain, String appId, String appVersion, String stage, String msg, String msgDescription,
                                  Event.Category category) {

        String correlationKey = org.wso2.carbon.appfactory.eventing.utils.Util.deploymentCorrelationKey
                (appId, stage, appVersion, tenantDomain);
        try {
            EventNotifier.getInstance().notify(ContinousIntegrationEventBuilderUtil.buildObtainWarDeploymentStatusEvent(appId,
                    tenantDomain, msg,
                    msgDescription,
                    category, correlationKey));
        } catch (AppFactoryEventException e) {
            log.error("Failed to notify the Application deployment success event " + e.getMessage(), e);
        }
    }

    /**
     * Extracts the application ID from the docbase.
     *
     * @param docBase
     * @return appId
     */
    private String extractAppId(String docBase) {
        String baseName = FilenameUtils.getName(docBase);
        String[] splits = baseName.split("-");

        String appId;
        if (splits.length > 0) {
            appId = splits[0];

        } else {
            appId = baseName;
        }
        return appId;

    }

    /**
     * Extracts version from context
     *
     * @param docBase
     * @return
     */
    private String extractVersion(String docBase) throws AppFactoryException {
        String baseName = FilenameUtils.getName(docBase);
        String[] splits = baseName.split("-", 2);
        String artifactVersionName = AppFactoryUtil.getAppfactoryConfiguration().
                getFirstProperty(AppFactoryConstants.TRUNK_WEBAPP_ARTIFACT_VERSION_NAME);
        String sourceVersionName = AppFactoryUtil.getAppfactoryConfiguration().
                getFirstProperty(AppFactoryConstants.TRUNK_WEBAPP_SOURCE_VERSION_NAME);

        String version = null;
        if (splits.length > 1) {
            version = splits[1];
        }

        if (version != null && artifactVersionName != null && version.equalsIgnoreCase(artifactVersionName)) {
            return sourceVersionName; //return if trunk
        }

        splits = baseName.split("-");
        if (splits.length > 0) {
            version = splits[splits.length - 1];
        }

        return version;

    }
}
