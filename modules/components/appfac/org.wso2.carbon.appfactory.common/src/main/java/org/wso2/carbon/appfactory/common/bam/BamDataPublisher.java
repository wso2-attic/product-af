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

package org.wso2.carbon.appfactory.common.bam;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.agent.thrift.conf.AgentConfiguration;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.agent.thrift.lb.DataPublisherHolder;
import org.wso2.carbon.databridge.agent.thrift.lb.LoadBalancingDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.lb.ReceiverGroup;
import org.wso2.carbon.databridge.agent.thrift.util.DataPublisherUtil;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;

public class BamDataPublisher {

	private String APP_CREATION_STREAM = "org.wso2.carbon.appfactory.appCreation";
	private String APP_VERSION_STREAM = "org.wso2.carbon.appfactory.appVersion";
	private String TENANT_USER_STREAM = "org.wso2.carbon.appfactory.tenantUser";
	private String APP_USER_STREAM = "org.wso2.carbon.appfactory.appUser";
	private String APP_ISSUE_STREAM = "org.wso2.carbon.appfactory.appIssue";
	private String APP_BUILD_STREAM = "org.wso2.carbon.appfactory.appBuild";
	private String USER_ACTIVITY_STREAM = "org.wso2.carbon.appfactory.userActions";
	private String APP_CREATION_STREAM_VERSION = "1.0.0";
	private String APP_VERSION_STREAM_VERSION = "1.0.0";
	private String TENANT_USER_STREAM_VERSION = "1.0.0";
	private String APP_USER_STREAM_VERSION = "1.0.0";
	private String APP_ISSUE_STREAM_VERSION = "1.0.0";
	private String APP_BUILD_STREAM_VERSION = "1.0.0";
	private String USER_ACTIVITY_STREAM_VERSION = "1.0.0";
	private boolean ENABLE_DATA_PUBLISHING;

    private LoadBalancingDataPublisher loadBalancingDataPublisher;

	private String appCreationStream = "{"
			+ " 'name': '"
			+ APP_CREATION_STREAM
			+ "',"
			+ " 'version': '"
			+ APP_CREATION_STREAM_VERSION
			+ "',"
			+ " 'nickName': 'Application Creation Information',"
			+ " 'description': 'This stream will store app creation data to BAM',"
			+ "   'payloadData':["
			+ "    {'name':'applicationName','type':'string'},"
			+ "    {'name':'applicationKey','type':'string'},"
			+ "    {'name':'timeStamp','type':'double'},"
			+ "    {'name':'user',  'type':'string' },"
			+ "    {'name':'appType','type':'string' },"
			+ "    {'name':'repoType', 'type':'string'},"
			+ "    {'name':'appDescription', 'type':'string'},"
			+ "    {'name':'tenantId', 'type':'string'}" + "    ]" + "    }";

	private String appVersionStream = "{"
			+ " 'name': '"
			+ APP_VERSION_STREAM
			+ "',"
			+ " 'version': '"
			+ APP_VERSION_STREAM_VERSION
			+ "',"
			+ " 'nickName': 'Application Version Information',"
			+ " 'description': 'This stream will store app version and stage data to BAM',"
			+ "   'payloadData':["
			+ "    {'name':'applicationName','type':'string'},"
			+ "    {'name':'applicationKey','type':'string'},"
			+ "    {'name':'timeStamp','type':'double'},"
			+ "    {'name':'tenantId', 'type':'string'},"
			+ "    {'name':'user',  'type':'string' },"
			+ "    {'name':'appVersion','type':'string' },"
			+ "    {'name':'stage', 'type':'string'}" + "    ]" + "    }";

	private String tenantUserStream = "{" + " 'name': '" + TENANT_USER_STREAM
			+ "'," + " 'version': '" + TENANT_USER_STREAM_VERSION + "',"
			+ " 'nickName': 'Tenant User Information',"
			+ " 'description': 'This stream will store tenant users to BAM',"
			+ "   'payloadData':["
			+ "    {'name':'tenantId', 'type':'string'},"
			+ "    {'name':'user',  'type':'string' },"
			+ "    {'name':'action', 'type':'string'},"
			+ "    {'name':'timeStamp','type':'double'}" + "    ]" + "    }";

	private String appUserStream = "{" + " 'name': '" + APP_USER_STREAM + "',"
			+ " 'version': '" + APP_USER_STREAM_VERSION + "',"
			+ " 'nickName': 'Application User Information',"
			+ " 'description': 'This stream will store app users to BAM',"
			+ "   'payloadData':["
			+ "    {'name':'applicationName','type':'string'},"
			+ "    {'name':'applicationKey','type':'string'},"
			+ "    {'name':'timeStamp','type':'double'},"
			+ "    {'name':'tenantId', 'type':'string'},"
			+ "    {'name':'action', 'type':'string'},"
			+ "    {'name':'user',  'type':'string' }" + "    ]" + "    }";

