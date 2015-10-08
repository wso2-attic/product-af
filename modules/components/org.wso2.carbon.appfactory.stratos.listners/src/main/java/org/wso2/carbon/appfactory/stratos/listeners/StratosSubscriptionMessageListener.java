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
import org.apache.stratos.common.beans.application.signup.ApplicationSignUpBean;
import org.apache.stratos.common.beans.artifact.repository.ArtifactRepositoryBean;
import org.apache.stratos.common.client.StratosManagerServiceClient;
import org.apache.stratos.manager.service.stub.domain.application.signup.ApplicationSignUp;
import org.apache.stratos.manager.user.management.TenantUserRoleManager;
import org.apache.stratos.messaging.domain.application.Application;
import org.apache.stratos.messaging.message.receiver.application.ApplicationManager;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.beans.RuntimeBean;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.s4.integration.RepositoryProvider;
import org.wso2.carbon.appfactory.stratos.listeners.dto.RepositoryBean;
import org.wso2.carbon.appfactory.stratos.util.StratosUtils;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.stratos.common.exception.StratosException;
import org.wso2.carbon.tenant.mgt.util.TenantMgtUtil;

import javax.jms.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Listens to topic "subscribe_tenant_in_stratos" in the Message Broker and when a new message arrived, executes the
 * onMessage method. AUTO_ACKNOWLEDGE is used.
 * This class is set as the Message Listener in StratosSubscriptionDurableSubscriber class.
 */
public class StratosSubscriptionMessageListener implements MessageListener {
    private static Log log = LogFactory.getLog(StratosSubscriptionMessageListener.class);

    private TopicConnection topicConnection;
    private TopicSession topicSession;
    private TopicSubscriber topicSubscriber;
    private int currentMsgCount = 0;


    public StratosSubscriptionMessageListener(TopicConnection topicConnection, TopicSession topicSession,
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

        RuntimeBean[] runtimeBeans = null;
        TenantInfoBean tenantInfoBean = null;
        String[] stages = null;

        if (message instanceof MapMessage) {
            MapMessage mapMessage = ((MapMessage) message);
            try {
                String runtimesJson = mapMessage.getString(AppFactoryConstants.RUNTIMES_INFO);
                String tenantInfoJson = mapMessage.getString(AppFactoryConstants.TENANT_INFO);
                String stageJson = mapMessage.getString(AppFactoryConstants.STAGE);

                ObjectMapper mapper = new ObjectMapper();
                runtimeBeans = mapper.readValue(runtimesJson, RuntimeBean[].class);
                tenantInfoBean = mapper.readValue(tenantInfoJson, TenantInfoBean.class);
                stages = mapper.readValue(stageJson, String[].class);
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
		    PrivilegedCarbonContext.startTenantFlow();
		    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
				    tenantInfoBean.getTenantDomain(), true);
		    PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(
				    tenantInfoBean.getAdmin());
		    TenantMgtUtil.triggerAddTenant(tenantInfoBean);
	    } catch (StratosException e) {
		    String msg = "Error in notifying tenant addition.";
		    log.error(msg, e);
//		    throw new Exception(msg, e);

	    } finally {
		    PrivilegedCarbonContext.endTenantFlow();
	    }

        try {

	        //Notify tenant addition
//	        try {
//		        TenantMgtUtil.triggerAddTenant(tenantInfoBean);
//	        } catch (StratosException e) {
//		        String msg = "Error in notifying tenant addition.";
//		        log.error(msg, e);
//		        throw new Exception(msg, e);
//	        }

	        TenantUserRoleManager tenantUserRoleManager = new TenantUserRoleManager();
	        tenantUserRoleManager.onTenantCreate(tenantInfoBean);

            for (String stage : stages) {
                currentMsgCount++;
                for (RuntimeBean runtimeBean : runtimeBeans) {
                    RepositoryBean repositoryBean = createGitRepository(runtimeBean, tenantInfoBean, stage);

                    try {
                        PrivilegedCarbonContext.startTenantFlow();
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                                tenantInfoBean.getTenantDomain(), true);
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(
                                tenantInfoBean.getAdmin());
                        signUp(repositoryBean, runtimeBean, stage);
                    } finally {
                        PrivilegedCarbonContext.endTenantFlow();
                    }
                }
                //TODO remove this logsendPostRequest
                log.info("subscription done in environment : " + stage
                         + "of tenant :" + tenantInfoBean.getTenantDomain());
            }
        } catch (JMSException e) {
            String msg = "Can not read received map massage at count " + currentMsgCount;
            log.error(msg, e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            String msg = "Can not subscribe to stratos cartridge";
            log.error(msg, e);
            throw new RuntimeException(e);
        }

    }

