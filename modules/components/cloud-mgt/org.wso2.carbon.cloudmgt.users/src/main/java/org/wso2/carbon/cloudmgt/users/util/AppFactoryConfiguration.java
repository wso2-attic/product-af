/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.cloudmgt.users.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppFactoryConfiguration {
	 private Map<String, List<String>> configuration;

	    public AppFactoryConfiguration(Map<String, List<String>> config) {
	        configuration = new HashMap<String, List<String>>();
	        configuration.putAll(config);
	    }

	    public String[] getProperties(String key) {
	        List<String> values = configuration.get(key);
	        if (values == null) {
	            return new String[0];
	        }
	        return values.toArray(new String[values.size()]);
	    }
	    //todo: fix this
	    public String[] getPropertiesAsNativeArray(String key) {
	        List<String> values = configuration.get(key);
	        if (values == null) {
	            return new String[0];
	        }
	        return values.toArray(new String[values.size()]);
	    }

	    public String getFirstProperty(String key) {
	        List<String> value = configuration.get(key);
	        if (value == null) {
	            return null;
	        }
	        return value.get(0);
	    }

	    public Map<String,List<String>> getAllProperties(){
	        return configuration;
	    }
}
