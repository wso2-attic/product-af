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

package org.wso2.carbon.appfactory.core;

import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.dto.API;

public interface APIIntegration {

    /**
     * Method to create an application in the API Manager
     *
     * @param applicationId the id of the application to be created.
     * @return whether the application creation was successful or not
     */
    public boolean createApplication(String applicationId) throws AppFactoryException;

    /**
     * Method to remove an application from the API Manager
     *
     * @param applicationID the id of the application to be removed
     * @return whether the application removal was successful or not
     */
    public boolean removeApplication(String applicationID) throws AppFactoryException;

    /**
     * Method to add an API to an application
     *
     * @param applicationId the id of the application
     * @param apiName       the name of the API
     * @param apiVersion    the version of the API
     * @param apiProvider   the name of the API provider
     * @return whether the operation was successful or not
     */
    public boolean addAPIsToApplication(String applicationId, String apiName, String apiVersion, String apiProvider) throws AppFactoryException;

    /**
     * Method to get all the APIs of an application
     *
     * @param applicationId the id of the application
     * @return array of API info data objects which contain the name,version and the provider of the API
     */
    public API[] getAPIsOfApplication(String applicationId) throws AppFactoryException;

    /**
     * Method to remove an API from an application
     *
     * @param applicationId the id of the application
     * @param apiName       the name of the API
     * @param apiVersion    the version of the API
     * @param apiProvider   the name of the API provider
     * @return an array of API data objects
     */
    public boolean removeAPIFromApplication(String applicationId, String apiName, String apiVersion, String apiProvider) throws AppFactoryException;

    /**
     * Method to get the details of a single API
     *
     * @param name     the name of the API
     * @param version  the version of the API
     * @param provider the provider of the API
     * @return an API data object
     */
    public API getAPIInformation(String name, String version, String provider) throws AppFactoryException;
}