    private RepositoryBean createGitRepository(RuntimeBean runtimeBean, TenantInfoBean tenantInfoBean, String stage)
            throws AppFactoryException {
        RepositoryBean repositoryBean = new RepositoryBean();
        repositoryBean.setCommitEnabled(true);
        repositoryBean.setRepositoryType(AppFactoryConstants.GIT_REPOSITORY_TYPE);
        repositoryBean.setRepositoryAdminUsername(AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty
                (AppFactoryConstants.PAAS_ARTIFACT_REPO_PROVIDER_ADMIN_USER_NAME));
        repositoryBean.setRepositoryAdminPassword(AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty
                (AppFactoryConstants.PAAS_ARTIFACT_REPO_PROVIDER_ADMIN_PASSWORD));
        String repoUrl;
        try {
            String repoProviderClassName = AppFactoryUtil.getAppfactoryConfiguration().
                    getFirstProperty(AppFactoryConstants.PAAS_ARTIFACT_REPO_PROVIDER_CLASS_NAME);
            ClassLoader loader = getClass().getClassLoader();
            Class<?> repoProviderClass = Class.forName(repoProviderClassName, true, loader);
            RepositoryProvider repoProvider = (RepositoryProvider) repoProviderClass.newInstance();
            repoProvider.setBaseUrl(AppFactoryUtil.getAppfactoryConfiguration().
                    getFirstProperty(AppFactoryConstants.PAAS_ARTIFACT_REPO_PROVIDER_BASE_URL));
            repoProvider.setAdminUsername(repositoryBean.getRepositoryAdminUsername());
            repoProvider.setAdminPassword(repositoryBean.getRepositoryAdminPassword());
            repoProvider.setRepoName(generateRepoUrlFromTemplate(
                    runtimeBean.getPaasRepositoryURLPattern(), tenantInfoBean.getTenantId(), stage));

            repoUrl = repoProvider.createRepository();
            repositoryBean.setRepositoryURL(repoUrl);
            log.info("Repo Url : " + repoUrl);
        } catch (InstantiationException e) {
            String msg = "Unable to create repository";
            throw new AppFactoryException(msg, e);
        } catch (IllegalAccessException e) {
            String msg = "Unable to create repository";
            throw new AppFactoryException(msg, e);
        } catch (AppFactoryException e) {
            String msg = "Unable to create repository";
            throw new AppFactoryException(msg, e);
        } catch (ClassNotFoundException e) {
            String msg = "PAAS artifact repository provider class not found";
            throw new AppFactoryException(msg, e);
        }
        return repositoryBean;
    }

    private String generateRepoUrlFromTemplate(String pattern, int tenantId,
                                               String stage) {
        String s = pattern.replace(AppFactoryConstants.STAGE_PLACE_HOLDER, stage) + File.separator
                   + Integer.toString(tenantId);
        log.info("Generated repo URL for stage " + stage + " : " + s);
        return s;

    }

    private static boolean signUp(RepositoryBean repositoryBean, RuntimeBean runtimeBean, String stage)
            throws Exception {

        String stratosAppId = runtimeBean.getStratosAppId() + stage.toLowerCase();
        String alias = runtimeBean.getCartridgeAliasPrefix() + stage.toLowerCase();

        ApplicationSignUpBean applicationSignUpBean = new ApplicationSignUpBean();
        List<ArtifactRepositoryBean> repo = new ArrayList<ArtifactRepositoryBean>();
        ArtifactRepositoryBean artifactRepositoryBean = new ArtifactRepositoryBean();
        artifactRepositoryBean.setRepoUrl(repositoryBean.getRepositoryURL());
        artifactRepositoryBean.setRepoUsername(repositoryBean.getRepositoryAdminUsername());
        artifactRepositoryBean.setRepoPassword(repositoryBean.getRepositoryAdminPassword());
        artifactRepositoryBean.setPrivateRepo(false);
        artifactRepositoryBean.setAlias(alias);
        repo.add(artifactRepositoryBean);
        applicationSignUpBean.setArtifactRepositories(repo);

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        ApplicationSignUp applicationSignUp = StratosUtils.convertApplicationSignUpBeanToStubApplicationSignUp(
                applicationSignUpBean);
        applicationSignUp.setApplicationId(stratosAppId);
        applicationSignUp.setTenantId(tenantId);
        Application application = ApplicationManager.getApplications().getApplication(stratosAppId);
        List<String> clusterIds = StratosUtils.findApplicationClusterIds(application);
        String[] clusterIdsArray = clusterIds.toArray(new String[clusterIds.size()]);
        applicationSignUp.setClusterIds(clusterIdsArray);

        // Encrypt artifact repository passwords
        StratosUtils.encryptRepositoryPasswords(applicationSignUp, application.getKey());

        StratosManagerServiceClient serviceClient = StratosManagerServiceClient.getInstance();
        serviceClient.addApplicationSignUp(applicationSignUp);

        if (log.isInfoEnabled()) {
            log.info(String.format("Application signup added successfully: [application-id] %s [tenant-id] %d",
                                   stratosAppId, tenantId));
        }
        serviceClient.notifyArtifactUpdatedEventForSignUp(stratosAppId, tenantId);
        if (log.isInfoEnabled()) {
            log.info(String.format("Artifact updated event sent: [application-id] %s [tenant-id] %d",
                                   stratosAppId, tenantId));
        }
        return true;
    }

}
