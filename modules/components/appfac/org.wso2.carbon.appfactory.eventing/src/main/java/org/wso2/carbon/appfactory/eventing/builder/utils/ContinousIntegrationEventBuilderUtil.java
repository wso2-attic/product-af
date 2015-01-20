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

package org.wso2.carbon.appfactory.eventing.builder.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.eventing.Event;
import org.wso2.carbon.appfactory.eventing.Event.Category;
import org.wso2.carbon.appfactory.eventing.utils.EventingConstants;
import org.wso2.carbon.appfactory.eventing.utils.Util;


public class ContinousIntegrationEventBuilderUtil {


    private static Log log = LogFactory.getLog(ContinousIntegrationEventBuilderUtil.class);


    /**
     *
     * @param appId application key
     * @param repoForm whether the repo is master repo or the forked repo
     * @param buildTriggeredBy user who triggered the build
     * @param title notification title
     * @param description notification description
     * @param category notification category whether it is SUCCESS or ERROR
     * @param correlationKey correlation key that will comprise of application key, tenant domain, repo type (master/fork) and version
     * @return event that will be triggered when the build is started
     */
    public static Event buildTriggerBuildEvent(String appId, String repoForm, String buildTriggeredBy, String title, String description,
                                               Category category, String correlationKey) {
        Event event = new Event();
        String sender = Util.getSender(buildTriggeredBy);
        Event event1 = getDispatchTypesAndTargetForEvent(category, repoForm, appId, sender);
        Event.EventDispatchType[] eventDispatchTypes;
        eventDispatchTypes = event1.getEventDispatchTypes();
        event.setEventDispatchTypes(eventDispatchTypes);
        event.setTarget(event1.getTarget());
        event.setSender(sender);
        event.setCategory(category);
        event.setMessageTitle(title);
        event.setMessageBody(description);
        event.setType(EventingConstants.BUILD);
        event.setState(Event.State.START);
        event.setCorrelationKey(correlationKey);
        return event;
    }

    // When the build is successfully finished or failed
    public static Event buildContinuousIntegrationEvent(String appId, String repoForm, String title,
                                                        String description,
                                                        Category category,
                                                        String buildTriggeredBy, String correlationKey) {
        Event event = new Event();
        Event.EventDispatchType[] eventDispatchTypes;
        if (category == (Category.INFO)) {
            eventDispatchTypes =
                    new Event.EventDispatchType[]{Event.EventDispatchType.SOCIAL_ACTIVITY};
        } else {
            eventDispatchTypes =
                    new Event.EventDispatchType[]{
                            Event.EventDispatchType.SOCIAL_ACTIVITY,
                            Event.EventDispatchType.GUARANTEED_DELIVERY,
                            Event.EventDispatchType.EMAIL};
        }
        event.setEventDispatchTypes(eventDispatchTypes);
        String sender = Util.getSender(buildTriggeredBy);
        event.setSender(sender);
        event.setCategory(category);
        if (repoForm.equals(EventingConstants.ORIGINAL_REPO_FORM)) {
            event.setTarget(appId);
        } else {
            event.setTarget(appId + EventingConstants.FORK_USER_CONTEXT + sender);
        }
        // event.setTarget(appId);
        event.setMessageTitle(title);
        event.setMessageBody(description);
        event.setType(EventingConstants.BUILD);
        event.setState(Event.State.COMPLETE);
        event.setCorrelationKey(correlationKey);
        return event;
    }


    /**
     *
     * @param appId applicationKey
     * @param changedBy user who changed the auto build status
     * @param title notification title
     * @param description notification description
     * @param category notification category whether it is SUCCESS or ERROR
     * @return event that will be triggered when the auto build status is changed
     */
    public static Event autoBuildStatusChangeEvent(String appId, String changedBy, String title, String description, String category) {
        Category eventStatus;
        if ("INFO".equals(category)) {
            eventStatus = Category.INFO;
        } else {
            eventStatus = Category.ERROR;
        }
        Event event = new Event();
        Event.EventDispatchType[] eventDispatchTypes =
                new Event.EventDispatchType[]{
                        Event.EventDispatchType.SOCIAL_ACTIVITY
                };
        event.setEventDispatchTypes(eventDispatchTypes);
        event.setSender(changedBy);
        event.setCategory(eventStatus);
        event.setTarget(appId);
        event.setMessageTitle(title);
        event.setMessageBody(description);

        return event;
    }


