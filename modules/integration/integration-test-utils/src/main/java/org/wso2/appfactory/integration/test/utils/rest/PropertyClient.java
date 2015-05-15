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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.httpclient.HttpStatus;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestException;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import java.util.HashMap;
import java.util.Map;


public class PropertyClient extends BaseClient {

    private static final String ACTION="action";
    private static final String APPLICATION_KEY="applicationKey";
    private static final String RESOURCE_NAME="resourceName";
    private static final String RESOURCE_DESCRIPTION="resourceDescription";
    private static final String RESOURCE_MEDEA_TYPE="resourceMediaType";
    private static final String CONTENT_VALUE="contentValue";
    private static final String STAGE="stage";
    private static final String COPY_TO_ALL="copyToAll";
    private static final String DEPLOYMENT_STAGE="deploymentStage";
    private static final String DESCRIPTION="description";
    /**
     * Construct authenticates REST client to invoke appmgt functions
     *
     * @param backEndUrl backend url
     * @param username   username
     * @param password   password
     * @throws Exception
     */
    public PropertyClient(String backEndUrl, String username, String password) throws Exception {
        super(backEndUrl, username, password);
    }
    /**
     *Create a property to the application
     *
     * @param applicationKey applicationKey
     * @throws AFIntegrationTestException
     */
    public boolean createResource(String action,String applicationKey,String resourceName,String resourceDesc,
                                  String resourceMediaType,String contentValue,String stage,String isCopyToAll)
            throws AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(ACTION, action);
        msgBodyMap.put(APPLICATION_KEY, applicationKey);
        msgBodyMap.put(RESOURCE_NAME, resourceName);
        msgBodyMap.put(RESOURCE_DESCRIPTION, resourceDesc);
        msgBodyMap.put(RESOURCE_MEDEA_TYPE, resourceMediaType);
        msgBodyMap.put(CONTENT_VALUE, contentValue);
        msgBodyMap.put(STAGE, stage);
        msgBodyMap.put(COPY_TO_ALL, isCopyToAll);

