/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.appfactory.bam.integration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.databridge.agent.thrift.AsyncDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;

public class BamDataPublisher {


    private String APP_CREATION_STREAM = "org.wso2.carbon.appfactory.appCreation" ;
    private String APP_VERSION_STREAM = "org.wso2.carbon.appfactory.appVersion" ;
    private String TENANT_USER_STREAM = "org.wso2.carbon.appfactory.tenantUser" ;
    private String APP_USER_STREAM = "org.wso2.carbon.appfactory.appUser" ;
    private String APP_ISSUE_STREAM = "org.wso2.carbon.appfactory.appIssue" ;
    private String APP_BUILD_STREAM = "org.wso2.carbon.appfactory.appBuild" ;
    private String APP_CREATION_STREAM_VERSION = "1.0.0";
    private String APP_VERSION_STREAM_VERSION = "1.0.0";
    private String TENANT_USER_STREAM_VERSION = "1.0.0";
    private String APP_USER_STREAM_VERSION = "1.0.0";
    private String APP_ISSUE_STREAM_VERSION = "1.0.0";
    private String APP_BUILD_STREAM_VERSION = "1.0.0";
    private boolean ENABLE_DATA_PUBLISHING;

    private AsyncDataPublisher asyncDataPublisher;

    private String appCreationStream =  "{"+
            " 'name': '"+APP_CREATION_STREAM+"'," +
            " 'version': '"+APP_CREATION_STREAM_VERSION+"',"+
            " 'nickName': 'Application Creation Information',"+
            " 'description': 'This stream will store app creation data to BAM',"+
            "   'payloadData':["+
            "    {'name':'applicationName','type':'string'},"+
            "    {'name':'applicationKey','type':'string'},"+
            "    {'name':'timeStamp','type':'double'},"+
            "    {'name':'user',  'type':'string' },"+
            "    {'name':'appType','type':'string' },"+
            "    {'name':'repoType', 'type':'string'},"+
            "    {'name':'appDescription', 'type':'string'},"+
            "    {'name':'tenantId', 'type':'string'}"+
            "    ]"+
            "    }";

    private String appVersionStream =  "{"+
            " 'name': '"+APP_VERSION_STREAM+"'," +
            " 'version': '"+APP_VERSION_STREAM_VERSION+"',"+
            " 'nickName': 'Application Version Information',"+
            " 'description': 'This stream will store app version and stage data to BAM',"+
            "   'payloadData':["+
            "    {'name':'applicationName','type':'string'},"+
            "    {'name':'applicationKey','type':'string'},"+
            "    {'name':'timeStamp','type':'double'},"+
            "    {'name':'tenantId', 'type':'string'},"+
            "    {'name':'user',  'type':'string' },"+
            "    {'name':'appVersion','type':'string' },"+
            "    {'name':'stage', 'type':'string'}"+
            "    ]"+
            "    }";

    private String tenantUserStream =  "{"+
            " 'name': '"+TENANT_USER_STREAM+"'," +
            " 'version': '"+TENANT_USER_STREAM_VERSION+"',"+
            " 'nickName': 'Tenant User Information',"+
            " 'description': 'This stream will store tenant users to BAM',"+
            "   'payloadData':["+
            "    {'name':'tenantId', 'type':'string'},"+
            "    {'name':'user',  'type':'string' },"+
            "    {'name':'action', 'type':'string'},"+
            "    {'name':'timeStamp','type':'double'}"+
            "    ]"+
            "    }";

    private String appUserStream =  "{"+
            " 'name': '"+APP_USER_STREAM+"'," +
            " 'version': '"+APP_USER_STREAM_VERSION+"',"+
            " 'nickName': 'Application User Information',"+
            " 'description': 'This stream will store app users to BAM',"+
            "   'payloadData':["+
            "    {'name':'applicationName','type':'string'},"+
            "    {'name':'applicationKey','type':'string'},"+
            "    {'name':'timeStamp','type':'double'},"+
            "    {'name':'tenantId', 'type':'string'},"+
            "    {'name':'action', 'type':'string'},"+
            "    {'name':'user',  'type':'string' }"+
            "    ]"+
            "    }";

    private String appIssueStream =  "{"+
            " 'name': '"+APP_ISSUE_STREAM+"'," +
            " 'version': '"+APP_ISSUE_STREAM_VERSION+"',"+
            " 'nickName': 'Application Version Issue Information',"+
            " 'description': 'This stream will store app version issues to BAM',"+
            "   'payloadData':["+
            "    {'name':'issueKey','type':'string'},"+
            "    {'name':'applicationName','type':'string'},"+
            "    {'name':'applicationKey','type':'string'},"+
            "    {'name':'appVersion','type':'string'},"+
            "    {'name':'timeStamp','type':'double'},"+
            "    {'name':'tenantId', 'type':'string'},"+
            "    {'name':'type',  'type':'string' }, "+
            "    {'name':'priority',  'type':'string' }, "+
            "    {'name':'status',  'type':'string' }, "+
            "    {'name':'reporter',  'type':'string' }, "+
            "    {'name':'assignee',  'type':'string' }, "+
            "    {'name':'action',  'type':'string' }, "+
            "    {'name':'severity',  'type':'string' }, "+
            "    {'name':'createdTime',  'type':'string' }, "+
            "    {'name':'updatedTime',  'type':'string' } "+
            "    ]"+
            "    }";

