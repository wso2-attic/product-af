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

package org.wso2.carbon.cloudmgt.hostobjects.configuration;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.cloudmgt.users.util.AppFactoryConfigReader;
import org.wso2.carbon.cloudmgt.users.util.AppFactoryConfiguration;

public class AppFactoryConfigurationReader extends ScriptableObject {
	  private static final String hostObjectName = "AppFactoryConfigurationReader";
	    @Override
	    public String getClassName() {
	        return hostObjectName;
	    }

	    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj,
	                                           boolean inNewExpr) throws Exception {
	        return new AppFactoryConfigurationReader();
	    }

	    public String jsFunction_getFirstProperty(String key) throws Exception {
	    	AppFactoryConfiguration appFactoryConfiguration = AppFactoryConfigReader.getAppfactoryConfiguration();
	        return appFactoryConfiguration.getFirstProperty(key);
	    }

	    public NativeArray jsFunction_getProperties(String key) throws Exception {
	    	AppFactoryConfiguration appFactoryConfiguration = AppFactoryConfigReader.getAppfactoryConfiguration();
	        String[] values = appFactoryConfiguration.getPropertiesAsNativeArray(key);
	        NativeArray array = new NativeArray(values.length);
	        for (int i = 0; i < values.length; i++) {
	            String element = values[i];
	            array.put(i, array, element);
	        }
	        return array;
	    }

	    public NativeArray jsFunction_getAllPropertyNames() throws Exception{
	    	AppFactoryConfiguration appFactoryConfiguration = AppFactoryConfigReader.getAppfactoryConfiguration();
	        Map<String,List<String>> allProperties  = appFactoryConfiguration.getAllProperties();

	        Set<String> keySet = allProperties.keySet();
	        String[] keys = keySet.toArray(new String[keySet.size()]);

	        NativeArray array = new NativeArray(keys.length);

	        for (int i = 0; i < keys.length; i++) {
	            String key = keys[i];
	            array.put(i,array,key);
	        }
	        return array;
	    }
}
