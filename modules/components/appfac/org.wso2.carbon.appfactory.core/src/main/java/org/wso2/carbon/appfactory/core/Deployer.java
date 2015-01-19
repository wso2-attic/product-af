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

package org.wso2.carbon.appfactory.core;

import java.util.Map;

import org.wso2.carbon.appfactory.common.AppFactoryException;

public interface Deployer {

    public void deployTaggedArtifact(Map<String,String[]> requestParameters) throws Exception;

    public void deployLatestSuccessArtifact(Map<String,String[]> requestParameters) throws Exception;
    
    public void deployPromotedArtifact(Map<String,String[]> requestParameters) throws Exception;

    public void unDeployArtifact(Map<String,String[]> requestParameters) throws Exception;
    
    public void handleException(String msg, Exception e) throws AppFactoryException ;
    
}
