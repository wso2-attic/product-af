/*
 * Copyright 2015 WSO2, Inc. (http://wso2.com)
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.appfactory.integration.test.utils.rest;

import java.util.HashMap;
import java.util.Map;

public class GovernanceRestClient extends BaseRestClient{

    /**
     * Construct authenticates REST client to invoke appmgt functions
     *
     * @param backEndUrl backend url
     * @param username   username
     * @param password   password
     * @throws Exception
     */
    public GovernanceRestClient(String backEndUrl, String username, String password) throws Exception {
        super(backEndUrl, username, password);
    }

    public void Promote(String appKey, String stageName, String version, String tagName, String comment, String userName) throws Exception {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put("action", "Promote");
        msgBodyMap.put("appKey", appKey);
        msgBodyMap.put("stageName", stageName);
        msgBodyMap.put("version", version );
        msgBodyMap.put("tagName", tagName);
        msgBodyMap.put("comment", comment);
        msgBodyMap.put("userName", userName);
    }


   /** invokeDoVersion
            createArtifact
    invokeUpdateLifeCycleCheckList
            itemChecked
    getAppVersionsInStagesWithLifeCycleInfo
            getLifeCycleHistoryForApplication



    **/
}