	private String appIssueStream = "{" + " 'name': '"
			+ APP_ISSUE_STREAM
			+ "',"
			+ " 'version': '"
			+ APP_ISSUE_STREAM_VERSION
			+ "',"
			+ " 'nickName': 'Application Version Issue Information',"
			+ " 'description': 'This stream will store app version issues to BAM',"
			+ "   'payloadData':[" + "    {'name':'issueKey','type':'string'},"
			+ "    {'name':'applicationName','type':'string'},"
			+ "    {'name':'applicationKey','type':'string'},"
			+ "    {'name':'appVersion','type':'string'},"
			+ "    {'name':'timeStamp','type':'double'},"
			+ "    {'name':'tenantId', 'type':'string'},"
			+ "    {'name':'type',  'type':'string' }, "
			+ "    {'name':'priority',  'type':'string' }, "
			+ "    {'name':'status',  'type':'string' }, "
			+ "    {'name':'reporter',  'type':'string' }, "
			+ "    {'name':'assignee',  'type':'string' }, "
			+ "    {'name':'action',  'type':'string' }, "
			+ "    {'name':'severity',  'type':'string' }, "
			+ "    {'name':'createdTime',  'type':'string' }, "
			+ "    {'name':'updatedTime',  'type':'string' } " + "    ]"
			+ "    }";

	private String appBuildStream = "{"
			+ " 'name': '"
			+ APP_BUILD_STREAM
			+ "',"
			+ " 'version': '"
			+ APP_BUILD_STREAM_VERSION
			+ "',"
			+ " 'nickName': 'Application Build Information',"
			+ " 'description': 'This stream will store app build information to BAM',"
			+ "   'payloadData':["
			+ "    {'name':'applicationName','type':'string'},"
			+ "    {'name':'applicationKey','type':'string'},"
			+ "    {'name':'applicationVersion','type':'string'},"
			+ "    {'name':'timeStamp','type':'double'},"
			+ "    {'name':'tenantId', 'type':'string'},"
			+ "    {'name':'status', 'type':'string'},"
			+ "    {'name':'buildId', 'type':'string'},"
			+ "    {'name':'revision', 'type':'string'},"
			+ "    {'name':'user',  'type':'string' }" + "    ]" + "    }";

	private String userActivityStream = "{"
			+ " 'name': '"
			+ USER_ACTIVITY_STREAM
			+ "',"
			+ " 'version': '"
			+ USER_ACTIVITY_STREAM_VERSION
			+ "',"
			+ " 'nickName': 'Appfactory User Activity',"
			+ " 'description': 'This stream will store user activity',"
			+ "   'payloadData':["
			+ "    {'name':'item', 'type':'string'},"
			+ "    {'name':'action', 'type':'string'},"
			+ "    {'name':'timeStamp','type':'long'},"
			+ "    {'name':'user',  'type':'string' }," 
			+ "    {'name':'tenantId', 'type':'int'},"
			+ "    {'name':'applicationName','type':'string'},"
			+ "    {'name':'applicationKey','type':'string'},"
			+ "    {'name':'applicationVersion','type':'string'}"
			+ "    ]" + "    }";

	private static Log log = LogFactory.getLog(BamDataPublisher.class);

	private static BamDataPublisher bamDataPublisher = new BamDataPublisher();

	public static BamDataPublisher getInstance() {
		return bamDataPublisher;
	}

