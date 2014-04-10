/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.appfactory.core.util;

import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;

public class CommonUtil {
    public static String getAdminUsername() {
        return ServiceHolder.getAppFactoryConfiguration()
                .getFirstProperty(AppFactoryConstants.SERVER_ADMIN_NAME);
    }

    public static String getAdminUsername(String applicationId) {
        return ServiceHolder.getAppFactoryConfiguration()
                .getFirstProperty(AppFactoryConstants.SERVER_ADMIN_NAME) +
                "@" + applicationId;
    }

    public static String getServerAdminPassword() {
        return ServiceHolder.getAppFactoryConfiguration()
                .getFirstProperty(AppFactoryConstants.SERVER_ADMIN_PASSWORD);
    }
}
