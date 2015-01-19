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
package org.wso2.carbon.appfactory.application.mgt.eta;

import java.io.Serializable;


public class ETAValue implements Serializable{

	private final String applicationKey;
	
	private final String stage;
	 
	private final String version;
	
	private final String user;
	
	private final String etaFrom;
	
	private final String etaTo;
	
	public ETAValue(String applicationKey, String stage, String version, String user,
                    String etaFrom, String etaTo) {
	    super();
	    this.applicationKey = applicationKey;
	    this.stage = stage;
	    this.version = version;
	    this.user = user;
	    this.etaFrom = etaFrom;
	    this.etaTo = etaTo;
    }

	public String getApplicationKey() {
		return applicationKey;
	}

	public String getVersion() {
		return version;
	}

	public String getUser() {
		return user;
	}

	public String getEtaFrom() {
		return etaFrom;
	}

	public String getEtaTo() {
		return etaTo;
	}

	/**
	 * @return the stage
	 */
    public String getStage() {
	    return stage;
    }

	
	
}