	private BamDataPublisher() {
		try {
			AppFactoryConfiguration config = AppFactoryUtil
					.getAppfactoryConfiguration();
			String EnableStatPublishing = config
					.getFirstProperty("BAM.EnableStatPublishing");


            //Adding client truststore configs since the authentication is done via secured port
            String CLIENT_TRUSTSTORE_LOCATION = "Security.TrustStore.Location";
            String CLIENT_TRUSTSTORE_TYPE = "Security.TrustStore.Type";
            String CLIENT_TRUSTSTORE_PASSWORD = "Security.TrustStore.Password";

            System.setProperty("javax.net.ssl.trustStore",
                    CarbonUtils.getServerConfiguration().getFirstProperty(CLIENT_TRUSTSTORE_LOCATION));
            System.setProperty("javax.net.ssl.trustStoreType",
                    CarbonUtils.getServerConfiguration().getFirstProperty(CLIENT_TRUSTSTORE_TYPE));
            System.setProperty("javax.net.ssl.trustStorePassword",
                    CarbonUtils.getServerConfiguration().getFirstProperty(CLIENT_TRUSTSTORE_PASSWORD));
            
			String bamServerURL = config.getFirstProperty("BAM.BAMServerURL");
			
						// Check for multiple URLS separated by ","
			
			String bamServerUserName = config
					.getFirstProperty("BAM.BAMUserName");
			String bamServerPassword = config
					.getFirstProperty("BAM.BAMPassword");

            ArrayList<ReceiverGroup> allReceiverGroups = new ArrayList<ReceiverGroup>();
            ArrayList<String> receiverGroupUrls = DataPublisherUtil.getReceiverGroups(bamServerURL);

            for(String aReceiverGroupURL : receiverGroupUrls){
                ArrayList<DataPublisherHolder> dataPublisherHolders =  new ArrayList<DataPublisherHolder>();
                String [] urls = aReceiverGroupURL.split(",");
                for(String aUrl : urls){
                    DataPublisherHolder aNode = new DataPublisherHolder(null, aUrl.trim(), bamServerUserName, bamServerPassword);
                    dataPublisherHolders.add(aNode);
                }
                ReceiverGroup group = new ReceiverGroup(dataPublisherHolders, false);
                allReceiverGroups.add(group);
            }

			if (EnableStatPublishing != null
					&& EnableStatPublishing.equals("true")) {
				
				if (bamServerURL == null) {
					log.error("BAM Server URL is not set");
					throw new RuntimeException("BAM Server URL is not set");
				}
				
				ENABLE_DATA_PUBLISHING = true;
                loadBalancingDataPublisher =  new LoadBalancingDataPublisher(allReceiverGroups);
			}

		} catch (AppFactoryException e) {
			String errorMsg = "Unable to create Data publisher "
					+ e.getMessage();
			log.error(errorMsg, e);
		}
	}