        HttpResponse response = super.doPostRequest(ADD_PROPERTY_URL, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            boolean isResourseCreate=getResource(applicationKey,resourceName,resourceDesc,contentValue,
                    resourceMediaType,stage);
            if(isResourseCreate){
                return true;
            }
            return false;
        }else{
            throw new AFIntegrationTestException("Application Promotion failed " + response.getResponseCode() +
                    response.getData());
        }
    }
    /**
     *Delete a property of the application
     *
     * @param applicationKey applicationKey
     * @throws AFIntegrationTestException
     */
    public boolean deleteResource(String action,String applicationKey,String resourceName,String resourceDesc,
                                  String resourceMediaType,String contentValue,String stage)
            throws AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(ACTION, action);
        msgBodyMap.put(APPLICATION_KEY, applicationKey);
        msgBodyMap.put(RESOURCE_NAME, resourceName);
        msgBodyMap.put(RESOURCE_DESCRIPTION, resourceDesc);
        msgBodyMap.put(RESOURCE_MEDEA_TYPE, resourceMediaType);
        msgBodyMap.put(CONTENT_VALUE, contentValue);
        msgBodyMap.put(STAGE, stage);
        HttpResponse response = super.doPostRequest(ADD_PROPERTY_URL, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            if(response.getData().equals(null))
                return true;
        } else {
            throw new AFIntegrationTestException("Application Promotion failed " + response.getResponseCode() +
                    response.getData());
        }
    return false;

    }
    /**
     * Update Description of a property in the application
     *
     * @param applicationKey applicationKey
     * @throws AFIntegrationTestException
     */
    public void updateDescription(String applicationKey) throws AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(ACTION, "updateDescription");
        msgBodyMap.put(APPLICATION_KEY, applicationKey);
        msgBodyMap.put(RESOURCE_NAME, "resource2");
        msgBodyMap.put(RESOURCE_DESCRIPTION, "updated_desc");
        msgBodyMap.put(RESOURCE_MEDEA_TYPE, "Registry");
        msgBodyMap.put(DEPLOYMENT_STAGE, "Development");
        HttpResponse response = super.doPostRequest(UPDATE_DESC_URL, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            //TODO
            return;
        } else {
            throw new AFIntegrationTestException("Application Promotion failed " + response.getResponseCode() +
                    response.getData());
        }

    }
    /**
     * Update Resources of the property in the application
     *
     * @param applicationKey applicationKey
     * @throws AFIntegrationTestException
     */

    public boolean updateResource(String action,String applicationKey,String resourceName,String resourceDesc,
                                  String resourceMediaType,String contentValue,String stage)
            throws AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(ACTION, action);
        msgBodyMap.put(APPLICATION_KEY, applicationKey);
        msgBodyMap.put(RESOURCE_NAME, resourceName);
        msgBodyMap.put(DESCRIPTION, resourceDesc);
        msgBodyMap.put(RESOURCE_MEDEA_TYPE, resourceMediaType);
        msgBodyMap.put(DEPLOYMENT_STAGE, stage);
        msgBodyMap.put(CONTENT_VALUE, contentValue);
        HttpResponse response = super.doPostRequest(UPDATE_DESC_URL, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            boolean isResourseCreate=getResource(applicationKey, resourceName, resourceDesc, contentValue,
                    resourceMediaType, stage);
            if(isResourseCreate)
                return true;

        } else {
            throw new AFIntegrationTestException("Application Promotion failed " + response.getResponseCode() +
                    response.getData());
        }
        return false;
    }

    /**
     * Get All dependencies of the property in the application
     *
     * @param applicationKey applicationKey
     * @throws AFIntegrationTestException
     */
    public boolean getAllDependencies(String action,String applicationKey,String resourceName)
            throws AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(ACTION, "getAllDependencies");
        msgBodyMap.put(APPLICATION_KEY, applicationKey);

        HttpResponse response = super.doPostRequest(GET_RESOURCE_URL, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            String  stringResponse = response.getData();
            JsonParser jsonParser = new JsonParser();
            JsonElement jsonElement = jsonParser.parse(stringResponse);
            JsonObject addIssueResponse = jsonElement.getAsJsonObject();
            if(addIssueResponse.has(resourceName)){
                return true;
            }
            return false;

        } else {
            throw new AFIntegrationTestException("Application Promotion failed " + response.getResponseCode() +
                    response.getData());
        }
    }
    /**
     * Get Resources of the property in the application
     *
     * @param applicationKey applicationKey
     * @param resourceName resourceName
     * @param description description
     * @param value value
     * @param mediaType mediaType
     * @throws AFIntegrationTestException
     */

    public boolean getResource(String applicationKey,String resourceName,String description,String value,
                               String mediaType,String stage)
            throws AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(ACTION, "getResource");
        msgBodyMap.put(APPLICATION_KEY, applicationKey);
        msgBodyMap.put(RESOURCE_NAME, resourceName);
        msgBodyMap.put(STAGE, stage);


        HttpResponse response = super.doPostRequest(GET_RESOURCE_URL, msgBodyMap);

        if (response.getResponseCode() == HttpStatus.SC_OK) {
            try {
                JsonParser jsonParser = new JsonParser();
                JsonElement jsonElement = jsonParser.parse(response.getData());
                JsonObject editResponse = jsonElement.getAsJsonObject();
                if (editResponse.getAsJsonObject().get("name").getAsString().equals(resourceName) &&
                        editResponse.getAsJsonObject().get("description").getAsString().equals(description) &&
                        editResponse.getAsJsonObject().get("value").getAsString().equals(value) &&
                        editResponse.getAsJsonObject().get("mediaType").getAsString().equals(mediaType)) {
                    return true;
                }
            }catch (Exception e){
                return false;
            }

        } else {
            throw new AFIntegrationTestException("Application Promotion failed " + response.getResponseCode() +
                    response.getData());
        }
        return false;
    }
    /**
     * Get Stages of the property in the application
     *
     * @param applicationKey applicationKey
     * @throws AFIntegrationTestException
     */
    public void propertyExistInStages(String applicationKey) throws AFIntegrationTestException {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put("action", "getResource");
        msgBodyMap.put("applicationKey", applicationKey);
        msgBodyMap.put("propertyName", "");
        msgBodyMap.put("givenValues", "");
        msgBodyMap.put("copyToAll", "");
        msgBodyMap.put("::stage:", "");

        HttpResponse response = super.doPostRequest(GET_RESOURCE_URL, msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            //TODO
            return;
        } else {
            throw new AFIntegrationTestException("Application Promotion failed " + response.getResponseCode() +
                    response.getData());
        }

    }

}
