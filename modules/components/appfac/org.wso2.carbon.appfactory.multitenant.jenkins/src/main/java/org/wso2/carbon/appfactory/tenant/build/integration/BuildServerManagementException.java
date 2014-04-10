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
package org.wso2.carbon.appfactory.tenant.build.integration;

/**
 * General Exception to handle Build Server Management related exceptions.
 */
public class BuildServerManagementException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Represents diffent error codes related to Build Server Management
	 * 
	 */
	public enum Code {
		ERROR_CREATING_TENANT(1001), ERROR_CREATING_TENANT_APP(1002), INVALID_TANANT_DOMAIN(1003),
		TENANT_APP_ALREADY_EXISTS(1004),ERROR_WHILE_COPYING_CONFIG_TEMPLATES(1005),
        ERROR_WHILE_UPDATING_CONFIG_TEMPLATES(1006);
		int code;

		Code(int code) {
			this.code = code;
		}

		public int getCode() {
			return this.code;
		}
	}

	private Code code = null;

	public BuildServerManagementException() {
	}

	public BuildServerManagementException(String msg, Code code) {
		super(msg);
		this.code = code;
	}

	public BuildServerManagementException(String s, Throwable throwable, Code code) {
		super(s, throwable);
		this.code = code;
	}

	public int getExceptionCode() {
		return this.code.getCode();
	}

}
