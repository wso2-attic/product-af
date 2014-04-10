/*
 * Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.appfactory.tenant.build.integration.buildserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.tenant.build.integration.BuildServerManagementException;

/**
 * Abstract class to represents build server application.
 */
public abstract class BuildServerApp {

	private static Log log = LogFactory.getLog(BuildServerApp.class);

	private String serverType = null;

	private File appFile = null;

	/**
	 * Constructor takes name and path of the build server app.
	 * 
	 * @param serverType
	 *            - name of the build server
	 * @param filePath
	 *            - path to the build server app
	 * @throws FileNotFoundException
	 *             if file not exist.
	 */
	public BuildServerApp(String serverType, String filePath) throws FileNotFoundException {
		File file = new File(filePath);
		if (file.exists()) {
			this.appFile = file;
			this.serverType = serverType;
		} else {
			String msg = "File does not exist - " + filePath;
			log.fatal(msg);
			throw new FileNotFoundException(msg);
		}
	}

	/**
	 * Returns the build server app file
	 * 
	 * @return build server app file
	 */
	public File getFile() {
		return this.appFile;
	}

	/**
	 * Returns the type of the build server.
	 * 
	 * @return build server type eg:- jenkins
	 */
	public String getServerType() {
		return this.serverType;
	}

	/**
	 * Implementation of this method defines how to modify the build server app
	 * based on the given {@code tenant}
	 * 
	 * @param tenant
	 * @return path of the modified build server app.
	 * @throws IOException
	 * @throws BuildServerManagementException
	 */
	public abstract String getModifiedAppPath(String tenant) throws IOException,
	                                                        BuildServerManagementException;

}
