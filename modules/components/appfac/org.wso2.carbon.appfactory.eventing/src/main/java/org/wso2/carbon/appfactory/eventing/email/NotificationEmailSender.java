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

package org.wso2.carbon.appfactory.eventing.email;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.eventing.AppFactoryEventException;
import org.wso2.carbon.appfactory.eventing.Event;
import org.wso2.carbon.appfactory.eventing.EventDispatcher;
import org.wso2.carbon.appfactory.utilities.services.EmailSenderService;
import org.wso2.carbon.email.verification.util.EmailVerifierConfig;

public class NotificationEmailSender implements EventDispatcher {
    Log log = LogFactory.getLog(NotificationEmailSender.class);

    @Override
    public void dispatchEvent(Event event) throws AppFactoryEventException {

        EmailSenderService emailSenderService = new EmailSenderService();
        try {
            String mailSubject = event.getMessageTitle();
            String mailBody = event.getMessageBody();
            String userName = event.getSender();
            String emailTemplate = "notification-email-config.xml";
            emailSenderService.sendMail(mailSubject, mailBody, emailTemplate, userName);
        } catch (AppFactoryException e) {
            log.error("Failed to send the email due to " + e.getMessage(), e);
            throw new AppFactoryEventException("Failed to send the email due to " + e.getMessage(), e);
        }
    }
}
