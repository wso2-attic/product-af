/*
 *
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * /
 */

package org.wso2.carbon.appfactory.application.mgt.type;

import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeValidationStatus;

import java.io.File;

/**
 * Uploaded Jaggery ApplicationType Processor
 */
public class UploadedJaggeryApplicationTypeProcessor extends UploadedApplicationTypeProcessor {

    UploadedJaggeryApplicationTypeProcessor(String type) {
        super(type);
    }

    public ApplicationTypeValidationStatus validate(String uploadedFileName) {
        File zipFile = new File(getUploadedApplicationTmpPath() + File.separator +
                                uploadedFileName);
        if(!zipFile.exists()) {
            return new ApplicationTypeValidationStatus(false ,"File does not exist");
        } else if(!zipFile.canRead()) {
            return new ApplicationTypeValidationStatus(false ,"File can\'t be read");
        }
        return new ApplicationTypeValidationStatus(true ,"Successfully Validated!");
    }
}