    private String appBuildStream =  "{"+
            " 'name': '"+APP_BUILD_STREAM+"'," +
            " 'version': '"+APP_BUILD_STREAM_VERSION+"',"+
            " 'nickName': 'Application Build Information',"+
            " 'description': 'This stream will store app build information to BAM',"+
            "   'payloadData':["+
            "    {'name':'applicationName','type':'string'},"+
            "    {'name':'applicationKey','type':'string'},"+
            "    {'name':'applicationVersion','type':'string'},"+
            "    {'name':'timeStamp','type':'double'},"+
            "    {'name':'tenantId', 'type':'string'},"+
            "    {'name':'status', 'type':'string'},"+
            "    {'name':'buildId', 'type':'string'},"+
            "    {'name':'revision', 'type':'string'},"+
            "    {'name':'user',  'type':'string' }"+
            "    ]"+
            "    }";

    private static Log log = LogFactory.getLog(BamDataPublisher.class);


    public BamDataPublisher(){
        try {
            AppFactoryConfiguration config = AppFactoryUtil.getAppfactoryConfiguration();
            String EnableStatPublishing = config.getFirstProperty("BAM.EnableStatPublishing");
            String bamServerURLS = config.getFirstProperty("BAM.BAMServerURL");

            // Check for multiple URLS separated by ","
            String bamServerURL[] = null;
            if(bamServerURLS != null){
                bamServerURL = bamServerURLS.split(",");
            }

            String bamServerUserName = config.getFirstProperty("BAM.BAMUserName");
            String bamServerPassword = config.getFirstProperty("BAM.BAMPassword");

            if (EnableStatPublishing != null && EnableStatPublishing.equals("true")) {
                ENABLE_DATA_PUBLISHING = true;

                if (bamServerURL == null || bamServerURLS.length() <= 0) {
                    throw new AppFactoryException("Can not find BAM Server URL");
                } else {
                    for (String url : bamServerURL) {
                        asyncDataPublisher = new AsyncDataPublisher(url,
                                bamServerUserName, bamServerPassword);

                    }
                }

            }

        } catch (AppFactoryException e) {
            String errorMsg = "Unable to create Data publisher " + e.getMessage();
            log.error(errorMsg, e);
        }
    }