	public void PublishAppCreationEvent(String appName, String appKey,
			String appDescription, String appType, String repoType,
			double timestamp, String tenantId, String username)
			throws AppFactoryException {

		if (!ENABLE_DATA_PUBLISHING) {
			return;
		}

		Event event = new Event();

		if (!loadBalancingDataPublisher.isStreamDefinitionAdded(APP_CREATION_STREAM,
				APP_CREATION_STREAM_VERSION)) {

            loadBalancingDataPublisher.addStreamDefinition(appCreationStream,
					APP_CREATION_STREAM, APP_CREATION_STREAM_VERSION);
		}

		event.setTimeStamp(System.currentTimeMillis());
		event.setMetaData(null);
		event.setCorrelationData(null);
		event.setPayloadData(new Object[] { appName, appKey, timestamp,
				username, appType, repoType, appDescription, tenantId });

		try {

			publishEvents(event, APP_CREATION_STREAM,
					APP_CREATION_STREAM_VERSION);

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

	public void PublishAppVersionEvent(String appName, String appKey,
			double timestamp, String tenantId, String username,
			String appVersion, String stage) throws AppFactoryException {

		if (!ENABLE_DATA_PUBLISHING) {
			return;
		}

		Event event = new Event();
		if (!loadBalancingDataPublisher.isStreamDefinitionAdded(APP_VERSION_STREAM,
				APP_VERSION_STREAM_VERSION)) {

            loadBalancingDataPublisher.addStreamDefinition(appVersionStream,
					APP_VERSION_STREAM, APP_VERSION_STREAM_VERSION);
		}

		event.setTimeStamp(System.currentTimeMillis());
		event.setMetaData(null);
		event.setCorrelationData(null);
		event.setPayloadData(new Object[] { appName, appKey, timestamp,
				tenantId, username, appVersion, stage });

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

	public void PublishTenantUserUpdateEvent(String tenantId, String username,
			String action, double timestamp) throws AppFactoryException {

		if (!ENABLE_DATA_PUBLISHING) {
			return;
		}

		Event event = new Event();
		if (!loadBalancingDataPublisher.isStreamDefinitionAdded(TENANT_USER_STREAM,
				TENANT_USER_STREAM_VERSION)) {

            loadBalancingDataPublisher.addStreamDefinition(tenantUserStream,
					TENANT_USER_STREAM, TENANT_USER_STREAM_VERSION);
		}

		event.setTimeStamp(System.currentTimeMillis());
		event.setMetaData(null);
		event.setCorrelationData(null);
		event.setPayloadData(new Object[] { tenantId, username, action,
				timestamp });

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

	public void PublishUserUpdateEvent(String appName, String appKey,
			double timestamp, String tenantId, String action, String username)
			throws AppFactoryException {

		if (!ENABLE_DATA_PUBLISHING) {
			return;
		}

		Event event = new Event();
		if (!loadBalancingDataPublisher.isStreamDefinitionAdded(APP_USER_STREAM,
				APP_USER_STREAM_VERSION)) {

            loadBalancingDataPublisher.addStreamDefinition(appUserStream,
					APP_USER_STREAM, APP_USER_STREAM_VERSION);
		}

		event.setTimeStamp(System.currentTimeMillis());
		event.setMetaData(null);
		event.setCorrelationData(null);
		event.setPayloadData(new Object[] { appName, appKey, timestamp,
				tenantId, username, action });

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

	public void PublishIssueEvent(String issueKey, String appName,
			String appKey, String appVersion, double timestamp,
			String tenantId, String issueType, String issuePriority,
			String issueStatus, String issueReporter, String issueAssignee,
			String action, String severeity, String createdTime,
			String updatedTime) throws AppFactoryException {

		if (!ENABLE_DATA_PUBLISHING) {
			return;
		}

		Event event = new Event();
		if (!loadBalancingDataPublisher.isStreamDefinitionAdded(APP_ISSUE_STREAM,
				APP_ISSUE_STREAM_VERSION)) {

            loadBalancingDataPublisher.addStreamDefinition(appIssueStream,
					APP_ISSUE_STREAM, APP_ISSUE_STREAM_VERSION);
		}

		event.setTimeStamp(System.currentTimeMillis());
		event.setMetaData(null);
		event.setCorrelationData(null);
		event.setPayloadData(new Object[] { issueKey, appName, appKey,
				appVersion, timestamp, tenantId, issueType, issuePriority,
				issueStatus, issueReporter, issueAssignee, action, severeity,
				createdTime, updatedTime });

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

	public void PublishBuildEvent(String appName, String appKey,
			String appVersion, double timestamp, String tenantId,
			String status, String buildId, String revision, String user)
			throws AppFactoryException {

		if (!ENABLE_DATA_PUBLISHING) {
			return;
		}

		Event event = new Event();
		if (!loadBalancingDataPublisher.isStreamDefinitionAdded(APP_BUILD_STREAM,
				APP_BUILD_STREAM_VERSION)) {

            loadBalancingDataPublisher.addStreamDefinition(appBuildStream,
					APP_BUILD_STREAM, APP_BUILD_STREAM_VERSION);
		}

		event.setTimeStamp(System.currentTimeMillis());
		event.setMetaData(null);
		event.setCorrelationData(null);
		event.setPayloadData(new Object[] { appName, appKey, appVersion,
				timestamp, tenantId, status, buildId, revision, user });

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

	public void publishUserActivityEvents(int tenantId, String user,
			String[] activities) throws AppFactoryException {

		if (!ENABLE_DATA_PUBLISHING) {
			return;
		}

		if (!loadBalancingDataPublisher.isStreamDefinitionAdded(USER_ACTIVITY_STREAM,
				USER_ACTIVITY_STREAM_VERSION)) {

            loadBalancingDataPublisher.addStreamDefinition(userActivityStream,
					USER_ACTIVITY_STREAM, USER_ACTIVITY_STREAM_VERSION);
		}

		for (String activity : activities) {
			
			Event event = new Event();
			event.setTimeStamp(System.currentTimeMillis());
			event.setMetaData(null);
			event.setCorrelationData(null);

			try {

				JSONObject activityJSON = new JSONObject(activity);

				String item = activityJSON.getString("item");
				String action = activityJSON.getString("action");
				String timestampStr = activityJSON.getString("timestamp");
				
				Long timeStamp = new BigDecimal(timestampStr).longValue();
				
				if (log.isDebugEnabled()) {
				    log.debug("Recieved activity event :: " + activity);
					log.debug("Timestamp is :: " + timeStamp.toString());
				}
				
				
				String appName = (activityJSON.has("appName")) ? activityJSON.getString("appName") : null;
				String appKey = (activityJSON.has("appKey")) ? activityJSON.getString("appKey") : null;
				String appVersion = (activityJSON.has("appVersion")) ? activityJSON.getString("appVersion") : null;

				event.setPayloadData(new Object[] { item, action, timeStamp, user, tenantId, appName, appKey,
						appVersion});

                loadBalancingDataPublisher.publish(USER_ACTIVITY_STREAM, USER_ACTIVITY_STREAM_VERSION, event);

			} catch (AgentException e) {
				String msg = "Failed to publish build event";
				log.error(msg, e);
			} catch (JSONException e) {
				log.error("JSON parse error" + e.getMessage(), e);
			}
			
		}
	}

	public void publishEvents(Event event, String Stream, String version)
			throws AgentException, InterruptedException {
        loadBalancingDataPublisher.publish(Stream, version, event);
	}

}
