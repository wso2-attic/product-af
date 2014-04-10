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

package org.wso2.carbon.appfactory.userstore.internal;

import org.wso2.carbon.appfactory.userstore.UserClaimStore;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryService;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.user.api.Claim;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class RegistryBasedUserClaimStore implements UserClaimStore {
    public static final String REGISTRY_USER_CLAIMS_PATH = "/repository/users";
    private static final String AT_SYMBOL_REPLACE_STRING = "AT_SYMBOL";

    @Override
    public void addUserClaims(String username, Claim[] claims) throws Exception {
        RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
        Registry userRegistry = registryService.getGovernanceSystemRegistry();
        username = username.replaceAll("@", AT_SYMBOL_REPLACE_STRING);
        String userInfoResourcePath = REGISTRY_USER_CLAIMS_PATH +
                                      RegistryConstants.PATH_SEPARATOR + username +
                                      RegistryConstants.PATH_SEPARATOR + "claims";
        if (!userRegistry.resourceExists(userInfoResourcePath)) {
            Resource userInfoResource = userRegistry.newResource();
            userRegistry.put(userInfoResourcePath, userInfoResource);
        }

        Resource userInfoResource = userRegistry.get(userInfoResourcePath);
        for (Claim claim : claims) {
            String check = userInfoResource.getProperty(claim.getClaimUri());
            if (check != null){
               userInfoResource.removeProperty(claim.getClaimUri());
            }

            userInfoResource.addProperty(claim.getClaimUri(), claim.getValue());
        }
        userRegistry.put(userInfoResourcePath, userInfoResource);
   }

    @Override
    public Claim[] getUserClaims(String username) throws Exception {
        RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
        Registry userRegistry = registryService.getGovernanceSystemRegistry();
        username = username.replaceAll("@", AT_SYMBOL_REPLACE_STRING);
        String userInfoResourcePath = REGISTRY_USER_CLAIMS_PATH +
                                      RegistryConstants.PATH_SEPARATOR + username +
                                      RegistryConstants.PATH_SEPARATOR + "claims";
        if (!userRegistry.resourceExists(userInfoResourcePath)) {
            throw new Exception("No claims are stored for user:" + username);
        }

        Resource userInfoResource = userRegistry.get(userInfoResourcePath);
        List<Claim> claimList = new ArrayList<Claim>();
        Properties properties = userInfoResource.getProperties();
        for (Map.Entry entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            Object value = entry.getValue();
            if (value instanceof ArrayList) {
                List<String> values = (ArrayList<String>) value;
                if (value != null && !values.isEmpty()) {
                    Claim claim = new Claim();
                    claim.setClaimUri(key);
                    claim.setValue(values.get(0));
                    claimList.add(claim);
                }
            }
        }
        return claimList.toArray(new Claim[claimList.size()]);
    }

    @Override
    public Claim getUserClaim(String username, String claimURI) throws Exception {
        RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
        Registry userRegistry = registryService.getGovernanceSystemRegistry();
        username = username.replaceAll("@", AT_SYMBOL_REPLACE_STRING);
        String userInfoResourcePath = REGISTRY_USER_CLAIMS_PATH +
                                      RegistryConstants.PATH_SEPARATOR + username +
                                      RegistryConstants.PATH_SEPARATOR + "claims";
        if (!userRegistry.resourceExists(userInfoResourcePath)) {
            throw new Exception("No claims are stored for user:" + username);
        }

        Resource userInfoResource = userRegistry.get(userInfoResourcePath);
        Properties properties = userInfoResource.getProperties();
        for (Map.Entry entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            Object value = entry.getValue();
            if (value instanceof ArrayList) {
                List<String> values = (ArrayList<String>) value;
                if (value != null && !values.isEmpty()) {
                    Claim claim = new Claim();
                    claim.setClaimUri(key);
                    claim.setValue(values.get(0));
                    return claim;
                }
            }
        }
        return null;
    }

}