    public void PublishAppCreationEvent(String appName, String appKey,
                                        String appDescription, String appType,
                                        String repoType, double timestamp, String tenantId,
                                        String username) throws AppFactoryException {

        if (!ENABLE_DATA_PUBLISHING) {
            return;
        }

        Event event = new Event();

        if(!asyncDataPublisher.isStreamDefinitionAdded(APP_CREATION_STREAM,
                APP_CREATION_STREAM_VERSION)) {

           asyncDataPublisher.addStreamDefinition(appCreationStream,
                   APP_CREATION_STREAM,APP_CREATION_STREAM_VERSION);
        }

        event.setTimeStamp(System.currentTimeMillis());
        event.setMetaData(null);
        event.setCorrelationData(null);
        event.setPayloadData(new Object[]{appName, appKey, timestamp, username, appType,
                repoType, appDescription, tenantId});

        try {

            publishEvents(event,APP_CREATION_STREAM,APP_CREATION_STREAM_VERSION);

        } catch (AgentException e) {
            String msg = "Failed to publish app creation event";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (InterruptedException e) {
            String msg = "Failed to publish app creation event";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
    }

    public void PublishAppVersionEvent(String appName, String appKey, double timestamp, String tenantId,
                                       String username , String appVersion, String stage) throws AppFactoryException{

        if (!ENABLE_DATA_PUBLISHING) {
            return;
        }

        Event event = new Event();
        if(!asyncDataPublisher.isStreamDefinitionAdded(APP_VERSION_STREAM,
                APP_VERSION_STREAM_VERSION)) {

            asyncDataPublisher.addStreamDefinition(appVersionStream,
                    APP_VERSION_STREAM, APP_VERSION_STREAM_VERSION);
        }

        event.setTimeStamp(System.currentTimeMillis());
        event.setMetaData(null);
        event.setCorrelationData(null);
        event.setPayloadData(new Object[]{appName, appKey,
                timestamp, tenantId, username, appVersion, stage});

        try {

            publishEvents(event, APP_VERSION_STREAM, APP_VERSION_STREAM_VERSION);

        } catch (AgentException e) {
            String msg = "Failed to publish app version creation event";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (InterruptedException e) {
            String msg = "Failed to publish app version creation event";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }

    }

    public void PublishTenantUserUpdateEvent(String tenantId, String username, String action,
                                             double timestamp) throws AppFactoryException {

        if (!ENABLE_DATA_PUBLISHING) {
            return;
        }

        Event event = new Event();
        if(!asyncDataPublisher.isStreamDefinitionAdded(TENANT_USER_STREAM,
                TENANT_USER_STREAM_VERSION)) {

            asyncDataPublisher.addStreamDefinition(tenantUserStream,
                    TENANT_USER_STREAM, TENANT_USER_STREAM_VERSION);
        }

        event.setTimeStamp(System.currentTimeMillis());
        event.setMetaData(null);
        event.setCorrelationData(null);
        event.setPayloadData(new Object[]{tenantId, username, action, timestamp});

        try {

            publishEvents(event, TENANT_USER_STREAM, TENANT_USER_STREAM_VERSION);

        } catch (AgentException e) {
            String msg = "Failed to publish tenant user update event";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (InterruptedException e) {
            String msg = "Failed to publish tenant user update event";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }

    }


    public void PublishUserUpdateEvent(String appName, String appKey, double timestamp, String tenantId, String action,
                                       String username) throws AppFactoryException{

        if (!ENABLE_DATA_PUBLISHING) {
            return;
        }

        Event event = new Event();
        if(!asyncDataPublisher.isStreamDefinitionAdded(APP_USER_STREAM,
                APP_USER_STREAM_VERSION)) {

            asyncDataPublisher.addStreamDefinition(appUserStream,
                    APP_USER_STREAM, APP_USER_STREAM_VERSION);
        }

        event.setTimeStamp(System.currentTimeMillis());
        event.setMetaData(null);
        event.setCorrelationData(null);
        event.setPayloadData(new Object[]{appName, appKey,
                timestamp, tenantId, username, action});

        try {

            publishEvents(event, APP_USER_STREAM, APP_USER_STREAM_VERSION);

        } catch (AgentException e) {
            String msg = "Failed to publish app user update event";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (InterruptedException e) {
            String msg = "Failed to publish app user update event";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }

    }
    
    public void PublishIssueEvent(String issueKey, String appName, String appKey, String appVersion, double timestamp, String tenantId,
                                       String issueType, String issuePriority, String issueStatus, String issueReporter,
                                       String issueAssignee, String action, String severeity, String createdTime, String updatedTime) throws AppFactoryException{

        if (!ENABLE_DATA_PUBLISHING) {
            return;
        }

        Event event = new Event();
        if(!asyncDataPublisher.isStreamDefinitionAdded(APP_ISSUE_STREAM,
                APP_ISSUE_STREAM_VERSION)) {

            asyncDataPublisher.addStreamDefinition(appIssueStream,
                    APP_ISSUE_STREAM, APP_ISSUE_STREAM_VERSION);
        }

        event.setTimeStamp(System.currentTimeMillis());
        event.setMetaData(null);
        event.setCorrelationData(null);
        event.setPayloadData(new Object[]{issueKey, appName, appKey, appVersion, 
                timestamp, tenantId, issueType, issuePriority, issueStatus,
                issueReporter, issueAssignee, action, severeity, createdTime, updatedTime});

        try {

            publishEvents(event, APP_ISSUE_STREAM, APP_ISSUE_STREAM_VERSION);

        } catch (AgentException e) {
            String msg = "Failed to publish issue event";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (InterruptedException e) {
            String msg = "Failed to publish issue event";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }

    }


    public void PublishBuildEvent(String appName, String appKey, String appVersion, double timestamp,
                                  String tenantId, String status, String buildId, String revision,
                                  String user) throws AppFactoryException {

        if (!ENABLE_DATA_PUBLISHING) {
            return;
        }

        Event event = new Event();
        if (!asyncDataPublisher.isStreamDefinitionAdded(APP_BUILD_STREAM,
                APP_BUILD_STREAM_VERSION)) {

            asyncDataPublisher.addStreamDefinition(appBuildStream,
                    APP_BUILD_STREAM, APP_BUILD_STREAM_VERSION);
        }

        event.setTimeStamp(System.currentTimeMillis());
        event.setMetaData(null);
        event.setCorrelationData(null);
        event.setPayloadData(new Object[]{appName, appKey, appVersion,
                timestamp, tenantId, status, buildId, revision, user});

        try {

            publishEvents(event, APP_BUILD_STREAM, APP_BUILD_STREAM_VERSION);

        } catch (AgentException e) {
            String msg = "Failed to publish build event";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (InterruptedException e) {
            String msg = "Failed to publish build event";
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
    }


    public void publishEvents(Event event,String Stream,
                              String version) throws AgentException, InterruptedException {

        asyncDataPublisher.publish(Stream,version,event);
        asyncDataPublisher.stop();

    }

}