    /**
     *
     * @param appId applicationKey
     * @param changedBy user who changed the auto build status
     * @param title notification title
     * @param description notification description
     * @param category notification category whether it is SUCCESS or ERROR
     * @return event that will be triggered when the auto deploy status is changed
     */
    public static Event autoDeployStatusChangeEvent(String appId, String changedBy, String title, String description, String category) {
        Category eventStatus;
        if ("INFO".equals(category)) {
            eventStatus = Category.INFO;
        } else {
            eventStatus = Category.ERROR;
        }
        Event event = new Event();
        Event.EventDispatchType[] eventDispatchTypes =
                new Event.EventDispatchType[]{
                        Event.EventDispatchType.SOCIAL_ACTIVITY
                };
        event.setEventDispatchTypes(eventDispatchTypes);
        event.setSender(changedBy);
        event.setCategory(eventStatus);
        event.setTarget(appId);
        event.setMessageTitle(title);
        event.setMessageBody(description);

        return event;
    }

    /**
     *
     * @param appId applicationKey
     * @param tenantDomain tenant domain
     * @param title notification title
     * @param description notification description
     * @param category notification category whether it is SUCCESS or ERROR
     * @param correlationKey correlation key that will comprise of application key, tenant domain, stage and version
     * @return the event that will be triggered when a webapp/jaxrs/jaxws/jaggery app is deployed
     */
    public static Event buildObtainWarDeploymentStatusEvent(String appId, String tenantDomain,
                                                            String title, String description,
                                                            Category category, String correlationKey) {
        Event event = new Event();
        Event.EventDispatchType[] eventDispatchTypes;
        if (category == Category.INFO) {
            eventDispatchTypes =
                    new Event.EventDispatchType[]{Event.EventDispatchType.SOCIAL_ACTIVITY};
        } else {
            eventDispatchTypes =
                    new Event.EventDispatchType[]{
                            Event.EventDispatchType.SOCIAL_ACTIVITY,
                            Event.EventDispatchType.GUARANTEED_DELIVERY};
        }

        event.setEventDispatchTypes(eventDispatchTypes);
        String sender = Util.getSender();
        event.setSender(sender);
        event.setCategory(category);
        event.setTarget(appId);
        event.setMessageBody(description);
        event.setMessageTitle(title);
        event.setType(EventingConstants.DEPLOY);
        event.setState(Event.State.COMPLETE);
        event.setCorrelationKey(correlationKey);
        return event;
    }


    /**
     *
     * @param appId application key
     * @param title notification title
     * @param description notification description
     * @param status notification category whether it is SUCCESS or ERROR
     * @param correlationKey comprise of application key, stage and version name
     * @return event that will be triggered when a data service(.dbs) is deployed in the application server
     */
    public static Event buildObtainDbsDeploymentStatusEvent(String appId, String title,
                                                            String description,
                                                            Category status, String correlationKey) {
        Event event = new Event();
        Event.EventDispatchType[] eventDispatchTypes;

        if (status == Category.INFO) {
            eventDispatchTypes =
                    new Event.EventDispatchType[]{Event.EventDispatchType.SOCIAL_ACTIVITY};
        } else {

            eventDispatchTypes =
                    new Event.EventDispatchType[]{
                            Event.EventDispatchType.SOCIAL_ACTIVITY,
                            Event.EventDispatchType.GUARANTEED_DELIVERY};
        }

        event.setEventDispatchTypes(eventDispatchTypes);
        String sender = Util.getSender();
        event.setSender(sender);
        event.setCategory(status);
        event.setTarget(appId);
        event.setMessageBody(description);
        event.setMessageTitle(title);
        event.setType(EventingConstants.DEPLOY);
        event.setState(Event.State.COMPLETE);
        event.setCorrelationKey(correlationKey);
        return event;
    }


