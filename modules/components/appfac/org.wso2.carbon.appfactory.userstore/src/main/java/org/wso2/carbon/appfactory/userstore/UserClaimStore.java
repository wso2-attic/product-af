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
package org.wso2.carbon.appfactory.userstore;

import org.wso2.carbon.user.api.Claim;

/**
 * This interface is used to store additional claims of user.
 * Since we have provided a feature to connect to existing LDAP,
 * adding more user claims to that is not practical.
 */
public interface UserClaimStore {
    void addUserClaims(String username, Claim[] claims) throws Exception;

    Claim[] getUserClaims(String username) throws Exception;
    Claim getUserClaim(String username, String claimURI) throws Exception;
}
