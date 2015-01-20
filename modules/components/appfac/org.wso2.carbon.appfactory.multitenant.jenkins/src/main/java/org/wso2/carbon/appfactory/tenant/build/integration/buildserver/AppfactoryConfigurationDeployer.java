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

package org.wso2.carbon.appfactory.tenant.build.integration.buildserver;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class AppfactoryConfigurationDeployer {
    private static final Log log = LogFactory.getLog(AppfactoryConfigurationDeployer.class);

	protected void copyBundledConfigs(String src, String dest) throws IOException {
		 
		 String configPath = src + File.separator + JenkinsTenantConstants.COMMON_CONFIGS_DIR;
		 File source = new File(configPath);
		 
		 if(!source.exists()){
			 throw new IllegalArgumentException("Common plugin location cannot be found at " + configPath );
		 }
		 
		 File destination = new File(dest, JenkinsTenantConstants.COMMON_CONFIG_DESTINATION);
		 
		 //Copy if doesnot exists
		 String files[] = source.list();
		 for (String file : files) {
			 File srcFile = new File(source, file);
			 File destFile = new File(destination, file);
			 if(!destFile.exists()){
				 FileUtils.copyFile(srcFile, destFile);
			 }
		 }
	 }

    /**
    * We are using this method to update the appfactory plugin configuration
    * When copying that plugin we need to change the Jenkins storage path and temp location for each tenant.
    *
    * */
    protected void updatePluginConfigs(String src,String tenantJenkinsHome) throws IOException{
        String storagePath =  tenantJenkinsHome + File.separator + "storage";
        String tempPath =  tenantJenkinsHome + File.separator + "temp";

        String configPath = src + File.separator + JenkinsTenantConstants.COMMON_CONFIGS_DIR + File.separator +
                "org.wso2.carbon.appfactory.jenkins.AppfactoryPluginManager.xml";
        File source = new File(configPath);

        String configFilePath = tenantJenkinsHome + File.separator + "org.wso2.carbon.appfactory.jenkins.AppfactoryPluginManager.xml";

        FileInputStream configStream = new FileInputStream(source);
        FileOutputStream outputStream = new FileOutputStream(new File(configFilePath));

        try {
            OMElement contentElement = new StAXOMBuilder(configPath).getDocumentElement();

            OMElement storageElement = contentElement.getFirstChildWithName(new QName("storagePath"));
            storageElement.setText(storagePath);

            OMElement tempElement = contentElement.getFirstChildWithName(new QName("tempPath"));
            tempElement.setText(tempPath);

            contentElement.serializeAndConsume(outputStream);

        } catch (XMLStreamException e) {
            String msg = "Unable to read the appfactory plugin configuration";
            log.error(msg,e);
            throw new IOException(msg,e);
        }finally {
            configStream.close();
            outputStream.close();
        }
    }
}