    /**
     *
     * @param appId application key
     * @param tenantDomain tenant domain
     * @param title notification title
     * @param description notification description
     * @param correlationKey comprise of application key, stage and version name
     * @return event that will be triggered when the deployment is successfully started
     */
    public static Event buildApplicationDeployementStartedEvent(String appId, String tenantDomain,
                                                                String title, String description, String correlationKey) {
        Event event = new Event();
        Event.EventDispatchType[] eventDispatchTypes =
                new Event.EventDispatchType[]{Event.EventDispatchType.SOCIAL_ACTIVITY};

        event.setEventDispatchTypes(eventDispatchTypes);
        event.setSender(Util.getSender());
        event.setCategory(Category.INFO);
        event.setTarget(appId);
        event.setMessageBody(description);
        event.setMessageTitle(title);
        event.setType(EventingConstants.DEPLOY);
        event.setState(Event.State.START);
        event.setCorrelationKey(correlationKey);

        return event;
    }


    /**
     * This method is invoked when a git commit is done via notifycommits.groovy hook
     *
     * @param appId applicationKey
     * @param user username of the user who did the commit
     * @param title notification title
     * @param description in this case commit message
     * @return event that will be triggered when a commit is done by a user
     */
    public static Event buildPostCommitEvents(String appId, String user, String title, String description) {
        Event event = new Event();
        Event.EventDispatchType[] eventDispatchTypes =
                new Event.EventDispatchType[]{Event.EventDispatchType.SOCIAL_ACTIVITY};

        event.setEventDispatchTypes(eventDispatchTypes);
        event.setSender(user);
        event.setCategory(Category.ACTION);
        event.setTarget(appId);
        event.setMessageBody(description);
        event.setMessageTitle(title);

        return event;
    }


    /**
     *
     * @param category  notification category whether it is SUCCESS or ERROR
     * @param repoForm whether the repo is master repo or the forked repo
     * @param appId application key
     * @param sender user who triggered the build
     * @return event object with the dispatch type and target
     */
    public static Event getDispatchTypesAndTargetForEvent(Event.Category category, String repoForm, String appId, String sender) {
        Event event = new Event();


        //if the main repo build start successful
        if (category == Event.Category.INFO  & (EventingConstants.ORIGINAL_REPO_FORM).equals(repoForm)) {
            Event.EventDispatchType[] eventDispatchTypes =
                    new Event.EventDispatchType[]{Event.EventDispatchType.SOCIAL_ACTIVITY};
            event.setTarget(appId);
            event.setEventDispatchTypes(eventDispatchTypes);

        // if the forked repo build start successful
        } else if (category == Event.Category.INFO & (EventingConstants.FORKED_REPO_FORM).equals(repoForm)) {
            Event.EventDispatchType[] eventDispatchTypes =
                    new Event.EventDispatchType[]{Event.EventDispatchType.SOCIAL_ACTIVITY};
            event.setTarget(appId + EventingConstants.FORK_USER_CONTEXT + sender);
            event.setEventDispatchTypes(eventDispatchTypes);

        // if the main repo build did not start
        } else if (category == Event.Category.ERROR & (EventingConstants.ORIGINAL_REPO_FORM).equals(repoForm)) {
            Event.EventDispatchType[] eventDispatchTypes =
                    new Event.EventDispatchType[]{
                            Event.EventDispatchType.SOCIAL_ACTIVITY,
                            Event.EventDispatchType.GUARANTEED_DELIVERY,
                            Event.EventDispatchType.EMAIL};
            event.setTarget(appId);
            event.setEventDispatchTypes(eventDispatchTypes);

        // if the fork repo build did not start
        } else {
            Event.EventDispatchType[] eventDispatchTypes =
                    new Event.EventDispatchType[]{
                            Event.EventDispatchType.SOCIAL_ACTIVITY,
                            Event.EventDispatchType.GUARANTEED_DELIVERY,
                            Event.EventDispatchType.EMAIL};
            event.setTarget(appId + EventingConstants.FORK_USER_CONTEXT + sender);
            event.setEventDispatchTypes(eventDispatchTypes);
        }
        return event;
    }

}
